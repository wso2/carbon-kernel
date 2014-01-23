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

package org.apache.axiom.om.impl.serialize;

import org.apache.axiom.ext.stax.datahandler.DataHandlerReader;
import org.apache.axiom.ext.stax.datahandler.DataHandlerWriter;
import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMSerializer;
import org.apache.axiom.om.impl.OMStAXWrapper;
import org.apache.axiom.om.impl.util.OMSerializerUtil;
import org.apache.axiom.util.stax.XMLStreamReaderUtils;
import org.apache.axiom.util.stax.XMLStreamWriterUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Class StreamingOMSerializer */
public class StreamingOMSerializer implements XMLStreamConstants, OMSerializer {
    
    static Log log = LogFactory.getLog(StreamingOMSerializer.class);
    private static final boolean DEBUG_ENABLED = log.isDebugEnabled();
    
    private static int namespaceSuffix = 0;
    public static final String NAMESPACE_PREFIX = "ns";

    private static final String XSI_URI = "http://www.w3.org/2001/XMLSchema-instance";
    private static final String XSI_LOCAL_NAME = "type";
    /*
    * The behavior of the serializer is such that it returns when it encounters the
    * starting element for the second time. The depth variable tracks the depth of the
    * serilizer and tells it when to return.
    * Note that it is assumed that this serialization starts on an Element.
    */

    /** Field depth */
    private int depth = 0;
    
    private DataHandlerReader dataHandlerReader;
    private DataHandlerWriter dataHandlerWriter;

    /**
     * Method serialize.
     *
     * @param reader
     * @param writer
     * @throws XMLStreamException
     */
    public void serialize(XMLStreamReader reader, XMLStreamWriter writer)
            throws XMLStreamException {
        serialize(reader, writer, true);
    }
    
    /**
     * @param reader
     * @param writer
     * @param startAtNext indicate if reading should start at next event or current event
     * @throws XMLStreamException
     */
    public void serialize(XMLStreamReader reader, XMLStreamWriter writer, boolean startAtNext)
            throws XMLStreamException {
        
        dataHandlerReader = XMLStreamReaderUtils.getDataHandlerReader(reader);
        dataHandlerWriter = XMLStreamWriterUtils.getDataHandlerWriter(writer);
        
        if (reader instanceof OMStAXWrapper) {
            int event = reader.getEventType();
            if (event <= 0 ||
                event == XMLStreamReader.PROCESSING_INSTRUCTION ||
                event == XMLStreamReader.START_DOCUMENT) {
                // Since we are serializing an entire document,
                // enable the the optimized DataSource events.
                // This will allow OMDataSource elements to be serialized
                // directly without expansion.
                if (log.isDebugEnabled()) {
                    log.debug("Enable OMDataSource events while serializing this document");
                }
                ((OMStAXWrapper) reader).enableDataSourceEvents(true);
            }
        }
        try {
            serializeNode(reader, writer, startAtNext);
        } finally {
            if (reader instanceof OMStAXWrapper) {
                ((OMStAXWrapper) reader).enableDataSourceEvents(false);
            }
        }
        
    }

    /**
     * Method serializeNode.
     *
     * @param reader
     * @param writer
     * @throws XMLStreamException
     */
    protected void serializeNode(XMLStreamReader reader, XMLStreamWriter writer) 
        throws XMLStreamException {
        serializeNode(reader, writer, true);
    }
    protected void serializeNode(XMLStreamReader reader, 
                                 XMLStreamWriter writer, 
                                 boolean startAtNext)
            throws XMLStreamException {
        // TODO We get the StAXWriter at this point and uses it hereafter 
        // assuming that this is the only entry point to this class.
        // If there can be other classes calling methodes of this we might 
        // need to change methode signatures to OMOutputer
        
        // If not startAtNext, then seed the processing with the current element.
        boolean useCurrentEvent = !startAtNext;
        
        while (reader.hasNext() || useCurrentEvent) {
            int event = 0;
            OMDataSource ds = null;
            if (useCurrentEvent) {
                event = reader.getEventType();
                useCurrentEvent = false;
            } else {
                event = reader.next(); 
            }
            
            // If the reader is exposing a DataSourc
            // for this event, then simply serialize the
            // DataSource
            if (reader instanceof OMStAXWrapper) {
                ds = ((OMStAXWrapper) reader).getDataSource();
            }
            if (ds != null) {
                ds.serialize(writer);
            } else {
                switch (event) {
                case START_ELEMENT:
                    serializeElement(reader, writer);
                    depth++;
                    break;
                case ATTRIBUTE:
                    serializeAttributes(reader, writer);
                    break;
                case CHARACTERS:
                    if (dataHandlerReader != null && dataHandlerReader.isBinary()) {
                        serializeDataHandler();
                        break;
                    }
                    // Fall through
                case SPACE:
                    serializeText(reader, writer);
                    break;
                case COMMENT:
                    serializeComment(reader, writer);
                    break;
                case CDATA:
                    serializeCData(reader, writer);
                    break;
                case PROCESSING_INSTRUCTION:
                    serializeProcessingInstruction(reader, writer);
                    break;
                case END_ELEMENT:
                    serializeEndElement(writer);
                    depth--;
                    break;
                case START_DOCUMENT:
                    depth++; //if a start document is found then increment the depth
                    break;
                case END_DOCUMENT:
                    if (depth != 0) depth--;  //for the end document - reduce the depth
                    try {
                        serializeEndElement(writer);
                    } catch (Exception e) {
                        //TODO: log exceptions
                    }
                }
            }
            if (depth == 0) {
                break;
            }
        }
    }

    /**
     * @param reader
     * @param writer
     * @throws XMLStreamException
     */
    protected void serializeElement(XMLStreamReader reader,
                                    XMLStreamWriter writer)
            throws XMLStreamException {
        

        // Note: To serialize the start tag, we must follow the order dictated by the JSR-173 (StAX) specification.
        // Please keep this code in sync with the code in OMSerializerUtil.serializeStartpart

        // The algorithm is:
        // ... generate writeStartElement
        //
        // ... generate setPrefix/setDefaultNamespace for each namespace declaration if the prefix is unassociated.
        // ... generate setPrefix/setDefaultNamespace if the prefix of the element is unassociated
        // ... generate setPrefix/setDefaultNamespace for each unassociated prefix of the attributes.
        //
        // ... generate writeNamespace/writerDefaultNamespace for the new namespace declarations determine during the "set" processing
        // ... generate writeAttribute for each attribute

        List<String> writePrefixList = new ArrayList<String>();
        List<String> writeNSList = new ArrayList<String>();

        // Get the prefix and namespace of the element.  "" and null are identical.
        String ePrefix = reader.getPrefix();
        ePrefix = (ePrefix != null && ePrefix.isEmpty()) ? null : ePrefix;
        String eNamespace = reader.getNamespaceURI();
        eNamespace = (eNamespace != null && eNamespace.isEmpty()) ? null : eNamespace;
        
        // Write the startElement if required
        String readerLocalName = reader.getLocalName();
        if (eNamespace != null) {
            if (ePrefix == null) {
                if (!OMSerializerUtil.isAssociated("", eNamespace, writer)) {
                    writePrefixList.add("");
                    writeNSList.add(eNamespace);
                }   
                
                writer.writeStartElement("", readerLocalName, eNamespace);
            } else {
                
                if (!OMSerializerUtil.isAssociated(ePrefix, eNamespace, writer)) {
                    writePrefixList.add(ePrefix);
                    writeNSList.add(eNamespace);
                }
                
                writer.writeStartElement(ePrefix, readerLocalName, eNamespace);
            }
        } else {
            writer.writeStartElement(readerLocalName);
        }

        // Generate setPrefix for the namespace declarations
        int count = reader.getNamespaceCount();
        for (int i = 0; i < count; i++) {
            String prefix = reader.getNamespacePrefix(i);
            prefix = (prefix != null && prefix.isEmpty()) ? null : prefix;
            String namespace = reader.getNamespaceURI(i);
            namespace = (namespace != null && namespace.isEmpty()) ? null : namespace;

            String newPrefix = OMSerializerUtil.generateSetPrefix(prefix, namespace, writer, false);
            // If this is a new association, remember it so that it can written out later
            if (newPrefix != null) {
                if (!writePrefixList.contains(newPrefix)) {
                    writePrefixList.add(newPrefix);
                    writeNSList.add(namespace);
                }
            }
        }

        // Generate setPrefix for the element
        // If the prefix is not associated with a namespace yet, remember it so that we can
        // write out a namespace declaration
        String newPrefix = OMSerializerUtil.generateSetPrefix(ePrefix, eNamespace, writer, false);
        // If this is a new association, remember it so that it can written out later
        if (newPrefix != null) {
            if (!writePrefixList.contains(newPrefix)) {
                writePrefixList.add(newPrefix);
                writeNSList.add(eNamespace);
            }
        }

        // Now Generate setPrefix for each attribute
        count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            String prefix = reader.getAttributePrefix(i);
            prefix = (prefix != null && prefix.isEmpty()) ? null : prefix;
            String namespace = reader.getAttributeNamespace(i);
            namespace = (namespace != null && namespace.isEmpty()) ? null : namespace;

            // Default prefix referencing is not allowed on an attribute
            if (prefix == null && namespace != null) {
                String writerPrefix = writer.getPrefix(namespace);
                writerPrefix =
                        (writerPrefix != null && writerPrefix.isEmpty()) ? null : writerPrefix;
                prefix = (writerPrefix != null) ?
                        writerPrefix :
                        generateUniquePrefix(writer.getNamespaceContext());
            }
            newPrefix = OMSerializerUtil.generateSetPrefix(prefix, namespace, writer, true);
            // If the prefix is not associated with a namespace yet, remember it so that we can
            // write out a namespace declaration
            if (newPrefix != null) {
                if (!writePrefixList.contains(newPrefix)) {
                    writePrefixList.add(newPrefix);
                    writeNSList.add(namespace);
                }
            }
        }
        
        // Now Generate setPrefix for each prefix referenced in an xsi:type
        // For example xsi:type="p:dataType"
        // The following code will make sure that setPrefix is called for "p".
        count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            String namespace = reader.getAttributeNamespace(i);
            namespace = (namespace != null && namespace.isEmpty()) ? null : namespace;
            String localName = reader.getAttributeLocalName(i);
            
            if  (XSI_URI.equals(namespace) &&
                XSI_LOCAL_NAME.equals(localName)) {
                String value = reader.getAttributeValue(i);
                if (DEBUG_ENABLED) {
                    log.debug("The value of xsi:type is " + value);
                }
                if (value != null) {
                    value = value.trim();
                    if (value.indexOf(':') > 0) {
                        String refPrefix = value.substring(0, value.indexOf(':'));
                        String refNamespace = reader.getNamespaceURI(refPrefix);
                        if (refNamespace != null && refNamespace.length() > 0) {
                            
                            newPrefix = OMSerializerUtil.generateSetPrefix(refPrefix, 
                                            refNamespace, 
                                            writer, 
                                            true);
                            // If the prefix is not associated with a namespace yet, remember it so that we can
                            // write out a namespace declaration
                            if (newPrefix != null && !writePrefixList.contains(newPrefix)) {
                                    writePrefixList.add(newPrefix);
                                    writeNSList.add(refNamespace);
                                }
                        }
                        
                    }
                }
            }
        }

        // Now write out the list of namespace declarations in this list that we constructed
        // while doing the "set" processing.
        if (writePrefixList != null) {
            for (int i = 0; i < writePrefixList.size(); i++) {
                String prefix = writePrefixList.get(i);
                String namespace = writeNSList.get(i);
                if (prefix != null) {
                    if (namespace == null) {
                        writer.writeNamespace(prefix, "");
                    } else {
                        writer.writeNamespace(prefix, namespace);
                    }
                } else {
                    writer.writeDefaultNamespace(namespace);
                }
            }
        }

        // Now write the attributes
        count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            String prefix = reader.getAttributePrefix(i);
            prefix = (prefix != null && prefix.isEmpty()) ? null : prefix;
            String namespace = reader.getAttributeNamespace(i);
            namespace = (namespace != null && namespace.isEmpty()) ? null : namespace;


            if (prefix == null && namespace != null) {
                // Default namespaces are not allowed on an attribute reference.
                // Earlier in this code, a unique prefix was added for this case...now obtain and use it
                prefix = writer.getPrefix(namespace);
                //XMLStreamWriter doesn't allow for getPrefix to know whether you're asking for the prefix
                //for an attribute or an element. So if the namespace matches the default namespace getPrefix will return
                //the empty string, as if it were an element, in all cases (even for attributes, and even if 
                //there was a prefix specifically set up for this), which is not the desired behavior.
                //Since the interface is base java, we can't fix it where we need to (by adding an attr boolean to 
                //XMLStreamWriter.getPrefix), so we hack it in here...
                if (prefix == null || prefix.isEmpty()) {
                    for (int j = 0; j < writePrefixList.size(); j++) {
                        if (namespace.equals(writeNSList.get(j))) {
                            prefix = writePrefixList.get(j);
                        }
                    }
                }
            } else if (namespace != null && !prefix.equals("xml")) {
                // Use the writer's prefix if it is different, but if the writers 
                // prefix is empty then do not replace because attributes do not
                // default to the default namespace like elements do.
                String writerPrefix = writer.getPrefix(namespace);
                if (!prefix.equals(writerPrefix) && writerPrefix.isEmpty()) {
                    prefix = writerPrefix;
                }
            }
            if (namespace != null) {
                // Qualified attribute
                writer.writeAttribute(prefix, namespace,
                                      reader.getAttributeLocalName(i),
                                      reader.getAttributeValue(i));
            } else {
                // Unqualified attribute
                writer.writeAttribute(reader.getAttributeLocalName(i),
                                      reader.getAttributeValue(i));
            }
        }
    }

    /**
     * Method serializeEndElement.
     *
     * @param writer
     * @throws XMLStreamException
     */
    protected void serializeEndElement(XMLStreamWriter writer)
            throws XMLStreamException {
        writer.writeEndElement();
    }

    /**
     * @param reader
     * @param writer
     * @throws XMLStreamException
     */
    protected void serializeText(XMLStreamReader reader,
                                 XMLStreamWriter writer)
            throws XMLStreamException {
        writer.writeCharacters(reader.getText());
    }

    /**
     * Method serializeCData.
     *
     * @param reader
     * @param writer
     * @throws XMLStreamException
     */
    protected void serializeCData(XMLStreamReader reader,
                                  XMLStreamWriter writer)
            throws XMLStreamException {
        writer.writeCData(reader.getText());
    }

    /**
     * Method serializeComment.
     *
     * @param reader
     * @param writer
     * @throws XMLStreamException
     */
    protected void serializeComment(XMLStreamReader reader,
                                    XMLStreamWriter writer)
            throws XMLStreamException {
        writer.writeComment(reader.getText());
    }

    /**
     * Method serializeProcessingInstruction.
     *
     * @param reader
     * @param writer
     * @throws XMLStreamException
     */
    protected void serializeProcessingInstruction(XMLStreamReader reader,
                                                  XMLStreamWriter writer)
            throws XMLStreamException {
        writer.writeProcessingInstruction(reader.getPITarget(), reader.getPIData());
    }

    /**
     * @param reader
     * @param writer
     * @throws XMLStreamException
     */
    protected void serializeAttributes(XMLStreamReader reader,
                                       XMLStreamWriter writer)
            throws XMLStreamException {
        int count = reader.getAttributeCount();
        String prefix = null;
        String namespaceName = null;
        String writerPrefix = null;
        for (int i = 0; i < count; i++) {
            prefix = reader.getAttributePrefix(i);
            namespaceName = reader.getAttributeNamespace(i);
            /*
               Some parser implementations return null for the unqualified namespace.
               But getPrefix(null) will throw an exception (according to the XMLStreamWriter
               javadoc. We guard against this by using "" for the unqualified namespace. 
            */
            namespaceName =(namespaceName == null) ? "" : namespaceName;

            // Using getNamespaceContext should be avoided when not necessary.
            // Some parser implementations construct a new NamespaceContext each time it is invoked.
            // writerPrefix = writer.getNamespaceContext().getPrefix(namespaceName);
            writerPrefix = writer.getPrefix(namespaceName);

            if (!namespaceName.isEmpty()) {
                //prefix has already being declared but this particular attrib has a
                //no prefix attached. So use the prefix provided by the writer
                if (writerPrefix != null && (prefix == null || prefix.equals(""))) {
                    writer.writeAttribute(writerPrefix, namespaceName,
                                          reader.getAttributeLocalName(i),
                                          reader.getAttributeValue(i));

                    //writer prefix is available but different from the current
                    //prefix of the attrib. We should be decalring the new prefix
                    //as a namespace declaration
                } else if (prefix != null && !prefix.isEmpty() && !prefix.equals(writerPrefix)) {
                    writer.writeNamespace(prefix, namespaceName);
                    writer.writeAttribute(prefix, namespaceName,
                                          reader.getAttributeLocalName(i),
                                          reader.getAttributeValue(i));

                    //prefix is null (or empty), but the namespace name is valid! it has not
                    //being written previously also. So we need to generate a prefix
                    //here
                } else {
                    prefix = generateUniquePrefix(writer.getNamespaceContext());
                    writer.writeNamespace(prefix, namespaceName);
                    writer.writeAttribute(prefix, namespaceName,
                                          reader.getAttributeLocalName(i),
                                          reader.getAttributeValue(i));
                }
            } else {
                //empty namespace is equal to no namespace!
                writer.writeAttribute(reader.getAttributeLocalName(i),
                                      reader.getAttributeValue(i));
            }


        }
    }

    /**
     * Generates a unique namespace prefix that is not in the scope of the NamespaceContext
     *
     * @param nsCtxt
     * @return string
     */
    private String generateUniquePrefix(NamespaceContext nsCtxt) {
        String prefix = NAMESPACE_PREFIX + namespaceSuffix++;
        //null should be returned if the prefix is not bound!
        while (nsCtxt.getNamespaceURI(prefix) != null) {
            prefix = NAMESPACE_PREFIX + namespaceSuffix++;
        }

        return prefix;
    }

    /**
     * Method serializeNamespace.
     *
     * @param prefix
     * @param URI
     * @param writer
     * @throws XMLStreamException
     */
    private void serializeNamespace(String prefix,
                                    String URI,
                                    XMLStreamWriter writer)
            throws XMLStreamException {
        String prefix1 = writer.getPrefix(URI);
        if (prefix1 == null) {
            writer.writeNamespace(prefix, URI);
            writer.setPrefix(prefix, URI);
        }
    }
    
    private void serializeDataHandler() throws XMLStreamException {
        try {
            if (dataHandlerReader.isDeferred()) {
                dataHandlerWriter.writeDataHandler(dataHandlerReader.getDataHandlerProvider(),
                        dataHandlerReader.getContentID(), dataHandlerReader.isOptimized());
            } else {
                dataHandlerWriter.writeDataHandler(dataHandlerReader.getDataHandler(),
                        dataHandlerReader.getContentID(), dataHandlerReader.isOptimized());
            }
        } catch (IOException ex) {
            throw new XMLStreamException("Error while reading data handler", ex);
        }
    }
}
