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

import java.io.ByteArrayInputStream;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.util.AXIOMUtil;

public abstract class OMElementTestBase extends AbstractTestCase {
    protected final OMMetaFactory omMetaFactory;

    public OMElementTestBase(OMMetaFactory omMetaFactory) {
        this.omMetaFactory = omMetaFactory;
    }

    public void testSetText() {
        OMFactory factory = omMetaFactory.getOMFactory();
        String localName = "TestLocalName";
        String namespace = "http://ws.apache.org/axis2/ns";
        String prefix = "axis2";
        OMElement elem = factory.createOMElement(localName, namespace, prefix);

        String text = "The quick brown fox jumps over the lazy dog";

        elem.setText(text);

        assertEquals("Text value mismatch", text, elem.getText());
    }

    public void testCDATA() throws Exception {
        OMFactory factory = omMetaFactory.getOMFactory();
        OMElement omElement = factory.createOMElement("TestElement", null);
        final String text = "this is <some> text in a CDATA";
        factory.createOMText(omElement, text, XMLStreamConstants.CDATA);
        assertEquals(text, omElement.getText());

        // OK, CDATA on its own worked - now confirm that a plain text + a CDATA works
        omElement = factory.createOMElement("element2", null);
        final String normalText = "regular text and ";
        factory.createOMText(omElement, normalText);
        factory.createOMText(omElement, text, XMLStreamConstants.CDATA);
        assertEquals(normalText + text, omElement.getText());
    }
    
    public void testAddChild() {
        OMFactory factory = omMetaFactory.getOMFactory();
        String localName = "TestLocalName";
        String childLocalName = "TestChildLocalName";
        String namespace = "http://ws.apache.org/axis2/ns";
        String prefix = "axis2";

        OMElement elem = factory.createOMElement(localName, namespace, prefix);
        OMElement childElem = factory.createOMElement(childLocalName, namespace, prefix);

        elem.addChild(childElem);

        Iterator it = elem.getChildrenWithName(new QName(namespace, childLocalName));

        int count = 0;
        while (it.hasNext()) {
            OMElement child = (OMElement) it.next();
            assertEquals("Child local name mismatch", childLocalName, child.getLocalName());
            assertEquals("Child namespace mismatch", namespace,
                         child.getNamespace().getNamespaceURI());
            count ++;
        }
        assertEquals("In correct number of children", 1, count);
    }
    
    public void testFindNamespaceByPrefix() throws Exception {
        OMElement root =
                AXIOMUtil.stringToOM(omMetaFactory.getOMFactory(), "<a:root xmlns:a='urn:a'><child/></a:root>");
        OMNamespace ns = root.getFirstElement().findNamespace(null, "a");
        assertNotNull(ns);
        assertEquals("urn:a", ns.getNamespaceURI());
        root.close(false);
    }
    
    private int getNumberOfOccurrences(String xml, String pattern) {
        int index = -1;
        int count = 0;
        while ((index = xml.indexOf(pattern, index + 1)) != -1) {
            count++;
        }

        return count;
    }

    public void testDeclareDefaultNamespace1() throws XMLStreamException {

        /**
         * <RootElement xmlns="http://one.org">
         *   <ns2:ChildElementOne xmlns:ns2="http://ws.apache.org/axis2" xmlns="http://two.org">
         *      <ChildElementTwo xmlns="http://one.org" />
         *   </ns2:ChildElementOne>
         * </RootElement>
         */

        OMFactory omFac = omMetaFactory.getOMFactory();

        OMElement documentElement = omFac.createOMElement("RootElement", null);
        documentElement.declareDefaultNamespace("http://one.org");

        OMNamespace ns = omFac.createOMNamespace("http://ws.apache.org/axis2", "ns2");
        OMElement childOne = omFac.createOMElement("ChildElementOne", ns, documentElement);
        childOne.declareDefaultNamespace("http://two.org");

        OMElement childTwo = omFac.createOMElement("ChildElementTwo", null, childOne);
        childTwo.declareDefaultNamespace("http://one.org");


        assertEquals(2, getNumberOfOccurrences(documentElement.toStringWithConsume(),
                "xmlns=\"http://one.org\""));
    }

    public void testDeclareDefaultNamespace2() throws XMLStreamException {

        /**
         * <RootElement xmlns:ns1="http://one.org" xmlns:ns2="http://one.org">
         *   <ns2:ChildElementOne xmlns="http://one.org">
         *      <ns2:ChildElementTwo />
         *   </ns2:ChildElementOne>
         * </RootElement>
         */

        OMFactory omFac = omMetaFactory.getOMFactory();

        OMElement documentElement = omFac.createOMElement("RootElement", null);
        OMNamespace ns1 = documentElement.declareNamespace("http://one.org", "ns1");
        OMNamespace ns2 = documentElement.declareNamespace("http://one.org", "ns2");

        OMElement childOne = omFac.createOMElement("ChildElementOne", ns2, documentElement);
        childOne.declareDefaultNamespace("http://one.org");

        OMElement childTwo = omFac.createOMElement("ChildElementTwo", ns1, childOne);

        assertEquals(1, getNumberOfOccurrences(documentElement.toStringWithConsume(),
                "xmlns:ns2=\"http://one.org\""));
    }

    public void testMultipleDefaultNS() {
        OMFactory omFactory = omMetaFactory.getOMFactory();
        OMNamespace defaultNS1 = omFactory.createOMNamespace("http://defaultNS1.org", null);
        OMNamespace defaultNS2 = omFactory.createOMNamespace("http://defaultNS2.org", null);

        OMElement omElementOne = omFactory.createOMElement("DocumentElement", null);
        omElementOne.declareDefaultNamespace("http://defaultNS1.org");
        OMElement omElementOneChild = omFactory.createOMElement("ChildOne", null, omElementOne);


        OMElement omElementTwo = omFactory.createOMElement("Foo", defaultNS2, omElementOne);
        omElementTwo.declareDefaultNamespace("http://defaultNS2.org");
        OMElement omElementTwoChild = omFactory.createOMElement("ChildOne", null, omElementTwo);

        OMElement omElementThree = omFactory.createOMElement("Bar", defaultNS1, omElementTwo);
        omElementThree.declareDefaultNamespace("http://defaultNS1.org");

        OMNamespace omElementOneChildNS = omElementOneChild.getNamespace();
        OMNamespace omElementTwoChildNS = omElementTwoChild.getNamespace();
        // TODO: LLOM's and DOOM's behaviors are slightly different here; need to check if both are allowed
        assertTrue(omElementOneChildNS == null || "".equals(omElementOneChildNS.getNamespaceURI()));
        assertTrue(omElementTwoChildNS == null || "".equals(omElementTwoChildNS.getNamespaceURI()));
    }

    public void testChildReDeclaringParentsDefaultNSWithPrefix() throws Exception {
        OMFactory fac = omMetaFactory.getOMFactory();
        OMElement elem = fac.createOMElement("RequestSecurityToken", null);
        elem.declareDefaultNamespace("http://schemas.xmlsoap.org/ws/2005/02/trust");
        fac.createOMElement(new QName("TokenType"), elem).setText("test");
        fac.createOMElement(new QName("RequestType"), elem).setText("test1");

        fac.createOMElement(
                new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "Entropy", "wst"),
                elem);
        String xml = elem.toString();

        OMXMLParserWrapper builder = OMXMLBuilderFactory.createOMBuilder(omMetaFactory.getOMFactory(),
                new ByteArrayInputStream(xml.getBytes()));

        builder.getDocumentElement().build();

        // The StAX implementation may or may not have a trailing blank in the tag
        String assertText1 =
                "<wst:Entropy xmlns:wst=\"http://schemas.xmlsoap.org/ws/2005/02/trust\" />";
        String assertText2 =
                "<wst:Entropy xmlns:wst=\"http://schemas.xmlsoap.org/ws/2005/02/trust\"/>";
        String assertText3 =
                "<wst:Entropy xmlns:wst=\"http://schemas.xmlsoap.org/ws/2005/02/trust\"></wst:Entropy>";

        assertTrue((xml.indexOf(assertText1) != -1) ||
                (xml.indexOf(assertText2) != -1) ||
                (xml.indexOf(assertText3) != -1));
    }

    public void testChildReDeclaringGrandParentsDefaultNSWithPrefix() {
        OMFactory fac = omMetaFactory.getOMFactory();
        OMElement elem = fac.createOMElement("RequestSecurityToken", null);
        elem.declareDefaultNamespace("http://schemas.xmlsoap.org/ws/2005/02/trust");
        fac.createOMElement(new QName("TokenType"), elem).setText("test");
        fac.createOMElement(new QName("RequestType"), elem).setText("test1");

        OMElement entElem = fac.createOMElement(
                new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "Entropy", "wst"),
                elem);
        OMElement binSecElem = fac.createOMElement(
                new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "Binarysecret", "wst"),
                entElem);
        binSecElem.setText("secret value");
        String xml = elem.toString();
        assertTrue("Binarysecret element should have \'wst\' ns prefix",
                   xml.indexOf("<wst:Binarysecret") != -1);
    }
}
