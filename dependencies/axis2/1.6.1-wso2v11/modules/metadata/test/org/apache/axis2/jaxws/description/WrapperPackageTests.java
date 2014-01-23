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
import org.apache.log4j.BasicConfigurator;

import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.WebFault;

/** Tests the request and response wrappers. */
public class WrapperPackageTests extends TestCase {
    static {
        // Note you will probably need to increase the java heap size, for example
        // -Xmx512m.  This can be done by setting maven.junit.jvmargs in project.properties.
        // To change the settings, edit the log4j.property file
        // in the test-resources directory.
        BasicConfigurator.configure();
    }

    public void testSEIPackageWrapper() {
        EndpointInterfaceDescription eiDesc = getEndpointInterfaceDesc(SEIPackageWrapper.class);
        
        // See if the name is correct..currently we cannot do this directly, so I am using
        // the debug dump.
        String dump = eiDesc.toString();
        assertTrue(dump.contains("Name: SEIPackageWrapper"));
        
        OperationDescription opDesc = eiDesc.getOperation("method1");
        String requestWrapperClass = opDesc.getRequestWrapperClassName();

        // The algorithm to find the response wrapper is not defined by the specification.
        // The marshalling layer (jaxws) can use proprietary mechanisms to find, build or operate
        // without the wrapper class.

        //assertEquals("org.apache.axis2.jaxws.description.Method1", requestWrapperClass);
        assertEquals(null, requestWrapperClass);

        String responseWrapperClass = opDesc.getResponseWrapperClassName();
        //assertEquals("org.apache.axis2.jaxws.description.Method1Response", responseWrapperClass);
        assertEquals(null, responseWrapperClass);

        FaultDescription fDesc = opDesc.getFaultDescriptions()[0];
        String faultExceptionClass = fDesc.getExceptionClassName();
        assertEquals("org.apache.axis2.jaxws.description.Method1Exception", faultExceptionClass);
        String faultBeanClass = fDesc.getFaultBean();
        assertEquals("org.apache.axis2.jaxws.description.ExceptionBean", faultBeanClass);

    }

    public void testSEISubPackageWrapper() {
        EndpointInterfaceDescription eiDesc = getEndpointInterfaceDesc(SEISubPackageWrapper.class);
        OperationDescription opDesc = eiDesc.getOperation("subPackageMethod1");
        // The algorithm to find the response wrapper is not defined by the specification.
        // The marshalling layer (jaxws) can use proprietary mechanisms to find, build or operate
        // without the wrapper class.

        String requestWrapperClass = opDesc.getRequestWrapperClassName();
        //assertEquals("org.apache.axis2.jaxws.description.jaxws.SubPackageMethod1", requestWrapperClass);
        assertEquals(null, requestWrapperClass);
        String responseWrapperClass = opDesc.getResponseWrapperClassName();
        //assertEquals("org.apache.axis2.jaxws.description.jaxws.SubPackageMethod1Response", responseWrapperClass);
        assertEquals(null, responseWrapperClass);
        FaultDescription fDesc = opDesc.getFaultDescriptions()[0];
        String faultExceptionClass = fDesc.getExceptionClassName();
        assertEquals("org.apache.axis2.jaxws.description.SubPackageException", faultExceptionClass);
        String faultBeanClass = fDesc.getFaultBean();
        // Due to the missing getFaultInfo, the runtime must find or build a fault bean.
        assertEquals("", faultBeanClass);
        //assertEquals("org.apache.axis2.jaxws.description.jaxws.SubPackageExceptionBean", faultBeanClass);

    }

    /*
    * Method to return the endpoint interface description for a given implementation class.
    */
    private EndpointInterfaceDescription getEndpointInterfaceDesc(Class implementationClass) {
        // Use the description factory directly; this will be done within the JAX-WS runtime
        ServiceDescription serviceDesc =
                DescriptionFactory.createServiceDescription(implementationClass);
        assertNotNull(serviceDesc);

        EndpointDescription[] endpointDesc = serviceDesc.getEndpointDescriptions();
        assertNotNull(endpointDesc);
        assertEquals(1, endpointDesc.length);

        // TODO: How will the JAX-WS dispatcher get the appropriate port (i.e. endpoint)?  Currently assumes [0]
        EndpointDescription testEndpointDesc = endpointDesc[0];
        EndpointInterfaceDescription testEndpointInterfaceDesc =
                testEndpointDesc.getEndpointInterfaceDescription();
        assertNotNull(testEndpointInterfaceDesc);

        return testEndpointInterfaceDesc;
    }
}

@WebService()
class SEIPackageWrapper {
    @RequestWrapper()
    @ResponseWrapper()
    public String method1(String string) throws Method1Exception {
        return string;
    }
}

class Method1 {

}

class Method1Response {

}

@WebFault()
class Method1Exception extends Exception {
    public ExceptionBean getFaultInfo() {
        return null;
    }
}

class ExceptionBean {

}

@WebFault
class SubPackageException extends Exception {
    // No getFaultInfo method
}

@WebService()
class SEISubPackageWrapper {
    @RequestWrapper()
    @ResponseWrapper()
    public String subPackageMethod1(String string) throws SubPackageException {
        return string;
    }
}