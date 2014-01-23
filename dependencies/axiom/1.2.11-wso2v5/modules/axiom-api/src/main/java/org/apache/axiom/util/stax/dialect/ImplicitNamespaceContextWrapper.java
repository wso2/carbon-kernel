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

import javax.xml.namespace.NamespaceContext;

import org.apache.axiom.util.namespace.AbstractNamespaceContext;

/**
 * {@link NamespaceContext} wrapper that adds support for the implicit namespace
 * bindings for the <tt>xml</tt> and <tt>xmlns</tt> prefixes. This wrapper may
 * be used to fix the behavior of broken {@link NamespaceContext}
 * implementations.
 */
class ImplicitNamespaceContextWrapper extends AbstractNamespaceContext {
    private final NamespaceContext parent;
    
    public ImplicitNamespaceContextWrapper(NamespaceContext parent) {
        this.parent = parent;
    }

    protected String doGetNamespaceURI(String prefix) {
        return parent.getNamespaceURI(prefix);
    }
    
    protected String doGetPrefix(String namespaceURI) {
        return parent.getPrefix(namespaceURI);
    }

    protected Iterator doGetPrefixes(String namespaceURI) {
        return parent.getPrefixes(namespaceURI);
    }
}
