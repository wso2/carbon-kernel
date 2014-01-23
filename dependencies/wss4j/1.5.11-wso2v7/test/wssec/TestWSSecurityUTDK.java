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
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;
import org.apache.ws.security.message.WSSecDKEncrypt;
import org.apache.ws.security.message.WSSecDKSign;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecUsernameToken;
import org.apache.ws.security.message.token.UsernameToken;
import org.apache.ws.security.util.Base64;
import org.apache.ws.security.util.WSSecurityUtil;
import org.apache.xml.security.signature.XMLSignature;
import org.w3c.dom.Document;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Vector;

/**
 * WS-Security Test Case for UsernameToken Key Derivation, as defined in the 
 * UsernameTokenProfile 1.1 specification. The derived keys are used to encrypt
 * and sign, as per wsc:DerivedKeyToken.
 */
public class TestWSSecurityUTDK extends TestCase implements CallbackHandler {
    private static final Log LOG = LogFactory.getLog(TestWSSecurityUTDK.class);
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
    public TestWSSecurityUTDK(String name) {
        super(name);
    }

    /**
     * JUnit suite
     * <p/>
     * 
     * @return a junit test suite
     */
    public static Test suite() {
        return new TestSuite(TestWSSecurityUTDK.class);
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
     * Unit test for the UsernameToken derived key functionality 
     */
    public void testUsernameTokenUnit() throws Exception {
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        UsernameToken usernameToken = new UsernameToken(true, doc, null);
        usernameToken.setName("bob");
        
        byte[] salt = usernameToken.addSalt(doc, null, false);
        assertTrue(salt.length == 16);
        assertTrue(salt[15] == 0x02);
        byte[] utSalt = usernameToken.getSalt();
        assertTrue(salt.length == utSalt.length);
        for (int i = 0; i < salt.length; i++) {
            assertTrue(salt[i] == utSalt[i]);
        }
        
        usernameToken.addIteration(doc, 500);
        assertTrue(usernameToken.getIteration() == 500);
        
        WSSecurityUtil.prependChildElement(
            secHeader.getSecurityHeader(), usernameToken.getElement()
        );
        
        String outputString = 
            org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(doc);
        assertTrue(outputString.indexOf("wsse:Username") != -1);
        assertTrue(outputString.indexOf("wsse:Password") == -1);
        assertTrue(outputString.indexOf("wsse11:Salt") != -1);
        assertTrue(outputString.indexOf("wsse11:Iteration") != -1);
        
        byte[] derivedKey = UsernameToken.generateDerivedKey("security", salt, 500);
        assertTrue(derivedKey.length == 20);
        
        // "c2VjdXJpdHk=" is the Base64 encoding of "security"
        derivedKey = UsernameToken.generateDerivedKey(Base64.decode("c2VjdXJpdHk="), salt, 500);
        assertTrue(derivedKey.length == 20);
    }

    /**
     * Test for encoded passwords.
     */
    public void testDerivedKeyWithEncodedPasswordBaseline() throws Exception {
        String password = "password";
        // The SHA-1 of the password is known as a password equivalent in the UsernameToken specification.
        byte[] passwordHash = MessageDigest.getInstance("SHA-1").digest(password.getBytes("UTF-8"));

        byte[] salt = Base64.decode("LKpycbfgRzwDnBz6kkhAAQ==");
        int iteration = 1049;
        byte[] expectedDerivedKey = Base64.decode("C7Ll/OY4TECb6hZuMMiX/5hzszo=");
        byte[] derivedKey = UsernameToken.generateDerivedKey(passwordHash, salt, iteration);
        assertTrue("the derived key is not as expected", Arrays.equals(expectedDerivedKey, derivedKey));
    }

    /**
     * Test using a UsernameToken derived key for encrypting a SOAP body
     */
    public void testDerivedKeyEncryption() throws Exception {
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        WSSecUsernameToken builder = new WSSecUsernameToken();
        builder.setUserInfo("bob", "security");
        builder.addDerivedKey(false, null, 1000);
        builder.prepare(doc);
        
        byte[] derivedKey = builder.getDerivedKey();
        assertTrue(derivedKey.length == 20);
        
        String tokenIdentifier = builder.getId();
        
        //
        // Derived key encryption
        //
        WSSecDKEncrypt encrBuilder = new WSSecDKEncrypt();
        encrBuilder.setSymmetricEncAlgorithm(WSConstants.AES_128);
        encrBuilder.setExternalKey(derivedKey, tokenIdentifier);
        Document encryptedDoc = encrBuilder.build(doc, secHeader);
        
        builder.prependToHeader(secHeader);
        
        String outputString = 
            org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(encryptedDoc);
        assertTrue(outputString.indexOf("wsse:Username") != -1);
        assertTrue(outputString.indexOf("wsse:Password") == -1);
        assertTrue(outputString.indexOf("wsse11:Salt") != -1);
        assertTrue(outputString.indexOf("wsse11:Iteration") != -1);
        assertTrue(outputString.indexOf("testMethod") == -1);
        if (LOG.isDebugEnabled()) {
            LOG.debug(outputString);
        }
        
        verify(encryptedDoc);
    }
    
    /**
     * Test using a UsernameToken derived key for encrypting a SOAP body
     */
    public void testDerivedKeyEncryptionWithEncodedPassword() throws Exception {
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        WSSecUsernameToken builder = new WSSecUsernameToken();
        builder.setPasswordsAreEncoded(true);
        builder.setUserInfo("bob", Base64.encode(MessageDigest.getInstance("SHA-1").digest("security".getBytes("UTF-8"))));
        builder.addDerivedKey(false, null, 1000);
        builder.prepare(doc);
        
        byte[] derivedKey = builder.getDerivedKey();
        assertTrue(derivedKey.length == 20);
        
        String tokenIdentifier = builder.getId();
        
        //
        // Derived key encryption
        //
        WSSecDKEncrypt encrBuilder = new WSSecDKEncrypt();
        encrBuilder.setSymmetricEncAlgorithm(WSConstants.AES_128);
        encrBuilder.setExternalKey(derivedKey, tokenIdentifier);
        Document encryptedDoc = encrBuilder.build(doc, secHeader);
        
        builder.prependToHeader(secHeader);
        
        String outputString = 
            org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(encryptedDoc);
        assertTrue(outputString.indexOf("wsse:Username") != -1);
        assertTrue(outputString.indexOf("wsse:Password") == -1);
        assertTrue(outputString.indexOf("wsse11:Salt") != -1);
        assertTrue(outputString.indexOf("wsse11:Iteration") != -1);
        assertTrue(outputString.indexOf("testMethod") == -1);
        if (LOG.isDebugEnabled()) {
            LOG.debug(outputString);
        }
        
        boolean passwordsAreEnabledOrig = WSSecurityEngine.getInstance().getWssConfig().getPasswordsAreEncoded();
        try {
            WSSecurityEngine.getInstance().getWssConfig().setPasswordsAreEncoded(true);
            verify(encryptedDoc);
        } finally {
            WSSecurityEngine.getInstance().getWssConfig().setPasswordsAreEncoded(passwordsAreEnabledOrig);
        }
    }
    
    /**
     * Test using a UsernameToken derived key for encrypting a SOAP body. In this test the
     * derived key is modified before encryption, and so decryption should fail.
     */
    public void testDerivedKeyChangedEncryption() throws Exception {
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        WSSecUsernameToken builder = new WSSecUsernameToken();
        builder.setUserInfo("bob", "security");
        builder.addDerivedKey(false, null, 1000);
        builder.prepare(doc);
        
        byte[] derivedKey = builder.getDerivedKey();
        derivedKey[5] = 12;
        assertTrue(derivedKey.length == 20);
        
        String tokenIdentifier = builder.getId();
        
        //
        // Derived key encryption
        //
        WSSecDKEncrypt encrBuilder = new WSSecDKEncrypt();
        encrBuilder.setSymmetricEncAlgorithm(WSConstants.AES_128);
        encrBuilder.setExternalKey(derivedKey, tokenIdentifier);
        Document encryptedDoc = encrBuilder.build(doc, secHeader);
        
        builder.prependToHeader(secHeader);
        
        String outputString = 
            org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(encryptedDoc);
        assertTrue(outputString.indexOf("wsse:Username") != -1);
        assertTrue(outputString.indexOf("wsse:Password") == -1);
        assertTrue(outputString.indexOf("wsse11:Salt") != -1);
        assertTrue(outputString.indexOf("wsse11:Iteration") != -1);
        assertTrue(outputString.indexOf("testMethod") == -1);
        if (LOG.isDebugEnabled()) {
            LOG.debug(outputString);
        }
        
        try {
            verify(encryptedDoc);
            throw new Exception("Failure expected on a bad derived encryption");
        } catch (WSSecurityException ex) {
            assertTrue(ex.getErrorCode() == WSSecurityException.FAILED_CHECK);
            // expected
        }
    }
    
    /**
     * Test using a UsernameToken derived key for encrypting a SOAP body. In this test the
     * user is "alice" rather than "bob", and so decryption should fail.
     */
    public void testDerivedKeyBadUserEncryption() throws Exception {
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        WSSecUsernameToken builder = new WSSecUsernameToken();
        builder.setUserInfo("alice", "security");
        builder.addDerivedKey(false, null, 1000);
        builder.prepare(doc);
        
        byte[] derivedKey = builder.getDerivedKey();
        assertTrue(derivedKey.length == 20);
        
        String tokenIdentifier = builder.getId();
        
        //
        // Derived key encryption
        //
        WSSecDKEncrypt encrBuilder = new WSSecDKEncrypt();
        encrBuilder.setSymmetricEncAlgorithm(WSConstants.AES_128);
        encrBuilder.setExternalKey(derivedKey, tokenIdentifier);
        Document encryptedDoc = encrBuilder.build(doc, secHeader);
        
        builder.prependToHeader(secHeader);
        
        String outputString = 
            org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(encryptedDoc);
        assertTrue(outputString.indexOf("wsse:Username") != -1);
        assertTrue(outputString.indexOf("wsse:Password") == -1);
        assertTrue(outputString.indexOf("wsse11:Salt") != -1);
        assertTrue(outputString.indexOf("wsse11:Iteration") != -1);
        assertTrue(outputString.indexOf("testMethod") == -1);
        if (LOG.isDebugEnabled()) {
            LOG.debug(outputString);
        }
        
        try {
            verify(encryptedDoc);
            throw new Exception("Failure expected on a bad derived encryption");
        } catch (WSSecurityException ex) {
            assertTrue(ex.getErrorCode() == WSSecurityException.FAILED_AUTHENTICATION);
            // expected
        }
    }
    
    /**
     * Test using a UsernameToken derived key for signing a SOAP body
     */
    public void testDerivedKeySignature() throws Exception {
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        WSSecUsernameToken builder = new WSSecUsernameToken();
        builder.setUserInfo("bob", "security");
        builder.addDerivedKey(true, null, 1000);
        builder.prepare(doc);
        
        byte[] derivedKey = builder.getDerivedKey();
        assertTrue(derivedKey.length == 20);
        
        String tokenIdentifier = builder.getId();
        
        //
        // Derived key signature
        //
        WSSecDKSign sigBuilder = new WSSecDKSign();
        sigBuilder.setExternalKey(derivedKey, tokenIdentifier);
        sigBuilder.setSignatureAlgorithm(XMLSignature.ALGO_ID_MAC_HMAC_SHA1);
        Document signedDoc = sigBuilder.build(doc, secHeader);
        
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
            WSSecurityUtil.fetchActionResult(results, WSConstants.SIGN);
        java.security.Principal principal = 
            (java.security.Principal) actionResult.get(WSSecurityEngineResult.TAG_PRINCIPAL);
        //System.out.println(principal.getName());
        assertTrue(principal.getName().indexOf("derivedKey") != -1);
    }
    
    /**
     * Test using a UsernameToken derived key for signing a SOAP body
     */
    public void testDerivedKeySignatureWithEncodedPassword() throws Exception {
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        WSSecUsernameToken builder = new WSSecUsernameToken();
        builder.setPasswordsAreEncoded(true);
        builder.setUserInfo("bob", Base64.encode(MessageDigest.getInstance("SHA-1").digest("security".getBytes("UTF-8"))));
        builder.addDerivedKey(true, null, 1000);
        builder.prepare(doc);
        
        byte[] derivedKey = builder.getDerivedKey();
        assertTrue(derivedKey.length == 20);
        
        String tokenIdentifier = builder.getId();
        
        //
        // Derived key signature
        //
        WSSecDKSign sigBuilder = new WSSecDKSign();
        sigBuilder.setExternalKey(derivedKey, tokenIdentifier);
        sigBuilder.setSignatureAlgorithm(XMLSignature.ALGO_ID_MAC_HMAC_SHA1);
        Document signedDoc = sigBuilder.build(doc, secHeader);
        
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
        
        boolean passwordsAreEnabledOrig = WSSecurityEngine.getInstance().getWssConfig().getPasswordsAreEncoded();
        try {
            WSSecurityEngine.getInstance().getWssConfig().setPasswordsAreEncoded(true);
            Vector results = verify(signedDoc);
            WSSecurityEngineResult actionResult =
                WSSecurityUtil.fetchActionResult(results, WSConstants.SIGN);
            java.security.Principal principal = 
                (java.security.Principal) actionResult.get(WSSecurityEngineResult.TAG_PRINCIPAL);
            //System.out.println(principal.getName());
            assertTrue(principal.getName().indexOf("derivedKey") != -1);
        } finally {
            WSSecurityEngine.getInstance().getWssConfig().setPasswordsAreEncoded(passwordsAreEnabledOrig);
        }
    }
    
    /**
     * Test using a UsernameToken derived key for signing a SOAP body. In this test the
     * derived key is modified before signature, and so signature verification should
     * fail.
     */
    public void testDerivedKeyChangedSignature() throws Exception {
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        WSSecUsernameToken builder = new WSSecUsernameToken();
        builder.setUserInfo("bob", "security");
        builder.addDerivedKey(true, null, 1000);
        builder.prepare(doc);
        
        byte[] derivedKey = builder.getDerivedKey();
        derivedKey[5] = 12;
        assertTrue(derivedKey.length == 20);
        
        String tokenIdentifier = builder.getId();
        
        //
        // Derived key signature
        //
        WSSecDKSign sigBuilder = new WSSecDKSign();
        sigBuilder.setExternalKey(derivedKey, tokenIdentifier);
        sigBuilder.setSignatureAlgorithm(XMLSignature.ALGO_ID_MAC_HMAC_SHA1);
        Document signedDoc = sigBuilder.build(doc, secHeader);
        
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
            assertTrue(ex.getErrorCode() == WSSecurityException.FAILED_CHECK);
            // expected
        }
    }
    
    /**
     * Test using a UsernameToken derived key for signing a SOAP body. In this test the
     * user is "alice" rather than "bob", and so signature verification should fail.
     */
    public void testDerivedKeyBadUserSignature() throws Exception {
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        WSSecUsernameToken builder = new WSSecUsernameToken();
        builder.setUserInfo("alice", "security");
        builder.addDerivedKey(true, null, 1000);
        builder.prepare(doc);
        
        byte[] derivedKey = builder.getDerivedKey();
        assertTrue(derivedKey.length == 20);
        
        String tokenIdentifier = builder.getId();
        
        //
        // Derived key signature
        //
        WSSecDKSign sigBuilder = new WSSecDKSign();
        sigBuilder.setExternalKey(derivedKey, tokenIdentifier);
        sigBuilder.setSignatureAlgorithm(XMLSignature.ALGO_ID_MAC_HMAC_SHA1);
        Document signedDoc = sigBuilder.build(doc, secHeader);
        
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
                    if (WSSecurityEngine.getInstance().getWssConfig().getPasswordsAreEncoded()) {
                        // "jux7xGGAjguKKHg9C+waOiLrCCE=" is the Base64 encoded SHA-1 hash of "security".
                        pc.setPassword("jux7xGGAjguKKHg9C+waOiLrCCE=");
                    } else {
                        pc.setPassword("security");
                    }
                } else {
                    throw new IOException("Authentication failed");
                }
            } else {
                throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
            }
        }
    }

}
