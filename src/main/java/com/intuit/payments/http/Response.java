/**
 * Copyright 2017 Intuit Inc. All rights reserved. Unauthorized reproduction
 * is a violation of applicable law. This material contains certain
 * confidential or proprietary information and trade secrets of Intuit Inc.
 */
package com.intuit.payments.http;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.intuit.payments.http.util.Util.fromJson;
import static com.intuit.payments.http.util.Util.isNullOrBlank;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;

/**
 * This response DTO holds useful response fields from underlying Http client.
 *
 * @author saung
 * @since 7/5/17
 */
public class Response {
    /** Logger instance */
    private static final Logger LOG = LoggerFactory.getLogger(Response.class);

    private final int statusCode;

    private final String statusReason;

    private final String rawString;

    private final Map<String, String> headers;

    /**
     * Default constructor.
     *
     * @param statusCode - a Http status code.
     * @param statusReason - a Http status reason line string.
     * @param rawString - a Http response body.
     * @param headers - Http response headers.
     */
    public Response(int statusCode, String statusReason, String rawString, Header[] headers) {
        this.statusCode = statusCode;
        this.statusReason = statusReason;
        this.rawString = rawString;
        this.headers = Stream.of(headers).collect(Collectors.toMap(Header::getName, Header::getValue));
    }

    /**
     * Gets a Http status code.
     *
     * @return a Http status code from the server.
     */
    public int statusCode() {
        return statusCode;
    }

    /**
     * Gets a Http status reason line.
     *
     * @return a status reason String.
     */
    public String statusReason() {
        return statusReason;
    }

    /**
     * Gets a raw response body String.
     *
     * @return - a body String.
     */
    public String rawString() {
        return rawString;
    }

    /**
     * Gets a map of response headers.
     *
     * @return - Map of response headers.
     */
    public Map<String, String> headers() {
        return headers;
    }

    /**
     * De-serializes the response body string to key-value map via Gson.
     *
     * @return Map of response body if the pre-conditions are met; return null otherwise.
     */
    public Map<String, Object> map() {
        if (!isValidJson()) {
            return new HashMap<>();
        }

        try {
            return fromJson(rawString);
        } catch(Exception e) {
            LOG.error("Failed to deserialize the body JSON string to key-value map. raw_string=" + rawString);
            throw e;
        }
    }

    /**
     * De-serializes the response body string to a given class via Gson.
     *
     * @param clazz - a Class to be de-serialized.
     * @param <T> -  a generic Type <T> of the given class.
     * @return instance of the given clazz if the pre-conditions are met; return null otherwise.
     */
    public <T> T json(Class<T> clazz) {
        if (!isValidJson()) {
            return null;
        }

        try {
            return fromJson(rawString, clazz);
        } catch(Exception e) {
            LOG.error("Failed to deserialize the body JSON string to the given type<{}>. raw_string=" + rawString, clazz);
            throw e;
        }
    }

    private boolean isValidJson() {
        if (isNullOrBlank(rawString)) {
            LOG.warn("Response raw string was null or empty. Couldn't deserialize!. raw_string=" + rawString);
            return false;
        }

        String headerValue = headers.get(HttpHeaders.CONTENT_TYPE);
        if (isNullOrBlank(headerValue) || !headerValue.contains(APPLICATION_JSON.getMimeType())) {
            LOG.warn("Response Content-Type was NOT expected {}. content_type={}"
                    , APPLICATION_JSON.getMimeType(), headerValue);
            throw new IllegalArgumentException("Response Content-Type was NOT expected "
                    + APPLICATION_JSON.getMimeType() + ". content_type=" + headerValue);
        }

        return true;
    }
}
