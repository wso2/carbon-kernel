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

package org.apache.axis2.jaxws.message;

import junit.framework.TestCase;

import org.apache.axiom.om.OMAttachmentAccessor;
import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.OMSourcedElement;
import org.apache.axiom.om.OMXMLStreamReader;
import org.apache.axiom.om.util.CopyUtils;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.Constants;
import org.apache.axis2.Constants.Configuration;
import org.apache.axis2.datasource.jaxb.JAXBDataSource;
import org.apache.axis2.jaxws.unitTest.TestLogger;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.message.databinding.JAXBBlockContext;
import org.apache.axis2.jaxws.message.databinding.impl.JAXBBlockImpl;
import org.apache.axis2.jaxws.message.factory.JAXBBlockFactory;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.message.util.MessageUtils;
import org.apache.axis2.jaxws.providerapi.DataSourceImpl;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.test.mtom.ImageDepot;
import org.test.mtom.SendImage;
import test.EchoStringResponse;
import test.ObjectFactory;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.xml.namespace.QName;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * These tests simulate the outbound processing 
 * from JAX-WS with Message Persistance.  The tests validate that the
 * message is properly cloned/written/read and that the OM is not
 * unnecessarily expanded (which is a performance concern).
 */
public class MessagePersistanceTests extends TestCase {

    DataSource stringDS, imageDS;
    public String imageResourceDir = "test-resources" + File.separator + "image";
    private final String sampleText = "Sample Text";
    private final static QName XOP_INCLUDE = 
        new QName("http://www.w3.org/2004/08/xop/include", "Include");
    
    protected void setUp() throws Exception {
        super.setUp();
        // Create a DataSource from a String
        stringDS = new org.apache.axiom.attachments.ByteArrayDataSource(sampleText.getBytes(), "text/plain");

        // Create a DataSource from an image 
        File file = new File(imageResourceDir + File.separator + "test.jpg");
        ImageInputStream fiis = new FileImageInputStream(file);
        Image image = ImageIO.read(fiis);
        imageDS = new DataSourceImpl("image/jpeg", "test.jpg", image);
    }
    
    /**
     * Create a JAXBBlock containing a JAX-B business object and simulate a normal Dispatch<Object>
     * output flow
     * 
     * @throws Exception
     */
    public void testPersist_File() throws Exception {
        
        // Create the JAX-B object that is typical from a JAX-WS app
        String sampleJAXBText = "sample return value";
        ObjectFactory of = new ObjectFactory();
        EchoStringResponse obj = of.createEchoStringResponse();
        obj.setEchoStringReturn("sample return value");
        
        // The JAXB object is stored in the Axiom tree as an OMSourcedElement.
        // The typical structure is
        //   OM SOAPEnvelope
        //   OM SOAPBody
        //   OMSourcedElement that is sourced by a JAXBBlockImpl which is backecd by a JAXB Object.
        Message m = createMessage(obj);
        
        // The Message is set on the JAXWS MessageContext
        MessageContext jaxwsMC = new MessageContext();
        jaxwsMC.setMessage(m);
        
        // Check to see if the message is a fault. The engine will always call this method.
        // The Message must respond appropriately without doing a conversion.
        boolean isFault = m.isFault();
        assertTrue(!isFault);
        assertTrue("XMLPart Representation is " + m.getXMLPartContentType(),
                   "SPINE".equals(m.getXMLPartContentType()));
        
        // The JAX-WS MessageContext is converted into an Axis2 MessageContext
        org.apache.axis2.context.MessageContext axisMC = jaxwsMC.getAxisMessageContext();
        MessageUtils.putMessageOnMessageContext(m, jaxwsMC.getAxisMessageContext());
        
        // Add other properties to the MessageContext
        axisMC.setProperty(Constants.JAXWS_WEBMETHOD_EXCEPTION, new NullPointerException());
        axisMC.setProperty("keyString", "valueString");
        axisMC.setProperty("keyInteger", new Integer(5));
        
        
        // Make sure the Axiom structure is intact
        SOAPEnvelope env = axisMC.getEnvelope();
        SOAPBody body = env.getBody();
        OMElement child = body.getFirstElement();
        assertTrue(child instanceof OMSourcedElement);
        OMSourcedElement omse = (OMSourcedElement) child;
        assertTrue(!omse.isExpanded());
        OMDataSource ds = omse.getDataSource();
        assertTrue(ds instanceof JAXBBlockImpl);
        
        // Now simulate persisting the message
        File theFile = null;
        String theFilename = null;
        theFile = File.createTempFile("MessagePersistTest", null);
        //theFile.deleteOnExit();
        theFilename = theFile.getName();
        TestLogger.logger.debug("temp file = [" + theFilename + "]");
        
        // Setup an output stream to a physical file
        FileOutputStream outStream = new FileOutputStream(theFile);

        // Attach a stream capable of writing objects to the 
        // stream connected to the file
        ObjectOutputStream outObjStream = new ObjectOutputStream(outStream);

        // Try to save the message context
        TestLogger.logger.debug("saving message context.....");
        outObjStream.writeObject(axisMC);

        // Close out the streams
        outObjStream.flush();
        outObjStream.close();
        outStream.flush();
        outStream.close();
        TestLogger.logger.debug("....saved message context.....");
        long filesize = theFile.length();
        TestLogger.logger.debug("file size after save [" + filesize
                + "]   temp file = [" + theFilename + "]");
        
        // Make sure the Axiom structure is intact.  
        env = axisMC.getEnvelope();
        body = env.getBody();
        child = body.getFirstElement();
        assertTrue(child instanceof OMSourcedElement);
        omse = (OMSourcedElement) child;
        assertTrue(!omse.isExpanded());
        ds = omse.getDataSource();
        assertTrue(ds instanceof JAXBBlockImpl);
        
        // Simulate transport
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        env.serializeAndConsume(baos, new OMOutputFormat());

        // To check that the output is correct, get the String contents of the
        // reader
        String newText = baos.toString();
        TestLogger.logger.debug(newText);
        assertTrue(newText.contains(sampleJAXBText));
        assertTrue(newText.contains("soap"));
        assertTrue(newText.contains("Envelope"));
        assertTrue(newText.contains("Body"));
        
        
        // Now read in the persisted message
        // Setup an input stream to the file
        FileInputStream inStream = new FileInputStream(theFile);

        // attach a stream capable of reading objects from the 
        // stream connected to the file
        ObjectInputStream inObjStream = new ObjectInputStream(inStream);

        // try to restore the message context
        TestLogger.logger.debug("restoring a message context.....");

        org.apache.axis2.context.MessageContext restoredMC = 
            (org.apache.axis2.context.MessageContext) inObjStream.readObject();
        inObjStream.close();
        inStream.close();
        TestLogger.logger.debug("....restored message context.....");
        
        // At this point in time, the restoredMessage will be a full tree.
        // TODO If this changes, please add more assertions here.
        
        // Check persisted properties
        
        // The exception might be persisted....but in the very least it should
        // not cause an exception.
        Throwable t = (Throwable) restoredMC.getProperty(Constants.JAXWS_WEBMETHOD_EXCEPTION);
        assertTrue(t == null || t instanceof NullPointerException);
        
        // Make sure the keyString property was persisted
        String valueString =  (String) restoredMC.getProperty("keyString");
        assertTrue("valueString".equals(valueString));
        
        // Make sure the keyInteger property was persisted.
        Integer valueInteger =  (Integer) restoredMC.getProperty("keyInteger");
        assertTrue(valueInteger == 5);
        
        axisMC.setProperty("keyInteger", new Integer(5));
        
        // Simulate transport
        baos = new ByteArrayOutputStream();
        env = restoredMC.getEnvelope();
        env.serializeAndConsume(baos, new OMOutputFormat());
        String restoredText = baos.toString();
        TestLogger.logger.debug(restoredText);
        assertTrue(restoredText.contains(sampleJAXBText));
        assertTrue(restoredText.contains("soap"));
        assertTrue(restoredText.contains("Envelope"));
        assertTrue(restoredText.contains("Body"));
        assertTrue(restoredText.equals(newText));
        
    }
    
    /**
     * Create a JAXBBlock containing a JAX-B business object and simulate a normal Dispatch<Object>
     * output flow
     * 
     * @throws Exception
     */
    public void testPersist_InMemory() throws Exception {
        
        // Create the JAX-B object that is typical from a JAX-WS app
        String sampleJAXBText = "sample return value";
        ObjectFactory of = new ObjectFactory();
        EchoStringResponse obj = of.createEchoStringResponse();
        obj.setEchoStringReturn("sample return value");
        
        // The JAXB object is stored in the Axiom tree as an OMSourcedElement.
        // The typical structure is
        //   OM SOAPEnvelope
        //   OM SOAPBody
        //   OMSourcedElement that is sourced by a JAXBBlockImpl which is backecd by a JAXB Object.
        Message m = createMessage(obj);
        
        // The Message is set on the JAXWS MessageContext
        MessageContext jaxwsMC = new MessageContext();
        jaxwsMC.setMessage(m);
        
        // Check to see if the message is a fault. The engine will always call this method.
        // The Message must respond appropriately without doing a conversion.
        boolean isFault = m.isFault();
        assertTrue(!isFault);
        assertTrue("XMLPart Representation is " + m.getXMLPartContentType(),
                   "SPINE".equals(m.getXMLPartContentType()));
        
        // The JAX-WS MessageContext is converted into an Axis2 MessageContext
        org.apache.axis2.context.MessageContext axisMC = jaxwsMC.getAxisMessageContext();
        MessageUtils.putMessageOnMessageContext(m, jaxwsMC.getAxisMessageContext());
        
        // Make sure the Axiom structure is intact
        SOAPEnvelope env = axisMC.getEnvelope();
        SOAPBody body = env.getBody();
        OMElement child = body.getFirstElement();
        assertTrue(child instanceof OMSourcedElement);
        OMSourcedElement omse = (OMSourcedElement) child;
        assertTrue(!omse.isExpanded());
        OMDataSource ds = omse.getDataSource();
        assertTrue(ds instanceof JAXBBlockImpl);
        
        // Now simulate persisting the message in memory
        SOAPEnvelope env2 = CopyUtils.copy(env);
        
        // Make sure the Axiom structure is intact.  
        env = axisMC.getEnvelope();
        body = env.getBody();
        child = body.getFirstElement();
        assertTrue(child instanceof OMSourcedElement);
        omse = (OMSourcedElement) child;
        assertTrue(!omse.isExpanded());
        ds = omse.getDataSource();
        assertTrue(ds instanceof JAXBBlockImpl);
        
        // Simulate transport
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        env.serializeAndConsume(baos, new OMOutputFormat());

        // To check that the output is correct, get the String contents of the
        // reader
        String newText = baos.toString();
        TestLogger.logger.debug(newText);
        assertTrue(newText.contains(sampleJAXBText));
        assertTrue(newText.contains("soap"));
        assertTrue(newText.contains("Envelope"));
        assertTrue(newText.contains("Body"));
        
        
        // Now check the copied envelope
        body = env2.getBody();
        child = body.getFirstElement();
        assertTrue(child instanceof OMSourcedElement);
        omse = (OMSourcedElement) child;
        assertTrue(!omse.isExpanded());
        ds = omse.getDataSource();
        assertTrue(ds instanceof JAXBDataSource);
        
        // Simulate transport
        baos = new ByteArrayOutputStream();
        env2.serializeAndConsume(baos, new OMOutputFormat());
        String restoredText = baos.toString();
        TestLogger.logger.debug(restoredText);
        assertTrue(restoredText.contains(sampleJAXBText));
        assertTrue(restoredText.contains("soap"));
        assertTrue(restoredText.contains("Envelope"));
        assertTrue(restoredText.contains("Body"));
        assertTrue(restoredText.equals(newText));
        
    }
    
    /**
     * Create a JAXBBlock containing a JAX-B business object and simulate a normal Dispatch<Object>
     * output flow
     * 
     * @throws Exception
     */
    public void testPersist_Attachments_File() throws Exception {
        
        // TODO Add a SWARef and a raw attachment
        
        // Create the JAX-B object with an attachment
        // Create a DataHandler with the String DataSource object
        DataHandler dataHandler = new DataHandler(stringDS);

        //Store the data handler in ImageDepot bean
        org.test.mtom.ObjectFactory of = new org.test.mtom.ObjectFactory();
        ImageDepot imageDepot = new org.test.mtom.ObjectFactory().createImageDepot();
        imageDepot.setImageData(dataHandler);
        SendImage obj = of.createSendImage();
        obj.setInput(imageDepot);
        
        // The JAXB object is stored in the Axiom tree as an OMSourcedElement.
        // The typical structure is
        //   OM SOAPEnvelope
        //   OM SOAPBody
        //   OMSourcedElement that is sourced by a JAXBBlockImpl which is backecd by a JAXB Object.
        Message m = createMessage(obj);
        m.setMTOMEnabled(true);
        
        // The Message is set on the JAXWS MessageContext
        MessageContext jaxwsMC = new MessageContext();
        jaxwsMC.setMessage(m);
        
        // Check to see if the message is a fault. The engine will always call this method.
        // The Message must respond appropriately without doing a conversion.
        boolean isFault = m.isFault();
        assertTrue(!isFault);
        assertTrue("XMLPart Representation is " + m.getXMLPartContentType(),
                   "SPINE".equals(m.getXMLPartContentType()));
        
        // The JAX-WS MessageContext is converted into an Axis2 MessageContext
        org.apache.axis2.context.MessageContext axisMC = jaxwsMC.getAxisMessageContext();
        MessageUtils.putMessageOnMessageContext(m, jaxwsMC.getAxisMessageContext());
        axisMC.setProperty(Configuration.ENABLE_MTOM, "true" );
        
        // Make sure the Axiom structure is intact
        SOAPEnvelope env = axisMC.getEnvelope();
        SOAPBody body = env.getBody();
        OMElement child = body.getFirstElement();
        assertTrue(child instanceof OMSourcedElement);
        OMSourcedElement omse = (OMSourcedElement) child;
        assertTrue(!omse.isExpanded());
        OMDataSource ds = omse.getDataSource();
        assertTrue(ds instanceof JAXBBlockImpl);
        
        // Now simulate persisting the message
        File theFile = null;
        String theFilename = null;
        theFile = File.createTempFile("MessagePersistTest", null);
        //theFile.deleteOnExit();
        theFilename = theFile.getName();
        TestLogger.logger.debug("temp file = [" + theFilename + "]");
        
        // Setup an output stream to a physical file
        FileOutputStream outStream = new FileOutputStream(theFile);

        // Attach a stream capable of writing objects to the 
        // stream connected to the file
        ObjectOutputStream outObjStream = new ObjectOutputStream(outStream);

        // Try to save the message context
        TestLogger.logger.debug("saving message context.....");
        outObjStream.writeObject(axisMC);

        // Close out the streams
        outObjStream.flush();
        outObjStream.close();
        outStream.flush();
        outStream.close();
        TestLogger.logger.debug("....saved message context.....");
        long filesize = theFile.length();
        TestLogger.logger.debug("file size after save [" + filesize
                + "]   temp file = [" + theFilename + "]");
        
        // Make sure the Axiom structure is intact.  
        env = axisMC.getEnvelope();
        body = env.getBody();
        child = body.getFirstElement();
        assertTrue(child instanceof OMSourcedElement);
        omse = (OMSourcedElement) child;
        assertTrue(!omse.isExpanded());
        ds = omse.getDataSource();
        assertTrue(ds instanceof JAXBBlockImpl);
        
        // Simulate transport
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OMOutputFormat outputFormat = new OMOutputFormat();
        outputFormat.setDoOptimize(true);
        outputFormat.setMimeBoundary("MIMEBoundary_Axis2Rocks");
        env.serializeAndConsume(baos, outputFormat);

        // Make sure the output is correct
        String newText = baos.toString();
        TestLogger.logger.debug(newText);
        assertTrue(newText.contains("soap"));
        assertTrue(newText.contains("Envelope"));
        assertTrue(newText.contains("Body"));
        assertTrue(newText.indexOf("MIMEBoundary_Axis2Rocks") > 0);
        assertTrue(newText.indexOf(sampleText) > 0);
        assertTrue(newText.indexOf("<soapenv:Body><sendImage xmlns=\"urn://mtom.test.org\"><input><imageData><xop:Include") > 0);

        
        // Now read in the persisted message
        // Setup an input stream to the file
        FileInputStream inStream = new FileInputStream(theFile);

        // attach a stream capable of reading objects from the 
        // stream connected to the file
        ObjectInputStream inObjStream = new ObjectInputStream(inStream);

        // try to restore the message context
        TestLogger.logger.debug("restoring a message context.....");

        org.apache.axis2.context.MessageContext restoredMC = 
            (org.apache.axis2.context.MessageContext) inObjStream.readObject();
        inObjStream.close();
        inStream.close();
        TestLogger.logger.debug("....restored message context.....");
        
        // At this point in time, the restoredMessage will be a full tree.
        // TODO If this changes, please add more assertions here.
        
        // Simulate transport on the restored message
        baos = new ByteArrayOutputStream();
        env = restoredMC.getEnvelope();
        outputFormat = new OMOutputFormat();
        outputFormat.setDoOptimize(true);
        outputFormat.setMimeBoundary("MIMEBoundary_Axis2Rocks");
        env.serializeAndConsume(baos, outputFormat);
        String restoredText = baos.toString();
        TestLogger.logger.debug(restoredText);
        assertTrue(restoredText.contains("soap"));
        assertTrue(restoredText.contains("Envelope"));
        assertTrue(restoredText.contains("Body"));
        assertTrue(restoredText.indexOf("MIMEBoundary_Axis2Rocks") > 0);
        assertTrue(restoredText.indexOf(sampleText) > 0);
        assertTrue(restoredText.indexOf("<soapenv:Body><sendImage xmlns=\"urn://mtom.test.org\"><input><imageData><xop:Include") > 0);
        
        // Now try walking the saved envelope as an XMLInputStream.
        // try to restore the message context again
        // Now read in the persisted message
        // Setup an input stream to the file
        inStream = new FileInputStream(theFile);

        // attach a stream capable of reading objects from the 
        // stream connected to the file
        inObjStream = new ObjectInputStream(inStream);
        TestLogger.logger.debug("restoring a message context again.....");

        restoredMC = 
            (org.apache.axis2.context.MessageContext) inObjStream.readObject();
        inObjStream.close();
        inStream.close();
        TestLogger.logger.debug("....restored message context.....");    
        env = restoredMC.getEnvelope();
        env.build();
        
        // Use tree as input to XMLStreamReader
        OMXMLStreamReader xmlStreamReader = (OMXMLStreamReader) env.getXMLStreamReader();
        
        // Issue XOP:Include events for optimized MTOM text nodes
        xmlStreamReader.setInlineMTOM(false);
        
        DataHandler dh = null;
        while(xmlStreamReader.hasNext()) {
            xmlStreamReader.next();
            if (xmlStreamReader.isStartElement()) {
                QName qName =xmlStreamReader.getName();
                if (XOP_INCLUDE.equals(qName)) {
                    String hrefValue = xmlStreamReader.getAttributeValue("", "href");
                    if (hrefValue != null) {
                        dh =((OMAttachmentAccessor)xmlStreamReader).getDataHandler(hrefValue);
                    }
                }
            }
        }
        
        assertTrue(dh != null);       
    }
    
    /**
     * Create a JAXBBlock containing a JAX-B business object and simulate a normal Dispatch<Object>
     * output flow
     * 
     * @throws Exception
     */
    public void testPersist_Attachments_InMemory() throws Exception {
        
        // TODO Add a SWARef and a raw attachment
        
        // Create the JAX-B object with an attachment
        // Create a DataHandler with the String DataSource object
        DataHandler dataHandler = new DataHandler(stringDS);

        //Store the data handler in ImageDepot bean
        org.test.mtom.ObjectFactory of = new org.test.mtom.ObjectFactory();
        ImageDepot imageDepot = new org.test.mtom.ObjectFactory().createImageDepot();
        imageDepot.setImageData(dataHandler);
        SendImage obj = of.createSendImage();
        obj.setInput(imageDepot);
        
        // The JAXB object is stored in the Axiom tree as an OMSourcedElement.
        // The typical structure is
        //   OM SOAPEnvelope
        //   OM SOAPBody
        //   OMSourcedElement that is sourced by a JAXBBlockImpl which is backecd by a JAXB Object.
        Message m = createMessage(obj);
        m.setMTOMEnabled(true);
        
        // The Message is set on the JAXWS MessageContext
        MessageContext jaxwsMC = new MessageContext();
        jaxwsMC.setMessage(m);
        
        // Check to see if the message is a fault. The engine will always call this method.
        // The Message must respond appropriately without doing a conversion.
        boolean isFault = m.isFault();
        assertTrue(!isFault);
        assertTrue("XMLPart Representation is " + m.getXMLPartContentType(),
                   "SPINE".equals(m.getXMLPartContentType()));
        
        // The JAX-WS MessageContext is converted into an Axis2 MessageContext
        org.apache.axis2.context.MessageContext axisMC = jaxwsMC.getAxisMessageContext();
        MessageUtils.putMessageOnMessageContext(m, jaxwsMC.getAxisMessageContext());
        axisMC.setProperty(Configuration.ENABLE_MTOM, "true");
        
        // Make sure the Axiom structure is intact
        SOAPEnvelope env = axisMC.getEnvelope();
        SOAPBody body = env.getBody();
        OMElement child = body.getFirstElement();
        assertTrue(child instanceof OMSourcedElement);
        OMSourcedElement omse = (OMSourcedElement) child;
        assertTrue(!omse.isExpanded());
        OMDataSource ds = omse.getDataSource();
        assertTrue(ds instanceof JAXBBlockImpl);
        
        // Now simulate persisting the message in memory
        SOAPEnvelope env2 = CopyUtils.copy(env);
        
        // Make sure the Axiom structure is intact.  
        env = axisMC.getEnvelope();
        body = env.getBody();
        child = body.getFirstElement();
        assertTrue(child instanceof OMSourcedElement);
        omse = (OMSourcedElement) child;
        assertTrue(!omse.isExpanded());
        ds = omse.getDataSource();
        assertTrue(ds instanceof JAXBBlockImpl);
        
        // Simulate transport
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OMOutputFormat outputFormat = new OMOutputFormat();
        outputFormat.setDoOptimize(true);
        outputFormat.setMimeBoundary("MIMEBoundary_Axis2Rocks");
        env.serializeAndConsume(baos, outputFormat);

        String newText = baos.toString();
        TestLogger.logger.debug(newText);
        assertTrue(newText.contains("soap"));
        assertTrue(newText.contains("Envelope"));
        assertTrue(newText.contains("Body"));
        assertTrue(newText.indexOf("MIMEBoundary_Axis2Rocks") > 0);
        assertTrue(newText.indexOf(sampleText) > 0);
        assertTrue(newText.indexOf("<soapenv:Body><sendImage xmlns=\"urn://mtom.test.org\"><input><imageData><xop:Include") > 0);     
        
        // Now check the copied envelope
        body = env2.getBody();
        child = body.getFirstElement();
        assertTrue(child instanceof OMSourcedElement);
        omse = (OMSourcedElement) child;
        assertTrue(!omse.isExpanded());
        ds = omse.getDataSource();
        assertTrue(ds instanceof JAXBDataSource);
          
        // Simulate transport on the copied message
        baos = new ByteArrayOutputStream();
        outputFormat = new OMOutputFormat();
        outputFormat.setDoOptimize(true);
        outputFormat.setMimeBoundary("MIMEBoundary_Axis2Rocks");
        env2.serializeAndConsume(baos, outputFormat);
        String restoredText = baos.toString();
        TestLogger.logger.debug(restoredText);
        assertTrue(restoredText.contains("soap"));
        assertTrue(restoredText.contains("Envelope"));
        assertTrue(restoredText.contains("Body"));
        assertTrue(restoredText.indexOf("MIMEBoundary_Axis2Rocks") > 0);
        
        // Make sure that attachment is not inlined
        assertTrue(restoredText.indexOf(sampleText) > 0);
        assertTrue(restoredText.indexOf("<soapenv:Body><sendImage xmlns=\"urn://mtom.test.org\"><input><imageData><xop:Include") > 0);
        
    }
    
    private Message createMessage(Object jaxbObj) throws Exception {
        // Create a SOAP 1.1 Message
        MessageFactory mf = (MessageFactory) FactoryRegistry.getFactory(MessageFactory.class);
        Message m = mf.create(Protocol.soap11);

        // Get the BlockFactory
        JAXBBlockFactory bf = (JAXBBlockFactory) FactoryRegistry.getFactory(JAXBBlockFactory.class);

        // Create the JAXBContext
        JAXBBlockContext context =
                new JAXBBlockContext(jaxbObj.getClass().getPackage().getName());

        // Create a JAXBBlock using the Echo object as the content. This simulates
        // what occurs on the outbound JAX-WS Dispatch<Object> client
        Block block = bf.createFrom(jaxbObj, context, null);

        // Add the block to the message as normal body content.
        m.setBodyBlock(block);
        return m;
    }
}
