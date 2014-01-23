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

package org.apache.axis2.jaxws.context.sei;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.ws.Holder;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;


@WebService(name = "MessageContext", 
        portName="MessageContextPort",
        targetNamespace = "http://context.jaxws.axis2.apache.org/", 
        wsdlLocation = "META-INF/MessageContext.wsdl")
public interface MessageContext {
    /**
     * @param value
     * @param type
     * @param propertyName
     * @param isFound
     */
    @WebMethod
    @RequestWrapper(localName = "isPropertyPresent", targetNamespace = "http://context.jaxws.axis2.apache.org/", className = "org.apache.axis2.jaxws.context.sei.IsPropertyPresent")
    @ResponseWrapper(localName = "isPropertyPresentResponse", targetNamespace = "http://context.jaxws.axis2.apache.org/", className = "org.apache.axis2.jaxws.context.sei.IsPropertyPresentResponse")
    public void isPropertyPresent(
            @WebParam(name = "propertyName", targetNamespace = "", mode = WebParam.Mode.INOUT)
            Holder<String> propertyName,
            @WebParam(name = "value", targetNamespace = "", mode = WebParam.Mode.INOUT)
            Holder<String> value,
            @WebParam(name = "type", targetNamespace = "", mode = WebParam.Mode.INOUT)
            Holder<String> type,
            @WebParam(name = "isFound", targetNamespace = "", mode = WebParam.Mode.INOUT)
            Holder<Boolean> isFound);

}
