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

package org.apache.axiom.om;

import org.apache.axiom.om.ds.ByteArrayDataSource;
import org.apache.axiom.om.ds.CharArrayDataSource;
import org.apache.axiom.om.ds.InputStreamDataSource;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;

import javax.xml.stream.XMLStreamReader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Validate the basic functionality of the OMSourcedElement interface,
 * the OMDataSourceExt interface, InputStreamDataSource and ByteArrayDataSource.
 */
public class OMSourcedElementTest extends AbstractTestCase implements OMConstants {
    protected OMFactory ombuilderFactory;
    protected SOAPFactory soapFactory;
    protected SOAPEnvelope soapEnvelope;
    ByteArrayDataSource bads1;
    ByteArrayDataSource bads2;
    InputStreamDataSource isds1;
    InputStreamDataSource isds2;
    CharArrayDataSource cads;
    String localName = "myPayload";
    OMNamespace ns;
    
    private final String ENCODING = "utf-8";
    
    String payload1 = "<tns:myPayload xmlns:tns=\"urn://test\">Payload One</tns:myPayload>";
    String payload2 = "<tns:myPayload xmlns:tns=\"urn://test\">Payload Two</tns:myPayload>";

    public OMSourcedElementTest(String testName) {
        super(testName);
        ombuilderFactory = OMAbstractFactory.getOMFactory();
        soapFactory = OMAbstractFactory.getSOAP11Factory();
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        soapEnvelope = soapFactory.createSOAPEnvelope();
        SOAPBody soapBody = soapFactory.createSOAPBody(soapEnvelope);
        SOAPHeader soapHeader = soapFactory.createSOAPHeader(soapEnvelope);
        
        bads1 = new ByteArrayDataSource(payload1.getBytes(ENCODING), ENCODING);
        bads2 = new ByteArrayDataSource(payload2.getBytes(ENCODING), ENCODING);
        ByteArrayInputStream bais1 = new ByteArrayInputStream(payload1.getBytes(ENCODING));
        ByteArrayInputStream bais2 = new ByteArrayInputStream(payload2.getBytes(ENCODING));
        isds1 = new InputStreamDataSource(bais1, ENCODING);
        isds2 = new InputStreamDataSource(bais2, ENCODING);
        cads = new CharArrayDataSource(payload1.toCharArray());
        ns = soapFactory.createOMNamespace("urn://test", "tns");
    }

    /**
     * Validates creation and insertion of OMSourcedElement
     * @throws Exception
     */
    public void testFactory() throws Exception {
        SOAPBody soapBody = soapEnvelope.getBody();
        OMFactory factory = soapBody.getOMFactory();
        OMSourcedElement omse = factory.createOMElement(bads1, localName, ns);
        soapBody.addChild(omse);
        OMNode firstChild = soapBody.getFirstOMChild();
        assertTrue("Expected OMSourcedElement child", firstChild instanceof OMSourcedElement);
    }
    
    /**
     * Tests functionality of ByteArrayDataSource
     * @throws Exception
     */
    public void testByteArrayDS() throws Exception {
        SOAPBody soapBody = soapEnvelope.getBody();
        OMFactory factory = soapBody.getOMFactory();
        OMSourcedElement omse = factory.createOMElement(bads1, localName, ns);
        soapBody.addChild(omse);
        OMNode firstChild = soapBody.getFirstOMChild();
        assertTrue("Expected OMSourcedElement child", firstChild instanceof OMSourcedElement);
        OMSourcedElement child = (OMSourcedElement) firstChild;
        assertTrue("OMSourcedElement is expanded.  This is unexpected", !child.isExpanded());
        assertTrue("OMSourcedElement should be backed by a ByteArrayDataSource",
                   child.getDataSource() instanceof ByteArrayDataSource);
        
        // A ByteArrayDataSource does not consume the backing object when read.
        // Thus getting the XMLStreamReader of the ByteArrayDataSource should not 
        // cause expansion of the OMSourcedElement.
        XMLStreamReader reader = child.getXMLStreamReader();
        reader.next();
        assertTrue("OMSourcedElement is expanded.  This is unexpected", !child.isExpanded());
        
        // Likewise, a ByteArrayDataSource does not consume the backing object when 
        // written.  Thus serializing the OMSourcedElement should not cause the expansion
        // of the OMSourcedElement.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        soapBody.serialize(baos);
        String output = baos.toString(ENCODING);
//        System.out.println(output);
        assertTrue("The payload was not present in the output",
                   output.indexOf(payload1) > 0);
        assertTrue("OMSourcedElement is expanded.  This is unexpected", !child.isExpanded());
        
        // If a consumer calls build or buildWithAttachments on the tree, the 
        // tree should not be expanded.
        soapBody.build();
        assertTrue("OMSourcedElement is expanded after build().  This is unexpected", !child.isExpanded());
        soapBody.buildWithAttachments();
        assertTrue("OMSourcedElement is expanded after buildWithAttachments().  This is unexpected", !child.isExpanded());
        
        // Test getting the raw bytes from the ByteArrayDataSource.
        OMDataSourceExt ds = (OMDataSourceExt) child.getDataSource();
        byte[] bytes = ds.getXMLBytes("UTF-16");  // Get the bytes as UTF-16 
        String payload = new String(bytes, "utf-16");
        assertTrue("The obtained bytes did not match the payload",
                   payload1.equals(payload));
        
       
        // Test getting the raw bytes with the default encoding
        OMOutputFormat outputFormat = new OMOutputFormat();
        baos = new ByteArrayOutputStream();
        ds.serialize(baos, outputFormat);
        output = baos.toString(OMOutputFormat.DEFAULT_CHAR_SET_ENCODING);
//        System.out.println(output);
        assertTrue("The obtained bytes did not match the payload",
                   payload1.equals(output));     
    }
    
    /**
     * Tests functionality of ByteArrayDataSource
     * @throws Exception
     */
    public void testHeader_ByteArrayDS() throws Exception {
        SOAPHeader soapHeader = soapEnvelope.getHeader();
        OMFactory factory = soapHeader.getOMFactory();
        
        // Set an empty MustUnderstand property on the data source
        bads1.setProperty(SOAPHeaderBlock.MUST_UNDERSTAND_PROPERTY, null);
        
        OMSourcedElement omse = ((SOAPFactory)factory).createSOAPHeaderBlock(localName, ns, bads1);
        soapHeader.addChild(omse);
        OMNode firstChild = soapHeader.getFirstOMChild();
        assertTrue("Expected OMSourcedElement child", firstChild instanceof SOAPHeaderBlock);
        SOAPHeaderBlock child = (SOAPHeaderBlock) firstChild;
        assertTrue("OMSourcedElement is expanded.  This is unexpected", !child.isExpanded());
        assertTrue("OMSourcedElement should be backed by a ByteArrayDataSource",
                   child.getDataSource() instanceof ByteArrayDataSource);
        
        // Make sure that getting the MustUnderstand property does not cause expansion.
        assertTrue(!child.getMustUnderstand());
        assertTrue("OMSourcedElement is expanded.  This is unexpected", !child.isExpanded());
        assertTrue("OMSourcedElement should be backed by a ByteArrayDataSource",
                   child.getDataSource() instanceof ByteArrayDataSource);
        
        // A ByteArrayDataSource does not consume the backing object when read.
        // Thus getting the XMLStreamReader of the ByteArrayDataSource should not 
        // cause expansion of the OMSourcedElement.
        XMLStreamReader reader = child.getXMLStreamReader();
        reader.next();
        assertTrue("OMSourcedElement is expanded.  This is unexpected", !child.isExpanded());
        
        // Likewise, a ByteArrayDataSource does not consume the backing object when 
        // written.  Thus serializing the OMSourcedElement should not cause the expansion
        // of the OMSourcedElement.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        soapHeader.serialize(baos);
        String output = baos.toString(ENCODING);
//        System.out.println(output);
        assertTrue("The payload was not present in the output",
                   output.indexOf(payload1) > 0);
        assertTrue("OMSourcedElement is expanded.  This is unexpected", !child.isExpanded());
        
        // Test getting the raw bytes from the ByteArrayDataSource.
        OMDataSourceExt ds = (OMDataSourceExt) child.getDataSource();
        byte[] bytes = ds.getXMLBytes("UTF-16");  // Get the bytes as UTF-16 
        String payload = new String(bytes, "utf-16");
        assertTrue("The obtained bytes did not match the payload",
                   payload1.equals(payload));
        
    }
    
    /**
     * Tests functionality of ByteArrayDataSource
     * @throws Exception
     */
    public void testCharArrayDS() throws Exception {
        SOAPBody soapBody = soapEnvelope.getBody();
        OMFactory factory = soapBody.getOMFactory();
        OMSourcedElement omse = factory.createOMElement(cads, localName, ns);
        soapBody.addChild(omse);
        OMNode firstChild = soapBody.getFirstOMChild();
        assertTrue("Expected OMSourcedElement child", firstChild instanceof OMSourcedElement);
        OMSourcedElement child = (OMSourcedElement) firstChild;
        assertTrue("OMSourcedElement is expanded.  This is unexpected", !child.isExpanded());
        assertTrue("OMSourcedElement should be backed by a ByteArrayDataSource",
                   child.getDataSource() instanceof CharArrayDataSource);
        
        // A CharArrayDataSource does not consume the backing object when read.
        // Thus getting the XMLStreamReader of the CharArrayDataSource should not 
        // cause expansion of the OMSourcedElement.
        XMLStreamReader reader = child.getXMLStreamReader();
        reader.next();
        assertTrue("OMSourcedElement is expanded.  This is unexpected", !child.isExpanded());
        
        // Likewise, a CharArrayDataSource does not consume the backing object when 
        // written.  Thus serializing the OMSourcedElement should not cause the expansion
        // of the OMSourcedElement.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        soapBody.serialize(baos);
        String output = baos.toString(ENCODING);
//        System.out.println(output);
        assertTrue("The payload was not present in the output",
                   output.indexOf(payload1) > 0);
        assertTrue("OMSourcedElement is expanded.  This is unexpected", !child.isExpanded());
        
        // Test getting the raw bytes from the ByteArrayDataSource.
        OMDataSourceExt ds = (OMDataSourceExt) child.getDataSource();
        char[] chars = (char[]) ds.getObject();  // Get the chars
        String payload = new String(chars);
        assertTrue("The obtained chars did not match the payload",
                   payload1.equals(payload));
        
        // Validate close
        ds.close();
        assertTrue("Close should free the resource", ds.getObject() == null);
        
    }
    
    /**
     * Tests functionality of InputStreamDataSource
     * @throws Exception
     */
    public void testInputStreamDS() throws Exception {
        SOAPBody soapBody = soapEnvelope.getBody();
        OMFactory factory = soapBody.getOMFactory();
        OMSourcedElement omse = factory.createOMElement(isds1, localName, ns);
        soapBody.addChild(omse);
        OMNode firstChild = soapBody.getFirstOMChild();
        assertTrue("Expected OMSourcedElement child", firstChild instanceof OMSourcedElement);
        OMSourcedElement child = (OMSourcedElement) firstChild;
        assertTrue("OMSourcedElement is expanded.  This is unexpected", !child.isExpanded());
        assertTrue("OMSourcedElement should be backed by a InputStreamDataSource",
                   child.getDataSource() instanceof InputStreamDataSource);
        
        // A InputStreamDataSource consumes the backing object when read.
        // Thus getting the XMLStreamReader of the ByteArrayDataSource should  
        // cause expansion of the OMSourcedElement.
        XMLStreamReader reader = child.getXMLStreamReader();
        reader.next();
        assertTrue("OMSourcedElement is not expanded.  This is unexpected", 
                   child.isExpanded());
        
        child.detach();
        
        // Reset the tree
        isds1 = new InputStreamDataSource(
            new ByteArrayInputStream(payload1.getBytes(ENCODING)), 
            ENCODING);
        omse = factory.createOMElement(isds1, localName, ns);
        soapBody.addChild(omse);
        firstChild = soapBody.getFirstOMChild();
        child = (OMSourcedElement) firstChild;
        
        // Likewise, an InputStreamDataSource consumes the backing object when 
        // written.  Thus serializing the OMSourcedElement should cause the expansion
        // of the OMSourcedElement.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        soapBody.serialize(baos);
        String output = baos.toString(ENCODING);
        assertTrue("The payload was not present in the output",
                   output.indexOf(payload1) > 0);
        assertTrue("OMSourcedElement is not expanded.  This is unexpected", child.isExpanded());
        
        // Reset the tree
        child.detach();
        isds1 = new InputStreamDataSource(
            new ByteArrayInputStream(payload1.getBytes(ENCODING)), 
            ENCODING);
        omse = factory.createOMElement(isds1, localName, ns);
        soapBody.addChild(omse);
        firstChild = soapBody.getFirstOMChild();
        child = (OMSourcedElement) firstChild;
        
        // Test getting the raw bytes from the ByteArrayDataSource.
        OMDataSourceExt ds = (OMDataSourceExt) child.getDataSource();
        byte[] bytes = ds.getXMLBytes(ENCODING);  // Get the bytes as UTF-16 
        String payload = new String(bytes, ENCODING);
        assertTrue("The obtained bytes did not match the payload",
                   payload1.equals(payload));
        
    }

    /**
     * Verifies that a OMDataSource can be replaced with another one
     * @throws Exception
     */
    public void testOMSEReplacement() throws Exception {
        SOAPBody soapBody = soapEnvelope.getBody();
        OMFactory factory = soapBody.getOMFactory();
        OMSourcedElement omse = factory.createOMElement(bads1, localName, ns);
        soapBody.addChild(omse);
        OMNode firstChild = soapBody.getFirstOMChild();
        assertTrue("Expected OMSourcedElement child", firstChild instanceof OMSourcedElement);
        OMSourcedElement child = (OMSourcedElement) firstChild;
        assertTrue("OMSourcedElement is expanded.  This is unexpected", !child.isExpanded());
        assertTrue("OMSourcedElement should be backed by a ByteArrayDataSource",
                   child.getDataSource() instanceof ByteArrayDataSource);
        
        // Write out the body
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        soapBody.serialize(baos);
        String output = baos.toString(ENCODING);
//        System.out.println(output);
        assertTrue("The payload was not present in the output",
                   output.indexOf(payload1) > 0);
        assertTrue("OMSourcedElement is expanded.  This is unexpected", !child.isExpanded());
        
        // Replace with payload2.  
        // Important note, it is legal to replace the OMDataSource, but
        // the namespace and local name of the OMSourcedElement cannot be changed.
        child.setDataSource(bads2);
        
        // Write out the body
        baos = new ByteArrayOutputStream();
        soapBody.serialize(baos);
        output = baos.toString(ENCODING);
//        System.out.println(output);
        assertTrue("The payload was not present in the output",
                   output.indexOf(payload2) > 0);
        assertTrue("OMSourcedElement is expanded.  This is unexpected", !child.isExpanded());
        
        // Now Replace with payload1 from an InputStreamDataSource
        child.setDataSource(isds1);
        baos = new ByteArrayOutputStream();
        soapBody.serialize(baos);
        output = baos.toString(ENCODING);
//        System.out.println(output);
        assertTrue("The payload was not present in the output",
                   output.indexOf(payload1) > 0);
        
        // Now Replace with payload2 from an InputStreamDataSource.
        // Note at this point, the child's tree is expanded.
        child.setDataSource(isds2);
        baos = new ByteArrayOutputStream();
        soapBody.serialize(baos);
        output = baos.toString(ENCODING);
//        System.out.println(output);
        assertTrue("The payload was not present in the output",
                   output.indexOf(payload2) > 0);
        
    }
}
