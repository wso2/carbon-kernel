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

package org.apache.axis2.jaxws.attachments;

import junit.framework.TestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.OMText;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.impl.llom.soap11.SOAP11Factory;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.databinding.JAXBBlockContext;
import org.apache.axis2.jaxws.message.factory.BlockFactory;
import org.apache.axis2.jaxws.message.factory.JAXBBlockFactory;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.providerapi.DataSourceImpl;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.unitTest.TestLogger;
import org.test.mtom.ImageDepot;
import org.test.mtom.ObjectFactory;
import org.test.mtom.SendImage;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;

public class MTOMSerializationTests extends TestCase {

    private DataSource imageDS;
    
    public void setUp() throws Exception {
        String imageResourceDir = System.getProperty("basedir",".")+"/"+"test-resources"+File.separator+"image";
        
        //Create a DataSource from an image 
        File file = new File(imageResourceDir+File.separator+"test.jpg");
        ImageInputStream fiis = new FileImageInputStream(file);
        Image image = ImageIO.read(fiis);
        imageDS = new DataSourceImpl("image/jpeg","test.jpg",image);
    }
    
    public MTOMSerializationTests(String name) {
        super(name);
    }
    
    /*
     * Simulate building up an OM that is sourced from JAXB and contains
     * binary data that should be optimized when serialized.  
     */
    public void testPlainOMSerialization() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        OMElement payload = createPayload();
        
        OMOutputFormat format = new OMOutputFormat();
        format.setDoOptimize(true);
        format.setSOAP11(true);
               
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        payload.serializeAndConsume(baos, format);

        TestLogger.logger.debug("==================================");
        TestLogger.logger.debug(baos.toString());
        TestLogger.logger.debug("==================================");
    }
    
    /*
     * Simulate building up an OM SOAPEnvelope that has the contents of
     * the body sourced from JAXB and contains binary data that should be 
     * optimized when serialized.  
     */
    public void testSoapOMSerialization() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        OMElement payload = createPayload();
        
        SOAPFactory factory = new SOAP11Factory();
        SOAPEnvelope env = factory.createSOAPEnvelope();
        SOAPBody body = factory.createSOAPBody(env);
        
        body.addChild(payload);
        
        OMOutputFormat format = new OMOutputFormat();
        format.setDoOptimize(true);
        format.setSOAP11(true);
               
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        env.serializeAndConsume(baos, format);

        TestLogger.logger.debug("==================================");
        TestLogger.logger.debug(baos.toString());
        TestLogger.logger.debug("==================================");
    }
    
    public void testMTOMAttachmentWriter() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        //Create a DataHandler with the String DataSource object
        DataHandler dataHandler = new DataHandler(imageDS);
                        
        //Store the data handler in ImageDepot bean
        ImageDepot imageDepot = new ObjectFactory().createImageDepot();
        imageDepot.setImageData(dataHandler);
        
        //JAXBContext jbc = JAXBContext.newInstance("org.test.mtom");
        JAXBBlockContext context = new JAXBBlockContext(SendImage.class.getPackage().getName());
        
        //Create a request bean with imagedepot bean as value
        ObjectFactory factory = new ObjectFactory();
        SendImage request = factory.createSendImage();
        request.setInput(imageDepot);
        
        BlockFactory blkFactory = (JAXBBlockFactory) FactoryRegistry.getFactory(JAXBBlockFactory.class);
        Block block = blkFactory.createFrom(request, context, null);
        
        MessageFactory msgFactory = (MessageFactory) FactoryRegistry.getFactory(MessageFactory.class);
        Message msg = msgFactory.create(Protocol.soap11);
        
        msg.setBodyBlock(block);
        
        msg.setMTOMEnabled(true);
        
        SOAPEnvelope soapOM = (SOAPEnvelope) msg.getAsOMElement();
        
        OMOutputFormat format = new OMOutputFormat();
        format.setDoOptimize(true);
        format.setSOAP11(true);
               
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        soapOM.serializeAndConsume(baos, format);
        String outputText = baos.toString();
        // Make sure the attachment is serialized
        assertTrue(outputText.indexOf("Content-Type: image/jpeg") > 0);

        TestLogger.logger.debug("==================================");
        TestLogger.logger.debug(outputText);
        TestLogger.logger.debug("==================================");
    }
    
    public void testMTOMAttachmentWriter2() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        //Create a DataHandler with the String DataSource object
        DataHandler dataHandler = new DataHandler(imageDS);
                        
        //Store the data handler in ImageDepot bean
        ImageDepot imageDepot = new ObjectFactory().createImageDepot();
        imageDepot.setImageData(dataHandler);
        
        //JAXBContext jbc = JAXBContext.newInstance("org.test.mtom");
        JAXBBlockContext context = new JAXBBlockContext(SendImage.class.getPackage().getName());
        
        //Create a request bean with imagedepot bean as value
        ObjectFactory factory = new ObjectFactory();
        SendImage request = factory.createSendImage();
        request.setInput(imageDepot);
        
        BlockFactory blkFactory = (JAXBBlockFactory) FactoryRegistry.getFactory(JAXBBlockFactory.class);
        Block block = blkFactory.createFrom(request, context, null);
        
        MessageFactory msgFactory = (MessageFactory) FactoryRegistry.getFactory(MessageFactory.class);
        Message msg = msgFactory.create(Protocol.soap11);
        
        msg.setBodyBlock(block);
        
        msg.setMTOMEnabled(true);
        
        // Convert message to SAAJ to simulate an outbound handler
        msg.getAsSOAPMessage();
        
        // Now convert it back to AXIOM
        
        SOAPEnvelope soapOM = (SOAPEnvelope) msg.getAsOMElement();
        
        OMOutputFormat format = new OMOutputFormat();
        format.setDoOptimize(true);
        format.setSOAP11(true);
               
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        soapOM.serializeAndConsume(baos, format);
        String outputText = baos.toString();
        // Make sure the attachment is serialized
        assertTrue(outputText.indexOf("Content-Type: image/jpeg") > 0);

        TestLogger.logger.debug("==================================");
        TestLogger.logger.debug(outputText);
        TestLogger.logger.debug("==================================");
    }
    
    private OMElement createPayload() {
        //Create a DataHandler with the String DataSource object
        DataHandler dataHandler = new DataHandler(imageDS);
                        
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("urn://mtom.test.org", "mtom");
        
        OMElement sendImage = fac.createOMElement("sendImage", omNs);
        
        OMElement input = fac.createOMElement("input", omNs);
        sendImage.addChild(input);
        
        OMElement imageData = fac.createOMElement("imageData", omNs);
        input.addChild(imageData);
        
        OMText binaryData = fac.createOMText(dataHandler, true);
        imageData.addChild(binaryData);
        
        return sendImage;
    }
}
