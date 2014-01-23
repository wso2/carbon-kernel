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
package org.apache.axiom.ts.om.element;

import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.ts.AxiomTestCase;

/**
 * Test that {@link OMElement#addAttribute(String, String, OMNamespace)} behaves correctly when an
 * attribute with the same name and namespace URI already exists.
 */
public class TestAddAttributeReplace2 extends AxiomTestCase {
    public TestAddAttributeReplace2(OMMetaFactory metaFactory) {
        super(metaFactory);
    }

    protected void runTest() throws Throwable {
        OMFactory factory = metaFactory.getOMFactory();
        // Use same namespace URI but different prefixes
        OMNamespace ns1 = factory.createOMNamespace("urn:ns", "p1");
        OMNamespace ns2 = factory.createOMNamespace("urn:ns", "p2");
        OMElement element = factory.createOMElement(new QName("test"));
        OMAttribute att1 = element.addAttribute("test", "value1", ns1);
        OMAttribute att2 = element.addAttribute("test", "value2", ns2);
        Iterator it = element.getAllAttributes();
        assertTrue(it.hasNext());
        assertSame(att2, it.next());
        assertFalse(it.hasNext());
        assertNull(att1.getOwner());
        assertSame(element, att2.getOwner());
        assertEquals("value1", att1.getAttributeValue());
        assertEquals("value2", att2.getAttributeValue());
    }
}
