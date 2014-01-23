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
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.MTOMXMLStreamWriter;
import org.apache.axis2.datasource.jaxb.JAXBDSContext;
import org.apache.axis2.datasource.jaxb.JAXBDataSource;
import org.apache.axis2.jaxws.message.databinding.JAXBBlockContext;
import org.apache.axis2.jaxws.message.databinding.JAXBUtils;
import org.apache.axis2.jaxws.message.factory.BlockFactory;
import org.apache.axis2.jaxws.message.factory.JAXBBlockFactory;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.message.factory.OMBlockFactory;
import org.apache.axis2.jaxws.message.factory.SourceBlockFactory;
import org.apache.axis2.jaxws.message.factory.XMLStringBlockFactory;
import org.apache.axis2.jaxws.message.util.Reader2Writer;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.unitTest.TestLogger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import test.Data;
import test.ObjectFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.TreeSet;

/**
 * JAXBDSContextTests
 * Tests to create and validate JAXBDSContext
 * These are not client/server tests.
 */
public class JAXBDSContextTests extends TestCase {

    
    private static XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    private static XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();


    public JAXBDSContextTests() {
        super();
    }

    public JAXBDSContextTests(String arg0) {
        super(arg0);
    }

    
    /**
     * Create a Block representing an JAXB and simulate a 
     * normal Dispatch<JAXB> flow
     * @throws Exception
     */
    public void testMarshal() throws Exception {
        
        // Create a JAXBDSContext for the package containing Data
        TreeSet<String> packages = new TreeSet<String>();
        packages.add(Data.class.getPackage().getName());
        JAXBDSContext context = new JAXBDSContext(packages);
        
        TestLogger.logger.debug(context.getJAXBContext().toString());
        
        // Force marshal by type
        context.setProcessType(Data.class);
        
        // Create an Data value
        ObjectFactory factory = new ObjectFactory();
        Data value = factory.createData(); 
        value.setInput("Hello World");
        
        // Create a JAXBElement
        QName qName = new QName("urn://sample", "data");
        JAXBElement jaxbElement = new JAXBElement(qName, Data.class, value);

        // Create a writer
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OMOutputFormat format = new OMOutputFormat();
        format.setDoOptimize(true);
        MTOMXMLStreamWriter writer = new MTOMXMLStreamWriter(baos, format);
        
        // Marshal the value
        writer.writeStartDocument();
        writer.writeStartElement("root");
        context.marshal(jaxbElement, writer);
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.flush();
        
        assertTrue(baos.toString().indexOf("Hello World") > 0);
        assertTrue(baos.toString().indexOf("</root>") > 0);
    }
    
    /**
     * Create a Block representing an JAXB and simulate a 
     * normal Dispatch<JAXB> flow
     * @throws Exception
     */
    public void testMarshalArray() throws Exception {
        
        // Create a JAXBDSContext for the package containing Data
        TreeSet<String> packages = new TreeSet<String>();
        packages.add(Data.class.getPackage().getName());
        JAXBDSContext context = new JAXBDSContext(packages);
        
        TestLogger.logger.debug(context.getJAXBContext().toString());
        
        // Force marshal by type
        context.setProcessType(Data[].class);
        
        // Create an Data value
        ObjectFactory factory = new ObjectFactory();
        Data value[] = new Data[3];
        value[0] = factory.createData(); 
        value[0].setInput("Hello");
        value[1] = factory.createData(); 
        value[1].setInput("Beautiful");
        value[2] = factory.createData(); 
        value[2].setInput("World");
        
        // Create a JAXBElement.
        // To indicate "occurrence elements", the value is wrapped in
        // an OccurrenceArray
        QName qName = new QName("urn://sample", "data");
        OccurrenceArray occurrenceArray = new OccurrenceArray(value);
        JAXBElement jaxbElement = new JAXBElement(qName, Data[].class, occurrenceArray);

        // Create a writer
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OMOutputFormat format = new OMOutputFormat();
        format.setDoOptimize(true);
        MTOMXMLStreamWriter writer = new MTOMXMLStreamWriter(baos, format);
        
        // Marshal the value
        writer.writeStartElement("root");
        context.marshal(jaxbElement, writer);
        writer.writeEndElement();

        writer.flush();
        
        String outputText = baos.toString();
        String subText = outputText;
        int count = 0;
        while (subText.indexOf("data") > 0) {
            count++;
            subText = subText.substring(subText.indexOf("data") + 1);
        }
        // 3 data refs for start tag name
        // 3 data refs for end tag name
        // 3 xsi type refs
        assertTrue("Expected 9 data tags but found "+count+"  Text is:"+outputText, count == 9);
    }
   
}
