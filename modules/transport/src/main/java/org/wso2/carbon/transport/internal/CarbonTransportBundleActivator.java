/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.transport.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transports.CarbonTransport;

/**
 * This is the base activator class which will create a Jetty server instance and register it as an
 * OSGi service. This will also expose the OSGi HttpService by registering the HttpServiceServlet
 * with Jetty
 */

public class CarbonTransportBundleActivator implements BundleActivator {
    private static final Logger logger = LoggerFactory.
            getLogger(CarbonTransportBundleActivator.class);

    private ServiceRegistration jettyServerRegistration;

    @Override
    public void start(BundleContext bundleContext) throws Exception {

        logger.debug("Activating jetty transport bundle..........................................");
        DataHolder.getInstance().setBundleContext(bundleContext);

        JettyCarbonTransport jettyCarbonTransport = new JettyCarbonTransport("jetty");

        jettyServerRegistration =
                bundleContext.registerService(CarbonTransport.class.getName(), jettyCarbonTransport, null);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        logger.info("Unregistering jetty server instance");
        jettyServerRegistration.unregister();
    }
}
