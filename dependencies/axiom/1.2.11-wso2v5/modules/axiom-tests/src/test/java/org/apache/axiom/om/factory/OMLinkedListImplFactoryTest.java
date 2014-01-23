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

package org.apache.axiom.om.factory;

import org.apache.axiom.om.AbstractTestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMTestUtils;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.TestConstants;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPConstants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;

import javax.xml.namespace.QName;

/** User: Eran Chinthaka (eran.chinthaka@gmail.com) Date: Feb 8, 2005 Time: 11:06:09 AM */
public class OMLinkedListImplFactoryTest extends AbstractTestCase {
    public OMLinkedListImplFactoryTest(String testName) {
        super(testName);
    }

    SOAPFactory omFactory;
    OMNamespace namespace;
    String nsUri = "http://www.apache.org/~chinthaka";
    String nsPrefix = "myhome";

    protected void setUp() throws Exception {
        super.setUp();
        omFactory = OMAbstractFactory.getSOAP11Factory();
        namespace = omFactory.createOMNamespace(nsUri, nsPrefix);
    }

    public void testCreateOMElementWithNoBuilder() {
        OMElement omElement = omFactory.createOMElement("chinthaka",
                                                        namespace);
        assertTrue(
                "Programatically created OMElement should have done = true ",
                omElement.isComplete());

    }

    public void testCreateOMElement() throws Exception {
        OMXMLParserWrapper omBuilder = OMTestUtils.getOMBuilder(
                getTestResource(TestConstants.WHITESPACE_MESSAGE));
        OMElement envelope = omBuilder.getDocumentElement();
        
        // The body is the second element
        OMNode node = envelope.getFirstElement();
        node = node.getNextOMSibling();
        while (node != null && !(node instanceof OMElement)) {
            node = node.getNextOMSibling();
        }
        OMElement body = (OMElement) node;
        

        OMElement child = omFactory.createOMElement("child",
                                                    namespace,
                                                    body,
                                                    omBuilder);
        assertFalse("OMElement with a builder should start with done = false",
                    child.isComplete());
        assertNotNull("This OMElement must have a builder", child.getBuilder());

        // Try the QName version
        QName qname = new QName(nsUri, "local", nsPrefix);
        OMElement element = omFactory.createOMElement(qname);

        assertNotNull(element);

        // Now make one with a parent (and thus inherit the NS)
        element = omFactory.createOMElement(qname, child);
        assertNotNull(element);
        assertEquals("Namespace wasn't found correctly",
                     element.getNamespace(),
                     namespace);
    }

    public void testCreateOMNamespace() throws Exception {
        assertTrue("OMNamespace uri not correct",
                   nsUri.equals(
                           namespace.getNamespaceURI()));   // here equalsIgnoreCase should not be used as case does matter
        assertTrue("OMNamespace prefix not correct",
                   nsPrefix.equals(
                           namespace.getPrefix()));  // here equalsIgnoreCase should not be used as case does matter
    }

    public void testCreateSOAPBody() throws Exception {
        OMXMLParserWrapper omBuilder = OMTestUtils.getOMBuilder(
                getTestResource(TestConstants.MINIMAL_MESSAGE));
        SOAPEnvelope soapEnvelope = (SOAPEnvelope) omBuilder.getDocumentElement();
        SOAPBody soapBodyOne = omFactory.createSOAPBody(soapEnvelope);
        assertTrue(
                "Programatically created SOAPBody should have done = true ",
                soapBodyOne.isComplete());
    }

    public void testCreateSOAPBodyWithBuilder() throws Exception {
        OMXMLParserWrapper omBuilder = OMTestUtils.getOMBuilder(
                getTestResource(TestConstants.MINIMAL_MESSAGE));
        SOAPEnvelope soapEnvelope = (SOAPEnvelope) omBuilder.getDocumentElement();

        SOAPBody soapBodyTwo = omFactory.createSOAPBody(soapEnvelope,
                                                        omBuilder);
        assertTrue(
                "SOAPBody with a builder should start with done = false ",
                !soapBodyTwo.isComplete());
        assertNotNull("This SOAPBody must have a builder ", soapBodyTwo.getBuilder());
    }

    public void testCreateSOAPEnvelope() throws Exception {
        omFactory.createOMNamespace(
                SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                SOAPConstants.SOAP_DEFAULT_NAMESPACE_PREFIX);
        SOAPEnvelope soapEnvelopeTwo = omFactory.createSOAPEnvelope();
        assertTrue(
                "Programatically created SOAPEnvelope should have done = true ",
                soapEnvelopeTwo.isComplete());
        SOAPEnvelope soapEnvelope = omFactory.createSOAPEnvelope(
                OMTestUtils.getOMBuilder(
                        getTestResource(TestConstants.MINIMAL_MESSAGE)));
        assertTrue(
                "SOAPEnvelope with a builder should start with done = false ",
                !soapEnvelope.isComplete());
        assertNotNull("This SOAPEnvelope must have a builder", soapEnvelope.getBuilder());
    }

    public void testCreateSOAPHeader() throws Exception {
        OMXMLParserWrapper omBuilder = OMTestUtils.getOMBuilder(
                getTestResource(TestConstants.MINIMAL_MESSAGE));
        SOAPEnvelope soapEnvelope = (SOAPEnvelope) omBuilder.getDocumentElement();
        SOAPHeader soapHeader = omFactory.createSOAPHeader(soapEnvelope);
        assertTrue(
                "Programatically created SOAPHeader should have done = true ",
                soapHeader.isComplete());
        soapHeader.detach();
        SOAPHeader soapHeaderTwo = omFactory.createSOAPHeader(soapEnvelope,
                                                              omBuilder);
        assertTrue(
                "SOAPHeader with a builder should start with done = false ",
                !soapHeaderTwo.isComplete());
        assertNotNull("This SOAPHeader must have a builder ", soapHeaderTwo.getBuilder());
    }

    public void testCreateSOAPHeaderBlock() throws Exception {
        OMXMLParserWrapper omBuilder = OMTestUtils.getOMBuilder(
                getTestResource(TestConstants.SOAP_SOAPMESSAGE));
        SOAPEnvelope soapEnvelope = (SOAPEnvelope) omBuilder.getDocumentElement();
        SOAPHeader soapHeader = soapEnvelope.getHeader();
        SOAPHeaderBlock soapHeaderBlock = omFactory.createSOAPHeaderBlock(
                "soapHeaderBlockOne", namespace, soapHeader);
        assertTrue(
                "Programatically created SOAPHeaderBlock should have done = true ",
                soapHeaderBlock.isComplete());
        SOAPHeaderBlock soapHeaderBlockTwo = omFactory.createSOAPHeaderBlock(
                "soapHeaderBlockOne", namespace, soapHeader, omBuilder);
        assertTrue(
                "SOAPHeaderBlock with a builder should start with done = false ",
                !soapHeaderBlockTwo.isComplete());
        assertNotNull("SOAPHeaderBlock must have a builder", soapHeaderBlockTwo.getBuilder());
    }

    public void testCreateSOAPFault() throws Exception {
        OMXMLParserWrapper omBuilder = OMTestUtils.getOMBuilder(
                getTestResource(TestConstants.SOAP_SOAPMESSAGE));
        SOAPEnvelope soapEnvelope = (SOAPEnvelope) omBuilder.getDocumentElement();
        SOAPBody soapBody = soapEnvelope.getBody();
        SOAPFault soapFault = omFactory.createSOAPFault(soapBody,
                                                        new Exception(" this is just a test "));
        assertTrue(
                "Programatically created SOAPFault should have done = true ",
                soapFault.isComplete());
        soapFault.detach();
        SOAPFault soapFaultTwo = omFactory.createSOAPFault(soapBody,
                                                           omBuilder);
        assertTrue(
                "SOAPFault with a builder should start with done = false ",
                !soapFaultTwo.isComplete());
        assertNotNull("This SOAPFault must have a builder", soapFaultTwo.getBuilder());
    }
}
