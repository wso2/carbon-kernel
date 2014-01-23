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

package org.apache.axiom.om;

import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class OMElementCloneTest extends AbstractTestCase {

    public void testElementCloningWithoutUsingOMElementMethod() throws Exception {
        SOAPEnvelope soapEnvelope =
                (SOAPEnvelope) OMTestUtils.getOMBuilder(
                        getTestResource(TestConstants.SOAP_SOAPMESSAGE))
                        .getDocumentElement();
        SOAPBody body = soapEnvelope.getBody();

        OMElement firstClonedBodyElement =
                new StAXOMBuilder(body.getXMLStreamReader()).getDocumentElement();
        OMElement secondClonedBodyElement =
                new StAXOMBuilder(body.getXMLStreamReader()).getDocumentElement();

        // first check whether both have the same information
        String firstClonedBodyElementText = firstClonedBodyElement.toString();
        String secondClonedBodyElementText = secondClonedBodyElement.toString();
        String bodyText = body.toString();
        assertXMLEqual(newDocument(bodyText), newDocument(firstClonedBodyElementText));
        assertXMLEqual(newDocument(bodyText), newDocument(secondClonedBodyElementText));
        assertXMLEqual(newDocument(firstClonedBodyElementText),
                       newDocument(secondClonedBodyElementText));

        // lets check some links. They must not be equal
        assertNotSame(body.getParent(), firstClonedBodyElement.getParent());
        assertNotSame(body.getParent(), secondClonedBodyElement.getParent());
        assertNotSame(firstClonedBodyElement.getParent(), secondClonedBodyElement.getParent());

        soapEnvelope.close(false);
    }

    public void testElementCloningUsingOMElementMethod() throws Exception {
        SOAPEnvelope soapEnvelope =
                (SOAPEnvelope) OMTestUtils.getOMBuilder(
                        getTestResource(TestConstants.SOAP_SOAPMESSAGE))
                        .getDocumentElement();
        SOAPBody body = soapEnvelope.getBody();

        OMElement firstClonedBodyElement = body.cloneOMElement();
        OMElement secondClonedBodyElement = body.cloneOMElement();

        // first check whether both have the same information
        assertXMLEqual(newDocument(body.toString()),
                       newDocument(firstClonedBodyElement.toString()));
        assertXMLEqual(newDocument(body.toString()),
                       newDocument(secondClonedBodyElement.toString()));
        assertXMLEqual(newDocument(firstClonedBodyElement.toString()),
                       newDocument(secondClonedBodyElement.toString()));

        // lets check some links. They must not be equal
        assertNotSame(body.getParent(), firstClonedBodyElement.getParent());
        assertNotSame(body.getParent(), secondClonedBodyElement.getParent());
        assertNotSame(firstClonedBodyElement.getParent(), secondClonedBodyElement.getParent());

        soapEnvelope.close(false);
    }

    public Document newDocument(String xml)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(new ByteArrayInputStream(xml.getBytes()));
    }
}
