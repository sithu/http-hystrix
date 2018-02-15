/**
 * Copyright 2018 Intuit Inc. All rights reserved. Unauthorized reproduction
 * is a violation of applicable law. This material contains certain
 * confidential or proprietary information and trade secrets of Intuit Inc.
 */
package com.intuit.payments.http.auth;


import org.junit.Test;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit test for Offline ticket auth.
 *
 * @author saung
 * @since 2/14/18.
 */
public class OfflineTicketTest {

    private static final String OFFLINE_APP_ID_APP_SECRET = "Intuit_IAM_Authentication intuit_token_type=IAM-Offline-Ticket,"
            + "intuit_appid=%s,intuit_app_secret=%s,intuit_token=%s";

    @Test
    public void getAuthHeader() throws Exception {
        AuthInterface auth = new OfflineTicket("appId", "appSecret");
        assertNotNull(auth);
        assertEquals(format(OFFLINE_APP_ID_APP_SECRET, "appId", "appSecret", "offline_ticket"), auth.getAuthHeader("offline_ticket"));
    }
}