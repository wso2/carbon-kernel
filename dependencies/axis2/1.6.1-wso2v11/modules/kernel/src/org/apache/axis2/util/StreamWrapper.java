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


package org.apache.axis2.util;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class StreamWrapper implements XMLStreamReader {
    private static final int STATE_SWITCHED = 0;
    private static final int STATE_INIT = 1;
    private static final int STATE_SWITCH_AT_NEXT = 2;
    private static final int STATE_COMPLETE_AT_NEXT = 3;
    private static final int STATE_COMPLETED = 4;
    private XMLStreamReader realReader = null;
    private int state = STATE_INIT;
    private int prevState = state;


    public StreamWrapper(XMLStreamReader realReader) {
        if (realReader == null) {
            throw new UnsupportedOperationException("Reader cannot be null");
        }

        this.realReader = realReader;
    }

    public void close() throws XMLStreamException {
        if (state != STATE_INIT) {
            realReader.close();
        } else {
            throw new XMLStreamException();
        }
    }

    public int next() throws XMLStreamException {
        prevState = state;
        int returnEvent = -1;

        switch (state) {
            case STATE_INIT:
                if (realReader.getEventType() == START_DOCUMENT) {
                    state = STATE_SWITCHED;
                    returnEvent = realReader.next();
                } else {
                    state = STATE_SWITCHED;
                    returnEvent = realReader.getEventType();
                }
                break;
            case STATE_SWITCHED:
                returnEvent = realReader.next();
                if (returnEvent == END_DOCUMENT) {
                    state = STATE_COMPLETED;
                } else if (!realReader.hasNext()) {
                    state = STATE_COMPLETE_AT_NEXT;
                }
                break;
//            case STATE_SWITCH_AT_NEXT:
//                state = STATE_SWITCHED;
//                returnEvent = realReader.getEventType();
//                break;
            case STATE_COMPLETE_AT_NEXT:
                state = STATE_COMPLETED;
                returnEvent = END_DOCUMENT;
                break;
            case STATE_COMPLETED:
                //oops - no way we can go beyond this
                throw new XMLStreamException("end reached!");
            default:
                throw new UnsupportedOperationException();
        }

        return returnEvent;
    }

    public int nextTag() throws XMLStreamException {
        if (prevState != STATE_INIT) {
            return realReader.nextTag();
        } else {
            throw new XMLStreamException();
        }
    }

    public void require(int i, String s, String s1) throws XMLStreamException {
        if (state != STATE_INIT) {
            realReader.require(i, s, s1);
        }
    }

    public boolean standaloneSet() {
        if (state != STATE_INIT) {
            return realReader.standaloneSet();
        } else {
            return false;
        }
    }

    public int getAttributeCount() {
        if (state != STATE_INIT) {
            return realReader.getAttributeCount();
        } else {
            return 0;
        }
    }

    public String getAttributeLocalName(int i) {
        if (state != STATE_INIT) {
            return realReader.getAttributeLocalName(i);
        } else {
            return null;
        }
    }

    public QName getAttributeName(int i) {
        if (state != STATE_INIT) {
            return realReader.getAttributeName(i);
        } else {
            return null;
        }
    }

    public String getAttributeNamespace(int i) {
        if (state != STATE_INIT) {
            return realReader.getAttributeNamespace(i);
        } else {
            return null;
        }
    }

    public String getAttributePrefix(int i) {
        if (state != STATE_INIT) {
            return realReader.getAttributePrefix(i);
        } else {
            return null;
        }
    }

    public String getAttributeType(int i) {
        if (state != STATE_INIT) {
            return realReader.getAttributeType(i);
        } else {
            return null;
        }
    }

    public String getAttributeValue(int i) {
        if (state != STATE_INIT) {
            return realReader.getAttributeValue(i);
        } else {
            return null;
        }
    }

    public String getAttributeValue(String s, String s1) {
        if (state != STATE_INIT) {
            return realReader.getAttributeValue(s, s1);
        } else {
            return null;
        }
    }

    public String getCharacterEncodingScheme() {
        if (state != STATE_INIT) {
            return realReader.getCharacterEncodingScheme();
        } else {
            return null;
        }
    }

    public String getElementText() throws XMLStreamException {
        if (state != STATE_INIT) {
            return realReader.getElementText();
        } else {
            throw new XMLStreamException();
        }
    }

    public String getEncoding() {
        if (state != STATE_INIT) {
            return realReader.getEncoding();
        } else {
            return null;
        }
    }

    public int getEventType() {
        if (state == STATE_INIT) {
            return START_DOCUMENT;
        } else {
            return realReader.getEventType();
        }
    }

    public String getLocalName() {
        if (state != STATE_INIT) {
            return realReader.getLocalName();
        } else {
            return null;
        }
    }

    public Location getLocation() {
        if (state != STATE_INIT) {
            return realReader.getLocation();
        } else {
            return null;
        }
    }

    public QName getName() {
        if (state != STATE_INIT) {
            return realReader.getName();
        } else {
            return null;
        }
    }

    public NamespaceContext getNamespaceContext() {
        if (state != STATE_INIT) {
            return realReader.getNamespaceContext();
        } else {
            return null;
        }
    }

    public int getNamespaceCount() {
        if (state != STATE_INIT) {
            return realReader.getNamespaceCount();
        } else {
            return 0;
        }
    }

    public String getNamespacePrefix(int i) {
        if (state != STATE_INIT) {
            return realReader.getNamespacePrefix(i);
        } else {
            return null;
        }
    }

    public String getNamespaceURI() {
        if (state != STATE_INIT) {
            return realReader.getNamespaceURI();
        } else {
            return null;
        }
    }

    public String getNamespaceURI(int i) {
        if (state != STATE_INIT) {
            return realReader.getNamespaceURI(i);
        } else {
            return null;
        }
    }

    public String getNamespaceURI(String s) {
        if (state != STATE_INIT) {
            return realReader.getNamespaceURI(s);
        } else {
            return null;
        }
    }

    public String getPIData() {
        if (state != STATE_INIT) {
            return realReader.getPIData();
        } else {
            return null;
        }
    }

    public String getPITarget() {
        if (state != STATE_INIT) {
            return realReader.getPITarget();
        } else {
            return null;
        }
    }

    public String getPrefix() {
        if (state != STATE_INIT) {
            return realReader.getPrefix();
        } else {
            return null;
        }
    }

    public Object getProperty(String s) throws IllegalArgumentException {
        return realReader.getProperty(s);
    }

    public String getText() {
        if (state != STATE_INIT) {
            return realReader.getText();
        } else {
            return null;
        }
    }

    public char[] getTextCharacters() {
        if (state != STATE_INIT) {
            return realReader.getTextCharacters();
        } else {
            return new char[0];
        }
    }

    public int getTextCharacters(int i, char[] chars, int i1, int i2) throws XMLStreamException {
        if (state != STATE_INIT) {
            return realReader.getTextCharacters(i, chars, i1, i2);
        } else {
            return 0;
        }
    }

    public int getTextLength() {
        if (state != STATE_INIT) {
            return realReader.getTextLength();
        } else {
            return 0;
        }
    }

    public int getTextStart() {
        if (state != STATE_INIT) {
            return realReader.getTextStart();
        } else {
            return 0;
        }
    }

    public String getVersion() {
        if (state != STATE_INIT) {
            return realReader.getVersion();
        } else {
            return null;
        }
    }

    public boolean hasName() {
        if (state != STATE_INIT) {
            return realReader.hasName();
        } else {
            return false;
        }
    }

    public boolean hasNext() throws XMLStreamException {
        if (state == STATE_COMPLETE_AT_NEXT) {
            return true;
        } else if (state == STATE_COMPLETED) {
            return false;
        } else if (state != STATE_INIT) {
            return realReader.hasNext();
        } else {
            return true;
        }
    }

    public boolean hasText() {
        if (state != STATE_INIT) {
            return realReader.hasText();
        } else {
            return false;
        }
    }

    public boolean isAttributeSpecified(int i) {
        if (state != STATE_INIT) {
            return realReader.isAttributeSpecified(i);
        } else {
            return false;
        }
    }

    public boolean isCharacters() {
        if (state != STATE_INIT) {
            return realReader.isCharacters();
        } else {
            return false;
        }
    }

    public boolean isEndElement() {
        if (state != STATE_INIT) {
            return realReader.isEndElement();
        } else {
            return false;
        }
    }

    public boolean isStandalone() {
        if (state != STATE_INIT) {
            return realReader.isStandalone();
        } else {
            return false;
        }
    }

    public boolean isStartElement() {
        if (state != STATE_INIT) {
            return realReader.isStartElement();
        } else {
            return false;
        }
    }

    public boolean isWhiteSpace() {
        if (state != STATE_INIT) {
            return realReader.isWhiteSpace();
        } else {
            return false;
        }
    }
}
