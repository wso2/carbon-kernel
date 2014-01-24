/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.integration.core;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.http.SimpleHTTPServer;
import org.wso2.carbon.utils.ServerConstants;

import java.io.File;

/**
 *  This class will start simple HTTP server programatically with SimpleStockQuoteService.call happen
 *  with separate thread with the server start main thread.
 */

/**
 * deploy SimpleStockQuoteService and start SimpleHTTPServer
 */
public class HTTPServerManager implements Runnable {
    private static SimpleHTTPServer server;
    protected int serverPort = 9000;

    public synchronized void startHTTPServer() {
        ConfigurationContext context;
        try {
            String carbonHome = System.getProperty(ServerConstants.CARBON_HOME);
            if (carbonHome.contains(File.separator + "." + File.separator)) {
                carbonHome = carbonHome.replace(File.separator + "." + File.separator, File.separator);

            }
            context = ConfigurationContextFactory.
                    createConfigurationContextFromFileSystem(carbonHome + File.separator + "samples" +
                                                             File.separator + "axis2Server" + File.separator +
                                                             "repository", null);
            server = new SimpleHTTPServer(context, serverPort);
            String resourcePath = HTTPServerManager.class.getResource("/SimpleStockQuoteService.aar").getPath();
            AxisServiceGroup serviceGroup = DeploymentEngine.loadServiceGroup(new File(resourcePath), context);
            AxisConfiguration xConfig = context.getAxisConfiguration();
            xConfig.addServiceGroup(serviceGroup);
            server.start();

        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }
    }

    /**
     * Method used to stop SimpleHTTPServer
     */
    public synchronized void stopHTTPServer() {
        server.stop();
    }

    public void run() {
        startHTTPServer();
    }
}
