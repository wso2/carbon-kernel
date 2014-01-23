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

/**
 * 
 */
package org.apache.axiom.om;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;
import java.io.Writer;

/**
 * Interface to arbitrary source of XML element data. This provides the hook for using a general
 * data source (such as data binding frameworks) as the backing source of data for an element.
 */
public interface OMDataSource {
    /**
     * Serializes element data directly to stream.
     *
     * @param output destination stream for element XML text
     * @param format Output format information. The implementation must use this information
     *               to choose the correct character set encoding when writing to the
     *               output stream. This parameter must not be null.
     * @throws XMLStreamException
     */
    void serialize(OutputStream output, OMOutputFormat format)
            throws XMLStreamException;

    /**
     * Serializes element data directly to writer.
     *
     * @param writer destination writer for element XML text
     * @param format output format information (<code>null</code> if none; may be ignored if not
     *               supported by data binding even if supplied)
     * @throws XMLStreamException
     */
    void serialize(Writer writer, OMOutputFormat format)
            throws XMLStreamException;

    /**
     * Serializes element data directly to StAX writer.
     *
     * @param xmlWriter destination writer
     * @throws XMLStreamException
     */
    // TODO: specify whether the implementation MUST, MAY or MUST NOT write START_DOCUMENT and END_DOCUMENT events to the stream
    void serialize(XMLStreamWriter xmlWriter)
            throws XMLStreamException;

    /**
     * Get parser for element data. In the general case this may require the data source to
     * serialize data as XML text and then parse that text.
     *
     * @return element parser
     * @throws XMLStreamException
     */
    XMLStreamReader getReader() throws XMLStreamException;
}
