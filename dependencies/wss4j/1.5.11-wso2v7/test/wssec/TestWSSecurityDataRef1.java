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
import java.util.ArrayList;
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
import org.apache.ws.security.WSDataRef;
import org.apache.ws.security.WSEncryptionPart;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;
import org.apache.ws.security.message.WSSecEncrypt;
import org.apache.ws.security.message.WSSecHeader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test that checks for correct WSDataRef which should be returned by 
 * <code>org.apache.ws.security.processor.EncryptedKeyProcessor</code> 
 * 
 * This test uses the RSA_15 algorithm to transport (wrap) the symmetric key.
 * The test case creates a ReferenceList element that references EncryptedData
 * elements. The ReferencesList element is put into the EncryptedKey. The 
 * EncryptedData elements contain a KeyInfo that references the EncryptedKey via 
 * a STR/Reference structure.
 * 
 * WSDataRef object must contain the correct QName of the decrypted element. 
 * 
 * 
 */
public class TestWSSecurityDataRef1 extends TestCase implements CallbackHandler {
    private static final Log LOG = LogFactory.getLog(TestWSSecurityDataRef1.class);
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
    private Crypto crypto = CryptoFactory.getInstance("wss40.properties");
    private MessageContext msgContext;
    private Message message;

    /**
     * TestWSSecurityDataRef constructor <p/>
     * 
     * @param name
     *            name of the test
     */
    public TestWSSecurityDataRef1(String name) {
        super(name);
    }

    /**
     * JUnit suite <p/>
     * 
     * @return a junit test suite
     */
    public static Test suite() {
        return new TestSuite( TestWSSecurityDataRef1.class);
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
     * Test that check for correct WSDataRef object from EncryptedKey Processor 
     * 
     * 
     * @throws Exception
     *             Thrown when there is an error in encryption or decryption
     */
    public void testDataRefEncryptedKeyProcessor() throws Exception {
      
        SOAPEnvelope unsignedEnvelope = message.getSOAPEnvelope();
        WSSecEncrypt builder = new WSSecEncrypt();
        builder.setUserInfo("wss40");
        builder.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);
        builder.setSymmetricEncAlgorithm(WSConstants.TRIPLE_DES);
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        LOG.info("Before Encryption Triple DES....");

        /*
         * Prepare the Encrypt object with the token, setup data structure
         */
        builder.prepare(doc, crypto);

        /*
         * Set up the parts structure to encrypt the body
         */
        Vector parts = new Vector();
        WSEncryptionPart encP = new WSEncryptionPart("testMethod", "uri:LogTestService2",
                "Element");
        parts.add(encP);

        /*
         * Encrypt the element (testMethod), create EncrypedData elements that reference
         * the EncryptedKey, and get a ReferenceList that can be put into the EncryptedKey
         * itself as a child.
         */
        Element refs = builder.encryptForExternalRef(null, parts);
        
        /*
         * We use this method because we want the reference list to be inside the 
         * EncryptedKey element
         */
        builder.addInternalRefElement(refs);

        /*
         * now add (prepend) the EncryptedKey element, then a
         * BinarySecurityToken if one was setup during prepare
         */
        builder.prependToHeader(secHeader);

        builder.prependBSTElementToHeader(secHeader);

        Document encryptedDoc = doc;
        LOG.info("After Encryption Triple DES....");

        checkDataRef(encryptedDoc);
    }

    /**
     * Verifies the soap envelope <p/>
     * 
     * @param envelope
     * @throws Exception
     *             Thrown when there is a problem in verification
     */
    private void checkDataRef(Document doc) throws Exception {
        
        // Retrieve the wsResults vector 
        Vector wsResults = secEngine.processSecurityHeader(doc, null, this, crypto);
        boolean found = false;
                
        for (int i = 0; i < wsResults.size(); i++) {
            
            WSSecurityEngineResult wsSecEngineResult = 
                (WSSecurityEngineResult)wsResults.get(i);           
            int action = ((java.lang.Integer) 
                wsSecEngineResult.get(WSSecurityEngineResult.TAG_ACTION)).intValue();
            
            // We want to filter only encryption results
            if (action != WSConstants.ENCR) {
                continue;
            }
            ArrayList dataRefs = (ArrayList)wsSecEngineResult
                .get(WSSecurityEngineResult.TAG_DATA_REF_URIS);
            
            //We want check only the DATA_REF_URIS 
            if (dataRefs != null && dataRefs.size() > 0) {
                for (int j = 0; j < dataRefs.size(); j++) {
                    Object obj = dataRefs.get(i);                            

                    // ReferenceList Processor must Return a WSDataRef objects
                    assertTrue(obj instanceof WSDataRef);

                    WSDataRef dataRef = (WSDataRef) obj;

                    // Check whether dataRef URI is set
                    assertNotNull(dataRef.getDataref());

                    // Check whether QName is correctly set
                    assertEquals("testMethod", dataRef.getName().getLocalPart());
                    assertEquals("uri:LogTestService2", dataRef.getName().getNamespaceURI());

                    // Check whether wsu:Id is set
                    assertNotNull(dataRef.getWsuId());
                    
                    // Check the encryption algorithm was set
                    assertEquals(WSConstants.TRIPLE_DES, dataRef.getAlgorithm());

                    // flag to indicate the element was found in TAG_DATA_REF_URIS
                    found = true;

                }
            }
        }
        
        // Make sure the element is actually found in the decrypted elements
        assertTrue(found);
        
    }

    public void handle(Callback[] callbacks) throws IOException,
            UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof WSPasswordCallback) {
                WSPasswordCallback pc = (WSPasswordCallback) callbacks[i];
                /*
                 * here call a function/method to lookup the password for the
                 * given identifier (e.g. a user name or keystore alias) e.g.:
                 * pc.setPassword(passStore.getPassword(pc.getIdentfifier)) for
                 * Testing we supply a fixed name here.
                 */
                pc.setPassword("security");
            } else {
                throw new UnsupportedCallbackException(callbacks[i],
                        "Unrecognized Callback");
            }
        }
    }
}
