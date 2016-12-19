/*
 * Copyright 2015 WSO2, Inc. http://www.wso2.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.kernel.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.Constants;
import org.wso2.carbon.kernel.config.model.CarbonConfiguration;
import org.wso2.carbon.kernel.configprovider.CarbonConfigurationException;
import org.wso2.carbon.kernel.configprovider.ConfigFileReader;
import org.wso2.carbon.kernel.configprovider.ConfigProvider;
import org.wso2.carbon.kernel.configprovider.YAMLBasedConfigFileReader;
import org.wso2.carbon.kernel.internal.configprovider.ConfigProviderImpl;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;

import java.text.DecimalFormat;

/**
 * CarbonStartupHandler class handle the startup finalization utilities.
 *
 * @since 5.0.0
 */

public class CarbonStartupHandler {
    private static final Logger logger = LoggerFactory.getLogger(CarbonStartupHandler.class);

    private CarbonStartupHandler() {
    }

    /**
     * Log the server start up time.
     */
    public static void logServerStartupTime() {
        double startTime = Long.parseLong(System.getProperty(org.wso2.carbon.kernel.Constants.START_TIME));
        double startupTime = (System.currentTimeMillis() - startTime) / 1000;

        DecimalFormat decimalFormatter = new DecimalFormat("#,##0.000");
        logger.info(getCarbonConfigs().getName() + " started in " + decimalFormatter.format(startupTime) + " sec");
    }

    /**
     * Register the the CarbonServerInfo as an OSGi service. Other components can identify the server startup completion
     * by listening to the CarbonServerInfo Service registration.
     */
    public static void registerCarbonServerInfoService() {
        DataHolder.getInstance().getBundleContext().registerService(CarbonServerInfo.class,
                new CarbonServerInfo(), null);
    }

    private static CarbonConfiguration getCarbonConfigs() {
        ConfigFileReader fileReader = new YAMLBasedConfigFileReader(Constants.DEPLOYMENT_CONFIG_YAML);
        ConfigProvider configProvider = new ConfigProviderImpl(fileReader);
        try {
            return configProvider.getConfigurationObject(CarbonConfiguration.class);
        } catch (CarbonConfigurationException e) {
            logger.error("Error while getting carbon configuration object.", e);
        }
        return null;
    }
}
