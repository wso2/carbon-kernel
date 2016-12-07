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
package org.wso2.carbon.kernel.internal.configprovider;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.Constants;
import org.wso2.carbon.kernel.configprovider.ConfigFileReader;
import org.wso2.carbon.kernel.configprovider.ConfigProvider;
import org.wso2.carbon.kernel.configprovider.YAMLBasedConfigFileReader;
import org.wso2.carbon.kernel.securevault.SecureVault;


/**
 * This service component is responsible for registering ConfigProvider OSGi service.
 *
 * @since 5.2.0
 */
@Component(
        name = "org.wso2.carbon.kernel.internal.configprovider.ConfigProviderComponent",
        immediate = true
)
public class ConfigProviderComponent {
    private static final Logger logger = LoggerFactory.getLogger(ConfigProviderComponent.class);

    @Activate
    protected void start(BundleContext bundleContext) {
        try {
            ConfigFileReader configFileReader = new YAMLBasedConfigFileReader(Constants.DEPLOYMENT_CONFIG_YAML);
            ConfigProvider configProvider = new ConfigProviderImpl(configFileReader);
            bundleContext.registerService(ConfigProvider.class, configProvider, null);
            logger.debug("ConfigProvider OSGi service registered");
        } catch (Throwable throwable) {
            logger.error("An error occurred while activating ConfigProviderComponent", throwable);
        }
    }

    @Deactivate
    protected void stop() {
        logger.debug("Stopping ConfigProviderComponent");
    }

    @Reference(
            name = "config.resolver.secure.vault",
            service = SecureVault.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unRegisterSecureVault"
    )
    protected void registerSecureVault(SecureVault secureVault) {
        ConfigProviderDataHolder.getInstance().setSecureVault(secureVault);
    }

    protected void unRegisterSecureVault(SecureVault secureVault) {
        ConfigProviderDataHolder.getInstance().setSecureVault(null);
    }
}
