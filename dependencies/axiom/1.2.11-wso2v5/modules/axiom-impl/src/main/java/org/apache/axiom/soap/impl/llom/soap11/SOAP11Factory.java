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

import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.impl.OMNamespaceImpl;
import org.apache.axiom.om.impl.llom.factory.OMLinkedListImplFactory;
import org.apache.axiom.om.impl.llom.factory.OMLinkedListMetaFactory;
import org.apache.axiom.soap.SOAP11Constants;
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
import org.apache.axiom.soap.SOAP11Version;
import org.apache.axiom.soap.impl.llom.SOAPEnvelopeImpl;
import org.apache.axiom.soap.impl.llom.SOAPMessageImpl;

/**
 */
public class SOAP11Factory extends OMLinkedListImplFactory implements SOAPFactory {
    public SOAP11Factory(OMLinkedListMetaFactory metaFactory) {
        super(metaFactory);
    }

    public SOAP11Factory() {
    }

    public OMNamespace getNamespace() {
        return new OMNamespaceImpl(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                                   SOAP11Constants.SOAP_DEFAULT_NAMESPACE_PREFIX);
    }

    public String getSoapVersionURI() {
        return SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
    }

    public SOAPVersion getSOAPVersion() {
        return SOAP11Version.getSingleton();
    }

    public SOAPEnvelope createSOAPEnvelope() {
        return new SOAPEnvelopeImpl(
                new OMNamespaceImpl(
                        SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                        SOAP11Constants.SOAP_DEFAULT_NAMESPACE_PREFIX),
                this);
    }
    
    public SOAPEnvelope createSOAPEnvelope(OMNamespace ns) {
        return new SOAPEnvelopeImpl(ns,
                                    this);
    }

    public SOAPHeader createSOAPHeader(SOAPEnvelope envelope)
            throws SOAPProcessingException {
        return new SOAP11HeaderImpl(envelope, this);
    }

    public SOAPHeader createSOAPHeader() throws SOAPProcessingException {
        return new SOAP11HeaderImpl(this);
    }

    public SOAPHeader createSOAPHeader(SOAPEnvelope envelope,
                                       OMXMLParserWrapper builder) {
        return new SOAP11HeaderImpl(envelope, builder, this);
    }

    public SOAPHeaderBlock createSOAPHeaderBlock(String localName,
                                                 OMNamespace ns, SOAPHeader parent)
            throws SOAPProcessingException {
        return new SOAP11HeaderBlockImpl(localName, ns, parent, this);
    }

    public SOAPHeaderBlock createSOAPHeaderBlock(String localName,
                                                 OMNamespace ns) throws SOAPProcessingException {
        return new SOAP11HeaderBlockImpl(localName, ns, this);
    }

    public SOAPHeaderBlock createSOAPHeaderBlock(String localName,
                                                 OMNamespace ns,
                                                 OMDataSource ds) 
        throws SOAPProcessingException {
        return new SOAP11HeaderBlockImpl(localName, ns, this, ds);
    }
    public SOAPHeaderBlock createSOAPHeaderBlock(String localName,
                                                 OMNamespace ns,
                                                 SOAPHeader parent,
                                                 OMXMLParserWrapper builder)
            throws SOAPProcessingException {
        return new SOAP11HeaderBlockImpl(localName, ns, parent, builder, this);
    }

    public SOAPFault createSOAPFault(SOAPBody parent, Exception e)
            throws SOAPProcessingException {
        return new SOAP11FaultImpl(parent, e, this);
    }

    public SOAPFault createSOAPFault() throws SOAPProcessingException {
        return new SOAP11FaultImpl(this);
    }

    public SOAPFault createSOAPFault(SOAPBody parent)
            throws SOAPProcessingException {
        return new SOAP11FaultImpl(parent, this);
    }

    public SOAPFault createSOAPFault(SOAPBody parent,
                                     OMXMLParserWrapper builder) {
        return new SOAP11FaultImpl(parent, builder, this);
    }

    public SOAPBody createSOAPBody(SOAPEnvelope envelope)
            throws SOAPProcessingException {
        return new SOAP11BodyImpl(envelope, this);
    }

    public SOAPBody createSOAPBody(SOAPEnvelope envelope,
                                   OMXMLParserWrapper builder) {
        return new SOAP11BodyImpl(envelope, builder, this);
    }

    public SOAPBody createSOAPBody() throws SOAPProcessingException {
        return new SOAP11BodyImpl(this);
    }

    public SOAPFaultCode createSOAPFaultCode(SOAPFault parent)
            throws SOAPProcessingException {
        return new SOAP11FaultCodeImpl(parent, this);
    }

    public SOAPFaultCode createSOAPFaultCode() throws SOAPProcessingException {
        return new SOAP11FaultCodeImpl(this);
    }

    public SOAPFaultCode createSOAPFaultCode(SOAPFault parent,
                                             OMXMLParserWrapper builder) {
        return new SOAP11FaultCodeImpl(parent, builder, this);
    }

    public SOAPFaultValue createSOAPFaultValue(SOAPFaultCode parent)
            throws SOAPProcessingException {
        return new SOAP11FaultValueImpl(parent, this);
    }

    public SOAPFaultValue createSOAPFaultValue() throws SOAPProcessingException {
        return new SOAP11FaultValueImpl(this);
    }

    public SOAPFaultValue createSOAPFaultValue(SOAPFaultCode parent,
                                               OMXMLParserWrapper builder) {
        return new SOAP11FaultValueImpl(parent, builder, this);
    }

    //added
    public SOAPFaultValue createSOAPFaultValue(SOAPFaultSubCode parent)
            throws SOAPProcessingException {
        return new SOAP11FaultValueImpl(parent, this);
    }

    //added
    public SOAPFaultValue createSOAPFaultValue(SOAPFaultSubCode parent,
                                               OMXMLParserWrapper builder) {
        return new SOAP11FaultValueImpl(parent, builder, this);
    }

    //changed
    public SOAPFaultSubCode createSOAPFaultSubCode(SOAPFaultCode parent)
            throws SOAPProcessingException {
        return new SOAP11FaultSubCodeImpl(parent, this);
    }

    public SOAPFaultSubCode createSOAPFaultSubCode() throws SOAPProcessingException {
        return new SOAP11FaultSubCodeImpl(this);
    }

    //changed
    public SOAPFaultSubCode createSOAPFaultSubCode(SOAPFaultCode parent,
                                                   OMXMLParserWrapper builder) {
        return new SOAP11FaultSubCodeImpl(parent, builder, this);
    }

    public SOAPFaultSubCode createSOAPFaultSubCode(SOAPFaultSubCode parent)
            throws SOAPProcessingException {
        return new SOAP11FaultSubCodeImpl(parent, this);
    }

    public SOAPFaultSubCode createSOAPFaultSubCode(SOAPFaultSubCode parent,
                                                   OMXMLParserWrapper builder) {
        return new SOAP11FaultSubCodeImpl(parent, builder, this);
    }

    public SOAPFaultReason createSOAPFaultReason(SOAPFault parent)
            throws SOAPProcessingException {
        return new SOAP11FaultReasonImpl(parent, this);
    }

    public SOAPFaultReason createSOAPFaultReason() throws SOAPProcessingException {
        return new SOAP11FaultReasonImpl(this);
    }

    public SOAPFaultReason createSOAPFaultReason(SOAPFault parent,
                                                 OMXMLParserWrapper builder) {
        return new SOAP11FaultReasonImpl(parent, builder, this);
    }

    public SOAPFaultText createSOAPFaultText(SOAPFaultReason parent)
            throws SOAPProcessingException {
        return new SOAP11FaultTextImpl(parent, this);
    }

    public SOAPFaultText createSOAPFaultText() throws SOAPProcessingException {
        return new SOAP11FaultTextImpl(this);
    }

    public SOAPFaultText createSOAPFaultText(SOAPFaultReason parent,
                                             OMXMLParserWrapper builder) {
        return new SOAP11FaultTextImpl(parent, builder, this);
    }

    public SOAPFaultNode createSOAPFaultNode(SOAPFault parent)
            throws SOAPProcessingException {
        throw new UnsupportedOperationException("SOAP 1.1 has no SOAP Fault Node");
    }

    public SOAPFaultNode createSOAPFaultNode() throws SOAPProcessingException {
        throw new UnsupportedOperationException("SOAP 1.1 has no SOAP Fault Node");
    }

    public SOAPFaultNode createSOAPFaultNode(SOAPFault parent,
                                             OMXMLParserWrapper builder) {
        throw new UnsupportedOperationException("SOAP 1.1 has no SOAP Fault Node");
    }

    public SOAPFaultRole createSOAPFaultRole(SOAPFault parent)
            throws SOAPProcessingException {
        return new SOAP11FaultRoleImpl(parent, this);
    }

    public SOAPFaultRole createSOAPFaultRole() throws SOAPProcessingException {
        return new SOAP11FaultRoleImpl(this);
    }

    public SOAPFaultRole createSOAPFaultRole(SOAPFault parent,
                                             OMXMLParserWrapper builder) {
        return new SOAP11FaultRoleImpl(parent, builder, this);
    }

    public SOAPFaultDetail createSOAPFaultDetail(SOAPFault parent)
            throws SOAPProcessingException {
        return new SOAP11FaultDetailImpl(parent, this);
    }

    public SOAPFaultDetail createSOAPFaultDetail() throws SOAPProcessingException {
        return new SOAP11FaultDetailImpl(this);
    }

    public SOAPFaultDetail createSOAPFaultDetail(SOAPFault parent,
                                                 OMXMLParserWrapper builder) {
        return new SOAP11FaultDetailImpl(parent, builder, this);
    }

    public SOAPEnvelope getDefaultEnvelope() throws SOAPProcessingException {
        OMNamespace ns =
                new OMNamespaceImpl(
                        SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                        SOAP11Constants.SOAP_DEFAULT_NAMESPACE_PREFIX);
        SOAPEnvelopeImpl env = new SOAPEnvelopeImpl(ns, this);
        createSOAPHeader(env);
        createSOAPBody(env);
        return env;
    }

    public SOAPEnvelope getDefaultFaultEnvelope() throws SOAPProcessingException {
        SOAPEnvelope defaultEnvelope = getDefaultEnvelope();
        SOAPFault fault = createSOAPFault(defaultEnvelope.getBody());

        SOAPFaultCode faultCode = createSOAPFaultCode(fault);

        SOAPFaultReason reason = createSOAPFaultReason(fault);
        //createSOAPFaultText(reason);

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
