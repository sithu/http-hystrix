/**
 * Copyright 2015 Intuit Inc. All rights reserved. Unauthorized reproduction
 * is a violation of applicable law. This material contains certain
 * confidential or proprietary information and trade secrets of Intuit Inc.
 */
package com.intuit.payments.http.util;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
        Foo foo = new Foo();
        foo.value = "bar";

        String json = Util.toJson(foo);
        String expected = "{\"value\":\"bar\"}";
        assertEquals(expected, json);

        json = Util.toJson(new HashMap<String, Object>() {{
            put("foo", "bar");
        }});
        assertEquals("{\"foo\":\"bar\"}", json);
    }

    @Test
    public void testFromJson() throws Exception {
        Map<String, Object> map = Util.fromJson("{\"foo\":\"bar\"}");
        assertNotNull(map);
        assertEquals(1, map.size());
        assertEquals("bar", map.get("foo"));
    }

    @Test
    public void testFromJson_Class() throws Exception {
        Foo foo = new Foo();
        foo.value = "bar";

        Foo actual = Util.fromJson("{\"value\":\"bar\"}", Foo.class);
        assertNotNull(actual);
        assertEquals(foo.value, actual.value);
    }

    @Test
    public void TestToNameValuePairList() {
        Map<String, String> nvps = new HashMap<String, String>() {{
            put("foo", "bar");
        }};

        List<NameValuePair> expected = new ArrayList<>();
        expected.add(new BasicNameValuePair("foo", "bar"));

        List<NameValuePair> actual = Util.toNameValuePairList(nvps);
        assertEquals(actual, expected);
    }

    @Test
    public void getFullURL() {
        String host = "http://localhost";
        String path = "/v1/foo";

        assertEquals(host, Util.getFullURL(host, null));
        assertEquals(host + path, Util.getFullURL(host, path));
        path = "/v1/users/{0}";
        assertEquals(host + "/v1/users/123", Util.getFullURL(host, path, 123));
    }

    class Foo {
        String value;

        public Foo() {
            value = "";
        }
    }
}