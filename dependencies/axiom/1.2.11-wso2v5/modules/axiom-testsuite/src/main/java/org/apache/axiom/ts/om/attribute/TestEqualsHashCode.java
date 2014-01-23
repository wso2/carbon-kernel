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
package org.apache.axiom.ts.om.attribute;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.ts.AxiomTestCase;

public class TestEqualsHashCode extends AxiomTestCase {
    public TestEqualsHashCode(OMMetaFactory metaFactory) {
        super(metaFactory);
    }

    protected void runTest() throws Throwable {
        OMFactory factory = metaFactory.getOMFactory();
        OMNamespace ns1 = factory.createOMNamespace("urn:ns1", "ns1");
        OMNamespace ns2 = factory.createOMNamespace("urn:ns2", "ns2");
        OMNamespace ns3 = factory.createOMNamespace("urn:ns1", "p");
        OMAttribute attr1 = factory.createOMAttribute("attr", ns1, "value");
        OMAttribute attr2 = factory.createOMAttribute("attr", ns1, "value");
        assertEquals(attr1, attr1);
        assertEquals(attr1, attr2);
        assertEquals(attr1.hashCode(), attr2.hashCode());
        assertFalse(attr1.equals(factory.createOMAttribute("otherattr", ns1, "value")));
        assertFalse(attr1.equals(factory.createOMAttribute("attr", ns2, "value")));
        assertFalse(attr1.equals(factory.createOMAttribute("attr", ns1, "othervalue")));
        assertFalse(attr1.equals(factory.createOMAttribute("attr", ns3, "value")));
    }
}
