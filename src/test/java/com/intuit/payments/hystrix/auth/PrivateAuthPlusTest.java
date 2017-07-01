package com.intuit.payments.hystrix.auth; /**
 * Copyright 2017 Intuit Inc. All rights reserved. Unauthorized reproduction
 * is a violation of applicable law. This material contains certain
 * confidential or proprietary information and trade secrets of Intuit Inc.
 */

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author saung
 * @since 6/30/17.
 */
public class PrivateAuthPlusTest {
    @Test
    public void getAuthHeader() throws Exception {
        AuthInterface authInterface = new PrivateAuthPlus("MyAppId", "MyAppSecret");
        assertNotNull(authInterface);
        assertEquals("Intuit_IAM_Authentication intuit_appid=MyAppId,intuit_app_secret=MyAppSecret",
                authInterface.getAuthHeader());
        assertEquals("Intuit_IAM_Authentication intuit_appid=MyAppId,intuit_app_secret=MyAppSecret",
                authInterface.getAuthHeader("x"));
        assertEquals("Intuit_IAM_Authentication intuit_appid=MyAppId,intuit_app_secret=MyAppSecret" +
                        ",intuit_token_type=IAM-Ticket,intuit_token=ticket-v1,intuit_userid=user-id-111",
                authInterface.getAuthHeader("ticket-v1", "user-id-111"));
        assertEquals("Intuit_IAM_Authentication intuit_appid=MyAppId,intuit_app_secret=MyAppSecret" +
                        ",intuit_token_type=CBT-Token,intuit_token=ticket-v1,intuit_userid=user-id-111",
                authInterface.getAuthHeader("CBT-Token", "ticket-v1", "user-id-111"));
    }
}