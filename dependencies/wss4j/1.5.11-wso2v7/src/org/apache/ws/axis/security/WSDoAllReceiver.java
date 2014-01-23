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

package org.apache.ws.axis.security;

/**
 * @author Werner Dittmann (werner@apache.org)
 *
 */

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.SOAPPart;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.axis.security.handler.WSDoAllHandler;
import org.apache.ws.security.SOAPConstants;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.handler.RequestData;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.handler.WSHandlerResult;
import org.apache.ws.security.message.token.Timestamp;
import org.apache.ws.security.util.WSSecurityUtil;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Document;

import javax.security.auth.callback.CallbackHandler;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import java.io.ByteArrayOutputStream;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

public class WSDoAllReceiver extends WSDoAllHandler {

    protected static Log log = LogFactory.getLog(WSDoAllReceiver.class.getName());
    private static Log tlog =
        LogFactory.getLog("org.apache.ws.security.TIME");

    /**
     * Axis calls invoke to handle a message.
     * <p/>
     *
     * @param msgContext message context.
     * @throws AxisFault
     */
    public void invoke(MessageContext msgContext) throws AxisFault {

        boolean doDebug = log.isDebugEnabled();

        if (doDebug) {
            log.debug("WSDoAllReceiver: enter invoke() with msg type: "
                    + msgContext.getCurrentMessage().getMessageType());
        }
        long t0 = 0, t1 = 0, t2 = 0, t3 = 0, t4 = 0;
        if (tlog.isDebugEnabled()) {
            t0 = System.currentTimeMillis();
        }        

        RequestData reqData = new RequestData();
        /*
        * The overall try, just to have a finally at the end to perform some
        * housekeeping.
        */
        try {
            reqData.setMsgContext(msgContext);

            Vector actions = new Vector();
            String action = null;
            if ((action = (String) getOption(WSHandlerConstants.ACTION)) == null) {
                action = (String) msgContext
                        .getProperty(WSHandlerConstants.ACTION);
            }
            if (action == null) {
                throw new AxisFault("WSDoAllReceiver: No action defined");
            }
            int doAction = WSSecurityUtil.decodeAction(action, actions);

            String actor = (String) getOption(WSHandlerConstants.ACTOR);

            Message sm = msgContext.getCurrentMessage();
            Document doc = null;

            /**
             * We did not receive anything...Usually happens when we get a
             * HTTP 202 message (with no content)
             */
            if(sm == null){
                return;
            }

            try {
                doc = sm.getSOAPEnvelope().getAsDocument();
                if (doDebug) {
                    log.debug("Received SOAP request: ");
                    log.debug(org.apache.axis.utils.XMLUtils
                            .PrettyDocumentToString(doc));
                }
            } catch (Exception ex) {
                if (doDebug) {
                    log.debug(ex.getMessage(), ex);
                }
                throw new AxisFault(
                        "WSDoAllReceiver: cannot convert into document", ex);
            }
            /*
            * Check if it's a response and if its a fault. Don't process
            * faults.
            */
            String msgType = sm.getMessageType();
            if (msgType != null && msgType.equals(Message.RESPONSE)) {
                SOAPConstants soapConstants = WSSecurityUtil
                        .getSOAPConstants(doc.getDocumentElement());
                if (WSSecurityUtil.findElement(doc.getDocumentElement(),
                        "Fault", soapConstants.getEnvelopeURI()) != null) {
                    return;
                }
            }

            /*
            * To check a UsernameToken or to decrypt an encrypted message we
            * need a password.
            */
            CallbackHandler cbHandler = null;
            if ((doAction & (WSConstants.ENCR | WSConstants.UT)) != 0) {
                cbHandler = getPasswordCB(reqData);
            }

            /*
            * Get and check the Signature specific parameters first because
            * they may be used for encryption too.
            */
            doReceiverAction(doAction, reqData);
            
            Vector wsResult = null;
            if (tlog.isDebugEnabled()) {
                t1 = System.currentTimeMillis();
            }        

            try {
                wsResult = secEngine.processSecurityHeader(doc, actor,
                        cbHandler, reqData.getSigCrypto(), reqData.getDecCrypto());
            } catch (WSSecurityException ex) {
                if (doDebug) {
                    log.debug(ex.getMessage(), ex);
                }
                throw new AxisFault(
                        "WSDoAllReceiver: security processing failed", ex);
            }

            if (tlog.isDebugEnabled()) {
                t2 = System.currentTimeMillis();
            }        

            if (wsResult == null) { // no security header found
                if (doAction == WSConstants.NO_SECURITY) {
                    return;
                } else {
                    throw new AxisFault(
                            "WSDoAllReceiver: Request does not contain required Security header");
                }
            }

            if (reqData.getWssConfig().isEnableSignatureConfirmation() && msgContext.getPastPivot()) {
                checkSignatureConfirmation(reqData, wsResult);
            }
            /*
            * save the processed-header flags
            */
            ArrayList processedHeaders = new ArrayList();
            Iterator iterator = sm.getSOAPEnvelope().getHeaders().iterator();
            while (iterator.hasNext()) {
                org.apache.axis.message.SOAPHeaderElement tempHeader = (org.apache.axis.message.SOAPHeaderElement) iterator
                        .next();
                if (tempHeader.isProcessed()) {
                    processedHeaders.add(tempHeader.getQName());
                }
            }

            /*
            * If we had some security processing, get the original SOAP part of
            * Axis' message and replace it with new SOAP part. This new part
            * may contain decrypted elements.
            */
            SOAPPart sPart = (org.apache.axis.SOAPPart) sm.getSOAPPart();

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            XMLUtils.outputDOM(doc, os, true);
            sPart.setCurrentMessage(os.toByteArray(), SOAPPart.FORM_BYTES);
            if (doDebug) {
                log.debug("Processed received SOAP request");
                log.debug(org.apache.axis.utils.XMLUtils
                        .PrettyDocumentToString(doc));
            }
            if (tlog.isDebugEnabled()) {
                t3 = System.currentTimeMillis();
            }        

            /*
            * set the original processed-header flags
            */
            iterator = processedHeaders.iterator();
            while (iterator.hasNext()) {
                QName qname = (QName) iterator.next();
                Enumeration headersByName = sm.getSOAPEnvelope().getHeadersByName(
                        qname.getNamespaceURI(), qname.getLocalPart());
                while (headersByName.hasMoreElements()) {
                    org.apache.axis.message.SOAPHeaderElement tempHeader =
                        (org.apache.axis.message.SOAPHeaderElement) headersByName.nextElement();
                    tempHeader.setProcessed(true);
                }
            }

            /*
            * After setting the new current message, probably modified because
            * of decryption, we need to locate the security header. That is, we
            * force Axis (with getSOAPEnvelope()) to parse the string, build
            * the new header. Then we examine, look up the security header and
            * set the header as processed.
            *
            * Please note: find all header elements that contain the same actor
            * that was given to processSecurityHeader(). Then check if there is
            * a security header with this actor.
            */

            SOAPHeader sHeader = null;
            try {
                sHeader = sm.getSOAPEnvelope().getHeader();
            } catch (Exception ex) {
                if (doDebug) {
                    log.debug(ex.getMessage(), ex);
                }
                throw new AxisFault(
                        "WSDoAllReceiver: cannot get SOAP header after security processing",
                        ex);
            }

            Iterator headers = sHeader.examineHeaderElements(actor);

            SOAPHeaderElement headerElement = null;
            while (headers.hasNext()) {
                org.apache.axis.message.SOAPHeaderElement hE = (org.apache.axis.message.SOAPHeaderElement) headers.next();
                if (hE.getLocalName().equals(WSConstants.WSSE_LN)
                        && hE.getNamespaceURI().equals(WSConstants.WSSE_NS)) {
                    headerElement = hE;
                    break;
                }
            }
            ((org.apache.axis.message.SOAPHeaderElement) headerElement)
                    .setProcessed(true);

            /*
            * Now we can check the certificate used to sign the message. In the
            * following implementation the certificate is only trusted if
            * either it itself or the certificate of the issuer is installed in
            * the keystore.
            *
            * Note: the method verifyTrust(X509Certificate) allows custom
            * implementations with other validation algorithms for subclasses.
            */

            // Extract the signature action result from the action vector
            WSSecurityEngineResult actionResult = WSSecurityUtil
                    .fetchActionResult(wsResult, WSConstants.SIGN);

            if (actionResult != null) {
                X509Certificate returnCert = actionResult.getCertificate();

                if (returnCert != null && !verifyTrust(returnCert, reqData)) {
                    throw new AxisFault(
                            "WSDoAllReceiver: The certificate used for the signature is not trusted");
                }
            }

            /*
            * Perform further checks on the timestamp that was transmitted in
            * the header. In the following implementation the timestamp is
            * valid if it was created after (now-ttl), where ttl is set on
            * server side, not by the client.
            *
            * Note: the method verifyTimestamp(Timestamp) allows custom
            * implementations with other validation algorithms for subclasses.
            */

            // Extract the timestamp action result from the action vector
            actionResult = WSSecurityUtil.fetchActionResult(wsResult,
                    WSConstants.TS);

            if (actionResult != null) {
                Timestamp timestamp = actionResult.getTimestamp();

                if (timestamp != null 
                    && !verifyTimestamp(timestamp, decodeTimeToLive(reqData))) {
                    throw new AxisFault("WSDoAllReceiver: The timestamp could not be validated");
                }
            }

            /*
            * now check the security actions: do they match, in right order?
            */
            if (!checkReceiverResults(wsResult, actions)) {
                throw new AxisFault(
                    "WSDoAllReceiver: security processing failed (actions mismatch)");                
                
            }
            /*
            * All ok up to this point. Now construct and setup the security
            * result structure. The service may fetch this and check it.
            */
            Vector results = null;
            if ((results = (Vector) msgContext
                    .getProperty(WSHandlerConstants.RECV_RESULTS)) == null) {
                results = new Vector();
                msgContext
                        .setProperty(WSHandlerConstants.RECV_RESULTS, results);
            }
            WSHandlerResult rResult = new WSHandlerResult(actor, wsResult);
            results.add(0, rResult);
            if (tlog.isDebugEnabled()) {
                t4 = System.currentTimeMillis();
                tlog.debug("Receive request: total= " + (t4 - t0) +
                        " request preparation= " + (t1 - t0) +
                        " request processing= " + (t2 - t1) +
                        " request to Axis= " + (t3 - t2) + 
                        " header, cert verify, timestamp= " + (t4 - t3) +
                        "\n");                                
            }        

            if (doDebug) {
                log.debug("WSDoAllReceiver: exit invoke()");
            }
        } catch (WSSecurityException e) {
            if (doDebug) {
                log.debug(e.getMessage(), e);
            }
            throw new AxisFault(e.getMessage(), e);
        } finally {
            reqData.clear();
            reqData = null;
        }
    }
}
