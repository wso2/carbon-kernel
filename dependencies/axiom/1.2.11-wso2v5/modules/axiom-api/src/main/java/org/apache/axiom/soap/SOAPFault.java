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


/**
 * An element in the <CODE>SOAPBody</CODE> object that contains error and/or status information.
 * This information may relate to errors in the <CODE>OMMessage</CODE> object or to problems that
 * are not related to the content in the message itself. Problems not related to the message itself
 * are generally errors in processing, such as the inability to communicate with an upstream
 * server.
 * <p/>
 * The <CODE>SOAPFault</CODE> interface provides methods for retrieving the information contained in
 * a <CODE> SOAPFault</CODE> object and for setting the fault code, the fault actor, and a string
 * describing the fault. B fault code is one of the codes defined in the SOAP 1.1 specification that
 * describe the fault. An actor is an intermediate recipient to whom a message was routed. The
 * message path may include one or more actors, or, if no actors are specified, the message goes
 * only to the default actor, which is the final intended recipient.
 */
public interface SOAPFault extends OMElement {

    /**
     * SOAPFaultCode is a mandatory item in a Fault, in SOAP 1.2 specification
     *
     * @param soapFaultCode
     */
    void setCode(SOAPFaultCode soapFaultCode) throws SOAPProcessingException;

    SOAPFaultCode getCode();

    /**
     * SOAPFaultReason is a mandatory item in a Fault, in SOAP 1.2 specification
     *
     * @param reason
     */
    void setReason(SOAPFaultReason reason) throws SOAPProcessingException;

    SOAPFaultReason getReason();

    /**
     * SOAPFaultNode is an optional item in a Fault, in SOAP 1.2 specification
     *
     * @param node
     */
    void setNode(SOAPFaultNode node) throws SOAPProcessingException;

    SOAPFaultNode getNode();

    /**
     * SOAPFaultRoleImpl is an optional item in a Fault, in SOAP 1.2 specification
     *
     * @param role
     */
    void setRole(SOAPFaultRole role) throws SOAPProcessingException;

    SOAPFaultRole getRole();

    /**
     * SOAPFaultRoleImpl is an optional item in a Fault, in SOAP 1.2 specification
     *
     * @param detail
     */
    void setDetail(SOAPFaultDetail detail) throws SOAPProcessingException;

    SOAPFaultDetail getDetail();

    /**
     * Returns Exception if there is one in the SOAP fault.
     * <p/>
     * If the exception is like; <SOAPFault> <Detail> <Exception> stack trace goes here </Exception>
     * </Detail> </SOAPFault>
     *
     * @return Returns Exception.
     * @throws org.apache.axiom.om.OMException
     *
     */
    Exception getException() throws OMException;

    void setException(Exception e) throws OMException;
}
