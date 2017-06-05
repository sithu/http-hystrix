/**
 * Copyright 2017 Intuit Inc. All rights reserved. Unauthorized reproduction
 * is a violation of applicable law. This material contains certain
 * confidential or proprietary information and trade secrets of Intuit Inc.
 */
package com.intuit.payments.hystrix.util;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author saung
 * @since 6/5/17.
 */
public class HystrixLogTest {

    @Test
    public void logMetrics_Null_HystrixRequestLog() throws Exception {
        assertNotNull(new HystrixLog());
        int responseCode = 200;
        int processingTime = 1000;
        final StringBuilder expected = new StringBuilder("type=hystrix;");
        expected.append("response_code=").append(responseCode).append(";")
                .append("processing_time=").append(String.valueOf(processingTime)).append(";")
                .append("reason=HystrixRequestLog.getCurrentRequest() returned null. Try HystrixRequestContext.initializeContext()");
        StringBuilder actual = HystrixLog.logMetrics(responseCode, processingTime);
        assertNotNull(actual);
        assertEquals(expected.toString(), actual.toString());
    }

    @Test
    public void logMetrics() throws Exception {
        HystrixRequestContext context = HystrixRequestContext.initializeContext();
        HystrixCommand<String> hystrixCommand = new CommandHelloWorld("foo");
        hystrixCommand.execute();

        int responseCode = 200;
        int processingTime = 5000;
        final StringBuilder expected = new StringBuilder("type=hystrix;");
        expected.append("response_code=").append(responseCode).append(";")
                .append("processing_time=").append(String.valueOf(processingTime)).append(";")
                .append("CommandHelloWorld_is_success=yes;CommandHelloWorld_is_timeout=no;CommandHelloWorld_is_cb_open=no;CommandHelloWorld_exe_time=")
                .append(hystrixCommand.getExecutionTimeInMilliseconds()).append(";");
        expected.append("num_dependencies=").append(1).append(";");
        expected.append("is_all_dep_successful=").append(true).append(";");
        expected.append("dependencies_exe_time=").append(hystrixCommand.getExecutionTimeInMilliseconds()).append(";");
        expected.append("code_exe_time=").append(processingTime - hystrixCommand.getExecutionTimeInMilliseconds()).append(";");
        StringBuilder actual = HystrixLog.logMetrics(responseCode, processingTime);
        context.shutdown();
        assertNotNull(actual);
        assertEquals(expected.toString(), actual.toString());
    }

    @Test
    public void logMetrics_Failed() throws Exception {
        HystrixRequestContext context = HystrixRequestContext.initializeContext();
        HystrixCommand<String> hystrixCommand = new CommandHelloWorldFailed("foo");
        try {
            hystrixCommand.execute();
        } catch (Exception e) {
            // do nothing
        }

        int responseCode = 200;
        int processingTime = 5000;
        final StringBuilder expected = new StringBuilder("type=hystrix;");
        expected.append("response_code=").append(responseCode).append(";")
                .append("processing_time=").append(String.valueOf(processingTime)).append(";")
                .append("CommandHelloWorldFailed_is_success=no;CommandHelloWorldFailed_is_timeout=no;CommandHelloWorldFailed_is_cb_open=no;CommandHelloWorldFailed_exe_time=")
                .append(hystrixCommand.getExecutionTimeInMilliseconds()).append(";")
                .append("CommandHelloWorldFailed_failed_exception=class java.lang.RuntimeException;");
        expected.append("num_dependencies=").append(1).append(";");
        expected.append("is_all_dep_successful=").append(false).append(";");
        expected.append("dependencies_exe_time=").append(hystrixCommand.getExecutionTimeInMilliseconds()).append(";");
        expected.append("code_exe_time=").append(processingTime - hystrixCommand.getExecutionTimeInMilliseconds()).append(";");
        StringBuilder actual = HystrixLog.logMetrics(responseCode, processingTime);
        context.shutdown();
        assertNotNull(actual);
        assertEquals(expected.toString(), actual.toString());
    }

    private static class CommandHelloWorld extends HystrixCommand<String> {

        private final String name;

        public CommandHelloWorld(String name) {
            super(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"));
            this.name = name;
        }

        @Override
        protected String run() {
            return name;
        }
    }

    private static class CommandHelloWorldFailed extends HystrixCommand<String> {

        private final String name;

        public CommandHelloWorldFailed(String name) {
            super(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"));
            this.name = name;
        }

        @Override
        protected String run() {
            throw new RuntimeException("make it failed");
        }
    }
}