/**
 * Copyright 2017 Intuit Inc. All rights reserved. Unauthorized reproduction
 * is a violation of applicable law. This material contains certain
 * confidential or proprietary information and trade secrets of Intuit Inc.
 */
package com.intuit.payments.http;

import com.intuit.payments.http.auth.*;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;

import static com.intuit.payments.http.util.Util.checkStringIsNotBlank;
import static com.intuit.payments.http.util.Util.getFullURL;
import static org.apache.http.HttpHeaders.AUTHORIZATION;

/**
 * This base class implements a wrapper for {@HttpHystrixCommand} and provides
 * utility like simple GET and formPOST. If you want to implement other Http methods, you can
 * add more here.
 *
 * NOTE: Both serverBaseUrl and authHeader are instance variables and the same instance should not be
 * shared with different clients. If you need per-request auth, you should alter the implementation.
 *
 * @author saung
 * @since 4/27/17
 */
public class Client {
    /** Logger instance */
    private static final Logger log = LoggerFactory.getLogger(Client.class);

    /**
     * Http client connection pool manager instance.
     */
    private final PoolingHttpClientConnectionManager connectionManager;

    /**
     * Default period of inactivity in milliseconds after which persistent connections must be re-validated.
     * 1 min = 60000 ms
     */
    private static final int DEFAULT_CONNECTION_POOL_VALIDATE_AFTER_INACTIVITY = 60000;

    /** target host URL */
    private final String serverBaseUrl;

    /** Http Auth interface */
    private AuthInterface authInterface;

    /** Time to wait to get a connection */
    private int connectionTimeoutInMilliSec;

    /** Time to wait to send a request and receive a response */
    private int socketTimeoutInMilliSec;

    /**
     * Default constructor
     *
     * @param serverBaseUrl - a target host URL string.
     */
    public Client(String serverBaseUrl) {
        this(serverBaseUrl, null);
    }

    /**
     * constructor with url and custom sslContext
     *
     * @param serverBaseUrl - a target host URL string.
     * @oaram sslContext - sslContext for certificate setup
     */
    public Client(String serverBaseUrl, SSLContext sslContext) {
        checkStringIsNotBlank(serverBaseUrl, "serverBaseUrl must not be null or empty");
        this.serverBaseUrl = serverBaseUrl;
        /** Default is no auth! */
        this.authInterface = (x) -> null;
        /** Default 10 seconds timeout to get a network connection to server. */
        this.connectionTimeoutInMilliSec = 10000;

        /** Default 60 seconds timeout to receive individual packets */
        this.socketTimeoutInMilliSec = 60000;
        boolean isSSLContextNull = null == sslContext;
        this.connectionManager = isSSLContextNull ?
                new PoolingHttpClientConnectionManager() : new PoolingHttpClientConnectionManager(getRegistryBuilder(sslContext));
        this.connectionManager.setValidateAfterInactivity(DEFAULT_CONNECTION_POOL_VALIDATE_AFTER_INACTIVITY);
        log.info("type=init;".concat("host={};isSSLContextNull={};default_conn_timeout={};default_socket_timeout={}"),
                this.serverBaseUrl, isSSLContextNull, this.connectionTimeoutInMilliSec, this.socketTimeoutInMilliSec);
    }

    /**
     * Enables Http Basic Auth.
     *
     * @param username_password - a comma-separated username and password string.
     * @return {@link Client} instance.
     */
    public Client httpBasicAuth(String username_password) {
        authInterface = new HttpBasic(username_password);
        return this;
    }

    /**
     * Enables IAM Private Auth.
     *
     * @param appId - a client appId string.
     * @param appSecret - a client appSecret string.
     * @return {@link Client} instance.
     */
    public Client privateAuth(String appId, String appSecret) {
        authInterface = new PrivateAuth(appId, appSecret);
        return this;
    }

    /**
     * Enables IAM Private Auth Plus, but set app id and app secret for now.
     *
     * NOTE: Each request must call withPrivateAuthPlus() before execute()
     *
     * @param appId - a client appId string.
     * @param appSecret - a client appSecret string.
     * @return {@link Client} instance.
     */
    public Client privateAuthPlus(String appId, String appSecret) {
        authInterface = new PrivateAuthPlus(appId, appSecret);
        return this;
    }

    /**
     * Enables IAM Offline ticket auth in this Client instance.
     *
     * @param appId - a client appId string.
     * @param appSecret - a client appSecret string.
     * @return {@link Client} instance.
     */
    public Client offlineTicket(String appId, String appSecret) {
        authInterface = new OfflineTicket(appId, appSecret);
        return this;
    }

    /**
     * Sets a custom auth implementation.
     *
     * @param authImpl - your own implementation of {@link AuthInterface}.
     * @return {@link Client} instance.
     */
    public Client customAuth(AuthInterface authImpl) {
        this.authInterface = authImpl;
        return this;
    }

    /**
     * Sets the Http connection timeout in millisecond
     *
     * @param connectionTimeoutInMilliSec -  the timeout in millisecond
     * @return {@link Client} instance.
     */
    public Client connectionTimeoutInMilliSec(int connectionTimeoutInMilliSec) {
        this.connectionTimeoutInMilliSec = connectionTimeoutInMilliSec;
        return this;
    }

    /**
     * Sets the Http socket timeout
     *
     * @param socketTimeoutInMilliSec - the Http Socket timeout in milliseconds.
     * @return {@link Client} instance.
     */
    public Client socketTimeoutInMilliSec(int socketTimeoutInMilliSec) {
        this.socketTimeoutInMilliSec = socketTimeoutInMilliSec;
        return this;
    }

    /**
     * Defines period of inactivity in milliseconds after which persistent connections
     * must be re-validated prior to being leased to the consumer.
     * Non-positive value passed to this method disables connection validation.
     * This check helps detect connections that have become stale (half-closed) while kept inactive in the pool.
     *
     * @param milliseconds - inactivity in milliseconds to revalidate the connection. Default is 1 min.
     * @return {@link Client} instance.
     */
    public Client validateConnectionAfterInactivity(int milliseconds) {
        connectionManager.setValidateAfterInactivity(milliseconds);
        return this;
    }

    /**
     * Sets a maximum limit of concurrent connection on a per route to be cached in the
     * {@ClientConnectionPoolManager} pool.
     *
     * @param maxNumConnection -  Maximum number of connection per route in pool.
     *                         Default is 2 concurrent connections per route.
     * @return {@link Client} instance.
     */
    public Client maxConcurrentConnection(int maxNumConnection) {
        connectionManager.setDefaultMaxPerRoute(maxNumConnection);
        return this;
    }

    /**
     * Creates new {@link Request} instance.
     *
     * @param endpointName - API endpoint name a.k.a. Hystrix command name. E.g. "GetUsers"
     * @param endpointGroup - API endpoint group a.k.a. Hystrix command group name. E.g. "UsersGroup"
     * @param urlPath - an API Name a.k.a. a mapping key to a path: "users" -> "/v1/users/{0}"
     * @param urlPathValues - an optional varargs for the URL template.. E.g. user id "{0}" -> 123
     * @return new {@link Request} instance.
     */
    public Request Request(String endpointName, String endpointGroup,
                           String urlPath, Object... urlPathValues) {
        return new Request(connectionManager,
                getFullURL(serverBaseUrl, urlPath, urlPathValues),
                endpointName,
                endpointGroup,
                connectionTimeoutInMilliSec,
                socketTimeoutInMilliSec)
                .header(AUTHORIZATION, authInterface.getAuthHeader());
    }

    /**
     * Sets the Http authorization header in a given Request instance.
     *
     * @param request - A Request instance.
     * @param ticket - Optional IAM session ticket for PA+ or super long offline ticket.
     * @param userId - Optional IAM user id for PA+.
     * @return a Request instance.
     */
    public Request withAuthHeader(Request request, String ticket, String userId) {
        return request.header(AUTHORIZATION, authInterface.getAuthHeader(ticket, userId));
    }

    /**
     * Build registry for sslContext
     * @param sslContext
     * @return Register with SSLConnectionSocketFactory
     */
    public Registry<ConnectionSocketFactory> getRegistryBuilder (SSLContext sslContext) {
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
                .<ConnectionSocketFactory> create()
                .register("https", new SSLConnectionSocketFactory(sslContext))
                .build();
        return socketFactoryRegistry;
    }
}
