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
import org.apache.ws.security.WSSConfig;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Vector;

/**
 * This is the base class for WS Security messages. It provides common functions
 * and fields used by the specific message classes such as sign, encrypt, and
 * username token.
 * 
 * @author Werner Dittmann (Werner.Dittmann@apache.org)
 */
public class WSSecBase {
    protected String user = null;

    protected String password = null;

    protected int keyIdentifierType = WSConstants.ISSUER_SERIAL;

    protected Vector parts = null;

    protected boolean doDebug = false;

    protected WSSConfig wssConfig = WSSConfig.getDefaultWSConfig();

    /**
     * Constructor.
     */
    public WSSecBase() {
    }

    /**
     * Set which parts of the message to encrypt/sign. <p/>
     * 
     * @param parts The vector containing the WSEncryptionPart objects
     */
    public void setParts(Vector parts) {
        this.parts = parts;
    }

    /**
     * Sets which key identifier to use. 
     * 
     * <p/> 
     * 
     * Defines the key identifier type to
     * use in the {@link WSSecSignature#prepare(Document, Crypto, WSSecHeader) method} or
     * the {@link WSSecEncrypt#prepare(Document, Crypto) method} function to
     * set up the key identification elements.
     * 
     * @param keyIdType
     * @see WSConstants#ISSUER_SERIAL
     * @see WSConstants#BST_DIRECT_REFERENCE
     * @see WSConstants#X509_KEY_IDENTIFIER
     * @see WSConstants#SKI_KEY_IDENTIFIER
     */
    public void setKeyIdentifierType(int keyIdType) {
        keyIdentifierType = keyIdType;
    }

    /**
     * Gets the value of the <code>keyIdentifierType</code>.
     * 
     * @return The <code>keyIdentifyerType</code>.
     * @see WSConstants#ISSUER_SERIAL
     * @see WSConstants#BST_DIRECT_REFERENCE
     * @see WSConstants#X509_KEY_IDENTIFIER
     * @see WSConstants#SKI_KEY_IDENTIFIER
     */
    public int getKeyIdentifierType() {
        return keyIdentifierType;
    }

    /**
     * @param wsConfig
     *            The wsConfig to set.
     */
    public void setWsConfig(WSSConfig wsConfig) {
        this.wssConfig = wsConfig;
    }

    /**
     * Looks up or adds a body id. <p/> First try to locate the
     * <code>wsu:Id</code> in the SOAP body element. If one is found, the
     * value of the <code>wsu:Id</code> attribute is returned. Otherwise the
     * method generates a new <code>wsu:Id</code> and an appropriate value.
     * 
     * @param doc The SOAP envelope as <code>Document</code>
     * @return The value of the <code>wsu:Id</code> attribute of the SOAP body
     * @throws Exception
     */
    protected String setBodyID(Document doc) throws Exception {
        SOAPConstants soapConstants = 
            WSSecurityUtil.getSOAPConstants(doc.getDocumentElement());
        Element bodyElement = 
            (Element) WSSecurityUtil.getDirectChild(
                doc.getFirstChild(), 
                soapConstants.getBodyQName().getLocalPart(),
                soapConstants.getEnvelopeURI()
            );
        if (bodyElement == null) {
            throw new Exception("SOAP Body Element node not found");
        }
        return setWsuId(bodyElement);
    }

    protected String setWsuId(Element bodyElement) {
        String id = bodyElement.getAttributeNS(WSConstants.WSU_NS, "Id");

        String newAttrNs = WSConstants.WSU_NS;
        String newAttrPrefix = WSConstants.WSU_PREFIX;

        if ((id == null || id.length() == 0)
            && WSConstants.ENC_NS.equals(bodyElement.getNamespaceURI())
            && (WSConstants.ENC_DATA_LN.equals(bodyElement.getLocalName())
                    || WSConstants.ENC_KEY_LN.equals(bodyElement.getLocalName()))
        ) {
            // If it is an XML-Enc derived element, it may already have an ID,
            // plus it is not schema valid to add an additional ID.
            id = bodyElement.getAttribute("Id");
            newAttrPrefix = WSConstants.ENC_PREFIX;
            newAttrNs = WSConstants.ENC_NS;
        }
        
        if ((id == null) || (id.length() == 0)) {
            id = wssConfig.getIdAllocator().createId("id-", bodyElement);
            String prefix = 
                WSSecurityUtil.setNamespace(bodyElement, newAttrNs, newAttrPrefix);
            bodyElement.setAttributeNS(newAttrNs, prefix + ":Id", id);
        }
        return id;
    }

    /**
     * Set the user and password info. 
     * 
     * Both information is used to get the user's private signing key.
     * 
     * @param user
     *            This is the user's alias name in the keystore that identifies
     *            the private key to sign the document
     * @param password
     *            The user's password to get the private signing key from the
     *            keystore
     */
    public void setUserInfo(String user, String password) {
        this.user = user;
        this.password = password;
    }
}
