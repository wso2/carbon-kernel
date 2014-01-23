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

package org.apache.axiom.util.stax.xop;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.ext.stax.datahandler.DataHandlerProvider;
import org.apache.axiom.ext.stax.datahandler.DataHandlerReader;
import org.apache.axiom.util.base64.Base64Utils;
import org.apache.axiom.util.stax.XMLEventUtils;
import org.apache.axiom.util.stax.wrapper.XMLStreamReaderWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * {@link XMLStreamReader} wrapper that decodes XOP. It uses the extension defined by
 * {@link DataHandlerReader} to expose the {@link DataHandler} objects referenced by
 * <tt>xop:Include</tt> elements encountered in the underlying stream. If the consumer uses
 * {@link #getText()}, {@link #getTextCharacters()},
 * {@link #getTextCharacters(int, char[], int, int)} or {@link #getElementText()} when an
 * <tt>xop:Include</tt> element is present in the underlying stream, then the decoder will produce
 * a base64 representation of the data.
 * <p>
 * Note that this class only implements infoset transformation, but doesn't handle MIME processing.
 * A {@link MimePartProvider} implementation must be provided to the constructor of this class. This
 * object will be used to load MIME parts referenced by <tt>xop:Include</tt> elements encountered
 * in the underlying stream.
 * <p>
 * This class supports deferred loading of MIME parts: If the consumer uses
 * {@link DataHandlerReader#getDataHandlerProvider()}, then the {@link MimePartProvider} will only
 * be invoked when {@link DataHandlerProvider#getDataHandler()} is called.
 */
public class XOPDecodingStreamReader extends XMLStreamReaderWrapper implements DataHandlerReader {
    private static final String SOLE_CHILD_MSG =
            "Expected xop:Include as the sole child of an element information item (see section " +
            "3.2 of http://www.w3.org/TR/xop10/)";
    
    private static class DataHandlerProviderImpl implements DataHandlerProvider {
        private final MimePartProvider mimePartProvider;
        private final String contentID;
        
        public DataHandlerProviderImpl(MimePartProvider mimePartProvider, String contentID) {
            this.mimePartProvider = mimePartProvider;
            this.contentID = contentID;
        }

        public String getContentID() {
            return contentID;
        }

        public boolean isLoaded() {
            return mimePartProvider.isLoaded(contentID);
        }

        public DataHandler getDataHandler() throws IOException {
            return mimePartProvider.getDataHandler(contentID);
        }
    }
    
    private static final Log log = LogFactory.getLog(XOPDecodingStreamReader.class);
    
    private final MimePartProvider mimePartProvider;
    private DataHandlerProviderImpl dh;
    private String base64;

    /**
     * Constructor.
     * 
     * @param parent
     *            the XML stream to decode
     * @param mimePartProvider
     *            An implementation of the {@link MimePartProvider} interface that will be used to
     *            load the {@link DataHandler} objects for MIME parts referenced by
     *            <tt>xop:Include</tt> element information items encountered in the underlying
     *            stream.
     */
    public XOPDecodingStreamReader(XMLStreamReader parent, MimePartProvider mimePartProvider) {
        super(parent);
        this.mimePartProvider = mimePartProvider;
    }

    private void resetDataHandler() {
        dh = null;
        base64 = null;
    }
    
    /**
     * Process an <tt>xop:Include</tt> event and return the content ID.
     * <p>
     * Precondition: The parent reader is on the START_ELEMENT event for the <tt>xop:Include</tt>
     * element. Note that the method doesn't check this condition.
     * <p>
     * Postcondition: The parent reader is on the event following the END_ELEMENT event for the
     * <tt>xop:Include</tt> element, i.e. the parent reader is on the END_ELEMENT event of the
     * element enclosing the <tt>xop:Include</tt> element.
     * 
     * @return the content ID the <tt>xop:Include</tt> refers to
     * 
     * @throws XMLStreamException
     */
    private String processXopInclude() throws XMLStreamException {
        if (super.getAttributeCount() != 1 ||
                !super.getAttributeLocalName(0).equals(XOPConstants.HREF)) {
            throw new XMLStreamException("Expected xop:Include element information item with " +
                    "a (single) href attribute");
        }
        String href = super.getAttributeValue(0);
        if(log.isDebugEnabled()){
             log.debug("processXopInclude - found href : " + href);
        }
        if (!href.startsWith("cid:")) {
            throw new XMLStreamException("Expected href attribute containing a URL in the " +
                    "cid scheme");
        }
        String contentID;
        try {
            // URIs should always be decoded using UTF-8. On the other hand, since non ASCII
            // characters are not allowed in content IDs, we can simply decode using ASCII
            // (which is a subset of UTF-8)
            contentID = URLDecoder.decode(href.substring(4), "ascii");
            if(log.isDebugEnabled()){
                 log.debug("processXopInclude - decoded contentID : " + contentID);
            }
        } catch (UnsupportedEncodingException ex) {
            // We should never get here
            throw new XMLStreamException(ex);
        }
        if (super.next() != END_ELEMENT) {
            throw new XMLStreamException(
                    "Expected xop:Include element information item to be empty");
        }
        // Also consume the END_ELEMENT event of the xop:Include element. There are
        // two reasons for this:
        //  - It allows us to validate that the message conforms to the XOP specs.
        //  - It makes it easier to implement the getNamespaceContext method.
        if (super.next() != END_ELEMENT) {
            throw new XMLStreamException(SOLE_CHILD_MSG);
        }
        if (log.isDebugEnabled()) {
            log.debug("Encountered xop:Include for content ID '" + contentID + "'");
        }
        return contentID;
    }
    
    public int next() throws XMLStreamException {
        boolean wasStartElement;
        int event;
        if (dh != null) {
            resetDataHandler();
            // We already advanced to the next event after the xop:Include (see below), so there
            // is no call to parent.next() here
            event = END_ELEMENT;
            wasStartElement = false;
        } else {
            wasStartElement = super.getEventType() == START_ELEMENT;
            event = super.next();
        }
        if (event == START_ELEMENT
                && super.getLocalName().equals(XOPConstants.INCLUDE)
                && super.getNamespaceURI().equals(XOPConstants.NAMESPACE_URI)) {
            if (!wasStartElement) {
                throw new XMLStreamException(SOLE_CHILD_MSG);
            }
            dh = new DataHandlerProviderImpl(mimePartProvider, processXopInclude());
            return CHARACTERS;
        } else {
            return event;
        }
    }

    public int getEventType() {
        return dh == null ? super.getEventType() : CHARACTERS;
    }

    public int nextTag() throws XMLStreamException {
        if (dh != null) {
            resetDataHandler();
            // We already advanced to the next event after the xop:Include (see the implementation
            // of the next() method) and we now that it is an END_ELEMENT event.
            return END_ELEMENT;
        } else {
            return super.nextTag();
        }
    }

    public Object getProperty(String name) throws IllegalArgumentException {
        if (DataHandlerReader.PROPERTY.equals(name)) {
            return this;
        } else {
            return super.getProperty(name);
        }
    }

    public String getElementText() throws XMLStreamException {
        if (super.getEventType() != START_ELEMENT) {
            throw new XMLStreamException("The current event is not a START_ELEMENT event");
        }
        int event = super.next();
        // Note that an xop:Include must be the first child of the element
        if (event == START_ELEMENT
                && super.getLocalName().equals(XOPConstants.INCLUDE)
                && super.getNamespaceURI().equals(XOPConstants.NAMESPACE_URI)) {
            String contentID = processXopInclude();
            try {
                return toBase64(mimePartProvider.getDataHandler(contentID));
            } catch (IOException ex) {
                throw new XMLStreamException("Failed to load MIME part '" + contentID + "'", ex);
            }
        } else {
            String text = null;
            StringBuffer buffer = null;
            while (event != END_ELEMENT) {
                switch (event) {
                    case CHARACTERS:
                    case CDATA:
                    case SPACE:
                    case ENTITY_REFERENCE:
                        if (text == null && buffer == null) {
                            text = super.getText();
                        } else {
                            String thisText = super.getText();
                            if (buffer == null) {
                                buffer = new StringBuffer(text.length() + thisText.length());
                                buffer.append(text);
                            }
                            buffer.append(thisText);
                        }
                        break;
                    case PROCESSING_INSTRUCTION:
                    case COMMENT:
                        // Skip this event
                        break;
                    default:
                        throw new XMLStreamException("Unexpected event " +
                                XMLEventUtils.getEventTypeString(event) +
                                " while reading element text");
                }
                event = super.next();
            }
            if (buffer != null) {
                return buffer.toString();
            } else if (text != null) {
                return text;
            } else {
                return "";
            }
        }
    }

    public String getPrefix() {
        if (dh != null) {
            throw new IllegalStateException();
        } else {
            return super.getPrefix();
        }
    }

    public String getNamespaceURI() {
        if (dh != null) {
            throw new IllegalStateException();
        } else {
            return super.getNamespaceURI();
        }
    }

    public String getLocalName() {
        if (dh != null) {
            throw new IllegalStateException();
        } else {
            return super.getLocalName();
        }
    }

    public QName getName() {
        if (dh != null) {
            throw new IllegalStateException();
        } else {
            return super.getName();
        }
    }

    public Location getLocation() {
        return super.getLocation();
    }

    public String getNamespaceURI(String prefix) {
        String uri = super.getNamespaceURI(prefix);
        if ("xop".equals(prefix) && uri != null) {
            System.out.println(prefix + " -> " + uri);
        }
        return uri;
    }

    public int getNamespaceCount() {
        if (dh != null) {
            throw new IllegalStateException();
        } else {
            return super.getNamespaceCount();
        }
    }

    public String getNamespacePrefix(int index) {
        if (dh != null) {
            throw new IllegalStateException();
        } else {
            return super.getNamespacePrefix(index);
        }
    }

    public String getNamespaceURI(int index) {
        if (dh != null) {
            throw new IllegalStateException();
        } else {
            return super.getNamespaceURI(index);
        }
    }

    private static String toBase64(DataHandler dh) throws XMLStreamException {
        try {
            return Base64Utils.encode(dh);
        } catch (IOException ex) {
            throw new XMLStreamException("Exception when encoding data handler as base64", ex);
        }
    }
    
    private String toBase64() throws XMLStreamException {
        if (base64 == null) {
            try {
                base64 = toBase64(dh.getDataHandler());
            } catch (IOException ex) {
                throw new XMLStreamException("Failed to load MIME part '" + dh.getContentID() + "'", ex);
            }
        }
        return base64;
    }
    
    public String getText() {
        if (dh != null) {
            try {
                return toBase64();
            } catch (XMLStreamException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            return super.getText();
        }
    }

    public char[] getTextCharacters() {
        if (dh != null) {
            try {
                return toBase64().toCharArray();
            } catch (XMLStreamException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            return super.getTextCharacters();
        }
    }

    public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length)
            throws XMLStreamException {
        if (dh != null) {
            String text = toBase64();
            int copied = Math.min(length, text.length()-sourceStart);
            text.getChars(sourceStart, sourceStart + copied, target, targetStart);
            return copied;
        } else {
            return super.getTextCharacters(sourceStart, target, targetStart, length);
        }
    }

    public int getTextLength() {
        if (dh != null) {
            try {
                return toBase64().length();
            } catch (XMLStreamException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            return super.getTextLength();
        }
    }

    public int getTextStart() {
        if (dh != null) {
            return 0;
        } else {
            return super.getTextStart();
        }
    }

    public boolean hasText() {
        return dh != null || super.hasText();
    }

    public boolean isCharacters() {
        return dh != null || super.isCharacters();
    }

    public boolean isStartElement() {
        return dh == null && super.isStartElement();
    }

    public boolean isEndElement() {
        return dh == null && super.isEndElement();
    }

    public boolean hasName() {
        return dh == null && super.hasName();
    }

    public boolean isWhiteSpace() {
        return dh == null && super.isWhiteSpace();
    }

    public void require(int type, String namespaceURI, String localName)
            throws XMLStreamException {
        if (dh != null) {
            if (type != CHARACTERS) {
                throw new XMLStreamException("Expected CHARACTERS event");
            }
        } else {
            super.require(type, namespaceURI, localName);
        }
    }

    public boolean isBinary() {
        return dh != null;
    }

    public boolean isOptimized() {
        // xop:Include implies optimized
        return true;
    }

    public boolean isDeferred() {
        return true;
    }
    
    public String getContentID() {
        return dh.getContentID();
    }

    public DataHandler getDataHandler() throws XMLStreamException{
        try {
            return dh.getDataHandler();
        } catch (IOException ex) {
            throw new XMLStreamException("Failed to load MIME part '" + dh.getContentID() + "'");
        }
    }

    public DataHandlerProvider getDataHandlerProvider() {
        return dh;
    }

    XOPEncodedStream getXOPEncodedStream() {
        return new XOPEncodedStream(getParent(), mimePartProvider);
    }
}
