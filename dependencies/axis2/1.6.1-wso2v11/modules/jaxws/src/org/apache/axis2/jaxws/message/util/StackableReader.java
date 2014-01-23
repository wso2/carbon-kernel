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

package org.apache.axis2.jaxws.message.util;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.Stack;

/**
 * StackableReader A StackableStreamReader provides an additional method push(XMLStreamReader)
 * <p/>
 * You can call push(...) to add a new XMLStreamReader.  The next event will use the pushed stream
 * reader. After the XMLStreamReader is consumed, it is automatically popped off of the stack.
 * <p/>
 * Note the information returned by the StackableReader is only applicable for the topmost
 * XMLStreamReader.  For example the NamespaceContext that is returned is not a combination of all
 * the namespace contexts on the stack.
 */
public class StackableReader implements XMLStreamReader {

    Stack<XMLStreamReader> stack = new Stack<XMLStreamReader>();
    XMLStreamReader current = null;

    /**
     * Create a stackable reader with the initial reader
     *
     * @param first
     */
    public StackableReader(XMLStreamReader first) {
        current = first;
    }

    /**
     * Push a new StreamReader
     *
     * @param streamReader
     */
    public void push(XMLStreamReader streamReader) throws XMLStreamException {
        // Push the current reader if it is not consumed
        if (current != null &&
                current.hasNext()) {
            stack.push(current);
        }
        current = streamReader;
    }

    public void close() throws XMLStreamException {
        current.close();
    }

    public int getAttributeCount() {
        return current.getAttributeCount();
    }

    public String getAttributeLocalName(int arg0) {
        return current.getAttributeLocalName(arg0);
    }

    public QName getAttributeName(int arg0) {
        return current.getAttributeName(arg0);
    }

    public String getAttributeNamespace(int arg0) {
        return current.getAttributeNamespace(arg0);
    }

    public String getAttributePrefix(int arg0) {
        return current.getAttributePrefix(arg0);
    }

    public String getAttributeType(int arg0) {
        return current.getAttributeType(arg0);
    }

    public String getAttributeValue(int arg0) {
        return current.getAttributeValue(arg0);
    }

    public String getAttributeValue(String arg0, String arg1) {
        return current.getAttributeValue(arg0, arg1);
    }

    public String getCharacterEncodingScheme() {
        return current.getCharacterEncodingScheme();
    }

    public String getElementText() throws XMLStreamException {
        return current.getElementText();
    }

    public String getEncoding() {
        return current.getEncoding();
    }

    public int getEventType() {
        return current.getEventType();
    }

    public String getLocalName() {
        return current.getLocalName();
    }

    public Location getLocation() {
        return current.getLocation();
    }

    public QName getName() {
        return current.getName();
    }

    public NamespaceContext getNamespaceContext() {
        return current.getNamespaceContext();
    }

    public int getNamespaceCount() {
        return current.getNamespaceCount();
    }

    public String getNamespacePrefix(int arg0) {
        return current.getNamespacePrefix(arg0);
    }

    public String getNamespaceURI() {
        return current.getNamespaceURI();
    }

    public String getNamespaceURI(int arg0) {
        return current.getNamespaceURI(arg0);
    }

    public String getNamespaceURI(String arg0) {
        return current.getNamespaceURI(arg0);
    }

    public String getPIData() {
        return current.getPIData();
    }

    public String getPITarget() {
        return current.getPITarget();
    }

    public String getPrefix() {
        return current.getPrefix();
    }

    public Object getProperty(String arg0) throws IllegalArgumentException {
        return current.getProperty(arg0);
    }

    public String getText() {
        return current.getText();
    }

    public char[] getTextCharacters() {
        return current.getTextCharacters();
    }

    public int getTextCharacters(int arg0, char[] arg1, int arg2, int arg3)
            throws XMLStreamException {
        return current.getTextCharacters(arg0, arg1, arg2, arg3);
    }

    public int getTextLength() {
        return current.getTextLength();
    }

    public int getTextStart() {
        return current.getTextStart();
    }

    public String getVersion() {
        return current.getVersion();
    }

    public boolean hasName() {
        return current.hasName();
    }

    public boolean hasNext() throws XMLStreamException {
        // This code assumes that the stack only contains readers that are not consumed
        if (!current.hasNext() &&
                !stack.isEmpty()) {
            return stack.peek().hasNext();
        }
        return current.hasNext();
    }

    public boolean hasText() {
        return current.hasText();
    }

    public boolean isAttributeSpecified(int arg0) {
        return current.isAttributeSpecified(arg0);
    }

    public boolean isCharacters() {
        return current.isCharacters();
    }

    public boolean isEndElement() {
        return current.isEndElement();
    }

    public boolean isStandalone() {
        return current.isStandalone();
    }

    public boolean isStartElement() {
        return current.isStartElement();
    }

    public boolean isWhiteSpace() {
        return current.isWhiteSpace();
    }

    public int next() throws XMLStreamException {
        // Only next is allowed to pop the stack
        if (!current.hasNext() &&
                !stack.isEmpty()) {
            current = stack.pop();
        }
        // The assumption is that the event on the stream reader was processed
        // prior to pushing a new xmlstreamreader.  thus we proceed to the next
        // event in all cases
        int tag = current.next();

        // Skip start document and end document events for
        // stacked stream readers
        if ((tag == this.START_DOCUMENT ||
                tag == this.END_DOCUMENT) &&
                !stack.isEmpty()) {
            tag = next();
        }

        return tag;
    }

    public int nextTag() throws XMLStreamException {
        if (!current.hasNext() &&
                !stack.isEmpty()) {
            return stack.peek().nextTag();
        }
        return current.nextTag();
    }

    public void require(int arg0, String arg1, String arg2) throws XMLStreamException {
        current.require(arg0, arg1, arg2);
    }

    public boolean standaloneSet() {
        return current.standaloneSet();
    }


}
