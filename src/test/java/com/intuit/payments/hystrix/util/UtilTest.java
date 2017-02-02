/**
 * Copyright 2015 Intuit Inc. All rights reserved. Unauthorized reproduction
 * is a violation of applicable law. This material contains certain
 * confidential or proprietary information and trade secrets of Intuit Inc.
 */
package com.intuit.payments.hystrix.util;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Test cases for Util class.
 */
public class UtilTest {

    @Test
    public void testPrivateAuthHeader() throws Exception {
        String iamHeader = Util.privateAuth("appId", "appSecret");
        assertEquals("Intuit_IAM_Authentication intuit_appid=appId,intuit_app_secret=appSecret", iamHeader);
    }

    @Test
    public void testPrivateAuthPlusHeader() throws Exception {
        String iamHeader = Util.privateAuth("appId", "appSecret").concat(Util.plus("CBT-Token", "token-123", "userId-123"));
        assertEquals("Intuit_IAM_Authentication intuit_appid=appId,intuit_app_secret=appSecret" +
                ",intuit_token_type=CBT-Token,intuit_token=token-123,intuit_userid=userId-123",
                iamHeader);
    }

    @Test
    public void testPrivateAuthPlusWithDefaultTokenTypeHeader() throws Exception {
        String iamHeader = Util.privateAuth("appId", "appSecret").concat(Util.plus("token-123", "userId-123"));
        assertEquals("Intuit_IAM_Authentication intuit_appid=appId,intuit_app_secret=appSecret" +
                        ",intuit_token_type=IAM-Ticket,intuit_token=token-123,intuit_userid=userId-123",
                iamHeader);
    }

    @Test
    public void testRequestHeaders() throws Exception {
        Map<String, String> map = Util.requestHeaders(
                "Intuit_IAM_Authentication intuit_appid=appId,intuit_app_secret=appSecre",
                "company-auth-id-123",
                "request-id-999"
        );
        assertEquals(4, map.size());
        assertEquals("Intuit_IAM_Authentication intuit_appid=appId,intuit_app_secret=appSecre", map.get("Authorization"));
        assertEquals("company-auth-id-123", map.get("Company-Id"));
        assertEquals("request-id-999", map.get("Request-Id"));
        assertEquals("request-id-999", map.get("intuit_tid"));
    }

    @Test
    public void testToJson() throws Exception {
        String json = Util.toJson(new HashMap<String, Object>() {{
            put("foo", "bar");
        }});
        String expected = "{\"foo\":\"bar\"}";
        assertEquals(expected, json);
    }

    @Test
    public void testFromJson() throws Exception {
        Map<String, Object> map = Util.fromJson("{\"foo\":\"bar\"}");
        assertNotNull(map);
        assertEquals(1, map.size());
        assertEquals("bar", map.get("foo"));
    }
}