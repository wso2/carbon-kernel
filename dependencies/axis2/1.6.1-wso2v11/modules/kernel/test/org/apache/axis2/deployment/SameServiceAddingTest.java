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

package org.apache.axis2.deployment;

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.engine.AxisConfiguration;

/**
 * This test confirms that we behave correctly when adding ServiceGroups with duplicate
 * Services to ones that have already been deployed.
 */
public class SameServiceAddingTest extends TestCase {
    AxisConfiguration config;

    public void testServiceGroup() throws AxisFault {
        final String SERVICE1 = "serevice1";
        final String SERVICE2 = "serevice2";
        final String SERVICE4 = "serevice4";
        final String SERVICE_GROUP2 = "ServiceGroup2";

        config = ConfigurationContextFactory.createEmptyConfigurationContext()
                .getAxisConfiguration();

        // First create a ServiceGroup with S1 and S4
        AxisServiceGroup axisServiceGroup1 = new AxisServiceGroup();
        axisServiceGroup1.setServiceGroupName("ServiceGroup1");
        AxisService service1 = new AxisService();
        service1.setName(SERVICE1);
        axisServiceGroup1.addService(service1);

        AxisService service4 = new AxisService();
        service4.setName(SERVICE4);
        axisServiceGroup1.addService(service4);
        config.addServiceGroup(axisServiceGroup1);

        // Now create another ServiceGroup with S2 and S4
        AxisServiceGroup axisServiceGroup2 = new AxisServiceGroup();
        axisServiceGroup2.setServiceGroupName(SERVICE_GROUP2);
        AxisService service2 = new AxisService();
        service2.setName(SERVICE2);
        axisServiceGroup2.addService(service2);

        AxisService service24 = new AxisService();
        service24.setName(SERVICE4);
        axisServiceGroup2.addService(service24);
        try {
            // This should fail!
            config.addServiceGroup(axisServiceGroup2);
        } catch (AxisFault axisFault) {
            // This is expected because S4 was a duplicate name to an already existing service
            assertTrue("Caught the wrong fault!", axisFault.getMessage().indexOf(SERVICE4) > -1);
        }


        AxisService service = config.getService(SERVICE1);
        assertNotNull("Service 1 wasn't deployed!", service);
        service = config.getService(SERVICE4);
        assertNotNull("Service 4 wasn't deployed!", service);

        service = config.getService(SERVICE2);
        assertNull("Service 2 wasn't supposed to be deployed!", service);
        assertNull("ServiceGroup2 wasn't supposed to be deployed!",
                   config.getServiceGroup("service2"));
    }

}
