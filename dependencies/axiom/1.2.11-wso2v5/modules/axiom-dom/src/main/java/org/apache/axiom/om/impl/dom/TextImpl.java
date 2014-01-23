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

import javax.xml.namespace.QName;

import org.apache.axiom.ext.stax.datahandler.DataHandlerProvider;
import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.w3c.dom.Node;

public class TextImpl extends TextNodeImpl {
    private boolean isWhitespace;

    public TextImpl(DocumentImpl ownerNode, char[] value, OMFactory factory) {
        super(ownerNode, value, factory);
    }

    public TextImpl(DocumentImpl ownerNode, Object dataHandler, boolean optimize, OMFactory factory) {
        super(ownerNode, dataHandler, optimize, factory);
    }

    public TextImpl(DocumentImpl ownerNode, String contentID,
            DataHandlerProvider dataHandlerProvider, boolean optimize, OMFactory factory) {
        super(ownerNode, contentID, dataHandlerProvider, optimize, factory);
    }

    public TextImpl(DocumentImpl ownerNode, OMFactory factory) {
        super(ownerNode, factory);
    }

    public TextImpl(DocumentImpl ownerNode, String value, OMFactory factory) {
        super(ownerNode, value, factory);
    }

    public TextImpl(DocumentImpl ownerNode, String value, int nodeType, OMFactory factory) {
        super(ownerNode, value, factory);
        isWhitespace = nodeType == SPACE_NODE;
    }

    public TextImpl(DocumentImpl ownerNode, String value, String mimeType, boolean optimize,
            OMFactory factory) {
        super(ownerNode, value, mimeType, optimize, factory);
    }

    public TextImpl(OMContainer parent, QName text, int nodeType, OMFactory factory) {
        super(parent, text, nodeType, factory);
    }

    public TextImpl(OMContainer parent, QName text, OMFactory factory) {
        super(parent, text, factory);
    }

    public TextImpl(OMContainer parent, TextNodeImpl source, OMFactory factory) {
        super(parent, source, factory);
    }

    public TextImpl(String contentID, OMContainer parent, OMXMLParserWrapper builder,
            OMFactory factory) {
        super(contentID, parent, builder, factory);
    }

    public TextImpl(String text, OMFactory factory) {
        super(text, factory);
    }

    public TextImpl(String text, String mimeType, boolean optimize, boolean isBinary,
            OMFactory factory) {
        super(text, mimeType, optimize, isBinary, factory);
    }

    public TextImpl(String text, String mimeType, boolean optimize, OMFactory factory) {
        super(text, mimeType, optimize, factory);
    }

    public int getType() throws OMException {
        return isWhitespace ? OMNode.SPACE_NODE : OMNode.TEXT_NODE;
    }

    public void setType(int nodeType) throws OMException {
        switch (nodeType) {
            case OMNode.TEXT_NODE:
                isWhitespace = false;
                break;
            case OMNode.SPACE_NODE:
                isWhitespace = true;
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    public Node cloneNode(boolean deep) {
        TextImpl textImpl = new TextImpl(this.textValue, this.factory);
        textImpl.setOwnerDocument(this.ownerNode);
        return textImpl;
    }
}
