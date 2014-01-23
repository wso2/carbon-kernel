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

package org.apache.axiom.attachments;

import org.apache.axiom.attachments.AttachmentCacheMonitor;
import org.apache.axiom.attachments.utils.IOUtils;
import org.apache.axiom.om.AbstractTestCase;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.TestConstants;
import org.apache.axiom.om.impl.MIMEOutputUtils;
import org.apache.axiom.om.impl.MTOMXMLStreamWriter;
import org.apache.axiom.om.impl.builder.XOPAwareStAXOMBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.impl.builder.MTOMStAXSOAPModelBuilder;
import org.apache.axiom.testutils.io.IOTestUtils;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamReader;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Set;

public class AttachmentsTest extends AbstractTestCase {

    public AttachmentsTest(String testName) {
        super(testName);
    }

    String img1FileName = "mtom/img/test.jpg";
    String img2FileName = "mtom/img/test2.jpg";
    String inSWAFileName = "soap/soap11/SWAAttachmentStream.txt";
    
    String contentTypeString =
        "multipart/related; boundary=\"MIMEBoundaryurn:uuid:A3ADBAEE51A1A87B2A11443668160701\"; type=\"application/xop+xml\"; start=\"<0.urn:uuid:A3ADBAEE51A1A87B2A11443668160702@apache.org>\"; start-info=\"application/soap+xml\"; charset=UTF-8;action=\"mtomSample\"";

    public void testMIMEHelper() {
    }

    public void testGetAttachmentSpecType() {
    }

    public void testSimultaneousStreamAccess() throws Exception {
        InputStream inStream;
        Attachments attachments;

        inStream = getTestResource(TestConstants.MTOM_MESSAGE);
        attachments = new Attachments(inStream, TestConstants.MTOM_MESSAGE_CONTENT_TYPE);

        attachments.getDataHandler("2.urn:uuid:A3ADBAEE51A1A87B2A11443668160994@apache.org");

        // This should throw an error
        try {
            attachments.getIncomingAttachmentStreams();
            fail("No exception caught when attempting to access datahandler and stream at the same time");
        } catch (IllegalStateException ise) {
            // Nothing
        }

        inStream.close();

        // Try the other way around.
        inStream = getTestResource(TestConstants.MTOM_MESSAGE);
        attachments = new Attachments(inStream, TestConstants.MTOM_MESSAGE_CONTENT_TYPE);

        attachments.getIncomingAttachmentStreams();

        // These should NOT throw error even though they are using part based access
        try {
            String contentType = attachments.getSOAPPartContentType();
            assertTrue(contentType.indexOf("application/xop+xml;") >=0);
            assertTrue(contentType.indexOf("charset=UTF-8;") >=0);
            assertTrue(contentType.indexOf("type=\"application/soap+xml\";") >=0);
        } catch (IllegalStateException ise) {
            fail("No exception expected when requesting SOAP part data");
            ise.printStackTrace();
        }

        try {
            attachments.getSOAPPartInputStream();
        } catch (IllegalStateException ise) {
            fail("No exception expected when requesting SOAP part data");
        }

        // These should throw an error
        try {
            attachments.getDataHandler("2.urn:uuid:A3ADBAEE51A1A87B2A11443668160994@apache.org");
            fail("No exception caught when attempting to access stream and datahandler at the same time");
        } catch (IllegalStateException ise) {
            // Nothing
        }

        // Additionally, we also need to ensure mutual exclusion if someone
        // tries to access part data directly

        try {
            attachments.getAllContentIDs();
            fail("No exception caught when attempting to access stream and contentids list at the same time");
        } catch (IllegalStateException ise) {
            // Nothing
        }

        try {
            attachments.getDataHandler("2.urn:uuid:A3ADBAEE51A1A87B2A11443668160994@apache.org");
            fail("No exception caught when attempting to access stream and part at the same time");
        } catch (IllegalStateException ise) {
            // Nothing
        }
    }

    public void testGetInputAttachhmentStreams() throws Exception {

        IncomingAttachmentInputStream dataIs;
        InputStream expectedDataIs;

        InputStream inStream = getTestResource(TestConstants.MTOM_MESSAGE);
        Attachments attachments = new Attachments(inStream, TestConstants.MTOM_MESSAGE_CONTENT_TYPE);

        // Since SOAP part operated independently of other streams, access it
        // directly, and then get to the streams. If this sequence throws an
        // error, something is wrong with the stream handling code.
        InputStream is = attachments.getSOAPPartInputStream();
        while (is.read() != -1) ;

        // Get the inputstream container
        IncomingAttachmentStreams ias = attachments.getIncomingAttachmentStreams();

        dataIs = ias.getNextStream();
        expectedDataIs = getTestResource(img1FileName);
        IOTestUtils.compareStreams(dataIs, expectedDataIs);

        dataIs = ias.getNextStream();
        expectedDataIs = getTestResource(img2FileName);
        IOTestUtils.compareStreams(dataIs, expectedDataIs);

        // Confirm that no more streams are left
        assertEquals(null, ias.getNextStream());
        
        // After all is done, we should *still* be able to access and
        // re-consume the SOAP part stream, as it should be cached.. can we?
        is = attachments.getSOAPPartInputStream();
        while (is.read() != -1) ;  
    }
    
    public void testWritingBinaryAttachments() throws Exception {

        // Read in message: SOAPPart and 2 image attachments
        InputStream inStream = getTestResource(TestConstants.MTOM_MESSAGE);
        Attachments attachments = new Attachments(inStream, TestConstants.MTOM_MESSAGE_CONTENT_TYPE);
        
        attachments.getSOAPPartInputStream();

        String[] contentIDs = attachments.getAllContentIDs();
        
        OMOutputFormat oof = new OMOutputFormat();
        oof.setDoOptimize(true);
        oof.setMimeBoundary(TestConstants.MTOM_MESSAGE_BOUNDARY);
        oof.setRootContentId(TestConstants.MTOM_MESSAGE_START);
        
        // Write out the message
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MTOMXMLStreamWriter writer = new MTOMXMLStreamWriter(baos, oof);
        
        XOPAwareStAXOMBuilder builder = 
            new XOPAwareStAXOMBuilder(attachments.getSOAPPartInputStream(),
                                      attachments);
        OMElement om = builder.getDocumentElement();
        om.serialize(writer);
        om.close(false);
        String outNormal = baos.toString();
        
        assertTrue(outNormal.indexOf("base64") == -1);
        
        // Now do it again but use base64 content-type-encoding for 
        // binary attachments
        baos = new ByteArrayOutputStream();
        oof.setProperty(OMOutputFormat.USE_CTE_BASE64_FOR_NON_TEXTUAL_ATTACHMENTS, 
                        Boolean.TRUE);
        writer = new MTOMXMLStreamWriter(baos, oof);
        builder = 
            new XOPAwareStAXOMBuilder(attachments.getSOAPPartInputStream(),
                                      attachments);
        om = builder.getDocumentElement();
        om.serialize(writer);
        om.close(false);
        String outBase64 = baos.toString();
        
        
        // Do a quick check to see if the data is base64 and is
        // writing base64 compliant code.
        assertTrue(outBase64.indexOf("base64") != -1);
        assertTrue(outBase64.indexOf("GBgcGBQgHBwcJCQgKDBQNDAsL") != -1);
        
        // Now read the data back in
        InputStream is = new ByteArrayInputStream(outBase64.getBytes());
        Attachments attachments2 = new Attachments(is, TestConstants.MTOM_MESSAGE_CONTENT_TYPE);
        
        // Now write it back out with binary...
        baos = new ByteArrayOutputStream();
        oof.setProperty(OMOutputFormat.USE_CTE_BASE64_FOR_NON_TEXTUAL_ATTACHMENTS, 
                        Boolean.FALSE);
        writer = new MTOMXMLStreamWriter(baos, oof);
        builder = 
            new XOPAwareStAXOMBuilder(attachments2.getSOAPPartInputStream(),
                                      attachments2);
        om = builder.getDocumentElement();
        om.serialize(writer);
        om.close(false);
        String outBase64ToNormal = baos.toString();
        
        assertTrue(outBase64ToNormal.indexOf("base64") == -1);
        
        // Now do it again but use base64 content-type-encoding for 
        // binary attachments
        baos = new ByteArrayOutputStream();
        oof.setProperty(OMOutputFormat.USE_CTE_BASE64_FOR_NON_TEXTUAL_ATTACHMENTS, 
                        Boolean.TRUE);
        writer = new MTOMXMLStreamWriter(baos, oof);
        builder = 
            new XOPAwareStAXOMBuilder(attachments2.getSOAPPartInputStream(),
                                      attachments2);
        om = builder.getDocumentElement();
        om.serialize(writer);
        om.close(false);
        String outBase64ToBase64 = baos.toString();
        
        // Do a quick check to see if the data is base64 and is
        // writing base64 compliant code.
        assertTrue(outBase64ToBase64.indexOf("base64") != -1);
        assertTrue(outBase64ToBase64.indexOf("GBgcGBQgHBwcJCQgKDBQNDAsL") != -1);
    }
    
    public void testSWAWriteWithContentIDOrder() throws Exception {

        // Read the stream that has soap xml followed by BAttachment then AAttachment
        InputStream inStream = getTestResource(inSWAFileName);
        Attachments attachments = new Attachments(inStream, contentTypeString);

        // Get the contentIDs to force the reading
        String[] contentIDs = attachments.getAllContentIDs();
        
        // Get the root
        XMLStreamReader reader =
                StAXUtils.createXMLStreamReader(new BufferedReader(new InputStreamReader(attachments.getSOAPPartInputStream())));
        MTOMStAXSOAPModelBuilder builder = 
            new MTOMStAXSOAPModelBuilder(reader, attachments, null);
        OMElement root = builder.getDocumentElement();
        StringWriter xmlWriter = new StringWriter();
        root.serialize(xmlWriter);
        
        // Serialize the message using the legacy behavior (order by content id)
        OMOutputFormat format = new OMOutputFormat();
        format.setCharSetEncoding("utf-8");
        format.setDoingSWA(true);
        format.setProperty(OMOutputFormat.RESPECT_SWA_ATTACHMENT_ORDER, Boolean.FALSE);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        MIMEOutputUtils.writeSOAPWithAttachmentsMessage(xmlWriter, baos, attachments, format);
        
        String text = baos.toString();
        // Assert that AAttachment occurs before BAttachment since
        // that is the natural ordering of the content ids.
        assertTrue(text.indexOf("AAttachment") < text.indexOf("BAttachment"));
        
    }
    
    public void testSWAWriteWithIncomingOrder() throws Exception {

        // Read the stream that has soap xml followed by BAttachment then AAttachment
        InputStream inStream = getTestResource(inSWAFileName);
        Attachments attachments = new Attachments(inStream, contentTypeString);

        // Get the contentIDs to force the reading
        String[] contentIDs = attachments.getAllContentIDs();
        
        // Get the root
        XMLStreamReader reader =
                StAXUtils.createXMLStreamReader(new BufferedReader(new InputStreamReader(attachments.getSOAPPartInputStream())));
        MTOMStAXSOAPModelBuilder builder = 
            new MTOMStAXSOAPModelBuilder(reader, attachments, null);
        OMElement root = builder.getDocumentElement();
        StringWriter xmlWriter = new StringWriter();
        root.serialize(xmlWriter);
        
        // Serialize the message using the legacy behavior (order by content id)
        OMOutputFormat format = new OMOutputFormat();
        format.setCharSetEncoding("utf-8");
        format.setDoingSWA(true);
        format.setProperty(OMOutputFormat.RESPECT_SWA_ATTACHMENT_ORDER, Boolean.TRUE);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        MIMEOutputUtils.writeSOAPWithAttachmentsMessage(xmlWriter, baos, attachments, format);
        
        String text = baos.toString();
        // Assert that AAttachment occurs before BAttachment since
        // that is the natural ordering of the content ids.
        assertTrue(text.indexOf("BAttachment") < text.indexOf("AAttachment"));
        
    }

    public void testGetDataHandler() throws Exception {

        InputStream inStream = getTestResource(TestConstants.MTOM_MESSAGE);
        Attachments attachments = new Attachments(inStream, TestConstants.MTOM_MESSAGE_CONTENT_TYPE);

        DataHandler dh = attachments
                .getDataHandler("2.urn:uuid:A3ADBAEE51A1A87B2A11443668160994@apache.org");
        InputStream dataIs = dh.getDataSource().getInputStream();

        InputStream expectedDataIs = getTestResource(img2FileName);

        // Compare data across streams
        IOTestUtils.compareStreams(dataIs, expectedDataIs);
    }

    public void testNonExistingMIMEPart() throws Exception {

        InputStream inStream = getTestResource(TestConstants.MTOM_MESSAGE);
        Attachments attachments = new Attachments(inStream, TestConstants.MTOM_MESSAGE_CONTENT_TYPE);

        DataHandler dh = attachments.getDataHandler("ThisShouldReturnNull");
        assertNull(dh);
    }

    public void testGetAllContentIDs() throws Exception {

        InputStream inStream = getTestResource(TestConstants.MTOM_MESSAGE);
        Attachments attachments = new Attachments(inStream, TestConstants.MTOM_MESSAGE_CONTENT_TYPE);

        String[] contentIDs = attachments.getAllContentIDs();
        assertEquals(contentIDs.length, 3);
        assertEquals(contentIDs[0], "0.urn:uuid:A3ADBAEE51A1A87B2A11443668160702@apache.org");
        assertEquals(contentIDs[1], "1.urn:uuid:A3ADBAEE51A1A87B2A11443668160943@apache.org");
        assertEquals(contentIDs[2], "2.urn:uuid:A3ADBAEE51A1A87B2A11443668160994@apache.org");

        Set idSet = attachments.getContentIDSet();
        assertTrue(idSet.contains("0.urn:uuid:A3ADBAEE51A1A87B2A11443668160702@apache.org"));
        assertTrue(idSet.contains("2.urn:uuid:A3ADBAEE51A1A87B2A11443668160994@apache.org"));
        assertTrue(idSet.contains("1.urn:uuid:A3ADBAEE51A1A87B2A11443668160943@apache.org"));
        
        // Make sure the length is correct
        long length = attachments.getContentLength();
        long fileSize = IOUtils.getStreamAsByteArray(getTestResource(TestConstants.MTOM_MESSAGE)).length;
        assertTrue("Expected MessageContent Length of " + fileSize + " but received " + length,
                   length == fileSize);
    }
    
    public void testCachedFilesExpired() throws Exception {
    	
    	// Set file expiration to 10 seconds
    	long INTERVAL = 3 * 1000; // 3 seconds for Thread to sleep
        Thread t = Thread.currentThread();

       
        // Get the AttachmentCacheMonitor and force it to remove files after
        // 10 seconds.
        AttachmentCacheMonitor acm = AttachmentCacheMonitor.getAttachmentCacheMonitor();
        int previousTime = acm.getTimeout();
        
        try {
            acm.setTimeout(10); 


            File aFile = new File("A");
            aFile.createNewFile();
            String aFileName = aFile.getCanonicalPath();
            acm.register(aFileName);

            t.sleep(INTERVAL);

            File bFile = new File("B");
            bFile.createNewFile();
            String bFileName = bFile.getCanonicalPath();
            acm.register(bFileName);

            t.sleep(INTERVAL);

            acm.access(aFileName);

            // time since file A registration <= cached file expiration
            assertTrue("File A should still exist", aFile.exists());

            t.sleep(INTERVAL);

            acm.access(bFileName);

            // time since file B registration <= cached file expiration
            assertTrue("File B should still exist", bFile.exists());

            t.sleep(INTERVAL);

            File cFile = new File("C");
            cFile.createNewFile();
            String cFileName = cFile.getCanonicalPath();
            acm.register(cFileName);
            acm.access(bFileName);

            t.sleep(INTERVAL);

            acm.checkForAgedFiles();

            // time since file C registration <= cached file expiration
            assertTrue("File C should still exist", cFile.exists());

            t.sleep(10* INTERVAL);  // Give task loop time to delete aged files


            // All files should be gone by now
            assertFalse("File A should no longer exist", aFile.exists());
            assertFalse("File B should no longer exist", bFile.exists());
            assertFalse("File C should no longer exist", cFile.exists());
        } finally {
       
            // Reset the timeout to the previous value so that no 
            // other tests are affected
            acm.setTimeout(previousTime);
        }
    }

    private void testGetSOAPPartContentID(String contentTypeStartParam, String contentId)
            throws Exception {
        // It doesn't actually matter what the stream *is* it just needs to exist
        String contentType = "multipart/related; boundary=\"" + TestConstants.MTOM_MESSAGE_BOUNDARY +
                "\"; type=\"text/xml\"; start=\"" + contentTypeStartParam + "\"";
        InputStream inStream = getTestResource(TestConstants.MTOM_MESSAGE);
        Attachments attachments = new Attachments(inStream, contentType);
        assertEquals("Did not obtain correct content ID", contentId,
                attachments.getSOAPPartContentID());
    }
    
    public void testGetSOAPPartContentIDWithoutBrackets() throws Exception {
        testGetSOAPPartContentID("my-content-id@localhost", "my-content-id@localhost");
    }
    
    public void testGetSOAPPartContentIDWithBrackets() throws Exception {
        testGetSOAPPartContentID("<my-content-id@localhost>", "my-content-id@localhost");
    }
    
    // Not sure when exactly somebody uses the "cid:" prefix in the start parameter, but
    // this is how the code currently works.
    public void testGetSOAPPartContentIDWithCidPrefix() throws Exception {
        testGetSOAPPartContentID("cid:my-content-id@localhost", "my-content-id@localhost");
    }
    
    // Regression test for WSCOMMONS-329
    public void testGetSOAPPartContentIDWithCidPrefix2() throws Exception {
        testGetSOAPPartContentID("<cid-73920@192.168.0.1>", "cid-73920@192.168.0.1");
    }
    
    public void testGetSOAPPartContentIDShort() throws Exception {
        testGetSOAPPartContentID("bbb", "bbb");
    }
    
    public void testGetSOAPPartContentIDShortWithBrackets() throws Exception {
        testGetSOAPPartContentID("<b>", "b");
    }
    
    public void testGetSOAPPartContentIDBorderline() throws Exception {
        testGetSOAPPartContentID("cid:", "cid:");
    }
}
