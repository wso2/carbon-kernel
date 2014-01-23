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

import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.message.factory.BlockFactory;

import javax.activation.DataHandler;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;
import java.util.List;
import java.util.Map;

/**
 * Message
 * <p/>
 * A Message represents the XML + Attachments
 * <p/>
 * Most of the methods available on a message are only applicable to the XML part of the Message.
 * See the XMLPart interface for an explantation of these methods.
 *
 * @see XMLPart
 * @see Attachment
 */
public interface Message extends XMLPart {

    /**
     * Get the protocol for this Message (soap11, soap12, etc.)
     *
     * @return Protocl
     */
    public Protocol getProtocol();

    /**
     * getAsSOAPMessage Get the xml part as a read/write SOAPEnvelope
     *
     * @return SOAPEnvelope
     */
    public SOAPMessage getAsSOAPMessage() throws WebServiceException;

    /**
     * Add Attachment
     * @param dh DataHandler (type of Attachment is inferred from dh.getContentType)
     * @param id String which is the Attachment content id
     * @see addAttachment(Attachment)
     */
    public void addDataHandler(DataHandler dh, String id);
    
    /**
     * Get the list of attachment content ids for the message
     * @return List<String>
     */
    public List<String> getAttachmentIDs();
  
    /**
     * Get the indicated (non-soap part) attachment id
     * @param index
     * @return CID or null if not present
     */
    public String getAttachmentID(int index);
    
    /**
     * Get the indicated (non-soap part) attachment id
     * @param partName (WS-I indicates that SWA attachments have a partName prefix)
     * @return CID or null if not present
     */
    public String getAttachmentID(String partName);
    
    
    /**
     * Get the attachment identified by the contentID 
     * @param cid
     * @return
     */
    public DataHandler getDataHandler(String cid);
    
    /**
     * Indicate that an SWA DataHandler was added to the message.
     * This information will be used to trigger SWA serialization.
     * @param value
     */
    public void setDoingSWA(boolean value);
    
    /**
     * @return true if SWA DataHandler is present
     */
    public boolean isDoingSWA();
    
    /** 
     * Get the attachment and remove it from the Message
     * @param cid
     */
    public DataHandler removeDataHandler(String cid);
    
    /**
     * A message is MTOM enabled if the 
     * associated dispatch/client/impl/provider has a binding type 
     * that enables MTOM.
     * @return if this is an MTOM message
     */
    public boolean isMTOMEnabled();

    /** 
     * A message is MTOM enabled if the 
     * associated dispatch/client/impl/provider has a binding type 
     * that enables MTOM.
     * Indicate whether this is an MTOM message
     * @param b
     */
    public void setMTOMEnabled(boolean b);
    

    /** 
     * @return get the transport headers map.
     */
    public Map getMimeHeaders();

    /**
     * Set the transport headers
     * @param map Map
     */
    public void setMimeHeaders(Map map);

    /**
     * Indicate that this message is passed the pivot point. For example, this is set in the JAX-WS
     * Dispatcher to indicate
     */
    public void setPostPivot();

    /** @return true if post pivot */
    public boolean isPostPivot();

    /**
     * JAX-WS Message Context that owns the Message
     * @param messageContext
     */
    public void setMessageContext(MessageContext messageContext);
    
    /**
     * @return JAX-WS MessageContext
     */
    public MessageContext getMessageContext();

    /* 
    * Get the entire message rendered in a certain type of value (i.e. String, Source, SOAPMessage, etc.)
    * @param context
    * @param blockFactory blockfactory associated with the kind of rendering
    */
    public Object getValue(Object context, BlockFactory blockFactory)
            throws WebServiceException;
}
