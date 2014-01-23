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

package org.apache.axis2.jaxws.sample.dlwmin.sei;

import org.apache.axis2.jaxws.sample.dlwmin.types.TestBean;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

@WebService(targetNamespace = "http://apache.org/axis2/jaxws/sample/dlwmin", name = "Greeter")

public interface Greeter {
    @WebResult(targetNamespace = "http://apache.org/axis2/jaxws/sample/dlwmin", name = "responseType")
    @WebMethod(operationName = "greetMe", action="greetMe")
    public java.lang.String greetMe(
        @WebParam(targetNamespace = "http://apache.org/axis2/jaxws/sample/dlwmin", name = "requestType")
        java.lang.String requestType
    );
    
    @WebResult(targetNamespace = "", partName = "unqualifiedResponse")
    @WebMethod(operationName = "testUnqualified", action="testUnqualified")
    public java.lang.String testUnqualified(
        @WebParam(targetNamespace = "", partName = "unqualifiedRequest")
        java.lang.String requestType
    );
 
    @WebResult(targetNamespace = "", partName = "out")
    @WebMethod(operationName = "process", action="process")
    public TestBean process(
        @WebParam(targetNamespace = "", partName = "inAction")
        int inAction,
        @WebParam(targetNamespace = "", partName = "in")
        TestBean in
    ) throws TestException, TestException2, TestException3;
    
    @WebMethod
    public String simpleTest(
            @WebParam(targetNamespace = "http://apache.org/axis2/jaxws/sample/dlwmin", name = "name")
            String name, 
            @WebParam(targetNamespace = "http://apache.org/axis2/jaxws/sample/dlwmin", name = "bytes")
            byte[] bytes);
    
}
