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

package org.apache.axis2.swa;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axis2.Constants;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.util.Utils;

public class EchoRawSwAFileCacheTest extends EchoRawSwATest {

    private AxisService service;


    public EchoRawSwAFileCacheTest() {
        super(EchoRawSwAFileCacheTest.class.getName());
    }

    public EchoRawSwAFileCacheTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return getTestSetup2(new TestSuite(EchoRawSwAFileCacheTest.class),
                             TestingUtils.prefixBaseDirectory(Constants.TESTING_PATH + "SwA-fileCache-enabledRepository"));
    }

    protected void setUp() throws Exception {
        service = Utils.createSimpleService(serviceName, EchoSwA.class.getName(),
                                            operationName);
        UtilServer.deployService(service);
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.unDeployClientService();
    }

//    public void testEchoXMLASync() throws Exception {
//        super.testEchoXMLASync();
//    }

    public void testEchoXMLSync() throws Exception {
        super.testEchoXMLSync();
    }

//    public void testEchoXMLSyncSeperateListener() throws Exception {
//        super.testEchoXMLSyncSeperateListener();
//    }
}
