/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
*KIND, either express or implied. See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.integration.common.utils;


import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.beans.ContextUrls;

public abstract class CarbonIntegrationBaseTest {

    protected ContextUrls contextUrls = new ContextUrls();
    protected AutomationContext context;
    protected String sessionCookie;
    protected TestUserMode userMode;

    protected void init() throws Exception {
        userMode = TestUserMode.SUPER_TENANT_ADMIN;
        init(userMode);
    }

    protected void init(TestUserMode userMode) throws Exception {
        context = new AutomationContext("CARBON", userMode);
        contextUrls = context.getContextUrls();

    }


}
