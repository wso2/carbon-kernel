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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;

/**
 * Namespace context implementation that stores namespace bindings in a {@link Map}.
 */
public class MapBasedNamespaceContext extends AbstractNamespaceContext {
    protected Map namespaces;

    public MapBasedNamespaceContext(Map map) {
        namespaces = map;
    }

    protected String doGetNamespaceURI(String prefix) {
        String namespaceURI = (String)namespaces.get(prefix);
        return namespaceURI == null ? XMLConstants.NULL_NS_URI : namespaceURI;
    }

    protected String doGetPrefix(String nsURI) {
        Iterator iter = namespaces.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String uri = (String) entry.getValue();
            if (uri.equals(nsURI)) {
                return (String) entry.getKey();
            }
        }
        if (nsURI.isEmpty()) {
            return "";
        }
        return null;
    }

    protected Iterator doGetPrefixes(String nsURI) {
        Set prefixes = null;
        Iterator iter = namespaces.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String uri = (String) entry.getValue();
            if (uri.equals(nsURI)) {
                if (prefixes == null) {
                    prefixes = new HashSet();
                }
                prefixes.add(entry.getKey());
            }
        }
        if (prefixes != null) {
            return Collections.unmodifiableSet(prefixes).iterator();
        } else if (nsURI.isEmpty()) {
            return Collections.singleton("").iterator();
        } else {
            return Collections.EMPTY_LIST.iterator();
        }
    }
}