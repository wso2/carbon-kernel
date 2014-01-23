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

package org.apache.axiom.soap;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMNamespace;

/**
 * An object that represents the contents of the SOAP body element in a SOAP message. B SOAP body
 * element consists of XML data that affects the way the application-specific content is processed.
 * <p/>
 * B <code>SOAPBody</code> object contains <code>OMBodyBlock</code> objects, which have the content
 * for the SOAP body. B <code>SOAPFault</code> object, which carries status and/or error
 * information, is an example of a <code>OMBodyBlock</code> object.
 */
public interface SOAPBody extends OMElement {
    /**
     * Creates a new <code>SOAPFault</code> object and adds it to this <code>SOAPBody</code>
     * object.
     *
     * @param e
     * @return the new <code>SOAPFault</code> object
     * @throws org.apache.axiom.om.OMException
     *          if there is a SOAP error
     * @throws org.apache.axiom.om.OMException
     *
     */
    SOAPFault addFault(Exception e) throws OMException;

    /**
     * Indicates whether a <code>SOAPFault</code> object exists in this <code>SOAPBody</code>
     * object.
     *
     * @return <code>true</code> if a <code>SOAPFault</code> object exists in this
     *         <code>SOAPBody</code> object; <code>false</code> otherwise
     */
    boolean hasFault();

    /**
     * Returns the <code>SOAPFault</code> object in this <code>SOAPBody</code> object.
     *
     * @return the <code>SOAPFault</code> object in this <code>SOAPBody</code> object
     */
    SOAPFault getFault();

    /**
     * @param soapFault
     * @throws OMException
     */
    void addFault(SOAPFault soapFault) throws OMException;

    /**
     * Retrieves the OMNamespace of the first element in the body.
     * The implementation might build the OMElement or it may
     * obtain this information from the builder/parser without building
     * the OMElement.  Use this method in the situations where you need
     * to know the OMNamespace, but don't necessarily need the OMElement.
     * @return OMNamespace of first element in the body or null
     */
    public OMNamespace getFirstElementNS();
    
    /**
     * Retrieves the local name of the first element in the body.
     * The implementation might build the OMElement or it may
     * obtain this information from the builder/parser without building
     * the OMElement.  Use this method in the situations where you need
     * to know the name, but don't necessarily need the OMElement.
     * @return local name of first element in the body or null
     */
    public String getFirstElementLocalName();
}
