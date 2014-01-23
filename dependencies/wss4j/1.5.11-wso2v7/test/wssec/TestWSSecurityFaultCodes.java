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
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;
import org.apache.ws.security.message.WSSecEncrypt;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecTimestamp;
import org.apache.ws.security.message.WSSecUsernameToken;
import org.apache.ws.security.message.token.Reference;
import org.apache.ws.security.message.token.UsernameToken;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Document;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.xml.namespace.QName;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * WS-Security Test Case for fault codes. The SOAP Message Security specification 1.1 defines
 * standard fault codes and fault strings for error propagation.
 */
public class TestWSSecurityFaultCodes extends TestCase implements CallbackHandler {
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
    public TestWSSecurityFaultCodes(String name) {
        super(name);
    }

    /**
     * JUnit suite
     * <p/>
     * 
     * @return a junit test suite
     */
    public static Test suite() {
        return new TestSuite(TestWSSecurityFaultCodes.class);
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
     * Test for the wsse:FailedCheck faultcode. This will fail due to a bad password in
     * the callback handler.
     */
    public void testFailedCheck() throws Exception {
        WSSecEncrypt builder = new WSSecEncrypt();
        builder.setUserInfo("16c73ab6-b892-458f-abf5-2f875f74882e", "security");
        builder.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);        
        Document encryptedDoc = builder.build(doc, crypto, secHeader);
        
        try {
            verify(encryptedDoc);
        } catch (WSSecurityException ex) {
            assertTrue(ex.getErrorCode() == 6);
            assertTrue(ex.getMessage().startsWith("The signature or decryption was invalid"));
            QName faultCode = new QName(WSConstants.WSSE_NS, "FailedCheck");
            assertTrue(ex.getFaultCode().equals(faultCode));
        }
    }
    
    /**
     * Test for the wsse:UnsupportedAlgorithm faultcode. This will fail due to the argument
     * passed to getCipherInstance.
     */
    public void testUnsupportedAlgorithm() throws Exception {
        try {
            WSSecurityUtil.getCipherInstance("Bad Algorithm");
        } catch (WSSecurityException ex) {
            assertTrue(ex.getErrorCode() == 2);
            assertTrue(ex.getMessage().startsWith(
                "An unsupported signature or encryption algorithm was used"));
            QName faultCode = new QName(WSConstants.WSSE_NS, "UnsupportedAlgorithm");
            assertTrue(ex.getFaultCode().equals(faultCode));
        }
    }
    
    
    /**
     * Test for the wsse:SecurityTokenUnavailable faultcode. This will fail due to the 
     * argument to loadCertificate.
     */
    public void testSecurityTokenUnavailable() throws Exception {
        try {
            crypto.loadCertificate(new java.io.ByteArrayInputStream(new byte[]{}));
        } catch (WSSecurityException ex) {
            assertTrue(ex.getErrorCode() == 7);
            assertTrue(ex.getMessage().startsWith(
                "Referenced security token could not be retrieved"));
            QName faultCode = new QName(WSConstants.WSSE_NS, "SecurityTokenUnavailable");
            assertTrue(ex.getFaultCode().equals(faultCode));
        }
    }
    
    /**
     * Test for the wsse:MessageExpired faultcode. This will fail due to the argument
     * passed to setTimeToLive.
     */
    public void testMessageExpired() throws Exception {
        WSSecTimestamp builder = new WSSecTimestamp();
        builder.setTimeToLive(-1);
        
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);        
        Document timestampedDoc = builder.build(doc, secHeader);
        
        try {
            verify(timestampedDoc);
        } catch (WSSecurityException ex) {
            assertTrue(ex.getErrorCode() == 8);
            assertTrue(ex.getMessage().startsWith(
                "The message has expired"));
            QName faultCode = new QName(WSConstants.WSSE_NS, "MessageExpired");
            assertTrue(ex.getFaultCode().equals(faultCode));
        }
    }
    
    /**
     * Test for the wsse:FailedAuthentication faultcode. This will fail due to a bad password in
     * the callback handler.
     */
    public void testFailedAuthentication() throws Exception {
        WSSecUsernameToken builder = new WSSecUsernameToken();
        builder.addCreated();
        builder.addNonce();
        builder.setUserInfo("16c73ab6-b892-458f-abf5-2f875f74882e", "security");
        
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);        
        Document timestampedDoc = builder.build(doc, secHeader);
        
        try {
            verify(timestampedDoc);
        } catch (WSSecurityException ex) {
            assertTrue(ex.getErrorCode() == 5);
            assertTrue(ex.getMessage().startsWith(
                "The security token could not be authenticated or authorized"));
            QName faultCode = new QName(WSConstants.WSSE_NS, "FailedAuthentication");
            assertTrue(ex.getFaultCode().equals(faultCode));
        }
    }
    
    /**
     * Test for the wsse:InvalidSecurityToken faultcode. This will fail due to the fact
     * that a null username is used.
     */
    public void testInvalidSecurityToken() throws Exception {
        WSSecUsernameToken builder = new WSSecUsernameToken();
        builder.addCreated();
        builder.addNonce();
        builder.setUserInfo(null, "security");
        
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);        
        builder.build(doc, secHeader);
        
        try {
            new UsernameToken(doc.getDocumentElement());
        } catch (WSSecurityException ex) {
            assertTrue(ex.getErrorCode() == 4);
            assertTrue(ex.getMessage().startsWith(
                "An invalid security token was provided"));
            QName faultCode = new QName(WSConstants.WSSE_NS, "InvalidSecurityToken");
            assertTrue(ex.getFaultCode().equals(faultCode));
        }
    }
    
    /**
     * Test for the wsse:InvalidSecurity faultcode. 
     */
    public void testInvalidSecurity() throws Exception {
        try {
            new Reference((org.w3c.dom.Element)null);
        } catch (WSSecurityException ex) {
            assertTrue(ex.getErrorCode() == 3);
            assertTrue(ex.getMessage().startsWith(
                "An error was discovered processing the <wsse:Security> header"));
            QName faultCode = new QName(WSConstants.WSSE_NS, "InvalidSecurity");
            assertTrue(ex.getFaultCode().equals(faultCode));
        }
    }
    
    
    /**
     * Verifies the soap envelope.
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
                //
                // Deliberately wrong password
                //
                pc.setPassword("securit");
            } else {
                throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
            }
        }
    }

}
