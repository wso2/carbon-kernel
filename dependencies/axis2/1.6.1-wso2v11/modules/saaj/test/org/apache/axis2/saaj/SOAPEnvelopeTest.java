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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.Name;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.soap.Text;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.Iterator;

@RunWith(SAAJTestRunner.class)
public class SOAPEnvelopeTest extends Assert {

    private static final String XML_STRING =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                    "                   xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                    "                   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                    " <soapenv:Header>\n" +
                    "  <shw:Hello xmlns:shw=\"http://www.jcommerce.net/soap/ns/SOAPHelloWorld\">\n" +
                    "    <shw:Myname>Tony</shw:Myname>\n" +
                    "  </shw:Hello>\n" +
                    " </soapenv:Header>\n" +
                    " <soapenv:Body>\n" +
                    "<shw:Address shw:t='test' xmlns:shw=\"http://www.jcommerce.net/soap/ns/SOAPHelloWorld\">\n" +
                    "<shw:City>GENT</shw:City>\n" +
                    "</shw:Address>\n" +
                    "</soapenv:Body>\n" +
                    "</soapenv:Envelope>";
    
    private static final String XML_INPUT_1 = "<root><a><!-- this is a test with a comment node --></a></root>";

    @Validated @Test
    public void testEnvelope() throws Exception {
        MessageFactory mf = MessageFactory.newInstance();
        SOAPMessage smsg =
                mf.createMessage(new MimeHeaders(),
                                 new ByteArrayInputStream(XML_STRING.getBytes()));
        SOAPPart sp = smsg.getSOAPPart();
        SOAPEnvelope se = sp.getEnvelope();
        assertTrue(se != null);

        // validate the body
        final SOAPBody body = sp.getEnvelope().getBody();
        validateBody(body.getChildElements());
    }

    @Validated @Test
    public void testDetachHeader() throws Exception {
        MessageFactory mf = MessageFactory.newInstance();
        SOAPMessage smsg =
                mf.createMessage(new MimeHeaders(),
                                 new ByteArrayInputStream(XML_STRING.getBytes()));
        SOAPPart sp = smsg.getSOAPPart();
        SOAPEnvelope se = sp.getEnvelope();
        assertTrue(se != null);
        SOAPHeader header = se.getHeader();
        assertNotNull(header);
        header.detachNode();
        assertNull(se.getHeader());
        assertNull(smsg.getSOAPHeader());
    }

    @Validated @Test
    public void testDetachBody() {
        try {
            MessageFactory mf = MessageFactory.newInstance();
            SOAPMessage smsg =
                    mf.createMessage(new MimeHeaders(),
                                     new ByteArrayInputStream(XML_STRING.getBytes()));
            SOAPPart sp = smsg.getSOAPPart();
            SOAPEnvelope se = sp.getEnvelope();

            try {
                se.addBody();
                fail("Expected Exception did not occur");
            } catch (SOAPException e) {
                assertTrue(true);
            }

            se.getBody().detachNode();
            assertNull(se.getBody());
            try {
                se.addBody();
            } catch (SOAPException e) {
                e.printStackTrace();
                fail("Unexpected Exception occurred.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected Exception : " + e);
        }
    }

    @Validated @Test
    public void testEnvelope2() throws Exception {
        MessageFactory mf = MessageFactory.newInstance();
        final ByteArrayInputStream baIS = new ByteArrayInputStream(XML_STRING.getBytes());
        final MimeHeaders mimeheaders = new MimeHeaders();
        mimeheaders.addHeader("Content-Type", "text/xml");
        SOAPMessage smsg =
                mf.createMessage(mimeheaders, baIS);

        SOAPEnvelope envelope = smsg.getSOAPPart().getEnvelope();
        SOAPBody body = envelope.getBody();
        assertTrue(body != null);
    }

    // TODO: This test fails due to some issues in OM. Needs to be added to the test suite
    //   that issue is fixed
    public void _testEnvelopeWithLeadingComment() throws Exception {
        String soapMessageWithLeadingComment =
                "<?xml version='1.0' encoding='UTF-8'?>" +
                        "<!-- Comment -->" +
                        "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
                        "<env:Body><echo><arg0>Hello</arg0></echo></env:Body>" +
                        "</env:Envelope>";

        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage message =
                factory.createMessage(new MimeHeaders(),
                                      new ByteArrayInputStream(
                                              soapMessageWithLeadingComment.getBytes()));
        SOAPPart part = message.getSOAPPart();
        SOAPEnvelope envelope = part.getEnvelope();
        message.writeTo(System.out);
        assertTrue(envelope != null);
        assertTrue(envelope.getBody() != null);
    }

    @Validated @Test
    public void testEnvelopeWithCommentInEnvelope() throws Exception {

        String soapMessageWithLeadingComment =
                "<?xml version='1.0' encoding='UTF-8'?>\n" +
                        "<soapenv:Envelope  xmlns='http://somewhere.com/html'\n" +
                        "                   xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/'\n" +
                        "                   xmlns:xsd='http://www.w3.org/2001/XMLSchema'\n" +
                        "                   xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>\n" +
                        "<!-- Comment -->" +
                        " <soapenv:Body>\n" +
                        "    <echo><arg0>Hello</arg0></echo>" +
//                "    <t:echo xmlns:t='http://test.org/Test'><t:arg0>Hello</t:arg0></t:echo>" +
                        " </soapenv:Body>\n" +
                        "</soapenv:Envelope>";

        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage message =
                factory.createMessage(new MimeHeaders(),
                                      new ByteArrayInputStream(
                                              soapMessageWithLeadingComment.getBytes()));
        SOAPPart part = message.getSOAPPart();
        SOAPEnvelope envelope = part.getEnvelope();
        assertTrue(envelope != null);
        assertTrue(envelope.getBody() != null);
    }

    @Validated @Test
    public void testEnvelopeWithCommentInBody() throws Exception {

        String soapMessageWithLeadingComment =
                "<?xml version='1.0' encoding='UTF-8'?>\n" +
                        "<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/'\n" +
                        "                   xmlns:xsd='http://www.w3.org/2001/XMLSchema'\n" +
                        "                   xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>\n" +
                        " <soapenv:Body>\n" +
                        "<!-- Comment -->" +
//                "    <echo><arg0>Hello</arg0></echo>" +
                        "    <t:echo xmlns:t='http://test.org/Test'><t:arg0>Hello</t:arg0></t:echo>" +
                        " </soapenv:Body>\n" +
                        "</soapenv:Envelope>";

        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage message =
                factory.createMessage(new MimeHeaders(),
                                      new ByteArrayInputStream(
                                              soapMessageWithLeadingComment.getBytes()));
        SOAPPart part = message.getSOAPPart();
        SOAPEnvelope envelope = part.getEnvelope();
        assertTrue(envelope != null);
        assertTrue(envelope.getBody() != null);
    }

    @Validated @Test
    public void testEnvelopeWithComments() throws Exception {

        String soapMessageWithLeadingComment =
                "<?xml version='1.0' encoding='UTF-8'?>\n" +
                        "<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/'\n" +
                        "                   xmlns:xsd='http://www.w3.org/2001/XMLSchema'\n" +
                        "                   xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>\n" +
                        " <soapenv:Header>\n" +
                        "<!-- Comment -->" +
                        "  <shw:Hello xmlns:shw=\"http://www.jcommerce.net/soap/ns/SOAPHelloWorld\">\n" +
                        "<!-- Comment -->" +
                        "    <shw:Myname><!-- Comment -->Tony</shw:Myname>\n" +
                        "  </shw:Hello>\n" +
                        " </soapenv:Header>\n" +
                        " <soapenv:Body>\n" +
                        "<!-- Comment -->" +
                        "    <t:echo xmlns:t='http://test.org/Test'><t:arg0>Hello</t:arg0></t:echo>" +
                        " </soapenv:Body>\n" +
                        "</soapenv:Envelope>";

        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage message =
                factory.createMessage(new MimeHeaders(),
                                      new ByteArrayInputStream(
                                              soapMessageWithLeadingComment.getBytes()));
        SOAPPart part = message.getSOAPPart();
        SOAPEnvelope envelope = part.getEnvelope();
        assertTrue(envelope != null);
        assertTrue(envelope.getBody() != null);
    }

    public void _testFaults() throws Exception {
        SOAPEnvelope envelope = getSOAPEnvelope();
        SOAPBody body = envelope.getBody();

        assertFalse(body.hasFault());
        SOAPFault soapFault = body.addFault();
        soapFault.setFaultString("myFault");
        soapFault.setFaultCode("CODE");

        assertTrue(body.hasFault());
        assertNotNull(body.getFault());
        assertSame(soapFault, body.getFault());

        assertEquals("myFault", soapFault.getFaultString());
        assertEquals("CODE", soapFault.getFaultCode());
    }

    public void _testFaults2() throws Exception {
        SOAPEnvelope envelope = getSOAPEnvelope();
        SOAPBody body = envelope.getBody();
        SOAPFault fault = body.addFault();

        assertTrue(body.getFault() != null);

        Detail d1 = fault.addDetail();
        Name name = envelope.createName("GetLastTradePrice", "WOMBAT",
                                        "http://www.wombat.org/trader");
        d1.addDetailEntry(name);

        Detail d2 = fault.getDetail();
        assertTrue(d2 != null);
        Iterator i = d2.getDetailEntries();
        assertTrue(getIteratorCount(i) == 1);
        i = d2.getDetailEntries();
        while (i.hasNext()) {
            DetailEntry de = (DetailEntry)i.next();
            assertEquals(de.getElementName(), name);
        }
    }

    @Validated @Test
    public void testHeaderElements() throws Exception {
        SOAPEnvelope envelope = getSOAPEnvelope();
        SOAPHeader header = envelope.getHeader();

        SOAPHeaderElement headerEle = header.addHeaderElement(envelope.createName("foo1",
                                                                                  "f1",
                                                                                  "foo1-URI"));
        headerEle.setActor("actor-URI");
        headerEle.setMustUnderstand(true);

        Iterator iterator = header.extractHeaderElements("actor-URI");
        int cnt = 0;
        while (iterator.hasNext()) {
            cnt++;
            SOAPHeaderElement resultHeaderEle = (SOAPHeaderElement)iterator.next();

            assertEquals(headerEle.getActor(), resultHeaderEle.getActor());
            assertEquals(resultHeaderEle.getMustUnderstand(), headerEle.getMustUnderstand());
        }
        assertTrue(cnt == 1);
        iterator = header.extractHeaderElements("actor-URI");
        assertTrue(!iterator.hasNext());
    }

    @Validated @Test
    public void testText() throws Exception {
        SOAPEnvelope envelope = getSOAPEnvelope();
        SOAPBody body = envelope.getBody();
        Iterator iStart = body.getChildElements();
        int countStart = getIteratorCount(iStart);

        final String bodyText = "This is the body text";

        SOAPElement se = body.addChildElement("Child");
        assertTrue(se != null);
        SOAPElement soapElement = se.addTextNode(bodyText);
        assertEquals(bodyText, soapElement.getValue());

        Iterator i = body.getChildElements();
        int count = getIteratorCount(i);
        assertTrue(count == countStart + 1);
    }

    @Validated @Test
    public void testNonCommentText() throws Exception {
        SOAPEnvelope envelope = getSOAPEnvelope();
        SOAPBody body = envelope.getBody();
        SOAPElement se = body.addChildElement("Child");
        se.addTextNode("This is text");
        Iterator iterator = se.getChildElements();
        Object o = null;
        while (iterator.hasNext()) {
            o = iterator.next();
            if (o instanceof Text) {
                break;
            }
        }
        assertTrue(o instanceof Text);
        Text t = (Text)o;
        assertTrue(!t.isComment());
    }

    @Validated @Test
    public void testCommentText() throws Exception {
        SOAPEnvelope envelope = getSOAPEnvelope();
        SOAPBody body = envelope.getBody();
        SOAPElement se = body.addChildElement("Child");
        se.addTextNode("<!-- This is a comment -->");
        Iterator iterator = se.getChildElements();
        Node n = null;
        while (iterator.hasNext()) {
            n = (Node)iterator.next();
            if (n instanceof Text) {
                break;
            }
        }
        assertTrue(n instanceof Text);
        Text t = (Text)n;
        assertTrue(t.isComment());
    }

    @Validated @Test
    public void testAttributes() throws Exception {
        SOAPEnvelope envelope = getSOAPEnvelope();
        SOAPBody body = envelope.getBody();

        Name name1 = envelope.createName("MyAttr1");
        String value1 = "MyValue1";

        Name name2 = envelope.createName("MyAttr2");
        String value2 = "MyValue2";

        Name name3 = envelope.createName("MyAttr3");
        String value3 = "MyValue3";

        body.addAttribute(name1, value1);
        body.addAttribute(name2, value2);
        body.addAttribute(name3, value3);

        Iterator iterator = body.getAllAttributes();
        assertTrue(getIteratorCount(iterator) == 3);
        iterator = body.getAllAttributes();

        boolean foundName1 = false;
        boolean foundName2 = false;
        boolean foundName3 = false;
        while (iterator.hasNext()) {
            Name name = (Name)iterator.next();
            if (name.equals(name1)) {
                foundName1 = true;
                assertEquals(value1, body.getAttributeValue(name));
            } else if (name.equals(name2)) {
                foundName2 = true;
                assertEquals(value2, body.getAttributeValue(name));
            } else if (name.equals(name3)) {
                foundName3 = true;
                assertEquals(value3, body.getAttributeValue(name));
            }
        }
        assertTrue(foundName1 && foundName2 && foundName3);
    }

    @Validated @Test
    public void testAttributes2() throws Exception {
        SOAPEnvelope envelope = getSOAPEnvelope();
        SOAPBody body = envelope.getBody();

        Name name1 = envelope.createName("MyAttr1", "att", "http://test.com/Attr");
        String value1 = "MyValue1";

        Name name2 = envelope.createName("MyAttr2");
        String value2 = "MyValue2";

        Name name3 = envelope.createName("MyAttr3");
        String value3 = "MyValue3";

        body.addAttribute(name1, value1);
        body.addAttribute(name2, value2);
        body.addAttribute(name3, value3);

        Iterator iterator = body.getAllAttributes();
        assertTrue(getIteratorCount(iterator) == 3);
        iterator = body.getAllAttributes();

        boolean foundName1 = false;
        boolean foundName2 = false;
        boolean foundName3 = false;
        while (iterator.hasNext()) {
            Name name = (Name)iterator.next();
            if (name.equals(name1)) {
                foundName1 = true;
                assertEquals(value1, body.getAttributeValue(name));
            } else if (name.equals(name2)) {
                foundName2 = true;
                assertEquals(value2, body.getAttributeValue(name));
            } else if (name.equals(name3)) {
                foundName3 = true;
                assertEquals(value3, body.getAttributeValue(name));
            }
        }
        assertTrue(foundName1 && foundName2 && foundName3);
    }

    @Validated @Test
    public void testAttributes3() throws Exception {
        SOAPEnvelope envelope = getSOAPEnvelope();
        SOAPBody body = envelope.getBody();

        Name name1 = envelope.createName("MyAttr1", "att", "http://test.com/Attr");
        String value1 = "MyValue1";

        Name name2 = envelope.createName("MyAttr2", "att", "http://test.com/Attr");
        String value2 = "MyValue2";

        Name name3 = envelope.createName("MyAttr3", "att", "http://test.com/Attr");
        String value3 = "MyValue3";

        body.addAttribute(name1, value1);
        body.addAttribute(name2, value2);
        body.addAttribute(name3, value3);

        Iterator iterator = body.getAllAttributes();
        assertTrue(getIteratorCount(iterator) == 3);
        iterator = body.getAllAttributes();

        boolean foundName1 = false;
        boolean foundName2 = false;
        boolean foundName3 = false;
        while (iterator.hasNext()) {
            Name name = (Name)iterator.next();
            if (name.equals(name1)) {
                foundName1 = true;
                assertEquals(value1, body.getAttributeValue(name));
            } else if (name.equals(name2)) {
                foundName2 = true;
                assertEquals(value2, body.getAttributeValue(name));
            } else if (name.equals(name3)) {
                foundName3 = true;
                assertEquals(value3, body.getAttributeValue(name));
            }
        }
        assertTrue(foundName1 && foundName2 && foundName3);
    }

    @Validated @Test
    public void testAddHeader() {
        try {
            SOAPEnvelope envelope = getSOAPEnvelope();
            try {
                envelope.addHeader();
                fail("Did not get expected SOAPException");
            } catch (SOAPException e) {
                assertTrue("Got expected SOAPException", true);
            }
            envelope.getHeader().detachNode();
            assertNull(envelope.getHeader());
            SOAPHeader myhdr;

            try {
                myhdr = envelope.addHeader();
                assertNotNull("SOAPHeader return value is null", myhdr);
            } catch (SOAPException e) {
                fail("Unexpected SOAPException : " + e);
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected Exception : " + e);
        }
    }

    private SOAPEnvelope getSOAPEnvelope() throws Exception {
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage message = factory.createMessage();
        return message.getSOAPPart().getEnvelope();
    }

    private int getIteratorCount(java.util.Iterator i) {
        int count = 0;
        while (i.hasNext()) {
            count++;
            i.next();
        }
        return count;
    }

    private void validateBody(Iterator iter) {
        while (iter.hasNext()) {
            final Object obj = iter.next();
            if (obj instanceof Text) {
                final String data = ((Text)obj).getData();
                assertTrue("\n".equals(data) || "GENT".equals(data));
            } else {
                final SOAPElement soapElement = (SOAPElement)obj;
                final Iterator attIter = soapElement.getAllAttributes();
                while (attIter.hasNext()) {
                    final Object o = attIter.next();
                    assertEquals("test", soapElement.getAttributeValue((Name)o));
                }

                final Iterator childElementIter = soapElement.getChildElements();
                if (childElementIter == null) {
                    return;
                }
                validateBody(childElementIter);
            }
        }
    }

    @Validated @Test
    public void testSetEncodingStyle() throws Exception {
        SOAPEnvelope envelope = getSOAPEnvelope();
        envelope.setEncodingStyle("http://example.com/MyEncodings");
        assertNotNull(envelope.getEncodingStyle());
        assertEquals("http://example.com/MyEncodings",envelope.getEncodingStyle());
    }

    @Validated @Test
    public void testElementAfterBody() throws Exception {
        MessageFactory factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage message = factory.createMessage();
        SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();

        try {
            //SOAP1.2 does not allow trailing blocks after the Body
            //Call SOAPEnvelope.addChildElement() and (expect SOAPException)
            Name elementAfterBody = envelope.createName("AfterBody", "e", "some-uri");
            envelope.addChildElement(elementAfterBody);
            fail("Did not throw expected SOAPException");
        } catch (SOAPException e) {
            //Did throw expected SOAPException"
        } catch (Exception e) {
            fail("Unexpected Exception: " + e.getMessage());
        }
    }
    
    @Validated @Test
    public void testTransform() throws Exception {
        MessageFactory fact = MessageFactory.newInstance();
        SOAPMessage message = fact.createMessage();
        SOAPBody body = message.getSOAPBody();
        Source source = new DOMSource(createDocument());
        Result result = new DOMResult(body);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(source, result);
        
        assertEquals(1, body.getChildNodes().getLength());
        Iterator iter = body.getChildElements();
        assertTrue(iter.hasNext()); 
        Object obj = iter.next();
        assertTrue(obj instanceof SOAPBodyElement);  
        SOAPElement soapElement = (SOAPElement)obj;
        assertEquals("http://example.com", soapElement.getNamespaceURI());
        assertEquals("GetLastTradePrice", soapElement.getLocalName());
        
        iter = soapElement.getChildElements();
        assertTrue(iter.hasNext()); 
        obj = iter.next();
        assertTrue(obj instanceof SOAPElement);  
        soapElement = (SOAPElement)obj;
        assertNull(soapElement.getNamespaceURI());
        assertEquals("symbol", soapElement.getLocalName());
        assertFalse(iter.hasNext());
        
        iter = soapElement.getChildElements();
        assertTrue(iter.hasNext()); 
        obj = iter.next();
        assertTrue(obj instanceof Text);  
        Text text = (Text)obj;
        assertEquals("DEF", text.getData());
        assertFalse(iter.hasNext());
    }
    
    private Element createDocument() throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        Element getLastTradePrice = document.createElementNS("http://example.com", "m:GetLastTradePrice");        
        Element symbol = document.createElement("symbol");
        getLastTradePrice.appendChild(symbol);
        org.w3c.dom.Text def = document.createTextNode("DEF");
        symbol.appendChild(def);
        document.appendChild(getLastTradePrice);
        return getLastTradePrice;
    }
    
    @Validated @Test
    public void testTransformWithComments() throws Exception {
        MessageFactory fact = MessageFactory.newInstance();
        SOAPMessage message = fact.createMessage();
        SOAPBody body = message.getSOAPBody();
        Source source = new SAXSource(new InputSource(new StringReader(XML_INPUT_1)));
        DOMResult result = new DOMResult(body);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(source, result);
        
        // test DOM
        org.w3c.dom.Node rootNode = result.getNode();
        org.w3c.dom.Node node = rootNode.getFirstChild();
        assertTrue(node instanceof Element);
        assertEquals("root", node.getNodeName());
        
        node = node.getFirstChild();
        assertTrue(node instanceof Element);
        assertEquals("a", node.getNodeName());
        
        node = node.getFirstChild();
        assertTrue(node instanceof Comment);
        assertEquals("this is a test with a comment node", node.getNodeValue().trim());
        
        // test SAAJ
        assertEquals(1, body.getChildNodes().getLength());
        Iterator iter = body.getChildElements();
        assertTrue(iter.hasNext()); 
        Object obj = iter.next();
        assertTrue(obj instanceof SOAPBodyElement);  
        SOAPElement soapElement = (SOAPElement)obj;
        assertEquals("root", soapElement.getLocalName());
        
        iter = soapElement.getChildElements();
        assertTrue(iter.hasNext()); 
        obj = iter.next();
        assertTrue(obj instanceof SOAPElement);  
        soapElement = (SOAPElement)obj;
        assertEquals("a", soapElement.getLocalName());
        assertFalse(iter.hasNext());
        
        iter = soapElement.getChildElements();
        assertTrue(iter.hasNext()); 
        obj = iter.next();
        assertTrue(obj instanceof Text);  
        Text text = (Text)obj;
        assertTrue(text.isComment());
        assertEquals("this is a test with a comment node", text.getData().trim());
        assertFalse(iter.hasNext());        
    }
}