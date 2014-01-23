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

import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMXMLParserWrapper;


public interface SOAPFactory extends OMFactory {

    String getSoapVersionURI();

    SOAPVersion getSOAPVersion();

    /** Eran Chinthaka (chinthaka@apache.org) */

    SOAPMessage createSOAPMessage();

    SOAPMessage createSOAPMessage(OMXMLParserWrapper builder);

    SOAPMessage createSOAPMessage(SOAPEnvelope envelope, OMXMLParserWrapper parserWrapper);

    /**
     * Create a SOAP envelope. The returned element will have the namespace URI specified by the
     * SOAP version that this factory represents. It will have the prefix given by
     * {@link SOAPConstants#SOAP_DEFAULT_NAMESPACE_PREFIX}. It will also have a corresponding
     * namespace declaration.
     * 
     * @return the SOAP envelope
     */
    // TODO: when does this thing throw a SOAPProcessingException??
    SOAPEnvelope createSOAPEnvelope() throws SOAPProcessingException;
    
    SOAPEnvelope createSOAPEnvelope(OMNamespace ns); 

    SOAPEnvelope createSOAPEnvelope(OMXMLParserWrapper builder);

    /**
     * @param envelope
     * @return Returns SOAPHeader.
     */
    SOAPHeader createSOAPHeader(SOAPEnvelope envelope) throws SOAPProcessingException;

    SOAPHeader createSOAPHeader() throws SOAPProcessingException;

    /**
     * @param envelope
     * @param builder
     * @return Returns SOAPHeader.
     */
    SOAPHeader createSOAPHeader(SOAPEnvelope envelope,
                                       OMXMLParserWrapper builder);

    /**
     * @param localName
     * @param ns
     * @return Returns SOAPHeaderBlock.
     */
    SOAPHeaderBlock createSOAPHeaderBlock(String localName,
                                                 OMNamespace ns,
                                                 SOAPHeader parent) throws SOAPProcessingException;

    SOAPHeaderBlock createSOAPHeaderBlock(String localName,
                                                 OMNamespace ns) throws SOAPProcessingException;
    
    /**
     * Create SOAPHeaderBlock that has an OMDataSource
     * @param localName
     * @param ns
     * @param ds
     * @return SOAPHeaderBlock
     * @throws SOAPProcessingException
     */
    SOAPHeaderBlock createSOAPHeaderBlock(String localName,
                                          OMNamespace ns,
                                          OMDataSource ds) throws SOAPProcessingException;

    /**
     * @param localName
     * @param ns
     * @param parent
     * @param builder
     * @return Returns SOAPHeaderBlock.
     */
    SOAPHeaderBlock createSOAPHeaderBlock(String localName,
                                                 OMNamespace ns,
                                                 SOAPHeader parent,
                                                 OMXMLParserWrapper builder)
            throws SOAPProcessingException;

    /**
     * @param parent
     * @param e
     * @return Returns SOAPFault.
     */
    SOAPFault createSOAPFault(SOAPBody parent, Exception e) throws SOAPProcessingException;

    SOAPFault createSOAPFault(SOAPBody parent) throws SOAPProcessingException;

    SOAPFault createSOAPFault() throws SOAPProcessingException;

    /**
     * @param parent
     * @param builder
     * @return Returns SOAPFault.
     */
    SOAPFault createSOAPFault(SOAPBody parent,
                                     OMXMLParserWrapper builder);

    /**
     * @param envelope
     * @return Returns SOAPBody.
     */
    SOAPBody createSOAPBody(SOAPEnvelope envelope) throws SOAPProcessingException;

    SOAPBody createSOAPBody() throws SOAPProcessingException;

    /**
     * @param envelope
     * @param builder
     * @return Returns SOAPBody.
     */
    SOAPBody createSOAPBody(SOAPEnvelope envelope,
                                   OMXMLParserWrapper builder);

    /* ========================
       =  SOAPFaultCode       =
       ======================== */

    /**
     * Code eii under SOAPFault (parent)
     *
     * @param parent
     * @return Returns SOAPFaultCode.
     */
    SOAPFaultCode createSOAPFaultCode(SOAPFault parent) throws SOAPProcessingException;

    SOAPFaultCode createSOAPFaultCode() throws SOAPProcessingException;

    /**
     * Code eii under SOAPFault (parent)
     *
     * @param parent
     * @param builder
     * @return Returns SOAPFaultCode.
     */
    SOAPFaultCode createSOAPFaultCode(SOAPFault parent,
                                             OMXMLParserWrapper builder);

    /*========================
   =  SOAPFaultCodeValue  =
   ======================== */

    /**
     * Value eii under Code (parent)
     *
     * @param parent
     * @return Returns SOAPFaultValue.
     */
    SOAPFaultValue createSOAPFaultValue(SOAPFaultCode parent) throws SOAPProcessingException;

    SOAPFaultValue createSOAPFaultValue() throws SOAPProcessingException;

    /**
     * Value eii under Code (parent)
     *
     * @param parent
     * @param builder
     * @return Returns SOAPFaultValue.
     */
    SOAPFaultValue createSOAPFaultValue(SOAPFaultCode parent,
                                               OMXMLParserWrapper builder);

    /*========================
      =  SOAPFaultSubCode    =
      ======================== */

    /**
     * SubCode eii under Value (parent)
     *
     * @param parent
     * @return Returns SOAPFaultValue.
     */

    //added
    SOAPFaultValue createSOAPFaultValue(SOAPFaultSubCode parent)
            throws SOAPProcessingException;

    //added
    SOAPFaultValue createSOAPFaultValue(SOAPFaultSubCode parent,
                                               OMXMLParserWrapper builder);

    //changed
    SOAPFaultSubCode createSOAPFaultSubCode(SOAPFaultCode parent)
            throws SOAPProcessingException;

    SOAPFaultSubCode createSOAPFaultSubCode() throws SOAPProcessingException;

    /**
     * SubCode eii under Value (parent)
     *
     * @param parent
     * @param builder
     * @return Returns SOAPFaultSubCode.
     */
    //changed
    SOAPFaultSubCode createSOAPFaultSubCode(SOAPFaultCode parent,
                                                   OMXMLParserWrapper builder);

    /**
     * SubCode eii under SubCode (parent)
     *
     * @param parent
     * @return Returns SOAPFaultSubCode.
     */
    SOAPFaultSubCode createSOAPFaultSubCode(SOAPFaultSubCode parent)
            throws SOAPProcessingException;

    /**
     * SubCode eii under SubCode (parent)
     *
     * @param parent
     * @param builder
     * @return Returns SOAPFaultSubCode.
     */
    SOAPFaultSubCode createSOAPFaultSubCode(SOAPFaultSubCode parent,
                                                   OMXMLParserWrapper builder);

    /*========================
   =  SOAPFaultReason     =
   ======================== */

    /**
     * Reason eii under SOAPFault (parent)
     *
     * @param parent
     * @return Returns SOAPFaultReason.
     */
    SOAPFaultReason createSOAPFaultReason(SOAPFault parent) throws SOAPProcessingException;

    SOAPFaultReason createSOAPFaultReason() throws SOAPProcessingException;

    /**
     * Reason eii under SOAPFault (parent)
     *
     * @param parent
     * @param builder
     * @return Returns SOAPFaultReason.
     */
    SOAPFaultReason createSOAPFaultReason(SOAPFault parent,
                                                 OMXMLParserWrapper builder);

    /*========================
      =  SOAPFaultReasonText     =
      ======================== */

    /**
     * SubCode eii under SubCode (parent)
     *
     * @param parent
     * @return Returns SOAPFaultText.
     */
    SOAPFaultText createSOAPFaultText(SOAPFaultReason parent) throws SOAPProcessingException;

    SOAPFaultText createSOAPFaultText() throws SOAPProcessingException;

    /**
     * SubCode eii under SubCode (parent)
     *
     * @param parent
     * @param builder
     * @return Returns SOAPFaultText.
     */
    SOAPFaultText createSOAPFaultText(SOAPFaultReason parent,
                                             OMXMLParserWrapper builder);

    /*========================
   =  SOAPFaultNode       =
   ======================== */

    /**
     * Node eii under SOAPFault (parent)
     *
     * @param parent
     * @return Returns SOAPFaultNode.
     */
    SOAPFaultNode createSOAPFaultNode(SOAPFault parent) throws SOAPProcessingException;

    SOAPFaultNode createSOAPFaultNode() throws SOAPProcessingException;

    /**
     * Node eii under SOAPFault (parent)
     *
     * @param parent
     * @param builder
     * @return Returns SOAPFaultNode.
     */
    SOAPFaultNode createSOAPFaultNode(SOAPFault parent,
                                             OMXMLParserWrapper builder);

    /*========================
      =  SOAPFaultRole       =
      ======================== */

    /**
     * Role eii under SOAPFault (parent)
     *
     * @param parent
     * @return Returns SOAPFaultRole.
     */
    SOAPFaultRole createSOAPFaultRole(SOAPFault parent) throws SOAPProcessingException;

    SOAPFaultRole createSOAPFaultRole() throws SOAPProcessingException;

    /**
     * Role eii under SOAPFault (parent)
     *
     * @param parent
     * @param builder
     * @return Returns SOAPFaultRole.
     */
    SOAPFaultRole createSOAPFaultRole(SOAPFault parent,
                                             OMXMLParserWrapper builder);

    /*========================
      =  SOAPFaultDetail     =
      ======================== */

    /**
     * Detail eii under SOAPFault (parent)
     *
     * @param parent
     * @return Returns SOAPFaultDetail.
     */
    SOAPFaultDetail createSOAPFaultDetail(SOAPFault parent) throws SOAPProcessingException;

    SOAPFaultDetail createSOAPFaultDetail() throws SOAPProcessingException;

    /**
     * Role eii under SOAPFault (parent)
     *
     * @param parent
     * @param builder
     * @return Returns SOAPFaultDetail.
     */
    SOAPFaultDetail createSOAPFaultDetail(SOAPFault parent,
                                                 OMXMLParserWrapper builder);


    /**
     * Method getDefaultEnvelope. This returns a SOAP envelope consisting with an empty Header and a
     * Body. This is just a util method which can be used everywhere.
     *
     * @return Returns SOAPEnvelope.
     */
    SOAPEnvelope getDefaultEnvelope() throws SOAPProcessingException;

    SOAPEnvelope getDefaultFaultEnvelope() throws SOAPProcessingException;

    OMNamespace getNamespace();

}
