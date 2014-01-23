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

package org.apache.axis2.corba.idl.values;

import org.apache.axis2.corba.idl.types.ArrayType;
import org.apache.axis2.corba.idl.types.CompositeDataType;
import org.apache.axis2.corba.idl.types.DataType;
import org.apache.axis2.corba.idl.types.EnumType;
import org.apache.axis2.corba.idl.types.ExceptionType;
import org.apache.axis2.corba.idl.types.Member;
import org.apache.axis2.corba.idl.types.SequenceType;
import org.apache.axis2.corba.idl.types.Struct;
import org.apache.axis2.corba.idl.types.Typedef;
import org.apache.axis2.corba.idl.types.UnionType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.omg.CORBA.Any;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;

import java.io.Serializable;

public abstract class AbstractValue {
    protected Object[] memberValues;
    protected CompositeDataType dataType;
    private static final Log log = LogFactory.getLog(AbstractValue.class);

    protected AbstractValue (CompositeDataType dataType) {
        this.dataType = dataType;
    }

    public Member[] getMembers() {
        return dataType.getMembers();
    }

    public void setMemberValues(Object[] memberValues) {
        this.memberValues = memberValues;
    }

    public Object[] getMemberValues() {
        return memberValues;
    }

    public TypeCode getTypeCode() {
        return dataType.getTypeCode();
    }

    protected void write(Object value, DataType dataType, OutputStream outputStream) {
        TCKind kind = dataType.getTypeCode().kind();
        switch(kind.value()) {
            case TCKind._tk_long : outputStream.write_long(((Integer) value).intValue()); break;
            case TCKind._tk_ulong : outputStream.write_ulong(((Integer) value).intValue()); break;
            case TCKind._tk_longlong : outputStream.write_longlong(((Long) value).longValue()); break;
            case TCKind._tk_ulonglong : outputStream.write_ulonglong(((Long) value).longValue()); break;
            case TCKind._tk_short : outputStream.write_short(((Short) value).shortValue()); break;
            case TCKind._tk_ushort : outputStream.write_ushort(((Short) value).shortValue()); break;
            case TCKind._tk_float : outputStream.write_float(((Float) value).floatValue()); break;
            case TCKind._tk_double : outputStream.write_double(((Double) value).floatValue()); break;
            case TCKind._tk_char : outputStream.write_char(((Character) value).charValue()); break;
            case TCKind._tk_wchar : outputStream.write_wchar(((Character) value).charValue()); break;
            case TCKind._tk_boolean : outputStream.write_boolean(((Boolean) value).booleanValue()); break;
            case TCKind._tk_octet : outputStream.write_octet(((Byte) value).byteValue()); break;
            case TCKind._tk_string : outputStream.write_string((String) value); break;
            case TCKind._tk_wstring : outputStream.write_wstring((String) value); break;
            case TCKind._tk_any : outputStream.write_any((Any) value); break;
            case TCKind._tk_value : outputStream.write_value((Serializable) value); break;
            case TCKind._tk_struct : ((StructValue) value).write(outputStream); break;
            case TCKind._tk_enum : ((EnumValue) value).write(outputStream); break;
            case TCKind._tk_union: ((UnionValue) value).write(outputStream); break;
            case TCKind._tk_alias: ((AliasValue) value).write(outputStream); break;
            case TCKind._tk_sequence: ((SequenceValue) value).write(outputStream); break;
            case TCKind._tk_array: ((ArrayValue) value).write(outputStream); break;
            default:
                log.error("ERROR! Invalid dataType");
                break;
        }
    }

    protected Object read(DataType dataType, InputStream inputStream) {
        TCKind kind = dataType.getTypeCode().kind();
        Object ret = null;
        switch(kind.value()) {
            case TCKind._tk_long: ret = new Integer(inputStream.read_long()); break;
            case TCKind._tk_ulong: ret = new Integer(inputStream.read_ulong()); break;
            case TCKind._tk_longlong: ret = new Long(inputStream.read_longlong()); break;
            case TCKind._tk_ulonglong: ret = new Long(inputStream.read_ulonglong()); break;
            case TCKind._tk_short: ret = new Short(inputStream.read_short()); break;
            case TCKind._tk_ushort: ret = new Short(inputStream.read_ushort()); break;
            case TCKind._tk_float: ret = new Float(inputStream.read_float()); break;
            case TCKind._tk_double: ret = new Double(inputStream.read_double()); break;
            case TCKind._tk_char: ret = new Character(inputStream.read_char()); break;
            case TCKind._tk_wchar: ret = new Character(inputStream.read_wchar()); break;
            case TCKind._tk_boolean: ret = Boolean.valueOf(inputStream.read_boolean()); break;
            case TCKind._tk_octet: ret = new Byte(inputStream.read_octet()); break;
            case TCKind._tk_string: ret = inputStream.read_string(); break;
            case TCKind._tk_wstring: ret = inputStream.read_wstring(); break;
            case TCKind._tk_any: ret = inputStream.read_any(); break;
            case TCKind._tk_value: ret = inputStream.read_value(); break;
            //case TCKind._tk_longdouble :
            case TCKind._tk_struct:
                StructValue structValue = new StructValue((Struct) dataType);
                structValue.read(inputStream);
                ret = structValue;
                break;
            case TCKind._tk_enum:
                EnumValue enumValue = new EnumValue((EnumType) dataType);
                enumValue.read(inputStream);
                ret = enumValue;
                break;
            case TCKind._tk_union:
                UnionValue unionValue = new UnionValue((UnionType) dataType);
                unionValue.read(inputStream);
                ret = unionValue;
                break;
            case TCKind._tk_alias:
                AliasValue aliasValue = new AliasValue((Typedef) dataType);
                aliasValue.read(inputStream);
                ret = aliasValue;
                break;
            case TCKind._tk_sequence:
                SequenceValue sequenceValue = new SequenceValue((SequenceType) dataType);
                sequenceValue.read(inputStream);
                ret = sequenceValue;
                break;
            case TCKind._tk_array:
                ArrayValue arrayValue = new ArrayValue((ArrayType) dataType);
                arrayValue.read(inputStream);
                ret = arrayValue;
                break;
            case TCKind._tk_except:
                ExceptionValue exValue = new ExceptionValue((ExceptionType) dataType);
                exValue.read(inputStream);
                ret = exValue;
                break;
            default:
                log.error("ERROR! Invalid dataType");
                break;
        }
        return ret;
    }

    public String toString() {
        Member[] members = getMembers();
        String ret = "CompositeDataType name: " + dataType.getModule() + dataType.getName() + '\n';
        for (int i = 0; i < members.length; i++) {
            Object value = memberValues[i];
            ret += '\t' + members[i].getName() + ": " + value + '\n';
        }
        return ret;
    }
}
