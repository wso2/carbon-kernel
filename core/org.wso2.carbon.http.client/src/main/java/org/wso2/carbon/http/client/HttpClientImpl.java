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

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.core5.http.HttpEntity;
import org.wso2.carbon.http.client.exception.HttpClientException;
import org.wso2.carbon.utils.CustomHostNameVerifier;

import javax.net.ssl.HostnameVerifier;
import java.io.IOException;
import java.io.InputStream;

import static org.wso2.carbon.CarbonConstants.ALLOW_ALL;
import static org.wso2.carbon.CarbonConstants.DEFAULT_AND_LOCALHOST;
import static org.wso2.carbon.CarbonConstants.HOST_NAME_VERIFIER;

/**
 * Class to create Http clients with connection managers.
 */
public abstract class HttpClientImpl implements HttpClient, CloseableHttpClientFactory {

//    // --- deprecated ---
//    protected HttpClientConnectionManager connectionManager;
//
//    protected HttpClientImpl() {
//    }
//
//    protected HttpClientImpl(HttpClientConnectionManager connectionManager) {
//        this.connectionManager = connectionManager;
//    }
//
//    protected void setConnectionManager(HttpClientConnectionManager connectionManager) {
//        this.connectionManager = connectionManager;
//    }
//    // -------
//

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

//    /**
//     * @param url: URL to send the GET request.
//     * @return InputStream: Response content as an input stream.
//     */
//    @Override
//    public InputStream get(String url) throws HttpClientException {
//
//        HttpGet getUrl = new HttpGet(url);
//
//        try (CloseableHttpClient httpClient = getClient()) {
//            return httpClient.execute(getUrl, response -> {
//                HttpEntity entity = response.getEntity();
//                if (entity != null) {
//                    return entity.getContent();
//                } else {
//                    return null;
//                }
//            });
//        } catch (IOException e) {
//            throw new HttpClientException("Error occurred while executing the GET request.", e);
//        }
//    }
//
//    /**
//     * Close the connection manager.
//     *
//     * @throws HttpClientException
//     */
//    @Override
//    public void closeConnectionManager() throws HttpClientException {
//        connectionManager.close();
//    }

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
