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
import org.apache.axis2.AbstractTestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;

import java.util.List;

/**
 * When you develop service, you can specify the list of transports on which you service should be exposed.
 * This class will test this functionality.
 */
public class ExposedTransportsTest extends TestCase {
    AxisConfiguration ar;
    String repo = AbstractTestCase.basedir + "/test-resources/deployment/exposedTransportsRepo";

    protected void setUp() throws Exception {
        ar = ConfigurationContextFactory.createConfigurationContextFromFileSystem(repo, repo + "/axis2.xml")
                .getAxisConfiguration();
    }

    /**
     * EchoService1 should only be exposed on http
     * @throws AxisFault in the case of an Error
     */
    public void testExposedTransportsEchoService1() throws AxisFault {
        AxisService service1 = ar.getService("EchoService1");
        assertNotNull(service1);

        List<String> exposedTransportsList = service1.getExposedTransports();
        assertFalse("Exposed Transports list should not be empty.", exposedTransportsList.isEmpty());

        assertTrue("EchoService1 is not exposed on http.", checkExistenceInList(exposedTransportsList, "http"));

        assertEquals("EchoService1 should only be exposed on http.", 1, exposedTransportsList.size());
    }

    /**
     * https transport is not available in Axis2. But the deployment of the EchoService2 should not fail.
     *     and also EchoService2 should be deployed in http
     * @throws AxisFault in the case of an Error
     */
    public void testExposedTransportsEchoService2() throws AxisFault {

        AxisService service1 = ar.getService("EchoService2");
        assertNotNull(service1);

        List<String> exposedTransportsList = service1.getExposedTransports();
        assertFalse("Exposed Transport list should not be empty.", exposedTransportsList.isEmpty());

        assertFalse("EchoService2 should not be exposed on https.", checkExistenceInList(exposedTransportsList, "https"));

        assertTrue("EchoService2 is not exposed on http.", checkExistenceInList(exposedTransportsList, "http"));

        assertTrue("EchoService2 is not exposed on jms.", checkExistenceInList(exposedTransportsList, "jms"));

        assertEquals("EchoService1 should only be exposed on http.", 2, exposedTransportsList.size());
    }

    /**
     * https transport is not available in Axis2. Therefore the deployment of the EchoService2 should fail.
     * @throws AxisFault in the case of an Error
     */
    public void testExposedTransportsEchoService3() throws AxisFault {
        AxisService service1 = ar.getService("EchoService3");
        assertNull("EchoService3 deployment should fail, because it has been exposed in unavailable transports.", service1);
    }

    private boolean checkExistenceInList(List<String> exposedTransportsList, String value){
        for(String transportName : exposedTransportsList){
            if(value.equalsIgnoreCase(transportName)) {
                return true;
            }
        }
        return false;
    }
}
