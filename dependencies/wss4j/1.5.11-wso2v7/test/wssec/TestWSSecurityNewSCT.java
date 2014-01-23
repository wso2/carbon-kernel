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
import java.security.SecureRandom;
import java.util.Hashtable;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.xml.crypto.dsig.SignatureMethod;

import junit.framework.TestCase;

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
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;
import org.apache.ws.security.conversation.ConversationConstants;
import org.apache.ws.security.message.WSSecDKEncrypt;
import org.apache.ws.security.message.WSSecDKSign;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecSecurityContextToken;
import org.apache.ws.security.message.WSSecSignature;
import org.apache.xml.security.signature.XMLSignature;
import org.w3c.dom.Document;

/**
 * Testcase to test WSSecSecurityContextToken
 * 
 * @see org.apache.ws.security.message.WSSecSecurityContextToken
 * 
 * @author Ruchith Fernando (ruchith.fernando@gmail.com)
 */
public class TestWSSecurityNewSCT extends TestCase implements CallbackHandler {
    private static final Log LOG = LogFactory.getLog(TestWSSecurityNewSCT.class);
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
     * Table of secrets idexd by the sct identifiers
     */
    private Hashtable secrets = new Hashtable();

    /**
     * @param arg0
     */
    public TestWSSecurityNewSCT(String arg0) {
        super(arg0);
    }

    /**
     * Setup method <p/>
     * 
     * @throws Exception
     *             Thrown when there is a problem in setup
     */
    protected void setUp() throws Exception {
        AxisClient tmpEngine = new AxisClient(new NullProvider());
        msgContext = new MessageContext(tmpEngine);
        message = getSOAPMessage();
    }

    public void testBuild() {
        try {
            SOAPEnvelope unsignedEnvelope = message.getSOAPEnvelope();
            Document doc = unsignedEnvelope.getAsDocument();
            WSSecHeader secHeader = new WSSecHeader();
            secHeader.insertSecurityHeader(doc);

            WSSecSecurityContextToken sctBuilder = new WSSecSecurityContextToken();
            sctBuilder.prepare(doc, crypto);
            
            sctBuilder.prependSCTElementToHeader(doc, secHeader);

            String out = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(doc);

            assertTrue(
                "SecurityContextToken missing",
                out.indexOf(ConversationConstants.SECURITY_CONTEXT_TOKEN_LN) > 0
            );
            assertTrue(
                "wsc:Identifier missing", 
                out.indexOf(ConversationConstants.IDENTIFIER_LN) > 0
            );

            // System.out.println(out);

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /**
     * Test encryption using a derived key which is based on a secret associated
     * with a security context token
     */
    public void testSCTDKTEncrypt() {
        try {
            SOAPEnvelope unsignedEnvelope = message.getSOAPEnvelope();
            Document doc = unsignedEnvelope.getAsDocument();
            WSSecHeader secHeader = new WSSecHeader();
            secHeader.insertSecurityHeader(doc);

            WSSecSecurityContextToken sctBuilder = new WSSecSecurityContextToken();
            sctBuilder.prepare(doc, crypto);

            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            byte[] tempSecret = new byte[16];
            random.nextBytes(tempSecret);

            // Store the secret
            this.secrets.put(sctBuilder.getIdentifier(), tempSecret);

            String tokenId = sctBuilder.getSctId();

            // Derived key encryption
            WSSecDKEncrypt encrBuilder = new WSSecDKEncrypt();
            encrBuilder.setSymmetricEncAlgorithm(WSConstants.AES_128);
            encrBuilder.setExternalKey(tempSecret, tokenId);
            encrBuilder.build(doc, secHeader);

            sctBuilder.prependSCTElementToHeader(doc, secHeader);

            if (LOG.isDebugEnabled()) {
                String outputString = 
                    org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(doc);
                LOG.debug(outputString);
            }

            verify(doc);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testSCTKDKTSign() {
        try {
            SOAPEnvelope unsignedEnvelope = message.getSOAPEnvelope();
            Document doc = unsignedEnvelope.getAsDocument();
            WSSecHeader secHeader = new WSSecHeader();
            secHeader.insertSecurityHeader(doc);

            WSSecSecurityContextToken sctBuilder = new WSSecSecurityContextToken();
            sctBuilder.prepare(doc, crypto);

            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            byte[] tempSecret = new byte[16];
            random.nextBytes(tempSecret);

            // Store the secret
            this.secrets.put(sctBuilder.getIdentifier(), tempSecret);

            String tokenId = sctBuilder.getSctId();

            // Derived key signature
            WSSecDKSign sigBuilder = new WSSecDKSign();
            sigBuilder.setExternalKey(tempSecret, tokenId);
            sigBuilder.setSignatureAlgorithm(XMLSignature.ALGO_ID_MAC_HMAC_SHA1);
            sigBuilder.build(doc, secHeader);
            
            sctBuilder.prependSCTElementToHeader(doc, secHeader);

            if (LOG.isDebugEnabled()) {
                String outputString = 
                    org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(doc);
                LOG.debug(outputString);
            }

            verify(doc);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    /**
     * Test for WSS-217:
     * "Add ability to specify a reference to an absolute URI in the derived key functionality".
     */
    public void testSCTKDKTSignAbsolute() {
        try {
            SOAPEnvelope unsignedEnvelope = message.getSOAPEnvelope();
            Document doc = unsignedEnvelope.getAsDocument();
            WSSecHeader secHeader = new WSSecHeader();
            secHeader.insertSecurityHeader(doc);

            WSSecSecurityContextToken sctBuilder = new WSSecSecurityContextToken();
            sctBuilder.prepare(doc, crypto);

            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            byte[] tempSecret = new byte[16];
            random.nextBytes(tempSecret);

            // Store the secret
            this.secrets.put(sctBuilder.getIdentifier(), tempSecret);

            // Derived key signature
            WSSecDKSign sigBuilder = new WSSecDKSign();
            sigBuilder.setExternalKey(tempSecret, sctBuilder.getIdentifier());
            sigBuilder.setTokenIdDirectId(true);
            sigBuilder.setSignatureAlgorithm(XMLSignature.ALGO_ID_MAC_HMAC_SHA1);
            sigBuilder.build(doc, secHeader);
            
            sctBuilder.prependSCTElementToHeader(doc, secHeader);

            if (LOG.isDebugEnabled()) {
                LOG.debug("DKT Absolute");
                String outputString = 
                    org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(doc);
                LOG.debug(outputString);
            }

            verify(doc);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    
    public void testSCTKDKTSignEncrypt() {
        try {
            SOAPEnvelope unsignedEnvelope = message.getSOAPEnvelope();
            Document doc = unsignedEnvelope.getAsDocument();
            WSSecHeader secHeader = new WSSecHeader();
            secHeader.insertSecurityHeader(doc);

            WSSecSecurityContextToken sctBuilder = new WSSecSecurityContextToken();
            sctBuilder.prepare(doc, crypto);

            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            byte[] tempSecret = new byte[16];
            random.nextBytes(tempSecret);

            // Store the secret
            this.secrets.put(sctBuilder.getIdentifier(), tempSecret);

            String tokenId = sctBuilder.getSctId();

            // Derived key signature
            WSSecDKSign sigBuilder = new WSSecDKSign();
            sigBuilder.setExternalKey(tempSecret, tokenId);
            sigBuilder.setSignatureAlgorithm(XMLSignature.ALGO_ID_MAC_HMAC_SHA1);
            sigBuilder.build(doc, secHeader);

            // Derived key encryption
            WSSecDKEncrypt encrBuilder = new WSSecDKEncrypt();
            encrBuilder.setSymmetricEncAlgorithm(WSConstants.AES_128);
            encrBuilder.setExternalKey(tempSecret, tokenId);
            encrBuilder.build(doc, secHeader);

            sctBuilder.prependSCTElementToHeader(doc, secHeader);

            if (LOG.isDebugEnabled()) {
                String outputString = 
                    org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(doc);
                LOG.debug(outputString);
            }
            
            verify(doc);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testSCTKDKTEncryptSign() {
        try {
            SOAPEnvelope unsignedEnvelope = message.getSOAPEnvelope();
            Document doc = unsignedEnvelope.getAsDocument();
            WSSecHeader secHeader = new WSSecHeader();
            secHeader.insertSecurityHeader(doc);

            WSSecSecurityContextToken sctBuilder = new WSSecSecurityContextToken();
            sctBuilder.prepare(doc, crypto);

            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            byte[] tempSecret = new byte[16];
            random.nextBytes(tempSecret);

            // Store the secret
            this.secrets.put(sctBuilder.getIdentifier(), tempSecret);

            String tokenId = sctBuilder.getSctId();

            // Derived key encryption
            WSSecDKEncrypt encrBuilder = new WSSecDKEncrypt();
            encrBuilder.setSymmetricEncAlgorithm(WSConstants.AES_128);
            encrBuilder.setExternalKey(tempSecret, tokenId);
            encrBuilder.build(doc, secHeader);

            // Derived key signature
            WSSecDKSign sigBuilder = new WSSecDKSign();
            sigBuilder.setExternalKey(tempSecret, tokenId);
            sigBuilder.setSignatureAlgorithm(XMLSignature.ALGO_ID_MAC_HMAC_SHA1);
            sigBuilder.build(doc, secHeader);

            sctBuilder.prependSCTElementToHeader(doc, secHeader);

            if (LOG.isDebugEnabled()) {
                String outputString = 
                    org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(doc);
                LOG.debug(outputString);
            }

            verify(doc);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    
    /**
     * Test signature and verification using a SecurityContextToken directly,
     * rather than using a DerivedKeyToken to point to a SecurityContextToken.
     * See WSS-216 - https://issues.apache.org/jira/browse/WSS-216
     */
    public void testSCTSign() {
        try {
            SOAPEnvelope unsignedEnvelope = message.getSOAPEnvelope();
            Document doc = unsignedEnvelope.getAsDocument();
            WSSecHeader secHeader = new WSSecHeader();
            secHeader.insertSecurityHeader(doc);

            WSSecSecurityContextToken sctBuilder = new WSSecSecurityContextToken();
            sctBuilder.prepare(doc, crypto);

            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            byte[] tempSecret = new byte[16];
            random.nextBytes(tempSecret);

            // Store the secret
            this.secrets.put(sctBuilder.getIdentifier(), tempSecret);

            String tokenId = sctBuilder.getSctId();

            WSSecSignature builder = new WSSecSignature();
            builder.setSecretKey(tempSecret);
            builder.setKeyIdentifierType(WSConstants.CUSTOM_SYMM_SIGNING);
            builder.setCustomTokenValueType(WSConstants.WSC_SCT);
            builder.setCustomTokenId(tokenId);
            builder.setSignatureAlgorithm(SignatureMethod.HMAC_SHA1);
            builder.build(doc, crypto, secHeader);
            
            sctBuilder.prependSCTElementToHeader(doc, secHeader);
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("SCT sign");
                String outputString = 
                    org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(doc);
                LOG.debug(outputString);
            }

            verify(doc);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    /**
     * Verifies the soap envelope <p/>
     * 
     * @param envelope
     * @throws Exception
     *             Thrown when there is a problem in verification
     */
    private void verify(Document doc) throws Exception {
        secEngine.processSecurityHeader(doc, null, this, crypto);
        String outputString = 
            org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(doc);
        assertTrue(outputString.indexOf("LogTestService2") > 0 ? true
                : false);
    }

    /**
     * Constructs a soap envelope <p/>
     * 
     * @return soap envelope
     * @throws Exception
     *             if there is any problem constructing the soap envelope
     */
    protected Message getSOAPMessage() throws Exception {
        InputStream in = new ByteArrayInputStream(SOAPMSG.getBytes());
        Message msg = new Message(in);
        msg.setMessageContext(msgContext);
        return msg;
    }

    public void handle(Callback[] callbacks) throws IOException,
        UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof WSPasswordCallback) {
                WSPasswordCallback pc = (WSPasswordCallback) callbacks[i];
                byte[] secret = (byte[]) this.secrets.get(pc.getIdentifier());
                pc.setKey(secret);
            } else {
                throw new UnsupportedCallbackException(callbacks[i],
                        "Unrecognized Callback");
            }
        }
    }

}
