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
import org.apache.ws.security.WSDataRef;
import org.apache.ws.security.WSEncryptionPart;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;
import org.apache.ws.security.message.WSSecEncrypt;
import org.apache.ws.security.message.WSSecSignature;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Document;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Vector;

/**
 * WS-Security Test Case <p/>
 * 
 * @author Davanum Srinivas (dims@yahoo.com)
 */
public class TestWSSecurityNew6 extends TestCase implements CallbackHandler {
    private static final Log LOG = LogFactory.getLog(TestWSSecurityNew6.class);
    private static final String soapMsg = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
        + "<SOAP-ENV:Envelope "
        +   "xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" "
        +   "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "
        +   "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" 
        +   "<SOAP-ENV:Body>" 
        +       "<add xmlns=\"http://ws.apache.org/counter/counter_port_type\">" 
        +           "<value xmlns=\"http://blah.com\">15</value>" 
        +       "</add>" 
        +   "</SOAP-ENV:Body>" 
        + "</SOAP-ENV:Envelope>";

    private WSSecurityEngine secEngine = new WSSecurityEngine();
    private Crypto crypto = CryptoFactory.getInstance();
    private MessageContext msgContext;
    private Message message;

    /**
     * TestWSSecurity constructor <p/>
     * 
     * @param name
     *            name of the test
     */
    public TestWSSecurityNew6(String name) {
        super(name);
    }

    /**
     * JUnit suite <p/>
     * 
     * @return a junit test suite
     */
    public static Test suite() {
        return new TestSuite(TestWSSecurityNew6.class);
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

    /**
     * Constructs a soap envelope <p/>
     * 
     * @return soap envelope
     * @throws Exception
     *             if there is any problem constructing the soap envelope
     */
    protected Message getSOAPMessage() throws Exception {
        InputStream in = new ByteArrayInputStream(soapMsg.getBytes());
        Message msg = new Message(in);
        msg.setMessageContext(msgContext);
        return msg;
    }

    /**
     * Test that encrypts and signs a WS-Security envelope, then performs
     * verification and decryption <p/>
     * 
     * @throws Exception
     *             Thrown when there is any problem in signing, encryption,
     *             decryption, or verification
     */
    public void testEncryptionSigning() throws Exception {
        SOAPEnvelope unsignedEnvelope = message.getSOAPEnvelope();
        WSSecEncrypt encrypt = new WSSecEncrypt();
        WSSecSignature sign = new WSSecSignature();
        encrypt.setUserInfo("16c73ab6-b892-458f-abf5-2f875f74882e");
        sign.setUserInfo("16c73ab6-b892-458f-abf5-2f875f74882e", "security");
        LOG.info("Before Encryption....");
        Document doc = unsignedEnvelope.getAsDocument();

        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);

        Document encryptedDoc = encrypt.build(doc, crypto, secHeader);
        Document encryptedSignedDoc = sign.build(encryptedDoc, crypto,
                secHeader);
        LOG.info("After Encryption....");
        verify(encryptedSignedDoc);
    }
    
    /**
     * Test that encrypts and then signs a WS-Security envelope (including the 
     * encrypted element), then performs verification and decryption <p/>
     * 
     * @throws Exception
     *             Thrown when there is any problem in signing, encryption,
     *             decryption, or verification
     */
    public void testEncryptionElementSigning() throws Exception {
        WSSecEncrypt encrypt = new WSSecEncrypt();
        WSSecSignature sign = new WSSecSignature();
        encrypt.setUserInfo("16c73ab6-b892-458f-abf5-2f875f74882e");
        sign.setUserInfo("16c73ab6-b892-458f-abf5-2f875f74882e", "security");
        LOG.info("Before Encryption....");
        Document doc = message.getSOAPEnvelope().getAsDocument();

        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);

        Vector encParts = new Vector();
        encParts.add(
                new WSEncryptionPart(
                        "add",
                        "http://ws.apache.org/counter/counter_port_type",
                        "Element"));
        encrypt.setParts(encParts);
        
        Document encryptedDoc = encrypt.build(doc, crypto, secHeader);
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("After Encryption....");
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(encryptedDoc);
            LOG.debug(outputString);
        }
        
        Vector sigParts = new Vector();
        sigParts.add(
                new WSEncryptionPart(
                        WSConstants.ENC_DATA_LN,
                        WSConstants.ENC_NS,
                        "Element"));
        sign.setParts(sigParts);
        
        Document encryptedSignedDoc = sign.build(encryptedDoc, crypto, secHeader);
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("After Signing....");
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(encryptedSignedDoc);
            LOG.debug(outputString);
        }
        
        Vector results = verify(encryptedSignedDoc);
        
        Vector sigSecEngResults = new Vector();
        WSSecurityUtil.fetchAllActionResults(results,
                WSConstants.SIGN, sigSecEngResults);
        
        Vector encSecEngResults = new Vector();
        WSSecurityUtil.fetchAllActionResults(results,
                WSConstants.ENCR, encSecEngResults);
        
        assertEquals(1, sigSecEngResults.size());
        assertEquals(1, encSecEngResults.size());
        
        List sigDataRefs = (List) ((WSSecurityEngineResult) sigSecEngResults.get(0))
                .get(WSSecurityEngineResult.TAG_DATA_REF_URIS);
        
        List encDataRefs = (List) ((WSSecurityEngineResult) encSecEngResults.get(0))
                .get(WSSecurityEngineResult.TAG_DATA_REF_URIS);
        
        assertNotNull(sigDataRefs);
        assertNotNull(encDataRefs);
        assertEquals(1, sigDataRefs.size());
        assertEquals(1, encDataRefs.size());
        
        assertNull(((WSDataRef) sigDataRefs.get(0))
                .getProtectedElement().getAttributeNodeNS(WSConstants.WSU_NS, "Id"));
        
        assertTrue(((WSDataRef) sigDataRefs.get(0)).getWsuId().indexOf(
                ((WSDataRef) encDataRefs.get(0)).getWsuId()) != -1);
    }
    
    /**
     * Test that signs and then encrypts a WS-Security envelope, then performs
     * decryption and verification <p/>
     * 
     * @throws Exception
     *             Thrown when there is any problem in signing, encryption,
     *             decryption, or verification
     */
    public void testSigningEncryption() throws Exception {
        SOAPEnvelope unsignedEnvelope = message.getSOAPEnvelope();
        WSSecEncrypt encrypt = new WSSecEncrypt();
        WSSecSignature sign = new WSSecSignature();
        encrypt.setUserInfo("16c73ab6-b892-458f-abf5-2f875f74882e");
        sign.setUserInfo("16c73ab6-b892-458f-abf5-2f875f74882e", "security");
        LOG.info("Before Encryption....");
        Document doc = unsignedEnvelope.getAsDocument();

        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);

        Document signedDoc = sign.build(doc, crypto, secHeader);
        Document encryptedSignedDoc = encrypt.build(signedDoc, crypto, secHeader);
        LOG.info("After Encryption....");
        verify(encryptedSignedDoc);
    }
    
    
    /**
     * Test that signs a SOAP Body, and then encrypts some data inside the SOAP Body.
     * As the encryption adds a wsu:Id to the encrypted element, this test checks that
     * verification still works ok.
     */
    public void testWSS198() throws Exception {
        SOAPEnvelope unsignedEnvelope = message.getSOAPEnvelope();
        WSSecEncrypt encrypt = new WSSecEncrypt();
        WSSecSignature sign = new WSSecSignature();
        encrypt.setUserInfo("16c73ab6-b892-458f-abf5-2f875f74882e");
        sign.setUserInfo("16c73ab6-b892-458f-abf5-2f875f74882e", "security");
        LOG.info("Before Encryption....");
        Document doc = unsignedEnvelope.getAsDocument();
        
        Vector parts = new Vector();
        WSEncryptionPart encP =
            new WSEncryptionPart(
                "add",
                "http://ws.apache.org/counter/counter_port_type",
                "");
        parts.add(encP);
        encrypt.setParts(parts);

        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);

        Document signedDoc = sign.build(doc, crypto, secHeader);
        Document encryptedSignedDoc = encrypt.build(signedDoc, crypto, secHeader);
        LOG.info("WSS198");
        if (LOG.isDebugEnabled()) {
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(encryptedSignedDoc);
            LOG.debug(outputString);
        }
        verify(encryptedSignedDoc);
    }

    /**
     * Verifies the soap envelope <p/>
     * 
     * @param envelope
     *
     * @return the <code>WSSecurityEngineResult</code>s from processing
     *
     * @throws Exception
     *             Thrown when there is a problem in verification
     */
    private Vector verify(Document doc) throws Exception {
        Vector results = secEngine.processSecurityHeader(doc, null, this, crypto);
        if (LOG.isDebugEnabled()) {
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(doc);
            LOG.debug(outputString);
        }
        
        return results;
    }

    public void handle(Callback[] callbacks) throws IOException,
        UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof WSPasswordCallback) {
                WSPasswordCallback pc = (WSPasswordCallback) callbacks[i];
                /*
                 * here call a function/method to lookup the password for the
                 * given identifier (e.g. a user name or keystore alias) e.g.:
                 * pc.setPassword(passStore.getPassword(pc.getIdentfifier)) for
                 * Testing we supply a fixed name here.
                 */
                pc.setPassword("security");
            } else {
                throw new UnsupportedCallbackException(callbacks[i],
                        "Unrecognized Callback");
            }
        }
    }
}
