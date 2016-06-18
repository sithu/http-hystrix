/**
 * Copyright 2016 Intuit Inc. All rights reserved. Unauthorized reproduction
 * is a violation of applicable law. This material contains certain
 * confidential or proprietary information and trade secrets of Intuit Inc.
 */
package com.intuit.payments.hystrix.util;


import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static org.apache.http.HttpHeaders.AUTHORIZATION;

/**
 * Utility class for the PayAPI client.
 *
 * @author saung
 * @since 7/23/15
 */
public class Util {
    /** IAM Authentication HTTP header format */
    private static final String AUTH_HEADER_FORMAT = "Intuit_IAM_Authentication intuit_appid=%s, intuit_app_secret=%s";

    /** Gson instance */
    private static final Gson gson = new Gson();

    /**
     * Creates an IAM Auth header using the specified appId and appSecret.
     *
     * @param appId - an IAM application id.
     * @param appSecret - an IAM application secret.
     * @return an IAM authentication header string.
     */
    public static String createIAMAuthHeader(final String appId, final String appSecret) {
        checkStringIsNotBlank(appId, "Invalid app Id=" + appId);
        checkStringIsNotBlank(appSecret, "Invalid app secret=" + appSecret);
        return format(AUTH_HEADER_FORMAT, appId, appSecret);
    }

    /**
     * Creates a request header map.
     *
     * @param iamAuthHeader - an IAM Auth header value.
     * @param companyAuthId - a company auth id.
     * @param requestId - a unique request id.
     * @return Map of HTTP headers.
     */
    public static Map<String, String> requestHeaders(String iamAuthHeader, String companyAuthId, String requestId) {
        checkStringIsNotBlank(iamAuthHeader, "");
        checkStringIsNotBlank(companyAuthId, "");
        checkStringIsNotBlank(requestId, "");
        final Map<String, String> headerMap = new HashMap<String, String>();
        headerMap.put(AUTHORIZATION, iamAuthHeader);
        headerMap.put("Company-Id", companyAuthId);
        headerMap.put("Request-Id", requestId);
        return headerMap;
    }

    /**
     * Converts from a Map to a JSON string.
     *
     * @param map - a Map instance.
     * @return a JSON string
     */
    public static String toJson(final Map<String, ?> map) {
        return gson.toJson(map, Map.class);
    }

    /**
     * Converts from a JSON string to a Map.
     *
     * @param jsonStr - a JSON string.
     * @return a Map instance.
     */
    public static Map fromJson(final String jsonStr) {
        return gson.fromJson(jsonStr, Map.class);
    }

    /**
     *
     * @param str
     * @param err
     */
    public static void checkStringIsNotBlank(String str, String err) {
        if (str == null || str.trim().length() < 1) {
            throw new IllegalArgumentException(err);
        }
    }
}
