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

package org.apache.axis2.saaj;

import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.impl.dom.DOOMAbstractFactory;
import org.apache.axiom.om.impl.dom.TextImpl;
import org.w3c.dom.DOMException;

import javax.xml.soap.SOAPElement;
import javax.xml.soap.Text;
import javax.xml.stream.XMLStreamException;

public class TextImplEx extends NodeImplEx implements Text {

    //TODO: assign textNode

    private TextImpl textNode;

    private org.w3c.dom.Node previousSibling;
    private org.w3c.dom.Node nextSibling;

    public TextImplEx(String data, SOAPElement parent) {
        super(((SOAPElementImpl)parent).getOMFactory());
        textNode = (TextImpl)DOOMAbstractFactory.getOMFactory().createOMText(data);
        this.parentElement = parent;
    }

    public TextImplEx(TextImpl textNode, SOAPElement parent) {
        super(((SOAPElementImpl)parent).getOMFactory());
        this.textNode = textNode;
        this.parentElement = parent;
    }

    public TextImplEx(String data, SOAPElement parent,
                      org.w3c.dom.Node prevSibling, org.w3c.dom.Node nextSibling) {
        super(((SOAPElementImpl)parent).getOMFactory());
        textNode = (TextImpl)DOOMAbstractFactory.getOMFactory().createOMText(data);
        this.parentElement = parent;
        this.previousSibling = prevSibling;
        this.nextSibling = nextSibling;
    }

    TextImpl getTextNode() {
        return textNode;
    }
    
    public void setNextSibling(org.w3c.dom.Node nextSibling) {
        this.nextSibling = nextSibling;
    }

    public void setPreviousSibling(org.w3c.dom.Node previousSibling) {
        this.previousSibling = previousSibling;
    }

    /**
     * Retrieves whether this <CODE>Text</CODE> object represents a comment.
     *
     * @return <CODE>true</CODE> if this <CODE>Text</CODE> object is a comment; <CODE>false</CODE>
     *         otherwise
     */
    public boolean isComment() {
        String value = textNode.getText();
        return value.startsWith("<!--") && value.endsWith("-->");
    }

    /** The name of this node, depending on its type; see the table above. */
    public String getNodeName() {
        return textNode.getNodeName();
    }

    /** A code representing the type of the underlying object, as defined above. */
    public short getNodeType() {
        return textNode.getNodeType();
    }

    /**
     * Breaks this node into two nodes at the specified <code>offset</code>, keeping both in the
     * tree as siblings. After being split, this node will contain all the content up to the
     * <code>offset</code> point. A new node of the same type, which contains all the content at and
     * after the <code>offset</code> point, is returned. If the original node had a parent node, the
     * new node is inserted as the next sibling of the original node. When the <code>offset</code>
     * is equal to the length of this node, the new node has no data.
     *
     * @param offset The 16-bit unit offset at which to split, starting from <code>0</code>.
     * @return The new node, of the same type as this node.
     * @throws DOMException INDEX_SIZE_ERR: Raised if the specified offset is negative or greater
     *                      than the number of 16-bit units in <code>data</code>.
     *                      <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     */
    public org.w3c.dom.Text splitText(int offset) throws DOMException {
        return textNode.splitText(offset);
    }

    public boolean isElementContentWhitespace() {
        // TODO - Fixme.
        throw new UnsupportedOperationException("TODO");
    }

    public String getWholeText() {
        // TODO - Fixme.
        throw new UnsupportedOperationException("TODO");
    }

    public org.w3c.dom.Text replaceWholeText(String content) throws DOMException {
        // TODO - Fixme.
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * The character data of the node that implements this interface. The DOM implementation may not
     * put arbitrary limits on the amount of data that may be stored in a <code>CharacterData</code>
     * node. However, implementation limits may mean that the entirety of a node's data may not fit
     * into a single <code>DOMString</code>. In such cases, the user may call
     * <code>substringData</code> to retrieve the data in appropriately sized pieces.
     *
     * @throws DOMException NO_MODIFICATION_ALLOWED_ERR: Raised when the node is readonly.
     * @throws DOMException DOMSTRING_SIZE_ERR: Raised when it would return more characters than fit
     *                      in a <code>DOMString</code> variable on the implementation platform.
     */
    public String getData() throws DOMException {
        return textNode.getData();
    }

    /**
     * The character data of the node that implements this interface. The DOM implementation may not
     * put arbitrary limits on the amount of data that may be stored in a <code>CharacterData</code>
     * node. However, implementation limits may mean that the entirety of a node's data may not fit
     * into a single <code>DOMString</code>. In such cases, the user may call
     * <code>substringData</code> to retrieve the data in appropriately sized pieces.
     *
     * @throws DOMException NO_MODIFICATION_ALLOWED_ERR: Raised when the node is readonly.
     * @throws DOMException DOMSTRING_SIZE_ERR: Raised when it would return more characters than fit
     *                      in a <code>DOMString</code> variable on the implementation platform.
     */
    public void setData(String data) throws DOMException {
        textNode.setData(data);
    }

    /**
     * Extracts a range of data from the node.
     *
     * @param offset Start offset of substring to extract.
     * @param count  The number of 16-bit units to extract.
     * @return The specified substring. If the sum of <code>offset</code> and <code>count</code>
     *         exceeds the <code>length</code>, then all 16-bit units to the end of the data are
     *         returned.
     * @throws DOMException INDEX_SIZE_ERR: Raised if the specified <code>offset</code> is negative
     *                      or greater than the number of 16-bit units in <code>data</code>, or if
     *                      the specified <code>count</code> is negative. <br>DOMSTRING_SIZE_ERR:
     *                      Raised if the specified range of text does not fit into a
     *                      <code>DOMString</code>.
     */
    public String substringData(int offset, int count) throws DOMException {
        return textNode.substringData(offset, count);
    }

    /**
     * Append the string to the end of the character data of the node. Upon success,
     * <code>data</code> provides access to the concatenation of <code>data</code> and the
     * <code>DOMString</code> specified.
     *
     * @param value The <code>DOMString</code> to append.
     * @throws DOMException NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     */
    public void appendData(String value) throws DOMException {
        textNode.appendData(value);
    }

    /**
     * Insert a string at the specified 16-bit unit offset.
     *
     * @param offset The character offset at which to insert.
     * @param data   The <code>DOMString</code> to insert.
     * @throws DOMException INDEX_SIZE_ERR: Raised if the specified <code>offset</code> is negative
     *                      or greater than the number of 16-bit units in <code>data</code>.
     *                      <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     */
    public void insertData(int offset, String data) throws DOMException {
        textNode.insertData(offset, data);
    }

    /**
     * Remove a range of 16-bit units from the node. Upon success, <code>data</code> and
     * <code>length</code> reflect the change.
     *
     * @param offset The offset from which to start removing.
     * @param count  The number of 16-bit units to delete. If the sum of <code>offset</code> and
     *               <code>count</code> exceeds <code>length</code> then all 16-bit units from
     *               <code>offset</code> to the end of the data are deleted.
     * @throws DOMException INDEX_SIZE_ERR: Raised if the specified <code>offset</code> is negative
     *                      or greater than the number of 16-bit units in <code>data</code>, or if
     *                      the specified <code>count</code> is negative. <br>NO_MODIFICATION_ALLOWED_ERR:
     *                      Raised if this node is readonly.
     */
    public void deleteData(int offset, int count) throws DOMException {
        textNode.deleteData(offset, count);
    }

    /**
     * Replace the characters starting at the specified 16-bit unit offset with the specified
     * string.
     *
     * @param offset The offset from which to start replacing.
     * @param count  The number of 16-bit units to replace. If the sum of <code>offset</code> and
     *               <code>count</code> exceeds <code>length</code>, then all 16-bit units to the
     *               end of the data are replaced; (i.e., the effect is the same as a
     *               <code>remove</code> method call with the same range, followed by an
     *               <code>append</code> method invocation).
     * @param data   The <code>DOMString</code> with which the range must be replaced.
     * @throws DOMException INDEX_SIZE_ERR: Raised if the specified <code>offset</code> is negative
     *                      or greater than the number of 16-bit units in <code>data</code>, or if
     *                      the specified <code>count</code> is negative. <br>NO_MODIFICATION_ALLOWED_ERR:
     *                      Raised if this node is readonly.
     */
    public void replaceData(int offset, int count, String data) throws DOMException {
        textNode.replaceData(offset, count, data);
    }

    /* (non-Javadoc)
      * @see org.apache.axiom.om.impl.OMNodeEx#setParent(org.apache.axiom.om.OMContainer)
      */
    public void setParent(OMContainer element) {
        textNode.setParent(element);
    }

    /* (non-Javadoc)
      * @see org.apache.axiom.om.OMNode#getParent()
      */
    public OMContainer getParent() {
        return textNode.getParent();
    }

    /* (non-Javadoc)
      * @see org.apache.axiom.om.OMNode#discard()
      */
    public void discard() throws OMException {
        textNode.discard();
    }

    public void internalSerialize(javax.xml.stream.XMLStreamWriter writer, boolean cache)
            throws XMLStreamException {
        textNode.internalSerialize(writer, cache);
    }

    /**
     * Retrieve the text value (data) of this
     *
     * @return The text value (data) of this
     */
    public String getValue() {
        return textNode.getData();
    }

    public String getNodeValue() {
        return textNode.getData();
    }

    /**
     * If this is a Text node then this method will set its value, otherwise it sets the value of
     * the immediate (Text) child of this node. The value of the immediate child of this node can be
     * set only if, there is one child node and that node is a Text node, or if there are no
     * children in which case a child Text node will be created.
     *
     * @param value the text to set
     * @throws IllegalStateException if the node is not a Text  node and either has more than one
     *                               child node or has a child node that is not a Text node
     */
    public void setValue(String value) {
        textNode.setData(value);
    }

    public void setNodeValue(String value) {
        textNode.setData(value);
    }

    public String toString() {
        return getValue();
    }


    public org.w3c.dom.Node getNextSibling() {
        return toSAAJNode(nextSibling);
    }


    public org.w3c.dom.Node getPreviousSibling() {
        return toSAAJNode(previousSibling);
    }
}
