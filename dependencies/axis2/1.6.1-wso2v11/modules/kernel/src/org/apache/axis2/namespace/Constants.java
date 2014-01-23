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

package org.apache.axis2.namespace;

import javax.xml.namespace.QName;

public class Constants {

    public static final String AXIS2_NAMESPACE_URI =
            "http://ws.apache.org/namespaces/axis2";
    public static final String AXIS2_NAMESPACE_PREFIX = "axis2";

    // Namespace Prefix Constants
    //////////////////////////////////////////////////////////////////////////
    public static final String NS_PREFIX_SOAP_ENV = "soapenv";
    public static final String NS_PREFIX_SOAP_ENC = "soapenc";
    public static final String NS_PREFIX_SCHEMA_XSI = "xsi";
    public static final String NS_PREFIX_SCHEMA_XSD = "xsd";
    public static final String NS_PREFIX_WSDL = "wsdl";
    public static final String NS_PREFIX_WSDL_SOAP = "wsdlsoap";
    public static final String NS_PREFIX_XML = "xml";
    public static final String NS_PREFIX_XOP = "xop";

    //
    // SOAP-ENV Namespaces
    //
    public static final String URI_SOAP11_ENV =
            "http://schemas.xmlsoap.org/soap/envelope/";
    public static final String URI_SOAP12_ENV =
            "http://www.w3.org/2003/05/soap-envelope";

    public static final String URI_LITERAL_ENC = "";

    //
    // SOAP-ENC Namespaces
    //
    public static final String URI_SOAP11_ENC =
            "http://schemas.xmlsoap.org/soap/encoding/";
    public static final String URI_SOAP12_ENC =
            "http://www.w3.org/2003/05/soap-encoding";
    public static final String URI_SOAP12_NOENC =
            "http://www.w3.org/2003/05/soap-envelope/encoding/none";

    // Misc SOAP Namespaces / URIs
    public static final String URI_SOAP11_NEXT_ACTOR =
            "http://schemas.xmlsoap.org/soap/actor/next";
    public static final String URI_SOAP12_NEXT_ROLE =
            "http://www.w3.org/2003/05/soap-envelope/role/next";
    /**
     * @deprecated use URI_SOAP12_NEXT_ROLE
     */
    public static final String URI_SOAP12_NEXT_ACTOR = URI_SOAP12_NEXT_ROLE;

    public static final String URI_SOAP12_RPC =
            "http://www.w3.org/2003/05/soap-rpc";

    public static final String URI_SOAP12_NONE_ROLE =
            "http://www.w3.org/2003/05/soap-envelope/role/none";
    public static final String URI_SOAP12_ULTIMATE_ROLE =
            "http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver";

    public static final String URI_SOAP11_HTTP =
            "http://schemas.xmlsoap.org/soap/http";
    public static final String URI_SOAP12_HTTP =
            "http://www.w3.org/2003/05/http";

    public static final String NS_URI_XMLNS =
            "http://www.w3.org/2000/xmlns/";

    public static final String NS_URI_XML =
            "http://www.w3.org/XML/1998/namespace";

    //
    // Schema XSD Namespaces
    //
    public static final String URI_1999_SCHEMA_XSD =
            "http://www.w3.org/1999/XMLSchema";
    public static final String URI_2000_SCHEMA_XSD =
            "http://www.w3.org/2000/10/XMLSchema";
    public static final String URI_2001_SCHEMA_XSD =
            "http://www.w3.org/2001/XMLSchema";

    public static final String URI_DEFAULT_SCHEMA_XSD = URI_2001_SCHEMA_XSD;

    //
    // Schema XSI Namespaces
    //
    public static final String URI_1999_SCHEMA_XSI =
            "http://www.w3.org/1999/XMLSchema-instance";
    public static final String URI_2000_SCHEMA_XSI =
            "http://www.w3.org/2000/10/XMLSchema-instance";
    public static final String URI_2001_SCHEMA_XSI =
            "http://www.w3.org/2001/XMLSchema-instance";
    public static final String URI_DEFAULT_SCHEMA_XSI = URI_2001_SCHEMA_XSI;

    public static final String URI_POLICY =
            "http://schemas.xmlsoap.org/ws/2004/09/policy";
    public static final String FORMAT_BINDING = "http://schemas.xmlsoap.org/wsdl/formatbinding/";
    public static final String JAVA_NS = "http://schemas.xmlsoap.org/wsdl/java/";
    /**
     * WSDL Namespace.
     */
    public static final String NS_URI_WSDL11 =
            "http://schemas.xmlsoap.org/wsdl/";

    public static final String NS_URI_WSDL20 =
            "http://www.w3.org/ns/wsdl";

    //
    // WSDL extensions for SOAP in DIME
    // (http://gotdotnet.com/team/xml_wsspecs/dime/WSDL-Extension-for-DIME.htm)
    //
    public static final String URI_DIME_WSDL =
            "http://schemas.xmlsoap.org/ws/2002/04/dime/wsdl/";

    public static final String URI_DIME_CONTENT =
            "http://schemas.xmlsoap.org/ws/2002/04/content-type/";

    public static final String URI_DIME_REFERENCE =
            "http://schemas.xmlsoap.org/ws/2002/04/reference/";

    public static final String URI_DIME_CLOSED_LAYOUT =
            "http://schemas.xmlsoap.org/ws/2002/04/dime/closed-layout";

    public static final String URI_DIME_OPEN_LAYOUT =
            "http://schemas.xmlsoap.org/ws/2002/04/dime/open-layout";

    // XOP/MTOM
    public static final String URI_XOP_INCLUDE =
            "http://www.w3.org/2004/08/xop/include";
    public static final String ELEM_XOP_INCLUDE = "Include";


    //
    // WSDL SOAP Namespace
    //
    public static final String URI_WSDL11_SOAP =
            "http://schemas.xmlsoap.org/wsdl/soap/";
    public static final String URI_WSDL12_SOAP =
            "http://schemas.xmlsoap.org/wsdl/soap12/";

    public static final String ELEM_ENVELOPE = "Envelope";
    public static final String ELEM_HEADER = "Header";
    public static final String ELEM_BODY = "Body";
    public static final String ELEM_FAULT = "Fault";

    public static final String ELEM_NOTUNDERSTOOD = "NotUnderstood";
    public static final String ELEM_UPGRADE = "Upgrade";
    public static final String ELEM_SUPPORTEDENVELOPE = "SupportedEnvelope";

    public static final String ELEM_FAULT_CODE = "faultcode";
    public static final String ELEM_FAULT_STRING = "faultstring";
    public static final String ELEM_FAULT_DETAIL = "detail";
    public static final String ELEM_FAULT_ACTOR = "faultactor";

    public static final String ELEM_FAULT_CODE_SOAP12 = "Code";
    public static final String ELEM_FAULT_VALUE_SOAP12 = "Value";
    public static final String ELEM_FAULT_SUBCODE_SOAP12 = "Subcode";
    public static final String ELEM_FAULT_REASON_SOAP12 = "Reason";
    public static final String ELEM_FAULT_NODE_SOAP12 = "Node";
    public static final String ELEM_FAULT_ROLE_SOAP12 = "Role";
    public static final String ELEM_FAULT_DETAIL_SOAP12 = "Detail";
    public static final String ELEM_TEXT_SOAP12 = "Text";

    public static final String ATTR_MUST_UNDERSTAND = "mustUnderstand";
    public static final String ATTR_ENCODING_STYLE = "encodingStyle";
    public static final String ATTR_ACTOR = "actor";
    public static final String ATTR_ROLE = "role";
    public static final String ATTR_RELAY = "relay";
    public static final String ATTR_ROOT = "root";
    public static final String ATTR_ID = "id";
    public static final String ATTR_HREF = "href";
    public static final String ATTR_REF = "ref";
    public static final String ATTR_QNAME = "qname";
    public static final String ATTR_ARRAY_TYPE = "arrayType";
    public static final String ATTR_ITEM_TYPE = "itemType";
    public static final String ATTR_ARRAY_SIZE = "arraySize";
    public static final String ATTR_OFFSET = "offset";
    public static final String ATTR_POSITION = "position";
    public static final String ATTR_TYPE = "type";
    public static final String ATTR_HANDLERINFOCHAIN = "handlerInfoChain";

    // Fault Codes
    //////////////////////////////////////////////////////////////////////////
    public static final String FAULT_CLIENT = "Client";

    public static final String FAULT_SERVER_GENERAL =
            "Server.generalException";

    public static final String FAULT_SERVER_USER =
            "Server.userException";

    public static final QName FAULT_VERSIONMISMATCH =
            new QName(URI_SOAP11_ENV, "VersionMismatch");

    public static final QName FAULT_MUSTUNDERSTAND =
            new QName(URI_SOAP11_ENV, "MustUnderstand");


    public static final QName FAULT_SOAP12_MUSTUNDERSTAND =
            new QName(URI_SOAP12_ENV, "MustUnderstand");

    public static final QName FAULT_SOAP12_VERSIONMISMATCH =
            new QName(URI_SOAP12_ENV, "VersionMismatch");

    public static final QName FAULT_SOAP12_DATAENCODINGUNKNOWN =
            new QName(URI_SOAP12_ENV, "DataEncodingUnknown");

    public static final QName FAULT_SOAP12_SENDER =
            new QName(URI_SOAP12_ENV, "Sender");

    public static final QName FAULT_SOAP12_RECEIVER =
            new QName(URI_SOAP12_ENV, "Receiver");

    // SOAP 1.2 Fault subcodes
    public static final QName FAULT_SUBCODE_BADARGS =
            new QName(URI_SOAP12_RPC, "BadArguments");
    public static final QName FAULT_SUBCODE_PROC_NOT_PRESENT =
            new QName(URI_SOAP12_RPC, "ProcedureNotPresent");

    // QNames
    //////////////////////////////////////////////////////////////////////////
    public static final QName QNAME_FAULTCODE =
            new QName("", ELEM_FAULT_CODE);
    public static final QName QNAME_FAULTSTRING =
            new QName("", ELEM_FAULT_STRING);
    public static final QName QNAME_FAULTACTOR =
            new QName("", ELEM_FAULT_ACTOR);
    public static final QName QNAME_FAULTDETAILS =
            new QName("", ELEM_FAULT_DETAIL);

    public static final QName QNAME_FAULTCODE_SOAP12 =
            new QName(URI_SOAP12_ENV, ELEM_FAULT_CODE_SOAP12);
    public static final QName QNAME_FAULTVALUE_SOAP12 =
            new QName(URI_SOAP12_ENV, ELEM_FAULT_VALUE_SOAP12);
    public static final QName QNAME_FAULTSUBCODE_SOAP12 =
            new QName(URI_SOAP12_ENV, ELEM_FAULT_SUBCODE_SOAP12);
    public static final QName QNAME_FAULTREASON_SOAP12 =
            new QName(URI_SOAP12_ENV, ELEM_FAULT_REASON_SOAP12);
    public static final QName QNAME_TEXT_SOAP12 =
            new QName(URI_SOAP12_ENV, ELEM_TEXT_SOAP12);

    public static final QName QNAME_FAULTNODE_SOAP12 =
            new QName(URI_SOAP12_ENV, ELEM_FAULT_NODE_SOAP12);
    public static final QName QNAME_FAULTROLE_SOAP12 =
            new QName(URI_SOAP12_ENV, ELEM_FAULT_ROLE_SOAP12);
    public static final QName QNAME_FAULTDETAIL_SOAP12 =
            new QName(URI_SOAP12_ENV, ELEM_FAULT_DETAIL_SOAP12);
    public static final QName QNAME_NOTUNDERSTOOD =
            new QName(URI_SOAP12_ENV, ELEM_NOTUNDERSTOOD);

    // Define qnames for the all of the XSD and SOAP-ENC encodings
    public static final QName XSD_STRING = new QName(URI_DEFAULT_SCHEMA_XSD, "string");
    public static final QName XSD_BOOLEAN = new QName(URI_DEFAULT_SCHEMA_XSD, "boolean");
    public static final QName XSD_DOUBLE = new QName(URI_DEFAULT_SCHEMA_XSD, "double");
    public static final QName XSD_FLOAT = new QName(URI_DEFAULT_SCHEMA_XSD, "float");
    public static final QName XSD_INT = new QName(URI_DEFAULT_SCHEMA_XSD, "int");
    public static final QName XSD_INTEGER = new QName(URI_DEFAULT_SCHEMA_XSD, "integer");
    public static final QName XSD_LONG = new QName(URI_DEFAULT_SCHEMA_XSD, "long");
    public static final QName XSD_SHORT = new QName(URI_DEFAULT_SCHEMA_XSD, "short");
    public static final QName XSD_BYTE = new QName(URI_DEFAULT_SCHEMA_XSD, "byte");
    public static final QName XSD_DECIMAL = new QName(URI_DEFAULT_SCHEMA_XSD, "decimal");
    public static final QName XSD_BASE64 = new QName(URI_DEFAULT_SCHEMA_XSD, "base64Binary");
    public static final QName XSD_HEXBIN = new QName(URI_DEFAULT_SCHEMA_XSD, "hexBinary");
    public static final QName XSD_ANYSIMPLETYPE =
            new QName(URI_DEFAULT_SCHEMA_XSD, "anySimpleType");
    public static final QName XSD_ANYTYPE = new QName(URI_DEFAULT_SCHEMA_XSD, "anyType");
    public static final QName XSD_ANY = new QName(URI_DEFAULT_SCHEMA_XSD, "any");
    public static final QName AXIS2_NONE = new QName("http://org.apache.axis2", "none");
    public static final QName XSD_QNAME = new QName(URI_DEFAULT_SCHEMA_XSD, "QName");
    public static final QName XSD_DATETIME = new QName(URI_DEFAULT_SCHEMA_XSD, "dateTime");
    public static final QName XSD_DATE = new QName(URI_DEFAULT_SCHEMA_XSD, "date");
    public static final QName XSD_TIME = new QName(URI_DEFAULT_SCHEMA_XSD, "time");
    public static final QName XSD_TIMEINSTANT1999 = new QName(URI_1999_SCHEMA_XSD, "timeInstant");
    public static final QName XSD_TIMEINSTANT2000 = new QName(URI_2000_SCHEMA_XSD, "timeInstant");

    public static final QName XSD_NORMALIZEDSTRING =
            new QName(URI_2001_SCHEMA_XSD, "normalizedString");
    public static final QName XSD_TOKEN = new QName(URI_2001_SCHEMA_XSD, "token");

    public static final QName XSD_UNSIGNEDLONG = new QName(URI_2001_SCHEMA_XSD, "unsignedLong");
    public static final QName XSD_UNSIGNEDINT = new QName(URI_2001_SCHEMA_XSD, "unsignedInt");
    public static final QName XSD_UNSIGNEDSHORT = new QName(URI_2001_SCHEMA_XSD, "unsignedShort");
    public static final QName XSD_UNSIGNEDBYTE = new QName(URI_2001_SCHEMA_XSD, "unsignedByte");
    public static final QName XSD_POSITIVEINTEGER =
            new QName(URI_2001_SCHEMA_XSD, "positiveInteger");
    public static final QName XSD_NEGATIVEINTEGER =
            new QName(URI_2001_SCHEMA_XSD, "negativeInteger");
    public static final QName XSD_NONNEGATIVEINTEGER =
            new QName(URI_2001_SCHEMA_XSD, "nonNegativeInteger");
    public static final QName XSD_NONPOSITIVEINTEGER =
            new QName(URI_2001_SCHEMA_XSD, "nonPositiveInteger");

    public static final QName XSD_YEARMONTH = new QName(URI_2001_SCHEMA_XSD, "gYearMonth");
    public static final QName XSD_MONTHDAY = new QName(URI_2001_SCHEMA_XSD, "gMonthDay");
    public static final QName XSD_YEAR = new QName(URI_2001_SCHEMA_XSD, "gYear");
    public static final QName XSD_MONTH = new QName(URI_2001_SCHEMA_XSD, "gMonth");
    public static final QName XSD_DAY = new QName(URI_2001_SCHEMA_XSD, "gDay");
    public static final QName XSD_DURATION = new QName(URI_2001_SCHEMA_XSD, "duration");

    public static final QName XSD_NAME = new QName(URI_2001_SCHEMA_XSD, "Name");
    public static final QName XSD_NCNAME = new QName(URI_2001_SCHEMA_XSD, "NCName");
    public static final QName XSD_NMTOKEN = new QName(URI_2001_SCHEMA_XSD, "NMTOKEN");
    public static final QName XSD_NMTOKENS = new QName(URI_2001_SCHEMA_XSD, "NMTOKENS");
    public static final QName XSD_NOTATION = new QName(URI_2001_SCHEMA_XSD, "NOTATION");
    public static final QName XSD_ENTITY = new QName(URI_2001_SCHEMA_XSD, "ENTITY");
    public static final QName XSD_ENTITIES = new QName(URI_2001_SCHEMA_XSD, "ENTITIES");
    public static final QName XSD_IDREF = new QName(URI_2001_SCHEMA_XSD, "IDREF");
    public static final QName XSD_IDREFS = new QName(URI_2001_SCHEMA_XSD, "IDREFS");
    public static final QName XSD_ANYURI = new QName(URI_2001_SCHEMA_XSD, "anyURI");
    public static final QName XSD_LANGUAGE = new QName(URI_2001_SCHEMA_XSD, "language");
    public static final QName XSD_ID = new QName(URI_2001_SCHEMA_XSD, "ID");
    public static final QName XSD_SCHEMA = new QName(URI_2001_SCHEMA_XSD, "schema");

    public static final QName XML_LANG = new QName(NS_URI_XML, "lang");

    public static final QName SOAP_BASE64 = new QName(URI_SOAP11_ENC, "base64");
    public static final QName SOAP_BASE64BINARY = new QName(URI_SOAP11_ENC, "base64Binary");
    public static final QName SOAP_STRING = new QName(URI_SOAP11_ENC, "string");
    public static final QName SOAP_BOOLEAN = new QName(URI_SOAP11_ENC, "boolean");
    public static final QName SOAP_DOUBLE = new QName(URI_SOAP11_ENC, "double");
    public static final QName SOAP_FLOAT = new QName(URI_SOAP11_ENC, "float");
    public static final QName SOAP_INT = new QName(URI_SOAP11_ENC, "int");
    public static final QName SOAP_LONG = new QName(URI_SOAP11_ENC, "long");
    public static final QName SOAP_SHORT = new QName(URI_SOAP11_ENC, "short");
    public static final QName SOAP_BYTE = new QName(URI_SOAP11_ENC, "byte");
    public static final QName SOAP_INTEGER = new QName(URI_SOAP11_ENC, "integer");
    public static final QName SOAP_DECIMAL = new QName(URI_SOAP11_ENC, "decimal");
    public static final QName SOAP_ARRAY = new QName(URI_SOAP11_ENC, "Array");
    public static final QName SOAP_COMMON_ATTRS11 = new QName(URI_SOAP11_ENC, "commonAttributes");
    public static final QName SOAP_COMMON_ATTRS12 = new QName(URI_SOAP12_ENC, "commonAttributes");
    public static final QName SOAP_ARRAY_ATTRS11 = new QName(URI_SOAP11_ENC, "arrayAttributes");
    public static final QName SOAP_ARRAY_ATTRS12 = new QName(URI_SOAP12_ENC, "arrayAttributes");
    public static final QName SOAP_ARRAY12 = new QName(URI_SOAP12_ENC, "Array");

    public static final QName QNAME_LITERAL_ITEM = new QName(URI_LITERAL_ENC, "item");
    public static final QName QNAME_RPC_RESULT = new QName(URI_SOAP12_RPC, "result");

    public static final String MIME_CT_APPLICATION_OCTETSTREAM = "application/octet-stream";
    public static final String MIME_CT_TEXT_PLAIN = "text/plain";
    public static final String MIME_CT_IMAGE_JPEG = "image/jpeg";
    public static final String MIME_CT_IMAGE_GIF = "image/gif";
    public static final String MIME_CT_TEXT_XML = "text/xml";
    public static final String MIME_CT_APPLICATION_XML = "application/xml";
    public static final String MIME_CT_MULTIPART_PREFIX = "multipart/";

    public static final QName BASE_64_CONTENT_QNAME =
            new QName(URI_2001_SCHEMA_XSD, "base64Binary");
    public static final QName XMIME_CONTENT_TYPE_QNAME =
            new QName("http://www.w3.org/2004/06/xmlmime", "contentType");
    public static final String URI_SECURITYPOLICY =
            "http://schemas.xmlsoap.org/ws/2005/07/securitypolicy";
}
