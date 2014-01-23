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

import java.io.IOException;
import java.io.Writer;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * {@link Writer} implementation that writes data as
 * {@link javax.xml.stream.XMLStreamConstants#CHARACTERS} events to an {@link XMLStreamWriter}.
 * Note that this class
 * <ul>
 *   <li>doesn't buffer the data;</li>
 *   <li>ignores calls to {@link #flush()} and {@link #close()};</li>
 *   <li>is not thread-safe (synchronized).</li>
 * </ul>
 * Any {@link XMLStreamException} occurring in the underlying {@link XMLStreamWriter} will
 * be wrapped using {@link XMLStreamIOException}.
 */
public class XMLStreamWriterWriter extends Writer {
    private final XMLStreamWriter writer;

    /**
     * Constructor.
     * 
     * @param writer the XML stream writer to write the events to
     */
    public XMLStreamWriterWriter(XMLStreamWriter writer) {
        this.writer = writer;
    }

    public void write(char[] cbuf, int off, int len) throws IOException {
        try {
            writer.writeCharacters(cbuf, off, len);
        } catch (XMLStreamException ex) {
            throw new XMLStreamIOException(ex);
        }
    }

    public void write(String str, int off, int len) throws IOException {
        write(str.substring(off, off+len));
    }

    public void write(String str) throws IOException {
        try {
            writer.writeCharacters(str);
        } catch (XMLStreamException ex) {
            throw new XMLStreamIOException(ex);
        }
    }
    
    public void write(int c) throws IOException {
        write(new char[] { (char)c });
    }

    public void flush() throws IOException {
        // Do nothing
    }

    public void close() throws IOException {
        // Do nothing
    }
}
