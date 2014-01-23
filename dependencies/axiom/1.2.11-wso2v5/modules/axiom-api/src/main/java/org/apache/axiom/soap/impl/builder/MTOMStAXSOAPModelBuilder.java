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

package org.apache.axiom.soap.impl.builder;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.impl.builder.OMAttachmentAccessorMimePartProvider;
import org.apache.axiom.om.impl.builder.XOPBuilder;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.util.stax.xop.XOPDecodingStreamReader;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamReader;

public class MTOMStAXSOAPModelBuilder extends StAXSOAPModelBuilder implements XOPBuilder {
    
    /** <code>Attachments</code> handles deferred parsing of incoming MIME Messages. */
    Attachments attachments;

    int partIndex = 0;

    public MTOMStAXSOAPModelBuilder(XMLStreamReader parser,
                                    SOAPFactory factory, Attachments attachments,
                                    String soapVersion) {
        super(new XOPDecodingStreamReader(parser, new OMAttachmentAccessorMimePartProvider(
                attachments)), factory, soapVersion);
        this.attachments = attachments;
    }

    /**
     * @param reader
     * @param attachments
     */
    public MTOMStAXSOAPModelBuilder(XMLStreamReader reader,
                                    Attachments attachments, String soapVersion) {
        super(new XOPDecodingStreamReader(reader, new OMAttachmentAccessorMimePartProvider(
                attachments)), soapVersion);
        this.attachments = attachments;
    }

    public MTOMStAXSOAPModelBuilder(XMLStreamReader reader,
                                    Attachments attachments) {
        super(new XOPDecodingStreamReader(reader, new OMAttachmentAccessorMimePartProvider(
                attachments)));
        this.attachments = attachments;
    }

    /* (non-Javadoc)
      * @see org.apache.axiom.soap.impl.builder.XOPBuilder#getDataHandler(java.lang.String)
      */
    public DataHandler getDataHandler(String blobContentID) throws OMException {
        DataHandler dataHandler = attachments.getDataHandler(blobContentID);
        /* The getDataHandler javadoc indicates that null indicate that the datahandler
         * was not found
         * 
        if (dataHandler == null) {
            throw new OMException(
                    "Referenced Attachment not found in the MIME Message. ContentID:"
                            + blobContentID);
        }
        */
        return dataHandler;
    }
    
    public Attachments getAttachments() {
        return attachments;
    }
}
