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

import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMXMLStreamReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.DataHandler;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import java.util.Stack;

// TODO: this needs reformatting; the (generated) Javadoc is unreadable!
/**
 * There are several places in the code where events are passed from 
 * a source to a consumer using XMLStreamReader events. 
 * 
 *     OMXMLStreamReader (impl)--> consumer of XMLStreamReader events
 * 
 * This simple class can be interjected as a filter and used to do some simple validation.
 * Validating the events coming from source (impl) can help find and correct errors 
 * when they occur.  Otherwise the errors may be caught much further downstream and hard to fix.
 * 
 *    OMXMLStreamReader (impl)--> OMXMLStreamReaderValiator-> consumer of XMLStreamReader events
 * 
 * 
 * In the initial version, the XMStreamValidator ensures that the start element events match the 
 * end element events.
 *
 * @see org.apache.axiom.om.OMElement#getXMLStreamReader(boolean)
 */
public class OMXMLStreamReaderValidator implements OMXMLStreamReader {

    private static Log log = LogFactory.getLog(OMXMLStreamReaderValidator.class);
    private static boolean IS_DEBUG_ENABLED = log.isDebugEnabled();
    private static boolean IS_ADV_DEBUG_ENABLED = false;  // Turn this on to trace every event
    
    
    private final OMXMLStreamReader delegate;  // Actual XMLStreamReader implementation 
    private boolean throwExceptions = false;   // Indicates whether OMException should be thrown if errors are disovered
    private Stack stack = new Stack();         // Stack keeps track of the nested element QName
    

    /**
     * @param delegate XMLStreamReader to validate
     * @param throwExceptions (true if exceptions should be thrown when errors are encountered)
     */
    public OMXMLStreamReaderValidator(OMXMLStreamReader delegate, boolean throwExceptions) {
        super();
        this.delegate = delegate;
        this.throwExceptions = throwExceptions;
    }

 
    public int next() throws XMLStreamException {
        int event = delegate.next();
        logParserState();
        
        // Make sure that the start element and end element events match.
        // Mismatched events are a key indication that the delegate stream reader is 
        // broken or corrupted.
        switch (event) {
        case XMLStreamConstants.START_ELEMENT:
            stack.push(delegate.getName());
            break;
        case XMLStreamConstants.END_ELEMENT:
            QName delegateQName = delegate.getName();
            if (stack.isEmpty()) {
                reportMismatchedEndElement(null, delegateQName);
            } else {
                QName expectedQName = (QName) stack.pop();
                
                if (!expectedQName.equals(delegateQName)) {
                    reportMismatchedEndElement(expectedQName, delegateQName);
                }
            }
            break;
            
        default :
        
        }
        
        return event;
    }
    
    
    /**
     * Report a mismatched end element.
     * @param expectedQName
     * @param delegateQName
     */
    private void reportMismatchedEndElement(QName expectedQName, QName delegateQName) {
        String text = null;
        if (expectedQName == null) {
            text = "An END_ELEMENT event for " + delegateQName + 
                " was encountered, but the START_ELEMENT stack is empty.";
        } else {
            text = "An END_ELEMENT event for " + delegateQName + 
                " was encountered, but this doesn't match the corresponding START_ELEMENT " + 
                expectedQName + " event.";
        }
        if (IS_DEBUG_ENABLED) {
            log.debug(text);
        }       
        if (throwExceptions) {
            throw new OMException(text);
        }
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

    public int getTextCharacters(int arg0, char[] arg1, int arg2, int arg3) throws XMLStreamException {
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

   

    public int nextTag() throws XMLStreamException {
        return delegate.nextTag();
    }

    public void require(int arg0, String arg1, String arg2) throws XMLStreamException {
        delegate.require(arg0, arg1, arg2);
    }

    public boolean standaloneSet() {
        return delegate.standaloneSet();
    }
    
    
    /**
     * Dump the current event of the delegate.
     */
    protected void logParserState() {
        if (IS_ADV_DEBUG_ENABLED) {
            int currentEvent = delegate.getEventType();
            
            switch (currentEvent) {
            case XMLStreamConstants.START_ELEMENT:
                log.trace("START_ELEMENT: ");
                log.trace("  QName: " + delegate.getName());
                break;
            case XMLStreamConstants.START_DOCUMENT:
                log.trace("START_DOCUMENT: ");
                break;
            case XMLStreamConstants.CHARACTERS:
                log.trace("CHARACTERS: ");
                log.trace(   "[" + delegate.getText() + "]");
                break;
            case XMLStreamConstants.CDATA:
                log.trace("CDATA: ");
                log.trace(   "[" + delegate.getText() + "]");
                break;
            case XMLStreamConstants.END_ELEMENT:
                log.trace("END_ELEMENT: ");
                log.trace("  QName: " + delegate.getName());
                break;
            case XMLStreamConstants.END_DOCUMENT:
                log.trace("END_DOCUMENT: ");
                break;
            case XMLStreamConstants.SPACE:
                log.trace("SPACE: ");
                log.trace(   "[" + delegate.getText() + "]");
                break;
            case XMLStreamConstants.COMMENT:
                log.trace("COMMENT: ");
                log.trace(   "[" + delegate.getText() + "]");
                break;
            case XMLStreamConstants.DTD:
                log.trace("DTD: ");
                log.trace(   "[" + delegate.getText() + "]");
                break;
            case XMLStreamConstants.PROCESSING_INSTRUCTION:
                log.trace("PROCESSING_INSTRUCTION: ");
                log.trace("   [" + delegate.getPITarget() + "][" +
                            delegate.getPIData() + "]");
                break;
            case XMLStreamConstants.ENTITY_REFERENCE:
                log.trace("ENTITY_REFERENCE: ");
                log.trace("    " + delegate.getLocalName() + "[" +
                            delegate.getText() + "]");
                break;
            default :
                log.trace("UNKNOWN_STATE: " + currentEvent);
            
            }
        }
    }

    public DataHandler getDataHandler(String blobcid) {
        return delegate.getDataHandler(blobcid);
    }


    public boolean isInlineMTOM() {
        return delegate.isInlineMTOM();
    }


    public void setInlineMTOM(boolean value) {
        delegate.setInlineMTOM(value);
    }
    

}
