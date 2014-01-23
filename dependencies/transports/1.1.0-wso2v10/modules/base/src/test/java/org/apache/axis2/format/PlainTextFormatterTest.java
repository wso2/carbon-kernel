/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.axis2.format;

import java.io.ByteArrayOutputStream;

import junit.framework.TestCase;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.format.PlainTextFormatter;
import org.apache.axis2.transport.base.BaseConstants;

public class PlainTextFormatterTest extends TestCase {
	private static final String testString = "\u00e0 peine arriv\u00e9s nous entr\u00e2mes dans sa chambre";
	
	private MessageContext createMessageContext(String textPayload) throws AxisFault {
		MessageContext messageContext = new MessageContext();
		SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope envelope = factory.getDefaultEnvelope();
        OMElement textWrapper = factory.createOMElement(BaseConstants.DEFAULT_TEXT_WRAPPER);
        textWrapper.setText(textPayload);
        envelope.getBody().addChild(textWrapper);
        messageContext.setEnvelope(envelope);
		return messageContext;
	}
	
	private void testGetBytes(String encoding) throws Exception {
		MessageContext messageContext = createMessageContext(testString);
        OMOutputFormat format = new OMOutputFormat();
        format.setCharSetEncoding(encoding);
        byte[] bytes = new PlainTextFormatter().getBytes(messageContext, format);
        assertEquals(testString, new String(bytes, encoding));
	}
	
	public void testGetBytesUTF8() throws Exception {
		testGetBytes("UTF-8");
	}
	
	public void testGetBytesLatin1() throws Exception {
		testGetBytes("ISO-8859-1");
	}
	
	private void testWriteTo(String encoding) throws Exception {
		MessageContext messageContext = createMessageContext(testString);
        OMOutputFormat format = new OMOutputFormat();
        format.setCharSetEncoding(encoding);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new PlainTextFormatter().writeTo(messageContext, format, baos, false);
        assertEquals(testString, new String(baos.toByteArray(), encoding));
	}
	
	public void testWriteToUTF8() throws Exception {
		testWriteTo("UTF-8");
	}
	
	public void testWriteToLatin1() throws Exception {
		testWriteTo("ISO-8859-1");
	}
}
