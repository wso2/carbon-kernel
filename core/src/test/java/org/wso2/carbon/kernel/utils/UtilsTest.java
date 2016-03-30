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
package org.wso2.carbon.kernel.utils;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.Constants;

/**
 * This class tests the functionality of org.wso2.carbon.kernel.utils.Utils class.
 *
 * @since 5.0.0
 */
public class UtilsTest {

    @Test
    public void testSubstituteVarsSystemPropertyNotNull() {
        String carbonHome = System.getProperty(Constants.CARBON_HOME);
        boolean isCarbonHomeChanged = false;

        if (carbonHome == null) {
            carbonHome = "test-carbon-home";
            System.setProperty(Constants.CARBON_HOME, carbonHome);
            isCarbonHomeChanged = true;
        }

        Assert.assertEquals(Utils.substituteVariables("${carbon.home}"), carbonHome);

        if (isCarbonHomeChanged) {
            System.clearProperty(Constants.CARBON_HOME);
        }
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testSubstituteVarsSystemPropertyIsNull() {
        String carbonHome = System.getProperty(Constants.CARBON_HOME);
        boolean isCarbonHomeChanged = false;

        if (carbonHome != null) {
            System.clearProperty(Constants.CARBON_HOME);
            isCarbonHomeChanged = true;
        }

        try {
            Utils.substituteVariables("${carbon.home}");
        } finally {
            if (isCarbonHomeChanged) {
                System.setProperty(Constants.CARBON_HOME, carbonHome);
            }
        }
    }

    @DataProvider(name = "paths")
    public Object[][] createPaths() {
        return new Object[][]{{"/home/wso2/wso2carbon", "/"},
                {"C:\\Users\\WSO2\\Desktop\\CARBON~1\\WSO2CA~1.0-S", "\\"}};
    }

    @Test(dataProvider = "paths")
    public void testPathSubstitution(String carbonHome, String pathSeparator) {
        System.setProperty(Constants.CARBON_HOME, carbonHome);
        String config = "${" + Constants.CARBON_HOME + "}" + pathSeparator + "deployment" + pathSeparator;
        Assert.assertEquals(Utils.substituteVariables(config),
                carbonHome + pathSeparator + "deployment" + pathSeparator);
    }
}
