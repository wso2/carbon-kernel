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

package org.apache.axis2.saaj;

import junit.framework.Assert;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11Factory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.soap.Text;
import javax.xml.transform.dom.DOMSource;
import java.io.File;
import java.util.Iterator;

/**
 * 
 */
@RunWith(SAAJTestRunner.class)
public class SOAPPartTest extends Assert {
    @Validated @Test
    public void testAddSource() throws Exception {
        /*
        FileReader testFile = new FileReader(new File(System.getProperty("basedir",".")+"/test-resources" + File.separator + "soap-part.xml"));
        StAXOMBuilder stAXOMBuilder =
                OMXMLBuilderFactory.createStAXOMBuilder(
                        OMAbstractFactory.getSOAP11Factory(),
                        XMLInputFactory.newInstance().createXMLStreamReader(
                                testFile));
        */

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder builder = dbf.newDocumentBuilder();
        Document document = builder.parse(TestUtils.getTestFileURI("soap-part.xml"));
        DOMSource domSource = new DOMSource(document);

        SOAPMessage message = MessageFactory.newInstance().createMessage();

        // Get the SOAP part and set its content to domSource
        SOAPPart soapPart = message.getSOAPPart();
        soapPart.setContent(domSource);
        message.saveChanges();

        SOAPHeader header = message.getSOAPHeader();
        if (header != null) {
            Iterator iter1 = header.getChildElements();
            getContents(iter1, "");
        }

        SOAPBody body = message.getSOAPBody();
        Iterator iter2 = body.getChildElements();
        getContents(iter2, "");
    }

    public void getContents(Iterator iterator, String indent) {
        while (iterator.hasNext()) {
            Node node = (Node)iterator.next();
            SOAPElement element = null;
            Text text = null;

            if (node instanceof SOAPElement) {
                element = (SOAPElement)node;

                Name name = element.getElementName();

                Iterator attrs = element.getAllAttributes();

                while (attrs.hasNext()) {
                    Name attrName = (Name)attrs.next();
                    assertNotNull(attrName);
                }

                Iterator iter2 = element.getChildElements();
                getContents(iter2, indent + " ");
            } else {
                text = (Text)node;
                String content = text.getValue();
                assertNotNull(content);
            }
        }
    }

    @Validated @Test
    public void testAddSource2() throws Exception {
        SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
        SOAPEnvelope soapEnv = soapMessage.getSOAPPart().getEnvelope();
        SOAPHeader header = soapEnv.getHeader();
        SOAPBody body = soapEnv.getBody();

        assertTrue(header.addChildElement("ebxmlms1", "ch2",
                                          "http://test.apache.org") instanceof SOAPHeaderElement);
        assertTrue(header.addHeaderElement(
                soapEnv.createName("ebxmlms2", "ch3", "http://test2.apache.org")) != null);
        assertTrue(header.addHeaderElement(
                new PrefixedQName("http://test3.apache.org", "ebxmlms3", "ch5")) != null);

        body.addChildElement("bodyEle1", "ele1", "http://ws.apache.org");
        soapMessage.saveChanges();

        SOAPMessage soapMessage2 = MessageFactory.newInstance().createMessage();
        SOAPPart soapPart = soapMessage2.getSOAPPart();
        soapPart.setContent(soapMessage.getSOAPPart().getContent());
        soapMessage2.saveChanges();
        assertNotNull(soapMessage2);
    }

    @Validated @Test
    public void testAddSource3() throws Exception {
        SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
        SOAPEnvelope soapEnv = soapMessage.getSOAPPart().getEnvelope();
        SOAPHeader header = soapEnv.getHeader();
        SOAPBody body = soapEnv.getBody();

        assertTrue(header.addChildElement("ebxmlms1", "ch2",
                                          "http://test.apache.org") instanceof SOAPHeaderElement);
        assertTrue(header.addHeaderElement(
                soapEnv.createName("ebxmlms2", "ch3", "http://test2.apache.org")) != null);
        assertTrue(header.addHeaderElement(
                new PrefixedQName("http://test3.apache.org", "ebxmlms3", "ch5")) != null);

        body.addChildElement("bodyEle1", "ele1", "http://ws.apache.org");
        soapMessage.saveChanges();

        SOAPMessage soapMessage2 = MessageFactory.newInstance().createMessage();
        SOAPPart soapPart = soapMessage2.getSOAPPart();
        soapPart.setContent(soapMessage.getSOAPPart().getContent());
        soapMessage2.saveChanges();
        assertNotNull(soapMessage2);
    }


    public void _testInputEncoding() throws Exception {
        DOMSource domSource;
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = builder.parse(new File(System.getProperty("basedir", ".") +
                "/test-resources" + File.separator + "soap-part.xml"));
        domSource = new DOMSource(document);

        SOAPMessage message = MessageFactory.newInstance().createMessage();

        // Get the SOAP part and set its content to domSource
        SOAPPart soapPart = message.getSOAPPart();
        soapPart.setContent(domSource);
        message.saveChanges();

        SOAPPart sp = message.getSOAPPart();

//            String inputEncoding = sp.getInputEncoding();
//            assertNotNull(inputEncoding);
    }
    
    /**
     * Check parent processing of SOAPMessage
     */
    @Validated @Test
    public void test_parentAccess1() throws Exception {

        MessageFactory mf = MessageFactory.newInstance();
        SOAPMessage m = mf.createMessage();
        SOAPPart sp = m.getSOAPPart();
        Node node = sp.getParentNode();
        assertTrue(node == null);
        
        SOAPElement e = sp.getParentElement();
        assertTrue(node == null);
    }
    
    /**
     * Check parent processing of SOAPMessage
     */
    // TODO: check why this fails with Sun's SAAJ implementation
    @Test
    public void test_parentAccess2() throws Exception {

        MessageFactory mf = MessageFactory.newInstance();
        SOAPMessage m = mf.createMessage();
        SOAPPart sp = m.getSOAPPart();
        SOAPEnvelope se = sp.getEnvelope();
        Node node = se.getParentNode();
        assertTrue(node == sp);
        node = node.getParentNode();
        assertTrue(node == null);

        SOAPElement e = se.getParentElement();
        assertTrue(node == null);
    }
    
    /**
     * Check parent processing of SOAPMessage
     */
    @Validated @Test
    public void test_parentAccess3() throws Exception {

        SOAP11Factory axiomSF = new SOAP11Factory();
        org.apache.axiom.soap.SOAPEnvelope axiomSE = axiomSF.createSOAPEnvelope();
        org.apache.axiom.soap.SOAPMessage axiomSM = axiomSF.createSOAPMessage(axiomSE, null);
        
        SOAPEnvelopeImpl se = 
            new SOAPEnvelopeImpl((org.apache.axiom.soap.impl.dom.SOAPEnvelopeImpl)axiomSE);
        SOAPMessageImpl sm = new SOAPMessageImpl(se);
        SOAPPartImpl sp = new SOAPPartImpl(sm, se);
        
        Node node = se.getParentNode();
        assertTrue(node == sp);
        node = node.getParentNode();
        assertTrue(node == null);

        SOAPElement e = se.getParentElement();
        assertTrue(node == null);
    }
    
    // TODO: check why this fails with Sun's SAAJ implementation
    @Test
    public void testNodeTypes() throws Exception {
        MessageFactory fact = MessageFactory.newInstance();
        SOAPMessage message = fact.createMessage();
        SOAPPart soapPart = message.getSOAPPart();     

        assertTrue("first child", soapPart.getFirstChild() instanceof SOAPEnvelope);
        assertTrue("last child", soapPart.getLastChild() instanceof SOAPEnvelope);
                
        NodeList nodes = soapPart.getChildNodes();
        
        assertEquals(1, nodes.getLength());
        for (int i = 0; i < nodes.getLength(); i++) {
            assertTrue(nodes.item(i) instanceof SOAPEnvelope);
        }             
    }
    
    // TODO: check why this fails with Sun's SAAJ implementation
    @Test
    public void testRemoveChild1() throws Exception {
        MessageFactory fact = MessageFactory.newInstance();
        SOAPMessage message = fact.createMessage();
        SOAPPart soapPart = message.getSOAPPart();  
                                
        assertTrue("soap env before", soapPart.getFirstChild() instanceof SOAPEnvelope);
        
        soapPart.removeChild(soapPart.getFirstChild());
        
        assertTrue("soap env after", soapPart.getFirstChild() == null);
    }
    
    // TODO: check why this fails with Sun's SAAJ implementation
    @Test
    public void testRemoveChild2() throws Exception {
        MessageFactory fact = MessageFactory.newInstance();
        SOAPMessage message = fact.createMessage();
        SOAPPart soapPart = message.getSOAPPart();  
                                
        assertTrue("soap env before", soapPart.getFirstChild() instanceof SOAPEnvelope);
        
        soapPart.removeChild(soapPart.getEnvelope());
        
        assertTrue("soap env after", soapPart.getFirstChild() == null);
    }
}
