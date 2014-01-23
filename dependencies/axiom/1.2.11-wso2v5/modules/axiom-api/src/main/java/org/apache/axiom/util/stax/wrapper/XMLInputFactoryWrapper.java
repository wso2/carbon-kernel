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

package org.apache.axiom.util.stax.wrapper;

import java.io.InputStream;
import java.io.Reader;

import javax.xml.stream.EventFilter;
import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.XMLEventAllocator;
import javax.xml.transform.Source;

/**
 * Base class for {@link XMLInputFactory} wrappers. The class provides default implementations for
 * all methods. Each of them calls the corresponding method in the parent factory.
 */
public class XMLInputFactoryWrapper extends XMLInputFactory {
    private final XMLInputFactory parent;

    /**
     * Constructor.
     * 
     * @param parent the parent factory
     */
    public XMLInputFactoryWrapper(XMLInputFactory parent) {
        this.parent = parent;
    }

    public XMLEventReader createFilteredReader(XMLEventReader reader, EventFilter filter)
            throws XMLStreamException {
        return parent.createFilteredReader(reader, filter);
    }

    public XMLStreamReader createFilteredReader(XMLStreamReader reader, StreamFilter filter)
            throws XMLStreamException {
        return parent.createFilteredReader(reader, filter);
    }

    public XMLEventReader createXMLEventReader(InputStream stream, String encoding)
            throws XMLStreamException {
        return parent.createXMLEventReader(stream, encoding);
    }

    public XMLEventReader createXMLEventReader(InputStream stream) throws XMLStreamException {
        return parent.createXMLEventReader(stream);
    }

    public XMLEventReader createXMLEventReader(Reader reader) throws XMLStreamException {
        return parent.createXMLEventReader(reader);
    }

    public XMLEventReader createXMLEventReader(Source source) throws XMLStreamException {
        return parent.createXMLEventReader(source);
    }

    public XMLEventReader createXMLEventReader(String systemId, InputStream stream)
            throws XMLStreamException {
        return parent.createXMLEventReader(systemId, stream);
    }

    public XMLEventReader createXMLEventReader(String systemId, Reader reader)
            throws XMLStreamException {
        return parent.createXMLEventReader(systemId, reader);
    }

    public XMLEventReader createXMLEventReader(XMLStreamReader reader) throws XMLStreamException {
        return parent.createXMLEventReader(reader);
    }

    public XMLStreamReader createXMLStreamReader(InputStream stream, String encoding)
            throws XMLStreamException {
        return parent.createXMLStreamReader(stream, encoding);
    }

    public XMLStreamReader createXMLStreamReader(InputStream stream) throws XMLStreamException {
        return parent.createXMLStreamReader(stream);
    }

    public XMLStreamReader createXMLStreamReader(Reader reader) throws XMLStreamException {
        return parent.createXMLStreamReader(reader);
    }

    public XMLStreamReader createXMLStreamReader(Source source) throws XMLStreamException {
        return parent.createXMLStreamReader(source);
    }

    public XMLStreamReader createXMLStreamReader(String systemId, InputStream stream)
            throws XMLStreamException {
        return parent.createXMLStreamReader(systemId, stream);
    }

    public XMLStreamReader createXMLStreamReader(String systemId, Reader reader)
            throws XMLStreamException {
        return parent.createXMLStreamReader(systemId, reader);
    }

    public XMLEventAllocator getEventAllocator() {
        return parent.getEventAllocator();
    }

    public Object getProperty(String name) throws IllegalArgumentException {
        return parent.getProperty(name);
    }

    public XMLReporter getXMLReporter() {
        return parent.getXMLReporter();
    }

    public XMLResolver getXMLResolver() {
        return parent.getXMLResolver();
    }

    public boolean isPropertySupported(String name) {
        return parent.isPropertySupported(name);
    }

    public void setEventAllocator(XMLEventAllocator allocator) {
        parent.setEventAllocator(allocator);
    }

    public void setProperty(String name, Object value) throws IllegalArgumentException {
        parent.setProperty(name, value);
    }

    public void setXMLReporter(XMLReporter reporter) {
        parent.setXMLReporter(reporter);
    }

    public void setXMLResolver(XMLResolver resolver) {
        parent.setXMLResolver(resolver);
    }
}
