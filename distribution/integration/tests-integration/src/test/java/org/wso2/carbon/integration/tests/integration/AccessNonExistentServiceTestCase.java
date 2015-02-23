/*
*Copyright (c) 2014​, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.integration.tests.integration;

import org.apache.commons.httpclient.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.carbon.integration.common.utils.CarbonIntegrationBaseTest;

import static org.testng.Assert.assertEquals;

/**
 * Test case for - https://wso2.org/jira/browse/CARBON-11833
 * Test that ?wsdl on a non-existent service returns an HTTP 404
 */
public class AccessNonExistentServiceTestCase extends CarbonIntegrationBaseTest {

    @BeforeClass(alwaysRun = true)
    public void initTests() throws Exception {
        super.init();
    }

    @Test(groups = {"carbon.core"}, description = "Test that ?wsdl on a non-existent service returns an HTTP 404. " +
                                                  "See https://wso2.org/jira/browse/CARBON-11833")
    public void testAccessNonExistentServiceWsdl() throws Exception {
        String URL = contextUrls.getServiceUrl();
        HttpResponse httpResponse = HttpRequestUtil.sendGetRequest(URL + "/XXXFoo", "wsdl");
        assertEquals(httpResponse.getResponseCode(), HttpStatus.SC_NOT_FOUND);
    }
}
