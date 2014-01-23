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
import org.apache.axis2.rmi.metadata.impl.TypeImpl;
import org.apache.axis2.rmi.metadata.xml.XmlImport;
import org.apache.axis2.rmi.metadata.xml.XmlSchema;
import org.apache.axis2.rmi.util.Constants;

import javax.xml.namespace.QName;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class Service {

    /**
     * name of the service
     */
    private String name;
    private String namespace;
    /**
     * java class for which we going to create the service
     */
    private Class javaClass;

    /**
     * this list contains a List of Operation objects representing
     * operations of the java class
     */
    private List operations;

    private Map processedTypeMap;
    private Configurator configurator;
    private Map schemaMap;

    /**
     * this map keeps the exception class name and exception relavent parameter
     * used in serialization. we need to keep the message name unique within the
     * service. so exceptionclass map should be global
     */
    private Map exceptionClassToParameterMap;

    /**
     * this map keeps the exception QName and exception relavent parameter
     */
    private Map exceptionQNameToParameterMap;

    /**
     * default constructor
     */
    public Service() {
        this.operations = new ArrayList();
        this.exceptionClassToParameterMap = new HashMap();
        this.exceptionQNameToParameterMap = new HashMap();
    }

    /**
     * constracutor with the java class name
     *
     * @param javaClass
     */
    public Service(Class javaClass, Configurator configurator) {
        this();
        this.javaClass = javaClass;
        this.configurator = configurator;
    }


    /**
     * this method suppose to fill all the operations data using the java
     * class given. here we assume that the javaClass is properly set.
     */
    public void populateMetaData() throws MetaDataPopulateException {
        // set the service name using the java class name
        this.name = javaClass.getName();
        // remove the package part
        this.name = this.name.substring(this.name.lastIndexOf(".") + 1);
        this.namespace = configurator.getNamespace(javaClass.getPackage().getName());
        this.processedTypeMap = new HashMap();

        // populate operations
        //TODO: this method returns uninherited methods find a way to get them as well
        Method[] javaMethods = this.javaClass.getDeclaredMethods();
        Method javaMethod;
        Operation operation;
        for (int i = 0; i < javaMethods.length; i++) {
            javaMethod = javaMethods[i];
            // we generate oprations only from the public methods
            //TODO : remove overload methods
            if (this.javaClass.isInterface() || (Modifier.isPublic(javaMethod.getModifiers()) &&
                    !Modifier.isAbstract(javaMethod.getModifiers()))) {
                operation = new Operation(javaMethod);
                operation.setNamespace(this.namespace);
                operation.populateMetaData(configurator, this.processedTypeMap, this.exceptionClassToParameterMap);
                this.operations.add(operation);
            }
        }

        // add the extension classes if they have not already populated
        Class extensionClass = null;
        Type extensionType = null;
        for (Iterator iter = this.configurator.getExtensionClasses().iterator(); iter.hasNext();) {
            extensionClass = (Class) iter.next();
            if (!this.processedTypeMap.containsKey(extensionClass)) {
                extensionType = new TypeImpl(extensionClass);
                this.processedTypeMap.put(extensionClass, extensionType);
                extensionType.populateMetaData(this.configurator, this.processedTypeMap);
            }
        }
    }

    public void generateSchema() throws SchemaGenerationException {
        //first we have to generate the input and output elements
        // to operations

        // create schema map object and add the target namespace object
        this.schemaMap = new HashMap();
        this.schemaMap.put(this.namespace, new XmlSchema(this.namespace));
        Operation operation;
        for (Iterator iter = this.operations.iterator(); iter.hasNext();) {
            operation = (Operation) iter.next();
            operation.generateSchema(configurator,
                    this.schemaMap,
                    this.exceptionClassToParameterMap);
        }

        // add exception class elements to the schema
        generateSchemaForExceptionParameters();

        // generate the schema for other extension classes
        Class extensionClass = null;
        Type extensionType = null;
        for (Iterator iter = this.configurator.getExtensionClasses().iterator(); iter.hasNext();) {
            extensionClass = (Class) iter.next();
            extensionType = (Type) this.processedTypeMap.get(extensionClass);
            if (!extensionType.isSchemaGenerated()) {
                extensionType.generateSchema(this.configurator, this.schemaMap);
            }
        }

    }

    private void generateSchemaForExceptionParameters() throws SchemaGenerationException {
        Parameter parameter;
        for (Iterator iter = this.exceptionClassToParameterMap.values().iterator(); iter.hasNext();) {
            parameter = (Parameter) iter.next();
            if (!parameter.isSchemaGenerated()) {
                parameter.generateSchema(configurator, schemaMap);
            }
            QName elementTypeQName = parameter.getElement().getType().getQname();
            // get the schema to add the complex type
            if (schemaMap.get(elementTypeQName.getNamespaceURI()) == null) {
                // create a new namespace for this schema
                schemaMap.put(elementTypeQName.getNamespaceURI(), new XmlSchema(elementTypeQName.getNamespaceURI()));
            }
            XmlSchema xmlSchema = (XmlSchema) schemaMap.get(elementTypeQName.getNamespaceURI());
            if (!xmlSchema.containsNamespace(elementTypeQName.getNamespaceURI())) {
                if (!elementTypeQName.getNamespaceURI().equals(Constants.URI_2001_SCHEMA_XSD)) {
                    XmlImport xmlImport = new XmlImport(elementTypeQName.getNamespaceURI());
                    xmlSchema.addImport(xmlImport);
                }
                xmlSchema.addNamespace(elementTypeQName.getNamespaceURI());
            }
            parameter.getElement().setTopElement(true);
            xmlSchema.addElement(parameter.getElement());
            this.exceptionQNameToParameterMap.put(elementTypeQName, parameter);
        }
    }

    /**
     * this method returns the operation for the given name if found
     * otherwise return null
     *
     * @param operationName
     * @return operation
     */

    public Operation getOperation(String operationName) {
        Operation operation = null;
        boolean operationFound = false;
        for (Iterator iter = this.operations.iterator(); iter.hasNext();) {
            operation = (Operation) iter.next();
            if (operation.getName().equals(operationName)) {
                operationFound = true;
                break;
            }
        }
        if (operationFound) {
            return operation;
        } else {
            return null;
        }
    }

    public Parameter getExceptionParameter(Class exceptionClass) {
        return (Parameter) this.exceptionClassToParameterMap.get(exceptionClass);
    }

    public Parameter getExceptionParameter(QName exceptionElementQname) {
        return (Parameter) this.exceptionQNameToParameterMap.get(exceptionElementQname);
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class getJavaClass() {
        return javaClass;
    }

    public void setJavaClass(Class javaClass) {
        this.javaClass = javaClass;
    }

    public List getOperations() {
        return operations;
    }

    public void setOperations(List operations) {
        this.operations = operations;
    }

    public Map getProcessedTypeMap() {
        return processedTypeMap;
    }

    public void setProcessedTypeMap(Map processedTypeMap) {
        this.processedTypeMap = processedTypeMap;
    }

    public Configurator getConfigurator() {
        return configurator;
    }

    public void setConfigurator(Configurator configurator) {
        this.configurator = configurator;
    }

    public Map getSchemaMap() {
        return schemaMap;
    }

    public void setSchemaMap(Map schemaMap) {
        this.schemaMap = schemaMap;
    }

    public Map getExceptionClassToParameterMap() {
        return exceptionClassToParameterMap;
    }

    public void setExceptionClassToParameterMap(Map exceptionClassToParameterMap) {
        this.exceptionClassToParameterMap = exceptionClassToParameterMap;
    }


}
