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

import org.apache.axiom.om.OMNode;

import javax.xml.namespace.QName;

/**
 * Iterate over elements with the same namespace uri
 *
 */
public class OMChildrenNamespaceIterator extends OMChildrenQNameIterator {

    public OMChildrenNamespaceIterator(OMNode currentChild, String uri) {
        super(currentChild, new QName(uri, "dummyName"));
    }

    /**
     * This version of equals returns true if the local parts match.
     * 
     * @param searchQName
     * @param currentQName
     * @return true if equals
     */
    public boolean isEqual(QName searchQName, QName currentQName) {
        return searchQName.getNamespaceURI().equals(searchQName.getNamespaceURI());
    }
}
