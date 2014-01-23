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
import org.apache.ws.security.message.WSSecSignature;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.saml.SAMLIssuer;
import org.apache.ws.security.saml.SAMLIssuerFactory;
import org.apache.ws.security.saml.WSSecSignatureSAML;
import org.apache.ws.security.util.WSSecurityUtil;
import org.apache.xml.security.algorithms.MessageDigestAlgorithm;
import org.apache.xml.security.signature.XMLSignature;
import org.opensaml.SAMLAssertion;
import org.w3c.dom.Document;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

/**
 * This is some unit tests for signing using signature parts. Note that the "soapMsg" below
 * has a custom header added.
 */
public class TestWSSecuritySignatureParts extends TestCase implements CallbackHandler {
    private static final Log LOG = LogFactory.getLog(TestWSSecuritySignatureParts.class);
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
    public TestWSSecuritySignatureParts(String name) {
        super(name);
    }

    /**
     * JUnit suite
     * <p/>
     * 
     * @return a junit test suite
     */
    public static Test suite() {
        return new TestSuite(TestWSSecuritySignatureParts.class);
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
     * Test signing a custom SOAP header
     */
    public void testSOAPHeader() throws Exception {
        SOAPEnvelope unsignedEnvelope = message.getSOAPEnvelope();
        WSSecSignature sign = new WSSecSignature();
        sign.setUserInfo("16c73ab6-b892-458f-abf5-2f875f74882e", "security");
        sign.setKeyIdentifierType(WSConstants.ISSUER_SERIAL);

        Document doc = unsignedEnvelope.getAsDocument();

        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        Vector parts = new Vector();
        WSEncryptionPart encP =
            new WSEncryptionPart(
                "foobar",
                "urn:foo.bar",
                "");
        parts.add(encP);
        sign.setParts(parts);
        
        Document signedDoc = sign.build(doc, crypto, secHeader);
        
        if (LOG.isDebugEnabled()) {
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(signedDoc);
            LOG.debug(outputString);
        }
        
        Vector results = verify(signedDoc);
        
        WSSecurityEngineResult actionResult = 
            WSSecurityUtil.fetchActionResult(results, WSConstants.SIGN);
        assertTrue(actionResult != null);
        assertFalse(actionResult.isEmpty());
        final java.util.List refs =
            (java.util.List) actionResult.get(WSSecurityEngineResult.TAG_DATA_REF_URIS);
        
        WSDataRef wsDataRef = (WSDataRef)refs.get(0);
        String xpath = wsDataRef.getXpath();
        assertEquals("/soapenv:Envelope/soapenv:Header/foo:foobar", xpath);
        assertEquals(XMLSignature.ALGO_ID_SIGNATURE_RSA, wsDataRef.getAlgorithm());
        
        assertEquals(MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA1, wsDataRef.getDigestAlgorithm());
        
        String sigMethod = (String)actionResult.get(WSSecurityEngineResult.TAG_SIGNATURE_METHOD);
        assertEquals(XMLSignature.ALGO_ID_SIGNATURE_RSA, sigMethod);
        
        String c14nMethod = 
            (String)actionResult.get(WSSecurityEngineResult.TAG_CANONICALIZATION_METHOD);
        assertEquals(WSConstants.C14N_EXCL_OMIT_COMMENTS, c14nMethod);
    }
    
    /**
     * Test signing of a header through a STR Dereference Transform
     */
    public void testSOAPHeaderSTRTransform() throws Exception {
        SOAPEnvelope unsignedEnvelope = message.getSOAPEnvelope();

        Document doc = unsignedEnvelope.getAsDocument();
        
        SAMLIssuer saml = SAMLIssuerFactory.getInstance("saml4.properties");
        // Provide info to SAML issuer that it can construct a Holder-of-key
        // SAML token.
        saml.setInstanceDoc(doc);
        saml.setUserCrypto(crypto);
        saml.setUsername("16c73ab6-b892-458f-abf5-2f875f74882e");
        SAMLAssertion assertion = saml.newAssertion();

        WSSecSignatureSAML wsSign = new WSSecSignatureSAML();
        wsSign.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);
        wsSign.setUserInfo("16c73ab6-b892-458f-abf5-2f875f74882e", "security");
        
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        Vector parts = new Vector();
        WSEncryptionPart encP =
            new WSEncryptionPart("STRTransform", "", "Element");
        parts.add(encP);
        wsSign.setParts(parts);

        LOG.info("Before SAMLSignedKeyHolder....");
        
        //
        // set up for keyHolder
        //
        Document signedDoc = wsSign.build(doc, crypto, assertion, null, null, null, secHeader);
        LOG.info("After SAMLSignedKeyHolder....");

        if (LOG.isDebugEnabled()) {
            LOG.debug("Signed SAML message (key holder):");
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(signedDoc);
            LOG.debug(outputString);
        }
        
        Vector results = verify(signedDoc);
        WSSecurityEngineResult stUnsignedActionResult =
            WSSecurityUtil.fetchActionResult(results, WSConstants.ST_UNSIGNED);
        SAMLAssertion receivedAssertion = 
            (SAMLAssertion) stUnsignedActionResult.get(WSSecurityEngineResult.TAG_SAML_ASSERTION);
        assertTrue(receivedAssertion != null);
        
        WSSecurityEngineResult signActionResult = 
            WSSecurityUtil.fetchActionResult(results, WSConstants.SIGN);
        assertTrue(signActionResult != null);
        assertFalse(signActionResult.isEmpty());
        final java.util.List refs =
            (java.util.List) signActionResult.get(WSSecurityEngineResult.TAG_DATA_REF_URIS);
        
        WSDataRef wsDataRef = (WSDataRef)refs.get(0);
        String xpath = wsDataRef.getXpath();
        assertEquals("/soapenv:Envelope/soapenv:Header/wsse:Security/Assertion", xpath);
    }
    
    /**
     * Test signing a custom SOAP header with a bad localname
     */
    public void testBadLocalname() throws Exception {
        SOAPEnvelope unsignedEnvelope = message.getSOAPEnvelope();
        WSSecSignature sign = new WSSecSignature();
        sign.setUserInfo("16c73ab6-b892-458f-abf5-2f875f74882e", "security");
        sign.setKeyIdentifierType(WSConstants.ISSUER_SERIAL);

        Document doc = unsignedEnvelope.getAsDocument();

        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        Vector parts = new Vector();
        WSEncryptionPart encP =
            new WSEncryptionPart(
                "foobar2",
                "urn:foo.bar",
                "");
        parts.add(encP);
        sign.setParts(parts);
        
        try {
            sign.build(doc, crypto, secHeader);
            fail("Failure expected on a bad localname");
        } catch (WSSecurityException ex) {
            // expected
        }
    }
    
    /**
     * Test signing a custom SOAP header with a bad namespace
     */
    public void testBadNamespace() throws Exception {
        SOAPEnvelope unsignedEnvelope = message.getSOAPEnvelope();
        WSSecSignature sign = new WSSecSignature();
        sign.setUserInfo("16c73ab6-b892-458f-abf5-2f875f74882e", "security");
        sign.setKeyIdentifierType(WSConstants.ISSUER_SERIAL);

        Document doc = unsignedEnvelope.getAsDocument();

        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        Vector parts = new Vector();
        WSEncryptionPart encP =
            new WSEncryptionPart(
                "foobar",
                "urn:foo.bar2",
                "");
        parts.add(encP);
        sign.setParts(parts);
        
        try {
            sign.build(doc, crypto, secHeader);
            fail("Failure expected on a bad namespace");
        } catch (WSSecurityException ex) {
            // expected
        }
    }
    
    /**
     * Test signing a custom SOAP header and the SOAP body
     */
    public void testSOAPHeaderAndBody() throws Exception {
        SOAPEnvelope unsignedEnvelope = message.getSOAPEnvelope();
        SOAPConstants soapConstants = 
            WSSecurityUtil.getSOAPConstants(unsignedEnvelope.getAsDOM());
        WSSecSignature sign = new WSSecSignature();
        sign.setUserInfo("16c73ab6-b892-458f-abf5-2f875f74882e", "security");
        sign.setKeyIdentifierType(WSConstants.ISSUER_SERIAL);

        Document doc = unsignedEnvelope.getAsDocument();

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
        sign.setParts(parts);
        
        Document signedDoc = sign.build(doc, crypto, secHeader);
        
        if (LOG.isDebugEnabled()) {
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(signedDoc);
            LOG.debug(outputString);
        }
        
        verify(signedDoc);
    }
    

    /**
     * Verifies the soap envelope
     * <p/>
     * 
     * @param doc 
     * @throws Exception Thrown when there is a problem in verification
     */
    private Vector verify(Document doc) throws Exception {
        Vector results = secEngine.processSecurityHeader(doc, null, this, crypto);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Verfied and decrypted message:");
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
