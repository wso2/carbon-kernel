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

import org.eclipse.equinox.http.servlet.HttpServiceServlet;
import org.eclipse.jetty.osgi.boot.OSGiServerConstants;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Dictionary;
import java.util.Hashtable;

/**
 * This is the base activator class which will create a Jetty server instance and register it as an
 * OSGi service. This will also expose the OSGi HttpService by registering the HttpServiceServlet
 * with Jetty
 */

public class CarbonTransportBundleActivator implements BundleActivator {
    private static final Logger logger = LoggerFactory.
            getLogger(CarbonTransportBundleActivator.class);

    private ServiceRegistration jettyServiceRegistration;
    private ServiceRegistration contextHandlerRegistration;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        String jettyHome = System.getProperty("carbon.repository") + File.separator + "conf" +
                           File.separator + "jetty";

        Server server = new Server();

        String serverName = "carbon-server";
        Dictionary serverProps = new Hashtable();
        serverProps.put(OSGiServerConstants.MANAGED_JETTY_SERVER_NAME, serverName);
        serverProps.put(OSGiServerConstants.MANAGED_JETTY_XML_CONFIG_URLS,
                        "file:" + jettyHome + File.separator + "jetty.xml");

        //register as an OSGi Service for Jetty to find
        jettyServiceRegistration = bundleContext.
                registerService(Server.class.getName(), server, serverProps);
        logger.info("Jetty server instance is registered as service : {}", server);

        //exposing the OSGi HttpService by registering the HttpServiceServlet with Jetty.
        ServletHolder holder = new ServletHolder(new HttpServiceServlet());
        ServletContextHandler httpContext = new ServletContextHandler();

        httpContext.addServlet(holder, "/*");
        Dictionary servletProps = new Hashtable();
        servletProps.put(OSGiServerConstants.MANAGED_JETTY_SERVER_NAME, serverName);

        contextHandlerRegistration = bundleContext.
                registerService(ContextHandler.class.getName(), httpContext, servletProps);

    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        logger.info("Unregistering jetty server instance");
        jettyServiceRegistration.unregister();
        contextHandlerRegistration.unregister();
    }
}
