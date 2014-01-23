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
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.apache.axis2.wsdl.WSDLConstants;

import javax.xml.namespace.QName;
import java.util.ArrayList;

public class ServiceCreateTest extends UtilServerBasedTestCase {

    ConfigurationContext configContext;
    ConfigurationContext clinetConfigurationctx;

    public static Test suite() {
        return getTestSetup(new TestSuite(ServiceCreateTest.class));
    }

    protected void setUp() throws Exception {
        configContext = UtilServer.getConfigurationContext();
        clinetConfigurationctx = ConfigurationContextFactory.
                createConfigurationContextFromFileSystem(null, null);
    }

    public void testServiceCreate() throws AxisFault {
        AxisConfiguration axisConfig = configContext.getAxisConfiguration();
        AxisService service =
                AxisService.createService("org.apache.axis2.engine.MyService", axisConfig);
        assertNotNull(service);
        axisConfig.addService(service);
        assertEquals("MyService", service.getName());
        AxisOperation axisOperation = service.getOperation(new QName("add"));
        assertNotNull(axisOperation);
        AxisMessage messge = axisOperation.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
        // messge.getSchemaElement().toString()
        assertNotNull(messge);
        assertNotNull(messge.getSchemaElement());
        assertNotNull(service.getOperation(new QName("putValue")));
        assertNotNull(axisConfig.getService("MyService"));

        RPCServiceClient client = new RPCServiceClient(clinetConfigurationctx, null);

        EndpointReference targetEPR = new EndpointReference(
                "http://127.0.0.1:" + (UtilServer.TESTING_PORT)
                        + "/axis2/services/MyService/add");
        Options options = new Options();

        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        client.setOptions(options);
        ArrayList args = new ArrayList();
        args.add("100");
        args.add("200");

        OMElement response = client.invokeBlocking(
                new QName("http://engine.axis2.apache.org", "add", "ns1"), args.toArray());
        assertEquals(Integer.parseInt(response.getFirstElement().getText()), 300);
    }
}
