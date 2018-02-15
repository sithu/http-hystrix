/**
 * Copyright 2018 Intuit Inc. All rights reserved. Unauthorized reproduction
 * is a violation of applicable law. This material contains certain
 * confidential or proprietary information and trade secrets of Intuit Inc.
 */
package com.intuit.payments.http.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.intuit.payments.http.util.Util.checkStringIsNotBlank;
import static java.lang.String.format;

/**
 * Intuit IAM offline ticket implementation.
 *
 * @author saung
 * @since 2/14/18
 */
public class OfflineTicket implements AuthInterface {
    /** Logger instance */
    private static final Logger log = LoggerFactory.getLogger(OfflineTicket.class);

    /**
     * Offline ticket header example:
     *
     * Authorization:
     * Intuit_IAM_Authentication intuit_token_type=IAM-Offline-Ticket,
     * intuit_appid=<YOUR_APP_ID>,intuit_app_secret=<YOUR_APP_SECRET>,
     * intuit_token=<OFFLINE_TICKET_GENERATED_VIA_IAMOfflineTicketClient>
     **/
    private static final String OFFLINE_APP_ID_APP_SECRET = "Intuit_IAM_Authentication intuit_token_type=IAM-Offline-Ticket,"
            + "intuit_appid=%s,intuit_app_secret=%s";

    /** Intuit Offline Ticket Header value */
    private final String iamAuth;

    /**
     * Default constructor.
     *
     * @param appId - a client appId string.
     * @param appSecret - a client appSecret string.
     */
    public OfflineTicket(String appId, String appSecret) {
        checkStringIsNotBlank(appId, "appId must not be null or empty");
        checkStringIsNotBlank(appSecret, "appSecret must not be null or empty");
        iamAuth = format(OFFLINE_APP_ID_APP_SECRET, appId, appSecret);
        log.info("type=auth_init;".concat("offline_ticket_header={}"), iamAuth.substring(0, 77));
    }

    /**
     * Gets a complete offline ticket auth header value.
     *
     * @param preRequestParams - Per-request Auth parameters.
     * @return an offline ticket auth header value.
     */
    @Override
    public String getAuthHeader(String... preRequestParams) {
        if (preRequestParams != null && preRequestParams.length >= 1) {
            return iamAuth + ",intuit_token=" + preRequestParams[0];
        } else {
            throw new IllegalArgumentException("The offline ticket argument was missing!");
        }
    }
}
