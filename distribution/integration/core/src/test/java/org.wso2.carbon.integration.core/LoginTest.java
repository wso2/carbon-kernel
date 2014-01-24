/**
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.integration.core;

import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Test for log in to a Carbon server instance
 */

public class LoginTest extends TestTemplate {

    private static final Log log = LogFactory.getLog(LoginTest.class);

    @Override
    public void init() {

    }

    /**
     * Test to check the login status of Carbon server
     */

    @Override
    public void runSuccessCase() {
        ServerLogin serverLogin = new ServerLogin();
        try {
            String sessionCookie = serverLogin.login();
            if (sessionCookie.equals(null)) {
                Assert.fail("Sorry dude....Login Failed!!!!!!!!!!");
            }


        } catch (Exception e) {
            log.error("Error occurred while logging in" + e.getMessage());
        }
    }

    @Override
    public void runFailureCase() {

    }

    @Override
    public void cleanup() {

    }
}
