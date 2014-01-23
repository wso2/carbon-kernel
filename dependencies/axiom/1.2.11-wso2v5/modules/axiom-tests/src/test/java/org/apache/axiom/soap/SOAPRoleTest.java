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

package org.apache.axiom.soap;

import javax.xml.stream.XMLStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axiom.om.AbstractTestCase;
import org.apache.axiom.om.util.StAXUtils;

public class SOAPRoleTest extends AbstractTestCase {
    public static final String CUSTOM_ROLE = "http://example.org/myCustomRole";

    class MyRolePlayer implements RolePlayer {
        boolean ultimateReceiver;
        List roles;

        public MyRolePlayer(boolean ultimateReceiver) {
            this.ultimateReceiver = ultimateReceiver;
            roles = null;
        }

        public MyRolePlayer(boolean ultimateReceiver, String [] roles) {
            this.ultimateReceiver = ultimateReceiver;
            this.roles = new ArrayList();
            for (int i = 0; i < roles.length; i++) {
                this.roles.add(roles[i]);
            }
        }

        public List getRoles() {
            return roles;
        }

        public boolean isUltimateDestination() {
            return ultimateReceiver;
        }
    }

    public SOAPRoleTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    public StAXSOAPModelBuilder getSOAPBuilder(String fileName) throws Exception {
        XMLStreamReader parser = StAXUtils.createXMLStreamReader(
                getTestResource(fileName));
        return new StAXSOAPModelBuilder(parser, null);
    }

    public void testSOAP11Roles() throws Exception {
        String testfile = "soap/soap11/soap11RoleMessage.xml";
        StAXSOAPModelBuilder builder = getSOAPBuilder(testfile);
        SOAPEnvelope env = builder.getSOAPEnvelope();
        SOAPHeader soapHeader = env.getHeader();

        String roles [] = { CUSTOM_ROLE };
        RolePlayer rp = new MyRolePlayer(true, roles);

        Iterator headers = soapHeader.getHeadersToProcess(rp);
        assertTrue("No headers!", headers.hasNext());

        int numHeaders = 0;
        while (headers.hasNext()) {
            SOAPHeaderBlock header = (SOAPHeaderBlock)headers.next();
            assertNotNull(header);
            numHeaders++;
        }

        assertEquals("Didn't get right number of headers (with custom role)", 4, numHeaders);

        rp = new MyRolePlayer(true);

        headers = soapHeader.getHeadersToProcess(rp);
        assertTrue(headers.hasNext());

        numHeaders = 0;
        while (headers.hasNext()) {
            SOAPHeaderBlock header = (SOAPHeaderBlock)headers.next();
            assertNotNull(header);
            numHeaders++;
        }

        assertEquals("Didn't get right number of headers (no custom role)", 3, numHeaders);
        
        env.close(false);
    }

    public void testSOAP12Roles() throws Exception {
        String testfile = "soap/soap12RoleMessage.xml";
        StAXSOAPModelBuilder builder = getSOAPBuilder(testfile);
        SOAPEnvelope env = builder.getSOAPEnvelope();
        SOAPHeader soapHeader = env.getHeader();

        String roles [] = { CUSTOM_ROLE };
        RolePlayer rp = new MyRolePlayer(true, roles);

        Iterator headers = soapHeader.getHeadersToProcess(rp);
        assertTrue("No headers!", headers.hasNext());

        int numHeaders = 0;
        while (headers.hasNext()) {
            SOAPHeaderBlock header = (SOAPHeaderBlock)headers.next();
            numHeaders++;
        }

        assertEquals("Didn't get right number of headers (with custom role)", 5, numHeaders);

        rp = new MyRolePlayer(true);

        headers = soapHeader.getHeadersToProcess(rp);
        assertTrue(headers.hasNext());

        numHeaders = 0;
        while (headers.hasNext()) {
            SOAPHeaderBlock header = (SOAPHeaderBlock)headers.next();
            numHeaders++;
        }

        assertEquals("Didn't get right number of headers (no custom role)", 4, numHeaders);

        // Intermediary test
        rp = new MyRolePlayer(false);

        headers = soapHeader.getHeadersToProcess(rp);
        assertTrue(headers.hasNext());

        numHeaders = 0;
        while (headers.hasNext()) {
            SOAPHeaderBlock header = (SOAPHeaderBlock)headers.next();
            numHeaders++;
        }

        assertEquals("Didn't get right number of headers (no custom role)", 1, numHeaders);
        
        env.close(false);
    }
}
