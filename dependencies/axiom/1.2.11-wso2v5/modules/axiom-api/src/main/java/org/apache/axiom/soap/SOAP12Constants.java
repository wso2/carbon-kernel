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


public interface SOAP12Constants extends SOAPConstants {
    /** Eran Chinthaka (chinthaka@apache.org) */

    public String SOAP_ENVELOPE_NAMESPACE_URI =
            "http://www.w3.org/2003/05/soap-envelope";
    public String SOAP_ENCODING_NAMESPACE_URI =
            "http://www.w3.org/2003/05/soap-encoding";

    public static final String SOAP_ROLE = "role";
    public static final String SOAP_RELAY = "relay";

    // SOAP Fault Code
    public static final String SOAP_FAULT_CODE_LOCAL_NAME = "Code";
    public static final String SOAP_FAULT_SUB_CODE_LOCAL_NAME = "Subcode";
    public static final String SOAP_FAULT_VALUE_LOCAL_NAME = "Value";

    // SOAP Fault Codes
    public static final String SOAP_FAULT_VALUE_VERSION_MISMATCH = "VersionMismatch";
    public static final String SOAP_FAULT_VALUE_MUST_UNDERSTAND = "MustUnderstand";
    public static final String SOAP_FAULT_VALUE_DATA_ENCODING_UKNOWN = "DataEncodingUnknown";
    public static final String SOAP_FAULT_VALUE_SENDER = "Sender";
    public static final String SOAP_FAULT_VALUE_RECEIVER = "Receiver";

    // SOAP Fault Reason
    public static final String SOAP_FAULT_REASON_LOCAL_NAME = "Reason";
    public static final String SOAP_FAULT_TEXT_LOCAL_NAME = "Text";
    public static final String SOAP_FAULT_TEXT_LANG_ATTR_LOCAL_NAME = "lang";
    public static final String SOAP_FAULT_TEXT_LANG_ATTR_NS_URI =
            "http://www.w3.org/XML/1998/namespace";
    public static final String SOAP_FAULT_TEXT_LANG_ATTR_NS_PREFIX = "xml";

    // SOAP Fault Node
    public static final String SOAP_FAULT_NODE_LOCAL_NAME = "Node";

    // SOAP Fault Detail
    public static final String SOAP_FAULT_DETAIL_LOCAL_NAME = "Detail";

    // SOAP Fault Role
    public static final String SOAP_FAULT_ROLE_LOCAL_NAME = "Role";

    //SOAP 1.2 Content Type
    public static final String SOAP_12_CONTENT_TYPE = "application/soap+xml";

    // -------- SOAP Fault Codes ------------------------------
    public static final String FAULT_CODE_SENDER = "Sender";
    public static final String FAULT_CODE_RECEIVER = "Receiver";

    public static final String SOAP_ROLE_NEXT = "http://www.w3.org/2003/05/soap-envelope/role/next";
    public static final String SOAP_ROLE_NONE = "http://www.w3.org/2003/05/soap-envelope/role/none";
    public static final String SOAP_ROLE_ULTIMATE_RECEIVER =
            "http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver";

    // QNames
    static final QName QNAME_ROLE = new QName(SOAP_ENVELOPE_NAMESPACE_URI, SOAP_ROLE);
    static final QName QNAME_RELAY = new QName(SOAP_ENVELOPE_NAMESPACE_URI, SOAP_RELAY);
    static final QName QNAME_MU_FAULTCODE = new QName(SOAP_ENVELOPE_NAMESPACE_URI,
                                                      FAULT_CODE_MUST_UNDERSTAND);
    static final QName QNAME_SENDER_FAULTCODE = new QName(SOAP_ENVELOPE_NAMESPACE_URI,
                                                          FAULT_CODE_SENDER);
    static final QName QNAME_RECEIVER_FAULTCODE = new QName(SOAP_ENVELOPE_NAMESPACE_URI,
                                                            FAULT_CODE_RECEIVER);

    static final QName QNAME_FAULT_REASON = new QName(SOAP_ENVELOPE_NAMESPACE_URI,
                                                      SOAP_FAULT_REASON_LOCAL_NAME);
    static final QName QNAME_FAULT_CODE = new QName(SOAP_ENVELOPE_NAMESPACE_URI,
                                                    SOAP_FAULT_CODE_LOCAL_NAME);
    static final QName QNAME_FAULT_NODE = new QName(SOAP_ENVELOPE_NAMESPACE_URI,
                                                    SOAP_FAULT_NODE_LOCAL_NAME);
    static final QName QNAME_FAULT_DETAIL = new QName(SOAP_ENVELOPE_NAMESPACE_URI,
                                                      SOAP_FAULT_DETAIL_LOCAL_NAME);
    static final QName QNAME_FAULT_ROLE = new QName(SOAP_ENVELOPE_NAMESPACE_URI,
                                                    SOAP_FAULT_ROLE_LOCAL_NAME);
    static final QName QNAME_FAULT_VALUE = new QName(SOAP_ENVELOPE_NAMESPACE_URI,
                                                     SOAP_FAULT_VALUE_LOCAL_NAME);
    static final QName QNAME_FAULT_SUBCODE = new QName(SOAP_ENVELOPE_NAMESPACE_URI,
                                                       SOAP_FAULT_SUB_CODE_LOCAL_NAME);
    static final QName QNAME_FAULT_TEXT = new QName(SOAP_ENVELOPE_NAMESPACE_URI,
                                                    SOAP_FAULT_TEXT_LOCAL_NAME);
}
