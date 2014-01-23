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
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.axis2.util.Utils;

import javax.xml.namespace.QName;

public class EchoRawRuntimeProxyTest extends UtilServerBasedTestCase {
    public static final EndpointReference targetEPR = new EndpointReference(
            "http://localhost:5555"
                    + "/axis2/services/EchoXMLService/echoOMElement");

    public static final QName serviceName = new QName("EchoXMLService");

    public static final QName operationName = new QName("echoOMElement");


    public EchoRawRuntimeProxyTest() {
        super(EchoRawRuntimeProxyTest.class.getName());
    }

    public EchoRawRuntimeProxyTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return getTestSetup(new TestSuite(EchoRawRuntimeProxyTest.class));
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


    public void testEchoXMLSync() throws Exception {

        OMElement payload = TestingUtils.createDummyOMElement();
        /**
         * Proxy setting in runtime
         */
        HttpTransportProperties.ProxyProperties proxyproperties =
                new HttpTransportProperties.ProxyProperties();
        proxyproperties.setProxyName("localhost");
        proxyproperties.setProxyPort(5555);
        proxyproperties.setDomain("anonymous");
        proxyproperties.setPassWord("anonymous");
        proxyproperties.setUserName("anonymous");

        Options options = new Options();
        options.setProperty(HTTPConstants.PROXY, proxyproperties);
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                        null, null);
        ServiceClient sender = new ServiceClient(configContext, null);
        sender.setOptions(options);

        OMElement result = sender.sendReceive(payload);

        TestingUtils.compareWithCreatedOMElement(result);
    }
}
