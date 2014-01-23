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

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;

public class NameValueArrayStreamReader implements ADBXMLStreamReader {

    private static final int START_ELEMENT_STATE = 0;
    private static final int TEXT_STATE = 1;
    private static final int END_ELEMENT_STATE = 2;
    private static final int FINAL_END_ELEMENT_STATE = 3;
    private static final int START_ELEMENT_STATE_WITH_NULL = 4;

    private static final QName NIL_QNAME =
            new QName("http://www.w3.org/2001/XMLSchema-instance", "nil", "xsi");
    private static final String NIL_VALUE_TRUE = "true";


    private ADBNamespaceContext namespaceContext =
            new ADBNamespaceContext();
    //the index of the array
    private int arrayIndex = 0;

    private QName name;
    private String[] values;

    //start element is the default state
    private int state = START_ELEMENT_STATE;


    public NameValueArrayStreamReader(QName name, String[] values) {
        this.name = name;
        this.values = values;
    }

    public void addNamespaceContext(NamespaceContext nsContext) {
        this.namespaceContext.setParentNsContext(nsContext);
    }

    public void init() {
        //todo what if the Qname namespace has not been declared
    }

    public Object getProperty(String string) throws IllegalArgumentException {
        return null;
    }

    /** @throws XMLStreamException  */
    public int next() throws XMLStreamException {
        switch (state) {
            case START_ELEMENT_STATE:
                if (values.length > 0) {
                    state = TEXT_STATE;
                    return CHARACTERS;
                } else {
                    state = FINAL_END_ELEMENT_STATE;
                    return END_ELEMENT;
                }

            case START_ELEMENT_STATE_WITH_NULL:
                if (arrayIndex == (values.length - 1)) {
                    state = FINAL_END_ELEMENT_STATE;
                } else {
                    state = END_ELEMENT_STATE;
                }
                return END_ELEMENT;
            case FINAL_END_ELEMENT_STATE:
                //oops, not supposed to happen!
                throw new XMLStreamException("end already reached!");
            case END_ELEMENT_STATE:
                //we've to have more values since this is not the
                //last value
                //increment the counter
                arrayIndex++;
                if (values[arrayIndex] == null) {
                    state = START_ELEMENT_STATE_WITH_NULL;
                } else {
                    state = START_ELEMENT_STATE;
                }
                return START_ELEMENT;
            case TEXT_STATE:
                if (arrayIndex == (values.length - 1)) {
                    state = FINAL_END_ELEMENT_STATE;
                    return END_ELEMENT;
                } else {
                    state = END_ELEMENT_STATE;
                    return END_ELEMENT;
                }

            default:
                throw new XMLStreamException("unknown event type!");
        }
    }

    public void require(int i, String string, String string1) throws XMLStreamException {
        //nothing done here
    }

    public String getElementText() throws XMLStreamException {
        return null; //not implemented
    }

    public int nextTag() throws XMLStreamException {
        return 0; //not implemented
    }


    public String getAttributeValue(String string, String string1) {
        if (state == TEXT_STATE) {
            //todo something
            return null;
        } else {
            return null;
        }

    }

    public int getAttributeCount() {
        if (state == START_ELEMENT_STATE_WITH_NULL) return 1;
        if (state == START_ELEMENT_STATE) {
            return 0;
        } else {
            throw new IllegalStateException();
        }

    }

    public QName getAttributeName(int i) {
        if (state == START_ELEMENT_STATE_WITH_NULL && i == 0)
            return NIL_QNAME;
        if (state == START_ELEMENT_STATE) {
            return null;
        } else {
            throw new IllegalStateException();
        }
    }

    public String getAttributeNamespace(int i) {
        if (state == START_ELEMENT_STATE_WITH_NULL && i == 0)
            return NIL_QNAME.getNamespaceURI();
        if (state == START_ELEMENT_STATE) {
            return null;
        } else {
            throw new IllegalStateException();
        }
    }

    public String getAttributeLocalName(int i) {
        if (state == START_ELEMENT_STATE_WITH_NULL && i == 0)
            return NIL_QNAME.getLocalPart();
        if (state == START_ELEMENT_STATE) {
            return null;
        } else {
            throw new IllegalStateException();
        }
    }

    public String getAttributePrefix(int i) {
        if (state == START_ELEMENT_STATE_WITH_NULL && i == 0)
            return NIL_QNAME.getPrefix();
        if (state == START_ELEMENT_STATE) {
            return null;
        } else {
            throw new IllegalStateException();
        }
    }

    public String getAttributeType(int i) {
        return null;  //not implemented
    }

    public String getAttributeValue(int i) {
        if (state == START_ELEMENT_STATE_WITH_NULL && i == 0)
            return NIL_VALUE_TRUE;
        if (state == START_ELEMENT_STATE) {
            return null;
        } else {
            throw new IllegalStateException();
        }
    }

    public boolean isAttributeSpecified(int i) {
        return false; //not supported
    }

    public int getNamespaceCount() {
        if (state == START_ELEMENT_STATE_WITH_NULL && isXsiNamespacePresent())
            return 1;
        else
            return 0;

    }

    public String getNamespacePrefix(int i) {
        if (state == START_ELEMENT_STATE_WITH_NULL
                && isXsiNamespacePresent() && i == 0)
            return NIL_QNAME.getPrefix();
        else
            return null;
    }

    public String getNamespaceURI(int i) {
        if (state == START_ELEMENT_STATE_WITH_NULL
                && isXsiNamespacePresent() && i == 0)
            return NIL_QNAME.getNamespaceURI();
        else
            return null;
    }

    public NamespaceContext getNamespaceContext() {
        return this.namespaceContext;
    }

    public boolean isDone() {
        return (state == FINAL_END_ELEMENT_STATE);
    }

    public int getEventType() {
        switch (state) {
            case START_ELEMENT_STATE:
                return START_ELEMENT;
            case END_ELEMENT_STATE:
                return END_ELEMENT;
            case TEXT_STATE:
                return CHARACTERS;
            case FINAL_END_ELEMENT_STATE:
                return END_ELEMENT;
            default:
                throw new UnsupportedOperationException();
                //we've no idea what this is!!!!!
        }

    }

    public String getText() {
        if (state == TEXT_STATE) {
            return values[arrayIndex];
        } else {
            throw new IllegalStateException();
        }
    }

    public char[] getTextCharacters() {
        if (state == TEXT_STATE) {
            return values[arrayIndex].toCharArray();
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
            return values[arrayIndex].length();
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
        return null;  //not supported
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

    public boolean hasNext() throws XMLStreamException {
        return (state != FINAL_END_ELEMENT_STATE);
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
        }
    }

    /** Test whether the xsi namespace is present */
    private boolean isXsiNamespacePresent() {
        return (namespaceContext.getNamespaceURI(NIL_QNAME.getPrefix()) != null);
    }
}
