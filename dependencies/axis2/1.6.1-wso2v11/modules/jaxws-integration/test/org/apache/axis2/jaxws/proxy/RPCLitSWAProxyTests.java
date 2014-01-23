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
import org.apache.axis2.jaxws.framework.AbstractTestCase;
import org.apache.axis2.jaxws.provider.DataSourceImpl;
import org.apache.axis2.jaxws.proxy.rpclitswa.sei.RPCLitSWA;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Holder;
import javax.xml.ws.Service;
import java.awt.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class RPCLitSWAProxyTests extends AbstractTestCase {

    private QName serviceName = new QName(
            "http://org/apache/axis2/jaxws/proxy/rpclitswa", "RPCLitSWAService");
    private String axisEndpoint = "http://localhost:6060/axis2/services/RPCLitSWAService.RPCLitSWA";
    private QName portName = new QName("http://org/apache/axis2/jaxws/proxy/rpclitswa",
            "RPCLitSWA");
    private String wsdlLocation = System.getProperty("basedir",".")+"/"+
    "test/org/apache/axis2/jaxws/proxy/rpclitswa/META-INF/RPCLitSWA.wsdl";
        
    static {
        String imageResourceDir =
                System.getProperty("basedir", ".") + "/" + "test-resources" + File.separator
                        + "image";

        //Create a DataSource from an image 
        File file = new File(imageResourceDir + File.separator + "test.jpg");
        ImageInputStream fiis = null;
        try {
            fiis = new FileImageInputStream(file);
            Image image = ImageIO.read(fiis);
            imageDS = new DataSourceImpl("image/jpeg", "test.jpg", image);
        } catch (Exception e) {
            throw new RuntimeException(e);    
        }
    }
    private static DataSource imageDS;
    
    public static Test suite() {
        return getTestSetup(new TestSuite(RPCLitSWAProxyTests.class));
    }
    
    /**
     * Utility method to get the proxy
     * @return RPCLit proxy
     * @throws MalformedURLException
     */
    public RPCLitSWA getProxy() throws MalformedURLException {
        File wsdl= new File(wsdlLocation);
        assertTrue("WSDL does not exist:" + wsdlLocation,wsdl.exists());
        URL wsdlUrl = wsdl.toURL(); 
        Service service = Service.create(wsdlUrl, serviceName);
        Object proxy =service.getPort(portName, RPCLitSWA.class);
        BindingProvider p = (BindingProvider)proxy; 
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,axisEndpoint);
        
        return (RPCLitSWA)proxy;
    }
    
    /**
     * Utility Method to get a Dispatch<String>
     * @return
     * @throws MalformedURLException
     */
    public Dispatch<String> getDispatch() throws MalformedURLException {
        File wsdl= new File(wsdlLocation); 
        URL wsdlUrl = wsdl.toURL(); 
        Service service = Service.create(null, serviceName);
        service.addPort(portName, null, axisEndpoint);
        Dispatch<String> dispatch = service.createDispatch(portName, String.class, 
                                                           Service.Mode.PAYLOAD);
        return dispatch;
    }
    
    public void testNOOP() {
        
    }
    
    /**
     * Simple test that ensures that we can echo a string to an rpc/lit web service
     * NOTE:
     * To enable this test, remove the _.
     * Once you do this, you can add the following code to OperationDescriptionImpl.
     * buildAttachmentInformation()
     * // TODO: Start HACK for RPCLitSWAProxyTest
        addPartAttachmentDescription("dummyAttachmentIN",
                                     new AttachmentDescriptionImpl(AttachmentType.SWA, 
                                                                   new String[] {"text/plain"}));
        addPartAttachmentDescription("dummyAttachmentINOUT",
                                     new AttachmentDescriptionImpl(AttachmentType.SWA, 
                                                                   new String[] {"image/jpeg"}));
        addPartAttachmentDescription("dummyAttachmentOUT",
                                     new AttachmentDescriptionImpl(AttachmentType.SWA, 
                                                                   new String[] {"text/plain"}));
        // TODO: End HACK for RPCListSWAProxyTest
     */
    public void testRPCLitSWAEcho() throws Exception {
        
            RPCLitSWA proxy = getProxy();
            String request = "This is a not attachment data";
            
            String attachmentIN = "Data for plain text attachment";
            DataHandler attachmentINOUT = getImageDH();
            
            Holder<DataHandler> attachmentINOUT_Holder = new Holder<DataHandler>();
            attachmentINOUT_Holder.value = attachmentINOUT;
            Holder<String> response_Holder = new Holder<String>();
            Holder<String> attachmentOUT_Holder = new Holder<String>();
           
            proxy.echo(request, attachmentIN, attachmentINOUT_Holder, response_Holder, 
                       attachmentOUT_Holder);
            
            assertTrue("Bad Response Holder", response_Holder != null);
            assertTrue("Response value is null", response_Holder.value != null);
            assertTrue("Response is not the same as request. Receive="+response_Holder.value, 
                       request.equals(response_Holder.value));
            
            assertTrue("The output attachment holder is null", attachmentOUT_Holder != null);
            assertTrue("The output attachment is null", attachmentOUT_Holder.value != null);
            assertTrue("The output attachment is not the same as the input.  Received=" + 
                       attachmentOUT_Holder.value, 
                    attachmentIN.equals(attachmentOUT_Holder.value));
            
           
            assertTrue("The inout attachment holder is null", attachmentINOUT_Holder != null);
            assertTrue("The inout attachment is null", attachmentINOUT_Holder.value != null);
            // Ensure that this is not the same object
            assertTrue(attachmentINOUT_Holder.value != attachmentINOUT); 
            
            // ------------------
            // Try again with the same proxy
            // ------------------
            request = "This is a not attachment data";
            
            attachmentIN = "Data for plain text attachment";
            attachmentINOUT = getImageDH();
            
            attachmentINOUT_Holder = new Holder<DataHandler>();
            attachmentINOUT_Holder.value = attachmentINOUT;
            response_Holder = new Holder<String>();
            attachmentOUT_Holder = new Holder<String>();
           
            proxy.echo(request, attachmentIN, attachmentINOUT_Holder, response_Holder, 
                       attachmentOUT_Holder);
            
            assertTrue("Bad Response Holder", response_Holder != null);
            assertTrue("Response value is null", response_Holder.value != null);
            assertTrue("Response is not the same as request. Receive="+response_Holder.value, 
                       request.equals(response_Holder.value));
            
            assertTrue("The output attachment holder is null", attachmentOUT_Holder != null);
            assertTrue("The output attachment is null", attachmentOUT_Holder.value != null);
            assertTrue("The output attachment is not the same as the input.  Received=" + 
                       attachmentOUT_Holder.value, 
                    attachmentIN.equals(attachmentOUT_Holder.value));
            
           
            assertTrue("The inout attachment holder is null", attachmentINOUT_Holder != null);
            assertTrue("The inout attachment is null", attachmentINOUT_Holder.value != null);
            // Ensure that this is not the same object
            assertTrue(attachmentINOUT_Holder.value != attachmentINOUT);  
        
    }
    
    private DataHandler getImageDH() {
        return new DataHandler(imageDS);
    }
    
}
