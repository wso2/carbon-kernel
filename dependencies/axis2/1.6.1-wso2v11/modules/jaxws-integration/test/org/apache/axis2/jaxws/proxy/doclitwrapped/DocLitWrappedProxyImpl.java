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

package org.apache.axis2.jaxws.proxy.doclitwrapped;

import org.apache.axis2.jaxws.TestLogger;

import javax.xml.ws.BindingType;
import javax.xml.ws.Provider;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.soap.SOAPBinding;
@WebServiceProvider(
        serviceName="ProxyDocLitWrappedService", 
        wsdlLocation="META-INF/ProxyDocLitWrapped.wsdl", 
        targetNamespace="http://doclitwrapped.proxy.test.org")
@BindingType(SOAPBinding.SOAP11HTTP_BINDING)
public class DocLitWrappedProxyImpl implements Provider<String> {

    public String invoke(String invoke_str) {
        // TODO Auto-generated method stub
        TestLogger.logger.debug("End point called with String value =" + invoke_str);
        return new String("<ns2:ReturnType xmlns:ns2=\"http://doclitwrapped.proxy.test.org\"><return_str>some response</return_str></ns2:ReturnType>");
    }
}
