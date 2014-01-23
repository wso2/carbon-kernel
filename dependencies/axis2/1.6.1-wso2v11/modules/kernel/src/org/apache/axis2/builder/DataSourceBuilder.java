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
package org.apache.axis2.builder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.attachments.impl.BufferUtils;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;

public class DataSourceBuilder implements Builder {

    public OMElement processDocument(InputStream inputStream, String contentType,
                                     MessageContext msgContext)
            throws AxisFault {
        msgContext.setDoingREST(true);
        
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace ns = factory.createOMNamespace("", "");
        byte[] bytes;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
            BufferUtils.inputStream2OutputStream(inputStream, baos);
            baos.flush();
            bytes = baos.toByteArray();
        } catch (IOException e) {
            throw AxisFault.makeFault(e);
        }
        ByteArrayDataSourceEx ds = new ByteArrayDataSourceEx(bytes, contentType);
        return factory.createOMElement(ds, "dummy", ns);
    }

    public class ByteArrayDataSourceEx extends javax.mail.util.ByteArrayDataSource implements OMDataSource {
        private byte[] bytes;
    
        public ByteArrayDataSourceEx(byte[] bytes, String s) {
            super(bytes, s);
            this.bytes = bytes;
        }

        public void serialize(OutputStream output, OMOutputFormat format) throws XMLStreamException {
            try {
                output.write(bytes);
            } catch (IOException e) {
                throw new XMLStreamException(e);
            }
        }
    
        public java.lang.String getContentType() {
            return super.getContentType();
        }

        public void serialize(Writer writer, OMOutputFormat format) throws XMLStreamException {
            throw new UnsupportedOperationException("FIXME");
        }

        public void serialize(XMLStreamWriter xmlWriter) throws XMLStreamException {
            throw new UnsupportedOperationException("FIXME");
        }

        public XMLStreamReader getReader() throws XMLStreamException {
            throw new UnsupportedOperationException("FIXME");
        }
    }
}
