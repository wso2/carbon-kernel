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
import org.apache.ws.security.WSSConfig;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;
import org.apache.ws.security.handler.RequestData;
import org.apache.ws.security.handler.WSHandler;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.message.WSSecSignature;
import org.apache.ws.security.message.WSSecHeader;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.InputStream;


/**
 * WS-Security Test Case
 * <p/>
 * 
 * @author Davanum Srinivas (dims@yahoo.com)
 */
public class TestWSSecurityNew18 extends TestCase {
    private static final Log LOG = LogFactory.getLog(TestWSSecurityNew18.class);
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
    public TestWSSecurityNew18(String name) {
        super(name);
    }

    /**
     * JUnit suite
     * <p/>
     * 
     * @return a junit test suite
     */
    public static Test suite() {
        return new TestSuite(TestWSSecurityNew18.class);
    }

    /**
     * Setup method
     * <p/>
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
     * The test uses the ThumbprintSHA1 key identifier type.
     * <p/>
     * 
     * @throws java.lang.Exception Thrown when there is any problem in signing or verification
     */
    public void testX509SignatureIS() throws Exception {
        WSSecSignature builder = new WSSecSignature();
        builder.setUserInfo("16c73ab6-b892-458f-abf5-2f875f74882e", "security");
        builder.setKeyIdentifierType(WSConstants.ISSUER_SERIAL);
        builder.setSignatureAlgorithm("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256");
        builder.setDigestAlgo("http://www.w3.org/2001/04/xmlenc#sha256");
        LOG.info("Before Signing IS....");
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        Document signedDoc = builder.build(doc, crypto, secHeader);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Signed message with IssuerSerial key identifier:");
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(signedDoc);
            LOG.debug(outputString);
        }
        LOG.info("After Signing IS....");
        verify(signedDoc);
    }


    /**
     * Test that signs (twice) and verifies a WS-Security envelope.
     * <p/>
     * 
     * @throws java.lang.Exception Thrown when there is any problem in signing or verification
     */
    public void testDoubleX509SignatureIS() throws Exception {
        WSSecSignature builder = new WSSecSignature();
        builder.setUserInfo("16c73ab6-b892-458f-abf5-2f875f74882e", "security");
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        Document signedDoc = builder.build(doc, crypto, secHeader);
        Document signedDoc1 = builder.build(signedDoc, crypto, secHeader);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Signed message with IssuerSerial key identifier (Doc1):");
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(signedDoc1);
            LOG.debug(outputString);
        }

        verify(signedDoc1);
    }

    /**
     * Verifies the soap envelope.
     * This method verifies all the signature generated. 
     * 
     * @param env soap envelope
     * @throws java.lang.Exception Thrown when there is a problem in verification
     */
    private void verify(Document doc) throws Exception {
        secEngine.processSecurityHeader(doc, null, null, crypto);
    }
    
    /**
     * Ensure that we can load a custom crypto implementation using a Map
     */
    public void testCustomCrypto() {
        java.util.Map tmp = new java.util.TreeMap();
        Crypto crypto = CryptoFactory.getInstance(
            "wssec.CustomCrypto",
            tmp
        );
        assertNotNull(crypto);
        assertTrue(crypto instanceof CustomCrypto);
        CustomCrypto custom = (CustomCrypto)crypto;
        assertSame(tmp, custom.config);
    }
    
    /**
     * A test for "SignatureAction does not set DigestAlgorithm on WSSecSignature instance"
     */
    public void
    testWSS170() throws Exception {
        final WSSConfig cfg = WSSConfig.getNewInstance();
        final int action = WSConstants.SIGN;
        final RequestData reqData = new RequestData();
        reqData.setWssConfig(cfg);
        reqData.setUsername("16c73ab6-b892-458f-abf5-2f875f74882e");
        java.util.Map config = new java.util.TreeMap();
        config.put(WSHandlerConstants.SIG_PROP_FILE, "crypto.properties");
        config.put("password", "security");
        config.put(
            WSHandlerConstants.SIG_ALGO, 
            "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256"
        );
        config.put(
            WSHandlerConstants.SIG_DIGEST_ALGO, 
            "http://www.w3.org/2001/04/xmlenc#sha256"
        );
        reqData.setMsgContext(config);
        
        final java.util.Vector actions = new java.util.Vector();
        actions.add(new Integer(action));
        final Document doc = unsignedEnvelope.getAsDocument();
        MyHandler handler = new MyHandler();
        handler.doit(
            action, 
            doc, 
            reqData, 
            actions
        );
        String outputString = 
            org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(doc);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Signed message:");
            LOG.debug(outputString);
        }
        assertTrue(
            outputString.indexOf("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256") != -1
        );
        assertTrue(
            outputString.indexOf("http://www.w3.org/2001/04/xmlenc#sha256") != -1
        );
        
        verify(doc);
    }
    
    /**
     * a trivial extension of the WSHandler type
     */
    public static class MyHandler extends WSHandler {
        
        public Object 
        getOption(String key) {
            return null;
        }
        
        public void 
        setProperty(
            Object msgContext, 
            String key, 
            Object value
        ) {
        }

        public Object 
        getProperty(Object ctx, String key) {
            return ((java.util.Map)ctx).get(key);
        }
    
        public void 
        setPassword(Object msgContext, String password) {
        }
        
        public String 
        getPassword(Object msgContext) {
            return (String)((java.util.Map)msgContext).get("password");
        }

        void doit(
            int action, 
            Document doc,
            RequestData reqData, 
            java.util.Vector actions
        ) throws org.apache.ws.security.WSSecurityException {
            doSenderAction(
                action, 
                doc, 
                reqData, 
                actions,
                true
            );
        }
    }
}
