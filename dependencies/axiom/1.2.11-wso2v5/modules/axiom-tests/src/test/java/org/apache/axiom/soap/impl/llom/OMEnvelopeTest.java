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

package org.apache.axiom.soap.impl.llom;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMTestCase;
import org.apache.axiom.om.OMTestUtils;
import org.apache.axiom.om.TestConstants;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class OMEnvelopeTest extends OMTestCase {
    private static Log log = LogFactory.getLog(OMEnvelopeTest.class);

    public OMEnvelopeTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testGetHeader1() {
        SOAPHeader header = soapEnvelope.getHeader();
        assertTrue("Header information retrieved not correct",
                   (header != null &&
                           header.getLocalName().equalsIgnoreCase("Header")));
    }

    public void testGetBody1() {
        SOAPBody body = soapEnvelope.getBody();
        assertTrue("Header information retrieved not correct",
                   (body != null && body.getLocalName().equalsIgnoreCase("Body")));
    }

    private SOAPEnvelope getSecondEnvelope() throws Exception {
        return (SOAPEnvelope) OMTestUtils.getOMBuilder(
                getTestResource(TestConstants.SAMPLE1))
                .getDocumentElement();
    }

    public void testGetHeader2() throws Exception {
        SOAPHeader header = getSecondEnvelope().getHeader();
        assertTrue("Header information retrieved not correct",
                   (header != null &&
                           header.getLocalName().equalsIgnoreCase("Header")));
        header.close(false);
    }

    public void testGetBody2() throws Exception {
        SOAPBody body = getSecondEnvelope().getBody();
        assertTrue("Header information retrieved not correct",
                   (body != null && body.getLocalName().equalsIgnoreCase("Body")));
        body.close(false);
    }

    public void testDefaultEnveleope() {
        SOAPEnvelope env = OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();
        assertNotNull(env);
        assertNotNull("Body should not be null", env.getBody());
    }
}
