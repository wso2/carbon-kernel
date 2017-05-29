/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.osgi.config;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.container.CarbonContainerFactory;
import org.wso2.carbon.container.options.CopyFileOption;
import org.wso2.carbon.kernel.config.model.CarbonConfiguration;

import java.nio.file.Path;
import java.nio.file.Paths;
import javax.inject.Inject;

import static org.ops4j.pax.exam.CoreOptions.systemProperty;

/**
 * This class test basic functionality of Configuration provider service with property replacing when variable used.
 * i.e. sys, env, sec.
 *
 * @since 5.2.0
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@ExamFactory(CarbonContainerFactory.class)
public class PropertyResolveConfigProviderOSGITest {

    private static final String DEPLOYMENT_FILENAME = "deployment.yaml";
    private static final String SECRET_PROPERTIES_FILENAME = "secrets.properties";

    @Inject
    private ConfigProvider configProvider;

    @Configuration
    public Option[] createConfiguration() {
        setEnvProperty("SERVER_NAME", "Test WSO2 Carbon Kernel");
        return new Option[]{ copyCarbonYAMLOption(), systemProperty("server.id").value("test-carbon-kernel"),
                copySecretPropertiesOption() };
    }

    @Test
    public void testPropertyResolve() throws ConfigurationException {
        CarbonConfiguration carbonConfiguration = configProvider.getConfigurationObject(
                CarbonConfiguration.class);
        String id = carbonConfiguration.getId();
        String name = carbonConfiguration.getName();
        String tenant = carbonConfiguration.getTenant();

        Assert.assertEquals(id, "test-carbon-kernel", "id should be test-carbon-kernel");
        Assert.assertEquals(name, "Test WSO2 Carbon Kernel",
                "name should be Test WSO2 Carbon Kernel");
        Assert.assertEquals(tenant, "default", "tenant should be default");

    }

    /**
     * Replace the existing deployment.yaml file with the file found at property-resolve directory.
     */
    private CopyFileOption copyCarbonYAMLOption() {

        Path carbonYmlFilePath;

        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }
        carbonYmlFilePath = Paths.get(basedir, "src", "test", "resources", "carbon-config",
                DEPLOYMENT_FILENAME);
        return new CopyFileOption(carbonYmlFilePath, Paths.get("conf", "default", DEPLOYMENT_FILENAME));
    }

    /**
     * Replace the existing secrets.properties file with the file found in property-resolve directory.
     *
     * @return CopyFileOption object.
     */
    private CopyFileOption copySecretPropertiesOption() {
        Path carbonYmlFilePath;

        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }
        carbonYmlFilePath = Paths.get(basedir, "src", "test", "resources", "carbon-config",
                SECRET_PROPERTIES_FILENAME);
        return new CopyFileOption(carbonYmlFilePath, Paths.get("conf", "default", SECRET_PROPERTIES_FILENAME));
    }

    /**
     *  Set environment property.
     *
     * @param key key to set in environment property.
     * @param value value to set for the above mentioned key.
     * @return empty Option object.
     */
    private void setEnvProperty(String key, String value) {
        EnvironmentUtils.setEnv(key, value);
    }

}
