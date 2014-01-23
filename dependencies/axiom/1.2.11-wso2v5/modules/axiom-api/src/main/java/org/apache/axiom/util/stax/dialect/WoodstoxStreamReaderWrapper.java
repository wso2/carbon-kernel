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
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.ext.stax.DelegatingXMLStreamReader;
import org.apache.axiom.util.stax.wrapper.XMLStreamReaderWrapper;

class WoodstoxStreamReaderWrapper extends XMLStreamReaderWrapper implements DelegatingXMLStreamReader {
    public WoodstoxStreamReaderWrapper(XMLStreamReader reader) {
        super(reader);
    }

    public String getCharacterEncodingScheme() {
        if (getEventType() == START_DOCUMENT) {
            return super.getCharacterEncodingScheme();
        } else {
            throw new IllegalStateException();
        }
    }

    public String getEncoding() {
        if (getEventType() == START_DOCUMENT) {
            return super.getEncoding();
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

    public boolean isCharacters() {
        // TODO: in the dialect detection we should take into account the Woodstox version,
        //       so that we can avoid creating the wrapper for Woodstox versions where this
        //       issue has been fixed
        // This addresses WSTX-201:
        return getEventType() == CHARACTERS;
    }

    public String getPrefix() {
        // Woodstox 4.0 may return "" instead of null
        String prefix = super.getPrefix();
        return prefix == null || prefix.isEmpty() ? null : prefix;
    }

    public String getNamespaceURI() {
        // Woodstox 4.0 may return "" instead of null
        String uri = super.getNamespaceURI();
        return uri == null || uri.isEmpty() ? null : uri;
    }

    public String getNamespaceURI(String prefix) {
        // Woodstox 4.0 may return "" instead of null
        String uri = super.getNamespaceURI(prefix);
        return uri == null || uri.isEmpty() ? null : uri;
    }

    public String getNamespacePrefix(int index) {
        // Woodstox 4.0 may return "" instead of null
        String prefix = super.getNamespacePrefix(index);
        return prefix == null || prefix.isEmpty() ? null : prefix;
    }

    public String getAttributeNamespace(int index) {
        // Woodstox 4.0 may return "" instead of null
        String uri = super.getAttributeNamespace(index);
        return uri == null || uri.isEmpty() ? null : uri;
    }

    public NamespaceContext getNamespaceContext() {
        return new NamespaceURICorrectingNamespaceContextWrapper(super.getNamespaceContext());
    }

    public XMLStreamReader getParent() {
        return super.getParent();
    }
}
