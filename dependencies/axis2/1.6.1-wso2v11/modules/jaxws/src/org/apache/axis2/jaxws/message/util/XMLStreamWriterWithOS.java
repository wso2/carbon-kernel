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

import org.apache.axiom.om.util.StAXUtils;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;

/**
 * XMLStreamReader that exposes direct access to the OutputStream.
 * Writing to the output stream is faster in some cases.
 */
public class XMLStreamWriterWithOS implements XMLStreamWriter {
    XMLStreamWriter writer;
    String charSetEncoding;
    OutputStream os;
    

    public XMLStreamWriterWithOS(OutputStream os, String charSetEncoding) 
        throws XMLStreamException {
        super();
        writer = null; // Writer is created when needed
        this.os = os;
        this.charSetEncoding = charSetEncoding;
    }

    /**
     * The writer is created lazily. 
     * If only the output stream is used, then the writer is never created.
     */
    private void createWriter() throws XMLStreamException {
        if (writer == null) {
            writer =  StAXUtils.createXMLStreamWriter(os, charSetEncoding);
        }
    }
    
    
    public void close() throws XMLStreamException {
        if (writer != null) {
            writer.close();
        }
    }

    public void flush() throws XMLStreamException {
        if (writer != null) {
            writer.flush();
        }
    }

    public NamespaceContext getNamespaceContext() {
        try {
            createWriter();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }        
        return writer.getNamespaceContext();
    }

    public String getPrefix(String arg0) throws XMLStreamException {
        createWriter();
        return writer.getPrefix(arg0);
    }

    public Object getProperty(String arg0) throws IllegalArgumentException {
        try {
            createWriter();
        } catch (XMLStreamException e) {
            throw new IllegalArgumentException(e);
        }
        return writer.getProperty(arg0);
    }

    public void setDefaultNamespace(String arg0) throws XMLStreamException {
        createWriter();
        writer.setDefaultNamespace(arg0);
    }

    public void setNamespaceContext(NamespaceContext arg0) throws XMLStreamException {
        createWriter();
        writer.setNamespaceContext(arg0);
    }

    public void setPrefix(String arg0, String arg1) throws XMLStreamException {
        createWriter();
        writer.setPrefix(arg0, arg1);
    }

    public void writeAttribute(String arg0, String arg1, String arg2, String arg3) 
    throws XMLStreamException {
        createWriter();
        writer.writeAttribute(arg0, arg1, arg2, arg3);
    }

    public void writeAttribute(String arg0, String arg1, String arg2) 
    throws XMLStreamException {
        createWriter();
        writer.writeAttribute(arg0, arg1, arg2);
    }

    public void writeAttribute(String arg0, String arg1) throws XMLStreamException {
        createWriter();
        writer.writeAttribute(arg0, arg1);
    }

    public void writeCData(String arg0) throws XMLStreamException {
        createWriter();
        writer.writeCData(arg0);
    }

    public void writeCharacters(char[] arg0, int arg1, int arg2) 
    throws XMLStreamException {
        createWriter();
        writer.writeCharacters(arg0, arg1, arg2);
    }

    public void writeCharacters(String arg0) throws XMLStreamException {
        createWriter();
        writer.writeCharacters(arg0);
    }

    public void writeComment(String arg0) throws XMLStreamException {
        createWriter();
        writer.writeComment(arg0);
    }

    public void writeDefaultNamespace(String arg0) throws XMLStreamException {
        createWriter();
        writer.writeDefaultNamespace(arg0);
    }

    public void writeDTD(String arg0) throws XMLStreamException {
        createWriter();
        writer.writeDTD(arg0);
    }

    public void writeEmptyElement(String arg0, String arg1, String arg2) 
    throws XMLStreamException {
        createWriter();
        writer.writeEmptyElement(arg0, arg1, arg2);
    }

    public void writeEmptyElement(String arg0, String arg1) throws XMLStreamException {
        createWriter();
        writer.writeEmptyElement(arg0, arg1);
    }

    public void writeEmptyElement(String arg0) throws XMLStreamException {
        createWriter();
        writer.writeEmptyElement(arg0);
    }

    public void writeEndDocument() throws XMLStreamException {
        createWriter();
        writer.writeEndDocument();
    }

    public void writeEndElement() throws XMLStreamException {
        createWriter();
        writer.writeEndElement();
    }

    public void writeEntityRef(String arg0) throws XMLStreamException {
        createWriter();
        writer.writeEntityRef(arg0);
    }

    public void writeNamespace(String arg0, String arg1) throws XMLStreamException {
        createWriter();
        writer.writeNamespace(arg0, arg1);
    }

    public void writeProcessingInstruction(String arg0, String arg1) 
    throws XMLStreamException {
        createWriter();
        writer.writeProcessingInstruction(arg0, arg1);
    }

    public void writeProcessingInstruction(String arg0) throws XMLStreamException {
        createWriter();
        writer.writeProcessingInstruction(arg0);
    }

    public void writeStartDocument() throws XMLStreamException {
        createWriter();
        writer.writeStartDocument();
    }

    public void writeStartDocument(String arg0, String arg1) throws XMLStreamException {
        createWriter();
        writer.writeStartDocument(arg0, arg1);
    }

    public void writeStartDocument(String arg0) throws XMLStreamException {
        createWriter();
        writer.writeStartDocument(arg0);
    }

    public void writeStartElement(String arg0, String arg1, String arg2) 
    throws XMLStreamException {
        createWriter();
        writer.writeStartElement(arg0, arg1, arg2);
    }

    public void writeStartElement(String arg0, String arg1) throws XMLStreamException {
        createWriter();
        writer.writeStartElement(arg0, arg1);
    }

    public void writeStartElement(String arg0) throws XMLStreamException {
        createWriter();
        writer.writeStartElement(arg0);
    }
    
    /**
     * If this XMLStreamWriter is connected to an OutputStream
     * then the OutputStream is returned.  This allows a node
     * (perhaps an OMSourcedElement) to write its content
     * directly to the OutputStream.
     * @return OutputStream or null
     */
    public OutputStream getOutputStream() throws XMLStreamException {
        
        if (os != null) {
            // Flush the state of the writer..Many times the 
            // write defers the writing of tag characters (>)
            // until the next write.  Flush out this character
            if (writer != null) {
                this.writeCharacters(""); 
                this.flush();
            }
        }
        return os;
    }
}
