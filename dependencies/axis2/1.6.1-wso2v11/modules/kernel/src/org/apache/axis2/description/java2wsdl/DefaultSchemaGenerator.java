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

package org.apache.axis2.description.java2wsdl;

import org.apache.axis2.AxisFault;
import org.apache.axis2.jaxrs.JAXRSUtils;
import org.apache.axis2.jaxrs.JAXRSModel;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.deployment.util.BeanExcludeInfo;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.*;
import org.apache.axis2.description.java2wsdl.bytecode.MethodTable;
import org.apache.axis2.jsr181.JSR181Helper;
import org.apache.axis2.jsr181.WebMethodAnnotation;
import org.apache.axis2.jsr181.WebParamAnnotation;
import org.apache.axis2.jsr181.WebResultAnnotation;
import org.apache.axis2.jsr181.WebServiceAnnotation;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.apache.ws.commons.schema.utils.NamespacePrefixList;
import org.w3c.dom.Document;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.beans.PropertyDescriptor;
import java.beans.BeanInfo;
import java.beans.Introspector;

public class DefaultSchemaGenerator implements Java2WSDLConstants, SchemaGenerator {

    private static final Log log = LogFactory.getLog(DefaultSchemaGenerator.class);

    public static final String NAME_SPACE_PREFIX = "ax2";// axis2 name space

    private static int prefixCount = 1;

    private JAXRSModel classModel;

    protected Map<String,String> targetNamespacePrefixMap = new Hashtable<String,String>();

    protected Map<String,XmlSchema> schemaMap = new Hashtable<String,XmlSchema>();

    protected XmlSchemaCollection xmlSchemaCollection = new XmlSchemaCollection();


    protected ClassLoader classLoader;

    protected String className;

    protected TypeTable typeTable = new TypeTable();

    // to keep loadded method using JAM
    protected Method methods[];

    //to store byte code method using Axis 1.x codes
    protected MethodTable methodTable;

    protected String schemaTargetNameSpace;

    protected String schema_namespace_prefix;

    protected String attrFormDefault = null;

    protected String elementFormDefault = null;

    protected ArrayList<String> excludeMethods = new ArrayList<String>();

    protected ArrayList<String> extraClasses = null;

    protected boolean useWSDLTypesNamespace = false;

    protected Map<String,String> pkg2nsmap = null;

    protected NamespaceGenerator nsGen = null;

    protected String targetNamespace = null;
    //to keep the list of operations which use other MR not RPC MR
    protected ArrayList<String> nonRpcMethods = new ArrayList<String>();

    protected Class<?> serviceClass = null;
    protected AxisService service;
    // location of the custom schema , if any
    protected String customSchemaLocation;
    // location of the class name to package mapping file
    // File is simple file with qualifiedClassName:SchemaQName
    protected String mappingFileLocation;

    //To check whether we need to generate Schema element for Exception
    protected boolean generateBaseException;

    protected boolean sortAttributes = true;

    protected boolean isGenerateWrappedArrayTypes = false;

    public NamespaceGenerator getNsGen() throws Exception {
        if (nsGen == null) {
            nsGen = new DefaultNamespaceGenerator();
        }
        return nsGen;
    }

    public void setNsGen(NamespaceGenerator nsGen) {
        this.nsGen = nsGen;
    }

    public DefaultSchemaGenerator(ClassLoader loader, String className,
                                  String schematargetNamespace,
                                  String schematargetNamespacePrefix,
                                  AxisService service)
            throws Exception {
        this.classLoader = loader;
        this.className = className;
        this.service = service;

        serviceClass = Class.forName(className, true, loader);
        methodTable = new MethodTable(serviceClass);

        this.targetNamespace = Java2WSDLUtils.targetNamespaceFromClassName(
                className, loader, getNsGen()).toString();

        if (schematargetNamespace != null
                && schematargetNamespace.trim().length() != 0) {
            this.schemaTargetNameSpace = schematargetNamespace;
        } else {
            this.schemaTargetNameSpace =
                    Java2WSDLUtils.schemaNamespaceFromClassName(className, loader, getNsGen())
                            .toString();
        }

        if (schematargetNamespacePrefix != null
                && schematargetNamespacePrefix.trim().length() != 0) {
            this.schema_namespace_prefix = schematargetNamespacePrefix;
        } else {
            this.schema_namespace_prefix = SCHEMA_NAMESPACE_PRFIX;
        }
        if (service !=null ) {
            Parameter sortAtt = service.getParameter("SortAttributes");
            if (sortAtt !=null && "false".equals(sortAtt.getValue())){
                sortAttributes = false;
            }

            Parameter generateWrappedArrayTypes = service.getParameter("generateWrappedArrayTypes");
            if ((generateWrappedArrayTypes != null) && JavaUtils.isTrue(generateWrappedArrayTypes.getValue())){
               isGenerateWrappedArrayTypes = true;
            }

            Parameter extraClassesParam = service.getParameter("extraClass");
            if (extraClassesParam != null){
                String extraClassesString = (String) extraClassesParam.getValue();
                String[] extraClassesArray = extraClassesString.split(",");
                if (this.extraClasses == null){
                    this.extraClasses = new ArrayList<String>();
                }

                for (String extraClass : extraClassesArray){
                    this.extraClasses.add(extraClass);
                }
            }
        }




    }

    //This will locad the custom schema file and add that into the schema map
    private void loadCustomSchemaFile() {
        if (customSchemaLocation != null) {
            try {
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                documentBuilderFactory.setNamespaceAware(true);
                Document doc = documentBuilderFactory.newDocumentBuilder().parse(new File(customSchemaLocation));
                XmlSchema schema = xmlSchemaCollection.read(doc, null);
                schemaMap.put(schema.getTargetNamespace(), schema);
            } catch (Exception e) {
                log.info(e.getMessage());
            }
        }
    }

    /**
     * This will load the mapping file and update the Typetable with the Class name and the Qname
     * Mapping file look like
     * org.foo.bar.FooException|http://www.abc.com/soaframework/common/types|ErrorMessage
     */
    private void loadMappingFile() {
        if (mappingFileLocation != null) {
            File file = new File(mappingFileLocation);
            BufferedReader input = null;
            try {
                input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                String line;
                while ((line = input.readLine()) != null) {
                    line = line.trim();
                    if (line.length() > 0 && line.charAt(0) != '#') {
                        String values[] = line.split("\\|");
                        if (values != null && values.length > 2) {
                            typeTable.addComplexSchema(values[0], new QName(values[1], values[2]));
                        }
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    if (input != null) {
                        input.close();
                    }
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }


    /**
     * Generates schema for all the parameters in method. First generates schema for all different
     * parameter type and later refers to them.
     *
     * @return Returns XmlSchema.
     * @throws Exception
     */
    public Collection<XmlSchema> generateSchema() throws Exception {
        loadCustomSchemaFile();
        loadMappingFile();
        //all most all the time the ittr will have only one class in it
        /**
         * Schema genertaion done in two stage 1. Load all the methods and
         * create type for methods parameters (if the parameters are Bean
         * then it will create Complex types foer those , and if the
         * parameters are simple type which decribe in SimpleTypeTable
         * nothing will happen) 2. In the next stage for all the methods
         * messages and port types will be creteated
         */
        WebServiceAnnotation webservice =
                JSR181Helper.INSTANCE.getWebServiceAnnotation(serviceClass);
        if (webservice != null) {
            String tns = webservice.getTargetNamespace();
            if (tns != null && !"".equals(tns)) {
                targetNamespace = tns;
                schemaTargetNameSpace = tns;
            }
            service.setName(Utils.getAnnotatedServiceName(serviceClass, webservice));
        }
        classModel= JAXRSUtils.getClassModel(serviceClass);
        methods = processMethods(serviceClass.getDeclaredMethods());
        
        for (String extraClassName : getExtraClasses()) {
            Class<?> extraClass = Class.forName(extraClassName, true, classLoader);
            if (typeTable.getSimpleSchemaTypeName(extraClassName) == null) {
                generateSchema(extraClass);
            }
        }
        
        return schemaMap.values();
    }

    protected Method[] processMethods(Method[] declaredMethods) throws Exception {
        ArrayList<Method> list = new ArrayList<Method>();
        //short the elements in the array
        Arrays.sort(declaredMethods, new MathodComparator());


        // since we do not support overload
        HashMap<String,Method> uniqueMethods = new LinkedHashMap<String,Method>();
        XmlSchemaComplexType methodSchemaType;
        XmlSchemaSequence sequence = null;

        for (Method jMethod : declaredMethods) {
            if (jMethod.isBridge()) {
                continue;
            }

            WebMethodAnnotation methodAnnon = JSR181Helper.INSTANCE.getWebMethodAnnotation(jMethod);
            String methodName = jMethod.getName();
            if (methodAnnon != null) {
                if (methodAnnon.isExclude()) {
                    continue;
                }
                if (methodAnnon.getOperationName() != null){
                    methodName = methodAnnon.getOperationName();
                }
            }

            // no need to think abt this method , since that is system
            // config method
            if (excludeMethods.contains(methodName)) {
                continue;
            }

            if (!Modifier.isPublic(jMethod.getModifiers())) {
                // no need to generate Schema for non public methods
                continue;
            }

            if (uniqueMethods.get(methodName) != null) {
                log.warn("We don't support method overloading. Ignoring [" +
                        methodName + "]");
                continue;
            }
            boolean addToService = false;
            AxisOperation axisOperation = service.getOperation(new QName(methodName));
            if (axisOperation == null) {
                axisOperation = Utils.getAxisOperationForJmethod(jMethod);
//                if (WSDL2Constants.MEP_URI_ROBUST_IN_ONLY.equals(
//                        axisOperation.getMessageExchangePattern())) {
//                    AxisMessage outMessage = axisOperation.getMessage(
//                            WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
//                    if (outMessage != null) {
//                        outMessage.setName(methodName + RESPONSE);
//                    }
//                }
                addToService = true;
            }
// by now axis operation should be assigned but we better recheck & add the paramether
            if(axisOperation != null){
             axisOperation.addParameter("JAXRSAnnotaion", JAXRSUtils.getMethodModel(this.classModel,jMethod));
            }
            // Maintain a list of methods we actually work with
            list.add(jMethod);

            processException(jMethod, axisOperation);
            uniqueMethods.put(methodName, jMethod);
            Class<?>[] parameters = jMethod.getParameterTypes();
            String parameterNames[] = null;
            AxisMessage inMessage = axisOperation.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            if (inMessage != null) {
                inMessage.setName(methodName + Java2WSDLConstants.MESSAGE_SUFFIX);
            }
            if (parameters.length > 0) {
                parameterNames = methodTable.getParameterNames(methodName);
                // put the parameter names to use it for parsing
                service.addParameter(methodName, parameterNames);
            }

            // we need to add the method opration wrapper part even to
            // empty parameter operations 
            sequence = new XmlSchemaSequence();

            String requestElementSuffix = getRequestElementSuffix();
            String requestLocalPart = methodName;
            if (requestElementSuffix != null) {
                requestLocalPart += requestElementSuffix;
            }

            methodSchemaType = createSchemaTypeForMethodPart(requestLocalPart);
            methodSchemaType.setParticle(sequence);
            inMessage.setElementQName(typeTable.getQNamefortheType(requestLocalPart));

            Parameter param = service.getParameter(Java2WSDLConstants.MESSAGE_PART_NAME_OPTION_LONG);
            if (param != null) {
                inMessage.setPartName((String) param.getValue());
            }

            service.addMessageElementQNameToOperationMapping(methodSchemaType.getQName(),
                    axisOperation);


            Annotation[][] parameterAnnotation = jMethod.getParameterAnnotations();

            Type[] genericParameterTypes = jMethod.getGenericParameterTypes();
            for (int j = 0; j < parameters.length; j++) {
                Class<?> methodParameter = parameters[j];
                String parameterName = getParameterName(parameterAnnotation, j, parameterNames);
                if (nonRpcMethods.contains(jMethod.getName())) {
                    generateSchemaForType(sequence, null, jMethod.getName());
                    break;
                } else {
                    Type genericParameterType = genericParameterTypes[j];
                    Type genericType = null;
                    if(genericParameterType instanceof ParameterizedType){
                        ParameterizedType aType = (ParameterizedType) genericParameterType;
                        Type[] parameterArgTypes = aType.getActualTypeArguments();
                        genericType = parameterArgTypes[0];
                        generateSchemaForType(sequence, genericType, parameterName, true);
                    }else{
                        generateSchemaForType(sequence, methodParameter, parameterName);
                    }
                }
            }
            // for its return type
            Class<?> returnType = jMethod.getReturnType();
            if (!"void".equals(jMethod.getReturnType().getName())) {
                String partQname = methodName + RESPONSE;
                methodSchemaType =
                        createSchemaTypeForMethodPart(partQname);
                sequence = new XmlSchemaSequence();
                methodSchemaType.setParticle(sequence);
                WebResultAnnotation returnAnnon = JSR181Helper.INSTANCE.getWebResultAnnotation(jMethod);
                String returnName = "return";
                if (returnAnnon != null) {
                    returnName = returnAnnon.getName();
                    if (returnName == null || "".equals(returnName)) {
                        returnName = "return";
                    }
                }
                Type genericParameterType = jMethod.getGenericReturnType();
                if (nonRpcMethods.contains(jMethod.getName())) {
                    generateSchemaForType(sequence, null, returnName);
                } else if(genericParameterType instanceof ParameterizedType){
                        ParameterizedType aType = (ParameterizedType) genericParameterType;
                        Type[] parameterArgTypes = aType.getActualTypeArguments();
                        generateSchemaForType(sequence, parameterArgTypes[0], returnName, true);
                    } else {
                    generateSchemaForType(sequence, returnType, returnName);
                }

                AxisMessage outMessage = axisOperation.getMessage(
                        WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                outMessage.setElementQName(typeTable.getQNamefortheType(partQname));
                outMessage.setName(partQname);

                Parameter outparam = service.getParameter(Java2WSDLConstants.MESSAGE_PART_NAME_OPTION_LONG);
                if (outparam != null) {
                    outMessage.setPartName((String) outparam.getValue());
                }

                service.addMessageElementQNameToOperationMapping(methodSchemaType.getQName(),
                        axisOperation);
            }
            if (addToService) {
                service.addOperation(axisOperation);
            }
        }
        return list.toArray(new Method[list.size()]);
    }

    /**
     * This method will generate Schema element for all the excetion types in a given JMethod
     * - No matter what it will generate Schema element for java.lang.Exception so that for other
     * exception which extend java.lang.Excetion can use as the base class type
     */
    protected void processException(Method jMethod,
                                    AxisOperation axisOperation) throws Exception {
        XmlSchemaComplexType methodSchemaType;
        XmlSchemaSequence sequence;
        if (jMethod.getExceptionTypes().length > 0) {
            for (Class<?> extype : jMethod.getExceptionTypes()) {
                if (AxisFault.class.getName().equals(extype.getName())) {
                    continue;
                }
                String partQname = this.service.getName() + getSimpleClassName(extype);
                methodSchemaType = createSchemaTypeForFault(partQname);
                QName elementName =
                        new QName(this.schemaTargetNameSpace, partQname, this.schema_namespace_prefix);
                sequence = new XmlSchemaSequence();
                if (Exception.class.getName().equals(extype.getName())) {
                    if (typeTable.getComplexSchemaType(Exception.class.getName()) == null) {
                        generateComplexTypeforException();
                    }
                    QName schemaTypeName = typeTable.getComplexSchemaType(Exception.class.getName());
                    addContentToMethodSchemaType(sequence,
                            schemaTypeName,
                            partQname,
                            false);
                    methodSchemaType.setParticle(sequence);
                    typeTable.addComplexSchema(Exception.class.getPackage().getName(),
                            methodSchemaType.getQName());
                    resolveSchemaNamespace(Exception.class.getPackage().getName());
                    addImport(getXmlSchema(schemaTargetNameSpace), schemaTypeName);
                } else {
                    generateSchemaForType(sequence, extype, getSimpleClassName(extype));
                    methodSchemaType.setParticle(sequence);
                }

                typeTable.addComplexSchema(partQname, elementName);

                if (AxisFault.class.getName().equals(extype.getName())) {
                    continue;
                }
                AxisMessage faultMessage = new AxisMessage();
                faultMessage.setName(this.service.getName() + getSimpleClassName(extype));
                faultMessage.setElementQName(typeTable.getQNamefortheType(partQname));

                Parameter param = service.getParameter(Java2WSDLConstants.MESSAGE_PART_NAME_OPTION_LONG);
                if (param != null) {
                    faultMessage.setPartName((String) param.getValue());
                }

                axisOperation.setFaultMessages(faultMessage);
            }
        }
    }

    private void generateComplexTypeforException() {
        XmlSchemaSequence sequence = new XmlSchemaSequence();
        XmlSchema xmlSchema = getXmlSchema(schemaTargetNameSpace);
        QName elementName = new QName(schemaTargetNameSpace, "Exception", schema_namespace_prefix);
        XmlSchemaComplexType complexType = new XmlSchemaComplexType(xmlSchema);
        complexType.setName("Exception");
        xmlSchema.getItems().add(complexType);
        xmlSchema.getElements().add(elementName, complexType);
        typeTable.addComplexSchema(Exception.class.getName(), elementName);
        QName schemaTypeName = new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "string");
        addContentToMethodSchemaType(sequence, schemaTypeName, "Message", false);
        complexType.setParticle(sequence);
    }

    /**
     * Generate schema construct for given type
     *
     * @param javaType : Class to whcih need to generate Schema
     * @return : Generated QName
     */
    protected QName generateSchema(Class<?> javaType) throws Exception {
        String name = getClassName(javaType);
        QName schemaTypeName = typeTable.getComplexSchemaType(name);
        if (schemaTypeName == null) {
            String simpleName = getSimpleClassName(javaType);

            String packageName = getQualifiedName(javaType.getPackage());
            String targetNameSpace = resolveSchemaNamespace(packageName);

            XmlSchema xmlSchema = getXmlSchema(targetNameSpace);
            String targetNamespacePrefix = targetNamespacePrefixMap.get(targetNameSpace);
            if (targetNamespacePrefix == null) {
                targetNamespacePrefix = generatePrefix();
                targetNamespacePrefixMap.put(targetNameSpace, targetNamespacePrefix);
            }

            XmlSchemaComplexType complexType = new XmlSchemaComplexType(xmlSchema);
            XmlSchemaSequence sequence = new XmlSchemaSequence();
            XmlSchemaComplexContentExtension complexExtension =
                    new XmlSchemaComplexContentExtension();

            XmlSchemaElement eltOuter = new XmlSchemaElement();
            schemaTypeName = new QName(targetNameSpace, simpleName, targetNamespacePrefix);
            eltOuter.setName(simpleName);
            eltOuter.setQName(schemaTypeName);

            Class<?> sup = javaType.getSuperclass();
            if ((sup != null)
                    && (!"java.lang.Object".equals(sup.getName()))
                    && (!"java.lang.Exception".equals(sup.getName())) 
                    && !getQualifiedName(sup.getPackage()).startsWith("org.apache.axis2")
                    && !getQualifiedName(sup.getPackage()).startsWith("java.util")){
                String superClassName = sup.getName();
                String superclassname = getSimpleClassName(sup);
                String tgtNamespace;
                String tgtNamespacepfx;
                QName qName = typeTable.getSimpleSchemaTypeName(superClassName);
                if (qName != null) {
                    tgtNamespace = qName.getNamespaceURI();
                    tgtNamespacepfx = qName.getPrefix();
                } else {
                    tgtNamespace = resolveSchemaNamespace(getQualifiedName(sup.getPackage()));
                    tgtNamespacepfx = targetNamespacePrefixMap.get(tgtNamespace);
                    QName superClassQname = generateSchema(sup);
                    if (superClassQname != null) {
                        tgtNamespacepfx = superClassQname.getPrefix();
                        tgtNamespace = superClassQname.getNamespaceURI();
                    }
                }
                if (tgtNamespacepfx == null) {
                    tgtNamespacepfx = generatePrefix();
                    targetNamespacePrefixMap.put(tgtNamespace, tgtNamespacepfx);
                }
                //if the parent class package name is differ from the child
                if (!((NamespaceMap) xmlSchema.getNamespaceContext()).values().
                        contains(tgtNamespace)) {
                    XmlSchemaImport importElement = new XmlSchemaImport();
                    importElement.setNamespace(tgtNamespace);
                    xmlSchema.getItems().add(importElement);
                    ((NamespaceMap) xmlSchema.getNamespaceContext()).
                            put(generatePrefix(), tgtNamespace);
                }

                QName basetype = new QName(tgtNamespace, superclassname, tgtNamespacepfx);
                complexExtension.setBaseTypeName(basetype);
                complexExtension.setParticle(sequence);
                XmlSchemaComplexContent contentModel = new XmlSchemaComplexContent();
                contentModel.setContent(complexExtension);
                complexType.setContentModel(contentModel);

            } else {
                complexType.setParticle(sequence);
            }

            complexType.setName(simpleName);

            if (Modifier.isAbstract(javaType.getModifiers())) {
                complexType.setAbstract(true);
            }

//            xmlSchema.getItems().add(eltOuter);
            xmlSchema.getElements().add(schemaTypeName, eltOuter);
            eltOuter.setSchemaTypeName(complexType.getQName());

            xmlSchema.getItems().add(complexType);
            xmlSchema.getSchemaTypes().add(schemaTypeName, complexType);

            // adding this type to the table
            typeTable.addComplexSchema(name, eltOuter.getQName());
            // adding this type's package to the table, to support inheritance.
            typeTable.addComplexSchema(getQualifiedName(javaType.getPackage()), eltOuter.getQName());

            typeTable.addClassNameForQName(eltOuter.getQName(), name);

            BeanExcludeInfo beanExcludeInfo = null;
            if (service.getExcludeInfo() != null) {
                beanExcludeInfo = service.getExcludeInfo().getBeanExcludeInfoForClass(getClassName(javaType));
            }

            // we need to get properties only for this bean. hence ignore the super
            // class properties
            BeanInfo beanInfo = Introspector.getBeanInfo(javaType, javaType.getSuperclass());

            for (PropertyDescriptor property : beanInfo.getPropertyDescriptors()) {
                String propertyName = property.getName();
                if (!property.getName().equals("class") && (property.getPropertyType() != null)) {
                    if ((beanExcludeInfo == null) || !beanExcludeInfo.isExcludedProperty(propertyName)) {
                        Type genericFieldType = null;
                        try {
                            Field field = javaType.getDeclaredField(propertyName);
                            genericFieldType = field.getGenericType();
                        } catch (Exception e) {
                            //log.info(e.getMessage());
                        }

                        if(genericFieldType instanceof ParameterizedType){
                            ParameterizedType aType = (ParameterizedType) genericFieldType;
                            Type[] fieldArgTypes = aType.getActualTypeArguments();
                            try {
                                generateSchemaforGenericFields(xmlSchema,
                                    sequence,
                                    fieldArgTypes[0],
                                    propertyName);
                            } catch (Exception e) {
                                generateSchemaforFieldsandProperties(xmlSchema,
                                    sequence,
                                    property.getPropertyType(),
                                    propertyName,
                                    property.getPropertyType().isArray());
                            }
                        } else {
                            generateSchemaforFieldsandProperties(xmlSchema,
                                    sequence,
                                    property.getPropertyType(),
                                    propertyName,
                                    property.getPropertyType().isArray());
                        }
                    }
                }
            }
        }
        return schemaTypeName;
    }


    // moved code common to Fields & properties out of above method
    protected void generateSchemaforFieldsandProperties(XmlSchema xmlSchema,
                                                        XmlSchemaSequence sequence, Class<?> type,
                                                        String name, boolean isArrayType)
            throws Exception {
        String propertyName;
        if (isArrayType) {
            propertyName = getClassName(type.getComponentType());
            if (type.getComponentType().isArray()) {
                // this is a double array element
                Class<?> simpleType = type.getComponentType();
                String simpleTypeName = "";
                while (simpleType.isArray()) {
                    simpleTypeName += "ArrayOf";
                    simpleType = simpleType.getComponentType();
                }
                simpleTypeName += getSimpleClassName(simpleType);

                if (xmlSchema.getTypeByName(simpleTypeName) == null) {
                    XmlSchemaComplexType xmlSchemaComplexType = new XmlSchemaComplexType(xmlSchema);
                    XmlSchemaSequence xmlSchemaSequence = new XmlSchemaSequence();
                    xmlSchemaComplexType.setParticle(xmlSchemaSequence);
                    generateSchemaforFieldsandProperties(xmlSchema,
                            xmlSchemaSequence, type.getComponentType(), "array", true);

                    xmlSchemaComplexType.setName(simpleTypeName);
                    xmlSchema.getItems().add(xmlSchemaComplexType);
                    xmlSchema.getSchemaTypes().add(
                            new QName(xmlSchema.getTargetNamespace(), simpleTypeName), xmlSchemaComplexType);
                }

                if (isGenerateWrappedArrayTypes) {
                    XmlSchemaElement xmlSchemaElement = new XmlSchemaElement();
                    xmlSchemaElement.setName( name + "Wrapper");
                    xmlSchemaElement.setNillable(true);
                    sequence.getItems().add(xmlSchemaElement);

                    String complexTypeName = simpleTypeName + "Wrapper";

                    XmlSchemaComplexType xmlSchemaComplexType = new XmlSchemaComplexType(xmlSchema);
                    XmlSchemaSequence xmlSchemaSequence = new XmlSchemaSequence();
                    xmlSchemaComplexType.setParticle(xmlSchemaSequence);
                    xmlSchemaComplexType.setName(complexTypeName);

                    xmlSchema.getItems().add(xmlSchemaComplexType);
                    xmlSchema.getSchemaTypes().add(
                            new QName(schemaTargetNameSpace, xmlSchemaComplexType.getName()),
                            xmlSchemaComplexType);
                    addContentToMethodSchemaType(xmlSchemaSequence,
                            new QName(xmlSchema.getTargetNamespace(), simpleTypeName),
                            "array",
                            true);

                    xmlSchemaElement.setSchemaType(xmlSchemaComplexType);
                    xmlSchemaElement.setSchemaTypeName(new QName(schemaTargetNameSpace,
                            xmlSchemaComplexType.getName()));

                } else {
                    addContentToMethodSchemaType(sequence,
                            new QName(xmlSchema.getTargetNamespace(), simpleTypeName),
                            name,
                            true);

                }
                return;
            }
        } else {
            propertyName = getClassName(type);
        }
        if (isArrayType && "byte".equals(propertyName)) {
            propertyName = "base64Binary";
        }
        if (isDataHandler(type)) {
            propertyName = "base64Binary";
        }
        if (typeTable.isSimpleType(propertyName)) {

            if (isGenerateWrappedArrayTypes && isArrayType) {

                processGenerateWrappedArrayTypes(xmlSchema, sequence, type, name, isArrayType, propertyName);


            } else {
                addElementToSequence(name,
                        typeTable.getSimpleSchemaTypeName(propertyName),
                        sequence,
                        propertyName.equals("base64Binary"),
                        isArrayType,
                        type.isPrimitive());
            }

        } else {
            if (isArrayType) {
                generateSchema(type.getComponentType());
            } else {
                generateSchema(type);
            }

            if (isGenerateWrappedArrayTypes && isArrayType) {

                processGenerateWrappedArrayTypes(xmlSchema, sequence, type, name, isArrayType, propertyName);


            } else {
                addElementToSequence(name,
                        typeTable.getComplexSchemaType(propertyName),
                        sequence,
                        false,
                        isArrayType,
                        type.isPrimitive());
            }

            if (typeTable.getComplexSchemaType(propertyName) != null && !((NamespaceMap) xmlSchema.getNamespaceContext()).values().
                    contains(typeTable.getComplexSchemaType(propertyName).getNamespaceURI())) {
                XmlSchemaImport importElement = new XmlSchemaImport();
                importElement.setNamespace(
                        typeTable.getComplexSchemaType(propertyName).getNamespaceURI());
                xmlSchema.getItems().add(importElement);
                ((NamespaceMap) xmlSchema.getNamespaceContext()).
                        put(generatePrefix(),
                                typeTable.getComplexSchemaType(propertyName).getNamespaceURI());
            }
        }


    }

    protected void generateSchemaforGenericFields(XmlSchema xmlSchema,
                                                  XmlSchemaSequence sequence, Type genericType,
                                                  String name)
            throws Exception {
        String propertyName;
        Class<?> type;
        boolean isArrayType = true;
        if (genericType instanceof GenericArrayType) {
            Class<?> simpleType = (Class<?>) ((GenericArrayType) genericType).getGenericComponentType();
            propertyName = simpleType.getName();
            // this is a doble array element
            String simpleTypeName = "";
            while (simpleType.isArray()) {
                simpleTypeName += "ArrayOf";
                simpleType = simpleType.getComponentType();
            }
            simpleTypeName += simpleType.getSimpleName();

            if (xmlSchema.getTypeByName(simpleTypeName) == null) {
                XmlSchemaComplexType xmlSchemaComplexType = new XmlSchemaComplexType(xmlSchema);
                XmlSchemaSequence xmlSchemaSequence = new XmlSchemaSequence();
                xmlSchemaComplexType.setParticle(xmlSchemaSequence);
                generateSchemaforGenericFields(xmlSchema,
                        xmlSchemaSequence, simpleType, "array");

                xmlSchemaComplexType.setName(simpleTypeName);
                xmlSchema.getItems().add(xmlSchemaComplexType);
                xmlSchema.getSchemaTypes().add(
                        new QName(xmlSchema.getTargetNamespace(), simpleTypeName), xmlSchemaComplexType);
            }

            if (isGenerateWrappedArrayTypes) {
                XmlSchemaElement xmlSchemaElement = new XmlSchemaElement();
                xmlSchemaElement.setName( name + "Wrapper");
                xmlSchemaElement.setNillable(true);
                sequence.getItems().add(xmlSchemaElement);

                String complexTypeName = simpleTypeName + "Wrapper";

                XmlSchemaComplexType xmlSchemaComplexType = new XmlSchemaComplexType(xmlSchema);
                XmlSchemaSequence xmlSchemaSequence = new XmlSchemaSequence();
                xmlSchemaComplexType.setParticle(xmlSchemaSequence);
                xmlSchemaComplexType.setName(complexTypeName);

                xmlSchema.getItems().add(xmlSchemaComplexType);
                xmlSchema.getSchemaTypes().add(
                        new QName(schemaTargetNameSpace, xmlSchemaComplexType.getName()),
                        xmlSchemaComplexType);
                addContentToMethodSchemaType(xmlSchemaSequence,
                        new QName(xmlSchema.getTargetNamespace(), simpleTypeName),
                        "array",
                        true);

                xmlSchemaElement.setSchemaType(xmlSchemaComplexType);
                xmlSchemaElement.setSchemaTypeName(new QName(schemaTargetNameSpace,
                        xmlSchemaComplexType.getName()));

            } else {
                addContentToMethodSchemaType(sequence,
                        new QName(xmlSchema.getTargetNamespace(), simpleTypeName),
                        name,
                        true);

            }
            return;
        } else {
//            isArrayType = false;
            type = (Class<?>) genericType;
            propertyName = type.getName();
        }

        if (isArrayType && "byte".equals(propertyName)) {
            propertyName = "base64Binary";
        }
        if (isDataHandler(type)) {
            propertyName = "base64Binary";
        }
        if (typeTable.isSimpleType(propertyName)) {

            if (isGenerateWrappedArrayTypes && isArrayType) {

                processGenerateWrappedArrayTypes(xmlSchema, sequence, type, name, isArrayType, propertyName);


            } else {
                addElementToSequence(name,
                        typeTable.getSimpleSchemaTypeName(propertyName),
                        sequence,
                        propertyName.equals("base64Binary"),
                        isArrayType,
                        type.isPrimitive());
            }

        } else {
            if (isArrayType) {
                generateSchema(type.getComponentType());
            } else {
                generateSchema(type);
            }

            if (isGenerateWrappedArrayTypes && isArrayType) {

                processGenerateWrappedArrayTypes(xmlSchema, sequence, type, name, isArrayType, propertyName);


            } else {
                addElementToSequence(name,
                        typeTable.getComplexSchemaType(propertyName),
                        sequence,
                        false,
                        isArrayType,
                        type.isPrimitive());
            }

            if (typeTable.getComplexSchemaType(propertyName) != null && !((NamespaceMap) xmlSchema.getNamespaceContext()).values().
                    contains(typeTable.getComplexSchemaType(propertyName).getNamespaceURI())) {
                XmlSchemaImport importElement = new XmlSchemaImport();
                importElement.setNamespace(
                        typeTable.getComplexSchemaType(propertyName).getNamespaceURI());
                xmlSchema.getItems().add(importElement);
                ((NamespaceMap) xmlSchema.getNamespaceContext()).
                        put(generatePrefix(),
                                typeTable.getComplexSchemaType(propertyName).getNamespaceURI());
            }
        }


    }


    private void processGenerateWrappedArrayTypes(XmlSchema xmlSchema,
                                                  XmlSchemaSequence sequence,
                                                  Class<?> type,
                                                  String name,
                                                  boolean isArrayType,
                                                  String propertyName) {
        XmlSchemaElement xmlSchemaElement = new XmlSchemaElement();
        xmlSchemaElement.setName( name + "Wrapper");
        xmlSchemaElement.setNillable(true);
        sequence.getItems().add(xmlSchemaElement);

        String complexTypeName =
                typeTable.getSimpleSchemaTypeName(propertyName).getLocalPart() + "Wrapper";

        XmlSchemaComplexType xmlSchemaComplexType ;
        if (xmlSchema.getTypeByName(complexTypeName) == null) {
            xmlSchemaComplexType = new XmlSchemaComplexType(xmlSchema);
            XmlSchemaSequence xmlSchemaSequence = new XmlSchemaSequence();
            xmlSchemaComplexType.setParticle(xmlSchemaSequence);
            xmlSchemaComplexType.setName(complexTypeName);

            xmlSchema.getItems().add(xmlSchemaComplexType);
            xmlSchema.getSchemaTypes().add(
                    new QName(schemaTargetNameSpace, xmlSchemaComplexType.getName()),
                    xmlSchemaComplexType);
            addElementToSequence("array",
                typeTable.getSimpleSchemaTypeName(propertyName),
                xmlSchemaSequence,
                propertyName.equals("base64Binary"),
                isArrayType,
                type.isPrimitive());
        } else {
           xmlSchemaComplexType = (XmlSchemaComplexType) xmlSchema.getTypeByName(complexTypeName);
        }

        xmlSchemaElement.setSchemaType(xmlSchemaComplexType);
        xmlSchemaElement.setSchemaTypeName(new QName(schemaTargetNameSpace, xmlSchemaComplexType.getName()));
    }

    private void addElementToSequence(String name,
                                      QName propertyQName,
                                      XmlSchemaSequence sequence,
                                      boolean isBase64Binary,
                                      boolean isArryType,
                                      boolean isPrimitive) {
        XmlSchemaElement elt1 = new XmlSchemaElement();
        elt1.setName(name);
        elt1.setSchemaTypeName(propertyQName);
        sequence.getItems().add(elt1);
        if (isArryType && !isBase64Binary) {
            elt1.setMaxOccurs(Long.MAX_VALUE);
        }
        elt1.setMinOccurs(0);

        boolean disallowNillables = false;
        Parameter param = service.getParameter(Java2WSDLConstants.DISALLOW_NILLABLE_ELEMENTS_OPTION_LONG);
        if (param != null) {
            disallowNillables = JavaUtils.isTrueExplicitly(param.getValue());
        }

        if (!isPrimitive && !disallowNillables) {
            elt1.setNillable(true);
        }
    }


    private QName generateSchemaForType(XmlSchemaSequence sequence, Class<?> type, String partName)
            throws Exception {

        boolean isArrayType = false;
        if (type != null) {
            isArrayType = type.isArray();
        }
        if (isArrayType) {
            if (type.getComponentType().isArray()) {
                // this is a double array element
                Class<?> simpleType = type.getComponentType();
                String simpleTypeName = "";
                while (simpleType.isArray()) {
                    simpleTypeName += "ArrayOf";
                    simpleType = simpleType.getComponentType();
                }
                simpleTypeName += getSimpleClassName(simpleType);

                return processParameterArrayTypes(sequence, type, partName, simpleTypeName);


            } else {
                type = type.getComponentType();
            }
        }
        if (AxisFault.class.getName().equals(type)) {
            return null;
        }
        String classTypeName;
        if (type == null) {
            classTypeName = "java.lang.Object";
        } else {
            classTypeName = type.getName();
        }
        if (isArrayType && "byte".equals(classTypeName)) {
            classTypeName = "base64Binary";
            isArrayType = false;
        }

        if (isDataHandler(type)) {
            classTypeName = "base64Binary";
        }

        return generateSchemaTypeforNameCommon(sequence, partName, isArrayType, type, classTypeName);
    }


    private QName generateSchemaForType(XmlSchemaSequence sequence, Type genericType, String partName, boolean isArrayType)
            throws Exception {


        Class<?> type;
        if (genericType instanceof GenericArrayType) {
            // this is a double array element
            Class<?> simpleType = (Class<?>) ((GenericArrayType) genericType).getGenericComponentType();
            String simpleTypeName = "";
            while (simpleType.isArray()) {
                simpleTypeName += "ArrayOf";
                simpleType = simpleType.getComponentType();
            }
            simpleTypeName += simpleType.getSimpleName();

            XmlSchema xmlSchema = getXmlSchema(schemaTargetNameSpace);
            if (xmlSchema.getTypeByName(simpleTypeName) == null) {
                XmlSchemaComplexType xmlSchemaComplexType = new XmlSchemaComplexType(xmlSchema);
                XmlSchemaSequence xmlSchemaSequence = new XmlSchemaSequence();
                xmlSchemaComplexType.setParticle(xmlSchemaSequence);
                generateSchemaForType(xmlSchemaSequence, simpleType, "array",true);
                xmlSchemaComplexType.setName(simpleTypeName);
                xmlSchema.getItems().add(xmlSchemaComplexType);
                xmlSchema.getSchemaTypes().add(
                        new QName(xmlSchema.getTargetNamespace(), simpleTypeName), xmlSchemaComplexType);
            }

            if (isGenerateWrappedArrayTypes) {
                XmlSchemaElement xmlSchemaElement = new XmlSchemaElement();
                xmlSchemaElement.setName(partName + "Wrapper");
                xmlSchemaElement.setNillable(true);
                sequence.getItems().add(xmlSchemaElement);

                String complexTypeName = simpleTypeName + "Wrapper";

                XmlSchemaComplexType xmlSchemaComplexType = new XmlSchemaComplexType(xmlSchema);
                XmlSchemaSequence xmlSchemaSequence = new XmlSchemaSequence();
                xmlSchemaComplexType.setParticle(xmlSchemaSequence);
                xmlSchemaComplexType.setName(complexTypeName);

                xmlSchema.getItems().add(xmlSchemaComplexType);
                xmlSchema.getSchemaTypes().add(
                        new QName(schemaTargetNameSpace, xmlSchemaComplexType.getName()),
                        xmlSchemaComplexType);
                addContentToMethodSchemaType(xmlSchemaSequence,
                        new QName(xmlSchema.getTargetNamespace(), simpleTypeName),
                        "array",
                        true);

                xmlSchemaElement.setSchemaType(xmlSchemaComplexType);
                xmlSchemaElement.setSchemaTypeName(new QName(schemaTargetNameSpace, xmlSchemaComplexType.getName()));
                return new QName(xmlSchema.getTargetNamespace(), complexTypeName);
            } else {
                addContentToMethodSchemaType(sequence,
                        new QName(xmlSchema.getTargetNamespace(), simpleTypeName),
                        partName,
                        true);
                return new QName(xmlSchema.getTargetNamespace(), simpleTypeName);
            }


        } else{
            type = (Class<?>) genericType;
        }
        if (AxisFault.class.getName().equals(type)) {
            return null;
        }
        String classTypeName;
        if (type == null) {
            classTypeName = "java.lang.Object";
        } else {
            classTypeName = type.getName();
        }
        if (isArrayType && "byte".equals(classTypeName)) {
            classTypeName = "base64Binary";
            isArrayType = false;
        }

        if (isDataHandler(type)) {
            classTypeName = "base64Binary";
        }

        return generateSchemaTypeforNameCommon(sequence, partName, isArrayType, type, classTypeName);
    }

    private QName generateSchemaTypeforNameCommon(XmlSchemaSequence sequence,
                                                  String partName,
                                                  boolean isArrayType,
                                                  Class<?> type,
                                                  String classTypeName) throws Exception {
        QName schemaTypeName = typeTable.getSimpleSchemaTypeName(classTypeName);
        if (schemaTypeName == null) {
            schemaTypeName = generateSchema(type);
            if (isGenerateWrappedArrayTypes && isArrayType) {

                XmlSchemaElement xmlSchemaElement = new XmlSchemaElement();
                xmlSchemaElement.setName(partName + "Wrapper");
                xmlSchemaElement.setNillable(true);
                sequence.getItems().add(xmlSchemaElement);

                String complexTypeName = schemaTypeName.getLocalPart() + "Wrapper";
                XmlSchema xmlSchema = getXmlSchema(schemaTargetNameSpace);

                XmlSchemaComplexType xmlSchemaComplexType = null;
                if (xmlSchema.getTypeByName(complexTypeName) == null) {
                    xmlSchemaComplexType = new XmlSchemaComplexType(xmlSchema);
                    XmlSchemaSequence xmlSchemaSequence = new XmlSchemaSequence();
                    xmlSchemaComplexType.setParticle(xmlSchemaSequence);
                    xmlSchemaComplexType.setName(complexTypeName);

                    xmlSchema.getItems().add(xmlSchemaComplexType);
                    xmlSchema.getSchemaTypes().add(
                            new QName(schemaTargetNameSpace, xmlSchemaComplexType.getName()),
                            xmlSchemaComplexType);
                    addContentToMethodSchemaType(xmlSchemaSequence,
                        schemaTypeName,
                        "array",
                        isArrayType);
                } else {
                   xmlSchemaComplexType = (XmlSchemaComplexType) xmlSchema.getTypeByName(complexTypeName);
                }

                xmlSchemaElement.setSchemaType(xmlSchemaComplexType);
                xmlSchemaElement.setSchemaTypeName(new QName(schemaTargetNameSpace, xmlSchemaComplexType.getName()));


            } else {
                addContentToMethodSchemaType(sequence,
                    schemaTypeName,
                    partName,
                    isArrayType);
            }

            String schemaNamespace;
            schemaNamespace = resolveSchemaNamespace(getQualifiedName(type.getPackage()));
            addImport(getXmlSchema(schemaNamespace), schemaTypeName);

        } else {
            if (isGenerateWrappedArrayTypes && isArrayType) {

                XmlSchemaElement xmlSchemaElement = new XmlSchemaElement();
                xmlSchemaElement.setName(partName +"Wrapper");
                xmlSchemaElement.setNillable(true);
                sequence.getItems().add(xmlSchemaElement);

                String complexTypeName = schemaTypeName.getLocalPart() + "Wrapper";
                XmlSchema xmlSchema = getXmlSchema(schemaTargetNameSpace);

                XmlSchemaComplexType xmlSchemaComplexType = null;
                if (xmlSchema.getTypeByName(complexTypeName) == null) {
                    xmlSchemaComplexType = new XmlSchemaComplexType(xmlSchema);
                    XmlSchemaSequence xmlSchemaSequence = new XmlSchemaSequence();
                    xmlSchemaComplexType.setParticle(xmlSchemaSequence);
                    xmlSchemaComplexType.setName(complexTypeName);

                    xmlSchema.getItems().add(xmlSchemaComplexType);
                    xmlSchema.getSchemaTypes().add(
                            new QName(schemaTargetNameSpace, xmlSchemaComplexType.getName()),
                            xmlSchemaComplexType);
                    addContentToMethodSchemaType(xmlSchemaSequence,
                        schemaTypeName,
                        "array",
                        isArrayType);
                } else {
                   xmlSchemaComplexType = (XmlSchemaComplexType) xmlSchema.getTypeByName(complexTypeName);
                }

                xmlSchemaElement.setSchemaType(xmlSchemaComplexType);
                xmlSchemaElement.setSchemaTypeName(new QName(schemaTargetNameSpace, xmlSchemaComplexType.getName()));


            } else {
                addContentToMethodSchemaType(sequence,
                        schemaTypeName,
                        partName,
                        isArrayType);
            }

        }
        addImport(getXmlSchema(schemaTargetNameSpace), schemaTypeName);
        return schemaTypeName;
    }

    private QName processParameterArrayTypes(XmlSchemaSequence sequence, Class<?> type, String partName, String simpleTypeName) throws Exception {
        XmlSchema xmlSchema = getXmlSchema(schemaTargetNameSpace);
        if (xmlSchema.getTypeByName(simpleTypeName) == null) {
            XmlSchemaComplexType xmlSchemaComplexType = new XmlSchemaComplexType(xmlSchema);
            XmlSchemaSequence xmlSchemaSequence = new XmlSchemaSequence();
            xmlSchemaComplexType.setParticle(xmlSchemaSequence);
            generateSchemaForType(xmlSchemaSequence, type.getComponentType(), "array");
            xmlSchemaComplexType.setName(simpleTypeName);
            xmlSchema.getItems().add(xmlSchemaComplexType);
            xmlSchema.getSchemaTypes().add(
                    new QName(xmlSchema.getTargetNamespace(), simpleTypeName), xmlSchemaComplexType);
        }

        if (isGenerateWrappedArrayTypes) {
            XmlSchemaElement xmlSchemaElement = new XmlSchemaElement();
            xmlSchemaElement.setName(partName + "Wrapper");
            xmlSchemaElement.setNillable(true);
            sequence.getItems().add(xmlSchemaElement);

            String complexTypeName = simpleTypeName + "Wrapper";

            XmlSchemaComplexType xmlSchemaComplexType = new XmlSchemaComplexType(xmlSchema);
            XmlSchemaSequence xmlSchemaSequence = new XmlSchemaSequence();
            xmlSchemaComplexType.setParticle(xmlSchemaSequence);
            xmlSchemaComplexType.setName(complexTypeName);

            xmlSchema.getItems().add(xmlSchemaComplexType);
            xmlSchema.getSchemaTypes().add(
                    new QName(schemaTargetNameSpace, xmlSchemaComplexType.getName()),
                    xmlSchemaComplexType);
            addContentToMethodSchemaType(xmlSchemaSequence,
                    new QName(xmlSchema.getTargetNamespace(), simpleTypeName),
                    "array",
                    true);

            xmlSchemaElement.setSchemaType(xmlSchemaComplexType);
            xmlSchemaElement.setSchemaTypeName(new QName(schemaTargetNameSpace, xmlSchemaComplexType.getName()));
            return new QName(xmlSchema.getTargetNamespace(), complexTypeName);
        } else {
            addContentToMethodSchemaType(sequence,
                    new QName(xmlSchema.getTargetNamespace(), simpleTypeName),
                    partName,
                    true);
            return new QName(xmlSchema.getTargetNamespace(), simpleTypeName);
        }
    }

    protected boolean isDataHandler(Class<?> clazz) {
        return clazz != null && DataHandler.class.isAssignableFrom(clazz);
    }

    protected void addContentToMethodSchemaType(XmlSchemaSequence sequence,
                                                QName schemaTypeName,
                                                String paraName,
                                                boolean isArray) {
        XmlSchemaElement elt1 = new XmlSchemaElement();
        elt1.setName(paraName);

        elt1.setSchemaTypeName(schemaTypeName);
        if (sequence != null) {
            sequence.getItems().add(elt1);
        }

        if (isArray) {
            elt1.setMaxOccurs(Long.MAX_VALUE);
        }
        elt1.setMinOccurs(0);

        boolean disallowNillables = false;
        Parameter param = service.getParameter(Java2WSDLConstants.DISALLOW_NILLABLE_ELEMENTS_OPTION_LONG);
        if (param != null) {
            disallowNillables = JavaUtils.isTrueExplicitly(param.getValue());
        }

        if (!("int".equals(schemaTypeName.getLocalPart()) ||
                "double".equals(schemaTypeName.getLocalPart()) ||
                "long".equals(schemaTypeName.getLocalPart()) ||
                "boolean".equals(schemaTypeName.getLocalPart()) ||
                "short".equals(schemaTypeName.getLocalPart()) ||
                "float".equals(schemaTypeName.getLocalPart())) && !disallowNillables) {
            elt1.setNillable(true);
        }
    }

    private XmlSchemaComplexType createSchemaTypeForMethodPart(String localPartName) {
        XmlSchema xmlSchema = getXmlSchema(schemaTargetNameSpace);
        QName elementName =
                new QName(this.schemaTargetNameSpace, localPartName, this.schema_namespace_prefix);

        XmlSchemaComplexType complexType = getComplexTypeForElement(xmlSchema, elementName);
        if (complexType == null) {
            complexType = new XmlSchemaComplexType(xmlSchema);

            XmlSchemaElement globalElement = new XmlSchemaElement();
            globalElement.setName(localPartName);
            globalElement.setQName(elementName);

            boolean disallowAnonTypes = isAnonymousTypesDisallowed();
            if (disallowAnonTypes) {
                String complexTypeName = localPartName.substring(0, 1).toUpperCase()
                        + localPartName.substring(1);
                complexType.setName(complexTypeName);
                globalElement.setSchemaTypeName(complexType.getQName());
                xmlSchema.getItems().add(complexType);
                xmlSchema.getSchemaTypes().add(complexType.getQName(), complexType);
            } else {
                globalElement.setSchemaType(complexType);
            }

            xmlSchema.getItems().add(globalElement);
            xmlSchema.getElements().add(elementName, globalElement);
        }
        typeTable.addComplexSchema(localPartName, elementName);

        return complexType;
    }

    private XmlSchemaComplexType createSchemaTypeForFault(String localPartName) {
        XmlSchema xmlSchema = getXmlSchema(schemaTargetNameSpace);
        QName elementName =
                new QName(this.schemaTargetNameSpace, localPartName, this.schema_namespace_prefix);

        XmlSchemaComplexType complexType = getComplexTypeForElement(xmlSchema, elementName);
        if (complexType == null) {
            complexType = new XmlSchemaComplexType(xmlSchema);

            XmlSchemaElement globalElement = new XmlSchemaElement();
            globalElement.setName(localPartName);
            globalElement.setQName(elementName);

            boolean disallowAnonTypes = isAnonymousTypesDisallowed();
            if (disallowAnonTypes) {
                complexType.setName(localPartName);
                globalElement.setSchemaTypeName(complexType.getQName());
                xmlSchema.getItems().add(complexType);
                xmlSchema.getSchemaTypes().add(complexType.getQName(), complexType);
            } else {
                globalElement.setSchemaType(complexType);
            }

            xmlSchema.getItems().add(globalElement);
            xmlSchema.getElements().add(elementName, globalElement);

        }
        return complexType;
    }

     private boolean isAnonymousTypesDisallowed() {
        boolean disallowAnonTypes = false;
        Parameter param = service.getParameter(Java2WSDLConstants.DISALLOW_ANON_TYPES_OPTION_LONG);
        if (param != null) {
            disallowAnonTypes = JavaUtils.isTrueExplicitly(param.getValue());
        }
        return disallowAnonTypes;
    }

    private String getRequestElementSuffix() {
        String requestElementSuffix = null;
        Parameter param = service.getParameter(Java2WSDLConstants.REQUEST_ELEMENT_SUFFIX_OPTION_LONG);
        if (param != null) {
            requestElementSuffix = (String) param.getValue();
        }
        return requestElementSuffix;
    }

    protected XmlSchemaComplexType getComplexTypeForElement(XmlSchema xmlSchema, QName name) {
        
        Iterator<?> iterator = xmlSchema.getItems().getIterator();
        while (iterator.hasNext()) {
            XmlSchemaObject object = (XmlSchemaObject) iterator.next();
            if (object instanceof XmlSchemaElement && ((XmlSchemaElement) object).getQName().equals(name)) {
                XmlSchemaComplexType xmlSchemaComplexType = (XmlSchemaComplexType) ((XmlSchemaElement) object).getSchemaType();
                if ((xmlSchemaComplexType == null) && (((XmlSchemaElement) object).getSchemaTypeName() != null)){
                    xmlSchemaComplexType =
                            (XmlSchemaComplexType) xmlSchema.getTypeByName(((XmlSchemaElement) object).getSchemaTypeName());
                }
                return xmlSchemaComplexType;
            }
        }
        return null;
    }

    protected XmlSchema getXmlSchema(String targetNamespace) {
        XmlSchema xmlSchema;

        if ((xmlSchema = schemaMap.get(targetNamespace)) == null) {
            String targetNamespacePrefix;

            if (targetNamespace.equals(schemaTargetNameSpace) &&
                    schema_namespace_prefix != null) {
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

    public Method[] getMethods() {
        return methods;
    }

    protected String generatePrefix() {
        return NAME_SPACE_PREFIX + prefixCount++;
    }

    public void setExcludeMethods(ArrayList<String> excludeMethods) {
        if (excludeMethods == null) excludeMethods = new ArrayList<String>();
        this.excludeMethods = excludeMethods;
    }

    public String getSchemaTargetNameSpace() {
        return schemaTargetNameSpace;
    }

    protected void addImport(XmlSchema xmlSchema, QName schemaTypeName) {
        NamespacePrefixList map = xmlSchema.getNamespaceContext();
        if (map == null ||
                ((map instanceof NamespaceMap) && ((NamespaceMap) map).values() == null) ||
                schemaTypeName == null) {
            return;
        }
        if (map instanceof NamespaceMap && !((NamespaceMap) map).values().
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

    protected XmlSchemaForm getAttrFormDefaultSetting() {
        if (FORM_DEFAULT_UNQUALIFIED.equals(getAttrFormDefault())) {
            return new XmlSchemaForm(XmlSchemaForm.UNQUALIFIED);
        } else {
            return new XmlSchemaForm(XmlSchemaForm.QUALIFIED);
        }
    }

    protected XmlSchemaForm getElementFormDefaultSetting() {
        if (FORM_DEFAULT_UNQUALIFIED.equals(getElementFormDefault())) {
            return new XmlSchemaForm(XmlSchemaForm.UNQUALIFIED);
        } else {
            return new XmlSchemaForm(XmlSchemaForm.QUALIFIED);
        }
    }

    public ArrayList<String> getExtraClasses() {
        if (extraClasses == null) {
            extraClasses = new ArrayList<String>();
        }
        return extraClasses;
    }

    public void setExtraClasses(ArrayList<String> extraClasses) {
        this.extraClasses = extraClasses;
    }

    protected String resolveSchemaNamespace(String packageName) throws Exception {
        //if all types must go into the wsdl types schema namespace
        if (useWSDLTypesNamespace) {
            //return schemaTargetNameSpace;
            return pkg2nsmap.get("all");
        } else {
            if (pkg2nsmap != null && !pkg2nsmap.isEmpty()) {
                //if types should go into namespaces that are mapped against the package name for the type
                if (pkg2nsmap.get(packageName) != null) {
                    //return that mapping
                    return pkg2nsmap.get(packageName);
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

    public Map<String,String> getPkg2nsmap() {
        return pkg2nsmap;
    }

    public void setPkg2nsmap(Map<String,String> pkg2nsmap) {
        this.pkg2nsmap = pkg2nsmap;
    }

    public String getTargetNamespace() {
        return targetNamespace;
    }

    protected String getClassName(Class<?> type) {
        String name = type.getName();
        if (name.indexOf("$") > 0) {
            name = name.replace('$', '_');
        }
        return name;
    }
    
    protected String getSimpleClassName(Class type) {
        String simpleClassName = type.getName();
        int idx = simpleClassName.lastIndexOf('.');
        if (idx != -1 && idx < (simpleClassName.length() - 1)) {
            simpleClassName = simpleClassName.substring(idx + 1);
        }
        
        return simpleClassName.replace('$', '_');
    }

    protected String getQualifiedName(Package packagez) {
        if (packagez != null) {
            return packagez.getName();
        } else {
            return "";
        }
    }

    public void setNonRpcMethods(ArrayList<String> nonRpcMethods) {
        if (nonRpcMethods != null) {
            this.nonRpcMethods = nonRpcMethods;
        }
    }

    public void setAxisService(AxisService service) {
        this.service = service;
    }


    public String getCustomSchemaLocation() {
        return customSchemaLocation;
    }

    public void setCustomSchemaLocation(String customSchemaLocation) {
        this.customSchemaLocation = customSchemaLocation;
    }

    public String getMappingFileLocation() {
        return mappingFileLocation;
    }

    public void setMappingFileLocation(String mappingFileLocation) {
        this.mappingFileLocation = mappingFileLocation;
    }

    protected String getParameterName(Annotation[][] parameterAnnotation,
                                      int j,
                                      String[] parameterNames) {
        String parameterName = null;
        if (parameterAnnotation.length > 0) {
            WebParamAnnotation annotation =
                    JSR181Helper.INSTANCE.getWebParamAnnotation(parameterAnnotation[j]);
            if (annotation != null) {
                parameterName = annotation.getName();
            }
        }
        if (parameterName == null || "".equals(parameterName)) {
            if(parameterNames != null && parameterNames.length > j) {
                parameterName = parameterNames[j];
            }
            if (parameterName == null || "".equals(parameterName)) {
                parameterName = "args" + j;
            }
        }
        return parameterName;
    }

    public class MathodComparator implements Comparator<Method> {
        public int compare(Method method1, Method method2) {
            String[] values = new String[2];
            values[0] = method1.getName();
            values[1] = method2.getName();
            Arrays.sort(values);
            if (values[0].equals(method1.getName())) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    public class PropertyComparator implements Comparator<PropertyDescriptor> {
        public int compare(PropertyDescriptor propertyDescriptor1,
                PropertyDescriptor propertyDescriptor2) {
            
            String[] values = new String[2];
            values[0] = propertyDescriptor1.getName();
            values[1] = propertyDescriptor2.getName();
            Arrays.sort(values);
            if (values[0].equals(propertyDescriptor1.getName())) {
                return 0;
            } else {
                return 1;
            }
        }
    }
}
