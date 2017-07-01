/**
 * Copyright 2017 Intuit Inc. All rights reserved. Unauthorized reproduction
 * is a violation of applicable law. This material contains certain
 * confidential or proprietary information and trade secrets of Intuit Inc.
 */
package com.intuit.payments.hystrix.auth;

import com.intuit.payments.hystrix.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.intuit.payments.hystrix.util.Util.checkStringIsNotBlank;
import static java.lang.String.format;

/**
 * Intuit IAM Private Auth Plus implementation.
 *
 * @author saung
 * @since 6/29/17
 */
public class PrivateAuthPlus implements AuthInterface {
    /** Logger instance */
    private static final Logger log = LoggerFactory.getLogger(PrivateAuthPlus.class);

    /**
     *  Private Auth Plus Header
     * Example:
     *
     * Authorization: Intuit_IAM_Authentication
     *  intuit_appid=Intuit.cto.api.gateway.simulator.test,
     *  intuit_app_secret=ctyj58n8VZvAqpl3W4pkn7,
     *  intuit_token_type=IAM-Ticket,
     *  intuit_realmid=50000003, [OPTIONAL]
     *  intuit_token=V1-65-Q3y8elbiw74j5wdna2cei5,
     *  intuit_userid=100186433
     **/

    /** Private Auth Format. NOTE: QBO V3 does NOT like having space between app id and app secret! */
    private static final String IAM_APP_ID_APP_SECRET = "Intuit_IAM_Authentication intuit_appid=%s,intuit_app_secret=%s";

    /** Intuit Private Auth Header value */
    private final String iamAuth;

    /**
     * Default constructor.
     *
     * @param appId - a client appId string.
     * @param appSecret - a client appSecret string.
     */
    public PrivateAuthPlus(String appId, String appSecret) {
        checkStringIsNotBlank(appId, "appId must not be null or empty");
        checkStringIsNotBlank(appSecret, "appSecret must not be null or empty");
        iamAuth = format(IAM_APP_ID_APP_SECRET, appId, appSecret);
        log.info("type=auth_init;".concat("private_auth_plus_header={}"), iamAuth.substring(0, 40));
    }

    /**
     * Gets a Http Basic Auth header value.
     *
     * @param preRequestParams - Per-request Auth parameters.
     * @return a Base64 encoded username : password.
     */
    @Override
    public String getAuthHeader(String... preRequestParams) {
        if (preRequestParams != null && preRequestParams.length == 2) {
            String plus = Util.plus(preRequestParams[0], preRequestParams[1]);
            return iamAuth + plus;
        } else if(preRequestParams != null && preRequestParams.length == 3) {
            String plus = Util.plus(preRequestParams[0], preRequestParams[1], preRequestParams[2]);
            return iamAuth + plus;
        } else {
            return iamAuth;
        }
    }
}
