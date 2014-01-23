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
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.impl.OMNamespaceImpl;
import org.apache.axiom.om.impl.llom.util.XMLComparator;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.handlers.util.TestUtil;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;

import javax.xml.namespace.QName;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AddressingOutHandlerTest extends XMLTestCase implements AddressingConstants {
    private AddressingOutHandler outHandler;
    private MessageContext msgCtxt;

    public AddressingOutHandlerTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        outHandler = new AddressingOutHandler();

    }

    public void testAddToSOAPHeader() throws Exception {
        EndpointReference replyTo = new EndpointReference(
                "http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous");
        EndpointReference epr = new EndpointReference("http://www.to.org/service/");

        for (int i = 0; i < 5; i++) {
            epr.addReferenceParameter(
                    new QName(Submission.WSA_NAMESPACE, "Reference" + i,
                              AddressingConstants.WSA_DEFAULT_PREFIX),
                    "Value " + i * 100);

        }


        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope defaultEnvelope = factory.getDefaultEnvelope();

        ConfigurationContext configCtx =
                ConfigurationContextFactory.createEmptyConfigurationContext();
        MessageContext msgCtxt = configCtx.createMessageContext();
        msgCtxt.setProperty(WS_ADDRESSING_VERSION, Submission.WSA_NAMESPACE);
        msgCtxt.setTo(epr);
        msgCtxt.setReplyTo(replyTo);
        msgCtxt.setEnvelope(defaultEnvelope);
        msgCtxt.setWSAAction("http://www.actions.org/action");
        msgCtxt.setMessageID("urn:test:123");

        OMAttribute extAttr = OMAbstractFactory.getOMFactory().createOMAttribute("AttrExt",
                                                                                 OMAbstractFactory
                                                                                         .getOMFactory().createOMNamespace(
                                                                                         "http://ws.apache.org/namespaces/axis2",
                                                                                         "axis2"),
                                                                                 "123456789");
        ArrayList al = new ArrayList();
        al.add(extAttr);

        msgCtxt.setProperty(AddressingConstants.ACTION_ATTRIBUTES, al);
        msgCtxt.setProperty(AddressingConstants.MESSAGEID_ATTRIBUTES, al);

        outHandler.invoke(msgCtxt);

        OMXMLParserWrapper omBuilder = TestUtil.getOMBuilder("eprTest.xml");

        XMLUnit.setIgnoreWhitespace(true);
        assertXMLEqual(omBuilder.getDocumentElement().toString(), defaultEnvelope.toString());

    }

    public void testHeaderCreationFromMsgCtxtInformation() throws Exception {
        ConfigurationContext cfgCtx =
                ConfigurationContextFactory.createEmptyConfigurationContext();
        msgCtxt = cfgCtx.createMessageContext();

        EndpointReference epr = new EndpointReference("http://www.from.org/service/");
        epr.addReferenceParameter(new QName("Reference2"),
                                  "Value 200");
        msgCtxt.setFrom(epr);

        epr = new EndpointReference("http://www.to.org/service/");
        epr.addReferenceParameter(
                new QName("http://reference.org", "Reference4", "myRef"),
                "Value 400");
        epr.addReferenceParameter(
                new QName("http://reference.org", "Reference3", "myRef"),
                "Value 300");

        msgCtxt.setTo(epr);
        msgCtxt.setProperty(WS_ADDRESSING_VERSION, Submission.WSA_NAMESPACE);

        epr = new EndpointReference("http://www.replyTo.org/service/");
        msgCtxt.setReplyTo(epr);

        msgCtxt.setMessageID("123456-7890");
        msgCtxt.setWSAAction("http://www.actions.org/action");

        org.apache.axis2.addressing.RelatesTo relatesTo = new org.apache.axis2.addressing.RelatesTo(
                "http://www.relatesTo.org/service/", "TestRelation");
        msgCtxt.addRelatesTo(relatesTo);

        msgCtxt.setEnvelope(
                OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope());
        outHandler.invoke(msgCtxt);

        XMLUnit.setIgnoreWhitespace(true);
        assertXMLEqual(msgCtxt.getEnvelope().toString(),
                TestUtil.getOMBuilder("OutHandlerTest.xml")
                        .getDocumentElement().toString());
    }

    public void testMustUnderstandSupport() throws Exception {
        ConfigurationContext cfgCtx =
                ConfigurationContextFactory.createEmptyConfigurationContext();
        msgCtxt = cfgCtx.createMessageContext();

        msgCtxt.setProperty(AddressingConstants.ADD_MUST_UNDERSTAND_TO_ADDRESSING_HEADERS,
                            Boolean.TRUE);

        EndpointReference epr = new EndpointReference("http://www.from.org/service/");
        epr.addReferenceParameter(new QName("Reference2"),
                                  "Value 200");
        msgCtxt.setFrom(epr);

        epr = new EndpointReference("http://www.to.org/service/");
        epr.addReferenceParameter(
                new QName("http://reference.org", "Reference4", "myRef"),
                "Value 400");
        epr.addReferenceParameter(
                new QName("http://reference.org", "Reference3", "myRef"),
                "Value 300");

        msgCtxt.setTo(epr);
        msgCtxt.setProperty(WS_ADDRESSING_VERSION, Submission.WSA_NAMESPACE);

        epr = new EndpointReference("http://www.replyTo.org/service/");
        msgCtxt.setReplyTo(epr);

        msgCtxt.setMessageID("123456-7890");
        msgCtxt.setWSAAction("http://www.actions.org/action");

        org.apache.axis2.addressing.RelatesTo relatesTo = new org.apache.axis2.addressing.RelatesTo(
                "http://www.relatesTo.org/service/", "TestRelation");
        msgCtxt.addRelatesTo(relatesTo);

        msgCtxt.setEnvelope(
                OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope());
        outHandler.invoke(msgCtxt);

        XMLUnit.setIgnoreWhitespace(true);
        assertXMLEqual(msgCtxt.getEnvelope().toString(),
                TestUtil.getOMBuilder("mustUnderstandTest.xml")
                        .getDocumentElement().toString());
    }

    public void testSOAPRoleSupport() throws Exception {
        ConfigurationContext cfgCtx =
                ConfigurationContextFactory.createEmptyConfigurationContext();
        msgCtxt = cfgCtx.createMessageContext();

        msgCtxt.setProperty(AddressingConstants.SOAP_ROLE_FOR_ADDRESSING_HEADERS,
                            "urn:test:role");

        EndpointReference epr = new EndpointReference("http://www.from.org/service/");
        epr.addReferenceParameter(new QName("Reference2"),
                                  "Value 200");
        msgCtxt.setFrom(epr);

        epr = new EndpointReference("http://www.to.org/service/");
        epr.addReferenceParameter(
                new QName("http://reference.org", "Reference4", "myRef"),
                "Value 400");
        epr.addReferenceParameter(
                new QName("http://reference.org", "Reference3", "myRef"),
                "Value 300");

        msgCtxt.setTo(epr);
        msgCtxt.setProperty(WS_ADDRESSING_VERSION, Submission.WSA_NAMESPACE);

        epr = new EndpointReference("http://www.replyTo.org/service/");
        msgCtxt.setReplyTo(epr);

        msgCtxt.setMessageID("123456-7890");
        msgCtxt.setWSAAction("http://www.actions.org/action");

        org.apache.axis2.addressing.RelatesTo relatesTo = new org.apache.axis2.addressing.RelatesTo(
                "http://www.relatesTo.org/service/", "TestRelation");
        msgCtxt.addRelatesTo(relatesTo);

        msgCtxt.setEnvelope(
                OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope());
        outHandler.invoke(msgCtxt);

        XMLUnit.setIgnoreWhitespace(true);
        assertXMLEqual(msgCtxt.getEnvelope().toString(),
                TestUtil.getOMBuilder("soap11roleTest.xml").getDocumentElement().toString());
    }

    public void testSOAP12RoleSupport() throws Exception {
        ConfigurationContext cfgCtx =
                ConfigurationContextFactory.createEmptyConfigurationContext();
        msgCtxt = cfgCtx.createMessageContext();

        msgCtxt.setProperty(AddressingConstants.SOAP_ROLE_FOR_ADDRESSING_HEADERS,
                            "urn:test:role");

        EndpointReference epr = new EndpointReference("http://www.from.org/service/");
        epr.addReferenceParameter(new QName("Reference2"),
                                  "Value 200");
        msgCtxt.setFrom(epr);

        epr = new EndpointReference("http://www.to.org/service/");
        epr.addReferenceParameter(
                new QName("http://reference.org", "Reference4", "myRef"),
                "Value 400");
        epr.addReferenceParameter(
                new QName("http://reference.org", "Reference3", "myRef"),
                "Value 300");

        msgCtxt.setTo(epr);
        msgCtxt.setProperty(WS_ADDRESSING_VERSION, Submission.WSA_NAMESPACE);

        epr = new EndpointReference("http://www.replyTo.org/service/");
        msgCtxt.setReplyTo(epr);

        msgCtxt.setMessageID("123456-7890");
        msgCtxt.setWSAAction("http://www.actions.org/action");

        org.apache.axis2.addressing.RelatesTo relatesTo = new org.apache.axis2.addressing.RelatesTo(
                "http://www.relatesTo.org/service/", "TestRelation");
        msgCtxt.addRelatesTo(relatesTo);

        msgCtxt.setEnvelope(
                OMAbstractFactory.getSOAP12Factory().getDefaultEnvelope());
        outHandler.invoke(msgCtxt);

        XMLUnit.setIgnoreWhitespace(true);
        assertXMLEqual(msgCtxt.getEnvelope().toString(),
                TestUtil.getOMBuilder("soap12roleTest.xml")
                        .getDocumentElement().toString());
    }

    public void testDuplicateHeaders() throws Exception {

        // this will check whether we can add to epr, if there is one already.
        EndpointReference eprOne = new EndpointReference("http://whatever.org");
        EndpointReference duplicateEpr = new EndpointReference("http://whatever.duplicate.org");
        RelatesTo reply = new RelatesTo("urn:id");
        ConfigurationContext cfgCtx =
                ConfigurationContextFactory.createEmptyConfigurationContext();
        msgCtxt = cfgCtx.createMessageContext();
        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope defaultEnvelope = factory.getDefaultEnvelope();
        msgCtxt.setEnvelope(defaultEnvelope);

        msgCtxt.addRelatesTo(reply);
        msgCtxt.setTo(eprOne);
        msgCtxt.setWSAAction("http://www.actions.org/action");
        outHandler.invoke(msgCtxt);

        // now the soap message within the msgCtxt must have a to header.
        // lets invoke twice and see
        msgCtxt.setTo(duplicateEpr);
        outHandler.invoke(msgCtxt);

        assertEquals("http://whatever.org", defaultEnvelope.getHeader()
                .getFirstChildWithName(Final.QNAME_WSA_TO).getText());
        Iterator iterator =
                defaultEnvelope.getHeader().getChildrenWithName(Final.QNAME_WSA_RELATES_TO);
        int i = 0;
        while (iterator.hasNext()) {
            iterator.next();
            i++;
        }
        assertEquals("Reply should be added twice.", 2, i);
    }

    public void testDuplicateHeadersWithOverridingOn() throws Exception {

        // this will check whether we can add to epr, if there is one already.
        EndpointReference eprOne = new EndpointReference("http://whatever.org");
        RelatesTo custom = new RelatesTo("urn:id", "customRelationship");
        ConfigurationContext cfgCtx =
                ConfigurationContextFactory.createEmptyConfigurationContext();
        msgCtxt = cfgCtx.createMessageContext();
        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope defaultEnvelope = factory.getDefaultEnvelope();
        OMNamespace addressingNamespace =
                factory.createOMNamespace(Final.WSA_NAMESPACE, WSA_DEFAULT_PREFIX);
        SOAPHeaderBlock soapHeaderBlock =
                defaultEnvelope.getHeader().addHeaderBlock(WSA_TO, addressingNamespace);
        soapHeaderBlock.setText("http://oldEPR.org");
        soapHeaderBlock =
                defaultEnvelope.getHeader().addHeaderBlock(WSA_RELATES_TO, addressingNamespace);
        soapHeaderBlock.setText("urn:id");
        soapHeaderBlock =
                defaultEnvelope.getHeader().addHeaderBlock(WSA_RELATES_TO, addressingNamespace);
        soapHeaderBlock.setText("urn:id");
        soapHeaderBlock
                .addAttribute(WSA_RELATES_TO_RELATIONSHIP_TYPE, custom.getRelationshipType(), null);
        msgCtxt.setEnvelope(defaultEnvelope);

        msgCtxt.setProperty(REPLACE_ADDRESSING_HEADERS, Boolean.TRUE);
        msgCtxt.addRelatesTo(custom);
        msgCtxt.setTo(eprOne);
        msgCtxt.setWSAAction("http://www.actions.org/action");
        outHandler.invoke(msgCtxt);

        assertEquals("http://whatever.org", defaultEnvelope.getHeader()
                .getFirstChildWithName(Final.QNAME_WSA_TO).getText());
        Iterator iterator =
                defaultEnvelope.getHeader().getChildrenWithName(Final.QNAME_WSA_RELATES_TO);
        int i = 0;
        while (iterator.hasNext()) {
            iterator.next();
            i++;
        }
        assertEquals("Custom should replace reply.", 1, i);
    }

    public void testDuplicateHeadersWithOverridingOff() throws Exception {

        // this will check whether we can add to epr, if there is one already.
        EndpointReference eprOne = new EndpointReference("http://whatever.org");
        RelatesTo custom = new RelatesTo("urn:id", "customRelationship");
        ConfigurationContext cfgCtx =
                ConfigurationContextFactory.createEmptyConfigurationContext();
        msgCtxt = cfgCtx.createMessageContext();
        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope defaultEnvelope = factory.getDefaultEnvelope();
        OMNamespace addressingNamespace =
                factory.createOMNamespace(Final.WSA_NAMESPACE, WSA_DEFAULT_PREFIX);
        SOAPHeaderBlock soapHeaderBlock =
                defaultEnvelope.getHeader().addHeaderBlock(WSA_TO, addressingNamespace);
        soapHeaderBlock.setText("http://oldEPR.org");
        soapHeaderBlock =
                defaultEnvelope.getHeader().addHeaderBlock(WSA_RELATES_TO, addressingNamespace);
        soapHeaderBlock.setText("urn:id");
        msgCtxt.setEnvelope(defaultEnvelope);

        msgCtxt.setProperty(REPLACE_ADDRESSING_HEADERS, Boolean.FALSE);
        msgCtxt.addRelatesTo(custom);
        msgCtxt.setTo(eprOne);
        msgCtxt.setWSAAction("http://www.actions.org/action");
        outHandler.invoke(msgCtxt);

        assertEquals("http://oldEPR.org", defaultEnvelope.getHeader()
                .getFirstChildWithName(Final.QNAME_WSA_TO).getText());
        Iterator iterator =
                defaultEnvelope.getHeader().getChildrenWithName(Final.QNAME_WSA_RELATES_TO);
        int i = 0;
        while (iterator.hasNext()) {
            iterator.next();
            i++;
        }
        assertEquals("Both reply and custom should be found.", 2, i);
    }
    
    public void testAxis2DisableAddressingForOutMessagesTrue() throws Exception {
        File configFile = new File(System.getProperty("basedir",".") +
                "/test-resources/axis2-disableAddressingForOutMessagesTrue.xml");
        ConfigurationContext cfgCtx = ConfigurationContextFactory
        .createConfigurationContextFromFileSystem("target/test-classes",
                configFile.getAbsolutePath());
        
        msgCtxt = cfgCtx.createMessageContext();
        
        // Need to add a SOAP Header to stop this error from XMLComparator:
        // "There is no Header element under Envelope"
        SOAPEnvelope envelope = OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();
        SOAPHeaderBlock soapHeaderBlock = envelope.getHeader().addHeaderBlock(
                "testHeader", new OMNamespaceImpl("http://test.com", "test"));
        msgCtxt.setEnvelope(envelope);
        
        outHandler.invoke(msgCtxt);

        XMLUnit.setIgnoreWhitespace(true);
        assertXMLEqual(msgCtxt.getEnvelope().toString(), TestUtil
                .getOMBuilder("addressingDisabledTest.xml")
                .getDocumentElement().toString());   
    }
    
    public void testAxis2DisableAddressingForOutMessagesFalse() throws Exception {
        File configFile = new File(System.getProperty("basedir",".") +
                "/test-resources/axis2-disableAddressingForOutMessagesFalse.xml");
        ConfigurationContext cfgCtx = ConfigurationContextFactory
        .createConfigurationContextFromFileSystem("target/test-classes",
                configFile.getAbsolutePath());
        
        msgCtxt = cfgCtx.createMessageContext();
        msgCtxt.setEnvelope(OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope());
        msgCtxt.setTo(new EndpointReference("http://www.to.org/service/"));
        msgCtxt.setFrom(new EndpointReference("http://www.from.org/service/"));
        msgCtxt.setReplyTo(new EndpointReference("http://www.replyTo.org/service/"));
        msgCtxt.setFaultTo(new EndpointReference("http://www.faultTo.org/service/"));
        msgCtxt.setWSAAction("http://www.actions.org/action");
        msgCtxt.setMessageID("123456-7890");
        msgCtxt.addRelatesTo(new RelatesTo("http://www.relatesTo.org/service/"));
        msgCtxt.setProperty(WS_ADDRESSING_VERSION, Final.WSA_NAMESPACE);
        
        outHandler.invoke(msgCtxt);

        XMLUnit.setIgnoreWhitespace(true);
        assertXMLEqual(msgCtxt.getEnvelope().toString(), TestUtil
                .getOMBuilder("addressingEnabledTest.xml")
                .getDocumentElement().toString());   
    }
    
    public void testAxis2IncludeOptionalHeadersTrue() throws Exception {
        File configFile = new File(System.getProperty("basedir",".") +
                "/test-resources/axis2-IncludeOptionalHeadersTrue.xml");
        ConfigurationContext cfgCtx = ConfigurationContextFactory
        .createConfigurationContextFromFileSystem("target/test-classes",
                configFile.getAbsolutePath());
        
        msgCtxt = cfgCtx.createMessageContext();
        msgCtxt.setEnvelope(OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope());
        msgCtxt.setTo(new EndpointReference("http://www.to.org/service/"));
        msgCtxt.setFrom(new EndpointReference("http://www.from.org/service/"));
        msgCtxt.setReplyTo(new EndpointReference("http://www.replyTo.org/service/"));
        msgCtxt.setFaultTo(new EndpointReference("http://www.faultTo.org/service/"));
        msgCtxt.setWSAAction("http://www.actions.org/action");
        msgCtxt.setMessageID("123456-7890");
        msgCtxt.addRelatesTo(new RelatesTo("http://www.relatesTo.org/service/"));
        msgCtxt.setProperty(WS_ADDRESSING_VERSION, Final.WSA_NAMESPACE);
        
        outHandler.invoke(msgCtxt);

        XMLUnit.setIgnoreWhitespace(true);
        assertXMLEqual(msgCtxt.getEnvelope().toString(), TestUtil
                .getOMBuilder("withOptionalHeadersTest.xml")
                .getDocumentElement().toString());   
    }
    
    public void testAxis2IncludeOptionalHeadersFalse() throws Exception {
        File configFile = new File(System.getProperty("basedir",".") +
                "/test-resources/axis2-IncludeOptionalHeadersFalse.xml");
        ConfigurationContext cfgCtx = ConfigurationContextFactory
        .createConfigurationContextFromFileSystem("target/test-classes",
                configFile.getAbsolutePath());
        
        msgCtxt = cfgCtx.createMessageContext();
        msgCtxt.setEnvelope(OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope());
        msgCtxt.setTo(new EndpointReference("http://www.to.org/service/"));
        msgCtxt.setFrom(new EndpointReference("http://www.from.org/service/"));
        msgCtxt.setReplyTo(new EndpointReference("http://www.replyTo.org/service/"));
        msgCtxt.setFaultTo(new EndpointReference("http://www.faultTo.org/service/"));
        msgCtxt.setWSAAction("http://www.actions.org/action");
        msgCtxt.setMessageID("123456-7890");
        msgCtxt.addRelatesTo(new RelatesTo("http://www.relatesTo.org/service/"));
        msgCtxt.setProperty(WS_ADDRESSING_VERSION, Final.WSA_NAMESPACE);
        
        outHandler.invoke(msgCtxt);

        XMLUnit.setIgnoreWhitespace(true);
        assertXMLEqual(msgCtxt.getEnvelope().toString(), TestUtil
                .getOMBuilder("addressingEnabledTest.xml")
                .getDocumentElement().toString());   
    }
    
    public void testModuleDisableAddressingForOutMessagesTrue() throws Exception {
        File configFile = new File(System.getProperty("basedir",".") + 
                "/test-resources/axis2-noParameters.xml");
        ConfigurationContext cfgCtx = ConfigurationContextFactory
        .createConfigurationContextFromFileSystem("target/test-classes",
                configFile.getAbsolutePath());
        AxisConfiguration config = cfgCtx.getAxisConfiguration();
        
        // Can't test with a module.xml file in test-resources because it gets
        // overridden by target\classes\META-INF\module.xml, so create our own
        // AxisModule with the required parameter value
        AxisModule module = config.getModule("addressing");
        module.addParameter(new Parameter("disableAddressingForOutMessages", "true"));
        
        msgCtxt = cfgCtx.createMessageContext();
        
        // Need to add a SOAP Header to stop this error from XMLComparator:
        // "There is no Header element under Envelope"
        SOAPEnvelope envelope = OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();
        SOAPHeaderBlock soapHeaderBlock = envelope.getHeader().addHeaderBlock(
                "testHeader", new OMNamespaceImpl("http://test.com", "test"));
        msgCtxt.setEnvelope(envelope);
        
        outHandler.invoke(msgCtxt);

        XMLUnit.setIgnoreWhitespace(true);
        assertXMLEqual(msgCtxt.getEnvelope().toString(), TestUtil
                .getOMBuilder("addressingDisabledTest.xml")
                .getDocumentElement().toString());
    }
    
    public void testModuleDisableAddressingForOutMessagesFalse() throws Exception {
        File configFile = new File(System.getProperty("basedir",".") + 
                "/test-resources/axis2-noParameters.xml");
        ConfigurationContext cfgCtx = ConfigurationContextFactory
        .createConfigurationContextFromFileSystem("target/test-classes",
                configFile.getAbsolutePath());
        AxisConfiguration config = cfgCtx.getAxisConfiguration();

        // Can't test with a module.xml file in test-resources because it gets
        // overridden by target\classes\META-INF\module.xml, so create our own
        // AxisModule with the required parameter value
        AxisModule module = config.getModule("addressing");
        module.addParameter(new Parameter("disableAddressingForOutMessages", "false"));
        
        msgCtxt = cfgCtx.createMessageContext();
        msgCtxt.setEnvelope(OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope());
        msgCtxt.setTo(new EndpointReference("http://www.to.org/service/"));
        msgCtxt.setFrom(new EndpointReference("http://www.from.org/service/"));
        msgCtxt.setReplyTo(new EndpointReference("http://www.replyTo.org/service/"));
        msgCtxt.setFaultTo(new EndpointReference("http://www.faultTo.org/service/"));
        msgCtxt.setWSAAction("http://www.actions.org/action");
        msgCtxt.setMessageID("123456-7890");
        msgCtxt.addRelatesTo(new RelatesTo("http://www.relatesTo.org/service/"));
        msgCtxt.setProperty(WS_ADDRESSING_VERSION, Final.WSA_NAMESPACE);
        
        outHandler.invoke(msgCtxt);

        XMLUnit.setIgnoreWhitespace(true);
        assertXMLEqual(msgCtxt.getEnvelope().toString(), TestUtil
                .getOMBuilder("addressingEnabledTest.xml")
                .getDocumentElement().toString());   
    }
    
    public void testModuleIncludeOptionalHeadersTrue() throws Exception {
        File configFile = new File(System.getProperty("basedir",".") + 
                "/test-resources/axis2-noParameters.xml");
        ConfigurationContext cfgCtx = ConfigurationContextFactory
        .createConfigurationContextFromFileSystem("target/test-classes",
                configFile.getAbsolutePath());
        AxisConfiguration config = cfgCtx.getAxisConfiguration();

        // Can't test with a module.xml file in test-resources because it gets
        // overridden by target\classes\META-INF\module.xml, so create our own
        // AxisModule with the required parameter value
        AxisModule module = config.getModule("addressing");
        module.addParameter(new Parameter("includeOptionalHeaders", "true"));

        msgCtxt = cfgCtx.createMessageContext();
        msgCtxt.setEnvelope(OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope());
        msgCtxt.setTo(new EndpointReference("http://www.to.org/service/"));
        msgCtxt.setFrom(new EndpointReference("http://www.from.org/service/"));
        msgCtxt.setReplyTo(new EndpointReference("http://www.replyTo.org/service/"));
        msgCtxt.setFaultTo(new EndpointReference("http://www.faultTo.org/service/"));
        msgCtxt.setWSAAction("http://www.actions.org/action");
        msgCtxt.setMessageID("123456-7890");
        msgCtxt.addRelatesTo(new RelatesTo("http://www.relatesTo.org/service/"));
        msgCtxt.setProperty(WS_ADDRESSING_VERSION, Final.WSA_NAMESPACE);
        
        outHandler.invoke(msgCtxt);

        XMLUnit.setIgnoreWhitespace(true);
        assertXMLEqual(msgCtxt.getEnvelope().toString(), TestUtil
                .getOMBuilder("withOptionalHeadersTest.xml")
                .getDocumentElement().toString());   
    }
    
    public void testModuleIncludeOptionalHeadersFalse() throws Exception {
        File configFile = new File(System.getProperty("basedir",".") + 
                "/test-resources/axis2-noParameters.xml");
        ConfigurationContext cfgCtx = ConfigurationContextFactory
        .createConfigurationContextFromFileSystem("target/test-classes",
                configFile.getAbsolutePath());
        AxisConfiguration config = cfgCtx.getAxisConfiguration();

        // Can't test with a module.xml file in test-resources because it gets
        // overridden by target\classes\META-INF\module.xml, so create our own
        // AxisModule with the required parameter value
        AxisModule module = config.getModule("addressing");
        module.addParameter(new Parameter("includeOptionalHeaders", "false"));
        
        msgCtxt = cfgCtx.createMessageContext();
        msgCtxt.setEnvelope(OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope());
        msgCtxt.setTo(new EndpointReference("http://www.to.org/service/"));
        msgCtxt.setFrom(new EndpointReference("http://www.from.org/service/"));
        msgCtxt.setReplyTo(new EndpointReference("http://www.replyTo.org/service/"));
        msgCtxt.setFaultTo(new EndpointReference("http://www.faultTo.org/service/"));
        msgCtxt.setWSAAction("http://www.actions.org/action");
        msgCtxt.setMessageID("123456-7890");
        msgCtxt.addRelatesTo(new RelatesTo("http://www.relatesTo.org/service/"));
        msgCtxt.setProperty(WS_ADDRESSING_VERSION, Final.WSA_NAMESPACE);
        
        outHandler.invoke(msgCtxt);

        XMLUnit.setIgnoreWhitespace(true);
        assertXMLEqual(msgCtxt.getEnvelope().toString(), TestUtil
                .getOMBuilder("addressingEnabledTest.xml")
                .getDocumentElement().toString());   
    }
}
