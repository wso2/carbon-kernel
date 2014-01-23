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

import java.util.Collections;
import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/**
 * Partial {@link NamespaceContext} implementation that takes care of the
 * implicit namespace bindings (for the <tt>xml</tt> and <tt>xmlns</tt>
 * prefixes) defined in the {@link NamespaceContext} Javadoc.
 */
public abstract class AbstractNamespaceContext implements NamespaceContext {

    public final String getNamespaceURI(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("prefix can't be null");
        } else if (prefix.equals(XMLConstants.XML_NS_PREFIX)) {
            return XMLConstants.XML_NS_URI;
        } else if (prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
            return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
        } else {
            return doGetNamespaceURI(prefix);
        }
    }

    /**
     * Get namespace URI bound to a prefix in the current scope. The contract of
     * this method is the same as
     * {@link NamespaceContext#getNamespaceURI(String)}, except that the
     * implementation is not required to handle the implicit namespace bindings.
     * 
     * @param prefix
     *            prefix to look up
     * @return namespace URI bound to prefix in the current scope
     */
    protected abstract String doGetNamespaceURI(String prefix);
    
    public final String getPrefix(String namespaceURI) {
        if (namespaceURI == null) {
            throw new IllegalArgumentException("namespaceURI can't be null");
        } else if (namespaceURI.equals(XMLConstants.XML_NS_URI)) {
            return XMLConstants.XML_NS_PREFIX;
        } else if (namespaceURI.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
            return XMLConstants.XMLNS_ATTRIBUTE;
        } else {
            return doGetPrefix(namespaceURI);
        }
    }
    
    /**
     * Get prefix bound to namespace URI in the current scope. The contract of
     * this method is the same as {@link NamespaceContext#getPrefix(String)},
     * except that the implementation is not required to handle the implicit
     * namespace bindings.
     * 
     * @param namespaceURI
     *            URI of namespace to lookup
     * @return prefix bound to namespace URI in current context
     */
    protected abstract String doGetPrefix(String namespaceURI);

    public final Iterator getPrefixes(String namespaceURI) {
        if (namespaceURI == null) {
            throw new IllegalArgumentException("namespaceURI can't be null");
        } else if (namespaceURI.equals(XMLConstants.XML_NS_URI)) {
            return Collections.singleton(XMLConstants.XML_NS_PREFIX).iterator();
        } else if (namespaceURI.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
            return Collections.singleton(XMLConstants.XMLNS_ATTRIBUTE).iterator();
        } else {
            return doGetPrefixes(namespaceURI);
        }
    }

    /**
     * Get all prefixes bound to a namespace URI in the current scope. The
     * contract of this method is the same as
     * {@link NamespaceContext#getPrefixes(String)}, except that the
     * implementation is not required to handle the implicit namespace bindings.
     * 
     * @param namespaceURI
     *            URI of namespace to lookup
     * @return iterator for all prefixes bound to the namespace URI in the
     *         current scope
     */
    protected abstract Iterator doGetPrefixes(String namespaceURI);
}
