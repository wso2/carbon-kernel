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

package org.apache.axis2.jaxws.provider.soapmsgreturnnull;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.BindingType;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.soap.SOAPFaultException;

import org.apache.axis2.jaxws.TestLogger;

@WebServiceProvider(serviceName="SoapMessageNullProviderService",
		targetNamespace="http://soapmsgreturnnull.provider.jaxws.axis2.apache.org",
		portName="SoapMessageNullProviderPort")
@BindingType(SOAPBinding.SOAP11HTTP_BINDING)
@ServiceMode(value=Service.Mode.MESSAGE)
public class SoapMessageNullProvider implements Provider<SOAPMessage> {
      
    
    public SOAPMessage invoke(SOAPMessage soapMessage) throws SOAPFaultException {
        TestLogger.logger.debug(">> SoapMessageNullProvider: Request received.");
        return null;
    }
    

}
