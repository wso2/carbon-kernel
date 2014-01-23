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

package org.apache.axiom.om.impl.builder;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import junit.framework.Assert;

import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.util.stax.xop.XOPEncodedStream;
import org.apache.axiom.util.stax.xop.XOPUtils;

public class JAXBCustomBuilder implements CustomBuilder {
    private final JAXBContext jaxbContext;
    private final boolean expectBareReader;
    private Object jaxbObject;
    private boolean attachmentsAccessed;
    
    public JAXBCustomBuilder(JAXBContext jaxbContext, boolean expectBareReader) {
        this.jaxbContext = jaxbContext;
        this.expectBareReader = expectBareReader;
    }

    public OMElement create(String namespaceURI, String localPart,
            OMContainer parent, XMLStreamReader reader, OMFactory factory)
            throws OMException {
        try {
            XOPEncodedStream xopStream = XOPUtils.getXOPEncodedStream(reader);
            XMLStreamReader encodedReader = xopStream.getReader();
            if (expectBareReader) {
                String className = encodedReader.getClass().getName();
                Assert.assertTrue(!className.startsWith("org.apache.axiom.")
                        || className.startsWith("org.apache.axiom.util.stax.dialect."));
            }
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            AttachmentUnmarshallerImpl attachmentUnmarshaller = new AttachmentUnmarshallerImpl(xopStream.getMimePartProvider());
            unmarshaller.setAttachmentUnmarshaller(attachmentUnmarshaller);
            // For the purpose of the test we just store the JAXB object and return
            // a dummy element. Normally, one would create an OMSourcedElement here.
            jaxbObject = unmarshaller.unmarshal(encodedReader);
            attachmentsAccessed = attachmentUnmarshaller.isAccessed();
            return factory.createOMElement(new QName(namespaceURI, localPart), parent);
        } catch (JAXBException ex) {
            throw new OMException(ex);
        }
    }

    public Object getJaxbObject() {
        return jaxbObject;
    }

    public boolean isAttachmentsAccessed() {
        return attachmentsAccessed;
    }
}
