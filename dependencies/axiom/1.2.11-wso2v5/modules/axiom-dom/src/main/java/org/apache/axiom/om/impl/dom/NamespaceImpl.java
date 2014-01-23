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

package org.apache.axiom.om.impl.dom;

import org.apache.axiom.om.OMNamespace;

public class NamespaceImpl implements OMNamespace {

    private final String nsUri;

    private final String nsPrefix;

    public NamespaceImpl(String uri) {
        this(uri, null);
    }

    public NamespaceImpl(String uri, String prefix) {
        if (uri == null) {
            throw new IllegalArgumentException("Namespace URI may not be null");
        }
        this.nsUri = uri;
        this.nsPrefix = prefix;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.axiom.om.OMNamespace#equals(java.lang.String,
     *      java.lang.String)
     */
    public boolean equals(String uri, String prefix) {
        return (nsUri.equals(uri) &&
                (nsPrefix == null ? prefix == null :
                        nsPrefix.equals(prefix)));
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof OMNamespace)) return false;
        OMNamespace other = (OMNamespace)obj;
        String otherPrefix = other.getPrefix();
        return (nsUri.equals(other.getNamespaceURI()) &&
                (nsPrefix == null ? otherPrefix == null :
                        nsPrefix.equals(otherPrefix)));
    }

    /*
    * (non-Javadoc)
    *
    * @see org.apache.axiom.om.OMNamespace#getPrefix()
    */
    public String getPrefix() {
        return this.nsPrefix;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.axiom.om.OMNamespace#getName()
     */
    public String getName() {
        return this.nsUri;
    }

    public String getNamespaceURI() {
        return this.nsUri;
    }

    public int hashCode() {
        return nsUri.hashCode() ^ (nsPrefix != null ? nsPrefix.hashCode() : 0);
    }
}
