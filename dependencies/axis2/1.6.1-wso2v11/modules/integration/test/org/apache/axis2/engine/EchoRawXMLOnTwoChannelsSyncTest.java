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
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.apache.axis2.util.Utils;

public class EchoRawXMLOnTwoChannelsSyncTest extends UtilServerBasedTestCase
        implements TestConstants {

    public EchoRawXMLOnTwoChannelsSyncTest() {
        super(EchoRawXMLOnTwoChannelsSyncTest.class.getName());
    }

    public EchoRawXMLOnTwoChannelsSyncTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return getTestSetup(new TestSuite(EchoRawXMLOnTwoChannelsSyncTest.class));
    }

    protected void setUp() throws Exception {
        AxisService service =
                Utils.createSimpleService(serviceName,
                                          Echo.class.getName(),
                                          operationName);
        UtilServer.deployService(service);

    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
    }


    public void testEchoXMLCompleteSync() throws Exception {
        AxisService service =
                Utils.createSimpleServiceforClient(serviceName,
                                                   Echo.class.getName(),
                                                   operationName);

        ConfigurationContext configConetxt = UtilServer.createClientConfigurationContext();

        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");
        OMElement method = fac.createOMElement("echoOMElement", omNs);
        OMElement value = fac.createOMElement("myValue", omNs);
        value.setText("Isaac Asimov, The Foundation Trilogy");
        method.addChild(value);
        Options options = new Options();
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        options.setUseSeparateListener(true);
        options.setAction(operationName.getLocalPart());

        ServiceClient sender = new ServiceClient(configConetxt, service);
        sender.setOptions(options);

        OMElement result = sender.sendReceive(operationName, method);

        TestingUtils.compareWithCreatedOMElement(result);
        sender.cleanup();

    }

    public void testEchoXMLCompleteSyncwithTwoTransport() throws Exception {
        AxisService service =
                Utils.createSimpleServiceforClient(serviceName,
                                                   Echo.class.getName(),
                                                   operationName);

        ConfigurationContext configConetxt = UtilServer.createClientConfigurationContext();

        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");
        OMElement method = fac.createOMElement("echoOMElement", omNs);
        OMElement value = fac.createOMElement("myValue", omNs);
        value.setText("Isaac Asimov, The Foundation Trilogy");
        method.addChild(value);
        Options options = new Options();
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_LOCAL);
        options.setUseSeparateListener(false);
        options.setAction(operationName.getLocalPart());

        ServiceClient sender = new ServiceClient(configConetxt, service);
        sender.setOptions(options);

        OMElement result = sender.sendReceive(operationName, method);

        TestingUtils.compareWithCreatedOMElement(result);
        sender.cleanup();

    }


}
