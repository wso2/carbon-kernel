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

package org.apache.axiom.soap.impl.llom.soap11;

import org.apache.axiom.om.OMTestCase;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;

import javax.xml.stream.XMLStreamException;

public class SOAP11SerializerTest extends OMTestCase {

    public SOAP11SerializerTest(String testName) {
        super(testName);
    }

    protected StAXSOAPModelBuilder getOMBuilder(String fileName) throws Exception {
        return super.getOMBuilder(fileName);
    }

    protected void setUp() throws Exception {
        soapEnvelope =
                (SOAPEnvelope) getOMBuilder("soap/soap11/soap11fault.xml")
                        .getDocumentElement();
    }

    protected void tearDown() throws Exception {
    }

    /**
     * This will check whether we can call the serialize method two times, if the first calls makes
     * the object model.
     *
     * @throws Exception
     */
    public void testSerialize() throws Exception {
        try {
            soapEnvelope.toString();
            soapEnvelope.toStringWithConsume();
        } catch (XMLStreamException e) {
            fail("This test should not fail as one must be able to serialize twice if the object model is built in the first time");
        }
    }
}
