/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.axis2.transport.testkit.message;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMSourcedElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;

/**
 * Class encapsulating a SOAP envelope and an attachment map.
 * This class is used by {@link MockMessageReceiver} because it is not safe to
 * keep a reference to the {@link org.apache.axis2.context.MessageContext} object.
 */
public class AxisMessage {
    private String messageType;
    private SOAPEnvelope envelope;
    private Attachments attachments;
    
    public AxisMessage() {
    }
    
    public AxisMessage(MessageContext msgContext) throws Exception {
        envelope = msgContext.getEnvelope();
        // If possible, build the parent (i.e. the OMDocument) to make sure that the entire message is read.
        // If the transport doesn't handle the end of the message properly, then this problem
        // will show up here.
        OMDocument document = (OMDocument)envelope.getParent();
        if (document != null) {
            document.build();
        } else {
            envelope.build();
        }
        
        // TODO: quick & dirty hack to force expansion of OMSourceElement payloads
        OMElement content = envelope.getBody().getFirstElement();
        if (content instanceof OMSourcedElement) {
            ((OMSourcedElement)content).getFirstOMChild();
            ((OMSourcedElement)content).build();
        }
        
        if (msgContext.isDoingSwA()) {
            // Make sure that all attachments are read
            attachments = msgContext.getAttachmentMap();
            attachments.getAllContentIDs();
        }
        messageType = (String)msgContext.getProperty(Constants.Configuration.MESSAGE_TYPE);
    }
    
    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public SOAPEnvelope getEnvelope() {
        return envelope;
    }

    public void setEnvelope(SOAPEnvelope envelope) {
        this.envelope = envelope;
    }

    public Attachments getAttachments() {
        return attachments;
    }
    
    public void setAttachments(Attachments attachments) {
        this.attachments = attachments;
    }
}
