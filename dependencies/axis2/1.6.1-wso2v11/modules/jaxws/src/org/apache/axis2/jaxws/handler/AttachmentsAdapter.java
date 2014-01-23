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
import org.apache.axis2.jaxws.message.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.DataHandler;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The JAX-WS exposes attachment properties whose value is Map<String, DataHandler>.  The
 * String is the content-id and DataHandler is the data handler representing the attachment.
 * 
 * The JAX-WS MessageContext stores attachments in an Axiom Attachments object located on the JAX-WS 
 * Message.
 * 
 * This class, AttachmentAdapter, is an adapter between the Map<String, DataHandler> interface needed
 * by the properties and the actual implementation.  All useful function is delegated through the MessageContext, so 
 * that we only have one copy of the attachment information.  
 * 
 * To use this class, invoke the install method.  This will create an AttachmentAdapter (if necessary) and install it
 * on the property JAX-WS standard attachment property.  (See BaseMessageContext.)
 */
public class AttachmentsAdapter implements Map<String, DataHandler> {

    private static final Log log = LogFactory.getLog(AttachmentsAdapter.class);
    
    MessageContext mc;    // MessageContext which provides the backing implementation of Attachments
    String propertyName;  // The name of the JAX-WS property
    
    /**
     * The backing storage of the Attachments is the JAX-WS MessageContext.
     * Intentionally private, use install(MessageContext)
     */
    private AttachmentsAdapter(MessageContext mc, String propertyName) {
        this.mc = mc;
        this.propertyName = propertyName;
    }
    
    /**
     * Add the AttachmentAdapter as the property for the inbound or
     * outbound attachment property
     * @param mc MessageContext
     */
    public static void install(MessageContext mc) {
        
        Message m = mc.getMessage();
        
        if (m == null) {
            // Probably a unit test, can't continue.
            return;
        }
        
        boolean isOutbound = mc.isOutbound();
        
        // The property is either an inbound or outbound property
        String propertyName = (isOutbound) ?
                javax.xml.ws.handler.MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS :
                javax.xml.ws.handler.MessageContext.INBOUND_MESSAGE_ATTACHMENTS;
        
        
        if (log.isDebugEnabled()) {
            log.debug("Installing AttachmentsAdapter for " + propertyName);
        }
        
        // See if there is an existing map
        Object map = mc.getProperty(propertyName);
        
        // Reuse existing AttachmentsAdapter
        if (map instanceof AttachmentsAdapter) {
            if (log.isDebugEnabled()) {
                log.debug("An AttachmentsAdapter is already installed.  Reusing the existing one.");
            }
            return;
        } 
        
        // Create a new AttachmentsAdapter and set it on the property 
        AttachmentsAdapter aa = new AttachmentsAdapter(mc, propertyName);
        
        if (map != null) {
            if (log.isDebugEnabled()) {
                log.debug("The DataHandlers in the existing map (" + propertyName + ") are copied to the AttachmentsAdapter.");
            }
            // Copy the existing Map contents to this new adapter
            aa.putAll((Map<String, DataHandler>) map);
        }
        mc.setPropertyNoReturn(propertyName, aa);
    }

    
    public void clear() {
        if (log.isDebugEnabled()) {
            log.debug("clear()");
        }
        Message m = mc.getMessage();
        Set<String> keys = this.keySet();
        for(String key: keys) {
            m.removeDataHandler(key);
        }
    }
    
    public boolean containsKey(Object key) {
        Message m = mc.getMessage();
        Set<String> keys = this.keySet();
        return keys.contains(key);
    }
    
    public boolean containsValue(Object value) {
        Message m = mc.getMessage();
        
        Set<String> keys = this.keySet();
        for(String key: keys) {
            DataHandler dh = m.getDataHandler(key);
            if (dh.equals(value)) {
                return true;
            }
        }
        return false;
    }
    
    public Set<Entry<String, DataHandler>> entrySet() {
        Map tempMap = new HashMap<String, DataHandler>();
        tempMap.putAll(this);
        return tempMap.entrySet();
    }
    
    public DataHandler get(Object key) {
        Message m = mc.getMessage();
        DataHandler dh =  m.getDataHandler((String) key);
        if (log.isDebugEnabled()) {
            log.debug("get(" + key + ") returns dh=" + dh);
        }
        return dh;
    }
    
    public boolean isEmpty() {
        return this.keySet().isEmpty();
    }
    
    public Set<String> keySet() {
        Set<String> keys = new HashSet<String>();
        Message m = mc.getMessage();
        // Note that the SOAPPart is ignored
        int i = 0;
        String key = m.getAttachmentID(i);
        while (key != null) {
            keys.add(key);
            i++;
            key = m.getAttachmentID(i);
        }
        return keys;
    }
    
    public DataHandler put(String key, DataHandler dh) {
        Message m = mc.getMessage();
        if (log.isDebugEnabled()) {
            log.debug("put(" + key + " , " + dh + ")");
        }
        DataHandler old = get(key);
        m.addDataHandler(dh, key);
        m.setDoingSWA(true);  // Can't really tell if the attachment is MTOM or SWA, etc.  Mark as SWA to make sure it is written out
        return old;
        
    }
    
    public void putAll(Map<? extends String, ? extends DataHandler> t) {
        Message m = mc.getMessage();
        for(String key: t.keySet()) {
            DataHandler dh = t.get(key);
            if (log.isDebugEnabled()) {
                log.debug("addDataHandler via putAll (" + key + " , " + dh + ")");
            }
            m.addDataHandler(dh, key);
            m.setDoingSWA(true);  // Can't really tell if the attachment is MTOM or SWA, etc.  Mark as SWA to make sure it is written out
        }
    }
    
    public DataHandler remove(Object key) {
        Message m = mc.getMessage();
        // Note that the SOAPPart is ignored
        return m.removeDataHandler((String) key); 
    }
    
    public int size() {
        Message m = mc.getMessage();
        Set<String> keys = this.keySet();
        return keys.size();
    }
    
    public Collection<DataHandler> values() {
        Map tempMap = new HashMap<String, DataHandler>();
        tempMap.putAll(this);
        return tempMap.values();
    }
    

}
