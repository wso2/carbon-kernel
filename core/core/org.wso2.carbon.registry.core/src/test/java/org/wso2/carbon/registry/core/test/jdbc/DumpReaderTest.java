/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.registry.core.test.jdbc;

import junit.framework.TestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMText;
import org.wso2.carbon.registry.core.jdbc.utils.DumpReader;
import org.wso2.carbon.registry.core.jdbc.utils.DumpWriter;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.FileReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class DumpReaderTest extends TestCase {

    String dumpDir;
    public void setUp() {
        dumpDir =  "src/test/resources/dumps/";
    }

    public void testDump1() throws Exception{
        FileReader reader = new FileReader(dumpDir + "dump1.xml");
        DumpReader dumpReader = new DumpReader(reader);
        List<String> result = new ArrayList<String>();
        processDump(dumpReader, result, "");

        assertEquals("1", result.get(0));
        assertEquals("1:2", result.get(1));
    }


    public void testDump2() throws Exception{
        FileReader reader = new FileReader(dumpDir + "dump2.xml");
        DumpReader dumpReader = new DumpReader(reader);

//        XMLStreamReader xmlReader =
//                XMLInputFactory.newInstance().createXMLStreamReader(dumpReader);
//        StAXOMBuilder builder = new StAXOMBuilder(xmlReader);
//        //get the root element (in this case the envelope)
//        OMElement documentElement =  builder.getDocumentElement();
//        documentElement.toString();
        List<String> result = new ArrayList<String>();
        processDump(dumpReader, result, "");

        assertEquals("1", result.get(0));
        assertEquals("1:2", result.get(1));
        assertEquals("1:3", result.get(2));
    }

    public void testDump3() throws Exception{
        FileReader reader = new FileReader(dumpDir + "dump3.xml");
        DumpReader dumpReader = new DumpReader(reader);
        List<String> result = new ArrayList<String>();
        processDump(dumpReader, result, "");

        assertEquals("1", result.get(0));
        assertEquals("1:2", result.get(1));
        assertEquals("1:3", result.get(2));
        assertEquals("1:3:4", result.get(3));
    }

    public void testDump4() throws Exception{
        FileReader reader = new FileReader(dumpDir + "dump4.xml");
        DumpReader dumpReader = new DumpReader(reader);
        List<String> result = new ArrayList<String>();
        processDump(dumpReader, result, "");

        assertEquals("1", result.get(0));
        assertEquals("1:2", result.get(1));
        assertEquals("1:3", result.get(2));
        assertEquals("1:3:4", result.get(3));
        assertEquals("1:3:4:5", result.get(4));
        assertEquals("1:3:4:6", result.get(5));
        assertEquals("1:3:7", result.get(6));
        assertEquals("1:3:7:8", result.get(7));
        assertEquals("1:3:7:9", result.get(8));
        assertEquals("1:3:7:9:10", result.get(9));
        assertEquals("1:3:7:9:10:11", result.get(10));
        assertEquals("1:3:7:9:10:12", result.get(11));
        assertEquals("1:3:7:9:13", result.get(12));
        assertEquals("1:3:7:9:13:14", result.get(13));
        assertEquals("1:3:7:9:13:15", result.get(14));
    }

    public void testDump5() throws Exception{
        FileReader reader = new FileReader(dumpDir + "dump5.xml");
        DumpReader dumpReader = new DumpReader(reader);
//        XMLStreamReader xmlReader =
//                XMLInputFactory.newInstance().createXMLStreamReader(dumpReader);
//        StAXOMBuilder builder = new StAXOMBuilder(xmlReader);
//        //get the root element (in this case the envelope)
//        OMElement documentElement =  builder.getDocumentElement();
//        documentElement.toString();
        List<String> result = new ArrayList<String>();
        processDump(dumpReader, result, "");

        assertEquals("1", result.get(0));
        assertEquals("1:2", result.get(1));
        assertEquals("1:2:3", result.get(2));
        assertEquals("1:2:3:4", result.get(3));
    }

    public void testDump6() throws Exception{
        FileReader reader = new FileReader(dumpDir + "dump6.xml");
        DumpReader dumpReader = new DumpReader(reader);
        List<String> result = new ArrayList<String>();
        processDump(dumpReader, result, "");
        assertEquals(7, result.size());
    }

    public void testDump7() throws Exception{
        FileReader reader = new FileReader(dumpDir + "dump7.xml");
        DumpReader dumpReader = new DumpReader(reader);
        List<String> result = new ArrayList<String>();
        processDump(dumpReader, result, "");
        assertEquals(62, result.size());

        System.out.println("total read: " + DumpReader.getTotalRead());
        System.out.println("total buffered: " + DumpReader.getTotalBuffered());
        System.out.println("maximum buffer size: " + DumpReader.getMaxBufferedSize());
        System.out.println("total buffer read size: " + DumpReader.getTotalBufferedRead());
    }



    public void testDump9() throws Exception{
        FileReader reader = new FileReader(dumpDir + "dump9.xml");
        DumpReader dumpReader = new DumpReader(reader);
        List<String> result = new ArrayList<String>();
        processDump(dumpReader, result, "");
        assertEquals(3, result.size());
    }

    public void processDump(DumpReader dumpReader, List<String>result, String prefix)
            throws Exception {
        XMLStreamReader xmlReader =
                XMLInputFactory.newInstance().createXMLStreamReader(dumpReader);

        while (!xmlReader.isStartElement() && xmlReader.hasNext()) {
            xmlReader.next();
        }

        if (!xmlReader.hasNext()) {
            // nothing to parse
            return;
        }

        if (!xmlReader.getLocalName().equals("resource")) {
            String msg = "Invalid dump to restore";
            throw new Exception(msg);
        }

        String n = xmlReader.getAttributeValue(null, "n");
        result.add(prefix + n);

        // got to next element
        xmlReader.next();
        while (!xmlReader.isStartElement() && xmlReader.hasNext()) {
            xmlReader.next();
        }
        if (!xmlReader.hasNext() || !xmlReader.getLocalName().equals("children")) {
            // finished the recursion
            return;
        }
        do {
            xmlReader.next();
            if (xmlReader.isEndElement() && xmlReader.getLocalName().equals("children")) {
                // this means empty children, just quit from here
                // before that we have to set the cursor to the start of the next element
                if (xmlReader.hasNext()) {
                    do {
                        xmlReader.next();
                    } while ((!xmlReader.isStartElement() && xmlReader.hasNext()) &&
                            !(xmlReader.isEndElement() &&
                                    xmlReader.getLocalName().equals("resource")));
                }
                return;
            }
        } while (!xmlReader.isStartElement() && xmlReader.hasNext());

        while (xmlReader.hasNext() && !xmlReader.isStartElement()) {
            xmlReader.next();
        }
        if (xmlReader.hasNext() && xmlReader.getLocalName().equals("resource")) {

            int i = 0;
            while (true) {

                if (i != 0) {
                    dumpReader.setReadingChildResourceIndex(i);

                    // otherwise we will set the stuff for the next resource
                    // get an xlm reader in the checking child by parent mode.
                    xmlReader = XMLInputFactory.newInstance().createXMLStreamReader(dumpReader);

                    while (!xmlReader.isStartElement() && xmlReader.hasNext()) {
                        xmlReader.next();
                    }
                }   
                if (!xmlReader.getLocalName().equals("resource")) {
                    String msg = "Invalid dump to restore";
                    throw new Exception(msg);
                }

                if (i != 0) {
                    // we give the control back to the child.
                    dumpReader.setCheckingChildByParent(false);
                }
                dumpReader.setReadingChildResourceIndex(i);
                processDump(new DumpReader(dumpReader), result, prefix + n + ":");
                // by this time we know the child dump reader has consumed the early resource
                // completely

                dumpReader.setCheckingChildByParent(true);
                if (dumpReader.isLastResource(i)) {
                    dumpReader.setCheckingChildByParent(false);
                    break;
                }
                // by this time i ++ child resource should exist
                i++;
            }
        }
    }

    public void testDumpWriter1() throws Exception {
        Writer writer = new StringWriter();
        XMLOutputFactory xof = XMLOutputFactory.newInstance();
        XMLStreamWriter xmlWriter = xof.createXMLStreamWriter(writer);

        xmlWriter.writeStartElement("wrapper");
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMText emptyText = factory.createOMText("");
        emptyText.serialize(xmlWriter);
        xmlWriter.flush();

        writer.write("<resource name=\"abc\"");
        writer.flush();
        Writer dumpWriter = new DumpWriter(writer);

        xof = XMLOutputFactory.newInstance();
        XMLStreamWriter xmlWriter2 = xof.createXMLStreamWriter(dumpWriter);
        xmlWriter2.writeStartElement("resource");
        xmlWriter2.writeAttribute("name", "pqr");
        xmlWriter2.writeAttribute("isCollection", "true");
        xmlWriter2.writeStartElement("test");
        xmlWriter2.writeCharacters("bang");
        xmlWriter2.writeEndElement(); // ending test
        xmlWriter2.writeEndElement(); // ending resource
        xmlWriter2.flush();

        xmlWriter.writeEndElement(); // ending wrapper
        xmlWriter.flush();

        String writerStr = writer.toString();
        assertEquals("<wrapper><resource name=\"abc\" " +
                "isCollection=\"true\"><test>bang</test></resource></wrapper>", writerStr);
    }


    public void testDumpWriterWithNameLast() throws Exception {
        Writer writer = new StringWriter();
        XMLOutputFactory xof = XMLOutputFactory.newInstance();
        XMLStreamWriter xmlWriter = xof.createXMLStreamWriter(writer);

        xmlWriter.writeStartElement("wrapper");
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMText emptyText = factory.createOMText("");
        emptyText.serialize(xmlWriter);
        xmlWriter.flush();

        writer.write("<resource name=\"abc\"");
        writer.flush();
        Writer dumpWriter = new DumpWriter(writer);

        xof = XMLOutputFactory.newInstance();
        XMLStreamWriter xmlWriter2 = xof.createXMLStreamWriter(dumpWriter);
        xmlWriter2.writeStartElement("resource");
        xmlWriter2.writeAttribute("isCollection", "true");
        xmlWriter2.writeAttribute("name", "pqr");
        xmlWriter2.writeStartElement("test");
        xmlWriter2.writeCharacters("bang");
        xmlWriter2.writeEndElement(); // ending test
        xmlWriter2.writeEndElement(); // ending resource
        xmlWriter2.flush();

        xmlWriter.writeEndElement(); // ending wrapper
        xmlWriter.flush();

        String writerStr = writer.toString();
        assertEquals("<wrapper><resource name=\"abc\" " +
                "isCollection=\"true\"><test>bang</test></resource></wrapper>", writerStr);
    }

    public void testDumpWriterWithNameLastAndManyAttribute() throws Exception {
        Writer writer = new StringWriter();
        XMLOutputFactory xof = XMLOutputFactory.newInstance();
        XMLStreamWriter xmlWriter = xof.createXMLStreamWriter(writer);

        xmlWriter.writeStartElement("wrapper");
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMText emptyText = factory.createOMText("");
        emptyText.serialize(xmlWriter);
        xmlWriter.flush();

        writer.write("<resource name=\"abc\"");
        writer.flush();
        Writer dumpWriter = new DumpWriter(writer);

        xof = XMLOutputFactory.newInstance();
        XMLStreamWriter xmlWriter2 = xof.createXMLStreamWriter(dumpWriter);
        xmlWriter2.writeStartElement("resource");
        xmlWriter2.writeAttribute("isCollection", "true");
        xmlWriter2.writeAttribute("isEarly", "bang");
        xmlWriter2.writeAttribute("name", "pqr");
        xmlWriter2.writeAttribute("isLate", "kaboom");
        xmlWriter2.writeStartElement("test");
        xmlWriter2.writeCharacters("bang");
        xmlWriter2.writeEndElement(); // ending test
        xmlWriter2.writeEndElement(); // ending resource
        xmlWriter2.flush();

        xmlWriter.writeEndElement(); // ending wrapper
        xmlWriter.flush();

        String writerStr = writer.toString();
        assertEquals("<wrapper><resource name=\"abc\" " +
                "isCollection=\"true\" isEarly=\"bang\" isLate=\"kaboom\">" +
                "<test>bang</test></resource></wrapper>", writerStr);
    }
}
