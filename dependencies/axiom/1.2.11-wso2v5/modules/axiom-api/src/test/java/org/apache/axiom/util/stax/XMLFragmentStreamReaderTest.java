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
package org.apache.axiom.util.stax;

import java.io.StringReader;

import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.testutils.stax.XMLStreamReaderComparator;

public class XMLFragmentStreamReaderTest extends TestCase {
    /**
     * Test comparing the output of {@link XMLFragmentStreamReader} with that
     * of a native StAX parser. In particular this tests the behavior for START_DOCUMENT
     * and END_DOCUMENT events.
     * 
     * @throws Exception
     */
    public void test() throws Exception {
        String xml = "<ns:a xmlns:ns='urn:ns'>test</ns:a>";
        XMLStreamReader expected = StAXUtils.createXMLStreamReader(new StringReader(xml));
        XMLStreamReader reader = StAXUtils.createXMLStreamReader(new StringReader(xml));
        reader.nextTag();
        XMLStreamReader actual = new XMLFragmentStreamReader(reader);
        new XMLStreamReaderComparator(expected, actual).compare();
        assertEquals(XMLStreamReader.END_DOCUMENT, reader.getEventType());
        expected.close();
        reader.close();
    }
}
