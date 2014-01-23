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

package org.apache.axis2.saaj;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;

/**
 * Class Prefixed QName
 * <p/>
 * Took this implementation from Axis 1.2 code
 */
public class PrefixedQName implements Name {
    /** comment/shared empty string */
    private static final String EMPTY_STRING = "".intern();

    /** Field prefix */
    private String prefix;
    /** Field qName */
    private QName qName;

    /**
     * Constructor PrefixedQName.
     *
     * @param uri
     * @param localName
     * @param prefix
     */
    public PrefixedQName(String uri, String localName, String prefix) {
        qName = new QName(uri, localName);
        this.prefix = (prefix == null)
                ? EMPTY_STRING
                : prefix.intern();
    }

    /**
     * Constructor PrefixedQName
     *
     * @param qname
     */
    public PrefixedQName(QName qname) {
        this.qName = qname;
        this.prefix = (qname.getPrefix() == null)
                ? EMPTY_STRING
                : qname.getPrefix().intern();
    }

    /**
     * Gets the local name part of the XML name that this <code>Name</code> object represents.
     *
     * @return Returns the local name.
     */
    public String getLocalName() {
        return qName.getLocalPart();
    }

    /**
     * Gets the namespace-qualified name of the XML name that this <code>Name</code> object
     * represents.
     *
     * @return Returns the namespace-qualified name.
     */
    public String getQualifiedName() {
        StringBuffer buf = new StringBuffer(prefix);
        if (!prefix.equals(EMPTY_STRING))
            buf.append(':');
        buf.append(qName.getLocalPart());
        return buf.toString();
    }

    /**
     * Returns the URI of the namespace for the XML name that this <code>Name</code> object
     * represents.
     *
     * @return Returns the URI as a string.
     */
    public String getURI() {
        return qName.getNamespaceURI();
    }

    /**
     * Returns the prefix associated with the namespace for the XML name that this <code>Name</code>
     * object represents.
     *
     * @return Returns the prefix as a string.
     */
    public String getPrefix() {
        return prefix;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof PrefixedQName)) {
            return false;
        }
        if (!qName.equals(((PrefixedQName)obj).qName)) {
            return false;
        }
        return true;
        //Is this correct?
        //return prefix.equals(((PrefixedQName) obj).prefix);
    }

    public int hashCode() {
        return prefix.hashCode() + qName.hashCode();
    }

    public String toString() {
        return qName.toString();
    }
}
