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

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.SOAPPart;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.axis.security.handler.WSDoAllHandler;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.handler.RequestData;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.util.WSSecurityUtil;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Document;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

/**
 * @author Werner Dittmann (werner@apache.org)
 */
public class WSDoAllSender extends WSDoAllHandler {

    protected static Log log = LogFactory.getLog(WSDoAllSender.class.getName());
    private static Log tlog =
        LogFactory.getLog("org.apache.ws.security.TIME");
    
    /**
     * Axis calls invoke to handle a message. <p/>
     *
     * @param mc message context.
     * @throws AxisFault
     */
    public void invoke(MessageContext mc) throws AxisFault {

        boolean doDebug = log.isDebugEnabled();
        
        long t0 = 0, t1 = 0, t2 = 0, t3 = 0;
        if (tlog.isDebugEnabled()) {
            t0 = System.currentTimeMillis();
        }

        if (doDebug && mc.getCurrentMessage() != null
                && mc.getCurrentMessage().getMessageType() != null) {
            log.debug("WSDoAllSender: enter invoke() with msg type: "
                    + mc.getCurrentMessage().getMessageType());
        }

        RequestData reqData = new RequestData();

        reqData.setMsgContext(mc);
        /*
         * The overall try, just to have a finally at the end to perform some
         * housekeeping.
         */
        try {
            /*
             * Get the action first.
             */
            Vector actions = new Vector();
            String action = getString(WSHandlerConstants.ACTION, mc);
            if (action == null) {
                throw new AxisFault("WSDoAllSender: No action defined");
            }
            int doAction = WSSecurityUtil.decodeAction(action, actions);
            if (doAction == WSConstants.NO_SECURITY) {
                return;
            }

            /*
             * For every action we need a username, so get this now. The
             * username defined in the deployment descriptor takes precedence.
             */
            reqData.setUsername((String) getOption(WSHandlerConstants.USER));
            if (reqData.getUsername() == null || reqData.getUsername().equals("")) {
                String username = (String) getProperty(reqData.getMsgContext(), WSHandlerConstants.USER);
                if (username != null) {
                    reqData.setUsername(username);
                } else {
                    reqData.setUsername(((MessageContext)reqData.getMsgContext()).getUsername());
                    ((MessageContext)reqData.getMsgContext()).setUsername(null);
                }
            }
            /*
             * Now we perform some set-up for UsernameToken and Signature
             * functions. No need to do it for encryption only. Check if
             * username is available and then get a passowrd.
             */
            if (((doAction & (WSConstants.SIGN | WSConstants.UT | WSConstants.UT_SIGN)) != 0)
                && (reqData.getUsername() == null || reqData.getUsername().equals(""))) {
                /*
                 * We need a username - if none throw an AxisFault. For
                 * encryption there is a specific parameter to get a username.
                 */
                 throw new AxisFault("WSDoAllSender: Empty username for specified action");
            }
            if (doDebug) {
                log.debug("Action: " + doAction);
                log.debug("Actor: " + reqData.getActor());
            }
            /*
             * Now get the SOAP part from the request message and convert it
             * into a Document.
             *
             * This forces Axis to serialize the SOAP request into FORM_STRING.
             * This string is converted into a document.
             *
             * During the FORM_STRING serialization Axis performs multi-ref of
             * complex data types (if requested), generates and inserts
             * references for attachements and so on. The resulting Document
             * MUST be the complete and final SOAP request as Axis would send it
             * over the wire. Therefore this must shall be the last (or only)
             * handler in a chain.
             *
             * Now we can perform our security operations on this request.
             */
            Document doc = null;
            Message message = mc.getCurrentMessage();

            /**
             * There is nothing to send...Usually happens when the provider needs to
             * send a HTTP 202 message (with no content)
             */
            if(message == null){
                return;
            }

            /*
            * If the message context property conatins a document then this is
            * a chained handler.
            */
            SOAPPart sPart = (org.apache.axis.SOAPPart) message.getSOAPPart();
            if ((doc = (Document) ((MessageContext)reqData.getMsgContext())
                    .getProperty(WSHandlerConstants.SND_SECURITY)) == null) {
                try {
                    doc = ((org.apache.axis.message.SOAPEnvelope) sPart
                            .getEnvelope()).getAsDocument();
                } catch (Exception e) {
                    throw new AxisFault(
                            "WSDoAllSender: cannot get SOAP envlope from message"
                                    + e);
                }
            }
            if (tlog.isDebugEnabled()) {
                t1 = System.currentTimeMillis();
            }

            doSenderAction(doAction, doc, reqData, actions, !mc.getPastPivot());
            
            if (tlog.isDebugEnabled()) {
                t2 = System.currentTimeMillis();
            }

            /*
             * If required convert the resulting document into a message first.
             * The outputDOM() method performs the necessary c14n call. After
             * that we extract it as a string for further processing.
             *
             * Set the resulting byte array as the new SOAP message.
             *
             * If noSerialization is false, this handler shall be the last (or
             * only) one in a handler chain. If noSerialization is true, just
             * set the processed Document in the transfer property. The next
             * Axis WSS4J handler takes it and performs additional security
             * processing steps.
             *
             */
            if (reqData.isNoSerialization()) {
                ((MessageContext)reqData.getMsgContext()).setProperty(WSHandlerConstants.SND_SECURITY,
                        doc);
            } else {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                XMLUtils.outputDOM(doc, os, true);
                sPart.setCurrentMessage(os.toByteArray(), SOAPPart.FORM_BYTES);
                if (doDebug) {
                    String osStr = null;
                    try {
                        osStr = os.toString("UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        osStr = os.toString();
                    }
                    log.debug("Send request:");
                    log.debug(osStr);
                }
                ((MessageContext)reqData.getMsgContext()).setProperty(WSHandlerConstants.SND_SECURITY,
                        null);
            }
            if (tlog.isDebugEnabled()) {
                t3 = System.currentTimeMillis();
                tlog.debug("Send request: total= " + (t3 - t0) +
                        " request preparation= " + (t1 - t0) +
                        " request processing= " + (t2 - t1) +
                        " request to Axis= " + (t3 - t2) +
                        "\n");                
            }

            if (doDebug) {
                log.debug("WSDoAllSender: exit invoke()");
            }
        } catch (WSSecurityException e) {
            throw new AxisFault(e.getMessage(), e);
        } finally {
            reqData.clear();
            reqData = null;
        }
    }
}
