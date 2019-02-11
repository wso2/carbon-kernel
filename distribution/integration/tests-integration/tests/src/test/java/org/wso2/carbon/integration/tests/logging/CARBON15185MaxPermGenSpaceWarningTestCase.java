/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.integration.tests.logging;


import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.tests.common.utils.CarbonIntegrationBaseTest;

import javax.xml.xpath.XPathExpressionException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;

/**
 * This test case checks whether Maximum PermGen space warning is printed at server startup.
 * <p/>
 * max perm gen is no longer there from jdk1.8 onwards. But we are checking for max perm gen space value and warn that
 * it is 0 in jadk1.8.
 * <p/>
 * https://wso2.org/jira/browse/CARBON-15185
 */
public class CARBON15185MaxPermGenSpaceWarningTestCase extends CarbonIntegrationBaseTest {

    private static final String CARBON_HOME = "carbon.home";

    @BeforeTest
    public void init() throws XPathExpressionException {
        super.init();
    }

    @Test(groups = "org.wso2.carbon.logging", description = "reads the wso2carbon.log file and check whether " +
            "Max perm gen space warning is there.")
    public void testForMaxPermGenSpaceWarning() {
        String carbonHome = System.getProperty(CARBON_HOME);
        File wso2carbonLog = Paths.get(carbonHome, "repository", "logs", "wso2carbon.log").toFile();

        try (FileInputStream fis = new FileInputStream(wso2carbonLog)) {
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));

            String log;
            while ((log = br.readLine()) != null) {
                if (log.contains("Maximum PermGen space (MB) :0 of the running JVM is set below the recommended minimum size")) {
                    Assert.fail("Maximum PermGen space WARNING is there in the carbon log.");
                }
            }
        } catch (FileNotFoundException e) {
            Assert.fail("wso2carbon.log file not found.");
        } catch (IOException e) {
            Assert.fail("Error while reading wso2carbon.log");
        }
    }

}
