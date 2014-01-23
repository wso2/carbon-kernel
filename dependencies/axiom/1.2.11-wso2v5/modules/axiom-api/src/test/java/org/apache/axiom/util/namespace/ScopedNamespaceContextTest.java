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

package org.apache.axiom.util.namespace;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import org.apache.axiom.testutils.namespace.NamespaceContextTestUtils;

import junit.framework.TestCase;

public class ScopedNamespaceContextTest extends TestCase {
    private static Set getPrefixes(NamespaceContext nc, String namespaceURI) {
        Set result = new HashSet();
        for (Iterator it = nc.getPrefixes(namespaceURI); it.hasNext(); ) {
            result.add(it.next());
        }
        return result;
    }
    
    public void testSimple() {
        ScopedNamespaceContext nc = new ScopedNamespaceContext();
        nc.setPrefix("", "urn:ns1");
        nc.setPrefix("a", "urn:ns2");
        nc.setPrefix("b", "urn:ns3");
        assertEquals("urn:ns1", nc.getNamespaceURI(""));
        assertEquals("urn:ns2", nc.getNamespaceURI("a"));
        assertEquals("urn:ns3", nc.getNamespaceURI("b"));
        assertEquals("", nc.getPrefix("urn:ns1"));
        assertEquals("a", nc.getPrefix("urn:ns2"));
        assertEquals("b", nc.getPrefix("urn:ns3"));
        assertEquals(Collections.singleton(""), getPrefixes(nc, "urn:ns1"));
        assertEquals(Collections.singleton("a"), getPrefixes(nc, "urn:ns2"));
        assertEquals(Collections.singleton("b"), getPrefixes(nc, "urn:ns3"));
    }
    
    public void testMultiplePrefixes() {
        ScopedNamespaceContext nc = new ScopedNamespaceContext();
        nc.setPrefix("", "urn:ns1");
        nc.setPrefix("a", "urn:ns2");
        nc.setPrefix("b", "urn:ns1");
        String prefix = nc.getPrefix("urn:ns1");
        assertTrue(prefix.equals("") || prefix.equals("b"));
        assertEquals(new HashSet(Arrays.asList(new String[] { "", "b" })),
                     getPrefixes(nc, "urn:ns1"));
    }
    
    public void testScope() {
        ScopedNamespaceContext nc = new ScopedNamespaceContext();
        nc.setPrefix("ns1", "urn:ns1");
        nc.startScope();
        nc.setPrefix("ns2", "urn:ns2");
        nc.startScope();
        nc.setPrefix("ns3", "urn:ns3");
        assertEquals("urn:ns1", nc.getNamespaceURI("ns1"));
        assertEquals("urn:ns2", nc.getNamespaceURI("ns2"));
        assertEquals("urn:ns3", nc.getNamespaceURI("ns3"));
        nc.endScope();
        assertEquals("urn:ns1", nc.getNamespaceURI("ns1"));
        assertEquals("urn:ns2", nc.getNamespaceURI("ns2"));
        assertEquals(XMLConstants.NULL_NS_URI, nc.getNamespaceURI("ns3"));
        nc.endScope();
        assertEquals("urn:ns1", nc.getNamespaceURI("ns1"));
        assertEquals(XMLConstants.NULL_NS_URI, nc.getNamespaceURI("ns2"));
        assertEquals(XMLConstants.NULL_NS_URI, nc.getNamespaceURI("ns3"));
    }
    
    public void testMaskedPrefix() {
        ScopedNamespaceContext nc = new ScopedNamespaceContext();
        nc.setPrefix("p", "urn:ns1");
        nc.startScope();
        nc.setPrefix("p", "urn:ns2");
        assertEquals("urn:ns2", nc.getNamespaceURI("p"));
        assertNull(nc.getPrefix("urn:ns1"));
        assertEquals(Collections.singleton("p"), getPrefixes(nc, "urn:ns2"));
        assertFalse(nc.getPrefixes("urn:ns1").hasNext());
        nc.endScope();
        assertEquals("p", nc.getPrefix("urn:ns1"));
        assertEquals(Collections.singleton("p"), getPrefixes(nc, "urn:ns1"));
    }
    
    public void testImplicitNamespaces() {
        NamespaceContextTestUtils.checkImplicitNamespaces(new ScopedNamespaceContext());
    }
}
