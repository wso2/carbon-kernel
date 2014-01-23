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

package org.apache.axiom.om;

import javax.xml.namespace.QName;

/** Interface OMAttribute */
public interface OMAttribute {
    /** @return Returns localName. */
    String getLocalName();

    /** @param localName  */
    void setLocalName(String localName);

    /** @return Returns String. */
    String getAttributeValue();

    /** @param value  */
    void setAttributeValue(String value);

    /** @return Returns String. */
    String getAttributeType();

    /** @param value  */
    void setAttributeType(String value);

    /** @param omNamespace  */
    void setOMNamespace(OMNamespace omNamespace);

    /** @return Returns OMNamespace. */
    OMNamespace getNamespace();

    /** @return Returns javax.xml.namespace.QName */
    QName getQName();

    /** Returns the OMFactory that created this object */
    OMFactory getOMFactory();

    /**
     * Returns the owner element of this attribute
     * @return OMElement - The owner element
     */
    OMElement getOwner();
}
