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
    /**
     *  Private Auth Plus Header
     * Example:
     *
     * Authorization: Intuit_IAM_Authentication
     *  intuit_appid=Intuit.cto.api.gateway.simulator.test,
     *  intuit_app_secret=ctyj58n8VZvAqpl3W4pkn7,
     *  intuit_token_type=IAM-Ticket,
     *  intuit_realmid=50000003, [OPTIONAL]
     *  intuit_token=V1-65-Q3y8elbiw74j5wdna2cei5,
     *  intuit_userid=100186433
     **/

    /** Private Auth Format. NOTE: QBO V3 does NOT like having space between app id and app secret! */
    private static final String IAM_APP_ID_APP_SECRET = "Intuit_IAM_Authentication intuit_appid=%s,intuit_app_secret=%s";

    /** Private Auth Plus Format */
    private static final String TOKEN_TYPE_TOKEN_USER_ID = ",intuit_token_type=%s,intuit_token=%s,intuit_userid=%s";

    /** Gson instance */
    private static final Gson gson = new Gson();

    /** IAM Ticket Token Type */
    private static final String IAM_TICKET = "IAM-Ticket";

    /**
     * Creates an Private Auth IAM Auth header using the specified appId and appSecret.
     *
     * @param appId - an IAM application id.
     * @param appSecret - an IAM application secret.
     * @return an IAM authentication header string.
     */
    public static String privateAuth(final String appId, final String appSecret) {
        checkStringIsNotBlank(appId, "Invalid app Id=" + appId);
        checkStringIsNotBlank(appSecret, "Invalid app secret=" + appSecret);
        return format(IAM_APP_ID_APP_SECRET, appId, appSecret);
    }

    /**
     * Create a Private Auth's Plus header portion.
     *
     * @param tokenType -  Private Auth Plus token type.
     * @param token - IAM Ticket a.k.a. token.
     * @param userId - User Id.
     *
     * @return Private Auth's Plus header portion.
     */
    public static String plus(String tokenType, String token, String userId) {
        checkStringIsNotBlank(tokenType, "Invalid app Id=" + tokenType);
        checkStringIsNotBlank(token, "Invalid app secret=" + token);
        checkStringIsNotBlank(userId, "Invalid userId=" + userId);
        return format(TOKEN_TYPE_TOKEN_USER_ID, tokenType, token, userId);
    }

    /**
     * Create a default IAM-Ticket type (Private Auth) Plus header.
     *
     * @param token - IAM Ticket a.k.a. token.
     * @param userId - User Id.
     *
     * @return Private Auth's Plus header portion using IAM-Ticket token type
     */
    public static String plus(String token, String userId) {
        checkStringIsNotBlank(token, "Invalid token (IAM ticket) =" + token);
        checkStringIsNotBlank(userId, "Invalid userId =" + userId);
        return format(TOKEN_TYPE_TOKEN_USER_ID, IAM_TICKET, token, userId);
    }

    /**
     * Creates a standard Intuit request header map.
     *
     * @param iamAuthHeader - an IAM Auth header value.
     * @param companyAuthId - a company auth id.
     * @param requestId - a unique request id to be set in "intuit_tid" and "Request-Id" headers.
     * @return Map of HTTP headers.
     */
    public static Map<String, String> requestHeaders(String iamAuthHeader, String companyAuthId, String requestId) {
        checkStringIsNotBlank(iamAuthHeader, "Authorization header value is required");
        final Map<String, String> headerMap = new HashMap<>();
        headerMap.put(AUTHORIZATION, iamAuthHeader);
        if (companyAuthId != null && companyAuthId.length() > 0) {
            headerMap.put("Company-Id", companyAuthId);
        }
        if (requestId != null && requestId.length() > 0) {
            headerMap.put("Request-Id", requestId);
            headerMap.put("intuit_tid", requestId);
        }
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
