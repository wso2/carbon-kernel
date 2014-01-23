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
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.apache.axis2.util.Utils;

public class MultipleInvocationTest extends UtilServerBasedTestCase implements TestConstants {
    protected AxisConfiguration engineRegistry;
    protected MessageContext mc;
    protected ServiceContext serviceContext;
    protected AxisService service;

    protected boolean finish = false;

    public MultipleInvocationTest() {
        super(MultipleInvocationTest.class.getName());
    }

    public MultipleInvocationTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return getTestSetup(new TestSuite(MultipleInvocationTest.class));
    }

    protected void setUp() throws Exception {
        service =
                Utils.createSimpleService(serviceName,
                        Echo.class.getName(),
                        operationName);
        UtilServer.deployService(service);
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.unDeployClientService();
    }

    public void testMutipleInvocation() throws Exception {

        OMElement payload = TestingUtils.createDummyOMElement();
        Options options = new Options();
        options.setAction("urn:echoOM");
        options.setTo(targetEPR);
        options.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        options.setCallTransportCleanup(true);

        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                        TestingUtils.prefixBaseDirectory(Constants.TESTING_REPOSITORY), null);
        ServiceClient sender = new ServiceClient(configContext, null);
        sender.engageModule("addressing");
        sender.setOptions(options);
        OMElement result = sender.sendReceive(payload);
        TestingUtils.compareWithCreatedOMElement(result);
        result = sender.sendReceive(payload);
        TestingUtils.compareWithCreatedOMElement(result);
        result = sender.sendReceive(payload);
        TestingUtils.compareWithCreatedOMElement(result);
        result = sender.sendReceive(payload);
        TestingUtils.compareWithCreatedOMElement(result);
    }

}
