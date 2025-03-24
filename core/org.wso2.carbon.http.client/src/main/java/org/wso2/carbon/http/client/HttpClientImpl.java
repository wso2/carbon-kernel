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

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.wso2.carbon.utils.CustomHostNameVerifier;

import javax.net.ssl.HostnameVerifier;

import static org.wso2.carbon.CarbonConstants.ALLOW_ALL;
import static org.wso2.carbon.CarbonConstants.DEFAULT_AND_LOCALHOST;
import static org.wso2.carbon.CarbonConstants.HOST_NAME_VERIFIER;

/**
 * Class to create Http clients with connection managers.
 */
public abstract class HttpClientImpl implements HttpClient, CloseableHttpClientFactory {

    /**
     * Create a http client with system properties.
     *
     * @return CloseableHttpClient.
     */
    public static CloseableHttpClient createSystemClient() {

        return HttpClients.custom()
                .useSystemProperties()
                .setConnectionManager(PoolingConnectionHttpClientImpl.getConnectionManager())
                .setConnectionManagerShared(true)
                .build();
    }

    /**
     * Create a http client.
     *
     * @return CloseableHttpClient.
     */
    public static CloseableHttpClient createDefaultClient() {

        return HttpClients.custom()
                .setConnectionManager(PoolingConnectionHttpClientImpl.getConnectionManager())
                .setConnectionManagerShared(true)
                .build();
    }

    /**
     * Get a httpclient with custom hostname verifier.
     *
     * @return CloseableHttpClient.
     */
    public static CloseableHttpClient createClientWithCustomVerifier() {

        HttpClientBuilder httpClientBuilder = HttpClients.custom().useSystemProperties();

        HostnameVerifier hostnameVerifier;
        if (DEFAULT_AND_LOCALHOST.equals(System.getProperty(HOST_NAME_VERIFIER))) {
            hostnameVerifier = new CustomHostNameVerifier();
        } else if (ALLOW_ALL.equals(System.getProperty(HOST_NAME_VERIFIER))) {
            hostnameVerifier = NoopHostnameVerifier.INSTANCE;
        } else {
            hostnameVerifier = null;
        }

        httpClientBuilder.setConnectionManager(
                PoolingConnectionHttpClientImpl.getConnectionManagerWithCustomVerifier(hostnameVerifier))
                .setConnectionManagerShared(true);

        return httpClientBuilder.build();
    }
}
