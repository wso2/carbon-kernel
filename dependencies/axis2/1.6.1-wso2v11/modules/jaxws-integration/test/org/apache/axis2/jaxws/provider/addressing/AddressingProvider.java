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

package org.apache.axis2.jaxws.provider.addressing;

import java.io.ByteArrayInputStream;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.BindingType;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.soap.Addressing;
import javax.xml.ws.RespectBinding;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.soap.SOAPFaultException;

@WebServiceProvider(serviceName="AddressingProviderService",
		    targetNamespace="http://addressing.provider.jaxws.axis2.apache.org",
		    wsdlLocation="META-INF/AddressingProvider.wsdl",
		    portName="AddressingProviderPort")
@BindingType(SOAPBinding.SOAP11HTTP_BINDING)
@Addressing(enabled=true)
@RespectBinding
@ServiceMode(value=Service.Mode.MESSAGE)
public class AddressingProvider implements Provider<SOAPMessage> {
      
    String responseMsgStart = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Header/><soapenv:Body>";
    String responseMsgEnd = "</soapenv:Body></soapenv:Envelope>";   
    String XML_RETURN = "<ns2:outMessage xmlns:ns2=\"http://addressing.provider.jaxws.axis2.apache.org\">Hello Response</ns2:outMessage>";
     
    public SOAPMessage invoke(SOAPMessage soapMessage) throws SOAPFaultException {
    	try {
            MessageFactory factory = MessageFactory.newInstance();
            String responseXML = responseMsgStart + XML_RETURN + responseMsgEnd;
            SOAPMessage response = factory.createMessage(null, new ByteArrayInputStream(responseXML.getBytes()));

            return response;
        } catch(Exception e){
            e.printStackTrace();
    	}
    	return null;
    }   
}
