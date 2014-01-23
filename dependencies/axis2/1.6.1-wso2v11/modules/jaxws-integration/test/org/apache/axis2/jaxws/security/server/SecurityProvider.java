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

package org.apache.axis2.jaxws.security.server;

import org.apache.axis2.jaxws.TestLogger;

import javax.xml.ws.BindingType;
import javax.xml.ws.Provider;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.http.HTTPBinding;

@WebServiceProvider(serviceName="BasicAuthSecurityService",portName="SimpleProviderServiceSOAP11port0")
@BindingType(SOAPBinding.SOAP11HTTP_BINDING)
public class SecurityProvider implements Provider<String> {

    private static String responseGood = "<provider><message>request processed</message></provider>";
    private static String responseBad  = "<provider><message>ERROR:null request received</message><provider>";
    
    public String invoke(String obj) {
        if (obj != null) {
            String str = (String) obj;
            TestLogger.logger.debug(">> StringProvider received a new request");
            TestLogger.logger.debug(">> request [" + str + "]");
            
            return responseGood;
        }
        TestLogger.logger.debug(">> ERROR:null request received");
        return responseBad;
    }
}
