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

package org.apache.axis2.jaxws.message;

import org.apache.axiom.om.OMDataSourceExt;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.jaxws.message.factory.BlockFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.WebServiceException;

/**
 * Block A Block represents an xml element and associated sub-tree. The name of the element must be
 * defined by a root element in a schema. All prefixes within the subtree must correspond to
 * namespace declarations defined within the tree. Many specifications refer to this as a "embedded
 * document" or "xml block".  I chose the term, block, for simplicity.
 * <p/>
 * The block can be exposed as: * BusinessObject * StAX object
 * <p/>
 * Note that the whole Message can also be thought of as a Block.  Thus a Message can be createFrom
 * a Block and written as a Block.
 * <p/>
 * In addition, each of the accessors has a consume parameter.  If consume is true, the Block is no
 * longer valid after the message is called. (i.e. the implementation does not need to cache the
 * information)
 */
public interface Block extends OMDataSourceExt {

    /**
     * Get a reference to the Business Object represented by this Block
     *
     * @param consume true if this is the last request on the block.
     * @return Object (JAXB, String etc.)
     * @throws XMLStreamException
     * @throws WebServiceException
     */
    public Object getBusinessObject(boolean consume) throws XMLStreamException, WebServiceException;

    /**
     * GetBusinesContext Some business objects have an associated context object (i.e. JAXBContext)
     *
     * @return Context Object or null
     */
    public Object getBusinessContext();

    /**
     * Get the XMLStreamReader represented by this Block
     *
     * @param consume true if this is the last request on the block.
     * @return XMLStreamReader
     * @throws XMLStreamException
     */
    public XMLStreamReader getXMLStreamReader(boolean consume)
            throws XMLStreamException, WebServiceException;

    /**
     * Get the OMElement represented by this Block. This call always consumes the block because you are
     * taking control of the underlying OM
     *
     * @return
     * @throws XMLStreamException
     * @throws WebServiceException
     */
    public OMElement getOMElement() throws XMLStreamException, WebServiceException;

    /**
     * Write out the Block
     *
     * @param writer  XMLStreamWriter
     * @param consume true if this is the last request on the block.
     * @throws XMLStreamException
     * @trhows WebServiceException
     */
    public void outputTo(XMLStreamWriter writer, boolean consume)
            throws XMLStreamException, WebServiceException;

    /**
     * isConsumed Return true if the block is consumed.  Once consumed, the information in the block
     * is no longer available.
     *
     * @return true if the block is consumed (a method was called with consume=true)
     */
    public boolean isConsumed();

    /**
     * Get a traceString...the trace string dumps the contents of the Block without forcing an
     * underlying ill-performant transformation of the message.
     *
     * @return String containing trace information
     * @boolean indent String containing indent characters
     */
    public String traceString(String indent);

    /**
     * @return If QName is available without doing an expensive parse of the business object, then
     *         return true Otherwise return false Note: This method should be used in situations where
     *         it would be nice to know the qname (like logging or a special check) but we don't want
     *         to cause an ill-performant parse.
     */
    public boolean isQNameAvailable();

    /**
     * Get the QName (namespace, localpart) of the Block.  Do not depend on prefix being set correctly.
     * Asking for the QName can cause a performant hit.
     *
     * @return QName of the block
     * @throw WebServiceException
     * @see isQNameAvailable
     */
    public QName getQName() throws WebServiceException;

    /**
     * Get BlockFactory
     *
     * @return BlockFactory that created the Block
     */
    public BlockFactory getBlockFactory();

    /**
     * Get the Message associated with this block
     *
     * @return Message
     */
    public Message getParent();

    /**
     * Set the Message associated with this block (This method is intended to be called by the
     * Message Implementation only)
     *
     * @param parent
     */
    public void setParent(Message parent);

    /**
     * @return true if data is always an element; false if possibly mixed content or multiple
     *         elements
     */
    public boolean isElementData();
}
