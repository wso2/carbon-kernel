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
package org.apache.axis2.jaxws.provider.soapbinding;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.BindingType;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceProvider;

import org.apache.axis2.jaxws.Constants;
@WebServiceProvider(serviceName="SOAPBindingProviderService", 
					targetNamespace="http://SOAPBindingProvider.provider.jaxws.axis2.apache.org", 
					portName="SOAPBindingProviderPort")
@ServiceMode(value=Service.Mode.MESSAGE)
@BindingType(Constants.SOAP_HTTP_BINDING) 
public class SOAPBindingProvider implements Provider<SOAPMessage> {
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


	String soap11ResponseStr = "SOAP11 request received";
	String soap12ResponseStr = "SOAP12 request received";
	
	public SOAPMessage invoke(SOAPMessage soapMessage) {
		try{
			System.out.println("Start Invoke(SOAPMessage)");
			String namespaceURI = getSoapVersionURI(soapMessage);
			//return SOAP11 response if this is a SOAP11 Message
			if(namespaceURI.equals(SOAP11_NS_URI)){
				System.out.println("SOAP11_NS_URI found in the request");
		        return getSOAP11Response();
			}
			//return SOAP12 response if this is a SOAP11 Message
			if(namespaceURI.equals(SOAP12_NS_URI)){
				System.out.println("SOAP12_NS_URI found in the request");
				return getSOAP12Response();
			}
		}catch(Exception e){
			throw new WebServiceException(e);
		}
		throw new WebServiceException("Request received but could not interper the protocol");
	}

	private String getSoapVersionURI(SOAPMessage soapMessage)throws SOAPException{
		SOAPPart sp = soapMessage.getSOAPPart();
		SOAPEnvelope envelope = sp.getEnvelope();
		return envelope.getNamespaceURI();
	}
	
	private SOAPMessage getSOAP11Response() throws SOAPException, IOException{
		MessageFactory factory = MessageFactory.newInstance();
        String responseXML = SOAP11_ENVELOPE_HEAD +"<return>"+ soap11ResponseStr+"</return>" + SOAP11_ENVELOPE_TAIL;
        System.out.println("Creating SOAP11 Response");
        return factory.createMessage(null, new ByteArrayInputStream(responseXML.getBytes()));
		
	}
	
	private SOAPMessage getSOAP12Response() throws SOAPException, IOException{
		MessageFactory factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
		MimeHeaders header = new MimeHeaders();
		header.addHeader("Content-Type", "application/soap+xml");
        String responseXML = SOAP12_ENVELOPE_HEAD +"<return>"+ soap12ResponseStr+"</return>" + SOAP12_ENVELOPE_TAIL;
        System.out.println("Creating SOAP12 Response");
        return factory.createMessage(header, new ByteArrayInputStream(responseXML.getBytes()));
	}
}

