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

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;

import javax.xml.namespace.QName;
import java.net.URL;

public class WSDLClientTest extends UtilServerBasedTestCase implements TestConstants {

    protected AxisService service;

    public static Test suite() {
        return getTestSetup(new TestSuite(WSDLClientTest.class));
    }

    protected void setUp() throws Exception {
        service = AxisService.createService(Echo.class.getName(),
                                            UtilServer
                                                    .getConfigurationContext().getAxisConfiguration());
        service.setName(serviceName.getLocalPart());
        UtilServer.deployService(service);
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.unDeployClientService();
    }

    public void testWSDLClient() throws Exception {
        URL wsdlURL = new URL("http://localhost:" + UtilServer.TESTING_PORT +
                "/axis2/services/EchoXMLService?wsdl");
        ServiceClient serviceClient = new ServiceClient(null, wsdlURL,
                                                        new QName(
                                                                "http://engine.axis2.apache.org",
                                                                "EchoXMLService"),
                                                        "EchoHttpSoap11Endpoint");
        OMElement payload =
                TestingUtils.createDummyOMElement("http://engine.axis2.apache.org");
        String epr = "http://127.0.0.1:" + UtilServer.TESTING_PORT +
                "/axis2/services/EchoXMLService";
        //This is not smt we need to do but , my build is fail if I dont do that :)
        serviceClient.getOptions().setTo(new EndpointReference(epr));
        System.out.println(serviceClient.getOptions().getTo().getAddress());
        OMElement response = serviceClient.sendReceive(new QName("http://engine.axis2.apache.org", "echoOM"), payload);
        assertNotNull(response);
        String textValue = response.getFirstElement().getFirstElement().getText();
        assertEquals(textValue, "Isaac Asimov, The Foundation Trilogy");
    }
}
