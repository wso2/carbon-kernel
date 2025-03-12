/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.http.client;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.client5.http.ssl.TlsSocketStrategy;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.wso2.carbon.http.client.exception.HttpClientException;

import javax.net.ssl.HostnameVerifier;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class PoolingConnectionHttpClientImpl {
    private static final int MAX_TOTAL_CONNECTIONS = 100;
    private static final int MAX_CONNECTIONS_PER_ROUTE = 5;

    private static final int SOCKET_TIMEOUT_MINUTES = 1;
    private static final int CONNECTION_SOCKET_TIMEOUT_MINUTES = 1;
    private static final int CONNECT_TIMEOUT_MINUTES = 1;
    private static final int TIME_TO_LIVE_MINUTES = 10;
//
//    public PoolingConnectionHttpClientImpl() {
//        super(getConnectionManager());
//    }

//    public PoolingConnectionHttpClientImpl(
//            int maxTotalConnections,
//            int maxConnectionsPerRoute,
//            int socketTimeoutMinutes,
//            int connectionSocketTimeoutMinutes,
//            int connectTimeoutMinutes,
//            int timeToLiveMinutes
//    ) {
//        super();
//
//        MAX_TOTAL_CONNECTIONS = maxTotalConnections;
//        MAX_CONNECTIONS_PER_ROUTE = maxConnectionsPerRoute;
//        SOCKET_TIMEOUT_MINUTES = socketTimeoutMinutes;
//        CONNECTION_SOCKET_TIMEOUT_MINUTES = connectionSocketTimeoutMinutes;
//        CONNECT_TIMEOUT_MINUTES = connectTimeoutMinutes;
//        TIME_TO_LIVE_MINUTES = timeToLiveMinutes;
//
//        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = getConnectionManager();
//
//        poolingHttpClientConnectionManager.setDefaultMaxPerRoute(maxConnectionsPerRoute);
//        poolingHttpClientConnectionManager.setMaxTotal(maxTotalConnections);
//        poolingHttpClientConnectionManager.setDefaultSocketConfig(SocketConfig.custom()
//                .setSoTimeout(Timeout.ofMinutes(socketTimeoutMinutes))
//                .build());
//        poolingHttpClientConnectionManager.setDefaultConnectionConfig(ConnectionConfig.custom()
//                .setSocketTimeout(Timeout.ofMinutes(connectionSocketTimeoutMinutes))
//                .setConnectTimeout(Timeout.ofMinutes(connectTimeoutMinutes))
//                .setTimeToLive(TimeValue.ofMinutes(timeToLiveMinutes))
//                .build());
//        super.setConnectionManager(poolingHttpClientConnectionManager);
//    }

    public static PoolingHttpClientConnectionManager getConnectionManagerWithCustomVerifier
            (HostnameVerifier hostnameVerifier) throws HttpClientException {

        // TODO - May need to remove trusting all certificates
        // Create a custom SSL context to trust all certificates (including self-signed)
        SSLContextBuilder sslContextBuilder = null;  // Trust all certificates
        try {
            sslContextBuilder = SSLContexts.custom()
                    .loadTrustMaterial((chain, authType) -> true);
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            throw new HttpClientException("Error occurred while creating the SSL context.", e);
        }

        TlsSocketStrategy tlsSocketStrategy = null;
        // Create the PoolingHttpClientConnectionManager with SSL support
        // Use the custom SSL context
        try {
            tlsSocketStrategy = (TlsSocketStrategy) ClientTlsStrategyBuilder.create()
                    .setHostnameVerifier(hostnameVerifier)
                    .setSslContext(sslContextBuilder.build())  // Use the custom SSL context
                    .setTlsVersions(TLS.V_1_3)
                    .build();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new HttpClientException("Error occurred while creating the TLS socket strategy.", e);
        }

        return getConnectionManager(tlsSocketStrategy);
    }

    public static PoolingHttpClientConnectionManager getConnectionManager(TlsSocketStrategy tlsSocketStrategy) {

        SocketConfig socketConfig = SocketConfig.custom()
                .setSoTimeout(Timeout.ofMinutes(SOCKET_TIMEOUT_MINUTES))
                .build();
        PoolConcurrencyPolicy poolConcurrencyPolicy = PoolConcurrencyPolicy.STRICT;
        PoolReusePolicy poolReusePolicy = PoolReusePolicy.LIFO;
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setSocketTimeout(Timeout.ofMinutes(CONNECTION_SOCKET_TIMEOUT_MINUTES))
                .setConnectTimeout(Timeout.ofMinutes(CONNECT_TIMEOUT_MINUTES))
                .setTimeToLive(TimeValue.ofMinutes(TIME_TO_LIVE_MINUTES))
                .build();

        return getConnectionManager(
                tlsSocketStrategy,
                socketConfig,
                poolConcurrencyPolicy,
                poolReusePolicy,
                connectionConfig
        );
    }

    private static PoolingHttpClientConnectionManager getConnectionManager(
            TlsSocketStrategy tlsSocketStrategy,
            SocketConfig socketConfig,
            PoolConcurrencyPolicy poolConcurrencyPolicy,
            PoolReusePolicy poolReusePolicy,
            ConnectionConfig connectionConfig
    )
    {
        return PoolingHttpClientConnectionManagerBuilder.create()
                .setTlsSocketStrategy(tlsSocketStrategy)
                .setDefaultSocketConfig(socketConfig)
                .setPoolConcurrencyPolicy(poolConcurrencyPolicy)
                .setConnPoolPolicy(poolReusePolicy)
                .setDefaultConnectionConfig(connectionConfig)
                .setMaxConnPerRoute(PoolingConnectionHttpClientImpl.MAX_CONNECTIONS_PER_ROUTE)
                .setMaxConnTotal(PoolingConnectionHttpClientImpl.MAX_TOTAL_CONNECTIONS)
                .build();
    }
}
