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

package org.apache.axiom.c14n.omwrapper.interfaces;

/**
 * @author Saliya Ekanayake (esaliya@gmail.com)
 */
public interface Node {
// some of these may not be actually necessary for the wrappers
// but I kept them for the sake of future modifications
     public static final short ELEMENT_NODE              = 1;
    /**
     * The node is an <code>AttrImpl</code>.
     */
    public static final short ATTRIBUTE_NODE            = 2;
    /**
     * The node is a <code>Text</code> node.
     */
    public static final short TEXT_NODE                 = 3;
    /**
     * The node is a <code>CDATASection</code>.
     */
    public static final short CDATA_SECTION_NODE        = 4;
    /**
     * The node is an <code>EntityReference</code>.
     */
    public static final short ENTITY_REFERENCE_NODE     = 5;
    /**
     * The node is an <code>Entity</code>.
     */
    public static final short ENTITY_NODE               = 6;
    /**
     * The node is a <code>ProcessingInstruction</code>.
     */
    public static final short PROCESSING_INSTRUCTION_NODE = 7;
    /**
     * The node is a <code>Comment</code>.
     */
    public static final short COMMENT_NODE              = 8;
    /**
     * The node is a <code>Document</code>.
     */
    public static final short DOCUMENT_NODE             = 9;
    /**
     * The node is a <code>DocumentType</code>.
     */
    public static final short DOCUMENT_TYPE_NODE        = 10;
    /**
     * The node is a <code>DocumentFragment</code>.
     */
    public static final short DOCUMENT_FRAGMENT_NODE    = 11;
    /**
     * The node is a <code>Notation</code>.
     */
    public static final short NOTATION_NODE             = 12;


// this should be overridden as appropriate
    public short getNodeType();

// this should be overridden as appropriate
    public String getNodeValue();

// this should be overridden as appropriate
    public String getNodeName();

// this should be overridden as appropriate
    public Node getFirstChild();

// this should be overridden as appropriate
    public Node getNextSibling();

// this should be overridden as appropriate
    public Node getPreviousSibling();

// this should be overridden as appropriate
    public Node getParentNode();

// this should be overridden as appropriate
    public NodeList getChildNodes();

// this should be overridden as appropriate
    public String getNamespaceURI();

// this should be overridden as appropriate
    public String getPrefix();

}
