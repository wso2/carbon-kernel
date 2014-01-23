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

//todo

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.apache.axis2.util.Utils;

public class EchoRawXMLChunkedTest extends UtilServerBasedTestCase implements TestConstants {

    private static final String CLIENT_HOME = TestingUtils.prefixBaseDirectory(Constants.TESTING_PATH + "chunking-enabledRepository");

    public EchoRawXMLChunkedTest() {
        super(EchoRawXMLChunkedTest.class.getName());
    }

    public EchoRawXMLChunkedTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return getTestSetup2(new TestSuite(EchoRawXMLChunkedTest.class), CLIENT_HOME);
    }

    protected void setUp() throws Exception {
        AxisService service = Utils.createSimpleService(serviceName,
                                                        Echo.class.getName(),
                                                        operationName);
        UtilServer.deployService(service);
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
    }

    public void testEchoXMLSync() throws Exception {
        OMElement payload = TestingUtils.createDummyOMElement();

        Options clientOptions = new Options();
        clientOptions.setTransportInProtocol(Constants.TRANSPORT_HTTP);

        ConfigurationContext configContext =
                ConfigurationContextFactory
                        .createConfigurationContextFromFileSystem(CLIENT_HOME, null);
        ServiceClient sender = new ServiceClient(configContext, null);
        sender.setOptions(clientOptions);
        clientOptions.setTo(targetEPR);

        OMElement result = sender.sendReceive(payload);


        TestingUtils.compareWithCreatedOMElement(result);
    }
}
