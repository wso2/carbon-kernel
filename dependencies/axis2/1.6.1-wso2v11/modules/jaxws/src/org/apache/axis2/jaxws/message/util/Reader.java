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

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.WebServiceException;
import java.util.Arrays;

/**
 * Reader In many situations, you want the ability to reset an XMLStreamReader. (Or at least ask if
 * the XMLStreamReader is resettable).
 * <p/>
 * The Reader abstract class: - accepts an XMLStreamReader - provides reset() and isResettable()
 * methods Adds support resettable support to XMLStreamReader
 * <p/>
 * Derived classes must pass the initial reader to the constructor and indicate if it is resettable.
 * Derived classes must also provide an implementation of the newReader() method if resettable.
 */
public abstract class Reader implements XMLStreamReader {
    protected XMLStreamReader reader;
    private final boolean resettable;
    private static final Log log = LogFactory.getLog(Reader.class);

    /**
     * @param reader
     * @param resettable
     */
    Reader(XMLStreamReader reader, boolean resettable) {
        this.reader = reader;
        this.resettable = resettable;
    }

    /**
     * Get a newReader from the Object
     *
     * @return XMLStreamReader
     */
    protected abstract XMLStreamReader newReader();

    /**
     * isResettable
     *
     * @return true or false
     */
    public boolean isResettable() {
    	debug("Entering isResettable....");
    	debug("resettable = ", resettable);
        return resettable;
    }

    public void reset() throws WebServiceException {
    	debug("Entering reset....");
        if (!resettable) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("resetReaderErr"));
        }
        reader = newReader();
        
        debug("Exiting reset....");
    }

    public void close() throws XMLStreamException {
    	debug("Entering close....");
        reader.close();
        debug("Exiting close....");
    }

    public int getAttributeCount() {
    	debug("Entering getAttributeCount....");
    	int ac = reader.getAttributeCount();
    	debug("reader.getAttributeCount() = ", ac);
        return ac;
    }

    public String getAttributeLocalName(int arg0) {
    	debug("Entering getAttributeLocalName....");
    	String aln = reader.getAttributeLocalName(arg0);
    	debug("reader.getAttributeLocalName(arg0) = ", aln);
        return aln;
    }

    public QName getAttributeName(int arg0) {
    	debug("Entering getAttributeName....");
    	QName q = reader.getAttributeName(arg0);
    	debug("reader.getAttributeName(arg0) = ", q);
        return q;
    }

    public String getAttributeNamespace(int arg0) {
    	debug("Entering getAttributeNamespace....");
    	String an = reader.getAttributeNamespace(arg0);
    	debug("reader.getAttributeNamespace(arg0) = ", an);
        return an;
    }

    public String getAttributePrefix(int arg0) {
    	debug("Entering getAttributePrefix....");
    	String ap = reader.getAttributePrefix(arg0);
    	debug("reader.getAttributePrefix(arg0) = ", ap);
        return ap;
    }

    public String getAttributeType(int arg0) {
    	debug("Entering getAttributeType....");
    	String at = reader.getAttributeType(arg0);
    	debug("reader.getAttributeType(arg0) = ", at);
        return at;
    }

    public String getAttributeValue(int arg0) {
    	debug("Entering getAttributeValue....");
    	String av = reader.getAttributeValue(arg0);
    	debug("reader.getAttributeValue(arg0) = ", av);
        return av;
    }

    public String getAttributeValue(String arg0, String arg1) {
    	debug("Entering getAttributeValue....");
    	String av = reader.getAttributeValue(arg0, arg1);
    	debug("reader.getAttributeValue(arg0, arg1) = ", av);
        return av;
    }

    public String getCharacterEncodingScheme() {
    	debug("Entering getCharacterEncodingScheme....");
    	String ces = reader.getCharacterEncodingScheme();
    	debug("reader.getCharacterEncodingScheme = ", ces);
        return ces;
    }

    public String getElementText() throws XMLStreamException {
    	debug("Entering getElementText....");
    	String et = reader.getElementText();
    	debug("reader.getElementText = ", et);
        return et;
    }

    public String getEncoding() {
    	debug("Entering getEncoding....");
    	String e = reader.getEncoding();
    	debug("reader.getEncoding() = ", e);
        return e;
    }

    public int getEventType() {
    	debug("Entering getEventType....");
    	int et = reader.getEventType();
    	debug("reader.getEventType() = ", et);
        return et;
    }

    public String getLocalName() {
    	debug("Entering getLocation....");
    	String ln = reader.getLocalName();
    	debug("reader.getLocalName() = ", ln);
        return ln;
    }

    public Location getLocation() {
    	debug("Entering getLocation....");
    	Location l = reader.getLocation();
    	debug("reader.getLocation() = ", l);
        return l;
    }

    public QName getName() {
    	debug("Entering getName....");
    	QName qn = reader.getName();
    	debug("reader.getName() = ", qn);
        return qn;
    }

    public NamespaceContext getNamespaceContext() {
    	debug("Entering getNamespaceContext....");
    	NamespaceContext nsContext = reader.getNamespaceContext();
    	debug("reader.getNamespaceContext() = ", nsContext);
        return nsContext;
    }

    public int getNamespaceCount() {
    	debug("Entering getNamespaceCount....");
    	int nsCount = reader.getNamespaceCount();
    	debug("reader.getNamespaceCount() = ", nsCount);
        return nsCount;
    }

    public String getNamespacePrefix(int arg0) {
    	debug("Entering getNamespacePrefix....");
    	String nsPrefix = reader.getNamespacePrefix(arg0);
    	debug("reader.getNamespacePrefix(arg0 = ", nsPrefix);
        return nsPrefix;
    }

    public String getNamespaceURI() {
    	debug("Entering getNamespaceURI....");
    	String nsUri = reader.getNamespaceURI();
    	debug("reader.getNamespaceURI() = ", nsUri);
        return nsUri;
    }

    public String getNamespaceURI(int arg0) {
    	debug("Entering getNamespaceURI....");
    	String nsUri = reader.getNamespaceURI(arg0);
    	debug("reader.getNamespaceURI(arg0) = ", nsUri);
        return nsUri;
    }

    public String getNamespaceURI(String arg0) {
    	debug("Entering getNamespaceURI....");
    	String nsUri = reader.getNamespaceURI(arg0);
    	debug("reader.getNamespaceURI(arg0) = ", nsUri);
        return nsUri;
    }

    public String getPIData() {
    	debug("Entering getPIData....");
    	String pid = reader.getPIData();
    	debug("reader.getPIData() = ", pid);
        return pid;
    }

    public String getPITarget() {
    	debug("Entering getPITarget....");
    	String pit = reader.getPITarget();
    	debug("reader.getPITarget() = ", pit);
        return pit;
    }

    public String getPrefix() {
    	debug("Entering getPrefix....");
    	String gpf = reader.getPrefix();
    	debug("reader.getPrefix() = ", gpf);
        return gpf;
    }

    public Object getProperty(String arg0) throws IllegalArgumentException {
    	debug("Entering getProperty for ..." + arg0);
    	Object o = reader.getProperty(arg0);
    	debug("reader.getProperty(arg0) = ", o);
        return o;
    }

    public String getText() {
    	debug("Entering getText....");
    	String gt = reader.getText();
    	debug("reader.getText() = ", gt);
        return gt;
    }

    public char[] getTextCharacters() {
    	debug("Entering getTextCharacters....");
    	char[] gtc = reader.getTextCharacters();
    	debug("reader.getTextCharacters() = ", Arrays.toString(gtc));
        return gtc;
    }

    public int getTextCharacters(int arg0, char[] arg1, int arg2, int arg3) throws XMLStreamException {
    	debug("Entering getTextCharacters....");
    	int gtc = reader.getTextCharacters(arg0, arg1, arg2, arg3);
    	debug("reader.getTextCharacters() = ", gtc);
        return gtc;
    }

    public int getTextLength() {
    	debug("Entering getTextLength....");
    	int gtl = reader.getTextLength();
    	debug("reader.getTextLength() = ", gtl);
        return gtl;
    }

    public int getTextStart() {
    	debug("Entering getTextStart....");
    	int gts = reader.getTextStart();
    	debug("reader.getTextStart() = ", gts);
        return reader.getTextStart();
    }

    public String getVersion() {
    	debug("Entering getVersion....");
    	String gv = reader.getVersion();
    	debug("reader.getVersion() = ", gv);
        return gv;
    }

    public boolean hasName() {
    	debug("Entering hasName....");
    	boolean b = reader.hasName();
    	debug("reader.hasName() = ", b);
        return b;
    }

    public boolean hasNext() throws XMLStreamException {
    	debug("Entering hasNext....");
    	boolean b = reader.hasNext();
    	debug("reader.hasNext() = ", b);
        return b;
    }

    public boolean hasText() {
    	debug("Entering hasText....");
    	boolean b = reader.hasText();
    	debug("reader.hasText() = ", b);
        return b;
    }

    public boolean isAttributeSpecified(int arg0) {
    	debug("Entering isAttributeSpecified....");
    	boolean b = reader.isAttributeSpecified(arg0);
    	debug("Entering reader.isAttributeSpecified(arg0) ", b);
        return b;
    }

    public boolean isCharacters() {
    	debug("Entering isCharacters....");
    	boolean b = reader.isCharacters();
    	debug("reader.isCharacters() = ", b);
        return b;
    }

    public boolean isEndElement() {
    	debug("Entering isEndElement....");
    	boolean b = reader.isEndElement();
    	debug("reader.isEndElement() = ", b);
        return b;
    }

    public boolean isStandalone() {
    	debug("Entering isStandalone....");
    	boolean b = reader.isStandalone();
    	debug("reader.isStandalone() = ", b);
        return b;
    }

    public boolean isStartElement() {
    	debug("Entering isStartElement....");
    	boolean b = reader.isStartElement();
    	debug("reader.isStartElement() = ", b);
        return b;
    }

    public boolean isWhiteSpace() {
    	debug("Entering isWhiteSpace....");
    	boolean b = reader.isWhiteSpace();
    	debug("reader.isWhiteSpace() = ", b);
        return b;
    }

    public int next() throws XMLStreamException {
    	debug("Entering next....");
    	int nxt = reader.next();
    	debug("reader.next() = ", nxt);
        return nxt;
    }

    public int nextTag() throws XMLStreamException {
    	debug("Entering nextTag....");
    	int tag = reader.nextTag();
    	debug("reader.nextTag() = ", tag);
        return tag;
    }

    public void require(int arg0, String arg1, String arg2) throws XMLStreamException {
    	debug("Entering require....");
    	debug("reader.require -> arg0 = ", arg0, " ,arg1 = ",arg1, " ,arg2 = ", arg2);
        reader.require(arg0, arg1, arg2);
    }

    public boolean standaloneSet() {
    	debug("Entering standaloneSet....");
    	boolean b = reader.standaloneSet();
    	debug("reader.standaloneSet() = ", b);
        return b;
    }

    private void debug(Object... messages) {
        if (log.isDebugEnabled()) {
            StringBuffer sbuff = new StringBuffer();
            for (Object msg : messages) {
                sbuff.append(msg);
            }
            log.debug(sbuff.toString());
        }
    }
}
