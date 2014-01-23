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

public interface SOAPConstants {
    /**
     * Eran Chinthaka (chinthaka@apache.org)
     */
    /** Field SOAP_DEFAULT_NAMESPACE_PREFIX */
    static final String SOAP_DEFAULT_NAMESPACE_PREFIX = "soapenv";
    /** Field SOAPENVELOPE_LOCAL_NAME */
    static final String SOAPENVELOPE_LOCAL_NAME = "Envelope";

    /** Field HEADER_LOCAL_NAME */
    static final String HEADER_LOCAL_NAME = "Header";

    /** Field BODY_LOCAL_NAME */
    static final String BODY_LOCAL_NAME = "Body";
    /** Field BODY_NAMESPACE_PREFIX */
    static final String BODY_NAMESPACE_PREFIX =
            SOAP_DEFAULT_NAMESPACE_PREFIX;
    /** Field BODY_FAULT_LOCAL_NAME */
    static final String BODY_FAULT_LOCAL_NAME = "Fault";

    /** Field ATTR_MUSTUNDERSTAND */
    static final String ATTR_MUSTUNDERSTAND = "mustUnderstand";
    static final String ATTR_MUSTUNDERSTAND_TRUE = "true";
    static final String ATTR_MUSTUNDERSTAND_FALSE = "false";
    static final String ATTR_MUSTUNDERSTAND_0 = "0";
    static final String ATTR_MUSTUNDERSTAND_1 = "1";
    /** Field SOAPFAULT_LOCAL_NAME */
    static final String SOAPFAULT_LOCAL_NAME = "Fault";
    /** Field SOAPFAULT_DETAIL_LOCAL_NAME */
    static final String SOAPFAULT_DETAIL_LOCAL_NAME = "detail";

    static final String SOAP_FAULT_DETAIL_EXCEPTION_ENTRY = "Exception";

    // -------- SOAP Fault Codes ------------------------------
    static final String FAULT_CODE_VERSION_MISMATCH = "VersionMismatch";
    static final String FAULT_CODE_MUST_UNDERSTAND = "MustUnderstand";
    static final String FAULT_CODE_DATA_ENCODING_UNKNOWN = "DataEncodingUnknown";

    // Followings are different in SOAP 1.1 and 1.2 specifications
    static final String FAULT_CODE_SENDER = "";
    static final String FAULT_CODE_RECEIVER = "";

    // Special Property available on some parsers to get the 
    // Qname of the first child element in the soap body.
    static final String SOAPBODY_FIRST_CHILD_ELEMENT_QNAME =
            "org.apache.axiom.SOAPBodyFirstChildElementQName";
}
