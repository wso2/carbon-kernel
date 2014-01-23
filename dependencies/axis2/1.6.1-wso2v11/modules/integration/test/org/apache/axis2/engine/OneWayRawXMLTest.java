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
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.apache.axis2.util.Utils;

public class OneWayRawXMLTest extends UtilServerBasedTestCase implements TestConstants {
    public static Test suite() {
        return getTestSetup(new TestSuite(OneWayRawXMLTest.class));
    }
    
	private boolean received;
    protected AxisService service;
    protected void setUp() throws Exception {
    	service = Utils.createSimpleInOnlyService(serviceName,new MessageReceiver(){
            public void receive(MessageContext messageCtx) throws AxisFault {
                SOAPEnvelope envelope = messageCtx.getEnvelope();
                TestingUtils.compareWithCreatedOMElement(envelope.getBody().getFirstElement());
                received = true;
            }
        },
                operationName);
        UtilServer.deployService(service);
    }

    public void testOneWay() throws Exception {
        ConfigurationContext configContext =
            ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                    TestingUtils.prefixBaseDirectory(Constants.TESTING_PATH + "integrationRepo/"), null);
        ServiceClient sender = new ServiceClient(configContext, null);
        Options op = new Options();
//        op.setTo(new EndpointReference(
// //               "http://127.0.0.1:" + (UtilServer.TESTING_PORT)
//                "http://127.0.0.1:" + 5556
//                        + "/axis2/services/"+service.getName()+"/"+operationName.getLocalPart()));
        op.setTo(targetEPR);
        op.setAction("urn:SomeAction");
        sender.setOptions(op);
        sender.fireAndForget(TestingUtils.createDummyOMElement());
        int index = 0;
        while (!received) {
            Thread.sleep(1000);
            index++;
            if (index == 20) {
                throw new AxisFault("error Occured");
            }
        }
    }

}
