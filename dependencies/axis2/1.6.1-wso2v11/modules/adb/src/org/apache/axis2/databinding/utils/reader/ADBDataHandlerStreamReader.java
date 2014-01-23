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

package org.apache.axis2.databinding.utils.reader;

import org.apache.axis2.databinding.utils.ConverterUtil;

import javax.activation.DataHandler;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;

public class ADBDataHandlerStreamReader implements ADBXMLStreamReader {
    private static final int START_ELEMENT_STATE = 0;
    private static final int TEXT_STATE = 1;
    private static final int END_ELEMENT_STATE = 2;

    private ADBNamespaceContext namespaceContext =
            new ADBNamespaceContext();

    private QName name;
    private DataHandler value;

    private int state = START_ELEMENT_STATE;
    //initiate at the start element state

    //keeps track whether the namespace is declared
    //false by default
    private boolean nsDeclared = false;

    public ADBDataHandlerStreamReader(QName name, DataHandler value) {
        this.name = name;
        this.value = value;
    }

    private String convertedText = null;

    /**
     * Return the right properties for the optimization
     *
     * @param propKey
     * @throws IllegalArgumentException
     */
    public Object getProperty(String propKey) throws IllegalArgumentException {
        if (OPTIMIZATION_ENABLED.equals(propKey)) {
            return Boolean.TRUE;
        }
        if (state == TEXT_STATE) {
            if (IS_BINARY.equals(propKey)) {
                return Boolean.TRUE;
            } else if (DATA_HANDLER.equals(propKey)) {
                return value;
            }
        }
        return null;

    }

    public int next() throws XMLStreamException {
        //no need to handle null here. it should have been handled
        //already
        switch (state) {
            case START_ELEMENT_STATE:
                state = TEXT_STATE;
                return CHARACTERS;
            case END_ELEMENT_STATE:
                //oops, not supposed to happen!
                throw new XMLStreamException("end already reached!");
            case TEXT_STATE:
                state = END_ELEMENT_STATE;
                return END_ELEMENT;
            default:
                throw new XMLStreamException("unknown event type!");
        }
    }

    public void require(int i, String string, String string1) throws XMLStreamException {
        //not implemented
    }

    public String getElementText() throws XMLStreamException {
        if (state == START_ELEMENT) {
            //move to the end state and return the value
            state = END_ELEMENT_STATE;
            if (convertedText == null) {
                convertedText = ConverterUtil.getStringFromDatahandler(value);
            }
            return convertedText;
        } else {
            throw new XMLStreamException();
        }

    }

    public int nextTag() throws XMLStreamException {
        return 0;//todo
    }

    public boolean hasNext() throws XMLStreamException {
        return (state != END_ELEMENT_STATE);
    }

    public void close() throws XMLStreamException {
        //Do nothing - we've nothing to free here
    }

    public String getNamespaceURI(String prefix) {
        return namespaceContext.getNamespaceURI(prefix);
    }

    public boolean isStartElement() {
        return (state == START_ELEMENT_STATE);
    }

    public boolean isEndElement() {
        return (state == END_ELEMENT_STATE);
    }

    public boolean isCharacters() {
        return (state == TEXT_STATE);
    }

    public boolean isWhiteSpace() {
        return false;  //no whitespaces here
    }

    public String getAttributeValue(String string, String string1) {
        return null;
    }

    public int getAttributeCount() {
        return 0;
    }

    public QName getAttributeName(int i) {
        return null;
    }

    public String getAttributeNamespace(int i) {
        return null;
    }

    public String getAttributeLocalName(int i) {
        return null;
    }

    public String getAttributePrefix(int i) {
        return null;
    }

    public String getAttributeType(int i) {
        return null;
    }

    public String getAttributeValue(int i) {
        return null;
    }

    public boolean isAttributeSpecified(int i) {
        return false; //no attribs here
    }

    public int getNamespaceCount() {
        return (nsDeclared) ? 1 : 0;
    }

    public String getNamespacePrefix(int i) {
        return (nsDeclared && i == 0) ? name.getPrefix() : null;
    }

    public String getNamespaceURI(int i) {
        return (nsDeclared && i == 0) ? name.getNamespaceURI() : null;
    }

    public NamespaceContext getNamespaceContext() {
        return this.namespaceContext;
    }

    public int getEventType() {
        switch (state) {
            case START_ELEMENT_STATE:
                return START_ELEMENT;
            case END_ELEMENT_STATE:
                return END_ELEMENT;
            case TEXT_STATE:
                return CHARACTERS;
            default:
                throw new UnsupportedOperationException();
                //we've no idea what this is!!!!!
        }

    }

    public String getText() {
        if (state == TEXT_STATE) {
            if (convertedText == null) {
                convertedText =
                        ConverterUtil.getStringFromDatahandler(value);
            }
            return convertedText;
        } else {
            throw new IllegalStateException();
        }
    }

    public char[] getTextCharacters() {
        if (state == TEXT_STATE) {
            if (convertedText == null) {
                convertedText =
                        ConverterUtil.getStringFromDatahandler(value);
            }
            return convertedText.toCharArray();
        } else {
            throw new IllegalStateException();
        }
    }

    public int getTextCharacters(int i, char[] chars, int i1, int i2) throws XMLStreamException {
        //not implemented
        throw new UnsupportedOperationException();
    }

    public int getTextStart() {
        if (state == TEXT_STATE) {
            return 0;
        } else {
            throw new IllegalStateException();
        }
    }

    public int getTextLength() {
        if (state == TEXT_STATE) {
            if (convertedText == null) {
                convertedText =
                        ConverterUtil.getStringFromDatahandler(value);
            }
            return convertedText.length();
        } else {
            throw new IllegalStateException();
        }

    }

    public String getEncoding() {
        return null;
    }

    public boolean hasText() {
        return (state == TEXT_STATE);
    }

    public Location getLocation() {
        return new Location() {
            public int getLineNumber() {
                return 0;
            }

            public int getColumnNumber() {
                return 0;
            }

            public int getCharacterOffset() {
                return 0;
            }

            public String getPublicId() {
                return null;
            }

            public String getSystemId() {
                return null;
            }
        };
    }

    public QName getName() {
        if (state != TEXT_STATE) {
            return name;
        } else {
            return null;
        }
    }

    public String getLocalName() {
        if (state != TEXT_STATE) {
            return name.getLocalPart();
        } else {
            return null;
        }
    }

    public boolean hasName() {
        return (state != TEXT_STATE);

    }

    public String getNamespaceURI() {
        if (state != TEXT_STATE) {
            return name.getNamespaceURI();
        } else {
            return null;
        }

    }

    public String getPrefix() {
        if (state != TEXT_STATE) {
            return name.getPrefix();
        } else {
            return null;
        }
    }

    public String getVersion() {
        return null;  //todo 1.0 ?
    }

    public boolean isStandalone() {
        return false;
    }

    public boolean standaloneSet() {
        return false;
    }

    public String getCharacterEncodingScheme() {
        return null;
    }

    public String getPITarget() {
        return null;
    }

    public String getPIData() {
        return null;
    }

    public boolean isDone() {
        return (state == END_ELEMENT_STATE);
    }

    public void addNamespaceContext(NamespaceContext nsContext) {
        this.namespaceContext.setParentNsContext(nsContext);
    }

    public void init() {
        //just add the current elements namespace and prefix to the this
        //elements nscontext
        addToNsMap(name.getPrefix(), name.getNamespaceURI());


    }

    /**
     * @param prefix
     * @param uri
     */
    private void addToNsMap(String prefix, String uri) {
        //todo - need to fix this up to cater for cases where
        //namespaces are having  no prefixes
        if (!uri.equals(namespaceContext.getNamespaceURI(prefix))) {
            //this namespace is not there. Need to declare it
            namespaceContext.pushNamespace(prefix, uri);
            nsDeclared = true;
        }
    }


}
