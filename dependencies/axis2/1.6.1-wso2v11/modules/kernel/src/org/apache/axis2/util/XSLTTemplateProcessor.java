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


import org.w3c.dom.Document;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.OutputStream;


public class XSLTTemplateProcessor {

    /**
     * Parses an XML stream with an XSL stream
     *
     * @param out        Stream to write the output
     * @param xmlStream  Source XML stream
     * @param xsltStream Source XSL stream
     * @throws TransformerFactoryConfigurationError
     *
     * @throws TransformerException
     */
    public static void parse(OutputStream out,
                             InputStream xmlStream,
                             InputStream xsltStream)
            throws TransformerFactoryConfigurationError, TransformerException {
        Source xmlSource = new StreamSource(xmlStream);
        Source xsltSource = new StreamSource(xsltStream);
        Result result = new StreamResult(out);
        Transformer transformer = TransformerFactory.newInstance()
                .newTransformer(xsltSource);
        transformer.transform(xmlSource, result);

    }

    /**
     * Parses an XML stream with an XSL stream
     *
     * @param out         Stream to write the output
     * @param doc
     * @param transformer
     * @throws TransformerFactoryConfigurationError
     *
     * @throws TransformerException
     */
    public static void parse(OutputStream out,
                             Document doc,
                             Transformer transformer)
            throws TransformerFactoryConfigurationError, TransformerException {
        Source xmlSource = new DOMSource(doc);
        Result result = new StreamResult(out);
        transformer.transform(xmlSource, result);

    }


    /**
     * @param out
     * @param document
     * @param xsltStream
     * @throws TransformerFactoryConfigurationError
     *
     * @throws TransformerException
     */
    public static void parse(OutputStream out,
                             Document document,
                             InputStream xsltStream)
            throws TransformerFactoryConfigurationError, TransformerException {
        Source xsltSource = new StreamSource(xsltStream);
        Transformer transformer = TransformerFactory.newInstance()
                .newTransformer(xsltSource);
        parse(out, document, transformer);

    }

    /**
     * @param out
     * @param document
     * @param xsltStream
     * @throws TransformerFactoryConfigurationError
     *
     * @throws TransformerException
     */
    public static void parse(OutputStream out,
                             Document document,
                             InputStream xsltStream,
                             URIResolver customResolver)
            throws TransformerFactoryConfigurationError, TransformerException {
        parse(out, document, xsltStream, customResolver, false);
    }

    /**
     * @param out
     * @param document
     * @param xsltStream
     * @throws TransformerFactoryConfigurationError
     *
     * @throws TransformerException
     */
    public static void parse(OutputStream out,
                             Document document,
                             InputStream xsltStream,
                             URIResolver customResolver,
                             boolean pretty)
            throws TransformerFactoryConfigurationError, TransformerException {
        Source xsltSource = new StreamSource(xsltStream);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        if (pretty) {
            try {
                transformerFactory.setAttribute("indent-number", new Integer(2));
            } catch (Exception e) {
            }
        }
        if (customResolver != null) {
            transformerFactory.setURIResolver(customResolver);
        }

        Transformer transformer = transformerFactory
                .newTransformer(xsltSource);
        if (pretty) {
            try {
                transformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "2");
            } catch (Exception e) {
            }
        }

        parse(out, document, transformer);

    }
}
