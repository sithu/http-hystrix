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
public class HttpHystrixCommandIT {

    @Test
    public void test() throws Exception {
        HttpHystrixCommand httpHystrixCommand = new HttpHystrixCommand(
                HttpHystrixCommand.Http.GET,
                "https://httpbin.org/ip",
                "TestCmd",
                "TestGroup",
                10000,
                10000);

        httpHystrixCommand.headers(new HashMap<String, String>() {{
            put("x-header", "x-value");
        }});
        Map<String, Object> response = httpHystrixCommand.run();
        assertNotNull(response);
        assertEquals(200, response.get("_http_status_code"));
        assertEquals("OK", response.get("_http_status_reason"));
        assertTrue(response.containsKey("origin"));
        assertNotNull(response.get("_http_raw_response"));
        assertTrue(String.valueOf(response.get("_http_raw_response")).contains("origin"));
    }
}