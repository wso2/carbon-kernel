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
package org.apache.axiom.ts.soap.header;

import java.util.Iterator;

import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axiom.ts.soap.SOAPSpec;
import org.apache.axiom.ts.soap.SOAPTestCase;

public class TestExamineHeaderBlocks extends SOAPTestCase {
    public TestExamineHeaderBlocks(OMMetaFactory metaFactory, SOAPSpec spec) {
        super(metaFactory, spec);
    }

    protected void runTest() throws Throwable {
        String roleNextURI = spec.getNextRoleURI();
        SOAPEnvelope soapEnvelope = soapFactory.createSOAPEnvelope();
        SOAPHeader soapHeader = soapFactory.createSOAPHeader(soapEnvelope);
        OMNamespace namespace = soapFactory.createOMNamespace("http://www.example.org", "test");
        soapHeader.addHeaderBlock("echoOk1", namespace).setRole("urn:test-role");
        soapHeader.addHeaderBlock("echoOk2", namespace).setRole(roleNextURI);
        Iterator iterator = soapHeader.examineHeaderBlocks(roleNextURI);
        iterator.hasNext();
        SOAPHeaderBlock headerBlockWithRole = (SOAPHeaderBlock) iterator.next();
        assertEquals(
                "SOAP Header Test : - headerBlockWithRole local name mismatch",
                "echoOk2", headerBlockWithRole.getLocalName());
        assertEquals(
                "SOAP Header Test : - headerBlockWithRole role value mismatch",
                roleNextURI, headerBlockWithRole.getRole());

        assertFalse(
                "SOAP Header Test : - header has one headerBlock with role, but examineHeaderBlocks(String role) method returns an iterator with more than one object",
                iterator.hasNext());
    }
}
