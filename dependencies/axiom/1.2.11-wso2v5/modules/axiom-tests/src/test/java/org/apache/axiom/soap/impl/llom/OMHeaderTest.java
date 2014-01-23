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

import org.apache.axiom.om.OMTestCase;
import org.apache.axiom.om.impl.OMNamespaceImpl;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;

import java.util.Iterator;

public class OMHeaderTest extends OMTestCase {
    SOAPHeader soapHeader;

    public OMHeaderTest(String testName) {
        super(testName);
    }

    public static void main(String[] args) {
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        soapHeader = soapEnvelope.getHeader();
    }

    public void testAddHeaderElement() {
        String newElementName = "MyHeaderElement";
        SOAPHeaderBlock soapHeaderElement = soapHeader.addHeaderBlock(
                newElementName,
                new OMNamespaceImpl("http://opensource.lk", "lsf"));
        assertTrue(
                "Header Element added has different parent than it should have",
                soapHeaderElement.getParent() == soapHeader);
        assertTrue(
                "Header Element added has different localname than it was given",
                soapHeaderElement.getLocalName().equalsIgnoreCase(
                        newElementName));
    }

    public void testExamineHeaderElements() {
    }

    public void testExtractHeaderElements() {
        //TODO Implement extractHeaderBlocks().
    }

    public void testExamineMustUnderstandHeaderElements() {
        //TODO Implement examineMustUnderstandHeaderBlocks().
    }

    public void testExamineAllHeaderElements() {
        Iterator iterator = soapHeader.examineAllHeaderBlocks();
        int headerElementCount = 0;
        while (iterator.hasNext()) {
            iterator.next();
            headerElementCount++;
        }
        assertTrue(
                "Number of header elements in the header differs from expected value of 3",
                headerElementCount == 3);
    }

    public void testExtractAllHeaderElements() {
        //TODO Implement extractAllHeaderBlocks().
    }

}
