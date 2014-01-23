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

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.ts.AxiomTestCase;

/**
 * Tests the behavior of {@link OMElement#getFirstChildWithName(QName)}.
 */
public class TestGetFirstChildWithName extends AxiomTestCase {
    public TestGetFirstChildWithName(OMMetaFactory metaFactory) {
        super(metaFactory);
    }

    protected void runTest() throws Throwable {
        OMFactory factory = metaFactory.getOMFactory();
        OMNamespace ns1 = factory.createOMNamespace("urn:ns1", "ns1");
        OMNamespace ns2 = factory.createOMNamespace("urn:ns2", "ns2");
        OMElement parent = factory.createOMElement("root", null);
        OMElement child1 = factory.createOMElement("a", ns1, parent);
        factory.createOMComment(parent, "some comment");
        OMElement child2 = factory.createOMElement("b", ns2, parent);
        OMElement child3 = factory.createOMElement("b", ns1, parent);
        OMElement child4 = factory.createOMElement("c", null, parent);
        factory.createOMElement("a", ns1, parent);
        
        // Check that it's really the first element that is returned
        assertSame(child1, parent.getFirstChildWithName(new QName("urn:ns1", "a")));
        
        // Test with a child that is not the first one
        assertSame(child2, parent.getFirstChildWithName(new QName("urn:ns2", "b")));
        
        // Check that the namespace URI is taken into account
        assertNull(parent.getFirstChildWithName(new QName("b")));
        
        // Check that the prefix of the given QName is not taken into account
        assertSame(child3, parent.getFirstChildWithName(new QName("urn:ns1", "b", "ns2")));
        
        // Test with null namespace
        assertSame(child4, parent.getFirstChildWithName(new QName("c")));
    }
}
