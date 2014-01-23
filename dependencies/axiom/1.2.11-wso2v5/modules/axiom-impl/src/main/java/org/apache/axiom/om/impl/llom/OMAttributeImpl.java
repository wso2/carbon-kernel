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

package org.apache.axiom.om.impl.llom;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMConstants;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMElement;

import javax.xml.namespace.QName;

/** Class OMAttributeImpl */
public class OMAttributeImpl implements OMAttribute {
    /** Field localName */
    private String localName;

    /** Field value */
    private String value;

    /** Field type */
    private String type;

    /** Field namespace */
    private OMNamespace namespace;
    
    private QName qName;

    /** <code>OMFactory</code> that created this <code>OMAttribute</code> */
    private OMFactory factory;

    // Keep track of the owner of the attribute
    protected OMElement owner;

    /**
     * Constructor OMAttributeImpl.
     *
     * @param localName
     * @param ns
     * @param value
     */
    public OMAttributeImpl(String localName, OMNamespace ns, String value, OMFactory factory) 
    {
        if (localName == null || localName.trim().isEmpty())
            throw new IllegalArgumentException("Local name may not be null or empty");

        this.localName = localName;
        this.value = value;
        this.namespace = ns;
        this.type = OMConstants.XMLATTRTYPE_CDATA;
        this.factory = factory;
    }

    /** @return Returns QName. */
    public QName getQName() {
        if (qName != null) {
            return qName;
        }

        if (namespace != null) {
            // Guard against QName implementation sillyness.
            if (namespace.getPrefix() == null) {
                this.qName = new QName(namespace.getNamespaceURI(), localName);
            } else {
                this.qName =  new QName(namespace.getNamespaceURI(), localName, namespace.getPrefix());
            }
        } else {
            this.qName =  new QName(localName);
        }
        return this.qName;
    }

    // -------- Getters and Setters

    /**
     * Method getLocalName.
     *
     * @return Returns local name.
     */
    public String getLocalName() {
        return localName;
    }

    /**
     * Method setLocalName.
     *
     * @param localName
     */
    public void setLocalName(String localName) {
        if (localName == null || localName.trim().isEmpty())
            throw new IllegalArgumentException("Local name may not be null or empty");
        this.localName = localName;
        this.qName = null;
    }

    /**
     * Method getAttributeValue.
     *
     * @return Returns value.
     */
    public String getAttributeValue() {
        return value;
    }

    /**
     * Method setAttributeValue.
     *
     * @param value
     */
    public void setAttributeValue(String value) {
        this.value = value;
    }

    /**
     * Method getAttributeType.
     *
     * @return Returns type.
     */
    public String getAttributeType() {
    	return type;
    }

    /**
     * Method setAttributeType.
     *
     * @param type
     */
    public void setAttributeType(String type) {
        this.type = type;
    }

    /**
     * Method setOMNamespace.
     *
     * @param omNamespace
     */
    public void setOMNamespace(OMNamespace omNamespace) {
        this.namespace = omNamespace;
        this.qName = null;
    }

    /**
     * Method getNamespace.
     *
     * @return Returns namespace.
     */
    public OMNamespace getNamespace() {
        return namespace;
    }

    public OMFactory getOMFactory() {
        return this.factory;
    }

    /**
     * Returns the owner element of this attribute
     * @return OMElement - the owner element
     */
    public OMElement getOwner() {
        return owner;
    }

    /**
     * Checks for the equality of two <code>OMAttribute</code> instances. Thus the object to compare
     * this with may be an instance of <code>OMAttributeImpl</code> (an instance of this class) or
     * an instance of <code>AttrImpl</code>. The method returns false for any object of type other
     * than <code>OMAttribute</code>.
     *
     * <p>We check for the equality of namespaces first (note that if the namespace of this instance is null
     * then for the <code>obj</code> to be equal its namespace must also be null). This condition solely
     * doesn't determine the equality. So we check for the equality of names and values (note that the value
     * can also be null in which case the same argument holds as that for the namespace) of the two instances.
     * If all three conditions are met then we say the two instances are equal.
     *
     * Note: We ignore the owner when checking for the equality. This is simply because the owner is
     * introduced just to keep things simple for the programmer and not as part of an attribute itself.
     *
     * @param obj The object to compare with this instance.
     * @return True if obj is equal to this or else false.
     */
    public boolean equals(Object obj) {
        if (! (obj instanceof OMAttribute)) return false;
        OMAttribute other = (OMAttribute)obj;
        //first check namespace then localName then value to improve performance
        return (namespace == null ? other.getNamespace() == null :
                namespace.equals(other.getNamespace()) &&
                localName.equals(other.getLocalName()) &&
                (value == null ? other.getAttributeValue() == null :
                        value.equals(other.getAttributeValue())));

    }

    public int hashCode() {
        return localName.hashCode() ^ (value != null ? value.hashCode() : 0) ^
                (namespace != null ? namespace.hashCode() : 0);
    }

}
