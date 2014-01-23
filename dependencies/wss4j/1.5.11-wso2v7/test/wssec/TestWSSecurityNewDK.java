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


import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.client.AxisClient;
import org.apache.axis.configuration.NullProvider;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;
import org.apache.ws.security.message.WSSecDKEncrypt;
import org.apache.ws.security.message.WSSecDKSign;
import org.apache.ws.security.message.WSSecEncryptedKey;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.token.SecurityTokenReference;
import org.apache.ws.security.util.WSSecurityUtil;
import org.apache.xml.security.signature.XMLSignature;
import org.w3c.dom.Document;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.Vector;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestWSSecurityNewDK extends TestCase implements CallbackHandler {
    private static final Log LOG = LogFactory.getLog(TestWSSecurityNewDK.class);
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
    private Crypto crypto = CryptoFactory.getInstance("wss40.properties");
    private MessageContext msgContext;
    private Message message;

    /**
     * TestWSSecurity constructor
     * <p/>
     * 
     * @param name name of the test
     */
    public TestWSSecurityNewDK(String name) {
        super(name);
    }

    /**
     * JUnit suite
     * <p/>
     * 
     * @return a junit test suite
     */
    public static Test suite() {
        return new TestSuite(TestWSSecurityNewDK.class);
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
     * Test encryption using a DerivedKeyToken using TRIPLEDES
     * @throws Exception Thrown when there is any problem in signing or 
     * verification
     */
    public void testEncryptionDecryptionTRIPLEDES() throws Exception {
        SOAPEnvelope unsignedEnvelope = message.getSOAPEnvelope();
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);

        //EncryptedKey
        WSSecEncryptedKey encrKeyBuilder = new WSSecEncryptedKey();
        encrKeyBuilder.setUserInfo("wss40");
        encrKeyBuilder.setKeyIdentifierType(WSConstants.THUMBPRINT_IDENTIFIER);
        encrKeyBuilder.prepare(doc, crypto);

        //Key information from the EncryptedKey
        byte[] ek = encrKeyBuilder.getEphemeralKey();
        String tokenIdentifier = encrKeyBuilder.getId();  
        
        //Derived key encryption
        WSSecDKEncrypt encrBuilder = new WSSecDKEncrypt();
        encrBuilder.setSymmetricEncAlgorithm(WSConstants.AES_128);
        encrBuilder.setExternalKey(ek, tokenIdentifier);
        Document encryptedDoc = encrBuilder.build(doc, secHeader);
        
        encrKeyBuilder.prependToHeader(secHeader);
        encrKeyBuilder.prependBSTElementToHeader(secHeader);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Encrypted message: 3DES  + DerivedKeys");
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(encryptedDoc);
            LOG.debug(outputString);
        }
        verify(doc);
    }

    /**
     * Test encryption using a DerivedKeyToken using AES128 
     * @throws Exception Thrown when there is any problem in signing or verification
     */
     public void testEncryptionDecryptionAES128() throws Exception {
         SOAPEnvelope unsignedEnvelope = message.getSOAPEnvelope();
         Document doc = unsignedEnvelope.getAsDocument();
         WSSecHeader secHeader = new WSSecHeader();
         secHeader.insertSecurityHeader(doc);

         //EncryptedKey
         WSSecEncryptedKey encrKeyBuilder = new WSSecEncryptedKey();
         encrKeyBuilder.setUserInfo("wss40");
         encrKeyBuilder.setKeyIdentifierType(WSConstants.THUMBPRINT_IDENTIFIER);
         encrKeyBuilder.prepare(doc, crypto);

         //Key information from the EncryptedKey
         byte[] ek = encrKeyBuilder.getEphemeralKey();
         String tokenIdentifier = encrKeyBuilder.getId();  
         
         //Derived key encryption
         WSSecDKEncrypt encrBuilder = new WSSecDKEncrypt();
         encrBuilder.setSymmetricEncAlgorithm(WSConstants.AES_128);
         encrBuilder.setExternalKey(ek, tokenIdentifier);
         Document encryptedDoc = encrBuilder.build(doc, secHeader);
         
         encrKeyBuilder.prependToHeader(secHeader);
         encrKeyBuilder.prependBSTElementToHeader(secHeader);
         
         if (LOG.isDebugEnabled()) {
             LOG.debug("Encrypted message: 3DES  + DerivedKeys");
             String outputString = 
                 org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(encryptedDoc);
             LOG.debug(outputString);
         }
        verify(doc);
     }
     
     public void testSignature() throws Exception {
         SOAPEnvelope unsignedEnvelope = message.getSOAPEnvelope();
         Document doc = unsignedEnvelope.getAsDocument();
         WSSecHeader secHeader = new WSSecHeader();
         secHeader.insertSecurityHeader(doc);

         //EncryptedKey
         WSSecEncryptedKey encrKeyBuilder = new WSSecEncryptedKey();
         encrKeyBuilder.setUserInfo("wss40");
         encrKeyBuilder.setKeyIdentifierType(WSConstants.THUMBPRINT_IDENTIFIER);
         encrKeyBuilder.prepare(doc, crypto);

         //Key information from the EncryptedKey
         byte[] ek = encrKeyBuilder.getEphemeralKey();
         String tokenIdentifier = encrKeyBuilder.getId();         
         
         //Derived key encryption
         WSSecDKSign sigBuilder = new WSSecDKSign();
         sigBuilder.setExternalKey(ek, tokenIdentifier);
         sigBuilder.setSignatureAlgorithm(XMLSignature.ALGO_ID_MAC_HMAC_SHA1);
         /* Document signedDoc = */ sigBuilder.build(doc, secHeader);
         
         encrKeyBuilder.prependToHeader(secHeader);
         encrKeyBuilder.prependBSTElementToHeader(secHeader);
         
         if (LOG.isDebugEnabled()) {
             LOG.debug("Encrypted message: 3DES  + DerivedKeys");
             String outputString = 
                 org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(doc);
             LOG.debug(outputString);
         }
         
         Vector results = verify(doc);
         
         WSSecurityEngineResult actionResult = 
             WSSecurityUtil.fetchActionResult(results, WSConstants.SIGN);
         assertTrue(actionResult != null);
         assertFalse(actionResult.isEmpty());
         
         assertTrue(actionResult.get(WSSecurityEngineResult.TAG_DECRYPTED_KEY) != null);
     }
     
     
     /**
      * A test for WSS-211 - "WSS4J does not support ThumbprintSHA1 in DerivedKeyTokens".
      * Here we're signing the SOAP body, where the signature refers to a DerivedKeyToken
      * which uses a Thumbprint-SHA1 reference to the encoded certificate (which is in the
      * keystore)
      */
     public void testSignatureThumbprintSHA1() throws Exception {
         SOAPEnvelope unsignedEnvelope = message.getSOAPEnvelope();
         Document doc = unsignedEnvelope.getAsDocument();
         WSSecHeader secHeader = new WSSecHeader();
         secHeader.insertSecurityHeader(doc);

         SecurityTokenReference secToken = new SecurityTokenReference(doc);
         X509Certificate[] certs = crypto.getCertificates("wss40");
         secToken.setKeyIdentifierThumb(certs[0]);
         secToken.getElement();
         
         WSSecDKSign sigBuilder = new WSSecDKSign();
         java.security.Key key = crypto.getPrivateKey("wss40", "security");
         sigBuilder.setExternalKey(key.getEncoded(), secToken.getElement());
         sigBuilder.setSignatureAlgorithm(XMLSignature.ALGO_ID_MAC_HMAC_SHA1);
         sigBuilder.build(doc, secHeader);
         
         sigBuilder.appendSigToHeader(secHeader);
         
         if (LOG.isDebugEnabled()) {
             LOG.debug("Encrypted message: ThumbprintSHA1 + DerivedKeys");
             String outputString = 
                 org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(doc);
             LOG.debug(outputString);
         }
         
         Vector results = verify(doc);
         
         WSSecurityEngineResult actionResult = 
             WSSecurityUtil.fetchActionResult(results, WSConstants.SIGN);
         assertTrue(actionResult != null);
         assertFalse(actionResult.isEmpty());
         
         assertTrue(actionResult.get(WSSecurityEngineResult.TAG_DECRYPTED_KEY) != null);
     }
     
     
     /**
      * Here we're signing the SOAP body, where the signature refers to a DerivedKeyToken
      * which uses an SKI reference to the encoded certificate (which is in the
      * keystore)
      */
     public void testSignatureSKI() throws Exception {
         SOAPEnvelope unsignedEnvelope = message.getSOAPEnvelope();
         Document doc = unsignedEnvelope.getAsDocument();
         WSSecHeader secHeader = new WSSecHeader();
         secHeader.insertSecurityHeader(doc);

         SecurityTokenReference secToken = new SecurityTokenReference(doc);
         X509Certificate[] certs = crypto.getCertificates("wss40");
         secToken.setKeyIdentifierSKI(certs[0], crypto);
         secToken.getElement();
         
         WSSecDKSign sigBuilder = new WSSecDKSign();
         java.security.Key key = crypto.getPrivateKey("wss40", "security");
         sigBuilder.setExternalKey(key.getEncoded(), secToken.getElement());
         sigBuilder.setSignatureAlgorithm(XMLSignature.ALGO_ID_MAC_HMAC_SHA1);
         sigBuilder.build(doc, secHeader);
         
         sigBuilder.appendSigToHeader(secHeader);
         
         if (LOG.isDebugEnabled()) {
             LOG.debug("Encrypted message: SKI + DerivedKeys");
             String outputString = 
                 org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(doc);
             LOG.debug(outputString);
         }
         
         Vector results = verify(doc);
         
         WSSecurityEngineResult actionResult = 
             WSSecurityUtil.fetchActionResult(results, WSConstants.SIGN);
         assertTrue(actionResult != null);
         assertFalse(actionResult.isEmpty());
         
         assertTrue(actionResult.get(WSSecurityEngineResult.TAG_DECRYPTED_KEY) != null);
     }
     
     
     public void testSignatureEncrypt() throws Exception {
        SOAPEnvelope unsignedEnvelope = message.getSOAPEnvelope();
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);

        //EncryptedKey
        WSSecEncryptedKey encrKeyBuilder = new WSSecEncryptedKey();
        encrKeyBuilder.setUserInfo("wss40");
        encrKeyBuilder.setKeyIdentifierType(WSConstants.THUMBPRINT_IDENTIFIER);
        encrKeyBuilder.prepare(doc, crypto);

        //Key information from the EncryptedKey
        byte[] ek = encrKeyBuilder.getEphemeralKey();
        String tokenIdentifier = encrKeyBuilder.getId();

        //Derived key encryption
        WSSecDKSign sigBuilder = new WSSecDKSign();
        sigBuilder.setExternalKey(ek, tokenIdentifier);
        sigBuilder.setSignatureAlgorithm(XMLSignature.ALGO_ID_MAC_HMAC_SHA1);
        LOG.info("Before HMAC-SHA1 signature");
        Document signedDoc = sigBuilder.build(doc, secHeader);

        //Derived key signature
        WSSecDKEncrypt encrBuilder = new WSSecDKEncrypt();
        encrBuilder.setSymmetricEncAlgorithm(WSConstants.AES_128);
        encrBuilder.setExternalKey(ek, tokenIdentifier);
        Document signedEncryptedDoc = encrBuilder.build(signedDoc, secHeader);

        encrKeyBuilder.prependToHeader(secHeader);
        encrKeyBuilder.prependBSTElementToHeader(secHeader);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Encrypted message: 3DES  + DerivedKeys");
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(signedEncryptedDoc);
            LOG.debug(outputString);
        }
        verify(signedEncryptedDoc);
    }
     
     public void testEncryptSignature() throws Exception {
         SOAPEnvelope unsignedEnvelope = message.getSOAPEnvelope();
         Document doc = unsignedEnvelope.getAsDocument();
         WSSecHeader secHeader = new WSSecHeader();
         secHeader.insertSecurityHeader(doc);

         //EncryptedKey
         WSSecEncryptedKey encrKeyBuilder = new WSSecEncryptedKey();
         encrKeyBuilder.setUserInfo("wss40");
         encrKeyBuilder.setKeyIdentifierType(WSConstants.THUMBPRINT_IDENTIFIER);
         encrKeyBuilder.prepare(doc, crypto);
         
         //Key information from the EncryptedKey
         byte[] ek = encrKeyBuilder.getEphemeralKey();
         String tokenIdentifier = encrKeyBuilder.getId();
         
         //Derived key encryption
         WSSecDKEncrypt encrBuilder = new WSSecDKEncrypt();
         encrBuilder.setSymmetricEncAlgorithm(WSConstants.AES_128);
         encrBuilder.setExternalKey(ek, tokenIdentifier);
         encrBuilder.build(doc, secHeader);
         
         //Derived key signature
         WSSecDKSign sigBuilder = new WSSecDKSign();
         sigBuilder.setExternalKey(ek, tokenIdentifier);
         sigBuilder.setSignatureAlgorithm(XMLSignature.ALGO_ID_MAC_HMAC_SHA1);
         LOG.info("Before HMAC-SHA1 signature");
         Document encryptedSignedDoc = sigBuilder.build(doc, secHeader);
         
         encrKeyBuilder.prependToHeader(secHeader);
         encrKeyBuilder.prependBSTElementToHeader(secHeader);
         
         if (LOG.isDebugEnabled()) {
             LOG.debug("Encrypted message: 3DES  + DerivedKeys");
             String outputString = 
                 org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(encryptedSignedDoc);
             LOG.debug(outputString);
         }

         verify(encryptedSignedDoc);
     }
    
    /**
     * Verifies the soap envelope
     * <p/>
     * 
     * @param envelope 
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
                throw new UnsupportedCallbackException(callbacks[i],
                        "Unrecognized Callback");
            }
        }
    }
}
