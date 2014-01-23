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
package org.apache.axiom.om;

import java.io.InputStream;
import java.io.Reader;

import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.util.StAXParserConfiguration;
import org.apache.axiom.soap.SOAPFactory;
import org.xml.sax.InputSource;

/**
 * Provides static factory methods to create various kinds of object model builders from different
 * types of input sources. The methods defined by this class are the starting point to parse XML
 * documents into Axiom trees.
 */
public class OMXMLBuilderFactory {
    private OMXMLBuilderFactory() {}
    
    /**
     * Create an object model builder for plain XML that pulls events from a StAX stream reader.
     * 
     * @param parser
     *            the stream reader to read the XML data from
     * @return the builder
     */
    public static OMXMLParserWrapper createStAXOMBuilder(XMLStreamReader parser) {
        OMMetaFactory metaFactory = OMAbstractFactory.getMetaFactory();
        return metaFactory.createStAXOMBuilder(metaFactory.getOMFactory(), parser);
    }
    
    /**
     * Create an object model builder that pulls events from a StAX stream reader using a specified
     * object model factory.
     * 
     * @param omFactory
     *            the object model factory to use
     * @param parser
     *            the stream reader to read the XML data from
     * @return the builder
     */
    public static OMXMLParserWrapper createStAXOMBuilder(OMFactory omFactory, XMLStreamReader parser) {
        return omFactory.getMetaFactory().createStAXOMBuilder(omFactory, parser);
    }
    
    /**
     * Create an object model builder that reads a plain XML document from the provided input stream
     * with the default parser configuration defined by {@link StAXParserConfiguration#DEFAULT}.
     * 
     * @param in
     *            the input stream representing the XML document
     * @return the builder
     */
    public static OMXMLParserWrapper createOMBuilder(InputStream in) {
        return createOMBuilder(StAXParserConfiguration.DEFAULT, in);
    }
    
    /**
     * Create an object model builder that reads a plain XML document from the provided input stream
     * with a given parser configuration.
     * 
     * @param configuration
     *            the parser configuration to use
     * @param in
     *            the input stream representing the XML document
     * @return the builder
     */
    public static OMXMLParserWrapper createOMBuilder(StAXParserConfiguration configuration, InputStream in) {
        OMMetaFactory metaFactory = OMAbstractFactory.getMetaFactory();
        return metaFactory.createOMBuilder(metaFactory.getOMFactory(), configuration, new InputSource(in));
    }
    
    /**
     * Create an object model builder that reads an XML document from the provided input stream
     * using a specified object model factory and with the default parser configuration defined by
     * {@link StAXParserConfiguration#DEFAULT}.
     * 
     * @param omFactory
     *            the object model factory to use
     * @param in
     *            the input stream representing the XML document
     * @return the builder
     */
    public static OMXMLParserWrapper createOMBuilder(OMFactory omFactory, InputStream in) {
        return createOMBuilder(omFactory, StAXParserConfiguration.DEFAULT, in);
    }
    
    /**
     * Create an object model builder that reads an XML document from the provided input stream
     * using a specified object model factory and with a given parser configuration.
     * 
     * @param omFactory
     *            the object model factory to use
     * @param configuration
     *            the parser configuration to use
     * @param in
     *            the input stream representing the XML document
     * @return the builder
     */
    public static OMXMLParserWrapper createOMBuilder(OMFactory omFactory, StAXParserConfiguration configuration, InputStream in) {
        return omFactory.getMetaFactory().createOMBuilder(omFactory, configuration, new InputSource(in));
    }
    
    /**
     * Create an object model builder that reads a plain XML document from the provided character
     * stream with the default parser configuration defined by
     * {@link StAXParserConfiguration#DEFAULT}.
     * 
     * @param in
     *            the character stream representing the XML document
     * @return the builder
     */
    public static OMXMLParserWrapper createOMBuilder(Reader in) {
        return createOMBuilder(StAXParserConfiguration.DEFAULT, in);
    }
    
    /**
     * Create an object model builder that reads a plain XML document from the provided character
     * stream with a given parser configuration.
     * 
     * @param configuration
     *            the parser configuration to use
     * @param in
     *            the character stream representing the XML document
     * @return the builder
     */
    public static OMXMLParserWrapper createOMBuilder(StAXParserConfiguration configuration, Reader in) {
        OMMetaFactory metaFactory = OMAbstractFactory.getMetaFactory();
        return metaFactory.createOMBuilder(metaFactory.getOMFactory(), configuration, new InputSource(in));
    }
    
    /**
     * Create an object model builder that reads an XML document from the provided character stream
     * using a specified object model factory and with the default parser configuration defined by
     * {@link StAXParserConfiguration#DEFAULT}.
     * 
     * @param omFactory
     *            the object model factory to use
     * @param in
     *            the character stream representing the XML document
     * @return the builder
     */
    public static OMXMLParserWrapper createOMBuilder(OMFactory omFactory, Reader in) {
        return createOMBuilder(omFactory, StAXParserConfiguration.DEFAULT, in);
    }
    
    /**
     * Create an object model builder that reads an XML document from the provided character stream
     * using a specified object model factory and with a given parser configuration.
     * 
     * @param omFactory
     *            the object model factory to use
     * @param configuration
     *            the parser configuration to use
     * @param in
     *            the character stream representing the XML document
     * @return the builder
     */
    public static OMXMLParserWrapper createOMBuilder(OMFactory omFactory, StAXParserConfiguration configuration, Reader in) {
        return omFactory.getMetaFactory().createOMBuilder(omFactory, configuration, new InputSource(in));
    }
    
    /**
     * Create an object model builder for SOAP that reads a message from the provided input stream,
     * using a given charset encoding. The method will select the appropriate {@link SOAPFactory}
     * based on the namespace URI of the SOAP envelope. It will configure the underlying parser as
     * specified by {@link StAXParserConfiguration#SOAP}.
     * 
     * @param in
     *            the input stream containing the SOAP message
     * @param encoding
     *            the charset encoding
     * @return the builder
     */
    public static OMXMLParserWrapper createSOAPModelBuilder(InputStream in, String encoding) {
        InputSource is = new InputSource(in);
        is.setEncoding(encoding);
        return OMAbstractFactory.getMetaFactory().createSOAPModelBuilder(StAXParserConfiguration.SOAP, is);
    }
}
