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

package org.apache.axis2.jaxws.server;

import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

/**
 * This class represents static methods that are utilized during the course
 * of invocation of a JAX-WS endpoint. This utility class is specifically
 * meant to be used within the JAX-WS server-side flow.
 *
 */
public class InvocationHelper {
    
    private static final Log log = LogFactory.getLog(InvocationHelper.class);
    
    /**
     * This method is responsible for driving the method below. It will appropriately
     * wrap the MessageContext in an EndpointInvocationContext. The MessageContext 
     * instance MUST be a request MessageContext.
     */
    public static void callListenersForException(Throwable t, MessageContext context) {
        EndpointInvocationContext eic = new EndpointInvocationContextImpl();
        eic.setRequestMessageContext(context);
        eic.setInvocationListeners((List<InvocationListener>)context.getProperty(org.apache.axis2.jaxws.spi.Constants.INVOCATION_LISTENER_LIST));
        callListenersForException(t, eic);
    }
    
    /**
     * This method is responsible for driving the InvocationListener instances'
     * 'notifyOnException' method. This method will be called anytime that an
     * exception occurs within the JAX-WS server side code flow.
     */
    public static void callListenersForException(Throwable t, EndpointInvocationContext eic) {
        List<InvocationListener> invocationListeners = eic.getInvocationListeners();
        Throwable returnException = null;
        if(invocationListeners != null && !invocationListeners.isEmpty()) {
            InvocationListenerBean bean = new InvocationListenerBean();
            bean.setEndpointInvocationContext(eic);
            bean.setState(eic.getResponseMessageContext() != null ? InvocationListenerBean.State.RESPONSE : 
                InvocationListenerBean.State.REQUEST);
            bean.setThrowable(t);
            for(InvocationListener listener : invocationListeners) {
                listener.notifyOnException(bean);
            }
        }
    }
    
    /**
     * This method will drive the call to the above methods. It will drive the call to
     * the 'notifyOnException' methods of all InvocationListeners. After doing this, it
     * will determine if another exception has been set as the mapped exception, and if
     * so it will return this exception. Otherwise, null is returned.
     */
    public static Throwable determineMappedException(Throwable t, EndpointInvocationContext eic) {
        Throwable returnThrowable = null;
        callListenersForException(t, eic);
        MessageContext requestContext = eic.getRequestMessageContext();
        MessageContext responseContext = eic.getResponseMessageContext();
        if(requestContext != null 
                && 
                requestContext.getProperty(org.apache.axis2.jaxws.spi.Constants.MAPPED_EXCEPTION) != null) {
            returnThrowable = (Throwable) requestContext.getProperty(org.apache.axis2.jaxws.spi.Constants.MAPPED_EXCEPTION);
            if(log.isDebugEnabled()) {
                log.debug("Setting mapped exception to: " + returnThrowable + " from request " +
                		"MessageContext");
            }
        }
        else if(responseContext != null
                &&
                responseContext.getProperty(org.apache.axis2.jaxws.spi.Constants.MAPPED_EXCEPTION) != null) {
            returnThrowable = (Throwable) responseContext.getProperty(org.apache.axis2.jaxws.spi.Constants.MAPPED_EXCEPTION);
            if(log.isDebugEnabled()) {
                log.debug("Setting mapped exception to: " + returnThrowable + " from response " +
                                "MessageContext");
            }
        }
        return returnThrowable;
    }
    
    /**
     * This method will drive the call to the above methods. It will drive the call to
     * the 'notifyOnException' methods of all InvocationListeners. After doing this, it
     * will determine if another exception has been set as the mapped exception, and if
     * so it will return this exception. Otherwise, null is returned.
     */
    public static Throwable determineMappedException(Throwable t, MessageContext context) {
        EndpointInvocationContext eic = (EndpointInvocationContext)context.getInvocationContext();
        if (eic == null) {
            eic = new EndpointInvocationContextImpl();
            eic.setRequestMessageContext(context);
        }
        eic.setInvocationListeners((List<InvocationListener>)context.getProperty(org.apache.axis2.jaxws.spi.Constants.INVOCATION_LISTENER_LIST));
        return determineMappedException(t, eic);
    }

}
