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

import java.util.HashMap;
import java.util.Map;

import org.apache.axiom.c14n.omwrapper.interfaces.Element;
import org.apache.axiom.c14n.omwrapper.interfaces.NamedNodeMap;
import org.apache.axiom.c14n.omwrapper.interfaces.Attr;
import org.apache.axiom.c14n.DataParser;
import org.apache.axiom.om.OMElement;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import javax.xml.namespace.QName;

/**
 * @author Saliya Ekanayake (esaliya@gmail.com)
 */
public class AttrImplTest extends TestCase {
    private DataParser dp;
    private NamedNodeMap nnm;
    private Attr attr;

    public AttrImplTest(String name){
        super(name);
    }

    public static Test suite() {
        return new TestSuite(AttrImplTest.class);
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public void setUp() throws Exception{
        dp = new DataParser("/sample1.xml");
        dp.init();
        // get e5
        OMElement e5 = dp.omDocEle.getFirstChildWithName(new QName("http://example.org","e5"));
        // get the wrapped element of e5
        Element e = (Element)dp.fac.getNode(e5);
        nnm = e.getAttributes();

    }

    public void testAttrProperties(){
        // e5 has 7 attributes
        assertEquals(7, nnm.getLength());
        
        Map map = new HashMap();
        for (int i = 0; i < 7; i++) {
            attr = (Attr)nnm.item(i);
            QName name = new QName(attr.getNamespaceURI(), attr.getLocalName());
            map.put(name, attr);
        }
        
        //attr is a:attr="out"
        attr = (Attr)map.get(new QName("http://www.w3.org", "attr"));
        assertNotNull(attr);
        assertEquals("attr", attr.getLocalName());
        assertEquals("a:attr", attr.getName());
        assertEquals("a:attr", attr.getNodeName());
        assertEquals("a", attr.getPrefix());
        assertEquals("http://www.w3.org", attr.getNamespaceURI());

        // attr is attr2="all"
        attr = (Attr)map.get(new QName("attr2"));
        assertNotNull(attr);
        assertEquals("attr2", attr.getLocalName());
        assertEquals("attr2", attr.getName());
        assertEquals("attr2", attr.getNodeName());
        assertNull("prefix of attr2=\"all\" is null", attr.getPrefix());
        assertNull("Namespace URI of attr2=\"all\" is null", attr.getNamespaceURI());
        assertEquals("all", attr.getValue());
        assertEquals("all", attr.getNodeValue());

        // attr is xmlns:a="http://www.w3.org"
        attr = (Attr)map.get(new QName("http://www.w3.org/2000/xmlns/", "a"));
        assertNotNull(attr);
        assertEquals("a", attr.getLocalName());
        assertEquals("xmlns:a", attr.getName());
        assertEquals("xmlns:a", attr.getNodeName());
        assertEquals("xmlns", attr.getPrefix());
        // the namespace URI of xmlns is "http://www.w3.org/2000/xmlns/"
        assertEquals("http://www.w3.org/2000/xmlns/", attr.getNamespaceURI());
        assertEquals("http://www.w3.org", attr.getValue());
        assertEquals("http://www.w3.org", attr.getNodeValue());

        // attr is xmlns="http://example.org"
        attr = (Attr)map.get(new QName("http://www.w3.org/2000/xmlns/", "xmlns"));
        assertNotNull(attr);
        assertEquals("xmlns", attr.getLocalName());
        assertEquals("xmlns", attr.getName());
        assertEquals("xmlns", attr.getNodeName());
        assertNull("prefix of xmlns=\"http://example.org\" is null", attr.getPrefix());
        // the namespace URI of xmlns is "http://www.w3.org/2000/xmlns/"
        assertEquals("http://www.w3.org/2000/xmlns/", attr.getNamespaceURI());
        assertEquals("http://example.org", attr.getValue());
        assertEquals("http://example.org", attr.getNodeValue());
    }

    public void testOwnerElement() {
        // get e5
        OMElement e5 = dp.omDocEle.getFirstChildWithName(new QName("http://example.org","e5"));
        // get the wrapped element of e5
        Element e = (Element)dp.fac.getNode(e5);
        // attr is a:attr="out"
        attr = (Attr)e.getAttributes().item(0);
        // the getOwnerElement() should provide a reference to the same object pointed by reference e
        assertEquals(e, attr.getOwnerElement());
    }

    public void testGetNextSibling() {
        // attr is a:attr="out"
        attr = (Attr)nnm.item(0);
        assertNull("getNextSibling() should return null", attr.getNextSibling());
    }

    public void testGetPreviousSibling(){
        // attr is a:attr="out"
        attr = (Attr)nnm.item(0);
        assertNull("getPreviousSibling() should return null", attr.getPreviousSibling());
    }

    public void testGetParentNode(){
        // attr is a:attr="out"
        attr = (Attr)nnm.item(0);
        assertNull("getParentNode() should return null", attr.getParentNode());

    }



}
