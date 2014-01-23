/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.engine;

import org.apache.axis2.AxisFault;
import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.clustering.ClusteringConstants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisService;

/**
 * This class provide a very convenient way of creating server and deploying services.
 * Once someone call start method it will fire up configuration context and start up the listeners.
 * One can provide repository location and axis.xml as system properties.
 */

public class AxisServer {

    protected ConfigurationContext configContext;
    protected ListenerManager listenerManager;
    private boolean startOnDeploy;
    private boolean started = false;


    /**
     * If you do not want Axis2 to start the server automatically then pass the "false" else "true"
     *
     * @param startOnDeploy : boolean
     */
    public AxisServer(boolean startOnDeploy) {
        this.startOnDeploy = startOnDeploy;
        listenerManager = new ListenerManager();
    }


    /**
     * Server will start automatically if you call deployService
     */
    public AxisServer() {
        this(true);
    }

    /**
     * Will make Java class into a web service
     *
     * @param serviceClassName : Actual class you want to make as a web service
     * @throws AxisFault : If something went wrong
     */
    public void deployService(String serviceClassName) throws AxisFault {
        if (configContext == null) {
            configContext = getConfigurationContext();
        }
        AxisConfiguration axisConfig = configContext.getAxisConfiguration();
        AxisService service = AxisService.createService(serviceClassName, axisConfig);
        axisConfig.addService(service);
        if (startOnDeploy) {
            start();
        }
    }

    /**
     * Will create a configuration context from the avialable data and then it
     * will start the listener manager
     *
     * @throws AxisFault if something went wrong
     */
    protected void start() throws AxisFault {
        if (configContext == null) {
            configContext = getConfigurationContext();
        }
        if (!started) {

            ClusteringAgent clusteringAgent =
                    configContext.getAxisConfiguration().getClusteringAgent();
            String avoidInit = ClusteringConstants.Parameters.AVOID_INITIATION;
            if (clusteringAgent != null &&
                clusteringAgent.getParameter(avoidInit) != null &&
                ((String) clusteringAgent.getParameter(avoidInit).getValue()).equalsIgnoreCase("true")) {
                clusteringAgent.setConfigurationContext(configContext);
                clusteringAgent.init();
            }

            listenerManager.startSystem(configContext);
            started = true;
        }
    }

    /**
     * Stop the server, automatically terminates the listener manager as well.
     *
     * @throws AxisFault
     */
    public void stop() throws AxisFault {
        if (configContext != null) {
            configContext.terminate();
        }
    }

    /**
     * Set the configuration context. Please call this before you call deployService or start method
     *
     * @param configContext ConfigurationContext
     */
    public void setConfigurationContext(ConfigurationContext configContext) {
        this.configContext = configContext;
    }

    /**
     * Creates a default configuration context if one is not set already via setConfigurationContext
     *
     * @return ConfigurationContext
     * @throws AxisFault
     */
    public ConfigurationContext getConfigurationContext() throws AxisFault {
        if (configContext == null) {
            configContext = createDefaultConfigurationContext();
        }
        return configContext;
    }

    /**
     * Users extending this class can override this method to supply a custom ConfigurationContext
     *
     * @return ConfigurationContext
     * @throws AxisFault
     */
    protected ConfigurationContext createDefaultConfigurationContext() throws AxisFault {
        return ConfigurationContextFactory.createConfigurationContextFromFileSystem(null);
    }
}
