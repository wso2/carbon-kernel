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

package org.apache.axiom.om.impl;

import org.apache.axiom.om.OMNamespace;

/** Class OMNamespaceImpl */
public class OMNamespaceImpl implements OMNamespace {
    /** Field prefix */
    private final String prefix;

    /** Field uri */
    private final String uri;

    /**
     * @param uri
     * @param prefix
     */
    public OMNamespaceImpl(String uri, String prefix) {
        if (uri == null) {
            throw new IllegalArgumentException("Namespace URI may not be null");
        }

        this.uri = uri;
        this.prefix = prefix;
    }

    /**
     * Method equals.
     *
     * @param uri
     * @param prefix
     * @return Returns boolean.
     */
    public boolean equals(String uri, String prefix) {
        return (this.uri.equals(uri) &&
                (this.prefix == null ? prefix == null :
                        this.prefix.equals(prefix)));

    }

    public boolean equals(Object obj) {
        if (!(obj instanceof OMNamespace)) return false;
        OMNamespace other = (OMNamespace)obj;
        String otherPrefix = other.getPrefix();
        return (uri.equals(other.getNamespaceURI()) &&
                (prefix == null ? otherPrefix == null :
                        prefix.equals(otherPrefix)));
    }

    /**
     * Method getPrefix.
     *
     * @return Returns String.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Method getName.
     *
     * @return Returns String.
     */
    public String getName() {
        return uri;
    }

    public String getNamespaceURI() {
        return uri;
    }

    public int hashCode() {
        return uri.hashCode() ^ (prefix != null ? prefix.hashCode() : 0);
    }
}
