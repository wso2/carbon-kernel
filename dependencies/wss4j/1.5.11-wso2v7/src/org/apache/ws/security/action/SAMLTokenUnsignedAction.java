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

import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.handler.RequestData;
import org.apache.ws.security.handler.WSHandler;
import org.apache.ws.security.message.WSSecSAMLToken;
import org.apache.ws.security.saml.SAMLIssuer;
import org.opensaml.SAMLAssertion;
import org.w3c.dom.Document;

public class SAMLTokenUnsignedAction extends SAMLTokenSignedAction {

    public void execute(WSHandler handler, int actionToDo, Document doc, RequestData reqData)
            throws WSSecurityException {
        WSSecSAMLToken builder = new WSSecSAMLToken();
        builder.setWsConfig(reqData.getWssConfig());

        SAMLIssuer saml = loadSamlIssuer(handler, reqData);
        saml.setUsername(reqData.getUsername());
        SAMLAssertion assertion = saml.newAssertion();

        // add the SAMLAssertion Token to the SOAP Envelope
        builder.build(doc, assertion, reqData.getSecHeader());
    }
}
