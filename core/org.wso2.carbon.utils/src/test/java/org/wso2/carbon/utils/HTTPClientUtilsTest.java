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

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.BaseTest;

import java.lang.reflect.Field;

import static org.wso2.carbon.CarbonConstants.ALLOW_ALL;
import static org.wso2.carbon.CarbonConstants.DEFAULT_AND_LOCALHOST;
import static org.wso2.carbon.CarbonConstants.HOST_NAME_VERIFIER;

public class HTTPClientUtilsTest extends BaseTest {

    public static final String DUMMY_PROPERTY = "dummy";

    @AfterMethod
    public void tearDown() {
    }

    @DataProvider(name = "hostnameVerifierDataProvider")
    public Object[][] hostnameVerifierDataProvider() {
        return new Object[][]{
                {DEFAULT_AND_LOCALHOST, CustomHostNameVerifier.class},
                {ALLOW_ALL, AllowAllHostnameVerifier.class},
                {DUMMY_PROPERTY, null}
        };
    }

    @Test(dataProvider = "hostnameVerifierDataProvider")
    public void testCreateClientWithCustomVerifierDefaultAndLocalHost(String hostnameVerifierType,
          Class<? extends X509HostnameVerifier> expectedVerifier) throws NoSuchFieldException, IllegalAccessException {

        System.setProperty(HOST_NAME_VERIFIER, hostnameVerifierType);

        HttpClientBuilder httpClientBuilder = HTTPClientUtils.createClientWithCustomVerifier();

        // Use reflection APIs to get the hostname verifier field value
        Class<HttpClientBuilder> httpClientBuilderClass = HttpClientBuilder.class;
        Field hostnameVerifierField = httpClientBuilderClass.getDeclaredField("hostnameVerifier");
        hostnameVerifierField.setAccessible(true);

        // If the hostname verifier property is set to "DEFAULT_AND_LOCALHOST" or "ALLOW_ALL", the hostname verifier
        // field in the httpClientBuilder should be set with an instance of the expected verifier class.
        // Otherwise, it should be null.
        X509HostnameVerifier hostnameVerifier = (X509HostnameVerifier) hostnameVerifierField.get(httpClientBuilder);
        if (expectedVerifier == null) {
            Assert.assertNull(hostnameVerifier);
        } else {
            Assert.assertTrue(expectedVerifier.isInstance(hostnameVerifier));
        }
    }
}
