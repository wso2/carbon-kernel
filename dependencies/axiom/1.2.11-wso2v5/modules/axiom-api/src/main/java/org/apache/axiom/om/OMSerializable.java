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
import javax.xml.stream.XMLStreamWriter;

/**
 * Information item that can be serialized (written to an XML stream writer) and
 * deserialized (retrieved from an XML parser) as a unit.
 * This is the common super-interface for {@link OMDocument} and {@link OMNode}.
 * Note that {@link OMAttribute} and {@link OMNamespace} are information items that don't
 * match the definition of this interface because they can only be read from the parser
 * as part of a larger unit, namely an element.
 * <p>
 * In accordance with the definition given above, this interface declares two sets of methods:
 * <ul>
 *   <li>Methods allowing to control whether the information item has been completely built,
 *       i.e. whether all events corresponding to the information item have been retrieved
 *       from the parser.</li>
 *   <li>Methods to write the StAX events corresponding to the information item to an
 *       {@link XMLStreamWriter}.</li>
 * </ul>
 */
public interface OMSerializable {
    /**
     * Returns the OMFactory that created this object
     */
    OMFactory getOMFactory();

    /**
     * Indicates whether parser has parsed this information item completely or not. If some info are
     * not available in the item, one has to check this attribute to make sure that, this item has been
     * parsed completely or not.
     *
     * @return Returns boolean.
     */
    boolean isComplete();

    /** Builds itself. */
    void build();

    /**
     * If a builder and parser is associated with the node, it is closed.
     * @param build if true, the object is built first before closing the builder/parser
     */
    void close(boolean build);

    /**
     * Serializes the information item with caching. This method has the same effect as
     * {@link #serialize(XMLStreamWriter, boolean)} with <code>cache</code> set to
     * <code>true</code>.
     *
     * @param xmlWriter
     * @throws XMLStreamException
     */
    void serialize(XMLStreamWriter xmlWriter) throws XMLStreamException;

    /**
     * Serializes the information item without caching. This method has the same effect as
     * {@link #serialize(XMLStreamWriter, boolean)} with <code>cache</code> set to
     * <code>false</code>.
     *
     * @param xmlWriter
     * @throws XMLStreamException
     */
    void serializeAndConsume(XMLStreamWriter xmlWriter) throws XMLStreamException;
    
    /**
     * Serializes the information item.
     * 
     * @param xmlWriter
     * @param cache indicates if caching should be enabled
     * @throws XMLStreamException
     */
    void serialize(XMLStreamWriter xmlWriter, boolean cache) throws XMLStreamException;
}
