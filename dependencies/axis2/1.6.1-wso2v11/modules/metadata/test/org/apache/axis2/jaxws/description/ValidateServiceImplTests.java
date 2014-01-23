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

package org.apache.axis2.jaxws.description;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.MethodDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.converter.JavaClassToDBCConverter;

import javax.jws.WebService;
import java.util.HashMap;
import java.util.List;

/**
 * 
 */
public class ValidateServiceImplTests extends TestCase {

    public void testValidServiceImpl() {
        try {
            Class serviceImplClass = ServiceImpl.class;
            JavaClassToDBCConverter converter = new JavaClassToDBCConverter(serviceImplClass);
            HashMap<String, DescriptionBuilderComposite> dbcMap = converter.produceDBC();
            List<ServiceDescription> serviceDescList =  DescriptionFactory.createServiceDescriptionFromDBCMap(dbcMap);
            assertNotNull(serviceDescList);
            assertEquals(1, serviceDescList.size());
        }
        catch (Exception e) {
            e.printStackTrace();
            fail("Caught unexpected exception" + e);
        }
    }
    
    public void testMissingMethods() {
        try {
            Class serviceImplClass = MissingMethodsImpl.class;
            JavaClassToDBCConverter converter = new JavaClassToDBCConverter(serviceImplClass);
            HashMap<String, DescriptionBuilderComposite> dbcMap = converter.produceDBC();
            List<ServiceDescription> serviceDescList =  DescriptionFactory.createServiceDescriptionFromDBCMap(dbcMap);
            fail("Should have caused exception");
        }
        catch (Exception e) {
            // Expected path
        }
    }
    
    public void testInvalidThrows() {
        try {
            Class serviceImplClass = InvalidThrowsImpl.class;
            JavaClassToDBCConverter converter = new JavaClassToDBCConverter(serviceImplClass);
            HashMap<String, DescriptionBuilderComposite> dbcMap = converter.produceDBC();
            List<ServiceDescription> serviceDescList =  DescriptionFactory.createServiceDescriptionFromDBCMap(dbcMap);
            fail("Should have caused exception");
        }
        catch (Exception e) {
            // Expected path
        }
    }
    
    public void testMismatchedReturnTypesDBC() {
        try {
            Class serviceImplClass = ServiceImpl.class;
            JavaClassToDBCConverter converter = new JavaClassToDBCConverter(serviceImplClass);
            HashMap<String, DescriptionBuilderComposite> dbcMap = converter.produceDBC();
            // Set the return types for one of the methods on the impl class to mismatch the SEI
            DescriptionBuilderComposite implDBC = dbcMap.get("org.apache.axis2.jaxws.description.ServiceImpl");
            assertNotNull(implDBC);
            List<MethodDescriptionComposite> m1MDCList = implDBC.getMethodDescriptionComposite("method1");
            assertNotNull(m1MDCList);
            assertEquals(1, m1MDCList.size());
            MethodDescriptionComposite m1MDC = m1MDCList.get(0);
            assertEquals("java.lang.String", m1MDC.getReturnType());
            m1MDC.setReturnType("lava.lang.Integer");
            
            List<ServiceDescription> serviceDescList =  DescriptionFactory.createServiceDescriptionFromDBCMap(dbcMap);
            fail("Should have caused exception");
        }
        catch (Exception e) {
            // Expected path
        }
    }

    public void testMismatchedReturnTypes() {
        try {
            Class serviceImplClass = MismatchedReturnTypesImpl.class;
            JavaClassToDBCConverter converter = new JavaClassToDBCConverter(serviceImplClass);
            HashMap<String, DescriptionBuilderComposite> dbcMap = converter.produceDBC();
            List<ServiceDescription> serviceDescList =  DescriptionFactory.createServiceDescriptionFromDBCMap(dbcMap);
            fail("Should have caused exception");
        }
        catch (Exception e) {
            // Expected path
        }
    }

    public void testMismatchedParameterNumber() {
        try {
            Class serviceImplClass = MismatchedParameterNumberImpl.class;
            JavaClassToDBCConverter converter = new JavaClassToDBCConverter(serviceImplClass);
            HashMap<String, DescriptionBuilderComposite> dbcMap = converter.produceDBC();
            List<ServiceDescription> serviceDescList =  DescriptionFactory.createServiceDescriptionFromDBCMap(dbcMap);
            fail("Should have caused exception");
        }
        catch (Exception e) {
            // Expected path
        }
    }

    public void testMismatchedParameterTypes() {
        try {
            Class serviceImplClass = MismatchedParameterTypesImpl.class;
            JavaClassToDBCConverter converter = new JavaClassToDBCConverter(serviceImplClass);
            HashMap<String, DescriptionBuilderComposite> dbcMap = converter.produceDBC();
            List<ServiceDescription> serviceDescList =  DescriptionFactory.createServiceDescriptionFromDBCMap(dbcMap);
            fail("Should have caused exception");
        }
        catch (Exception e) {
            // Expected path
        }
    }

}

@WebService
interface EndpointInterface {
    public String method1(int param1, int param2) throws MyException;
    
    public void method2(String param1) throws MyException;
    
    public int method3();
}

class MyException extends Exception {
    
}

@WebService (endpointInterface = "org.apache.axis2.jaxws.description.EndpointInterface")
class ServiceImpl {
    public String method1(int param1, int param2) throws MyException {
        return null;
    }
    
    // Intentionally doesn't throw MyException.  It is valid for a service impl to throw
    // fewer exceptions than the endpoint interface
    public void method2(String param1) {
        
    }
    
    public int method3() {
        return 0;
    }
}

@WebService (endpointInterface = "org.apache.axis2.jaxws.description.EndpointInterface")
class MissingMethodsImpl {
    public String method1(int param1, int param2) {
        return null;
    }
    
//    public void method2(String param1) {
//        
//    }
    
    public int method3() {
        return 0;
    }
}

@WebService (endpointInterface = "org.apache.axis2.jaxws.description.EndpointInterface")
class InvalidThrowsImpl {
    public String method1(int param1, int param2) throws MyException {
        return null;
    }
    
    // Intentionally doesn't throw MyException.  It is valid for a service impl to throw
    // fewer exceptions than the endpoint interface
    public void method2(String param1) {
        
    }
    // It is invalid to throw more exceptions than the endpoint interface
    public int method3() throws MyException {
        return 0;
    }
}

@WebService (endpointInterface = "org.apache.axis2.jaxws.description.EndpointInterface")
class MismatchedReturnTypesImpl {
    // Return type doesn't match SEI
    public Integer method1(int param1, int param2) throws MyException {
        return null;
    }
    
    public void method2(String param1) {
        
    }
    
    public int method3() {
        return 0;
    }
}

@WebService (endpointInterface = "org.apache.axis2.jaxws.description.EndpointInterface")
class MismatchedParameterNumberImpl {
    public String method1(int param1) throws MyException {
        return null;
    }
    
    public void method2(String param1) {
        
    }
    
    public int method3() {
        return 0;
    }
}

@WebService (endpointInterface = "org.apache.axis2.jaxws.description.EndpointInterface")
class MismatchedParameterTypesImpl {
    public String method1(int param1, String param2) throws MyException {
        return null;
    }
    
    // Intentionally doesn't throw MyException.  It is valid for a service impl to throw
    // fewer exceptions than the endpoint interface
    public void method2(String param1) {
        
    }
    
    public int method3() {
        return 0;
    }
}
