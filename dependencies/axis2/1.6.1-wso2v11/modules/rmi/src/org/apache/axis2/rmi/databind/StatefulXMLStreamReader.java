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

package org.apache.axis2.rmi.databind;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.Stack;

/**
 * StatefulXMLStreamReader class that primarily delegates to the initial reader. However, in 
 * addition to the normal functions performed by the initial reader, a stack is also maintained
 * to determine the current state.
 */
public class StatefulXMLStreamReader implements XMLStreamReader {

    protected XMLStreamReader delegate;
    private Stack tagStack;
    private boolean priorAccess;

    /**
     * Construct a StatefulXMLStreamReader.  Do not assume that the reader is being
     * converted prior to accessed.
     *
     * @param reader XMLStreamReader for which a state is also needed.
     */
    public StatefulXMLStreamReader(XMLStreamReader reader) {
        delegate = reader;
        tagStack = new Stack();
        priorAccess = true;
    }
    
    /**
     * Construct a StatefulXMLStreamReader.
     *
     * @param reader XMLStreamReader for which a state is also needed.
     * @param at0 indicates whether reader has been advanced, which in turn 
     *        indicates whether the XML parsing is at an intermediate state
     *        prior to being converted to a StatefulXMLStreamReader
     */
    public StatefulXMLStreamReader(XMLStreamReader reader, boolean at0) {
        delegate = reader;
        tagStack = new Stack();
        priorAccess = !at0;
    }    

    public void close() throws XMLStreamException {
        delegate.close();
    }

    public int getAttributeCount() {
        return delegate.getAttributeCount();
    }

    public String getAttributeLocalName(int arg0) {
        return delegate.getAttributeLocalName(arg0);
    }

    public QName getAttributeName(int arg0) {
        return delegate.getAttributeName(arg0);
    }

    public String getAttributeNamespace(int arg0) {
        return delegate.getAttributeNamespace(arg0);
    }

    public String getAttributePrefix(int arg0) {
        return delegate.getAttributePrefix(arg0);
    }

    public String getAttributeType(int arg0) {
        return delegate.getAttributeType(arg0);
    }

    public String getAttributeValue(int arg0) {
        return delegate.getAttributeValue(arg0);
    }

    public String getAttributeValue(String arg0, String arg1) {
        return delegate.getAttributeValue(arg0, arg1);
    }

    public String getCharacterEncodingScheme() {
        return delegate.getCharacterEncodingScheme();
    }

    public String getElementText() throws XMLStreamException {
        return delegate.getElementText();
    }

    public String getEncoding() {
        return delegate.getEncoding();
    }

    public int getEventType() {
        return delegate.getEventType();
    }

    public String getLocalName() {
        return delegate.getLocalName();
    }

    public Location getLocation() {
        return delegate.getLocation();
    }

    public QName getName() {
        return delegate.getName();
    }

    public NamespaceContext getNamespaceContext() {
        return delegate.getNamespaceContext();
    }

    public int getNamespaceCount() {
        return delegate.getNamespaceCount();
    }

    public String getNamespacePrefix(int arg0) {
        return delegate.getNamespacePrefix(arg0);
    }

    public String getNamespaceURI() {
        return delegate.getNamespaceURI();
    }

    public String getNamespaceURI(int arg0) {
        return delegate.getNamespaceURI(arg0);
    }

    public String getNamespaceURI(String arg0) {
        return delegate.getNamespaceURI(arg0);
    }

    public String getPIData() {
        return delegate.getPIData();
    }

    public String getPITarget() {
        return delegate.getPITarget();
    }

    public String getPrefix() {
        return delegate.getPrefix();
    }

    public Object getProperty(String arg0) throws IllegalArgumentException {
        return delegate.getProperty(arg0);
    }

    public String getText() {
        return delegate.getText();
    }

    public char[] getTextCharacters() {
        return delegate.getTextCharacters();
    }

    public int getTextCharacters(int arg0, char[] arg1, int arg2, int arg3)
            throws XMLStreamException {
        return delegate.getTextCharacters(arg0, arg1, arg2, arg3);
    }

    public int getTextLength() {
        return delegate.getTextLength();
    }

    public int getTextStart() {
        return delegate.getTextStart();
    }

    public String getVersion() {
        return delegate.getVersion();
    }

    public boolean hasName() {
        return delegate.hasName();
    }

    public boolean hasNext() throws XMLStreamException {
        return delegate.hasNext();
    }

    public boolean hasText() {
        return delegate.hasText();
    }

    public boolean isAttributeSpecified(int arg0) {
        return delegate.isAttributeSpecified(arg0);
    }

    public boolean isCharacters() {
        return delegate.isCharacters();
    }

    public boolean isEndElement() {
        return delegate.isEndElement();
    }

    public boolean isStandalone() {
        return delegate.isStandalone();
    }

    public boolean isStartElement() {
        return delegate.isStartElement();
    }

    public boolean isWhiteSpace() {
        return delegate.isWhiteSpace();
    }

    public int next() throws XMLStreamException {
        int returnValue = delegate.next();
        
        if (delegate.isEndElement()) {
            QName top = (QName) tagStack.peek();
            if (!(delegate.getName().equals(top)) && !priorAccess) {
                throw new IllegalStateException("The end element (" + delegate.getName() + ") does not "
                        + "correspond to the current start element (" + top + ").");
            }
            if (tagStack.size() > 0) {
                tagStack.pop();        
            } else if (!priorAccess) {
                throw new IllegalStateException("An end element was encountered for which "
                        + "there was not a corresponding start element.");
            }
        } else if (delegate.isStartElement()) {
            tagStack.push(delegate.getName());
        }

        return returnValue;
    }

    public int nextTag() throws XMLStreamException {
        return delegate.nextTag();
    }

    public void require(int arg0, String arg1, String arg2) throws XMLStreamException {
        delegate.require(arg0, arg1, arg2);
    }

    public boolean standaloneSet() {
        return delegate.standaloneSet();
    }
    
    public int getDepth() {
        return tagStack.size();
    }

}
