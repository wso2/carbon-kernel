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

package org.apache.axis2.jaxws.message.impl;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.RolePlayer;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.XMLFault;
import org.apache.axis2.jaxws.message.factory.BlockFactory;

import javax.jws.soap.SOAPBinding.Style;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.WebServiceException;
import java.util.List;
import java.util.Set;

/**
 * XMLSpine
 * <p/>
 * An XMLSpine is an optimized form of the xml part of the message. Currently there is only one
 * implementation (XMLSpineImpl).
 *
 * @see XMLSpineImpl for more details
 */
interface XMLSpine {
    /**
     * Get the protocol for this Message (soap11, soap12, etc.)
     *
     * @return Protocol
     */
    public Protocol getProtocol();

    /**
     * Write out the Message
     *
     * @param writer  XMLStreamWriter
     * @param consume true if this is the last request on the block.
     * @throws WebServiceException
     */
    public void outputTo(XMLStreamWriter writer, boolean consume)
            throws XMLStreamException, WebServiceException;

    /**
     * Get the XMLStreamReader represented by this Message for the xml part
     *
     * @param consume true if this is the last request on the Message
     * @return XMLStreamReader
     * @throws WebServiceException
     * @throws XMLStreamException
     */
    public XMLStreamReader getXMLStreamReader(boolean consume) throws WebServiceException;

    /** @return the Style (document or rpc) */
    public Style getStyle();

    /** @return the QName of the operation element if Style.rpc.  Otherwise null */
    public QName getOperationElement() throws WebServiceException;

    /**
     * Set the operation element qname.  The operation qname is only used if Style.rpc
     *
     * @param operationQName
     */
    public void setOperationElement(QName operationQName) throws WebServiceException;

    /**
     * isConsumed Return true if the part is consumed.  Once consumed, the information in the part
     * is no longer available.
     *
     * @return true if the block is consumed (a method was called with consume=true)
     */
    public boolean isConsumed();

    /**
     * Determines whether the XMLPart represents a Fault
     *
     * @return true if the message represents a fault
     */
    public boolean isFault() throws WebServiceException;

    /**
     * If the XMLPart represents a fault, an XMLFault is returned which describes the fault in a
     * protocol agnostic manner
     *
     * @return the XMLFault object or null
     * @see XMLFault
     */
    public XMLFault getXMLFault() throws WebServiceException;

    /**
     * Change the XMLPart so that it represents the fault described by XMLFault
     *
     * @param xmlfault
     * @see XMLFault
     */
    public void setXMLFault(XMLFault xmlFault) throws WebServiceException;

    /**
     * getAsOMElement Get the xml part as a read/write OM
     *
     * @return OMElement (probably OM SOAPEnvelope)
     * @throws WebServiceException
     */
    public OMElement getAsOMElement() throws WebServiceException;

    /**
     * getNumBodyBlocks
     *
     * @return number of body blocks
     * @throws WebServiceException
     */
    public int getNumBodyBlocks() throws WebServiceException;
    
    /**
     * getBodyBlockQNames 
     * Calling this method will cache the OM.  Avoid it in performant situations.
     *
     * @return List of QNames
     * @throws WebServiceException
     */
    public List<QName> getBodyBlockQNames() throws WebServiceException;

    /**
     * getBodyBlock Get the body block at the specificed index. The BlockFactory and object 
     * context are passed in to help create the proper kind of block. Calling this method 
     * will cache the OM.
     * Avoid it in performant situations.
     *
     * @param index
     * @param context
     * @param blockFactory
     * @return Block or null
     * @throws WebServiceException
     * @see getBodyBlock
     */
    public Block getBodyBlock(int index, Object context, BlockFactory blockFactory)
            throws WebServiceException;

    /**
     * getBodyBlock Get the single Body Block. The BlockFactory and object context are passed in to
     * help create the proper kind of block. This method should only be invoked when it is known
     * that there is zero or one block.
     *
     * @param index
     * @param context
     * @param blockFactory
     * @return Block or null
     * @throws WebServiceException
     */
    public Block getBodyBlock(Object context, BlockFactory blockFactory)
            throws WebServiceException;

    /**
     * setBodyBlock Set the block at the specified index Once set, the Message owns the block.  You
     * must use the getBodyBlock method to access it.
     *
     * @param index
     * @param block
     * @throws WebServiceException
     */
    public void setBodyBlock(int index, Block block) throws WebServiceException;

    /**
     * setBodyBlock Set this as block as the single block for the message.
     *
     * @param index
     * @param block
     * @throws WebServiceException
     */
    public void setBodyBlock(Block block) throws WebServiceException;

    /**
     * removeBodyBlock Removes the indicated BodyBlock
     *
     * @param index
     * @throws WebServiceException
     */
    public void removeBodyBlock(int index) throws WebServiceException;


    /**
     * getNumHeaderBlocks
     *
     * @return number of header blocks
     * @throws WebServiceException
     */
    public int getNumHeaderBlocks() throws WebServiceException;

    /**
     * getHeaderBlock Get the first header block with the specified name.
     * The BlockFactory and object
     * context are passed in to help create the proper kind of block.
     *
     * @param namespace
     * @param localPart
     * @param context
     * @param blockFactory
     * @return Block
     * @throws WebServiceException
     */
    public Block getHeaderBlock(String namespace, String localPart,
                                Object context,
                                BlockFactory blockFactory)
            throws WebServiceException;
    
    /**
     * getHeaderBlock 
     * Get the header blocks with the specified name.
     * The BlockFactory and object
     * context are passed in to help create the proper kind of block.
     *
     * @param namespace
     * @param localPart
     * @param context
     * @param blockFactory
     * @param rolePlayer(if set) indicates the Roles that should be considered
     * @return Block
     * @throws WebServiceException
     */
    public List<Block> getHeaderBlocks(String namespace, 
                                       String localPart,
                                       Object context,
                                       BlockFactory blockFactory, 
                                       RolePlayer rolePlayer)
            throws WebServiceException;

    /**
     * setHeaderBlock 
     * replaces the first existing header block with this new block.  If there is no
     * existing header block, one is added to the end of the headers
     *
     * @param namespace
     * @param localPart
     * @param block
     * @throws WebServiceException
     */
    public void setHeaderBlock(String namespace, String localPart, Block block)
            throws WebServiceException;

    /**
     * appendHeaderBlock 
     * Append the block to the list of header blocks. The Message owns the block.
     * You must use the getHeaderBlock method to access it.
     *
     * @param namespace
     * @param localPart
     * @param block
     * @throws WebServiceException
     */
    public void appendHeaderBlock(String namespace, String localPart, Block block)
        throws WebServiceException;
    /**
     * @return Set of QNames
     */
    public Set<QName> getHeaderQNames();
    
    /**
     * removePayload 
     * Removes all header blocks with this namespace/localpart
     *
     * @param namespace
     * @param localPart
     * @throws WebServiceException
     */
    public void removeHeaderBlock(String namespace, String localPart)
            throws WebServiceException;


    /**
     * Get a traceString...the trace string dumps the contents of the Block without forcing an
     * underlying ill-performant transformation of the message.
     *
     * @return String containing trace information
     * @boolean indent String containing indent characters
     */
    public String traceString(String indent);

    /**
     * Used to identify the Message parent of the XMLSpine
     *
     * @param msg
     */
    public void setParent(Message msg);

}
