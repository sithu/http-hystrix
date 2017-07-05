/**
 * Copyright 2016 Intuit Inc. All rights reserved. Unauthorized reproduction
 * is a violation of applicable law. This material contains certain
 * confidential or proprietary information and trade secrets of Intuit Inc.
 */
package com.intuit.payments.http;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by saung on 6/20/16.
 */
public class RequestIT {

    @Test
    public void test() throws Exception {
        Request request = new Request(
                "http://httpbin.org/headers",
                "TestCmd",
                "TestGroup",
                10000,
                10000).GET();

        request.headers(new HashMap<String, String>() {{
            put("X-Header", "x-value");
            put("Accept", "text/html");
        }});
        request.validateConnectionAfterInactivity(60000);

        Response response = request.run();
        assertNotNull(response);
        assertEquals(200, response.statusCode());
        assertEquals("OK", response.statusReason());
        assertNotNull(response.rawString());
        Map<String, Object> resp = (Map) response.map().get("headers");
        assertEquals("x-value", resp.get("X-Header"));
        assertEquals("text/html", resp.get("Accept"));
    }

    @Test(expected = RuntimeException.class)
    public void test_FailedHttpStatusCode() throws Exception {
        Request request = new Request(
                "http://httpbin.org/ip",
                "TestCmd",
                "TestGroup",
                10000,
                10000).GET().throwExceptionIfResponseCodeIsGreaterThanOrEqual(200); // Forces to throw RuntimeException even with 200.

        request.headers(new HashMap<String, String>() {{
            put("x-header", "x-value");
        }});

        request.run();
    }

    @Test
    public void testFormPOST() throws Exception {
        Map<String, String> payload = new HashMap<String, String>() {{
            put("username", "foo");
        }};
        Request request = new Request(
                "http://httpbin.org/post",
                "FormPOSTCmd",
                "TestGroup",
                10000,
                10000).FORM_POST(payload);

        request.headers(new HashMap<String, String>() {{
            put("x-header", "x-value");
        }});

        Response response = request.run();
        assertNotNull(response);
        assertEquals(200, response.statusCode());
        assertEquals("OK", response.statusReason());
        assertNotNull(response.rawString());
        assertEquals("application/x-www-form-urlencoded; charset=UTF-8", ((Map) response.map().get("headers")).get("Content-Type"));
        assertEquals("foo", ((Map)response.map().get("form")).get("username"));
    }
}