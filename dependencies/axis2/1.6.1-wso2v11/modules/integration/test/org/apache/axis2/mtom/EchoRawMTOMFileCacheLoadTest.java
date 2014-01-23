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

package org.apache.axis2.mtom;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axis2.Constants;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.Echo;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.util.Utils;

import javax.xml.namespace.QName;

public class EchoRawMTOMFileCacheLoadTest extends EchoRawMTOMLoadTest {

    private QName serviceName = new QName("EchoXMLService");

    private QName operationName = new QName("echoOMElement");

    private AxisService service;

    public EchoRawMTOMFileCacheLoadTest() {
        super(EchoRawMTOMFileCacheLoadTest.class.getName());
    }

    public EchoRawMTOMFileCacheLoadTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return getTestSetup2(new TestSuite(EchoRawMTOMFileCacheLoadTest.class),
                             TestingUtils.prefixBaseDirectory(Constants.TESTING_PATH + "MTOM-fileCache-enabledRepository"));
    }

    protected void setUp() throws Exception {
        service = Utils.createSimpleService(serviceName, Echo.class.getName(),
                                            operationName);
        UtilServer.deployService(service);
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.unDeployClientService();
    }


    public void testEchoXMLSync() throws Exception {
        super.testEchoXMLSync();
    }
}
