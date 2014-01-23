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
import org.apache.ws.security.SOAPConstants;
import org.apache.ws.security.WSEncryptionPart;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.util.WSSecurityUtil;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;
import org.apache.ws.security.message.WSSecSignature;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecTimestamp;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Vector;

/**
 * WS-Security Test Case
 * <p/>
 * 
 * @author Davanum Srinivas (dims@yahoo.com)
 */
public class TestWSSecurityNew11 extends TestCase {
    private static final Log LOG = LogFactory.getLog(TestWSSecurityNew11.class);
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
    private Crypto crypto = CryptoFactory.getInstance("wss40.properties");
    private MessageContext msgContext;
    private SOAPEnvelope unsignedEnvelope;

    /**
     * TestWSSecurity constructor
     * <p/>
     * 
     * @param name name of the test
     */
    public TestWSSecurityNew11(String name) {
        super(name);
    }

    /**
     * JUnit suite
     * <p/>
     * 
     * @return a junit test suite
     */
    public static Test suite() {
        return new TestSuite(TestWSSecurityNew11.class);
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
     * Test that signs and verifies a WS-Security envelope.
     * This test uses the direct reference key identifier (certificate included
     * as a BinarySecurityToken (BST) in the message). The test signs the message
     * body (SOAP Body) and uses the STRTransform to sign the embedded certificate
     * <p/>
     * 
     * @throws java.lang.Exception Thrown when there is any problem in signing or verification
     */
    public void testX509SignatureDirectSTR() throws Exception {
        WSSecSignature builder = new WSSecSignature();
        builder.setUserInfo("wss40", "security");
        // builder.setUserInfo("john", "keypass");
        SOAPConstants soapConstants = WSSecurityUtil.getSOAPConstants(unsignedEnvelope.getAsDOM());
        Vector parts = new Vector();
        
        /*
         * Set up to sign body and use STRTransorm to sign
         * the signature token (e.g. X.509 certificate)
         */
        WSEncryptionPart encP =
            new WSEncryptionPart(
                soapConstants.getBodyQName().getLocalPart(),
                soapConstants.getEnvelopeURI(),
                "Content");
        parts.add(encP);
        encP =
            new WSEncryptionPart(
                "STRTransform",
                soapConstants.getEnvelopeURI(),
                "Content");
        parts.add(encP);

        builder.setParts(parts);
        builder.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);

        LOG.info("Before Signing STR DirectReference....");
        Document doc = unsignedEnvelope.getAsDocument();

        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);

        Document signedDoc = builder.build(doc, crypto, secHeader);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Signed message with STR DirectReference key identifier:");
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(signedDoc);
            LOG.debug(outputString);
        }
        LOG.info("After Signing STR DirectReference....");
        verify(signedDoc);
    }
    
    /**
     * This is a test for WSS-96:
     * "Error when making a signature when containing a WSSecTimestamp"
     * A timestamp is added to the document and signed.
     */
    public void testWSS96() throws Exception {
        WSSecSignature builder = new WSSecSignature();
        builder.setUserInfo("wss40", "security");
        SOAPConstants soapConstants = WSSecurityUtil.getSOAPConstants(unsignedEnvelope.getAsDOM());
        Vector parts = new Vector();
        
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        /*
         * Set up to sign body and use STRTransorm to sign
         * the signature token (e.g. X.509 certificate)
         */
        WSEncryptionPart encP =
            new WSEncryptionPart(
                soapConstants.getBodyQName().getLocalPart(),
                soapConstants.getEnvelopeURI(),
                "Content");
        parts.add(encP);
        encP =
            new WSEncryptionPart(
                "STRTransform",
                soapConstants.getEnvelopeURI(),
                "Content");
        parts.add(encP);
        
        WSSecTimestamp timestamp = new WSSecTimestamp();
        timestamp.setTimeToLive(600);
        timestamp.build(doc, secHeader);
        parts.add(new WSEncryptionPart(timestamp.getId()));

        builder.setParts(parts);
        builder.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);

        LOG.info("Before Signing STR DirectReference....");
        Document signedDoc = builder.build(doc, crypto, secHeader);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Signed message with STR DirectReference key identifier:");
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(signedDoc);
            LOG.debug(outputString);
        }
        LOG.info("After Signing STR DirectReference....");
        verify(signedDoc);
    }


    /**
     * Test that signs and verifies a WS-Security envelope.
     * This test uses the IssuerSerial reference key identifier (certificate not included
     * in the message)and reads the certificate from a keystore using IssuerSerialNumber
     * to identify it.
     * <p/>
     * 
     * @throws java.lang.Exception Thrown when there is any problem in signing or verification
     */
    public void testX509SignatureISSTR() throws Exception {
        WSSecSignature builder = new WSSecSignature();
        builder.setUserInfo("wss40", "security");
        // builder.setUserInfo("john", "keypass");
        SOAPConstants soapConstants = WSSecurityUtil.getSOAPConstants(unsignedEnvelope.getAsDOM());
        Vector parts = new Vector();
        
        /*
         * Set up to sign body and use STRTransorm to sign
         * the signature token (e.g. X.509 certificate)
         */
        WSEncryptionPart encP =
            new WSEncryptionPart(
                soapConstants.getBodyQName().getLocalPart(),    // define the body
                soapConstants.getEnvelopeURI(),
                "Content");
        parts.add(encP);
        encP =
            new WSEncryptionPart(
                "STRTransform",                // reserved word to use STRTransform
                soapConstants.getEnvelopeURI(),
                "Content");
        parts.add(encP);

        builder.setParts(parts);
        builder.setKeyIdentifierType(WSConstants.ISSUER_SERIAL);

        LOG.info("Before Signing STR IS....");
        Document doc = unsignedEnvelope.getAsDocument();
        
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);

        Document signedDoc = builder.build(doc, crypto, secHeader);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Signed message with STR IssuerSerial key identifier:");
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(signedDoc);
            LOG.debug(outputString);
        }
        LOG.info("After Signing STR IS....");
        verify(signedDoc);
    }
    
    /**
     * Test that signs and verifies a WS-Security envelope.
     * This test uses the SubjectKeyIdentifier key identifier (certificate not included
     * in the message) and reads the certificate from a keystore using SKI
     * to identify it.
     * <p/>
     * 
     * @throws java.lang.Exception Thrown when there is any problem in signing or verification
     */
    public void testX509SignatureSKISTR() throws Exception {
        WSSecSignature builder = new WSSecSignature();
        builder.setUserInfo("wss40", "security");
        // builder.setUserInfo("john", "keypass");
        SOAPConstants soapConstants = WSSecurityUtil.getSOAPConstants(unsignedEnvelope.getAsDOM());
        Vector parts = new Vector();
        
        /*
         * Set up to sign body and use STRTransorm to sign
         * the signature token (e.g. X.509 certificate)
         */
        WSEncryptionPart encP =
            new WSEncryptionPart(
                soapConstants.getBodyQName().getLocalPart(),    // define the body
                soapConstants.getEnvelopeURI(),
                "Content");
        parts.add(encP);
        encP =
            new WSEncryptionPart(
                "STRTransform",                // reserved word to use STRTransform
                soapConstants.getEnvelopeURI(),
                "Content");
        parts.add(encP);

        builder.setParts(parts);
        builder.setKeyIdentifierType(WSConstants.SKI_KEY_IDENTIFIER);

        LOG.info("Before Signing STR SKI....");
        Document doc = unsignedEnvelope.getAsDocument();
        
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);

        Document signedDoc = builder.build(doc, crypto, secHeader);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Signed message with STR SKI key identifier:");
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(signedDoc);
            LOG.debug(outputString);
        }
        LOG.info("After Signing STR SKI....");
        verify(signedDoc);
    }


    /**
     * Verifies the soap envelope
     * 
     * @param env soap envelope
     * @throws java.lang.Exception Thrown when there is a problem in verification
     */
    private void verify(Document doc) throws Exception {
        secEngine.processSecurityHeader(doc, null, null, crypto);
    }
}
