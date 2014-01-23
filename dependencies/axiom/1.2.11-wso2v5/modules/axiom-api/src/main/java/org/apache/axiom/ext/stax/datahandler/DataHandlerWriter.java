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

package org.apache.axiom.ext.stax.datahandler;

import java.io.IOException;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;

/**
 * Extension interface for {@link javax.xml.stream.XMLStreamWriter} implementations that can
 * receive base64 encoded binary content as {@link DataHandler} objects. A stream writer
 * implementing this extension may write the binary data as base64 encoded character data
 * or using some optimization such as XOP/MTOM.
 * <p>
 * All the requirements specified in {@link org.apache.axiom.ext.stax} apply to
 * this extension interface. In particular,
 * a consumer MUST use {@link javax.xml.stream.XMLStreamWriter#getProperty(String)} with the property
 * name defined by {@link #PROPERTY} to get a reference to this extension interface.
 * <p>
 * The interface defines two methods to write binary content, one that takes a {@link DataHandler}
 * argument and one with a {@link DataHandlerProvider} argument. The first should be used when
 * the content is immediately available, while the second supports deferred loading of the data
 * handler. The meaning of the <code>contentID</code> and <code>optimize</code> arguments is
 * the same for both methods:
 * <dl>
 *   <dt><code>contentID</code>
 *   <dd>
 *     A content ID of the binary content, if available. The semantics of this argument are
 *     similar to those defined for the return value of the
 *     {@link DataHandlerReader#getContentID()} method:
 *     <ul>
 *       <li>This argument should only be set if a content ID has been used
 *           previously in an interaction with another component or system.
 *           The caller SHOULD NOT generate a new content ID solely for the
 *           purpose of invoking the extension.
 *       <li>The argument must be a raw content ID.
 *       <li>The implementation MUST NOT make any assumption about the uniqueness or validity
 *           of the content ID. It MAY ignore the supplied value.
 *     </ul>
 *   <dt><code>optimize</code>
 *   <dd>
 *     Specifies if binary content is eligible for optimization (e.g. using XOP) or if it should
 *     be serialized as base64. This is only an indication and the implementation MAY choose
 *     to override this value or ignore it entirely.
 * </dl>
 * Instead of interacting directly with this extension interface, the consumer may use the
 * {@link org.apache.axiom.util.stax.XMLStreamWriterUtils#writeDataHandler(javax.xml.stream.XMLStreamWriter, DataHandler, String, boolean)} or
 * {@link org.apache.axiom.util.stax.XMLStreamWriterUtils#writeDataHandler(javax.xml.stream.XMLStreamWriter, DataHandlerProvider, String, boolean)}
 * utility methods. These methods make the processing of binary data entirely transparent for
 * the caller.
 */
public interface DataHandlerWriter {
    /**
     * The name of the property used to look up this extension interface from a
     * {@link javax.xml.stream.XMLStreamWriter} implementation.
     */
    String PROPERTY = DataHandlerWriter.class.getName();

    /**
     * Write binary content to the stream. The implementation may choose to write the data as base64
     * encoded character data or using an optimization protocol such as XOP/MTOM.
     * 
     * @param dataHandler
     *            the binary content to write
     * @param contentID
     *            an existing content ID for the binary data (see above)
     * @param optimize
     *            indicates whether the content is eligible for optimization (see above)
     * @throws IOException
     *             if an error occurs while reading from the data handler
     * @throws XMLStreamException
     *             if an error occurs while writing to the underlying stream
     */
    void writeDataHandler(DataHandler dataHandler, String contentID, boolean optimize)
            throws IOException, XMLStreamException;

    /**
     * Write binary content to the stream. This method allows the implementation to defer loading of
     * the content. More precisely, if the implementation decides to use an optimization scheme such
     * as XOP, then the content will not be written immediately to the underlying stream, but only
     * after the XML infoset is complete. If the caller uses this method, the implementation can
     * defer the actual loading of the binary content.
     * 
     * @param dataHandlerProvider
     *            the binary content to write
     * @param contentID
     *            an existing content ID for the binary data (see above)
     * @param optimize
     *            indicates whether the content is eligible for optimization (see above)
     * @throws IOException
     *             If an error occurs while reading from the data handler. Since the implementation
     *             is free to override the supplied <code>optimize</code> argument, it may attempt
     *             to load the binary data immediately. Because this operation may fail, the method
     *             must declare this exception.
     * @throws XMLStreamException
     *             if an error occurs while writing to the underlying stream
     */
    void writeDataHandler(DataHandlerProvider dataHandlerProvider, String contentID,
            boolean optimize) throws IOException, XMLStreamException;
}
