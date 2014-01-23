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

package org.apache.axis2.jaxws.sample;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.framework.AbstractTestCase;
import org.apache.axis2.jaxws.sample.mtom1.ImageDepot;
import org.apache.axis2.jaxws.sample.mtom1.Invoke;
import org.apache.axis2.jaxws.sample.mtom1.ObjectFactory;
import org.apache.axis2.jaxws.sample.mtom1.SendImageResponse;
import org.w3._2005._05.xmlmime.Base64Binary;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;
import java.awt.*;
import java.io.File;

public class MtomSampleByteArrayTests extends AbstractTestCase {

    private static final QName QNAME_SERVICE = new QName("urn://mtom1.sample.jaxws.axis2.apache.org", "SendImageService");
    private static final QName QNAME_PORT    = new QName("urn://mtom1.sample.jaxws.axis2.apache.org", "sendImageSoap");
    private static final String URL_ENDPOINT = "http://localhost:6060/axis2/services/SendImageService.sendImagePort";
    private static final String IMAGE_DIR = System.getProperty("basedir",".")+File.separator+"test-resources"+File.separator+"image";

    public static Test suite() {
        return getTestSetup(new TestSuite(MtomSampleByteArrayTests.class));
    }
    
    /*
     * Enable attachment Optimization through the SOAPBinding method 
     * -- setMTOMEnabled([true|false])
     * Using SOAP11
     */
    public void _testAttachmentByteArrayAPI11() throws Exception {
        TestLogger.logger.debug("----------------------------------");
        TestLogger.logger.debug("test: " + getName());
    	
      	String imageResourceDir = IMAGE_DIR;
      		
      	Service svc = Service.create(QNAME_SERVICE);
      	svc.addPort(QNAME_PORT, SOAPBinding.SOAP11HTTP_BINDING, URL_ENDPOINT);
      	
      	JAXBContext jbc = JAXBContext.newInstance("org.apache.axis2.jaxws.sample.mtom1");
      	Dispatch<Object> dispatch = svc.createDispatch(QNAME_PORT, jbc, Service.Mode.PAYLOAD);
      	
      	SOAPBinding binding = (SOAPBinding)dispatch.getBinding();
      	binding.setMTOMEnabled(true);
      	
      	Image image = ImageIO.read (new File(imageResourceDir+File.separator+"test.jpg"));
      	ImageDepot imageDepot = new ObjectFactory().createImageDepot();
      	imageDepot.setImageData(image);
        setText(imageDepot);
      	
      	//Create a request bean with imagedepot bean as value
      	ObjectFactory factory = new ObjectFactory();
      	Invoke request = factory.createInvoke();
      	request.setInput(imageDepot);
      	
      	SendImageResponse response = (SendImageResponse) dispatch.invoke(request);
      	
      	assertNotNull(response);
        assertNotNull(response.getOutput().getImageData());
        checkText(response.getOutput());
        
        // Repeat to verify behavior
        response = (SendImageResponse) dispatch.invoke(request);
        
        assertNotNull(response);
        assertNotNull(response.getOutput().getImageData());
        checkText(response.getOutput());
    }
    
    /*
     * Disable attachment Optimization through the SOAPBinding method 
     * -- setMTOMEnabled([true|false])
     * Using SOAP11
     */
    public void testAttachmentByteArrayAPI11_ClientSendsNonOptimizedMTOM() throws Exception {
        TestLogger.logger.debug("----------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        String imageResourceDir = IMAGE_DIR;
            
        Service svc = Service.create(QNAME_SERVICE);
        svc.addPort(QNAME_PORT, SOAPBinding.SOAP11HTTP_BINDING, URL_ENDPOINT);
        
        JAXBContext jbc = JAXBContext.newInstance("org.apache.axis2.jaxws.sample.mtom1");
        Dispatch<Object> dispatch = svc.createDispatch(QNAME_PORT, jbc, Service.Mode.PAYLOAD);
        
        SOAPBinding binding = (SOAPBinding)dispatch.getBinding();
        binding.setMTOMEnabled(false);  // Disabling MTOM optimization on client, but server will respond with optimized MTOM
        
        Image image = ImageIO.read (new File(imageResourceDir+File.separator+"test.jpg"));
        ImageDepot imageDepot = new ObjectFactory().createImageDepot();
        imageDepot.setImageData(image);
        setText(imageDepot);
        
        //Create a request bean with imagedepot bean as value
        ObjectFactory factory = new ObjectFactory();
        Invoke request = factory.createInvoke();
        request.setInput(imageDepot);
        
        SendImageResponse response = (SendImageResponse) dispatch.invoke(request);
        
        assertNotNull(response);
        assertNotNull(response.getOutput().getImageData());
        checkText(response.getOutput());
        
        // Repeat to verify behavior
        response = (SendImageResponse) dispatch.invoke(request);
        
        assertNotNull(response);
        assertNotNull(response.getOutput().getImageData());
        checkText(response.getOutput());
    }
    
    /*
     * Enable attachment optimization using the SOAP11 binding
     * property for MTOM.
     */
    public void testAttachmentByteArrayProperty11() throws Exception {
        
        TestLogger.logger.debug("----------------------------------");
        TestLogger.logger.debug("test: " + getName());
    	
      	String imageResourceDir = IMAGE_DIR;
      		
      	Service svc = Service.create(QNAME_SERVICE);
      	svc.addPort(QNAME_PORT, SOAPBinding.SOAP11HTTP_MTOM_BINDING, URL_ENDPOINT);
      	
      	JAXBContext jbc = JAXBContext.newInstance("org.apache.axis2.jaxws.sample.mtom1");
      	Dispatch<Object> dispatch = svc.createDispatch(QNAME_PORT, jbc, Service.Mode.PAYLOAD);
      	
      	Image image = ImageIO.read (new File(imageResourceDir+File.separator+"test.jpg"));
      	ImageDepot imageDepot = new ObjectFactory().createImageDepot();
      	imageDepot.setImageData(image);
        setText(imageDepot);
      	
      	//Create a request bean with imagedepot bean as value
      	ObjectFactory factory = new ObjectFactory();
      	Invoke request = factory.createInvoke();
      	request.setInput(imageDepot);
      	
      	SendImageResponse response = (SendImageResponse) dispatch.invoke(request);
      	
      	assertNotNull(response);
        assertNotNull(response.getOutput().getImageData());
        checkText(response.getOutput());
        
        // Repeat to verify behavior
        response = (SendImageResponse) dispatch.invoke(request);
        
        assertNotNull(response);
        assertNotNull(response.getOutput().getImageData());
        checkText(response.getOutput());
        
    }
    
    private static final String text = "Binary Attachments are radical";
    private void setText(ImageDepot depot) {
        
        Base64Binary binary = new Base64Binary();
        binary.setContentType("");
        binary.setValue(text.getBytes());
        depot.setTextData(binary);
    }
    
    private void checkText(ImageDepot depot) {
        Base64Binary binary = depot.getTextData();
        assertTrue(binary != null);
        String contentType = binary.getContentType();
        assertTrue("".equals(contentType));
        byte[] bytes = binary.getValue();
        assertTrue(bytes != null);
        String theText = new String(bytes);
        assertTrue(text.equals(theText));
    }
    
}
