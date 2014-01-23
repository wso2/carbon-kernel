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

package org.apache.axis2.corba.deployer;

import org.apache.axis2.corba.exceptions.SchemaGeneratorException;
import org.apache.axis2.corba.idl.types.AbstractCollectionType;
import org.apache.axis2.corba.idl.types.ArrayType;
import org.apache.axis2.corba.idl.types.CompositeDataType;
import org.apache.axis2.corba.idl.types.DataType;
import org.apache.axis2.corba.idl.types.EnumType;
import org.apache.axis2.corba.idl.types.ExceptionType;
import org.apache.axis2.corba.idl.types.IDL;
import org.apache.axis2.corba.idl.types.Interface;
import org.apache.axis2.corba.idl.types.Member;
import org.apache.axis2.corba.idl.types.Operation;
import org.apache.axis2.corba.idl.types.PrimitiveDataType;
import org.apache.axis2.corba.idl.types.Typedef;
import org.apache.axis2.corba.idl.types.UnionType;
import org.apache.axis2.corba.receivers.CorbaUtil;
import org.apache.axis2.description.java2wsdl.DefaultNamespaceGenerator;
import org.apache.axis2.description.java2wsdl.Java2WSDLConstants;
import org.apache.axis2.description.java2wsdl.NamespaceGenerator;
import org.apache.axis2.description.java2wsdl.TypeTable;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaChoice;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaEnumerationFacet;
import org.apache.ws.commons.schema.XmlSchemaForm;
import org.apache.ws.commons.schema.XmlSchemaImport;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction;
import org.apache.ws.commons.schema.utils.NamespaceMap;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SchemaGenerator implements CorbaConstants {
    private static int prefixCount = 1;
    protected Map targetNamespacePrefixMap = new Hashtable();
    protected Map schemaMap = new Hashtable();
    protected XmlSchemaCollection xmlSchemaCollection = new XmlSchemaCollection();
    private IDL idl;
    private String interfaceName;
    private TypeTable typeTable = new TypeTable();
    private String schemaTargetNameSpace;
    private String schema_namespace_prefix;
    private String attrFormDefault = null;
    private String elementFormDefault = null;
    private ArrayList excludeMethods = new ArrayList();
    private ArrayList extraClasses = null;
    private boolean useWSDLTypesNamespace = false;
    private Map pkg2nsmap = null;
    private NamespaceGenerator nsGen = null;
    private String targetNamespace = null;
    private ArrayList nonRpcMethods = new ArrayList();

    public NamespaceGenerator getNsGen() throws SchemaGeneratorException {
        if ( nsGen == null ) {
            nsGen = new DefaultNamespaceGenerator();
        }
        return nsGen;
    }

    public void setNsGen(NamespaceGenerator nsGen) {
        this.nsGen = nsGen;
    }

    public SchemaGenerator(IDL idl,
                           String interfaceName, String schematargetNamespace,
                           String schematargetNamespacePrefix)
            throws Exception {
        this.idl = idl;
        this.interfaceName = interfaceName;

        StringBuffer stringBuffer = namespaceFromModuleName(interfaceName, getNsGen());
        if (stringBuffer.length() == 0) {
            stringBuffer.append(Java2WSDLConstants.DEFAULT_TARGET_NAMESPACE);
        }
        this.targetNamespace = stringBuffer.toString();

        if (schematargetNamespace != null
                && schematargetNamespace.trim().length() != 0) {
            this.schemaTargetNameSpace = schematargetNamespace;
        } else {
            stringBuffer = namespaceFromModuleName(interfaceName, getNsGen());
            if (stringBuffer.length() == 0) {
                stringBuffer.append(Java2WSDLConstants.DEFAULT_TARGET_NAMESPACE);
            }
            stringBuffer.append(SCHEMA_NAMESPACE_EXTN);
            this.schemaTargetNameSpace = stringBuffer.toString();
        }

        if (schematargetNamespacePrefix != null
                && schematargetNamespacePrefix.trim().length() != 0) {
            this.schema_namespace_prefix = schematargetNamespacePrefix;
        } else {
            this.schema_namespace_prefix = Java2WSDLConstants.SCHEMA_NAMESPACE_PRFIX;
        }
    }

    private static StringBuffer namespaceFromModuleName(String interfaceName,
                                                        NamespaceGenerator nsGen) throws Exception {
        String moduleName = null;
        if (interfaceName != null) {
            int i = interfaceName.lastIndexOf("::");
            if (i > 0)
                moduleName = interfaceName.substring(0, i);
        }

        if (moduleName==null) {
            return new StringBuffer();
        } else {
            return nsGen.namespaceFromPackageName(moduleName);
        }
    }

    /**
     * Generates schema for all the parameters in method. First generates schema for all different
     * parameter type and later refers to them.
     *
     * @return Returns XmlSchema.
     * @throws SchemaGeneratorException if failed
     */
    public Collection generateSchema() throws SchemaGeneratorException {
        Map interfaces = idl.getInterfaces();

        if (interfaces==null)
            throw new SchemaGeneratorException("No interfaces defined");

        if (interfaceName==null)
            throw new SchemaGeneratorException("Interface name required");

        Interface intf = (Interface) interfaces.get(interfaceName);
        /**
         * Schema genertaion done in two stage 1. Load all the methods and
         * create type for methods parameters (if the parameters are Beans
         * then it will create Complex types for those , and if the
         * parameters are simple type which decribe in SimpleTypeTable
         * nothing will happen) 2. In the next stage for all the methods
         * messages and port types will be creteated
         */

        if (intf==null)
            throw new SchemaGeneratorException("Interface " + interfaceName + " does not exists");
        Operation[] operations = intf.getOperations();

        // since we do not support overload
        HashMap uniqueMethods = new HashMap();
        XmlSchemaComplexType methodSchemaType;
        XmlSchemaSequence sequence = null;
        List processedExs = new ArrayList();
        for (int i = 0; i < operations.length; i++) {
            Operation operation = operations[i];
            String operationName = operation.getName();
            if (excludeMethods.contains(operationName)) {
                continue;
            }

            if (uniqueMethods.get(operationName) != null) {
                throw new SchemaGeneratorException(
                        " Sorry we don't support methods overloading !!!! ");
            }

            if (operation.hasRaises()) {
                List extypes = operation.getRaises();
                for (int j = 0; j < extypes.size(); j++) {
                    ExceptionType extype = (ExceptionType) extypes.get(j);
                    if (processedExs.contains(extype.getName()))
                        continue;
                    processedExs.add(extype.getName());
                    methodSchemaType = createSchemaTypeForMethodPart(extype.getName()+ "Fault");
                    sequence = new XmlSchemaSequence();
                    generateSchemaForType(sequence, extype, extype.getName());
                    methodSchemaType.setParticle(sequence);
                }
            }
            uniqueMethods.put(operationName, operation);
            //create the schema type for the method wrapper
            List paras = operation.getParams();
            if (paras != null && paras.size() > 0) {
                sequence = new XmlSchemaSequence();
                methodSchemaType = createSchemaTypeForMethodPart(operationName);
                methodSchemaType.setParticle(sequence);
            }

            List outparas = null;
            if (paras != null) {
                for (int j = 0; j < paras.size(); j++) {
                    Member param = (Member) paras.get(j);
                    String parameterName = param.getName();
                    DataType paraType = param.getDataType();
                    if (Member.MODE_INOUT.equals(param.getMode())) {
                        if (outparas==null)
                            outparas = new ArrayList();
                        outparas.add(param);
                    } else if (Member.MODE_OUT.equals(param.getMode())) {
                        if (outparas==null)
                            outparas = new ArrayList();
                        outparas.add(param);
                        continue;
                    }
                    if(nonRpcMethods.contains(operationName)){
                        generateSchemaForType(sequence, null, operationName);
                        break;
                    } else {
                        generateSchemaForType(sequence, paraType, parameterName);
                    }
                }
            }

            DataType returnType = operation.getReturnType();

            if ((returnType != null && !CorbaUtil.getQualifiedName(returnType).equals(VOID)) || outparas!=null) {
                methodSchemaType = createSchemaTypeForMethodPart(operationName + RESPONSE);
                sequence = new XmlSchemaSequence();
                methodSchemaType.setParticle(sequence);

                if (returnType != null && !CorbaUtil.getQualifiedName(returnType).equals(VOID)) {
                    String returnName ="return";
                    if(nonRpcMethods.contains(operationName)){
                        generateSchemaForType(sequence, null, returnName);
                    } else {
                        generateSchemaForType(sequence, returnType, returnName);
                    }
                }

                if (outparas != null) {
                    for (int j = 0; j < outparas.size(); j++) {
                        Member param = (Member) outparas.get(j);
                        String parameterName = param.getName();
                        DataType paraType = param.getDataType();
                        if(nonRpcMethods.contains(operationName)){
                            generateSchemaForType(sequence, null, operationName);
                            break;
                        } else {
                            generateSchemaForType(sequence, paraType, parameterName);
                        }
                    }
                }
            }

        }
        /*} else {
        //generate the schema type for extra classes
        extraSchemaTypeName = typeTable.getSimpleSchemaTypeName(getQualifiedName(jclass));
        if (extraSchemaTypeName == null) {
            generateSchema(jclass);
        }*/
        return schemaMap.values();
    }

    /**
     * JAM convert first name of an attribute into UpperCase as an example if there is a instance
     * variable called foo in a bean , then Jam give that as Foo so this method is to correct that
     * error
     *
     * @param wrongName wrong name
     * @return the right name, using english as the locale for case conversion
     */
    public static String getCorrectName(String wrongName) {
        if (wrongName.length() > 1) {
            return wrongName.substring(0, 1).toLowerCase(Locale.ENGLISH)
                    + wrongName.substring(1, wrongName.length());
        } else {
            return wrongName.substring(0, 1).toLowerCase(Locale.ENGLISH);
        }
    }

    /**
     * Generate schema construct for given type
     *
     * @param dataType object
     * @return Qname
     * @throws SchemaGeneratorException if fails
     */
    //private QName generateSchema(JClass dataType) throws Exception {
    private QName generateSchema(CompositeDataType dataType) throws SchemaGeneratorException {
        String name = CorbaUtil.getQualifiedName(dataType);
        QName schemaTypeName = typeTable.getComplexSchemaType(name);
        if (schemaTypeName == null) {
            String simpleName =  getSimpleName(dataType);

            String packageName = getModuleName(dataType);
            String targetNameSpace = resolveSchemaNamespace(packageName);

            XmlSchema xmlSchema = getXmlSchema(targetNameSpace);
            String targetNamespacePrefix = (String) targetNamespacePrefixMap.get(targetNameSpace);
            schemaTypeName = new QName(targetNameSpace, simpleName, targetNamespacePrefix);

            if (dataType instanceof EnumType) {
                XmlSchemaSimpleType simpleType = new XmlSchemaSimpleType(xmlSchema);
                XmlSchemaSimpleTypeRestriction restriction = new XmlSchemaSimpleTypeRestriction();
                restriction.setBaseTypeName(typeTable.getSimpleSchemaTypeName("java.lang.String"));
                simpleType.setContent(restriction);
                simpleType.setName(simpleName);

                XmlSchemaObjectCollection facets = restriction.getFacets();
                EnumType enumType = (EnumType) dataType;
                List enumMembers = enumType.getEnumMembers();
                for (int i = 0; i < enumMembers.size(); i++) {
                    facets.add(new XmlSchemaEnumerationFacet(enumMembers.get(i), false));
                }

                XmlSchemaElement eltOuter = new XmlSchemaElement();
                eltOuter.setName(simpleName);
                eltOuter.setQName(schemaTypeName);

                xmlSchema.getItems().add(eltOuter);
                xmlSchema.getElements().add(schemaTypeName, eltOuter);
                eltOuter.setSchemaTypeName(simpleType.getQName());

                xmlSchema.getItems().add(simpleType);
                xmlSchema.getSchemaTypes().add(schemaTypeName, simpleType);

                // adding this type to the table
                typeTable.addComplexSchema(name, eltOuter.getQName());
            } else if (dataType instanceof UnionType) {
                XmlSchemaComplexType complexType = new XmlSchemaComplexType(xmlSchema);
                XmlSchemaChoice choice = new XmlSchemaChoice();
                XmlSchemaObjectCollection items = choice.getItems();

                UnionType unionType = (UnionType) dataType;
                Member[] members = unionType.getMembers();
                for (int i = 0; i < members.length; i++) {
                    items.add(generateSchemaforFieldsandProperties(xmlSchema, members[i].getDataType(),
                            members[i].getName(), true));
                }

                complexType.setParticle(choice);
                complexType.setName(simpleName);

                XmlSchemaElement eltOuter = new XmlSchemaElement();
                eltOuter.setName(simpleName);
                eltOuter.setQName(schemaTypeName);

                xmlSchema.getItems().add(eltOuter);
                xmlSchema.getElements().add(schemaTypeName, eltOuter);
                eltOuter.setSchemaTypeName(complexType.getQName());
                xmlSchema.getItems().add(complexType);
                xmlSchema.getSchemaTypes().add(schemaTypeName, complexType);

                typeTable.addComplexSchema(name, eltOuter.getQName());
            } else {
                XmlSchemaComplexType complexType = new XmlSchemaComplexType(xmlSchema);
                XmlSchemaSequence sequence = new XmlSchemaSequence();
                XmlSchemaElement eltOuter = new XmlSchemaElement();
                eltOuter.setName(simpleName);
                eltOuter.setQName(schemaTypeName);
                complexType.setParticle(sequence);
                complexType.setName(simpleName);

                xmlSchema.getItems().add(eltOuter);
                xmlSchema.getElements().add(schemaTypeName, eltOuter);
                eltOuter.setSchemaTypeName(complexType.getQName());

                xmlSchema.getItems().add(complexType);
                xmlSchema.getSchemaTypes().add(schemaTypeName, complexType);

                // adding this type to the table
                typeTable.addComplexSchema(name, eltOuter.getQName());
                if (dataType instanceof Typedef) {
                    Typedef typedef = (Typedef) dataType;
                    DataType aliasType = typedef.getDataType();
                    sequence.getItems().add(generateSchemaforFieldsandProperties(xmlSchema, aliasType, "item", false));
                } else {
                    //Set propertiesNames = new HashSet() ;
                    Member[] members = dataType.getMembers();
                    for (int i = 0; i < members.length; i++) {
                        Member member = members[i];
                        String propertyName = member.getName();
                        DataType propertyType = member.getDataType();
                        //String propname = getCorrectName(property.getSimpleName()) ;
                        //propertiesNames.add(propertyName) ;
                        sequence.getItems().add(generateSchemaforFieldsandProperties(xmlSchema, propertyType,
                                propertyName, false));
                    }
                }
            }
        }
        return schemaTypeName;
    }


    // moved code common to Fields & properties out of above method
    private XmlSchemaElement generateSchemaforFieldsandProperties(XmlSchema xmlSchema,
                                                                  DataType type,
                                                                  String name, boolean forceNotNillable) throws SchemaGeneratorException {
        boolean isArryType = false;
        long maxOccurs = 0;
        long minOccurs = 0;
        if (type instanceof AbstractCollectionType) {
            AbstractCollectionType collectionType = (AbstractCollectionType) type;
            type = collectionType.getDataType();
            isArryType = true;
            int elementCount = collectionType.getElementCount();
            if (collectionType.isArray()) {
                minOccurs = maxOccurs = elementCount;
            } else if (collectionType.isSequence()) {
                minOccurs = 0;
                maxOccurs = (elementCount == 0) ? Long.MAX_VALUE : elementCount;
            }
            if (type instanceof AbstractCollectionType) {
                AbstractCollectionType child = (AbstractCollectionType) type;
                Typedef typedef = new Typedef();
                typedef.setDataType(type);
                typedef.setModule(child.getElementModule());
                typedef.setName("_" + (child.getDepth() - 1) + "_" + child.getElementName());
                type = typedef;
            }
        }

        String propertyTypeName = CorbaUtil.getQualifiedName(type);

        if(isArryType&&"byte".equals(propertyTypeName)){
            propertyTypeName = "base64Binary";
        }

        XmlSchemaElement elt1 = new XmlSchemaElement();
        elt1.setName(name);

        if (isArryType && (!propertyTypeName.equals("base64Binary"))){
            elt1.setMaxOccurs(maxOccurs);
            elt1.setMinOccurs(minOccurs);
        }

        if(isNillable(type) && !forceNotNillable)
            elt1.setNillable(true) ;

        if (typeTable.isSimpleType(propertyTypeName)) {
            elt1.setSchemaTypeName(typeTable.getSimpleSchemaTypeName(propertyTypeName));

        } else if (type instanceof CompositeDataType) {
            generateSchema((CompositeDataType) type);
            elt1.setSchemaTypeName(typeTable.getComplexSchemaType(propertyTypeName));

            if (!((NamespaceMap) xmlSchema.getNamespaceContext()).values().
                    contains(typeTable.getComplexSchemaType(propertyTypeName).getNamespaceURI())) {
                XmlSchemaImport importElement = new XmlSchemaImport();
                importElement.setNamespace(
                        typeTable.getComplexSchemaType(propertyTypeName).getNamespaceURI());
                xmlSchema.getItems().add(importElement);
                ((NamespaceMap) xmlSchema.getNamespaceContext()).
                        put(generatePrefix(),
                                typeTable.getComplexSchemaType(propertyTypeName).getNamespaceURI());
            }
        } else {
            throw new SchemaGeneratorException("Unsupported type:" + type);
        }
        return elt1;
    }

    private boolean isNillable(DataType type) {
        if (type instanceof CompositeDataType) {
            return true;
        } else if (type instanceof PrimitiveDataType) {
            PrimitiveDataType primitiveDataType = (PrimitiveDataType) type;
            if (primitiveDataType.getTypeName().equals("java.lang.String"))
                return true;
        }
        return false;
    }


    private QName generateSchemaForType(XmlSchemaSequence sequence, DataType type, String partName)
            throws SchemaGeneratorException {

        boolean isArrayType = false;
        if(type!=null){
            isArrayType = (type instanceof ArrayType);
        }
        if (isArrayType) {
            ArrayType arrayType = (ArrayType) type;
            type = arrayType.getDataType();
        }
        String classTypeName;
        if(type==null){
            classTypeName = "java.lang.Object";
        } else {
            classTypeName = CorbaUtil.getQualifiedName(type);
        }
        if (isArrayType && "byte".equals(classTypeName)) {
            classTypeName = "base64Binary";
            isArrayType = false;
        }
        if("javax.activation.DataHandler".equals(classTypeName)){
            classTypeName = "base64Binary";
        }
        QName schemaTypeName = typeTable.getSimpleSchemaTypeName(classTypeName);
        if (schemaTypeName == null && type instanceof CompositeDataType) {
            schemaTypeName = generateSchema((CompositeDataType) type);
            addContentToMethodSchemaType(sequence,
                    schemaTypeName,
                    partName,
                    isArrayType);
            String schemaNamespace;
            schemaNamespace = resolveSchemaNamespace(getModuleName(type));
            addImport(getXmlSchema(schemaNamespace), schemaTypeName);

        } else {
            addContentToMethodSchemaType(sequence,
                    schemaTypeName,
                    partName,
                    isArrayType);
        }

        return schemaTypeName;
    }

    private void addContentToMethodSchemaType(XmlSchemaSequence sequence,
                                              QName schemaTypeName,
                                              String paraName,
                                              boolean isArray) {
        XmlSchemaElement elt1 = new XmlSchemaElement();
        elt1.setName(paraName);
        elt1.setSchemaTypeName(schemaTypeName);
        sequence.getItems().add(elt1);

        if (isArray) {
            elt1.setMaxOccurs(Long.MAX_VALUE);
            elt1.setMinOccurs(1);
        }
        elt1.setNillable(true);
    }

    private XmlSchemaComplexType createSchemaTypeForMethodPart(String localPartName) {
        XmlSchema xmlSchema = getXmlSchema(schemaTargetNameSpace);
        QName elementName =
                new QName(this.schemaTargetNameSpace, localPartName, this.schema_namespace_prefix);
        XmlSchemaComplexType complexType = new XmlSchemaComplexType(xmlSchema);

        XmlSchemaElement globalElement = new XmlSchemaElement();
        globalElement.setSchemaType(complexType);
        globalElement.setName(localPartName);
        globalElement.setQName(elementName);
        xmlSchema.getItems().add(globalElement);
        xmlSchema.getElements().add(elementName, globalElement);

        typeTable.addComplexSchema(localPartName, elementName);

        return complexType;
    }

    private XmlSchema getXmlSchema(String targetNamespace) {
        XmlSchema xmlSchema;

        if ((xmlSchema = (XmlSchema) schemaMap.get(targetNamespace)) == null) {
            String targetNamespacePrefix;

            if ( targetNamespace.equals(schemaTargetNameSpace) &&
                    schema_namespace_prefix != null ) {
                targetNamespacePrefix = schema_namespace_prefix;
            } else {
                targetNamespacePrefix = generatePrefix();
            }


            xmlSchema = new XmlSchema(targetNamespace, xmlSchemaCollection);
            xmlSchema.setAttributeFormDefault(getAttrFormDefaultSetting());
            xmlSchema.setElementFormDefault(getElementFormDefaultSetting());


            targetNamespacePrefixMap.put(targetNamespace, targetNamespacePrefix);
            schemaMap.put(targetNamespace, xmlSchema);

            NamespaceMap prefixmap = new NamespaceMap();
            prefixmap.put(DEFAULT_SCHEMA_NAMESPACE_PREFIX, URI_2001_SCHEMA_XSD);
            prefixmap.put(targetNamespacePrefix, targetNamespace);
            xmlSchema.setNamespaceContext(prefixmap);
        }
        return xmlSchema;
    }

    public TypeTable getTypeTable() {
        return typeTable;
    }

    private String generatePrefix() {
        return NAME_SPACE_PREFIX + prefixCount++;
    }

    public void setExcludeMethods(ArrayList excludeMethods) {
        if (excludeMethods == null) excludeMethods = new ArrayList();
        this.excludeMethods = excludeMethods;
    }

    public String getSchemaTargetNameSpace() {
        return schemaTargetNameSpace;
    }

    private void addImport(XmlSchema xmlSchema, QName schemaTypeName) {
        if (!((NamespaceMap) xmlSchema.getNamespaceContext()).values().
                contains(schemaTypeName.getNamespaceURI())) {
            XmlSchemaImport importElement = new XmlSchemaImport();
            importElement.setNamespace(schemaTypeName.getNamespaceURI());
            xmlSchema.getItems().add(importElement);
            ((NamespaceMap) xmlSchema.getNamespaceContext()).
                    put(generatePrefix(), schemaTypeName.getNamespaceURI());
        }
    }

    public String getAttrFormDefault() {
        return attrFormDefault;
    }

    public void setAttrFormDefault(String attrFormDefault) {
        this.attrFormDefault = attrFormDefault;
    }

    public String getElementFormDefault() {
        return elementFormDefault;
    }

    public void setElementFormDefault(String elementFormDefault) {
        this.elementFormDefault = elementFormDefault;
    }

    private XmlSchemaForm getAttrFormDefaultSetting() {
        if (FORM_DEFAULT_UNQUALIFIED.equals(getAttrFormDefault())) {
            return new XmlSchemaForm(XmlSchemaForm.UNQUALIFIED);
        } else {
            return new XmlSchemaForm(XmlSchemaForm.QUALIFIED);
        }
    }

    private XmlSchemaForm getElementFormDefaultSetting() {
        if (FORM_DEFAULT_UNQUALIFIED.equals(getElementFormDefault())) {
            return new XmlSchemaForm(XmlSchemaForm.UNQUALIFIED);
        } else {
            return new XmlSchemaForm(XmlSchemaForm.QUALIFIED);
        }
    }

    public ArrayList getExtraClasses() {
        if (extraClasses == null) {
            extraClasses = new ArrayList();
        }
        return extraClasses;
    }

    public void setExtraClasses(ArrayList extraClasses) {
        this.extraClasses = extraClasses;
    }

    private String resolveSchemaNamespace(String packageName) throws SchemaGeneratorException {
        if (useWSDLTypesNamespace) {
            return (String) pkg2nsmap.get("all");
        } else {
            if (pkg2nsmap != null && !pkg2nsmap.isEmpty()) {
                //if types should go into namespaces that are mapped against the package name for the type
                if (pkg2nsmap.get(packageName) != null) {
                    //return that mapping
                    return (String) pkg2nsmap.get(packageName);
                } else {
                    return getNsGen().schemaNamespaceFromPackageName(packageName).toString();
                }
            } else {
                // if  pkg2nsmap is null and if not default schema ns found for the custom bean
                return getNsGen().schemaNamespaceFromPackageName(packageName).toString();
            }
        }
    }

    public boolean isUseWSDLTypesNamespace() {
        return useWSDLTypesNamespace;
    }

    public void setUseWSDLTypesNamespace(boolean useWSDLTypesNamespace) {
        this.useWSDLTypesNamespace = useWSDLTypesNamespace;
    }

    public Map getPkg2nsmap() {
        return pkg2nsmap;
    }

    public void setPkg2nsmap(Map pkg2nsmap) {
        this.pkg2nsmap = pkg2nsmap;
    }

    public String getTargetNamespace() {
        return targetNamespace;
    }

    protected String getSimpleName(DataType type){
        if (type instanceof CompositeDataType) {
            CompositeDataType compositeType = (CompositeDataType) type;
            return compositeType.getName();
        } else {
            return CorbaUtil.getQualifiedName(type);
        }
    }

    public void setNonRpcMethods(ArrayList nonRpcMethods) {
        if(nonRpcMethods!=null){
            this.nonRpcMethods = nonRpcMethods;
        }
    }

    private String getModuleName(DataType type) {
        if (type instanceof CompositeDataType) {
            CompositeDataType compositeType = (CompositeDataType) type;
            String module = compositeType.getModule();
            module = module.replaceAll("::", ".");
            if (module.endsWith(".")) {
                module = module.substring(0, module.length() - 1);
            }
            return module;
        } else {
            return "";
        }
    }
}
