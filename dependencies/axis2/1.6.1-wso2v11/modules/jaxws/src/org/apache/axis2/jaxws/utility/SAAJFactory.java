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

package org.apache.axis2.jaxws.utility;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.i18n.Messages;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.ws.WebServiceException;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides convenience methods to construct a SOAP 1.1 or SOAP 1.2 SAAJ MessageFactory or
 * SOAPFactory. The code uses reflection; thus, when Axis2 upgrades to SAAJ 1.3, no changes will be
 * neded to this class.
 */
public class SAAJFactory {

    private static final String SOAP11_ENV_NS = "http://schemas.xmlsoap.org/soap/envelope/";
    private static final String SOAP12_ENV_NS = "http://www.w3.org/2003/05/soap-envelope";

    // Protocol Names per the SAAJ 1.3 specification.
    public static final String SOAP_1_1_PROTOCOL = "SOAP 1.1 Protocol";
    public static final String SOAP_1_2_PROTOCOL = "SOAP 1.2 Protocol";
    public static final String DYNAMIC_PROTOCOL = "Dynamic Protocol";
    
    // Cache the MessgeFactory and SOAPFactory instances.  There will be only a few at most
    private static Map<String, MessageFactory> _mmap = new ConcurrentHashMap<String, MessageFactory>();
    private static Map<String, SOAPFactory> _smap = new ConcurrentHashMap<String, SOAPFactory>();

    /**
     * Create SOAPFactory using information from the envelope namespace
     *
     * @param namespace
     * @return
     */
    
    // Since there are only a few protocols, this map will only contain 3 or less MessageFactories
    public static SOAPFactory createSOAPFactory(String namespace)
            throws WebServiceException, SOAPException {
        SOAPFactory sf = null;
        // Try quick cache
        sf = _smap.get(namespace);
        if (sf != null){
            return sf;
        }
        
        Method m = getSOAPFactoryNewInstanceProtocolMethod();
        if (m == null) {
            if (namespace.equals(SOAP11_ENV_NS)) {
                sf = SOAPFactory.newInstance();
            } else {
                throw ExceptionFactory
                        .makeWebServiceException(Messages.getMessage("SOAP12WithSAAJ12Err"));
            }
        } else {
            String protocol = DYNAMIC_PROTOCOL;
            if (namespace.equals(SOAP11_ENV_NS)) {
                protocol = SOAP_1_1_PROTOCOL;
            } else if (namespace.equals(SOAP12_ENV_NS)) {
                protocol = SOAP_1_2_PROTOCOL;
            }
            try {
                sf = (SOAPFactory)m.invoke(null, new Object[] { protocol });
            } catch (Exception e) {
                throw ExceptionFactory.makeWebServiceException(e);
            }
        }
        
        if (sf != null) {
            _smap.put(namespace, sf);
        }
        return sf;
    }

    /**
     * Create MessageFactory using information from the envelope namespace
     *
     * @param namespace
     * @return
     */
    
    public static MessageFactory createMessageFactory(String namespace)
            throws WebServiceException, SOAPException {
        
        // Try quick cache
        MessageFactory mf = null;
        mf = _mmap.get(namespace);
        if (mf != null){
            return mf;
        }
        
        Method m = getMessageFactoryNewInstanceProtocolMethod();
        if (m == null) {
            if (namespace.equals(SOAP11_ENV_NS)) {
                mf = MessageFactory.newInstance();
            } else {
                throw ExceptionFactory
                        .makeWebServiceException(Messages.getMessage("SOAP12WithSAAJ12Err"));
            }
        } else {
            String protocol = DYNAMIC_PROTOCOL;
            if (namespace.equals(SOAP11_ENV_NS)) {
                protocol = SOAP_1_1_PROTOCOL;
            } else if (namespace.equals(SOAP12_ENV_NS)) {
                protocol = SOAP_1_2_PROTOCOL;
            }
            try {
                mf = (MessageFactory)m.invoke(null, new Object[] { protocol });
            } catch (Exception e) {
                throw ExceptionFactory.makeWebServiceException(e);
            }
        }
        if (mf != null) {
            _mmap.put(namespace, mf);
        }
        return mf;
    }

    private static Method messageFactoryNewInstanceProtocolMethod = null;

    /**
     * SAAJ 1.3 has a newInstance method that has a protocol parameter.
     *
     * @return newInstance(String) method if available
     */
    private static Method getMessageFactoryNewInstanceProtocolMethod() {
        if (messageFactoryNewInstanceProtocolMethod == null) {
            try {
                messageFactoryNewInstanceProtocolMethod =
                        MessageFactory.class.getMethod("newInstance", new Class[] { String.class });
            } catch (Exception e) {
                // TODO Might want to log this.
                // Flow to here indicates that the installed SAAJ model does not support version 1.3
                messageFactoryNewInstanceProtocolMethod = null;
            }
        }
        return messageFactoryNewInstanceProtocolMethod;
    }

    private static Method soapFactoryNewInstanceProtocolMethod = null;

    /**
     * SAAJ 1.3 has a newInstance method that has a protocol parameter.
     *
     * @return newInstance(String) method if available
     */
    private static Method getSOAPFactoryNewInstanceProtocolMethod() {
        if (soapFactoryNewInstanceProtocolMethod == null) {
            try {
                soapFactoryNewInstanceProtocolMethod =
                        SOAPFactory.class.getMethod("newInstance", new Class[] { String.class });
            } catch (Exception e) {
                // TODO Might want to log this.
                // Flow to here indicates that the installed SAAJ model does not support version 1.3
                soapFactoryNewInstanceProtocolMethod = null;
            }
        }
        return soapFactoryNewInstanceProtocolMethod;
    }


}
