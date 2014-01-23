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

package org.apache.axis2.swa;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.impl.llom.OMTextImpl;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.wsdl.WSDLConstants;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;

/** @version $Rev: $ $Date: $ */

public class EchoSwA {

    public EchoSwA() {
    }

    public OMElement echoAttachment(OMElement omEle) throws AxisFault {
        OMElement child = (OMElement)omEle.getFirstOMChild();
        OMAttribute attr = child.getAttribute(new QName("href"));
        String contentID = attr.getAttributeValue();
        contentID = contentID.trim();
        if (contentID.substring(0, 3).equalsIgnoreCase("cid")) {
            contentID = contentID.substring(4);
        }

        MessageContext msgCtx = MessageContext.getCurrentMessageContext();
        Attachments attachment = (msgCtx).getAttachmentMap();

        DataHandler dataHandler = attachment.getDataHandler(contentID);
        OMText textNode = new OMTextImpl(dataHandler, omEle.getOMFactory());
        omEle.build();
        child.detach();
        omEle.addChild(textNode);
        return omEle;
    }

    public OMElement echoOMElement(OMElement omEle) throws AxisFault {
        OMElement child = (OMElement)omEle.getFirstOMChild();
        MessageContext msgCtx = MessageContext.getCurrentMessageContext();
        Attachments attachment = (msgCtx).getAttachmentMap();
        msgCtx.removeAttachment(attachment.getSOAPPartContentID());
        msgCtx.getOperationContext().getMessageContext(
                WSDLConstants.MESSAGE_LABEL_OUT_VALUE).setAttachmentMap(attachment);

        omEle.build();
        return omEle;
    }
}