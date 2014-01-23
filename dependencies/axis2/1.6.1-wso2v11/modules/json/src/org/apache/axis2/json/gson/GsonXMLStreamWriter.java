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

package org.apache.axis2.json.gson;

import com.google.gson.stream.JsonWriter;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.json.gson.factory.JSONType;
import org.apache.axis2.json.gson.factory.JsonConstant;
import org.apache.axis2.json.gson.factory.JsonObject;
import org.apache.axis2.json.gson.factory.XmlNode;
import org.apache.axis2.json.gson.factory.XmlNodeGenerator;
import org.apache.ws.commons.schema.XmlSchema;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;


public class GsonXMLStreamWriter implements XMLStreamWriter {

    private JsonWriter jsonWriter;

    /**
     * queue is used to keep the outgoing response structure according to the response XMLSchema
     */
    private Queue<JsonObject> queue = new LinkedList<JsonObject>();

    /**
     * This stacks use to process the outgoing response
     */
    private Stack<JsonObject> stack = new Stack<JsonObject>();
    private Stack<JsonObject> miniStack = new Stack<JsonObject>();

    private JsonObject flushObject;

    /**
     * topNestedArrayObj is use to keep the most top nested array object
     */
    private JsonObject topNestedArrayObj;

    /**
     * processedJsonObject stack is used to keep processed JsonObject for future reference
     */
    private Stack<JsonObject> processedJsonObjects = new Stack<JsonObject>();

    private List<XmlSchema> xmlSchemaList;

    /**
     * Element QName of outgoing message , which is get from the outgoing message context
     */
    private QName elementQName;

    /**
     * Intermediate representation of XmlSchema
     */
    private XmlNode mainXmlNode;

    private ConfigurationContext configContext;

    private XmlNodeGenerator xmlNodeGenerator;

    private boolean isProcessed;

    /**
     * This map is used to keep namespace uri with prefixes
     */
    private Map<String, String> uriPrefixMap = new HashMap<String, String>();


    public GsonXMLStreamWriter(JsonWriter jsonWriter, QName elementQName, List<XmlSchema> xmlSchemaList,
                               ConfigurationContext context) {
        this.jsonWriter = jsonWriter;
        this.elementQName = elementQName;
        this.xmlSchemaList = xmlSchemaList;
        this.configContext = context;
    }

    private void process() throws IOException {
        Object ob = configContext.getProperty(JsonConstant.XMLNODES);
        if (ob != null) {
            Map<QName, XmlNode> nodeMap = (Map<QName, XmlNode>) ob;
            XmlNode resNode = nodeMap.get(elementQName);
            if (resNode != null) {
                xmlNodeGenerator = new XmlNodeGenerator();
                queue = xmlNodeGenerator.getQueue(resNode);
            } else {
                xmlNodeGenerator = new XmlNodeGenerator(xmlSchemaList, elementQName);
                mainXmlNode = xmlNodeGenerator.getMainXmlNode();
                queue = xmlNodeGenerator.getQueue(mainXmlNode);
                nodeMap.put(elementQName, mainXmlNode);
                configContext.setProperty(JsonConstant.XMLNODES, nodeMap);
            }
        } else {
            Map<QName, XmlNode> newNodeMap = new HashMap<QName, XmlNode>();
            xmlNodeGenerator = new XmlNodeGenerator(xmlSchemaList, elementQName);
            mainXmlNode = xmlNodeGenerator.getMainXmlNode();
            queue = xmlNodeGenerator.getQueue(mainXmlNode);
            newNodeMap.put(elementQName, mainXmlNode);
            configContext.setProperty(JsonConstant.XMLNODES, newNodeMap);
        }
        isProcessed = true;
        this.jsonWriter.beginObject();
    }


    private void writeStartJson(JsonObject jsonObject) throws IOException {

        if (jsonObject.getType() == JSONType.OBJECT) {
            jsonWriter.name(jsonObject.getName());
        } else if (jsonObject.getType() == JSONType.ARRAY) {
            jsonWriter.name(jsonObject.getName());
            jsonWriter.beginArray();

        } else if (jsonObject.getType() == JSONType.NESTED_ARRAY) {
            jsonWriter.name(jsonObject.getName());
            jsonWriter.beginArray();
            jsonWriter.beginObject();
            if (topNestedArrayObj == null) {
                topNestedArrayObj = jsonObject;
                processedJsonObjects.push(jsonObject);
            }
        } else if (jsonObject.getType() == JSONType.NESTED_OBJECT) {
            jsonWriter.name(jsonObject.getName());
            jsonWriter.beginObject();

        }

    }

    private void writeEndJson(JsonObject endJson) throws IOException {
        if (endJson.getType() == JSONType.OBJECT) {
            // nothing write to json writer
        } else if (endJson.getType() == JSONType.ARRAY) {
            jsonWriter.endArray();
        } else if (endJson.getType() == JSONType.NESTED_ARRAY) {
            jsonWriter.endArray();
        } else if (endJson.getType() == JSONType.NESTED_OBJECT) {
            jsonWriter.endObject();
        }

    }


    private JsonObject popStack() {
        if (topNestedArrayObj == null || stack.peek().getType() == JSONType.NESTED_OBJECT
                || stack.peek().getType() == JSONType.NESTED_ARRAY) {
            return stack.pop();
        } else {
            processedJsonObjects.push(stack.peek());
            return stack.pop();
        }
    }

    private void fillMiniStack(JsonObject nestedJsonObject) {

        while (!processedJsonObjects.peek().getName().equals(nestedJsonObject.getName())) {
            miniStack.push(processedJsonObjects.pop());
        }
        processedJsonObjects.pop();
    }


    /**
     * Writes a start tag to the output.  All writeStartElement methods
     * open a new scope in the internal namespace context.  Writing the
     * corresponding EndElement causes the scope to be closed.
     *
     * @param localName local name of the tag, may not be null
     * @throws javax.xml.stream.XMLStreamException
     *
     */

    public void writeStartElement(String localName) throws XMLStreamException {
        if (!isProcessed) {
            try {
                process();
            } catch (IOException e) {
                throw new XMLStreamException("Error occours while write first begin object ");
            }
        }
        JsonObject stackObj = null;
        try {
            if (miniStack.isEmpty()) {
                if (!queue.isEmpty()) {
                    JsonObject queObj = queue.peek();
                    if (queObj.getName().equals(localName)) {
                        if (flushObject != null) {
                            if (topNestedArrayObj != null && flushObject.getType() == JSONType.NESTED_ARRAY
                                    && flushObject.getName().equals(topNestedArrayObj.getName())) {
                                topNestedArrayObj = null;
                                processedJsonObjects.clear();
                            }
                            popStack();
                            writeEndJson(flushObject);
                            flushObject = null;
                        }

                        if (topNestedArrayObj != null && (queObj.getType() == JSONType.NESTED_ARRAY ||
                                queObj.getType() == JSONType.NESTED_OBJECT)) {
                            processedJsonObjects.push(queObj);
                        }
                        writeStartJson(queObj);
                        stack.push(queue.poll());
                    } else if (!stack.isEmpty()) {
                        stackObj = stack.peek();
                        if (stackObj.getName().equals(localName)) {
                            if (stackObj.getType() == JSONType.NESTED_ARRAY) {
                                fillMiniStack(stackObj);
                                jsonWriter.beginObject();
                                processedJsonObjects.push(stackObj);
                            }
                            flushObject = null;
                        } else {
                            throw new XMLStreamException("Invalid Staring element");
                        }
                    }
                } else {
                    if (!stack.isEmpty()) {
                        stackObj = stack.peek();
                        if (stackObj.getName().equals(localName)) {
                            flushObject = null;
                            if (stackObj.getType() == JSONType.NESTED_ARRAY) {
                                fillMiniStack(stackObj);
                                jsonWriter.beginObject();
                                processedJsonObjects.push(stackObj);
                            }
                        } else {
                            throw new XMLStreamException("Invalid Staring element");
                        }
                    } else {
                        throw new XMLStreamException("Invalid Starting  element");
                    }
                }
            } else {
                JsonObject queObj = miniStack.peek();
                if (queObj.getName().equals(localName)) {
                    if (flushObject != null) {
                        popStack();
                        writeEndJson(flushObject);
                        flushObject = null;
                    }
                    if (topNestedArrayObj != null && (queObj.getType() == JSONType.NESTED_OBJECT
                            || queObj.getType() == JSONType.NESTED_ARRAY)) {
                        processedJsonObjects.push(queObj);
                    }
                    writeStartJson(queObj);
                    stack.push(miniStack.pop());
                } else if (!stack.isEmpty()) {
                    stackObj = stack.peek();
                    if (stackObj.getName().equals(localName)) {
                        flushObject = null;
                        if (stackObj.getType() == JSONType.NESTED_ARRAY) {
                            fillMiniStack(stackObj);
                            jsonWriter.beginObject();
                            processedJsonObjects.push(stackObj);
                        }
                    } else {
                        throw new XMLStreamException("Invalid Staring element");
                    }
                }
            }
        } catch (IOException e) {
            throw new XMLStreamException(" Json Writer throw an error");
        }
    }

    /**
     * Writes a start tag to the output
     *
     * @param namespaceURI the namespaceURI of the prefix to use, may not be null
     * @param localName    local name of the tag, may not be null
     * @throws javax.xml.stream.XMLStreamException
     *          if the namespace URI has not been bound to a prefix and
     *          javax.xml.stream.isRepairingNamespaces has not been set to true
     */

    public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
        writeStartElement(localName);
    }

    /**
     * Writes a start tag to the output
     *
     * @param localName    local name of the tag, may not be null
     * @param prefix       the prefix of the tag, may not be null
     * @param namespaceURI the uri to bind the prefix to, may not be null
     * @throws javax.xml.stream.XMLStreamException
     *
     */

    public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        writeStartElement(localName);
    }

    /**
     * Writes an empty element tag to the output
     *
     * @param namespaceURI the uri to bind the tag to, may not be null
     * @param localName    local name of the tag, may not be null
     * @throws javax.xml.stream.XMLStreamException
     *          if the namespace URI has not been bound to a prefix and
     *          javax.xml.stream.isRepairingNamespaces has not been set to true
     */

    public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
        throw new UnsupportedOperationException("Method is not implemented");
    }

    /**
     * Writes an empty element tag to the output
     *
     * @param prefix       the prefix of the tag, may not be null
     * @param localName    local name of the tag, may not be null
     * @param namespaceURI the uri to bind the tag to, may not be null
     * @throws javax.xml.stream.XMLStreamException
     *
     */

    public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        throw new UnsupportedOperationException("Method is not implemented");
    }

    /**
     * Writes an empty element tag to the output
     *
     * @param localName local name of the tag, may not be null
     * @throws javax.xml.stream.XMLStreamException
     *
     */

    public void writeEmptyElement(String localName) throws XMLStreamException {
        throw new UnsupportedOperationException("Method is not implemented");
    }

    /**
     * Writes an end tag to the output relying on the internal
     * state of the writer to determine the prefix and local name
     * of the event.
     *
     * @throws javax.xml.stream.XMLStreamException
     *
     */

    public void writeEndElement() throws XMLStreamException {
        if (!isProcessed) {
            try {
                process();
            } catch (IOException e) {
                throw new XMLStreamException("Error occours while write first begin object ");
            }
        }
        JsonObject stackObj = null;
        try {
            if (flushObject != null) {
                if (topNestedArrayObj != null && flushObject.getType() == JSONType.NESTED_ARRAY &&
                        flushObject.equals(topNestedArrayObj.getName())) {
                    topNestedArrayObj = null;
                    processedJsonObjects.clear();
                }
                popStack();
                writeEndJson(flushObject);
                flushObject = null;
                writeEndElement();
            } else {
                if (stack.peek().getType() == JSONType.ARRAY) {
                    flushObject = stack.peek();
                } else if (stack.peek().getType() == JSONType.NESTED_ARRAY) {
                    flushObject = stack.peek();
                    jsonWriter.endObject();
                } else {
                    stackObj = popStack();
                    writeEndJson(stackObj);
                }
            }
        } catch (IOException e) {
            throw new XMLStreamException("Json writer throw an exception");
        }
    }

    /**
     * Closes any start tags and writes corresponding end tags.
     *
     * @throws javax.xml.stream.XMLStreamException
     *
     */

    public void writeEndDocument() throws XMLStreamException {
        if (!isProcessed) {
            try {
                process();
            } catch (IOException e) {
                throw new XMLStreamException("Error occours while write first begin object ");
            }
        }
        if (queue.isEmpty() && stack.isEmpty()) {
            try {
                if (flushObject != null) {
                    writeEndJson(flushObject);
                }
                jsonWriter.endObject();
                jsonWriter.flush();
                jsonWriter.close();
            } catch (IOException e) {
                throw new XMLStreamException("JsonWriter threw an exception", e);
            }
        } else {
            throw new XMLStreamException("Invalid xml element");
        }
    }

    /**
     * Close this writer and free any resources associated with the
     * writer.  This must not close the underlying output stream.
     *
     * @throws javax.xml.stream.XMLStreamException
     *
     */

    public void close() throws XMLStreamException {
        try {
            jsonWriter.close();
        } catch (IOException e) {
            throw new RuntimeException("Error occur while closing JsonWriter");
        }
    }

    /**
     * Write any cached data to the underlying output mechanism.
     *
     * @throws javax.xml.stream.XMLStreamException
     *
     */
    
    public void flush() throws XMLStreamException {
    }

    /**
     * Writes an attribute to the output stream without
     * a prefix.
     *
     * @param localName the local name of the attribute
     * @param value     the value of the attribute
     * @throws IllegalStateException if the current state does not allow Attribute writing
     * @throws javax.xml.stream.XMLStreamException
     *
     */

    public void writeAttribute(String localName, String value) throws XMLStreamException {
        throw new UnsupportedOperationException("Method is not implemented");
    }

    /**
     * Writes an attribute to the output stream
     *
     * @param prefix       the prefix for this attribute
     * @param namespaceURI the uri of the prefix for this attribute
     * @param localName    the local name of the attribute
     * @param value        the value of the attribute
     * @throws IllegalStateException if the current state does not allow Attribute writing
     * @throws javax.xml.stream.XMLStreamException
     *                               if the namespace URI has not been bound to a prefix and
     *                               javax.xml.stream.isRepairingNamespaces has not been set to true
     */

    public void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws XMLStreamException {
        // GsonXMLStreamReader doesn't write Attributes
    }

    /**
     * Writes an attribute to the output stream
     *
     * @param namespaceURI the uri of the prefix for this attribute
     * @param localName    the local name of the attribute
     * @param value        the value of the attribute
     * @throws IllegalStateException if the current state does not allow Attribute writing
     * @throws javax.xml.stream.XMLStreamException
     *                               if the namespace URI has not been bound to a prefix and
     *                               javax.xml.stream.isRepairingNamespaces has not been set to true
     */

    public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
        throw new UnsupportedOperationException("Method is not implemented");
    }

    /**
     * Writes a namespace to the output stream
     * If the prefix argument to this method is the empty string,
     * "xmlns", or null this method will delegate to writeDefaultNamespace
     *
     * @param prefix       the prefix to bind this namespace to
     * @param namespaceURI the uri to bind the prefix to
     * @throws IllegalStateException if the current state does not allow Namespace writing
     * @throws javax.xml.stream.XMLStreamException
     *
     */

    public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
    }

    /**
     * Writes the default namespace to the stream
     *
     * @param namespaceURI the uri to bind the default namespace to
     * @throws IllegalStateException if the current state does not allow Namespace writing
     * @throws javax.xml.stream.XMLStreamException
     *
     */

    public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
        // do nothing
    }

    /**
     * Writes an xml comment with the data enclosed
     *
     * @param data the data contained in the comment, may be null
     * @throws javax.xml.stream.XMLStreamException
     *
     */

    public void writeComment(String data) throws XMLStreamException {
        throw new UnsupportedOperationException("Method is not implemented");
    }

    /**
     * Writes a processing instruction
     *
     * @param target the target of the processing instruction, may not be null
     * @throws javax.xml.stream.XMLStreamException
     *
     */

    public void writeProcessingInstruction(String target) throws XMLStreamException {
        throw new UnsupportedOperationException("Method is not implemented");
    }

    /**
     * Writes a processing instruction
     *
     * @param target the target of the processing instruction, may not be null
     * @param data   the data contained in the processing instruction, may not be null
     * @throws javax.xml.stream.XMLStreamException
     *
     */

    public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
        throw new UnsupportedOperationException("Method is not implemented");
    }

    /**
     * Writes a CData section
     *
     * @param data the data contained in the CData Section, may not be null
     * @throws javax.xml.stream.XMLStreamException
     *
     */

    public void writeCData(String data) throws XMLStreamException {
        throw new UnsupportedOperationException("Method is not implemented");
    }

    /**
     * Write a DTD section.  This string represents the entire doctypedecl production
     * from the XML 1.0 specification.
     *
     * @param dtd the DTD to be written
     * @throws javax.xml.stream.XMLStreamException
     *
     */

    public void writeDTD(String dtd) throws XMLStreamException {
        throw new UnsupportedOperationException("Method is not implemented");
    }

    /**
     * Writes an entity reference
     *
     * @param name the name of the entity
     * @throws javax.xml.stream.XMLStreamException
     *
     */

    public void writeEntityRef(String name) throws XMLStreamException {
        throw new UnsupportedOperationException("Method is not implemented");
    }

    /**
     * Write the XML Declaration. Defaults the XML version to 1.0, and the encoding to utf-8
     *
     * @throws javax.xml.stream.XMLStreamException
     *
     */

    public void writeStartDocument() throws XMLStreamException {
        if (!isProcessed) {
            try {
                process();
            } catch (IOException e) {
                throw new XMLStreamException("Error occur while write first begin object ");
            }
        }
    }

    /**
     * Write the XML Declaration. Defaults the XML version to 1.0
     *
     * @param version version of the xml document
     * @throws javax.xml.stream.XMLStreamException
     *
     */

    public void writeStartDocument(String version) throws XMLStreamException {
        throw new UnsupportedOperationException("Method is not implemented");
    }

    /**
     * Write the XML Declaration.  Note that the encoding parameter does
     * not set the actual encoding of the underlying output.  That must
     * be set when the instance of the XMLStreamWriter is created using the
     * XMLOutputFactory
     *
     * @param encoding encoding of the xml declaration
     * @param version  version of the xml document
     * @throws javax.xml.stream.XMLStreamException
     *          If given encoding does not match encoding
     *          of the underlying stream
     */

    public void writeStartDocument(String encoding, String version) throws XMLStreamException {
        if (!isProcessed) {
            xmlNodeGenerator.getMainXmlNode();
            queue = xmlNodeGenerator.getQueue(mainXmlNode);
            isProcessed = true;
        }
    }

    /**
     * Write text to the output
     *
     * @param text the value to write
     * @throws javax.xml.stream.XMLStreamException
     *
     */

    public void writeCharacters(String text) throws XMLStreamException {
        if (!isProcessed) {
            try {
                process();
            } catch (IOException e) {
                throw new XMLStreamException("Error occur while trying to write first begin object ");
            }
        }
        try {
            JsonObject peek = stack.peek();
            String valueType = peek.getValueType();
            if (valueType.equals("string")) {
                jsonWriter.value(text);
            } else if (valueType.equals("int")) {
                Number num = new Integer(text);
                jsonWriter.value(num);
            } else if (valueType.equals("long")) {
                jsonWriter.value(Long.valueOf(text));
            } else if (valueType.equals("double")) {
                jsonWriter.value(Double.valueOf(text));
            } else if (valueType.equals("boolean")) {
                jsonWriter.value(Boolean.valueOf(text));
            } else {
                jsonWriter.value(text);
            }
        } catch (IOException e) {
            throw new XMLStreamException("JsonWriter throw an exception");
        }
    }

    /**
     * Write text to the output
     *
     * @param text  the value to write
     * @param start the starting position in the array
     * @param len   the number of characters to write
     * @throws javax.xml.stream.XMLStreamException
     *
     */

    public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
        throw new UnsupportedOperationException("Method is not implemented");
    }

    /**
     * Gets the prefix the uri is bound to
     *
     * @return the prefix or null
     * @throws javax.xml.stream.XMLStreamException
     *
     */

    public String getPrefix(String uri) throws XMLStreamException {
        return uriPrefixMap.get(uri);
    }

    /**
     * Sets the prefix the uri is bound to.  This prefix is bound
     * in the scope of the current START_ELEMENT / END_ELEMENT pair.
     * If this method is called before a START_ELEMENT has been written
     * the prefix is bound in the root scope.
     *
     * @param prefix the prefix to bind to the uri, may not be null
     * @param uri    the uri to bind to the prefix, may be null
     * @throws javax.xml.stream.XMLStreamException
     *
     */

    public void setPrefix(String prefix, String uri) throws XMLStreamException {
        uriPrefixMap.put(uri, prefix);
    }

    /**
     * Binds a URI to the default namespace
     * This URI is bound
     * in the scope of the current START_ELEMENT / END_ELEMENT pair.
     * If this method is called before a START_ELEMENT has been written
     * the uri is bound in the root scope.
     *
     * @param uri the uri to bind to the default namespace, may be null
     * @throws javax.xml.stream.XMLStreamException
     *
     */

    public void setDefaultNamespace(String uri) throws XMLStreamException {
        //do nothing. 
    }

    /**
     * Sets the current namespace context for prefix and uri bindings.
     * This context becomes the root namespace context for writing and
     * will replace the current root namespace context.  Subsequent calls
     * to setPrefix and setDefaultNamespace will bind namespaces using
     * the context passed to the method as the root context for resolving
     * namespaces.  This method may only be called once at the start of
     * the document.  It does not cause the namespaces to be declared.
     * If a namespace URI to prefix mapping is found in the namespace
     * context it is treated as declared and the prefix may be used
     * by the StreamWriter.
     *
     * @param context the namespace context to use for this writer, may not be null
     * @throws javax.xml.stream.XMLStreamException
     *
     */

    public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
        throw new UnsupportedOperationException("Method is not implemented");
    }

    /**
     * Returns the current namespace context.
     *
     * @return the current NamespaceContext
     */

    public NamespaceContext getNamespaceContext() {
        return new GsonNamespaceConext();
    }

    /**
     * Get the value of a feature/property from the underlying implementation
     *
     * @param name The name of the property, may not be null
     * @return The value of the property
     * @throws IllegalArgumentException if the property is not supported
     * @throws NullPointerException     if the name is null
     */

    public Object getProperty(String name) throws IllegalArgumentException {
        return null;
    }
}
