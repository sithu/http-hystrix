/**
 * Copyright 2017 Intuit Inc. All rights reserved. Unauthorized reproduction
 * is a violation of applicable law. This material contains certain
 * confidential or proprietary information and trade secrets of Intuit Inc.
 */
package com.intuit.payments.http.auth;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author saung
 * @since 6/29/17.
 */
public class PrivateAuthTest {
    @Test
    public void getAuthHeader() throws Exception {
        AuthInterface authInterface = new PrivateAuth("MyAppId", "MyAppSecret");
        assertNotNull(authInterface);
        assertEquals("Intuit_IAM_Authentication intuit_appid=MyAppId,intuit_app_secret=MyAppSecret", authInterface.getAuthHeader());
    }
}