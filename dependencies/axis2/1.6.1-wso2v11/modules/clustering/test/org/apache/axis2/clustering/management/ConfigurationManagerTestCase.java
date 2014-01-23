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

package org.apache.axis2.clustering.management;

import org.apache.axis2.clustering.ClusterManagerTestCase;
import org.apache.axis2.clustering.ClusteringFault;

import javax.xml.stream.XMLStreamException;


public abstract class ConfigurationManagerTestCase extends ClusterManagerTestCase {

    public void testLoadServiceGroup() throws ClusteringFault {

        String serviceGroupName = "testService";
//        clusterManager1.getNodeManager().loadServiceGroups(new String[]{serviceGroupName});

        /*try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List eventList = configurationManagerListener2.getEventList();
        assertNotNull(eventList);
        assertEquals(eventList.size(), 1);
        ConfigurationEvent event = (ConfigurationEvent) eventList.get(0);

        assertNotNull(event);

        String[] serviceGroupNames = event.getServiceGroupNames();
        assertNotNull(serviceGroupNames);
        assertEquals(serviceGroupNames[0], serviceGroupName);*/
    }

    public void testUnloadServiceGroup() throws ClusteringFault {

        String serviceGroupName = "testService";
//        clusterManager1.getNodeManager().unloadServiceGroups(new String[]{serviceGroupName});

        /*try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List eventList = configurationManagerListener2.getEventList();
        assertNotNull(eventList);
        assertEquals(eventList.size(), 1);
        ConfigurationEvent event = (ConfigurationEvent) eventList.get(0);

        assertNotNull(event);

        String[] serviceGroupNames = event.getServiceGroupNames();
        assertNotNull(serviceGroupNames);
        assertEquals(serviceGroupNames[0], serviceGroupName);*/
    }

    public void testApplyPolicy() throws ClusteringFault, XMLStreamException {

        String serviceGroupName = "testService";
//        clusterManager1.getNodeManager().loadServiceGroups(new String[]{serviceGroupName});
        String policyID = "policy1";

        /*try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Policy policy = new Policy();
        policy.setId(policyID);

        StringWriter writer = new StringWriter();
        XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newInstance()
                .createXMLStreamWriter(writer);

        policy.serialize(xmlStreamWriter);
        xmlStreamWriter.flush();

        clusterManager1.getConfigurationManager().applyPolicy(serviceGroupName, writer.toString());

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List eventList = configurationManagerListener2.getEventList();
        assertNotNull(eventList);
        assertEquals(eventList.size(), 2);
        ConfigurationEvent event = (ConfigurationEvent) eventList.get(1);
        assertNotNull(event);
        assertEquals(event.getServiceName(), serviceName);
        assertNotNull(event.getPolicy());*/

    }

    public void testPrepare() throws ClusteringFault {

        String serviceGroupName = "testService";
        clusterManager1.getNodeManager().prepare();

        /*try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List eventList = configurationManagerListener2.getEventList();
        assertNotNull(eventList);
        assertEquals(eventList.size(), 1);
        ConfigurationEvent event = (ConfigurationEvent) eventList.get(0);

        assertNotNull(event);*/
    }

    public void testCommit() throws ClusteringFault {

        String serviceGroupName = "testService";
        clusterManager1.getNodeManager().commit();

        /*try {
           Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List eventList = configurationManagerListener2.getEventList();
        assertNotNull(eventList);
        assertEquals(eventList.size(), 1);
        ConfigurationEvent event = (ConfigurationEvent) eventList.get(0);

        assertNotNull(event);*/
    }

    public void testRollback() throws ClusteringFault {

        String serviceGroupName = "testService";
        clusterManager1.getNodeManager().rollback();

        /*try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List eventList = configurationManagerListener2.getEventList();
        assertNotNull(eventList);
        assertEquals(eventList.size(), 1);
        ConfigurationEvent event = (ConfigurationEvent) eventList.get(0);

        assertNotNull(event);*/
    }

    public void testReloadConfiguration() throws ClusteringFault {

        String serviceGroupName = "testService";
//        clusterManager1.getNodeManager().reloadConfiguration();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /*List eventList = configurationManagerListener2.getEventList();
        assertNotNull(eventList);
        assertEquals(eventList.size(), 1);
        ConfigurationEvent event = (ConfigurationEvent) eventList.get(0);

        assertNotNull(event);*/
    }

}
