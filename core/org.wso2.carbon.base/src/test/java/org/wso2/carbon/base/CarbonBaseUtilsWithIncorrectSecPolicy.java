/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.base;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * Test class for CarbonBaseUtil related methods with a correct sec policy.
 */
public class CarbonBaseUtilsWithIncorrectSecPolicy {

    @BeforeClass
    public void setSecurityManager() {
        URL resourceURL = CarbonBaseUtilsTest.class.getClassLoader().getResource("");
        if (resourceURL != null) {
            String resourcePath = resourceURL.getPath();
            resourcePath = resourcePath + "incorrect-test-policy.policy";
            System.setProperty("java.security.policy", resourcePath);
            System.setSecurityManager(new SecurityManager());
        }
    }

    @Test(groups = {"org.wso2.carbon.base"}, expectedExceptions = java.security.AccessControlException.class)
    public void testCheckSecurityWithClasses() {
        List<String> allowedClasses = Arrays.asList("org.junit.runners.model.FrameworkMethod$1.runReflectiveCall",
                "org.wso2.carbon.base.CarbonBaseUtils.checkSecurity", "org.junit.internal.runners.statements" +
                        ".InvokeMethod.evaluate", "org.junit.runner.JUnitCore.run", "org.junit.runners.ParentRunner.run");
        CarbonBaseUtils.checkSecurity(allowedClasses);
    }
}
