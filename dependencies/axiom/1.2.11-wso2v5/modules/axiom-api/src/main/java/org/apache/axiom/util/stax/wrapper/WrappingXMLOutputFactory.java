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

import java.io.OutputStream;
import java.io.Writer;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;

/**
 * {@link XMLOutputFactory} wrapper that wraps all {@link XMLEventWriter} and {@link XMLStreamWriter}
 * instances created from it.
 */
public class WrappingXMLOutputFactory extends XMLOutputFactoryWrapper {
    /**
     * Constructor.
     * 
     * @param parent the parent factory
     */
    public WrappingXMLOutputFactory(XMLOutputFactory parent) {
        super(parent);
    }

    /**
     * Wrap a writer created from this factory. Implementations should override this method if they
     * which to wrap {@link XMLEventWriter} instances created from the factory. The default
     * implementation simply returns the unwrapped writer.
     * 
     * @param writer
     *            the writer to wrap
     * @return the wrapped writer
     */
    protected XMLEventWriter wrap(XMLEventWriter writer) {
        return writer;
    }
    
    /**
     * Wrap a writer created from this factory. Implementations should override this method if they
     * which to wrap {@link XMLStreamWriter} instances created from the factory. The default
     * implementation simply returns the unwrapped writer.
     * 
     * @param writer
     *            the writer to wrap
     * @return the wrapped writer
     */
    protected XMLStreamWriter wrap(XMLStreamWriter writer) {
        return writer;
    }

    public XMLEventWriter createXMLEventWriter(OutputStream stream, String encoding)
            throws XMLStreamException {
        return wrap(super.createXMLEventWriter(stream, encoding));
    }

    public XMLEventWriter createXMLEventWriter(OutputStream stream) throws XMLStreamException {
        return wrap(super.createXMLEventWriter(stream));
    }

    public XMLEventWriter createXMLEventWriter(Result result) throws XMLStreamException {
        return wrap(super.createXMLEventWriter(result));
    }

    public XMLEventWriter createXMLEventWriter(Writer stream) throws XMLStreamException {
        return wrap(super.createXMLEventWriter(stream));
    }

    public XMLStreamWriter createXMLStreamWriter(OutputStream stream, String encoding)
            throws XMLStreamException {
        return wrap(super.createXMLStreamWriter(stream, encoding));
    }

    public XMLStreamWriter createXMLStreamWriter(OutputStream stream) throws XMLStreamException {
        return wrap(super.createXMLStreamWriter(stream));
    }

    public XMLStreamWriter createXMLStreamWriter(Result result) throws XMLStreamException {
        return wrap(super.createXMLStreamWriter(result));
    }

    public XMLStreamWriter createXMLStreamWriter(Writer stream) throws XMLStreamException {
        return wrap(super.createXMLStreamWriter(stream));
    }
}
