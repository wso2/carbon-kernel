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
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSConfig;
import org.apache.ws.security.handler.RequestData;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.message.WSSecUsernameToken;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.token.UsernameToken;
import org.apache.ws.security.util.Base64;
import org.w3c.dom.Document;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Arrays;


/**
 * WS-Security Test Case for UsernameTokens.
 * 
 * @author Davanum Srinivas (dims@yahoo.com)
 */
public class TestWSSecurityNew5 extends TestCase implements CallbackHandler {
    private static final Log LOG = LogFactory.getLog(TestWSSecurityNew5.class);
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

    private static final String SOAPUTMSG = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
        + "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" "
        + "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "
        + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
        + "<SOAP-ENV:Header>"
        + "<wsse:Security SOAP-ENV:mustUnderstand=\"1\" "
        + "xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">"
        + "<wsse:UsernameToken wsu:Id=\"UsernameToken-29477163\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">"
        + "<wsse:Username>wernerd</wsse:Username>"
        + "<wsse:Password>verySecret</wsse:Password>"
        + "</wsse:UsernameToken></wsse:Security></SOAP-ENV:Header>"
        + "<SOAP-ENV:Body>" 
        + "<add xmlns=\"http://ws.apache.org/counter/counter_port_type\">" 
        + "<value xmlns=\"\">15</value>" + "</add>" 
        + "</SOAP-ENV:Body>\r\n       \r\n" + "</SOAP-ENV:Envelope>";
    private static final String EMPTY_PASSWORD_MSG =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
        + "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" "
        + "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "
        + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
        + "<SOAP-ENV:Header>"
        + "<wsse:Security SOAP-ENV:mustUnderstand=\"1\" "
        + "xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">"
        + "<wsse:UsernameToken wsu:Id=\"UsernameToken-1\" "
        + "xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" "
        + "xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">"
        + "<wsse:Username>wernerd</wsse:Username>"
        + "<wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText\"/>"
        + "</wsse:UsernameToken></wsse:Security></SOAP-ENV:Header>"
        + "<SOAP-ENV:Body>" 
        + "<add xmlns=\"http://ws.apache.org/counter/counter_port_type\">" 
        + "<value xmlns=\"\">15</value>" + "</add>" 
        + "</SOAP-ENV:Body>\r\n       \r\n" + "</SOAP-ENV:Envelope>";
    
    private WSSecurityEngine secEngine = new WSSecurityEngine();
    private MessageContext msgContext;
    private SOAPEnvelope unsignedEnvelope;

    /**
     * TestWSSecurity constructor
     * 
     * @param name name of the test
     */
    public TestWSSecurityNew5(String name) {
        super(name);
    }

    /**
     * JUnit suite
     * 
     * @return a junit test suite
     */
    public static Test suite() {
        return new TestSuite(TestWSSecurityNew5.class);
    }

    /**
     * Setup method
     * 
     * @throws java.lang.Exception Thrown when there is a problem in setup
     */
    protected void setUp() throws Exception {
        AxisClient tmpEngine = new AxisClient(new NullProvider());
        msgContext = new MessageContext(tmpEngine);
        unsignedEnvelope = getSOAPEnvelope();
    }

    /**
     * Constructs a soap envelope
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
     * Test that adds a UserNameToken with password Digest to a WS-Security envelope
     */
    public void testUsernameTokenDigest() throws Exception {
        WSSecUsernameToken builder = new WSSecUsernameToken();
        builder.setUserInfo("wernerd", "verySecret");
        LOG.info("Before adding UsernameToken PW Digest....");
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        Document signedDoc = builder.build(doc, secHeader);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Message with UserNameToken PW Digest:");
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(signedDoc);
            LOG.debug(outputString);
        }
        LOG.info("After adding UsernameToken PW Digest....");
        verify(signedDoc);
    }
    
    /**
     * Test for encoded passwords.
     */
    public void testUsernameTokenWithEncodedPasswordBaseline() throws Exception {
        String password = "password";
        // The SHA-1 of the password is known as a password equivalent in the UsernameToken specification.
        byte[] passwordHash = MessageDigest.getInstance("SHA-1").digest(password.getBytes("UTF-8"));

        String nonce = "0x7bXAPZVn40AdCD0Xbt0g==";
        String created = "2010-06-28T15:16:37Z";
        String expectedPasswordDigest = "C0rena/6gKpRZ9ATj+e6ss5sAbQ=";
        String actualPasswordDigest = UsernameToken.doPasswordDigest(nonce, created, passwordHash);
        assertEquals("the password digest is not as expected", expectedPasswordDigest, actualPasswordDigest);
    }
    
    /**
     * Test that adds a UserNameToken with password Digest to a WS-Security envelope
     */
    public void testUsernameTokenWithEncodedPassword() throws Exception {
        WSSecUsernameToken builder = new WSSecUsernameToken();
        builder.setPasswordsAreEncoded(true);
        builder.setUserInfo("wernerd", Base64.encode(MessageDigest.getInstance("SHA-1").digest("verySecret".getBytes("UTF-8"))));
        LOG.info("Before adding UsernameToken PW Digest....");
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        Document signedDoc = builder.build(doc, secHeader);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Message with UserNameToken PW Digest:");
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(signedDoc);
            LOG.debug(outputString);
        }
        LOG.info("After adding UsernameToken PW Digest....");

        boolean passwordsAreEnabledOrig = WSSecurityEngine.getInstance().getWssConfig().getPasswordsAreEncoded();
        try {
            WSSecurityEngine.getInstance().getWssConfig().setPasswordsAreEncoded(true);
            verify(signedDoc);
        } finally {
            WSSecurityEngine.getInstance().getWssConfig().setPasswordsAreEncoded(passwordsAreEnabledOrig);
        }
    }
    
    /**
     * Test that a bad username with password digest does not leak whether the username
     * is valid or not - see WSS-141.
     */
    public void testUsernameTokenBadUsername() throws Exception {
        WSSecUsernameToken builder = new WSSecUsernameToken();
        builder.setUserInfo("badusername", "verySecret");
        LOG.info("Before adding UsernameToken PW Digest....");
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        Document signedDoc = builder.build(doc, secHeader);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Message with UserNameToken PW Digest:");
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(signedDoc);
            LOG.debug(outputString);
        }
        LOG.info("After adding UsernameToken PW Digest....");
        try {
            verify(signedDoc);
            throw new Exception("Failure expected on a bad username");
        } catch (WSSecurityException ex) {
            String message = ex.getMessage();
            assertTrue(message.indexOf("badusername") == -1);
            assertTrue(ex.getErrorCode() == WSSecurityException.FAILED_AUTHENTICATION);
            // expected
        }
    }
    
    /**
     * Test that adds a UserNameToken with a bad password Digest to a WS-Security envelope
     */
    public void testUsernameTokenBadDigest() throws Exception {
        WSSecUsernameToken builder = new WSSecUsernameToken();
        builder.setUserInfo("wernerd", "verySecre");
        LOG.info("Before adding UsernameToken PW Digest....");
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        Document signedDoc = builder.build(doc, secHeader);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Message with UserNameToken PW Digest:");
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(signedDoc);
            LOG.debug(outputString);
        }
        LOG.info("After adding UsernameToken PW Digest....");
        try {
            verify(signedDoc);
            throw new Exception("Failure expected on a bad password digest");
        } catch (WSSecurityException ex) {
            assertTrue(ex.getErrorCode() == WSSecurityException.FAILED_AUTHENTICATION);
            // expected
        }
    }

    /**
     * Test that adds a UserNameToken with password text to a WS-Security envelope
     */
    public void testUsernameTokenText() throws Exception {
        WSSecUsernameToken builder = new WSSecUsernameToken();
        builder.setPasswordType(WSConstants.PASSWORD_TEXT);
        builder.setUserInfo("wernerd", "verySecret");
        LOG.info("Before adding UsernameToken PW Text....");
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        Document signedDoc = builder.build(doc, secHeader);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Message with UserNameToken PW Text:");
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(signedDoc);
            LOG.debug(outputString);
        }
        LOG.info("After adding UsernameToken PW Text....");
        verify(signedDoc);
    }
    
    /**
     * Test that adds a UserNameToken with a digested password but with type of
     * password test.
     */
    public void testUsernameTokenDigestText() throws Exception {
        WSSecUsernameToken builder = new WSSecUsernameToken();
        builder.setPasswordType(WSConstants.PASSWORD_TEXT);
        byte[] password = "verySecret".getBytes();
        MessageDigest sha = MessageDigest.getInstance("MD5");
        sha.reset();
        sha.update(password);
        String passwdDigest = Base64.encode(sha.digest());
        
        builder.setUserInfo("wernerd", passwdDigest);
        LOG.info("Before adding UsernameToken PW Text....");
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        Document signedDoc = builder.build(doc, secHeader);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Message with UserNameToken PW Text:");
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(signedDoc);
            LOG.debug(outputString);
        }
    }
    
    /**
     * Test that adds a UserNameToken with (bad) password text to a WS-Security envelope
     */
    public void testUsernameTokenBadText() throws Exception {
        WSSecUsernameToken builder = new WSSecUsernameToken();
        builder.setPasswordType(WSConstants.PASSWORD_TEXT);
        builder.setUserInfo("wernerd", "verySecre");
        LOG.info("Before adding UsernameToken PW Text....");
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        Document signedDoc = builder.build(doc, secHeader);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Message with UserNameToken PW Text:");
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(signedDoc);
            LOG.debug(outputString);
        }
        LOG.info("After adding UsernameToken PW Text....");
        
        try {
            verify(signedDoc);
            throw new Exception("Failure expected on a bad password text");
        } catch (WSSecurityException ex) {
            assertTrue(ex.getErrorCode() == WSSecurityException.FAILED_AUTHENTICATION);
            // expected
        }
    }
    
    /**
     * Test that adds a UserNameToken with no password type to a WS-Security envelope
     * See WSS-152 - https://issues.apache.org/jira/browse/WSS-152
     * "Problem with processing Username Tokens with no password type"
     * The 1.1 spec states that the password type is optional and defaults to password text, 
     * and so we should handle an incoming Username Token accordingly.
     */
    public void testUsernameTokenNoPasswordType() throws Exception {
        InputStream in = new ByteArrayInputStream(SOAPUTMSG.getBytes());
        Message msg = new Message(in);
        msg.setMessageContext(msgContext);
        SOAPEnvelope utEnvelope = msg.getSOAPEnvelope();
        Document doc = utEnvelope.getAsDocument();
        if (LOG.isDebugEnabled()) {
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(doc);
            LOG.debug(outputString);
        }
        verify(doc);
    }
    
    /**
     * Test that adds a UserNameToken with no password
     */
    public void testUsernameTokenNoPassword() throws Exception {
        WSSecUsernameToken builder = new WSSecUsernameToken();
        builder.setPasswordType(null);
        builder.setUserInfo("wernerd", null);
        LOG.info("Before adding UsernameToken with no password....");
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        Document signedDoc = builder.build(doc, secHeader);
        if (LOG.isDebugEnabled()) {
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(signedDoc);
            LOG.debug(outputString);
        }
        try {
            verify(signedDoc);
            throw new Exception("Failure expected on no password");
        } catch (WSSecurityException ex) {
            assertTrue(ex.getErrorCode() == WSSecurityException.FAILED_AUTHENTICATION);
            // expected
        }
    }
    
    /**
     * Test that adds a UserNameToken with an empty password
     */
    public void testUsernameTokenEmptyPassword() throws Exception {
        WSSecUsernameToken builder = new WSSecUsernameToken();
        builder.setPasswordType(WSConstants.PASSWORD_TEXT);
        builder.setUserInfo("wernerd", "");
        LOG.info("Before adding UsernameToken with an empty password....");
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        Document signedDoc = builder.build(doc, secHeader);
        if (LOG.isDebugEnabled()) {
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(signedDoc);
            LOG.debug(outputString);
        }
        verify(signedDoc);
    }
    
    /**
     * Test that processes a UserNameToken with an empty password
     */
    public void testEmptyPasswordProcessing() throws Exception {
        InputStream in = new ByteArrayInputStream(EMPTY_PASSWORD_MSG.getBytes());
        Message msg = new Message(in);
        msg.setMessageContext(msgContext);
        SOAPEnvelope utEnvelope = msg.getSOAPEnvelope();
        Document doc = utEnvelope.getAsDocument();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Empty password message: ");
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(doc);
            LOG.debug(outputString);
        }
        
        verify(doc);
    }
    
    /**
     * Test with a null token type. This will fail as the default is to reject custom
     * token types.
     */
    public void testUsernameTokenCustomFail() throws Exception {
        WSSecUsernameToken builder = new WSSecUsernameToken();
        builder.setPasswordType(null);
        builder.setUserInfo("wernerd", null);
        
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        Document signedDoc = builder.build(doc, secHeader);
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Message with UserNameToken PW Text:");
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(signedDoc);
            LOG.debug(outputString);
        }
        try {
            verify(signedDoc);
            throw new Exception("Custom token types are not permitted");
        } catch (WSSecurityException ex) {
            assertTrue(ex.getErrorCode() == WSSecurityException.FAILED_AUTHENTICATION);
            // expected
        }
    }
    
    /**
     * Test with a null password type. This will pass as the WSSConfig is configured to 
     * handle custom token types.
     */
    public void testUsernameTokenCustomPass() throws Exception {
        WSSecUsernameToken builder = new WSSecUsernameToken();
        builder.setPasswordType(null);
        builder.setUserInfo("customUser", null);
        
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        Document signedDoc = builder.build(doc, secHeader);
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Message with UserNameToken PW Text:");
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(signedDoc);
            LOG.debug(outputString);
        }
        
        //
        // Configure so that custom token types are accepted
        //
        WSSConfig cfg = WSSConfig.getNewInstance();
        cfg.setHandleCustomPasswordTypes(true);
        secEngine.setWssConfig(cfg);
        verify(signedDoc);
        
        //
        // Go back to default for other tests
        //
        cfg.setHandleCustomPasswordTypes(false);
        secEngine.setWssConfig(cfg);
    }
    
    
    /**
     * A test for WSS-66 - the nonce string is null
     * http://issues.apache.org/jira/browse/WSS-66
     * "Possible security hole when PasswordDigest is used by client."
     */
    public void testNullNonce() throws Exception {
        WSSecUsernameToken builder = new WSSecUsernameToken();
        builder.setPasswordType(WSConstants.PASSWORD_DIGEST);
        builder.setUserInfo("wernerd", "BAD_PASSWORD");
        
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        Document utDoc = builder.build(doc, secHeader);
        
        //
        // Manually find the Nonce node and set the content to null
        //
        org.w3c.dom.Element elem = builder.getUsernameTokenElement();
        org.w3c.dom.NodeList list = elem.getElementsByTagName("wsse:Nonce");
        org.w3c.dom.Node nonceNode = list.item(0);
        org.w3c.dom.Node childNode = nonceNode.getFirstChild();
        childNode.setNodeValue("");
        
        if (LOG.isDebugEnabled()) {
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(utDoc);
            LOG.debug(outputString);
        }
        
        try {
            //
            // Verification should fail as the password is bad
            //
            verify(utDoc);
            throw new Exception("Expected failure due to a bad password");
        } catch (WSSecurityException ex) {
            assertTrue(ex.getErrorCode() == WSSecurityException.FAILED_AUTHENTICATION);
            // expected
        }
    }
    
    /**
     * A test for WSS-66 - the created string is null
     * http://issues.apache.org/jira/browse/WSS-66
     * "Possible security hole when PasswordDigest is used by client."
     */
    public void testNullCreated() throws Exception {
        WSSecUsernameToken builder = new WSSecUsernameToken();
        builder.setPasswordType(WSConstants.PASSWORD_DIGEST);
        builder.setUserInfo("wernerd", "BAD_PASSWORD");
        
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        Document utDoc = builder.build(doc, secHeader);
        
        //
        // Manually find the Created node and set the content to null
        //
        org.w3c.dom.Element elem = builder.getUsernameTokenElement();
        org.w3c.dom.NodeList list = elem.getElementsByTagName("wsu:Created");
        org.w3c.dom.Node nonceNode = list.item(0);
        org.w3c.dom.Node childNode = nonceNode.getFirstChild();
        childNode.setNodeValue("");
        
        if (LOG.isDebugEnabled()) {
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(utDoc);
            LOG.debug(outputString);
        }
        
        try {
            //
            // Verification should fail as the password is bad
            //
            verify(utDoc);
            throw new Exception("Expected failure due to a bad password");
        } catch (WSSecurityException ex) {
            assertTrue(ex.getErrorCode() == WSSecurityException.FAILED_AUTHENTICATION);
            // expected
        }
    }
    
    /**
     * Test that verifies an EncodingType is set for the nonce. See WSS-169.
     */
    public void testUsernameTokenNonceEncodingType() throws Exception {
        WSSecUsernameToken builder = new WSSecUsernameToken();
        builder.setUserInfo("wernerd", "verySecret");
        LOG.info("Before adding UsernameToken PW Digest....");
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        Document signedDoc = builder.build(doc, secHeader);
        String outputString = 
            org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(signedDoc);
        assertTrue(outputString.indexOf("EncodingType") != -1);
    }
    
    /**
     * Test that adds a UserNameToken via WSHandler
     */
    public void testUsernameTokenWSHandler() throws Exception {
        MyHandler handler = new MyHandler();
        Document doc = unsignedEnvelope.getAsDocument();
        
        RequestData reqData = new RequestData();
        java.util.Map config = new java.util.TreeMap();
        config.put("password", "verySecret");
        config.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);
        reqData.setUsername("wernerd");
        reqData.setMsgContext(config);
        
        java.util.Vector actions = new java.util.Vector();
        actions.add(new Integer(WSConstants.UT));
        
        handler.send(WSConstants.UT, doc, reqData, actions, true);
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Username Token via WSHandler");
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(doc);
            LOG.debug(outputString);
        }
    }
    
    /**
     * Test that adds a UserNameToken with an empty password via WSHandler
     */
    public void testUsernameTokenWSHandlerEmptyPassword() throws Exception {
        MyHandler handler = new MyHandler();
        Document doc = unsignedEnvelope.getAsDocument();
        
        RequestData reqData = new RequestData();
        java.util.Map config = new java.util.TreeMap();
        config.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);
        config.put(WSHandlerConstants.PW_CALLBACK_REF, this);
        reqData.setUsername("emptyuser");
        reqData.setMsgContext(config);
        
        java.util.Vector actions = new java.util.Vector();
        actions.add(new Integer(WSConstants.UT));
        
        handler.send(WSConstants.UT, doc, reqData, actions, true);
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Username Token with an empty password via WSHandler");
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(doc);
            LOG.debug(outputString);
        }
    }
    
    /**
     * Verifies the soap envelope
     * 
     * @param env soap envelope
     * @throws java.lang.Exception Thrown when there is a problem in verification
     */
    private void verify(Document doc) throws Exception {
        LOG.info("Before verifying UsernameToken....");
        secEngine.processSecurityHeader(doc, null, this, null);
        LOG.info("After verifying UsernameToken....");
    }

    public void handle(Callback[] callbacks)
        throws IOException, UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof WSPasswordCallback) {
                WSPasswordCallback pc = (WSPasswordCallback) callbacks[i];
                boolean passwordsAreEncoded = WSSecurityEngine.getInstance().getWssConfig().getPasswordsAreEncoded();
                if (pc.getUsage() == WSPasswordCallback.USERNAME_TOKEN
                    && "wernerd".equals(pc.getIdentifier())) {
                    if (passwordsAreEncoded) {
                        // "hGqoUreBgahTJblQ3DbJIkE6uNs=" is the Base64 encoded SHA-1 hash of "verySecret".
                        pc.setPassword("hGqoUreBgahTJblQ3DbJIkE6uNs=");
                    } else {
                        pc.setPassword("verySecret");
                    }
                } else if (pc.getUsage() == WSPasswordCallback.USERNAME_TOKEN
                    && "emptyuser".equals(pc.getIdentifier())) {
                    pc.setPassword("");
                }  
                else if (
                    pc.getUsage() == WSPasswordCallback.USERNAME_TOKEN_UNKNOWN
                ) {
                    if ("wernerd".equals(pc.getIdentifier())
                        && "verySecret".equals(pc.getPassword())) {
                        return;
                    } else if ("customUser".equals(pc.getIdentifier())) {
                        return;
                    } else if ("wernerd".equals(pc.getIdentifier())
                        && "".equals(pc.getPassword())) {
                        return;
                    } else {
                        throw new IOException("Authentication failed");
                    }
                }
            } else {
                throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
            }
        }
    }
}
