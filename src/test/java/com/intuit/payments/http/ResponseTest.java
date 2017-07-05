/**
 * Copyright 2017 Intuit Inc. All rights reserved. Unauthorized reproduction
 * is a violation of applicable law. This material contains certain
 * confidential or proprietary information and trade secrets of Intuit Inc.
 */

package com.intuit.payments.http;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author saung
 * @since 7/5/17.
 */
public class ResponseTest {
    private Header[] headers = new BasicHeader[] {
        new BasicHeader("Content-Type", "application/json"),
        new BasicHeader("X-Request-Sent-At", "2017-07-05 11:12:35.650")
    };

    @Test
    public void response() throws Exception {
        Response response = new Response(200, "OK", "body", headers);
        assertNotNull(response);
    }

    @Test
    public void statusCode() throws Exception {
        Response response = new Response(201, "Created", "body", headers);
        assertNotNull(response);
        assertEquals(201, response.statusCode());
    }

    @Test
    public void statusReason() throws Exception {
        Response response = new Response(201, "Created", "body", headers);
        assertNotNull(response);
        assertEquals("Created", response.statusReason());
    }

    @Test
    public void rawString() throws Exception {
        Response response = new Response(201, "Created", "my-raw-body", headers);
        assertNotNull(response);
        assertEquals("my-raw-body", response.rawString());
    }

    @Test
    public void headers() throws Exception {
        Response response = new Response(201, "Created", "body", headers);
        assertNotNull(response);
        Map<String, String> expected = new HashMap<String, String>() {{
            put("Content-Type", "application/json");
            put("X-Request-Sent-At", "2017-07-05 11:12:35.650");
        }};
        assertEquals(expected, response.headers());
    }

    @Test
    public void map() throws Exception {
        Response response = new Response(201, "Created", "{ \"foo\": \"bar\" }", headers);
        assertNotNull(response);
        Map<String, String> expected = new HashMap<String, String>() {{
            put("foo", "bar");
        }};
        assertEquals(expected, response.map());
    }

    @Test
    public void map_empty_response() throws Exception {
        Response response = new Response(201, "Created", "", headers);
        assertNotNull(response);
        Map<String, String> expected = new HashMap<>();
        assertEquals(expected, response.map());
    }

    @Test(expected = IllegalArgumentException.class)
    public void map_not_application_json_content_type(){
        headers = new BasicHeader[] {
                new BasicHeader("Content-Type", "application/text"),
        };
        Response response = new Response(201, "Created", "Hello", headers);
        assertNotNull(response);
        Map<String, String> expected = new HashMap<String, String>() {{
            put("foo", "bar");
        }};
        assertEquals(expected, response.map());
    }

    @Test
    public void json() throws Exception {
        Response response = new Response(201, "Created", "{ \"value\": \"bar\" }", headers);
        assertNotNull(response);
        Foo expected = new Foo();
        expected.value = "bar";

        assertEquals(expected, response.json(Foo.class));
    }

    @Test
    public void json_null_response() throws Exception {
        Response response = new Response(201, "Created", "", headers);
        assertNotNull(response);
        assertNull(response.json(Foo.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void json_not_application_json_content_type() throws Exception {
        headers = new BasicHeader[] {
                new BasicHeader("X-Header", "X-Value"),
        };
        Response response = new Response(201, "Created", "{ \"value\": \"bar\" }", headers);
        assertNotNull(response);
        Foo expected = new Foo();
        expected.value = "bar";

        assertEquals(expected, response.json(Foo.class));
    }

    class Foo {
        String value;

        public Foo() {
            value = "";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Foo foo = (Foo) o;

            return value != null ? value.equals(foo.value) : foo.value == null;
        }

        @Override
        public int hashCode() {
            return value != null ? value.hashCode() : 0;
        }
    }
}