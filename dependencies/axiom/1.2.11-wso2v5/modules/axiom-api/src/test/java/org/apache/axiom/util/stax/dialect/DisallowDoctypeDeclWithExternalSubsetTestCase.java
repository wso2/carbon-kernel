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
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class DisallowDoctypeDeclWithExternalSubsetTestCase extends DialectTestCase {
    protected void runTest() throws Throwable {
        XMLInputFactory factory = newNormalizedXMLInputFactory();
        factory = getDialect().disallowDoctypeDecl(factory);
        DummyHTTPServer server = new DummyHTTPServer();
        server.start();
        try {
            boolean gotException = false;
            boolean reachedDocumentElement = false;
            try {
                XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(
                        "<?xml version='1.0'?><!DOCTYPE root SYSTEM '" + server.getBaseURL() +
                        "dummy.dtd'><root/>"));
                try {
                    while (reader.hasNext()) {
                        if (reader.next() == XMLStreamConstants.START_ELEMENT) {
                            reachedDocumentElement = true;
                        }
                    }
                } finally {
                    reader.close();
                }
            } catch (XMLStreamException ex) {
                gotException = true;
            } catch (RuntimeException ex) {
                gotException = true;
            }
            assertTrue("Expected exception", gotException);
            assertFalse("The parser tried to load external DTD subset", server.isRequestReceived());
            assertFalse("The parser failed to throw an exception before reaching the document element", reachedDocumentElement);
        } finally {
            server.stop();
        }
    }
}
