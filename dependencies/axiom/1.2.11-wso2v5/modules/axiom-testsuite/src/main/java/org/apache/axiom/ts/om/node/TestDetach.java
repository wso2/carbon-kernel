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
package org.apache.axiom.ts.om.node;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.ts.AxiomTestCase;

/**
 * Tests the behavior of {@link OMNode#detach()}.
 */
public class TestDetach extends AxiomTestCase {
    private final boolean build;
    
    public TestDetach(OMMetaFactory metaFactory, boolean build) {
        super(metaFactory);
        this.build = build;
        setName(getName() + " [build=" + build + "]");
    }

    protected void runTest() throws Throwable {
        OMElement root = AXIOMUtil.stringToOM(metaFactory.getOMFactory(), "<root><a/><b/><c/></root>");
        if (build) {
            root.build();
        } else {
            assertFalse(root.isComplete());
        }
        OMElement a = (OMElement)root.getFirstOMChild();
        assertEquals("a", a.getLocalName());
        OMElement b = (OMElement)a.getNextOMSibling();
        assertEquals("b", b.getLocalName());
        b.detach();
        assertNull(b.getParent());
        OMElement c = (OMElement)a.getNextOMSibling();
        assertEquals("c", c.getLocalName());
        assertSame(c, a.getNextOMSibling());
        assertSame(a, c.getPreviousOMSibling());
        root.close(false);
    }
}
