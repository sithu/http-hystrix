/**
 * Copyright 2016 Intuit Inc. All rights reserved. Unauthorized reproduction
 * is a violation of applicable law. This material contains certain
 * confidential or proprietary information and trade secrets of Intuit Inc.
 */
package com.intuit.payments.hystrix.howto;

import com.intuit.payments.hystrix.HttpHystrixCommand;

import java.util.HashMap;
import java.util.Map;

import static com.intuit.payments.hystrix.HttpHystrixCommand.*;
import static java.lang.System.out;

/**
 * @author saung
 * @since 6/15/16
 */
public class HttpHystrixCommandDemo {
    private static final String HTTP_STATUS_CODE = "_http_status_code";
    private static final String HTTP_STATUS_REASON = "_http_status_reason";
    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        httpGET();
        httpPOST();
        httpPOSTWithBodyStr();
    }

    private static void httpGET() {
        HttpHystrixCommand httpHystrixCommand = new HttpHystrixCommand(
                Http.GET,
                "https://httpbin.org/get",
                "GetHttpBinCommand",
                "HTTPGroup",
                100000,
                100000
        );

        Map<String, Object> response = httpHystrixCommand.execute();
        out.println("__Response__\n" +
                response.get(HTTP_STATUS_CODE) + " " +
                response.get(HTTP_STATUS_REASON) + "\n" + response);
        out.println("Hystrix_Execution_Time=" + httpHystrixCommand.getExecutionTimeInMilliseconds() + "ms");
    }

    private static void httpPOST() {
        HttpHystrixCommand httpHystrixCommand = new HttpHystrixCommand(
                Http.POST,
                "http://jsonplaceholder.typicode.com/posts",
                "PostJSONCommand",
                "HTTPGroup",
                100000,
                100000
        );
        // Set JSON Body
        httpHystrixCommand.body(new HashMap<String, Object>() {{
            put("foo", "bar");
        }});

        Map<String, Object> response = httpHystrixCommand.execute();
        System.out.println("\n__Response__\n" +
                response.get(HTTP_STATUS_CODE) + " " +
                response.get(HTTP_STATUS_REASON) + "\n" + response);
        out.println("Hystrix_Execution_Time=" + httpHystrixCommand.getExecutionTimeInMilliseconds() + "ms");
    }

    private static void httpPOSTWithBodyStr() {
        HttpHystrixCommand httpHystrixCommand = new HttpHystrixCommand(
                Http.POST,
                "http://jsonplaceholder.typicode.com/posts",
                "PostJSONStrCommand",
                "HTTPGroup",
                100000,
                100000
        );

        String json = "{  \"foo\": \"bar\" }";
        httpHystrixCommand.body(json);

        Map<String, Object> response = httpHystrixCommand.execute();
        System.out.println("\n__Response__\n" +
                response.get(HTTP_STATUS_CODE) + " " +
                response.get(HTTP_STATUS_REASON) + "\n" + response);
        out.println("Hystrix_Execution_Time=" + httpHystrixCommand.getExecutionTimeInMilliseconds() + "ms");
    }
}
