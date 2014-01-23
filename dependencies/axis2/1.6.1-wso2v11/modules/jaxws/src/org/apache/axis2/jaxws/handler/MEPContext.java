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

package org.apache.axis2.jaxws.handler;

import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.message.Message;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * The <tt>MEPContext</tt> is the version of the MessageContext
 * that will be given to application handlers as the handler list 
 * is traversed.  It is only to be used by application handlers.
 * 
 * The MEPContext object is constructed using a non-null request
 * context.  Once the request has been fully processed in the JAX-WS engine,
 * the response context should be set on this.  Since the response context
 * is always last, it takes priority in all MEPContext methods.
 * 
 */
public class MEPContext implements javax.xml.ws.handler.MessageContext {

    // If this a request flow, then the MEP contains the request MC.
    // If this a response flow, then the MEP contains both the request MC and the response MC.
    // (Note that access to the requestMC properties is sometimes synchronized in the
    // response flow.)
    protected MessageContext requestMC;
    protected MessageContext responseMC;
    
    private Map<String, Scope> scopes;  // APPLICATION or HANDLER scope for properties
    
    /*
     * Flag to indicate whether we're being called from a handler or an application
     * (endpoint or client).  Users of MEPContext should use the 'is' and
     * 'set' appropriately for this flag.  The most likely scenario is to set the
     * flag to true after the server-side inbound handlers are complete.
     * 
     * TODO, all methods should use this flag to check for access rights
     */
    private boolean ApplicationAccessLocked = false;

    /*
     * Ideally this would be "protected", but we want the junit tests to see it.
     */
    public MEPContext(MessageContext requestMsgCtx) {
        this.requestMC = requestMsgCtx;
        scopes = new HashMap<String, Scope>();
        // make sure the MessageContext points back to this
        requestMsgCtx.setMEPContext(this);
    }
    
    public EndpointDescription getEndpointDesc() {
        if (responseMC != null) {
            return responseMC.getEndpointDescription();
        }
        return requestMC.getEndpointDescription();
    }

    public MessageContext getRequestMessageContext() {
        return requestMC;
    }
    
    public MessageContext getResponseMessageContext() {
        return responseMC;
    }
    
    public MessageContext getMessageContext() {
        if (responseMC != null) {
            return responseMC;
        }
        return requestMC;
    }
    
    public void setResponseMessageContext(MessageContext responseMC) {
        if(this.responseMC != null) {
            responseMC.setProperty(javax.xml.ws.handler.MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS, 
                    this.responseMC.getProperty(javax.xml.ws.handler.MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS));
        }
        // TODO does ApplicationAccessLocked mean anything here? -- method is protected, so probably not
        this.responseMC = responseMC;
        // if callers are being careful, the responseMC should not be set
        // until the engine is done invoking the endpoint, on both server and
        // client side.  At that point, we can start allowing callers access
        // to HANDLER scoped properties again.  Set the flag:
        ApplicationAccessLocked = false;
    }
    
    public void setMessage(Message msg) {
        if (responseMC != null) {
            responseMC.setMessage(msg);
        }
        else {
            requestMC.setMessage(msg);
        }
    }
    
    public Scope getScope(String s) {
        if (scopes.get(s) == null) {
            // JAX-WS default 9.4.1.  However, we try to set the scope for
            // every incoming property to HANDLER.  If a property is coming from
            // the axis2 AbstractContext properties bag, we want those to be
            // APPLICATION scoped.  Those properties may have been set by an
            // axis application handler, and may need to be accessible by 
            // a client app or endpoint.
            return Scope.APPLICATION;
        }
        return scopes.get(s);
    }

    public void setScope(String s, Scope scope) {
        // TODO review next two lines
        if (isApplicationAccessLocked()) {  // endpoints are not allowed to change property scope.  They should all be APPLICATION scoped anyway
            return;
        }
        scopes.put(s, scope);
    }

    //--------------------------------------------------
    // java.util.Map methods
    //--------------------------------------------------

    public void clear() {
        // TODO review
        if (isApplicationAccessLocked()) {  // endpoints are allowed to clear APPLICATION scoped properties only
            Map<String, Object> appScopedProps = getApplicationScopedProperties();
            for(Iterator it = appScopedProps.keySet().iterator(); it.hasNext();) {
                String key = (String)it.next();
                remove(key);
                // TODO also remove Scope setting for "key"?  How?
            }
            return;
        }
        // TODO: REVIEW
        // I don't think this will work if the message contexts have a copy
        // of the map
        if (responseMC != null) {
            responseMC.getProperties().clear();
        }
        synchronized (requestMC) {
            requestMC.getProperties().clear();
        }
    }

    public boolean containsKey(Object key) {
        if (isApplicationAccessLocked()) {
            return getApplicationScopedProperties().containsKey(key);
        }
        synchronized (requestMC) {
            if (responseMC != null) {
                boolean containsKey = responseMC.containsKey(key) || requestMC.containsKey(key);
                if ((getScope((String)key) == Scope.APPLICATION) || (!isApplicationAccessLocked())) {
                    return containsKey;
                }
            }
            if ((getScope((String)key) == Scope.APPLICATION) || (!isApplicationAccessLocked())) {
                return requestMC.containsKey(key);
            }
        }
        return false;
    }

    public boolean containsValue(Object value) {
        if (isApplicationAccessLocked()) {
            return getApplicationScopedProperties().containsValue(value);
        }
        if (responseMC != null) {

            if (responseMC.getProperties().containsValue(value)) {
                return true; 
            }
            synchronized (requestMC) {
                return requestMC.getProperties().containsValue(value);
            }
        }
        return requestMC.getProperties().containsValue(value);
    }

    public Set entrySet() {
        // TODO should check ApplicationAccessLocked flag
        // and return only APPLICATION scoped properties if true
        if (isApplicationAccessLocked()) {
            return getApplicationScopedProperties().entrySet();
        }
        HashMap tempProps = new HashMap();
        
        synchronized (requestMC) {
            tempProps.putAll(requestMC.getProperties());
        }
        if (responseMC != null) {
            tempProps.putAll(responseMC.getProperties());
        }
        return tempProps.entrySet();
    }

    public Object get(Object keyObject) {
        String key = (String) keyObject;
        if (responseMC != null) {
            if (responseMC.getProperty(key) != null) {
                if ((getScope((String)key) == Scope.APPLICATION) || (!isApplicationAccessLocked())) {
                    return responseMC.getProperty(key);
                }
            }
        }
        synchronized (requestMC) {
            if ((getScope((String)key) == Scope.APPLICATION) || (!isApplicationAccessLocked())) {
                return requestMC.getProperty(key);
            }
        }
        return null;
    }

    public boolean isEmpty() {
        if (isApplicationAccessLocked()) {
            return getApplicationScopedProperties().isEmpty();
        }
        synchronized (requestMC) {
            if (responseMC != null) {
                return requestMC.getProperties().isEmpty() && requestMC.getProperties().isEmpty();
            }
            return requestMC.getProperties().isEmpty();
        }
    }

    public Set keySet() {
        if (isApplicationAccessLocked()) {
            return getApplicationScopedProperties().keySet();
        }
        HashMap tempProps = new HashMap();
        synchronized (requestMC) {
            tempProps.putAll(requestMC.getProperties());
        }
        if (responseMC != null) {
            tempProps.putAll(responseMC.getProperties());
        }
        return tempProps.keySet();
    }

    public Object put(String key, Object value) {
        // TODO careful:  endpoints may overwrite pre-existing key/value pairs.
        // Those key/value pairs may already have a scope attached to them, which
        // means an endpoint could "put" a property that is wrongly scoped
        if (scopes.get(key) == null) {  // check the scopes object directly, not through getScope()!!
            setScope(key, Scope.HANDLER);
        }
        synchronized (requestMC) {
            if (requestMC.containsKey(key)) {
                return requestMC.setProperty(key, value);
            }
            if (responseMC != null) {
                return responseMC.setProperty(key, value);
            }
            return requestMC.setProperty(key, value);
        }
    }

    public void putAll(Map t) {
        // TODO similar problem as "put"
        for(Iterator it = t.entrySet().iterator(); it.hasNext();) {
            Entry<String, Object> entry = (Entry)it.next();
            if (getScope(entry.getKey()) == null) {
                setScope(entry.getKey(), Scope.HANDLER);
            }
        }
        if (responseMC != null) {
            responseMC.setProperties(t);
        }
        else {
            synchronized (requestMC) {
                requestMC.setProperties(t);
            }
        }
    }

    public Object remove(Object key) {
        // check ApplicationAccessLocked flag and prevent removal of HANDLER scoped props
        if (isApplicationAccessLocked()) {
            if (getScope((String)key).equals(Scope.HANDLER)) {
                return null;
            }
        }
        
        // yes, remove from both and return the right object
        // TODO This won't work because getProperties returns a temporary map
        Object retVal = null;
        if (responseMC != null) {
            retVal = responseMC.getProperties().remove(key);
        }
        synchronized (requestMC) {
            if (retVal == null) {
                return requestMC.getProperties().remove(key);
            }
            else {
                requestMC.getProperties().remove(key);
            }
        }
        return retVal;
    }

    public int size() {
        if (isApplicationAccessLocked()) {
            return getApplicationScopedProperties().size();
        }
        
        // The properties must be combined together because some
        // keys may be the same on the request and the response.
        HashMap tempProps = new HashMap();
        synchronized (requestMC) {
            tempProps.putAll(requestMC.getProperties());
        }
        if (responseMC != null) {
            tempProps.putAll(responseMC.getProperties());
        }
        return tempProps.size();
    }

    public Collection values() {
        if (isApplicationAccessLocked()) {
            return getApplicationScopedProperties().values();
        }
        HashMap tempProps = new HashMap();
        synchronized (requestMC) {
            tempProps.putAll(requestMC.getProperties());
        }
        if (responseMC != null) {
            tempProps.putAll(responseMC.getProperties());
        }
        return tempProps.values();
    }

    public Message getMessageObject() {
        // TODO does ApplicationAccessLocked apply here?
        if (responseMC != null) {
            return responseMC.getMessage();
        }
        return requestMC.getMessage();
    }
    
    public boolean isApplicationAccessLocked() {
        // since MEPContext is both a wrapper and a subclass, we need to be careful to set it only on the wrapper object:
        if (this == requestMC.getMEPContext()) {  // object compare, I am the wrapper object
            return ApplicationAccessLocked;
        }
        if (responseMC == null) {
            return requestMC.getMEPContext().isApplicationAccessLocked();
        }
        else {
            return responseMC.getMEPContext().isApplicationAccessLocked() || requestMC.getMEPContext().isApplicationAccessLocked();
        }
    }

    public void setApplicationAccessLocked(boolean applicationAccessLocked) {
        // since MEPContext is both a wrapper and a subclass, we need to be careful to set it only on the wrapper object:
        if (this == requestMC.getMEPContext()) {  // object compare, I am the wrapper object
            ApplicationAccessLocked = applicationAccessLocked;
        }
        else {
            requestMC.getMEPContext().setApplicationAccessLocked(applicationAccessLocked);
        }
            
    }
    
    /**
     * The returned tempMap should be used as a read-only map as changes to it will
     * not propogate into the requestMC or responseMC
     * 
     * Watch out for infinite loop if you call another method in this class that uses this method.
     * 
     * @return
     */
    public Map<String, Object> getApplicationScopedProperties() {
        Map<String, Object> tempMap = new HashMap<String, Object>();
        if (!scopes.containsValue(Scope.APPLICATION)) {
            return tempMap;
        }
        synchronized (requestMC) {
            for(Iterator it = requestMC.getProperties().entrySet().iterator(); it.hasNext();) {
                Entry entry = (Entry)it.next();
                if (getScope((String)entry.getKey()).equals(Scope.APPLICATION)) {
                    tempMap.put((String)entry.getKey(), entry.getValue());
                }
            }
        }
        if (responseMC != null) {
            for(Iterator it = responseMC.getProperties().entrySet().iterator(); it.hasNext();) {
                Entry entry = (Entry)it.next();
                if (getScope((String)entry.getKey()).equals(Scope.APPLICATION)) {
                    tempMap.put((String)entry.getKey(), entry.getValue());
                }
            }
        }
        return tempMap;
    }
    
}
