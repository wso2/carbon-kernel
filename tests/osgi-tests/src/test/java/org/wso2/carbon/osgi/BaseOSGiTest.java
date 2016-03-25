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
package org.wso2.carbon.osgi;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;
import org.wso2.carbon.osgi.test.util.OSGiTestConfigurationUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import javax.inject.Inject;

/**
 * Base OSGi class to test the OSGi status of the org.wso2.carbon.core bundle.
 *
 * @since 5.0.0
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class BaseOSGiTest {

    private static final Logger logger = LoggerFactory.getLogger(BaseOSGiTest.class);

    @Configuration
    public Option[] createConfiguration() {
        List<Option> optionList = OSGiTestConfigurationUtils.getConfiguration();
        copyCarbonYAML();
        return optionList.toArray(new Option[optionList.size()]);
    }

    @Inject
    private BundleContext bundleContext;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Test
    public void testBundleContextStatus() {
        Assert.assertNotNull(bundleContext, "Bundle Context is null");
    }

    @Test
    public void testCarbonCoreBundleStatus() {

        Bundle coreBundle = null;
        for (Bundle bundle : bundleContext.getBundles()) {
            if (bundle.getSymbolicName().equals("org.wso2.carbon.core")) {
                coreBundle = bundle;
                break;
            }
        }
        Assert.assertNotNull(coreBundle, "Carbon Core bundle not found");
        Assert.assertEquals(coreBundle.getState(), Bundle.ACTIVE, "Carbon Core Bundle is not activated");
    }

    /**
     * Replace the existing carbon.yml file with populated carbon.yml file.
     */
    private static void copyCarbonYAML() {
        Path carbonYmlFilePath;

        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }
        try {
            carbonYmlFilePath = Paths.get(basedir, "src", "test", "resources", "runtime", "carbon.yml");
            Files.copy(carbonYmlFilePath, Paths.get(System.getProperty("carbon.home"), "conf", "carbon.yml"),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.error("Unable to copy the carbon.yml file", e);
        }
    }
}
