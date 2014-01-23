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

import org.apache.axis2.i18n.Messages;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The JAX-WS exposes transport properties whose value is Map<String, List<String>>.  The
 * String is the content-id and DataHandler is the data handler representing the TransportHeaders.
 * 
 * The JAX-WS MessageContext stores transport properties in an Map object located on the AXIS2
 * MessageContext.
 * 
 * This class, TransportHeadersAdapter, is an adapter between the Map<String, List<String>> 
 * interface needed by the properties and the actual implementation.  
 * All useful function is delegated through the MessageContext, 
 * so that we only have one copy of the information.  
 * 
 * To use this class, invoke the install method.  This will create an TransportHeadersAdapter 
 * (if necessary) and install it on the property JAX-WS standard TransportHeaders properties.  
 * (See BaseMessageContext.)
 */
public class TransportHeadersAdapter implements Map {

    private static final Log log = LogFactory.getLog(TransportHeadersAdapter.class);

    MessageContext mc; // MessageContext which provides the backing implementation
    String propertyName; // The name of the JAX-WS property

    /**
     * @param mc
     * @param propertyName
     */
    private TransportHeadersAdapter(MessageContext mc, String propertyName) {
        this.mc = mc;
        this.propertyName = propertyName;
    }

    /**
     * Add the TransportHeadersAdapter as the property for TransportHeaders
     * @param mc MessageContext
     */
    public static void install(MessageContext mc) {

        boolean isRequest = (mc.getMEPContext().getRequestMessageContext() == mc);

        // The property is either a request or response
        String propertyName =
                (isRequest) ? javax.xml.ws.handler.MessageContext.HTTP_REQUEST_HEADERS
                        : javax.xml.ws.handler.MessageContext.HTTP_RESPONSE_HEADERS;


        if (log.isDebugEnabled()) {
            log.debug("Installing TransportHeadersAdapter for " + propertyName);
        }

        // See if there is an existing map
        Object map = mc.getProperty(propertyName);

        // Reuse existing TransportHeadersAdapter
        if (map instanceof TransportHeadersAdapter) {
            if (log.isDebugEnabled()) {
                log.debug("An TransportHeadersAdapter is already installed.  " +
                                "Reusing the existing one.");
            }
            return;
        }

        // Create a new TransportHeadersAdapter and set it on the property 
        TransportHeadersAdapter tha = new TransportHeadersAdapter(mc, propertyName);

        if (map != null) {
            if (log.isDebugEnabled()) {
                log.debug("The TransportHeaders in the existing map (" + propertyName
                        + ") are copied to the TransportHeadersAdapter.");
            }
            // Copy the existing Map contents to this new adapter
            tha.putAll((Map) map);
        }
        mc.setPropertyNoReturn(propertyName, tha);
        
        // If this is a response, then also set the property for the response code
        if (!isRequest) {
            Object value = mc.getProperty(HTTPConstants.MC_HTTP_STATUS_CODE);
            mc.setProperty(javax.xml.ws.handler.MessageContext.HTTP_RESPONSE_CODE, value);
        }
    }

    /**
     * Get/Create the implementation map from the Axis2 properties
     * @param mc
     * @return Map
     */
    private static Map getDelegateMap(MessageContext mc) {
        // Get the axis2 Map
        Map map = (Map) mc.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        if (map == null) {
            map = new HashMap();
            mc.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, map);
        }
        return map;
    }

    /**
     * Convert intput into a List
     * @param o
     * @return List
     */
    private static List<String> convertToList(Object o) {
        if (o == null) {
            return null;
        } else if (o instanceof List) {
            return (List) o;
        } else if (o instanceof String) {
            String s = (String)o;
            String[] values = s.split(", ");
            List<String> l = new ArrayList<String>();
            l.addAll(Arrays.asList(values));
            return l;
        } else {
            throw ExceptionFactory.makeWebServiceException(
            		Messages.getMessage("inputConvertionErr",o.getClass().toString()));
        }
    }

    private static String convertToString(Object o) {
        if (o == null) {
            return null;
        } else if (o instanceof String) {
            return (String) o;
        } else if (o instanceof List) {

            List l = (List) o;
            if (l.size() == 0) {
                return null;
            } else {
                String s = "";
                for (int i = 0 ; i < l.size() ; i++) {
                    s += l.get(i);
                    if (i != l.size() - 1) {
                        s += ", ";
                    }
                }
                return s;
            }
        }
        throw ExceptionFactory.makeWebServiceException(
        		Messages.getMessage("inputConvertionErr1",o.getClass().toString()));
    }


    public int size() {
        return getDelegateMap(mc).size();
    }

    public boolean isEmpty() {
        return getDelegateMap(mc).isEmpty();
    }

    public boolean containsKey(Object key) {
        return getDelegateMap(mc).containsKey(key);
    }

    public boolean containsValue(Object value) {
        String valueString = convertToString(value);
        return getDelegateMap(mc).containsValue(valueString);
    }

    public Object get(Object key) {
        Object value = convertToList(getDelegateMap(mc).get(key));
        if (log.isDebugEnabled()) {
            log.debug("get(" + key + ") returns value=" + value);
        }
        return value;
    }

    public Object put(Object key, Object value) {
        if (log.isDebugEnabled()) {
            log.debug("put(" + key + " , " + value + ")");
        }
        String valueString = convertToString(value);
        return convertToList(getDelegateMap(mc).put(key, valueString));
    }

    public Object remove(Object key) {
        return convertToList(getDelegateMap(mc).remove(key));
    }

    public void putAll(Map t) {
        for (Object key : t.keySet()) {
            Object value = t.get(key);
            if (log.isDebugEnabled()) {
                log.debug("put via putAll (" + key + " , " + value + ")");
            }
            put(key, value);
        }
    }

    public void clear() {
        getDelegateMap(mc).clear();
    }

    public Set keySet() {
        return getDelegateMap(mc).keySet();
    }

    public Collection values() {
        return copy().values();
    }

    public Set entrySet() {
        return copy().entrySet();
    }
    
    private Map copy() {
        Map tempMap = new HashMap<String, List<String>>();
        for (Object key : keySet()) {
            List<String> value = (List<String>)get(key);
            tempMap.put(key, value);
        }
        return tempMap;
    }
    
    public String toString() {
        return "TransportHeadersAdapter: " + getDelegateMap(mc).toString();        
    }
}
