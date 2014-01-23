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

package org.apache.axiom.om.util;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.SequenceInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Vector;

import javax.activation.DataSource;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMSourcedElement;
import org.apache.axiom.om.ds.WrappedTextNodeOMDataSourceFromDataSource;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.llom.OMSourcedElementImpl;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.om.util.ElementHelper;
import org.apache.axiom.testutils.activation.RandomDataSource;
import org.apache.axiom.testutils.io.CharacterStreamComparator;
import org.apache.axiom.testutils.io.IOTestUtils;
import org.apache.commons.io.IOUtils;

public class ElementHelperTest extends TestCase {
    public void testGetTextAsStreamWithSingleTextNode() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement element = factory.createOMElement(new QName("a"));
        factory.createOMText(element, "test");
        Reader in = ElementHelper.getTextAsStream(element, true);
        assertTrue(in instanceof StringReader);
        assertEquals(element.getText(), IOUtils.toString(in));
    }
    
    public void testGetTextAsStreamWithNonTextChildren() throws Exception {
        OMElement element = AXIOMUtil.stringToOM("<a>A<b>B</b>C</a>");
        Reader in = ElementHelper.getTextAsStream(element, true);
        assertEquals(element.getText(), IOUtils.toString(in));
    }
    
    public void testGetTextAsStreamWithOMSourcedElement() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        DataSource ds = new RandomDataSource(445566, 32, 128, 20000000);
        QName qname = new QName("a");
        Charset cs = Charset.forName("ascii");
        OMSourcedElement element = new OMSourcedElementImpl(qname, factory,
                new WrappedTextNodeOMDataSourceFromDataSource(qname, ds, cs));
        Reader in = ElementHelper.getTextAsStream(element, true);
        assertFalse(in instanceof StringReader);
        IOTestUtils.compareStreams(new InputStreamReader(ds.getInputStream(), cs), in);
    }
    
    public void testGetTextAsStreamWithoutCaching() throws Exception {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        if (factory.getClass().getName().equals("com.bea.xml.stream.MXParserFactory")) {
            // Skip the test on the StAX reference implementation because it
            // causes an out of memory error
            return;
        }
        DataSource ds = new RandomDataSource(654321, 64, 128, 20000000);
        Vector/*<InputStream>*/ v = new Vector/*<InputStream>*/();
        v.add(new ByteArrayInputStream("<a>".getBytes("ascii")));
        v.add(ds.getInputStream());
        v.add(new ByteArrayInputStream("</a>".getBytes("ascii")));
        factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
        XMLStreamReader reader = factory.createXMLStreamReader(
                new SequenceInputStream(v.elements()), "ascii");
        OMElement element = new StAXOMBuilder(reader).getDocumentElement();
        Reader in = ElementHelper.getTextAsStream(element, false);
        IOTestUtils.compareStreams(new InputStreamReader(ds.getInputStream(), "ascii"), in);
    }
    
    public void testWriteTextTo() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement element = factory.createOMElement(new QName("a"));
        factory.createOMText(element, "test");
        StringWriter out = new StringWriter();
        ElementHelper.writeTextTo(element, out, true);
        assertEquals(element.getText(), out.toString());
    }
    
    public void testWriteTextToWithNonTextNodes() throws Exception {
        OMElement element = AXIOMUtil.stringToOM("<a>A<b>B</b>C</a>");
        StringWriter out = new StringWriter();
        ElementHelper.writeTextTo(element, out, true);
        assertEquals(element.getText(), out.toString());
    }
    
    public void testWriteTextToWithOMSourcedElement() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        DataSource ds = new RandomDataSource(665544, 32, 128, 20000000);
        QName qname = new QName("a");
        OMSourcedElement element = new OMSourcedElementImpl(qname, factory,
                new WrappedTextNodeOMDataSourceFromDataSource(qname, ds, Charset.forName("ascii")));
        Reader in = new InputStreamReader(ds.getInputStream(), "ascii");
        Writer out = new CharacterStreamComparator(in);
        ElementHelper.writeTextTo(element, out, true); // cache doesn't matter here
        out.close();
    }
}
