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

package org.apache.axis2.json.gson.rpc;

import junit.framework.Assert;
import org.apache.axis2.json.gson.UtilTest;
import org.apache.axis2.testutils.UtilServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class JSONRPCIntegrationTest {
    String contentType = "application/json";
    String charSet = "UTF-8";

    @BeforeClass
    public static void startTestServer() throws Exception {
        UtilServer.start("test-repository/gson", "test-repository/gson/axis2.xml");
    }

    @AfterClass
    public static void stopTestServer() throws Exception {
        UtilServer.stop();
    }

    @Test
    public void testJsonRpcMessageReceiver() throws Exception {
        String jsonRequest = "{\"echoPerson\":[{\"arg0\":{\"name\":\"Simon\",\"age\":\"35\",\"gender\":\"male\"}}]}";
        String echoPersonUrl = "http://localhost:" + UtilServer.TESTING_PORT +"/axis2/services/JSONPOJOService/echoPerson";
        String expectedResponse = "{\"response\":{\"name\":\"Simon\",\"age\":\"35\",\"gender\":\"male\"}}";
        String response = UtilTest.post(jsonRequest, echoPersonUrl, contentType, charSet);
        Assert.assertNotNull(response);
        Assert.assertEquals(expectedResponse , response);
    }

    @Test
    public void testJsonInOnlyRPCMessageReceiver() throws Exception {
        String jsonRequest = "{\"ping\":[{\"arg0\":{\"name\":\"Simon\",\"age\":\"35\",\"gender\":\"male\"}}]}";
        String echoPersonUrl = "http://localhost:" + UtilServer.TESTING_PORT +"/axis2/services/JSONPOJOService/ping";
        String response = UtilTest.post(jsonRequest, echoPersonUrl, contentType, charSet);
        Assert.assertEquals("", response);
    }
}
