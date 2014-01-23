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

package org.apache.axiom.om;

/** Interface OMXMLParserWrapper */
public interface OMXMLParserWrapper {
    /**
     * Proceed the parser one step and return the event value.
     *
     * @return Returns int.
     * @throws org.apache.axiom.om.OMException
     *
     * @throws OMException
     */
    int next() throws OMException;

    /**
     * Discards the current element. This will remove the given element and its descendants.
     *
     * @param el
     * @throws OMException
     *
     * @throws OMException
     */
    void discard(OMElement el) throws OMException;

    /**
     * @param b
     * @throws org.apache.axiom.om.OMException
     *
     * @throws OMException
     */
    void setCache(boolean b) throws OMException;
    
    /**
     * @return true if caching
     */
    boolean isCache();

    /**
     * Allows to access the underlying parser. Since the parser depends on the underlying
     * implementation, an Object is returned. However the implementations may have restrictions in
     * letting access to the parser.
     *
     * @return Returns Object.
     */
    Object getParser();

    /** @return Returns the complete status. */
    boolean isCompleted();

    /**
     * Get the document being built by this builder.
     * 
     * @return the {@link OMDocument} instance
     */
    OMDocument getDocument();
    
    /**
     * Get the document element, i.e. the root element of the document. Using this method is
     * equivalent to using {@link OMDocument#getOMDocumentElement()} on the document returned by
     * {@link #getDocument()}.
     * 
     * @return the document element
     */
    OMElement getDocumentElement();

    /**
     * Get the document element, optionally discarding the document. The return value of this method
     * is the same as {@link #getDocumentElement()}. However, if the <code>discardDocument</code>
     * parameter is set to <code>true</code>, then the document element is removed from the document
     * and the document itself is discarded. In contrast to using {@link OMElement#detach()} this
     * will not build the element. The implementation also ensures that the element is not built
     * when it is added to another OM tree. This makes it possible to add the content of a document
     * to an existing OM tree while preserving the deferred parsing feature. It is even possible to
     * create an OM tree where different subtrees are associated with different builder instances.
     * 
     * @param discardDocument
     *            specifies whether the document should be discarded
     * @return the document element
     */
    OMElement getDocumentElement(boolean discardDocument);

    /**
     * Returns the type of the builder. Can be either {@link OMConstants#PUSH_TYPE_BUILDER}
     * or {@link OMConstants#PULL_TYPE_BUILDER}.
     *
     * @return Returns short.
     */
    short getBuilderType();

    /**
     * Registers an external content handler. Especially useful for push type builders. Throws an
     * unsupportedOperationException if such handler registration is not supported.
     *
     * @param obj
     */
    void registerExternalContentHandler(Object obj);

    /**
     * get the registered external content handler
     *
     * @return Returns Object.
     */
    Object getRegisteredContentHandler();

    /**
     * Returns the encoding style of the XML data
     * @return the character encoding, defaults to "UTF-8"
     */
    public String getCharacterEncoding();
    
    /**
     * Close this builder. This method frees the resources associated with this builder. In
     * particular, it releases the resources held by the underlying parser. This method does
     * <b>not</b> close the underlying input source.
     */
    void close();
}
