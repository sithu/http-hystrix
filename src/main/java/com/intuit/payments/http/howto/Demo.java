/**
 * Copyright 2016 Intuit Inc. All rights reserved. Unauthorized reproduction
 * is a violation of applicable law. This material contains certain
 * confidential or proprietary information and trade secrets of Intuit Inc.
 */
package com.intuit.payments.http.howto;

import com.intuit.payments.http.Client;
import com.intuit.payments.http.Request;
import com.intuit.payments.http.Response;

import java.util.HashMap;

import static java.lang.System.out;

/**
 * This class shows how to use {@link Client} and {@link Request}.
 *
 * @author saung
 * @since 6/15/16
 */
public class Demo {
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

        Response response = client.Request("GetCommand","HttpGroup", "/get")
                .GET()
                .header("Intuit-Tid", "12345")
                .execute();

        out.println("__Response__\n" +
                response.statusCode() + " " +
                response.statusReason() + "\n" + response.map());
    }

    private static void httpPOST() {
        Client client = new Client("http://jsonplaceholder.typicode.com");

        Response response = client.Request("PostCommand", "HttpGroup","/posts")
                .POST()
                .body(
                        new HashMap<String, Object>() {{
                            put("foo", "bar");
                        }}
                ).execute();

        out.println("\n__Response__\n" +
                response.statusCode() + " " +
                response.statusReason() + "\n" + response.map());
    }
}
