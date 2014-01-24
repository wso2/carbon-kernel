/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.integration.tests;

import org.apache.http.HttpStatus;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.HttpRequestUtil;
import org.wso2.carbon.integration.framework.HttpResponse;

import static org.testng.Assert.assertEquals;

/**
 * Test for Jira issue https://wso2.org/jira/browse/CARBON-11833
 */
public class AccessNonExistentServiceTestCase {

    @Test(groups = {"carbon.core"},
          description = "Test that ?wsdl on a non-existent service returns an HTTP 404. " +
                        "See https://wso2.org/jira/browse/CARBON-11833")
    public void accessNonExistentServiceWsdl() throws Exception {
        ClientConnectionUtil.waitForPort(9763);
        HttpResponse httpResponse = HttpRequestUtil.sendGetRequest("http://localhost:9763/services/XXXFoo",
                                                                   "wsdl");
        assertEquals(httpResponse.getResponseCode(), HttpStatus.SC_NOT_FOUND);
    }
}
