/**
 * Copyright 2017 Intuit Inc. All rights reserved. Unauthorized reproduction
 * is a violation of applicable law. This material contains certain
 * confidential or proprietary information and trade secrets of Intuit Inc.
 */
package com.intuit.payments.hystrix.auth;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author saung
 * @since 6/30/17.
 */
public class HttpBasicTest {
    @Test
    public void getAuthHeader() throws Exception {
        AuthInterface authInterface = new HttpBasic("foo:bar");
        assertNotNull(authInterface);
        assertEquals("Basic Zm9vOmJhcg==", authInterface.getAuthHeader());
    }
}