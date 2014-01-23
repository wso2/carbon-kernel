/*
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

package org.apache.axis2.handlers.addressing;

import junit.framework.TestCase;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.RolePlayer;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.util.TestUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public abstract class AddressingInHandlerTestBase extends TestCase {
    private static Log log = LogFactory.getLog(AddressingInHandlerTestBase.class);
    AddressingInHandler inHandler;
    String addressingNamespace;
    String testFileName = "soapmessage.xml";

    String versionDirectory;

    private String action = "http://ws.apache.org/tests/action";
    private String messageID = "uuid:920C5190-0B8F-11D9-8CED-F22EDEEBF7E5";
    String fromAddress;
    String secondRelationshipType;
    private String faultAddress = "http://example.com/fabrikam/fault";
    private String replyAddress = "http://example.com/fabrikam/acct";
    private String toAddress = "http://localhost:8081/axis/services/BankPort";

    /** @param testName  */
    public AddressingInHandlerTestBase(String testName) {
        super(testName);
    }

    protected void basicExtractAddressingInformationFromHeaders(String testMessagePath,
                                                                MessageContext mc)
            throws Exception {
        SOAPEnvelope envelope = TestUtil.getSOAPEnvelope(testMessagePath);
        mc.setEnvelope(envelope);
        inHandler.invoke(mc);
    }

    protected Options extractAddressingInformationFromHeaders(RolePlayer rolePlayer)
            throws Exception {
        String testfile = "valid-messages/" + versionDirectory + "/" + testFileName;

        MessageContext mc = new MessageContext();
        final ConfigurationContext context =
                ConfigurationContextFactory.createEmptyConfigurationContext();
        mc.setConfigurationContext(context);
        if (rolePlayer != null) {
            mc.getConfigurationContext().getAxisConfiguration()
                    .addParameter(Constants.SOAP_ROLE_PLAYER_PARAMETER, rolePlayer);
        }
        basicExtractAddressingInformationFromHeaders(testfile, mc);

        Options options = mc.getOptions();

        if (options == null) {
            fail("Addressing Information Headers have not been retrieved properly");
        }
        assertEquals("action header is not correct", action, options.getAction());
        assertActionHasExtensibilityAttribute(mc);
        assertEquals("message id header is not correct",
                     options.getMessageId().trim(),
                     messageID.trim());
        assertMessageIDHasExtensibilityAttribute(mc);

        assertFullFromEPR(options.getFrom());
        assertFullFaultEPR(options.getFaultTo());
        assertFullReplyToEPR(options.getReplyTo());

        assertRelationships(options);

        return options;
    }

    private void testExtractAddressingInformationFromHeadersInvalidCardinality(String headerName) {
        String testfile = "invalid-cardinality-messages/" + versionDirectory +
                          "/invalidCardinality" + headerName + "Message.xml";
        try {
            MessageContext mc = new MessageContext();
            mc.setConfigurationContext(
                    ConfigurationContextFactory.createEmptyConfigurationContext());
            try {
                basicExtractAddressingInformationFromHeaders(testfile, mc);
                fail("An AxisFault should have been thrown due to 2 wsa:" + headerName +
                     " headers.");
            } catch (AxisFault af) {
                if (headerName.equals(AddressingConstants.WSA_REPLY_TO)) {
                    assertNull("No ReplyTo should be set on the MessageContext", mc.getReplyTo());
                } else {
                    assertReplyToEPR(mc.getReplyTo());
                }

                if (headerName.equals(AddressingConstants.WSA_FAULT_TO)) {
                    assertNull("No FaultTo should be set on the MessageContext", mc.getFaultTo());
                } else {
                    assertFaultEPR(mc.getFaultTo());
                }

                if (headerName.equals(AddressingConstants.WSA_ACTION)) {
                    assertNull("No Action should be set on the MessageContext", mc.getWSAAction());
                } else {
                    assertEquals("WSAAction property is not correct", mc.getWSAAction(), action);
                }

                if (headerName.equals(AddressingConstants.WSA_MESSAGE_ID)) {
                    assertNull("No MessageID should be set on the MessageContext",
                               mc.getMessageID());
                } else {
                    assertEquals("MessageID property is not correct", mc.getMessageID().trim(),
                                 messageID.trim());
                }

                if (headerName.equals(AddressingConstants.WSA_FROM)) {
                    assertNull("No From should be set on the MessageContext", mc.getFrom());
                } else {
                    assertFromEPR(mc.getFrom());
                }

                if (headerName.equals(AddressingConstants.WSA_TO)) {
                    assertNull("No To should be set on the MessageContext", mc.getTo());
                } else {
                    assertToEPR(mc.getTo());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.info(e.getMessage());
            fail(" An Exception has occured " + e.getMessage());
        }
    }

    protected Options testMessageWithOmittedHeaders(String testName) throws Exception {
        String testfile =
                "omitted-header-messages/" + versionDirectory + "/" + testName + "Message.xml";

        MessageContext mc = new MessageContext();
        ConfigurationContext cc = ConfigurationContextFactory.createEmptyConfigurationContext();
        mc.setConfigurationContext(cc);
        basicExtractAddressingInformationFromHeaders(testfile, mc);

        return mc.getOptions();
    }

    public void testExtractAddressingInformationFromHeadersInvalidCardinalityReplyTo() {
        testExtractAddressingInformationFromHeadersInvalidCardinality(
                AddressingConstants.WSA_REPLY_TO);
    }

    public void testExtractAddressingInformationFromHeadersInvalidCardinalityFaultTo() {
        testExtractAddressingInformationFromHeadersInvalidCardinality(
                AddressingConstants.WSA_FAULT_TO);
    }

    public void testExtractAddressingInformationFromHeadersInvalidCardinalityAction() {
        testExtractAddressingInformationFromHeadersInvalidCardinality(
                AddressingConstants.WSA_ACTION);
    }

    public void testExtractAddressingInformationFromHeadersInvalidCardinalityMessageID() {
        testExtractAddressingInformationFromHeadersInvalidCardinality(
                AddressingConstants.WSA_MESSAGE_ID);
    }

    public void testExtractAddressingInformationFromHeadersInvalidCardinalityFrom() {
        testExtractAddressingInformationFromHeadersInvalidCardinality(AddressingConstants.WSA_FROM);
    }

    public void testExtractAddressingInformationFromHeadersInvalidCardinalityTo() {
        testExtractAddressingInformationFromHeadersInvalidCardinality(AddressingConstants.WSA_TO);
    }

    private void assertFromEPR(EndpointReference fromEPR) {
        assertEquals("Address in From EPR is not valid",
                     fromEPR.getAddress().trim(),
                     fromAddress.trim());
    }

    private void assertFaultEPR(EndpointReference faultEPR) {
        assertEquals("Address in FaultTo EPR is not valid",
                     faultEPR.getAddress().trim(),
                     faultAddress.trim());
    }

    private void assertReplyToEPR(EndpointReference replyEPR) {
        assertEquals("Address in ReplytTo EPR is not valid",
                     replyEPR.getAddress().trim(),
                     replyAddress.trim());
    }

    private void assertToEPR(EndpointReference toEPR) {
        System.out.println(toEPR);
        assertEquals("Address in To EPR is not valid",
                     toEPR.getAddress().trim(),
                     toAddress.trim());
        assertEPRAddressHasExtensibilityAttribute(toEPR);
    }

    private void assertFullFromEPR(EndpointReference fromEPR) {
        assertEquals("Address in From EPR is not valid",
                     fromEPR.getAddress().trim(),
                     fromAddress.trim());
        assertEPRHasExtensibilityAttribute(fromEPR);
        assertEPRHasCorrectReferenceParameters(fromEPR);
        assertEPRHasCorrectExtensibilityElements(fromEPR);
    }

    private void assertFullFaultEPR(EndpointReference faultEPR) {
        assertEquals("Address in FaultTo EPR is not valid",
                     faultEPR.getAddress().trim(),
                     faultAddress.trim());
        assertEPRHasExtensibilityAttribute(faultEPR);
        assertEPRHasCorrectReferenceParameters(faultEPR);
        assertEPRHasCorrectExtensibilityElements(faultEPR);
    }

    private void assertFullReplyToEPR(EndpointReference replyEPR) {
        assertEquals("Address in ReplytTo EPR is not valid",
                     replyEPR.getAddress().trim(),
                     replyAddress.trim());
        assertEPRHasExtensibilityAttribute(replyEPR);
        assertEPRHasCorrectReferenceParameters(replyEPR);
        assertEPRHasCorrectExtensibilityElements(replyEPR);
    }

    private void assertEPRHasCorrectReferenceParameters(EndpointReference epr) {
        //<wsa:ReferenceParameters>
        //  <fabrikam:CustomerKey>123456789</fabrikam:CustomerKey>
        //  <fabrikam:ShoppingCart>ABCDEFG</fabrikam:ShoppingCart>
        //</wsa:ReferenceParameters>
        Map referenceParameters = epr.getAllReferenceParameters();
        if (referenceParameters != null) {
            OMElement refparm1 = (OMElement)referenceParameters
                    .get(new QName("http://example.com/fabrikam", "CustomerKey"));
            assertNotNull(refparm1);
            assertEquals("ReferenceParameter value incorrect.", refparm1.getText(), "123456789");

            OMElement refparm2 = (OMElement)referenceParameters
                    .get(new QName("http://example.com/fabrikam", "ShoppingCart"));
            assertNotNull(refparm2);
            assertEquals("ReferenceParameter value incorrect.", refparm2.getText(), "ABCDEFG");
        } else {
            fail("No ReferenceParameters found in EPR");
        }
    }

    private void assertActionHasExtensibilityAttribute(MessageContext mc) {
        boolean attributeFound = false;
        ArrayList attributes = (ArrayList)mc.getProperty(AddressingConstants.ACTION_ATTRIBUTES);
        if (attributes != null) {
            Iterator iter = attributes.iterator();
            while (iter.hasNext()) {
                OMAttribute oa = (OMAttribute)iter.next();
                if (oa.getLocalName().equals("AttrExt")) {
                    attributeFound = true;
                    assertEquals("Attribute value incorrectly deserialised", oa.getAttributeValue(),
                                 "123456789");
                }
            }
        }
        assertTrue("Extensibility attribute not found on Action", attributeFound);
    }

    private void assertMessageIDHasExtensibilityAttribute(MessageContext mc) {
        boolean attributeFound = false;
        ArrayList attributes = (ArrayList)mc.getProperty(AddressingConstants.MESSAGEID_ATTRIBUTES);
        if (attributes != null) {
            Iterator iter = attributes.iterator();
            while (iter.hasNext()) {
                OMAttribute oa = (OMAttribute)iter.next();
                if (oa.getLocalName().equals("AttrExt")) {
                    attributeFound = true;
                    assertEquals("Attribute value incorrectly deserialised", oa.getAttributeValue(),
                                 "123456789");
                }
            }
        }
        assertTrue("Extensibility attribute not found on MessageID", attributeFound);
    }

    private void assertRelatesToHasExtensibilityAttribute(RelatesTo rt) {
        boolean attributeFound = false;
        ArrayList attributes = rt.getExtensibilityAttributes();
        if (attributes != null) {
            Iterator iter = attributes.iterator();
            while (iter.hasNext()) {
                OMAttribute oa = (OMAttribute)iter.next();
                if (oa.getLocalName().equals("AttrExt")) {
                    attributeFound = true;
                    assertEquals("Attribute value incorrectly deserialised", oa.getAttributeValue(),
                                 "123456789");
                }
            }
        }
        assertTrue("Extensibility attribute not found on RelatesTo", attributeFound);
    }

    private void assertEPRAddressHasExtensibilityAttribute(EndpointReference epr) {
        boolean attributeFound = false;
        ArrayList attributes = epr.getAddressAttributes();
        if (attributes != null) {
            Iterator iter = attributes.iterator();
            while (iter.hasNext()) {
                OMAttribute oa = (OMAttribute)iter.next();
                if (oa.getLocalName().equals("AttrExt")) {
                    attributeFound = true;
                    assertEquals("Attribute value incorrectly deserialised", oa.getAttributeValue(),
                                 "123456789");
                }
            }
        }
        assertTrue("Extensibility attribute not found on EPR Address", attributeFound);
    }

    private void assertEPRHasExtensibilityAttribute(EndpointReference epr) {
        boolean attributeFound = false;
        ArrayList attributes = epr.getAttributes();
        if (attributes != null) {
            Iterator iter = attributes.iterator();
            while (iter.hasNext()) {
                OMAttribute oa = (OMAttribute)iter.next();
                if (oa.getLocalName().equals("AttrExt")) {
                    attributeFound = true;
                    assertEquals("Attribute value incorrectly deserialised", oa.getAttributeValue(),
                                 "123456789");
                }
            }
        }
        assertTrue("Extensibility attribute not found on EPR", attributeFound);
    }

    private void assertEPRHasCorrectExtensibilityElements(EndpointReference epr) {
        ArrayList eelements = epr.getExtensibleElements();
        if (eelements != null) {
            OMElement ee = (OMElement)eelements.get(0);
            assertEquals(ee.getQName(),
                         new QName("http://ws.apache.org/namespaces/axis2", "EPRExt"));
            assertEquals(ee.getText(), "123456789");
            assertEquals(ee.getAttributeValue(
                    new QName("http://ws.apache.org/namespaces/axis2", "AttrExt")), "123456789");
        } else {
            fail("No Extensibility Elements found in EPR");
        }
    }

    private void assertRelationships(Options options) {
        assertNotNull(options.getRelatesTo());
        assertRelatesToHasExtensibilityAttribute(options.getRelatesTo());
        assertEquals(options.getRelatesTo().getValue(), "http://some.previous.message");
        assertEquals(options.getRelatesTo(secondRelationshipType).getValue(),
                     "http://identifier.of.other.message/");
    }
}
