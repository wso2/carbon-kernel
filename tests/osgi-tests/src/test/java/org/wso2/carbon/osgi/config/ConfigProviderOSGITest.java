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

import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.container.CarbonContainerFactory;
import org.wso2.carbon.kernel.config.model.CarbonConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.inject.Inject;

/**
 * This class test basic functionality of Configuration provider service.
 *
 * @since 5.2.0
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@ExamFactory(CarbonContainerFactory.class)
public class ConfigProviderOSGITest {

    @Inject
    private ConfigProvider configProvider;

    @Test
    public void testConfigurationProvider() throws ConfigurationException {
        CarbonConfiguration carbonConfiguration = configProvider.getConfigurationObject(
                CarbonConfiguration.class);
        String id = carbonConfiguration.getId();
        String name = carbonConfiguration.getName();
        String tenant = carbonConfiguration.getTenant();

        Assert.assertEquals(id, "carbon-kernel", "id should be carbon-kernel");
        Assert.assertEquals(name, "WSO2 Carbon Kernel", "name should be WSO2 Carbon Kernel");
        Assert.assertEquals(tenant, "default", "tenant should be default");

        Map secureVaultConfiguration =
                (LinkedHashMap) configProvider.getConfigurationObject("wso2.securevault");

        Assert.assertEquals(((LinkedHashMap) (secureVaultConfiguration.get("secretRepository"))).get("type"),
                "org.wso2.carbon.secvault.repository.DefaultSecretRepository",
                "Default secret repository would be " +
                        "org.wso2.carbon.secvault.repository.DefaultSecretRepository");

        Assert.assertEquals(((LinkedHashMap) (secureVaultConfiguration.get("masterKeyReader"))).get("type"),
                "org.wso2.carbon.secvault.reader.DefaultMasterKeyReader",
                "Default master key reader would be " +
                        "org.wso2.carbon.secvault.reader.DefaultMasterKeyReader");
    }
}
