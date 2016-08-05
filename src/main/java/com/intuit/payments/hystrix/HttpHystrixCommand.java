/**
 * Copyright 2016 Intuit Inc. All rights reserved. Unauthorized reproduction
 * is a violation of applicable law. This material contains certain
 * confidential or proprietary information and trade secrets of Intuit Inc.
 */
package com.intuit.payments.hystrix;

import com.intuit.payments.hystrix.util.Util;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.netflix.hystrix.HystrixCommandProperties.Setter;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;

/**
 * HystrixCommand extension to support JSON Http Client.
 *
 *
 * @see <a href="https://hc.apache.org/httpcomponents-client-ga/tutorial/html/fluent.html">HttpClient's fluent API</a>
 * @link HystrixCommand
 * @author saung
 * @since 6/16/16
 */
public class HttpHystrixCommand extends HystrixCommand<Map<String, Object>> {
    /** Logger instance */
    private static final Logger LOG = LoggerFactory.getLogger(HttpHystrixCommand.class);

    /**
     * Additional HTTP Request Header to track network latency between this client and target server
     */
    private static final String X_REQUEST_SENT_AT = "x-request-sent-at";

    /**
     * HTTP Response headers included in the response map.
     */
    private static final String HTTP_STATUS_CODE = "_http_status_code";
    private static final String HTTP_STATUS_REASON = "_http_status_reason";
    /**
     * This value is only set when the response string was invalid JSON.
     */
    private static final String HTTP_RAW_RESPONSE = "_http_raw_response";

    /**
     * Array of {@link org.apache.http.Header} instances.
     */
    private static final String HTTP_RESPONSE_HEADERS = "_http_response_headers";

    /**
     * Hystrix execution timeout = Apache Http client timeouts + 1 millisecond so that underlying http client
     * will timeout first before Hystrix.
     */
    private static final int TIMEOUT_BUFFER_BETWEEN_HTTP_CLEINT_AND_HYSTRIX = 1;

    /**
     * Date format to return in the X_REQUEST_SENT_AT header
     */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * The request URL string.
     */
    private final String url;

    /**
     * Http method of the request.
     */
    private final Http http;

    /** Http client's connection timeout. */
    private final int connectionTimeout;

    /** Http client's socket timeout. */
    private final int socketTimeout;

    /**
     * Optional request header map
     */
    private Map<String, String> headerMap = new HashMap<>();

    /**
     * JSON key-value pairs in a String format.
     */
    private String jsonBody;

    /**
     * Log string builder
     */
    private StringBuilder logStr = new StringBuilder("type=http_hystrix;");

    /**
     * Default constructor
     *
     * @param http - a {@link com.intuit.payments.hystrix.HttpHystrixCommand.Http} method enum.
     * @param url - URL to be called.
     * @param hystrixCommandName - Hystrix command name.
     * @param hystrixGroupName - Hystrix command group name.
     * @param connectionTimeoutInMilliSec - Time to wait to get a connection.
     * @param socketTimeoutInMilliSec - Time to wait to send a request and receive a response.
     *
     * Hystrix Timeout = (Http Connection Timeout + Http Socket Timeout) + 1
     */
    public HttpHystrixCommand(Http http,
                              String url,
                              String hystrixCommandName,
                              String hystrixGroupName,
                              int connectionTimeoutInMilliSec,
                              int socketTimeoutInMilliSec) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory
                .asKey(hystrixGroupName))
                .andCommandKey(HystrixCommandKey.Factory.asKey(hystrixCommandName))
                .andCommandPropertiesDefaults(Setter()
                        .withExecutionTimeoutInMilliseconds(connectionTimeoutInMilliSec + socketTimeoutInMilliSec
                        + TIMEOUT_BUFFER_BETWEEN_HTTP_CLEINT_AND_HYSTRIX)));
        this.http = http;
        this.url = url;
        this.socketTimeout = socketTimeoutInMilliSec;
        this.connectionTimeout = connectionTimeoutInMilliSec;
        this.logStr.append("http=").append(http).append(";outURL=").append(url);
    }

    /**
     * Sets a request header. The "Accept" and "Content-Type" headers are auto-included.
     *
     * @param name - a Http header name
     * @param value - a Http header value
     * @return {@link HttpHystrixCommand} instance.
     */
    public HttpHystrixCommand header(String name, String value) {
        this.headerMap.put(name, value);
        return this;
    }

    /**
     * Sets request headers. The "Accept" and "Content-Type" headers are auto-included.
     *
     * @param headers - Map of Http request headers.
     * @return {@link HttpHystrixCommand} instance.
     */
    public HttpHystrixCommand headers(Map<String, String> headers) {
        this.headerMap.putAll(headers);
        return this;
    }

    /**
     * Sets JSON representation of key-value map as body. POST, PUT, and PATCH only!
     *
     * @param bodyMap - Map of key-value pairs.
     * @return {@link HttpHystrixCommand} instance.
     */
    public HttpHystrixCommand body(Map<String, Object> bodyMap) {
        this.jsonBody = Util.toJson(bodyMap);
        return this;
    }

    /**
     * Sets JSON representation of string body. POST, PUT, and PATCH only!
     *
     * @param body - JSON body string.
     * @return {@link HttpHystrixCommand} instance.
     */
    public HttpHystrixCommand body(String body) {
        this.jsonBody = body;
        return this;
    }

    /**
     * Executes a POST call and set the response details in the response map.
     *
     * @return Map of response including the HTTP headers.
     * @throws Exception if either Unirest call failed or setting response details to the map failed.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Map<String, Object> run() throws Exception {
        try(CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpUriRequest httpUriRequest = newHttpRequest();
            setRequestHeaders(httpUriRequest);

            HttpResponse httpResponse = httpClient.execute(httpUriRequest);

            int statusCode  = httpResponse.getStatusLine().getStatusCode();
            String statusReason = httpResponse.getStatusLine().getReasonPhrase();
            logStr.append(";status=").append(statusCode).append(";reason=").append(statusReason);

            String responseStr = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            if (LOG.isTraceEnabled()) {
                logStr.append(";request_headers=");
                for (Header header : httpUriRequest.getAllHeaders()) {
                    logStr.append(header.getName()).append(":").append(header.getValue()).append(",");
                }
                logStr.append(";request_body=").append(jsonBody)
                        .append(";response_headers=");
                for (Header header : httpResponse.getAllHeaders()) {
                    logStr.append(header.getName()).append(":").append(header.getValue()).append(",");
                }
                logStr.append(";response_body=").append(responseStr);
            }

            if (statusCode >= 500) {
                LOG.error(logStr.toString());
                throw new RuntimeException("Failed to " + http + " the remote server. status=" + statusCode);
            } else {
                LOG.info(logStr.toString());
            }

            Map<String, Object> responseMap = new HashMap<>();
            try {
                responseMap = Util.fromJson(responseStr);
            } catch (Exception ex) {
                LOG.error(logStr.append("String-to-JSON parsing failed. Setting response in _http_raw_response field.")
                        .toString(), ex);
                responseMap.put(HTTP_RAW_RESPONSE, responseStr);
            }

            responseMap.put(HTTP_STATUS_CODE, statusCode);
            responseMap.put(HTTP_STATUS_REASON, statusReason);
            responseMap.put(HTTP_RESPONSE_HEADERS, httpResponse.getAllHeaders());

            return responseMap;
        }
    }


    /**
     * Sets request headers like "Accept" and others, and JSON body if not empty.
     */
    private void setRequestHeaders(HttpUriRequest httpUriRequest) {
        httpUriRequest.addHeader(ACCEPT, APPLICATION_JSON.getMimeType());
        httpUriRequest.addHeader(X_REQUEST_SENT_AT, DATE_FORMAT.format(Calendar.getInstance().getTime()));

        for (String key : headerMap.keySet()) {
            httpUriRequest.addHeader(key, headerMap.get(key));
        }
    }

    private HttpUriRequest newHttpRequest() throws UnsupportedEncodingException {
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(socketTimeout)
                .setConnectTimeout(connectionTimeout)
                .build();

        switch (http) {
            case POST:
                HttpPost httpPost = new HttpPost(url);
                httpPost.setConfig(requestConfig);
                if (jsonBody != null && jsonBody.length() > 0) {
                    httpPost.addHeader(CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
                    httpPost.setEntity(new StringEntity(jsonBody));
                }
                return httpPost;

            case PUT:
                HttpPut httpPut = new HttpPut(url);
                httpPut.setConfig(requestConfig);
                if (jsonBody != null && jsonBody.length() > 0) {
                    httpPut.addHeader(CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
                    httpPut.setEntity(new StringEntity(jsonBody));
                }
                return httpPut;

            case PATCH:
                HttpPatch httpPatch = new HttpPatch(url);
                httpPatch.setConfig(requestConfig);
                if (jsonBody != null && jsonBody.length() > 0) {
                    httpPatch.addHeader(CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
                    httpPatch.setEntity(new StringEntity(jsonBody));
                }
                return httpPatch;

            case GET:
                HttpGet httpGet = new HttpGet(url);
                httpGet.setConfig(requestConfig);
                return httpGet;

            case DELETE:
                HttpDelete httpDelete = new HttpDelete(url);
                httpDelete.setConfig(requestConfig);
                return httpDelete;

            case HEAD:
                HttpHead httpHead = new HttpHead(url);
                httpHead.setConfig(requestConfig);
                return httpHead;

            case OPTIONS:
                HttpOptions httpOptions = new HttpOptions(url);
                httpOptions.setConfig(requestConfig);
                return httpOptions;

            default:
                throw new IllegalArgumentException("Invalid Http method:" + http);
        }
    }

    public enum Http {
        POST, GET, PUT, PATCH, DELETE, HEAD, OPTIONS
    }
}
