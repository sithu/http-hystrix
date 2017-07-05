/**
 * Copyright 2016 Intuit Inc. All rights reserved. Unauthorized reproduction
 * is a violation of applicable law. This material contains certain
 * confidential or proprietary information and trade secrets of Intuit Inc.
 */
package com.intuit.payments.http;

import com.intuit.payments.http.util.Util;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.exception.HystrixTimeoutException;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.intuit.payments.http.util.Util.isNullOrBlank;
import static com.intuit.payments.http.util.Util.toNameValuePairList;
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
public class Request extends HystrixCommand<Response> {
    /** Logger instance */
    private static final Logger LOG = LoggerFactory.getLogger(Request.class);

    /**
     * Additional HTTP Request Header to track network latency between this client and target server
     */
    private static final String X_REQUEST_SENT_AT = "x-request-sent-at";

    /**
     * Hystrix execution timeout = Apache HttpVerb client timeouts + 10 milliseconds so that underlying httpVerb client
     * will timeout first before Hystrix.
     */
    private static final int TIMEOUT_BUFFER_BETWEEN_HTTP_CLEINT_AND_HYSTRIX = 10;

    /**
     * Date format to return in the X_REQUEST_SENT_AT header
     */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Default period of inactivity in milliseconds after which persistent connections must be re-validated.
     * 1 min = 60000 ms
     */
    private static final int DEFAULT_CONNECTION_POOL_VALIDATE_AFTER_INACTIVITY = 60000;

    /**
     * The request URL string.
     */
    private final String url;

    /** timeout to get a network connection to server. */
    private final int connectionTimeout;

    /** timeout to receive individual packets after connection handshake. */
    private final int socketTimeout;

    /**
     * HttpVerb method of the request.
     */
    private HttpVerb httpVerb;

    /**
     * Optional request header map
     */
    private Map<String, String> headerMap = new HashMap<>();

    /**
     * JSON key-value pairs in a String format.
     */
    private String jsonBody;

    /**
     * {@link UrlEncodedFormEntity} to store HTML Form POST name-value pairs body.
     */
    private UrlEncodedFormEntity urlEncodedFormEntity;

    /**
     * Any HttpVerb Response code greater than or equal to this value will throw a @{@link RuntimeException}.
     * Default value is 500.
     */
    private int failedStatusCode = 500;

    /**
     * Log string builder
     */
    private StringBuilder logStr = new StringBuilder("type=http_hystrix;");

    /**
     * Non-positive value passed to this method disables connection validation.
     */
    private final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();

    /**
     * Default constructor
     *
     * @param url - URL to be called.
     * @param hystrixCommandName - Hystrix command name.
     * @param hystrixGroupName - Hystrix command group name.
     * @param connectionTimeoutInMilliSec - Time to wait to get a connection.
     * @param socketTimeoutInMilliSec - Time to wait to send a request and receive a response.
     *
     * Hystrix Timeout = (Connection Timeout + Socket Timeout) + 10 milliseconds buffer.
     */
    public Request(String url,
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
        this.url = url;
        this.socketTimeout = socketTimeoutInMilliSec;
        this.connectionTimeout = connectionTimeoutInMilliSec;
        this.connectionManager.setValidateAfterInactivity(DEFAULT_CONNECTION_POOL_VALIDATE_AFTER_INACTIVITY);
        this.logStr.append("http=").append(httpVerb).append(";outURL=").append(url);
    }

    /**
     * Sets the HttpVerb GET method.
     *
     * @return {@link Request} instance.
     */
    public Request GET() {
        httpVerb = HttpVerb.GET;
        return this;
    }

    /**
     * Sets the HttpVerb POST method.
     *
     * @return {@link Request} instance.
     */
    public Request POST() {
        httpVerb = HttpVerb.POST;
        return this;
    }

    /**
     * Sets the HttpVerb Form POST method and name-value pairs body.
     *
     * @param nvps - List of name-value pair string.
     * @return {@link Request} instance.
     */
    public Request FORM_POST(Map<String, String> nvps) {
        httpVerb = HttpVerb.FORM_POST;
        urlEncodedFormEntity = new UrlEncodedFormEntity(toNameValuePairList(nvps), StandardCharsets.UTF_8);
        return this;
    }

    /**
     * Sets the HttpVerb PUT method.
     *
     * @return {@link Request} instance.
     */
    public Request PUT() {
        httpVerb = HttpVerb.PUT;
        return this;
    }

    /**
     * Sets the HttpVerb DELETE method.
     *
     * @return {@link Request} instance.
     */
    public Request DELETE() {
        httpVerb = HttpVerb.DELETE;
        return this;
    }

    /**
     * Sets the HttpVerb HEAD method.
     *
     * @return {@link Request} instance.
     */
    public Request HEAD() {
        httpVerb = HttpVerb.HEAD;
        return this;
    }

    /**
     * Sets a request header. The "Accept" and "Content-Type" headers are auto-included.
     *
     * @param name - a HttpVerb header name
     * @param value - a HttpVerb header value
     * @return {@link Request} instance.
     */
    public Request header(String name, String value) {
        if (!isNullOrBlank(name) && !isNullOrBlank(value)) {
            this.headerMap.put(name, value);
        }
        return this;
    }

    /**
     * Sets request headers. The "Accept" and "Content-Type" headers are auto-included.
     *
     * @param headers - Map of HttpVerb request headers.
     * @return {@link Request} instance.
     */
    public Request headers(Map<String, String> headers) {
        if (headers != null) {
            this.headerMap.putAll(headers);
        }
        return this;
    }

    /**
     * Sets a JSON representation of string request body. POST, PUT, and PATCH only!
     *
     * @param body - a request JSON string.
     * @return {@link Request} instance.
     */
    public Request bodyStr(String body) {
        this.jsonBody = body;
        return this;
    }

    /**
     * Sets a request payload by converting to JSON string. POST, PUT, and PATCH only!
     *
     * @param request - Request payload object, such as Map<String,?> or DTO instance.
     * @return {@link Request} instance.
     */
    public Request body(Object request) {
        this.jsonBody = Util.toJson(request);
        return this;
    }

    /**
     * Sets a failed HttpVerb Status code to check against the client response code.
     *
     * @param failedStatusCode - A HTTP Response Code: 2xx, 3xx, 4xx, or 5xx.
     * @return {@link Request} instance.
     */
    public Request throwExceptionIfResponseCodeIsGreaterThanOrEqual(int failedStatusCode) {
        this.failedStatusCode = failedStatusCode;
        return this;
    }

    /**
     * Defines period of inactivity in milliseconds after which persistent connections
     * must be re-validated prior to being leased to the consumer.
     * Non-positive value passed to this method disables connection validation.
     * This check helps detect connections that have become stale (half-closed) while kept inactive in the pool.
     *
     * @param milliseconds - inactivity in milliseconds to revalidate the connection. Default is 1 min.
     * @return {@link Request} instance.
     */
    public Request validateConnectionAfterInactivity(int milliseconds) {
        this.connectionManager.setValidateAfterInactivity(milliseconds);
        return this;
    }

    /**
     * Executes a POST call and set the response details in the response map.
     *
     * NOTE: useSystemProperties() will read JVM arguments like -Dhttp.proxyHost=10.0.0.1
     *
     * @return Response Map.
     * @throws Exception if either HttpVerb client call failed or parsing to JSON failed.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Response run() throws Exception {
        try(CloseableHttpClient httpClient = HttpClients.custom().useSystemProperties()
                .setConnectionManager(connectionManager).build()) {
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

            if (statusCode >= failedStatusCode) {
                logStr.append(";failed_response_body=").append(responseStr);
                LOG.error(logStr.toString());
                throw new RuntimeException("Failed to " + httpVerb + " the remote server. status=" + statusCode);
            } else {
                LOG.info(logStr.toString());
            }

            return new Response(statusCode, statusReason, responseStr, httpResponse.getAllHeaders());
        } catch (SocketTimeoutException stoEx) {
            LOG.error(logStr.append("No data package received in " + socketTimeout + "ms.").toString(), stoEx);
            throw new HystrixTimeoutException();
        } catch (Exception ex) {
            LOG.error(logStr.append("Unknown exception=" + ex.getMessage()).toString(), ex);
            throw ex;
        }
    }

    /**
     * Sets request headers like "Accept" and others, and JSON body if not empty.
     *
     * NOTE: getMimeType() vs toString() differences!
     * APPLICATION_JSON.getMimeType() => application/json
     * APPLICATION_JSON.toString()    => application/json; charset=UTF-8
     */
    private void setRequestHeaders(HttpUriRequest httpUriRequest) {
        if (httpVerb != HttpVerb.FORM_POST && !headerMap.containsKey(ACCEPT)) {
            httpUriRequest.addHeader(ACCEPT, APPLICATION_JSON.getMimeType());
        }
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

        switch (httpVerb) {
            case POST:
                HttpPost httpPost = new HttpPost(url);
                httpPost.setConfig(requestConfig);
                if (jsonBody != null && jsonBody.length() > 0) {
                    httpPost.addHeader(CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
                    httpPost.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));
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

            case FORM_POST:
                HttpPost httpFormPost = new HttpPost(url);
                httpFormPost.setConfig(requestConfig);
                /** Uses this.urlEncodedFormEntity instead of jsonBody */
                httpFormPost.setEntity(this.urlEncodedFormEntity);
                return httpFormPost;

            default:
                throw new IllegalArgumentException("Invalid HttpVerb method:" + httpVerb);
        }
    }

    private enum HttpVerb {
        POST, GET, PUT, PATCH, DELETE, HEAD, OPTIONS, FORM_POST
    }
}
