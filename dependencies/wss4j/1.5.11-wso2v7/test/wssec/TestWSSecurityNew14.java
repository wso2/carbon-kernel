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
import org.apache.ws.security.WSSConfig;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;
import org.apache.ws.security.handler.RequestData;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.message.WSSecEncrypt;
import org.apache.ws.security.message.WSSecSignature;
import org.apache.ws.security.message.WSSecHeader;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;


/**
 * WS-Security Test Case for using the ThumbprintSHA1 key identifier for
 * signature and encryption, and the EncryptedKeySHA1 key identifier for encryption.
 * <p/>
 * 
 * @author Davanum Srinivas (dims@yahoo.com)
 */
public class TestWSSecurityNew14 extends TestCase implements CallbackHandler {
    private static final Log LOG = LogFactory.getLog(TestWSSecurityNew14.class);
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
    private byte[] keyData;
    private SecretKey key;

    /**
     * TestWSSecurity constructor
     * <p/>
     * 
     * @param name name of the test
     */
    public TestWSSecurityNew14(String name) {
        super(name);
    }

    /**
     * JUnit suite
     * <p/>
     * 
     * @return a junit test suite
     */
    public static Test suite() {
        return new TestSuite(TestWSSecurityNew14.class);
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
        
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        key = keyGen.generateKey();
        keyData = key.getEncoded();
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
     * Test that signs and verifies a WS-Security envelope.
     * The test uses the ThumbprintSHA1 key identifier type. 
     * <p/>
     * 
     * @throws java.lang.Exception Thrown when there is any problem in signing or verification
     */
    public void testX509SignatureThumb() throws Exception {
        WSSecSignature builder = new WSSecSignature();
        builder.setUserInfo("16c73ab6-b892-458f-abf5-2f875f74882e", "security");
        builder.setKeyIdentifierType(WSConstants.THUMBPRINT_IDENTIFIER);
        // builder.setUserInfo("john", "keypass");
        LOG.info("Before Signing ThumbprintSHA1....");
        Document doc = unsignedEnvelope.getAsDocument();
        
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);

        Document signedDoc = builder.build(doc, crypto, secHeader);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Signed message with ThumbprintSHA1 key identifier:");
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(signedDoc);
            LOG.debug(outputString);
        }
        LOG.info("After Signing ThumbprintSHA1....");
        verify(signedDoc);
    }

    /**
     * Test that signs (twice) and verifies a WS-Security envelope.
     * The test uses the ThumbprintSHA1 key identifier type.
     * <p/>
     * 
     * @throws java.lang.Exception Thrown when there is any problem in signing or verification
     */
    public void testDoubleX509SignatureThumb() throws Exception {
        WSSecSignature builder = new WSSecSignature();
        builder.setUserInfo("16c73ab6-b892-458f-abf5-2f875f74882e", "security");
        // builder.setUserInfo("john", "keypass");
        builder.setKeyIdentifierType(WSConstants.THUMBPRINT_IDENTIFIER);        
        Document doc = unsignedEnvelope.getAsDocument();
        
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);

        Document signedDoc = builder.build(doc, crypto, secHeader);
        Document signedDoc1 = builder.build(signedDoc, crypto, secHeader);
        verify(signedDoc1);
    }
    
    /**
     * Test that encrypts and decrypts a WS-Security envelope.
     * The test uses the ThumbprintSHA1 key identifier type. 
     * <p/>
     * 
     * @throws java.lang.Exception Thrown when there is any problem in encryption or decryption
     */
    public void testX509EncryptionThumb() throws Exception {
        WSSecEncrypt builder = new WSSecEncrypt();
        builder.setUserInfo("16c73ab6-b892-458f-abf5-2f875f74882e", "security");
        builder.setKeyIdentifierType(WSConstants.THUMBPRINT_IDENTIFIER);
        
        LOG.info("Before Encrypting ThumbprintSHA1....");
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);        
        Document encryptedDoc = builder.build(doc, crypto, secHeader);
        
        String outputString = 
            org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(encryptedDoc);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Encrypted message with THUMBPRINT_IDENTIFIER:");
            LOG.debug(outputString);
        }
        assertTrue(outputString.indexOf("#ThumbprintSHA1") != -1);
    
        LOG.info("After Encrypting ThumbprintSHA1....");
        verify(encryptedDoc);
    }
        
    /**
     * Test that encrypts and decrypts a WS-Security envelope.
     * The test uses the EncryptedKeySHA1 key identifier type. 
     * <p/>
     * 
     * @throws java.lang.Exception Thrown when there is any problem in encryption or decryption
     */
    public void testX509EncryptionSHA1() throws Exception {
        WSSecEncrypt builder = new WSSecEncrypt();
        builder.setUserInfo("16c73ab6-b892-458f-abf5-2f875f74882e", "security");
        builder.setKeyIdentifierType(WSConstants.ENCRYPTED_KEY_SHA1_IDENTIFIER);
        builder.setUseKeyIdentifier(true);
     
        LOG.info("Before Encrypting EncryptedKeySHA1....");
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);        
        Document encryptedDoc = builder.build(doc, crypto, secHeader);
     
        String outputString = 
            org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(encryptedDoc);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Encrypted message with ENCRYPTED_KEY_SHA1_IDENTIFIER:");
            LOG.debug(outputString);
        }
        assertTrue(outputString.indexOf("#EncryptedKeySHA1") != -1);
     
        LOG.info("After Encrypting EncryptedKeySHA1....");
        verify(encryptedDoc);
    }
    
    /**
     * Test that encrypts using EncryptedKeySHA1, where it uses a symmetric key, rather than a 
     * generated session key which is then encrypted using a public key.
     * 
     * @throws java.lang.Exception Thrown when there is any problem in encryption or decryption
     */
    public void testEncryptionSHA1Symmetric() throws Exception {
        WSSecEncrypt builder = new WSSecEncrypt();
        builder.setKeyIdentifierType(WSConstants.ENCRYPTED_KEY_SHA1_IDENTIFIER);
        builder.setSymmetricKey(key);
        builder.setEncryptSymmKey(false);
        builder.setUseKeyIdentifier(true);
        
        LOG.info("Before Encrypting EncryptedKeySHA1....");
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);        
        Document encryptedDoc = builder.build(doc, crypto, secHeader);
     
        String outputString = 
            org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(encryptedDoc);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Encrypted message with ENCRYPTED_KEY_SHA1_IDENTIFIER:");
            LOG.debug(outputString);
        }
        assertTrue(outputString.indexOf("#EncryptedKeySHA1") != -1);
     
        LOG.info("After Encrypting EncryptedKeySHA1....");
        verify(encryptedDoc);
    }
    
    
    /**
     * Test that encrypts using EncryptedKeySHA1, where it uses a symmetric key (bytes), 
     * rather than a generated session key which is then encrypted using a public key.
     * 
     * @throws java.lang.Exception Thrown when there is any problem in encryption or decryption
     */
    public void testEncryptionSHA1SymmetricBytes() throws Exception {
        WSSecEncrypt builder = new WSSecEncrypt();
        builder.setKeyIdentifierType(WSConstants.ENCRYPTED_KEY_SHA1_IDENTIFIER);
        builder.setEphemeralKey(keyData);
        builder.setEncryptSymmKey(false);
        builder.setUseKeyIdentifier(true);
        
        LOG.info("Before Encrypting EncryptedKeySHA1....");
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);        
        Document encryptedDoc = builder.build(doc, crypto, secHeader);
     
        String outputString = 
            org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(encryptedDoc);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Encrypted message with ENCRYPTED_KEY_SHA1_IDENTIFIER:");
            LOG.debug(outputString);
        }
        assertTrue(outputString.indexOf("#EncryptedKeySHA1") != -1);
     
        LOG.info("After Encrypting EncryptedKeySHA1....");
        verify(encryptedDoc);
    }
    
    
    /**
     * Test that encrypts using EncryptedKeySHA1, where it uses a symmetric key, rather than a 
     * generated session key which is then encrypted using a public key. The request is generated
     * using WSHandler, instead of coding it.
     * 
     * @throws java.lang.Exception Thrown when there is any problem in encryption or decryption
     * 
     */
    public void testEncryptionSHA1SymmetricBytesHandler() throws Exception {
        final WSSConfig cfg = WSSConfig.getNewInstance();
        final RequestData reqData = new RequestData();
        reqData.setWssConfig(cfg);
        java.util.Map messageContext = new java.util.TreeMap();
        messageContext.put(WSHandlerConstants.ENC_SYM_ENC_KEY, "false");
        messageContext.put(WSHandlerConstants.ENC_KEY_ID, "EncryptedKeySHA1");
        messageContext.put(WSHandlerConstants.PW_CALLBACK_REF, this);
        reqData.setMsgContext(messageContext);
        reqData.setUsername("");
        
        final java.util.Vector actions = new java.util.Vector();
        actions.add(new Integer(WSConstants.ENCR));
        
        Document doc = unsignedEnvelope.getAsDocument();
        MyHandler handler = new MyHandler();
        handler.send(
            WSConstants.ENCR, 
            doc, 
            reqData, 
            actions,
            true
        );
        
        String outputString = 
            org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(doc);
        if (LOG.isDebugEnabled()) {
            LOG.debug(outputString);
        }
        
        verify(doc);
    }
    

    /**
     * Verifies the soap envelope.
     * This method verifies all the signature generated. 
     * 
     * @param env soap envelope
     * @throws java.lang.Exception Thrown when there is a problem in verification
     */
    private void verify(Document doc) throws Exception {
        secEngine.processSecurityHeader(doc, null, this, crypto);
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
                pc.setKey(keyData);
            } else {
                throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
            }
        }
    }
}
