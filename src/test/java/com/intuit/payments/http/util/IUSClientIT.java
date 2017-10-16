/**
 * Copyright 2017 Intuit Inc. All rights reserved. Unauthorized reproduction
 * is a violation of applicable law. This material contains certain
 * confidential or proprietary information and trade secrets of Intuit Inc.
 */
package com.intuit.payments.http.util;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Integrate test for IUSClient.
 *
 * @author saung
 * @since 10/16/17.
 */
public class IUSClientIT {

    private IUSClient iusClient;

    @Test
    public void generateTicket() throws Exception {
        iusClient = new IUSClient("https://accounts-prf.platform.intuit.net", "Intuit.platform.wallettestcto.perftesting", "preprdJ1RSNFfRl3JukEcmbutteVOIcskZjKCssq");
        assertNotNull(iusClient);
        Map<String, ?> resp = iusClient.generateTicket("android.ihg1+1467913776925+iamtestpass@gmail.com", "test1234");
        assertNotNull(resp);
        Map<String, ?> iamTicket = (Map)resp.get("iamTicket");
        assertNotNull(iamTicket);
        assertTrue(String.valueOf(iamTicket.get("ticket")).startsWith("V1-"));
        assertEquals("123146405308532", iamTicket.get("userId"));
    }
}