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
package org.apache.axiom.util.stax.dialect;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.util.namespace.ScopedNamespaceContext;

public class GetNamespaceContextTestCase extends DialectTestCase {
    private final String testResource;
    
    public GetNamespaceContextTestCase(String testResource) {
        this.testResource = testResource;
        setName(getClass().getName() + " [" + testResource + "]");
    }

    // Copy & paste from XMLStreamReaderComparator
    private Set toPrefixSet(Iterator it) {
        Set set = new HashSet();
        while (it.hasNext()) {
            String prefix = (String)it.next();
            // TODO: Woodstox returns null instead of "" for the default namespace.
            //       This seems incorrect, but the javax.namespace.NamespaceContext specs are
            //       not very clear.
            set.add(prefix == null ? "" : prefix);
        }
        return set;
    }
    
    protected void runTest() throws Throwable {
        XMLInputFactory factory = newNormalizedXMLInputFactory();
        InputStream in = getTestResource(testResource);
        Set/*<String>*/ prefixes = new HashSet();
        Set/*<String>*/ namespaceURIs = new HashSet();
        prefixes.add("");
        try {
            ScopedNamespaceContext refNc = new ScopedNamespaceContext();
            XMLStreamReader reader = factory.createXMLStreamReader(in);
            int depth = 0;
            int eventType;
            while ((eventType = reader.getEventType()) != XMLStreamReader.END_DOCUMENT) {
                if (eventType == XMLStreamReader.START_ELEMENT) {
                    refNc.startScope();
                    for (int i=0; i<reader.getNamespaceCount(); i++) {
                        String prefix = reader.getNamespacePrefix(i);
                        String uri = reader.getNamespaceURI(i);
                        refNc.setPrefix(prefix == null ? "" : prefix,
                                        uri == null ? "" : uri);
                        if (prefix != null) {
                            prefixes.add(prefix);
                        }
                        if (uri != null && uri.length() > 0) {
                            namespaceURIs.add(uri);
                        }
                    }
                    depth++;
                }
                if (depth > 0) {
                    NamespaceContext nc = reader.getNamespaceContext();
                    for (Iterator it = prefixes.iterator(); it.hasNext(); ) {
                        String prefix = (String)it.next();
                        String expectedUri = refNc.getNamespaceURI(prefix);
                        String actualUri = nc.getNamespaceURI(prefix);
                        assertEquals("Namespace URI for prefix '" + prefix + "'", expectedUri, actualUri);
                    }
                    for (Iterator it = namespaceURIs.iterator(); it.hasNext(); ) {
                        String namespaceURI = (String)it.next();
                        assertEquals(
                                "Prefix for namespace URI '" + namespaceURI + "'",
                                refNc.getPrefix(namespaceURI),
                                nc.getPrefix(namespaceURI));
                        assertEquals(
                                "Prefixes for namespace URI '" + namespaceURI + "'",
                                toPrefixSet(refNc.getPrefixes(namespaceURI)),
                                toPrefixSet(nc.getPrefixes(namespaceURI)));
                    }
                }
                if (eventType == XMLStreamReader.END_ELEMENT) {
                    refNc.endScope();
                    depth--;
                }
                reader.next();
            }
        } finally {
            in.close();
        }
    }
}
