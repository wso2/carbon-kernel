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
import org.apache.axis2.corba.idl.types.*;
import org.apache.axis2.corba.exceptions.InvalidIDLException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Eranga
 * Date: Feb 11, 2007
 * Time: 10:06:23 PM
 */
public class IDLVisitor /*implements ASTVisitor*/ {
    private static final Log log = LogFactory.getLog(IDLVisitor.class);
    private IDL idl = new IDL();
    private String module = "";
    private String moduleForInnerTypes = null;
    private static final String INNERTYPE_SUFFIX = "Package";

    public IDL getIDL() {
        return idl;
    }

    public void setIDL(IDL idl) {
        this.idl = idl;
    }

    public void visit(AST node) throws InvalidIDLException {
        while (node != null) {
            switch (node.getType()) {

                case IDLTokenTypes.LITERAL_interface: {
                    idl.addInterface(visitInterface(node));
                    break;
                }

                case IDLTokenTypes.LITERAL_module: {
                    AST moduleName = node.getFirstChild();
                    IDLVisitor moduleVisitor = new IDLVisitor();
                    moduleVisitor.setIDL(idl);
                    moduleVisitor.setModule(module + moduleName);
                    moduleVisitor.visit(moduleName.getNextSibling());
                    idl.addIDL(moduleVisitor.getIDL());
                    break;
                }

                case IDLTokenTypes.LITERAL_struct: {
                    idl.addType(visitStruct(node));
                    break;
                }

                case IDLTokenTypes.LITERAL_valuetype: {
                    idl.addType(visitValueType(node));
                    break;
                }
                
                case IDLTokenTypes.LITERAL_exception: {
                    idl.addType(visitException(node));
                    break;
                }

                case IDLTokenTypes.LITERAL_enum: {
                    idl.addType(visitEnum(node));
                    break;
                }

                case IDLTokenTypes.LITERAL_union: {
                    idl.addType(visitUnion(node));
                    break;
                }

                case IDLTokenTypes.LITERAL_typedef: {
                    visitAndAddTypedefs(node, module);
                    break;
                }

                case IDLTokenTypes.LITERAL_const: {
                    idl.addType(visitConst(node));
                    break;
                }

                default: {
                    throw new InvalidIDLException("Unsupported IDL token " + node);
                }
            }
            node = node.getNextSibling();
        }
    }

    private Struct visitStruct(AST node) throws InvalidIDLException {
        AST structNode = node.getFirstChild();
        String structName = structNode.toString();
        Struct struct = new Struct();
        struct.setModule(module);
        struct.setName(structName);
        AST memberTypeNode = structNode.getNextSibling();
        while (memberTypeNode != null) {
            Member member = new Member();
            DataType dataType = findDataType(memberTypeNode, structName);
            AST memberNode = memberTypeNode.getNextSibling();
            String memberName = memberNode.getText();
            int dimensions = memberNode.getNumberOfChildren();
            if (dimensions > 0) {
                AST dimensionNode = memberNode.getFirstChild();
                ArrayType arrayType = null;
                ArrayType rootArrayType = null;
                int i = 1;
                while(dimensionNode!=null) {
                    ArrayType temp = new ArrayType();
                    temp.setElementModule(module);
                    temp.setElementName(memberName);
                    temp.setDepth(i);
                    i++;
                    if (arrayType != null) {
                        arrayType.setDataType(temp);
                    } else {
                        rootArrayType = temp;
                    }
                    arrayType = temp;
                    arrayType.setElementCount(Integer.parseInt(dimensionNode.getText()));
                    dimensionNode = dimensionNode.getNextSibling();
                }
                if (arrayType != null) {
                    arrayType.setDataType(dataType);
                }
//                dataType = rootArrayType;
                Typedef typedef = new Typedef();
                typedef.setDataType(rootArrayType);
                typedef.setModule(module);
                typedef.setName(structName + '_' + memberName);
                idl.addType(typedef);
                dataType = typedef;
            }

            member.setDataType(dataType);
            member.setName(memberName);
            struct.addMember(member);
            memberTypeNode = memberNode.getNextSibling();
        }
        return struct;
    }

    private ValueType visitValueType(AST node) throws InvalidIDLException {
        AST valueNode = node.getFirstChild();
        ValueType value = new ValueType();
        value.setModule(module);
        String valueName = valueNode.toString();
        value.setName(valueName);
        AST memberModifierNode = valueNode.getNextSibling();
        while (memberModifierNode != null) {
            String memberModifierName = memberModifierNode.toString();
            if (!memberModifierName.equals("private") && !memberModifierName.equals("public")) {
                if (IDLTokenTypes.PREPROC_DIRECTIVE==memberModifierNode.getType()) {
                    if (memberModifierName.startsWith("pragma ID ")) {
                        memberModifierName = memberModifierName.substring(10);
                        String[] pragma = memberModifierName.split(" ");
                        if (pragma.length==2 && pragma[0]!=null && pragma[1]!=null && pragma[0].equals(value.getName())) {
                            pragma[1] = pragma[1].replace('"', ' ');
                            value.setId(pragma[1].trim());
                        }
                    }
                } else {
                    // abstract operation
                    value.addOperation(visitOperation(memberModifierNode));
                }
                memberModifierNode = memberModifierNode.getNextSibling();
                continue;
            }
            Member memberType = new Member();
            memberType.setModifier(memberModifierName);
            AST memberTypeNode = memberModifierNode.getNextSibling();
            memberType.setDataType(findDataType(memberTypeNode, valueName));
            AST memberNode = memberTypeNode.getNextSibling();
            memberType.setName(memberNode.toString());
            value.addMember(memberType);
            memberModifierNode = memberNode.getNextSibling();
        }
        return value;
    }

    private Interface visitInterface(AST node) throws InvalidIDLException {
        Interface intf = new Interface();
        intf.setModule(module);
        AST interfaceNode = node.getFirstChild();
        String interfaceName = interfaceNode.toString();
        intf.setName(interfaceName);
        moduleForInnerTypes = module + interfaceName + INNERTYPE_SUFFIX + CompositeDataType.MODULE_SEPERATOR;
        AST node2 = interfaceNode.getNextSibling();
        while (node2 != null) {
            switch (node2.getType()) {
            case IDLTokenTypes.LITERAL_struct:
                Struct innerStruct = visitStruct(node2);
                innerStruct.setModule(moduleForInnerTypes);
                idl.addType(innerStruct);
                break;
            case IDLTokenTypes.LITERAL_valuetype:
                log.error("Unsupported IDL token " + node2);
                // CORBA 3.O spec does not support this
                break;
            case IDLTokenTypes.LITERAL_exception:
                Struct innerEx = visitException(node2);
                innerEx.setModule(moduleForInnerTypes);
                idl.addType(innerEx);
                break;
            case IDLTokenTypes.LITERAL_enum:
                EnumType innerEnum = visitEnum(node2);
                innerEnum.setModule(moduleForInnerTypes);
                idl.addType(innerEnum);
                break;
            case IDLTokenTypes.LITERAL_union:
                UnionType innerUnion = visitUnion(node2);
                innerUnion.setModule(moduleForInnerTypes);
                idl.addType(innerUnion);
                break;
            case IDLTokenTypes.LITERAL_typedef:
                visitAndAddTypedefs(node2, moduleForInnerTypes);
                break;
            case IDLTokenTypes.LITERAL_const:
                log.error("Unsupported IDL token " + node2);
                break;
            case IDLTokenTypes.LITERAL_attribute:
                intf.addOperation(visitGetAttribute(node2));
                intf.addOperation(visitSetAttribute(node2));
                break;
            default:
                if (node2.toString().startsWith("pragma ID ")) {
                    String pragmaId = node2.toString().substring(10);
                    String[] pragma = pragmaId.split(" ");
                    if (pragma.length==2 && pragma[0]!=null && pragma[1]!=null && pragma[0].equals(intf.getName())) {
                        pragma[1] = pragma[1].replace('"', ' ');
                        intf.setId(pragma[1].trim());
                    }
                } else {
                    intf.addOperation(visitOperation(node2));
                }
                break;
            }
            node2 = node2.getNextSibling();
        }
        moduleForInnerTypes = null;
        return intf;
    }

    private Operation visitGetAttribute(AST node) throws InvalidIDLException {
        Operation operation = new Operation();
        AST type = node.getFirstChild();
        AST name = type.getNextSibling();
        String attrName = name.toString();
        operation.setReturnType(findDataType(type, attrName));
        operation.setName("_get_" + attrName);
        return operation;
    }

    private Operation visitSetAttribute(AST node) throws InvalidIDLException {
        Operation operation = new Operation();
        AST type = node.getFirstChild();
        operation.setReturnType(PrimitiveDataType.getPrimitiveDataType("void"));
        AST name = type.getNextSibling();
        String attrName = name.toString();
        operation.setName("_set_" + attrName);
        Member param = new Member();
        param.setName(attrName);
        param.setDataType(findDataType(type, attrName));
        operation.addParam(param);
        return operation;
    }

    private Operation visitOperation(AST node) throws InvalidIDLException {
        Operation operation = new Operation();
        String opName = node.toString();
        operation.setName(opName);
        AST type = node.getFirstChild();
        operation.setReturnType(findDataType(type, opName));
        AST mode = type.getNextSibling();
        while(mode != null) {
            if (IDLTokenTypes.LITERAL_raises == mode.getType()) {
                AST idlType = mode.getFirstChild();
                while(idlType != null) {
                    operation.addRaises((ExceptionType) findDataType(idlType, opName));
                    idlType = idlType.getNextSibling();
                }
            } else {
                Member param = new Member();
                param.setMode(mode.toString());
                AST idlType = mode.getFirstChild();
                AST paramName = idlType.getNextSibling();
                String paramNameStr = paramName.toString();
                param.setDataType(findDataType(idlType, paramNameStr));
                param.setName(paramNameStr);
                operation.addParam(param);
            }
            mode = mode.getNextSibling();
        }
        return operation;
    }

    private ExceptionType visitException(AST node) throws InvalidIDLException {
        ExceptionType raisesType = new ExceptionType();
        AST exNode = node.getFirstChild();
        String exName = exNode.toString();
        raisesType.setModule(module);
        raisesType.setName(exName);
        AST memberTypeNode = exNode.getNextSibling();
        while (memberTypeNode != null) {
            Member member = new Member();
            member.setDataType(findDataType(memberTypeNode, exName));
            AST memberNode = memberTypeNode.getNextSibling();
            member.setName(memberNode.toString());
            raisesType.addMember(member);
            memberTypeNode = memberNode.getNextSibling();
        }
        return raisesType;
    }

    private DataType findDataType(AST typeNode, String parentName) throws InvalidIDLException {
        return findDataType(typeNode, parentName, true, false);
    }
    private DataType findDataType(AST typeNode, String parentName, boolean root, boolean noTypeDefForSeqs) throws InvalidIDLException {
        // Check for sequences
        if (typeNode.getType()==IDLTokenTypes.LITERAL_sequence) {
            SequenceType sequenceType = visitAnonymousSequence(typeNode, parentName, root);
            if (noTypeDefForSeqs) {
                return sequenceType;
            }
            Typedef typedef = new Typedef();
            typedef.setDataType(sequenceType);
            typedef.setModule(module);
            typedef.setName(parentName + '_' + sequenceType.getName());
            idl.addType(typedef);
            return typedef;
        }

        DataType dataType;

        String typeName;
        if (typeNode.getType() == IDLTokenTypes.LITERAL_unsigned) {
            AST nextNode = typeNode.getNextSibling();
            if (nextNode == null) {
                throw new InvalidIDLException("'unsigned' without a data type");
            } else if (nextNode.getType() == IDLTokenTypes.LITERAL_short) {
                typeNode.setNextSibling(nextNode.getNextSibling());
                typeNode.setFirstChild(nextNode.getFirstChild());
                typeName = "ushort";
            } else if (nextNode.getType() == IDLTokenTypes.LITERAL_long) {
                AST nextToLong = nextNode.getNextSibling();
                if (nextToLong != null && nextToLong.getType() == IDLTokenTypes.LITERAL_long) {
                    typeNode.setNextSibling(nextToLong.getNextSibling());
                    typeNode.setFirstChild(nextToLong.getFirstChild());
                    typeName = "ulonglong";
                } else {
                    typeNode.setNextSibling(nextNode.getNextSibling());
                    typeNode.setFirstChild(nextNode.getFirstChild());
                    typeName = "ulong";
                }
            } else {
                throw new InvalidIDLException("either 'long' or 'short' is expected after the 'unsigned' keyword");
            }
        } else if (typeNode.getType() == IDLTokenTypes.LITERAL_long) {
            AST nextToLong = typeNode.getNextSibling();
            if (nextToLong != null && nextToLong.getType() == IDLTokenTypes.LITERAL_long) {
                typeNode.setNextSibling(nextToLong.getNextSibling());
                typeNode.setFirstChild(nextToLong.getFirstChild());
                typeName = "longlong";
            } else {
                typeName = "long";
            }
        } else {
            typeName = getTypeName(typeNode);    
        }


       /*   Map compositeDataTypes = idl.getCompositeDataTypes();
            if (compositeDataTypes!=null) {
            if (!module.equals("")) {
                if (!typeName.startsWith(module)) {
                    dataType = (DataType) idl.getCompositeDataTypes().get(module + typeName);
                }
            }

            if (dataType==null && moduleForInnerTypes!=null) {
                if (!typeName.startsWith(module)) {
                    dataType = (DataType) idl.getCompositeDataTypes().get(moduleForInnerTypes + typeName);
                }
            }

            if (dataType==null)
                dataType = (DataType) idl.getCompositeDataTypes().get(typeName);
        }


        if (dataType == null)
            dataType = PrimitiveDataType.getPrimitiveDataType(typeName);

        if (dataType == null)
            throw new InvalidIDLException("Invalid data type: " + typeName);
        }

        */
        dataType = getDataType(typeName);

        return dataType;
    }

    DataType getDataType(String typeName) throws InvalidIDLException {
        DataType dataType = null;
        Map compositeDataTypes = idl.getCompositeDataTypes();

        if (compositeDataTypes != null) {
            if (moduleForInnerTypes!=null) {
                if (!typeName.startsWith(module)) {
                    dataType = (DataType) compositeDataTypes.get(moduleForInnerTypes + typeName);
                }
            }

            String tempModule = module;
            int modSepLen = CompositeDataType.MODULE_SEPERATOR.length();
            while (dataType == null) {
                dataType = (DataType) idl.getCompositeDataTypes().get(tempModule + typeName);
                if (dataType == null && tempModule.length() > 0) {
                    if (tempModule.endsWith(CompositeDataType.MODULE_SEPERATOR)) {
                        tempModule = tempModule.substring(0, tempModule.length() - modSepLen);    
                    }
                    int modSepPos = tempModule.lastIndexOf(CompositeDataType.MODULE_SEPERATOR);
                    if (modSepPos < 0) {
                        tempModule = "";
                    } else {
                        tempModule = tempModule.substring(0, modSepPos + modSepLen);
                    }
                } else {
                    break;
                }
            }
        }

        if (dataType == null) {
            dataType = PrimitiveDataType.getPrimitiveDataType(typeName);
        }

        if (dataType == null) {
            throw new InvalidIDLException("Invalid data type: " + typeName);
        }

        return dataType;
    }

    public String getTypeName(AST node) {
        String typeName = node.getText();
        AST memberTypeNodeChild = node.getFirstChild();
        while(memberTypeNodeChild!=null) {
            typeName = typeName + CompositeDataType.MODULE_SEPERATOR + memberTypeNodeChild.toString();
            memberTypeNodeChild = memberTypeNodeChild.getNextSibling();
        }
        return typeName;
    }

    public void setModule(String module) {
        if (module==null || module.length()<1)
            module = "";
        else if (!module.endsWith(CompositeDataType.MODULE_SEPERATOR))
            module += CompositeDataType.MODULE_SEPERATOR;
        this.module = module;
    }

    private EnumType visitEnum(AST node) {
        AST enumNode = node.getFirstChild();
        String enumName = enumNode.toString();
        EnumType enumType = new EnumType();
        enumType.setModule(module);
        enumType.setName(enumName);
        AST memberTypeNode = enumNode.getNextSibling();
        while (memberTypeNode != null) {
            enumType.addEnumMember(memberTypeNode.toString());
            memberTypeNode = memberTypeNode.getNextSibling();
        }
        return enumType;
    }

    private UnionType visitUnion(AST node) throws InvalidIDLException {
        UnionType unionType = new UnionType();
        AST unNode = node.getFirstChild();
        String unName = unNode.toString();
        unionType.setModule(module);
        unionType.setName(unName);
        AST switchTypeNode = unNode.getNextSibling();
        DataType discrimType = findDataType(switchTypeNode, unName);
        unionType.setDiscriminatorType(discrimType);
        AST caseOrDefaultNode = switchTypeNode.getNextSibling();
        while (caseOrDefaultNode != null) {
            UnionMember unionMember = new UnionMember();
            AST typeNode;
            if (IDLTokenTypes.LITERAL_default == caseOrDefaultNode.getType()) {
                unionMember.setDefault(true);
                typeNode = caseOrDefaultNode.getFirstChild();
            } else {
                unionMember.setDefault(false);
                AST caseValueNode = caseOrDefaultNode.getFirstChild();
                String caseNodeText = caseValueNode.getText();
                if (!(discrimType instanceof EnumType) && IDLTokenTypes.IDENT == caseValueNode.getType()) {
                    //Get const value
                    DataType constType = getDataType(caseNodeText);
                    if (constType instanceof ConstType) {
                        caseNodeText = ((ConstType) constType).getValue().toString();
                    } else {
                        throw new InvalidIDLException(caseNodeText + "is not a constant name");
                    }
                }
                unionMember.setDiscriminatorValue(caseNodeText);
                typeNode = caseValueNode.getNextSibling();
            }


            unionMember.setDataType(findDataType(typeNode, unName));

            AST memberNode = typeNode.getNextSibling();
            unionMember.setName(memberNode.toString());
            unionType.addMember(unionMember);
            caseOrDefaultNode = caseOrDefaultNode.getNextSibling();
        }
        return unionType;
    }

    private void visitAndAddTypedefs(AST node, String moduleName) throws InvalidIDLException {
        AST typedefNode = node.getFirstChild();

        DataType dataType;
        //SequenceType sequence = null;
        //if (typedefNode.getType()==IDLTokenTypes.LITERAL_sequence) {
        //    sequence = visitAnonymousSequence(typedefNode);
        //    dataType = sequence.getDataType();
        //} else {
            dataType = findDataType(typedefNode, null, true, true);
        //}
        AST typedefNameNode = typedefNode.getNextSibling();
        AST dimensionNode;
        String typedefName = typedefNameNode.toString();
        Typedef typedef;
        while (typedefNameNode != null) {
            int dimensions = typedefNameNode.getNumberOfChildren();
            if (dimensions > 0) {
                dimensionNode = typedefNameNode.getFirstChild();
                ArrayType arrayType = null;
                ArrayType rootArrayType = null;
                int i = 1;
                while(dimensionNode!=null) {
                    ArrayType temp = new ArrayType();
                    temp.setElementModule(moduleName);
                    temp.setElementName(typedefName);
                    temp.setDepth(i);
                    i++;
                    if (arrayType != null) {
                        arrayType.setDataType(temp);
                    } else {
                        rootArrayType = temp;
                    }
                    arrayType = temp;
                    arrayType.setElementCount(Integer.parseInt(dimensionNode.getText()));
                    dimensionNode = dimensionNode.getNextSibling();
                }
                if (arrayType != null) {
                    arrayType.setDataType(dataType);
                }
                dataType = rootArrayType;
            }
            typedef = new Typedef();
            typedef.setDataType(dataType);
            typedef.setModule(moduleName);
            typedef.setName(typedefName);
            idl.addType(typedef);
            typedefNameNode = typedefNameNode.getNextSibling();
        }
    }

    private SequenceType visitAnonymousSequence(AST node, String parentName, boolean root) throws InvalidIDLException {
        AST typeNode = node.getFirstChild();
        SequenceType sequenceType = new SequenceType();
        DataType dataType = findDataType(typeNode, parentName, false, false);
        sequenceType.setDataType(dataType);
        sequenceType.setElementModule(module);
        AST elementNode = node.getNextSibling();
        if (elementNode != null && root) {
            String elementName = elementNode.getText();
            sequenceType.setName(elementName);
            SequenceType tempSeqType = sequenceType;
            int i = 1;
            DataType tempDataType;
            while(true) {
                tempSeqType.setElementName(elementName);
                tempSeqType.setDepth(i);
                i++;
                tempDataType = tempSeqType.getDataType();
                if (tempDataType instanceof SequenceType) {
                    tempSeqType = (SequenceType) tempDataType;    
                } else {
                    break;
                }
            }
        }

        AST countNode = typeNode.getNextSibling();
        if (countNode!=null) {
            int count;
            if (IDLTokenTypes.IDENT == countNode.getType()) {
                //Get const value
                String constName = countNode.getText();
                DataType constType = getDataType(constName);
                if (constType instanceof ConstType) {
                    Object countValue = ((ConstType) constType).getValue();
                    if (countValue instanceof Integer) {
                        count = ((Integer) countValue).intValue();
                    } else {
                        throw new InvalidIDLException(constName + "is not a long");
                    }
                } else {
                    throw new InvalidIDLException(constName + "is not a constant name");
                }
            } else {
                count = Integer.parseInt(countNode.getText());
            }
            sequenceType.setElementCount(count);
            //sequenceType.setBounded(true);
        } else {
            sequenceType.setElementCount(0);
            //sequenceType.setBounded(false);
        }
        return sequenceType;
    }

    private ConstType visitConst(AST node) throws InvalidIDLException {
        AST constTypeNode = node.getFirstChild();
        AST constNameNode = constTypeNode.getNextSibling();
        while (constNameNode != null && IDLTokenTypes.IDENT != constNameNode.getType()) {
            constNameNode = constNameNode.getNextSibling();    
        }

        if (constNameNode == null) {
            throw new InvalidIDLException("Constant name not found");
        }

        String constName = constNameNode.toString();
        ConstType constType = new ConstType();
        constType.setModule(module);
        constType.setName(constName);
        DataType type = findDataType(constTypeNode, constName);
        constType.setDataType(type);
        AST constValueNode = constNameNode.getNextSibling();
        constType.setValue(ExpressionUtil.eval(constValueNode, type, this));
        //System.out.println(constType.getValue());
        return constType;
    }

}
