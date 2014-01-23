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

package org.apache.axis2.schema;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.schema.typemap.JavaTypeMap;
import org.apache.axis2.schema.typemap.TypeMap;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.wsdl.WSDLUtil;
import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.databinding.CTypeMapper;
import org.apache.axis2.wsdl.databinding.DefaultTypeMapper;
import org.apache.axis2.wsdl.databinding.JavaTypeMapper;
import org.apache.axis2.wsdl.databinding.TypeMapper;
import org.apache.axis2.wsdl.util.Constants;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAny;
import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaComplexContent;
import org.apache.ws.commons.schema.XmlSchemaComplexContentExtension;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaContent;
import org.apache.ws.commons.schema.XmlSchemaContentModel;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaImport;
import org.apache.ws.commons.schema.XmlSchemaInclude;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaType;

import javax.xml.namespace.QName;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This is the utility for the extension to call by reflection.
 */
public class ExtensionUtility {


    public static void invoke(CodeGenConfiguration configuration) throws Exception {
        List<XmlSchema> schemaList = new ArrayList<XmlSchema>();
        // add all the schemas to the list
        for (AxisService service : configuration.getAxisServices()) {
            schemaList.addAll(service.getSchema());
        }

        //hashmap that keeps the targetnamespace and the xmlSchema object
        //this is a convenience to locate the relevant schema quickly
        //by looking at the target namespace
        Map<String,XmlSchema> schemaMap = new HashMap<String,XmlSchema>();
        populateSchemaMap(schemaMap, schemaList);

        if (schemaList == null || schemaList.isEmpty()) {
            //there are no types to be code generated
            //However if the type mapper is left empty it will be a problem for the other
            //processes. Hence the default type mapper is set to the configuration
            configuration.setTypeMapper(new DefaultTypeMapper());
            return;
        }
        //call the schema compiler
        CompilerOptions options = new CompilerOptions();

        //set the default options
        populateDefaultOptions(options, configuration);

        //set the user parameters. the user parameters get the preference over
        //the default ones. But the user better know what he's doing if he
        //used module specific parameters
        populateUserparameters(options, configuration);

        SchemaCompiler schemaCompiler = new SchemaCompiler(options);
        // run the schema compiler
        schemaCompiler.compile(schemaList);

        //create the type mapper
        //First try to take the one that is already there
        TypeMapper mapper = configuration.getTypeMapper();
        if (mapper == null) {
            if (configuration.getOutputLanguage() != null &&
                    !configuration.getOutputLanguage().trim().equals("") &&
                    configuration.getOutputLanguage().toLowerCase().equals("c")) {
                mapper = new CTypeMapper();

            }  else {
                mapper = new JavaTypeMapper();
            }

        }

        if (options.isWriteOutput()) {
            //get the processed element map and transfer it to the type mapper
            for (Map.Entry<QName,String> entry : schemaCompiler.getProcessedElementMap().entrySet()) {
                mapper.addTypeMappingName(entry.getKey(), entry.getValue());
            }

        } else {
            //get the processed model map and transfer it to the type mapper
            //since the options mentiond that its not writable, it should have
            //populated the model map
            Map processedModelMap = schemaCompiler.getProcessedModelMap();
            Iterator processedkeys = processedModelMap.keySet().iterator();
            QName qNameKey;
            while (processedkeys.hasNext()) {
                qNameKey = (QName) processedkeys.next();
                mapper.addTypeMappingObject(qNameKey, processedModelMap.get(qNameKey));
            }

            for (Map.Entry<QName,String> entry : schemaCompiler.getProcessedElementMap().entrySet()) {
                mapper.addTypeMappingName(entry.getKey(), entry.getValue());
            }

            //get the ADB template from the schema compilers property bag and set the
            //template
            configuration.putProperty(Constants.EXTERNAL_TEMPLATE_PROPERTY_KEY,
                    schemaCompiler.getCompilerProperties().getProperty(
                            SchemaConstants.SchemaPropertyNames.BEAN_WRITER_TEMPLATE_KEY));

        }

        //process the unwrapped parameters
        if (!configuration.isParametersWrapped()) {
            //figure out the unwrapped operations
            for (AxisService axisService : configuration.getAxisServices()) {
                for (Iterator<AxisOperation> operations = axisService.getOperations();
                     operations.hasNext();) {
                    AxisOperation op = operations.next();
                    if (WSDLUtil.isInputPresentForMEP(op.getMessageExchangePattern())) {
                        walkSchema(op.getMessage(
                                WSDLConstants.MESSAGE_LABEL_IN_VALUE),
                                mapper,
                                schemaMap,
                                op.getName().getLocalPart(),
                                WSDLConstants.INPUT_PART_QNAME_SUFFIX);
                    }

                    // TODO: support for xml beans
                    if (configuration.getDatabindingType().equals("adb")) {
                        if (WSDLUtil.isOutputPresentForMEP(op.getMessageExchangePattern())) {
                            walkSchema(op.getMessage(
                                    WSDLConstants.MESSAGE_LABEL_OUT_VALUE),
                                    mapper,
                                    schemaMap,
                                    op.getName().getLocalPart(),
                                    WSDLConstants.OUTPUT_PART_QNAME_SUFFIX);
                        }
                    }

                }
            }

        }

        //put the complext types for the top level elements having them
        // this is needed in unwrapping and to provide backwordCompatibility
        if (!configuration.isParametersWrapped() || configuration.isBackwordCompatibilityMode()) {
            for (AxisService axisService : configuration.getAxisServices()) {
                AxisOperation axisOperation;
                AxisMessage axisMessage;
                for (Iterator<AxisOperation> operators = axisService.getOperations(); operators.hasNext();) {
                    axisOperation = operators.next();
                    if (WSDLUtil.isInputPresentForMEP(axisOperation.getMessageExchangePattern())) {
                        axisMessage = axisOperation.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                        setComplexTypeName(axisMessage);
                    }
                    if (WSDLUtil.isOutputPresentForMEP(axisOperation.getMessageExchangePattern())) {
                        axisMessage = axisOperation.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                        setComplexTypeName(axisMessage);
                    }
                }
            }

        }

        //set the type mapper to the config
        configuration.setTypeMapper(mapper);

    }

    /**
     * set the complext type class name as an message parameter if it exits
     * @param axisMessage
     */
    private static void setComplexTypeName(AxisMessage axisMessage) throws AxisFault {

        if (axisMessage.getSchemaElement() != null){

            XmlSchemaElement schemaElement = axisMessage.getSchemaElement();
            XmlSchemaType schemaType = schemaElement.getSchemaType();
            QName schemaTypeQname = schemaElement.getSchemaTypeName();

            if (schemaType == null) {
                if (schemaTypeQname != null) {
                    // find the schema type from all the schemas
                    // now we need to get the schema of the extension type from the parent schema. For that let's first retrieve
                    // the parent schema
                    AxisService axisService = axisMessage.getAxisOperation().getAxisService();
                    for (XmlSchema schema : axisService.getSchema()) {
                        schemaType = getSchemaType(schema, schemaTypeQname);
                        if (schemaType != null) {
                            break;
                        }
                    }
                }
            }

            if (schemaType instanceof XmlSchemaComplexType){
                XmlSchemaComplexType complexType = (XmlSchemaComplexType) schemaType;
                if ((complexType.getName() != null) && (complexType.getQName() != null)) {
                    Map metaInfo = complexType.getMetaInfoMap();
                    String complexTypeName = (String)
                            metaInfo.get(SchemaConstants.SchemaCompilerInfoHolder.CLASSNAME_KEY);
                    if (complexTypeName.endsWith("[]")){
                        complexTypeName = complexTypeName.substring(0,complexTypeName.length() -2);
                    }
                    // store the complext type name to process later
                   axisMessage.addParameter(new Parameter(Constants.COMPLEX_TYPE, complexTypeName));
                }
            }
        }


    }

    /**
     * Populate the schema objects into the
     *
     * @param schemaMap
     * @param schemaList
     */
    private static void populateSchemaMap(Map<String,XmlSchema> schemaMap, List<XmlSchema> schemaList) {
        for (XmlSchema xmlSchema : schemaList) {
            schemaMap.put(xmlSchema.getTargetNamespace(), xmlSchema);
        }
    }

    /**
     * @param message
     * @param mapper
     */
    private static void walkSchema(AxisMessage message,
                                   TypeMapper mapper,
                                   Map<String,XmlSchema> schemaMap,
                                   String opName,
                                   String qnameSuffix) {

        if (message.getParameter(Constants.UNWRAPPED_KEY) != null) {

            XmlSchemaElement schemaElement = message.getSchemaElement();
            XmlSchemaType schemaType = schemaElement.getSchemaType();
            QName schemaTypeQname = schemaElement.getSchemaTypeName();

            if (schemaType == null) {
                if (schemaTypeQname != null) {
                    // find the schema type from all the schemas
                    // now we need to get the schema of the extension type from the parent schema. For that let's first retrieve
                    // the parent schema
                    AxisService axisService = message.getAxisOperation().getAxisService();
                    for (XmlSchema schema : axisService.getSchema()) {
                        schemaType = getSchemaType(schema, schemaTypeQname);
                        if (schemaType != null) {
                            break;
                        }
                    }
                }
            }

            //create a type mapper
            TypeMap basicTypeMap = new JavaTypeMap();
            if (schemaType instanceof XmlSchemaComplexType) {
                processXMLSchemaComplexType(schemaType, mapper, opName, schemaMap, qnameSuffix);
            } else if ((schemaTypeQname != null) && basicTypeMap.getTypeMap().containsKey(schemaTypeQname)){
                QName partQName = WSDLUtil.getPartQName(opName,
                        qnameSuffix,
                        message.getElementQName().getLocalPart());
                mapper.addTypeMappingName(partQName, (String)basicTypeMap.getTypeMap().get(schemaTypeQname));
            } else if (schemaType instanceof XmlSchemaSimpleType) {
                XmlSchemaSimpleType xmlSchemaSimpleType = (XmlSchemaSimpleType) schemaType;
                populateClassName(xmlSchemaSimpleType.getMetaInfoMap(),
                        mapper,
                        opName,
                        false,
                        message.getElementQName().getLocalPart(),
                        qnameSuffix);
                // handle xsd:anyType
            }

        }
    }

    private static void processXMLSchemaComplexType(XmlSchemaType schemaType,
                                                    TypeMapper mapper,
                                                    String opName,
                                                    Map schemaMap,
                                                    String qnameSuffix) {
        if (schemaType instanceof XmlSchemaComplexType) {
            XmlSchemaComplexType cmplxType = (XmlSchemaComplexType) schemaType;
            if (cmplxType.getContentModel() == null) {
                processSchemaSequence(cmplxType.getParticle(), mapper, opName, schemaMap, qnameSuffix);
            } else {
                processComplexContentModel(cmplxType, mapper, opName, schemaMap, qnameSuffix);
            }
            processAttributes(cmplxType, opName, qnameSuffix, mapper);
        }
    }

    private static void processAttributes(XmlSchemaComplexType complexType,
                                   String opName,
                                   String qnameSuffix,
                                   TypeMapper typeMap) {
        XmlSchemaObjectCollection xmlObjectCollection = complexType.getAttributes();
        XmlSchemaObject item;
        for (Iterator iter = xmlObjectCollection.getIterator(); iter.hasNext();) {
            item = (XmlSchemaObject) iter.next();
            XmlSchemaAttribute xmlSchemaAttribute;
            if (item instanceof XmlSchemaAttribute) {
                xmlSchemaAttribute = (XmlSchemaAttribute) item;
                populateClassName(xmlSchemaAttribute.getMetaInfoMap(),
                        typeMap,
                        opName,
                        false,
                        xmlSchemaAttribute.getName(),
                        qnameSuffix);
            }

        }

    }

    private static XmlSchemaType getSchemaType(XmlSchema schema, QName typeName) {
        XmlSchemaType xmlSchemaType = null;
        if (schema != null) {
            xmlSchemaType = schema.getTypeByName(typeName);
            if (xmlSchemaType == null) {
                // try to find in an import or an include
                XmlSchemaObjectCollection includes = schema.getIncludes();
                if (includes != null) {
                    Iterator includesIter = includes.getIterator();
                    Object object = null;
                    while (includesIter.hasNext()) {
                        object = includesIter.next();
                        if (object instanceof XmlSchemaImport) {
                            XmlSchema schema1 = ((XmlSchemaImport) object).getSchema();
                            xmlSchemaType = getSchemaType(schema1,typeName);
                        }
                        if (object instanceof XmlSchemaInclude) {
                            XmlSchema schema1 = ((XmlSchemaInclude) object).getSchema();
                            xmlSchemaType = getSchemaType(schema1,typeName);
                        }
                        if (xmlSchemaType != null){
                            break;
                        }
                    }
                }
            }
        }
        return xmlSchemaType;
    }


    private static void processComplexContentModel(XmlSchemaComplexType cmplxType,
                                                   TypeMapper mapper,
                                                   String opName,
                                                   Map<String,XmlSchema> schemaMap,
                                                   String qnameSuffix) {
        XmlSchemaContentModel contentModel = cmplxType.getContentModel();
        if (contentModel instanceof XmlSchemaComplexContent) {
            XmlSchemaComplexContent xmlSchemaComplexContent = (XmlSchemaComplexContent) contentModel;
            XmlSchemaContent content = xmlSchemaComplexContent.getContent();
            if (content instanceof XmlSchemaComplexContentExtension) {
                XmlSchemaComplexContentExtension schemaExtension = (XmlSchemaComplexContentExtension) content;

                // process particles inside this extension, if any
                processSchemaSequence(schemaExtension.getParticle(), mapper, opName, schemaMap, qnameSuffix);

                 XmlSchemaType extensionSchemaType = null;
                for (XmlSchema xmlSchema : schemaMap.values()) {
                    extensionSchemaType = getSchemaType(xmlSchema,schemaExtension.getBaseTypeName());
                    if (extensionSchemaType != null){
                        break;
                    }
                }

                processXMLSchemaComplexType(extensionSchemaType, mapper, opName, schemaMap, qnameSuffix);
            }
        }
    }

    private static void processSchemaSequence(XmlSchemaParticle particle,
                                              TypeMapper mapper,
                                              String opName,
                                              Map<String,XmlSchema> schemaMap,
                                              String qnameSuffix) {
        if (particle instanceof XmlSchemaSequence) {
            XmlSchemaObjectCollection items = ((XmlSchemaSequence) particle).getItems();
            for (Iterator i = items.getIterator(); i.hasNext();) {
                Object item = i.next();
                // get each and every element in the sequence and
                // traverse through them
                if (item instanceof XmlSchemaElement) {
                    //populate the map with the partname - class name
                    //attached to the schema element
                    XmlSchemaElement xmlSchemaElement = (XmlSchemaElement) item;
                    boolean isArray = xmlSchemaElement.getMaxOccurs() > 1;

                    XmlSchemaType schemaType = xmlSchemaElement.getSchemaType();
                    String partName = null;
                    if (xmlSchemaElement.getRefName() != null) {
                        partName = xmlSchemaElement.getRefName().getLocalPart();
                    } else {
                        partName = xmlSchemaElement.getName();
                    }


                    // get the element class name from the element and populate the mapper
                    populateClassName(xmlSchemaElement.getMetaInfoMap(),
                                    mapper,
                                    opName,
                                    isArray,
                                    partName,
                                    qnameSuffix);
                } else if (item instanceof XmlSchemaAny) {

                    // if this is an instance of xs:any, then there is no part name for it. Using ANY_ELEMENT_FIELD_NAME
                    // for it for now

                    XmlSchemaAny xmlSchemaAny = (XmlSchemaAny) item;
                    boolean isArray = xmlSchemaAny.getMaxOccurs() > 1;

                    QName partQName = WSDLUtil.getPartQName(opName,
                            qnameSuffix,
                            Constants.ANY_ELEMENT_FIELD_NAME);

                    if (isArray) {
                        mapper.addTypeMappingName(partQName, "org.apache.axiom.om.OMElement[]");
                    } else {
                        mapper.addTypeMappingName(partQName, "org.apache.axiom.om.OMElement");
                    }
                }
            }
        }
    }

    // private static void

    /**
     * Util method to populate the class name into the typeMap
     *
     */
    private static void  populateClassName(Map metaInfoMap,
                                          TypeMapper typeMap,
                                          String opName,
                                          boolean isArray,
                                          String partName,
                                          String qnameSuffix) {



        if (metaInfoMap != null) {
            String className = (String) metaInfoMap.
                    get(SchemaConstants.SchemaCompilerInfoHolder.CLASSNAME_KEY);

            // this is a temporary patch
            // the acual problem is keeping the class name details on the schemaType in
            // XmlSchema compiler.
            // we have to store them in XmlElement
            if (isArray && !className.endsWith("[]")) {
                className += "[]";
            } else if (!isArray && className.endsWith("[]")) {
                className = className.substring(0, className.length() - 2);
            }


            QName partQName = WSDLUtil.getPartQName(opName,
                    qnameSuffix,
                    partName);
            typeMap.addTypeMappingName(partQName, className);
            if (Boolean.TRUE.equals(
                    metaInfoMap.get(SchemaConstants.
                            SchemaCompilerInfoHolder.CLASSNAME_PRIMITVE_KEY))) {

                //this type is primitive - add that to the type mapper status
                //for now lets add a boolean
                typeMap.addTypeMappingStatus(partQName, Boolean.TRUE);
            }
        }
    }


    /**
     * Look for a given schema type given the schema type Qname
     *
     * @param schemaMap
     * @return null if the schema is not found
     */
    private static XmlSchemaType findSchemaType(Map<String,XmlSchema> schemaMap, QName schemaTypeName) {
        //find the schema
        XmlSchema schema = schemaMap.get(schemaTypeName.getNamespaceURI());
        if (schema != null) {
            return schema.getTypeByName(schemaTypeName);
        }
        return null;
    }

    /**
     * populate parameters from the user
     *
     * @param options
     */
    private static void populateUserparameters(CompilerOptions options, CodeGenConfiguration configuration) {
        Map<Object,Object> propertyMap = configuration.getProperties();
        if (propertyMap.containsKey(SchemaConstants.SchemaCompilerArguments.WRAP_SCHEMA_CLASSES)) {
            if (Boolean.valueOf(
                    propertyMap.get(SchemaConstants.SchemaCompilerArguments.WRAP_SCHEMA_CLASSES).toString()).
                    booleanValue()) {
                options.setWrapClasses(true);
            } else {
                options.setWrapClasses(false);
            }
        }

        if (propertyMap.containsKey(SchemaConstants.SchemaCompilerArguments.WRITE_SCHEMA_CLASSES)) {
            if (Boolean.valueOf(
                    propertyMap.get(SchemaConstants.SchemaCompilerArguments.WRITE_SCHEMA_CLASSES).toString()).
                    booleanValue()) {
                options.setWriteOutput(true);
            } else {
                options.setWriteOutput(false);
            }
        }


        // add the custom package name
        if (propertyMap.containsKey(SchemaConstants.SchemaCompilerArguments.PACKAGE)) {
            String packageName = (String) propertyMap.get(SchemaConstants.SchemaCompilerArguments.PACKAGE);
            if (packageName != null || !"".equals(packageName)) {
                options.setPackageName(packageName);
            }

        }

        // set the package namespace to uri details
        options.setNs2PackageMap(configuration.getUri2PackageNameMap());

        //add custom mapper package name
        if (propertyMap.containsKey(SchemaConstants.SchemaCompilerArguments.MAPPER_PACKAGE)) {
            String packageName = (String) propertyMap.get(SchemaConstants.SchemaCompilerArguments.MAPPER_PACKAGE);
            if (packageName != null || !"".equals(packageName)) {
                options.setMapperClassPackage(packageName);
            }

        }

        if (propertyMap.containsKey(SchemaConstants.SchemaCompilerArguments.OFF_STRICT_VALIDATION)){
            options.setOffStrictValidation(true);
        }

        if (propertyMap.containsKey(SchemaConstants.SchemaCompilerArguments.USE_WRAPPER_CLASSES)){
            options.setUseWrapperClasses(true);
        }

        //set helper mode
        //this becomes effective only if the classes are unpacked
        if (!options.isWrapClasses()) {
            if (propertyMap.containsKey(SchemaConstants.SchemaCompilerArguments.HELPER_MODE)) {
                options.setHelperMode(true);
            }
        }
    }


    /**
     * populate the default options - called before the applying of user parameters
     *
     * @param options
     */
    private static void populateDefaultOptions(CompilerOptions options,
                                               CodeGenConfiguration configuration) {
        //create the output directory
        File outputDir = configuration.isFlattenFiles() ?
                configuration.getOutputLocation() :
                new File(configuration.getOutputLocation(), configuration.getSourceLocation());

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        /// these options need to be taken from the command line
        options.setOutputLocation(outputDir);
        options.setNs2PackageMap(configuration.getUri2PackageNameMap() == null ?
                new HashMap<String,String>() :
                configuration.getUri2PackageNameMap());

        //default setting is to set the wrap status depending on whether it's
        //the server side or the client side
        if (configuration.isServerSide()) {
            //for the serverside we generate unwrapped  by default
            options.setWrapClasses(false);
            //for the serverside we write the output by default
            options.setWriteOutput(true);
        } else {
            // for the client let the users preference be the word here
            options.setWrapClasses(configuration.isPackClasses());
            //for the client side the default setting is not to write the
            //output
            options.setWriteOutput(!configuration.isPackClasses());
        }

        if (configuration.isGenerateAll()) {
            options.setGenerateAll(true);
        }

        if (configuration.isBackwordCompatibilityMode()) {
            options.setBackwordCompatibilityMode(true);
        }

        if (configuration.isSuppressPrefixesMode()) {
            options.setSuppressPrefixesMode(true);
        }
    }

}
