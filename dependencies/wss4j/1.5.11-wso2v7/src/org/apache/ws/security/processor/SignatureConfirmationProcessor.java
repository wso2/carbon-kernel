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
import org.apache.ws.security.WSDocInfo;
import org.apache.ws.security.WSSConfig;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.message.token.SignatureConfirmation;
import org.w3c.dom.Element;

import javax.security.auth.callback.CallbackHandler;
import java.util.Vector;

public class SignatureConfirmationProcessor implements Processor {
    private static Log log = LogFactory.getLog(SignatureConfirmationProcessor.class.getName());

    private String scId;
    
    public void handleToken(
        Element elem, 
        Crypto crypto, 
        Crypto decCrypto, 
        CallbackHandler cb, 
        WSDocInfo wsDocInfo, 
        Vector returnResults, 
        WSSConfig wsc
    ) throws WSSecurityException {
        if (log.isDebugEnabled()) {
            log.debug("Found SignatureConfirmation list element");
        }
        //
        // Decode SignatureConfirmation, just store in result
        //
        SignatureConfirmation sigConf = new SignatureConfirmation(elem);
        returnResults.add(
            0, 
            new WSSecurityEngineResult(WSConstants.SC, sigConf)
        );
        scId = elem.getAttributeNS(WSConstants.WSU_NS, "Id");
    }
    
    public String getId() {
        return scId;
    }    
}
