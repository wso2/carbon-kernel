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

package org.apache.axiom.om.impl.llom;

import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMDocType;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNode;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class OMDocTypeImpl extends OMNodeImpl implements OMDocType {
    protected String value;

    /**
     * Constructor OMDocTypeImpl.
     *
     * @param parentNode
     * @param contentText
     */
    public OMDocTypeImpl(OMContainer parentNode, String contentText,
                         OMFactory factory) {
        super(parentNode, factory, true);
        this.value = contentText;
        nodeType = OMNode.DTD_NODE;
    }

    /**
     * Constructor OMDocTypeImpl.
     *
     * @param parentNode
     */
    public OMDocTypeImpl(OMContainer parentNode, OMFactory factory) {
        this(parentNode, null, factory);
    }

    public void internalSerialize(XMLStreamWriter writer, boolean cache) throws XMLStreamException {
        writer.writeDTD(this.value);
    }

    /**
     * Gets the value of this DocType.
     *
     * @return Returns String.
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of this DocType.
     *
     * @param text
     */
    public void setValue(String text) {
        this.value = text;
    }

    /**
     * Discards this node.
     *
     * @throws OMException
     */
    public void discard() throws OMException {
        if (done) {
            this.detach();
        } 
    }
}
