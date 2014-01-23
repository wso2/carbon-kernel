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

import org.apache.axiom.om.AbstractTestCase;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;

import javax.xml.stream.XMLStreamReader;

public class WrongEnvelopeNamespaceTester extends AbstractTestCase {
    public void testCode() {
        try {
            String filename = "soap/wrongEnvelopeNamespace.xml";
            XMLStreamReader xmlr = StAXUtils.createXMLStreamReader(
                    getTestResource(filename));
            StAXBuilder builder = new StAXSOAPModelBuilder(xmlr, null); //exception here
            fail("Builder must fail here due to wrong SOAP namespace");
        } catch (SOAPProcessingException e) {
            assertTrue(true);
        } catch (Exception e) {
            fail("Only SOAPProcessingException can be thrown here");
        }
    }

    public static void main(String[] args) {
        WrongEnvelopeNamespaceTester tester = new WrongEnvelopeNamespaceTester();
        tester.testCode();
    }
}
