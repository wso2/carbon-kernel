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
package org.apache.axiom.om.util;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * The base class for classes that are XMLStreamWriterFilters
 * Each of the XMLStreamWriter events is intercepted and passed to the delegate XMLStreamWriter
 * 
 * Character data is sent to the xmlData abstract method.  Derived classes may 
 * log or change the xml data.
 * 
 * @see XMLStreamWriterRemoveIllegalChars
 */
public abstract class XMLStreamWriterFilterBase implements XMLStreamWriterFilter {

	XMLStreamWriter delegate = null;

	public XMLStreamWriterFilterBase() {
	}
	
	public void setDelegate(XMLStreamWriter writer) {
		this.delegate = writer;
	}

	public XMLStreamWriter getDelegate() {
		return delegate;
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

	public String getPrefix(String uri) throws XMLStreamException {
		return delegate.getPrefix(uri);
	}

	public Object getProperty(String name) throws IllegalArgumentException {
		return delegate.getProperty(name);
	}

	public void setDefaultNamespace(String uri) throws XMLStreamException {
		delegate.setDefaultNamespace(uri);
	}

	public void setNamespaceContext(NamespaceContext context)
			throws XMLStreamException {
		delegate.setNamespaceContext(context);
	}

	public void setPrefix(String prefix, String uri) throws XMLStreamException {
		delegate.setPrefix(prefix, uri);
	}

	public void writeAttribute(String prefix, String namespaceURI,
			String localName, String value) throws XMLStreamException {
		delegate.writeAttribute(prefix, namespaceURI, localName, xmlData(value));
	}

	public void writeAttribute(String namespaceURI, String localName,
			String value) throws XMLStreamException {
		delegate.writeAttribute(namespaceURI, localName, xmlData(value));
	}

	public void writeAttribute(String localName, String value)
			throws XMLStreamException {
		delegate.writeAttribute(localName, xmlData(value));
	}

	public void writeCData(String data) throws XMLStreamException {
		delegate.writeCData(xmlData(data));
	}

	public void writeCharacters(char[] text, int start, int len)
			throws XMLStreamException {
	    // Adapt to writeCharacters that takes a String value
	    String value = new String(text, start, len);
		writeCharacters(value);
	}

	public void writeCharacters(String text) throws XMLStreamException {
		delegate.writeCharacters(xmlData(text));
	}

	public void writeComment(String data) throws XMLStreamException {
		delegate.writeComment(data);
	}

	public void writeDTD(String dtd) throws XMLStreamException {
		delegate.writeDTD(dtd);
	}

	public void writeDefaultNamespace(String namespaceURI)
			throws XMLStreamException {
		delegate.writeDefaultNamespace(namespaceURI);
	}

	public void writeEmptyElement(String prefix, String localName,
			String namespaceURI) throws XMLStreamException {
		delegate.writeEmptyElement(prefix, localName, namespaceURI);
	}

	public void writeEmptyElement(String namespaceURI, String localName)
			throws XMLStreamException {
		delegate.writeEmptyElement(namespaceURI, localName);
	}

	public void writeEmptyElement(String localName) throws XMLStreamException {
		delegate.writeEmptyElement(localName);
	}

	public void writeEndDocument() throws XMLStreamException {
		delegate.writeEndDocument();
	}

	public void writeEndElement() throws XMLStreamException {
		delegate.writeEndElement();
	}

	public void writeEntityRef(String name) throws XMLStreamException {
		delegate.writeEntityRef(name);
	}

	public void writeNamespace(String prefix, String namespaceURI)
			throws XMLStreamException {
		delegate.writeNamespace(prefix, namespaceURI);
	}

	public void writeProcessingInstruction(String target, String data)
			throws XMLStreamException {
		delegate.writeProcessingInstruction(target, data);
	}

	public void writeProcessingInstruction(String target)
			throws XMLStreamException {
		delegate.writeProcessingInstruction(target);
	}

	public void writeStartDocument() throws XMLStreamException {
		delegate.writeStartDocument();
	}

	public void writeStartDocument(String encoding, String version)
			throws XMLStreamException {
		delegate.writeStartDocument(encoding, version);
	}

	public void writeStartDocument(String version) throws XMLStreamException {
		delegate.writeStartDocument(version);
	}

	public void writeStartElement(String prefix, String localName,
			String namespaceURI) throws XMLStreamException {
		delegate.writeStartElement(prefix, localName, namespaceURI);
	}

	public void writeStartElement(String namespaceURI, String localName)
			throws XMLStreamException {
		delegate.writeStartElement(namespaceURI, localName);
	}

	public void writeStartElement(String localName) throws XMLStreamException {
		delegate.writeStartElement(localName);
	}

	/**
	 * Derived classes extend the method.  A derived class may log or modify the xml data
	 * @param value
	 * @return value
	 */
	protected abstract String xmlData(String value);

}
