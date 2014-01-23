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

import org.apache.ws.security.saml.SAMLIssuerFactory;
import org.apache.ws.security.saml.SAMLIssuer;

import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.client.AxisClient;
import org.apache.axis.configuration.NullProvider;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSConfig;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.saml.WSSecSignatureSAML;
import org.apache.ws.security.util.WSSecurityUtil;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;
import org.apache.ws.security.handler.RequestData;
import org.apache.ws.security.handler.WSHandler;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.message.WSSecHeader;
import org.w3c.dom.Document;

import org.opensaml.SAMLAssertion;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

/**
 * Test-case for sending and processing an signed (sender vouches) SAML Assertion.
 * 
 * @author Davanum Srinivas (dims@yahoo.com)
 */
public class TestWSSecurityNewST2 extends TestCase implements CallbackHandler {
    private static final Log LOG = LogFactory.getLog(TestWSSecurityNewST2.class);
    private static final String SOAPMSG = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
        + "<SOAP-ENV:Envelope "
        +   "xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" "
        +   "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "
        +   "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" 
        +   "<SOAP-ENV:Body>" 
        +      "<ns1:testMethod xmlns:ns1=\"uri:LogTestService2\"></ns1:testMethod>" 
        +   "</SOAP-ENV:Body>" 
        + "</SOAP-ENV:Envelope>";

    private WSSecurityEngine secEngine = new WSSecurityEngine();
    private Crypto crypto = CryptoFactory.getInstance("crypto.properties");
    private MessageContext msgContext;
    private Message message;

    /**
     * TestWSSecurity constructor
     * 
     * @param name name of the test
     */
    public TestWSSecurityNewST2(String name) {
        super(name);
    }

    /**
     * JUnit suite
     * 
     * @return a junit test suite
     */
    public static Test suite() {
        return new TestSuite(TestWSSecurityNewST2.class);
    }

    /**
     * Setup method
     * 
     * @throws Exception Thrown when there is a problem in setup
     */
    protected void setUp() throws Exception {
        AxisClient tmpEngine = new AxisClient(new NullProvider());
        msgContext = new MessageContext(tmpEngine);
        message = getSOAPMessage();
    }

    /**
     * Constructs a soap envelope
     * 
     * @return soap envelope
     * @throws Exception if there is any problem constructing the soap envelope
     */
    protected Message getSOAPMessage() throws Exception {
        InputStream in = new ByteArrayInputStream(SOAPMSG.getBytes());
        Message msg = new Message(in);
        msg.setMessageContext(msgContext);
        return msg;
    }

    /**
     * Test that creates, sends and processes an signed SAML assertion.
     */
    public void testSAMLSignedSenderVouches() throws Exception {
        SOAPEnvelope unsignedEnvelope = message.getSOAPEnvelope();
        SAMLIssuer saml = SAMLIssuerFactory.getInstance("saml.properties");

        SAMLAssertion assertion = saml.newAssertion();

        String issuerKeyName = saml.getIssuerKeyName();
        String issuerKeyPW = saml.getIssuerKeyPassword();
        Crypto issuerCrypto = saml.getIssuerCrypto();
        WSSecSignatureSAML wsSign = new WSSecSignatureSAML();
        wsSign.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);
        
        LOG.info("Before SAMLSignedSenderVouches....");
        
        Document doc = unsignedEnvelope.getAsDocument();

        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        Document signedDoc = 
            wsSign.build(doc, null, assertion, issuerCrypto, issuerKeyName, issuerKeyPW, secHeader);
        LOG.info("After SAMLSignedSenderVouches....");

        if (LOG.isDebugEnabled()) {
            LOG.debug("Signed SAML message (sender vouches):");
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(signedDoc);
            LOG.debug(outputString);
        }
        
        Vector results = verify(signedDoc);
        WSSecurityEngineResult actionResult =
            WSSecurityUtil.fetchActionResult(results, WSConstants.ST_UNSIGNED);
        SAMLAssertion receivedAssertion = 
            (SAMLAssertion) actionResult.get(WSSecurityEngineResult.TAG_SAML_ASSERTION);
        assertTrue(receivedAssertion != null);
    }
    
    
    /**
     * Test that creates, sends and processes an signed SAML assertion using a KeyIdentifier
     * instead of direct reference.
     */
    public void testSAMLSignedSenderVouchesKeyIdentifier() throws Exception {
        SOAPEnvelope unsignedEnvelope = message.getSOAPEnvelope();
        SAMLIssuer saml = SAMLIssuerFactory.getInstance("saml.properties");

        SAMLAssertion assertion = saml.newAssertion();

        String issuerKeyName = saml.getIssuerKeyName();
        String issuerKeyPW = saml.getIssuerKeyPassword();
        Crypto issuerCrypto = saml.getIssuerCrypto();
        WSSecSignatureSAML wsSign = new WSSecSignatureSAML();
        wsSign.setKeyIdentifierType(WSConstants.X509_KEY_IDENTIFIER);
        
        LOG.info("Before SAMLSignedSenderVouches....");
        
        Document doc = unsignedEnvelope.getAsDocument();

        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        Document signedDoc = 
            wsSign.build(doc, null, assertion, issuerCrypto, issuerKeyName, issuerKeyPW, secHeader);
        LOG.info("After SAMLSignedSenderVouches....");

        if (LOG.isDebugEnabled()) {
            LOG.debug("Signed SAML message (sender vouches):");
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(signedDoc);
            LOG.debug(outputString);
        }
        
        Vector results = verify(signedDoc);
        WSSecurityEngineResult actionResult =
            WSSecurityUtil.fetchActionResult(results, WSConstants.ST_UNSIGNED);
        SAMLAssertion receivedAssertion = 
            (SAMLAssertion) actionResult.get(WSSecurityEngineResult.TAG_SAML_ASSERTION);
        assertTrue(receivedAssertion != null);
    }
    
    
    /**
     * Test the default issuer class as specified in SAMLIssuerFactory. The configuration
     * file "saml3.properties" has no "org.apache.ws.security.saml.issuerClass" property,
     * and so the default value is used (A bad value was previously used for the default
     * value).
     */
    public void testDefaultIssuerClass() throws Exception {
        SOAPEnvelope unsignedEnvelope = message.getSOAPEnvelope();
        SAMLIssuer saml = SAMLIssuerFactory.getInstance("saml3.properties");

        SAMLAssertion assertion = saml.newAssertion();

        String issuerKeyName = saml.getIssuerKeyName();
        String issuerKeyPW = saml.getIssuerKeyPassword();
        Crypto issuerCrypto = saml.getIssuerCrypto();
        WSSecSignatureSAML wsSign = new WSSecSignatureSAML();
        wsSign.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);
        
        LOG.info("Before SAMLSignedSenderVouches....");
        
        Document doc = unsignedEnvelope.getAsDocument();

        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        Document signedDoc = wsSign.build(doc, null, assertion, issuerCrypto, issuerKeyName, issuerKeyPW, secHeader);
        LOG.info("After SAMLSignedSenderVouches....");

        if (LOG.isDebugEnabled()) {
            LOG.debug("Signed SAML message (sender vouches):");
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(signedDoc);
            LOG.debug(outputString);
        }
        
        Vector results = verify(signedDoc);
        WSSecurityEngineResult actionResult =
            WSSecurityUtil.fetchActionResult(results, WSConstants.ST_UNSIGNED);
        SAMLAssertion receivedAssertion = 
            (SAMLAssertion) actionResult.get(WSSecurityEngineResult.TAG_SAML_ASSERTION);
        assertTrue(receivedAssertion != null);
    }
    
    
    /**
     * A test for WSS-62: "the crypto file not being retrieved in the doReceiverAction
     * method for the Saml Signed Token"
     * 
     * https://issues.apache.org/jira/browse/WSS-62
     */
    public void testWSS62() throws Exception {
        SOAPEnvelope unsignedEnvelope = message.getSOAPEnvelope();
        SAMLIssuer saml = SAMLIssuerFactory.getInstance("saml.properties");

        SAMLAssertion assertion = saml.newAssertion();

        String issuerKeyName = saml.getIssuerKeyName();
        String issuerKeyPW = saml.getIssuerKeyPassword();
        Crypto issuerCrypto = saml.getIssuerCrypto();
        WSSecSignatureSAML wsSign = new WSSecSignatureSAML();
        wsSign.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);
        
        Document doc = unsignedEnvelope.getAsDocument();

        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        Document signedDoc = 
            wsSign.build(doc, null, assertion, issuerCrypto, issuerKeyName, issuerKeyPW, secHeader);
        
        //
        // Now verify it but first call Handler#doReceiverAction
        //
        final WSSConfig cfg = WSSConfig.getNewInstance();
        final RequestData reqData = new RequestData();
        reqData.setWssConfig(cfg);
        reqData.setMsgContext(new java.util.TreeMap());
        java.util.Map msgContext = new java.util.HashMap();
        msgContext.put(WSHandlerConstants.SIG_PROP_FILE, "crypto.properties");
        reqData.setMsgContext(msgContext);
        
        MyHandler handler = new MyHandler();
        handler.doit(WSConstants.ST_SIGNED, reqData);
        
        secEngine.processSecurityHeader(
            signedDoc, null, this, reqData.getSigCrypto(), reqData.getDecCrypto()
        );
        
        //
        // Negative test
        //
        msgContext.put(WSHandlerConstants.SIG_PROP_FILE, "crypto.properties.na");
        reqData.setMsgContext(msgContext);
        
        handler = new MyHandler();
        try {
            handler.doit(WSConstants.ST_SIGNED, reqData);
            fail("Failure expected on a bad crypto properties file");
        } catch (RuntimeException ex) {
            // expected
        }
    }

    
    /**
     * Verifies the soap envelope
     * 
     * @param doc
     * @throws Exception Thrown when there is a problem in verification
     */
    private Vector verify(Document doc) throws Exception {
        Vector results = secEngine.processSecurityHeader(doc, null, this, crypto);
        String outputString = 
            org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(doc);
        assertTrue(outputString.indexOf("LogTestService2") > 0 ? true : false);
        return results;
    }

    public void handle(Callback[] callbacks)
        throws IOException, UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof WSPasswordCallback) {
                WSPasswordCallback pc = (WSPasswordCallback) callbacks[i];
                /*
                 * here call a function/method to lookup the password for
                 * the given identifier (e.g. a user name or keystore alias)
                 * e.g.: pc.setPassword(passStore.getPassword(pc.getIdentfifier))
                 * for Testing we supply a fixed name here.
                 */
                pc.setPassword("security");
            } else {
                throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
            }
        }
    }
    
    /**
     * a trivial extension of the WSHandler type
     */
    public static class MyHandler extends WSHandler {
        
        public Object 
        getOption(String key) {
            return null;
        }
        
        public void 
        setProperty(
            Object msgContext, 
            String key, 
            Object value
        ) {
        }

        public Object 
        getProperty(Object ctx, String key) {
            java.util.Map ctxMap = (java.util.Map)ctx;
            return ctxMap.get(key);
        }
    
        public void 
        setPassword(Object msgContext, String password) {
        }
        
        public String 
        getPassword(Object msgContext) {
            return null;
        }

        void doit(
            int action, 
            RequestData reqData
        ) throws org.apache.ws.security.WSSecurityException {
            doReceiverAction(action, reqData);
        }
    }
}
