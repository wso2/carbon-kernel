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

package org.apache.axis2.jaxws.message;

import org.apache.axis2.jaxws.description.builder.MDQConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.soap.SOAPBinding;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Protocol Each message has a protocol (soap11, soap12, rest) This enum represents the protocol
 * within the Message sub-component
 */
public enum Protocol {
    soap11, soap12, rest, unknown;

    private static final Log log = LogFactory.getLog(Protocol.class);

    private static Map<String, Protocol> protocolMappings; 
    
    // These namespaces are used in the WSDL document to indentify a 
    // SOAP 1.1 vs. a SOAP 1.2 binding
    private static final String SOAP11_WSDL_BINDING = "http://schemas.xmlsoap.org/wsdl/soap";
    private static final String SOAP12_WSDL_BINDING = "http://schemas.xmlsoap.org/wsdl/soap12";

    static {
        // Normally a static HashMap can cause concurrency issues.
        // However, if the HashMap is only queried (never modified) then 
        // access by multiple theads is safe.
        protocolMappings = new HashMap<String, Protocol>();
        
        protocolMappings.put(Protocol.SOAP11_WSDL_BINDING, Protocol.soap11);
        protocolMappings.put(SOAPBinding.SOAP11HTTP_BINDING, Protocol.soap11);
        protocolMappings.put(SOAPBinding.SOAP11HTTP_MTOM_BINDING, Protocol.soap11);
        protocolMappings.put(MDQConstants.SOAP11JMS_BINDING, Protocol.soap11);
        protocolMappings.put(MDQConstants.SOAP11JMS_MTOM_BINDING, Protocol.soap11);

        protocolMappings.put(Protocol.SOAP12_WSDL_BINDING, Protocol.soap12);
        protocolMappings.put(SOAPBinding.SOAP12HTTP_BINDING, Protocol.soap12);
        protocolMappings.put(SOAPBinding.SOAP12HTTP_MTOM_BINDING, Protocol.soap12);
        protocolMappings.put(HTTPBinding.HTTP_BINDING, Protocol.rest);
        // There is only one binding value declared by the spec; there is no differentiation
        // between SOAP11 and SOAP12, unlike HTTP.  This may be an issue in the spec.  However,
        // for now, since the values are the same, we can only register one protocol, so we 
        // use SOAP11 (above).  See Jira AXIS2-4855 for more information.
//        protocolMappings.put(MDQConstants.SOAP12JMS_BINDING, Protocol.soap12);
//        protocolMappings.put(MDQConstants.SOAP12JMS_MTOM_BINDING, Protocol.soap12);
        
        // Add each of the URLs with a "/" at the end for flexibility
        Map<String, Protocol> updates = new HashMap<String, Protocol>();
        Iterator<String> keys = protocolMappings.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            if (!key.endsWith("/")) {
                updates.put(key + "/", protocolMappings.get(key));    
            }
        }
        protocolMappings.putAll(updates);
    }
    
    /**
     * Return the right value for the Protocol based on the binding URL that was passed in.
     *
     * @param url
     * @return Protocol or null
     */
    public static Protocol getProtocolForBinding(String url) {
        boolean debug = log.isDebugEnabled();
        if (debug) {
            log.debug("Configuring message protocol for binding [" + url + "]");
        }

        Protocol proto = protocolMappings.get(url);
        if (proto != null) {
            if (log.isDebugEnabled()) {
                log.debug("Found protocol mapping: " + proto);
            }
            return proto;
        } else {
            if (debug) {
                log.debug("Protocol was not found for:" + url);
            }
            return null;
        }
    }
}
