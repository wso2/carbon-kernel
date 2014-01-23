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

package org.apache.axiom.c14n.omwrapper;

import org.apache.axiom.c14n.omwrapper.interfaces.Element;
import org.apache.axiom.c14n.omwrapper.interfaces.Node;
import org.apache.axiom.c14n.DataParser;
import org.apache.axiom.om.OMElement;

import javax.xml.namespace.QName;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * @author Saliya Ekanayake (esaliya@gmail.com)
 */
public class ElementImplTest extends TestCase {
    private DataParser dp = null;

    public ElementImplTest(String name){
        super(name);
    }

    public static Test suite() {
        return new TestSuite(ElementImplTest.class);
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public void setUp() throws Exception{
        dp = new DataParser("/sample1.xml");
        dp.init();
    }

    public void testHasAttributes() {
        // first child of docEle is of Text type. So we get the next sibling of that,i.e. e1
        Element e = (Element) dp.docEle.getFirstChild().getNextSibling();
        // e1 has one attribute which is in fact a namespace declaration
        assertEquals(true, e.hasAttributes());
        // get e2
        e = (Element) e.getNextSibling().getNextSibling();
        // e2 has the default namespace declaration
        assertEquals(true, e.hasAttributes());
        // get e2child
        e = (Element) e.getFirstChild();
        // e2child gets the default namespace from parent, but it's not an attribute of it
        assertEquals(false, e.hasAttributes());
    }

    public void testGetNamespaceURI() {
        // <e2   xmlns="http://www.blankns.com" ><e2child>I've no attributes</e2child></e2>
        OMElement e2 = dp.omDocEle.getFirstChildWithName(new QName("http://www.blankns.com", "e2"));
        // get the wrapped element
        Element e = (Element) dp.fac.getNode(e2);
        assertEquals("http://www.blankns.com", e.getNamespaceURI());
        // <e2child>I've no attributes</e2child>
        e = (Element) e.getFirstChild();
        // e2child inherits the default namespace of e2
        assertEquals("http://www.blankns.com", e.getNamespaceURI());

        // <e3   name =    "elem3" id="elem3"/>
        OMElement e3 = dp.omDocEle.getFirstChildWithName(new QName("e3"));
        // get the wrapped element
        e = (Element) dp.fac.getNode(e3);
        assertNull("Namespace URI of e3 is null", e.getNamespaceURI());
    }

    public void testGetPrefix() {
        // <e2   xmlns="http://www.blankns.com" ><e2child>I've no attributes</e2child></e2>
        OMElement e2 = dp.omDocEle.getFirstChildWithName(new QName("http://www.blankns.com", "e2"));
        // get the wrapped element
        Element e = (Element) dp.fac.getNode(e2);
        assertNull("prefix of e2 is null", e.getPrefix());
        // <a:e1 xmlns:a="http://www.nonamespace.com"  />
        e = (Element) e.getPreviousSibling().getPreviousSibling();
        assertEquals("a", e.getPrefix());
    }

    public void testGetNodeName() {
        // <a:e1 xmlns:a="http://www.nonamespace.com"  />
        OMElement e1 = dp.omDocEle.getFirstElement();
        // get the wrapped element
        Element e = (Element) dp.fac.getNode(e1);
        assertEquals("a:e1", e.getNodeName());
        // <e2   xmlns="http://www.blankns.com" ><e2child>I've no attributes</e2child></e2>
        e = (Element) e.getNextSibling().getNextSibling();
        assertEquals("e2", e.getNodeName());
    }

    public void testGetFirstChild() {
        Element e = (Element) dp.fac.getNode(dp.omDocEle);
        // first child of root element represents the newline character so it's a text node
        assertEquals(Node.TEXT_NODE, e.getFirstChild().getNodeType());
    }


    // there is a difference when it comes to CDATA. Axiom treats it as OMText. Eg.,
    // <e1/>
    // <![CDATA[<don't><process><this>:)]]>
    // <e2/>
    // getNextSibling() of e1 would return "\n<don't><process><this>:)\n"
    // in DOM it would be just "\n". Taking the next sibling of that would be
    // <don't><process><this>:) and taking the next sibling of that would be "\n"
    // so Axiom put all these three into one OMText node. This wasn't a issue yet :)
    // but thought to mention it for clarity.
    public void testGetNextSibling() {
        Element e = (Element) dp.fac.getNode(dp.omDocEle);
        // first child of root element represents the newline character so it's a text node
        // the getNextSibling() of that should return element e1
        assertEquals(Node.ELEMENT_NODE, (e = (Element)e.getFirstChild().getNextSibling()).getNodeType());
        assertEquals("a:e1", e.getNodeName());
    }

    public void testGetPreviousSibling() {
        // <a:e1 xmlns:a="http://www.nonamespace.com"  />
        Element e = (Element) dp.fac.getNode(dp.omDocEle.getFirstElement());
        // this n is a text node representing the newline character
        Node n = e.getPreviousSibling();
        assertEquals(Node.TEXT_NODE, n.getNodeType());
    }


}
