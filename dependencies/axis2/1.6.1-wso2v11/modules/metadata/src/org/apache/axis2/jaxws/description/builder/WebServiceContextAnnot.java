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

package org.apache.axis2.jaxws.description.builder;

import org.w3c.dom.Element;

import javax.xml.ws.EndpointReference;
import javax.xml.ws.handler.MessageContext;
import java.lang.annotation.Annotation;
import java.security.Principal;

public class WebServiceContextAnnot implements javax.xml.ws.WebServiceContext {

    private MessageContext messageContext;
    private Principal userPrincipal;
    private boolean isUserInRole = false;

    /** A WebServiceContextAnnot cannot be instantiated. */
    private WebServiceContextAnnot() {

    }

    private WebServiceContextAnnot(
            MessageContext messageContext,
            Principal userPrincipal,
            boolean isUserInRole) {
        this.messageContext = messageContext;
        this.userPrincipal = userPrincipal;
        this.isUserInRole = isUserInRole;
    }

    /**
     * @param role The role to check.
     * @return Returns boolean indicating whether user is in Role
     */
    public boolean isUserInRole(String role) {
        return isUserInRole;
    }

    /** @return Returns the messageContext. */
    public MessageContext getMessageContext() {
        return messageContext;
    }

    /** @return Returns the userPrincipal. */
    public Principal getUserPrincipal() {
        return userPrincipal;
    }

    /** @param isUserInRole The isUserInRole to set. */
    public void setUserInRole(boolean isUserInRole) {
        this.isUserInRole = isUserInRole;
    }

    /** @param messageContext The messageContext to set. */
    public void setMessageContext(MessageContext messageContext) {
        this.messageContext = messageContext;
    }

    /** @param userPrincipal The userPrincipal to set. */
    public void setUserPrincipal(Principal userPrincipal) {
        this.userPrincipal = userPrincipal;
    }

    public <T extends EndpointReference> T getEndpointReference(Class<T> arg0, Element... arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public EndpointReference getEndpointReference(Element... arg0) {
        // TODO Auto-generated method stub
        return null;
    }


    //hmm, should we really do this
    public Class<Annotation> annotationType() {
        return Annotation.class;
    }
	
}
