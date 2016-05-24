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
package org.wso2.carbon.base.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.base.logging.LoggingConfiguration;

import java.io.FileNotFoundException;

/**
 * The base bundle which bootstrap pax-logging with the log4j2 config file. This bundle is added to the initial
 * bundle list for bootstrap logging before other bundles are provisioned.
 *
 * @since 5.1.0
 */
public class Activator implements BundleActivator, BundleListener {
    private static final Logger logger = LoggerFactory.getLogger(Activator.class);
    private static final String PAX_LOGGING_BUNDLE_SYMBOLIC_NAME = "org.ops4j.pax.logging.pax-logging-log4j2";
    private BundleContext bundleContext;

    /**
     * The activate method which called when the bundle is started. This will check for the presence of
     * ManagedService and then configure logging. Else this will throw a runtime exception.
     *
     * @param bundleContext BundleContext of this bundle
     * @throws Exception Could be thrown while bundle starting
     */
    public void start(BundleContext bundleContext) throws Exception {
        this.bundleContext = bundleContext;
        bundleContext.addBundleListener(this);
        configureLogging(bundleContext);
    }

    /**
     * This is called when the bundle is stopped.
     *
     * @param bundleContext BundleContext of this bundle
     * @throws Exception Could be thrown while bundle stopping
     */
    public void stop(BundleContext bundleContext) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Carbon base bundle activator stopped successfully");
        }
    }

    @Override
    public void bundleChanged(BundleEvent bundleEvent) {
        //TODO Properly fix this with CARBON-15908
        if (bundleEvent.getBundle().getSymbolicName().contains(PAX_LOGGING_BUNDLE_SYMBOLIC_NAME) &&
                bundleEvent.getType() == BundleEvent.STARTED) {
            configureLogging(this.bundleContext);
        }
    }


    private void configureLogging(BundleContext bundleContext) {
        ServiceReference reference = bundleContext.getServiceReference(ManagedService.class);
        if (reference != null) {
            //configuring logging using the managed config admin service
            ManagedService managedService = (ManagedService) bundleContext.getService(reference);
            try {
                LoggingConfiguration.getInstance().register(managedService);
            } catch (FileNotFoundException | ConfigurationException e) {
                logger.error("Error occurred while configuring logging framework", e);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Carbon base bundle activator is started successfully");
            }
        } else {
            //configuration managed admin service is a must to configure logging and to start carbon.
            throw new IllegalStateException("Cannot start carbon core bundle since configuration " +
                    "admin service is not available");
        }
    }
}
