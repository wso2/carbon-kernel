/**
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

package org.apache.ws.security.transform;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSDocInfo;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.message.token.SecurityTokenReference;
import org.apache.ws.security.message.token.X509Security;
import org.apache.ws.security.util.Base64;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * Utility class exposing the dereferencing logic of the {@link STRTransform} implementation.
 */
public class STRTransformUtil {
    private static Log log = LogFactory.getLog(STRTransformUtil.class.getName());

    /**
     * Retrieves the element representing the referenced content of a STR.
     * 
     * @return the element representing the referenced content. The element is either
     *         extracted from {@code doc} or a new element is created in the
     *         case of a key identifier or issuer serial STR.  {@code null} if
     *         {@code secRef} does not contain a direct reference, key identifier, or
     *         issuer serial.
     * @throws WSSecurityException
     *             If an issuer serial or key identifier is used in the STR and
     *             the certificate cannot be resolved from the crypto
     *             configuration or if there is an error working with the resolved
     *             cert
     */
    public static Element dereferenceSTR(Document doc,
            SecurityTokenReference secRef, WSDocInfo wsDocInfo) throws WSSecurityException
    {
        
        // NOTE: Here step numbers refer to the overall step in the complete processing
        // of the STRTransform.  See STRTransform for the lead up to these steps.
        //
        // Third step: locate the security token referenced by the STR element.
        // Either the Token is contained in the document as a
        // BinarySecurityToken or stored in some key storage.
        // 
        // Fourth step: after security token was located, prepare it. If its
        // reference via a direct reference, i.e. a relative URI that references
        // the BST directly in the message then just return that element.
        // Otherwise wrap the located token in a newly created BST element as
        // described in WSS Specification.
        // 
        //
        Element tokElement = null;
    
        //
        // First case: direct reference, according to chap 7.2 of OASIS WS
        // specification (main document). Only in this case return a true
        // reference to the BST. Copying is done by the caller.
        //
        if (secRef.containsReference()) {
            if (log.isDebugEnabled()) {
                log.debug("STR: Reference");
            }
            tokElement = secRef.getTokenElement(doc, wsDocInfo, null);
        }
        //
        // second case: IssuerSerial, lookup in keystore, wrap in BST according
        // to specification
        //
        else if (secRef.containsX509Data() || secRef.containsX509IssuerSerial()) {
            if (log.isDebugEnabled()) {
                log.debug("STR: IssuerSerial");
            }
            X509Certificate cert = null;
            X509Certificate[] certs = 
                secRef.getX509IssuerSerial(wsDocInfo.getCrypto());
            if (certs == null || certs.length == 0 || certs[0] == null) {
                throw new WSSecurityException(WSSecurityException.FAILED_CHECK);
            }
            cert = certs[0];
            tokElement = createBSTX509(doc, cert, secRef.getElement());
        }
        //
        // third case: KeyIdentifier. For SKI, lookup in keystore, wrap in
        // BST according to specification. Otherwise if it's a wsse:KeyIdentifier it could
        // be a SAML assertion, so try and find the referenced element.
        //
        else if (secRef.containsKeyIdentifier()) {
            if (log.isDebugEnabled()) {
                log.debug("STR: KeyIdentifier");
            }
            if(WSConstants.WSS_SAML_KI_VALUE_TYPE.equals(secRef.getKeyIdentifierValueType()) 
            		|| WSConstants.WSS_SAML2_KI_VALUE_TYPE.equals(secRef.getKeyIdentifierValueType())) {
                tokElement = secRef.getKeyIdentifierTokenElement(doc, wsDocInfo, null);
            } else {
                X509Certificate cert = null;
                X509Certificate[] certs = secRef.getKeyIdentifier(wsDocInfo.getCrypto());
                if (certs == null || certs.length == 0 || certs[0] == null) {
                    throw new WSSecurityException(WSSecurityException.FAILED_CHECK);
                }
                cert = certs[0];
                tokElement = createBSTX509(doc, cert, secRef.getElement());
            }
        }
        return tokElement;
    }
    
    protected static Element createBSTX509(Document doc, X509Certificate cert, Element secRefE) 
        throws WSSecurityException {
        byte data[];
        try {
            data = cert.getEncoded();
        } catch (CertificateEncodingException e) {
            throw new WSSecurityException(
                WSSecurityException.SECURITY_TOKEN_UNAVAILABLE, "encodeError", null, e
            );
        }
        String prefix = WSSecurityUtil.getPrefixNS(WSConstants.WSSE_NS, secRefE);
        Element elem = doc.createElementNS(WSConstants.WSSE_NS, prefix + ":BinarySecurityToken");
        WSSecurityUtil.setNamespace(elem, WSConstants.WSSE_NS, prefix);
        // elem.setAttributeNS(WSConstants.XMLNS_NS, "xmlns", "");
        elem.setAttributeNS(null, "ValueType", X509Security.X509_V3_TYPE);
        Text certText = doc.createTextNode(Base64.encode(data)); // no line wrap
        elem.appendChild(certText);
        return elem;
    }
    
    /**
     * Hidden in utility class.
     */
    private STRTransformUtil() {   
    }
    
}