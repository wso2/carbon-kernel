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

package org.apache.axiom.om.impl.builder;

import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertSame;

import java.io.IOException;
import java.io.OutputStream;

import javax.activation.DataHandler;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import junit.framework.Assert;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.testutils.activation.RandomDataSource;
import org.apache.axiom.testutils.activation.TestDataSource;
import org.apache.axiom.testutils.io.IOTestUtils;
import org.apache.axiom.util.blob.MemoryBlob;
import org.junit.BeforeClass;
import org.junit.Test;

public class JAXBCustomBuilderTest {
    private static JAXBContext jaxbContext;
    
    @BeforeClass
    public static void initJAXBContext() throws JAXBException {
        jaxbContext = JAXBContext.newInstance(MyDocument.class);
    }
    
    private OMElement createTestDocument(DataHandler dh) {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement document = factory.createOMElement(new QName("urn:test", "document"));
        OMElement name = factory.createOMElement(new QName("name"));
        name.setText("some name");
        document.addChild(name);
        OMElement content = factory.createOMElement(new QName("content"));
        content.addChild(factory.createOMText(dh, true));
        document.addChild(content);
        return document;
    }
    
    private void test(DataHandler dh, StAXOMBuilder builder, boolean same, boolean usesAttachments, boolean expectBareReader) throws IOException {
        JAXBCustomBuilder customBuilder = new JAXBCustomBuilder(jaxbContext, expectBareReader);
        builder.registerCustomBuilderForPayload(customBuilder);
        builder.getDocumentElement().build();
        MyDocument myDocument = (MyDocument)customBuilder.getJaxbObject();
        if (same) {
            assertSame(dh, myDocument.getContent());
        } else {
            assertNotSame(dh, myDocument.getContent());
            IOTestUtils.compareStreams(dh.getInputStream(), myDocument.getContent().getInputStream());
        }
        Assert.assertEquals(usesAttachments, customBuilder.isAttachmentsAccessed());
    }
    
    @Test
    public void testPlain() throws Exception {
        DataHandler dh = new DataHandler(new RandomDataSource(10000));
        MemoryBlob blob = new MemoryBlob();
        OutputStream out = blob.getOutputStream();
        createTestDocument(dh).serialize(out);
        out.close();
        test(dh, new StAXOMBuilder(blob.getInputStream()), false, false, true);
    }
    
    @Test
    public void testWithDataHandlerReaderExtension() throws Exception {
        DataHandler dh = new DataHandler(new TestDataSource('X', Integer.MAX_VALUE));
        OMElement document = createTestDocument(dh);
        test(dh, new StAXOMBuilder(document.getXMLStreamReader()), true, true, false);
    }
    
    @Test
    public void testWithXOP() throws Exception {
        DataHandler dh = new DataHandler(new RandomDataSource(10000));
        MemoryBlob blob = new MemoryBlob();
        OutputStream out = blob.getOutputStream();
        OMOutputFormat format = new OMOutputFormat();
        format.setDoOptimize(true);
        createTestDocument(dh).serialize(out, format);
        out.close();
        Attachments attachments = new Attachments(blob.getInputStream(), format.getContentType());
        test(dh, new XOPAwareStAXOMBuilder(attachments.getSOAPPartInputStream(), attachments), false, true, true);
    }
}
