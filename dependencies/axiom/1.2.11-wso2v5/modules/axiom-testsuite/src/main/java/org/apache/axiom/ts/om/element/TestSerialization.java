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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.ts.AxiomTestCase;

/**
 * Tests proper serialization for different combinations of namespaces on the element and its children.
 * The test creates a parent "person" and children "name", "age", "weight". The
 * parent is defined as either:
 * <ul>
 * <li>a qualified name (<code>parent=Q</code>)
 * <li>an unqualified name (<code>parent=U</code>)
 * <li>a qualified name using the default namespace (<code>parent=D</code>)
 * </ul>
 * <p>
 * Likewise the children are defined as either:
 * <ul>
 * <li>qualified names (<code>children=Q</code>)
 * <li>unqualified children (<code>children=U</code>)
 * <li>qualified using the default namespace (<code>children=D</code>)
 * </ul>
 */
public class TestSerialization extends AxiomTestCase {
    private static final String NS = "urn:ns";
    private static final String PREFIX = "p";
    
    private final String parent;
    private final String children;
    private final String expected;
    
    public TestSerialization(OMMetaFactory metaFactory, String parent, String children, String expected) {
        super(metaFactory);
        this.parent = parent;
        this.children = children;
        this.expected = expected;
        setName(getName() + " [parent=" + parent + ",children=" + children + "]");
    }
    
    private static OMNamespace createNamespace(OMFactory factory, String type) {
        if (type.equals("Q")) {
            return factory.createOMNamespace(NS, PREFIX);
        } else if (type.equals("U")) {
            return factory.createOMNamespace("", "");
        } else if (type.equals("D")) {
            return factory.createOMNamespace(NS, "");
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    protected void runTest() throws Throwable {
        OMFactory fac = metaFactory.getOMFactory();

        OMNamespace nsParent = createNamespace(fac, parent);
        OMNamespace nsChildren = createNamespace(fac, children);

        OMElement personElem = fac.createOMElement("person", nsParent);
        OMElement nameElem = fac.createOMElement("name", nsChildren);
        nameElem.setText("John");

        OMElement ageElem = fac.createOMElement("age", nsChildren);
        ageElem.setText("34");

        OMElement weightElem = fac.createOMElement("weight", nsChildren);
        weightElem.setText("50");

        //Add children to the person element
        personElem.addChild(nameElem);
        personElem.addChild(ageElem);
        personElem.addChild(weightElem);

        assertEquals(expected, personElem.toString());
    }
}
