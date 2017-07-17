/**
 * Copyright 2017 Intuit Inc. All rights reserved. Unauthorized reproduction
 * is a violation of applicable law. This material contains certain
 * confidential or proprietary information and trade secrets of Intuit Inc.
 */
package com.intuit.payments.http.exception;

/**
 * Http 404 exception.
 *
 * @author saung
 * @since 7/17/17
 */
public class HCResourceNotFoundException extends HCException {
    /**
     * Constructs a new runtime exception with the specified detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public HCResourceNotFoundException(String message) {
        super(message + ". Check the request URL path.");
    }
}
