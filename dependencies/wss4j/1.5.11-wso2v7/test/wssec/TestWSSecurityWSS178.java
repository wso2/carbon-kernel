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
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;
import org.apache.ws.security.message.WSSecEncrypt;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.saml.SAMLIssuer;
import org.apache.ws.security.saml.SAMLIssuerFactory;
import org.apache.ws.security.saml.WSSecSignatureSAML;
import org.opensaml.SAMLAssertion;
import org.w3c.dom.Document;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * WS-Security Test Case for WSS-178 - "signature verification failure of signed saml token due to "
 * "The Reference for URI (bst-saml-uri) has no XMLSignatureInput".
 * 
 * The problem is that the signature is referring to a SecurityTokenReference via the STRTransform,
 * which in turn is referring to the SAML Assertion. The request is putting the SAML Assertion
 * below the SecurityTokenReference, and this is causing SecurityTokenReference.get*TokenElement
 * to fail.
 */
public class TestWSSecurityWSS178 extends TestCase implements CallbackHandler {
    private static final Log LOG = LogFactory.getLog(TestWSSecurityWSS178.class);
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
    private Crypto crypto;
    private MessageContext msgContext;
    private Message message;

    /**
     * TestWSSecurity constructor
     * 
     * @param name name of the test
     */
    public TestWSSecurityWSS178(String name) {
        super(name);
        secEngine.getWssConfig(); //make sure BC gets registered
        crypto = CryptoFactory.getInstance("crypto.properties");
    }

    /**
     * JUnit suite
     * 
     * @return a junit test suite
     */
    public static Test suite() {
        return new TestSuite(TestWSSecurityWSS178.class);
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
     * Test where the Assertion is referenced using a Key Identifier 
     * (from the SecurityTokenReference).
     */
    public void testKeyIdentifier() throws Exception {
        SOAPEnvelope unsignedEnvelope = message.getSOAPEnvelope();
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        SAMLIssuer saml = SAMLIssuerFactory.getInstance("saml.properties");
        SAMLAssertion assertion = saml.newAssertion();
        String issuerKeyName = saml.getIssuerKeyName();
        String issuerKeyPW = saml.getIssuerKeyPassword();
        Crypto issuerCrypto = saml.getIssuerCrypto();
        WSSecSignatureSAML wsSign = new WSSecSignatureSAML();
        wsSign.setKeyIdentifierType(WSConstants.X509_KEY_IDENTIFIER);
        Document samlDoc = 
            wsSign.build(doc, null, assertion, issuerCrypto, 
                issuerKeyName, issuerKeyPW, secHeader
            );
        
        WSSecEncrypt builder = new WSSecEncrypt();
        builder.setUserInfo("16c73ab6-b892-458f-abf5-2f875f74882e");
        builder.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);
        Document encryptedDoc = builder.build(samlDoc, crypto, secHeader);
        
        //
        // Remove the assertion its place in the security header and then append it
        //
        org.w3c.dom.Element secHeaderElement = secHeader.getSecurityHeader();
        org.w3c.dom.Node assertionNode = 
            secHeaderElement.getElementsByTagName("Assertion").item(0);
        secHeaderElement.removeChild(assertionNode);
        secHeaderElement.appendChild(assertionNode);

        String outputString = 
            org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(encryptedDoc);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Encrypted message:");
            LOG.debug(outputString);
        }
        
        verify(encryptedDoc);
    }
    
    
    /**
     * Test where the Assertion is referenced using direct reference
     * (from the SecurityTokenReference).
     */
    public void testDirectReference() throws Exception {
        SOAPEnvelope unsignedEnvelope = message.getSOAPEnvelope();
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        SAMLIssuer saml = SAMLIssuerFactory.getInstance("saml.properties");
        SAMLAssertion assertion = saml.newAssertion();
        String issuerKeyName = saml.getIssuerKeyName();
        String issuerKeyPW = saml.getIssuerKeyPassword();
        Crypto issuerCrypto = saml.getIssuerCrypto();
        WSSecSignatureSAML wsSign = new WSSecSignatureSAML();
        wsSign.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);
        Document samlDoc = 
            wsSign.build(doc, null, assertion, issuerCrypto, 
                issuerKeyName, issuerKeyPW, secHeader
            );
        
        WSSecEncrypt builder = new WSSecEncrypt();
        builder.setUserInfo("16c73ab6-b892-458f-abf5-2f875f74882e");
        builder.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);
        Document encryptedDoc = builder.build(samlDoc, crypto, secHeader);
        
        //
        // Remove the assertion its place in the security header and then append it
        //
        org.w3c.dom.Element secHeaderElement = secHeader.getSecurityHeader();
        org.w3c.dom.Node assertionNode = 
            secHeaderElement.getElementsByTagName("Assertion").item(0);
        secHeaderElement.removeChild(assertionNode);
        secHeaderElement.appendChild(assertionNode);

        String outputString = 
            org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(encryptedDoc);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Encrypted message:");
            LOG.debug(outputString);
        }
        
        verify(encryptedDoc);
    }

    
    /**
     * Verifies the soap envelope
     * <p/>
     * 
     * @param envelope 
     * @throws Exception Thrown when there is a problem in verification
     */
    private java.util.List verify(Document doc) throws Exception {
        return secEngine.processSecurityHeader(doc, null, this, crypto);
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
