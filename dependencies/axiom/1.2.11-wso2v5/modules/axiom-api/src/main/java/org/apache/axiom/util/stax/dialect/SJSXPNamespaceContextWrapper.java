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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

class SJSXPNamespaceContextWrapper implements NamespaceContext {
    private final NamespaceContext parent;

    public SJSXPNamespaceContextWrapper(NamespaceContext parent) {
        this.parent = parent;
    }

    public String getNamespaceURI(String prefix) {
        String namespaceURI = parent.getNamespaceURI(prefix);
        return namespaceURI == null ? XMLConstants.NULL_NS_URI : namespaceURI;
    }

    public String getPrefix(String namespaceURI) {
        return parent.getPrefix(namespaceURI);
    }

    public Iterator getPrefixes(String namespaceURI) {
        // SJSXP doesn't correctly handle masked namespace declarations
        List prefixes = new ArrayList(5);
        for (Iterator it = parent.getPrefixes(namespaceURI); it.hasNext(); ) {
            String prefix = (String)it.next();
            String actualNamespaceURI = parent.getNamespaceURI(prefix);
            if (namespaceURI == actualNamespaceURI
                    || namespaceURI != null && namespaceURI.equals(actualNamespaceURI)) {
                prefixes.add(prefix);
            }
        }
        return prefixes.iterator();
    }
}
