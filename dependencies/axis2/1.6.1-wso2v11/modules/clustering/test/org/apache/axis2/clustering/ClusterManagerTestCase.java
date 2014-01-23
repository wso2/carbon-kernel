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

package org.apache.axis2.clustering;

import junit.framework.TestCase;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.util.Utils;

public abstract class ClusterManagerTestCase extends TestCase {

    protected ClusteringAgent clusterManager1 = null;
    protected ClusteringAgent clusterManager2 = null;
    protected AxisConfiguration axisConfiguration1 = null;
    protected AxisConfiguration axisConfiguration2 = null;
    protected ConfigurationContext configurationContext1 = null;
    protected ConfigurationContext configurationContext2 = null;
    protected AxisServiceGroup serviceGroup1 = null;
    protected AxisServiceGroup serviceGroup2 = null;
    protected AxisService service1 = null;
    protected AxisService service2 = null;
    protected String serviceName = "testService";

    protected abstract ClusteringAgent getClusterManager(ConfigurationContext configCtx);

    protected boolean skipChannelTests = false;

    protected void setUp() throws Exception {

        Thread.sleep(3000);

        configurationContext1 = ConfigurationContextFactory.createDefaultConfigurationContext();
        configurationContext2 = ConfigurationContextFactory.createDefaultConfigurationContext();

        clusterManager1 = getClusterManager(configurationContext1);
        clusterManager2 = getClusterManager(configurationContext2);

        clusterManager1.getStateManager().setConfigurationContext(configurationContext1);
        clusterManager2.getStateManager().setConfigurationContext(configurationContext2);

        clusterManager1.getNodeManager().setConfigurationContext(configurationContext1);
        clusterManager2.getNodeManager().setConfigurationContext(configurationContext2);

        //giving both Nodes the same deployment configuration
        axisConfiguration1 = configurationContext1.getAxisConfiguration();
        serviceGroup1 = new AxisServiceGroup(axisConfiguration1);
        service1 = new AxisService(serviceName);
        serviceGroup1.addService(service1);
        axisConfiguration1.addServiceGroup(serviceGroup1);

        axisConfiguration2 = configurationContext2.getAxisConfiguration();
        serviceGroup2 = new AxisServiceGroup(axisConfiguration2);
        service2 = new AxisService(serviceName);
        serviceGroup2.addService(service2);
        axisConfiguration2.addServiceGroup(serviceGroup2);

        //Initiating ClusterManagers
        System.setProperty(ClusteringConstants.LOCAL_IP_ADDRESS, Utils.getIpAddress());
        try {
            clusterManager1.init();
            System.out.println("ClusteringAgent-1 successfully initialized");
            System.out.println("*** PLEASE IGNORE THE java.net.ConnectException STACKTRACES. THIS IS EXPECTED ***");
            clusterManager2.init();
            System.out.println("ClusteringAgent-2 successfully initialized");
        } catch (ClusteringFault e) {
            String message =
                    "Could not initialize ClusterManagers. Please check the network connection";
            System.out.println(message + ": " + e);
            e.printStackTrace();
            skipChannelTests = true;
        }
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        clusterManager1.shutdown();
        clusterManager2.shutdown();
    }

}
