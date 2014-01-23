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

package org.apache.axiom.om.impl;

import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMSerializable;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Interface OMNodeEx
 * <p/>
 * Internal Implementation detail. Adding special interface to stop folks from accidently using
 * OMNode. Please use at your own risk. May corrupt the data integrity.
 */
public interface OMNodeEx extends OMNode {
    public void setNextOMSibling(OMNode node);

    public void setPreviousOMSibling(OMNode previousSibling);

    public void setParent(OMContainer element);

    public void setComplete(boolean state);

    public void setType(int nodeType) throws OMException;

    /**
     * Serializes the node. Note that this is an internal method that MUST NOT be used outside of
     * Axiom. Please use {@link OMSerializable#serialize(XMLStreamWriter, boolean)} instead.
     *
     * @param writer
     * @param cache indicates if caching should be enabled
     * @throws javax.xml.stream.XMLStreamException
     *
     */
    public void internalSerialize(XMLStreamWriter writer, boolean cache)
            throws XMLStreamException;

    /**
     * @deprecated This method will be removed in a future version of Axiom. It is only here to
     *             maintain backward compatibility with projects using this method despite the fact
     *             that it is marked as internal.
     */
    public void internalSerialize(XMLStreamWriter writer) throws XMLStreamException;

    /**
     * @deprecated This method will be removed in a future version of Axiom. It is only here to
     *             maintain backward compatibility with projects using this method despite the fact
     *             that it is marked as internal.
     */
    public void internalSerializeAndConsume(XMLStreamWriter writer) throws XMLStreamException;
    
    /**
     * Get the next sibling if it is available. The sibling is available if it is complete or
     * if the builder has started building the node. In the latter case,
     * {@link OMNode#isComplete()} may return <code>false</code> when called on the sibling. 
     * In contrast to {@link OMNode#getNextOMSibling()}, this method will never modify
     * the state of the underlying parser.
     * 
     * @return the next sibling or <code>null</code> if the node has no next sibling or
     *         the builder has not yet started to build the next sibling
     */
    public OMNode getNextOMSiblingIfAvailable();
}
