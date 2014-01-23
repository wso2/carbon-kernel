/**
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

package org.apache.ws.security;

import javax.xml.namespace.QName;
import java.io.Serializable;

/**
 * An interface definining SOAP constants.  This allows various parts of the
 * engine to avoid hardcoding dependence on a particular SOAP version and its
 * associated URIs, etc.
 * <p/>
 * This might be fleshed out later to encapsulate factories for behavioral
 * objects which act differently depending on the SOAP version, but for now
 * it just supplies common namespaces + QNames.
 *
 * @author Glen Daniels (gdaniels@apache.org)
 * @author Andras Avar (andras.avar@nokia.com)
 */
public interface SOAPConstants extends Serializable {
    /**
     * SOAP 1.1 constants - thread-safe and shared
     */
    public SOAP11Constants SOAP11_CONSTANTS = new SOAP11Constants();
    /**
     * SOAP 1.2 constants - thread-safe and shared
     */
    public SOAP12Constants SOAP12_CONSTANTS = new SOAP12Constants();

    /**
     * Obtain the envelope namespace for this version of SOAP
     */
    public String getEnvelopeURI();

    /**
     * Obtain the QName for the Header element
     */
    public QName getHeaderQName();

    /**
     * Obtain the QName for the Body element
     */
    public QName getBodyQName();

    /**
     * Obtain the QName for the role attribute (actor/role)
     */
    public QName getRoleAttributeQName();

    /**
     * Obtain the "next" role/actor URI
     */
    public String getNextRoleURI();

    /**
     * Obtain the "next" role/actor URI
     */
    public String getMustUnderstand();
    
    /**
     * Obtain the "next" role/actor URI
     * @deprecated use getMustUnderstand() instead
     */
    public String getMustunderstand();
    
    

}
