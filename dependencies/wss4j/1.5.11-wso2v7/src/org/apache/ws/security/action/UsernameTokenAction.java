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

import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.handler.RequestData;
import org.apache.ws.security.handler.WSHandler;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.message.WSSecUsernameToken;
import org.w3c.dom.Document;

public class UsernameTokenAction implements Action {
    public void execute(WSHandler handler, int actionToDo, Document doc, RequestData reqData)
            throws WSSecurityException {
        
        // Always call the callback for the username. We mis-use the configured password callback class and callback methods for this.
        String providedUsername = reqData.getUsername();
        WSPasswordCallback callbackData = handler.getPassword(reqData.getUsername(),
                        actionToDo,
                        WSHandlerConstants.PW_CALLBACK_CLASS,
                        WSHandlerConstants.PW_CALLBACK_REF, reqData);
        providedUsername = callbackData.getIdentifier();
        String password = callbackData.getPassword();

        WSSecUsernameToken builder = new WSSecUsernameToken();
        builder.setWsConfig(reqData.getWssConfig());
        builder.setPasswordType(reqData.getPwType());
        builder.setPasswordsAreEncoded(reqData.getWssConfig().getPasswordsAreEncoded());
        builder.setUserInfo(providedUsername, password);

        if (reqData.getUtElements() != null && reqData.getUtElements().length > 0) {
            for (int j = 0; j < reqData.getUtElements().length; j++) {
                reqData.getUtElements()[j].trim();
                if (reqData.getUtElements()[j].equals("Nonce")) {
                    builder.addNonce();
                }
                if (reqData.getUtElements()[j].equals("Created")) {
                    builder.addCreated();
                }
                reqData.getUtElements()[j] = null;
            }
        }
        builder.build(doc, reqData.getSecHeader());        
    }
}
