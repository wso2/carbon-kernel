/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.launcher.test;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.launcher.Constants;
import org.wso2.carbon.launcher.utils.Utils;

/**
 * Launcher Utils test class.
 *
 * @since 5.0.0
 */

@Test(groups = "utils")
public class UtilsTest extends BaseTest {

    public void substituteVarsTest() {
        System.setProperty(Constants.PROFILE, Constants.DEFAULT_PROFILE);
        String inputStr = "file:${runtime}";
        String expectedOutputStr = "file:default";

        String outputStr = Utils.initializeSystemProperties(inputStr);
        Assert.assertEquals(outputStr, expectedOutputStr);
    }

    public void substituteVarsTest2() {
        System.setProperty(Constants.PROFILE, Constants.DEFAULT_PROFILE);
        System.setProperty(Constants.CARBON_HOME, "/home/user/wso2carbon-kernel-5.0.0");
        System.setProperty(Constants.CARBON_PROFILE_REPOSITORY, Constants.PROFILE_REPOSITORY);
        String inputStr = "file:${carbon.home}/${carbon.runtime.repository}/${runtime}";
        String expectedOutputStr = "file:/home/user/wso2carbon-kernel-5.0.0/wso2/default";

        String outputStr = Utils.initializeSystemProperties(inputStr);
        Assert.assertEquals(outputStr, expectedOutputStr);
        setupCarbonHome();
    }

    public void substituteVarsTest3() {
        System.setProperty(Constants.PROFILE, Constants.DEFAULT_PROFILE);
        String inputStr = "${runtime}";
        String expectedOutputStr = "default";

        String outputStr = Utils.initializeSystemProperties(inputStr);
        Assert.assertEquals(outputStr, expectedOutputStr);
    }

    public void stringTokenizeTest() {
        String str = "file:plugins/org.eclipse.equinox.simpleconfigurator_1.1.200.v20160504-1450.jar@1:true," +
                "file:plugins/org.apache.felix.gogo.runtime_0.10.0.v201209301036.jar@2:true," +
                "file:plugins/org.apache.felix.gogo.command_0.10.0.v201209301215.jar@2:true," +
                "file:plugins/org.apache.felix.gogo.shell_0.10.0.v201212101605.jar@2:true," +
                "file:plugins/org.eclipse.equinox.console_1.1.200.v20150929-1405.jar@2:true";

        String[] expectedArray = new String[] {
                "file:plugins/org.eclipse.equinox.simpleconfigurator_1.1.200.v20160504-1450.jar@1:true",
                "file:plugins/org.apache.felix.gogo.runtime_0.10.0.v201209301036.jar@2:true",
                "file:plugins/org.apache.felix.gogo.command_0.10.0.v201209301215.jar@2:true",
                "file:plugins/org.apache.felix.gogo.shell_0.10.0.v201212101605.jar@2:true",
                "file:plugins/org.eclipse.equinox.console_1.1.200.v20150929-1405.jar@2:true" };

        String[] output = Utils.tokenize(str, ",");
        Assert.assertEquals(output, expectedArray);
    }

    public void stringTokenizeTest2() {
        String equinoxSimpleConfiguratorVersion = System.getProperty("equinox.simpleconfigurator.version");
        String str = "file:plugins/org.eclipse.equinox.simpleconfigurator_" + equinoxSimpleConfiguratorVersion
                + ".jar@1:true";

        String[] expectedArray = new String[] {
                "file:plugins/org.eclipse.equinox.simpleconfigurator_" + equinoxSimpleConfiguratorVersion
                        + ".jar@1:true" };

        String[] output = Utils.tokenize(str, ",");
        Assert.assertEquals(output, expectedArray);
    }
}
