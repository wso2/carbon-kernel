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

package org.apache.axis2.util;

import org.apache.axis2.AbstractTestCase;

/**
 * Test that things break
 */

public class UtilsParseRequestTest extends AbstractTestCase {

    public UtilsParseRequestTest(String testName) {
        super(testName);
    }

    public void testfailure() throws Exception {
        //fail("here");
    }

    public void testService() throws Exception {
        assertParsesTo("http://localhost:8081/axis2/services/System",
                       "System");
    }

    public void testServiceCalledServices() throws Exception {
        assertParsesTo("http://localhost:8081/axis2/services/services",
                       "services");
    }

    public void testServiceWithQuery() throws Exception {
        assertParsesTo("http://localhost:8081/axis2/services/System?system=ecb2f",
                       "System");
    }

    public void testServiceWithDoubleQuery() throws Exception {
        assertParsesTo("http://localhost:8081/axis2/services/System?system=ecb2f?job=3",
                       "System");
    }

    public void testOperation() throws Exception {
        assertParsesTo("http://localhost:8081/axis2/services/System/operation",
                       "System", "operation");
    }

    public void testOperationWithQuery() throws Exception {
        assertParsesTo("http://localhost:8081/axis2/services/System/operation?system=ecb2f",
                       "System", "operation");
    }

    public void testOperationServiceCalledServices() throws Exception {
        assertParsesTo("http://localhost:8081/axis2/services/services/operation",
                       "services", "operation");
    }

    private void assertParsesTo(String path, String service) {
        assertParsesTo(path, service, null);
    }

    private void assertParsesTo(String path, String service, String operation) {
        String[] strings = Utils.parseRequestURLForServiceAndOperation(path, "/axis2/services");
        assertEquals(service, strings[0]);
        assertEquals(operation, strings[1]);
    }

}
