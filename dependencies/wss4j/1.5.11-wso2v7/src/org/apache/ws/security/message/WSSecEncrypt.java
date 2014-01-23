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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.SOAPConstants;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSEncryptionPart;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.message.token.Reference;
import org.apache.ws.security.message.token.SecurityTokenReference;
import org.apache.ws.security.util.Base64;
import org.apache.ws.security.util.WSSecurityUtil;
import org.apache.xml.security.algorithms.JCEMapper;
import org.apache.xml.security.encryption.EncryptedData;
import org.apache.xml.security.encryption.XMLCipher;
import org.apache.xml.security.encryption.XMLEncryptionException;
import org.apache.xml.security.keys.KeyInfo;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Vector;

/**
 * Encrypts a parts of a message according to WS Specification, X509 profile,
 * and adds the encryption data.
 * 
 * @author Davanum Srinivas (dims@yahoo.com).
 * @author Werner Dittmann (Werner.Dittmann@apache.org).
 */
public class WSSecEncrypt extends WSSecEncryptedKey {
    private static Log log = LogFactory.getLog(WSSecEncrypt.class.getName());

    protected String symEncAlgo = WSConstants.AES_128;

    protected String encCanonAlgo = null;

    protected byte[] embeddedKey = null;

    protected String embeddedKeyName = null;

    protected boolean useKeyIdentifier;

    /**
     * Symmetric key used in the EncrytpedKey.
     */
    protected SecretKey symmetricKey = null;

    /**
     * SecurityTokenReference to be inserted into EncryptedData/keyInfo element.
     */
    protected SecurityTokenReference securityTokenReference = null;

    /**
     * Indicates whether to encrypt the symmetric key into an EncryptedKey 
     * or not.
     */
    private boolean encryptSymmKey = true;
    
    /**
     * Custom reference value
     */
    private String customReferenceValue;

    /**
     * ValueType for the encrypted key reference
     */
    private String encKeyValueType;
    
    /**
     * True if the encKeyId is a direct reference to a key identifier instead of a URI to a key
     */
    private boolean encKeyIdDirectId;

    /**
     * Constructor.
     */
    public WSSecEncrypt() {
    }

    /**
     * Sets the key to use during embedded encryption.
     * 
     * @param key to use during encryption. The key must fit the selected
     *            symmetrical encryption algorithm
     */
    public void setKey(byte[] key) {
        this.embeddedKey = key;
    }

    /**
     * Sets the algorithm to encode the symmetric key.
     * 
     * Default is the <code>WSConstants.KEYTRANSPORT_RSA15</code> algorithm.
     * 
     * @param keyEnc specifies the key encoding algorithm.
     * @see WSConstants#KEYTRANSPORT_RSA15
     * @see WSConstants#KEYTRANSPORT_RSAOEP
     */
    public void setKeyEnc(String keyEnc) {
        keyEncAlgo = keyEnc;
    }

    /**
     * Set the key name for EMBEDDED_KEYNAME
     * 
     * @param embeddedKeyName
     */
    public void setEmbeddedKeyName(String embeddedKeyName) {
        this.embeddedKeyName = embeddedKeyName;
    }
    
    /**
     * Set this true if a key identifier must be used in the KeyInfo
     * 
     * @param useKeyIdentifier
     */
    public void setUseKeyIdentifier(boolean useKeyIdentifier) {
        this.useKeyIdentifier = useKeyIdentifier;
    }
    
    /**
     * Set the name of the symmetric encryption algorithm to use.
     * 
     * This encryption algorithm is used to encrypt the data. If the algorithm
     * is not set then AES128 is used. Refer to WSConstants which algorithms are
     * supported.
     * 
     * @param algo Is the name of the encryption algorithm
     * @see WSConstants#TRIPLE_DES
     * @see WSConstants#AES_128
     * @see WSConstants#AES_192
     * @see WSConstants#AES_256
     */
    public void setSymmetricEncAlgorithm(String algo) {
        symEncAlgo = algo;
    }

    /**
     * Set the name of an optional canonicalization algorithm to use before
     * encryption.
     * 
     * This c14n algorithm is used to serialize the data before encryption. If
     * the algorithm is not set then a standard serialization is used (provided
     * by XMLCipher, usually a XMLSerializer according to DOM 3 specification).
     * 
     * @param algo Is the name of the canonicalization algorithm
     */
    public void setEncCanonicalization(String algo) {
        encCanonAlgo = algo;
    }
    
    /**
     * Get the name of symmetric encryption algorithm to use.
     * 
     * The name of the encryption algorithm to encrypt the data, i.e. the SOAP
     * Body. Refer to WSConstants which algorithms are supported.
     * 
     * @return the name of the currently selected symmetric encryption algorithm
     * @see WSConstants#TRIPLE_DES
     * @see WSConstants#AES_128
     * @see WSConstants#AES_192
     * @see WSConstants#AES_256
     */
    public String getSymmetricEncAlgorithm() {
        return symEncAlgo;
    }
    
    /**
     * Returns if Key Identifiers should be used in KeyInfo
     * @return if Key Identifiers should be used in KeyInfo
     */
    public boolean getUseKeyIdentifier() {
        return useKeyIdentifier;
    }
    
    /**
     * Initialize a WSSec Encrypt.
     * 
     * The method prepares and initializes a WSSec Encrypt structure after the
     * relevant information was set. After preparation of the token references
     * can be added and encrypted.
     * 
     * This method does not add any element to the security header. This must be
     * done explicitly.
     * 
     * @param doc The SOAP envelope as <code>Document</code>
     * @param crypto An instance of the Crypto API to handle keystore and certificates
     * @throws WSSecurityException
     */
    public void prepare(Document doc, Crypto crypto) throws WSSecurityException {

        document = doc;

        //
        // If no external key (symmetricalKey) was set generate an encryption
        // key (session key) for this Encrypt element. This key will be
        // encrypted using the public key of the receiver
        //
        if (this.ephemeralKey == null) {
            if (symmetricKey == null) {
                KeyGenerator keyGen = getKeyGenerator();
                this.symmetricKey = keyGen.generateKey();
            } 
            this.ephemeralKey = this.symmetricKey.getEncoded();
        }
        
        if (this.symmetricKey == null) {
            this.symmetricKey = WSSecurityUtil.prepareSecretKey(symEncAlgo, this.ephemeralKey);
        }
        
        //
        // Get the certificate that contains the public key for the public key
        // algorithm that will encrypt the generated symmetric (session) key.
        //
        if (this.encryptSymmKey) {
            X509Certificate remoteCert = null;
            if (useThisCert != null) {
                remoteCert = useThisCert;
            } else {
                X509Certificate[] certs = crypto.getCertificates(user);
                if (certs == null || certs.length <= 0) {
                    throw new WSSecurityException(
                        WSSecurityException.FAILURE,
                        "noUserCertsFound", 
                        new Object[] { user, "encryption" }
                    );
                }
                remoteCert = certs[0];
            }
            prepareInternal(this.ephemeralKey, remoteCert, crypto);
        } else {
            encryptedEphemeralKey = ephemeralKey;
        }
    }

    /**
     * Builds the SOAP envelope with encrypted Body and adds encrypted key.
     * 
     * This is a convenience method and for backward compatibility. The method
     * calls the single function methods in order to perform a <i>one shot
     * encryption</i>. This method is compatible with the build method of the
     * previous version with the exception of the additional WSSecHeader
     * parameter.
     * 
     * @param doc the SOAP envelope as <code>Document</code> with plain text Body
     * @param crypto an instance of the Crypto API to handle keystore and Certificates
     * @param secHeader the security header element to hold the encrypted key element.
     * @return the SOAP envelope with encrypted Body as <code>Document</code>
     * @throws WSSecurityException
     */
    public Document build(Document doc, Crypto crypto, WSSecHeader secHeader)
        throws WSSecurityException {
        doDebug = log.isDebugEnabled();

        if (keyIdentifierType == WSConstants.EMBEDDED_KEYNAME
            || keyIdentifierType == WSConstants.EMBED_SECURITY_TOKEN_REF) {
            return buildEmbedded(doc, secHeader);
        }

        if (doDebug) {
            log.debug("Beginning Encryption...");
        }

        prepare(doc, crypto);
        
        if (envelope == null) {
            envelope = document.getDocumentElement();
        }

        if (parts == null) {
            parts = new Vector();
            SOAPConstants soapConstants = WSSecurityUtil.getSOAPConstants(envelope);
            WSEncryptionPart encP = 
                new WSEncryptionPart(
                    soapConstants.getBodyQName().getLocalPart(), 
                    soapConstants.getEnvelopeURI(), 
                    "Content"
                );
            parts.add(encP);
        }

        Element refs = encryptForInternalRef(null, parts);
        if (encryptedKeyElement != null) {
            addInternalRefElement(refs);
            prependToHeader(secHeader); 
        } else {
            WSSecurityUtil.prependChildElement(secHeader.getSecurityHeader(), refs);
        }

        if (bstToken != null) {
            prependBSTElementToHeader(secHeader);
        }

        log.debug("Encryption complete.");
        return doc;
    }

    /**
     * Encrypt one or more parts or elements of the message (internal).
     * 
     * This method takes a vector of <code>WSEncryptionPart</code> object that
     * contain information about the elements to encrypt. The method call the
     * encryption method, takes the reference information generated during
     * encryption and add this to the <code>xenc:Reference</code> element.
     * This method can be called after <code>prepare()</code> and can be
     * called multiple times to encrypt a number of parts or elements.
     * 
     * The method generates a <code>xenc:Reference</code> element that <i>must</i>
     * be added to this token. See <code>addInternalRefElement()</code>.
     * 
     * If the <code>dataRef</code> parameter is <code>null</code> the method
     * creates and initializes a new Reference element.
     * 
     * @param dataRef A <code>xenc:Reference</code> element or <code>null</code>
     * @param references A vector containing WSEncryptionPart objects
     * @return Returns the updated <code>xenc:Reference</code> element
     * @throws WSSecurityException
     */
    public Element encryptForInternalRef(Element dataRef, Vector references)
        throws WSSecurityException {
        Vector encDataRefs = 
            doEncryption(document, this.symmetricKey, references);
        Element referenceList = dataRef;
        if (referenceList == null) {
            referenceList = 
                document.createElementNS(
                    WSConstants.ENC_NS,
                    WSConstants.ENC_PREFIX + ":ReferenceList"
                );
        }
        createDataRefList(document, referenceList, encDataRefs);
        return referenceList;
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

        Vector encDataRefs = 
            doEncryption(document, this.symmetricKey, references);
        Element referenceList = dataRef;
        if (referenceList == null) {
            referenceList = 
                document.createElementNS(
                    WSConstants.ENC_NS,
                    WSConstants.ENC_PREFIX + ":ReferenceList"
                );
            //
            // If we're not placing the ReferenceList in an EncryptedKey structure,
            // then add the ENC namespace
            //
            if (!encryptSymmKey) {
                WSSecurityUtil.setNamespace(
                    referenceList, WSConstants.ENC_NS, WSConstants.ENC_PREFIX
                );
            }
        }
        createDataRefList(document, referenceList, encDataRefs);
        return referenceList;
    }

    /**
     * Adds the internal Reference element to this Encrypt data.
     * 
     * The reference element <i>must</i> be created by the
     * <code>encryptForInternalRef()</code> method. The reference element is
     * added to the <code>EncryptedKey</code> element of this encrypt block.
     * 
     * @param dataRef The internal <code>enc:Reference</code> element
     */
    public void addInternalRefElement(Element dataRef) {
        encryptedKeyElement.appendChild(dataRef);
    }

    /**
     * Adds (prepends) the external Reference element to the Security header.
     * 
     * The reference element <i>must</i> be created by the
     * <code>encryptForExternalRef() </code> method. The method prepends the
     * reference element in the SecurityHeader.
     * 
     * @param dataRef The external <code>enc:Reference</code> element
     * @param secHeader The security header.
     */
    public void addExternalRefElement(Element dataRef, WSSecHeader secHeader) {
        WSSecurityUtil.prependChildElement(secHeader.getSecurityHeader(), dataRef);
    }

    private Vector doEncryption(Document doc, SecretKey secretKey, Vector references) 
        throws WSSecurityException {
        
        KeyInfo keyInfo = null;
        
        // Prepare KeyInfo if useKeyIdentifier is set
        if (useKeyIdentifier &&
            keyIdentifierType == WSConstants.ENCRYPTED_KEY_SHA1_IDENTIFIER) {
            keyInfo = new KeyInfo(document);
            SecurityTokenReference secToken = new SecurityTokenReference(document);
            if (this.customReferenceValue != null) {
                secToken.setKeyIdentifierEncKeySHA1(this.customReferenceValue);
            } else {
                secToken.setKeyIdentifierEncKeySHA1(getSHA1(encryptedEphemeralKey));
            }
            
            keyInfo.addUnknownElement(secToken.getElement());
            Element keyInfoElement = keyInfo.getElement();
            keyInfoElement.setAttributeNS(
                WSConstants.XMLNS_NS, "xmlns:"+ WSConstants.SIG_PREFIX, WSConstants.SIG_NS
            );
        } 
        
        return doEncryption(doc, secretKey, keyInfo, references);
    }

    private Vector doEncryption(
        Document doc, 
        SecretKey secretKey,
        KeyInfo keyInfo, 
        Vector references
    ) throws WSSecurityException {

        XMLCipher xmlCipher = null;
        try {
            xmlCipher = XMLCipher.getInstance(symEncAlgo);
        } catch (XMLEncryptionException e3) {
            throw new WSSecurityException(
                WSSecurityException.UNSUPPORTED_ALGORITHM, null, null, e3
            );
        }

        Vector encDataRef = new Vector();

        boolean cloneKeyInfo = false;
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
                body = (Element) WSSecurityUtil.findElement(document, elemName, nmSpace);
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
            encPart.setEncId(xencEncryptedDataId);

            cloneKeyInfo = true;
            
            if (keyInfo == null) {
                keyInfo = new KeyInfo(document);
                SecurityTokenReference secToken = new SecurityTokenReference(document);
                
                if (useKeyIdentifier && 
                        SecurityTokenReference.SAML_ID_URI.equals(customReferenceValue)) {
                    secToken.setSAMLKeyIdentifier((encKeyIdDirectId ? "":"#") + encKeyId);
                } else {
                   Reference ref = new Reference(document);
                   if (encKeyIdDirectId) {
                       ref.setURI(encKeyId);
                   } else {
                       ref.setURI("#" + encKeyId);                    
                   }
                   if (encKeyValueType != null) {
                       ref.setValueType(encKeyValueType);
                   }
                   secToken.setReference(ref);
                }
                
                keyInfo.addUnknownElement(secToken.getElement());
                Element keyInfoElement = keyInfo.getElement();
                keyInfoElement.setAttributeNS(
                    WSConstants.XMLNS_NS, "xmlns:" + WSConstants.SIG_PREFIX, WSConstants.SIG_NS
                );
            }
            //
            // Fourth step: encrypt data, and set necessary attributes in
            // xenc:EncryptedData
            //
            try {
                if (modifier.equals("Header")) {
                    
                    Element elem = 
                        doc.createElementNS(
                            WSConstants.WSSE11_NS, "wsse11:" + WSConstants.ENCRYPTED_HEADER
                        );
                    WSSecurityUtil.setNamespace(elem, WSConstants.WSSE11_NS, WSConstants.WSSE11_PREFIX);
                    String wsuPrefix = 
                        WSSecurityUtil.setNamespace(elem, WSConstants.WSU_NS, WSConstants.WSU_PREFIX);
                    elem.setAttributeNS(
                        WSConstants.WSU_NS, wsuPrefix + ":Id", 
                        wssConfig.getIdAllocator().createId("EncHeader-", body)
                    );
                    
                    NamedNodeMap map = body.getAttributes();
                    
                    for (int i = 0 ; i < map.getLength() ; i++) {
                        Attr attr = (Attr)map.item(i);
                        if (attr.getNamespaceURI().equals(WSConstants.URI_SOAP11_ENV)
                            || attr.getNamespaceURI().equals(WSConstants.URI_SOAP12_ENV)) {                         
                            String soapEnvPrefix = 
                                WSSecurityUtil.setNamespace(
                                    elem, attr.getNamespaceURI(), WSConstants.DEFAULT_SOAP_PREFIX
                                );
                            elem.setAttributeNS(
                                attr.getNamespaceURI(), 
                                soapEnvPrefix + ":" + attr.getLocalName(), 
                                attr.getValue()
                            );
                        }
                    }
                    
                    xmlCipher.init(XMLCipher.ENCRYPT_MODE, secretKey);
                    EncryptedData encData = xmlCipher.getEncryptedData();
                    encData.setId(xencEncryptedDataId);
                    encData.setKeyInfo(keyInfo);
                    xmlCipher.doFinal(doc, body, content);
                    
                    Element encDataElem = 
                        WSSecurityUtil.findElementById(
                            document.getDocumentElement(), xencEncryptedDataId, null
                        );
                    Node clone = encDataElem.cloneNode(true);
                    elem.appendChild(clone);
                    encDataElem.getParentNode().appendChild(elem);
                    encDataElem.getParentNode().removeChild(encDataElem); 
                } else {
                    xmlCipher.init(XMLCipher.ENCRYPT_MODE, secretKey);
                    EncryptedData encData = xmlCipher.getEncryptedData();
                    encData.setId(xencEncryptedDataId);
                    encData.setKeyInfo(keyInfo);
                    xmlCipher.doFinal(doc, body, content);          
                }
                if (cloneKeyInfo) {
                    keyInfo = new KeyInfo((Element) keyInfo.getElement().cloneNode(true), null);
                }
            } catch (Exception e2) {
                throw new WSSecurityException(
                    WSSecurityException.FAILED_ENCRYPTION, null, null, e2
                );
            }
            encDataRef.add("#" + xencEncryptedDataId);
        }
        return encDataRef;
    }

    private Document buildEmbedded(Document doc, WSSecHeader secHeader) 
        throws WSSecurityException {
        doDebug = log.isDebugEnabled();

        if (doDebug) {
            log.debug("Beginning Encryption embedded...");
        }
        envelope = doc.getDocumentElement();
        envelope.setAttributeNS(
            WSConstants.XMLNS_NS, "xmlns:" + WSConstants.ENC_PREFIX, WSConstants.ENC_NS
        );

        //
        // Second step: generate a symmetric key from the specified key
        // (password) for this algorithm, and set the cipher into encryption
        // mode.
        //
        if (this.symmetricKey == null) {
            if (embeddedKey == null) {
                throw new WSSecurityException(WSSecurityException.FAILURE, "noKeySupplied");
            }
            this.symmetricKey = WSSecurityUtil.prepareSecretKey(symEncAlgo, embeddedKey);
        }

        KeyInfo keyInfo = null;
        if (this.keyIdentifierType == WSConstants.EMBEDDED_KEYNAME) {
            keyInfo = new KeyInfo(doc);
            keyInfo.addKeyName(embeddedKeyName == null ? user : embeddedKeyName);
        } else if (this.keyIdentifierType == WSConstants.EMBED_SECURITY_TOKEN_REF) {
            //
            // This means that we want to embed a <wsse:SecurityTokenReference>
            // into keyInfo element. If we need this functionality, this.secRef
            // MUST be set before calling the build(doc, crypto) method. So if
            // secRef is null then throw an exception.
            //
            if (this.securityTokenReference == null) {
                throw new WSSecurityException(
                    WSSecurityException.SECURITY_TOKEN_UNAVAILABLE,
                    "You must set keyInfo element, if the keyIdentifier == EMBED_SECURITY_TOKEN_REF"
                 );
            } else {
                keyInfo = new KeyInfo(doc);
                Element tmpE = securityTokenReference.getElement();
                tmpE.setAttributeNS(
                    WSConstants.XMLNS_NS, "xmlns:" + tmpE.getPrefix(), tmpE.getNamespaceURI()
                );
                keyInfo.addUnknownElement(securityTokenReference.getElement());
            }
        }
        Element keyInfoElement = keyInfo.getElement();
        keyInfoElement.setAttributeNS(
            WSConstants.XMLNS_NS, "xmlns:" + WSConstants.SIG_PREFIX, WSConstants.SIG_NS
        );

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
        Vector encDataRefs = doEncryption(doc, this.symmetricKey, keyInfo, parts);

        //
        // At this point data is encrypted with the symmetric key and can be
        // referenced via the above Id
        //

        //
        // Now we need to setup the wsse:Security header block 1) get (or
        // create) the wsse:Security header block 2) The last step sets up the
        // reference list that pints to the encrypted data
        //
        Element wsseSecurity = secHeader.getSecurityHeader();

        Element referenceList = 
            doc.createElementNS(WSConstants.ENC_NS, WSConstants.ENC_PREFIX + ":ReferenceList");
        referenceList = createDataRefList(doc, referenceList, encDataRefs);
        WSSecurityUtil.prependChildElement(wsseSecurity, referenceList);

        return doc;
    }

    private KeyGenerator getKeyGenerator() throws WSSecurityException {
        try {
            //
            // Assume AES as default, so initialize it
            //
            String keyAlgorithm = JCEMapper.getJCEKeyAlgorithmFromURI(symEncAlgo);
            KeyGenerator keyGen = KeyGenerator.getInstance(keyAlgorithm);
            if (symEncAlgo.equalsIgnoreCase(WSConstants.AES_128)) {
                keyGen.init(128);
            } else if (symEncAlgo.equalsIgnoreCase(WSConstants.AES_192)) {
                keyGen.init(192);
            } else if (symEncAlgo.equalsIgnoreCase(WSConstants.AES_256)) {
                keyGen.init(256);
            }
            return keyGen;
        } catch (NoSuchAlgorithmException e) {
            throw new WSSecurityException(
                WSSecurityException.UNSUPPORTED_ALGORITHM, null, null, e
            );
        }
    }

    /**
     * Create DOM subtree for <code>xenc:EncryptedKey</code>
     * 
     * @param doc the SOAP envelope parent document
     * @param referenceList
     * @param encDataRefs
     * @return an <code>xenc:EncryptedKey</code> element
     */
    public static Element createDataRefList(
        Document doc,
        Element referenceList, 
        Vector encDataRefs
    ) {
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

    /**
     * @return The symmetric key
     */
    public SecretKey getSymmetricKey() {
        return symmetricKey;
    }

    /**
     * Set the symmetric key to be used for encryption
     * 
     * @param key
     */
    public void setSymmetricKey(SecretKey key) {
        this.symmetricKey = key;
    }

    /**
     * @return Return the SecurityTokenRefernce
     */
    public SecurityTokenReference getSecurityTokenReference() {
        return securityTokenReference;
    }

    /**
     * @param reference
     */
    public void setSecurityTokenReference(SecurityTokenReference reference) {
        securityTokenReference = reference;
    }

    public boolean isEncryptSymmKey() {
        return encryptSymmKey;
    }
    
    public void setEncryptSymmKey(boolean encryptSymmKey) {
        this.encryptSymmKey = encryptSymmKey;
    }
    
    private String getSHA1(byte[] input) throws WSSecurityException {
        try {
            MessageDigest sha = null;
            sha = MessageDigest.getInstance("SHA-1");
            sha.reset();
            sha.update(input);
            byte[] data = sha.digest();
            
            return Base64.encode(data);
        } catch (NoSuchAlgorithmException e) {
            throw new WSSecurityException(
                WSSecurityException.UNSUPPORTED_ALGORITHM, null, null, e
            );
        }
    }
    
    public void setCustomReferenceValue(String customReferenceValue) {
        this.customReferenceValue = customReferenceValue;
    }
    
    public void setEncKeyValueType(String e) {
        encKeyValueType = e;
    }
    
    public void setEncKeyIdDirectId(boolean b) {
        encKeyIdDirectId = b;
    }
}
