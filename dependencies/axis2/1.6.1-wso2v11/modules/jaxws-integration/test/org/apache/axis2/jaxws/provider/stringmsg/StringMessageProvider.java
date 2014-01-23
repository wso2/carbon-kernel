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

package org.apache.axis2.jaxws.provider.stringmsg;

import org.apache.axis2.jaxws.TestLogger;

import javax.xml.ws.BindingType;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.http.HTTPBinding;

@WebServiceProvider(serviceName="StringMessageProviderService")
@BindingType(SOAPBinding.SOAP11HTTP_BINDING)
@ServiceMode(value=Service.Mode.MESSAGE)
public class StringMessageProvider implements Provider<String> {
    private static String responseGood = "<?xml version='1.0' encoding='utf-8'?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Header /><soapenv:Body><provider><message>request processed</message></provider></soapenv:Body></soapenv:Envelope>";
    private static String responseBad  = "<?xml version='1.0' encoding='utf-8'?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Header /><soapenv:Body><provider><message>ERROR:null request received</message><provider></soapenv:Body></soapenv:Envelope>";
    
    public String invoke(String obj) {
        if (obj != null) {
            String str = (String) obj;
            TestLogger.logger.debug(">> StringMessageProvider received a new request");
            TestLogger.logger.debug(">> request [" + str + "]");
            
            // Make sure there are no extra characters (like a BOM) between the Body tag and the operation element
            if (str.contains("echo")) {
                if (str.contains("Body><echo")) {
                    // Good data...replace the echo with echoResponse
                    TestLogger.logger.debug("Valid");
                    str = str.replaceAll("echo", "echoResponse");
                    str = str.replaceAll("arg", "response");
                    return str;
                   
                } else {
                    TestLogger.logger.debug("Bad Data detected after the SOAP body");
                    // Bad data...simply return the bad response..this will cause an exception on the client
                    return responseBad;
                }
            }

            return responseGood;
        }
        TestLogger.logger.debug(">> ERROR:null request received");
        return responseBad;
    }
}
