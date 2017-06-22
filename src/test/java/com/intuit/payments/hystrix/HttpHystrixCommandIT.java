/**
 * Copyright 2016 Intuit Inc. All rights reserved. Unauthorized reproduction
 * is a violation of applicable law. This material contains certain
 * confidential or proprietary information and trade secrets of Intuit Inc.
 */
package com.intuit.payments.hystrix;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by saung on 6/20/16.
 */
public class HttpHystrixCommandIT {

    @Test
    public void test() throws Exception {
        HttpHystrixCommand httpHystrixCommand = new HttpHystrixCommand(
                HttpHystrixCommand.Http.GET,
                "http://httpbin.org/headers",
                "TestCmd",
                "TestGroup",
                10000,
                10000);

        httpHystrixCommand.headers(new HashMap<String, String>() {{
            put("X-Header", "x-value");
            put("Accept", "text/html");
        }});
        httpHystrixCommand.validateConnectionAfterInactivity(60000);

        Map<String, Object> response = httpHystrixCommand.run();
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
        HttpHystrixCommand httpHystrixCommand = new HttpHystrixCommand(
                HttpHystrixCommand.Http.GET,
                "http://httpbin.org/ip",
                "TestCmd",
                "TestGroup",
                10000,
                10000).failWhenStatusCodeIs(200); // Forces to throw RuntimeException even with 200.

        httpHystrixCommand.headers(new HashMap<String, String>() {{
            put("x-header", "x-value");
        }});

        httpHystrixCommand.run();
    }

    @Test
    public void testFormPOST() throws Exception {
        HttpHystrixCommand httpHystrixCommand = new HttpHystrixCommand(
                HttpHystrixCommand.Http.FORM_POST,
                "http://httpbin.org/post",
                "FormPOSTCmd",
                "TestGroup",
                10000,
                10000);

        httpHystrixCommand.headers(new HashMap<String, String>() {{
            put("x-header", "x-value");
        }});
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("username", "foo"));
        httpHystrixCommand.formBody(nvps);

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