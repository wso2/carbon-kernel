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

import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSDocInfo;
import org.apache.ws.security.WSSConfig;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.message.token.BinarySecurity;
import org.apache.ws.security.message.token.PKIPathSecurity;
import org.apache.ws.security.message.token.X509Security;
import org.w3c.dom.Element;

import javax.security.auth.callback.CallbackHandler;

import java.security.cert.X509Certificate;
import java.util.Vector;


/**
 * Processor implementation to handle wsse:BinarySecurityToken elements
 */
public class BinarySecurityTokenProcessor  implements Processor {

    /**
     * Token Id
     */
    private String id;
    
    /**
     * Token type
     */
    private String type;
    
    /**
     * Certificates carried in this token
     */
    private X509Certificate[] certificates;
    
    /**
     * Token object representing the token
     */
    private BinarySecurity token;
    
    /**
     * {@inheritDoc}
     */
    public String getId() {
        return this.id;
    }
    
    /**
     * {@inheritDoc}
     */
    public void handleToken(
        Element elem, 
        Crypto crypto, 
        Crypto decCrypto,
        CallbackHandler cb, 
        WSDocInfo wsDocInfo, 
        Vector returnResults,
        WSSConfig config
    ) throws WSSecurityException {
        if (crypto == null) {
            this.getCertificatesTokenReference(elem, decCrypto);
        } else {
            this.getCertificatesTokenReference(elem, crypto);
        }
        returnResults.add(
            0, 
            new WSSecurityEngineResult(WSConstants.BST, this.token, this.certificates)
        );
        id = elem.getAttributeNS(WSConstants.WSU_NS, "Id");
    }
    
    /**
     * Extracts the certificate(s) from the Binary Security token reference.
     *
     * @param elem The element containing the binary security token. This is
     *             either X509 certificate(s) or a PKIPath. Any other token type
     *             is ignored.
     * @throws WSSecurityException
     */
    private void getCertificatesTokenReference(Element elem, Crypto crypto)
        throws WSSecurityException {
        this.createSecurityToken(elem);
        if (token instanceof PKIPathSecurity) {
            this.certificates = ((PKIPathSecurity) token).getX509Certificates(false, crypto);
        } else if (token instanceof X509Security) {
            X509Certificate cert = ((X509Security) token).getX509Certificate(crypto);
            this.certificates = new X509Certificate[1];
            this.certificates[0] = cert;
        }
    }

    /**
     * Checks the <code>element</code> and creates appropriate binary security object.
     *
     * @param element The XML element that contains either a <code>BinarySecurityToken
     *                </code> or a <code>PKIPath</code> element.
     * @throws WSSecurityException
     */
    private void createSecurityToken(Element element) throws WSSecurityException {
        this.token = new BinarySecurity(element);
        type = token.getValueType();

        if (X509Security.X509_V3_TYPE.equals(type)) {
            this.token = new X509Security(element);
        } else if (PKIPathSecurity.getType().equals(type)) {
            this.token = new PKIPathSecurity(element);
        } 
    }

    public String getType() {
        return type;
    }

    public X509Certificate[] getCertificates() {
        return certificates;
    }

    public BinarySecurity getToken() {
        return token;
    }

}
