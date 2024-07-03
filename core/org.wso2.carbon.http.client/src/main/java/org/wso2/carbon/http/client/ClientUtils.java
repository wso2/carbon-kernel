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
package org.wso2.carbon.http.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;

public class ClientUtils {

    private static final Log log = LogFactory.getLog(ClientUtils.class);

    private ClientUtils() {
    }

    public static CloseableHttpClient createClient() {

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create().useSystemProperties();
        if (log.isDebugEnabled()) {
            log.debug("Creating a new HttpClient instance");
        }
        return httpClientBuilder.build();

    }
}
