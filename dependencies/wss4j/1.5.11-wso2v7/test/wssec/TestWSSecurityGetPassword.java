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
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSConfig;
import org.apache.ws.security.handler.WSHandler;
import org.apache.ws.security.handler.RequestData;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;


/**
 * WS-Security Test Case for the getPassword method in WSHandler.
 * <p/>
 */
public class TestWSSecurityGetPassword extends TestCase {
    private static final Log LOG = LogFactory.getLog(TestWSSecurityGetPassword.class);
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

    private MessageContext msgContext;
    private SOAPEnvelope unsignedEnvelope;

    /**
     * TestWSSecurity constructor
     * <p/>
     * 
     * @param name name of the test
     */
    public TestWSSecurityGetPassword(String name) {
        super(name);
    }

    /**
     * JUnit suite
     * <p/>
     * 
     * @return a junit test suite
     */
    public static Test suite() {
        return new TestSuite(TestWSSecurityGetPassword.class);
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
     * A unit test for {@link WSHandler#getPassword(String, int, String, String, RequestData)},
     * where the password is obtained from the Message Context.
     */
    public void
    testGetPasswordRequestContextUnit() throws Exception {
        
        final WSSConfig cfg = WSSConfig.getNewInstance();
        final RequestData reqData = new RequestData();
        reqData.setWssConfig(cfg);
        java.util.Map messageContext = new java.util.TreeMap();
        messageContext.put("password", "securityPassword");
        reqData.setMsgContext(messageContext);
        
        WSHandler handler = new MyHandler();
        WSPasswordCallback callback = 
            handler.getPassword(
                "bob", 
                WSConstants.UT, 
                "SomeCallbackTag", 
                "SomeCallbackRef",
                reqData
            );
        assertTrue("bob".equals(callback.getIdentifier()));
        assertTrue("securityPassword".equals(callback.getPassword()));
        assertTrue(WSPasswordCallback.USERNAME_TOKEN == callback.getUsage());
    }
    
    /**
     * A WSHandler test for {@link WSHandler#getPassword(String, int, String, String, RequestData)},
     * where the password is obtained from the Message Context.
     */
    public void
    testGetPasswordRequestContext() throws Exception {
        
        final WSSConfig cfg = WSSConfig.getNewInstance();
        final RequestData reqData = new RequestData();
        reqData.setWssConfig(cfg);
        reqData.setUsername("bob");
        reqData.setPwType(WSConstants.PASSWORD_TEXT);
        java.util.Map messageContext = new java.util.TreeMap();
        messageContext.put("password", "securityPassword");
        reqData.setMsgContext(messageContext);
        
        final java.util.Vector actions = new java.util.Vector();
        actions.add(new Integer(WSConstants.UT));
        Document doc = unsignedEnvelope.getAsDocument();
        MyHandler handler = new MyHandler();
        handler.doit(
            WSConstants.UT, 
            doc, 
            reqData, 
            actions
        );
        
        String outputString = 
            org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(doc);
        if (LOG.isDebugEnabled()) {
            LOG.debug(outputString);
        }
        assertTrue(outputString.indexOf("bob") != -1);
        assertTrue(outputString.indexOf("securityPassword") != -1);
    }
    
    /**
     * A test for {@link WSHandler#getPassword(String, int, String, String, RequestData)},
     * where the password is obtained from a Callback Handler, which is placed on the 
     * Message Context using a reference.
     */
    public void
    testGetPasswordCallbackHandlerRef() throws Exception {
        
        final WSSConfig cfg = WSSConfig.getNewInstance();
        final RequestData reqData = new RequestData();
        reqData.setWssConfig(cfg);
        reqData.setUsername("bob");
        reqData.setPwType(WSConstants.PASSWORD_TEXT);
        java.util.Map messageContext = new java.util.TreeMap();
        messageContext.put(
            WSHandlerConstants.PW_CALLBACK_REF, 
            new MyCallbackHandler()
        );
        reqData.setMsgContext(messageContext);
        
        final java.util.Vector actions = new java.util.Vector();
        actions.add(new Integer(WSConstants.UT));
        Document doc = unsignedEnvelope.getAsDocument();
        MyHandler handler = new MyHandler();
        handler.doit(
            WSConstants.UT, 
            doc, 
            reqData, 
            actions
        );
        
        String outputString = 
            org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(doc);
        if (LOG.isDebugEnabled()) {
            LOG.debug(outputString);
        }
        assertTrue(outputString.indexOf("bob") != -1);
        assertTrue(outputString.indexOf("securityPassword") != -1);
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
    
    public static class MyCallbackHandler implements CallbackHandler {
        public void handle(Callback[] callbacks)
            throws IOException, UnsupportedCallbackException {
            for (int i = 0; i < callbacks.length; i++) {
                if (callbacks[i] instanceof WSPasswordCallback) {
                    WSPasswordCallback pc = (WSPasswordCallback) callbacks[i];
                    if (pc.getIdentifier() == "bob") {
                        pc.setPassword("securityPassword");
                    }
                } else {
                    throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
                }
            }
        }
    }
    
}
