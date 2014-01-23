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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.RolePlayer;
import org.apache.axis2.jaxws.message.factory.BlockFactory;

import javax.jws.soap.SOAPBinding.Style;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.WebServiceException;
import java.util.List;
import java.util.Set;


/**
 * XMLPart
 * 
 * The XML portion of a Message
 * 
 * The JAX-WS implementation (proxy, message receiver, etc.) interact with the 
 * Message via Blocks.  A Block is represented in the message as a root element tree 
 * in either the header, body or fault detail section.  The Blocks can be easily
 * decomposed into business objects (which are needed on the JAX-WS interfaces).
 * 
 * In addition, the JAX-WS handler model requires that the XMLPart be exposed as
 * an SAAJ SOAPEnvelope.  
 * 
 * The XMLPart abstraction hides the details of the message transformations from
 * the JAX-WS implementation.
 * 
 * @see org.apache.axis2.jaxws.message.Message
 * @see org.apache.axis2.jaxws.message.Block
 * @see org.apache.axis2.jaxws.message.impl.XMLPartBase for implementation details
 * 
 */

public interface XMLPart {

    /**
     * Get the protocol for this Message (soap11, soap12, etc.)
     *
     * @return Protocl
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

    /**
     * Set the Style. If the style is DOCUMENT, the body blocks are located underneath the body
     * element. If the style is set to RPC, then the body blocks are located underneath the rpc
     * operation.
     *
     * @param style Style
     * @see set indirection
     */
    public void setStyle(Style style) throws WebServiceException;

    /**
     * Set indirection.  Used to force the code to look for blocks at a particular location. For
     * DOCUMENT the default is 0 For RPC the default is 1 This method is used to override these
     * settings for special cases.
     *
     * @param indirection (0 or 1)
     */
    public void setIndirection(int indirection);

    /**
     * Get indirection.  Used to force the code to look for blocks at a particular location. For
     * DOCUMENT the default is 0 For RPC the default is 1 This method is used to override these
     * settings for special cases.
     *
     * @return indirection (0 or 1)
     */
    public int getIndirection();

    /** @return the QName of the operation element if Style.rpc.  Otherwise null */
    public QName getOperationElement() throws WebServiceException;

    /**
     * Set the operation element qname.  The operation qname is only used if Style.rpc
     *
     * @param operationQName
     */
    public void setOperationElement(QName operationQName) throws WebServiceException;

    /**
     * isConsumed Return true if the part is consumed.  Once consumed, the information in the part is
     * no longer available.
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
     * getParent Get the Message object that this XMLPart is attached to, if it is attached to one
     * at all.
     *
     * @return
     */
    public Message getParent();

    /**
     * setParent Set the Message object that will hold this XMLPart
     *
     * @param m
     */
    public void setParent(Message m);

    /**
     * getAsEnvelope Get the xml part as a read/write SOAPEnvelope
     *
     * @return SOAPEnvelope
     * @throws WebServiceException
     */
    public SOAPEnvelope getAsSOAPEnvelope() throws WebServiceException;

    /**
     * getAsOMElement Get the xml part as a read/write OM...note this returns an OM SOAPEnvelope for
     * all protocols...even REST
     *
     * @return OMElement
     * @throws WebServiceException
     */
    public OMElement getAsOMElement() throws WebServiceException;

    /**
     * getNumBodyBlocks Calling this method will cache the OM.  Avoid it in performant situations.
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
     * getBodyBlock Get the body block at the specificed index. The BlockFactory and object context are
     * passed in to help create the proper kind of block. Calling this method will cache the OM.  Avoid
     * it in performant situations.
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
     * getHeaderBlock 
     * Get the firstheader block with the specified name.
     * The BlockFactory and object context
     * are passed in to help create the proper kind of block.
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
     * Get the header blocks with the specified name
     * The BlockFactory and object context
     * are passed in to help create the proper kind of block.
     *
     * @param namespace uri of header
     * @param localPart local name of header
     * @param context context for blockFactory
     * @param blockFactory  kind of factory (i.e. JAXB)
     * @param RolePlayer determines acceptable roles (or null)
     * @return List<Block>
     * @throws WebServiceException
     */
    public List<Block> getHeaderBlocks(String namespace, String localPart,
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
     * @return QNames of headers
     */
    public Set<QName> getHeaderQNames();
    
    /**
     * removeHeaderBlock
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
     * The representation of the XMLPart may be in a number of different forms.  Currently the forms
     * are UNKNOWN, OM, SOAPENVELOPE, and SPINE.  This method returns a String containing one of
     * these types.  This method should only be used for trace and testing purposes.  The consumer
     * of a Message should not make any decisions based on the representation of the XMLPart
     *
     * @return String
     */
    public String getXMLPartContentType();

    /**    
     * Used primarily to ensure the parser is forwarded to the end so it can be closed.
     */
    public void close();
}   
