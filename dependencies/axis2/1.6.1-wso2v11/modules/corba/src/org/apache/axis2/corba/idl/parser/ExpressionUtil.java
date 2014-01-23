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

package org.apache.axis2.corba.idl.parser;

import antlr.collections.AST;
import org.apache.axis2.corba.exceptions.InvalidIDLException;
import org.apache.axis2.corba.idl.types.*;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.TCKind;

import java.util.Map;

public class ExpressionUtil {
    public static Object eval(AST expressionNode, DataType returnType, IDLVisitor visitor) throws InvalidIDLException {
        Object value;
        AST node1;
        AST node2;
        int expressionType = expressionNode.getType();
        TypeCode typeCode = returnType.getTypeCode();
        switch (expressionType) {

            case IDLTokenTypes.PLUS:
                node1 = expressionNode.getFirstChild();
                node2 = node1.getNextSibling();
                value = node2 == null? eval(node1, returnType, visitor)
                        : add(eval(node1, returnType, visitor), eval(node2, returnType, visitor), typeCode);
                break;

            case IDLTokenTypes.MINUS:
                node1 = expressionNode.getFirstChild();
                node2 = node1.getNextSibling();
                value = node2 == null? minus(eval(node1, returnType, visitor), typeCode)
                        : subtract(eval(node1, returnType, visitor), eval(node2, returnType, visitor), typeCode);
                break;

            case IDLTokenTypes.STAR:
                node1 = expressionNode.getFirstChild();
                node2 = node1.getNextSibling();
                value = multiply(eval(node1, returnType, visitor), eval(node2, returnType, visitor), typeCode);
                break;

            case IDLTokenTypes.DIV:
                node1 = expressionNode.getFirstChild();
                node2 = node1.getNextSibling();
                value = div(eval(node1, returnType, visitor), eval(node2, returnType, visitor), typeCode);
                break;

            case IDLTokenTypes.MOD:
                node1 = expressionNode.getFirstChild();
                node2 = node1.getNextSibling();
                value = mod(eval(node1, returnType, visitor), eval(node2, returnType, visitor), typeCode);
                break;

            case IDLTokenTypes.OR:
                node1 = expressionNode.getFirstChild();
                node2 = node1.getNextSibling();
                value = or(eval(node1, returnType, visitor), eval(node2, returnType, visitor), typeCode);
                break;

            case IDLTokenTypes.AND:
                node1 = expressionNode.getFirstChild();
                node2 = node1.getNextSibling();
                value = and(eval(node1, returnType, visitor), eval(node2, returnType, visitor), typeCode);
                break;

            case IDLTokenTypes.RSHIFT:
                node1 = expressionNode.getFirstChild();
                node2 = node1.getNextSibling();
                value = rshift(eval(node1, returnType, visitor), eval(node2, returnType, visitor), typeCode);
                break;

            case IDLTokenTypes.LSHIFT:
                node1 = expressionNode.getFirstChild();
                node2 = node1.getNextSibling();
                value = lshift(eval(node1, returnType, visitor), eval(node2, returnType, visitor), typeCode);
                break;

            case IDLTokenTypes.XOR:
                node1 = expressionNode.getFirstChild();
                node2 = node1.getNextSibling();
                value = xor(eval(node1, returnType, visitor), eval(node2, returnType, visitor), typeCode);
                break;

            case IDLTokenTypes.TILDE:
                node1 = expressionNode.getFirstChild();
                Object boolObj = eval(node1, returnType, visitor);
                if (!(boolObj instanceof Boolean)) {
                    throw new InvalidIDLException("A boolean value is expected after (~) operator");
                }
                value = Boolean.valueOf(!((Boolean) boolObj).booleanValue());
                break;

            case IDLTokenTypes.INT:
            case IDLTokenTypes.FLOAT:
            case IDLTokenTypes.STRING_LITERAL:
            case IDLTokenTypes.WIDE_STRING_LITERAL:
            case IDLTokenTypes.CHAR_LITERAL:
            case IDLTokenTypes.WIDE_CHAR_LITERAL:
            case IDLTokenTypes.FIXED:
                value = getValueObject(expressionNode.getText(), returnType);
                break;

            case IDLTokenTypes.LITERAL_TRUE:
                value = Boolean.valueOf(true);
                break;

            case IDLTokenTypes.LITERAL_FALSE:
                value = Boolean.valueOf(true);
                break;

            case IDLTokenTypes.IDENT:
                value = getConstant(expressionNode.getText(), visitor);
                break;
            
            case IDLTokenTypes.LPAREN:
                value = eval(expressionNode.getFirstChild(), returnType, visitor);
                break;

            default:
                throw new InvalidIDLException("Unsupported IDL token " + expressionNode);
        }
        return value;
    }

    private static Object add(Object o1, Object o2, TypeCode returnType) throws InvalidIDLException {
        TCKind kind = returnType.kind();
        Object valueObj;
        switch (kind.value()) {
            case TCKind._tk_long:
            case TCKind._tk_ulong:
                valueObj = new Integer(((Integer) o1).intValue() + ((Integer) o2).intValue());
                break;
            case TCKind._tk_longlong:
            case TCKind._tk_ulonglong:
                valueObj = new Long(((Long) o1).longValue() + ((Long) o2).longValue());
                break;
            case TCKind._tk_float:
                valueObj = new Float(((Float) o1).floatValue() + ((Float) o2).floatValue());
                break;
            case TCKind._tk_short:
            case TCKind._tk_ushort:
                valueObj = new Short((short) (((Short) o1).shortValue() + ((Short) o2).shortValue()));
                break;
            case TCKind._tk_double:
                valueObj = new Double(((Double) o1).doubleValue() + ((Double) o2).doubleValue());
                break;
            case TCKind._tk_octet:
                valueObj = new Byte((byte) (((Byte) o1).byteValue() + ((Byte) o2).byteValue()));
                break;
            default:
                throw new InvalidIDLException("Unsupported IDL token");
        }
        return valueObj;
    }

    private static Object minus(Object o, TypeCode returnType) throws InvalidIDLException {
        TCKind kind = returnType.kind();
        Object valueObj;
        switch (kind.value()) {
            case TCKind._tk_long:
            case TCKind._tk_ulong:
                valueObj = new Integer(-((Integer) o).intValue());
                break;
            case TCKind._tk_longlong:
            case TCKind._tk_ulonglong:
                valueObj = new Long(-((Long) o).longValue());
                break;
            case TCKind._tk_float:
                valueObj = new Float(-((Float) o).floatValue());
                break;
            case TCKind._tk_short:
            case TCKind._tk_ushort:
                valueObj = new Short((short) (-((Short) o).shortValue()));
                break;
            case TCKind._tk_double:
                valueObj = new Double(- ((Double) o).doubleValue());
                break;
            case TCKind._tk_octet:
                valueObj = new Byte((byte) (- ((Byte) o).byteValue()));
                break;
            default:
                throw new InvalidIDLException("Unsupported IDL token");
        }
        return valueObj;
    }

    private static Object subtract(Object o1, Object o2, TypeCode returnType) throws InvalidIDLException {
        TCKind kind = returnType.kind();
        Object valueObj;
        switch (kind.value()) {
            case TCKind._tk_long:
            case TCKind._tk_ulong:
                valueObj = new Integer(((Integer) o1).intValue() - ((Integer) o2).intValue());
                break;
            case TCKind._tk_longlong:
            case TCKind._tk_ulonglong:
                valueObj = new Long(((Long) o1).longValue() - ((Long) o2).longValue());
                break;
            case TCKind._tk_float:
                valueObj = new Float(((Float) o1).floatValue() - ((Float) o2).floatValue());
                break;
            case TCKind._tk_short:
            case TCKind._tk_ushort:
                valueObj = new Short((short) (((Short) o1).shortValue() - ((Short) o2).shortValue()));
                break;
            case TCKind._tk_double:
                valueObj = new Double(((Double) o1).doubleValue() - ((Double) o2).doubleValue());
                break;
            case TCKind._tk_octet:
                valueObj = new Byte((byte) (((Byte) o1).byteValue() - ((Byte) o2).byteValue()));
                break;
            default:
                throw new InvalidIDLException("Unsupported IDL token");
        }
        return valueObj;
    }

    private static Object multiply(Object o1, Object o2, TypeCode returnType) throws InvalidIDLException {
        TCKind kind = returnType.kind();
        Object valueObj;
        switch (kind.value()) {
            case TCKind._tk_long:
            case TCKind._tk_ulong:
                valueObj = new Integer(((Integer) o1).intValue() * ((Integer) o2).intValue());
                break;
            case TCKind._tk_longlong:
            case TCKind._tk_ulonglong:
                valueObj = new Long(((Long) o1).longValue() * ((Long) o2).longValue());
                break;
            case TCKind._tk_float:
                valueObj = new Float(((Float) o1).floatValue() * ((Float) o2).floatValue());
                break;
            case TCKind._tk_short:
            case TCKind._tk_ushort:
                valueObj = new Short((short) (((Short) o1).shortValue() * ((Short) o2).shortValue()));
                break;
            case TCKind._tk_double:
                valueObj = new Double(((Double) o1).doubleValue() * ((Double) o2).doubleValue());
                break;
            case TCKind._tk_octet:
                valueObj = new Byte((byte) (((Byte) o1).byteValue() * ((Byte) o2).byteValue()));
                break;
            default:
                throw new InvalidIDLException("Unsupported IDL token");
        }
        return valueObj;
    }

    private static Object div(Object o1, Object o2, TypeCode returnType) throws InvalidIDLException {
        TCKind kind = returnType.kind();
        Object valueObj;
        switch (kind.value()) {
            case TCKind._tk_long:
            case TCKind._tk_ulong:
                valueObj = new Integer(((Integer) o1).intValue() / ((Integer) o2).intValue());
                break;
            case TCKind._tk_longlong:
            case TCKind._tk_ulonglong:
                valueObj = new Long(((Long) o1).longValue() / ((Long) o2).longValue());
                break;
            case TCKind._tk_float:
                valueObj = new Float(((Float) o1).floatValue() / ((Float) o2).floatValue());
                break;
            case TCKind._tk_short:
            case TCKind._tk_ushort:
                valueObj = new Short((short) (((Short) o1).shortValue() / ((Short) o2).shortValue()));
                break;
            case TCKind._tk_double:
                valueObj = new Double(((Double) o1).doubleValue() / ((Double) o2).doubleValue());
                break;
            case TCKind._tk_octet:
                valueObj = new Byte((byte) (((Byte) o1).byteValue() / ((Byte) o2).byteValue()));
                break;
            default:
                throw new InvalidIDLException("Unsupported IDL token");
        }
        return valueObj;
    }

    private static Object rshift(Object o1, Object o2, TypeCode returnType) throws InvalidIDLException {
        TCKind kind = returnType.kind();
        Object valueObj;
        switch (kind.value()) {
            case TCKind._tk_long:
            case TCKind._tk_ulong:
                valueObj = new Integer(((Integer) o1).intValue() >> ((Integer) o2).intValue());
                break;
            case TCKind._tk_longlong:
            case TCKind._tk_ulonglong:
                valueObj = new Long(((Long) o1).longValue() >> ((Long) o2).longValue());
                break;
            case TCKind._tk_short:
            case TCKind._tk_ushort:
                valueObj = new Short((short) (((Short) o1).shortValue() >> ((Short) o2).shortValue()));
                break;
            case TCKind._tk_octet:
                valueObj = new Byte((byte) (((Byte) o1).byteValue() >> ((Byte) o2).byteValue()));
                break;
            default:
                throw new InvalidIDLException("Unsupported IDL token");
        }
        return valueObj;
    }

    private static Object lshift(Object o1, Object o2, TypeCode returnType) throws InvalidIDLException {
        TCKind kind = returnType.kind();
        Object valueObj;
        switch (kind.value()) {
            case TCKind._tk_long:
            case TCKind._tk_ulong:
                valueObj = new Integer(((Integer) o1).intValue() << ((Integer) o2).intValue());
                break;
            case TCKind._tk_longlong:
            case TCKind._tk_ulonglong:
                valueObj = new Long(((Long) o1).longValue() << ((Long) o2).longValue());
                break;
            case TCKind._tk_short:
            case TCKind._tk_ushort:
                valueObj = new Short((short) (((Short) o1).shortValue() << ((Short) o2).shortValue()));
                break;
            case TCKind._tk_octet:
                valueObj = new Byte((byte) (((Byte) o1).byteValue() << ((Byte) o2).byteValue()));
                break;
            default:
                throw new InvalidIDLException("Unsupported IDL token");
        }
        return valueObj;
    }

    private static Object xor(Object o1, Object o2, TypeCode returnType) throws InvalidIDLException {
        TCKind kind = returnType.kind();
        Object valueObj;
        switch (kind.value()) {
            case TCKind._tk_long:
            case TCKind._tk_ulong:
                valueObj = new Integer(((Integer) o1).intValue() ^ ((Integer) o2).intValue());
                break;
            case TCKind._tk_longlong:
            case TCKind._tk_ulonglong:
                valueObj = new Long(((Long) o1).longValue() ^ ((Long) o2).longValue());
                break;
            case TCKind._tk_short:
            case TCKind._tk_ushort:
                valueObj = new Short((short) (((Short) o1).shortValue() ^ ((Short) o2).shortValue()));
                break;
            case TCKind._tk_octet:
                valueObj = new Byte((byte) (((Byte) o1).byteValue() ^ ((Byte) o2).byteValue()));
                break;
            default:
                throw new InvalidIDLException("Unsupported IDL token");
        }
        return valueObj;
    }

    private static Object mod(Object o1, Object o2, TypeCode returnType) throws InvalidIDLException {
        TCKind kind = returnType.kind();
        Object valueObj;
        switch (kind.value()) {
            case TCKind._tk_long:
            case TCKind._tk_ulong:
                valueObj = new Integer(((Integer) o1).intValue() % ((Integer) o2).intValue());
                break;
            case TCKind._tk_longlong:
            case TCKind._tk_ulonglong:
                valueObj = new Long(((Long) o1).longValue() % ((Long) o2).longValue());
                break;
            case TCKind._tk_float:
                valueObj = new Float(((Float) o1).floatValue() % ((Float) o2).floatValue());
                break;
            case TCKind._tk_short:
            case TCKind._tk_ushort:
                valueObj = new Short((short) (((Short) o1).shortValue() % ((Short) o2).shortValue()));
                break;
            case TCKind._tk_double:
                valueObj = new Double(((Double) o1).doubleValue() % ((Double) o2).doubleValue());
                break;
            case TCKind._tk_octet:
                valueObj = new Byte((byte) (((Byte) o1).byteValue() % ((Byte) o2).byteValue()));
                break;
            default:
                throw new InvalidIDLException("Unsupported IDL token");
        }
        return valueObj;
    }

    private static Object or(Object o1, Object o2, TypeCode returnType) throws InvalidIDLException {
        TCKind kind = returnType.kind();
        Object valueObj;
        switch (kind.value()) {
            case TCKind._tk_long:
            case TCKind._tk_ulong:
                valueObj = new Integer(((Integer) o1).intValue() | ((Integer) o2).intValue());
                break;
            case TCKind._tk_longlong:
            case TCKind._tk_ulonglong:
                valueObj = new Long(((Long) o1).longValue() | ((Long) o2).longValue());
                break;
            case TCKind._tk_short:
            case TCKind._tk_ushort:
                valueObj = new Short((short) (((Short) o1).shortValue() | ((Short) o2).shortValue()));
                break;
            case TCKind._tk_octet:
                valueObj = new Byte((byte) (((Byte) o1).byteValue() | ((Byte) o2).byteValue()));
                break;
            default:
                throw new InvalidIDLException("Unsupported IDL token");
        }
        return valueObj;
    }

    private static Object and(Object o1, Object o2, TypeCode returnType) throws InvalidIDLException {
        TCKind kind = returnType.kind();
        Object valueObj;
        switch (kind.value()) {
            case TCKind._tk_long:
            case TCKind._tk_ulong:
                valueObj = new Integer(((Integer) o1).intValue() & ((Integer) o2).intValue());
                break;
            case TCKind._tk_longlong:
            case TCKind._tk_ulonglong:
                valueObj = new Long(((Long) o1).longValue() & ((Long) o2).longValue());
                break;
            case TCKind._tk_short:
            case TCKind._tk_ushort:
                valueObj = new Short((short) (((Short) o1).shortValue() & ((Short) o2).shortValue()));
                break;
            case TCKind._tk_octet:
                valueObj = new Byte((byte) (((Byte) o1).byteValue() & ((Byte) o2).byteValue()));
                break;
            default:
                throw new InvalidIDLException("Unsupported IDL token");
        }
        return valueObj;
    }

    private static Object getValueObject(String value, DataType type) throws InvalidIDLException {
        TCKind kind = type.getTypeCode().kind();
        Object valueObj;
        switch (kind.value()) {
            case TCKind._tk_long:
            case TCKind._tk_ulong:
                valueObj = new Integer(value);
                break;
            case TCKind._tk_longlong:
            case TCKind._tk_ulonglong:
                valueObj = new Long(value);
                break;
            case TCKind._tk_float:
                valueObj = new Float(value);
                break;
            case TCKind._tk_short:
            case TCKind._tk_ushort:
                valueObj = new Short(value);
                break;
            case TCKind._tk_char:
            case TCKind._tk_wchar:
                valueObj = new Character(value.charAt(0));
                break;
            case TCKind._tk_double:
                valueObj = new Double(value);
                break;
            case TCKind._tk_octet:
                valueObj = new Byte(value);
                break;
            case TCKind._tk_string:
            case TCKind._tk_wstring:
                valueObj = value;
                break;
            case TCKind._tk_alias:
                Typedef typedef = (Typedef) type;
                valueObj = getValueObject(value, typedef.getDataType());
                break;
            default:
                throw new InvalidIDLException("Unsupported IDL token ");
        }
        return valueObj;
    }

    private static Object getConstant(String expressionName, IDLVisitor visitor) throws InvalidIDLException {
        Object value;
        DataType dataType = visitor.getDataType(expressionName);
        if (dataType != null && dataType instanceof ConstType) {
            ConstType constType = (ConstType) dataType;
            value = constType.getValue();
        } else {
            throw new InvalidIDLException("Constant " + expressionName + " not found.");
        }
        return value;
    }
}

