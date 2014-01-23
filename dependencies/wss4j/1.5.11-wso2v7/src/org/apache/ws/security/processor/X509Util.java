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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.crypto.SecretKey;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;

public class X509Util {
    private static Log log = LogFactory.getLog(X509Util.class.getName());

    public static boolean isContent(Node encBodyData) {
        //
        // Depending on the encrypted data type (Content or Element) the encBodyData either
        // holds the element whose contents where encrypted, e.g. soapenv:Body, or the
        // xenc:EncryptedData element (in case of Element encryption). In either case we need
        // to get the xenc:EncryptedData element. So get it. The findElement method returns
        // immediately if its already the correct element.
        // Then we can get the Type attribute.
        //
        Element tmpE = 
            (Element) WSSecurityUtil.findElement(
                encBodyData, "EncryptedData", WSConstants.ENC_NS
            );
        if (tmpE != null) {
            String typeStr = tmpE.getAttribute("Type");
            if (typeStr != null) {
                 return typeStr.equals(WSConstants.ENC_NS + "Content");
            }
        }
        return true;
    }

    public static String getEncAlgo(Node encBodyData) throws WSSecurityException {
        Element tmpE = 
            (Element) WSSecurityUtil.findElement(
                encBodyData, "EncryptionMethod", WSConstants.ENC_NS
            );
        String symEncAlgo = null;
        if (tmpE != null) {
            symEncAlgo = tmpE.getAttribute("Algorithm");
            if (symEncAlgo == null) {
                throw new WSSecurityException(
                    WSSecurityException.UNSUPPORTED_ALGORITHM, "noEncAlgo"
                );
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Sym Enc Algo: " + symEncAlgo);
        }
        return symEncAlgo;
    }

    protected static SecretKey getSharedKey(
        Element keyInfoElem,
        String algorithm,
        CallbackHandler cb
    ) throws WSSecurityException {
        String keyName = null;
        Element keyNmElem = 
            (Element) WSSecurityUtil.getDirectChild(
                keyInfoElem, "KeyName", WSConstants.SIG_NS
            );
        if (keyNmElem != null) {
            keyNmElem.normalize();
            Node tmpN = keyNmElem.getFirstChild();
            if (tmpN != null && tmpN.getNodeType() == Node.TEXT_NODE) {
                keyName = tmpN.getNodeValue();
            }
        }
        if (keyName == null) {
            throw new WSSecurityException(WSSecurityException.INVALID_SECURITY, "noKeyname");
        }
        WSPasswordCallback pwCb = new WSPasswordCallback(keyName, WSPasswordCallback.KEY_NAME);
        Callback[] callbacks = new Callback[1];
        callbacks[0] = pwCb;
        try {
            cb.handle(callbacks);
        } catch (IOException e) {
            throw new WSSecurityException(
                WSSecurityException.FAILURE,
                "noPassword",
                new Object[]{keyName}, 
                e
            );
        } catch (UnsupportedCallbackException e) {
            throw new WSSecurityException(
                WSSecurityException.FAILURE,
                "noPassword",
                new Object[]{keyName}, 
                e
            );
        }
        byte[] decryptedData = pwCb.getKey();
        if (decryptedData == null) {
            throw new WSSecurityException(
                WSSecurityException.FAILURE,
                "noPassword",
                new Object[]{keyName}
            );
        }
        return WSSecurityUtil.prepareSecretKey(algorithm, decryptedData);
    }

}
