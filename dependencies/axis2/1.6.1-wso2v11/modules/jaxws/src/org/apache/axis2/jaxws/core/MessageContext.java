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

package org.apache.axis2.jaxws.core;

import org.apache.axiom.om.util.DetachableInputStream;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.jaxws.api.MessageAccessor;
import org.apache.axis2.jaxws.api.MessageAccessorFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.handler.MEPContext;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.util.MessageUtils;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.WebServiceException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * The <code>org.apache.axis2.jaxws.core.MessageContext</code> is an interface that extends the
 * JAX-WS 2.0 <code>javax.xml.ws.handler.MessageContext</code> defined in the spec.  This
 * encapsulates all of the functionality needed of the MessageContext for the other JAX-WS spec
 * pieces (the handlers for example) and also provides the needed bits of contextual information for
 * the rest of the JAX-WS implementation.
 * <p/>
 * Specifically, this is responsible for providing APIs so that the client and server implementation
 * portions can get to the Message, defined by the Message Model format and also any metadata that
 * is available.
 */
public class MessageContext {

    private static Log log = LogFactory.getLog(MessageContext.class);
    
    private InvocationContext invocationCtx;
    private org.apache.axis2.context.MessageContext axisMsgCtx;
    private EndpointDescription endpointDesc;
    private OperationDescription operationDesc;
    private QName operationName;    //FIXME: This should become the OperationDescription
    private Message message;
    private Mode mode;
    private boolean isOutbound;  // Outbound or inbound message context
    private boolean isServer = false; // Indicate if server role, default is false
    
    /*
     * JAXWS runtime uses a request and response mc, but we need to know the pair.
     * We will use this mepCtx as a wrapper to the request and response message contexts
     * where the requestMC and responseMC have the same parent MEPContext to
     * preserve the relationship.
     */
    private MEPContext mepCtx;

    // If a local exception is thrown, the exception is placed on the message context.
    // It is not converted into a Message.
    private Throwable localException = null;
    private AxisFault causedByException = null;

    /**
     * Construct a MessageContext without a prior Axis2 MessageContext
     * (usage outbound dispatch/proxy)
     */
    public MessageContext() {
        axisMsgCtx = new org.apache.axis2.context.MessageContext();
        isOutbound = true;
        
        // Set the MessageAccessor object on the MessageContext so that it can be accessed
        MessageAccessorFactory factory = (MessageAccessorFactory)
                FactoryRegistry.getFactory(MessageAccessorFactory.class);
        if (factory != null) {
            this.setProperty(org.apache.axis2.jaxws.Constants.JAXWS_MESSAGE_ACCESSOR, 
                    factory.createMessageAccessor(this));
        }
    }
    
    /**
     * Construct a MessageContext with a prior MessageContext
     * (usage inbound client/server or outbound server)
     * @param mc
     * @throws WebServiceException
     */
    public MessageContext(org.apache.axis2.context.MessageContext mc) throws WebServiceException {
        // Assume inbound (caller must setOutbound)
        isOutbound = false;

        /*
         * Instead of creating a member MEPContext object every time, we will
         * rely on users of this MessageContext class to create a new
         * MEPContext and call setMEPContext(MEPContext)
         */
        
        if (mc != null) {
            axisMsgCtx = mc;
            message = MessageUtils.getMessageFromMessageContext(mc);
            if (message != null) {
                message.setMessageContext(this);
            }
        } else {
            axisMsgCtx = new org.apache.axis2.context.MessageContext();
        }
        
        // Set the MessageAccessor object on the MessageContext so that it can be accessed
        MessageAccessorFactory factory = (MessageAccessorFactory)
                FactoryRegistry.getFactory(MessageAccessorFactory.class);
        if (factory != null) {
            this.setProperty(org.apache.axis2.jaxws.Constants.JAXWS_MESSAGE_ACCESSOR, 
                    factory.createMessageAccessor(this));
        }
    }

    public InvocationContext getInvocationContext() {
        return invocationCtx;
    }

    public void setInvocationContext(InvocationContext ic) {
        invocationCtx = ic;
    }

    public Map<String, Object> getProperties() {
        // only use properties that are local to the axis2 MC,
        // not the options bag.  See org.apache.axis2.context.AbstractContext
        Iterator names = axisMsgCtx.getPropertyNames();
        HashMap tempProps = new HashMap<String, Object>();
        for (; names.hasNext();) {
            String name = (String)names.next();
            tempProps.put(name, axisMsgCtx.getProperty(name));
        }
        //return new ReadOnlyProperties(tempProps);
        return tempProps;
    }
    
    public void setProperties(Map<String, Object> _properties) {
        // make sure copy is made, not just reference:
        _properties.put(org.apache.axis2.context.MessageContext.COPY_PROPERTIES, true);
        axisMsgCtx.setProperties(_properties);
    }
    
    public Object getProperty(String key) {
        // only use properties that are local to the axis2 MC.
        return axisMsgCtx.getLocalProperty(key, false);
    }
    
    public boolean containsKey(Object key) {
        // Only use properties that are local to the axis2 MC.
        // @see getProperty(String key)
        return (key instanceof String && getProperty((String)key) != null);
    }
    
    // acts like Map.put(key, value)
    public Object setProperty(String key, Object value) {
        // only use properties that are local to the axis2 MC
        Object retval = axisMsgCtx.getLocalProperty(key, false);
        axisMsgCtx.setProperty(key, value);
        return retval;
    }
    
    /**
     * Like getProperty, but does not return prior value.
     * This method should be used in scenarios where
     * the prior value is not needed.
     * @param key String
     * @param value Object
     */
    public void setPropertyNoReturn(String key, Object value) {
        axisMsgCtx.setProperty(key, value);
    }

    public EndpointDescription getEndpointDescription() {
        return endpointDesc;
    }

    public void setEndpointDescription(EndpointDescription ed) {
        endpointDesc = ed;
    }

    public OperationDescription getOperationDescription() {
        return operationDesc;
    }

    public void setOperationDescription(OperationDescription od) {
        operationDesc = od;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode m) {
        mode = m;
    }

    //FIXME: This should become the OperationDescription
    public QName getOperationName() {
        return operationName;
    }

    //FIXME: This should become the OperationDescription
    public void setOperationName(QName op) {
        operationName = op;
    }

    public void setMessage(Message msg) {
        message = msg;
        msg.setMessageContext(this);
    }

    public Message getMessage() {
        return message;
    }

    public org.apache.axis2.context.MessageContext getAxisMessageContext() {
        return axisMsgCtx;
    }

    public ClassLoader getClassLoader() {
        AxisService svc = axisMsgCtx.getAxisService();
        if (svc != null)
            return svc.getClassLoader();
        else
            return null;
    }

    /**
     * Used to determine whether or not session state has been enabled.
     *
     * @return
     */
    public boolean isMaintainSession() {
        boolean maintainSession = false;

        Boolean value = (Boolean) getProperty(BindingProvider.SESSION_MAINTAIN_PROPERTY);
        if (value != null && value.booleanValue()) {
            maintainSession = true;
        }

        return maintainSession;
    }

    /**
     * The local exception is the Throwable object held on the Message from a problem that occurred
     * due to something other than the server.  In other words, no message ever travelled across
     * the wire.
     *
     * @return the Throwable object or null
     */
    public Throwable getLocalException() {
        return localException;
    }

    /**
     * The local exception is the Throwable object held on the Message from a problem that occurred
     * due to something other than the server.  In other words, no message ever travelled across the
     * wire.
     *
     * @param t
     * @see Throwable
     */
    public void setLocalException(Throwable t) {
        localException = t;
    }
    
    /**
     * @param t
     */
    public void setCausedByException (AxisFault t){
        this.causedByException = t;
    }
    
    /**
     * @return
     */
    public AxisFault getCausedByException(){
        return this.causedByException;
    }
    

    /**
     * Set the wrapper MEPContext.  Internally, this method also sets
     * the MEPContext's children so the pointer is bi-directional; you can
     * get the MEPContext from the MessageContext and vice-versa.
     * 
     * @param mepCtx
     */
    public void setMEPContext(MEPContext mepCtx) {
        if (this.mepCtx == null) {
            this.mepCtx = mepCtx;
            // and set parent's child pointer
            this.mepCtx.setResponseMessageContext(this);
        }
    }

    public MEPContext getMEPContext() {
        if (mepCtx == null) {
            setMEPContext(new MEPContext(this));
        }
        return mepCtx;
    }
    
    /**
     * @return if outbound MessageContext
     */
    public boolean isOutbound() {
        return isOutbound;
    }

    /**
     * @param isOutbound true if outbound MessageContext
     */
    public void setOutbound(boolean isOutbound) {
        this.isOutbound = isOutbound;
    }

    /**
     * @return true if server role
     */
    public boolean isServer() {
        return isServer;
    }

    /**
     * Indicate if server role
     * @param isServer
     */
    public void setServer(boolean isServer) {
        this.isServer = isServer;
    }

    /**
     * Free the resources associated with the incoming input stream. (i.e. HTTPInputStream)
     * This allows the transport layer to free resources and pool connections
     */
    public void freeInputStream() throws IOException {
        
        
        // During builder processing, the original input stream was wrapped with
        // a detachable input stream.  The detachable input stream's detach method
        // causes the original stream to be consumed and closed.
        DetachableInputStream is = (DetachableInputStream) 
            getProperty(Constants.DETACHABLE_INPUT_STREAM);
        if (is != null) {
            if (log.isDebugEnabled()) {
                log.debug("Detaching inbound input stream " + is);
            }
            is.detach();
        }
    }
}
