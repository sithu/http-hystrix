/**
 * Copyright 2016 Intuit Inc. All rights reserved. Unauthorized reproduction
 * is a violation of applicable law. This material contains certain
 * confidential or proprietary information and trade secrets of Intuit Inc.
 */
package com.intuit.payments.hystrix.howto;

import com.intuit.payments.hystrix.Client;
import com.intuit.payments.hystrix.Request;

import java.util.HashMap;
import java.util.Map;

import static java.lang.System.out;

/**
 * This class shows how to use {@link Client} and {@link Request}.
 *
 * @author saung
 * @since 6/15/16
 */
public class Demo {
    private static final String HTTP_STATUS_CODE = "_http_status_code";
    private static final String HTTP_STATUS_REASON = "_http_status_reason";

    /**
     * Main method.
     * @param args
     */
    public static void main(String[] args) {
        httpGET();
        httpPOST();
    }

    private static void httpGET() {
        Client client = new Client("https://httpbin.org");

        Map<String, Object> response = client.Request("GetCommand","HttpGroup", "/get")
                .GET()
                .header("Intuit-Tid", "12345")
                .execute();

        out.println("__Response__\n" +
                response.get(HTTP_STATUS_CODE) + " " +
                response.get(HTTP_STATUS_REASON) + "\n" + response);
    }

    private static void httpPOST() {
        Client client = new Client("http://jsonplaceholder.typicode.com");

        Map<String, Object> response = client.Request("PostCommand", "HttpGroup","/posts")
                .POST()
                .body(
                        new HashMap<String, Object>() {{
                            put("foo", "bar");
                        }}
                ).execute();

        out.println("\n__Response__\n" +
                response.get(HTTP_STATUS_CODE) + " " +
                response.get(HTTP_STATUS_REASON) + "\n" + response);
    }
}