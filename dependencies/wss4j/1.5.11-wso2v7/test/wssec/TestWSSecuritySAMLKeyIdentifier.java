/*
 * Copyright 2010 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package wssec;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.ws.security.saml.SAMLIssuerFactory;
import org.apache.ws.security.saml.SAMLIssuer;
import org.apache.ws.security.util.WSSecurityUtil;

import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.client.AxisClient;
import org.apache.axis.configuration.NullProvider;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSEncryptionPart;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;
import org.apache.ws.security.message.WSSecEncrypt;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.token.SecurityTokenReference;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.opensaml.SAMLAssertion;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * Test-case for checking KeyIdentifier (and not Reference) elements 
 * are used to identify SAML tokens
 * 
 */
public class TestWSSecuritySAMLKeyIdentifier extends TestCase implements CallbackHandler {
    private static final Log LOG = LogFactory.getLog(TestWSSecuritySAMLKeyIdentifier.class);
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
    private MessageContext msgContext;
    private Message message;

    /**
     * TestWSSecurity constructor
     * 
     * @param name name of the test
     */
    public TestWSSecuritySAMLKeyIdentifier(String name) {
        super(name);
    }

    /**
     * JUnit suite
     * 
     * @return a junit test suite
     */
    public static Test suite() {
        return new TestSuite(TestWSSecuritySAMLKeyIdentifier.class);
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
     * The body of the SOAP request is encrypted using a secret key, which is in turn encrypted
     * using the certificate embedded in the SAML assertion and referenced using a Key Identifier.
     */
    public void testSAMLEncryptedKey() throws Exception {
        SOAPEnvelope unsignedEnvelope = message.getSOAPEnvelope();
        
        // Create a SAML assertion
        SAMLIssuer saml = SAMLIssuerFactory.getInstance("saml4.properties");
        Document doc = unsignedEnvelope.getAsDocument();
        saml.setInstanceDoc(doc);
        Crypto hokCrypto = CryptoFactory.getInstance("crypto.properties");
        saml.setUserCrypto(hokCrypto);
        saml.setUsername("16c73ab6-b892-458f-abf5-2f875f74882e");
        SAMLAssertion assertion = saml.newAssertion();
        Node assertionNode = assertion.toDOM(doc);
        
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        secHeader.getSecurityHeader().appendChild(assertionNode);
        
        // Encrypt the SOAP body
        WSSecEncrypt builder = new WSSecEncrypt();
        builder.setUserInfo("16c73ab6-b892-458f-abf5-2f875f74882e");
        builder.setSymmetricEncAlgorithm(WSConstants.TRIPLE_DES);
        builder.setKeyIdentifierType(WSConstants.CUSTOM_KEY_IDENTIFIER);
        builder.setCustomEKTokenValueType(SecurityTokenReference.SAML_ID_URI);
        builder.setCustomEKTokenId(assertion.getId());
        
        builder.prepare(doc, hokCrypto);
        Vector parts = new Vector();
        WSEncryptionPart encP = new WSEncryptionPart("testMethod", "uri:LogTestService2", "Element");
        parts.add(encP);
        Element refElement = builder.encryptForInternalRef(null, parts);
        builder.addInternalRefElement(refElement);
        builder.appendToHeader(secHeader);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Signed SAML message (HOK):");
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(doc);
            LOG.debug(outputString);
        }
        
        Vector results = verify(doc, hokCrypto);
        WSSecurityEngineResult actionResult =
            WSSecurityUtil.fetchActionResult(results, WSConstants.ST_UNSIGNED);
        SAMLAssertion receivedAssertion = 
            (SAMLAssertion) actionResult.get(WSSecurityEngineResult.TAG_SAML_ASSERTION);
        assertTrue(receivedAssertion != null);
    }
    
    
    /**
     * Verifies the soap envelope
     * 
     * @param doc
     * @throws Exception Thrown when there is a problem in verification
     */
    private Vector verify(Document doc, Crypto verifyCrypto) throws Exception {
        Vector results = secEngine.processSecurityHeader(doc, null, this, verifyCrypto);
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
    
    
}
