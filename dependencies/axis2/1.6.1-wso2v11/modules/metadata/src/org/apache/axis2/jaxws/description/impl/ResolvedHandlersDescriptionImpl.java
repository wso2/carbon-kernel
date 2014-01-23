/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.description.impl;

import org.apache.axis2.jaxws.description.ResolvedHandlersDescription;

import java.util.List;
/**
 * Implementation of the ResolvedHandlersDescription interface 
 */
public class ResolvedHandlersDescriptionImpl implements ResolvedHandlersDescription {
    private List<Class> handlerClasses;
    private List<String> roles; 

    /* (non-Javadoc)
     * @see org.apache.axis2.jaxws.description.ResolvedHandlersDescription#addHandlerClass(java.lang.Class)
     */
    public void setHandlerClasses(List<Class> handlerClasses) {
        this.handlerClasses = handlerClasses;
    }

    /* (non-Javadoc)
     * @see org.apache.axis2.jaxws.description.ResolvedHandlersDescription#addRole(java.lang.String)
     */
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    /* (non-Javadoc)
     * @see org.apache.axis2.jaxws.description.ResolvedHandlersDescription#getHandlerClasses()
     */
    public List<Class> getHandlerClasses() {
        return this.handlerClasses;
    }

    /* (non-Javadoc)
     * @see org.apache.axis2.jaxws.description.ResolvedHandlersDescription#getRoles()
     */
    public List<String> getRoles() {
        return this.roles;
    }

}
