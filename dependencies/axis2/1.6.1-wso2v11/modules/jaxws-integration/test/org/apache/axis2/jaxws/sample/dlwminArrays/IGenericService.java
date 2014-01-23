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

package org.apache.axis2.jaxws.sample.dlwminArrays;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.Holder;
import javax.xml.ws.ResponseWrapper;

@WebService(name = "GenericService", targetNamespace = "http://apache.org/axis2/jaxws/sample/dlwminArrays")
public interface IGenericService {

    @WebMethod
    public String sayHello(String text);
    
    @WebMethod
    @WebResult(name = "simpleArrayReturn", targetNamespace = "http://apache.org/axis2/jaxws/sample/dlwminArrays")
    @ResponseWrapper(className = "org.apache.axis2.jaxws.sample.dlwminArrays.SimpleArrayResponse")
    public String[] getSimpleArray();

    @WebResult(name = "complexArrayReturn", targetNamespace = "http://apache.org/axis2/jaxws/sample/dlwminArrays")
    @ResponseWrapper(className = "org.apache.axis2.jaxws.sample.dlwminArrays.ComplexArrayResponse")
    public WSUser[] getComplexArray();

    @WebResult(name = "simpleListReturn", targetNamespace = "http://apache.org/axis2/jaxws/sample/dlwminArrays")
    @ResponseWrapper(className = "org.apache.axis2.jaxws.sample.dlwminArrays.SimpleListResponse")
    public List<String> getSimpleList();

    @WebResult(name = "complexListReturn", targetNamespace = "http://apache.org/axis2/jaxws/sample/dlwminArrays")
    @ResponseWrapper(className = "org.apache.axis2.jaxws.sample.dlwminArrays.ComplexListResponse" , 
            localName="complexListResponse", 
            targetNamespace="http://apache.org/axis2/jaxws/sample/dlwminArrays")
    public List<WSUser> getComplexList();
    
    @WebResult(name = "echoComplexListReturn", targetNamespace = "http://apache.org/axis2/jaxws/sample/dlwminArrays")
    public List<WSUser> echoComplexList(List<WSUser> in );
    
    @WebResult(name = "echo", targetNamespace = "http://apache.org/axis2/jaxws/sample/dlwminArrays")
    public List<WSUser> echo(List<WSUser> in, Holder<List<String>> ids);
    
    
    
}