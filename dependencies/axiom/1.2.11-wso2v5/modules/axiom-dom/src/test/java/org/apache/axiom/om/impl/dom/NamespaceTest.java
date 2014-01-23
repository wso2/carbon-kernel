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

package org.apache.axiom.om.impl.dom;

import junit.framework.TestCase;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.dom.factory.OMDOMFactory;

public class NamespaceTest extends TestCase {
    public void testEquals() throws Exception {
        boolean goodResult = false;
        NamespaceImpl ns1;
        NamespaceImpl ns2;

        try {
            new NamespaceImpl(null);
        } catch (IllegalArgumentException e) {
            // Caught null, good.
            goodResult = true;
        }
        if (!goodResult)
            fail("Null namespace allowed!");

        String URI1 = "http://testuri1";
        String URI2 = "http://";
        ns1 = new NamespaceImpl(URI1);
        ns2 = new NamespaceImpl("http://testuri1");
        URI2 = URI2 + "testuri1";  // Make sure the strings don't intern to the same place
        assertTrue(ns1.equals(URI2, null));
        assertTrue(ns1.equals(ns2));
    }

    public void testSearch() throws Exception {
        String NSURI = "http://testns";
        String NSURI_UPPER = "HTTP://TESTNS";

        OMFactory fac = new OMDOMFactory();
        OMNamespace ns = new NamespaceImpl(NSURI);
        OMElement el = fac.createOMElement("foo", null);
        el.declareNamespace(NSURI, "p");
        assertNull(el.findNamespace(NSURI_UPPER, "p"));
    }
}
