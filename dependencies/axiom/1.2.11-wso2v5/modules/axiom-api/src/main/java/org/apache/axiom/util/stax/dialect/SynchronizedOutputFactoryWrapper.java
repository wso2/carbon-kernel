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

import java.io.OutputStream;
import java.io.Writer;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;

import org.apache.axiom.util.stax.wrapper.XMLOutputFactoryWrapper;

class SynchronizedOutputFactoryWrapper extends XMLOutputFactoryWrapper {
    public SynchronizedOutputFactoryWrapper(XMLOutputFactory parent) {
        super(parent);
    }

    public synchronized XMLEventWriter createXMLEventWriter(OutputStream stream, String encoding)
            throws XMLStreamException {
        return super.createXMLEventWriter(stream, encoding);
    }

    public synchronized XMLEventWriter createXMLEventWriter(OutputStream stream)
            throws XMLStreamException {
        return super.createXMLEventWriter(stream);
    }

    public synchronized XMLEventWriter createXMLEventWriter(Result result)
            throws XMLStreamException {
        return super.createXMLEventWriter(result);
    }

    public synchronized XMLEventWriter createXMLEventWriter(Writer stream)
            throws XMLStreamException {
        return super.createXMLEventWriter(stream);
    }

    public synchronized XMLStreamWriter createXMLStreamWriter(OutputStream stream, String encoding)
            throws XMLStreamException {
        return super.createXMLStreamWriter(stream, encoding);
    }

    public synchronized XMLStreamWriter createXMLStreamWriter(OutputStream stream)
            throws XMLStreamException {
        return super.createXMLStreamWriter(stream);
    }

    public synchronized XMLStreamWriter createXMLStreamWriter(Result result)
            throws XMLStreamException {
        return super.createXMLStreamWriter(result);
    }

    public synchronized XMLStreamWriter createXMLStreamWriter(Writer stream)
            throws XMLStreamException {
        return super.createXMLStreamWriter(stream);
    }
}
