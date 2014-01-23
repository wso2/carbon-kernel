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

package org.apache.axis2.jaxws.message.attachments;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.jaxws.message.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamWriter;

/**
 * An implementation of the JAXB AttachmentMarshaller that is used to handle binary data from JAXB
 * and create populate the appropriate constructs within the JAX-WS Message Model.
 */
public class JAXBAttachmentMarshaller extends org.apache.axis2.datasource.jaxb.JAXBAttachmentMarshaller {
    
    private static final Log log = LogFactory.getLog(JAXBAttachmentMarshaller.class);
    private Message message;
    
    public JAXBAttachmentMarshaller(Message message, XMLStreamWriter writer) {
        super(getAxis2MessageContext(message), writer);
        this.message = message;
    }
    
    /**
     * Get the Axis2 Message Context out of the Message by going through the JAXWS Message Context.
     * @param message The Message from which to get the Axis Message Context
     * @return the Axis Message context or null if one is not found.
     */
    private static MessageContext getAxis2MessageContext(Message message) {
        MessageContext axisMessageContext = null;
        if (message != null) {
            if (message.getMessageContext() != null) {
                axisMessageContext = message.getMessageContext().getAxisMessageContext();
            }
        }
        return axisMessageContext;
    }
    /**
     * @return if MTOM enabled calculated from the context information
     */
    public boolean isMTOMEnabled() {
        if (message == null) {
            return false;
        } else {
            return message.isMTOMEnabled();
        }  
    }
    
    /**
     * Indicate on the context that SWA attachments are encountered
     */
    public void setDoingSWA() {
        if (message != null) {
            message.setDoingSWA(true);
        }
    }
    
    /**
     * Inform the context that an Attachment (DataHandler, CID) is written.
     * @param dh
     * @param cid
     */
    public void addDataHandler(DataHandler dh, String cid) {
        if (message != null) {
            message.addDataHandler(dh, cid);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("The msgContext is null.  The attachment is not stored");
                log.debug("   content id=" + cid);
                log.debug("   dataHandler  =" + dh);
            }
        }
    }
}
