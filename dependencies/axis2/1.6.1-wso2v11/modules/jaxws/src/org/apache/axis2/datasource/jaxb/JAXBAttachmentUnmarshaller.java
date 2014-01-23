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

package org.apache.axis2.datasource.jaxb;

import org.apache.axiom.util.stax.xop.MimePartProvider;
import org.apache.axis2.context.MessageContext;

import javax.activation.DataHandler;

/**
 * JAXBAttachmentUnmarshaller
 * <p/>
 * An implementation of the <link>javax.xml.bind.attachment.AttachmentUnmarshaller</link> that is
 * used for deserializing XOP elements into their corresponding binary data packages.
 */
public class JAXBAttachmentUnmarshaller extends AbstractJAXBAttachmentUnmarshaller {
    private final MessageContext msgContext;

    public JAXBAttachmentUnmarshaller(MimePartProvider mimePartProvider,
            MessageContext msgContext) {
        super(mimePartProvider);
        this.msgContext = msgContext;
    }

    @Override
    protected DataHandler getDataHandlerForSwA(String blobcid) {
        return msgContext.getAttachment(blobcid);
    }
}
