/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.http.client.services;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.wso2.carbon.http.client.ClientUtils;
import org.wso2.carbon.http.client.cache.HttpClientCache;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Callable;

import javax.net.ssl.HttpsURLConnection;

public class HttpClientServiceImpl implements HttpClientService {

    @Override
    public CloseableHttpClient getClosableHttpClient(String key, Callable<CloseableHttpClient> loader) {

        return HttpClientCache.getInstance().get(key, loader);
    }

    @Override
    public CloseableHttpClient getClosableHttpClient(String key) {

            return HttpClientCache.getInstance().get(key, ClientUtils::createClient);
    }

    @Override
    public HttpsURLConnection getHttpsURLConnection(URL url, String httpMethod)
            throws IOException {

        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod(httpMethod);
        return connection;
    }
}
