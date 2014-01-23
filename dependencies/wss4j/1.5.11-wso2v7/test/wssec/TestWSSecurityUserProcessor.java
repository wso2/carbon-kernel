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
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSConfig;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;
import org.apache.ws.security.handler.WSHandler;
import org.apache.ws.security.handler.RequestData;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.message.WSSecSignature;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Vector;


/**
 * WS-Security Test Case
 * <p/>
 */
public class TestWSSecurityUserProcessor extends TestCase {
    private static final Log LOG = LogFactory.getLog(TestWSSecurityUserProcessor.class);
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
    private SOAPEnvelope unsignedEnvelope;

    /**
     * TestWSSecurity constructor
     * <p/>
     * 
     * @param name name of the test
     */
    public TestWSSecurityUserProcessor(String name) {
        super(name);
    }

    /**
     * JUnit suite
     * <p/>
     * 
     * @return a junit test suite
     */
    public static Test suite() {
        return new TestSuite(TestWSSecurityUserProcessor.class);
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
     * Test to see that a custom processor configured through a 
     * WSSConfig instance is called
     */
    public void 
    testCustomUserProcessor() throws Exception {
        WSSecSignature builder = new WSSecSignature();
        builder.setUserInfo("16c73ab6-b892-458f-abf5-2f875f74882e", "security");
        builder.setKeyIdentifierType(WSConstants.ISSUER_SERIAL);
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
        //
        // Check to make sure we can install/replace and use our own processor
        //
        WSSConfig cfg = WSSConfig.getNewInstance();
        String p = "wssec.MyProcessor";
        cfg.setProcessor(
            WSSecurityEngine.SIGNATURE,
            p
        );
        final WSSecurityEngine engine = new WSSecurityEngine();
        engine.setWssConfig(cfg);
        final java.util.List results = 
            engine.processSecurityHeader(doc, null, null, crypto);
        boolean found = false;
        for (final java.util.Iterator pos = results.iterator();  pos.hasNext(); ) {
            final java.util.Map result = (java.util.Map) pos.next();
            Object obj = result.get("foo");
            if (obj != null) {
                if (obj.getClass().getName().equals(p)) {
                    found = true;
                }
            }
        }
        assertTrue("Unable to find result from MyProcessor", found);
    }
    
    /**
     * Test to see that a custom processor (object) configured through a 
     * WSSConfig instance is called
     */
    public void 
    testCustomUserProcessorObject() throws Exception {
        WSSecSignature builder = new WSSecSignature();
        builder.setUserInfo("16c73ab6-b892-458f-abf5-2f875f74882e", "security");
        builder.setKeyIdentifierType(WSConstants.ISSUER_SERIAL);
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
        //
        // Check to make sure we can install/replace and use our own processor
        //
        WSSConfig cfg = WSSConfig.getNewInstance();
        cfg.setProcessor(
            WSSecurityEngine.SIGNATURE,
            new wssec.MyProcessor()
        );
        final WSSecurityEngine engine = new WSSecurityEngine();
        engine.setWssConfig(cfg);
        final java.util.List results = 
            engine.processSecurityHeader(doc, null, null, crypto);
        boolean found = false;
        for (final java.util.Iterator pos = results.iterator();  pos.hasNext(); ) {
            final java.util.Map result = (java.util.Map) pos.next();
            Object obj = result.get("foo");
            if (obj != null) {
                if (obj.getClass().getName().equals(wssec.MyProcessor.class.getName())) {
                    found = true;
                }
            }
        }
        assertTrue("Unable to find result from MyProcessor", found);
    }
    
    /**
     * Test to see that a custom action configured through a
     * WSSConfig instance is called
     */
    public void
    testCustomAction() throws Exception {
        
        final WSSConfig cfg = WSSConfig.getNewInstance();
        final int action = 0xDEADF000;
        cfg.setAction(action, "wssec.MyAction");
        final RequestData reqData = new RequestData();
        reqData.setWssConfig(cfg);
        reqData.setMsgContext(new java.util.TreeMap());
        
        final java.util.Vector actions = new java.util.Vector();
        actions.add(new Integer(action));
        final Document doc = unsignedEnvelope.getAsDocument();
        MyHandler handler = new MyHandler();
        reqData.setMsgContext("bread");
        assertEquals(reqData.getMsgContext(), "bread");
        handler.doit(
            action, 
            doc, 
            reqData, 
            actions
        );
        assertEquals(reqData.getMsgContext(), "crumb");
    }
    
    /**
     * Test to see that a custom action object configured through a
     * WSSConfig instance is called
     */
    public void
    testCustomActionObject() throws Exception {
        
        final WSSConfig cfg = WSSConfig.getNewInstance();
        final int action = 0xDEADF000;
        cfg.setAction(action, new wssec.MyAction());
        final RequestData reqData = new RequestData();
        reqData.setWssConfig(cfg);
        reqData.setMsgContext(new java.util.TreeMap());
        
        final java.util.Vector actions = new java.util.Vector();
        actions.add(new Integer(action));
        final Document doc = unsignedEnvelope.getAsDocument();
        MyHandler handler = new MyHandler();
        reqData.setMsgContext("bread");
        assertEquals(reqData.getMsgContext(), "bread");
        handler.doit(
            action, 
            doc, 
            reqData, 
            actions
        );
        assertEquals(reqData.getMsgContext(), "crumb");
    }
    
    /**
     * Test to see that a custom action can be configured via WSSecurityUtil.decodeAction.
     * A standard Timestamp action is also configured.
     */
    public void
    testDecodeCustomAction() throws Exception {
        
        final WSSConfig cfg = WSSConfig.getNewInstance();
        final int customAction = 0xDEADF000;
        
        String actionString = 
            WSHandlerConstants.TIMESTAMP + " " + new Integer(customAction).toString();
        Vector actionList = new Vector();
        //
        // This parsing will fail as it doesn't know what the custom action is
        //
        try {
            WSSecurityUtil.decodeAction(actionString, actionList);
            fail("Failure expected on unknown action");
        } catch (WSSecurityException ex) {
            // expected
        }
        actionList.clear();
        
        //
        // This parsing will fail as WSSConfig doesn't know what the custom action is
        //
        try {
            WSSecurityUtil.decodeAction(actionString, actionList, cfg);
            fail("Failure expected on unknown action");
        } catch (WSSecurityException ex) {
            // expected
        }
        actionList.clear();
        
        //
        // This parsing will fail as the action String is badly formed
        //
        try {
            String badActionString = 
                WSHandlerConstants.TIMESTAMP + " " + "NewCustomAction";
            WSSecurityUtil.decodeAction(badActionString, actionList, cfg);
            fail("Failure expected on unknown action");
        } catch (WSSecurityException ex) {
            // expected
        }
        actionList.clear();
        
        //
        // This parsing should pass as WSSConfig has been configured with the custom action
        //
        cfg.setAction(customAction, "wssec.MyAction");
        int actions = WSSecurityUtil.decodeAction(actionString, actionList, cfg);
        
        final RequestData reqData = new RequestData();
        reqData.setWssConfig(cfg);
        
        final Document doc = SOAPUtil.toSOAPPart(SOAPMSG);
        MyHandler handler = new MyHandler();
        reqData.setMsgContext("bread");
        assertEquals(reqData.getMsgContext(), "bread");
        handler.doit(
            actions, 
            doc, 
            reqData, 
            actionList
        );
        assertEquals(reqData.getMsgContext(), "crumb");
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Message:");
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(doc);
            LOG.debug(outputString);
        }
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
            return null;
        }
    
        public void 
        setPassword(Object msgContext, String password) {
        }
        
        public String 
        getPassword(Object msgContext) {
            return null;
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
