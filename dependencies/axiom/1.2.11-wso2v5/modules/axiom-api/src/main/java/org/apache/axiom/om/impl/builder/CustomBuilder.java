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

import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMFactory;
import javax.xml.stream.XMLStreamReader;



/**
 * A Custom Builder is registered on the StAXBuilder for a particular QName or payload.
 * When the QName or payload is encountered, the CustomBuilder will build the OMElement
 * or OMSourcedElement for the StAXBuilder.  
 *
 * @see org.apache.axiom.om.impl.builder.StAXBuilder#registerCustomBuilder(javax.xml.namespace.QName, int, CustomBuilder)
 * @see org.apache.axiom.om.impl.builder.StAXBuilder#registerCustomBuilderForPayload(CustomBuilder)
 */
public interface CustomBuilder {
    /**
     * Create an OMElement for this whole subtree.
     * A null is returned if the default StAXBuilder behavior should be used.
     * @param namespace
     * @param localPart
     * @param parent
     * @param reader
     *            The stream reader to read the StAX events from. The data read
     *            from this reader always represents plain XML, even if the
     *            original document was XOP encoded. The reader optionally
     *            implements the {@link org.apache.axiom.ext.stax.datahandler.DataHandlerReader}
     *            extension to give the custom builder access to optimized
     *            binary data. This is appropriate for custom builders that
     *            support {@link org.apache.axiom.ext.stax.datahandler.DataHandlerReader}
     *            or in cases where there is no other option than to transfer
     *            binary data as base64 encoded character data.
     *            <p>
     *            However, if the custom builder interacts with a third party
     *            library that supports XOP, it may want to use that encoding
     *            to optimize the transfer of binary data. To do so, the
     *            custom builder MUST use {@link org.apache.axiom.util.stax.xop.XOPUtils#getXOPEncodedStream(XMLStreamReader)}
     *            to get an XOP encoded stream. This guarantees that the original
     *            reader is wrapped or unwrapped properly and also that
     *            the custom builder correctly gets access to the attachments,
     *            regardless of the type of the original reader. In particular,
     *            the custom builder MUST NOT attempt to retrieve attachments
     *            through the {@link org.apache.axiom.om.OMAttachmentAccessor}
     *            that may be implemented by the builder (because this wouldn't
     *            work if the builder was created from an {@link XMLStreamReader}
     *            implementing the {@link org.apache.axiom.ext.stax.datahandler.DataHandlerReader}
     *            extension).
     *            <p>
     *            The implementation MUST NOT assume that <code>reader</code> is the original
     *            reader returned by the StAX implementation. In general, it
     *            will be a wrapper around the original reader, e.g. one added
     *            by the {@link org.apache.axiom.util.stax.dialect.StAXDialect}
     *            implementation. If the method requires access to the original
     *            parser (e.g. to pass the {@link XMLStreamReader} object to
     *            another library that uses some special optimizations for
     *            particular parser implementations), it SHOULD use
     *            {@link org.apache.axiom.util.stax.XMLStreamReaderUtils#getOriginalXMLStreamReader(XMLStreamReader)}
     *            to unwrap the reader. If the method solely relies on the
     *            conformance of the reader to the StAX specification, it SHOULD
     *            NOT attempt to unwrap it.
     *            <p>
     *            If the implementation requires both an
     *            XOP encoded stream and wants to get access to the original reader, it should invoke
     *            {@link org.apache.axiom.util.stax.XMLStreamReaderUtils#getOriginalXMLStreamReader(XMLStreamReader)}
     *            after {@link org.apache.axiom.util.stax.xop.XOPUtils#getXOPEncodedStream(XMLStreamReader)}.
     * @return null or OMElement
     */
    public OMElement create(String namespace, 
                            String localPart, 
                            OMContainer parent, 
                            XMLStreamReader reader,
                            OMFactory factory)
        throws OMException;
}
