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

package org.apache.axis2.jaxws.provider;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axiom.attachments.ByteArrayDataSource;
import org.apache.axis2.jaxws.TestLogger;
import org.test.mtom.ImageDepot;
import org.test.mtom.ObjectFactory;
import org.test.mtom.SendImage;
import org.test.mtom.SendImageResponse;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.soap.MTOMFeature;
import javax.xml.ws.soap.SOAPBinding;

import java.awt.*;
import java.io.File;

/**
 * The intended purpose of this testcase is to test the MTOM functions in Axis2. 
 * It demostrate an alternative way of sending an attachment using DataHandler.
 * 
 * This testcase uses a JAXWS Dispatch invocation with JAXB generated request object
 * as parameter. The endpoint for these testcase is a JAXWS Source Provider.
 * 
 * These JAXB generated artifacts is based on jaxws\test-resources\xsd\samplemtom.xsd
 * schema.
 * 
 * Available Content types are:
 *       "image/gif"
 *       "image/jpeg"
 *       "text/plain"
 *       "multipart/*"
 *       "text/xml"
 *       "application/xml"
 * This initial testcase only covers the "multipart/*" and  "text/plain" mime types.
 * The ultimate goal is to provide testcases for the remaining mime types. 
 *
 */
public class JAXBProviderTests extends ProviderTestCase {

    String endpointUrl = "http://localhost:6060/axis2/services/JAXBProviderService.JAXBProviderPort";
    private QName serviceName = new QName("http://ws.apache.org/axis2", "JAXBProviderService");
    
    String PROVIDER_ENDPOINT_URL = "http://localhost:6060/axis2/services/SoapMessageCheckMTOMProviderService.SoapMessageCheckMTOMProviderPort";
    private QName PROVIDER_SERVICE_NAME = new QName("http://soapmsgcheckmtom.provider.jaxws.axis2.apache.org", "SoapMessageCheckMTOMProviderService");

    DataSource stringDS, imageDS;
    
    public JAXBProviderTests() {
        super();
        //Create a DataSource from a String
        String string = "Sending a JAXB generated string object to Source Provider endpoint";
        stringDS = new ByteArrayDataSource(string.getBytes(),"text/plain");

        try {
            //Create a DataSource from an image 
            File file = new File(imageResourceDir + File.separator + "test.jpg");
            ImageInputStream fiis = new FileImageInputStream(file);
            Image image = ImageIO.read(fiis);
            imageDS = new DataSourceImpl("image/jpeg", "test.jpg", image);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Test suite() {
        return getTestSetup(new TestSuite(JAXBProviderTests.class));
    }

    /**
     * test String
     * @throws Exception
     */
    public void testMTOMAttachmentString() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        //Create a DataHandler with the String DataSource object
        DataHandler dataHandler = new DataHandler(stringDS);
    	
        //Store the data handler in ImageDepot bean
    	ImageDepot imageDepot = new ObjectFactory().createImageDepot();
    	imageDepot.setImageData(dataHandler);
        
        Service svc = Service.create(serviceName);
        svc.addPort(portName, null, endpointUrl);
        
        JAXBContext jbc = JAXBContext.newInstance("org.test.mtom");
        
        Dispatch<Object> dispatch = svc
                .createDispatch(portName, jbc, Service.Mode.PAYLOAD);
        
        //Create a request bean with imagedepot bean as value
        ObjectFactory factory = new ObjectFactory();
        SendImage request = factory.createSendImage();
        request.setInput(imageDepot);

        TestLogger.logger.debug(">> Invoking Dispatch<Object> JAXBProviderService");
        
        SendImageResponse response = (SendImageResponse) dispatch.invoke(request);

        TestLogger.logger.debug(">> Response [" + response.toString() + "]");
        
        // Try again to verify
        response = (SendImageResponse) dispatch.invoke(request);

        TestLogger.logger.debug(">> Response [" + response.toString() + "]");
    }
    
    /**
     * test Image
     * @throws Exception
     */
    public void testMTOMAttachmentImage() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        //Create a DataHandler with the String DataSource object
        DataHandler dataHandler = new DataHandler(imageDS);
    	
        //Store the data handler in ImageDepot bean
    	ImageDepot imageDepot = new ObjectFactory().createImageDepot();
    	imageDepot.setImageData(dataHandler);
        
        Service svc = Service.create(serviceName);
        svc.addPort(portName, null, endpointUrl);
        
        JAXBContext jbc = JAXBContext.newInstance("org.test.mtom");
        
        Dispatch<Object> dispatch = svc
                .createDispatch(portName, jbc, Service.Mode.PAYLOAD);
        
        // Enable attachment optimization
        SOAPBinding binding = (SOAPBinding) dispatch.getBinding();
        binding.setMTOMEnabled(true);
        
        //Create a request bean with imagedepot bean as value
        ObjectFactory factory = new ObjectFactory();
        SendImage request = factory.createSendImage();
        request.setInput(imageDepot);

        TestLogger.logger.debug(">> Invoking Dispatch<Object> JAXBProviderService");
        
        SendImageResponse response = (SendImageResponse) dispatch.invoke(request);

        TestLogger.logger.debug(">> Response [" + response.toString() + "]");
        
        // Try again to verify
        response = (SendImageResponse) dispatch.invoke(request);

        TestLogger.logger.debug(">> Response [" + response.toString() + "]");
    }
    
    /**
     * This test dispatches to the SOAPMessage CheckMTOM endpoint
     * which verifies that an attachment was sent (versus inline)
     * @throws Exception
     */
    public void testMTOMAttachmentImageProvider_API() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        //Create a DataHandler with the String DataSource object
        DataHandler dataHandler = new DataHandler(imageDS);
        
        //Store the data handler in ImageDepot bean
        ImageDepot imageDepot = new ObjectFactory().createImageDepot();
        imageDepot.setImageData(dataHandler);
        
        Service svc = Service.create(PROVIDER_SERVICE_NAME);
        svc.addPort(portName, null, PROVIDER_ENDPOINT_URL);
        
        JAXBContext jbc = JAXBContext.newInstance("org.test.mtom");
        
        
        Dispatch<Object> dispatch = svc
                .createDispatch(portName, jbc, Service.Mode.PAYLOAD);
        
        // Enable attachment optimization
        SOAPBinding binding = (SOAPBinding) dispatch.getBinding();
        binding.setMTOMEnabled(true);
        
        //Create a request bean with imagedepot bean as value
        ObjectFactory factory = new ObjectFactory();
        SendImage request = factory.createSendImage();
        request.setInput(imageDepot);
        
        // The provider service returns the same image back if successful
        SendImage response = (SendImage) dispatch.invoke(request);

        assertTrue(response != null);
        
        // Try again to verify
        response = (SendImage) dispatch.invoke(request);

        assertTrue(response != null);
    }
    
    /**
     * This test dispatches to the SOAPMessage CheckMTOM endpoint
     * which verifies that an attachment was sent (versus inline)
     * @throws Exception
     */
    public void testMTOMAttachmentImageProvider_MTOMFeature() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        //Create a DataHandler with the String DataSource object
        DataHandler dataHandler = new DataHandler(imageDS);
        
        //Store the data handler in ImageDepot bean
        ImageDepot imageDepot = new ObjectFactory().createImageDepot();
        imageDepot.setImageData(dataHandler);
        
        Service svc = Service.create(PROVIDER_SERVICE_NAME);
        svc.addPort(portName, null, PROVIDER_ENDPOINT_URL);
        
        JAXBContext jbc = JAXBContext.newInstance("org.test.mtom");
        
        MTOMFeature mtom = new MTOMFeature();
        
        Dispatch<Object> dispatch = svc
                .createDispatch(portName, jbc, Service.Mode.PAYLOAD, mtom);
        
        //Create a request bean with imagedepot bean as value
        ObjectFactory factory = new ObjectFactory();
        SendImage request = factory.createSendImage();
        request.setInput(imageDepot);
        
        // The provider service returns the same image back if successful
        SendImage response = (SendImage) dispatch.invoke(request);

        assertTrue(response != null);
        
        // Try again to verify
        response = (SendImage) dispatch.invoke(request);

        assertTrue(response != null);
    }
    
    /**
     * This test dispatches to the SOAPMessage CheckMTOM endpoint
     * which verifies that an attachment was sent (versus inline)
     * @throws Exception
     */
    public void testMTOMAttachmentImageProvider_MTOMFeatureThreshhold() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        //Create a DataHandler with the String DataSource object
        DataHandler dataHandler = new DataHandler(imageDS);
        
        //Store the data handler in ImageDepot bean
        ImageDepot imageDepot = new ObjectFactory().createImageDepot();
        imageDepot.setImageData(dataHandler);
        
        Service svc = Service.create(PROVIDER_SERVICE_NAME);
        svc.addPort(portName, null, PROVIDER_ENDPOINT_URL);
        
        JAXBContext jbc = JAXBContext.newInstance("org.test.mtom");
        
        // Make sure MTOMFeature with a small threshhold (1) works correctly
        // by sending an optimized MTOM message.
        MTOMFeature mtom = new MTOMFeature(1);
        
        Dispatch<Object> dispatch = svc
                .createDispatch(portName, jbc, Service.Mode.PAYLOAD, mtom);
        
        //Create a request bean with imagedepot bean as value
        ObjectFactory factory = new ObjectFactory();
        SendImage request = factory.createSendImage();
        request.setInput(imageDepot);
        
        // The provider service returns the same image back if successful
        SendImage response = (SendImage) dispatch.invoke(request);

        assertTrue(response != null);
        
        // Try again to verify
        response = (SendImage) dispatch.invoke(request);

        assertTrue(response != null);
    }
}
