/**
 * Copyright 2017 Intuit Inc. All rights reserved. Unauthorized reproduction
 * is a violation of applicable law. This material contains certain
 * confidential or proprietary information and trade secrets of Intuit Inc.
 */
package com.intuit.payments.hystrix;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author saung
 * @since 6/30/17.
 */
public class ClientTest {
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private Client client = new Client("http://localhost");

    @Test
    public void httpBasicAuth() throws Exception {
        assertNotNull(client);
        client.httpBasicAuth("foo:bar");
    }

    @Test
    public void privateAuth() throws Exception {
        assertNotNull(client.privateAuth("appId", "appSecret"));
    }

    @Test
    public void customAuth() throws Exception {
        assertNotNull(client.customAuth(() -> "my-auth-impl"));
    }

    @Test
    public void connectionTimeoutInMilliSec() throws Exception {
        assertNotNull(client.connectionTimeoutInMilliSec(1000));
    }

    @Test
    public void socketTimeoutInMilliSec() throws Exception {
        assertNotNull(client.socketTimeoutInMilliSec(2000));
    }

    @Test
    public void request() throws Exception {
        Request request = client.Request("GetCmd", "HttpGroup", "/v1/users/{0}", 123);
        assertNotNull(request);
        assertEquals("GetCmd", request.getCommandKey().name());
        assertEquals("HttpGroup", request.getCommandGroup().name());
    }
}