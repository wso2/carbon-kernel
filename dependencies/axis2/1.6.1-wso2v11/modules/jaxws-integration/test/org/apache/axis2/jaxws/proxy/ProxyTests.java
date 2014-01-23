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

package org.apache.axis2.jaxws.proxy;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.framework.AbstractTestCase;
import org.apache.axis2.jaxws.proxy.doclitwrapped.sei.DocLitWrappedProxy;
import org.apache.axis2.jaxws.proxy.doclitwrapped.sei.ProxyDocLitWrappedService;
import org.test.proxy.doclitwrapped.ReturnType;

import javax.xml.namespace.QName;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Response;
import javax.xml.ws.Service;
import java.io.File;
import java.net.URL;
import java.util.concurrent.Future;

public class ProxyTests extends AbstractTestCase {
    private QName serviceName = new QName(
            "http://doclitwrapped.proxy.test.org", "ProxyDocLitWrappedService");
    private String axisEndpoint = "http://localhost:6060/axis2/services/ProxyDocLitWrappedService.DocLitWrappedProxyImplPort";
    private QName portName = new QName("http://doclitwrapped.proxy.test.org",
            "DocLitWrappedProxyImplPort");
    private String wsdlLocation = System.getProperty("basedir",".")+"/"+"test/org/apache/axis2/jaxws/proxy/doclitwrapped/META-INF/ProxyDocLitWrapped.wsdl";
    private boolean runningOnAxis = true;

    public static Test suite() {
        return getTestSetup(new TestSuite(ProxyTests.class));
    }

    public void testMultipleServiceCalls(){
        try{
            if(!runningOnAxis){
                return;
            }
            TestLogger.logger.debug("---------------------------------------");
            TestLogger.logger.debug("test:" + getName());
            String request = new String("some string request");
            TestLogger.logger.debug("Service Call #1");
            ProxyDocLitWrappedService service1 = new ProxyDocLitWrappedService();
            DocLitWrappedProxy proxy1 = service1.getDocLitWrappedProxyImplPort();
            BindingProvider p1 =    (BindingProvider)proxy1;
            p1.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,axisEndpoint);
            String response1 = proxy1.invoke(request);
            TestLogger.logger.debug("Proxy Response =" + response1);
            TestLogger.logger.debug("---------------------------------------");

            TestLogger.logger.debug("Service Call #2");
            ProxyDocLitWrappedService service2 = new ProxyDocLitWrappedService();
            DocLitWrappedProxy proxy2 = service2.getDocLitWrappedProxyImplPort();
            BindingProvider p2 =    (BindingProvider)proxy2;
            p2.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,axisEndpoint);
            String response2 = proxy2.invoke(request);
            TestLogger.logger.debug("Proxy Response =" + response2);
            TestLogger.logger.debug("---------------------------------------");
            
        }catch(Exception e){
            e.printStackTrace();
            fail(getName() + " failed");
        }
    }
    
    public void testInvokeWithNullParam(){
        try{ 
            if(!runningOnAxis){
                return;
            }
            TestLogger.logger.debug("---------------------------------------");
            TestLogger.logger.debug("Test Name: " + getName());
            File wsdl= new File(wsdlLocation); 
            URL wsdlUrl = wsdl.toURL(); 
            Service service = Service.create(null, serviceName); 
            Object proxy =service.getPort(portName, DocLitWrappedProxy.class);
            TestLogger.logger.debug(">>Invoking Binding Provider property");
            BindingProvider p = (BindingProvider)proxy;
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,axisEndpoint);

            DocLitWrappedProxy dwp = (DocLitWrappedProxy)proxy;
            TestLogger.logger.debug(">> Invoking Proxy Synchronously");
            String request = null;
            String response = dwp.invoke(request);
            TestLogger.logger.debug("Proxy Response =" + response);

            
            // Try again
            response = dwp.invoke(request);
            TestLogger.logger.debug("Proxy Response =" + response);
            TestLogger.logger.debug("---------------------------------------");
        }catch(Exception e){ 
            e.printStackTrace(); 
            fail("Exception received" + e);
        }
    }
    public void testInvoke(){
        try{ 
            if(!runningOnAxis){
                return;
            }
            TestLogger.logger.debug("---------------------------------------");
            
            File wsdl= new File(wsdlLocation); 
            URL wsdlUrl = wsdl.toURL(); 
            Service service = Service.create(null, serviceName);
            String request = new String("some string request"); 
            Object proxy =service.getPort(portName, DocLitWrappedProxy.class);
            TestLogger.logger.debug(">>Invoking Binding Provider property");
            BindingProvider p = (BindingProvider)proxy;
                p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,axisEndpoint);
                
            DocLitWrappedProxy dwp = (DocLitWrappedProxy)proxy;
            TestLogger.logger.debug(">> Invoking Proxy Synchronously");
            String response = dwp.invoke(request);
            TestLogger.logger.debug("Proxy Response =" + response);
            
            // Try again
            response = dwp.invoke(request);
            TestLogger.logger.debug("Proxy Response =" + response);
            TestLogger.logger.debug("---------------------------------------");
        }catch(Exception e){ 
            e.printStackTrace(); 
            fail("Exception received" + e);
        }
    }

    public void testInvokeWithWSDL(){

    	try{ 
            if(!runningOnAxis){
                return;
            }
            TestLogger.logger.debug("---------------------------------------");
            File wsdl= new File(wsdlLocation); 
            URL wsdlUrl = wsdl.toURL(); 
            Service service = Service.create(wsdlUrl, serviceName);
            String request = new String("some string request"); 
            Object proxy =service.getPort(portName, DocLitWrappedProxy.class);
            TestLogger.logger.debug(">>Invoking Binding Provider property");
            BindingProvider p = (BindingProvider)proxy;
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,axisEndpoint);
                
            DocLitWrappedProxy dwp = (DocLitWrappedProxy)proxy;
            TestLogger.logger.debug(">> Invoking Proxy Synchronously");
            String response = dwp.invoke(request);
            TestLogger.logger.debug("Proxy Response =" + response);
            
            // Try again
            response = dwp.invoke(request);
            TestLogger.logger.debug("Proxy Response =" + response);
            TestLogger.logger.debug("---------------------------------------");
        }catch(Exception e){ 
            e.printStackTrace(); 
            fail("Exception received" + e);
        }

    }
    
    public void testInvokeAsyncCallback(){
        try{ 
            if(!runningOnAxis){
                return;
            }
            TestLogger.logger.debug("---------------------------------------");
            
            File wsdl= new File(wsdlLocation); 
            URL wsdlUrl = wsdl.toURL(); 
            Service service = Service.create(null, serviceName);
            String request = new String("some string request"); 
            Object proxy =service.getPort(portName, DocLitWrappedProxy.class);
            TestLogger.logger.debug(">>Invoking Binding Provider property");
            BindingProvider p = (BindingProvider)proxy;
                p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,axisEndpoint);
                
            DocLitWrappedProxy dwp = (DocLitWrappedProxy)proxy;
            TestLogger.logger.debug(">> Invoking Proxy Asynchronous Callback");
            AsyncHandler handler = new AsyncCallback();
            Future<?> response = dwp.invokeAsync(request, handler);
            
            // Try again
            handler = new AsyncCallback();
            response = dwp.invokeAsync(request, handler);
            TestLogger.logger.debug("---------------------------------------");
        }catch(Exception e){ 
            e.printStackTrace(); 
            fail("Exception received" + e);
        }
    }
    
    public void testInvokeAsyncPolling() {
        try {
            TestLogger.logger.debug("---------------------------------------");
            
            File wsdl= new File(wsdlLocation); 
            URL wsdlUrl = wsdl.toURL(); 
            Service service = Service.create(null, serviceName);
            DocLitWrappedProxy proxy =service.getPort(portName, DocLitWrappedProxy.class);
            
            String request = new String("some string request");

            TestLogger.logger.debug(">> Invoking Binding Provider property");
            BindingProvider p = (BindingProvider) proxy;
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, axisEndpoint);

            TestLogger.logger.debug(">> Invoking Proxy with async polling request");
            Response<ReturnType> asyncResponse = proxy.invokeAsync(request);

            while (!asyncResponse.isDone()) {
                TestLogger.logger.debug(">> Async invocation still not complete");
                Thread.sleep(1000);
            }
            
            ReturnType response = asyncResponse.get();
            assertNotNull(response);
            
            // Try again
            asyncResponse = proxy.invokeAsync(request);

            while (!asyncResponse.isDone()) {
                TestLogger.logger.debug(">> Async invocation still not complete");
                Thread.sleep(1000);
            }
            
            response = asyncResponse.get();
            assertNotNull(response);
        }
        catch(Exception e) { 
            e.printStackTrace(); 
            fail("Exception received" + e);
        }
    }
    
    public void testTwoWay(){
        
        try{ 
            if(runningOnAxis){
                return;
            }
            File wsdl= new File(wsdlLocation); 
            URL wsdlUrl = wsdl.toURL(); 
            Service service = Service.create(null, serviceName);
            String request = new String("some string request"); 
            
            Object proxy =service.getPort(portName, DocLitWrappedProxy.class); 
            BindingProvider p = (BindingProvider)proxy;
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, axisEndpoint);
                
            DocLitWrappedProxy dwp = (DocLitWrappedProxy)proxy;  
            String response = dwp.twoWay(request);
            System.out.println("Response =" + response);
            
            // Try again
            response = dwp.twoWay(request);
            System.out.println("Response =" + response);
        } catch(Exception e){ 
            e.printStackTrace(); 
            fail("Exception received" + e);
        }
        
    }
    
    public void testOneWay(){
        
    }
    
    public void testHolder(){
        
    }
    
    public void testTwoWayAsyncCallback(){
        
        try{ 
            if(runningOnAxis){
                return;
            }
            File wsdl= new File(wsdlLocation); 
            URL wsdlUrl = wsdl.toURL(); 
            Service service = Service.create(null, serviceName);
            
            String request = new String("some string request"); 
            
            Object proxy =service.getPort(portName, DocLitWrappedProxy.class); 
            BindingProvider p = (BindingProvider)proxy;
                p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, axisEndpoint);
                
            DocLitWrappedProxy dwp = (DocLitWrappedProxy)proxy;
            AsyncHandler handler = new AsyncCallback();
            Future<?> response = dwp.twoWayAsync(request, handler);
            
            // Try again
            handler = new AsyncCallback();
            response = dwp.twoWayAsync(request, handler);
            
        }catch(Exception e){ 
            e.printStackTrace(); 
            fail("Exception received" + e);
        }
        
    }
    
    public void testAsyncPooling(){
        
    }
}

