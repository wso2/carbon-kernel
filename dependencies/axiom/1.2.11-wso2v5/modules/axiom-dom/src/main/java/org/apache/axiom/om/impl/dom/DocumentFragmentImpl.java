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

package org.apache.axiom.om.impl.dom;

import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMFactory;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class DocumentFragmentImpl extends ParentNode implements
        DocumentFragment {

    /** @param ownerDocument  */
    public DocumentFragmentImpl(DocumentImpl ownerDocument, OMFactory factory) {
        super(ownerDocument, factory);
        this.done = true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.w3c.dom.Node#getNodeType()
     */
    public short getNodeType() {
        return Node.DOCUMENT_FRAGMENT_NODE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.w3c.dom.Node#getNodeName()
     */
    public String getNodeName() {
        return "#document-fragment";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.axiom.om.OMNode#getType()
     */
    public int getType() throws OMException {
        return -1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.axiom.om.OMNode#setType(int)
     */
    public void setType(int nodeType) throws OMException {
        // DO Nothing :-?
    }

    public void internalSerialize(XMLStreamWriter writer, boolean cache) throws XMLStreamException {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public void serialize(XMLStreamWriter xmlWriter) throws XMLStreamException {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public void serializeAndConsume(XMLStreamWriter xmlWriter)
            throws XMLStreamException {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

}
