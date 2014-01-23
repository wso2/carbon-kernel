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

import org.apache.axiom.om.OMFactory;
import org.w3c.dom.CharacterData;
import org.w3c.dom.DOMException;

/**
 * This implements the OMText operations which are to be inherited by TextImpl, CommentImpl,
 * CDATASectionImpl.
 */
public abstract class CharacterImpl extends ChildNode implements CharacterData {

    protected String textValue;

    protected CharacterImpl(OMFactory factory) {
        super(factory);
    }

    /** @param ownerNode  */
    public CharacterImpl(DocumentImpl ownerNode, OMFactory factory) {
        super(ownerNode, factory);
    }

    public CharacterImpl(DocumentImpl ownerNode, String value, OMFactory factory) {
        super(ownerNode, factory);
        this.textValue = (value != null) ? value : "";
    }

    ///
    ///org.w3c.dom.CharacterData mrthods
    ///

    public void appendData(String value) throws DOMException {
                      
        if (this.isReadonly()) {
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                   DOMMessageFormatter.formatMessage(
                                           DOMMessageFormatter.DOM_DOMAIN,
                                           DOMException.NO_MODIFICATION_ALLOWED_ERR, null));
        }

        this.textValue += value;
    }

    /**
     *
     */
    public void deleteData(int offset, int count) throws DOMException {
        this.replaceData(offset, count, null);
    }

    /** If the given data is null the content will be deleted. */
    public void replaceData(int offset, int count, String data) throws
            DOMException {

        if (this.isReadonly()) {
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                   DOMMessageFormatter.formatMessage(
                                           DOMMessageFormatter.DOM_DOMAIN,
                                           DOMException.NO_MODIFICATION_ALLOWED_ERR, null));
        }

        int length = this.textValue.length();
        if (offset < 0 || offset > length - 1 || count < 0) {
            throw new DOMException(DOMException.INDEX_SIZE_ERR,
                                   DOMMessageFormatter.formatMessage(
                                           DOMMessageFormatter.DOM_DOMAIN, DOMException.INDEX_SIZE_ERR,
                                           null));
        } else {

            int end = Math.min(count + offset, length);

            if (data == null) {
                this.textValue = (new StringBuilder(textValue)).delete(offset, end).toString();
            } else {
                this.textValue = (new StringBuilder(textValue)).replace(offset, end, data).toString();
            }
        }

    }


    /** Returns the value of the data. */
    public String getData() throws DOMException {
        return (this.textValue != null) ? this.textValue : "";
    }

    /** Inserts a string at the specified offset. */
    public void insertData(int offset, String data) throws DOMException {
        int length = this.getLength();

        if (this.isReadonly()) {
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                   DOMMessageFormatter.formatMessage(
                                           DOMMessageFormatter.DOM_DOMAIN,
                                           DOMException.NO_MODIFICATION_ALLOWED_ERR, null));
        }

        if (offset < 0 || offset > length - 1) {
            throw new DOMException(DOMException.INDEX_SIZE_ERR,
                                   DOMMessageFormatter.formatMessage(
                                           DOMMessageFormatter.DOM_DOMAIN,
                                           DOMException.INDEX_SIZE_ERR, null));
        }

        this.textValue = (new StringBuilder(textValue)).insert(offset, data).toString();
    }

    /** Sets the text value of data. */
    public void setData(String data) throws DOMException {
        if (!this.isReadonly()) {
            this.textValue = data;
        } else {
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                   DOMMessageFormatter.formatMessage(
                                           DOMMessageFormatter.DOM_DOMAIN,
                                           DOMException.NO_MODIFICATION_ALLOWED_ERR, null));
        }
    }

    /**
     * Extracts a range of data from the node.
     *
     * @return Returns the specified substring. If the sum of offset and count exceeds the length, then
     *         all 16-bit units to the end of the data are returned.
     */
    public String substringData(int offset, int count) throws DOMException {
        if (offset < 0 || offset > this.getLength() || count < 0) {
            throw new DOMException(DOMException.INDEX_SIZE_ERR,
                                   DOMMessageFormatter.formatMessage(
                                           DOMMessageFormatter.DOM_DOMAIN,
                                           DOMException.INDEX_SIZE_ERR, null));
        }

        int end = Math.min(count + offset, textValue.length());
        return this.textValue.substring(offset, end);
    }

    /**
     * Returns the length of the string value.
     */
    public int getLength() {
        return (this.textValue != null) ? this.textValue.length() : 0;
	}
		
}
