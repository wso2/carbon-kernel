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

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.util.stax.xop.XOPDecodingStreamReader;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class XOPAwareStAXOMBuilder 
    extends StAXOMBuilder implements XOPBuilder {
    
    /** <code>Attachments</code> handles deferred parsing of incoming MIME Messages. */
    Attachments attachments;

    /**
     * Constructor StAXOMBuilder.
     *
     * @param ombuilderFactory
     * @param parser
     */
    public XOPAwareStAXOMBuilder(OMFactory ombuilderFactory,
                                 XMLStreamReader parser, Attachments attachments) {
        super(ombuilderFactory, new XOPDecodingStreamReader(parser,
                new OMAttachmentAccessorMimePartProvider(attachments)));
        this.attachments = attachments;
    }

    /**
     * Constructor linked to existing element.
     *
     * @param factory
     * @param parser
     * @param element
     */
    public XOPAwareStAXOMBuilder(OMFactory factory, XMLStreamReader parser,
                                 OMElement element, Attachments attachments) {
        super(factory, new XOPDecodingStreamReader(parser,
                new OMAttachmentAccessorMimePartProvider(attachments)), element);
        this.attachments = attachments;
    }

    /**
     * @param filePath - Path to the XML file
     * @throws XMLStreamException
     * @throws FileNotFoundException
     */
    public XOPAwareStAXOMBuilder(String filePath, Attachments attachments)
            throws XMLStreamException,
            FileNotFoundException {
        super(new XOPDecodingStreamReader(StAXUtils.createXMLStreamReader(new FileInputStream(
                filePath)), new OMAttachmentAccessorMimePartProvider(attachments)));
        this.attachments = attachments;
    }

    /**
     * @param inStream - instream which contains the XML
     * @throws XMLStreamException
     */
    public XOPAwareStAXOMBuilder(InputStream inStream, Attachments attachments)
            throws XMLStreamException {
        super(new XOPDecodingStreamReader(StAXUtils.createXMLStreamReader(inStream),
                new OMAttachmentAccessorMimePartProvider(attachments)));
        this.attachments = attachments;
    }

    /**
     * Constructor StAXXOPAwareOMBuilder.
     *
     * @param parser
     */
    public XOPAwareStAXOMBuilder(XMLStreamReader parser, Attachments attachments) {
        super(new XOPDecodingStreamReader(parser, new OMAttachmentAccessorMimePartProvider(
                attachments)));
        this.attachments = attachments;
    }

    public DataHandler getDataHandler(String blobContentID) throws OMException {
        return attachments.getDataHandler(blobContentID);
    }
    
    public Attachments getAttachments() {
        return attachments;
    }
}
