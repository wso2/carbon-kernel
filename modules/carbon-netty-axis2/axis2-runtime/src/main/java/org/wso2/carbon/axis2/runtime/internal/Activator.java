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
package org.wso2.carbon.axis2.runtime.internal;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.axis2.runtime.Axis2ServiceManager;
import org.wso2.carbon.axis2.runtime.Axis2ServiceManagerImpl;
import org.wso2.carbon.axis2.runtime.dummy.MyOtherService;
import org.wso2.carbon.axis2.runtime.dummy.MyService;
import org.wso2.carbon.axis2.runtime.dummy2.other.MySomeOtherService;

import java.io.File;
import java.nio.file.Paths;

/**
 * This is a sample bundle activator class.
 *
 * @since 1.0.0
 */
public class Activator implements BundleActivator {
    private static final Logger logger = LoggerFactory.getLogger(Activator.class);

    /**
     * This is called when the bundle is started.
     *
     * @param bundleContext BundleContext of this bundle
     */
    public void start(BundleContext bundleContext) {
        String axis2FilePath = Paths.get(System.getProperty("carbon.home"), "conf", "axis2", "axis2.xml").toString();
        File file = new File(axis2FilePath);
        if (!file.exists()) {
            logger.info("Axis2.xml file should be available in [PRODUCT_HOME]/conf/axis2/axis2.xml");
        }

        try {
            ConfigurationContext configurationContext = ConfigurationContextFactory.
                    createConfigurationContextFromFileSystem(null, axis2FilePath);
            DataHolder.getInstance().setConfigurationContext(configurationContext);
        } catch (AxisFault axisFault) {
            logger.error("Failed to initialize Axis2 engine with the given axis2.xml", axisFault);
        }

        bundleContext.registerService(Axis2ServiceManager.class, new Axis2ServiceManagerImpl(), null);
        DataHolder.getInstance().setBundleContext(bundleContext);
        logger.info("Axis2 engine is initialized successfully.");

        //TODO: Remove following dummy calls :)
        registerDummyService();
    }

    /**
     * This is called when the bundle is stopped.
     *
     * @param bundleContext BundleContext of this bundle
     * @throws Exception Could be thrown while bundle stopping
     */

    public void stop(BundleContext bundleContext) throws Exception {
        if (DataHolder.getInstance().getConfigurationContext() != null) {
            //TODO : unregister all the service groups + services
            logger.info("Stopping activator");
        }
    }

    //TODO: Remove this registerDummyService method :)
    private void registerDummyService() {
        BundleContext bundleContext = DataHolder.getInstance().getBundleContext();
        if (bundleContext != null) {
            ServiceReference serviceReference = bundleContext.getServiceReference(Axis2ServiceManager.class);
            if (serviceReference != null) {
                Axis2ServiceManager axis2ServiceManager =
                        (Axis2ServiceManager) bundleContext.getService(serviceReference);
                AxisService axisService = axis2ServiceManager.registerService(MyService.class);
                if (axisService != null) {
                    logger.info("Successfully added MyService into Axis2 engine.");
                }
                axis2ServiceManager.registerService(MyOtherService.class);
                axis2ServiceManager.registerService(MySomeOtherService.class);
            }
        }
    }
}
