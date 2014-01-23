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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.xml.crypto.dsig.SignatureMethod;

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
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSConfig;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;
import org.apache.ws.security.handler.RequestData;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.message.WSSecEncrypt;
import org.apache.ws.security.message.WSSecEncryptedKey;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecSignature;
import org.w3c.dom.Document;


/**
 * Test symmetric key signature created using an encrypted key
 * Demonstrates that Signature Crypto object can have null values when 
 * calling processSecurityHeader method of WSSecurityEngine.
 */
public class TestWSSecurityNew17 extends TestCase implements CallbackHandler {
    private static final Log LOG = LogFactory.getLog(TestWSSecurityNew17.class);
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
    private Message message;
    private byte[] keyData;

    /**
     * TestWSSecurity constructor
     * <p/>
     * 
     * @param name name of the test
     */
    public TestWSSecurityNew17(String name) {
        super(name);
    }

    /**
     * JUnit suite
     * <p/>
     * 
     * @return a junit test suite
     */
    public static Test suite() {
        return new TestSuite(TestWSSecurityNew17.class);
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
        message = getSOAPMessage();
        
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        SecretKey key = keyGen.generateKey();
        keyData = key.getEncoded();
    }

    /**
     * Constructs a soap envelope
     * <p/>
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
     * Test signing a message body using a symmetric key with EncryptedKeySHA1
     */
    public void testSymmetricSignatureSHA1() throws Exception {
        SOAPEnvelope unsignedEnvelope = message.getSOAPEnvelope();
        Document doc = unsignedEnvelope.getAsDocument();
        
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        WSSecSignature sign = new WSSecSignature();
        sign.setKeyIdentifierType(WSConstants.ENCRYPTED_KEY_SHA1_IDENTIFIER);
        sign.setSecretKey(keyData);
        sign.setSignatureAlgorithm(SignatureMethod.HMAC_SHA1);

        Document signedDoc = sign.build(doc, crypto, secHeader);
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Signed symmetric message SHA1:");
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(signedDoc);
            LOG.debug(outputString);
        }
        
        verify(signedDoc);
    }
    
    
    /**
     * Test signing a message body using a symmetric key with Direct Reference to an
     * EncryptedKey
     */
    public void testSymmetricSignatureDR() throws Exception {
        SOAPEnvelope unsignedEnvelope = message.getSOAPEnvelope();
        Document doc = unsignedEnvelope.getAsDocument();
        
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        WSSecEncryptedKey encrKey = new WSSecEncryptedKey();
        encrKey.setKeyIdentifierType(WSConstants.ISSUER_SERIAL);
        encrKey.setUserInfo("16c73ab6-b892-458f-abf5-2f875f74882e", "security");
        encrKey.setKeySize(192);
        encrKey.prepare(doc, crypto);
        
        WSSecSignature sign = new WSSecSignature();
        sign.setKeyIdentifierType(WSConstants.CUSTOM_SYMM_SIGNING);
        sign.setCustomTokenId(encrKey.getId());
        sign.setSecretKey(encrKey.getEphemeralKey());
        sign.setSignatureAlgorithm(SignatureMethod.HMAC_SHA1);
        sign.setCustomTokenValueType(
            WSConstants.SOAPMESSAGE_NS11 + "#" + WSConstants.ENC_KEY_VALUE_TYPE
        );

        Document signedDoc = sign.build(doc, crypto, secHeader);
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Signed symmetric message DR:");
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(signedDoc);
            LOG.debug(outputString);
        }
    }

    /**
     * Test that first signs, then encrypts a WS-Security envelope.
     * <p/>
     * 
     * @throws Exception Thrown when there is any problem in signing, encryption,
     *                   decryption, or verification
     */
    public void testEncryptedKeySignature() throws Exception {
        
        SOAPEnvelope unsignedEnvelope = message.getSOAPEnvelope();
        LOG.info("Before Sign/Encryption....");
        Document doc = unsignedEnvelope.getAsDocument();
        
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        WSSecEncryptedKey encrKey = new WSSecEncryptedKey();
        encrKey.setKeyIdentifierType(WSConstants.ISSUER_SERIAL);
        encrKey.setUserInfo("16c73ab6-b892-458f-abf5-2f875f74882e", "security");
        encrKey.setKeySize(192);
        encrKey.prepare(doc, crypto);   
        
        WSSecEncrypt encrypt = new WSSecEncrypt();
        encrypt.setEncKeyId(encrKey.getId());
        encrypt.setEphemeralKey(encrKey.getEphemeralKey());
        encrypt.setSymmetricEncAlgorithm(WSConstants.TRIPLE_DES);
        encrypt.setEncryptSymmKey(false);
        encrypt.setEncryptedKeyElement(encrKey.getEncryptedKeyElement());

        WSSecSignature sign = new WSSecSignature();
        sign.setKeyIdentifierType(WSConstants.CUSTOM_SYMM_SIGNING);
        sign.setCustomTokenId(encrKey.getId());
        sign.setSecretKey(encrKey.getEphemeralKey());
        sign.setSignatureAlgorithm(SignatureMethod.HMAC_SHA1);

        Document signedDoc = sign.build(doc, crypto, secHeader);
        Document encryptedSignedDoc = encrypt.build(signedDoc, crypto, secHeader);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Signed and encrypted message with IssuerSerial key identifier (both), 3DES:");
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(encryptedSignedDoc);
            LOG.debug(outputString);
        }
        
        LOG.info("After Sign/Encryption....");
        verify(encryptedSignedDoc);
    }
    
    
    /**
     * Test signing a message body using a symmetric key with EncryptedKeySHA1. 
     * The request is generated using WSHandler, instead of coding it.
     */
    public void testSymmetricSignatureSHA1Handler() throws Exception {
        final WSSConfig cfg = WSSConfig.getNewInstance();
        RequestData reqData = new RequestData();
        reqData.setWssConfig(cfg);
        java.util.Map messageContext = new java.util.TreeMap();
        messageContext.put(WSHandlerConstants.SIG_KEY_ID, "EncryptedKeySHA1");
        messageContext.put(WSHandlerConstants.SIG_ALGO, SignatureMethod.HMAC_SHA1);
        messageContext.put(WSHandlerConstants.PW_CALLBACK_REF, this);
        reqData.setMsgContext(messageContext);
        reqData.setUsername("");
        
        final java.util.Vector actions = new java.util.Vector();
        actions.add(new Integer(WSConstants.SIGN));
        
        SOAPEnvelope unsignedEnvelope = message.getSOAPEnvelope();
        Document doc = unsignedEnvelope.getAsDocument();
        MyHandler handler = new MyHandler();
        handler.send(
            WSConstants.SIGN, 
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
        
        reqData = new RequestData();
        reqData.setWssConfig(WSSConfig.getNewInstance());
        messageContext = new java.util.TreeMap();
        messageContext.put(WSHandlerConstants.PW_CALLBACK_REF, this);
        reqData.setMsgContext(messageContext);
        reqData.setUsername("");
        
        handler.receive(WSConstants.SIGN, reqData);
        
        verify(doc);
    }
    

    /**
     * Verifies the soap envelope
     * <p/>
     * 
     * @param doc 
     * @throws Exception Thrown when there is a problem in verification
     */
    private void verify(Document doc) throws Exception {
        secEngine.processSecurityHeader(doc, null, this, null, crypto);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Verfied and decrypted message:");
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(doc);
            LOG.debug(outputString);
        }
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
