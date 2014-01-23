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
import org.apache.axiom.om.OMNode;

/**
 * Interface OMContainerEx
 * <p/>
 * Internal Implementation detail. Adding special interface to stop folks from accidently using
 * OMContainer. Please use at your own risk. May corrupt the data integrity.
 */
public interface OMContainerEx extends OMContainer {
    public void setComplete(boolean state);

    /**
     * forcefully set the first element in this parent element
     * @param omNode
     */
    public void setFirstChild(OMNode omNode);

    /**
     * forcefully set the last element in this parent element
     * @param omNode
     */
    public void setLastChild(OMNode omNode);
    
    /**
     * Get the first child if it is available. The child is available if it is complete or
     * if the builder has started building the node. In the latter case,
     * {@link OMNode#isComplete()} may return <code>false</code> when called on the child. 
     * In contrast to {@link OMContainer#getFirstOMChild()}, this method will never modify
     * the state of the underlying parser.
     * 
     * @return the first child or <code>null</code> if the container has no children or
     *         the builder has not yet started to build the first child
     */
    public OMNode getFirstOMChildIfAvailable();
}
