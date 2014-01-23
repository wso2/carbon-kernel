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

package org.apache.axiom.om.ds;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;

import javax.activation.DataSource;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMDataSourceExt;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.impl.MTOMXMLStreamWriter;
import org.apache.axiom.om.impl.serialize.StreamingOMSerializer;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.util.stax.WrappedTextNodeStreamReader;

/**
 * {@link org.apache.axiom.om.OMDataSource} implementation that represents a text node wrapped
 * inside an element. The text data is provided by a {@link DataSource} object.
 */
public class WrappedTextNodeOMDataSourceFromDataSource extends OMDataSourceExtBase {
    private final QName wrapperElementName;
    private final DataSource binaryData;
    private final Charset charset;

    public WrappedTextNodeOMDataSourceFromDataSource(QName wrapperElementName, DataSource binaryData,
            Charset charset) {
        this.wrapperElementName = wrapperElementName;
        this.binaryData = binaryData;
        this.charset = charset;
    }
    
    public void serialize(OutputStream out, OMOutputFormat format) throws XMLStreamException {
        XMLStreamWriter writer = new MTOMXMLStreamWriter(out, format);
        serialize(writer);
        writer.flush();
    }

    public void serialize(Writer writer, OMOutputFormat format) throws XMLStreamException {
        MTOMXMLStreamWriter xmlWriter =
            new MTOMXMLStreamWriter(StAXUtils.createXMLStreamWriter(writer));
        xmlWriter.setOutputFormat(format);
        serialize(xmlWriter);
        xmlWriter.flush();
    }

    public void serialize(XMLStreamWriter xmlWriter) throws XMLStreamException {
        StreamingOMSerializer serializer = new StreamingOMSerializer();
        serializer.serialize(getReader(), xmlWriter);
    }

    public XMLStreamReader getReader() throws XMLStreamException {
        InputStream is;
        try {
            is = binaryData.getInputStream();
        }
        catch (IOException ex) {
            throw new XMLStreamException(ex);
        }
        return new WrappedTextNodeStreamReader(wrapperElementName, new InputStreamReader(is, charset));
    }

    public Object getObject() {
        return binaryData;
    }

    public boolean isDestructiveRead() {
        return false;
    }

    public boolean isDestructiveWrite() {
        return false;
    }
    
    public byte[] getXMLBytes(String encoding) throws UnsupportedEncodingException {
        throw new UnsupportedOperationException();
    }

    public void close() {
    }

    public OMDataSourceExt copy() {
        return new WrappedTextNodeOMDataSourceFromDataSource(wrapperElementName, binaryData, charset);
    }
}
