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
package org.wso2.carbon.kernel.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.CarbonRuntime;
import org.wso2.carbon.kernel.config.CarbonConfigProvider;
import org.wso2.carbon.kernel.internal.config.YAMLBasedConfigProvider;
import org.wso2.carbon.kernel.internal.context.CarbonRuntimeFactory;
import org.wso2.carbon.kernel.utils.MBeanRegistrator;

/**
 * Activator class for carbon core.
 *
 * @since 5.0.0
 */
public class CarbonCoreBundleActivator implements BundleActivator {
    private static final Logger logger = LoggerFactory.getLogger(CarbonCoreBundleActivator.class);

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        DataHolder.getInstance().setBundleContext(bundleContext);

        // 1) Find to initialize the Carbon configuration provider
        CarbonConfigProvider configProvider = new YAMLBasedConfigProvider();

        // 2) Creates the CarbonRuntime instance using the Carbon configuration provider.
        CarbonRuntime carbonRuntime = CarbonRuntimeFactory.createCarbonRuntime(configProvider);

        // 3) Register CarbonRuntime instance as an OSGi bundle.
        bundleContext.registerService(CarbonRuntime.class.getName(), carbonRuntime, null);

        DataHolder.getInstance().setCarbonRuntime(carbonRuntime);
        logger.debug("Carbon core bundle is started successfully");
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        MBeanRegistrator.unregisterAllMBeans();
        logger.debug("Carbon core bundle is stopped successfully");
    }
}
