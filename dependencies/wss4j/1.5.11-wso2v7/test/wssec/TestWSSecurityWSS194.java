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
import java.util.Vector;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

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
import org.apache.ws.security.WSSConfig;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.components.crypto.CryptoFactory;
import org.apache.ws.security.handler.RequestData;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.w3c.dom.Document;


/**
 * This is a test for WSS-194 - "Support overriding KeyStore alias for signature so that it can
 * be different than user name used for UsernameToken".
 */
public class TestWSSecurityWSS194 extends TestCase implements CallbackHandler {
    
    private static final Log LOG = LogFactory.getLog(TestWSSecurityWSS194.class);
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
    private MessageContext msgContext;
    private SOAPEnvelope unsignedEnvelope;

    /**
     * TestWSSecurity constructor
     * <p/>
     * 
     * @param name name of the test
     */
    public TestWSSecurityWSS194(String name) {
        super(name);
    }

    /**
     * JUnit suite
     * <p/>
     * 
     * @return a junit test suite
     */
    public static Test suite() {
        return new TestSuite(TestWSSecurityWSS194.class);
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
     * Test involving adding a Username Token to a SOAP message and signing it, where the
     * private key for signature is extracted from the KeyStore using a different username/alias
     * to the UsernameToken. 
     */
    public void 
    testUsernameTokenSignatureHandler() throws Exception {
        final WSSConfig cfg = WSSConfig.getNewInstance();
        final RequestData reqData = new RequestData();
        reqData.setWssConfig(cfg);
        reqData.setUsername("alice");
        reqData.setPwType(WSConstants.PASSWORD_DIGEST);
        java.util.Map messageContext = new java.util.TreeMap();
        messageContext.put(
            WSHandlerConstants.PW_CALLBACK_REF, 
            this
        );
        messageContext.put(WSHandlerConstants.SIGNATURE_USER, "wss86");
        messageContext.put(WSHandlerConstants.SIG_PROP_FILE, "wss86.properties");
        messageContext.put(
            WSHandlerConstants.SIGNATURE_PARTS, 
            "{}{" + WSConstants.WSSE_NS + "}" + "UsernameToken"
        );
        messageContext.put(WSHandlerConstants.SIG_KEY_ID, "DirectReference");
        reqData.setMsgContext(messageContext);
        
        final java.util.Vector actions = new java.util.Vector();
        actions.add(new Integer(WSConstants.UT));
        actions.add(new Integer(WSConstants.SIGN));
        final Document doc = unsignedEnvelope.getAsDocument();
        MyHandler handler = new MyHandler();
        handler.send(
            WSConstants.UT | WSConstants.SIGN, 
            doc, 
            reqData, 
            actions,
            true
        );
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("After Signing....");
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(doc);
            LOG.debug(outputString);
        }
        
        verify(doc);
        
    }
    

    /**
     * Verifies the soap envelope
     * <p/>
     * 
     * @param doc 
     * @throws Exception Thrown when there is a problem in verification
     */
    private Vector verify(Document doc) throws Exception {
        Vector results = 
            secEngine.processSecurityHeader(
                doc, null, this, CryptoFactory.getInstance("wss86.properties")
            );
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
                if (pc.getUsage() == WSPasswordCallback.USERNAME_TOKEN
                        && "alice".equals(pc.getIdentifier())) {
                    pc.setPassword("verySecret");
                } else if (pc.getUsage() == WSPasswordCallback.SIGNATURE
                        && "wss86".equals(pc.getIdentifier())) {
                    pc.setPassword("security");
                } else {
                    throw new IOException("Authentication failed");
                }
            } else {
                throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
            }
        }
    }

    
}
