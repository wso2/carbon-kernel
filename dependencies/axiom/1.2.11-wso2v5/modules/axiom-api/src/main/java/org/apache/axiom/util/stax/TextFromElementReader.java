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
import java.io.Reader;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * {@link Reader} implementation that extracts the text nodes from an element given by an
 * {@link XMLStreamReader}. The expected input is a document with only a document
 * element (as produced by {@link org.apache.axiom.om.OMElement#getXMLStreamReader()}).
 * The class will extract the text nodes that are direct children of that element, i.e. it uses
 * the same conventions as {@link org.apache.axiom.om.OMElement#getText()}.
 * It will call {@link XMLStreamReader#close()} when the end of the document is reached or when
 * {@link #close()} is called.
 * <p>
 * The main purpose of this class is to provide a convenient and efficient way to get the text
 * content of an element without converting it first to a string, i.e. without using
 * {@link org.apache.axiom.om.OMElement#getText()}. This is important for potentially
 * large contents, for which this class guarantees constant memory usage.
 * <p>
 * Note that this class should in general not be used directly. Instead, 
 * {@link org.apache.axiom.om.util.ElementHelper#getTextAsStream(org.apache.axiom.om.OMElement)}
 * should be called to get the most efficient stream implementation for a given an element.
 */
// This class has package access -> use XMLStreamReaderUtils#getElementTextAsStream
class TextFromElementReader extends Reader {
    private final XMLStreamReader stream;
    private final boolean allowNonTextChildren;
    
    /**
     * Flag indicating that we have reached the end of the document and that the underlying
     * parser has been closed.
     */
    private boolean endOfStream;
    
    /**
     * The current depth relative to the document element (not the document). A value greater than
     * 0 indicates that we are inside a nested element and that we need to skip text nodes.
     */
    private int skipDepth;
    
    /**
     * The current position in the character data of the event, or -1 if all the character data
     * has been consumed and a new event needs to be requested from the parser.
     */
    private int sourceStart = -1;
    
    TextFromElementReader(XMLStreamReader stream, boolean allowNonTextChildren) {
        this.stream = stream;
        this.allowNonTextChildren = allowNonTextChildren;
    }

    public int read(char[] cbuf, int off, int len) throws IOException {
        if (endOfStream) {
            return -1;
        }
        int read = 0;
        try {
            while (true) {
                if (sourceStart == -1) {
                    eventLoop: while (true) {
                        int type = stream.next();
                        switch (type) {
                            case XMLStreamReader.CHARACTERS:
                            case XMLStreamReader.CDATA:
                                if (skipDepth == 0) {
                                    sourceStart = 0;
                                    break eventLoop;
                                }
                                break;
                            case XMLStreamReader.START_ELEMENT:
                                if (allowNonTextChildren) {
                                    skipDepth++;
                                } else {
                                    throw new IOException("Unexpected START_ELEMENT event");
                                }
                                break;
                            case XMLStreamReader.END_ELEMENT:
                                if (skipDepth == 0) {
                                    endOfStream = true;
                                    return read == 0 ? -1 : read;
                                } else {
                                    skipDepth--;
                                }
                        }
                    }
                }
                int c = stream.getTextCharacters(sourceStart, cbuf, off, len);
                sourceStart += c;
                off += c;
                len -= c;
                read += c;
                if (len > 0) {
                    sourceStart = -1;
                } else {
                    return read;
                }
            }
        } catch (XMLStreamException ex) {
            throw new XMLStreamIOException(ex);
        }
    }

    public void close() throws IOException {
        // Do nothing
    }
}
