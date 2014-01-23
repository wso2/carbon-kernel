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

package org.apache.axis2.jaxws.message;

import org.apache.axiom.soap.SOAP12Constants;

import javax.xml.namespace.QName;

/**
 * Agnostic representation of SOAP 1.1 and SOAP 1.2 fault code values.
 *
 * @see XMLFault
 */
public class XMLFaultCode {


    //   SENDER             -> SOAP 1.2 Sender              / SOAP 1.1  Client
    //   RECEIVER           -> SOAP 1.2 Receiver            / SOAP 1.1  Server
    //   MUSTUNDERSTAND     -> SOAP 1.2 MustUnderstand      / SOAP 1.1  MustUnderstand
    //   DATAENCODINGUNKNOWN-> SOAP 1.2 DataEncodingUnknown / SOAP 1.1  Server
    //   VERSIONMISMATCH    -> SOAP 1.2 VersionMismatch     / SOAP 1.1  VersionMismatch
    //   CUSTOM_SOAP11_ONLY -> SOAP 1.2 Receiver            / SOAP 1.1  "custom qname"

    // Rendered as qnames with the following local names
    //     (the namespace is the corresponding envelope namespace)
    
    public static final XMLFaultCode SENDER = (new XMLFaultCode() {
        public QName toQName(String namespace) {
            String localPart;
            if (namespace.equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
                localPart = "Sender";
            } else {
                localPart = "Client";
            }
            return new QName(namespace, localPart);
        }
    });
    
    public static final XMLFaultCode RECEIVER = (new XMLFaultCode() {
        public QName toQName(String namespace) {
            String localPart;
            if (namespace.equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
                localPart = "Receiver";
            } else {
                localPart = "Server";
            }
            return new QName(namespace, localPart);
        }
    });
    
    public static final XMLFaultCode MUSTUNDERSTAND = (new XMLFaultCode() {
        public QName toQName(String namespace) {
            return new QName(namespace, "MustUnderstand");
        }
    });
    
    public static final XMLFaultCode DATAENCODINGUNKNOWN = (new XMLFaultCode() {
        public QName toQName(String namespace) {
            String localPart;
            if (namespace.equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
                localPart = "DataEncodingUnknown";
            } else {
                localPart = "Server";
            }
            return new QName(namespace, localPart);
        }
    });
    
    public static final XMLFaultCode VERSIONMISMATCH = (new XMLFaultCode() {
        public QName toQName(String namespace) {
            return new QName(namespace, "VersionMismatch");
        }
    });
    

    private QName faultCode;
    
    private XMLFaultCode() {        
    }
        
    public XMLFaultCode(QName faultCode) {
        if (faultCode == null) {
            throw new NullPointerException("Null fault code");
        }
        this.faultCode = faultCode;
    }
    
    // Utility Methods

    /**
     * Return QName for the given protocol
     *
     * @param namespace of the envelope for the protocol
     * @return
     */
    public QName toQName(String protocolNamespace) {
        return this.faultCode;
    }

    /**
     * get the XMLPart corresponding to this specified QName
     *
     * @param qName
     * @return corresponding XMLPart
     */
    public static XMLFaultCode fromQName(QName qName) {
        if (qName == null) {
            // Spec indicates that the default is receiver
            return RECEIVER;
        }
        String namespace = qName.getNamespaceURI();
        String localPart = qName.getLocalPart();
        XMLFaultCode xmlFaultCode = null;
        // Due to problems in the OM, sometimes that qname is not retrieved correctly.
        // So use the localName to find the XMLFaultCode
        if (localPart.equalsIgnoreCase("Sender")) {          // SOAP 1.2
            xmlFaultCode = SENDER;
        } else if (localPart.equalsIgnoreCase("Receiver")) { // SOAP 1.2
            xmlFaultCode = RECEIVER;
        } else if (localPart.equalsIgnoreCase("Client")) {   // SOAP 1.1
            xmlFaultCode = SENDER;
        } else if (localPart.equalsIgnoreCase("Server")) {   // SOAP 1.1
            xmlFaultCode = RECEIVER;
        } else if (localPart.equalsIgnoreCase("MustUnderstand")) {  // Both
            xmlFaultCode = MUSTUNDERSTAND;
        } else if (localPart.equalsIgnoreCase("DataEncodingUnknown")) {  // SOAP 1.2
            xmlFaultCode = DATAENCODINGUNKNOWN;
        } else if (localPart.equalsIgnoreCase("VersionMismatch")) { // Both
            xmlFaultCode = VERSIONMISMATCH;
        } else {
            xmlFaultCode = new XMLFaultCode(qName);
        }
        
        return xmlFaultCode;
    }
}
