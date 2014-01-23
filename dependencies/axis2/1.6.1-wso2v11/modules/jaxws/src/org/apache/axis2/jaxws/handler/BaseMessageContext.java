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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * BaseMessageContext is the base class for the two handler message contexts:
 * SoapMessageContext and LogicalMessageContext.  It delegates everything up to
 * the MEPContext, which itself delegates to the requestMC or responseMC, as
 * appropriate.
 * 
 */
public class BaseMessageContext implements javax.xml.ws.handler.MessageContext {
    private static final Log log = LogFactory.getLog(BaseMessageContext.class);

    protected MessageContext messageCtx;
    
    /**
     * @param messageCtx
     */
    protected BaseMessageContext(MessageContext messageCtx) {
        this.messageCtx = messageCtx;
        
        // Install an an AttachmentsAdapter between the 
        // jaxws attachment standard properties and the
        // MessageContext Attachments implementation.
        AttachmentsAdapter.install(messageCtx);
        TransportHeadersAdapter.install(messageCtx);
        SOAPHeadersAdapter.install(messageCtx);
    }

    /* (non-Javadoc)
     * @see java.util.Map#clear()
     */
    public void clear() {
        messageCtx.getMEPContext().clear();
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key) {
        return messageCtx.getMEPContext().containsKey(key);
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(Object value) {
        return messageCtx.getMEPContext().containsValue(value);
    }

    /* (non-Javadoc)
     * @see java.util.Map#entrySet()
     */
    public Set<java.util.Map.Entry<String, Object>> entrySet() {
        return messageCtx.getMEPContext().entrySet();
    }

    /* (non-Javadoc)
     * @see java.util.Map#get(java.lang.Object)
     */
    public Object get(Object key) {
        // There are some properties that, in some cases, should not span the message exchange;
        // that is, they should come from only the current message context.  For others properties,
        // they should span the message exchange, meaning a property could be set on the request
        // and it will also be available on the response.  [JAXWS 2.0, Sec 9.4.1.1, pp. 110-113]
        Object returnValue = null;
        if (shouldPropertySpanMEP(key)) {
            returnValue = messageCtx.getMEPContext().get(key);
        } else {
            returnValue = messageCtx.getProperty((String) key);
        }

        // For the HTTP_REQUEST_HEADERS and HTTP_RESPONSE_HEADERS, the CTS tests want a null returned 
        // if there are no headers.  Since we always put an instance of TransportHeadersAdapter,
        // which contains the headers, on the message context, return a null if it is empty.
        if (returnValue != null && (returnValue instanceof TransportHeadersAdapter)) {
            TransportHeadersAdapter adapter = (TransportHeadersAdapter) returnValue;
            if (adapter.isEmpty()) {
                return null;
            }
        }
        return returnValue;
    }

    private boolean shouldPropertySpanMEP(Object key) {
        boolean shouldSpan = true;
        String keyString = (String) key;

        // The CTS tests require that HTTP_REQUEST_HEADERS span the request and response contexts
        // on the service-provider, but do NOT span the request and response context on the 
        // service-requester.  So, for an INBOUND flow, do not allow HTTP_REQUEST_HEADERS to
        // span the request and response contexts.  The result is that the service-requester
        // inbound handler will not see the request headers while processing a response.
        Boolean outbound = (Boolean) messageCtx.getMEPContext().get(MESSAGE_OUTBOUND_PROPERTY);
        if (outbound != null && !outbound)
            if (javax.xml.ws.handler.MessageContext.HTTP_REQUEST_HEADERS.equals(keyString)) {
            shouldSpan = false;
        }
        return shouldSpan;
    }

    /* (non-Javadoc)
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {
        return messageCtx.getMEPContext().isEmpty();
    }

    /* (non-Javadoc)
     * @see java.util.Map#keySet()
     */
    public Set<String> keySet() {
        return messageCtx.getMEPContext().keySet();
    }

    /* (non-Javadoc)
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public Object put(String key, Object value) {
        return messageCtx.getMEPContext().put(key, value);
    }

    /* (non-Javadoc)
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map<? extends String, ? extends Object> t) {
        messageCtx.getMEPContext().putAll(t);
    }

    /* (non-Javadoc)
     * @see java.util.Map#remove(java.lang.Object)
     */
    public Object remove(Object key) {
        return messageCtx.getMEPContext().remove(key);
    }

    /* (non-Javadoc)
     * @see java.util.Map#size()
     */
    public int size() {
        return messageCtx.getMEPContext().size();
    }

    /* (non-Javadoc)
     * @see java.util.Map#values()
     */
    public Collection<Object> values() {
        return messageCtx.getMEPContext().values();
    }

    /* (non-Javadoc)
     * @see javax.xml.ws.handler.MessageContext#getScope(java.lang.String)
     */
    public Scope getScope(String s) {
        return messageCtx.getMEPContext().getScope(s);
    }

    /* (non-Javadoc)
     * @see javax.xml.ws.handler.MessageContext#setScope(java.lang.String, javax.xml.ws.handler.MessageContext.Scope)
     */
    public void setScope(String s, Scope scope) {
        messageCtx.getMEPContext().setScope(s, scope);
    }

}
