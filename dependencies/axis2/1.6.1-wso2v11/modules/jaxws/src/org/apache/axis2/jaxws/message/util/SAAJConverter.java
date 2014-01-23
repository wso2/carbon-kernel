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

package org.apache.axis2.jaxws.message.util;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.OMElement;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.ws.WebServiceException;

/** SAAJConverter Provides Conversion between SAAJ and OM Constructed via the SAAJConverterFactory */
public interface SAAJConverter {
	
	public final static String OM_ATTRIBUTE_KEY = "ATTRIBUTE_TYPE_KEY";
	
    /**
     * Convert OM SOAPEnvleope to SAAJ SOAPEnvelope
     *
     * @param omElement
     * @return SOAPEnvelope
     * @throws WebServiceException
     */
    public SOAPEnvelope toSAAJ(org.apache.axiom.soap.SOAPEnvelope omElement)
            throws WebServiceException;

    /**
     * Convert SAAJ SOAPEnvelope to OM SOAPEnvelope
     *
     * @param saajEnvelope
     * @return OM Envelope
     * @throws WebServiceException
     */
    public org.apache.axiom.soap.SOAPEnvelope toOM(SOAPEnvelope saajEnvelope)
            throws WebServiceException;
    
    /**
     * Convert SAAJ SOAPEnvelope to OM SOAPEnvelope
     *
     * @param saajEnvelope
     * @param Attachments
     * @return OM Envelope
     * @throws WebServiceException
     */
    public org.apache.axiom.soap.SOAPEnvelope toOM(SOAPEnvelope saajEnvelope, 
                                                   Attachments attachments)
            throws WebServiceException;

    /**
     * Convert SOAPElement into an OMElement
     *
     * @param soapElement
     * @return OMElement
     * @throws WebServiceException
     */
    public OMElement toOM(SOAPElement soapElement)
            throws WebServiceException;

    /**
     * Convert omElement into a SOAPElement and add it to the parent SOAPElement. This method requires
     * that the parent element have an ancestor that is a SOAPEnvelope. If this is not the case use the
     * toSAAJ(OMElement, SOAPElement, SOAPFactory) method
     *
     * @param omElement
     * @param parent    SOAPElement
     * @return SOAPElement that was added to the parent.
     * @throws WebServiceException
     * @see toSAAJ(OMElement, SOAPElement, SOAPFactory)
     */
    public SOAPElement toSAAJ(OMElement omElement, SOAPElement parent)
            throws WebServiceException;

    /**
     * Convert omElement into a SOAPElement and add it to the parent SOAPElement.
     *
     * @param omElement
     * @param parent    SOAPElement
     * @param sf        SOAPFactory that is used to create Name objects
     * @return SOAPElement that was added to the parent.
     * @throws WebServiceException * @see toSAAJ(OMElement, SOAPElement)
     */
    public SOAPElement toSAAJ(OMElement omElement, SOAPElement parent, SOAPFactory sf)
            throws WebServiceException;

    /**
     * Creates a MessageFactory that can support the SOAP version identified
     * by the specified envelope namespace.
     * @param namespace
     * @return
     * @throws WebServiceException if the namespace is SOAP 1.2 and the SAAJ does not support
     * SOAP 1.2 or the namespace is unknown.
     */
    public MessageFactory createMessageFactory(String namespace)
            throws SOAPException, WebServiceException;
}
