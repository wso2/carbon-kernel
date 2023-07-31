/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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
package org.wso2.carbon.utils;

import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;

import static org.wso2.carbon.CarbonConstants.ALLOW_ALL;
import static org.wso2.carbon.CarbonConstants.DEFAULT_AND_LOCALHOST;
import static org.wso2.carbon.CarbonConstants.HOST_NAME_VERIFIER;

/**
 * Util methods for HTTP Client.
 */
public class HTTPClientUtils {


    private HTTPClientUtils() {
        //disable external instantiation
    }

    /**
     * Get the httpclient builder with custom hostname verifier.
     *
     * @return HttpClientBuilder.
     */
    public static HttpClientBuilder createClientWithCustomVerifier() {

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create().useSystemProperties();
        if (DEFAULT_AND_LOCALHOST.equals(System.getProperty(HOST_NAME_VERIFIER))) {
            X509HostnameVerifier hostnameVerifier = new CustomHostNameVerifier();
            httpClientBuilder.setHostnameVerifier(hostnameVerifier);
        } else if (ALLOW_ALL.equals(System.getProperty(HOST_NAME_VERIFIER))) {
            httpClientBuilder.setHostnameVerifier(new AllowAllHostnameVerifier());
        }

        return httpClientBuilder;
    }
}
