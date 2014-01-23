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

package org.apache.axis2.builder;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.impl.MTOMConstants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.util.MessageProcessorSelector;

import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;
import java.io.InputStream;

public class MIMEBuilder implements Builder {

    public OMElement processDocument(InputStream inputStream, String contentType,
                                     MessageContext msgContext)
            throws AxisFault {
        Attachments attachments =
                BuilderUtil.createAttachmentsMap(msgContext, inputStream, contentType);
        String charSetEncoding =
                BuilderUtil.getCharSetEncoding(attachments.getSOAPPartContentType());

        if ((charSetEncoding == null)
                || "null".equalsIgnoreCase(charSetEncoding)) {
            charSetEncoding = MessageContext.UTF_8;
        }
        msgContext.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING,
                               charSetEncoding);

        //  Put a reference to Attachments Map in to the message context For
        // backword compatibility with Axis2 1.0 
        msgContext.setProperty(MTOMConstants.ATTACHMENTS, attachments);

        // Setting the Attachments map to new SwA API
        msgContext.setAttachmentMap(attachments);
        // We set the following for all the MIME messages.. Will be overridden
        // by subsequent builders(eg:MTOMBuilder) if needed..
        msgContext.setDoingSwA(true);
        
        ContentType ct;
        try {
            ct = new ContentType(contentType);
        } catch (ParseException e) {
            throw new OMException(
                    "Invalid Content Type Field in the Mime Message", e);
        }
        
        String type = ct.getParameter("type");
        Builder builder =
        	 MessageProcessorSelector.getMessageBuilder(type, msgContext);
        if(MTOMConstants.MTOM_TYPE.equals(type)){
            String startInfo = ct.getParameter("start-info");
            if(startInfo != null){
                type = startInfo;
            }
        }
        return builder.processDocument(attachments.getSOAPPartInputStream(),
                type, msgContext);
    }
}
