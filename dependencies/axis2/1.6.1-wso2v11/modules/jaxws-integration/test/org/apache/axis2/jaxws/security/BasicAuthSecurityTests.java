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

package org.apache.axis2.jaxws.security;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axis2.jaxws.BindingProvider;
import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.framework.AbstractTestCase;

import javax.xml.namespace.QName;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPBinding;

public class BasicAuthSecurityTests extends AbstractTestCase {

    private String endpointUrl = "http://localhost:6060/axis2/services/BasicAuthSecurityService.SimpleProviderServiceSOAP11port0";
    private String xmlString = "<invokeOp>test input</invokeOp>";
    private QName SERVICE_QNAME = new QName("http://ws.apache.org/axis2", "BasicAuthSecurityService");
    private QName PORT_QNAME = new QName("http://ws.apache.org/axis2", "SimpleProviderServiceSOAP11port0");

    private String USER_ID = "testid";
    private String PASSWORD = "testid";

    public static Test suite() {
        return getTestSetup(new TestSuite(BasicAuthSecurityTests.class));
    }
    
    public void testBasicAuth() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        Dispatch<String> dispatch = getDispatch(Service.Mode.PAYLOAD,
        		                                endpointUrl,
        		                                SOAPBinding.SOAP11HTTP_BINDING);

        TestLogger.logger.debug(">> Invoking Dispatch<String> BasicAuthSecurityService");
        String retVal = dispatch.invoke(xmlString);
        TestLogger.logger.debug(">> Response [" + retVal + "]");
        
        assertTrue(retVal != null);
        
        // Invoke a second time to verify
        retVal = dispatch.invoke(xmlString);
        TestLogger.logger.debug(">> Response [" + retVal + "]");
        
        assertTrue(retVal != null);
    }
    
    public void testBasicAuth_uid_pwd() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        Dispatch<String> dispatch = getDispatch(Service.Mode.PAYLOAD,
        		                                endpointUrl,
        		                                SOAPBinding.SOAP11HTTP_BINDING);
        
        dispatch.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, USER_ID);
		dispatch.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, PASSWORD);

        TestLogger.logger.debug(">> Invoking Dispatch<String> BasicAuthSecurityService");
        String retVal = dispatch.invoke(xmlString);
        TestLogger.logger.debug(">> Response [" + retVal + "]");
        
        assertTrue(retVal != null);
        
        // Invoke a second time to verify
        TestLogger.logger.debug(">> Invoking Dispatch<String> BasicAuthSecurityService");
        retVal = dispatch.invoke(xmlString);
        TestLogger.logger.debug(">> Response [" + retVal + "]");
        
        assertTrue(retVal != null);
    }
    
    public void testBasicAuth_uid()throws Exception{
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        Dispatch<String> dispatch = getDispatch(Service.Mode.PAYLOAD,
        		                                endpointUrl,
        		                                SOAPBinding.SOAP11HTTP_BINDING);
        
        dispatch.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, USER_ID);

        TestLogger.logger.debug(">> Invoking Dispatch<String> BasicAuthSecurityService");
        String retVal = dispatch.invoke(xmlString);
       	TestLogger.logger.debug(">> Response [" + retVal + "]");
        
        // Invoke a second time to verify
        TestLogger.logger.debug(">> Invoking Dispatch<String> BasicAuthSecurityService");
        retVal = dispatch.invoke(xmlString);
        TestLogger.logger.debug(">> Response [" + retVal + "]");
    }
    
    public void testBasicAuth_pwd()throws Exception{
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        Dispatch<String> dispatch = getDispatch(Service.Mode.PAYLOAD,
                                                endpointUrl,
                                                SOAPBinding.SOAP11HTTP_BINDING);

        dispatch.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, PASSWORD);

        TestLogger.logger.debug(">> Invoking Dispatch<String> BasicAuthSecurityService");

        try{
            String retVal = dispatch.invoke(xmlString);
            TestLogger.logger.debug(">> Response [" + retVal + "]");

            fail("Set PASSWORD with no USERID: WebServiceException is expected");
        }
        catch(WebServiceException wse){
            TestLogger.logger.debug(getName() + ": " + wse);
        }
        
        // Try a second time to verify
        TestLogger.logger.debug(">> Invoking Dispatch<String> BasicAuthSecurityService");

        try{
            String retVal = dispatch.invoke(xmlString);
            TestLogger.logger.debug(">> Response [" + retVal + "]");

            fail("Set PASSWORD with no USERID: WebServiceException is expected");
        }
        catch(WebServiceException wse){
            TestLogger.logger.debug(getName() + ": " + wse);
        }
    }
    
	/**
	 * Auxiliary method, generates a Dispatch object on demand
	 * 
	 * @param mode
	 *            Service.Mode
	 * @param endpoint
	 *            endpoint address
	 * @param binding
	 *            binding type
	 * @return
	 */
	private Dispatch<String> getDispatch(Service.Mode mode, String endpoint,String binding) {
		
		Service service = Service.create(SERVICE_QNAME);
		
		service.addPort(PORT_QNAME, binding, endpoint);
		javax.xml.ws.Dispatch<String> dispatch = service.createDispatch(PORT_QNAME, String.class,mode);

		assertNotNull("Dispatch not null", dispatch);

		return dispatch;
	}
}
