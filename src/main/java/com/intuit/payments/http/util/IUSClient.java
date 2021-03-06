/**
 * Copyright 2017 Intuit Inc. All rights reserved. Unauthorized reproduction
 * is a violation of applicable law. This material contains certain
 * confidential or proprietary information and trade secrets of Intuit Inc.
 */
package com.intuit.payments.http.util;

import com.intuit.payments.http.Client;
import com.intuit.payments.http.Response;

import java.util.Map;

import static java.lang.String.format;
/**
 * IUS Client to generate IAM ticket and offline ticket using username and password.
 *
 * @author saung
 * @since 10/16/17
 */
public class IUSClient {
    /** Http Client instance */
    private final Client client;

    /**
     * Default constructor to instantiate Http client with Private auth.
     *
     * @param host - a valid hostname starts with 'https://' and without ending with '/'
     * @param appId - App id
     * @param appSecret - App secret.
     */
    public IUSClient(String host, String appId, String appSecret) {
        this.client = new Client(host);
        this.client.privateAuth(appId, appSecret);
    }

    /**
     * Sets a maximum limit of concurrent connection on a per route to be cached in the
     * {@ClientConnectionPoolManager} pool.
     *
     * @param maxNumConnection -  Maximum number of connection per route in pool.
     *                         Default is 2 concurrent connections per route.
     * @return {@link Client} instance.
     */
    public IUSClient maxConcurrentConnection(int maxNumConnection) {
        client.maxConcurrentConnection(maxNumConnection);
        return this;
    }

    /**
     * Generates IAM ticket from IUS for a given account.
     * Sample Request:
     * {"username": "foo@bar.com","password" : "test1234"}
     *
     * @param username - IAM username.
     * @param password - IAM password.
     * @return Map of IUS response.
     * Sample Response:
     * {"iamTicket":{"ticket":"V1-52-b24iwutcp6ptrn3csxewfd","userId":"123146405308532","agentId":"123146405308532","authenticationLevel":"25","namespaceId":"50000003"},"needContactInfoUpdate":false,"action":"PASS","riskLevel":"LOW"}
     */
    public Map<String, ?> generateTicket(String username, String password) {
        String body = format("{\"username\": \"%s\",\"password\" : \"%s\"}", username, password);

        Response response = client.Request("GenerateIAMTicketCmd","IUSGroup","/v1/iamtickets/sign_in")
                .POST()
                .header("intuit_originatingip", "123.45.67.89")
                .bodyStr(body)
                .execute().raise_for_status();
        return response.map();
    }

    /**
     * Generates an offline ticket from IUS.
     *
     * @param username - IAM username.
     * @param password - IAM password.
     * @return Map of IUS offline ticket response.
     *
     * Response Example: {"offlineTicket":"SUPER_LONG_STRING"}
     */
    public Map<String, ?> generateOfflineTicket(String username, String password) {
        String body = format("{\"username\": \"%s\",\"password\" : \"%s\"}", username, password);

        Response response = client.Request("GenerateOfflineTicketCmd","IUSGroup","/v1/offline_tickets/create_for_system_user")
                .POST()
                .header("intuit_originatingip", "123.45.67.89")
                .bodyStr(body)
                .execute().raise_for_status();
        return response.map();
    }

    /**
     * Generates an offline ticket for system user.
     *
     * NOTE: Default ticket expiry is 24 hours.
     *
     * @param assetIdHeader - Asset id to be set in the "intuit_assetid" request header.
     * @param username - IAM username.
     * @param password - IAM password.
     * @param realmId - System/Robot Realm id.
     * @param audienceList - List of asset id of the services you are trying to call.
     * @return Map of IUS offline ticket response.
     *
     * Response Example: {"offlineTicket":"SUPER_LONG_STRING"}
     */
    public Map<String, ?> generateSysUserOfflineTicket(String assetIdHeader, String username, String password,
                                                       String realmId, String audienceList) {
        String body = format("{" +
                "\"username\": \"%s\"," +
                "\"password\" : \"%s\"," +
                "\"realmId\" : \"%s\"," +
                "\"audiences\" : [%s]" +
                "}", username, password, realmId, audienceList);
        /** Generic offline ticket request for any realm id. */
        if (Util.isNullOrBlank(realmId)) {
            body = format("{" +
                    "\"username\": \"%s\"," +
                    "\"password\" : \"%s\"," +
                    "\"audiences\" : [%s]" +
                    "}", username, password, audienceList);
        }

        Response response = client.Request("GenerateSysUserOfflineTicketCmd","IUSGroup","/v1/offline_tickets/create_for_system_user")
                .POST()
                .header("intuit_originatingip", "123.45.67.89")
                .header("intuit_assetid", assetIdHeader)
                .bodyStr(body)
                .execute().raise_for_status();
        return response.map();
    }

    /**
     * Re-generates offline ticket for target realm.
     *
     * @param assetIdHeader - Asset id to be set in the "intuit_assetid" request header.
     * @param offeringIdHeader - Offering id Http request header.
     * @param targetRealmId - Request or target realm id.
     * @param offlineTicket - Offline ticket generated by generateSysUserOfflineTicket() method.
     * @return Map of IUS offline ticket response.
     *
     * Response Example: {"offlineTicket":"SUPER_LONG_STRING"}
     */
    public Map<String, ?> addPersonaToOfflineTicket(String assetIdHeader, String offeringIdHeader,
                                                       String targetRealmId, String offlineTicket) {
        String body = format("{" +
                        " \"offlineTicket\": \"%s\"," +
                        "\"targetRealmId\" : \"%s\"}",
                offlineTicket, targetRealmId);
        Response response = client.Request("GenerateSysUserOfflineTicketCmd","IUSGroup","/v1/offline_tickets/assume_persona")
                .POST()
                .header("intuit_originatingip", "123.45.67.89")
                .header("intuit_assetid", assetIdHeader)
                .header("intuit_offeringid", offeringIdHeader)
                .bodyStr(body)
                .execute().raise_for_status();
        return response.map();
    }
}
