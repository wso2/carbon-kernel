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
package org.apache.axis2.jaxws.provider.soapbinding.tests;

import java.io.ByteArrayInputStream;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.axis2.jaxws.framework.AbstractTestCase;

public class StringProviderTests extends AbstractTestCase {
	private String endpointUrl = "http://localhost:6060/axis2/services/SOAPBindingStringProviderService.SOAPBindingStringProviderPort";
	private QName serviceName = new QName("http://StringProvider.soapbinding.provider.jaxws.axis2.apache.org", "SOAPBindingStringProviderService");
	private QName portName =  new QName("http://StringProvider.soapbinding.provider.jaxws.axis2.apache.org", "SOAPBindingStringProviderPort");

	private static final String SOAP11_NS_URI = "http://schemas.xmlsoap.org/soap/envelope/";
	private static final String SOAP12_NS_URI = "http://www.w3.org/2003/05/soap-envelope";
	public static final String SOAP11_ENVELOPE_HEAD = "<?xml version='1.0' encoding='utf-8'?>" + 
	"<soapenv:Envelope xmlns:soapenv=\"" + SOAP11_NS_URI + "\">" +
	"<soapenv:Header />" + 
	"<soapenv:Body>";

	public static final String SOAP12_ENVELOPE_HEAD = 
		"<?xml version='1.0' encoding='utf-8'?>" + 
		"<soapenv:Envelope xmlns:soapenv=\"" + SOAP12_NS_URI + "\">" +
		"<soapenv:Header />" + 
		"<soapenv:Body>";

	public static final String SOAP11_ENVELOPE_TAIL = 
		"</soapenv:Body>" + 
		"</soapenv:Envelope>";

	public static final String SOAP12_ENVELOPE_TAIL = 
		"</soapenv:Body>" + 
		"</soapenv:Envelope>";
	
	public static Test suite() {
        return getTestSetup(new TestSuite(StringProviderTests.class));
    }
/*
 * This test case makes sure that we receive a soap11 response for a soap11 request.
 */
	public void testsoap11request(){
		System.out.println("---------------------------------------");
		System.out.println("test: " + getName());
		try{
			Service svc = Service.create(serviceName);
			svc.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING, endpointUrl);

			Dispatch<String> dispatch =
				svc.createDispatch(portName, String.class, Service.Mode.MESSAGE);
			String xmlMessage = SOAP11_ENVELOPE_HEAD+"<invokeOp>soap11 request</invokeOp>"+SOAP11_ENVELOPE_TAIL;
			String response = dispatch.invoke(xmlMessage);

			MessageFactory factory = MessageFactory.newInstance();
			SOAPMessage soapMessage = factory.createMessage(null, new ByteArrayInputStream(response.getBytes()));
			assertTrue(getVersionURI(soapMessage).equals(SOAP11_NS_URI));
		}catch(Exception e){
			System.out.println("Failure while sending soap 11 request");
			System.out.println(e.getMessage());
			fail();
		}

	}

	private String getVersionURI(SOAPMessage soapMessage)throws SOAPException{
		SOAPPart sp = soapMessage.getSOAPPart();
		SOAPEnvelope envelope = sp.getEnvelope();
		return envelope.getNamespaceURI();
	}
}
