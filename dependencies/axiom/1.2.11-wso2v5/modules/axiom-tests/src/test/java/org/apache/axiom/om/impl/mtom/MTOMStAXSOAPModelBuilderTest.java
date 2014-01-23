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

package org.apache.axiom.om.impl.mtom;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.AbstractTestCase;
import org.apache.axiom.om.OMAttachmentAccessor;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.OMXMLStreamReader;
import org.apache.axiom.om.impl.OMStAXWrapper;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.impl.traverse.OMDescendantsIterator;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.impl.builder.MTOMStAXSOAPModelBuilder;
import org.apache.axiom.util.stax.XMLStreamReaderUtils;
import org.apache.axiom.util.stax.xop.XOPUtils;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MTOMStAXSOAPModelBuilderTest extends AbstractTestCase {

    private final static QName XOP_INCLUDE = 
        new QName("http://www.w3.org/2004/08/xop/include", "Include");
    
    /** @param testName  */
    public MTOMStAXSOAPModelBuilderTest(String testName) {
        super(testName);
    }


    protected void setUp() throws Exception {
        super.setUp();
    }

    private MTOMStAXSOAPModelBuilder createBuilderForTestMTOMMessage() throws Exception {
        String contentTypeString =
                "multipart/Related; charset=\"UTF-8\"; type=\"application/xop+xml\"; boundary=\"----=_AxIs2_Def_boundary_=42214532\"; start=\"SOAPPart\"";
        String inFileName = "mtom/MTOMBuilderTestIn.txt";
        InputStream inStream = getTestResource(inFileName);
        Attachments attachments = new Attachments(inStream, contentTypeString);
        XMLStreamReader reader = StAXUtils.createXMLStreamReader(new BufferedReader(
                new InputStreamReader(attachments.getSOAPPartInputStream())));
        return new MTOMStAXSOAPModelBuilder(reader, attachments,
                                               SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
    }

    private OMElement createTestMTOMMessage() throws Exception {
        return createBuilderForTestMTOMMessage().getDocumentElement();
    }
    
    
    private void checkSerialization(OMElement root, boolean optimize) throws Exception {
        OMOutputFormat format = new OMOutputFormat();
        format.setDoOptimize(optimize);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        root.serializeAndConsume(baos, format);
        String msg = baos.toString();
        if (optimize) {
            // Make sure there is an xop:Include element and an optimized attachment
            assertTrue(msg.indexOf("xop:Include") > 0);
            assertTrue(msg.indexOf("Content-ID: <-1609420109260943731>") > 0);
        } else {
            assertTrue(msg.indexOf("xop:Include") < 0);
            assertTrue(msg.indexOf("Content-ID: <-1609420109260943731>") < 0);
        }
    }
    
    public void testAccessToParser() throws Exception {
        OMElement root = createTestMTOMMessage();
        StAXBuilder builder = (StAXBuilder) root.getBuilder();
        // Disable caching so that the reader can be accessed.
        builder.setCache(false);
        XMLStreamReader reader = (XMLStreamReader) builder.getParser();
        
        // For an MTOM message, the reader is actually an XOPDecodingStreamReader. This one
        // cannot be unwrapped by getOriginalXMLStreamReader
        assertSame(reader, XMLStreamReaderUtils.getOriginalXMLStreamReader(reader));
        
        // To get access to the original reader, we first need to unwrap the
        // XOPDecodingStreamReader using XOPUtils
        XMLStreamReader encodedReader = XOPUtils.getXOPEncodedStream(reader).getReader();
        assertTrue(encodedReader != reader);

        // Now we can get to the original parser
        XMLStreamReader original = XMLStreamReaderUtils.getOriginalXMLStreamReader(encodedReader);
        
        assertTrue(!original.getClass().getName().startsWith("org.apache.axiom."));
    }
    
    public void testAccessToCachedParser() throws Exception {
        OMElement root = createTestMTOMMessage();
        XMLStreamReader reader = root.getXMLStreamReader(true);
        
        XMLStreamReader original = XMLStreamReaderUtils.getOriginalXMLStreamReader(reader);
        
        // The caching parser will be an OMStaXWrapper.
        assertTrue(original instanceof OMStAXWrapper);
    }
    
    public void testCreateOMElement() throws Exception {
        OMElement root = createTestMTOMMessage();
        OMElement body = (OMElement) root.getFirstOMChild();
        OMElement data = (OMElement) body.getFirstOMChild();

        Iterator childIt = data.getChildren();
        OMElement child = (OMElement) childIt.next();
        OMText blob = (OMText) child.getFirstOMChild();
        /*
         * Following is the procedure the user has to follow to read objects in
         * OBBlob User has to know the object type & whether it is serializable.
         * If it is not he has to use a Custom Defined DataSource to get the
         * Object.
         */
        byte[] expectedObject = new byte[] { 13, 56, 65, 32, 12, 12, 7, -3, -2,
                -1, 98 };
        DataHandler actualDH;
        actualDH = (DataHandler) blob.getDataHandler();
        //ByteArrayInputStream object = (ByteArrayInputStream) actualDH
        //.getContent();
        //byte[] actualObject= null;
        //  object.read(actualObject,0,10);

        //  assertEquals("Object check", expectedObject[5],actualObject[5] );
    }

    /**
     * Test that MIME parts are not loaded before requesting the DataHandlers from the corresponding
     * OMText nodes.
     *  
     * @throws Exception
     */
    public void testDeferredLoadingOfAttachments() throws Exception {
        MTOMStAXSOAPModelBuilder builder = createBuilderForTestMTOMMessage();
        Attachments attachments = builder.getAttachments();
        OMDocument doc = builder.getDocument();
        // Find all the binary nodes
        List/*<OMText>*/ binaryNodes = new ArrayList();
        for (Iterator it = new OMDescendantsIterator(doc.getFirstOMChild()); it.hasNext(); ) {
            OMNode node = (OMNode)it.next();
            if (node instanceof OMText) {
                OMText text = (OMText)node;
                if (text.isBinary()) {
                    binaryNodes.add(text);
                }
            }
        }
        assertFalse(binaryNodes.isEmpty());
        // At this moment only the SOAP part should have been loaded
        assertEquals(1, attachments.getContentIDList().size());
        for (Iterator it = binaryNodes.iterator(); it.hasNext(); ) {
            // Request the DataHandler and do something with it to make sure
            // the part is loaded
            ((DataHandler)((OMText)it.next()).getDataHandler()).getInputStream().close();
        }
        assertEquals(binaryNodes.size() + 1, attachments.getContentIDList().size());
    }
    
    /**
     * Test reading a message containing XOP and ensuring that the
     * the XOP is preserved when it is serialized.
     * @throws Exception
     */
    // TODO: because of the serializeAndConsume, this is actually NOT testing MTOMStAXSOAPModelBuilder, but StreamingOMSerializer!!!
    public void testCreateAndSerializeOptimized() throws Exception {
        OMElement root = createTestMTOMMessage();
        checkSerialization(root, true);
    }
    
    /**
     * Test reading a message containing XOP.
     * Then make a copy of the message.
     * Then ensure that the XOP is preserved when it is serialized.
     * @throws Exception
     */
    public void testCreateCloneAndSerializeOptimized() throws Exception {
        OMElement root = createTestMTOMMessage();
        
        // Create a clone of root
        OMElement root2 = root.cloneOMElement();
        
        // Write out the source
        checkSerialization(root, true);
        
        // Write out the clone
        checkSerialization(root2, true);
    }
    
    /**
     * Test reading a message containing XOP.
     * Fully build the tree.
     * Then make a copy of the message.
     * Then ensure that the XOP is preserved when it is serialized.
     * @throws Exception
     */
    public void testCreateBuildCloneAndSerializeOptimized() throws Exception {
        OMElement root = createTestMTOMMessage();
        
        // Fully build the root
        root.buildWithAttachments();
        
        // Create a clone of root
        OMElement root2 = root.cloneOMElement();
        
        // Write out the source
        checkSerialization(root, true);
        
        // Write out the clone
        checkSerialization(root2, true);
    }
    
    /**
     * Test reading a message containing XOP.
     * Serialize the tree (with caching).
     * Then ensure that the XOP is preserved when it is serialized again.
     * <p>
     * Regression test for WSCOMMONS-446.
     * 
     * @throws Exception
     */
    public void testCreateSerializeAndSerializeOptimized() throws Exception {
        OMElement root = createTestMTOMMessage();
        
        // Serialize the tree (with caching).
        root.serialize(new ByteArrayOutputStream());
        
        // Write out the source
        checkSerialization(root, true);
    }
    
    /**
     * Test reading a message containing XOP.
     * Enable inlining serialization
     * Then ensure that the data is inlined when written
     * @throws Exception
     */
    public void testCreateAndSerializeInlined() throws Exception {
        OMElement root = createTestMTOMMessage();
        
        checkSerialization(root, false);
    }

    public void testUTF16MTOMMessage() throws Exception {
        String contentTypeString =
                "multipart/Related; charset=\"UTF-8\"; type=\"application/xop+xml\"; boundary=\"----=_AxIs2_Def_boundary_=42214532\"; start=\"SOAPPart\"";
        String originalCID = "1.urn:uuid:A3ADBAEE51A1A87B2A11443668160994@apache.org";
        String cidURL = XOPUtils.getURLForContentID(originalCID);
        String xmlPlusMime1 = "------=_AxIs2_Def_boundary_=42214532\r\n" +
                "Content-Type: application/xop+xml; charset=UTF-16\r\n" +
                "Content-Transfer-Encoding: 8bit\r\n" +
                "Content-ID: SOAPPart\r\n" +
                "\r\n";
        String xmlPlusMime2 = "<soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\"><soapenv:Body><m:data xmlns:m=\"http://www.example.org/stuff\"><m:name m:contentType=\"text/plain\"><xop:Include xmlns:xop=\"http://www.w3.org/2004/08/xop/include\" href=\"" + cidURL + "\"></xop:Include></m:name></m:data></soapenv:Body></soapenv:Envelope>\r\n";
        String xmlPlusMime3 = "\r\n------=_AxIs2_Def_boundary_=42214532\r\n" +
                "Content-Transfer-Encoding: binary\r\n" +
                "Content-ID: " + originalCID + "\r\n" +
                "\r\n" +
                "Foo Bar\r\n" +
                "------=_AxIs2_Def_boundary_=42214532--\r\n";
        byte[] bytes1 = xmlPlusMime1.getBytes();
        byte[] bytes2 = xmlPlusMime2.getBytes("UTF-16");
        byte[] bytes3 = xmlPlusMime3.getBytes();
        byte[] full = append(bytes1, bytes2);
        full = append(full, bytes3);
        
        InputStream inStream = new BufferedInputStream(new ByteArrayInputStream(full));
        Attachments attachments = new Attachments(inStream, contentTypeString);
        XMLStreamReader reader = StAXUtils.createXMLStreamReader(
                attachments.getSOAPPartInputStream(), "UTF-16");
        MTOMStAXSOAPModelBuilder builder = new MTOMStAXSOAPModelBuilder(reader, attachments,
                                               SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        OMElement root = builder.getDocumentElement();
        root.build();
    }

    public void testCreateAndXMLStreamReader() throws Exception {
        OMElement root = createTestMTOMMessage();
        
        // Build tree
        root.build();
        
        // Use tree as input to XMLStreamReader
        OMXMLStreamReader xmlStreamReader = (OMXMLStreamReader) root.getXMLStreamReader();
        
        // Issue XOP:Include events for optimized MTOM text nodes
        xmlStreamReader.setInlineMTOM(false);
        
        DataHandler dh = null;
        while(xmlStreamReader.hasNext() && dh == null) {
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
        
        // Make sure next event is an an XOP_Include END element
        xmlStreamReader.next();
        assertTrue(xmlStreamReader.isEndElement());
        assertTrue(XOP_INCLUDE.equals(xmlStreamReader.getName()));
        
        // Make sure the next event is the end tag of name
        xmlStreamReader.next();
        assertTrue(xmlStreamReader.isEndElement());
        assertTrue("name".equals(xmlStreamReader.getLocalName()));
    }
   
    private byte[] append(byte[] a, byte[] b) {
        byte[] z = new byte[a.length + b.length];
        System.arraycopy(a, 0, z, 0, a.length);
        System.arraycopy(b, 0, z, a.length, b.length);
        return z;
    }
}