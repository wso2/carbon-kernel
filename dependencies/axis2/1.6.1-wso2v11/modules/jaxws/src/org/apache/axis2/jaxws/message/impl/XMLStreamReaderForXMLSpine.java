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

package org.apache.axis2.jaxws.message.impl;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.util.StackableReader;
import org.apache.axis2.jaxws.message.util.XMLStreamReaderFilter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.WebServiceException;
import java.util.List;

/**
 * XMLStreamReaderForXMLSpine
 * <p/>
 * An XMLSpine is composed of many different parts: a sparse OM tree, header blocks, body blocks,
 * etc.
 * <p/>
 * The XMLStreamReaderForXMLSpine provides an XMLStreamReader that over all of these combined
 * objects (without building a full OM tree). It does this by using a StackableXMLStreamReader for
 * the underlying implementation and pushing the XMLStreamReaders for the blocks onto the stack at
 * the appropriate points in the message.
 */
public class XMLStreamReaderForXMLSpine extends XMLStreamReaderFilter {

    // Components of an XMLSpine
    OMElement root;
    private List<Block> headerBlocks = null;
    private List<Block> bodyBlocks = null;
    private List<Block> detailBlocks = null;
    private boolean consume = false;
    Protocol protocol = null;

    // Local Constants
    private static final String BODY = "Body";
    private static final String HEADER = "Header";
    private static final String FAULT = "Fault";
    private static final String DETAIL11 = "detail";
    private static final String DETAIL12 = "Detail";

    boolean inFault = false;
    private List<Block> insertBlocks = null;

    /**
     * @param root         of the XMLSpine
     * @param headerBlocks
     * @param bodyBocks
     * @param detailBlocks
     * @param consume
     */
    public XMLStreamReaderForXMLSpine(OMElement root,
                                      Protocol protocol,
                                      List<Block> headerBlocks,
                                      List<Block> bodyBlocks,
                                      List<Block> detailBlocks,
                                      boolean consume) {
        // Create a stackable reader and prime it with the root om tree
        // The XMLStreamReader's for the blocks will be pushed onto the
        // stack as the message is processed.
        super(new StackableReader(root.getXMLStreamReader()));
        this.root = root;
        this.protocol = protocol;
        this.headerBlocks = headerBlocks;
        this.bodyBlocks = bodyBlocks;
        this.detailBlocks = detailBlocks;
        this.consume = consume;
    }

    @Override
    public int next() throws XMLStreamException {
        // The next method is overridden so that we can push
        // the block's XMLStreamReaders onto the stack at the
        // appropriate places in the message (pretty slick).

        // Insert pending blocks onto the stack
        if (insertBlocks != null) {
            pushBlocks(insertBlocks, consume);
            insertBlocks = null;

        }

        // Get the next event
        int event = super.next();

        // If this is a start element event, then we may need to insert
        // the blocks prior to the next event
        if (isStartElement()) {
            QName qName = super.getName();
            // Insert body blocks after the Body
            if (qName.getLocalPart().equals(BODY) &&
                    qName.getNamespaceURI().equals(root.getNamespace().getNamespaceURI())) {
                if (bodyBlocks != null) {
                    insertBlocks = bodyBlocks;
                    bodyBlocks = null;
                }
            }
            // Insert header blocks after the header
            else if (qName.getLocalPart().equals(HEADER) &&
                    qName.getNamespaceURI().equals(root.getNamespace().getNamespaceURI())) {
                if (headerBlocks != null) {
                    insertBlocks = headerBlocks;
                    headerBlocks = null;
                }
            } else if (qName.getLocalPart().equals(FAULT) &&
                    qName.getNamespaceURI().equals(root.getNamespace().getNamespaceURI())) {
                inFault = true;
            }
            // Insert Detail blocks afger the detail...note that
            // the detail name is different in SOAP 1.1 and SOAP 1.2
            else if (inFault) {
                if (qName.getLocalPart().equals(DETAIL11) &&
                        protocol.equals(Protocol.soap11) ||
                        qName.getLocalPart().equals(DETAIL12) &&
                                protocol.equals(Protocol.soap12)) {
                    if (detailBlocks != null) {
                        insertBlocks = detailBlocks;
                        detailBlocks = null;
                    }
                }
            }
        }
        return event;
    }

    /**
     * Push the XMLStreamReaders for the blocks
     *
     * @param blocks
     */
    private void pushBlocks(List<Block> blocks, boolean consume) throws XMLStreamException {
        // Push the XMLStreamReaders for the blocks onto the
        // delegate.  This is done in reverse order of the blocks so that the
        // first  block's xmlstreamreader is ontop of the stack.
        try {
            StackableReader sr = (StackableReader)delegate;
            for (int i = blocks.size() - 1; i >= 0; i--) {
                Block block = blocks.get(i);
                if (block != null) {
                    sr.push(block.getXMLStreamReader(consume));
                }
            }
        } catch (WebServiceException me) {
            throw new XMLStreamException(me);
        }
    }
}
