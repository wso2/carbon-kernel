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
package org.wso2.carbon.transport.internal;

import org.eclipse.equinox.http.servlet.HttpServiceServlet;
import org.eclipse.jetty.osgi.boot.OSGiServerConstants;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transports.CarbonTransport;

import java.nio.file.Paths;
import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Represents a Jetty transport in Carbon
 */
public class JettyCarbonTransport extends CarbonTransport {

    private static final Logger logger = LoggerFactory.getLogger(JettyCarbonTransport.class);

    private ServiceRegistration jettyServiceRegistration;
    private ServiceRegistration contextHandlerRegistration;

    public JettyCarbonTransport(String id) {
        super(id);
    }

    public void start() {
        logger.info("Starting Jetty Server..........");
        String jettyHome = Paths.get(System.getProperty("carbon.repository"), "conf", "jetty").toString();

        Server server = new Server();

        String serverName = "carbon-server";
        Dictionary<String, String> serverProps = new Hashtable<>();
        serverProps.put(OSGiServerConstants.MANAGED_JETTY_SERVER_NAME, serverName);
        serverProps.put(OSGiServerConstants.MANAGED_JETTY_XML_CONFIG_URLS,
                "file:" + Paths.get(jettyHome, "jetty.xml"));

        //register as an OSGi Service for Jetty to find
        jettyServiceRegistration = DataHolder.getInstance().getBundleContext().
                registerService(Server.class.getName(), server, serverProps);
        logger.info("Jetty server instance is registered as service : {}", server);

        //exposing the OSGi HttpService by registering the HttpServiceServlet with Jetty.
        ServletHolder holder = new ServletHolder(new HttpServiceServlet());
        ServletContextHandler httpContext = new ServletContextHandler();

        httpContext.addServlet(holder, "/*");
        Dictionary<String, String> servletProps = new Hashtable<>();
        servletProps.put(OSGiServerConstants.MANAGED_JETTY_SERVER_NAME, serverName);

        contextHandlerRegistration = DataHolder.getInstance().getBundleContext().
                registerService(ContextHandler.class.getName(), httpContext, servletProps);
    }


    public void stop() {
        logger.info("Unregistering jetty server instance");
        jettyServiceRegistration.unregister();
        contextHandlerRegistration.unregister();
    }

    @Override
    protected void beginMaintenance() {

    }

    @Override
    protected void endMaintenance() {

    }

}
