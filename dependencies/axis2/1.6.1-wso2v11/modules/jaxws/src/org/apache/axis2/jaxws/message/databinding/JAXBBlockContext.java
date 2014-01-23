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

package org.apache.axis2.jaxws.message.databinding;

import org.apache.axiom.util.stax.xop.MimePartProvider;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.datasource.jaxb.JAXBDSContext;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.attachments.JAXBAttachmentMarshaller;
import org.apache.axis2.jaxws.message.attachments.JAXBAttachmentUnmarshaller;
import org.apache.axis2.jaxws.spi.Constants;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.stream.XMLStreamWriter;
import java.util.TreeSet;

/*
 * A JAXBBlockContext controls access to the JAXBContext
 * In addition the JAXBBlockContext contains additional contextural information needed
 * by the JAX-WS component
 */

public class JAXBBlockContext extends JAXBDSContext {

    Message message = null;
    /**
     * Full Constructor JAXBBlockContext (most performant)
     *
     * @param packages Set of packages needed by the JAXBContext.
     */
    public JAXBBlockContext(TreeSet<String> packages, String packagesKey) {
        super(packages, packagesKey);
    }

    /**
     * Slightly slower constructor
     *
     * @param packages
     */
    public JAXBBlockContext(TreeSet<String> packages) {
        this(packages, packages.toString());
    }

    /**
     * Normal Constructor JAXBBlockContext
     *
     * @param contextPackage
     * @deprecated
     */
    public JAXBBlockContext(String contextPackage) {
        super(contextPackage);
    }

    /**
     * "Dispatch" Constructor Use this full constructor when the JAXBContent is provided by the
     * customer.
     *
     * @param jaxbContext
     */
    public JAXBBlockContext(JAXBContext jaxbContext) {
        super(jaxbContext);
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    @Override
    protected AttachmentMarshaller createAttachmentMarshaller(XMLStreamWriter writer) {
        return new JAXBAttachmentMarshaller(getMessage(), writer);
    }
    
    @Override
    protected AttachmentUnmarshaller createAttachmentUnmarshaller(MimePartProvider mimePartProvider) {
        return new JAXBAttachmentUnmarshaller(mimePartProvider, getMessage());
    }

    public ClassLoader getClassLoader() {
        if(message != null && message.getMessageContext() != null) {
            return (ClassLoader) message.getMessageContext().
                getProperty(Constants.CACHE_CLASSLOADER);
        }
        return super.getClassLoader();
    }

    public MessageContext getMessageContext() {
        if(message != null && message.getMessageContext() != null &&
           message.getMessageContext().getAxisMessageContext() != null) {
            super.setMessageContext(message.getMessageContext().getAxisMessageContext());
        }
        return super.getMessageContext();
    }
    
}
