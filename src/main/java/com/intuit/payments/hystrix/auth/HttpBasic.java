/**
 * Copyright 2017 Intuit Inc. All rights reserved. Unauthorized reproduction
 * is a violation of applicable law. This material contains certain
 * confidential or proprietary information and trade secrets of Intuit Inc.
 */
package com.intuit.payments.hystrix.auth;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.intuit.payments.hystrix.util.Util.checkStringIsNotBlank;

/**
 * Http Basic auth implementation.
 *
 * @author saung
 * @since 6/29/17
 */
public class HttpBasic implements AuthInterface {
    /** Logger instance */
    private static final Logger log = LoggerFactory.getLogger(HttpBasic.class);

    /** app secret - a comma-separated username and password */
    private final String usernameAndPassword;

    /**
     * Default constructor.
     *
     * @param usernameAndPassword - a comma-separated username and password
     */
    public HttpBasic(String usernameAndPassword) {
        checkStringIsNotBlank(usernameAndPassword, "usernameAndPassword (xxxx:yyyy) must not be null or empty");
        byte[] authEncBytes = Base64.encodeBase64(usernameAndPassword.getBytes());
        String authStringEnc = new String(authEncBytes);
        this.usernameAndPassword = "Basic " + authStringEnc;
        log.info("type=auth_init;".concat("http_basic_auth_header={}"), this.usernameAndPassword.substring(0,6));
    }

    /**
     * Gets a Http Basic Auth header value.
     *
     * @return a Base64 encoded username : password.
     */
    @Override
    public String getAuthHeader() {
        return usernameAndPassword;
    }
}
