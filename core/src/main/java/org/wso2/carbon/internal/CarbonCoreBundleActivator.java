/*
 *  Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.wso2.carbon.base.LoggingConfiguration;
import org.wso2.carbon.internal.base.ConfigAdminServiceTracker;
import org.wso2.carbon.internal.kernel.config.XMLBasedConfigProvider;
import org.wso2.carbon.internal.kernel.context.CarbonRuntimeFactory;
import org.wso2.carbon.kernel.CarbonRuntime;
import org.wso2.carbon.kernel.config.CarbonConfigProvider;

/**
 * Activator class for carbon core
 */
public class CarbonCoreBundleActivator implements BundleActivator {

    private static final Logger logger = LoggerFactory.getLogger(CarbonCoreBundleActivator.class);
    private static final Marker marker = MarkerFactory.getMarker("FATAL");
    private ServiceRegistration registration;
    private ServiceTracker configAdminServiceTracker;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        logger.debug("Activating carbon core bundle");

        //register pax logging on carbon server
        LoggingConfiguration loggingConfiguration = LoggingConfiguration.getInstance();
        configAdminServiceTracker = new ConfigAdminServiceTracker(bundleContext, loggingConfiguration);
        configAdminServiceTracker.open();

        DataHolder.getInstance().setBundleContext(bundleContext);

        // 1) Find to initialize the Carbon configuration provider
        CarbonConfigProvider configProvider = new XMLBasedConfigProvider();

        // 2) Creates the CarbonRuntime instance using the Carbon configuration provider.
        CarbonRuntime carbonRuntime = CarbonRuntimeFactory.createCarbonRuntime(configProvider);

        // 3) Register CarbonRuntime instance as an OSGi bundle.
        bundleContext.registerService(CarbonRuntime.class.getName(), carbonRuntime, null);

    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        logger.debug("Stopping carbon core bundle");

        //unregister pax logging on carbon server
        //registration.unregister();
        configAdminServiceTracker.close();
        configAdminServiceTracker = null;

        DataHolder.getInstance().setBundleContext(null);
    }
}
