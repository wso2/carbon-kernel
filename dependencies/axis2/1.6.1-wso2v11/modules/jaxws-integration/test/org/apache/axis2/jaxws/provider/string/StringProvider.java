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

package org.apache.axis2.jaxws.provider.string;

import org.apache.axis2.jaxws.TestLogger;

import javax.xml.ws.BindingType;
import javax.xml.ws.Provider;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.soap.SOAPBinding;


@WebServiceProvider(
        serviceName="StringProviderService", 
        wsdlLocation="META-INF/echostring.wsdl", 
        targetNamespace="http://stringprovider.sample.test.org")
@BindingType(SOAPBinding.SOAP11HTTP_BINDING)
public class StringProvider implements Provider<String> {
    
    public String invoke(String text) {
        TestLogger.logger.debug("StringProvider invoke received the message [" + text + "]");
        if (text == null) {
            return " ";
        } else if (text.contains("throwWebServiceException")) {
            throw new WebServiceException("provider");
        } else if (text.contains("returnNull")) {
            return null;
        } else if (text.contains("<Code>") && text.contains("SOAPFaultProviderTests")) { 
            // Make sure the received fault has the Reason string
            if (text.contains("<Reason>")) {
                return null;
            }
            else {
                throw new UnsupportedOperationException("Test failed: No <Reason> element");
            }
        } else if (text.contains("<faultcode>") && text.contains("SOAPFaultProviderTests")) { 
            // Make sure the received fault has the Reason string
            if (text.contains("<faultstring>")) {
                return null;
            }
            else {
                throw new UnsupportedOperationException("Test failed: No <faultstring> element");
            }
        } else {
            // Echo the input
            return text;
        }
    }
}
