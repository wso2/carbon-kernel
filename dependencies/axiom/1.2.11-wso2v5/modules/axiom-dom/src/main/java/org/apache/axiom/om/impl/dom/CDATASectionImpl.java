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
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.dom.factory.OMDOMFactory;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Node;

public class CDATASectionImpl extends TextNodeImpl implements CDATASection {
    public CDATASectionImpl(String text, OMFactory factory) {
        super(text, factory);
    }

    public CDATASectionImpl(DocumentImpl ownerDocument, String text, OMDOMFactory factory) {
        super(ownerDocument, text, factory);
    }

    public int getType() throws OMException {
        return OMNode.CDATA_SECTION_NODE;
    }

    public void setType(int nodeType) throws OMException {
        if (nodeType != OMNode.CDATA_SECTION_NODE) {
            throw new UnsupportedOperationException();
        }
    }

    public Node cloneNode(boolean deep) {
        CDATASectionImpl textImpl = new CDATASectionImpl(this.textValue, this.factory);
        textImpl.setOwnerDocument(this.ownerNode);
        return textImpl;
    }
}
