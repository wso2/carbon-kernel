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

package org.apache.axiom.om.impl.dom.jaxp;

import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.dom.DOMImplementationImpl;
import org.apache.axiom.om.impl.dom.DocumentImpl;
import org.apache.axiom.om.impl.dom.factory.OMDOMFactory;
import org.apache.axiom.om.util.StAXUtils;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DOOMDocumentBuilder extends DocumentBuilder {
    protected DOOMDocumentBuilder() {
    }

    /**
     * Returns whether the parser is configured to understand namespaces or not. The StAX parser
     * used by this DOM impl is namespace aware therefore this will always return true.
     *
     * @see javax.xml.parsers.DocumentBuilder#isNamespaceAware()
     */
    public boolean isNamespaceAware() {
        return true;
    }

    /**
     * The StAX builder used is the org.apache.axiom.om.impl.llom.StAXOMBuilder is a validating
     * builder.
     *
     * @see javax.xml.parsers.DocumentBuilder#isValidating()
     */
    public boolean isValidating() {
        return true;
    }

    public DOMImplementation getDOMImplementation() {
        return new DOMImplementationImpl();
    }

    /**
     * Returns a new document impl.
     *
     * @see javax.xml.parsers.DocumentBuilder#newDocument()
     */
    public Document newDocument() {
        OMDOMFactory factory = new OMDOMFactory();
        DocumentImpl documentImpl = new DocumentImpl(factory);
        documentImpl.setComplete(true);
        return documentImpl;
    }

    public void setEntityResolver(EntityResolver arg0) {
        // TODO
    }

    public void setErrorHandler(ErrorHandler arg0) {
        // TODO 
    }

    public Document parse(InputSource inputSource) throws SAXException,
            IOException {
        try {
            OMDOMFactory factory = new OMDOMFactory();
            // Not really sure whether this will work :-?
            XMLStreamReader reader = StAXUtils
                    .createXMLStreamReader(inputSource.getCharacterStream());
            StAXOMBuilder builder = new StAXOMBuilder(factory, reader);
            DocumentImpl doc = (DocumentImpl) builder.getDocument();
            doc.close(true);
            return doc;
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }
    }

    /** @see javax.xml.parsers.DocumentBuilder#parse(java.io.InputStream) */
    public Document parse(InputStream is) throws SAXException, IOException {
        try {
            OMDOMFactory factory = new OMDOMFactory();
            XMLStreamReader reader = StAXUtils
                    .createXMLStreamReader(is);
            StAXOMBuilder builder = new StAXOMBuilder(factory, reader);
            DocumentImpl doc = (DocumentImpl) builder.getDocument();
            doc.close(true);
            return doc;
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }
    }

    /** @see javax.xml.parsers.DocumentBuilder#parse(java.io.File) */
    public Document parse(File file) throws SAXException, IOException {
        FileInputStream in = new FileInputStream(file);
        try {
            return parse(in);
        } finally {
            in.close();
        }
    }

    /** @see javax.xml.parsers.DocumentBuilder#parse(java.io.InputStream, String) */
    public Document parse(InputStream is, String systemId) throws SAXException,
            IOException {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    /** @see javax.xml.parsers.DocumentBuilder#parse(String) */
    public Document parse(String uri) throws SAXException, IOException {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
