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

package org.apache.axiom.soap.impl.llom.soap12;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axiom.soap.SOAPFaultNode;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axiom.soap.SOAPFaultRole;
import org.apache.axiom.soap.SOAPProcessingException;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.impl.llom.SOAPFaultImpl;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;


public class SOAP12FaultImpl extends SOAPFaultImpl {


    /** Eran Chinthaka (chinthaka@apache.org) */


    public SOAP12FaultImpl(SOAPFactory factory) {
        super(factory.getNamespace(), factory);
    }

    public SOAP12FaultImpl(SOAPBody parent, Exception e, SOAPFactory factory)
            throws SOAPProcessingException {
        super(parent, e, factory);
    }

    public SOAP12FaultImpl(SOAPBody parent, OMXMLParserWrapper builder,
                           SOAPFactory factory) {
        super(parent, builder, factory);
    }

    /**
     * This is a convenience method for the SOAP Fault Impl.
     *
     * @param parent
     */
    public SOAP12FaultImpl(SOAPBody parent, SOAPFactory factory)
            throws SOAPProcessingException {
        super(parent, factory);
    }

    protected SOAPFaultDetail getNewSOAPFaultDetail(SOAPFault fault) {
        return new SOAP12FaultDetailImpl(fault, (SOAPFactory) this.factory);

    }

    public void setCode(SOAPFaultCode soapFaultCode)
            throws SOAPProcessingException {
        if (!(soapFaultCode instanceof SOAP12FaultCodeImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP12FaultCodeImpl, got " + soapFaultCode.getClass());
        }
        super.setCode(soapFaultCode);
    }


    public void setReason(SOAPFaultReason reason) throws SOAPProcessingException {
        if (!(reason instanceof SOAP12FaultReasonImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP12FaultReasonImpl, got " + reason.getClass());
        }
        super.setReason(reason);
    }

    public void setNode(SOAPFaultNode node) throws SOAPProcessingException {
        if (!(node instanceof SOAP12FaultNodeImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP12FaultNodeImpl, got " + node.getClass());
        }
        super.setNode(node);
    }

    public void setRole(SOAPFaultRole role) throws SOAPProcessingException {
        if (!(role instanceof SOAP12FaultRoleImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP12FaultRoleImpl, got " + role.getClass());
        }
        super.setRole(role);
    }

    public void setDetail(SOAPFaultDetail detail) throws SOAPProcessingException {
        if (!(detail instanceof SOAP12FaultDetailImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP12FaultDetailImpl, got " + detail.getClass());
        }
        super.setDetail(detail);
    }

    protected void checkParent(OMElement parent) throws SOAPProcessingException {
        if (!(parent instanceof SOAP12BodyImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP12BodyImpl, got " + parent.getClass());
        }
    }

    protected void serializeFaultNode(
            XMLStreamWriter writer)
            throws XMLStreamException {
        SOAPFaultNode faultNode = getNode();
        if (faultNode != null && faultNode.getText() != null
                && !faultNode.getText().isEmpty()) {
            ((SOAP12FaultNodeImpl) faultNode).internalSerialize(writer, true);
        }
    }

    public SOAPFaultNode getNode() {
        return (SOAPFaultNode) getFirstChildWithName(SOAP12Constants.QNAME_FAULT_NODE);
    }

    public SOAPFaultCode getCode() {
        return (SOAPFaultCode) getFirstChildWithName(SOAP12Constants.QNAME_FAULT_CODE);
    }

    public SOAPFaultReason getReason() {
        return (SOAPFaultReason) getFirstChildWithName(SOAP12Constants.QNAME_FAULT_REASON);
    }

    public SOAPFaultRole getRole() {
        return (SOAPFaultRole) getFirstChildWithName(SOAP12Constants.QNAME_FAULT_ROLE);
    }

    public SOAPFaultDetail getDetail() {
        return (SOAPFaultDetail)getFirstChildWithName(SOAP12Constants.QNAME_FAULT_DETAIL);
    }
}
