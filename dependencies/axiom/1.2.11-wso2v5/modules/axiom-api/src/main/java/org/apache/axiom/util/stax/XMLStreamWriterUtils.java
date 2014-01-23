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

package org.apache.axiom.util.stax;

import java.io.IOException;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.ext.stax.datahandler.DataHandlerProvider;
import org.apache.axiom.ext.stax.datahandler.DataHandlerWriter;
import org.apache.axiom.util.base64.Base64EncodingWriterOutputStream;

/**
 * Contains utility methods to work with {@link XMLStreamWriter} objects.
 */
public class XMLStreamWriterUtils {
    /**
     * Write base64 encoded data to a stream writer. This will result in one or more
     * {@link javax.xml.stream.XMLStreamConstants#CHARACTERS} events to be written
     * to the stream (or zero events if the data handler produces an empty byte sequence),
     * i.e. the data is streamed from the data handler directly to the stream writer.
     * Since no in-memory base64 representation of the entire binary data is built, this
     * method is suitable for very large amounts of data.
     * <p>
     * Note that this method will always serialize the data as base64 encoded character data.
     * Serialization code should prefer using
     * {@link #writeDataHandler(XMLStreamWriter, DataHandler, String, boolean)} or
     * {@link #writeDataHandler(XMLStreamWriter, DataHandlerProvider, String, boolean)} to
     * enable optimization (if supported by the {@link XMLStreamWriter}).
     * 
     * @param writer the stream writer to write the data to
     * @param dh the data handler containing the data to encode
     * @throws IOException if an error occurs when reading the data from the data handler
     * @throws XMLStreamException if an error occurs when writing the base64 encoded data to
     *         the stream
     */
    public static void writeBase64(XMLStreamWriter writer, DataHandler dh)
            throws IOException, XMLStreamException {
        
        Base64EncodingWriterOutputStream out = new Base64EncodingWriterOutputStream(
                new XMLStreamWriterWriter(writer));
        try {
            dh.writeTo(out);
            out.close();
        } catch (XMLStreamIOException ex) {
            throw ex.getXMLStreamException();
        }
    }

    private static DataHandlerWriter internalGetDataHandlerWriter(XMLStreamWriter writer) {
        try {
            return (DataHandlerWriter)writer.getProperty(DataHandlerWriter.PROPERTY);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /**
     * Get the {@link DataHandlerWriter} extension for a given {@link XMLStreamWriter}. If the
     * writer expose the extension, a reference to the extension interface implementation is
     * returned. If the writer doesn't expose the extension, this method returns an instance of the
     * extension interface that emulates the extension (by writing the binary data as base64
     * character data to the stream).
     * 
     * @param writer
     *            the stream for which the method should return the {@link DataHandlerWriter}
     *            extension
     * @return a reference to the extension interface exposed by the writer or an implementation the
     *         emulates the extension; the return value is never <code>null</code>
     */
    public static DataHandlerWriter getDataHandlerWriter(final XMLStreamWriter writer) {
        DataHandlerWriter dataHandlerWriter = internalGetDataHandlerWriter(writer);
        if (dataHandlerWriter == null) {
            return new DataHandlerWriter() {
                public void writeDataHandler(DataHandler dataHandler, String contentID,
                        boolean optimize) throws IOException, XMLStreamException {
                    writeBase64(writer, dataHandler);
                }

                public void writeDataHandler(DataHandlerProvider dataHandlerProvider,
                        String contentID, boolean optimize) throws IOException, XMLStreamException {
                    writeBase64(writer, dataHandlerProvider.getDataHandler());
                }
            };
        } else {
            return dataHandlerWriter;
        }
    }

    /**
     * Write binary content to the stream. Depending on the supplied {@link XMLStreamWriter},
     * the content will be written as base64 encoded character data or using an optimization
     * scheme such as XOP/MTOM. The method attempts to submit the binary content using the
     * {@link DataHandlerWriter} extension. If the writer doesn't expose this extension,
     * the method will fall back to {@link #writeBase64(XMLStreamWriter, DataHandler)}.
     * <p>
     * Please refer to the documentation of {@link DataHandlerWriter} for a more
     * detailed description of the semantics of the different arguments.
     * 
     * @param writer
     *            the stream writer to write the data to
     * @param dataHandler
     *            the binary content to write
     * @param contentID
     *            an existing content ID for the binary data
     * @param optimize
     *            indicates whether the content is eligible for optimization
     * @throws IOException
     *             if an error occurs while reading from the data handler
     * @throws XMLStreamException
     *             if an error occurs while writing to the underlying stream
     */
    public static void writeDataHandler(XMLStreamWriter writer, DataHandler dataHandler,
            String contentID, boolean optimize) throws IOException, XMLStreamException {
        DataHandlerWriter dataHandlerWriter = internalGetDataHandlerWriter(writer);
        if (dataHandlerWriter != null) {
            dataHandlerWriter.writeDataHandler(dataHandler, contentID, optimize);
        } else {
            writeBase64(writer, dataHandler);
        }
    }
    
    /**
     * Write binary content to the stream. This method is similar to
     * {@link #writeDataHandler(XMLStreamWriter, DataHandler, String, boolean)},
     * but supports deferred loading of the data handler.
     * 
     * @param writer
     *            the stream writer to write the data to
     * @param dataHandlerProvider
     *            the binary content to write
     * @param contentID
     *            an existing content ID for the binary data
     * @param optimize
     *            indicates whether the content is eligible for optimization
     * @throws IOException
     *             if an error occurs while reading from the data handler
     * @throws XMLStreamException
     *             if an error occurs while writing to the underlying stream
     */
    public static void writeDataHandler(XMLStreamWriter writer, DataHandlerProvider dataHandlerProvider,
            String contentID, boolean optimize) throws IOException, XMLStreamException {
        DataHandlerWriter dataHandlerWriter = internalGetDataHandlerWriter(writer);
        if (dataHandlerWriter != null) {
            dataHandlerWriter.writeDataHandler(dataHandlerProvider, contentID, optimize);
        } else {
            writeBase64(writer, dataHandlerProvider.getDataHandler());
        }
    }
}
