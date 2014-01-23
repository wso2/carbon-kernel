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

package org.apache.axis2.fastinfoset;

import com.sun.xml.fastinfoset.stax.StAXDocumentParser;
import com.sun.xml.fastinfoset.stax.StAXDocumentSerializer;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.custommonkey.xmlunit.XMLTestCase;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;

public class FastInfosetInputOutputTest extends XMLTestCase {

    /**
     * This is to test how fast infoset interoperate with Axiom.
     * This is how this test is organized.
     * <pre>
     *      de-ser(wstx)        ser(fast-info)             de-ser(fast-info)       ser(wstx)
     * XML  -------->     Axiom     ------>    binary file -------------->   Axiom ---------> XML
     * </pre>
     * <p/>
     * Then the initial XML file and the last XML will be compared to see whether they are the same.
     */
    public void testInputOutput() throws Exception {
        String inputFile = "pom.xml";
        File outputFile = new File("target/output.xml");
        File tempFile = new File("target/test.bin");

        try {
            // first let's read the xml document in to Axiom
            OMElement element = new StAXOMBuilder(inputFile).getDocumentElement();

            // output it using binary xml outputter
            XMLStreamWriter streamWriter = new StAXDocumentSerializer(new FileOutputStream(tempFile));
            streamWriter.writeStartDocument();
            element.serializeAndConsume(streamWriter);
            streamWriter.writeEndDocument();

            // now let's read the binary file in to Axiom
            XMLStreamReader streamReader = new StAXDocumentParser(new FileInputStream(tempFile));
            StAXBuilder builder = new StAXOMBuilder(streamReader);
            builder.getDocumentElement().serialize(new FileWriter(outputFile));

            // let's see this is the same that we fed in to this test initially
            assertXMLEqual(new FileReader(inputFile), new FileReader(outputFile));

        } finally {
            if (outputFile.exists()) outputFile.delete();
            if (tempFile.exists()) tempFile.delete();
        }
    }
}
