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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
 * Interface to a backing object that can can be read and written as XML.
 * 
 * To plug an arbitrary object into an OM tree.  Follow these steps
 *  1) Provide a class that implements OMDataSourceExt.
 *  2) Use OMFactory.createOMElement(OMDataSource, String, OMNamespace) to build an
 *     OMSourcedElement.
 *  3) Add the OMSourcedElement to the OM tree.
 * 
 * OMDataSourceExt provides additional methods that are not available on the
 * original OMDataSource.
 * 
 * @see OMDataSource
 * @see OMSourcedElement
 */
public interface OMDataSourceExt extends OMDataSource {
    
    /* Property lossyPrefix
     * Value null or Boolean.TRUE or Boolean.FALSE
     * If Boolean.TRUE, this indicates that expansion is needed to 
     * obtain the actual prefix name. 
     */
    public static final String LOSSY_PREFIX = "lossyPrefix";

    /**
     * Serializes element data directly to stream.
     * Assumes that the backing object is destroyed during serialization if isDestructiveWrite
     * @see OMDataSourceExt
     *
     * @param output destination stream for element XML text
     * @param format Output format information. The implementation must use this information
     *               to choose the correct character set encoding when writing to the
     *               output stream. This parameter must not be null.
     * @throws XMLStreamException
     */
    public void serialize(OutputStream output, OMOutputFormat format) throws XMLStreamException;

    /**
     * Serializes element data directly to writer.
     * Assumes that the backing object is destroyed during serialization isDestructiveWrite
     * @see OMDataSourceExt
     *
     * @param writer destination writer for element XML text
     * @param format output format information (<code>null</code> if none; may
     * be ignored if not supported by data binding even if supplied)
     * @throws XMLStreamException
     */
    public void serialize(Writer writer, OMOutputFormat format) throws XMLStreamException;

    /**
     * Serializes element data directly to StAX writer.
     * Assumes that the backing object is destroyed during serialization isDestructiveWrite
     * @see OMDataSourceExt
     *
     * @param xmlWriter destination writer
     * @throws XMLStreamException
     */
    public void serialize(XMLStreamWriter xmlWriter) throws XMLStreamException;

    /**
     * Get parser for element data. In the general case this may require the
     * data source to serialize data as XML text and then parse that text.
     *
     * @return element parser
     * @throws XMLStreamException
     */
    public XMLStreamReader getReader() throws XMLStreamException;
    
    /**
     * Returns the backing Object.
     * @return Object
     */
    public Object getObject();
    
    /**
     * Returns true if reading the backing object is destructive.
     * An example of an object with a destructive read is an InputSteam.
     * The owning OMSourcedElement uses this information to detemine if OM tree
     * expansion is needed when reading the OMDataSourceExt.
     * @return boolean
     */
    public boolean isDestructiveRead();
    
    /**
     * Returns true if writing the backing object is destructive.
     * An example of an object with a destructive write is an InputStream.
     * The owning OMSourcedElement uses this information to detemine if OM tree
     * expansion is needed when writing the OMDataSourceExt.
     * @return boolean
     */
    public boolean isDestructiveWrite();
    
    /**
     * Returns a InputStream representing the xml data
     * @param encoding String encoding of InputStream
     * @return InputStream
     */
    public InputStream getXMLInputStream(String encoding) throws UnsupportedEncodingException;
    
    /**
     * Returns a byte[] representing the xml data
     * @param encoding String encoding of InputStream
     * @return byte[]
     * @see #getXMLInputStream(String)
     */
    public byte[] getXMLBytes(String encoding) throws UnsupportedEncodingException;
    
    /**
     * Close the DataSource and free its resources.
     */
    public void close();
    
    /**
     * Create a copy of the OMDataSourceExt
     * @return OMDataSourceExt
     */
    public OMDataSourceExt copy();
    
    /**
     * Returns true if property is set
     * @param key
     * @return TODO
     */
    public boolean hasProperty(String key);
    
    /**
     * Query a property stored on the OMDataSource
     * @param key
     * @return value or null
     */
    public Object getProperty(String key);
    
    /**
     * Set a property on the OMDataSource
     * @param key
     * @param value
     * @return old property object or null
     */
    public Object setProperty(String key, Object value);
}
