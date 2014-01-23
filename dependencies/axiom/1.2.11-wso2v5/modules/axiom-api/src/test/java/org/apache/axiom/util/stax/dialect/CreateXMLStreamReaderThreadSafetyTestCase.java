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
package org.apache.axiom.util.stax.dialect;

import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.testutils.concurrent.Action;
import org.apache.axiom.testutils.concurrent.ConcurrentTestUtils;

public class CreateXMLStreamReaderThreadSafetyTestCase extends DialectTestCase {
    protected void runTest() throws Throwable {
        final XMLInputFactory factory = getDialect().makeThreadSafe(newNormalizedXMLInputFactory());
        ConcurrentTestUtils.testThreadSafety(new Action() {
            public void execute() throws Exception {
                String text = String.valueOf((int)(Math.random() * 10000));
                String xml = "<root>" + text + "</root>";
                XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(xml));
                assertEquals(XMLStreamReader.START_DOCUMENT, reader.getEventType());
                assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
                assertEquals(XMLStreamReader.CHARACTERS, reader.next());
                assertEquals(text, reader.getText());
                assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
                assertEquals(XMLStreamReader.END_DOCUMENT, reader.next());
                reader.close();
            }
        });
    }
}
