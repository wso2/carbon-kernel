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

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.ext.stax.DelegatingXMLStreamReader;
import org.apache.axiom.util.stax.wrapper.XMLStreamReaderWrapper;

class BEAStreamReaderWrapper extends XMLStreamReaderWrapper implements DelegatingXMLStreamReader {
    /**
     * The character set encoding as inferred from the start bytes of the stream.
     */
    private final String encodingFromStartBytes;
    
    private int depth;
    
    public BEAStreamReaderWrapper(XMLStreamReader parent, String encodingFromStartBytes) {
        super(parent);
        this.encodingFromStartBytes = encodingFromStartBytes;
    }

    public String getCharacterEncodingScheme() {
        if (getEventType() == START_DOCUMENT) {
            return super.getCharacterEncodingScheme();
        } else {
            throw new IllegalStateException();
        }
    }

    public String getVersion() {
        if (getEventType() == START_DOCUMENT) {
            return super.getVersion();
        } else {
            throw new IllegalStateException();
        }
    }

    public boolean isStandalone() {
        if (getEventType() == START_DOCUMENT) {
            return super.isStandalone();
        } else {
            throw new IllegalStateException();
        }
    }

    public boolean standaloneSet() {
        if (getEventType() == START_DOCUMENT) {
            return super.standaloneSet();
        } else {
            throw new IllegalStateException();
        }
    }

    public int next() throws XMLStreamException {
        if (!hasNext()) {
            // The reference implementation throws an XMLStreamException in this case.
            // This can't be considered as compliant with the specifications.
            throw new IllegalStateException("Already reached end of document");
        } else {
            int event = super.next();
            switch (event) {
                case START_ELEMENT: depth++; break;
                case END_ELEMENT: depth--;
            }
            return event;
        }
    }

    public String getEncoding() {
        if (getEventType() == START_DOCUMENT) {
            String encoding = super.getEncoding();
            if (encoding != null) {
                return encoding;
            } else {
                if (encodingFromStartBytes == null) {
                    // This means that the reader was created from a character stream
                    // ==> always return null
                    return null;
                } else {
                    // If an XML encoding declaration was present, return the specified
                    // encoding, otherwise fall back to the encoding we detected in
                    // the factory wrapper
                    encoding = getCharacterEncodingScheme();
                    return encoding == null ? encodingFromStartBytes : encoding;
                }
            }
        } else {
            throw new IllegalStateException();
        }
    }

    public String getText() {
        // The reference implementation fails to normalize line endings in the prolog/epilog; we work
        // around this at least for getText since this bug causes a test failure in the Axiom unit
        // tests on Windows.
        if (depth == 0) {
            String text = super.getText();
            StringBuffer buffer = null;
            int len = text.length();
            for (int i=0; i<len; i++) {
                char c = text.charAt(i);
                if (c == '\r' && (i==len || text.charAt(i+1) == '\n')) {
                    if (buffer == null) {
                        buffer = new StringBuffer(len-1);
                        buffer.append(text.substring(0, i));
                    }
                } else {
                    if (buffer != null) {
                        buffer.append(c);
                    }
                }
            }
            return buffer != null ? buffer.toString() : text;
        } else {
            return super.getText();
        }
    }

    public NamespaceContext getNamespaceContext() {
        // The NamespaceContext returned by the reference doesn't handle the
        // implicit namespace bindings (for the "xml" and "xmlns" prefixes)
        // correctly
        return new ImplicitNamespaceContextWrapper(
                new NamespaceURICorrectingNamespaceContextWrapper(super.getNamespaceContext()));
    }

    public XMLStreamReader getParent() {
        return super.getParent();
    }
}
