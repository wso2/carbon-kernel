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
import org.apache.ws.security.SOAPConstants;
import org.apache.ws.security.WSDataRef;
import org.apache.ws.security.WSEncryptionPart;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;
import org.apache.ws.security.message.WSSecEncrypt;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Document;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

/**
 * This is some unit tests for encryption using encryption using parts. Note that the "soapMsg" below
 * has a custom header added.
 */
public class TestWSSecurityEncryptionParts extends TestCase implements CallbackHandler {
    private static final Log LOG = LogFactory.getLog(TestWSSecurityEncryptionParts.class);
    private static final String SOAPMSG = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<soapenv:Envelope xmlns:foo=\"urn:foo.bar\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
            "   <soapenv:Header>" +
            "       <foo:foobar>baz</foo:foobar>" + 
            "   </soapenv:Header>" +
            "   <soapenv:Body>" +
            "      <ns1:testMethod xmlns:ns1=\"http://axis/service/security/test6/LogTestService8\"></ns1:testMethod>" +
            "   </soapenv:Body>" +
            "</soapenv:Envelope>";

    private WSSecurityEngine secEngine = new WSSecurityEngine();
    private Crypto crypto = CryptoFactory.getInstance();
    private MessageContext msgContext;
    private Message message;

    /**
     * TestWSSecurity constructor
     * <p/>
     * 
     * @param name name of the test
     */
    public TestWSSecurityEncryptionParts(String name) {
        super(name);
    }

    /**
     * JUnit suite
     * <p/>
     * 
     * @return a junit test suite
     */
    public static Test suite() {
        return new TestSuite(TestWSSecurityEncryptionParts.class);
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
     * Test encrypting a custom SOAP header
     */
    public void testSOAPHeader() throws Exception {
        SOAPEnvelope unencryptedEnvelope = message.getSOAPEnvelope();
        WSSecEncrypt encrypt = new WSSecEncrypt();
        encrypt.setUserInfo("16c73ab6-b892-458f-abf5-2f875f74882e", "security");
        encrypt.setKeyIdentifierType(WSConstants.ISSUER_SERIAL);

        Document doc = unencryptedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        Vector parts = new Vector();
        WSEncryptionPart encP =
            new WSEncryptionPart(
                "foobar",
                "urn:foo.bar",
                "");
        parts.add(encP);
        encrypt.setParts(parts);
        
        Document encryptedDoc = encrypt.build(doc, crypto, secHeader);
        
        if (LOG.isDebugEnabled()) {
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(encryptedDoc);
            LOG.debug(outputString);
        }
        
        Vector results = verify(encryptedDoc);
        
        WSSecurityEngineResult actionResult = 
            WSSecurityUtil.fetchActionResult(results, WSConstants.ENCR);
        assertTrue(actionResult != null);
        assertFalse(actionResult.isEmpty());
        
        assertEquals(WSConstants.KEYTRANSPORT_RSA15, 
                actionResult.get(WSSecurityEngineResult.TAG_ENCRYPTED_KEY_TRANSPORT_METHOD));
        
        final java.util.List refs =
            (java.util.List) actionResult.get(WSSecurityEngineResult.TAG_DATA_REF_URIS);
        WSDataRef wsDataRef = (WSDataRef)refs.get(0);
        String xpath = wsDataRef.getXpath();
        assertEquals("/soapenv:Envelope/soapenv:Header/foo:foobar", xpath);
        assertEquals(WSConstants.AES_128, wsDataRef.getAlgorithm());
    }
    
    
    /**
     * Test encrypting a custom SOAP header using wsse11:EncryptedHeader
     */
    public void testSOAPEncryptedHeader() throws Exception {
        SOAPEnvelope unencryptedEnvelope = message.getSOAPEnvelope();
        WSSecEncrypt encrypt = new WSSecEncrypt();
        encrypt.setUserInfo("16c73ab6-b892-458f-abf5-2f875f74882e", "security");
        encrypt.setKeyIdentifierType(WSConstants.ISSUER_SERIAL);

        Document doc = unencryptedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        Vector parts = new Vector();
        WSEncryptionPart encP =
            new WSEncryptionPart(
                "foobar",
                "urn:foo.bar",
                "Header");
        parts.add(encP);
        encrypt.setParts(parts);
        
        Document encryptedDoc = encrypt.build(doc, crypto, secHeader);
        
        String outputString = 
            org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(encryptedDoc);
        if (LOG.isDebugEnabled()) {
            LOG.debug(outputString);
        }
        assertTrue(outputString.indexOf("wsse11:EncryptedHeader") != -1);
        assertTrue(outputString.indexOf("foo:foobar") == -1);
        
        Vector results = verify(encryptedDoc);
        
        WSSecurityEngineResult actionResult =
                WSSecurityUtil.fetchActionResult(results, WSConstants.ENCR);
        assertTrue(actionResult != null);
        assertFalse(actionResult.isEmpty());
        
        assertEquals(WSConstants.KEYTRANSPORT_RSA15, 
                actionResult.get(WSSecurityEngineResult.TAG_ENCRYPTED_KEY_TRANSPORT_METHOD));
        
        final java.util.List refs =
            (java.util.List) actionResult.get(WSSecurityEngineResult.TAG_DATA_REF_URIS);
        
        WSDataRef wsDataRef = (WSDataRef)refs.get(0);
        String xpath = wsDataRef.getXpath();
        assertEquals("/soapenv:Envelope/soapenv:Header/foo:foobar", xpath);
    }
    
    /**
     * Test encrypting a custom SOAP header with a bad localname
     */
    public void testBadLocalname() throws Exception {
        SOAPEnvelope unencryptedEnvelope = message.getSOAPEnvelope();
        WSSecEncrypt encrypt = new WSSecEncrypt();
        encrypt.setUserInfo("16c73ab6-b892-458f-abf5-2f875f74882e", "security");
        encrypt.setKeyIdentifierType(WSConstants.ISSUER_SERIAL);

        Document doc = unencryptedEnvelope.getAsDocument();

        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        Vector parts = new Vector();
        WSEncryptionPart encP =
            new WSEncryptionPart(
                "foobar2",
                "urn:foo.bar",
                "");
        parts.add(encP);
        encrypt.setParts(parts);
        
        try {
            encrypt.build(doc, crypto, secHeader);
            fail("Failure expected on a bad localname");
        } catch (WSSecurityException ex) {
            // expected
        }
    }
    
    
    /**
     * Test encrypting a custom SOAP header with a bad namespace
     */
    public void testBadNamespace() throws Exception {
        SOAPEnvelope unencryptedEnvelope = message.getSOAPEnvelope();
        WSSecEncrypt encrypt = new WSSecEncrypt();
        encrypt.setUserInfo("16c73ab6-b892-458f-abf5-2f875f74882e", "security");
        encrypt.setKeyIdentifierType(WSConstants.ISSUER_SERIAL);

        Document doc = unencryptedEnvelope.getAsDocument();

        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        Vector parts = new Vector();
        WSEncryptionPart encP =
            new WSEncryptionPart(
                "foobar",
                "urn:foo.bar2",
                "");
        parts.add(encP);
        encrypt.setParts(parts);
        
        try {
            encrypt.build(doc, crypto, secHeader);
            fail("Failure expected on a bad namespace");
        } catch (WSSecurityException ex) {
            // expected
        }
    }
    
    
    /**
     * Test encrypting a custom SOAP header and the SOAP body
     */
    public void testSOAPHeaderAndBody() throws Exception {
        SOAPEnvelope unencryptedEnvelope = message.getSOAPEnvelope();
        SOAPConstants soapConstants = 
            WSSecurityUtil.getSOAPConstants(unencryptedEnvelope.getAsDOM());
        WSSecEncrypt encrypt = new WSSecEncrypt();
        encrypt.setUserInfo("16c73ab6-b892-458f-abf5-2f875f74882e", "security");
        encrypt.setKeyIdentifierType(WSConstants.ISSUER_SERIAL);

        Document doc = unencryptedEnvelope.getAsDocument();

        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        Vector parts = new Vector();
        WSEncryptionPart encP =
            new WSEncryptionPart(
                soapConstants.getBodyQName().getLocalPart(),    // define the body
                soapConstants.getEnvelopeURI(),
                "");
        parts.add(encP);
        WSEncryptionPart encP2 =
            new WSEncryptionPart(
                "foobar",
                "urn:foo.bar",
                "");
        parts.add(encP2);
        encrypt.setParts(parts);
        
        Document encryptedDoc = encrypt.build(doc, crypto, secHeader);
        
        if (LOG.isDebugEnabled()) {
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(encryptedDoc);
            LOG.debug(outputString);
        }
        
        verify(encryptedDoc);
    }
    

    /**
     * Verifies the soap envelope
     * <p/>
     * 
     * @param doc 
     * @throws Exception Thrown when there is a problem in verification
     */
    private Vector verify(Document doc) throws Exception {
        Vector results = secEngine.processSecurityHeader(doc, null, this, null, crypto);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Verified and decrypted message:");
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(doc);
            LOG.debug(outputString);
        }
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
}
