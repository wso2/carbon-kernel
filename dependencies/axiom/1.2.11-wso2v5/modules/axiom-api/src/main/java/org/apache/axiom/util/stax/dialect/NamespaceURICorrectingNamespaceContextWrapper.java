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

import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/**
 * Wrapper that fixes the behavior of {@link NamespaceContext#getNamespaceURI(String)}. The Javadoc
 * of that method clearly specifies that the return value of that method may never be
 * <code>null</code>. In particular, the expected result for an unbound prefix is
 * {@link XMLConstants#NULL_NS_URI}. However, many implementations incorrectly return
 * <code>null</code> in that case.
 */
class NamespaceURICorrectingNamespaceContextWrapper implements NamespaceContext {
    private final NamespaceContext parent;
    
    public NamespaceURICorrectingNamespaceContextWrapper(NamespaceContext parent) {
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
        return parent.getPrefixes(namespaceURI);
    }
}
