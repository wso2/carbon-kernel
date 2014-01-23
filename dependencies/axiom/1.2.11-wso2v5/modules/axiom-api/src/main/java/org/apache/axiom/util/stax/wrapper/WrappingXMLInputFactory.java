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
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;

/**
 * {@link XMLInputFactory} wrapper that wraps all {@link XMLEventReader} and {@link XMLStreamReader}
 * instances created from it.
 */
public class WrappingXMLInputFactory extends XMLInputFactoryWrapper {
    /**
     * Constructor.
     * 
     * @param parent the parent factory
     */
    public WrappingXMLInputFactory(XMLInputFactory parent) {
        super(parent);
    }

    /**
     * Wrap a reader created from this factory. Implementations should override this method if they
     * which to wrap {@link XMLEventReader} instances created from the factory. The default
     * implementation simply returns the unwrapped reader.
     * 
     * @param reader
     *            the reader to wrap
     * @return the wrapped reader
     */
    protected XMLEventReader wrap(XMLEventReader reader) {
        return reader;
    }
    
    /**
     * Wrap a reader created from this factory. Implementations should override this method if they
     * which to wrap {@link XMLStreamReader} instances created from the factory. The default
     * implementation simply returns the unwrapped reader.
     * 
     * @param reader
     *            the reader to wrap
     * @return the wrapped reader
     */
    protected XMLStreamReader wrap(XMLStreamReader reader) {
        return reader;
    }

    public XMLEventReader createFilteredReader(XMLEventReader reader, EventFilter filter)
            throws XMLStreamException {
        return wrap(super.createFilteredReader(reader, filter));
    }

    public XMLStreamReader createFilteredReader(XMLStreamReader reader, StreamFilter filter)
            throws XMLStreamException {
        return wrap(super.createFilteredReader(reader, filter));
    }

    public XMLEventReader createXMLEventReader(InputStream stream, String encoding)
            throws XMLStreamException {
        return wrap(super.createXMLEventReader(stream, encoding));
    }

    public XMLEventReader createXMLEventReader(InputStream stream) throws XMLStreamException {
        return wrap(super.createXMLEventReader(stream));
    }

    public XMLEventReader createXMLEventReader(Reader reader) throws XMLStreamException {
        return wrap(super.createXMLEventReader(reader));
    }

    public XMLEventReader createXMLEventReader(Source source) throws XMLStreamException {
        return wrap(super.createXMLEventReader(source));
    }

    public XMLEventReader createXMLEventReader(String systemId, InputStream stream)
            throws XMLStreamException {
        return wrap(super.createXMLEventReader(systemId, stream));
    }

    public XMLEventReader createXMLEventReader(String systemId, Reader reader)
            throws XMLStreamException {
        return wrap(super.createXMLEventReader(systemId, reader));
    }

    public XMLEventReader createXMLEventReader(XMLStreamReader reader) throws XMLStreamException {
        return wrap(super.createXMLEventReader(reader));
    }

    public XMLStreamReader createXMLStreamReader(InputStream stream, String encoding)
            throws XMLStreamException {
        return wrap(super.createXMLStreamReader(stream, encoding));
    }

    public XMLStreamReader createXMLStreamReader(InputStream stream) throws XMLStreamException {
        return wrap(super.createXMLStreamReader(stream));
    }

    public XMLStreamReader createXMLStreamReader(Reader reader) throws XMLStreamException {
        return wrap(super.createXMLStreamReader(reader));
    }

    public XMLStreamReader createXMLStreamReader(Source source) throws XMLStreamException {
        return wrap(super.createXMLStreamReader(source));
    }

    public XMLStreamReader createXMLStreamReader(String systemId, InputStream stream)
            throws XMLStreamException {
        return wrap(super.createXMLStreamReader(systemId, stream));
    }

    public XMLStreamReader createXMLStreamReader(String systemId, Reader reader)
            throws XMLStreamException {
        return wrap(super.createXMLStreamReader(systemId, reader));
    }
}
