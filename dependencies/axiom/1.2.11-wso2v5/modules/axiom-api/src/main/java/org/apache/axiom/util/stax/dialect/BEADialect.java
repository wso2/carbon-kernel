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

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

class BEADialect extends AbstractStAXDialect {
    public static final StAXDialect INSTANCE = new BEADialect();

    public String getName() {
        return "BEA";
    }

    public XMLInputFactory enableCDataReporting(XMLInputFactory factory) {
        factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
        factory.setProperty("http://java.sun.com/xml/stream/properties/report-cdata-event",
                Boolean.TRUE);
        return factory;
    }

    public XMLInputFactory disallowDoctypeDecl(XMLInputFactory factory) {
        return StAXDialectUtils.disallowDoctypeDecl(factory);
    }

    public XMLInputFactory makeThreadSafe(XMLInputFactory factory) {
        // Factories are already thread safe
        return factory;
    }

    public XMLOutputFactory makeThreadSafe(XMLOutputFactory factory) {
        // Factories are already thread safe
        return factory;
    }

    public XMLStreamReader normalize(XMLStreamReader reader) {
        return new BEAStreamReaderWrapper(reader, null);
    }

    public XMLStreamWriter normalize(XMLStreamWriter writer) {
        // The stream writer implementation of the reference implementation doesn't handle masked
        // namespace bindings correctly. We wrap the writer in a
        // NamespaceContextCorrectingXMLStreamWriterWrapper to work around this problem.
        return new NamespaceContextCorrectingXMLStreamWriterWrapper(writer);
    }

    public XMLInputFactory normalize(XMLInputFactory factory) {
        return new BEAInputFactoryWrapper(factory);
    }

    public XMLOutputFactory normalize(XMLOutputFactory factory) {
        return new NormalizingXMLOutputFactoryWrapper(factory, this);
    }
}
