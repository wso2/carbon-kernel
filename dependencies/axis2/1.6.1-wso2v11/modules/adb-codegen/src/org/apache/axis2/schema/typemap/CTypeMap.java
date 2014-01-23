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

package org.apache.axis2.schema.typemap;

import org.apache.axis2.schema.SchemaConstants;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

/**
 * The C type map. uses a static map for caching
 */
public class CTypeMap implements TypeMap{

    private static Map typeMap = new HashMap();

    // Type map for the standard schema types
    public Map getTypeMap(){
         return typeMap;
    }

    static {
        // If SOAP 1.1 over the wire, map wrapper classes to XSD primitives.
        CTypeMap.addTypemapping(SchemaConstants.XSD_STRING,
                                 "axis2_char_t*");

        // The XSD Primitives are mapped to axis2/c primitives.
        CTypeMap.addTypemapping(SchemaConstants.XSD_BOOLEAN, "axis2_bool_t");
        CTypeMap.addTypemapping(SchemaConstants.XSD_DOUBLE, "double");
        CTypeMap.addTypemapping(SchemaConstants.XSD_FLOAT, "float");
        CTypeMap.addTypemapping(SchemaConstants.XSD_INT, "int");
        CTypeMap.addTypemapping(SchemaConstants.XSD_INTEGER,
                                 "int");
        CTypeMap.addTypemapping(SchemaConstants.XSD_LONG, "int64_t");
        CTypeMap.addTypemapping(SchemaConstants.XSD_SHORT, "short");
        CTypeMap.addTypemapping(SchemaConstants.XSD_BYTE, "axis2_byte_t");
        CTypeMap.addTypemapping(SchemaConstants.XSD_ANY, "axiom_node_t*");
        CTypeMap.addTypemapping(SchemaConstants.XSD_DECIMAL, "double");

        CTypeMap.addTypemapping(SchemaConstants.XSD_ANYTYPE,
                                 "axiom_node_t*");

        CTypeMap.addTypemapping(SchemaConstants.XSD_QNAME,
                                 "axutil_qname_t*");

        CTypeMap.addTypemapping(SchemaConstants.XSD_DATE,
                                 "axutil_date_time_t*");

        CTypeMap.addTypemapping(SchemaConstants.XSD_TIME,
                                 "axutil_date_time_t*");
        CTypeMap.addTypemapping(SchemaConstants.XSD_DATETIME,
                                 "axutil_date_time_t*");

        CTypeMap.addTypemapping(SchemaConstants.XSD_BASE64,
                                 "axutil_base64_binary_t*");

        CTypeMap.addTypemapping(SchemaConstants.XSD_HEXBIN,
                                 "axiom_node_t*");

        // These are the g* types (gYearMonth, etc) which map to Axis types
        // These types are mapped to an integer
        CTypeMap.addTypemapping(SchemaConstants.XSD_YEARMONTH,
                                 "axutil_date_time_t*");
        CTypeMap.addTypemapping(SchemaConstants.XSD_YEAR,
                                 "int");
        CTypeMap.addTypemapping(SchemaConstants.XSD_MONTH,
                                 "int");
        CTypeMap.addTypemapping(SchemaConstants.XSD_DAY,
                                 "int");
        CTypeMap.addTypemapping(SchemaConstants.XSD_MONTHDAY,
                                 "axutil_date_time_t*");

        // xsd:token
        CTypeMap.addTypemapping(SchemaConstants.XSD_TOKEN, "axis2_char_t*");

        // a xsd:normalizedString
        CTypeMap.addTypemapping(SchemaConstants.XSD_NORMALIZEDSTRING,
                                 "axis2_char_t*");

        // a xsd:unsignedLong
        CTypeMap.addTypemapping(SchemaConstants.XSD_UNSIGNEDLONG,
                                 "uint64_t");

        // a xsd:unsignedInt
        CTypeMap.addTypemapping(SchemaConstants.XSD_UNSIGNEDINT,
                                 "unsigned int");

        // a xsd:unsignedShort
        CTypeMap.addTypemapping(SchemaConstants.XSD_UNSIGNEDSHORT,
                                 "unsigned short");

        // a xsd:unsignedByte
        CTypeMap.addTypemapping(SchemaConstants.XSD_UNSIGNEDBYTE,
                                 "axis2_unsigned_byte_t");

        // a xsd:nonNegativeInteger
        CTypeMap.addTypemapping(SchemaConstants.XSD_NONNEGATIVEINTEGER,
                                 "unsigned int");

        // a xsd:negativeInteger
        CTypeMap.addTypemapping(SchemaConstants.XSD_NEGATIVEINTEGER,
                                 "int");

        // a xsd:positiveInteger
        CTypeMap.addTypemapping(SchemaConstants.XSD_POSITIVEINTEGER,
                                 "unsigned int");

        // a xsd:nonPositiveInteger
        CTypeMap.addTypemapping(SchemaConstants.XSD_NONPOSITIVEINTEGER,
                                 "unsigned int");

        // a xsd:Name
        CTypeMap.addTypemapping(SchemaConstants.XSD_NAME, "axis2_char_t*");

        // a xsd:NCName
        CTypeMap.addTypemapping(SchemaConstants.XSD_NCNAME, "axis2_char_t*");

        // a xsd:ID
        CTypeMap.addTypemapping(SchemaConstants.XSD_ID, "axis2_char_t*");

        // a xsd:language
        CTypeMap.addTypemapping(SchemaConstants.XSD_LANGUAGE, "axis2_char_t*");

        // a xsd:NmToken
        CTypeMap.addTypemapping(SchemaConstants.XSD_NMTOKEN, "axis2_char_t*");

        // a xsd:NmTokens
        CTypeMap.addTypemapping(SchemaConstants.XSD_NMTOKENS, "axis2_char_t*");

        // a xsd:NOTATION
        CTypeMap.addTypemapping(SchemaConstants.XSD_NOTATION, "axiom_node_t*");

        // a xsd:XSD_ENTITY
        CTypeMap.addTypemapping(SchemaConstants.XSD_ENTITY, "axis2_char_t*");

        // a xsd:XSD_ENTITIES
        CTypeMap.addTypemapping(SchemaConstants.XSD_ENTITIES, "axis2_char_t*");

        // a xsd:XSD_IDREF
        CTypeMap.addTypemapping(SchemaConstants.XSD_IDREF, "axis2_char_t*");

        // a xsd:XSD_XSD_IDREFS
        CTypeMap.addTypemapping(SchemaConstants.XSD_IDREFS, "axis2_char_t*");

        // a xsd:Duration
        CTypeMap.addTypemapping(SchemaConstants.XSD_DURATION, "axutil_duration_t*");

        // a xsd:anyURI
        CTypeMap.addTypemapping(SchemaConstants.XSD_ANYURI,
                                 "axutil_uri_t*");
    }

    private static void addTypemapping(QName name, String str) {
        CTypeMap.typeMap.put(name, str);
    }

    // Type map for the soap encoding types
    public Map getSoapEncodingTypesMap() {
        return soapEncodingTypeMap;
    }

    private static Map soapEncodingTypeMap = new HashMap();

    static {
        // populate the soapEncodingTypeMap
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_ARRAY,
                "axutil_array_list_t*");
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_STRUCT,
                "axiom_node_t*");
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_BASE64,
                "axutil_base64_binary_t*");
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_DURATION,
                "axutil_duration_t*");
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_DATETIME,
                "axutil_date_time_t*");
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_NOTATION,
                "axiom_node_t*");
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_TIME,
                "axutil_date_time_t*");
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_DATE,
                "axutil_date_time_t*");
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_GYEARMONTH,
                "axutil_date_time_t*");
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_GYEAR,
                "int");
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_GMONTHDAY,
                "axutil_date_time_t*");
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_GDAY,
                "int");
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_GMONTH,
                "int");
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_BOOLEAN,
                "axis2_bool_t");
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_BASE64BINARY,
                "axutil_base64_binary_t*");
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_HEXBINARY,
                "axiom_node_t*");
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_FLOAT,
                "float");
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_DOUBLE,
                "double");
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_ANYURI,
                "axutil_uri_t*");
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_QNAME,
                "axutil_qname_t*");
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_STRING,
                "axis2_char_t*");
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_NORMALIZEDSTRING,
                "axis2_char_t*");
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_TOKEN,
                "axis2_char_t*");
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_LANGUAGE,
                "axis2_char_t*");
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_NAME,
                "axis2_char_t*");
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_NMTOKEN,
                "axis2_char_t*");
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_NCNAME,
                "axis2_char_t*");
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_ID,
                "axis2_char_t*");
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_IDREF,
                "axis2_char_t*");
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_ENTITY,
                "axis2_char_t*");
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_IDREFS,
                "axis2_char_t*");
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_ENTITIES,
                "axis2_char_t*");
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_DECIMAL,
                "double");
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_INTEGER,
                "int");
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_NONPOSITIVEINTEGER,
                "int");
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_NEGATIVEINTEGER,
                "int");
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_LONG,
                "int64_t");
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_INT,
                "int");
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_SHORT,
                "short");
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_BYTE,
                "axis2_byte_t");
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_NONNEGATIVEINTEGER,
                "unsigned int");
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_UNSIGNEDLONG,
                "uint64_t");
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_UNSIGNEDINT,
                "unsigned int");
    }

    private static void addSoapEncodingTypeMapping(QName name, String className) {
        soapEncodingTypeMap.put(name, className);
    }
}
