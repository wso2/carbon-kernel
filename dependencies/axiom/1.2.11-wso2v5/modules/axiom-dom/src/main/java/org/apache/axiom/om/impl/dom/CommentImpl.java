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

import org.apache.axiom.om.OMComment;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNode;
import org.w3c.dom.Comment;
import org.w3c.dom.Node;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class CommentImpl extends CharacterImpl implements Comment, OMComment {

    public CommentImpl(DocumentImpl ownerNode, OMFactory factory) {
        super(ownerNode, factory);
        this.done = true;
    }

    public CommentImpl(DocumentImpl ownerNode, String value, OMFactory factory) {
        super(ownerNode, value, factory);
        this.done = true;
    }

    public String getNodeName() {
        return "#comment";
    }

    public short getNodeType() {
        return Node.COMMENT_NODE;
    }

    public String getValue() {
        return this.getData();
    }

    public void setValue(String text) {
        this.textValue = text;
    }

    public int getType() {
        return OMNode.COMMENT_NODE;
    }

    public void setType(int nodeType) throws OMException {
        throw new UnsupportedOperationException(
                "You should not set the node type of a comment");
    }

    public void internalSerialize(XMLStreamWriter writer, boolean cache) throws XMLStreamException {
        writer.writeComment(this.textValue);
    }
}
