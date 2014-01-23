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

package org.apache.axis2.json.gson;

import org.apache.axis2.testutils.UtilServer;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;


public class JSONXMLStreamAPITest {

    @Before
    public void setUp()throws Exception {
        UtilServer.start("target/repo", "test-repository/gson/axis2.xml");

    }

    @After
    public void tearDown()throws Exception {
        UtilServer.stop();

    }

    @Test
    public void xmlStreamAPITest()throws Exception{
        String getLibURL = "http://localhost:" + UtilServer.TESTING_PORT +"/axis2/services/LibraryService/getLibrary";
        String echoLibURL = "http://localhost:" + UtilServer.TESTING_PORT +"/axis2/services/LibraryService/echoLibrary";
        String contentType = "application/json";
        String charSet = "UTF-8";

        String echoLibrary = "{\"echoLibrary\":{\"args0\":{\"admin\":{\"address\":{\"city\":\"My City\",\"country\":" +
                "\"My Country\",\"street\":\"My Street\",\"zipCode\":\"00000\"},\"age\":24,\"name\":\"micheal\"," +
                "\"phone\":12345},\"books\":[{\"author\":\"scofield\",\"numOfPages\":75,\"publisher\":\"malpiyali\"," +
                "\"reviewers\":[\"rev1\",\"rev2\",\"rev3\"]},{\"author\":\"Redman\",\"numOfPages\":75,\"publisher\":" +
                "\"malpiyali\",\"reviewers\":[\"rev1\",\"rev2\",\"rev3\"]},{\"author\":\"Snow\",\"numOfPages\":75," +
                "\"publisher\":\"malpiyali\",\"reviewers\":[\"rev1\",\"rev2\",\"rev3\"]},{\"author\":\"White\"," +
                "\"numOfPages\":75,\"publisher\":\"malpiyali\",\"reviewers\":[\"rev1\",\"rev2\",\"rev3\"]},{" +
                "\"author\":\"Jack\",\"numOfPages\":75,\"publisher\":\"malpiyali\",\"reviewers\":[\"rev1\",\"rev2\"," +
                "\"rev3\"]}],\"staff\":55}}}";

        String getLibrary = "{\"getLibrary\":{\"args0\":\"Newman\"}}";

        String echoLibraryResponse = "{\"echoLibraryResponse\":{\"return\":{\"admin\":{\"address\":{\"city\":" +
                "\"My City\",\"country\":\"My Country\",\"street\":\"My Street\",\"zipCode\":\"00000\"},\"age\":24," +
                "\"name\":\"micheal\",\"phone\":12345},\"books\":[{\"author\":\"scofield\",\"numOfPages\":75," +
                "\"publisher\":\"malpiyali\",\"reviewers\":[\"rev1\",\"rev2\",\"rev3\"]},{\"author\":\"Redman\"," +
                "\"numOfPages\":75,\"publisher\":\"malpiyali\",\"reviewers\":[\"rev1\",\"rev2\",\"rev3\"]}," +
                "{\"author\":\"Snow\",\"numOfPages\":75,\"publisher\":\"malpiyali\",\"reviewers\":[\"rev1\",\"rev2\"," +
                "\"rev3\"]},{\"author\":\"White\",\"numOfPages\":75,\"publisher\":\"malpiyali\",\"reviewers\":" +
                "[\"rev1\",\"rev2\",\"rev3\"]},{\"author\":\"Jack\",\"numOfPages\":75,\"publisher\":\"malpiyali\"," +
                "\"reviewers\":[\"rev1\",\"rev2\",\"rev3\"]}],\"staff\":55}}}";

        String getLibraryResponse = "{\"getLibraryResponse\":{\"return\":{\"admin\":{\"address\":{\"city\":\"My City\"," +
                "\"country\":\"My Country\",\"street\":\"My Street\",\"zipCode\":\"00000\"},\"age\":24,\"name\":" +
                "\"Newman\",\"phone\":12345},\"books\":[{\"author\":\"Jhon_0\",\"numOfPages\":175,\"publisher\":" +
                "\"Foxier\",\"reviewers\":[\"rev1\",\"rev2\",\"rev3\"]},{\"author\":\"Jhon_1\",\"numOfPages\":175," +
                "\"publisher\":\"Foxier\",\"reviewers\":[\"rev1\",\"rev2\",\"rev3\"]},{\"author\":\"Jhon_2\"," +
                "\"numOfPages\":175,\"publisher\":\"Foxier\",\"reviewers\":[\"rev1\",\"rev2\",\"rev3\"]},{\"author\":" +
                "\"Jhon_3\",\"numOfPages\":175,\"publisher\":\"Foxier\",\"reviewers\":[\"rev1\",\"rev2\",\"rev3\"]}," +
                "{\"author\":\"Jhon_4\",\"numOfPages\":175,\"publisher\":\"Foxier\",\"reviewers\":[\"rev1\",\"rev2\"," +
                "\"rev3\"]}],\"staff\":50}}}";

        String actualResponse = UtilTest.post(echoLibrary, echoLibURL, contentType, charSet);
        Assert.assertNotNull(actualResponse);
        Assert.assertEquals(echoLibraryResponse , actualResponse);

        String actualRespose_2 = UtilTest.post(getLibrary, getLibURL, contentType, charSet);
        Assert.assertNotNull(actualRespose_2);
        Assert.assertEquals(getLibraryResponse, actualRespose_2);

    }




}
