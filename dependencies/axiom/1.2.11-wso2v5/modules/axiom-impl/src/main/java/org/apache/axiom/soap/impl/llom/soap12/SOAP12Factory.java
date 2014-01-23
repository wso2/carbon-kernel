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

import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.impl.OMNamespaceImpl;
import org.apache.axiom.om.impl.llom.factory.OMLinkedListImplFactory;
import org.apache.axiom.om.impl.llom.factory.OMLinkedListMetaFactory;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axiom.soap.SOAPFaultNode;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axiom.soap.SOAPFaultRole;
import org.apache.axiom.soap.SOAPFaultSubCode;
import org.apache.axiom.soap.SOAPFaultText;
import org.apache.axiom.soap.SOAPFaultValue;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axiom.soap.SOAPMessage;
import org.apache.axiom.soap.SOAPProcessingException;
import org.apache.axiom.soap.SOAPVersion;
import org.apache.axiom.soap.SOAP12Version;
import org.apache.axiom.soap.impl.llom.SOAPEnvelopeImpl;
import org.apache.axiom.soap.impl.llom.SOAPMessageImpl;

/**
 */
public class SOAP12Factory extends OMLinkedListImplFactory implements SOAPFactory {
    public SOAP12Factory(OMLinkedListMetaFactory metaFactory) {
        super(metaFactory);
    }

    public SOAP12Factory() {
    }

    public String getSoapVersionURI() {
        return SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI;
    }

    public SOAPVersion getSOAPVersion() {
        return SOAP12Version.getSingleton();
    }    

    public OMNamespace getNamespace() {
        return new OMNamespaceImpl(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                                   SOAP12Constants.SOAP_DEFAULT_NAMESPACE_PREFIX);
    }

    public SOAPEnvelope createSOAPEnvelope() {
        return new SOAPEnvelopeImpl(
                new OMNamespaceImpl(
                        SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                        SOAP12Constants.SOAP_DEFAULT_NAMESPACE_PREFIX),
                this);
    }
    
    public SOAPEnvelope createSOAPEnvelope(OMNamespace ns) {
        return new SOAPEnvelopeImpl(ns,
                                    this);
    }

    public SOAPHeader createSOAPHeader(SOAPEnvelope envelope) throws SOAPProcessingException {
        return new SOAP12HeaderImpl(envelope, this);
    }

    public SOAPHeader createSOAPHeader() throws SOAPProcessingException {
        return new SOAP12HeaderImpl(this);
    }

    public SOAPHeader createSOAPHeader(SOAPEnvelope envelope,
                                       OMXMLParserWrapper builder) {
        return new SOAP12HeaderImpl(envelope, builder, this);
    }

    public SOAPHeaderBlock createSOAPHeaderBlock(String localName,
                                                 OMNamespace ns, SOAPHeader parent)
            throws SOAPProcessingException {
        return new SOAP12HeaderBlockImpl(localName, ns, parent, this);
    }

    public SOAPHeaderBlock createSOAPHeaderBlock(String localName,
                                                 OMNamespace ns, SOAPHeader parent,
                                                 OMXMLParserWrapper builder)
            throws SOAPProcessingException {
        return new SOAP12HeaderBlockImpl(localName, ns, parent, builder, this);
    }

    public SOAPFault createSOAPFault(SOAPBody parent, Exception e)
            throws SOAPProcessingException {
        return new SOAP12FaultImpl(parent, e, this);
    }

    public SOAPFault createSOAPFault(SOAPBody parent)
            throws SOAPProcessingException {
        return new SOAP12FaultImpl(parent, this);
    }

    public SOAPFault createSOAPFault() throws SOAPProcessingException {
        return new SOAP12FaultImpl(this);
    }

    public SOAPFault createSOAPFault(SOAPBody parent,
                                     OMXMLParserWrapper builder) {
        return new SOAP12FaultImpl(parent, builder, this);
    }

    public SOAPBody createSOAPBody(SOAPEnvelope envelope)
            throws SOAPProcessingException {
        return new SOAP12BodyImpl(envelope, this);
    }

    public SOAPBody createSOAPBody() throws SOAPProcessingException {
        return new SOAP12BodyImpl(this);
    }

    public SOAPBody createSOAPBody(SOAPEnvelope envelope,
                                   OMXMLParserWrapper builder) {
        return new SOAP12BodyImpl(envelope, builder, this);
    }

    public SOAPFaultCode createSOAPFaultCode(SOAPFault parent)
            throws SOAPProcessingException {
        return new SOAP12FaultCodeImpl(parent, this);
    }

    public SOAPFaultCode createSOAPFaultCode() throws SOAPProcessingException {
        return new SOAP12FaultCodeImpl(this);
    }

    public SOAPFaultCode createSOAPFaultCode(SOAPFault parent,
                                             OMXMLParserWrapper builder) {
        return new SOAP12FaultCodeImpl(parent, builder, this);
    }

    public SOAPFaultValue createSOAPFaultValue(SOAPFaultCode parent)
            throws SOAPProcessingException {
        return new SOAP12FaultValueImpl(parent, this);
    }

    public SOAPFaultValue createSOAPFaultValue() throws SOAPProcessingException {
        return new SOAP12FaultValueImpl(this);
    }

    public SOAPFaultValue createSOAPFaultValue(SOAPFaultCode parent,
                                               OMXMLParserWrapper builder) {
        return new SOAP12FaultValueImpl(parent, builder, this);
    }

    public SOAPHeaderBlock createSOAPHeaderBlock(String localName,
                                                 OMNamespace ns) throws SOAPProcessingException {
        return new SOAP12HeaderBlockImpl(localName, ns, this);
    }
    
    public SOAPHeaderBlock createSOAPHeaderBlock(String localName,
                                                 OMNamespace ns,
                                                 OMDataSource ds) 
        throws SOAPProcessingException {
        return new SOAP12HeaderBlockImpl(localName, ns, this, ds);
    }

    //added
    public SOAPFaultValue createSOAPFaultValue(SOAPFaultSubCode parent)
            throws SOAPProcessingException {
        return new SOAP12FaultValueImpl(parent, this);
    }

    //added
    public SOAPFaultValue createSOAPFaultValue(SOAPFaultSubCode parent,
                                               OMXMLParserWrapper builder) {
        return new SOAP12FaultValueImpl(parent, builder, this);
    }

    //changed
    public SOAPFaultSubCode createSOAPFaultSubCode(SOAPFaultCode parent)
            throws SOAPProcessingException {
        return new SOAP12FaultSubCodeImpl(parent, this);
    }

    public SOAPFaultSubCode createSOAPFaultSubCode()
            throws SOAPProcessingException {
        return new SOAP12FaultSubCodeImpl(this);
    }

    //changed
    public SOAPFaultSubCode createSOAPFaultSubCode(SOAPFaultCode parent,
                                                   OMXMLParserWrapper builder) {
        return new SOAP12FaultSubCodeImpl(parent, builder, this);
    }

    public SOAPFaultSubCode createSOAPFaultSubCode(SOAPFaultSubCode parent)
            throws SOAPProcessingException {
        return new SOAP12FaultSubCodeImpl(parent, this);
    }

    public SOAPFaultSubCode createSOAPFaultSubCode(SOAPFaultSubCode parent,
                                                   OMXMLParserWrapper builder) {
        return new SOAP12FaultSubCodeImpl(parent, builder, this);
    }

    public SOAPFaultReason createSOAPFaultReason(SOAPFault parent)
            throws SOAPProcessingException {
        return new SOAP12FaultReasonImpl(parent, this);
    }

    public SOAPFaultReason createSOAPFaultReason() throws SOAPProcessingException {
        return new SOAP12FaultReasonImpl(this);
    }

    public SOAPFaultReason createSOAPFaultReason(SOAPFault parent,
                                                 OMXMLParserWrapper builder) {
        return new SOAP12FaultReasonImpl(parent, builder, this);
    }

    public SOAPFaultText createSOAPFaultText(SOAPFaultReason parent)
            throws SOAPProcessingException {
        return new SOAP12FaultTextImpl(parent, this);
    }

    public SOAPFaultText createSOAPFaultText() throws SOAPProcessingException {
        return new SOAP12FaultTextImpl(this);
    }

    public SOAPFaultText createSOAPFaultText(SOAPFaultReason parent,
                                             OMXMLParserWrapper builder) {
        return new SOAP12FaultTextImpl(parent, builder, this);
    }

    public SOAPFaultNode createSOAPFaultNode(SOAPFault parent)
            throws SOAPProcessingException {
        return new SOAP12FaultNodeImpl(parent, this);
    }

    public SOAPFaultNode createSOAPFaultNode() throws SOAPProcessingException {
        return new SOAP12FaultNodeImpl(this);
    }

    public SOAPFaultNode createSOAPFaultNode(SOAPFault parent,
                                             OMXMLParserWrapper builder) {
        return new SOAP12FaultNodeImpl(parent, builder, this);
    }

    public SOAPFaultRole createSOAPFaultRole(SOAPFault parent)
            throws SOAPProcessingException {
        return new SOAP12FaultRoleImpl(parent, this);
    }

    public SOAPFaultRole createSOAPFaultRole() throws SOAPProcessingException {
        return new SOAP12FaultRoleImpl(this);
    }

    public SOAPFaultRole createSOAPFaultRole(SOAPFault parent,
                                             OMXMLParserWrapper builder) {
        return new SOAP12FaultRoleImpl(parent, builder, this);
    }

    public SOAPFaultDetail createSOAPFaultDetail(SOAPFault parent)
            throws SOAPProcessingException {
        return new SOAP12FaultDetailImpl(parent, this);
    }

    public SOAPFaultDetail createSOAPFaultDetail()
            throws SOAPProcessingException {
        return new SOAP12FaultDetailImpl(this);
    }

    public SOAPFaultDetail createSOAPFaultDetail(SOAPFault parent,
                                                 OMXMLParserWrapper builder) {
        return new SOAP12FaultDetailImpl(parent, builder, this);
    }

    public SOAPEnvelope getDefaultEnvelope() throws SOAPProcessingException {
        OMNamespace ns =
                new OMNamespaceImpl(
                        SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                        SOAP12Constants.SOAP_DEFAULT_NAMESPACE_PREFIX);
        SOAPEnvelopeImpl env = new SOAPEnvelopeImpl(ns, this);
        createSOAPHeader(env);
        createSOAPBody(env);

        return env;
    }

    public SOAPEnvelope getDefaultFaultEnvelope() throws SOAPProcessingException {
        SOAPEnvelope defaultEnvelope = getDefaultEnvelope();
        SOAPFault fault = createSOAPFault(defaultEnvelope.getBody());

        SOAPFaultCode faultCode = createSOAPFaultCode(fault);
        createSOAPFaultValue(faultCode);

        SOAPFaultReason reason = createSOAPFaultReason(fault);
        createSOAPFaultText(reason);

        createSOAPFaultDetail(fault);

        return defaultEnvelope;
    }

    public SOAPMessage createSOAPMessage() {
        return new SOAPMessageImpl(this);
    }

    public SOAPMessage createSOAPMessage(OMXMLParserWrapper builder) {
        return new SOAPMessageImpl(builder, this);
    }


    public SOAPMessage createSOAPMessage(SOAPEnvelope envelope, OMXMLParserWrapper parserWrapper) {
        return new SOAPMessageImpl(envelope, parserWrapper, this);
    }


    public SOAPEnvelope createSOAPEnvelope(OMXMLParserWrapper builder) {
        return new SOAPEnvelopeImpl(builder, this);
    }


}
