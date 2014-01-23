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

package org.apache.ws.security.action;

import java.util.Vector;

import org.apache.ws.security.SOAPConstants;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSEncryptionPart;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.handler.RequestData;
import org.apache.ws.security.handler.WSHandler;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.message.WSSecUsernameToken;
import org.apache.ws.security.message.WSSecSignature;
import org.apache.ws.security.util.WSSecurityUtil;
import org.apache.xml.security.signature.XMLSignature;
import org.w3c.dom.Document;

/**
 * Sign a request using a secret key derived from UsernameToken data.
 * 
 * Enhanced by Alberto Coletti to support digest password type for 
 * username token signature
 * 
 * @author Werner Dittmann (Werner.Dittmann@t-online.de)
 */

public class UsernameTokenSignedAction implements Action {
    public void execute(WSHandler handler, int actionToDo, Document doc, RequestData reqData)
            throws WSSecurityException {
        String password;
        password = handler.getPassword(reqData.getUsername(), actionToDo,
                WSHandlerConstants.PW_CALLBACK_CLASS,
                WSHandlerConstants.PW_CALLBACK_REF, reqData).getPassword();

        WSSecUsernameToken builder = new WSSecUsernameToken();
        builder.setWsConfig(reqData.getWssConfig());
        
        if (reqData.isUseDerivedKey()) {
            int iterations = reqData.getDerivedKeyIterations();
            boolean useMac = reqData.isUseDerivedKeyForMAC();
            builder.addDerivedKey(useMac, null, iterations);
        } else {
            builder.setPasswordType(reqData.getPwType());  // enhancement by Alberto Coletti
            builder.setSecretKeyLength(reqData.getSecretKeyLength());
        }
        
        builder.setUserInfo(reqData.getUsername(), password);
        builder.addCreated();
        builder.addNonce();
        builder.prepare(doc);

        // Now prepare to sign.
        // First step:  Get a WS Signature object and set config parameters
        // second step: set user data and algorithm parameters. This
        //              _must_ be done before we "prepare"
        // third step:  Call "prepare". This creates the internal WS Signature
        //              data structures, XML element, fills in the algorithms
        //              and other data.
        // fourth step: Get the references. These references identify the parts
        //              of the document that will be included into the 
        //              signature. If no references are given sign the message
        //              body by default.
        // fifth step:  compute the signature
        //
        // after "prepare" the Signature XML element is ready and may prepend
        // this to the security header.
        
        WSSecSignature sign = new WSSecSignature();
        sign.setWsConfig(reqData.getWssConfig());

        sign.setUsernameToken(builder);
        sign.setKeyIdentifierType(WSConstants.UT_SIGNING);

        if (reqData.getSigAlgorithm() != null) {
            sign.setSignatureAlgorithm(reqData.getSigAlgorithm());
        } else {
            sign.setSignatureAlgorithm(XMLSignature.ALGO_ID_MAC_HMAC_SHA1);
        }

        sign.prepare(doc, null, reqData.getSecHeader());

        // prepend in this order: first the Signature Element and then the
        // UsernameToken Element. This way the server gets the UsernameToken
        // first, can check it and are prepared to compute the Signature key.  
        sign.prependToHeader(reqData.getSecHeader());
        builder.prependToHeader(reqData.getSecHeader());

        Vector parts = null;
        if (reqData.getSignatureParts().size() > 0) {
            parts = reqData.getSignatureParts();
        }
        else {
            SOAPConstants soapConstants = WSSecurityUtil.getSOAPConstants(doc
                    .getDocumentElement());
            
            parts = new Vector();
            WSEncryptionPart encP = new WSEncryptionPart(soapConstants
                    .getBodyQName().getLocalPart(), soapConstants
                    .getEnvelopeURI(), "Content");
            parts.add(encP);
        }
        sign.addReferencesToSign(parts, reqData.getSecHeader());

        try {
            sign.computeSignature();
            reqData.getSignatureValues().add(sign.getSignatureValue());
        } catch (WSSecurityException e) {
            throw new WSSecurityException(
                "WSHandler: Error during UsernameTokenSignature", e
            );
        }
    }
}
