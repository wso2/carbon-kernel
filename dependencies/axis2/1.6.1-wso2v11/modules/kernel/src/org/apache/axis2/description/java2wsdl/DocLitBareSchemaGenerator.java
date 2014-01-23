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
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.jsr181.JSR181Helper;
import org.apache.axis2.jsr181.WebMethodAnnotation;
import org.apache.axis2.jsr181.WebResultAnnotation;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.utils.NamespaceMap;

import javax.xml.namespace.QName;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class DocLitBareSchemaGenerator extends DefaultSchemaGenerator {

    private static final Log log = LogFactory.getLog(DocLitBareSchemaGenerator.class);
    private HashMap<String,Method> processedParameters = new LinkedHashMap<String,Method>();

    public DocLitBareSchemaGenerator(ClassLoader loader,
                                     String className,
                                     String schematargetNamespace,
                                     String schematargetNamespacePrefix,
                                     AxisService service) throws Exception {
        super(loader, className, schematargetNamespace,
                schematargetNamespacePrefix, service);
    }

    @Override
    protected Method[] processMethods(Method[] declaredMethods) throws Exception {
        ArrayList<Method> list = new ArrayList<Method>();
        //short the elements in the array
        Arrays.sort(declaredMethods , new MathodComparator());

        // since we do not support overload
        HashMap<String,Method> uniqueMethods = new LinkedHashMap<String,Method>();
        XmlSchemaComplexType methodSchemaType;
        XmlSchemaSequence sequence;

        for (Method jMethod : declaredMethods) {
            if (jMethod.isBridge()) {
                continue;
            }
            WebMethodAnnotation methodAnnon = JSR181Helper.INSTANCE.getWebMethodAnnotation(jMethod);
            if (methodAnnon != null) {
                if (methodAnnon.isExclude()) {
                    continue;
                }
            }
            String methodName = jMethod.getName();
            // no need to think abt this method , since that is system
            // config method
            if (excludeMethods.contains(methodName)) {
                continue;
            }

            if (uniqueMethods.get(methodName) != null) {
                log.warn("We don't support method overloading. Ignoring [" +
                        methodName + "]");
                continue;
            }

           if (!Modifier.isPublic(jMethod.getModifiers())) {
                // no need to generate Schema for non public methods
                continue;
            }

            boolean addToService = false;
            AxisOperation axisOperation = service.getOperation(new QName(methodName));
            if (axisOperation == null) {
                axisOperation = Utils.getAxisOperationForJmethod(jMethod);
                if (WSDL2Constants.MEP_URI_ROBUST_IN_ONLY.equals(
                        axisOperation.getMessageExchangePattern())){
                    AxisMessage outMessage = axisOperation.getMessage(
                            WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                    if (outMessage !=null ){
                        outMessage.setName(methodName + RESULT);
                    }
                }
                addToService = true;
            }

            // Maintain a list of methods we actually work with
            list.add(jMethod);
            processException(jMethod,axisOperation);
            uniqueMethods.put(methodName, jMethod);
            //create the schema type for the method wrapper

            uniqueMethods.put(methodName, jMethod);
            Class<?>[] paras = jMethod.getParameterTypes();
            String parameterNames[] = methodTable.getParameterNames(methodName);
            AxisMessage inMessage = axisOperation.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            if (inMessage != null) {
                inMessage.setName(methodName + "RequestMessage");
            }
            Annotation[][] parameterAnnotation = jMethod.getParameterAnnotations();
            if (paras.length > 1) {
                sequence = new XmlSchemaSequence();
                methodSchemaType = createSchemaTypeForMethodPart(methodName);
                methodSchemaType.setParticle(sequence);
                inMessage.setElementQName(typeTable.getQNamefortheType(methodName));
                service.addMessageElementQNameToOperationMapping(methodSchemaType.getQName(),
                        axisOperation);
                inMessage.setPartName(methodName);
                for (int j = 0; j < paras.length; j++) {
                    Class<?> methodParameter = paras[j];
                    String parameterName = getParameterName(parameterAnnotation, j, parameterNames);
                    if (generateRequestSchema(methodParameter , parameterName,jMethod, sequence)) {
                        break;
                    }
                }
            } else if (paras.length == 1) {
                if (paras[0].isArray()) {
                    sequence = new XmlSchemaSequence();

                    methodSchemaType = createSchemaTypeForMethodPart(methodName);
                    methodSchemaType.setParticle(sequence);
                    Class<?> methodParameter = paras[0];
                    inMessage.setElementQName(typeTable.getQNamefortheType(methodName));
                    service.addMessageElementQNameToOperationMapping(methodSchemaType.getQName(),
                            axisOperation);
                    inMessage.setPartName(methodName);
                    String parameterName = getParameterName(parameterAnnotation, 0, parameterNames);
                    if (generateRequestSchema(methodParameter , parameterName,jMethod, sequence)) {
                        break;
                    }
                } else {
                    String parameterName = getParameterName(parameterAnnotation, 0, parameterNames);
                    Class<?> methodParameter = paras[0];
                    Method processMethod = processedParameters.get(parameterName);
                    if (processMethod != null) {
                        throw new AxisFault("Inavalid Java class," +
                                " there are two methods [" + processMethod.getName() + " and " +
                                jMethod.getName() + " ]which have the same parameter names");
                    } else {
                        processedParameters.put(parameterName, jMethod);
                        generateSchemaForType(null, methodParameter, parameterName);
                        inMessage.setElementQName(typeTable.getQNamefortheType(parameterName));
                        inMessage.setPartName(parameterName);
                        inMessage.setWrapped(false);
                        service.addMessageElementQNameToOperationMapping(typeTable.getQNamefortheType(parameterName),
                                axisOperation);
                    }
                }
            }

            // for its return type
            Class<?> returnType = jMethod.getReturnType();

            if (!"void".equals(jMethod.getReturnType().getName())) {
                AxisMessage outMessage = axisOperation.getMessage(
                        WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                if (returnType.isArray()) {
                    methodSchemaType =
                            createSchemaTypeForMethodPart(jMethod.getName() + RESULT);
                    sequence = new XmlSchemaSequence();
                    methodSchemaType.setParticle(sequence);
                    WebResultAnnotation returnAnnon = JSR181Helper.INSTANCE.getWebResultAnnotation(jMethod);
                    String returnName = "return";
                    if (returnAnnon != null) {
                        returnName = returnAnnon.getName();
                        if (returnName != null && !"".equals(returnName)) {
                            returnName = "return";
                        }
                    }
                    if (nonRpcMethods.contains(methodName)) {
                        generateSchemaForType(sequence, null, returnName);
                    } else {
                        generateSchemaForType(sequence, returnType, returnName);
                    }
                } else {
                    generateSchemaForType(null, returnType, methodName + RESULT);
                    outMessage.setWrapped(false);
                }
                outMessage.setElementQName(typeTable.getQNamefortheType(methodName + RESULT));
                outMessage.setName(methodName + "ResponseMessage");
                outMessage.setPartName(methodName + RESULT);
                service.addMessageElementQNameToOperationMapping(
                        typeTable.getQNamefortheType(methodName + RESULT),
                        axisOperation);
            }
            if (addToService) {
                service.addOperation(axisOperation);
            }
        }
        return list.toArray(new Method[list.size()]);
    }


    private boolean generateRequestSchema(Class<?> methodParameter,
                                          String parameterName,
                                          Method jMethod,
                                          XmlSchemaSequence sequence) throws Exception {
        if (nonRpcMethods.contains(jMethod.getName())) {
            generateSchemaForType(sequence, null, jMethod.getName());
            return true;
        } else {
            generateSchemaForType(sequence, methodParameter, parameterName);
        }
        return false;
    }

    private QName generateSchemaForType(XmlSchemaSequence sequence, Class<?> type, String partName)
            throws Exception {

        boolean isArrayType = false;
        if (type != null) {
            isArrayType = type.isArray();
        }
        if (isArrayType) {
            type = type.getComponentType();
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
        QName schemaTypeName = typeTable.getSimpleSchemaTypeName(classTypeName);
        if (schemaTypeName == null && type != null) {
            schemaTypeName = generateSchema(type);
            addContentToMethodSchemaType(sequence,
                    schemaTypeName,
                    partName,
                    isArrayType);
            String schemaNamespace = resolveSchemaNamespace(getQualifiedName(type.getPackage()));
            addImport(getXmlSchema(schemaNamespace), schemaTypeName);
            if(sequence==null){
                 generateSchemaForSingleElement(schemaTypeName, partName, isArrayType);
            }
        } else {
            if (sequence == null) {
                generateSchemaForSingleElement(schemaTypeName, partName, isArrayType);
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

    protected void generateSchemaForSingleElement(QName schemaTypeName,
                                                  String paraName,
                                                  boolean isArray) throws Exception {
        XmlSchemaElement elt1 = new XmlSchemaElement();
        elt1.setName(paraName);
        elt1.setSchemaTypeName(schemaTypeName);
        elt1.setNillable(true);
        QName elementName =
                new QName(schemaTargetNameSpace, paraName, schema_namespace_prefix);
        elt1.setQName(elementName);
        XmlSchema xmlSchema = getXmlSchema(schemaTargetNameSpace);
        xmlSchema.getElements().add(elementName, elt1);
        xmlSchema.getItems().add(elt1);
        typeTable.addComplexSchema(paraName, elementName);
    }

    /**
     * Generate schema construct for given type
     *
     * @param javaType
     */
    private XmlSchemaComplexType createSchemaTypeForMethodPart(String localPartName) {
        XmlSchema xmlSchema = getXmlSchema(schemaTargetNameSpace);
        QName elementName =
                new QName(this.schemaTargetNameSpace, localPartName, this.schema_namespace_prefix);

        XmlSchemaComplexType complexType = getComplexTypeForElement(xmlSchema, elementName);
        if (complexType == null) {
            complexType = new XmlSchemaComplexType(xmlSchema);

            XmlSchemaElement globalElement = new XmlSchemaElement();
            globalElement.setSchemaType(complexType);
            globalElement.setName(localPartName);
            globalElement.setQName(elementName);
            xmlSchema.getItems().add(globalElement);
            xmlSchema.getElements().add(elementName, globalElement);
        }
        typeTable.addComplexSchema(localPartName, elementName);

        return complexType;
    }

    // TODO: explain why we need to override the method if the implementation is identical!
    @Override
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

}
