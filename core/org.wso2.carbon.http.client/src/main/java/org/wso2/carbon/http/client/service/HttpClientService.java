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

package org.wso2.carbon.http.client.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.wso2.carbon.http.client.HttpClientImpl;
import org.wso2.carbon.http.client.exception.HttpClientException;

/**
 * This service is to configure the Http Client.
 */
public class HttpClientService {

    private static final Log LOG = LogFactory.getLog(HttpClientService.class);

    public static CloseableHttpClient createClientWithCustomVerifier() throws HttpClientException {
        return HttpClientImpl.createClientWithCustomVerifier();
    }


    public static CloseableHttpClient createSystemClient() {
        return HttpClientImpl.createSystemClient();
    }

    public static CloseableHttpClient createDefaultClient() {
        return HttpClientImpl.createDefaultClient();
    }
}
