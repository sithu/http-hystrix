/**
 * Copyright 2016 Intuit Inc. All rights reserved. Unauthorized reproduction
 * is a violation of applicable law. This material contains certain
 * confidential or proprietary information and trade secrets of Intuit Inc.
 */
package com.intuit.payments.hystrix;

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

        Map<String, Object> response = request.run();
        assertNotNull(response);
        assertEquals(200, response.get("_http_status_code"));
        assertEquals("OK", response.get("_http_status_reason"));
        assertNotNull(response.get("_http_raw_response"));
        Map<String, Object> respHeaders = (Map)response.get("headers");
        assertEquals("x-value", respHeaders.get("X-Header"));
        assertEquals("text/html", respHeaders.get("Accept"));
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
        Map<String, String> request = new HashMap<String, String>() {{
            put("username", "foo");
        }};
        Request httpHystrixCommand = new Request(
                "http://httpbin.org/post",
                "FormPOSTCmd",
                "TestGroup",
                10000,
                10000).FORM_POST(request);

        httpHystrixCommand.headers(new HashMap<String, String>() {{
            put("x-header", "x-value");
        }});

        Map<String, Object> response = httpHystrixCommand.run();
        assertNotNull(response);
        assertEquals(200, response.get("_http_status_code"));
        assertEquals("OK", response.get("_http_status_reason"));
        assertTrue(response.containsKey("origin"));
        assertNotNull(response.get("_http_raw_response"));
        assertEquals("application/x-www-form-urlencoded; charset=UTF-8", ((Map) response.get("headers")).get("Content-Type"));
        assertEquals("foo", ((Map) response.get("form")).get("username"));
    }
}