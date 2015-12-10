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
import org.wso2.carbon.kernel.utils.CarbonServerInfo;

import java.text.DecimalFormat;

/**
 * CarbonStartupHandler class handle the startup finalization utilities.
 *
 * @since 5.0.0
 */

public class CarbonStartupHandler {
    private static final Logger logger = LoggerFactory.getLogger(CarbonStartupHandler.class);

    /**
     * Log the server start up time.
     */
    public static void logServerStartupTime() {
        double startTime = Long.parseLong(System.getProperty(org.wso2.carbon.kernel.Constants.START_TIME));
        double startupTime = (System.currentTimeMillis() - startTime) / 1000;

        DecimalFormat decimalFormatter = new DecimalFormat("#,##0.000");
        logger.info("WSO2 Carbon started in " + Double.valueOf(decimalFormatter.format(startupTime)) + " sec");
    }

    /**
     * Register the the CarbonServerInfo as an OSGi service. Other components can identify the server startup completion
     * by listening to the CarbonServerInfo Service registration.
     */
    public static void registerCarbonServerInfoService() {
        DataHolder.getInstance().getBundleContext().registerService(CarbonServerInfo.class,
                new CarbonServerInfo(), null);
    }

}
