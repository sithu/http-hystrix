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
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

import static com.netflix.hystrix.HystrixCommandProperties.Setter;
import static org.apache.http.HttpHeaders.ACCEPT;
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
    /**
     * Additional HTTP Request Header to track network latency between this client and target server
     */
    private static final String X_REQUEST_SENT_AT = "x-request-sent-at";

    /**
     *
     */
    private static final int TIMEOUT_BUFFER_BETWEEN_HTTP_CLEINT_AND_HYSTRIX = 1;

    private static final String HTTP_STATUS_CODE = "_http_status_code";
    private static final String HTTP_STATUS_REASON = "_http_status_reason";
    private static final String HTTP_RESPONSE_HEADERS = "_http_response_headers";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * URL to be called
     */
    private final Request request;

    /**
     * Optional request header map
     */
    private Map<String, String> headerMap;

    /**
     * Form POST key-value pairs in a Map
     */
    private String jsonBody;

    /**
     * Default constructor
     *
     * @param http - a {@link com.intuit.payments.hystrix.HttpHystrixCommand.Http} enum.
     * @param url - URL to be called.
     * @param hystrixCommandName - Hystrix command name.
     * @param hystrixGroupName - Hystrix command group name.
     * @param connectionTimeoutInMilliSec - time to wait to get a connection.
     * @param socketTimeoutInMilliSec - time to wait to send a request and receive a response.
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
        this.request = createRequest(http, url);
        this.request.connectTimeout(connectionTimeoutInMilliSec).socketTimeout(socketTimeoutInMilliSec);
    }

    public HttpHystrixCommand headers(Map<String, String> headerMap) {
        this.headerMap = headerMap;
        return this;
    }

    public HttpHystrixCommand body(Map<String, String> bodyMap) {
        this.jsonBody = Util.toJson(bodyMap);
        return this;
    }

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
        setRequestHeadersAndBody();
        final HttpResponse httpResponse = request.execute().returnResponse();
        final Map<String, Object> responseMap = Util.fromJson(EntityUtils.toString(httpResponse.getEntity()));
        responseMap.put(HTTP_STATUS_CODE, httpResponse.getStatusLine().getStatusCode());
        responseMap.put(HTTP_STATUS_REASON, httpResponse.getStatusLine().getReasonPhrase());
        responseMap.put(HTTP_RESPONSE_HEADERS, httpResponse.getAllHeaders());

        return responseMap;
    }


    /**
     * Invokes a HTTP POST call.
     *
     * @throws IOException if something went wrong while making HTTP POST call to the
     *                                                              endpoint.
     */
    private void setRequestHeadersAndBody() throws IOException {
        request.addHeader(ACCEPT, APPLICATION_JSON.toString());
        request.addHeader(X_REQUEST_SENT_AT, DATE_FORMAT.format(Calendar.getInstance().getTime()));

        // add optional headers to the request
        if (headerMap != null) {
            for (Map.Entry header : headerMap.entrySet()) {
                request.addHeader(String.valueOf(header.getKey()), String.valueOf(header.getKey()));
            }
        }
        if (jsonBody != null && jsonBody.trim().length() > 0) {
            request.bodyString(jsonBody, ContentType.APPLICATION_JSON);
        }
    }

    private Request createRequest(Http http, String url) {
        switch (http) {
            case POST:
                return Request.Post(url);
            case GET:
                return Request.Get(url);
            case DELETE:
                return Request.Delete(url);
            case PUT:
                return Request.Put(url);
            case HEAD:
                return Request.Head(url);
            case OPTIONS:
                return Request.Options(url);
            default:
                throw new IllegalArgumentException("Invalid Http method:" + http);
        }
    }

    public enum Http {
        POST, GET, PUT, DELETE, HEAD, OPTIONS;
    }
}
