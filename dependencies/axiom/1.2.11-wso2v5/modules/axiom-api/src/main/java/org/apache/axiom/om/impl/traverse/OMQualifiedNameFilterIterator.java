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

package org.apache.axiom.om.impl.traverse;

import java.util.Iterator;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;

/**
 * Iterator that selects elements based on prefix and local name.
 * Note that namespace URIs are not taken into account by the filter.
 */
public class OMQualifiedNameFilterIterator extends OMFilterIterator {
    private final String prefix;
    private final String localName;

    public OMQualifiedNameFilterIterator(Iterator parent, String qualifiedName) {
        super(parent);
        int idx = qualifiedName.indexOf(':');
        if (idx == -1) {
            prefix = null;
            localName = qualifiedName;
        } else {
            prefix = qualifiedName.substring(0, idx);
            localName = qualifiedName.substring(idx+1);
        }
    }

    protected boolean matches(OMNode node) {
        if (node.getType() == OMNode.ELEMENT_NODE) {
            OMElement element = (OMElement)node;
            if (!localName.equals(element.getLocalName())) {
                return false;
            } else {
                OMNamespace ns = ((OMElement)node).getNamespace();
                if (prefix == null) {
                    return ns == null || ns.getPrefix().isEmpty();
                } else {
                    return ns != null && prefix.equals(ns.getPrefix());
                }
            }
        } else {
            return false;
        }
    }
}
