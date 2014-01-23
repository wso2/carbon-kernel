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

package org.apache.axis2.datasource.jaxb;



import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * In some cases, we want to marshal an array of objects as a series
 * of elements instead of a single element containing items.
 * 
 * Unfortunately there is no way to tell the JAXB marshal method to 
 * marshal an array or List as a series of elements.   
 * 
 * Instead, we use the JAXB marshal method to output a single element
 * with items and then filter the write events to transform it into
 * a series of elements.
 * 
 * Thus
 *     <myElement>
 *        <item>Hello</item>
 *        <item>World</item>
 *     </myElement>
 * Becomes
 *     <myElement>Hello</myElement>
 *     <myElement>World</myElement>
 *     
 * Special care is taken to ensure that namespace declarations are property preserved.
 */
public class XMLStreamWriterArrayFilter implements XMLStreamWriter {
    private static final Log log = LogFactory.getLog(XMLStreamWriterArrayFilter.class);
    XMLStreamWriter writer;
    int depth = 0;
    boolean isBuffering = true;
    List<List<String>> bufferedCommands = new ArrayList<List<String>>();
    
    // The extensive debug was used during development but is not necessary in production.
    // Change DEBUG_ENABLED = log.isDebugEnabled();
    // to get extensive debug output.
    private static boolean DEBUG_ENABLED = false; 
    
    private static final String XSI_URI =  "http://www.w3.org/2001/XMLSchema-instance";
   

    public XMLStreamWriterArrayFilter(XMLStreamWriter writer) 
        throws XMLStreamException {
        super();
        if (DEBUG_ENABLED) {
            log.debug("XMLStreamWriterArrayFilter " + writer);
        }
        this.writer = writer;
    }
    
    public void close() throws XMLStreamException {
        if (DEBUG_ENABLED) {
            log.debug("close");
        }
        if (writer != null) {
            writer.close();
        }
    }

    public void flush() throws XMLStreamException {
        if (DEBUG_ENABLED) {
            log.debug("flush");
        }
        if (writer != null) {
            writer.flush();
        }
    }

    public NamespaceContext getNamespaceContext() {  
        if (DEBUG_ENABLED) {
            log.debug("getNamespaceContext");
        }
        return writer.getNamespaceContext();
    }

    public String getPrefix(String arg0) throws XMLStreamException {
        if (DEBUG_ENABLED) {
            log.debug("getPrefix " + arg0);
        }
        return writer.getPrefix(arg0);
    }

    public Object getProperty(String arg0) throws IllegalArgumentException {
        if (DEBUG_ENABLED) {
            log.debug("getProperty " + arg0);
        }
        return writer.getProperty(arg0);
    }

    public void setDefaultNamespace(String arg0) throws XMLStreamException {
        if (DEBUG_ENABLED) {
            log.debug("setDefaultNamespace " + arg0);
        }
        writer.setDefaultNamespace(arg0);
    }

    public void setNamespaceContext(NamespaceContext arg0) throws XMLStreamException {
        if (DEBUG_ENABLED) {
            log.debug("setNamespaceContext " + arg0);
        }
        writer.setNamespaceContext(arg0);
    }

    public void setPrefix(String arg0, String arg1) throws XMLStreamException {
        if (DEBUG_ENABLED) {
            log.debug("setPrefix " + arg0 + " " + arg1);
        }
        writer.setPrefix(arg0, arg1);
    }

    public void writeAttribute(String prefix, String uri, String localName, String value) 
    throws XMLStreamException {
        if (DEBUG_ENABLED) {
            log.debug("writeAttribute " + prefix + " " + uri + " " + localName + " " + value);
        }
        writer.writeAttribute(prefix, uri, localName, value);
    }

    public void writeAttribute(String arg0, String arg1, String arg2) 
        throws XMLStreamException {
        if (DEBUG_ENABLED) {
            log.debug("writeAttribute " + arg0 + " " + arg1 + " " + arg2 );
        }
        writer.writeAttribute(arg0, arg1, arg2);
    }

    public void writeAttribute(String arg0, String arg1) throws XMLStreamException {
        if (DEBUG_ENABLED) {
            log.debug("writeAttribute " + arg0 + " " + arg1);
        }
        writer.writeAttribute(arg0, arg1);
    }

    public void writeCData(String arg0) throws XMLStreamException {
        if (DEBUG_ENABLED) {
            log.debug("writeCData " + arg0 );
        }
        writer.writeCData(arg0);
    }

    public void writeCharacters(char[] arg0, int arg1, int arg2) 
    throws XMLStreamException {
        if (DEBUG_ENABLED) {
            log.debug("writeCharacters " + arg0 + " " + arg1 + " " + arg2 );
        }
        writer.writeCharacters(arg0, arg1, arg2);
    }

    public void writeCharacters(String arg0) throws XMLStreamException {
        if (DEBUG_ENABLED) {
            log.debug("writeCharacters " + arg0);
        }
        writer.writeCharacters(arg0);
    }

    public void writeComment(String arg0) throws XMLStreamException {
        if (DEBUG_ENABLED) {
            log.debug("writeComment " + arg0 );
        }
        writer.writeComment(arg0);
    }

    public void writeDefaultNamespace(String uri) throws XMLStreamException {
        if (DEBUG_ENABLED) {
            log.debug("writeDefaultNamespace (" + uri + ")"  );
        }
      
        if (isBuffering) {
            if (DEBUG_ENABLED) {
                log.debug("  Supress writeDefaultNamespace on top element");
            }
        } else if (depth >= 2) {
            writer.writeDefaultNamespace(uri);
        }
    }

    public void writeDTD(String arg0) throws XMLStreamException {
        if (DEBUG_ENABLED) {
            log.debug("writeDTD " + arg0  );
        }
        writer.writeDTD(arg0);
    }

    public void writeEmptyElement(String arg0, String arg1, String arg2) 
    throws XMLStreamException {
        if (DEBUG_ENABLED) {
            log.debug("writeEmptyElement" + arg0 + " " + arg1 + " " + arg2 );
        }
        writeStartElement(arg0, arg1, arg2);
        writeEndElement();
    }

    public void writeEmptyElement(String arg0, String arg1) throws XMLStreamException {
        if (DEBUG_ENABLED) {
            log.debug("writeEmptyElement " + arg0 + " " + arg1);
        }
        writeStartElement(arg0, arg1);
        writeEndElement();
    }

    public void writeEmptyElement(String arg0) throws XMLStreamException {
        if (DEBUG_ENABLED) {
            log.debug("writeEmptyElement " + arg0 );
        }
        writeStartElement(arg0);
        writeEndElement();
    }

    public void writeEndDocument() throws XMLStreamException {
        if (DEBUG_ENABLED) {
            log.debug("writeEndDocument " );
        }
        writer.writeEndDocument();
    }

    public void writeEndElement() throws XMLStreamException {
        depth--;
        if (DEBUG_ENABLED) {
            log.debug("writeEndElement "  );
        }
        if (depth != 0) {
            writer.writeEndElement();
        } else {
            if (DEBUG_ENABLED) {
                log.debug("  Suppress writeEndElement for the top element..but performing a flush");
            }
            writer.flush();
        }
    }

    public void writeEntityRef(String arg0) throws XMLStreamException {
        if (DEBUG_ENABLED) {
            log.debug("writeEntityRef " + arg0 );
        }
        writer.writeEntityRef(arg0);
    }

    public void writeNamespace(String prefix, String uri) throws XMLStreamException {
        if (DEBUG_ENABLED) {
            log.debug("writeNamespace (" + prefix + ") (" + uri + ")" );
        }
        
        // The namespaces are buffered while reading the root element.
        // They will be written out by writeCommands when the child
        // elements are encountered.
        if (isBuffering) {
            if ("".equals(prefix)) {
                if (DEBUG_ENABLED) {
                    log.debug("  Supress default write namespace on top element to avoid collision");
                }
            } else {
                List<String> command = new ArrayList<String>();
                command.add("writeNamespace");
                command.add(prefix);
                command.add(uri);
                bufferCommand(command);
            }
        } else if (depth == 2 && XSI_URI.equals(uri)  && "xsi".equals(prefix)) {
            // The operation element already has an xsi namespace declaration;
            // thus this one is redundant and only makes the message larger.
            if (DEBUG_ENABLED) {
                log.debug("  Supressing xsi namespace declaration on array item");
            }
        } else if (depth >= 2) {
            writer.writeNamespace(prefix, uri);
        } 
    }

    public void writeProcessingInstruction(String arg0, String arg1) 
    throws XMLStreamException {
        if (DEBUG_ENABLED) {
            log.debug("writeProcessingInstruction " + arg0 + " " + arg1  );
        }
        writer.writeProcessingInstruction(arg0, arg1);
    }

    public void writeProcessingInstruction(String arg0) throws XMLStreamException {
        if (DEBUG_ENABLED) {
            log.debug("writeProcessingInstruction " + arg0);
        }
        writer.writeProcessingInstruction(arg0);
    }

    public void writeStartDocument() throws XMLStreamException {
        if (DEBUG_ENABLED) {
            log.debug("writeStartDocument " );
        }
        writer.writeStartDocument();
    }

    public void writeStartDocument(String arg0, String arg1) throws XMLStreamException {
        if (DEBUG_ENABLED) {
            log.debug("writeStartDocument " + arg0 + " " + arg1 );
        }
        writer.writeStartDocument(arg0, arg1);
    }

    public void writeStartDocument(String arg0) throws XMLStreamException {
        if (DEBUG_ENABLED) {
            log.debug("writeStartDocument " + arg0  );
        }
        writer.writeStartDocument(arg0);
    }

    public void writeStartElement(String arg0, String arg1, String arg2) 
    throws XMLStreamException {
        if (DEBUG_ENABLED) {
            log.debug("writeStartElement " + arg0 + " " + arg1 + " " + arg2 );
        }
        depth++;
        if (depth > 1) {
            isBuffering = false;
        }
        
        // The start element is buffered when we encounter the
        // outermost element.  The buffered event is written 
        // (instead of the child element tag).
        if (isBuffering) {
            List<String> command = new ArrayList<String>();
            command.add("writeStartElement");
            command.add(arg0);
            command.add(arg1);
            command.add(arg2);
            bufferCommand(command);
        } else if (depth == 2) {
            writeCommands();
        } else {
            writer.writeStartElement(arg0, arg1, arg2);
        }
        
    }

    public void writeStartElement(String arg0, String arg1) throws XMLStreamException {
        if (DEBUG_ENABLED) {
            log.debug("writeStartElement " + arg0 + " " + arg1);
        }
        depth++;
        if (depth > 1) {
            isBuffering = false;
        }
        
        // The start element is buffered when we encounter the
        // outermost element.  The buffered event is written 
        // (instead of the child element tag).
        if (isBuffering) {
            List<String> command = new ArrayList<String>();
            command.add("writeStartElement");
            command.add(arg0);
            command.add(arg1);
            bufferCommand(command);
        } else if (depth == 2) {
            writeCommands();
        } else {
            writer.writeStartElement(arg0, arg1);
        }
    }

    public void writeStartElement(String arg0) throws XMLStreamException {
        
        if (DEBUG_ENABLED) {
            log.debug("writeStartElement " + arg0 );
        }
        depth++;
        if (depth > 1) {
            isBuffering = false;
        }
        
        
        // The start element is buffered when we encounter the
        // outermost element.  The buffered event is written 
        // (instead of the child element tag).
        if (isBuffering) {
            List<String> command = new ArrayList<String>();
            command.add("writeStartElement");
            command.add(arg0);
            bufferCommand(command);
        } else if (depth == 2) {
            writeCommands();
        } else {
            writer.writeStartElement(arg0);
        }
    }
    
    void bufferCommand(List<String> command) {
        if (DEBUG_ENABLED) {
            log.debug("  Buffering command " + command);
        }
        bufferedCommands.add(command);
    }
    
    void writeCommands() throws XMLStreamException {
        for (int i=0;i<bufferedCommands.size(); i++) {
            List<String> command = bufferedCommands.get(i);
            String m = command.get(0);
            if ("writeNamespace".equals(m)) {
                if (DEBUG_ENABLED) {
                    log.debug("  Sending buffered writeNamespace " + command.get(1) + 
                            "  " + command.get(2));
                }
                writer.writeNamespace(command.get(1), command.get(2));
            } else if ("writeStartElement".equals(m)) {
                int len = command.size();
                if (len == 2) {
                    if (DEBUG_ENABLED) {
                        log.debug("  Sending buffered writeStartElement " + command.get(1));
                    }
                    writer.writeStartElement(command.get(1));
                } else if (len ==3) {
                    if (DEBUG_ENABLED) {
                        log.debug("  Sending buffered writeStartElement " + command.get(1) +
                                "  " + command.get(2));
                    }
                    writer.writeStartElement(command.get(1), command.get(2));
                } else if (len == 4) {
                    if (DEBUG_ENABLED) {
                        log.debug("  Sending buffered writeStartElement " + command.get(1) +
                                "  " + command.get(2) + " " + command.get(3));
                    }
                    writer.writeStartElement(command.get(1), command.get(2), command.get(3));
                }
            }
        }
    }      
    
}
