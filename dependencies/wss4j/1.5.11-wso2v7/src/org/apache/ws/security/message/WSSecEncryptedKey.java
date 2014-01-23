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

import java.security.InvalidKeyException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.message.token.BinarySecurity;
import org.apache.ws.security.message.token.Reference;
import org.apache.ws.security.message.token.SecurityTokenReference;
import org.apache.ws.security.message.token.X509Security;
import org.apache.ws.security.util.UUIDGenerator;
import org.apache.ws.security.util.WSSecurityUtil;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.keys.content.X509Data;
import org.apache.xml.security.keys.content.x509.XMLX509IssuerSerial;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * Builder class to build an EncryptedKey.
 * 
 * This is especially useful in the case where the same
 * <code>EncryptedKey</code> has to be used to sign and encrypt the message In
 * such a situation this builder will add the <code>EncryptedKey</code> to the
 * security header and we can use the information form the builder to provide to
 * other builders to reference to the token
 */
public class WSSecEncryptedKey extends WSSecBase {

    private static Log log = LogFactory.getLog(WSSecEncryptedKey.class.getName());

    protected Document document;

    /**
     * soap:Envelope element
     */
    protected Element envelope = null;

    /**
     * Session key used as the secret in key derivation
     */
    protected byte[] ephemeralKey;

    /**
     * Encrypted bytes of the ephemeral key
     */
    protected byte[] encryptedEphemeralKey;
    
    /**
     * Remote user's alias to obtain the cert to encrypt the ephemeral key
     */
    protected String encrUser = null;

    /**
     * Algorithm used to encrypt the ephemeral key
     */
    protected String keyEncAlgo = WSConstants.KEYTRANSPORT_RSA15;

    /**
     * xenc:EncryptedKey element
     */
    protected Element encryptedKeyElement = null;

    /**
     * The Token identifier of the token that the <code>DerivedKeyToken</code>
     * is (or to be) derived from.
     */
    protected String encKeyId = null;

    /**
     * Custom token value
     */
    protected String customEKTokenValueType;
    
    /**
     * Custom token id
     */
    protected String customEKTokenId;
    
    /**
     * BinarySecurityToken to be included in the case where BST_DIRECT_REFERENCE
     * is used to refer to the asymmetric encryption cert
     */
    protected BinarySecurity bstToken = null;
    
    protected X509Certificate useThisCert = null;
    
    /**
     * Key size in bits
     * Defaults to 128
     */
    protected int keySize = 128;

    /**
     * Set the user name to get the encryption certificate.
     * 
     * The public key of this certificate is used, thus no password necessary.
     * The user name is a keystore alias usually.
     * 
     * @param user
     */
    public void setUserInfo(String user) {
        this.user = user;
    }

    /**
     * Get the id generated during <code>prepare()</code>.
     * 
     * Returns the the value of wsu:Id attribute of the EncryptedKey element.
     * 
     * @return Return the wsu:Id of this token or null if <code>prepare()</code>
     *         was not called before.
     */
    public String getId() {
        return encKeyId;
    }

    /**
     * Prepare the ephemeralKey and the tokens required to be added to the
     * security header
     * 
     * @param doc The SOAP envelope as <code>Document</code>
     * @param crypto An instance of the Crypto API to handle keystore and certificates
     * @throws WSSecurityException
     */
    public void prepare(Document doc, Crypto crypto) throws WSSecurityException {

        document = doc;

        //
        // Set up the ephemeral key
        //
        if (this.ephemeralKey == null) {
            this.ephemeralKey = generateEphemeralKey();
        }

        //
        // Get the certificate that contains the public key for the public key
        // algorithm that will encrypt the generated symmetric (session) key.
        //
        X509Certificate remoteCert = null;
        if (useThisCert != null) {
            remoteCert = useThisCert;
        } else {
            X509Certificate[] certs = crypto.getCertificates(user);
            if (certs == null || certs.length <= 0) {
                throw new WSSecurityException(
                    WSSecurityException.FAILURE,
                    "noUserCertsFound", 
                    new Object[] {user, "encryption"}
                );
            }
            remoteCert = certs[0];
        }
        
        prepareInternal(ephemeralKey, remoteCert, crypto);
    }

    /**
     * Encrypt the symmetric key data and prepare the EncryptedKey element
     * 
     * This method does the most work for to prepare the EncryptedKey element.
     * It is also used by the WSSecEncrypt sub-class.
     * 
     * @param keyBytes The bytes that represent the symmetric key
     * @param remoteCert The certificate that contains the public key to encrypt the
     *                   symmetric key data
     * @param crypto An instance of the Crypto API to handle keystore and certificates
     * @throws WSSecurityException
     */
    protected void prepareInternal(
        byte[] keyBytes, 
        X509Certificate remoteCert,
        Crypto crypto
    ) throws WSSecurityException {
        String certUri = UUIDGenerator.getUUID();
        Cipher cipher = WSSecurityUtil.getCipherInstance(keyEncAlgo);
        try {
            cipher.init(Cipher.ENCRYPT_MODE, remoteCert.getPublicKey());
        } catch (InvalidKeyException e) {
            throw new WSSecurityException(
                WSSecurityException.FAILED_ENCRYPTION, null, null, e
            );
        }
        if (doDebug) {
            log.debug(
                "cipher blksize: " + cipher.getBlockSize()
                + ", symm key length: " + keyBytes.length
            );
        }
        int blockSize = cipher.getBlockSize();
        if (blockSize > 0 && blockSize < keyBytes.length) {
            throw new WSSecurityException(
                WSSecurityException.FAILURE,
                "unsupportedKeyTransp",
                new Object[] {"public key algorithm too weak to encrypt symmetric key"}
            );
        }
        
        try {
            this.encryptedEphemeralKey = cipher.doFinal(keyBytes);
        } catch (IllegalStateException e1) {
            throw new WSSecurityException(
                WSSecurityException.FAILED_ENCRYPTION, null, null, e1
            );
        } catch (IllegalBlockSizeException e1) {
            throw new WSSecurityException(
                WSSecurityException.FAILED_ENCRYPTION, null, null, e1
            );
        } catch (BadPaddingException e1) {
            throw new WSSecurityException(
                WSSecurityException.FAILED_ENCRYPTION, null, null, e1
            );
        }
        Text keyText = 
            WSSecurityUtil.createBase64EncodedTextNode(document, this.encryptedEphemeralKey);

        //
        // Now we need to setup the EncryptedKey header block 1) create a
        // EncryptedKey element and set a wsu:Id for it 2) Generate ds:KeyInfo
        // element, this wraps the wsse:SecurityTokenReference 3) Create and set
        // up the SecurityTokenReference according to the keyIdentifier parameter
        // 4) Create the CipherValue element structure and insert the encrypted
        // session key
        //
        encryptedKeyElement = createEncryptedKey(document, keyEncAlgo);
        if(this.encKeyId == null || "".equals(this.encKeyId)) {
            this.encKeyId = "EncKeyId-" + UUIDGenerator.getUUID();
        }
        encryptedKeyElement.setAttributeNS(null, "Id", this.encKeyId);

        KeyInfo keyInfo = new KeyInfo(document);

        SecurityTokenReference secToken = new SecurityTokenReference(document);

        switch (keyIdentifierType) {
        case WSConstants.X509_KEY_IDENTIFIER:
            secToken.setKeyIdentifier(remoteCert);
            break;

        case WSConstants.SKI_KEY_IDENTIFIER:
            secToken.setKeyIdentifierSKI(remoteCert, crypto);
            break;

        case WSConstants.THUMBPRINT_IDENTIFIER:
            secToken.setKeyIdentifierThumb(remoteCert);
            break;
            
        case WSConstants.ENCRYPTED_KEY_SHA1_IDENTIFIER:
            //
            // This identifier is not applicable for this case, so fall back to
            // ThumbprintRSA.
            //
            secToken.setKeyIdentifierThumb(remoteCert);
            break;

        case WSConstants.ISSUER_SERIAL:
            XMLX509IssuerSerial data = new XMLX509IssuerSerial(document, remoteCert);
            X509Data x509Data = new X509Data(document);
            x509Data.add(data);
            secToken.setX509IssuerSerial(x509Data);
            break;

        case WSConstants.BST_DIRECT_REFERENCE:
            Reference ref = new Reference(document);
            ref.setURI("#" + certUri);
            bstToken = new X509Security(document);
            ((X509Security) bstToken).setX509Certificate(remoteCert);
            bstToken.setID(certUri);
            ref.setValueType(bstToken.getValueType());
            secToken.setReference(ref);
            break;
            
        case WSConstants.CUSTOM_KEY_IDENTIFIER:
            secToken.setKeyIdentifier(customEKTokenValueType, customEKTokenId);
            break;           

        default:
            throw new WSSecurityException(WSSecurityException.FAILURE, "unsupportedKeyId");
        }
        keyInfo.addUnknownElement(secToken.getElement());
        Element keyInfoElement = keyInfo.getElement();
        keyInfoElement.setAttributeNS(
            WSConstants.XMLNS_NS, "xmlns:" + WSConstants.SIG_PREFIX, WSConstants.SIG_NS
        );
        encryptedKeyElement.appendChild(keyInfoElement);

        Element xencCipherValue = createCipherValue(document, encryptedKeyElement);
        xencCipherValue.appendChild(keyText);

        envelope = document.getDocumentElement();
        envelope.setAttributeNS(
            WSConstants.XMLNS_NS, "xmlns:" + WSConstants.ENC_PREFIX, WSConstants.ENC_NS
        );
    }

    /**
     * Create an ephemeral key
     * 
     * @return an ephemeral key
     * @throws WSSecurityException
     */
    protected byte[] generateEphemeralKey() throws WSSecurityException {
        try {     
            final SecureRandom r = WSSecurityUtil.resolveSecureRandom();
            if (r == null) {
                throw new WSSecurityException("Random generator is not initialzed.");
            }
            byte[] temp = new byte[this.keySize / 8];
            r.nextBytes(temp);
            return temp;
        } catch (Exception e) {
            throw new WSSecurityException("Error in creating the ephemeral key", e);
        }
    }

    /**
     * Create DOM subtree for <code>xenc:EncryptedKey</code>
     * 
     * @param doc the SOAP envelope parent document
     * @param keyTransportAlgo specifies which algorithm to use to encrypt the symmetric key
     * @return an <code>xenc:EncryptedKey</code> element
     */
    protected Element createEncryptedKey(Document doc, String keyTransportAlgo) {
        Element encryptedKey = 
            doc.createElementNS(WSConstants.ENC_NS, WSConstants.ENC_PREFIX + ":EncryptedKey");

        WSSecurityUtil.setNamespace(encryptedKey, WSConstants.ENC_NS, WSConstants.ENC_PREFIX);
        Element encryptionMethod = 
            doc.createElementNS(WSConstants.ENC_NS, WSConstants.ENC_PREFIX + ":EncryptionMethod");
        encryptionMethod.setAttributeNS(null, "Algorithm", keyTransportAlgo);
        encryptedKey.appendChild(encryptionMethod);
        return encryptedKey;
    }
    
    /**
     * Create DOM subtree for <code>xenc:EncryptedKey</code>
     * 
     * @param doc the SOAP envelope parent document
     * @param keyTransportAlgo specifies which algorithm to use to encrypt the symmetric key
     * @return an <code>xenc:EncryptedKey</code> element
     * @deprecated use createEncryptedKey(Document doc, String keyTransportAlgo) instead
     */
    protected Element createEnrcyptedKey(Document doc, String keyTransportAlgo) {
        return createEncryptedKey(doc, keyTransportAlgo);
    }

    protected Element createCipherValue(Document doc, Element encryptedKey) {
        Element cipherData = 
            doc.createElementNS(WSConstants.ENC_NS, WSConstants.ENC_PREFIX + ":CipherData");
        Element cipherValue = 
            doc.createElementNS(WSConstants.ENC_NS, WSConstants.ENC_PREFIX + ":CipherValue");
        cipherData.appendChild(cipherValue);
        encryptedKey.appendChild(cipherData);
        return cipherValue;
    }

    /**
     * Prepend the EncryptedKey element to the elements already in the Security
     * header.
     * 
     * The method can be called any time after <code>prepare()</code>. This
     * allows to insert the EncryptedKey element at any position in the Security
     * header.
     * 
     * @param secHeader The security header that holds the Signature element.
     */
    public void prependToHeader(WSSecHeader secHeader) {
        WSSecurityUtil.prependChildElement(secHeader.getSecurityHeader(), encryptedKeyElement);
    }

    /**
     * Append the EncryptedKey element to the elements already in the Security
     * header.
     * 
     * The method can be called any time after <code>prepare()</code>. This
     * allows to insert the EncryptedKey element at any position in the Security
     * header.
     * 
     * @param secHeader The security header that holds the Signature element.
     */
    public void appendToHeader(WSSecHeader secHeader) {
        Element secHeaderElement = secHeader.getSecurityHeader();
        secHeaderElement.appendChild(encryptedKeyElement);
    }
    
    /**
     * Prepend the BinarySecurityToken to the elements already in the Security
     * header.
     * 
     * The method can be called any time after <code>prepare()</code>. This
     * allows to insert the BST element at any position in the Security header.
     * 
     * @param secHeader The security header that holds the BST element.
     */
    public void prependBSTElementToHeader(WSSecHeader secHeader) {
        if (bstToken != null) {
            WSSecurityUtil.prependChildElement(
                secHeader.getSecurityHeader(), bstToken.getElement()
            );
        }
        bstToken = null;
    }

    /**
     * Append the BinarySecurityToken to the elements already in the Security
     * header.
     * 
     * The method can be called any time after <code>prepare()</code>. This
     * allows to insert the BST element at any position in the Security header.
     * 
     * @param secHeader The security header that holds the BST element.
     */
    public void appendBSTElementToHeader(WSSecHeader secHeader) {
        if (bstToken != null) {
            Element secHeaderElement = secHeader.getSecurityHeader();
            secHeaderElement.appendChild(bstToken.getElement());
        }
        bstToken = null;
    }
    
    /**
     * @return Returns the ephemeralKey.
     */
    public byte[] getEphemeralKey() {
        return ephemeralKey;
    }
    
    /**
     * Set the X509 Certificate to use for encryption.
     * 
     * If this is set <b>and</b> the key identifier is set to
     * <code>DirectReference</code> then use this certificate to get the
     * public key for encryption.
     * 
     * @param cert is the X509 certificate to use for encryption
     */
    public void setUseThisCert(X509Certificate cert) {
        useThisCert = cert;
    }

    /**
     * @return Returns the encryptedKeyElement.
     */
    public Element getEncryptedKeyElement() {
        return encryptedKeyElement;
    }
    
    /**
     * Set the encrypted key element when a pre prepared encrypted key is used
     * @param encryptedKeyElement EncryptedKey element of the encrypted key used
     */
    public void setEncryptedKeyElement(Element encryptedKeyElement) {
        this.encryptedKeyElement = encryptedKeyElement;
    }
    
    /**
     * @return Returns the BinarySecurityToken element.
     */
    public Element getBinarySecurityTokenElement() {
        if (this.bstToken != null) {
            return this.bstToken.getElement();
        }
        return null;
    }

    public void setKeySize(int keySize) throws WSSecurityException {
        if (keySize < 64) {
            // Minimum size has to be 64 bits - E.g. A DES key
            throw new WSSecurityException("invalidKeySize");
        }
        this.keySize = keySize;
    }

    public void setKeyEncAlgo(String keyEncAlgo) {
        this.keyEncAlgo = keyEncAlgo;
    }

    /**
     * @param ephemeralKey The ephemeralKey to set.
     */
    public void setEphemeralKey(byte[] ephemeralKey) {
        this.ephemeralKey = ephemeralKey;
    }
    
    /**
     * Get the id of the BSt generated  during <code>prepare()</code>.
     * 
     * @return Returns the the value of wsu:Id attribute of the 
     * BinaruSecurityToken element.
     */
    public String getBSTTokenId() {
        if (this.bstToken == null) {
            return null;
        }
        
        return this.bstToken.getID();
    }

    /**
     * @param document The document to set.
     */
    public void setDocument(Document document) {
        this.document = document;
    }

    /**
     * @param encKeyId The encKeyId to set.
     */
    public void setEncKeyId(String encKeyId) {
        this.encKeyId = encKeyId;
    }
    
    public boolean isCertSet() {
        return (useThisCert == null ? true : false) ;
    }

    public byte[] getEncryptedEphemeralKey() {
        return encryptedEphemeralKey;
    }
    
    public void setCustomEKTokenValueType(String customEKTokenValueType) {
        this.customEKTokenValueType = customEKTokenValueType;
    }

    public void setCustomEKTokenId(String customEKTokenId) {
        this.customEKTokenId = customEKTokenId;
    }
}
