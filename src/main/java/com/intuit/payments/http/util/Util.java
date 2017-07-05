/**
 * Copyright 2016 Intuit Inc. All rights reserved. Unauthorized reproduction
 * is a violation of applicable law. This material contains certain
 * confidential or proprietary information and trade secrets of Intuit Inc.
 */
package com.intuit.payments.http.util;


import com.google.gson.Gson;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
     * Converts from an Object to a JSON string.
     *
     * @param object - any Object instance.
     * @return a JSON string
     */
    public static String toJson(Object object) {
        if (object instanceof Map) {
            return gson.toJson(object, Map.class);
        }
        return gson.toJson(object);
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
     * Converts from a JSON string to instance of <T>.
     *
     * @param jsonStr - a JSON string.
     * @param classOfT -  a class of <T>.
     * @param <T> Generic type T to be serialized from JSON.
     * @return T instance.
     */
    public static <T> T fromJson(String jsonStr, Class<T> classOfT) {
        return gson.fromJson(jsonStr, classOfT);
    }

    /**
     * Checks whether a string is null or empty.
     *
     * @param str - a String to be checked.
     * @return true if the given string is not blank; return false otherwise.
     */
    public static boolean isNullOrBlank(String str) {
        return (str == null || str.trim().length() < 1);
    }

    /**
     * Checks whether a string is null or empty and raises an exception.
     *
     * @param str - a string to be checked.
     * @param err - an error message.
     */
    public static void checkStringIsNotBlank(String str, String err) {
        if (isNullOrBlank(str)) {
            throw new IllegalArgumentException(err);
        }
    }

    /**
     * Converts to a {@link NameValuePair} list for a Http Form POST request body.
     *
     * @param nvps - a name-value pair Http form POST map.
     * @return a {@link NameValuePair} list.
     */
    public static List<NameValuePair> toNameValuePairList(Map<String, String> nvps) {
        List<NameValuePair> nvpsList = nvps.entrySet().stream()
                .map(x -> new BasicNameValuePair(x.getKey(), x.getValue()) )
                .collect(Collectors.toList());
        return nvpsList;
    }

    /**
     * Constructs full URL with path using the given template in this format: /path/{0}/path/{1}
     *
     * @param host - Server host or server base URL.
     * @param path - API URL path, such as /v1/users/{0}.
     * @param templateValues - an optional varargs for URL template, such as user id "123" for path "/v1/users/{0}"
     * @return a full URL string.
     */
    public static String getFullURL(String host, String path, Object... templateValues) {
        String url = host;
        if (!isNullOrBlank(path)) {
            url += path;

            // replace {x} with actual values.
            if (templateValues.length > 0) {
                MessageFormat messageFormat = new MessageFormat(url);
                url = messageFormat.format(templateValues);
            }
        }

        return url;
    }
}
