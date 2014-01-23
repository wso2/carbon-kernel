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

package org.apache.axis2.jaxws.proxy.rpclitswa;

import org.apache.axis2.jaxws.proxy.rpclitswa.sei.RPCLitSWA;

import javax.activation.DataHandler;
import javax.jws.WebService;
import javax.xml.ws.Holder;

@WebService(targetNamespace="http://org/apache/axis2/jaxws/proxy/rpclitswa",
            serviceName="RPCLitSWAService", 
            portName="RPCLitSWA",
            endpointInterface="org.apache.axis2.jaxws.proxy.rpclitswa.sei.RPCLitSWA")
public class RPCLitSWAImpl implements RPCLitSWA {

    public void echo(String request, String dummyAttachmentIN,
            Holder<DataHandler> dummyAttachmentINOUT, Holder<String> response,
            Holder<String> dummyAttachmentOUT) {
        response.value = request;
        dummyAttachmentOUT.value = dummyAttachmentIN;
    }

}
