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

/**
 * Element whose data is backed by an arbitrary Java object. The backing Java object is accessed
 * via the {@link OMDataSource} (or {@link OMDataSourceExt}) interface.
 * <p>
 * An OMSourcedElement can be in one of two states:
 * <dl>
 *   <dt>Not Expanded</dt>
 *   <dd>In this state the backing object is used to read and write the XML.</dd>
 *   <dt>Expanded</dt>
 *   <dd>In this state, the OMSourcedElement is backed by a normal OM tree.</dd>
 * </dl>
 * <p>
 * Here are the steps to place an arbitrary java object into the OM tree:
 * <ol>
 *   <li>Write an {@link OMDataSourceExt} implementation that provides access to your Java
 *       object.</li>
 *   <li>Use {@link OMFactory#createOMElement(OMDataSource, String, OMNamespace)} to create
 *       the OMSourcedElement.</li>
 *   <li>Attach the OMSourcedElement to the tree.</li>
 * </ol>
 */
public interface OMSourcedElement extends OMElement {
    
    /**
     * @return true if tree is expanded or being expanded.
     */
    public boolean isExpanded();
    
    /**
     * @return OMDataSource
     */
    public OMDataSource getDataSource();
    
    /**
     * Replace an existing OMDataSource with a new one. 
     * @param dataSource new OMDataSource
     * @return null or old OMDataSource
     */
    public OMDataSource setDataSource(OMDataSource dataSource);
    
} 
