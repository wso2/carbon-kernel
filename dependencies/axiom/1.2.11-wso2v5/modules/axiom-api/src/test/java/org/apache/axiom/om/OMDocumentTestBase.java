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

import org.apache.commons.io.input.CountingInputStream;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.Iterator;

public class OMDocumentTestBase extends AbstractTestCase {
    private String sampleXML = "<?xml version='1.0' encoding='utf-8'?>" +
            "<!--This is some comments at the start of the document-->" +
            "<?PITarget PIData?>" +
            "<Axis2>" +
            "    <ProjectName>The Apache Web Sevices Project</ProjectName>" +
            "</Axis2>";
    
    private final OMMetaFactory omMetaFactory;
    
    public OMDocumentTestBase(OMMetaFactory omMetaFactory) {
        this.omMetaFactory = omMetaFactory;
    }

    public void testParse() {
        OMDocument doc = getSampleOMDocument(sampleXML);
        checkSampleXML(doc);
        doc.close(false);
    }
    
    public void testSerializeAndConsume() throws XMLStreamException {
        // read the string in to the builder
        OMDocument omDocument = getSampleOMDocument(sampleXML);

        // serialise it to a string
        String outXML = "";
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        omDocument.serializeAndConsume(outStream);
        outXML = new String(outStream.toByteArray());

        // again load that to another builder
        OMDocument omDocument2 = getSampleOMDocument(outXML);
        checkSampleXML(omDocument2);
        
        omDocument.close(false);
        omDocument2.close(false);
    }
    
    private void checkSampleXML(OMDocument document) {
        // check for the comment and the PI
        boolean commentFound = false;
        boolean piFound = false;
        Iterator children = document.getChildren();
        while (children.hasNext()) {
            OMNode omNode = (OMNode) children.next();
            if (omNode.getType() == OMNode.COMMENT_NODE) {
                commentFound = true;
            } else if (omNode.getType() == OMNode.PI_NODE) {
                piFound = true;
            } else if (omNode.getType() == OMNode.ELEMENT_NODE && !commentFound && !piFound) {
                fail("OMElement should come after Comment and PI");

            }
        }
        assertTrue(commentFound && piFound);
    }

    /**
     * Test that a document that is not well formed triggers an appropriate error.
     */
    public void testMalformedDocument() {
        OMDocument document = getSampleOMDocument("<Root><Child attr='a' attr='a'/></Root>");
        try {
            document.serialize(new ByteArrayOutputStream());
            fail("Expected exception");
        } catch (Exception ex) {
            // We expect an exception here
        }
        document.close(false);
    }

    private OMDocument getSampleOMDocument(String xml) {
        return OMXMLBuilderFactory.createOMBuilder(omMetaFactory.getOMFactory(), new StringReader(xml)).getDocument();
    }

//    private OMDocument getSampleOMDocument() {
//        OMFactory omFactory = OMAbstractFactory.getOMFactory();
//        OMDocument omDocument = omFactory.createOMDocument();
//        omFactory.createOMComment(omDocument, "This is some comments at the start of the document");
//        omDocument.setCharsetEncoding("utf-8");
//        omFactory.createOMProcessingInstruction(omDocument, "PITarget", "PIData");
//
//        OMElement documentElement = omFactory.createOMElement("Axis2", null, omDocument);
//        omDocument.setDocumentElement(documentElement);
//        omFactory.createOMElement("ProjectName", null, documentElement);
//        documentElement.getFirstElement().setText("The Apache Web Sevices Project");
//
//        return omDocument;
//    }

    public void testBuild() throws Exception {
        CountingInputStream in = new CountingInputStream(getTestResource(
                TestConstants.REALLY_BIG_MESSAGE));
        OMDocument doc = OMXMLBuilderFactory.createOMBuilder(omMetaFactory.getOMFactory(), in).getDocument();
        assertFalse(doc.isComplete());
        int countBeforeBuild = in.getCount();
        doc.build();
        assertTrue(doc.isComplete());
        int countAfterBuild = in.getCount();
        assertTrue(countAfterBuild > countBeforeBuild);
        OMNode node = doc.getFirstOMChild();
        while (node != null) {
            node = node.getNextOMSibling();
        }
        assertEquals(countAfterBuild, in.getCount());
    }
}
