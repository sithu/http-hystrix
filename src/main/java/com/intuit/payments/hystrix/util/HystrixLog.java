/**
 * Copyright 2017 Intuit Inc. All rights reserved. Unauthorized reproduction
 * is a violation of applicable law. This material contains certain
 * confidential or proprietary information and trade secrets of Intuit Inc.
 */
package com.intuit.payments.hystrix.util;

import com.netflix.hystrix.HystrixInvokableInfo;
import com.netflix.hystrix.HystrixRequestLog;

/**
 * This class implements logging all Hystrix command details including useful Http headers.

 * @author saung
 * @since 6/5/17
 */
public class HystrixLog {
    /** Circuit breaker status */
    private static final String YES = "yes";
    private static final String NO = "no";

    /**
     * Iterates all executed Hystrix commands and logs each command metrics.
     *
     * @param responseCode - Http response code.
     * @param processingTime - the total time to process the request.
     * @return a StringBuilder with Hystrix command details.
     */
    public static StringBuilder logMetrics(int responseCode, long processingTime) {
        final StringBuilder stringBuilder = new StringBuilder("type=hystrix;");
        stringBuilder.append("response_code=").append(responseCode).append(";")
                .append("processing_time=").append(String.valueOf(processingTime)).append(";");
        final HystrixRequestLog hystrixRequestLog = HystrixRequestLog.getCurrentRequest();
        if (hystrixRequestLog == null) {
            stringBuilder.append("reason=HystrixRequestLog.getCurrentRequest() returned null. Try HystrixRequestContext.initializeContext()");
            return stringBuilder;
        }
        long dependencyExeTime = 0;
        boolean isAllSuccessful = true;
        for (HystrixInvokableInfo<?> command : hystrixRequestLog.getAllExecutedCommands()) {
            String cmdName = command.getCommandKey().name();
            if (command.getExecutionTimeInMilliseconds() > 0 ) {
                dependencyExeTime += command.getExecutionTimeInMilliseconds();
            }
            stringBuilder.append(cmdName).append("_is_success=")
                    .append((command.isSuccessfulExecution() ? YES : NO)).append(";")
                    .append(cmdName).append("_is_timeout=")
                    .append((command.isResponseTimedOut() ? YES : NO)).append(";")
                    .append(cmdName).append("_is_cb_open=")
                    .append((command.isCircuitBreakerOpen() ? YES : NO)).append(";")
                    .append(cmdName).append("_exe_time=")
                    .append(String.valueOf(command.getExecutionTimeInMilliseconds())).append(";");

            if (isAllSuccessful && !command.isSuccessfulExecution()) {
                isAllSuccessful = false;
            }

            Throwable failedException = command.getFailedExecutionException();
            if (failedException != null) {
                stringBuilder.append(cmdName).append("_failed_exception=")
                        .append(failedException.getClass()).append(";");
            }
        } // end for
        stringBuilder.append("num_dependencies=").append(hystrixRequestLog.getAllExecutedCommands().size()).append(";")
                .append("is_all_dep_successful=").append(isAllSuccessful).append(";")
                .append("dependencies_exe_time=").append(String.valueOf(dependencyExeTime)).append(";")
                .append("code_exe_time=").append(String.valueOf(processingTime - dependencyExeTime)).append(";");

        return stringBuilder;
    }
}