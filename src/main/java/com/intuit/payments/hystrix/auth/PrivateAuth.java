/**
 * Copyright 2017 Intuit Inc. All rights reserved. Unauthorized reproduction
 * is a violation of applicable law. This material contains certain
 * confidential or proprietary information and trade secrets of Intuit Inc.
 */
package com.intuit.payments.hystrix.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.intuit.payments.hystrix.util.Util.checkStringIsNotBlank;
import static java.lang.String.format;

/**
 * Intuit IAM Private Auth implementation.
 *
 * @author saung
 * @since 6/29/17
 */
public class PrivateAuth implements AuthInterface {
    /** Logger instance */
    private static final Logger log = LoggerFactory.getLogger(PrivateAuth.class);

    /** IAM Authentication HTTP header format */
    private static final String AUTH_HEADER_FORMAT = "Intuit_IAM_Authentication intuit_appid=%s,intuit_app_secret=%s";

    /** Intuit Private Auth Header value */
    private final String iamAuth;

    /**
     * Default constructor.
     *
     * @param appId - a client appId string.
     * @param appSecret - a client appSecret string.
     */
    public PrivateAuth(String appId, String appSecret) {
        checkStringIsNotBlank(appId, "appId must not be null or empty");
        checkStringIsNotBlank(appSecret, "appSecret must not be null or empty");
        iamAuth = format(AUTH_HEADER_FORMAT, appId, appSecret);
        log.info("type=auth_init;".concat("auth_header={}"), iamAuth.substring(0, 40));
    }

    /**
     * Gets a Intuit Private Auth Basic Auth header value.

     * @param perRequestTicket - Not applicable in Private Auth mode.
     * @return a IAM Private Auth header value.
     */
    @Override
    public String getAuthHeader(String... perRequestTicket) {
        return iamAuth;
    }
}
