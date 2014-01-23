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

package org.apache.axis2.builder;

import java.io.File;
import java.io.FileInputStream;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMSourcedElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AbstractTestCase;
import org.apache.axis2.Constants;
import org.apache.axis2.builder.unknowncontent.UnknownContentOMDataSource;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.TransportUtils;


public class UnknownContentBuilderTest extends AbstractTestCase{
    public UnknownContentBuilderTest(String testName) {
		super(testName);
	}

	public void testMultipart() throws Exception {
//        File file = getTestResourceFile("mime_message.txt");
//        FileInputStream fis = new FileInputStream(file);
//        ConfigurationContext configContext = ConfigurationContextFactory.createDefaultConfigurationContext();
//        AxisConfiguration axisConfig = configContext.getAxisConfiguration();
//        MessageContext mc = new MessageContext();
//        mc.setConfigurationContext(configContext);
//        axisConfig.addMessageBuilder("multipart/related", new MIMEBuilder());
//        axisConfig.addMessageBuilder("multipart/mixed", new MIMEBuilder());
//        axisConfig.addMessageBuilder("application/soap+xml", new SOAPBuilder());
//        axisConfig.addMessageBuilder("text/xml", new SOAPBuilder());
//        axisConfig.addMessageBuilder("application/xop+xml", new MTOMBuilder());
//        axisConfig.addMessageBuilder("application/xml", new ApplicationXMLBuilder());
//        axisConfig.addMessageBuilder("application/x-www-form-urlencoded",
//                                     new XFormURLEncodedBuilder());
//        axisConfig.addParameter(Constants.Configuration.USE_DEFAULT_FALLBACK_BUILDER, "true");
//        OMElement envelope = TransportUtils.createSOAPMessage(mc,fis,"multipart/mixed;boundary=--MIMEBoundary258DE2D105298B756D");
//        
//        assertTrue(envelope != null);
//        assertTrue(envelope instanceof SOAPEnvelope);
//        envelope.buildWithAttachments();
//        mc.attachments.getAllContentIDs();
//        int mimePartCount = mc.getAttachmentMap().getMap().size();
//        assertEquals(2, mimePartCount);
    }
	
	/**
	 * A builder is not added for the multipart/mixed as in the above test.
	 * @throws Exception
	 */
	public void testNoBuilder() throws Exception {
        File file = getTestResourceFile("mime_message.txt");
        MIMEBuilder mimeBuilder = new MIMEBuilder();
        FileInputStream fis = new FileInputStream(file);
        ConfigurationContext configContext = ConfigurationContextFactory.createDefaultConfigurationContext();
        AxisConfiguration axisConfig = configContext.getAxisConfiguration();
        MessageContext mc = new MessageContext();
        mc.setConfigurationContext(configContext);
        axisConfig.addMessageBuilder("multipart/related", new MIMEBuilder());
        axisConfig.addMessageBuilder("application/soap+xml", new SOAPBuilder());
        axisConfig.addMessageBuilder("text/xml", new SOAPBuilder());
        axisConfig.addMessageBuilder("application/xop+xml", new MTOMBuilder());
        axisConfig.addMessageBuilder("application/xml", new ApplicationXMLBuilder());
        axisConfig.addMessageBuilder("application/x-www-form-urlencoded",
                                     new XFormURLEncodedBuilder());
        axisConfig.addParameter(Constants.Configuration.USE_DEFAULT_FALLBACK_BUILDER, "true");
        OMElement envelope = TransportUtils.createSOAPMessage(mc,fis,"multipart/mixed;boundary=--MIMEBoundary258DE2D105298B756D");
        
        assertTrue(envelope != null);
        assertTrue(envelope instanceof SOAPEnvelope);
        OMElement firstElement = ((SOAPEnvelope)envelope).getBody().getFirstElement();
		assertTrue(firstElement instanceof OMSourcedElement);
		assertTrue(((OMSourcedElement)firstElement).getDataSource() instanceof UnknownContentOMDataSource);
    }
}
