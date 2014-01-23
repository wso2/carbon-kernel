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

package org.apache.axiom.soap.impl.llom.soap11;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axiom.soap.SOAPFaultNode;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axiom.soap.SOAPFaultRole;
import org.apache.axiom.soap.SOAPProcessingException;
import org.apache.axiom.soap.impl.llom.SOAPFaultImpl;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;


public class SOAP11FaultImpl extends SOAPFaultImpl {
    /** Eran Chinthaka (chinthaka@apache.org) */


    public SOAP11FaultImpl(SOAPFactory factory) {
        super(factory.getNamespace(), factory);
    }

    public SOAP11FaultImpl(SOAPBody parent, Exception e, SOAPFactory factory)
            throws SOAPProcessingException {
        super(parent, e, factory);
    }

    public SOAP11FaultImpl(SOAPBody parent, OMXMLParserWrapper builder,
                           SOAPFactory factory) {
        super(parent, builder, factory);
    }

    /**
     * This is a convenience method for the SOAP Fault Impl.
     *
     * @param parent
     */
    public SOAP11FaultImpl(SOAPBody parent, SOAPFactory factory)
            throws SOAPProcessingException {
        super(parent, factory);

    }

    protected SOAPFaultDetail getNewSOAPFaultDetail(SOAPFault fault)
            throws SOAPProcessingException {
        return new SOAP11FaultDetailImpl(fault, (SOAPFactory) this.factory);
    }

    public void setCode(SOAPFaultCode soapFaultCode)
            throws SOAPProcessingException {
        if (!(soapFaultCode instanceof SOAP11FaultCodeImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP11FaultCodeImpl, got " + soapFaultCode.getClass());
        }
        super.setCode(soapFaultCode);
    }

    public void setReason(SOAPFaultReason reason) throws SOAPProcessingException {
        if (!(reason instanceof SOAP11FaultReasonImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP11FaultReasonImpl, got " + reason.getClass());
        }
        super.setReason(reason);
    }

    public void setNode(SOAPFaultNode node) throws SOAPProcessingException {
        throw new UnsupportedOperationException("SOAP 1.1 has no SOAP Fault Node");
    }

    public void setRole(SOAPFaultRole role) throws SOAPProcessingException {
        if (!(role instanceof SOAP11FaultRoleImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP11FaultRoleImpl, got " + role.getClass());
        }
        super.setRole(role);
    }

    protected void checkParent(OMElement parent) throws SOAPProcessingException {
        if (!(parent instanceof SOAP11BodyImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP11BodyImpl, got " + parent.getClass());
        }
    }

    public void setDetail(SOAPFaultDetail detail) throws SOAPProcessingException {
        if (!(detail instanceof SOAP11FaultDetailImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP11FaultDetailImpl, got " + detail.getClass());
        }
        super.setDetail(detail);
    }

    protected void serializeFaultNode(XMLStreamWriter writer)
            throws XMLStreamException {

    }

    public SOAPFaultCode getCode() {
        return (SOAPFaultCode) getFirstChildWithName(SOAP11Constants.QNAME_FAULT_CODE);
    }

    public SOAPFaultReason getReason() {
        return (SOAPFaultReason) getFirstChildWithName(SOAP11Constants.QNAME_FAULT_REASON);
    }

    public SOAPFaultNode getNode() {
        return null;
    }

    public SOAPFaultRole getRole() {
        return (SOAPFaultRole) getFirstChildWithName(SOAP11Constants.QNAME_FAULT_ROLE);
    }

    public SOAPFaultDetail getDetail() {
        return (SOAPFaultDetail) getFirstChildWithName(SOAP11Constants.QNAME_FAULT_DETAIL);
    }
}
