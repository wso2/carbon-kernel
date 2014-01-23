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

package org.apache.ws.security.message;

import org.apache.ws.security.SOAPConstants;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSEncryptionPart;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.conversation.ConversationException;
import org.apache.ws.security.message.token.Reference;
import org.apache.ws.security.message.token.SecurityTokenReference;
import org.apache.ws.security.util.WSSecurityUtil;
import org.apache.xml.security.encryption.EncryptedData;
import org.apache.xml.security.encryption.XMLCipher;
import org.apache.xml.security.encryption.XMLEncryptionException;
import org.apache.xml.security.keys.KeyInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.crypto.SecretKey;

import java.util.Vector;

/**
 * Encrypts and signs parts of a message with derived keys derived from a
 * symmetric key. This symmetric key will be included as an EncryptedKey
 * 
 * @author Ruchith Fernando (ruchith.fernando@gmail.com)
 */
public class WSSecDKEncrypt extends WSSecDerivedKeyBase {

    protected String symEncAlgo = WSConstants.AES_128;
    
    public Document build(Document doc, WSSecHeader secHeader)
        throws WSSecurityException, ConversationException {
        
        //
        // Setup the encrypted key
        //
        prepare(doc);
        this.envelope =  doc.getDocumentElement();
        //
        // prepend elements in the right order to the security header
        //
        prependDKElementToHeader(secHeader);
                
        SOAPConstants soapConstants = WSSecurityUtil.getSOAPConstants(envelope);
        if (parts == null) {
            parts = new Vector();
            WSEncryptionPart encP = 
                new WSEncryptionPart(
                    soapConstants.getBodyQName().getLocalPart(), 
                    soapConstants.getEnvelopeURI(), 
                    "Content"
                );
            parts.add(encP);
        }
        Element externRefList = encryptForExternalRef(null, parts);
        addExternalRefElement(externRefList, secHeader);

        return doc;
    }

    private Vector doEncryption(Document doc, byte[] secretKey, Vector references) 
        throws WSSecurityException {

        SecretKey key = WSSecurityUtil.prepareSecretKey(this.symEncAlgo, secretKey);
        XMLCipher xmlCipher = null;
        try {
            xmlCipher = XMLCipher.getInstance(symEncAlgo);
        } catch (XMLEncryptionException e3) {
            throw new WSSecurityException(
                WSSecurityException.UNSUPPORTED_ALGORITHM, null, null, e3
            );
        }

        Vector encDataRefs = new Vector();
        if (envelope == null) {
            envelope = doc.getDocumentElement();
        }
        
        for (int part = 0; part < references.size(); part++) {
            WSEncryptionPart encPart = (WSEncryptionPart) references.get(part);

            String idToEnc = encPart.getId();
            String elemName = encPart.getName();
            String nmSpace = encPart.getNamespace();
            String modifier = encPart.getEncModifier();
            //
            // Third step: get the data to encrypt.
            //
            Element body = null;
            if (idToEnc != null) {
                body = 
                    WSSecurityUtil.findElementById(
                        document.getDocumentElement(), idToEnc, WSConstants.WSU_NS
                    );
                if (body == null) {
                    body = 
                        WSSecurityUtil.findElementById(document.getDocumentElement(), idToEnc, null);
                }
            } else {
                body = (Element) WSSecurityUtil.findElement(envelope, elemName, nmSpace);
            }
            if (body == null) {
                throw new WSSecurityException(
                    WSSecurityException.FAILURE,
                    "noEncElement", 
                    new Object[] {"{" + nmSpace + "}" + elemName}
                );
            }

            boolean content = modifier.equals("Content") ? true : false;
            String xencEncryptedDataId = wssConfig.getIdAllocator().createId("EncDataId-", body);

            //
            // Fourth step: encrypt data, and set necessary attributes in
            // xenc:EncryptedData
            //
            try {
                // Create the SecurityTokenRef to the DKT
                KeyInfo keyInfo = new KeyInfo(document);
                SecurityTokenReference secToken = new SecurityTokenReference(document);
                Reference ref = new Reference(document);
                ref.setURI("#" + dktId);
                secToken.setReference(ref);

                keyInfo.addUnknownElement(secToken.getElement());
                Element keyInfoElement = keyInfo.getElement();
                keyInfoElement.setAttributeNS(
                    WSConstants.XMLNS_NS, "xmlns:" + WSConstants.SIG_PREFIX, WSConstants.SIG_NS
                );

                xmlCipher.init(XMLCipher.ENCRYPT_MODE, key);
                EncryptedData encData = xmlCipher.getEncryptedData();
                encData.setId(xencEncryptedDataId);
                encData.setKeyInfo(keyInfo);
                xmlCipher.doFinal(doc, body, content);
            } catch (Exception e2) {
                throw new WSSecurityException(
                    WSSecurityException.FAILED_ENCRYPTION, null, null, e2
                );
            }
            encDataRefs.add("#" + xencEncryptedDataId);
        }
        return encDataRefs;
    }
    
    /**
     * Encrypt one or more parts or elements of the message (external).
     * 
     * This method takes a vector of <code>WSEncryptionPart</code> object that
     * contain information about the elements to encrypt. The method call the
     * encryption method, takes the reference information generated during
     * encryption and add this to the <code>xenc:Reference</code> element.
     * This method can be called after <code>prepare()</code> and can be
     * called multiple times to encrypt a number of parts or elements.
     * 
     * The method generates a <code>xenc:Reference</code> element that <i>must</i>
     * be added to the SecurityHeader. See <code>addExternalRefElement()</code>.
     * 
     * If the <code>dataRef</code> parameter is <code>null</code> the method
     * creates and initializes a new Reference element.
     * 
     * @param dataRef A <code>xenc:Reference</code> element or <code>null</code>
     * @param references A vector containing WSEncryptionPart objects
     * @return Returns the updated <code>xenc:Reference</code> element
     * @throws WSSecurityException
     */
    public Element encryptForExternalRef(Element dataRef, Vector references)
        throws WSSecurityException {

        Vector encDataRefs = doEncryption(document, derivedKeyBytes, references);
        Element referenceList = dataRef;
        if (referenceList == null) {
            referenceList = 
                document.createElementNS(
                    WSConstants.ENC_NS, WSConstants.ENC_PREFIX + ":ReferenceList"
                );
        }
        createDataRefList(document, referenceList, encDataRefs);
        return referenceList;
    }
    
    /**
     * Adds (prepends) the external Reference element to the Security header.
     * 
     * The reference element <i>must</i> be created by the
     * <code>encryptForExternalRef() </code> method. The method adds the
     * reference element in the SecurityHeader.
     * 
     * @param referenceList The external <code>enc:Reference</code> element
     * @param secHeader The security header.
     */
    public void addExternalRefElement(Element referenceList, WSSecHeader secHeader) {
        Node node = dkt.getElement().getNextSibling();
        if (node instanceof Element) {
            secHeader.getSecurityHeader().insertBefore(referenceList, node);
        } else {
            // If (at this moment) DerivedKeyToken is the LAST element of 
            // the security header 
            secHeader.getSecurityHeader().appendChild(referenceList);
        }
    }

    public static Element createDataRefList(Document doc, Element referenceList, Vector encDataRefs) {
        for (int i = 0; i < encDataRefs.size(); i++) {
            String dataReferenceUri = (String) encDataRefs.get(i);
            Element dataReference = 
                doc.createElementNS(
                    WSConstants.ENC_NS, WSConstants.ENC_PREFIX + ":DataReference"
                );
            dataReference.setAttributeNS(null, "URI", dataReferenceUri);
            referenceList.appendChild(dataReference);
        }
        return referenceList;
    }

    
    public void setSymmetricEncAlgorithm(String algo) {
        symEncAlgo = algo;
    }

    /**
     * @see org.apache.ws.security.message.WSSecDerivedKeyBase#getDerivedKeyLength()
     */
    protected int getDerivedKeyLength() throws WSSecurityException{
        return (this.derivedKeyLength > 0) ? this.derivedKeyLength : 
            WSSecurityUtil.getKeyLength(this.symEncAlgo);
    }
    
}
