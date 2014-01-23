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
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;
import org.apache.ws.security.message.WSSecEncrypt;
import org.apache.ws.security.message.WSSecUsernameToken;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.token.Reference;
import org.apache.ws.security.message.token.SecurityTokenReference;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Document;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * TestCase10 for testing HMAC_SHA1 in wss4j. Based on TestCase9.
 * 
 * The objective of this TestCase is to test the HMAC_SHA1 signature.
 * 
 * @author Dimuthu Leelarathne. (muthulee@yahoo.com)
 */
public class TestWSSecurityNew10 extends TestCase implements CallbackHandler {
    private static final Log LOG = LogFactory.getLog(TestWSSecurityNew10.class);
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

    private Crypto crypto = CryptoFactory.getInstance();
    private MessageContext msgContext;
    private Message message;

    /**
     * TestWSSecurity constructor <p/>
     * 
     * @param name
     *            name of the test
     */
    public TestWSSecurityNew10(String name) {
        super(name);
    }
    
    /**
     * JUnit suite
     * <p/>
     * 
     * @return a junit test suite
     */
    public static Test suite() {
        return new TestSuite(TestWSSecurityNew10.class);
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
        InputStream in = new ByteArrayInputStream(SOAPMSG.getBytes());
        Message msg = new Message(in);
        msg.setMessageContext(msgContext);
        return msg;
    }

    /**
     * Test that encrypts and signs a WS-Security envelope, then performs
     * verification and decryption. <p/>
     * 
     * @throws Exception
     *             Thrown when there is any problem in signing, encryption,
     *             decryption, or verification
     */
    public void testEMBED_SECURITY_TOKEN_REF() throws Exception {

        SOAPEnvelope unsignedEnvelope = message.getSOAPEnvelope();
        WSSecEncrypt wsEncrypt = new WSSecEncrypt();

        // Get the message as document
        LOG.info("Before Encryption....");
        Document doc = unsignedEnvelope.getAsDocument();

        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);

        /*
         * Step 1 :: Add a UserNameToken. Step 2 :: Add an Id to it. Step 3 ::
         * Create a Reference to the UserNameToken. Step 4 :: Setting necessary
         * parameters in WSEncryptBody. Step 5 :: Encrypt using the using the
         * password of UserNameToken.
         */

        // Step 1
        String username = "Dimthu";
        String password = "Sri Lanka Sri Lanka UOM ";
        byte[] key = password.getBytes();

        WSSecUsernameToken builder = new WSSecUsernameToken();
        builder.setPasswordType(WSConstants.PASSWORD_TEXT);
        builder.setUserInfo(username, password);
        builder.build(doc, secHeader);

        // Step 3 ::
        Reference ref = new Reference(doc);
        ref.setURI("#" + builder.getId());
        ref.setValueType("UsernameToken");
        SecurityTokenReference secRef = new SecurityTokenReference(doc);
        secRef.setReference(ref);

        // adding the namespace
        WSSecurityUtil.setNamespace(secRef.getElement(), WSConstants.WSSE_NS,
                WSConstants.WSSE_PREFIX);

        // Step 4 ::
        wsEncrypt.setKeyIdentifierType(WSConstants.EMBED_SECURITY_TOKEN_REF);
        wsEncrypt.setSecurityTokenReference(secRef);
        wsEncrypt.setKey(key);
        wsEncrypt.setSymmetricEncAlgorithm(WSConstants.TRIPLE_DES);
        wsEncrypt.setDocument(doc);

        // Step 4 :: Encrypting using the key.
        Document encDoc = wsEncrypt.build(doc, crypto, secHeader);

        if (LOG.isDebugEnabled()) {
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(encDoc);
            LOG.debug(outputString);
        }
        LOG.info("Encryption Done\n");
    }


    /*
     * (non-Javadoc)
     * 
     * @see javax.security.auth.callback.CallbackHandler#handle(javax.security.auth.callback.Callback[])
     */
    public void handle(Callback[] callbacks) throws IOException,
        UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof WSPasswordCallback) {
                WSPasswordCallback pc = (WSPasswordCallback) callbacks[i];
                if (pc.getUsage() == WSPasswordCallback.USERNAME_TOKEN) {
                    pc.setPassword("Sri Lanka Sri Lanka UOM ");
                } else if (pc.getUsage() == WSPasswordCallback.DECRYPT) {
                    pc.setKey("Sri Lanka Sri Lanka UOM ".getBytes());

                }
            } else {
                throw new UnsupportedCallbackException(callbacks[i],
                        "Unrecognized Callback");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        junit.textui.TestRunner.run(suite());
    }

}
