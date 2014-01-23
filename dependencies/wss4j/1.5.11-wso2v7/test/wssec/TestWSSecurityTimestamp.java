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
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.util.WSSecurityUtil;
import org.apache.ws.security.util.XmlSchemaDateFormat;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.handler.WSHandler;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecTimestamp;
import org.apache.ws.security.message.token.Timestamp;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.Vector;

/**
 * WS-Security Test Case for Timestamps.
 */
public class TestWSSecurityTimestamp extends TestCase {
    private static final Log LOG = LogFactory.getLog(TestWSSecurityTimestamp.class);
    private static final String soapMsg = 
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
    public TestWSSecurityTimestamp(String name) {
        super(name);
    }

    /**
     * JUnit suite
     * <p/>
     * 
     * @return a junit test suite
     */
    public static Test suite() {
        return new TestSuite(TestWSSecurityTimestamp.class);
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
        InputStream in = new ByteArrayInputStream(soapMsg.getBytes());
        Message msg = new Message(in);
        msg.setMessageContext(msgContext);
        return msg.getSOAPEnvelope();
    }

    
    /**
     * This is a test for processing a valid Timestamp.
     */
    public void testValidTimestamp() throws Exception {
        
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        WSSecTimestamp timestamp = new WSSecTimestamp();
        timestamp.setTimeToLive(300);
        Document createdDoc = timestamp.build(doc, secHeader);

        if (LOG.isDebugEnabled()) {
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(createdDoc);
            LOG.debug(outputString);
        }
        
        //
        // Do some processing
        //
        Vector wsResult = verify(createdDoc);
        WSSecurityEngineResult actionResult = 
            WSSecurityUtil.fetchActionResult(wsResult, WSConstants.TS);
        assertTrue(actionResult != null);
        
        Timestamp receivedTimestamp = 
            (Timestamp)actionResult.get(WSSecurityEngineResult.TAG_TIMESTAMP);
        assertTrue(receivedTimestamp != null);

        MyHandler myHandler = new MyHandler();
        if (!myHandler.publicVerifyTimestamp(receivedTimestamp, 300)) {
            fail("The timestamp could not be validated");
        }
    }
    
    
    /**
     * This is a test for processing a valid Timestamp with no expires element
     */
    public void testValidTimestampNoExpires() throws Exception {
        
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        WSSecTimestamp timestamp = new WSSecTimestamp();
        timestamp.setTimeToLive(0);
        Document createdDoc = timestamp.build(doc, secHeader);

        if (LOG.isDebugEnabled()) {
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(createdDoc);
            LOG.debug(outputString);
        }
        
        //
        // Do some processing
        //
        Vector wsResult = verify(createdDoc);
        WSSecurityEngineResult actionResult = 
            WSSecurityUtil.fetchActionResult(wsResult, WSConstants.TS);
        assertTrue(actionResult != null);
        
        Timestamp receivedTimestamp = 
            (Timestamp)actionResult.get(WSSecurityEngineResult.TAG_TIMESTAMP);
        assertTrue(receivedTimestamp != null);

        MyHandler myHandler = new MyHandler();
        if (!myHandler.publicVerifyTimestamp(receivedTimestamp, 300)) {
            fail("The timestamp could not be validated");
        }
    }
    
    
    /**
     * This is a test for processing an expired Timestamp.
     */
    public void testExpiredTimestamp() throws Exception {
        
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        WSSecTimestamp timestamp = new WSSecTimestamp();
        timestamp.setTimeToLive(-1);
        Document createdDoc = timestamp.build(doc, secHeader);

        if (LOG.isDebugEnabled()) {
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(createdDoc);
            LOG.debug(outputString);
        }
        
        try {
            //
            // Note that "expired" verification is done in the TimestampProcessor, whereas
            // "created" verification is done in the WSHandler
            //
            verify(createdDoc);
            fail("Expected failure on an expired timestamp");
        } catch (WSSecurityException ex) {
            assertTrue(ex.getErrorCode() == WSSecurityException.MESSAGE_EXPIRED); 
        }        
    }
    
    
    /**
     * This is a test for processing an "old" Timestamp, i.e. one with a "Created" element that is
     * out of date
     */
    public void testOldTimestamp() throws Exception {
        
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        WSSecTimestamp timestamp = new WSSecTimestamp();
        Document createdDoc = timestamp.build(doc, secHeader);

        if (LOG.isDebugEnabled()) {
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(createdDoc);
            LOG.debug(outputString);
        }
        
        //
        // Do some processing
        //
        Vector wsResult = verify(createdDoc);
        WSSecurityEngineResult actionResult = 
            WSSecurityUtil.fetchActionResult(wsResult, WSConstants.TS);
        assertTrue(actionResult != null);
        
        Timestamp receivedTimestamp = 
            (Timestamp)actionResult.get(WSSecurityEngineResult.TAG_TIMESTAMP);
        assertTrue(receivedTimestamp != null);

        MyHandler myHandler = new MyHandler();
        if (myHandler.publicVerifyTimestamp(receivedTimestamp, -1)) {
            fail("The timestamp validation should have failed");
        }     
    }
    
    /**
     * This is a test for processing an Timestamp where the "Created" element is in the future.
     * This Timestamp should be rejected.
     */
    public void testFutureCreated() throws Exception {
        
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        Element timestampElement = 
            doc.createElementNS(
                WSConstants.WSU_NS, WSConstants.WSU_PREFIX + ":" + WSConstants.TIMESTAMP_TOKEN_LN
            );

        DateFormat zulu = new XmlSchemaDateFormat();
        Element elementCreated =
            doc.createElementNS(
                WSConstants.WSU_NS, WSConstants.WSU_PREFIX + ":" + WSConstants.CREATED_LN
            );
        Date createdDate = new Date();
        long currentTime = createdDate.getTime() + 300000;
        createdDate.setTime(currentTime);
        elementCreated.appendChild(doc.createTextNode(zulu.format(createdDate)));
        timestampElement.appendChild(elementCreated);

        secHeader.getSecurityHeader().appendChild(timestampElement);
        
        if (LOG.isDebugEnabled()) {
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(doc);
            LOG.debug(outputString);
        }
        
        //
        // Do some processing
        //
        Vector wsResult = verify(doc);
        WSSecurityEngineResult actionResult = 
            WSSecurityUtil.fetchActionResult(wsResult, WSConstants.TS);
        assertTrue(actionResult != null);
        
        Timestamp receivedTimestamp = 
            (Timestamp)actionResult.get(WSSecurityEngineResult.TAG_TIMESTAMP);
        assertTrue(receivedTimestamp != null);
        
        MyHandler myHandler = new MyHandler();
        if (myHandler.publicVerifyTimestamp(receivedTimestamp, 300)) {
            fail("The timestamp validation should have failed");
        }
    }
    
    
    /**
     * Verifies the soap envelope
     * 
     * @param env soap envelope
     * @throws java.lang.Exception Thrown when there is a problem in verification
     */
    private Vector verify(Document doc) throws Exception {
        return secEngine.processSecurityHeader(doc, null, null, null);
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

        boolean publicVerifyTimestamp(
            Timestamp timestamp, 
            int ttl
        ) throws org.apache.ws.security.WSSecurityException {
            return verifyTimestamp(timestamp, ttl);
        }
    }
    
}
