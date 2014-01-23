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

package org.apache.axis2.corba.receivers;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.corba.deployer.CorbaConstants;
import org.apache.axis2.corba.exceptions.CorbaException;
import org.apache.axis2.corba.exceptions.CorbaInvocationException;
import org.apache.axis2.corba.idl.IDLProcessor;
import org.apache.axis2.corba.idl.PreProcessorInputStream;
import org.apache.axis2.corba.idl.types.AbstractCollectionType;
import org.apache.axis2.corba.idl.types.ArrayType;
import org.apache.axis2.corba.idl.types.CompositeDataType;
import org.apache.axis2.corba.idl.types.DataType;
import org.apache.axis2.corba.idl.types.EnumType;
import org.apache.axis2.corba.idl.types.ExceptionType;
import org.apache.axis2.corba.idl.types.IDL;
import org.apache.axis2.corba.idl.types.Member;
import org.apache.axis2.corba.idl.types.PrimitiveDataType;
import org.apache.axis2.corba.idl.types.SequenceType;
import org.apache.axis2.corba.idl.types.Struct;
import org.apache.axis2.corba.idl.types.Typedef;
import org.apache.axis2.corba.idl.types.UnionMember;
import org.apache.axis2.corba.idl.types.UnionType;
import org.apache.axis2.corba.idl.types.ValueType;
import org.apache.axis2.corba.idl.values.AbstractCollectionValue;
import org.apache.axis2.corba.idl.values.AbstractValue;
import org.apache.axis2.corba.idl.values.AliasValue;
import org.apache.axis2.corba.idl.values.ArrayValue;
import org.apache.axis2.corba.idl.values.EnumValue;
import org.apache.axis2.corba.idl.values.ExceptionValue;
import org.apache.axis2.corba.idl.values.ObjectByValue;
import org.apache.axis2.corba.idl.values.SequenceValue;
import org.apache.axis2.corba.idl.values.StreamableValueFactory;
import org.apache.axis2.corba.idl.values.StructValue;
import org.apache.axis2.corba.idl.values.UnionValue;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.java2wsdl.TypeTable;
import org.apache.axis2.namespace.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.omg.CORBA.Any;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA_2_3.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import javax.xml.namespace.QName;
import java.io.File;
//import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
//import java.util.zip.ZipEntry;
//import java.util.zip.ZipInputStream;

public class CorbaUtil implements CorbaConstants {
    private static Map IDL_CACHE = new HashMap();
    private static final Log log = LogFactory.getLog(CorbaUtil.class);

    public static org.omg.CORBA_2_3.ORB getORB(AxisService service) {
        Parameter orbClass = service.getParameter(ORB_CLASS);
        Parameter orbSingletonClass = service.getParameter(ORB_SINGLETON_CLASS);
        Properties props = System.getProperties();

        if (orbClass!=null)
            props.put(ORG_OMG_CORBA_ORBCLASS, ((String) orbClass.getValue()).trim());
        else
            props.put(ORG_OMG_CORBA_ORBCLASS, DEFAULR_ORB_CLASS);

        if (orbSingletonClass!=null)
            props.put(ORG_OMG_CORBA_ORBSINGLETON_CLASS, ((String) orbSingletonClass.getValue()).trim());
        else
            props.put(ORG_OMG_CORBA_ORBSINGLETON_CLASS, DEFAULT_ORBSINGLETON_CLASS);

        return (ORB) ORB.init(new String[]{}, props);
    }

    public static org.omg.CORBA.Object resolveObject(AxisService service, org.omg.CORBA_2_3.ORB orb) throws CorbaInvocationException {
        org.omg.CORBA.Object obj;
        try {
            Parameter namingServiceUrl = service.getParameter(NAMING_SERVICE_URL);
            Parameter objectName = service.getParameter(OBJECT_NAME);
            Parameter iorFilePath = service.getParameter(IOR_FILE_PATH);
            Parameter iorString = service.getParameter(IOR_STRING);

            if (namingServiceUrl!=null && objectName!=null) {
                obj = orb.string_to_object(((String) namingServiceUrl.getValue()).trim());
                NamingContextExt nc = NamingContextExtHelper.narrow(obj);
                obj = nc.resolve(nc.to_name(((String) objectName.getValue()).trim()));
            } else if (iorFilePath!=null) {
                FileReader fileReader = new FileReader(((String) iorFilePath.getValue()).trim());
                char[] buf = new char[1000];
                fileReader.read(buf);
                obj = orb.string_to_object((new String(buf)).trim());
                fileReader.close();
            } else if (iorString!=null) {
                obj = orb.string_to_object(((String) iorString.getValue()).trim());
            } else {
                throw new CorbaInvocationException("cannot resolve object");
            }

        } catch (NotFound notFound) {
            throw new CorbaInvocationException("cannot resolve object", notFound);
        } catch (CannotProceed cannotProceed) {
            throw new CorbaInvocationException("cannot resolve object", cannotProceed);
        } catch (InvalidName invalidName) {
            throw new CorbaInvocationException("cannot resolve object", invalidName);
        } catch (IOException e) {
            throw new CorbaInvocationException("cannot resolve object", e);
        }
        return obj;
    }

    public static IDL getIDL(AxisService service, ORB orb, String dirName) throws CorbaException {
        Parameter idlFile = service.getParameter(IDL_FILE);

        if (idlFile == null) {
            throw new CorbaInvocationException("Please specify the IDL file");    
        }

        String idlFileName = ((String) idlFile.getValue()).trim();
        String cacheKey = dirName + File.separator + idlFileName;
        IDL idl = (IDL) IDL_CACHE.get(cacheKey);
        if (idl==null) {
            try {
                /*File file = new File(dirName);
                InputStream stream;
                if (file.isDirectory()) {
                    stream = new FileInputStream(cacheKey);
                } else {
                    ZipInputStream zin = new ZipInputStream(new FileInputStream(file));

                    ZipEntry entry;
                    boolean found = false;
                    while ((entry = zin.getNextEntry()) != null) {
                        if (entry.getName().equalsIgnoreCase(idlFileName)) {
                            found = true;
                            break;
                        }
                    }

                    if (!found)
                        new CorbaInvocationException("cannot find " + idlFileName + " in " + file.getPath());

                    stream = zin;
                }*/
                InputStream stream = new PreProcessorInputStream(dirName, idlFileName);
                //TODO: Set pre-processor system and user input paths
                IDLProcessor idlProcessor = new IDLProcessor(stream);
                idl = idlProcessor.process();
                stream.close();
                IDL_CACHE.put(cacheKey, idl);
            } catch (IOException e) {
                throw new CorbaInvocationException("cannot process idl file", e);
            }
        }

        Map types = idl.getCompositeDataTypes();
        if (types!=null) {
            Iterator iter = types.values().iterator();
            while (iter.hasNext()) {
                DataType type =  (DataType) iter.next();
                if (type instanceof ValueType) {
                    StreamableValueFactory.register(orb, (ValueType) type);
                }
            }
        }
        return idl;
    }

    public static Invoker getInvoker(AxisService service, org.omg.CORBA.Object obj, IDL idl, String methodName) throws CorbaInvocationException {
        InvokerFactory invokerFactory = new CorbaInvokerFactory(idl);
        Parameter interfaceName = service.getParameter(INTERFACE_NAME);
        if (interfaceName==null)
            throw new CorbaInvocationException("interfaceName cannot be null");
        return invokerFactory.newInvoker(((String) interfaceName.getValue()).trim(), methodName, obj);
    }

    public static Object[] extractParameters(OMElement methodElement, Member[] parameterMembers) throws CorbaInvocationException {
        if (parameterMembers==null)
            return new Object[0];

        Object[] retObjs = new Object[parameterMembers.length];

        if (methodElement==null)
            return retObjs;

        Iterator paramsIter = methodElement.getChildElements();
        Map paramsMap = new HashMap();
        String localName;
        while(paramsIter!=null && paramsIter.hasNext()) {
            OMElement param = (OMElement) paramsIter.next();
            localName = param.getLocalName();
            if (paramsMap.containsKey(localName)) {
                Object value = paramsMap.get(localName);
                if (value instanceof List) {
                    ((List) value).add(param);
                } else {
                    List valueList = new ArrayList();
                    valueList.add(value);
                    valueList.add(param);
                    paramsMap.put(localName, valueList);
                }
            } else {
                paramsMap.put(localName, param);
            }
        }

        String paramName;
        for (int i = 0; i < parameterMembers.length; i++) {
            paramName = parameterMembers[i].getName();
            retObjs[i] = extractValue(parameterMembers[i].getDataType(), paramsMap.get(paramName));
        }
        return retObjs;
    }

    private static Object extractValue(DataType dataType, Object param) throws CorbaInvocationException {
        if (dataType instanceof Typedef) {
            Typedef typedef = (Typedef) dataType;
            AliasValue aliasValue = new AliasValue(typedef);
            OMElement paramElement;
            if (param instanceof OMElement)
                paramElement = (OMElement) param;
            else
                return null;

            DataType aliasType = typedef.getDataType();
            if (!(aliasType instanceof AbstractCollectionType)) {
                paramElement = paramElement.getFirstElement();
                if (paramElement == null || !ARRAY_ITEM.equals(paramElement.getLocalName()))
                    return null;
            }
            aliasValue.setValue(extractValue(aliasType, paramElement));
            return aliasValue;
        } else if (dataType instanceof PrimitiveDataType) {
            if (param!=null)
                return parseValue(dataType, ((OMElement) param).getText());
        } else if (dataType instanceof AbstractCollectionType) {
            AbstractCollectionType collectionType = (AbstractCollectionType) dataType;
            OMElement paramElement;
            if (param instanceof OMElement)
                paramElement = (OMElement) param;
            else
                return null;

            Iterator paramsIter = paramElement.getChildElements();
            List children = new ArrayList();
            while (paramsIter.hasNext()) {
                children.add(extractValue(collectionType.getDataType(), paramsIter.next()));
            }

            AbstractCollectionValue collectionValue;
            if (collectionType.isArray()) {
                collectionValue = new ArrayValue((ArrayType) collectionType);
            } else if (collectionType.isSequence()) {
                collectionValue = new SequenceValue((SequenceType) collectionType);
            } else {
                return null;
            }

            collectionValue.setValues(children.toArray());
            return collectionValue;
        } else if (dataType instanceof EnumType) {
            EnumType enumType = (EnumType) dataType;
            String enumText = ((OMElement) param).getText();
            int index = enumType.getEnumMembers().indexOf(enumText);
            if (index >= 0) {
                EnumValue enumValue = new EnumValue(enumType);
                enumValue.setValue(index);
                return enumValue;
            }
        } else if (dataType instanceof UnionType) {
            UnionType unionType = (UnionType) dataType;
            OMElement unElement = ((OMElement) param).getFirstElement();
            String unionMemberName = unElement.getLocalName();
            UnionValue unionValue = new UnionValue(unionType);
            unionValue.setMemberName(unionMemberName);
            Member[] members = unionType.getMembers();
            UnionMember member = null;
            for (int i = 0; i < members.length; i++) {
                member = (UnionMember) members[i];
                if (member.getName().equals(unionMemberName)) {
                    break;
                }
            }
            if (member != null) {
                unionValue.setMemberValue(extractValue(member.getDataType(), unElement));
            }
            return unionValue;
        } else if (dataType instanceof CompositeDataType) {
            CompositeDataType compositeType = (CompositeDataType) dataType;
            Member[] compositeMembers = compositeType.getMembers();
            Object[] compositeValues = extractParameters(((OMElement) param), compositeMembers);

            AbstractValue value;
            if (compositeType instanceof ValueType)
                value = new ObjectByValue((ValueType) compositeType);
            else if (compositeType instanceof Struct)
                value = new StructValue((Struct) compositeType);
            else
                throw new CorbaInvocationException("Parameter type not supported");

            value.setMemberValues(compositeValues);
            return value;
        }
        return null;
    }

    public static void processResponse(Object resObject,
                                       Member[] params,
                                       Object[] outParamValues,
                                       DataType dataType,
                                       AxisService service,
                                       String methodName,
                                       SOAPFactory fac,
                                       String messageNameSpace,
                                       MessageContext outMessage) throws AxisFault {
        boolean qualified = service.isElementFormDefault();
        OMNamespace ns = fac.createOMNamespace(messageNameSpace, service.getSchemaTargetNamespacePrefix());
        OMElement bodyContent = fac.createOMElement(methodName + RESPONSE, ns);
        OMElement child;

        if (qualified) {
            child = fac.createOMElement(RETURN_WRAPPER, ns);
        } else {
            child = fac.createOMElement(RETURN_WRAPPER, null);
        }
        bodyContent.addChild(child);

        if (dataType!=null
                && !getQualifiedName(dataType).equals(VOID)
                && resObject!=null) {
            processResponse(child, bodyContent, resObject, dataType, fac, ns, qualified, service);
        } else {
            child.addAttribute("nil", "true", fac.createOMNamespace(Constants.URI_2001_SCHEMA_XSI,
                    Constants.NS_PREFIX_SCHEMA_XSI));
        }

        Member param;
        List outParamList = Arrays.asList(outParamValues);
        Iterator paramsIter = outParamList.iterator();
        for (int i = 0; i < params.length; i++) {
            param = params[i];
            if (Member.MODE_INOUT.equals(param.getMode())
                    || Member.MODE_OUT.equals(param.getMode())) {
                if (qualified) {
                    child = fac.createOMElement(param.getName(), ns);
                } else {
                    child = fac.createOMElement(param.getName(), null);
                }
                bodyContent.addChild(child);
                processResponse(child, bodyContent, paramsIter.next(), param.getDataType(), fac, ns, qualified, service);
            }
        }

        SOAPEnvelope envelope = fac.getDefaultEnvelope();
        envelope.getBody().addChild(bodyContent);
        outMessage.setEnvelope(envelope);
    }

    private static void processResponse(OMElement child, OMElement bodyContent, Object resObject, DataType dataType,
                                        SOAPFactory fac, OMNamespace defaultNS, boolean qualified, AxisService service) {
        if (dataType instanceof PrimitiveDataType) {
            child.addChild(fac.createOMText(child, resObject.toString()));
        } else if (dataType instanceof Typedef) {
            Typedef typedef = (Typedef) dataType;
            AliasValue aliasValue = (AliasValue) resObject;
            OMNamespace ns = getNameSpaceForType(fac, service, typedef);
            OMElement item = fac.createOMElement(ARRAY_ITEM, ns, child);
            processResponse(item, child, aliasValue.getValue(), typedef.getDataType(), fac, ns, qualified, service);
        } else if (dataType instanceof AbstractCollectionType) {
            AbstractCollectionType collectionType = (AbstractCollectionType) dataType;
            AbstractCollectionValue collectionValue = (AbstractCollectionValue) resObject;
            Object[] values = collectionValue.getValues();
            int length = values.length;
            for (int i=0; i<length; i++) {
                OMElement outer = bodyContent;
                if (collectionType.getDataType() instanceof AbstractCollectionType) {
                    outer = child;
                    if (qualified) {
                        child = fac.createOMElement(ARRAY_ITEM, defaultNS);
                    } else {
                        child = fac.createOMElement(ARRAY_ITEM, null);
                    }
                    outer.addChild(child);
                }
                processResponse(child, outer, values[i], collectionType.getDataType(), fac, defaultNS, qualified, service);
                if (i < (length -1)) {
                    if (qualified) {
                        child = fac.createOMElement(ARRAY_ITEM, defaultNS);
                    } else {
                        child = fac.createOMElement(ARRAY_ITEM, null);
                    }
                    bodyContent.addChild(child);
                }
            }
        } else if (dataType instanceof ValueType || dataType instanceof Struct) {
            AbstractValue resValue = (AbstractValue) resObject;
            Member[] members = resValue.getMembers();
            Object[] memberValues = resValue.getMemberValues();
            OMNamespace ns = getNameSpaceForType(fac, service, (CompositeDataType) dataType);
            for (int i = 0; i < memberValues.length; i++) {
                OMElement memberElement = fac.createOMElement(members[i].getName(), ns);
                processResponse(memberElement, bodyContent, memberValues[i], members[i].getDataType(), fac, ns, qualified, service);
                child.addChild(memberElement);
            }
        } else if (dataType instanceof UnionType) {
            UnionValue unionValue = (UnionValue) resObject;
            OMElement unMember;
            OMNamespace ns = getNameSpaceForType(fac, service, (CompositeDataType) dataType);
            if (qualified) {
                unMember = fac.createOMElement(unionValue.getMemberName(), ns);
            } else {
                unMember = fac.createOMElement(unionValue.getMemberName(), null);
            }
            processResponse(unMember, child, unionValue.getMemberValue(), unionValue.getMemberType(), fac, ns, qualified, service);
            child.addChild(unMember);
        } else if (dataType instanceof EnumType) {
            EnumValue enumValue = (EnumValue) resObject;
            child.addChild(fac.createOMText(child, enumValue.getValueAsString()));
        }
    }

    private static OMNamespace getNameSpaceForType(SOAPFactory fac, AxisService service, CompositeDataType dataType) {
        TypeTable typeTable = service.getTypeTable();
        String fullname = (dataType.getModule()!=null) ? dataType.getModule() + dataType.getName() : dataType.getName();
        fullname = fullname.replaceAll(CompositeDataType.MODULE_SEPERATOR, ".");
        QName qname = typeTable.getQNamefortheType(fullname);
        if (qname==null)
            return null;
        return fac.createOMNamespace(qname.getNamespaceURI(), qname.getPrefix());
    }

    public static String getQualifiedName(DataType type){
        if (type instanceof CompositeDataType) {
            CompositeDataType compositeType = (CompositeDataType) type;
            String module = compositeType.getModule();
            module = (module == null) ? "" : module.replaceAll("::", ".");
            return  module + compositeType.getName();
        } else if (type instanceof PrimitiveDataType) {
            PrimitiveDataType primitiveDataType = (PrimitiveDataType) type;
            return primitiveDataType.getTypeName();
        }
        return null;
    }

    public static void insertValue(Any arg, DataType type, Object value) {
        switch(type.getTypeCode().kind().value()) {
            case TCKind._tk_long: arg.insert_long(((Integer) value).intValue()); break;
            case TCKind._tk_ulong: arg.insert_ulong(((Integer) value).intValue()); break;
            case TCKind._tk_longlong: arg.insert_longlong(((Long) value).longValue()); break;
            case TCKind._tk_ulonglong: arg.insert_ulonglong(((Long) value).longValue()); break;
            case TCKind._tk_short: arg.insert_short(((Short) value).shortValue()); break;
            case TCKind._tk_ushort: arg.insert_ushort(((Short) value).shortValue()); break;
            case TCKind._tk_float: arg.insert_float(((Float) value).floatValue()); break;
            case TCKind._tk_double: arg.insert_double(((Double) value).floatValue()); break;
            case TCKind._tk_char: arg.insert_char(((Character) value).charValue()); break;
            case TCKind._tk_wchar: arg.insert_wchar(((Character) value).charValue()); break;
            case TCKind._tk_boolean: arg.insert_boolean(((Boolean) value).booleanValue()); break;
            case TCKind._tk_octet: arg.insert_octet(((Byte) value).byteValue()); break;
            case TCKind._tk_string: arg.insert_string((String) value); break;
            case TCKind._tk_wstring: arg.insert_wstring((String) value); break;
            case TCKind._tk_any: arg.insert_any((Any) value); break;
            case TCKind._tk_value: arg.insert_Value((Serializable) value); break;
            case TCKind._tk_objref: arg.insert_Object((org.omg.CORBA.Object) value); break;
            case TCKind._tk_struct:
                StructValue structValue = (StructValue) value;
                org.omg.CORBA_2_3.portable.OutputStream outputStream = (org.omg.CORBA_2_3.portable.OutputStream) arg.create_output_stream();
                arg.type(structValue.getTypeCode());
                structValue.write(outputStream);
                arg.read_value(outputStream.create_input_stream (), structValue.getTypeCode());
                break;
            case TCKind._tk_enum:
                EnumValue enumValue = (EnumValue) value;
                outputStream = (org.omg.CORBA_2_3.portable.OutputStream) arg.create_output_stream();
                arg.type(enumValue.getTypeCode());
                enumValue.write(outputStream);
                arg.read_value(outputStream.create_input_stream (), enumValue.getTypeCode());
                break;
            case TCKind._tk_union:
                UnionValue unionValue = (UnionValue) value;
                outputStream = (org.omg.CORBA_2_3.portable.OutputStream) arg.create_output_stream();
                arg.type(unionValue.getTypeCode());
                unionValue.write(outputStream);
                arg.read_value(outputStream.create_input_stream (), unionValue.getTypeCode());
                break;
            case TCKind._tk_alias:
                AliasValue aliasValue = (AliasValue) value;
                outputStream = (org.omg.CORBA_2_3.portable.OutputStream) arg.create_output_stream();
                arg.type(aliasValue.getTypeCode());
                aliasValue.write(outputStream);
                arg.read_value(outputStream.create_input_stream (), aliasValue.getTypeCode());
                break;
            case TCKind._tk_sequence:
                SequenceValue sequenceValue = (SequenceValue) value;
                outputStream = (org.omg.CORBA_2_3.portable.OutputStream) arg.create_output_stream();
                arg.type(sequenceValue.getTypeCode());
                sequenceValue.write(outputStream);
                arg.read_value(outputStream.create_input_stream (), sequenceValue.getTypeCode());
                break;
            case TCKind._tk_array:
                ArrayValue arrayValue = (ArrayValue) value;
                outputStream = (org.omg.CORBA_2_3.portable.OutputStream) arg.create_output_stream();
                arg.type(arrayValue.getTypeCode());
                arrayValue.write(outputStream);
                arg.read_value(outputStream.create_input_stream (), arrayValue.getTypeCode());
                break;
            default:
                log.error("ERROR! Invalid dataType");
                break;
        }
    }

    public static Object extractValue(DataType returnType, Any returned) {
        Object returnValue = null;
        TypeCode typeCode = returnType.getTypeCode();
        switch(typeCode.kind().value()) {
            case TCKind._tk_void: returnValue = null; break;
            case TCKind._tk_long: returnValue = new Integer(returned.extract_long()); break;
            case TCKind._tk_ulong: returnValue = new Integer(returned.extract_ulong()); break;
            case TCKind._tk_longlong: returnValue = new Long(returned.extract_longlong()); break;
            case TCKind._tk_ulonglong: returnValue = new Long(returned.extract_ulonglong()); break;
            case TCKind._tk_short: returnValue = new Short(returned.extract_short()); break;
            case TCKind._tk_ushort: returnValue = new Short(returned.extract_ushort()); break;
            case TCKind._tk_float: returnValue = new Float(returned.extract_float()); break;
            case TCKind._tk_double: returnValue = new Double(returned.extract_double()); break;
            case TCKind._tk_char: returnValue = new Character(returned.extract_char()); break;
            case TCKind._tk_wchar: returnValue = new Character(returned.extract_wchar()); break;
            case TCKind._tk_boolean: returnValue = Boolean.valueOf(returned.extract_boolean()); break;
            case TCKind._tk_octet: returnValue = new Byte(returned.extract_octet()); break;
            case TCKind._tk_string: returnValue = returned.extract_string(); break;
            case TCKind._tk_wstring: returnValue = returned.extract_wstring(); break;
            case TCKind._tk_any: returnValue = returned.extract_any(); break;
            case TCKind._tk_value: returnValue = returned.extract_Value(); break;
            case TCKind._tk_objref: returnValue = returned.extract_Object(); break;
            //case TCKind._tk_longdouble :
            case TCKind._tk_struct:
                Struct struct = (Struct) returnType;
                StructValue structValue = new StructValue(struct);
                org.omg.CORBA_2_3.portable.InputStream inputStream = (org.omg.CORBA_2_3.portable.InputStream) returned.create_input_stream();
                structValue.read(inputStream);
                returnValue = structValue;
                break;
            case TCKind._tk_except:
                ExceptionType exceptionType = (ExceptionType) returnType;
                ExceptionValue exceptionValue = new ExceptionValue(exceptionType);
                inputStream = (org.omg.CORBA_2_3.portable.InputStream) returned.create_input_stream();
                exceptionValue.read(inputStream);
                returnValue = exceptionValue;
                break;
            case TCKind._tk_enum:
                EnumType enumType = (EnumType) returnType;
                EnumValue enumValue = new EnumValue(enumType);
                inputStream = (org.omg.CORBA_2_3.portable.InputStream) returned.create_input_stream();
                enumValue.read(inputStream);
                returnValue = enumValue;
                break;
            case TCKind._tk_union:
                UnionType unionType = (UnionType) returnType;
                inputStream = (org.omg.CORBA_2_3.portable.InputStream) returned.create_input_stream();
                UnionValue unionValue = new UnionValue(unionType);
                unionValue.read(inputStream);
                returnValue = unionValue;
                break;
            case TCKind._tk_alias:
                Typedef typedef = (Typedef) returnType;
                inputStream = (org.omg.CORBA_2_3.portable.InputStream) returned.create_input_stream();
                AliasValue aliasValue = new AliasValue(typedef);
                aliasValue.read(inputStream);
                returnValue = aliasValue;
                break;
            case TCKind._tk_sequence:
                SequenceType sequenceType = (SequenceType) returnType;
                inputStream = (org.omg.CORBA_2_3.portable.InputStream) returned.create_input_stream();
                SequenceValue sequenceValue = new SequenceValue(sequenceType);
                sequenceValue.read(inputStream);
                returnValue = sequenceValue;
                break;
            case TCKind._tk_array:
                ArrayType arrayType = (ArrayType) returnType;
                inputStream = (org.omg.CORBA_2_3.portable.InputStream) returned.create_input_stream();
                ArrayValue arrayValue = new ArrayValue(arrayType);
                arrayValue.read(inputStream);
                returnValue = arrayValue;
                break;
            default:
                log.error("ERROR! Invalid dataType");
                break;
        }
        return returnValue;
    }

    public static Object parseValue(DataType type, String value) {
        if (value == null)
            return null;
        value = value.trim();
        Object ret = null;
        switch(type.getTypeCode().kind().value()) {
            case TCKind._tk_long : ret = Integer.valueOf(value); break;
            case TCKind._tk_ulong : ret = Integer.valueOf(value); break;
            case TCKind._tk_longlong : ret = Long.valueOf(value); break;
            case TCKind._tk_ulonglong : ret = Long.valueOf(value); break;
            case TCKind._tk_short : ret = Short.valueOf(value); break;
            case TCKind._tk_ushort : ret = Short.valueOf(value); break;
            case TCKind._tk_float : ret = Float.valueOf(value); break;
            case TCKind._tk_double : ret = Double.valueOf(value); break;
            case TCKind._tk_char : ret = Character.valueOf(value.charAt(0)); break;
            case TCKind._tk_wchar : ret = Character.valueOf(value.charAt(0)); break;
            case TCKind._tk_boolean : ret = Boolean.valueOf(value); break;
            case TCKind._tk_octet : ret = Byte.valueOf(value); break;
            case TCKind._tk_string : ret = value; break;
            case TCKind._tk_wstring : ret = value; break;
            case TCKind._tk_enum :
                EnumType enumType = (EnumType) type;
                EnumValue enumValue = new EnumValue(enumType);
                int i = enumType.getEnumMembers().indexOf(value);
                enumValue.setValue(i);
                ret = enumValue;
                break;
            default:
                log.error("ERROR! Invalid dataType");
                break;
        }
        return ret;
    }

    public static Object getEmptyValue(DataType type) {
        switch(type.getTypeCode().kind().value()) {
            case TCKind._tk_long: return new Integer(0);
            case TCKind._tk_ulong: return new Integer(0);
            case TCKind._tk_longlong: return new Long(0);
            case TCKind._tk_ulonglong: return new Long(0);
            case TCKind._tk_short: return new Short("0");
            case TCKind._tk_ushort: return new Short("0");
            case TCKind._tk_float: return new Float(0f);
            case TCKind._tk_double: return new Double(0d);
            case TCKind._tk_char: return new Character('0');
            case TCKind._tk_wchar: return new Character('0');
            case TCKind._tk_boolean: return Boolean.FALSE;
            case TCKind._tk_octet: return new Byte("0");
            case TCKind._tk_string: return "";
            case TCKind._tk_wstring: return "";
            //case TCKind._tk_any: return new Any();
            case TCKind._tk_value: return "";
            //case TCKind._tk_objref: return new org.omg.CORBA.Object();
            case TCKind._tk_struct:
                Struct struct = (Struct) type;
                StructValue value = new StructValue(struct);
                Member[] members = struct.getMembers();
                Object[] memberValues = new Object[members.length];
                for (int i = 0; i < members.length; i++) {
                    memberValues[i] = getEmptyValue(members[i].getDataType());
                }
                value.setMemberValues(memberValues);
                return value;
            case TCKind._tk_enum: return new EnumValue((EnumType) type);
            case TCKind._tk_union:
                UnionType unionType = (UnionType) type;
                UnionValue unionValue = new UnionValue(unionType);
                members = unionType.getMembers();
                unionValue.setMemberName(members[0].getName());
                unionValue.setMemberType(members[0].getDataType());
                unionValue.setMemberValue(getEmptyValue(members[0].getDataType()));
                return unionValue;
            case TCKind._tk_alias:
                Typedef typedef = (Typedef) type;
                AliasValue aliasValue = new AliasValue(typedef);
                aliasValue.setValue(getEmptyValue(typedef.getDataType()));
                return aliasValue;
            case TCKind._tk_sequence:
                SequenceType sequenceType = (SequenceType) type;
                SequenceValue sequenceValue = new SequenceValue(sequenceType);
                sequenceValue.setValues(new Object[0]);
                return sequenceValue;
            case TCKind._tk_array:
                ArrayType arrayType = (ArrayType) type;
                ArrayValue arrayValue = new ArrayValue(arrayType);
                Object[] objects = new Object[arrayType.getElementCount()];
                DataType arrayDataType = arrayType.getDataType();
                for (int i = 0; i < objects.length; i++) {
                    objects[i] = getEmptyValue(arrayDataType);
                }
                arrayValue.setValues(objects);
                return arrayValue;
            default:
                log.error("ERROR! Invalid dataType");
        }
        return null;
    }
}
