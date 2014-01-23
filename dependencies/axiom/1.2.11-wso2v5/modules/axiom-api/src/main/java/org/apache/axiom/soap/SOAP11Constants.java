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

package org.apache.axiom.soap;

import javax.xml.namespace.QName;


public interface SOAP11Constants extends SOAPConstants {
    /** Eran Chinthaka (chinthaka@apache.org) */
    static final String SOAP_ENVELOPE_NAMESPACE_URI =
            "http://schemas.xmlsoap.org/soap/envelope/";
    static final String SOAP_ENCODING_NAMESPACE_URI =
            "http://schemas.xmlsoap.org/soap/encoding/";

    /** Field ATTR_ACTOR */
    static final String ATTR_ACTOR = "actor";

    /** Field SOAP_FAULT_CODE_LOCAL_NAME */
    static final String SOAP_FAULT_CODE_LOCAL_NAME = "faultcode";
    /** Field SOAP_FAULT_STRING_LOCAL_NAME */
    static final String SOAP_FAULT_STRING_LOCAL_NAME = "faultstring";
    /** Field SOAP_FAULT_ACTOR_LOCAL_NAME */
    static final String SOAP_FAULT_ACTOR_LOCAL_NAME = "faultactor";

    static final String SOAP_FAULT_DETAIL_LOCAL_NAME = "detail";

    //SOAP 1.2 Content Type
    static final String SOAP_11_CONTENT_TYPE = "text/xml";

    // -------- SOAP Fault Codes ------------------------------
    static final String FAULT_CODE_SENDER = "Client";
    static final String FAULT_CODE_RECEIVER = "Server";

    static final String SOAP_ACTOR_NEXT = "http://schemas.xmlsoap.org/soap/actor/next";

    // QNames
    static final QName QNAME_ACTOR = new QName(SOAP_ENVELOPE_NAMESPACE_URI, ATTR_ACTOR);
    
    static final QName QNAME_MU_FAULTCODE = new QName(SOAP_ENVELOPE_NAMESPACE_URI,
                                                      FAULT_CODE_MUST_UNDERSTAND);
    static final QName QNAME_SENDER_FAULTCODE = new QName(SOAP_ENVELOPE_NAMESPACE_URI,
                                                          FAULT_CODE_SENDER);
    static final QName QNAME_RECEIVER_FAULTCODE = new QName(SOAP_ENVELOPE_NAMESPACE_URI,
                                                            FAULT_CODE_RECEIVER);

    static final QName QNAME_FAULT_REASON = new QName(SOAP_FAULT_STRING_LOCAL_NAME);
    static final QName QNAME_FAULT_CODE = new QName(SOAP_FAULT_CODE_LOCAL_NAME);
    static final QName QNAME_FAULT_DETAIL = new QName(SOAP_FAULT_DETAIL_LOCAL_NAME);
    static final QName QNAME_FAULT_ROLE = new QName(SOAP_FAULT_ACTOR_LOCAL_NAME);
}
