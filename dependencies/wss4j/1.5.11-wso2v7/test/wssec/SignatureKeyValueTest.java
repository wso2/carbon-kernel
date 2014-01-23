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
import org.apache.ws.security.PublicKeyCallback;
import org.apache.ws.security.PublicKeyPrincipal;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;
import org.apache.ws.security.message.WSSecSignature;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * This class tests signing where the the public key is transmitted in the message via
 * a ds:KeyInfo/ds:KeyValue element. Although this isn't strictly recommended for use in
 * WS-Security, it's necessary to support it for WCF interop.
 */
public class SignatureKeyValueTest extends TestCase implements CallbackHandler {
    private static final Log LOG = LogFactory.getLog(SignatureKeyValueTest.class);
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
    private Crypto crypto = CryptoFactory.getInstance("wss40.properties");
    private java.security.KeyStore keyStore = null;
    private MessageContext msgContext;
    private SOAPEnvelope unsignedEnvelope;

    /**
     * TestWSSecurity constructor
     * <p/>
     * 
     * @param name name of the test
     */
    public SignatureKeyValueTest(String name) {
        super(name);
        keyStore = crypto.getKeyStore();
    }

    /**
     * JUnit suite
     * <p/>
     * 
     * @return a junit test suite
     */
    public static Test suite() {
        return new TestSuite(SignatureKeyValueTest.class);
    }

    /**
     * Setup method
     * <p/>
     * 
     * @throws java.lang.Exception Thrown when there is a problem in setup
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
     * Successful RSAKeyValue test.
     */
    public void testRSAKeyValue() throws Exception {
        WSSecSignature builder = new WSSecSignature();
        builder.setUserInfo("wss40", "security");
        builder.setKeyIdentifierType(WSConstants.KEY_VALUE);
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        Document signedDoc = builder.build(doc, crypto, secHeader);

        String outputString = 
            org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(signedDoc);
        if (LOG.isDebugEnabled()) {
            LOG.debug(outputString);
        }
        assertTrue(outputString.indexOf("RSAKeyValue") != -1);
        
        final java.util.Vector results = verify(signedDoc);
        WSSecurityEngineResult actionResult = 
            WSSecurityUtil.fetchActionResult(results, WSConstants.SIGN);
        assertTrue(actionResult != null);
        
        java.security.Principal principal = 
            (java.security.Principal)actionResult.get(WSSecurityEngineResult.TAG_PRINCIPAL);
        assertTrue(principal instanceof PublicKeyPrincipal);
        java.security.PublicKey publicKey = 
            ((PublicKeyPrincipal)principal).getPublicKey();
        assertTrue(publicKey instanceof java.security.interfaces.RSAPublicKey);
        
    }
    
    
    /**
     * Failed RSAKeyValue test, where a message is signed using a key-pair which doesn't
     * correspond to the public key in the "trust"-store.
     */
    public void testBadRSAKeyValue() throws Exception {
        WSSecSignature builder = new WSSecSignature();
        builder.setUserInfo("wss86", "security");
        builder.setKeyIdentifierType(WSConstants.KEY_VALUE);
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        Document signedDoc = 
            builder.build(doc, CryptoFactory.getInstance("wss86.properties"), secHeader);

        String outputString = 
            org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(signedDoc);
        if (LOG.isDebugEnabled()) {
            LOG.debug(outputString);
        }
        assertTrue(outputString.indexOf("RSAKeyValue") != -1);
        
        try {
            verify(signedDoc);
            fail("Failure expected on bad public key");
        } catch (Exception ex) {
            // expected
        }
        
    }
    
    
    /**
     * Successful DSAKeyValue test.
     */
    public void testDSAKeyValue() throws Exception {
        WSSecSignature builder = new WSSecSignature();
        builder.setUserInfo("wss40DSA", "security");
        builder.setKeyIdentifierType(WSConstants.KEY_VALUE);
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        Document signedDoc = builder.build(doc, crypto, secHeader);

        String outputString = 
            org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(signedDoc);
        if (LOG.isDebugEnabled()) {
            LOG.debug(outputString);
        }
        assertTrue(outputString.indexOf("DSAKeyValue") != -1);
        
        final java.util.Vector results = verify(signedDoc);
        WSSecurityEngineResult actionResult = 
            WSSecurityUtil.fetchActionResult(results, WSConstants.SIGN);
        assertTrue(actionResult != null);
        
        java.security.Principal principal = 
            (java.security.Principal)actionResult.get(WSSecurityEngineResult.TAG_PRINCIPAL);
        assertTrue(principal instanceof PublicKeyPrincipal);
        java.security.PublicKey publicKey = 
            ((PublicKeyPrincipal)principal).getPublicKey();
        assertTrue(publicKey instanceof java.security.interfaces.DSAPublicKey);
    }


    /**
     * Verifies the soap envelope
     * <p/>
     * 
     * @param env soap envelope
     * @throws java.lang.Exception Thrown when there is a problem in verification
     */
    private java.util.Vector verify(Document doc) throws Exception {
        return secEngine.processSecurityHeader(doc, null, this, null);
    }
    
    public void handle(Callback[] callbacks) 
        throws IOException, UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof PublicKeyCallback) {
                PublicKeyCallback pc = (PublicKeyCallback) callbacks[i];
                java.security.PublicKey publicKey = pc.getPublicKey();
                if (publicKey == null || !pc.verifyTrust(keyStore)) {
                    throw new IOException("Authentication of public key failed");
                }
            } else {
                throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
            }
        }
    }

}
