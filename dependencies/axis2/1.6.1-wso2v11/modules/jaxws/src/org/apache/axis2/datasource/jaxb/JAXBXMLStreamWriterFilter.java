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

package org.apache.axis2.datasource.jaxb;

import org.apache.axis2.jaxws.utility.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * On some occasions, JAXB writes out incorrect xmlns and namespace information. This filter is
 * useful for logging and correcting these kinds of problems.
 */
class JAXBXMLStreamWriterFilter implements XMLStreamWriter {
    private static final Log log = LogFactory.getLog(JAXBXMLStreamWriterFilter.class);

    XMLStreamWriter delegate;
    int numElements = 0;
    int numDefaultNS = 0;

    public JAXBXMLStreamWriterFilter(XMLStreamWriter delegate) {
        this.delegate = delegate;
    }

    public void close() throws XMLStreamException {
        delegate.close();
    }

    public void flush() throws XMLStreamException {
        delegate.flush();
    }

    public NamespaceContext getNamespaceContext() {
        return delegate.getNamespaceContext();
    }

    public String getPrefix(String arg0) throws XMLStreamException {
        return delegate.getPrefix(arg0);
    }

    public Object getProperty(String arg0) throws IllegalArgumentException {
        return delegate.getProperty(arg0);
    }

    public void setDefaultNamespace(String namespaceURI) throws XMLStreamException {
        if (numElements == 1) {
            if (log.isDebugEnabled()) {
                log.debug("  default namespaceURI=" + namespaceURI);
            }
            numDefaultNS++;
            if (numDefaultNS > 1) {
                // Sometimes JAXB writes out the default namespace twice
                // This seems to be related to writing out xmlns...see below
                if (log.isDebugEnabled()) {
                    log.debug("    WHY IS THE DEFAULT NAMESPACE WRITTEN TWICE?");
                    log.trace(JavaUtils.stackToString());
                }
                return;
            }
        }
        delegate.setDefaultNamespace(namespaceURI);
    }

    public void setNamespaceContext(NamespaceContext arg0) throws XMLStreamException {
        delegate.setNamespaceContext(arg0);
    }

    public void setPrefix(String arg0, String arg1) throws XMLStreamException {
        delegate.setPrefix(arg0, arg1);
    }

    public void writeAttribute(String prefix, String namespaceURI, String localName, String value)
            throws XMLStreamException {
        if (numElements == 1) {
            if (log.isDebugEnabled()) {
                log.debug("  prefix=" + prefix + " namespace=" + namespaceURI + " localName=" +
                        localName + " value=" + value);
            }
        }
        delegate.writeAttribute(prefix, namespaceURI, localName, value);
    }

    public void writeAttribute(String namespaceURI, String localName, String value)
            throws XMLStreamException {
        if (numElements == 1) {
            if (log.isDebugEnabled()) {
                log.debug("  namespace=" + namespaceURI + " localName=" + localName + " value=" +
                        value);
            }
        }
        delegate.writeAttribute(namespaceURI, localName, value);
    }

    public void writeAttribute(String localName, String value) throws XMLStreamException {
        if (numElements == 1) {
            if (log.isDebugEnabled()) {
                log.debug("  localName=" + localName + " value=" + value);
            }
        }
        delegate.writeAttribute(localName, value);
    }

    public void writeCData(String arg0) throws XMLStreamException {
        delegate.writeCData(arg0);
    }

    public void writeCharacters(char[] arg0, int arg1, int arg2) throws XMLStreamException {
        delegate.writeCharacters(arg0, arg1, arg2);
    }

    public void writeCharacters(String arg0) throws XMLStreamException {
        delegate.writeCharacters(arg0);
    }

    public void writeComment(String arg0) throws XMLStreamException {
        delegate.writeComment(arg0);
    }

    public void writeDefaultNamespace(String arg0) throws XMLStreamException {
        delegate.writeDefaultNamespace(arg0);
    }

    public void writeDTD(String arg0) throws XMLStreamException {
        delegate.writeDTD(arg0);
    }

    public void writeEmptyElement(String arg0, String arg1, String arg2) throws XMLStreamException {
        delegate.writeEmptyElement(arg0, arg1, arg2);
    }

    public void writeEmptyElement(String arg0, String arg1) throws XMLStreamException {
        delegate.writeEmptyElement(arg0, arg1);
    }

    public void writeEmptyElement(String arg0) throws XMLStreamException {
        delegate.writeEmptyElement(arg0);
    }

    public void writeEndDocument() throws XMLStreamException {
        delegate.writeEndDocument();
    }

    public void writeEndElement() throws XMLStreamException {
        delegate.writeEndElement();
    }

    public void writeEntityRef(String arg0) throws XMLStreamException {
        delegate.writeEntityRef(arg0);
    }

    public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
        if (numElements == 1) {
            if (log.isDebugEnabled()) {
                log.debug("  prefix=" + prefix + " namespaceURI=" + namespaceURI);
            }
            if ("xmlns".equals(prefix)) {
                // Sometimes JAXB writes out the XMLNS attribute...need to find out why
                if (log.isDebugEnabled()) {
                    log.debug("    INVALID XMLNS attribute is removed prefix=");
                    log.trace(JavaUtils.stackToString());
                }

                return;
            }
        }
        delegate.writeNamespace(prefix, namespaceURI);
    }

    public void writeProcessingInstruction(String arg0, String arg1) throws XMLStreamException {
        delegate.writeProcessingInstruction(arg0, arg1);
    }

    public void writeProcessingInstruction(String arg0) throws XMLStreamException {
        delegate.writeProcessingInstruction(arg0);
    }

    public void writeStartDocument() throws XMLStreamException {
        delegate.writeStartDocument();
    }

    public void writeStartDocument(String arg0, String arg1) throws XMLStreamException {
        delegate.writeStartDocument(arg0, arg1);
    }

    public void writeStartDocument(String arg0) throws XMLStreamException {
        delegate.writeStartDocument(arg0);
    }

    public void writeStartElement(String prefix, String namespaceURI, String localName)
            throws XMLStreamException {
        numElements++;
        if (numElements == 1) {
            if (log.isDebugEnabled()) {
                log.debug("  prefix=" + prefix + " namespace=" + namespaceURI + " localName=" +
                        localName);
            }
        }
        delegate.writeStartElement(prefix, namespaceURI, localName);
    }

    public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
        numElements++;
        if (numElements == 1) {
            if (log.isDebugEnabled()) {
                log.debug("  namespace=" + namespaceURI + " localName=" + localName);
            }
        }
        delegate.writeStartElement(namespaceURI, localName);
    }

    public void writeStartElement(String localName) throws XMLStreamException {
        numElements++;
        if (numElements == 1) {
            if (log.isDebugEnabled()) {
                log.debug("  localName=" + localName);
            }
        }
        delegate.writeStartElement(localName);
    }
}