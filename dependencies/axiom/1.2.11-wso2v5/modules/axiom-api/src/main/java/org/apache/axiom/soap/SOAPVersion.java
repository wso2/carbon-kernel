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

import javax.xml.namespace.QName;

/**
 * A generic way to get at SOAP-version-specific values.  As long as we can get
 * one of these from a SOAP element, we can get at the right 
 */
public interface SOAPVersion {
    /**
     * Obtain the envelope namespace for this version of SOAP
     */
    String getEnvelopeURI();

    /**
     * Obtain the encoding namespace for this version of SOAP
     */
    String getEncodingURI();

    /**
     * Obtain the QName for the role attribute (actor/role)
     */
    QName getRoleAttributeQName();

    /**
     * Obtain the "next" role/actor URI
     */
    String getNextRoleURI();

    /**
     * Obtain the QName for the MustUnderstand fault code
     */
    QName getMustUnderstandFaultCode();

    /**
     * Obtain the QName for the Sender fault code
     * @return Sender fault code as a QName
     */
    QName getSenderFaultCode();

    /**
     * Obtain the QName for the Receiver fault code.
     * 
     * @return Receiver fault code as a QName
     */
    QName getReceiverFaultCode();

    /**
     * Obtain the QName for the fault reason element.
     * 
     * @return the QName for the fault reason element
     */
    QName getFaultReasonQName();

    /**
     * Obtain the QName for the fault code element.
     * 
     * @return the QName for the fault code element
     */
    QName getFaultCodeQName();

    /**
     * Obtain the QName for the fault detail element.
     * 
     * @return the QName for the fault detail element
     */
    QName getFaultDetailQName();

    /**
     * Obtain the QName for the fault role/actor element.
     * 
     * @return the QName for the fault role/actor element
     */
    QName getFaultRoleQName();
}
