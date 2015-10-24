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
import org.wso2.carbon.launcher.utils.Utils;

/**
 * Launcher Utils test class.
 */

@Test(groups = "utils")
public class UtilsTest {

    public void substituteVarsTest() {
        System.setProperty("profile", "default");
        String inputStr = "file:${profile}";
        String expectedOutputStr = "file:default";

        String outputStr = Utils.initializeSystemProperties(inputStr);
        Assert.assertEquals(outputStr, expectedOutputStr);
    }

    public void substituteVarsTest2() {
        System.setProperty("profile", "default");
        System.setProperty("carbon.home", "/home/user/wso2carbon-kernel-5.0.0");
        String inputStr = "file:${carbon.home}/repository/components/${profile}";
        String expectedOutputStr = "file:/home/user/wso2carbon-kernel-5.0.0/repository/components/default";

        String outputStr = Utils.initializeSystemProperties(inputStr);
        Assert.assertEquals(outputStr, expectedOutputStr);
    }

    public void substituteVarsTest3() {
        System.setProperty("profile", "default");
        String inputStr = "${profile}";
        String expectedOutputStr = "default";

        String outputStr = Utils.initializeSystemProperties(inputStr);
        Assert.assertEquals(outputStr, expectedOutputStr);
    }

    public void stringTokenizeTest() {
        String str = "file:plugins/org.eclipse.equinox.simpleconfigurator_1.0.400.v20130327-2119.jar@1:true," +
                "file:plugins/org.apache.felix.gogo.runtime_0.10.0.v201209301036.jar@2:true," +
                "file:plugins/org.apache.felix.gogo.command_0.10.0.v201209301215.jar@2:true," +
                "file:plugins/org.apache.felix.gogo.shell_0.10.0.v201212101605.jar@2:true," +
                "file:plugins/org.eclipse.equinox.console_1.0.100.v20130429-0953.jar@2:true";

        String[] expectedArray = new String[]{
                "file:plugins/org.eclipse.equinox.simpleconfigurator_1.0.400.v20130327-2119.jar@1:true",
                "file:plugins/org.apache.felix.gogo.runtime_0.10.0.v201209301036.jar@2:true",
                "file:plugins/org.apache.felix.gogo.command_0.10.0.v201209301215.jar@2:true",
                "file:plugins/org.apache.felix.gogo.shell_0.10.0.v201212101605.jar@2:true",
                "file:plugins/org.eclipse.equinox.console_1.0.100.v20130429-0953.jar@2:true"
        };

        String[] output = Utils.tokenize(str, ",");
        Assert.assertEquals(output, expectedArray);
    }

    public void stringTokenizeTest2() {
        String str = "file:plugins/org.eclipse.equinox.simpleconfigurator_1.0.400.v20130327-2119.jar@1:true";

        String[] expectedArray =
                new String[]{"file:plugins/org.eclipse.equinox.simpleconfigurator_1.0.400.v20130327-2119.jar@1:true"};

        String[] output = Utils.tokenize(str, ",");
        Assert.assertEquals(output, expectedArray);
    }
}
