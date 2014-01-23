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

package org.apache.axis2.jaxws.proxy.soap12;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

@WebService(name = "Echo", targetNamespace = "http://jaxws.axis2.apache.org/proxy/soap12")
public interface Echo {

    @WebMethod
    @WebResult(name = "response", targetNamespace = "http://jaxws.axis2.apache.org/proxy/soap12")
    @RequestWrapper(localName = "echo", targetNamespace = "http://jaxws.axis2.apache.org/proxy/soap12", className = "org.apache.axis2.jaxws.proxy.soap12.EchoType")
    @ResponseWrapper(localName = "echoResponse", targetNamespace = "http://jaxws.axis2.apache.org/proxy/soap12", className = "org.apache.axis2.jaxws.proxy.soap12.EchoResponseType")
    public String echo(
        @WebParam(name = "request", targetNamespace = "http://jaxws.axis2.apache.org/proxy/soap12")
        String request);

}
