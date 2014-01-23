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

package org.apache.axis2.corba.idl.types;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCode;

import java.util.HashMap;
import java.util.Map;

public class PrimitiveDataType extends DataType {
    private static final Log log = LogFactory.getLog(PrimitiveDataType.class);

    private static Map PRIMITIVE_TYPES = new HashMap();
    static {
        ORB orb = ORB.init();
        PRIMITIVE_TYPES.put("long",orb.get_primitive_tc(TCKind.tk_long));
        PRIMITIVE_TYPES.put("ulong",orb.get_primitive_tc(TCKind.tk_ulong));
        PRIMITIVE_TYPES.put("longlong",orb.get_primitive_tc(TCKind.tk_longlong));
        PRIMITIVE_TYPES.put("ulonglong",orb.get_primitive_tc(TCKind.tk_ulonglong));
        PRIMITIVE_TYPES.put("short",orb.get_primitive_tc(TCKind.tk_short));
        PRIMITIVE_TYPES.put("ushort",orb.get_primitive_tc(TCKind.tk_ushort));
        PRIMITIVE_TYPES.put("float",orb.get_primitive_tc(TCKind.tk_float));
        PRIMITIVE_TYPES.put("double",orb.get_primitive_tc(TCKind.tk_double));
        PRIMITIVE_TYPES.put("char",orb.get_primitive_tc(TCKind.tk_char));
        PRIMITIVE_TYPES.put("wchar",orb.get_primitive_tc(TCKind.tk_wchar));
        PRIMITIVE_TYPES.put("boolean",orb.get_primitive_tc(TCKind.tk_boolean));
        PRIMITIVE_TYPES.put("octet",orb.get_primitive_tc(TCKind.tk_octet));
        PRIMITIVE_TYPES.put("string",orb.get_primitive_tc(TCKind.tk_string));
        PRIMITIVE_TYPES.put("wstring",orb.get_primitive_tc(TCKind.tk_wstring));
        PRIMITIVE_TYPES.put("any",orb.get_primitive_tc(TCKind.tk_any));
        PRIMITIVE_TYPES.put("longdouble",orb.get_primitive_tc(TCKind.tk_longdouble));
        PRIMITIVE_TYPES.put("void",orb.get_primitive_tc(TCKind.tk_void));
        //PRIMITIVE_TYPES.put("dateTime",orb.get_primitive_tc(TCKind.tk_dateTime));
        //PRIMITIVE_TYPES.put("date",orb.get_primitive_tc(TCKind.tk_date));
        //PRIMITIVE_TYPES.put("time",orb.get_primitive_tc(TCKind.tk_time));
        //PRIMITIVE_TYPES.put("positiveInteger",orb.get_primitive_tc(TCKind.tk_positiveInteger));
        //PRIMITIVE_TYPES.put("nonPositiveInteger",orb.get_primitive_tc(TCKind.tk_nonPositiveInteger));
        //PRIMITIVE_TYPES.put("negativeInteger",orb.get_primitive_tc(TCKind.tk_negativeInteger));
        //PRIMITIVE_TYPES.put("nonNegativeInteger",orb.get_primitive_tc(TCKind.tk_nonNegativeInteger));
    }

    public static TypeCode getTypeCode(String typeName) {
        TypeCode typeCode = (TypeCode) PRIMITIVE_TYPES.get(typeName);
        if (typeCode == null && typeName != null && typeName.contains("::")) {
            String typeName2 = typeName.substring(0, typeName.indexOf("::"));
            typeCode = (TypeCode) PRIMITIVE_TYPES.get(typeName2);
            return typeCode;
        }
        return typeCode;
    }

    public static boolean isPrimitive(TypeCode typeCode) {
        return typeCode != null && PRIMITIVE_TYPES.values().contains(typeCode);
    }

    public static PrimitiveDataType getPrimitiveDataType(String typeName) {
        TypeCode typeCode = getTypeCode(typeName);
        if (typeCode == null) {
            return null;
        }
        return new PrimitiveDataType(typeCode);
    }

    public PrimitiveDataType(TypeCode typeCode){
        this.typeCode = typeCode;
    }

    protected TypeCode generateTypeCode() {
        return typeCode;
    }

    public String getTypeName() {
        String ret = null;
        if (typeCode != null) {
            TCKind kind = typeCode.kind();
            if (kind != null) {
                switch(kind.value()) {
                    case TCKind._tk_long : ret = "int"; break;
                    case TCKind._tk_ulong : ret = "int"; break;
                    case TCKind._tk_longlong : ret = "long"; break;
                    case TCKind._tk_ulonglong : ret = "long"; break;
                    case TCKind._tk_short : ret = "short"; break;
                    case TCKind._tk_ushort : ret = "short"; break;
                    case TCKind._tk_float : ret = "float"; break;
                    case TCKind._tk_double : ret = "double"; break;
                    case TCKind._tk_char : ret = "char"; break;
                    case TCKind._tk_wchar : ret = "char"; break;
                    case TCKind._tk_boolean : ret = "boolean"; break;
                    case TCKind._tk_octet : ret = "byte"; break;
                    case TCKind._tk_string : ret = "java.lang.String"; break;
                    case TCKind._tk_wstring : ret = "java.lang.String"; break;
                    case TCKind._tk_void : ret = "void"; break;
                    default:
                        log.error("Invalid primitive data type");
                        break;
                }
            }
        }
        return ret;
    }
}
