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

package org.apache.ws.security.processor;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Vector;

import javax.crypto.SecretKey;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSDataRef;
import org.apache.ws.security.WSDocInfo;
import org.apache.ws.security.WSParameterCallback;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSConfig;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.message.token.Reference;
import org.apache.ws.security.message.token.SecurityTokenReference;
import org.apache.ws.security.saml.SAML2KeyInfo;
import org.apache.ws.security.saml.SAML2Util;
import org.apache.ws.security.saml.SAMLKeyInfo;
import org.apache.ws.security.saml.SAMLUtil;
import org.apache.ws.security.util.WSSecurityUtil;
import org.apache.xml.security.encryption.XMLCipher;
import org.apache.xml.security.encryption.XMLEncryptionException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ReferenceListProcessor implements Processor {
    private static Log log = 
        LogFactory.getLog(ReferenceListProcessor.class.getName());

    private boolean debug = false;
    WSDocInfo wsDocInfo = null;
    Principal krbPricipal;


    public void handleToken(
        Element elem, 
        Crypto crypto, 
        Crypto decCrypto,
        CallbackHandler cb, 
        WSDocInfo wdi, 
        Vector returnResults,
        WSSConfig wsc
    ) throws WSSecurityException {

        debug = log.isDebugEnabled();
        if (debug) {
            log.debug("Found reference list element");
        }
        if (cb == null) {
            throw new WSSecurityException(WSSecurityException.FAILURE, "noCallback");
        }
        wsDocInfo = wdi;
        ArrayList uris = handleReferenceList(elem, cb, decCrypto);
        if (krbPricipal!=null) {
        	WSSecurityEngineResult secResults = null;
        	secResults = new WSSecurityEngineResult(WSConstants.KERBEROS_ENCR,
					uris);
        	secResults.put(WSSecurityEngineResult.TAG_PRINCIPAL, krbPricipal);
        	returnResults.add(0, secResults);
 		} else {
 			 returnResults.add(
 		            0,
 		            new WSSecurityEngineResult(WSConstants.ENCR, uris)
 		        );
		}      
    }

    /**
     * Dereferences and decodes encrypted data elements.
     * 
     * @param elem contains the <code>ReferenceList</code> to the encrypted
     *             data elements
     * @param cb the callback handler to get the key for a key name stored if
     *           <code>KeyInfo</code> inside the encrypted data elements
     */
    private ArrayList handleReferenceList(
        Element elem, 
        CallbackHandler cb,
        Crypto crypto
    ) throws WSSecurityException {
        Node tmpE = null;
        ArrayList dataRefUris = new ArrayList();
        for (tmpE = elem.getFirstChild(); 
            tmpE != null; 
            tmpE = tmpE.getNextSibling()
        ) {
            if (tmpE.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            if (!tmpE.getNamespaceURI().equals(WSConstants.ENC_NS)) {
                continue;
            }
            if (tmpE.getLocalName().equals("DataReference")) {
                String dataRefURI = ((Element) tmpE).getAttribute("URI");
                if (dataRefURI.charAt(0) == '#') {
                    dataRefURI = dataRefURI.substring(1);
                }
                WSDataRef dataRef = 
                    decryptDataRefEmbedded(elem.getOwnerDocument(), dataRefURI, cb, crypto);
                dataRefUris.add(dataRef);
            }
        }
        
        return dataRefUris;
    }

    
    /**
     * Decrypt an (embedded) EncryptedData element referenced by dataRefURI.
     */
    private WSDataRef decryptDataRefEmbedded(
        Document doc, 
        String dataRefURI, 
        CallbackHandler cb, 
        Crypto crypto
    ) throws WSSecurityException {
        if (log.isDebugEnabled()) {
            log.debug("Found data reference: " + dataRefURI);
        }
        //
        // Find the encrypted data element referenced by dataRefURI
        //
        Element encryptedDataElement = findEncryptedDataElement(doc, dataRefURI);
        //
        // Prepare the SecretKey object to decrypt EncryptedData
        //
        String symEncAlgo = X509Util.getEncAlgo(encryptedDataElement);
        Element keyInfoElement = 
            (Element)WSSecurityUtil.getDirectChildElement(
                encryptedDataElement, "KeyInfo", WSConstants.SIG_NS
            );
        if (keyInfoElement == null) {
            throw new WSSecurityException(WSSecurityException.INVALID_SECURITY, "noKeyinfo");
        }
        //
        // Try to get a security reference token, if none found try to get a
        // shared key using a KeyName.
        //
        Element secRefToken = 
            WSSecurityUtil.getDirectChildElement(
                keyInfoElement, "SecurityTokenReference", WSConstants.WSSE_NS
            );
        SecretKey symmetricKey = null;
        if (secRefToken == null) {
            symmetricKey = X509Util.getSharedKey(keyInfoElement, symEncAlgo, cb);
        } else {
            symmetricKey = 
                getKeyFromSecurityTokenReference(secRefToken, symEncAlgo, crypto, cb);
        }
        
        return 
            decryptEncryptedData(
                doc, dataRefURI, encryptedDataElement, symmetricKey, symEncAlgo
            );
    }

    
    /**
     * Look up the encrypted data. First try wsu:Id="someURI". If no such Id then try the 
     * generic lookup to find Id="someURI"
     * 
     * @param doc The document in which to find EncryptedData
     * @param dataRefURI The URI of EncryptedData
     * @return The EncryptedData element
     * @throws WSSecurityException if the EncryptedData element referenced by dataRefURI is 
     * not found
     */
    public static Element
    findEncryptedDataElement(
        Document doc,
        String dataRefURI
    ) throws WSSecurityException {
        Element encryptedDataElement = WSSecurityUtil.getElementByWsuId(doc, dataRefURI);
        if (encryptedDataElement == null) {   
            encryptedDataElement = WSSecurityUtil.getElementByGenId(doc, dataRefURI);
        }
        if (encryptedDataElement == null) {
            throw new WSSecurityException(
                WSSecurityException.INVALID_SECURITY, "dataRef", new Object[] {dataRefURI}
            );
        }
        return encryptedDataElement;
    }

    
    /**
     * Decrypt the EncryptedData argument using a SecretKey.
     * @param doc The (document) owner of EncryptedData
     * @param dataRefURI The URI of EncryptedData
     * @param encData The EncryptedData element
     * @param symmetricKey The SecretKey with which to decrypt EncryptedData
     * @param symEncAlgo The symmetric encryption algorithm to use
     * @throws WSSecurityException
     */
    public static WSDataRef
    decryptEncryptedData(
        Document doc,
        String dataRefURI,
        Element encData,
        SecretKey symmetricKey,
        String symEncAlgo
    ) throws WSSecurityException {
        XMLCipher xmlCipher = null;
        try {
            xmlCipher = XMLCipher.getInstance(symEncAlgo);
            xmlCipher.init(XMLCipher.DECRYPT_MODE, symmetricKey);
        } catch (XMLEncryptionException ex) {
            throw new WSSecurityException(
                WSSecurityException.UNSUPPORTED_ALGORITHM, null, null, ex
            );
        }

        WSDataRef dataRef = new WSDataRef(dataRefURI);
        dataRef.setWsuId(dataRefURI);
        dataRef.setAlgorithm(symEncAlgo);
        boolean content = X509Util.isContent(encData);
        dataRef.setContent(content);
        
        Node parent = encData.getParentNode();
        Node previousSibling = encData.getPreviousSibling();
        if (content) {
            encData = (Element) encData.getParentNode();
            parent = encData.getParentNode();
        }
        
        try {
            xmlCipher.doFinal(doc, encData, content);
        } catch (Exception ex) {
            throw new WSSecurityException(WSSecurityException.FAILED_CHECK, null, null, ex);
        }
        
        if (parent.getLocalName().equals(WSConstants.ENCRYPTED_HEADER)
            && parent.getNamespaceURI().equals(WSConstants.WSSE11_NS)) {
                
            Node decryptedHeader = parent.getFirstChild();
            Element decryptedHeaderClone = (Element)decryptedHeader.cloneNode(true);            
            parent.getParentNode().appendChild(decryptedHeaderClone);
            parent.getParentNode().removeChild(parent);
            dataRef.setProtectedElement(decryptedHeaderClone);
            dataRef.setXpath(getXPath(decryptedHeaderClone));
        } else if (content) {
            dataRef.setProtectedElement(encData);
            dataRef.setXpath(getXPath(encData));
        } else {
            Node decryptedNode;
            if (previousSibling == null) {
                decryptedNode = parent.getFirstChild();
            } else {
                decryptedNode = previousSibling.getNextSibling();
            }
            if (decryptedNode != null && Node.ELEMENT_NODE == decryptedNode.getNodeType()) {
                dataRef.setProtectedElement((Element)decryptedNode);
            }
            dataRef.setXpath(getXPath(decryptedNode));
        }
        
        return dataRef;
    }
    

    /**
     * Retrieves a secret key (session key) from a already parsed EncryptedKey
     * element
     * 
     * This method takes a security token reference (STR) element and checks if
     * it contains a Reference element. Then it gets the vale of the URI
     * attribute of the Reference and uses the retrieved value to lookup an
     * EncrypteKey element to get the decrypted session key bytes. Using the
     * algorithm parameter these bytes are converted into a secret key.
     * 
     * This method requires that the EncyrptedKey element is already available,
     * thus requires a strict layout of the security header. This method
     * supports EncryptedKey elements within the same message.
     * 
     * @param secRefToken The element containing the STR
     * @param algorithm A string that identifies the symmetric decryption algorithm
     * @param crypto Crypto instance to obtain key
     * @param cb CAllback handler to obtain the key passwords
     * @return The secret key for the specified algorithm
     * @throws WSSecurityException
     */
    private SecretKey getKeyFromSecurityTokenReference(
        Element secRefToken, 
        String algorithm,
        Crypto crypto, 
        CallbackHandler cb
    ) throws WSSecurityException {

        SecurityTokenReference secRef = new SecurityTokenReference(secRefToken);
        byte[] decryptedData = null;

        if (secRef.containsReference()) {
            Reference reference = secRef.getReference();
            String uri = reference.getURI();
            String id = uri;
            if (id.charAt(0) == '#') {
                id = id.substring(1);
            }
            Processor p = wsDocInfo.getProcessor(id);
            if (p instanceof EncryptedKeyProcessor) {
                EncryptedKeyProcessor ekp = (EncryptedKeyProcessor) p;
                decryptedData = ekp.getDecryptedBytes();
            } else if (p instanceof DerivedKeyTokenProcessor) {
                DerivedKeyTokenProcessor dkp = (DerivedKeyTokenProcessor) p;
                decryptedData = dkp.getKeyBytes(WSSecurityUtil.getKeyLength(algorithm));
            } else if (p instanceof SAMLTokenProcessor) {
                SAMLTokenProcessor samlp = (SAMLTokenProcessor) p;
                SAMLKeyInfo keyInfo = 
                    SAMLUtil.getSAMLKeyInfo(samlp.getSamlTokenElement(), crypto, cb);
                // TODO Handle malformed SAML tokens where they don't have the 
                // secret in them
                decryptedData = keyInfo.getSecret();
            }else if (p instanceof KerberosTokenProcessor) {
            	KerberosTokenProcessor krbp = (KerberosTokenProcessor) p;
				WSParameterCallback param = new WSParameterCallback(WSParameterCallback.KDC_DES_AES_FACTOR);
		        int factor = 0;
				  try {
					Callback[] callbacks = new Callback[] { param };
					cb.handle(callbacks);
					factor = param.getIntValue();
				} catch (Exception e) {
					//Ignore
					log.error("Error while executing parameter callback",e);
				}
				
				byte[] secret = krbp.getLastPrincipalFound()
						.getSessionKey();
				if (factor > 1) {
					byte[] newSecret = new byte[secret.length * factor];
					int j = 0;
					for (int i = 0; i < newSecret.length; i++) {
						newSecret[i] = secret[j++];
						if (j == secret.length)
							j = 0;
					}
					decryptedData = newSecret;
				} else {
					decryptedData = secret;
				}
					
				krbPricipal=krbp.getLastPrincipalFound();
			} else {
                // Try custom token
                WSPasswordCallback pwcb = new WSPasswordCallback(id, WSPasswordCallback.CUSTOM_TOKEN);
                try {
                    Callback[] callbacks = new Callback[]{pwcb};
                    cb.handle(callbacks);
                } catch (Exception e) {
                    throw new WSSecurityException(
                        WSSecurityException.FAILURE,
                        "noPassword", 
                        new Object[] {id}, 
                        e
                    );
                }
                decryptedData = pwcb.getKey();
                
                if (decryptedData == null) {
                    throw new WSSecurityException(
                        WSSecurityException.FAILED_CHECK, "unsupportedKeyId"
                    );
                }
            }
        } else if (secRef.containsKeyIdentifier()) {
            if (WSConstants.WSS_SAML_KI_VALUE_TYPE.equals(secRef.getKeyIdentifierValueType())) { 
                Element token = 
                    secRef.getKeyIdentifierTokenElement(secRefToken.getOwnerDocument(), wsDocInfo, cb);
                
                if (crypto == null) {
                    throw new WSSecurityException(
                        WSSecurityException.FAILURE, "noSigCryptoFile"
                    );
                }
                SAMLKeyInfo keyInfo = SAMLUtil.getSAMLKeyInfo(token, crypto, cb);
                // TODO Handle malformed SAML tokens where they don't have the 
                // secret in them
                decryptedData = keyInfo.getSecret();
			} else if (WSConstants.WSS_SAML2_KI_VALUE_TYPE.equals(secRef
					.getKeyIdentifierValueType())) {
				Element token = secRef.getKeyIdentifierTokenElement(
						secRefToken.getOwnerDocument(), wsDocInfo, cb);
				if (crypto == null) {
					throw new WSSecurityException(0, "noSigCryptoFile");
				}
				SAML2KeyInfo keyInfo = SAML2Util.getSAML2KeyInfo(token, crypto,
						cb);
				decryptedData = keyInfo.getSecret();
			} else {
                String sha = secRef.getKeyIdentifierValue();
                
                WSPasswordCallback pwcb = 
                    new WSPasswordCallback(
                        secRef.getKeyIdentifierValue(),
                        null,
                        secRef.getKeyIdentifierValueType(),
                        WSPasswordCallback.ENCRYPTED_KEY_TOKEN
                    );
                
                try {
                    Callback[] callbacks = new Callback[]{pwcb};
                    cb.handle(callbacks);
                } catch (Exception e) {
                    throw new WSSecurityException(
                        WSSecurityException.FAILURE,
                        "noPassword", 
                        new Object[] {sha}, 
                        e
                    );
                }
                decryptedData = pwcb.getKey();
            }
        } else {
            throw new WSSecurityException(WSSecurityException.FAILED_CHECK, "noReference");
        }
        return WSSecurityUtil.prepareSecretKey(algorithm, decryptedData);
    }
    
    public String getId() {
        return null;
    }
    
    
    /**
     * @param decryptedNode the decrypted node
     * @return a fully built xpath 
     *        (eg. &quot;/soapenv:Envelope/soapenv:Body/ns:decryptedElement&quot;)
     *        if the decryptedNode is an Element or an Attr node and is not detached
     *        from the document. <code>null</code> otherwise
     */
    public static String getXPath(Node decryptedNode) {
        if (decryptedNode == null) {
            return null;
        }

        String result = "";
        if (Node.ELEMENT_NODE == decryptedNode.getNodeType()) {
            result = decryptedNode.getNodeName();
            result = prependFullPath(result, decryptedNode.getParentNode());
        } else if (Node.ATTRIBUTE_NODE == decryptedNode.getNodeType()) {
            result = "@" + decryptedNode.getNodeName();
            result = prependFullPath(result, ((Attr)decryptedNode).getOwnerElement());
        } else {
            return null;
        }

        return result;
    }


    /**
     * Recursively build an absolute xpath (starting with the root &quot;/&quot;)
     * 
     * @param xpath the xpath expression built so far
     * @param node the current node whose name is to be prepended
     * @return a fully built xpath
     */
    private static String prependFullPath(String xpath, Node node) {
        if (node == null) {
            // probably a detached node... not really useful
            return null;
        } else if (Node.ELEMENT_NODE == node.getNodeType()) {
            xpath = node.getNodeName() + "/" + xpath;
            return prependFullPath(xpath, node.getParentNode());
        } else if (Node.DOCUMENT_NODE == node.getNodeType()) {
            return "/" + xpath;
        } else {
            return prependFullPath(xpath, node.getParentNode());
        }
    }

}
