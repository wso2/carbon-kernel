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

package org.apache.axis2.jaxws.proxy.soap12.server;

import org.apache.axis2.jaxws.TestLogger;

import javax.xml.ws.BindingType;
import javax.xml.ws.Provider;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.soap.SOAPBinding;

@WebServiceProvider(targetNamespace="http://jaxws.axis2.apache.org/proxy/soap12",
					serviceName="SOAP12EchoService",
					wsdlLocation="META-INF/SOAP12Echo.wsdl",
					portName="EchoPort")
@BindingType(SOAPBinding.SOAP12HTTP_BINDING)
@ServiceMode(Mode.MESSAGE)
public class SOAP12EchoImpl implements Provider<String> {
    
    private static final String SOAP11_NS_URI = "http://schemas.xmlsoap.org/soap/envelope/";
    private static final String SOAP12_NS_URI = "http://www.w3.org/2003/05/soap-envelope";
    
    private static final String SEND_SOAP11_RESPONSE = "RESPONSE-SOAP11";
    private static final String SEND_SOAP12_RESPONSE = "RESPONSE-SOAP12";
    
    public static final String SOAP11_ENVELOPE_HEAD = 
        "<?xml version='1.0' encoding='utf-8'?>" + 
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
        
    public String invoke(String input) {
        TestLogger.logger.debug("received request [" + input + "]");
        
        // check the request to see if it contains the SOAP 1.1 namespace 
        // URI.  if so, then that is an error and we should respond with 
        // a failure.
        String status = "FAIL";
        if (input.indexOf(SOAP12_NS_URI) > 0) {
            status = "PASS";
            TestLogger.logger.debug("the request contains the SOAP 1.2 namespace as expected.");
        }
        else {
            TestLogger.logger.debug("the request did NOT contain the SOAP 1.2 namespace.");
            TestLogger.logger.debug("sending back a failure");
        }
        
        // the contents of the response should contain the status
        String responseBody =             
            "<echoResponse xmlns=\"http://jaxws.axis2.apache.org/proxy/soap12\">" +
            "<response>" + status + "</response>" +
            "</echoResponse>"; 
        
        // build up the appropriate envelope type for the response
        // based on what was the client requested.
        StringBuffer response = new StringBuffer();
        if (input.indexOf(SEND_SOAP11_RESPONSE) > 0) {
            response.append(SOAP11_ENVELOPE_HEAD);
            response.append(responseBody);
            response.append(SOAP11_ENVELOPE_TAIL);
        }
        else if (input.indexOf(SEND_SOAP12_RESPONSE) > 0) {
            response.append(SOAP12_ENVELOPE_HEAD);
            response.append(responseBody);
            response.append(SOAP12_ENVELOPE_TAIL);
        }

        TestLogger.logger.debug("sending response [" + response + "]");
        return response.toString();
    }
}
