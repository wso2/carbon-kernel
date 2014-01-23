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

package org.apache.axiom.om.util;

import junit.framework.TestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.dom.DOOMAbstractFactory;

import javax.xml.namespace.QName;

public class OMAttributeHelperTest extends TestCase {

    public void testImportOMAttribute() {
        //Convert from OM to DOOM.
        OMFactory omf = OMAbstractFactory.getOMFactory();
        OMNamespace ns1 = omf.createOMNamespace("http://nsurl", "prefix");
        OMAttribute attr1 = omf.createOMAttribute("attr1", ns1, "attr1value");

        OMFactory doomf = DOOMAbstractFactory.getOMFactory();
        OMElement ome1 = doomf.createOMElement("element", ns1.getNamespaceURI(), ns1.getPrefix());
        AttributeHelper.importOMAttribute(attr1, ome1);
        assertNotSame(attr1, ome1.getAttribute(attr1.getQName()));
        assertEquals(attr1.getAttributeValue(),
                     ome1.getAttribute(attr1.getQName()).getAttributeValue());

        //Convert from DOOM to OM.
        OMNamespace ns2 = doomf.createOMNamespace("http://nsurl", "prefix");
        OMAttribute attr2 = doomf.createOMAttribute("attr2", ns2, "attr2value");

        OMElement ome2 = omf.createOMElement("element", ns2.getNamespaceURI(), ns2.getPrefix());
        AttributeHelper.importOMAttribute(attr2, ome2);
        assertNotSame(attr2, ome2.getAttribute(attr2.getQName()));
        assertEquals(attr2.getAttributeValue(),
                     ome2.getAttribute(attr2.getQName()).getAttributeValue());

        //OM only.
        OMNamespace ns3 = omf.createOMNamespace("http://nsurl", "prefix");
        OMAttribute attr3 = omf.createOMAttribute("attr3", ns3, "attr3value");

        OMElement ome3 = omf.createOMElement("element", ns3.getNamespaceURI(), ns3.getPrefix());
        AttributeHelper.importOMAttribute(attr3, ome3);
        assertSame(attr3, ome3.getAttribute(attr3.getQName()));

        //DOOM only.
        OMNamespace ns4 = doomf.createOMNamespace("http://nsurl", "prefix");
        OMAttribute attr4 = doomf.createOMAttribute("attr4", ns4, "attr4value");

        OMElement ome4 = doomf.createOMElement("element", ns4.getNamespaceURI(), ns4.getPrefix());
        AttributeHelper.importOMAttribute(attr4, ome4);
        assertSame(attr4, ome4.getAttribute(attr4.getQName()));
    }

    public void testDetachedElement() {
        OMNamespace top = DOOMAbstractFactory.getOMFactory().createOMNamespace("urn:test1", "t1");
        OMElement ome = DOOMAbstractFactory.getOMFactory().createOMElement("test", top);
        OMElement child =
                DOOMAbstractFactory.getOMFactory().createOMElement(new QName("test"), ome);
        OMAttribute oma = child.addAttribute("attr", "value", top);

        OMElement target =
                OMAbstractFactory.getOMFactory().createOMElement("test2", "urn:test2", "t2");
        AttributeHelper.importOMAttribute(oma, target);
    }
}
