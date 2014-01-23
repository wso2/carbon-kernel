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
package org.apache.ws.security.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.SOAPConstants;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.message.token.Timestamp;
import org.apache.ws.security.util.WSSecurityUtil;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.security.auth.callback.CallbackHandler;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.rpc.Call;
import javax.xml.rpc.JAXRPCException;
import javax.xml.rpc.handler.Handler;
import javax.xml.rpc.handler.HandlerInfo;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.handler.soap.SOAPMessageContext;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Vector;

/**
 * Merged and converted the the axis handlers WSDoAllReceiver and WSDoAllSender
 * into a single JAX-RPC Handler. All the axis dependencies are removed.
 *
 * @author Venkat Reddy (vreddyp@gmail.com).
 */
public class WSS4JHandler extends WSHandler implements Handler {
    private HandlerInfo handlerInfo;
    
    private static Log log = LogFactory.getLog(WSS4JHandler.class.getName());

    private static boolean doDebug = log.isDebugEnabled();

    static final String DEPLOYMENT = "deployment";
    static final String CLIENT_DEPLOYMENT = "client";
    static final String SERVER_DEPLOYMENT = "server";
    static final String FLOW = "flow";
    static final String REQUEST_ONLY = "request-only";
    static final String RESPONSE_ONLY = "response-only";
    static final String ALLOW_FORM_OPTIMIZATION = "axis.form.optimization";

    /**
     * Initializes the instance of the handler.
     */
    public void init(HandlerInfo hi) {
        handlerInfo = hi;
    }

    /**
     * Destroys the Handler instance.
     */
    public void destroy() {
    }

    public QName[] getHeaders() {
        return handlerInfo.getHeaders();
    }

    public boolean handleRequest(MessageContext mc) {
        mc.setProperty(ALLOW_FORM_OPTIMIZATION,
            Boolean.TRUE);
        try {
            return processMessage(mc, true);
        } catch (WSSecurityException e) {
            if (doDebug) {
                log.debug(e.getMessage(), e);
            }
            throw new JAXRPCException(e);
        }
    }

    public boolean handleResponse(MessageContext mc) {
        mc.setProperty(ALLOW_FORM_OPTIMIZATION,
            Boolean.TRUE);
        try {
            return processMessage(mc, false);
        } catch (WSSecurityException e) {
            if (doDebug) {
                log.debug(e.getMessage(), e);
            }
            throw new JAXRPCException(e);
        }
    }

    /**
     * Handles SOAP Faults that may occur during message processing
     */
    public boolean handleFault(MessageContext mc) {
        if (doDebug) {
            log.debug("Entered handleFault");
        }
        return true;
    }

    /**
     * Switch for transferring control to doReceiver and doSender
     */
    public boolean processMessage(MessageContext mc, boolean isRequestMessage) throws WSSecurityException {

        RequestData reqData = new RequestData();
        reqData.setMsgContext(mc);

        doDebug = log.isDebugEnabled();
        String deployment = null;
        String handleFlow = null;

        if ((deployment = (String) getOption(DEPLOYMENT)) == null) {
            deployment = (String) mc.getProperty(DEPLOYMENT);
        }
        if (deployment == null) {
            throw new JAXRPCException("WSS4JHandler.processMessage: No deployment defined");
        }
        if ((handleFlow = (String) getOption(FLOW)) == null) {
            handleFlow = (String) mc.getProperty(FLOW);
        }
        if (handleFlow == null) {
            handleFlow = "";
        }

        // call doSender if we are -
        // (handling request and client-side deployment) or (handling response and server-side deployment).
        // call doReceiver if we are -
        // (handling request and server-side deployment) or (handling response and client-side deployment).

        boolean needsHandling = ( isRequestMessage && !handleFlow.equals(RESPONSE_ONLY)) ||
                                (!isRequestMessage && !handleFlow.equals(REQUEST_ONLY));
        try {
            if (deployment.equals(CLIENT_DEPLOYMENT) ^ isRequestMessage) {
                if (needsHandling) {
                    return doReceiver(mc, reqData, isRequestMessage);
                }
            } else {
                if (needsHandling) {
                    return doSender(mc, reqData, isRequestMessage);
                }
            }
        } finally {
            reqData.clear();
            reqData = null;
        }
        return true;
    }

    /**
     * Handles incoming web service requests and outgoing responses
     */
    public boolean doSender(MessageContext mc, RequestData reqData, boolean isRequest) throws WSSecurityException {

        reqData.getSignatureParts().removeAllElements();
        reqData.getEncryptParts().removeAllElements();
        reqData.setNoSerialization(false);
        /*
        * Get the action first.
        */
        Vector actions = new Vector();
        String action = (String) getOption(WSHandlerConstants.SEND + '.' + WSHandlerConstants.ACTION);
        if (action == null) {
            action = (String) getOption(WSHandlerConstants.ACTION);
            if (action == null) {
                action = (String) mc.getProperty(WSHandlerConstants.ACTION);
            }
        }
        if (action == null) {
            throw new JAXRPCException("WSS4JHandler: No action defined");
        }
        int doAction = WSSecurityUtil.decodeAction(action, actions);
        if (doAction == WSConstants.NO_SECURITY) {
            return true;
        }
        
        /*
        * For every action we need a username, so get this now. The username
        * defined in the deployment descriptor takes precedence.
        */
        reqData.setUsername((String) getOption(WSHandlerConstants.USER));
        if (reqData.getUsername() == null || reqData.getUsername().equals("")) {
            reqData.setUsername((String) mc.getProperty(WSHandlerConstants.USER));
            mc.removeProperty(WSHandlerConstants.USER);
        }

        /*
        * Now we perform some set-up for UsernameToken and Signature
        * functions. No need to do it for encryption only. Check if username
        * is available and then get a password.
        */
        if (((doAction & (WSConstants.SIGN | WSConstants.UT | WSConstants.UT_SIGN)) != 0)
            && (reqData.getUsername() == null || reqData.getUsername().equals(""))) {
            /*
            * We need a username - if none throw an JAXRPCException. For encryption
            * there is a specific parameter to get a username.
            */
            throw new JAXRPCException("WSS4JHandler: Empty username for specified action");
        }
        if (doDebug) {
            log.debug("Action: " + doAction);
            log.debug("Actor: " + reqData.getActor());
        }
        /*
        * Now get the SOAP part from the request message and convert it into a
        * Document.
        *
        * This forces Axis to serialize the SOAP request into FORM_STRING.
        * This string is converted into a document.
        *
        * During the FORM_STRING serialization Axis performs multi-ref of
        * complex data types (if requested), generates and inserts references
        * for attachments and so on. The resulting Document MUST be the
        * complete and final SOAP request as Axis would send it over the wire.
        * Therefore this must shall be the last (or only) handler in a chain.
        *
        * Now we can perform our security operations on this request.
        */
        Document doc = null;
        SOAPMessage message = ((SOAPMessageContext)mc).getMessage();
        Boolean propFormOptimization = (Boolean)mc.getProperty("axis.form.optimization");
        log.debug("Form optimization: " + propFormOptimization);
        /*
        * If the message context property contains a document then this is a
        * chained handler.
        */
        SOAPPart sPart = message.getSOAPPart();
        if ((doc = (Document) mc.getProperty(WSHandlerConstants.SND_SECURITY))
                == null) {
            try {
                doc = messageToDocument(message);
            } catch (Exception e) {
                if (doDebug) {
                    log.debug(e.getMessage(), e);
                }
                throw new JAXRPCException("WSS4JHandler: cannot get SOAP envlope from message", e);
            }
        }
        if (doDebug) {
            log.debug("WSS4JHandler: orginal SOAP request: ");
            log.debug(org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(doc));
        }
        doSenderAction(doAction, doc, reqData, actions, isRequest);
 
        /*
        * If required convert the resulting document into a message first. The
        * outputDOM() method performs the necessary c14n call. After that we
        * extract it as a string for further processing.
        *
        * Set the resulting byte array as the new SOAP message.
        *
        * If noSerialization is false, this handler shall be the last (or only)
        * one in a handler chain. If noSerialization is true, just set the
        * processed Document in the transfer property. The next Axis WSS4J
        * handler takes it and performs additional security processing steps.
        *
        */
        if (reqData.isNoSerialization()) {
            mc.setProperty(WSHandlerConstants.SND_SECURITY, doc);
        } else {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            XMLUtils.outputDOM(doc, os, true);
            if (doDebug) {
                String osStr = null;
                try {
                    osStr = os.toString("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    if (doDebug) {
                        log.debug(e.getMessage(), e);
                    }
                    osStr = os.toString();
                }
                log.debug("Send request:");
                log.debug(osStr);
            }

            try {
                sPart.setContent(new StreamSource(new ByteArrayInputStream(os.toByteArray())));
            } catch (SOAPException se) {
                if (doDebug) {
                    log.debug(se.getMessage(), se);
                }
                throw new JAXRPCException("Couldn't set content on SOAPPart" + se.getMessage(), se);
            }
            mc.removeProperty(WSHandlerConstants.SND_SECURITY);
        }
        if (doDebug) {
            log.debug("WSS4JHandler: exit invoke()");
        }
        return true;
    }

    /**
     * handle responses
     *
     * @param mc
     * @param reqData
     * @return true on successful processing
     * @throws WSSecurityException
     */
    public boolean doReceiver(MessageContext mc, RequestData reqData, boolean isRequest) throws WSSecurityException {

        Vector actions = new Vector();
        String action = (String) getOption(WSHandlerConstants.RECEIVE + '.' + WSHandlerConstants.ACTION);
        if (action == null) {
            action = (String) getOption(WSHandlerConstants.ACTION);
            if (action == null) {
                action = (String) mc.getProperty(WSHandlerConstants.ACTION);
            }
        }
        if (action == null) {
            throw new JAXRPCException("WSS4JHandler: No action defined");
        }
        int doAction = WSSecurityUtil.decodeAction(action, actions);

        String actor = (String) getOption(WSHandlerConstants.ACTOR);

        SOAPMessage message = ((SOAPMessageContext)mc).getMessage();
        SOAPPart sPart = message.getSOAPPart();
        Document doc = null;
        try {
            doc = messageToDocument(message);
        } catch (Exception ex) {
            if (doDebug) {
                log.debug(ex.getMessage(), ex);
            }
            throw new JAXRPCException("WSS4JHandler: cannot convert into document",
                    ex);
        }
        /*
        * Check if it's a fault. Don't process faults.
        *
        */
        SOAPConstants soapConstants =
                WSSecurityUtil.getSOAPConstants(doc.getDocumentElement());
        if (WSSecurityUtil
                .findElement(doc.getDocumentElement(),
                        "Fault",
                        soapConstants.getEnvelopeURI())
                != null) {
            return false;
        }

        /*
        * To check a UsernameToken or to decrypt an encrypted message we need
        * a password.
        */
        CallbackHandler cbHandler = null;
        if ((doAction & (WSConstants.ENCR | WSConstants.UT)) != 0) {
            cbHandler = getPasswordCB(reqData);
        }

        /*
        * Get and check the Signature specific parameters first because they
        * may be used for encryption too.
        */
        doReceiverAction(doAction, reqData);

        Vector wsResult = null;
        try {
            wsResult =
                    secEngine.processSecurityHeader(doc,
                            actor,
                            cbHandler,
                            reqData.getSigCrypto(),
                            reqData.getDecCrypto());
        } catch (WSSecurityException ex) {
            if (doDebug) {
                log.debug(ex.getMessage(), ex);
            }
            throw new JAXRPCException("WSS4JHandler: security processing failed",
                    ex);
        }
        if (wsResult == null) {         // no security header found
            if (doAction == WSConstants.NO_SECURITY) {
                return true;
            } else {
                throw new JAXRPCException("WSS4JHandler: Request does not contain required Security header");
            }
        }
        if (reqData.getWssConfig().isEnableSignatureConfirmation() && !isRequest) {
            checkSignatureConfirmation(reqData, wsResult);
        }

        /*
        * If we had some security processing, get the original
        * SOAP part of Axis' message and replace it with new SOAP
        * part. This new part may contain decrypted elements.
        */

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        XMLUtils.outputDOM(doc, os, true);
        try {
            sPart.setContent(new StreamSource(new ByteArrayInputStream(os.toByteArray())));
        } catch (SOAPException se) {
            if (doDebug) {
                log.debug(se.getMessage(), se);
            }
            throw new JAXRPCException(
                "Couldn't set content on SOAPPart" + se.getMessage(), se
            );
        }

        if (doDebug) {
            log.debug("Processed received SOAP request");
        }

        /*
        * After setting the new current message, probably modified because
        * of decryption, we need to locate the security header. That is,
        * we force Axis (with getSOAPEnvelope()) to parse the string, build
        * the new header. Then we examine, look up the security header
        * and set the header as processed.
        *
        * Please note: find all header elements that contain the same
        * actor that was given to processSecurityHeader(). Then
        * check if there is a security header with this actor.
        */

        SOAPHeader sHeader = null;
        try {
            sHeader = message.getSOAPPart().getEnvelope().getHeader();
        } catch (Exception ex) {
            if (doDebug) {
                log.debug(ex.getMessage(), ex);
            }
            throw new JAXRPCException("WSS4JHandler: cannot get SOAP header after security processing", ex);
        }

        Iterator headers = sHeader.examineHeaderElements(actor);

        SOAPHeaderElement headerElement = null;
        while (headers.hasNext()) {
            SOAPHeaderElement hE = (SOAPHeaderElement) headers.next();
            if (hE.getElementName().getLocalName().equals(WSConstants.WSSE_LN)
                    && ((Node) hE).getNamespaceURI().equals(WSConstants.WSSE_NS)) {
                headerElement = hE;
                break;
            }
        }

        /* JAXRPC conversion changes */
        headerElement.setMustUnderstand(false); // is this sufficient?

        /*
        * Now we can check the certificate used to sign the message.
        * In the following implementation the certificate is only trusted
        * if either it itself or the certificate of the issuer is installed
        * in the keystore.
        *
        * Note: the method verifyTrust(X509Certificate) allows custom
        * implementations with other validation algorithms for subclasses.
        */

        // Extract the signature action result from the action vector

        WSSecurityEngineResult actionResult = WSSecurityUtil.fetchActionResult(wsResult, WSConstants.SIGN);

        if (actionResult != null) {
            X509Certificate returnCert = 
                (X509Certificate)actionResult.get(WSSecurityEngineResult.TAG_X509_CERTIFICATE);

            if (returnCert != null && !verifyTrust(returnCert, reqData)) {
                throw new JAXRPCException("WSS4JHandler: The certificate used for the signature is not trusted");
            }
        }

        /*
        * Perform further checks on the timestamp that was transmitted in the header.
        * In the following implementation the timestamp is valid if it was
        * created after (now-ttl), where ttl is set on server side, not by the client.
        *
        * Note: the method verifyTimestamp(Timestamp) allows custom
        * implementations with other validation algorithms for subclasses.
        */

        // Extract the timestamp action result from the action vector
        actionResult = WSSecurityUtil.fetchActionResult(wsResult, WSConstants.TS);

        if (actionResult != null) {
            Timestamp timestamp = 
                (Timestamp)actionResult.get(WSSecurityEngineResult.TAG_TIMESTAMP);

            if (timestamp != null && reqData.getWssConfig().isTimeStampStrict()
                && !verifyTimestamp(timestamp, decodeTimeToLive(reqData))) {
                throw new JAXRPCException("WSS4JHandler: The timestamp could not be validated");
            }
        }

        /*
        * now check the security actions: do they match, in right order?
        */
        if (!checkReceiverResults(wsResult, actions)) {
            throw new JAXRPCException("WSS4JHandler: security processing failed (actions mismatch)");
        }

        /*
        * All ok up to this point. Now construct and setup the
        * security result structure. The service may fetch this
        * and check it.
        */
        Vector results = null;
        if ((results = (Vector) mc.getProperty(WSHandlerConstants.RECV_RESULTS))
                == null) {
            results = new Vector();
            mc.setProperty(WSHandlerConstants.RECV_RESULTS, results);
        }
        WSHandlerResult rResult =
                new WSHandlerResult(actor,
                        wsResult);
        results.add(0, rResult);
        if (doDebug) {
            log.debug("WSS4JHandler: exit invoke()");
        }

        return true;
    }

    /**
     * Utility method to convert SOAPMessage to org.w3c.dom.Document
     */
    public static Document messageToDocument(SOAPMessage message) {
        try {
            Source content = message.getSOAPPart().getContent();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder builder = dbf.newDocumentBuilder();
            return builder.parse(org.apache.ws.security.util.XMLUtils.sourceToInputSource(content));
        } catch (Exception ex) {
            if (doDebug) {
                log.debug(ex.getMessage(), ex);
            }
            throw new JAXRPCException("messageToDocument: cannot convert SOAPMessage into Document", ex);
        }
    }

    public Object getOption(String key) {
        return handlerInfo.getHandlerConfig().get(key);
    }

    public Object getProperty(Object msgContext, String key) {
        return ((MessageContext)msgContext).getProperty(key);
    }

    public void setProperty(Object msgContext, String key, Object value) {
        ((MessageContext)msgContext).setProperty(key, value);
    }

    public String getPassword(Object msgContext) {
        return (String) ((MessageContext)msgContext).getProperty(Call.PASSWORD_PROPERTY);
    }

    public void setPassword(Object msgContext, String password) {
        ((MessageContext)msgContext).setProperty(Call.PASSWORD_PROPERTY, password);
    }
}
