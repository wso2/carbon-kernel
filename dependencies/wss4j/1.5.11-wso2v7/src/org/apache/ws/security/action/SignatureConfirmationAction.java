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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSEncryptionPart;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.handler.RequestData;
import org.apache.ws.security.handler.WSHandler;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.handler.WSHandlerResult;
import org.apache.ws.security.message.WSSecSignatureConfirmation;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Document;

import java.util.Vector;

public class SignatureConfirmationAction implements Action {
    protected static Log log = LogFactory.getLog(WSHandler.class.getName());

    public void execute(WSHandler handler, int actionToDo, Document doc, RequestData reqData)
            throws WSSecurityException {
        if (log.isDebugEnabled()) {
            log.debug("Perform Signature confirmation");
        }

        Vector results = (Vector) handler.getProperty(reqData.getMsgContext(),
                WSHandlerConstants.RECV_RESULTS);
        /*
         * loop over all results gathered by all handlers in the chain. For each
         * handler result get the various actions. After that loop we have all
         * signature results in the signatureActions vector
         */
        Vector signatureActions = new Vector();
        for (int i = 0; i < results.size(); i++) {
            WSHandlerResult wshResult = (WSHandlerResult) results.get(i);

            WSSecurityUtil.fetchAllActionResults(wshResult.getResults(),
                    WSConstants.SIGN, signatureActions);
            WSSecurityUtil.fetchAllActionResults(wshResult.getResults(),
                    WSConstants.ST_SIGNED, signatureActions);
            WSSecurityUtil.fetchAllActionResults(wshResult.getResults(),
                    WSConstants.UT_SIGN, signatureActions);
        }
        Vector signatureParts = reqData.getSignatureParts();
        // prepare a SignatureConfirmation token
        WSSecSignatureConfirmation wsc = new WSSecSignatureConfirmation();
        if (signatureActions.size() > 0) {
            if (log.isDebugEnabled()) {
                log.debug("Signature Confirmation: number of Signature results: "
                        + signatureActions.size());
            }
            for (int i = 0; i < signatureActions.size(); i++) {
                WSSecurityEngineResult wsr = (WSSecurityEngineResult) signatureActions
                        .get(i);
                byte[] sigVal = (byte[]) wsr
                        .get(WSSecurityEngineResult.TAG_SIGNATURE_VALUE);
                wsc.build(doc, sigVal, reqData.getSecHeader());
                signatureParts.add(new WSEncryptionPart(wsc.getId()));
            }
        } else {
            wsc.build(doc, null, reqData.getSecHeader());
            signatureParts.add(new WSEncryptionPart(wsc.getId()));
        }
        handler.setProperty(reqData.getMsgContext(), WSHandlerConstants.SIG_CONF_DONE,
                WSHandler.DONE);
    }
}
