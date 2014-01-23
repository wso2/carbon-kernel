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

package wssec;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.client.AxisClient;
import org.apache.axis.configuration.NullProvider;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSConfig;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;
import org.apache.ws.security.handler.RequestData;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecSignature;
import org.apache.ws.security.message.WSSecUsernameToken;
import org.apache.ws.security.util.WSSecurityUtil;
import org.apache.xml.security.signature.XMLSignature;
import org.w3c.dom.Document;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.Vector;

/**
 * WS-Security Test Case for UsernameToken Key Derivation, as defined in the 
 * UsernameTokenProfile 1.1 specification. The derived keys are used for signature.
 * Note that this functionality is different to the TestWSSecurityUTDK test case,
 * which uses the derived key in conjunction with wsc:DerivedKeyToken. It's also
 * different to TestWSSecurityNew13, which derives a key for signature using a 
 * non-standard implementation.
 */
public class TestWSSecurityUTSignature extends TestCase implements CallbackHandler {
    private static final Log LOG = LogFactory.getLog(TestWSSecurityUTSignature.class);
    private static final String SOAPMSG = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
        + "<SOAP-ENV:Envelope "
        +   "xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" "
        +   "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "
        +   "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" 
        +   "<SOAP-ENV:Body>" 
        +       "<add xmlns=\"http://ws.apache.org/counter/counter_port_type\">" 
        +           "<value xmlns=\"\">15</value>" 
        +       "</add>" 
        +   "</SOAP-ENV:Body>" 
        + "</SOAP-ENV:Envelope>";

    private WSSecurityEngine secEngine = new WSSecurityEngine();
    private Crypto crypto = CryptoFactory.getInstance();
    private MessageContext msgContext;
    private SOAPEnvelope unsignedEnvelope;

    /**
     * TestWSSecurity constructor
     * <p/>
     * 
     * @param name name of the test
     */
    public TestWSSecurityUTSignature(String name) {
        super(name);
    }

    /**
     * JUnit suite
     * <p/>
     * 
     * @return a junit test suite
     */
    public static Test suite() {
        return new TestSuite(TestWSSecurityUTSignature.class);
    }

    /**
     * Setup method
     * <p/>
     * 
     * @throws Exception Thrown when there is a problem in setup
     */
    protected void setUp() throws Exception {
        AxisClient tmpEngine = new AxisClient(new NullProvider());
        msgContext = new MessageContext(tmpEngine);
        unsignedEnvelope = getSOAPEnvelope();
    }

    /**
     * Constructs a soap envelope
     * <p/>
     * 
     * @return soap envelope
     * @throws java.lang.Exception if there is any problem constructing the soap envelope
     */
    protected SOAPEnvelope getSOAPEnvelope() throws Exception {
        InputStream in = new ByteArrayInputStream(SOAPMSG.getBytes());
        Message msg = new Message(in);
        msg.setMessageContext(msgContext);
        return msg.getSOAPEnvelope();
    }


    /**
     * Test using a UsernameToken derived key for signing a SOAP body
     */
    public void testSignature() throws Exception {
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        WSSecUsernameToken builder = new WSSecUsernameToken();
        builder.setUserInfo("bob", "security");
        builder.addDerivedKey(true, null, 1000);
        builder.prepare(doc);
        
        WSSecSignature sign = new WSSecSignature();
        sign.setUsernameToken(builder);
        sign.setKeyIdentifierType(WSConstants.UT_SIGNING);
        sign.setSignatureAlgorithm(XMLSignature.ALGO_ID_MAC_HMAC_SHA1);
        Document signedDoc = sign.build(doc, null, secHeader);
        builder.prependToHeader(secHeader);
        
        String outputString = 
            org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(signedDoc);
        assertTrue(outputString.indexOf("wsse:Username") != -1);
        assertTrue(outputString.indexOf("wsse:Password") == -1);
        assertTrue(outputString.indexOf("wsse11:Salt") != -1);
        assertTrue(outputString.indexOf("wsse11:Iteration") != -1);
        if (LOG.isDebugEnabled()) {
            LOG.debug(outputString);
        }
        
        Vector results = verify(signedDoc);
        WSSecurityEngineResult actionResult =
            WSSecurityUtil.fetchActionResult(results, WSConstants.UT_SIGN);
        java.security.Principal principal = 
            (java.security.Principal) actionResult.get(WSSecurityEngineResult.TAG_PRINCIPAL);
        assertTrue(principal.getName().indexOf("bob") != -1);
    }
    
    
    /**
     * Test using a UsernameToken derived key for signing a SOAP body. In this test the
     * user is "alice" rather than "bob", and so signature verification should fail.
     */
    public void testBadUserSignature() throws Exception {
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        WSSecUsernameToken builder = new WSSecUsernameToken();
        builder.setUserInfo("alice", "security");
        builder.addDerivedKey(true, null, 1000);
        builder.prepare(doc);
        
        WSSecSignature sign = new WSSecSignature();
        sign.setUsernameToken(builder);
        sign.setKeyIdentifierType(WSConstants.UT_SIGNING);
        sign.setSignatureAlgorithm(XMLSignature.ALGO_ID_MAC_HMAC_SHA1);
        Document signedDoc = sign.build(doc, null, secHeader);
        builder.prependToHeader(secHeader);
        
        String outputString = 
            org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(signedDoc);
        if (LOG.isDebugEnabled()) {
            LOG.debug(outputString);
        }

        try {
            verify(signedDoc);
            throw new Exception("Failure expected on a bad derived signature");
        } catch (WSSecurityException ex) {
            assertTrue(ex.getErrorCode() == WSSecurityException.FAILED_AUTHENTICATION);
            // expected
        }
    }
    
    /**
     * Test using a UsernameToken derived key for signing a SOAP body via WSHandler
     */
    public void testHandlerSignature() throws Exception {
        
        final WSSConfig cfg = WSSConfig.getNewInstance();
        RequestData reqData = new RequestData();
        reqData.setWssConfig(cfg);
        java.util.Map messageContext = new java.util.TreeMap();
        messageContext.put(WSHandlerConstants.PW_CALLBACK_REF, this);
        messageContext.put(WSHandlerConstants.USE_DERIVED_KEY, "true");
        reqData.setMsgContext(messageContext);
        reqData.setUsername("bob");
        
        final java.util.Vector actions = new java.util.Vector();
        actions.add(new Integer(WSConstants.UT_SIGN));
        
        Document doc = unsignedEnvelope.getAsDocument();
        MyHandler handler = new MyHandler();
        handler.send(
            WSConstants.UT_SIGN, 
            doc, 
            reqData, 
            actions,
            true
        );
        
        String outputString = 
            org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(doc);
        assertTrue(outputString.indexOf("wsse:Username") != -1);
        assertTrue(outputString.indexOf("wsse:Password") == -1);
        assertTrue(outputString.indexOf("wsse11:Salt") != -1);
        assertTrue(outputString.indexOf("wsse11:Iteration") != -1);
        if (LOG.isDebugEnabled()) {
            LOG.debug(outputString);
        }
        
        Vector results = verify(doc);
        WSSecurityEngineResult actionResult =
            WSSecurityUtil.fetchActionResult(results, WSConstants.UT_SIGN);
        java.security.Principal principal = 
            (java.security.Principal) actionResult.get(WSSecurityEngineResult.TAG_PRINCIPAL);
        assertTrue(principal.getName().indexOf("bob") != -1);
    }
    
    /**
     * Test using a UsernameToken derived key for signing a SOAP body via WSHandler
     */
    public void testHandlerSignatureIterations() throws Exception {
        
        final WSSConfig cfg = WSSConfig.getNewInstance();
        RequestData reqData = new RequestData();
        reqData.setWssConfig(cfg);
        java.util.Map messageContext = new java.util.TreeMap();
        messageContext.put(WSHandlerConstants.PW_CALLBACK_REF, this);
        messageContext.put(WSHandlerConstants.USE_DERIVED_KEY, "true");
        messageContext.put(WSHandlerConstants.DERIVED_KEY_ITERATIONS, "1234");
        reqData.setMsgContext(messageContext);
        reqData.setUsername("bob");
        
        final java.util.Vector actions = new java.util.Vector();
        actions.add(new Integer(WSConstants.UT_SIGN));
        
        Document doc = unsignedEnvelope.getAsDocument();
        MyHandler handler = new MyHandler();
        handler.send(
            WSConstants.UT_SIGN, 
            doc, 
            reqData, 
            actions,
            true
        );
        
        String outputString = 
            org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(doc);
        assertTrue(outputString.indexOf("wsse:Username") != -1);
        assertTrue(outputString.indexOf("wsse:Password") == -1);
        assertTrue(outputString.indexOf("wsse11:Salt") != -1);
        assertTrue(outputString.indexOf("wsse11:Iteration") != -1);
        assertTrue(outputString.indexOf("1234") != -1);
        if (LOG.isDebugEnabled()) {
            LOG.debug(outputString);
        }
        
        Vector results = verify(doc);
        WSSecurityEngineResult actionResult =
            WSSecurityUtil.fetchActionResult(results, WSConstants.UT_SIGN);
        java.security.Principal principal = 
            (java.security.Principal) actionResult.get(WSSecurityEngineResult.TAG_PRINCIPAL);
        assertTrue(principal.getName().indexOf("bob") != -1);
    }
    
    /**
     * Verifies the soap envelope.
     * 
     * @param env soap envelope
     * @throws java.lang.Exception Thrown when there is a problem in verification
     */
    private Vector verify(Document doc) throws Exception {
        return secEngine.processSecurityHeader(doc, null, this, crypto);
    }
    
    
    public void handle(Callback[] callbacks)
        throws IOException, UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof WSPasswordCallback) {
                WSPasswordCallback pc = (WSPasswordCallback) callbacks[i];
                if (pc.getUsage() == WSPasswordCallback.USERNAME_TOKEN_UNKNOWN
                    && "bob".equals(pc.getIdentifier())) {
                    pc.setPassword("security");
                } else if (pc.getUsage() == WSPasswordCallback.USERNAME_TOKEN
                    && "bob".equals(pc.getIdentifier())) {
                    pc.setPassword("security");
                } else {
                    throw new IOException("Authentication failed");
                }
            } else {
                throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
            }
        }
    }

}
