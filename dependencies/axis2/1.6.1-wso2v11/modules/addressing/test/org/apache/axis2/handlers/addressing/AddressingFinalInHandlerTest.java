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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.RolePlayer;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AddressingFinalInHandlerTest extends AddressingInHandlerTestBase {

    private static Log log = LogFactory.getLog(AddressingFinalInHandlerTest.class);

    /** @param testName  */
    public AddressingFinalInHandlerTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        inHandler = new AddressingInHandler();
        addressingNamespace = AddressingConstants.Final.WSA_NAMESPACE;
        versionDirectory = "final";
        fromAddress = "http://www.w3.org/2005/08/addressing/anonymous";
        secondRelationshipType = "http://some.custom.relationship";
    }

    public void testExtractAddressingInformationFromHeaders() {
        try {
            Options options = extractAddressingInformationFromHeaders(null);

            assertNotNull(options);
            assertNotNull(options.getTo());

            Map allReferenceParameters = options.getTo().getAllReferenceParameters();
            assertNotNull(allReferenceParameters);
            QName qName = new QName("http://ws.apache.org/namespaces/axis2", "ParamOne", "axis2");
            assertNotNull(allReferenceParameters.get(qName));

            assertEPRHasCorrectMetadata(options.getFrom());
            assertEPRHasCorrectMetadata(options.getFaultTo());
            assertEPRHasCorrectMetadata(options.getReplyTo());

        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            fail(" An Exception has occured " + e.getMessage());
        }
    }

    public void testExtractAddressingInformationFromHeadersCustomRole() throws Exception {
        testFileName = "soapmessage.customrole.xml";
        Options options = extractAddressingInformationFromHeaders(new RolePlayer() {
            public List getRoles() {
                ArrayList al = new ArrayList();
                al.add("http://my/custom/role");
                return al;
            }

            public boolean isUltimateDestination() {
                return false;
            }
        });

        assertNotNull(options);
        assertNotNull(options.getTo());
        assertEPRHasCorrectMetadata(options.getFrom());
        assertEPRHasCorrectMetadata(options.getFaultTo());
        assertEPRHasCorrectMetadata(options.getReplyTo());

    }

    public void testExtractAddressingInformationFromHeadersCustomRoleSOAP12() throws Exception {
        testFileName = "soapmessage.customrole.soap12.xml";
        Options options = extractAddressingInformationFromHeaders(new RolePlayer() {
            public List getRoles() {
                ArrayList al = new ArrayList();
                al.add("http://my/custom/role");
                return al;
            }

            public boolean isUltimateDestination() {
                return false;
            }
        });

        assertNotNull(options);
        assertNotNull(options.getTo());
        assertEPRHasCorrectMetadata(options.getFrom());
        assertEPRHasCorrectMetadata(options.getFaultTo());
        assertEPRHasCorrectMetadata(options.getReplyTo());

    }

    public void testMessageWithOmittedAction() {
        try {
            testMessageWithOmittedHeaders("noAction");
            fail("An AxisFault should have been thrown due to a missing Action header.");
        }
        catch (AxisFault af) {
            //test passed
        }
        catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            fail(" An Exception has occured " + e.getMessage());
        }
    }

    public void testMessageWithOmittedFaultTo() {
        try {
            Options options = testMessageWithOmittedHeaders("noFaultTo");
            EndpointReference epr = options.getFaultTo();

            assertNull("The FaultTo endpoint reference is not null.", epr);
        }
        catch (AxisFault af) {
            af.printStackTrace();
            log.error(af.getMessage());
            fail("An unexpected AxisFault was thrown due to a missing FaultTo header.");
        }
        catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            fail(" An Exception has occured " + e.getMessage());
        }
    }

    public void testMessageWithOmittedFrom() {
        try {
            Options options = testMessageWithOmittedHeaders("noFrom");
            EndpointReference epr = options.getFrom();

            assertNull("The From endpoint reference is not null.", epr);
        }
        catch (AxisFault af) {
            af.printStackTrace();
            log.error(af.getMessage());
            fail("An unexpected AxisFault was thrown due to a missing From header.");
        }
        catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            fail(" An Exception has occured " + e.getMessage());
        }
    }

    public void testMessageWithOmittedMessageID() {
        try {
            Options options = testMessageWithOmittedHeaders("noMessageID");
            String messageID = options.getMessageId();

            assertNull("The message id is not null.", messageID);
        }
        catch (AxisFault af) {
            af.printStackTrace();
            log.error(af.getMessage());
            fail("An unexpected AxisFault was thrown due to a missing MessageID header.");
        }
        catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            fail(" An Exception has occured " + e.getMessage());
        }
    }

    public void testMessageWithOmittedReplyTo() {
        try {
            Options options = testMessageWithOmittedHeaders("noReplyTo");
            EndpointReference epr = options.getReplyTo();
            String address = epr.getAddress();

            assertEquals("The address of the ReplyTo endpoint reference is not the anonymous URI.",
                         AddressingConstants.Final.WSA_ANONYMOUS_URL, address);
        }
        catch (AxisFault af) {
            af.printStackTrace();
            log.error(af.getMessage());
            fail("An unexpected AxisFault was thrown due to a missing ReplyTo header.");
        }
        catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            fail(" An Exception has occured " + e.getMessage());
        }
    }

    public void testMessageWithOmittedTo() {
        try {
            Options options = testMessageWithOmittedHeaders("noTo");
            EndpointReference epr = options.getTo();
            String address = epr.getAddress();

            assertEquals("The address of the To endpoint reference is not the anonymous URI.",
                         AddressingConstants.Final.WSA_ANONYMOUS_URL, address);
        }
        catch (AxisFault af) {
            af.printStackTrace();
            log.error(af.getMessage());
            fail("An unexpected AxisFault was thrown due to a missing To header.");
        }
        catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            fail(" An Exception has occured " + e.getMessage());
        }
    }

    public void testDifferentSoapActionProcessing() throws Exception {
        String testfile = "valid-messages/" + versionDirectory + "/soapmessage.xml";
        MessageContext mc = new MessageContext();
        mc.setConfigurationContext(ConfigurationContextFactory.createEmptyConfigurationContext());
        mc.setServerSide(true);
        try {
            mc.setSoapAction("http://ws.apache.org/tests/differentAction");
            basicExtractAddressingInformationFromHeaders(testfile, mc);
            fail("An AxisFault should have been thrown due to the soapaction being different to the ws-a action.");
        }
        catch (AxisFault af) {
            //Test passed.
        }
    }

    public void testSameSoapAction() throws Exception {
        String testfile = "valid-messages/" + versionDirectory + "/soapmessage.xml";
        MessageContext mc = new MessageContext();
        mc.setConfigurationContext(ConfigurationContextFactory.createEmptyConfigurationContext());
        mc.setServerSide(true);
        try {
            mc.setSoapAction("http://ws.apache.org/tests/action");
            basicExtractAddressingInformationFromHeaders(testfile, mc);
        }
        catch (AxisFault af) {
            af.printStackTrace();
            log.error(af.getMessage());
            fail("An unexpected AxisFault was thrown while testing with a soapaction and ws-a action that are the same.");
        }
    }

    public void testEmptySoapAction() throws Exception {
        String testfile = "valid-messages/" + versionDirectory + "/soapmessage.xml";
        MessageContext mc = new MessageContext();
        mc.setConfigurationContext(ConfigurationContextFactory.createEmptyConfigurationContext());
        try {
            mc.setSoapAction("");
            basicExtractAddressingInformationFromHeaders(testfile, mc);
        }
        catch (AxisFault af) {
            af.printStackTrace();
            log.error(af.getMessage());
            fail("An unexpected AxisFault was thrown while testing with an empty soapaction.");
        }
    }

    public void testNullSoapAction() throws Exception {
        String testfile = "valid-messages/" + versionDirectory + "/soapmessage.xml";
        MessageContext mc = new MessageContext();
        mc.setConfigurationContext(ConfigurationContextFactory.createEmptyConfigurationContext());
        try {
            mc.setSoapAction(null);
            basicExtractAddressingInformationFromHeaders(testfile, mc);
        }
        catch (AxisFault af) {
            af.printStackTrace();
            log.error(af.getMessage());
            fail("An unexpected AxisFault was thrown while testing with a null soapaction.");
        }
    }

    private void assertEPRHasCorrectMetadata(EndpointReference epr) {
        ArrayList metadata = epr.getMetaData();
        if (metadata != null) {
            OMElement md = (OMElement)metadata.get(0);
            assertEquals(md.getQName(),
                         new QName("http://ws.apache.org/namespaces/axis2", "MetaExt"));
            assertEquals(md.getText(), "123456789");
            assertEquals(md.getAttributeValue(
                    new QName("http://ws.apache.org/namespaces/axis2", "AttrExt")), "123456789");
        } else {
            fail("No Metadata found in EPR");
        }
    }
}
