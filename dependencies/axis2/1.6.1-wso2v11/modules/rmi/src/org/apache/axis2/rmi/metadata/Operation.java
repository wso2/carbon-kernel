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

package org.apache.axis2.rmi.metadata;

import org.apache.axis2.rmi.Configurator;
import org.apache.axis2.rmi.exception.MetaDataPopulateException;
import org.apache.axis2.rmi.exception.SchemaGenerationException;
import org.apache.axis2.rmi.metadata.xml.XmlElement;
import org.apache.axis2.rmi.metadata.xml.XmlImport;
import org.apache.axis2.rmi.metadata.xml.XmlSchema;
import org.apache.axis2.rmi.metadata.xml.XmlType;
import org.apache.axis2.rmi.metadata.xml.impl.XmlElementImpl;
import org.apache.axis2.rmi.metadata.xml.impl.XmlTypeImpl;
import org.apache.axis2.rmi.util.Constants;

import javax.xml.namespace.QName;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Operation {

    /**
     * javaMethod name
     */
    private String name;

    /**
     * namespace to which javaMethod belongs
     * most of the time this is same as the service namespace
     */
    private String namespace;
    /**
     * service class method for this javaMethod
     */
    private Method javaMethod;

    /**
     * input parameters list for this method(javaMethod)
     * this consists of a list of Parameter objects
     */
    private List inputParameters;

    /**
     * return type of the javaMethod
     */
    private Parameter outputParameter;

    /**
     * generated XmlElement for the input
     */
    private XmlElement inputElement;

    /**
     * generated XmlElement for the output
     */
    private XmlElement outPutElement;



    /**
     * default constructor
     */
    public Operation() {
        this.inputParameters = new ArrayList();

    }

    /**
     * constructor with the java method for this javaMethod
     *
     * @param operation
     */
    public Operation(Method operation) {
        this();
        this.javaMethod = operation;
    }

    public void populateMetaData(Configurator configurator,
                                 Map processedTypeMap,
                                 Map exceptionClassToParameterMap)
            throws MetaDataPopulateException {
        // set the name. namespace should already have set by the service
        this.name = this.javaMethod.getName();

        //populate input parameters
        Class[] inputTypes = this.javaMethod.getParameterTypes();
        Parameter parameter;
        for (int i = 0; i < inputTypes.length; i++) {
            parameter = new Parameter(inputTypes[i],"param" + i);
            parameter.populateMetaData(configurator, processedTypeMap);
            this.inputParameters.add(parameter);
        }

        // populate output parameter
        Class returnType = this.javaMethod.getReturnType();
        if (!returnType.equals(void.class)) {
            // i.e. we have a return type
            this.outputParameter = new Parameter(returnType, "return");
            this.outputParameter.populateMetaData(configurator, processedTypeMap);
        }

        // populate exception data
        Class[] exceptionType = this.javaMethod.getExceptionTypes();
        String exceptionName;
        String namespace;
        for (int i=0;i<exceptionType.length;i++){
            exceptionName = exceptionType[i].getName();
            exceptionName = exceptionName.substring(exceptionName.lastIndexOf(".") + 1);
            namespace = configurator.getNamespace(exceptionType[i].getPackage().getName());
            parameter = new Parameter(exceptionType[i],exceptionName,namespace);
            parameter.populateMetaData(configurator,processedTypeMap);
            if (!exceptionClassToParameterMap.containsKey(exceptionType[i])){
                exceptionClassToParameterMap.put(exceptionType[i],parameter);
            }
        }
    }

    public void generateSchema(Configurator configurator,
                               Map schemaMap,
                               Map exceptionClassToParameterMap)
            throws SchemaGenerationException {
        // here we have to generate the input element and out put elements
        // putting the other relavent schema parts to the schemaMap

        // get the schema to add the complex type
        if (schemaMap.get(this.namespace) == null) {
            // create a new namespace for this schema
            schemaMap.put(this.namespace, new XmlSchema(this.namespace));
        }
        XmlSchema xmlSchema = (XmlSchema) schemaMap.get(this.namespace);


        if (!configurator.isBare()) {

            // generating the input element
            this.inputElement = new XmlElementImpl(false);
            this.inputElement.setName(this.name);
            this.inputElement.setNamespace(this.namespace);
            this.inputElement.setTopElement(true);
            xmlSchema.addElement(this.inputElement);

            // set the complex type for this element
            XmlType xmlType = new XmlTypeImpl();
            xmlType.setAnonymous(true);
            xmlType.setSimpleType(false);

            Parameter parameter;
            for (Iterator iter = this.inputParameters.iterator(); iter.hasNext();) {
                parameter = (Parameter) iter.next();
                if (!parameter.isSchemaGenerated()){
                   parameter.generateSchema(configurator,schemaMap);
                }
                parameter.getElement().setTopElement(false);
                xmlType.addElement(parameter.getElement());
                QName elementTypeQName = parameter.getElement().getType().getQname();
                if (!xmlSchema.containsNamespace(elementTypeQName.getNamespaceURI())) {
                    // if the element namespace does not exists we have to add it
                    if (!elementTypeQName.getNamespaceURI().equals(Constants.URI_2001_SCHEMA_XSD)) {
                        XmlImport xmlImport = new XmlImport(elementTypeQName.getNamespaceURI());
                        xmlSchema.addImport(xmlImport);
                    }
                    xmlSchema.addNamespace(elementTypeQName.getNamespaceURI());
                }
            }

            this.inputElement.setType(xmlType);

            // generate the output Element
            this.outPutElement = new XmlElementImpl(false);
            this.outPutElement.setName(this.name + "Response");
            this.outPutElement.setNamespace(this.namespace);
            this.outPutElement.setTopElement(true);
            xmlSchema.addElement(this.outPutElement);

            xmlType = new XmlTypeImpl();
            xmlType.setAnonymous(true);
            xmlType.setSimpleType(false);
            if (this.outputParameter != null) {
                if (!this.outputParameter.isSchemaGenerated()){
                    this.outputParameter.generateSchema(configurator,schemaMap);
                }
                xmlType.addElement(this.outputParameter.getElement());
                QName elementTypeQName = this.outputParameter.getElement().getType().getQname();
                if (!xmlSchema.containsNamespace(elementTypeQName.getNamespaceURI())) {
                    // if the element namespace does not exists we have to add it
                    if (!elementTypeQName.getNamespaceURI().equals(Constants.URI_2001_SCHEMA_XSD)) {
                        XmlImport xmlImport = new XmlImport(elementTypeQName.getNamespaceURI());
                        xmlSchema.addImport(xmlImport);
                    }
                    xmlSchema.addNamespace(elementTypeQName.getNamespaceURI());
                }
            }
            this.outPutElement.setType(xmlType);

        } else {
            //TODO: generate the code for bare case
        }

    }


    public Parameter getOutputParameter() {
        return outputParameter;
    }

    public void setOutputParameter(Parameter outputParameter) {
        this.outputParameter = outputParameter;
    }

    public XmlElement getInputElement() {
        return inputElement;
    }

    public void setInputElement(XmlElement inputElement) {
        this.inputElement = inputElement;
    }

    public XmlElement getOutPutElement() {
        return outPutElement;
    }

    public void setOutPutElement(XmlElement outPutElement) {
        this.outPutElement = outPutElement;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Method getJavaMethod() {
        return javaMethod;
    }

    public void setJavaMethod(Method javaMethod) {
        this.javaMethod = javaMethod;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public List getInputParameters() {
        return inputParameters;
    }

    public void setInputParameters(List inputParameters) {
        this.inputParameters = inputParameters;
    }

}
