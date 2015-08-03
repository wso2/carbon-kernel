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
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transports.CarbonTransport;
import org.wso2.carbon.transports.TransportManager;

import java.util.Timer;
import java.util.TimerTask;

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

        JettyCarbonTransport jettyCarbonTransport = new JettyCarbonTransport("jetty-carbon-server");

        jettyServerRegistration = bundleContext.registerService(CarbonTransport.class.getName(), jettyCarbonTransport, null);

        TimerTaskTransport timerTaskTransport = new TimerTaskTransport();
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(timerTaskTransport, 0, 10 * 1000);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        logger.debug("Unregistering jetty server instance");
        jettyServerRegistration.unregister();
    }
}

class TimerTaskTransport extends TimerTask {

    boolean started = false;
    private static final Logger logger = LoggerFactory.getLogger(TimerTaskTransport.class);

    @Override
    public void run() {
        logger.debug("Inside timer task..............");
        ServiceReference serviceReference = DataHolder.getInstance().getBundleContext().getServiceReference(TransportManager.class.getName());
        TransportManager transportManager = null;

        if (serviceReference != null) {
            transportManager = (TransportManager) DataHolder.getInstance().getBundleContext().getService(serviceReference);
        } else {
            logger.debug("Service reference is null...");
        }

        if (transportManager != null) {
            try {
                if (!started) {
                    transportManager.startAllTransports();
                    started = true;
                } else {
                    transportManager.stopAllTransports();
                    started = false;
                }
            } catch (IllegalStateException e) {
                logger.error("Error", e);
            }
        } else {
            logger.debug("TransportManager is null...");
        }
    }

}