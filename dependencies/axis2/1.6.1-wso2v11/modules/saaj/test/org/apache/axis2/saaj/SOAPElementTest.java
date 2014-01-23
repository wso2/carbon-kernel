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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.soap.Text;
import java.util.Iterator;
import java.util.List;

@RunWith(SAAJTestRunner.class)
public class SOAPElementTest extends Assert {

    private SOAPElement soapEle;

    @Before
    public void setUp() throws Exception {
        soapEle =
                SOAPFactoryImpl.newInstance().createElement("Test",
                                                            "test",
                                                            "http://test.apache.org/");
    }

    @Validated @Test
    public void testAddTextNode() throws Exception {
        assertNotNull(soapEle);
        String value = "foo";
        soapEle.addTextNode(value);
        assertEquals(value, soapEle.getValue());
        Text text = assertContainsText(soapEle);
        assertEquals(value, text.getValue());
    }

    @Validated @Test
    public void testChildren() throws Exception {
        soapEle.addTextNode("foo");
        SOAPElement childEle1 =
                SOAPFactoryImpl.newInstance().createElement("Child1",
                                                            "ch",
                                                            "http://test.apache.org/");
        SOAPElement childEle2 =
                SOAPFactoryImpl.newInstance().createElement("Child2",
                                                            "ch",
                                                            "http://test.apache.org/");
        soapEle.addChildElement(childEle1);
        soapEle.addChildElement(childEle2);

        Object o = soapEle.getChildElements().next();
        Object o2 = soapEle.getChildElements().next();

        assertSame(o, o2); // both elements should be the same SAAJ Node
        assertEquals(((javax.xml.soap.Text)o).getValue(),
                     ((javax.xml.soap.Text)o2).getValue());

        int childrenCount = 0;
        for (Iterator iter = soapEle.getChildElements(); iter.hasNext();) {
            iter.next();
            childrenCount ++;
        }
        assertEquals(3, childrenCount);

        Object z1 = soapEle.getChildNodes().item(0);
        Object z2 = soapEle.getFirstChild();

        assertSame(o, z1);   // should be same SAAJ Node
        assertSame(z1, z2);  // should be same SAAJ Node

        assertEquals(((javax.xml.soap.Text)z1).getValue(),
                     ((javax.xml.soap.Text)z2).getValue());

        Node lastChildNode = (Node)soapEle.getLastChild();
        SOAPElement lastChildSOAPEle = (SOAPElement)lastChildNode;

        assertEquals("Child2", lastChildSOAPEle.getLocalName());
        assertEquals("http://test.apache.org/", lastChildSOAPEle.getNamespaceURI());
        assertEquals("ch", lastChildSOAPEle.getPrefix());
    }

    @Validated @Test
    public void testChildrenAndSiblings() throws Exception {
        soapEle.addTextNode("foo");
        soapEle.addChildElement("Child1", "ch", "http://test.apache.org/");
        soapEle.addChildElement("Child2", "ch", "http://test.apache.org/");

        Object o = soapEle.getChildElements().next();
        Object o2 = soapEle.getChildElements().next();
        assertSame(o, o2); // both elements should be the same SAAJ Node
        assertEquals(((javax.xml.soap.Text)o).getValue(),
                     ((javax.xml.soap.Text)o2).getValue());

        int childrenCount = 0;
        for (Iterator iter = soapEle.getChildElements(); iter.hasNext();) {
            iter.next();
            childrenCount ++;
        }
        assertEquals(3, childrenCount);

        Object z1 = soapEle.getChildNodes().item(0);
        Object z2 = soapEle.getFirstChild();
        assertSame(o, z1);   // should be same SAAJ Node
        assertSame(z1, z2);  // should be same SAAJ Node
        assertEquals(((javax.xml.soap.Text)z1).getValue(),
                     ((javax.xml.soap.Text)z2).getValue());

        SOAPElement lastChildSOAPEle = (SOAPElement)soapEle.getLastChild();

        assertEquals("Child2", lastChildSOAPEle.getLocalName());
        assertEquals("ch:Child2", lastChildSOAPEle.getNodeName());
        assertEquals("http://test.apache.org/", lastChildSOAPEle.getNamespaceURI());
        assertEquals("ch", lastChildSOAPEle.getPrefix());
        assertNotNull(lastChildSOAPEle.getParentNode());
        assertTrue(lastChildSOAPEle.getPreviousSibling() instanceof javax.xml.soap.SOAPElement);
        assertNull(lastChildSOAPEle.getNextSibling());

        javax.xml.soap.Node firstChild = (javax.xml.soap.Node)soapEle.getFirstChild();
        javax.xml.soap.Node nextSibling = (javax.xml.soap.Node)(firstChild.getNextSibling());
        assertNull(firstChild.getPreviousSibling());

        assertTrue(firstChild instanceof javax.xml.soap.Text);
        assertTrue(nextSibling instanceof javax.xml.soap.SOAPElement);
        assertTrue(nextSibling.getPreviousSibling() instanceof javax.xml.soap.Text);
        assertEquals("Child1", nextSibling.getLocalName());
        assertEquals("ch:Child1", nextSibling.getNodeName());
        assertEquals("http://test.apache.org/", nextSibling.getNamespaceURI());
        assertEquals("ch", nextSibling.getPrefix());

        javax.xml.soap.Node nextSibling2 = (javax.xml.soap.Node)nextSibling.getNextSibling();
        assertEquals("Child2", nextSibling2.getLocalName());
        assertEquals("ch:Child2", nextSibling2.getNodeName());
        assertEquals("http://test.apache.org/", lastChildSOAPEle.getNamespaceURI());
        assertEquals("ch", nextSibling2.getPrefix());
        assertNull(nextSibling2.getNextSibling());
    }

    @Validated @Test
    public void testCommentSibling() throws Exception {
        soapEle.addTextNode("foo");
        soapEle.addChildElement("Child1", "ch", "http://test.apache.org/");
        soapEle.addTextNode("<!-- This is a Comment-->");
        soapEle.addChildElement("Child2", "ch", "http://test.apache.org/");

        assertTrue(((Text)soapEle.getFirstChild().getNextSibling().getNextSibling()).isComment());
        assertTrue(((Text)soapEle.getLastChild().getPreviousSibling()).isComment());
    }

    @Validated @Test
    public void testCommentSibling2() throws Exception {
        soapEle.addTextNode("foo");
        soapEle.addTextNode("<!-- This is a Comment-->");
        soapEle.addTextNode("bar");
        soapEle.addChildElement("Child1", "ch", "http://test.apache.org/");
        soapEle.addChildElement("Child2", "ch", "http://test.apache.org/");

        assertTrue(((Text)soapEle.getFirstChild().getNextSibling()).isComment());
        assertFalse(((Text)soapEle.getLastChild().getPreviousSibling()
                .getPreviousSibling()).isComment());
        assertFalse(((Text)soapEle.getLastChild().getPreviousSibling()
                .getPreviousSibling()).isComment());
    }

    @Validated @Test
    public void testAddChildElement() throws Exception {
        String s = "MyName1";
        String p = "MyPrefix1";
        String u = "myURI";
        SOAPBody body = MessageFactory.newInstance().createMessage().getSOAPBody();
        SOAPElement myse = body.addNamespaceDeclaration(p, u);
        SOAPElement se = body.addChildElement(s, p);
        if (se == null) {
            fail("SOAPElement was null");
        } else {
            Iterator i = body.getChildElements();
            int count = getIteratorCount(i);
            i = body.getChildElements();
            if (count != 1) {
                fail("Wrong iterator count returned of " + count + ", expected 1");
            } else {
                SOAPElement se2 = (SOAPElement)i.next();
                if (!se.equals(se2)) {
                    fail("Elements not equal");
                }
            }
            String name = se.getElementName().getLocalName();
            Name n = se.getElementName();
            String prefix = se.getElementName().getPrefix();
            if (!name.equals(s) || !prefix.equals(p)) {
                fail("addChildElement() did not return correct local name and prefix");
            }
        }
    }

    @Validated @Test
    public void testAddChildElement2() throws Exception {
        boolean pass = true;
        SOAPMessage msg =
                MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL).createMessage();
        SOAPEnvelope soapEnvelope = msg.getSOAPPart().getEnvelope();
        SOAPBody body = msg.getSOAPBody();

        Name name = soapEnvelope.createName("MyChild1");
        //Add child element Name object with localName=MyChild1
        SOAPElement se = body.addChildElement(name);
        if (se == null) {
            fail("addChildElement() did not return SOAPElement");
            //pass = false;
        } else {
            //Find the child element just added
            Iterator childs = body.getChildElements(name);
            int count = 0;
            while (childs.hasNext()) {
                Object obj = (Object)childs.next();
                count++;
            }

            childs = body.getChildElements(name);
            assertTrue(count == 1);

            SOAPElement se2 = (SOAPElement)childs.next();
            assertEquals(se, se2);
            //se = se2 (expected)

            //Retrieve the SOAPElement Name
            Name n = se.getElementName();
            //System.out.println("localName="+n.getLocalName()+" prefix="
            //			+n.getPrefix()+" URI="+n.getURI()+" qualifiedName="
            //			+n.getQualifiedName());
            assertEquals(n, name);
            //if (!n.equals(name)) {
            //System.out.println("Name objects are not equal (unexpected)");
            //System.out.println("addChildElement() did not return " +
            //"correct Name object expected localName=" +
            //name.getLocalName() + ", got localName="
            //+ n.getLocalName());
            //}

            //Name objects are equal (expected)
        }
    }

    @Validated @Test
    public void testAddTextNode2() throws Exception {
        SOAPMessage msg = MessageFactory.newInstance().createMessage();
        SOAPEnvelope envelope = msg.getSOAPPart().getEnvelope();
        SOAPBody body = envelope.getBody();
        Iterator iStart = envelope.getChildElements();
        int countStart = getIteratorCount(iStart);
        SOAPElement se = envelope.addTextNode("<txt>This is text</txt>");
        if (se == null) {
            fail("addTextNode() did not return SOAPElement");
        } else if (!envelope.getValue().equals("<txt>This is text</txt>")) {
            String s = body.getValue();
            fail("addTextNode() did not return expected text, Returned " + s +
                    ", Expected <txt>This is text</txt>");
        }
        Iterator i = envelope.getChildElements();
        int count = getIteratorCount(i);
        i = envelope.getChildElements();
        if (count != ++countStart) {
            fail("Wrong iterator count returned of " +
                    count + ", expected " + countStart);
        } else {
            Object obj = null;
            while (i.hasNext()) {
                obj = i.next();
                if (obj instanceof Text) {
                    break;
                }
            }
            if (!(obj instanceof Text)) {
                fail("obj is not instanceof Text");
            }
        }
    }

    @Validated @Test
    public void testRemoveAttribute() throws Exception {
        SOAPMessage msg = MessageFactory.newInstance().createMessage();
        SOAPEnvelope envelope = msg.getSOAPPart().getEnvelope();
        SOAPBody body = envelope.getBody();
        Name name = envelope.createName("MyAttr1");
        String value = "MyValue1";
        body.addAttribute(name, value);
        boolean b = body.removeAttribute(name);
        assertTrue("removeAttribute() did not return true", b);
        b = body.removeAttribute(name);
        assertFalse("removeAttribute() did not return false", b);
        assertNull(body.getAttributeValue(name));
    }

    @Validated @Test
    public void testRemoveAttribute2() throws Exception {
        SOAPMessage msg = MessageFactory.newInstance().createMessage();
        SOAPEnvelope envelope = msg.getSOAPPart().getEnvelope();
        SOAPBody body = envelope.getBody();

        QName name = new QName("MyAttr1");
        String value = "MyValue1";
        body.addAttribute(name, value);
        boolean b = body.removeAttribute(name);
        assertTrue(b);

        b = body.removeAttribute(name);
        if (b) {
            //removeAttribute() did not return false
            fail();
        }
        //getAttributeValue should return null
        assertNull(body.getAttributeValue(name));
    }

    @Validated @Test
    public void testRemoveAttributeName() throws Exception {
        SOAPMessage msg = MessageFactory.newInstance().createMessage();
        SOAPEnvelope envelope = msg.getSOAPPart().getEnvelope();
        SOAPBody body = envelope.getBody();

        Name name = envelope.createName("MyAttr1");
        String value = "MyValue1";
        body.addAttribute(name, value);
        boolean b = body.removeAttribute(name);
        assertTrue(b);

        b = body.removeAttribute(name);
        assertTrue(!b);

        String s = body.getAttributeValue(name);
        assertNull(s);
    }


    public void _testRemoveAttributeQName() throws Exception {
        SOAPMessage msg =
                MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL).createMessage();
        SOAPEnvelope envelope = msg.getSOAPPart().getEnvelope();
        SOAPBody body = envelope.getBody();

        QName name = new QName("MyAttr1");
        String value = "MyValue1";
        body.addAttribute(name, value);
        boolean b = body.removeAttribute(name);
        assertTrue(b);
        b = body.removeAttribute(name);
        assertTrue(!b);

        assertNull(body.getAttributeValue(name));
    }

    @Validated @Test
    public void testRemoveNamespaceDeclaration() throws Exception {
        String prefix = "myPrefix";
        String uri = "myURI";
        SOAPMessage msg = MessageFactory.newInstance().createMessage();
        SOAPEnvelope envelope = msg.getSOAPPart().getEnvelope();
        SOAPBody body = envelope.getBody();
        body.addNamespaceDeclaration(prefix, uri);
        boolean b = body.removeNamespaceDeclaration(prefix);
        assertTrue("removeNamespaceDeclaration() did not return true", b);
        b = body.removeNamespaceDeclaration(prefix);
        assertFalse("removeNamespaceDeclaration() did not return false", b);
        assertNull(body.getNamespaceURI(prefix));
    }

    @Validated @Test
    public void testSetEncodingStyle() throws Exception {
        SOAPMessage msg = MessageFactory.newInstance().createMessage();
        SOAPEnvelope envelope = msg.getSOAPPart().getEnvelope();
        SOAPBody body = envelope.getBody();
        body.setEncodingStyle(SOAPConstants.URI_NS_SOAP_ENCODING);
        try {
            body.setEncodingStyle("BOGUS");
            fail("Expected Exception did not occur");
        } catch (IllegalArgumentException e) {
            assertTrue("Expected Exception occurred", true);
        }
    }

    private int getIteratorCount(Iterator iter) {
        int count = 0;
        while (iter.hasNext()) {
            iter.next();
            count ++;
        }
        return count;
    }

    private Text assertContainsText(SOAPElement soapElem) {
        assertTrue(soapElem.hasChildNodes());
        List childElems = toList(soapElem.getChildElements());
        assertTrue(childElems.size() == 1);
        Node node = (Node)childElems.get(0);
        assertTrue(node instanceof Text);
        return (Text)node;
    }

    private List toList(java.util.Iterator iter) {
        List list = new java.util.ArrayList();
        while (iter.hasNext()) {
            list.add(iter.next());
        }
        return list;
    }


    /*
    * test for addChildElement(QName qname)
    */
    @Validated @Test
    public void testAddChildElement3() throws Exception {
        QName qname = new QName("http://sample.apache.org/trader", "GetStockQuote", "w");
        soapEle.addChildElement(qname);
        assertNotNull(soapEle);
    }


    @Validated @Test
    public void testGetAttributeValue() throws Exception {
        assertNotNull(soapEle);
        String value = "234.50";
        QName qname = new QName("http://sample.apache.org/trader", "GetStockQuote", "w");
        soapEle.addAttribute(qname, value);
        String valueReturned = soapEle.getAttributeValue(qname);
        assertEquals(value, valueReturned);
    }


    public void _testGetChildElements() throws Exception {
        SOAPElement childEle1 =
                SOAPFactoryImpl.newInstance().createElement("Child1",
                                                            "ch",
                                                            "http://test.apache.org/");
        SOAPElement childEle2 =
                SOAPFactoryImpl.newInstance().createElement("Child2",
                                                            "ch",
                                                            "http://test.apache.org/");
        childEle1.addChildElement(childEle2);
        soapEle.addChildElement(childEle1);

        QName qname = new QName("http://test.apache.org/", "Child1", "ch");
        Iterator childElements = soapEle.getChildElements(qname);


        int childCount = 0;
        while (childElements.hasNext()) {
            Node node = (Node)childElements.next();
            childCount++;
        }
        assertEquals(childCount, 2);
    }

    @Validated @Test
    public void testGetChildElements2() throws Exception {
        MessageFactory fact = MessageFactory.newInstance();
        SOAPMessage message = fact.createMessage();
        SOAPPart soapPart = message.getSOAPPart();
        SOAPEnvelope soapEnvelope = soapPart.getEnvelope();
        SOAPBody soapBody = soapEnvelope.getBody();

        Name name = soapEnvelope.createName("MyChild1");
        SOAPElement se = soapBody.addChildElement(name);
        Iterator childElementsCount = soapBody.getChildElements();
        Iterator childElements = soapBody.getChildElements();

        int childCount = 0;
        while (childElementsCount.hasNext()) {
            Node node = (Node)childElementsCount.next();
            childCount++;
        }
        assertEquals(childCount, 1);
        SOAPElement se2 = (SOAPElement)childElements.next();
        if (!se.equals(se2)) {
            fail();
        } else {
            System.out.println("SOAPElement se = se2 (expected)");
        }

        Name n = se.getElementName();
        assertEquals(n, name);
    }

    @Validated @Test
    public void testGetChildElements3() throws Exception {
        MessageFactory fact = MessageFactory.newInstance();
        SOAPMessage message = fact.createMessage();
        SOAPPart soapPart = message.getSOAPPart();
        SOAPEnvelope soapEnvelope = soapPart.getEnvelope();
        SOAPBody soapBody = soapEnvelope.getBody();

        //Name name = soapEnvelope.createName("MyChild1");
        QName name = new QName("MyChild1");
        SOAPElement se = soapBody.addChildElement(name);
        Iterator childElementsCount = soapBody.getChildElements();
        Iterator childElements = soapBody.getChildElements();

        int childCount = 0;
        while (childElementsCount.hasNext()) {
            Node node = (Node)childElementsCount.next();
            childCount++;
        }
        assertEquals(childCount, 1);
        SOAPElement se2 = (SOAPElement)childElements.next();
        assertEquals(se, se2);

        QName n = se.getElementQName();
        assertEquals(n, name);
    }

    @Validated @Test
    public void testRemoveAttribute3() throws Exception {
        QName qname = new QName("http://child1.apache.org/", "Child1", "ch");
        String value = "MyValue1";
        soapEle.addAttribute(qname, value);
        boolean b = soapEle.removeAttribute(qname);
        assertTrue("removeAttribute() did not return true", b);
        b = soapEle.removeAttribute(qname);
        assertFalse("removeAttribute() did not return false", b);
        assertNull(soapEle.getAttributeValue(qname));
    }

    public void _testSetElementQName() throws Exception {
        QName qname = new QName("http://child1.apache.org/", "newName", "ch");
        soapEle.setElementQName(qname);
        assertNull(soapEle.getElementName().getLocalName(), "newName");
    }

    public void _testCreateQName() throws Exception {
        String prefix = "";
        //SOAPMessage message = MessageFactory.newInstance().createMessage();
        SOAPMessage message =
                MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL).createMessage();
        SOAPPart soapPart = message.getSOAPPart();
        SOAPEnvelope envelope = soapPart.getEnvelope();
        SOAPBody body = envelope.getBody();

        QName qname = envelope.createQName("qname", prefix);
        String tprefix = qname.getPrefix();
        String turi = qname.getNamespaceURI();
        String tname = qname.getLocalPart();
        if (!tprefix.equals(prefix) || !turi.equals(envelope.getElementName().getURI())) {
            fail("createQName() did not create correct qname\n" +
                    "expected: <uri=" + envelope.getElementName().getURI() +
                    ", prefix=" + prefix + ", localpart=qname>\n" +
                    "got:      <uri=" + turi +
                    ", prefix=" + tprefix + ", localpart=" + tname + ">");
        }
        qname = body.createQName("qname", body.getElementName().getPrefix());
        tprefix = qname.getPrefix();
        turi = qname.getNamespaceURI();
        tname = qname.getLocalPart();
        if (!tprefix.equals(body.getElementName().getPrefix()) ||
                !turi.equals(body.getElementName().getURI())) {
            fail("createQName() did not create correct qname\n" +
                    "expected: <uri=" + body.getElementName().getURI() +
                    ", prefix=" + body.getElementName().getPrefix() + ", localpart=qname>\n" +
                    "got:      <uri=" + turi +
                    ", prefix=" + tprefix + ", localpart=" + tname + ">");
        }
    }

    @Validated @Test(expected=SOAPException.class)
    public void testCreateQNameWithUnknownPrefix() throws Exception {
        soapEle.createQName("local", "nonexistingprefix");
    }

    @Validated @Test
    public void testCreateQNameWithAddNamespaceDeclaration() throws Exception {
        soapEle.addNamespaceDeclaration("p", "urn:test");
        QName qname = soapEle.createQName("local", "p");
        assertEquals("p", qname.getPrefix());
        assertEquals("local", qname.getLocalPart());
        assertEquals("urn:test", qname.getNamespaceURI());
    }

    @Validated @Test
    public void testRemoveContent() throws Exception {
        boolean pass = true;
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage message = factory.createMessage();
        SOAPPart soapPart = message.getSOAPPart();
        SOAPEnvelope envelope = soapPart.getEnvelope();
        SOAPBody body = envelope.getBody();

        Name name = envelope.createName("MyChild");
        SOAPElement se = body.addChildElement(name);
        assertNotNull(se);
        Iterator childs = body.getChildElements(name);
        int childElementCount = 0;
        for (int a = 0; childs.hasNext(); a++) {
            childs.next();
            childElementCount++;
        }
        childs = body.getChildElements(name);
        assertEquals(childElementCount, 1);

        Name n = se.getElementName();
        assertEquals(n, name);
        //Child addition verified, now call removeContents to delete it
        se.removeContents();
        childs = se.getChildElements();
        childElementCount = 0;
        for (int a = 0; childs.hasNext(); a++) {
            childs.next();
            childElementCount++;
        }
        assertEquals(childElementCount, 0);
    }

    @Validated @Test
    public void testSetElementQName() throws Exception {
        MessageFactory factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage message = factory.createMessage();
        SOAPPart soapPart = message.getSOAPPart();
        SOAPEnvelope envelope = soapPart.getEnvelope();
        SOAPBody body = envelope.getBody();

        QName qname1 = new QName("http://fooURI.com", "fooElement", "foo");
        QName qname2 = new QName("http://foo2URI.com", "fooElement2", "foo2");
        SOAPElement se = body.addChildElement(qname1);
        QName qname = se.getElementQName();
        se = se.setElementQName(qname2);
        qname = se.getElementQName();

        if (!qname.getNamespaceURI().equals(qname2.getNamespaceURI()) ||
                !qname.getLocalPart().equals(qname2.getLocalPart()) ||
                !qname.getPrefix().equals(qname2.getPrefix())) {
            System.out.println("setElementQName() did not reset " +
                    "element qname\nexpected: <URI=" + qname2.getNamespaceURI() +
                    ", prefix=" + qname2.getPrefix() + ", localpart=" + qname2.getLocalPart() +
                    ">\ngot:      <URI=" + qname.getNamespaceURI() + ", prefix=" +
                    qname.getPrefix() +
                    ", localpart=" + qname.getLocalPart() + ">");
        }
    }

    @Validated @Test
    public void testSetElementQName2() throws Exception {
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage message = factory.createMessage();
        SOAPPart soapPart = message.getSOAPPart();
        SOAPEnvelope envelope = soapPart.getEnvelope();
        SOAPBody body = envelope.getBody();
        SOAPHeader header = envelope.getHeader();

        QName qname = new QName("qname");
        //Try and change element name of SOAPEnvelope (expect SOAPException)
        try {
            envelope.setElementQName(qname);
            fail("Did not throw expected SOAPException");
        } catch (SOAPException e) {
            //Caught expected SOAPException
        }

        //Try and change element name of SOAPHeader (expect SOAPException)
        try {
            header.setElementQName(qname);
            fail("Did not throw expected SOAPException");
        } catch (SOAPException e) {
            //Caught expected SOAPException
        }

        //Try and change element name of SOAPBody (expect SOAPException)
        try {
            body.setElementQName(qname);
            fail("Did not throw expected SOAPException");
        } catch (SOAPException e) {
            //Caught expected SOAPException
        }
    }
    
    @Validated @Test
    public void testAppendChild() throws Exception {
        MessageFactory fact = MessageFactory.newInstance();
        SOAPMessage message = fact.createMessage();
        SOAPBody soapBody = message.getSOAPBody();

        QName qname1 = new QName("http://wombat.ztrade.com",
                                 "GetLastTradePrice", "ztrade");
        SOAPElement child = soapBody.addChildElement(qname1);
        
        assertFalse(child.getChildElements().hasNext());
        
        Document doc = child.getOwnerDocument();        
        String namespace = "http://example.com";
        String localName = "GetLastTradePrice";
        Element getLastTradePrice = doc.createElementNS(namespace, localName);        
        Element symbol = doc.createElement("symbol");
        symbol.setAttribute("foo", "bar");
        getLastTradePrice.appendChild(symbol);
        org.w3c.dom.Text def = doc.createTextNode("DEF");
        symbol.appendChild(def);
                
        child.appendChild(getLastTradePrice);
        
        Iterator iter = child.getChildElements();
        assertTrue(iter.hasNext()); 
        Object obj = iter.next();
        assertTrue(obj instanceof SOAPElement);  
        SOAPElement soapElement = (SOAPElement)obj;
        assertEquals(namespace, soapElement.getNamespaceURI());
        assertEquals(localName, soapElement.getLocalName());
        assertFalse(iter.hasNext());
        
        iter = soapElement.getChildElements();
        assertTrue(iter.hasNext()); 
        obj = iter.next();
        assertTrue(obj instanceof SOAPElement);  
        soapElement = (SOAPElement)obj;
        assertEquals(null, soapElement.getNamespaceURI());
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
    
    @Validated @Test
    public void testRemoveChild() throws Exception {
        MessageFactory fact = MessageFactory.newInstance();
        SOAPMessage message = fact.createMessage();
        SOAPBody soapBody = message.getSOAPBody();

        assertFalse(soapBody.getChildElements().hasNext());
        
        QName qname1 = new QName("http://wombat.ztrade.com",
                                 "GetLastTradePrice", "ztrade");
        SOAPElement child = soapBody.addChildElement(qname1);
        child.addTextNode("foo");
        
        assertTrue(child.getChildElements().hasNext());
        
        Node textNode = (Node)child.getChildElements().next();
        assertTrue(textNode instanceof Text);
        
        /* 
        child.removeChild(textNode);
        
        assertFalse(child.getChildElements().hasNext());
        */
        
        assertTrue(soapBody.getChildElements().hasNext());
        
        soapBody.removeChild(child);
        
        assertFalse(soapBody.getChildElements().hasNext());
    }
    
    @Validated @Test
    public void testAddRemoveTextNode() throws Exception {
        MessageFactory mf = MessageFactory.newInstance();
        SOAPMessage msg = mf.createMessage();
        SOAPBody body = msg.getSOAPBody();
        SOAPBodyElement bodyChild = body.addBodyElement(new QName("TestChild"));
        bodyChild.addTextNode("aaaaa");
        bodyChild.removeChild(bodyChild.getFirstChild());
    }
}
