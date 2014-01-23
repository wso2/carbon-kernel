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
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

class SJSXPDialect extends AbstractStAXDialect {
    private final boolean isUnsafeStreamResult;
    
    public SJSXPDialect(boolean isUnsafeStreamResult) {
        this.isUnsafeStreamResult = isUnsafeStreamResult;
    }

    public String getName() {
        return isUnsafeStreamResult ? "SJSXP (with thread safety issue)" : "SJSXP";
    }

    public XMLInputFactory enableCDataReporting(XMLInputFactory factory) {
        factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
        factory.setProperty("http://java.sun.com/xml/stream/properties/report-cdata-event",
                Boolean.TRUE);
        return factory;
    }

    public XMLInputFactory disallowDoctypeDecl(XMLInputFactory factory) {
        // SJSXP is particular because when SUPPORT_DTD is set to false, no DTD event is reported.
        // This means that we would not be able to throw an exception. The trick is to enable
        // DTD support and trigger an exception if the parser attempts to load the external subset
        // or returns a DTD event.
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.TRUE);
        factory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        factory.setXMLResolver(new XMLResolver() {
            public Object resolveEntity(String publicID, String systemID, String baseURI,
                    String namespace) throws XMLStreamException {
                throw new XMLStreamException("DOCTYPE is not allowed");
            }
        });
        return new DisallowDoctypeDeclInputFactoryWrapper(factory);
    }

    public XMLInputFactory makeThreadSafe(XMLInputFactory factory) {
        factory.setProperty("reuse-instance", Boolean.FALSE);
        return factory;
    }

    public XMLOutputFactory makeThreadSafe(XMLOutputFactory factory) {
        factory.setProperty("reuse-instance", Boolean.FALSE);
        if (isUnsafeStreamResult) {
            factory = new SynchronizedOutputFactoryWrapper(factory);
        }
        return factory;
    }

    public XMLStreamReader normalize(XMLStreamReader reader) {
        return new SJSXPStreamReaderWrapper(reader);
    }

    public XMLStreamWriter normalize(XMLStreamWriter writer) {
        return new SJSXPStreamWriterWrapper(writer);
    }
    
    public XMLInputFactory normalize(XMLInputFactory factory) {
        return new NormalizingXMLInputFactoryWrapper(factory, this);
    }
    
    public XMLOutputFactory normalize(XMLOutputFactory factory) {
        return new SJSXPOutputFactoryWrapper(factory, this);
    }
}
