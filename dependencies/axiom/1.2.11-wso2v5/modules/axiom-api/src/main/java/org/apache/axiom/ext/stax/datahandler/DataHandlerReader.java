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

package org.apache.axiom.ext.stax.datahandler;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;

/**
 * Extension interface for {@link javax.xml.stream.XMLStreamReader} implementations that expose
 * base64 encoded binary content as {@link DataHandler} objects.
 * <p>
 * All the requirements specified in {@link org.apache.axiom.ext.stax} apply to
 * this extension interface. In particular,
 * a consumer MUST use {@link javax.xml.stream.XMLStreamReader#getProperty(String)} with the property
 * name defined by {@link #PROPERTY} to get a reference to this extension interface.
 * <p>
 * If the {@link javax.xml.stream.XMLStreamReader} wishes to expose base64 encoded content using
 * this extension interface, it MUST do so using a single
 * {@link javax.xml.stream.XMLStreamConstants#CHARACTERS} event. To maintain compatibility with
 * consumers that are unaware of the extension, the implementation SHOULD make sure that
 * {@link javax.xml.stream.XMLStreamReader#getText()},
 * {@link javax.xml.stream.XMLStreamReader#getTextStart()},
 * {@link javax.xml.stream.XMLStreamReader#getTextLength()},
 * {@link javax.xml.stream.XMLStreamReader#getTextCharacters()},
 * {@link javax.xml.stream.XMLStreamReader#getTextCharacters(int, char[], int, int)} and
 * {@link javax.xml.stream.XMLStreamReader#getElementText()} behave as expected for this type of
 * event, i.e. return the base64 representation of the binary content.
 * <p>
 * The extension described by this interface will typically be implemented by XMLStreamReader
 * instances provided by databinding frameworks or XMLStreamReader proxies that enrich a stream of
 * StAX events with binary data existing outside of the XML document (e.g. an XOP/MTOM decoder).
 */
public interface DataHandlerReader {
    /**
     * The name of the property used to look up this extension interface from a
     * {@link javax.xml.stream.XMLStreamReader} implementation.
     */
    String PROPERTY = DataHandlerReader.class.getName();
    
    /**
     * Check whether the current event is a {@link javax.xml.stream.XMLStreamConstants#CHARACTERS}
     * event representing base64 encoded binary content and for which a
     * {@link javax.activation.DataHandler} is available.
     * 
     * @return <code>true</code> if the current event is a
     *         {@link javax.xml.stream.XMLStreamConstants#CHARACTERS} event representing base64
     *         encoded binary content and for which a {@link javax.activation.DataHandler} is
     *         available; <code>false</code> for all other types of events.
     */
    boolean isBinary();

    /**
     * Check if the binary content is eligible for optimization (e.g. using XOP) or if it should
     * be serialized as base64.
     * Calling this method is only meaningful if {@link #isBinary()} returns <code>true</code> for
     * the current event. The behavior of this method is undefined if this is not the case.
     * 
     * @return <code>true</code> if the binary content is eligible for optimization;
     *         <code>false</code> otherwise
     */
    boolean isOptimized();
    
    /**
     * Check whether the {@link javax.xml.stream.XMLStreamReader} supports deferred loading of the
     * binary content for the current event. If this method returns <code>true</code> then a
     * consumer MAY call {@link #getDataHandlerProvider()} and retrieve the
     * {@link javax.activation.DataHandler} later using {@link DataHandlerProvider#getDataHandler()}.
     * Calling this method is only meaningful if {@link #isBinary()} returns <code>true</code> for
     * the current event. The behavior of this method is undefined if this is not the case.
     * 
     * @return <code>true</code> if deferred loading is supported; <code>false</code> otherwise
     */
    boolean isDeferred();
    
    /**
     * Get the content ID of the binary content for the current event, if available. The result of
     * this method is defined if and only if {@link #isBinary()} returns <code>true</code> for the
     * current event.
     * <p>
     * The implementation SHOULD only return a non null value if the content ID has been used
     * previously in an interaction with another component or system. The implementation SHOULD NOT
     * generate a new content ID solely for the purpose of this method.
     * <p>
     * If available, the returned value MUST be a raw content ID. In particular:
     * <ul>
     * <li>If the content ID has been extracted from an <tt>href</tt> attribute, it MUST NOT
     * contain the <tt>cid:</tt> prefix.</li>
     * <li>If it has been extracted from a <tt>Content-ID</tt> MIME header, it MUST NOT be
     * enclosed in angles (<tt>&lt;></tt>).</li>
     * </ul>
     * <p>
     * A consumer MAY use the return value of this method in contexts where it is desirable to
     * preserve the original content ID used by another system or component to identify the binary
     * content. However, the consumer MUST NOT make any assumption about the uniqueness or validity
     * of the content ID (with respect to relevant standards such as RFC822) and SHOULD make
     * provision to sanitize the value if necessary.
     * 
     * @return any content ID used previously to identify the binary content, or <code>null</code>
     *         if no content ID is known
     */
    String getContentID();
    
    /**
     * Get the {@link DataHandler} with the binary content for the current event. The behavior of
     * this method is only defined for events for which {@link #isBinary()} returns
     * <code>true</code>. For events of this type the method MUST return a valid
     * {@link DataHandler}, regardless of the return value of {@link #isDeferred()}. If
     * {@link #isDeferred()} returns <code>true</code>, then the consumer may use this method to
     * force the implementation to load the binary content immediately.
     * 
     * @return the binary content for the current event
     * 
     * @throws XMLStreamException if an error occurs while loading the {@link DataHandler}
     */
    DataHandler getDataHandler() throws XMLStreamException;
    
    /**
     * Get a {@link DataHandlerProvider} instance for deferred loading of the binary content for the
     * current event. The behavior of this method is defined if and only if {@link #isDeferred()}
     * returns <code>true</code> for the current event. The returned reference MUST remain valid
     * after the current event has been consumed. It is up to the implementation to specify the
     * exact lifecycle of the returned instance, in particular until when the binary content can be
     * retrieved.
     * 
     * @return the {@link DataHandlerProvider} instance the consumer can use to load the binary
     *         content at a later time
     */
    DataHandlerProvider getDataHandlerProvider();
}
