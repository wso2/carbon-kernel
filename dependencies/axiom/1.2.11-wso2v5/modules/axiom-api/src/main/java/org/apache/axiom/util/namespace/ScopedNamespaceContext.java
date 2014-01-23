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

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/**
 * {@link NamespaceContext} implementation that supports nested scopes. A scope is typically
 * associated with a start tag&nbsp;/ end tag pair. The implementation takes care of correctly
 * handling masked namespace bindings. Masking occurs when the same prefix is bound to a different
 * namespace URI in a nested scope.
 */
public class ScopedNamespaceContext extends AbstractNamespaceContext {
    /**
     * Array containing the prefixes for the namespace bindings.
     */
    String[] prefixArray = new String[16];
    
    /**
     * Array containing the URIs for the namespace bindings.
     */
    String[] uriArray = new String[16];
    
    /**
     * The number of currently defined namespace bindings.
     */
    int bindings;
    
    /**
     * Tracks the scopes defined for this namespace context. Each entry in the array identifies
     * the first namespace binding defined in the corresponding scope and points to an entry
     * in {@link #prefixArray}/{@link #uriArray}.
     */
    private int[] scopeIndexes = new int[16];
    
    /**
     * The number of currently defined scopes. This is the same as the depth of the current scope,
     * where the depth of the root scope is 0.
     */
    private int scopes;

    /**
     * Define a prefix binding in the current scope. It will be visible in the current scope as
     * well as each nested scope, unless the prefix is bound to a different namespace URI in that
     * scope.
     * 
     * @param prefix the prefix to bind or the empty string to set the default namespace; may not
     *               be <code>null</code>
     * @param namespaceURI the corresponding namespace URI; may not be <code>null</code>
     */
    public void setPrefix(String prefix, String namespaceURI) {
        if (prefix == null || namespaceURI == null) {
            throw new IllegalArgumentException("prefix and namespaceURI may not be null");
        }
        if (bindings == prefixArray.length) {
            int len = prefixArray.length;
            int newLen = len*2;
            String[] newPrefixArray = new String[newLen];
            System.arraycopy(prefixArray, 0, newPrefixArray, 0, len);
            String[] newUriArray = new String[newLen];
            System.arraycopy(uriArray, 0, newUriArray, 0, len);
            prefixArray = newPrefixArray;
            uriArray = newUriArray;
        }
        prefixArray[bindings] = prefix;
        uriArray[bindings] = namespaceURI;
        bindings++;
    }
    
    /**
     * Start a new scope. Since scopes are nested, this will not end the current scope.
     */
    public void startScope() {
        if (scopes == scopeIndexes.length) {
            int[] newScopeIndexes = new int[scopeIndexes.length*2];
            System.arraycopy(scopeIndexes, 0, newScopeIndexes, 0, scopeIndexes.length);
            scopeIndexes = newScopeIndexes;
        }
        scopeIndexes[scopes++] = bindings;
    }
    
    /**
     * End the current scope and restore the scope in which the current scope was nested.
     * This will remove all prefix bindings declared since the corresponding invocation
     * of the {@link #startScope()} method.
     */
    public void endScope() {
        bindings = scopeIndexes[--scopes];
    }
    
    protected String doGetNamespaceURI(String prefix) {
        for (int i=bindings-1; i>=0; i--) {
            if (prefix.equals(prefixArray[i])) {
                return uriArray[i];
            }
        }
        // The Javadoc of NamespaceContext#getNamespaceURI specifies that XMLConstants.NULL_NS_URI
        // is returned both for unbound prefixes and the null namespace
        return XMLConstants.NULL_NS_URI;
    }

    protected String doGetPrefix(String namespaceURI) {
        outer: for (int i=bindings-1; i>=0; i--) {
            if (namespaceURI.equals(uriArray[i])) {
                String prefix = prefixArray[i];
                // Now check that the prefix is not masked
                for (int j=i+1; j<bindings; j++) {
                    if (prefix.equals(prefixArray[j])) {
                        continue outer;
                    }
                }
                return prefix;
            }
        }
        return null;
    }

    protected Iterator doGetPrefixes(final String namespaceURI) {
        return new Iterator() {
            private int binding = bindings;
            private String next;

            public boolean hasNext() {
                if (next == null) {
                    outer: while (--binding >= 0) {
                        if (namespaceURI.equals(uriArray[binding])) {
                            String prefix = prefixArray[binding];
                            // Now check that the prefix is not masked
                            for (int j=binding+1; j<bindings; j++) {
                                if (prefix.equals(prefixArray[j])) {
                                    continue outer;
                                }
                            }
                            next = prefix;
                            break;
                        }
                    }
                }
                return next != null;
            }

            public Object next() {
                if (hasNext()) {
                    String result = next;
                    next = null;
                    return result;
                } else {
                    throw new NoSuchElementException();
                }
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
