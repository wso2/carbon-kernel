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

package org.apache.axis2.databinding.utils.writer;

import org.apache.axiom.ext.stax.datahandler.DataHandlerProvider;
import org.apache.axiom.ext.stax.datahandler.DataHandlerWriter;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;

import javax.activation.DataHandler;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import java.io.IOException;
import java.util.*;


public class MTOMAwareOMBuilder implements XMLStreamWriter, DataHandlerWriter {
    // this is the om Element we are going to create
    private OMElement rootElement;
    private OMFactory omFactory;

    private OMElement currentOMElement;

    private Stack omElementStack;

    // this map contains the namespace key and OMNamespace
    private Map namespaceOMNamesapceMap;

    private int prefixNum;

    private OMStreamNamespaceContext omStreamNamespaceContext;

    /**
     * this stack keeps the name spaces added to the oMElementStack at a pirticular element
     * when end element is written the namspeces added at a pirticulare element is removed.
     */
    private Stack<List<String>> namespacesListStack;


    public MTOMAwareOMBuilder() {
        omFactory = OMAbstractFactory.getOMFactory();
        omElementStack = new Stack();
        currentOMElement = null;
        omStreamNamespaceContext = new OMStreamNamespaceContext();
        namespaceOMNamesapceMap = new HashMap();
        prefixNum = 0;
        namespacesListStack = new Stack<List<String>>();

    }

    // serailizer must have finish serializing when we call this method.
    public OMElement getOMElement() throws XMLStreamException {
        if (!omElementStack.isEmpty()) {
            throw new XMLStreamException("This is an invalid Xml ");
        }
        return rootElement;
    }

    private OMNamespace getOMNamespace(String namespace, String prefix) throws XMLStreamException {
        OMNamespace omNamespace = null;
        if (namespace != null) {
            if (namespaceOMNamesapceMap.containsKey(namespace)) {
                omNamespace = (OMNamespace) namespaceOMNamesapceMap.get(namespace);
            } else {
                if (prefix == null) {
                    prefix = "ns" + ++prefixNum;
                } 
                omNamespace = omFactory.createOMNamespace(namespace, prefix);
                this.omStreamNamespaceContext.registerNamespace(namespace, prefix);
                namespaceOMNamesapceMap.put(namespace, omNamespace);
                // add this namespce to top list as well
                namespacesListStack.peek().add(namespace);
            }
        }
        return omNamespace;
    }

    public void writeStartElement(String localName) throws XMLStreamException {
        writeStartElement(null, localName, null);
    }

    public void writeStartElement(String namespace, String localName) throws XMLStreamException {
        writeStartElement(null, localName, namespace);
    }

    public void writeStartElement(String prefix, String localName, String namespace) throws XMLStreamException {
        // add a new stack element at the start of a new element
        namespacesListStack.push(new ArrayList<String>());
        OMNamespace omNamespace = getOMNamespace(namespace, prefix);
        currentOMElement = omFactory.createOMElement(localName, omNamespace);
        if (!omElementStack.isEmpty()) {
            // we always keep the parent at the top of the stack
            OMElement parent = (OMElement) omElementStack.peek();
            parent.addChild(currentOMElement);
        } else {
            // i.e this must be an start root element
            rootElement = currentOMElement;
        }
        // set this as the top element
        omElementStack.push(currentOMElement);
    }

    public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
        writeEmptyElement(null, localName, namespaceURI);
    }

    public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        writeStartElement(prefix, localName, namespaceURI);
        writeEndElement();
    }

    public void writeEmptyElement(String localName) throws XMLStreamException {
        writeEmptyElement(null, localName, null);
    }

    public void writeEndElement() throws XMLStreamException {
        omElementStack.pop();
        // remove the namespaces declared by this element
        List<String> addedNamespaces = namespacesListStack.pop();
        for (String namespace : addedNamespaces){
            namespaceOMNamesapceMap.remove(namespace);
            omStreamNamespaceContext.removeNamespce(namespace);
        }
    }

    public void writeEndDocument() throws XMLStreamException {
        // nothing to do
    }

    public void close() throws XMLStreamException {
        // nothing to do
    }

    public void flush() throws XMLStreamException {
        // nothing to do


    }

    public void writeAttribute(String attributeName, String attributeValue) throws XMLStreamException {
        writeAttribute(null, null, attributeName, attributeValue);
    }

    public void writeAttribute(String prefix, String namespace, String attributeName, String attributeValue) throws XMLStreamException {
        currentOMElement.addAttribute(attributeName, attributeValue, getOMNamespace(namespace, prefix));
    }

    public void writeAttribute(String namespace, String attributeName, String attributeValue) throws XMLStreamException {
        writeAttribute(null, namespace, attributeName, attributeValue);
    }

    public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
        if (namespaceURI != null) {
            OMNamespace omNamespace = getOMNamespace(namespaceURI, prefix);
            currentOMElement.declareNamespace(omNamespace);
        }
    }

    public void writeDefaultNamespace(String namespace) throws XMLStreamException {
        rootElement.declareDefaultNamespace(namespace);
        getOMNamespace(namespace, "");
    }

    public void writeComment(String string) throws XMLStreamException {
        omFactory.createOMComment(currentOMElement, string);
    }

    public void writeProcessingInstruction(String string) throws XMLStreamException {
        throw new UnsupportedOperationException("this method has not yet been implemented");
    }

    public void writeProcessingInstruction(String string, String string1) throws XMLStreamException {
        throw new UnsupportedOperationException("this method has not yet been implemented");
    }

    public void writeCData(String string) throws XMLStreamException {
        throw new UnsupportedOperationException("this method has not yet been implemented");
    }

    public void writeDTD(String string) throws XMLStreamException {
        throw new UnsupportedOperationException("this method has not yet been implemented");
    }

    public void writeEntityRef(String string) throws XMLStreamException {
        throw new UnsupportedOperationException("this method has not yet been implemented");
    }

    public void writeStartDocument() throws XMLStreamException {
        // nothing to do
    }

    public void writeStartDocument(String string) throws XMLStreamException {
        // nothing to do
    }

    public void writeStartDocument(String string, String string1) throws XMLStreamException {
        // nothing to do
    }

    public void writeCharacters(String string) throws XMLStreamException {
        currentOMElement.setText(string);
    }

    public void writeCharacters(char[] chars, int i, int i1) throws XMLStreamException {
        writeCharacters(new String(chars, i, i1));
    }

    public String getPrefix(String namespace) throws XMLStreamException {
        return this.omStreamNamespaceContext.getPrefix(namespace);
    }

    public void setPrefix(String prefix, String uri) throws XMLStreamException {
        // this method will add the namespace correctly.
        getOMNamespace(uri, prefix);
    }

    public void setDefaultNamespace(String namespace) throws XMLStreamException {
        rootElement.declareDefaultNamespace(namespace);
        getOMNamespace(namespace, "");
    }

    public void setNamespaceContext(NamespaceContext namespaceContext) throws XMLStreamException {
        throw new UnsupportedOperationException("this method has not yet been implemented");
    }

    public NamespaceContext getNamespaceContext() {
        return this.omStreamNamespaceContext;
    }

    public Object getProperty(String name) throws IllegalArgumentException {
        if (DataHandlerWriter.PROPERTY.equals(name)) {
            return this;
        } else {
            throw new IllegalArgumentException("Property " + name + " not supported");
        }
    }

    public void writeDataHandler(DataHandler dataHandler, String contentID, boolean optimize)
            throws IOException, XMLStreamException {
        OMText omText = omFactory.createOMText(dataHandler, optimize);
        if (contentID != null) {
            omText.setContentID(contentID);
        }
        currentOMElement.addChild(omText);
    }

    public void writeDataHandler(DataHandlerProvider dataHandlerProvider, String contentID,
            boolean optimize) throws IOException, XMLStreamException {
        OMText omText = omFactory.createOMText(contentID, dataHandlerProvider, optimize);
        currentOMElement.addChild(omText);
    }
}
