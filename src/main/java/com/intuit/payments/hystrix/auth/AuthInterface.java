/**
 * Copyright 2017 Intuit Inc. All rights reserved. Unauthorized reproduction
 * is a violation of applicable law. This material contains certain
 * confidential or proprietary information and trade secrets of Intuit Inc.
 */
package com.intuit.payments.hystrix.auth;

/**
 * Http Hystrix client's authentication interface.
 *
 * @author saung
 * @since 6/29/17.
 */
public interface AuthInterface {
    /**
     * Gets a Http "Authorization" header value to be used in Http Hystrix client.
     *
     * @return the "Authorization" header value string.
     */
    String getAuthHeader();
}
