/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.osgi.context;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.context.test.CarbonContextInvoker;
import org.wso2.carbon.kernel.Constants;
import org.wso2.carbon.kernel.context.CarbonContext;
import org.wso2.carbon.kernel.context.PrivilegedCarbonContext;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;
import org.wso2.carbon.osgi.test.util.OSGiTestConfigurationUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import javax.inject.Inject;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

/**
 * CarbonContextOSGiTest class is to test the functionality of the Carbon Context API.
 *
 * @since 5.1.0
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class CarbonContextOSGiTest {
    private static final Logger logger = LoggerFactory.getLogger(CarbonContextOSGiTest.class);
    private static final String TEST_TENANT_NAME = "test.tenant";

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Configuration
    public Option[] createConfiguration() {
        System.setProperty(Constants.TENANT_NAME, TEST_TENANT_NAME);
        List<Option> optionList = OSGiTestConfigurationUtils.getConfiguration();
        copyConfigFiles();
        optionList.add(mavenBundle()
                .artifactId("carbon-context-test-artifact")
                .groupId("org.wso2.carbon")
                .versionAsInProject());
        return optionList.toArray(new Option[optionList.size()]);
    }

    @Test
    public void testCarbonContext() {
        CarbonContext carbonContext = CarbonContext.getCurrentContext();
        Assert.assertEquals(carbonContext.getTenant(), TEST_TENANT_NAME);
        Assert.assertEquals(carbonContext.getUserPrincipal(), null);
        Assert.assertEquals(carbonContext.getProperty("someProperty"), null);
    }

    @Test(dependsOnMethods = "testCarbonContext")
    public void testPrivilegeCarbonContext() {
        Principal userPrincipal = () -> "test";
        String carbonContextPropertyKey = "KEY";
        Object carbonContextPropertyValue = "VALUE";

        try {
            PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getCurrentContext();
            Assert.assertEquals(CarbonContext.getCurrentContext().getTenant(), TEST_TENANT_NAME);
            privilegedCarbonContext.setUserPrincipal(userPrincipal);
            privilegedCarbonContext.setProperty(carbonContextPropertyKey, carbonContextPropertyValue);
            Assert.assertEquals(CarbonContext.getCurrentContext().getUserPrincipal(), userPrincipal);
            Assert.assertEquals(CarbonContext.getCurrentContext().getProperty(carbonContextPropertyKey),
                    carbonContextPropertyValue);
        } finally {
            PrivilegedCarbonContext.destroyCurrentContext();
        }
        Assert.assertEquals(CarbonContext.getCurrentContext().getUserPrincipal(), null);
    }

    @Test(dependsOnMethods = "testPrivilegeCarbonContext")
    public void testCarbonContextFaultyScenario() {
        PrivilegedCarbonContext.destroyCurrentContext();
        Principal userPrincipal1 = () -> "test1";
        Principal userPrincipal2 = () -> "test2";
        try {
            PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getCurrentContext();
            Assert.assertEquals(CarbonContext.getCurrentContext().getTenant(), TEST_TENANT_NAME);
            try {
                privilegedCarbonContext.setUserPrincipal(userPrincipal1);
                Assert.assertEquals(CarbonContext.getCurrentContext().getUserPrincipal(), userPrincipal1);
                privilegedCarbonContext.setUserPrincipal(userPrincipal2);
            } catch (Exception e) {
                Assert.assertTrue(e.getMessage().contains("Trying to override the already available user principal " +
                        "from " + userPrincipal1.toString() + " to " + userPrincipal2.toString()));
            }
        } finally {
            PrivilegedCarbonContext.destroyCurrentContext();
        }
    }

    @Test(dependsOnMethods = "testCarbonContextFaultyScenario")
    public void testCustomBundle() {
        String carbonContextPropertyKey = "carbonContextPropertyKey";
        Object carbonContextPropertyValue = "carbonContextPropertyValue";
        String userPrincipalName = "userPrincipalName";
        CarbonContextInvoker carbonContextInvoker = new CarbonContextInvoker(carbonContextPropertyKey,
                carbonContextPropertyValue, userPrincipalName);
        carbonContextInvoker.invoke();
        CarbonContext carbonContext = CarbonContext.getCurrentContext();
        Assert.assertEquals(carbonContext.getProperty(carbonContextPropertyKey), carbonContextPropertyValue);
        Assert.assertEquals(carbonContext.getUserPrincipal().getName(), userPrincipalName);
    }

    /**
     * Replace the existing carbon.yaml file with the file found at runtime resources directory.
     */
    private void copyConfigFiles() {
        Path carbonYmlFilePath;

        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }
        try {
            carbonYmlFilePath = Paths.get(basedir, "src", "test", "resources", "carbon-context", "carbon.yml");
            Files.copy(carbonYmlFilePath, Paths.get(System.getProperty("carbon.home"), "conf",
                    "carbon.yml"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.error("Unable to copy the tenant.xml file", e);
        }
    }
}
