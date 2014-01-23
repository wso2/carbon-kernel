/**
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

package org.apache.ws.security.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

public class XMLUtils {
    
    private static final Log log = LogFactory.getLog(XMLUtils.class.getName());
    private static final boolean doDebug = log.isDebugEnabled();
    
    public static String PrettyDocumentToString(Document doc) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ElementToStream(doc.getDocumentElement(), baos);
        return new String(baos.toByteArray());
    }

    public static void ElementToStream(Element element, OutputStream out) {
        try {
            DOMSource source = new DOMSource(element);
            StreamResult result = new StreamResult(out);
            TransformerFactory transFactory = TransformerFactory.newInstance();
            Transformer transformer = transFactory.newTransformer();
            transformer.transform(source, result);
        } catch (Exception ex) {
            if (doDebug) {
                log.debug(ex.getMessage(), ex);
            }
        }
    }

    /**
     * Utility to get the bytes uri
     *
     * @param source the resource to get
     */
    public static InputSource sourceToInputSource(Source source) {
        if (source instanceof SAXSource) {
            return ((SAXSource) source).getInputSource();
        } else if (source instanceof DOMSource) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Node node = ((DOMSource) source).getNode();
            if (node instanceof Document) {
                node = ((Document) node).getDocumentElement();
            }
            Element domElement = (Element) node;
            ElementToStream(domElement, baos);
            InputSource isource = new InputSource(source.getSystemId());
            isource.setByteStream(new ByteArrayInputStream(baos.toByteArray()));
            return isource;
        } else if (source instanceof StreamSource) {
            StreamSource ss = (StreamSource) source;
            InputSource isource = new InputSource(ss.getSystemId());
            isource.setByteStream(ss.getInputStream());
            isource.setCharacterStream(ss.getReader());
            isource.setPublicId(ss.getPublicId());
            return isource;
        } else {
            return getInputSourceFromURI(source.getSystemId());
        }
    }

    /**
     * Utility to get the bytes uri.
     * Does NOT handle authenticated URLs,
     * use getInputSourceFromURI(uri, username, password)
     *
     * @param uri the resource to get
     */
    public static InputSource getInputSourceFromURI(String uri) {
        return new InputSource(uri);
    }
}
