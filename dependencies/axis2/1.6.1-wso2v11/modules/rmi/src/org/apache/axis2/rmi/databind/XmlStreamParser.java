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

package org.apache.axis2.rmi.databind;

import org.apache.axis2.rmi.Configurator;
import org.apache.axis2.rmi.exception.MetaDataPopulateException;
import org.apache.axis2.rmi.exception.SchemaGenerationException;
import org.apache.axis2.rmi.exception.XmlParsingException;
import org.apache.axis2.rmi.metadata.AttributeField;
import org.apache.axis2.rmi.metadata.ElementField;
import org.apache.axis2.rmi.metadata.Operation;
import org.apache.axis2.rmi.metadata.Parameter;
import org.apache.axis2.rmi.metadata.Type;
import org.apache.axis2.rmi.metadata.impl.TypeImpl;
import org.apache.axis2.rmi.types.MapType;
import org.apache.axis2.rmi.util.Constants;
import org.apache.axis2.rmi.util.JavaTypeToQNameMap;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class XmlStreamParser {

    private Configurator configurator;
    private Map qNameToTypeMap;
    private SimpleTypeHandler simpleTypeHandler;
    private Class simpleTypeHandlerClass;
    private boolean readerPriorAccess;
    private boolean priorAccessDetermined = false;

    public XmlStreamParser(Map processedTypeMap,
                           Configurator configurator,
                           Map schemaMap) {
        this.configurator = configurator;
        this.simpleTypeHandler = this.configurator.getSimpleTypeHandler();
        this.simpleTypeHandlerClass = this.simpleTypeHandler.getClass();
        try {
            populateQNameToTypeMap(processedTypeMap, schemaMap);
        } catch (MetaDataPopulateException e) {
            // TODO: what to do this exceptions is not going to happen
        } catch (SchemaGenerationException e) {
            // TODO: what to do this exceptions is not going to happen
        }
    }

    private void populateQNameToTypeMap(Map processedTypeMap, Map schemaMap)
            throws MetaDataPopulateException,
            SchemaGenerationException {
        // first add all the proceced type map values
        Set defaultTypeMapKeys = JavaTypeToQNameMap.getKeys();
        Class typeClass;
        for (Iterator iter = defaultTypeMapKeys.iterator(); iter.hasNext();) {
            typeClass = (Class) iter.next();
            if (!processedTypeMap.containsKey(typeClass)) {
                Type newType = new TypeImpl(typeClass);
                processedTypeMap.put(typeClass, newType);
                newType.populateMetaData(this.configurator, processedTypeMap);
                newType.generateSchema(this.configurator, schemaMap);
            }
        }
        this.qNameToTypeMap = new HashMap();
        Type type = null;
        for (Iterator iter = processedTypeMap.values().iterator(); iter.hasNext();) {
            type = (Type) iter.next();
            this.qNameToTypeMap.put(type.getXmlType().getQname(), type);
        }

    }

    public Object getOutputObject(XMLStreamReader reader_in,
                                  Operation operation)
            throws XMLStreamException, XmlParsingException {
        Object returnObject = null;
        
        determinePriorAccess(reader_in);
        StatefulXMLStreamReader reader = getStatefulReader(reader_in);
        
        advanceToFirstElement(reader);

        // first check whether we have got the correct input element or not
        if (reader.getLocalName().equals(operation.getOutPutElement().getName()) &&
                reader.getNamespaceURI().equals(operation.getOutPutElement().getNamespace())) {
            reader.next();
            if (operation.getOutputParameter() != null) {
                // i.e this is not a void return type
                returnObject = getObjectForParameter(reader, operation.getOutputParameter());
            }
        }
        return returnObject;
    }

    public Object[] getInputParameters(XMLStreamReader reader_in,
                                       Operation operation)
            throws XMLStreamException,
            XmlParsingException {

        determinePriorAccess(reader_in);
        StatefulXMLStreamReader reader = getStatefulReader(reader_in);
        
        List returnObjects = new ArrayList();
        advanceToFirstElement(reader);

        // first check whether we have got the correct input element or not
        if (reader.getLocalName().equals(operation.getInputElement().getName()) &&
                reader.getNamespaceURI().equals(operation.getInputElement().getNamespace())) {
            // point the reader to parameters
            reader.next();
            Parameter parameter = null;
            List inputParameters = operation.getInputParameters();
            QName parameterQName = null;
            int startDepth = reader.getDepth();
            
            for (Iterator iter = inputParameters.iterator(); iter.hasNext();) {
                parameter = (Parameter) iter.next();
                parameterQName = new QName(parameter.getNamespace(), parameter.getName());
                returnObjects.add(getObjectForParameter(reader, parameter));
                // if the reader is at the end of this parameter
                // then we move it to next element.
                if (reader.isEndElement() && reader.getName().equals(parameterQName)
                        && (readerPriorAccess || reader.getDepth() == startDepth - 1)){
                    reader.next();
                }
            }

        } else {
            throw new XmlParsingException("Unexpected Subelement " + reader.getName() + " but " +
                    "expected " + operation.getInputElement().getName());
        }
        return returnObjects.toArray();
    }

    /**
     * parameter has the same logic as the attribute. so reader pre and post conditions same.
     *
     * @param reader
     * @param parameter
     * @return
     * @throws XMLStreamException
     * @throws XmlParsingException
     */

    public Object getObjectForParameter(XMLStreamReader reader_in,
                                        Parameter parameter)
            throws XMLStreamException,
            XmlParsingException {

        QName parameterQName = new QName(parameter.getNamespace(), parameter.getName());
        
        determinePriorAccess(reader_in);
        StatefulXMLStreamReader reader = getStatefulReader(reader_in);
        
        return getObjectForElement(reader,
                parameterQName,
                parameter.getType(),
                parameter.isArray(),
                parameter.getElement().isNillable(),
                parameter.getElement().isMinOccurs0(),
                parameter.getClassType(),
                parameter.getJavaClass());

    }

    /**
     * when calls to this method reader must point to the start of the type.
     * if it is a simple type it points the the text
     * if it is a complex type it points to the start element of the first attribute. and we returning
     * from the method it should point to the end element of the last parameter.
     *
     * @param reader
     * @param type
     * @return
     * @throws XMLStreamException
     * @throws XmlParsingException
     */


    public Object getObjectForType(XMLStreamReader reader_in,
                                   Type type)
            throws XMLStreamException,
            XmlParsingException {

        try {
            determinePriorAccess(reader_in);
            StatefulXMLStreamReader reader = getStatefulReader(reader_in);
            
            Object returnObject = null;
            if (type.getXmlType().isSimpleType()) {
                // this is a simple known type for us
                // constructor should be able to invoke with the string.
                if (type.getJavaClass().equals(Object.class)) {
                    returnObject = new Object();
                } else {
                    // find the object for this string using converter util classes
                    returnObject = getSimpleTypeObject(type, reader, reader.getText());
                }
            } else {
                // first we have to point to the reader to the beginning for the element
                while (!reader.isStartElement() && !reader.isEndElement()) {
                    reader.next();
                }
                int startDepth = reader.getDepth();
                // this is a complex type
                returnObject = type.getJavaClass().newInstance();
                // we have to get all the elementField and populate them
                List elementFields = type.getAllElementFields();
                ElementField elementField;
                Object elementFieldObject;
                QName elementFieldQName = null;

                for (Iterator iter = elementFields.iterator(); iter.hasNext();) {
                    elementField = (ElementField) iter.next();
                    elementFieldObject = getObjectForElementField(reader, elementField);
                    elementFieldQName = new QName(elementField.getNamespace(), elementField.getName());
                    if (elementFieldObject != null) {
                        elementField.getSetterMethod().invoke(returnObject, new Object[]{elementFieldObject});
                    }
                    // if the reader is at the end of this elementField
                    // then we move it to next element.
                    if (reader.isEndElement() && reader.getName().equals(elementFieldQName) 
                            && (readerPriorAccess || reader.getDepth() == startDepth - 1)){
                        reader.next();
                    }

                }

            }
            return returnObject;
        } catch (InstantiationException e) {
            throw new XmlParsingException("Constructor invoking exception for type " + type.getName());
        } catch (IllegalAccessException e) {
            throw new XmlParsingException("Constructor invoking exception for type " + type.getName());
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new XmlParsingException("Constructor invoking exception for type " + type.getName());
        }

    }

    private Object getSimpleTypeObject(Type type, StatefulXMLStreamReader reader, String value)
            throws  XmlParsingException {
        Object returnObject;
        String methodName = null;
        try {
            methodName = getMethodName(type.getJavaClass().getName());
            Method  methodToInvoke = this.simpleTypeHandlerClass.getMethod(methodName,new Class[]{String.class});
            returnObject = methodToInvoke.invoke(this.simpleTypeHandler,new Object[]{value});
        } catch (NoSuchMethodException e) {
            throw new XmlParsingException("Can not invoke the converter util class method " + methodName, e);
        } catch (IllegalAccessException e) {
            throw new XmlParsingException("Can not invoke the converter util class method " + methodName, e);
        } catch (InvocationTargetException e) {
            throw new XmlParsingException("Can not invoke the converter util class method " + methodName, e);
        }
        return returnObject;
    }

    private String getMethodName(String className) {
        // first handle some exceptional classes
        if (className.equals(Integer.class.getName())) {
            return "convertToInt";
        } else if (className.equals(Calendar.class.getName())) {
            return "convertToDateTime";
        }
        if (className.indexOf(".") > -1) {
            className = className.substring(className.lastIndexOf(".") + 1);
        } else {
            // this is a primitive type class
            // so capitalize the first letter
            className = className.substring(0, 1).toUpperCase() + className.substring(1);
        }
        return "convertTo" + className;
    }

    /**
     * give the relevant object for elementField.
     *
     * @param reader
     * @param elementField
     * @return
     * @throws XMLStreamException
     * @throws XmlParsingException
     */

    private Object getObjectForElementField(StatefulXMLStreamReader reader,
                                            ElementField elementField)
            throws XMLStreamException,
            XmlParsingException {
        QName elementFieldQName = new QName(elementField.getNamespace(), elementField.getName());
        return getObjectForElement(reader,
                elementFieldQName,
                elementField.getType(),
                elementField.isArray(),
                elementField.getElement().isNillable(),
                elementField.getElement().isMinOccurs0(),
                elementField.getClassType(),
                elementField.getPropertyDescriptor().getPropertyType());

    }

    /**
     * element parser corresponds to parse the element (for an attribute or parameter) correctly.
     * when calling to this method reader must point to the start element of the element. and when returning
     * it should point to the corresponding end element of the element.
     * if the element is an array it may point to the start element of the next element.
     *
     * @param reader
     * @param elementQName
     * @param elementType
     * @param isArray
     * @param classType
     * @param javaClass - if this is a list javaClass for the list
     * @return
     * @throws XMLStreamException
     * @throws XmlParsingException
     */

    private Object getObjectForElement(StatefulXMLStreamReader reader,
                                       QName elementQName,
                                       Type elementType,
                                       boolean isArray,
                                       boolean isNillable,
                                       boolean isMinOccurs0,
                                       int classType,
                                       Class javaClass)
            throws XMLStreamException,
            XmlParsingException {
        // first we have to point to the reader to the beginning for the element
        while (!reader.isStartElement() && !reader.isEndElement()) {
            reader.next();
        }

        // first validate the attribute
        if (reader.getName().equals(elementQName)) {

            String nillble = reader.getAttributeValue(Constants.URI_DEFAULT_SCHEMA_XSI, "nil");
            // actual element type may be different from the element type given
            // if extensions has used.
            Type actualElementType = elementType;
            QName typeQName = getTypeQName(reader);
            if ((typeQName != null) && !elementType.getXmlType().getQname().equals(typeQName)) {
                // i.e this is an extension type
                if (this.qNameToTypeMap.containsKey(typeQName)) {
                    actualElementType = (Type) this.qNameToTypeMap.get(typeQName);
                } else {
                    throw new XmlParsingException("Unknown type found ==> " + typeQName);
                }
            }
            // point to the complex type elements
            if (isArray) {
                Collection objectsCollection = getCollectionObject(classType, javaClass);

                // read the first element
                if ("true".equals(nillble) || "1".equals(nillble)) {
                    // this is a nill attribute
                    while (!reader.isEndElement()) {
                        reader.next();
                    }
                    if (isNillable){
                       objectsCollection.add(null);
                    } else {
                       throw new XmlParsingException("Element " + elementQName + " can not be null");
                    }

                } else {

                    Object returnObject = getElementObjectFromReader(actualElementType, reader);
                    objectsCollection.add(returnObject);

                    // we have to move the cursor until the end element of this attribute
                    while (!reader.isEndElement() || !reader.getName().equals(elementQName)) {
                        reader.next();
                    }
                }
                boolean loop = true;
                while (loop) {
                    while (!reader.isEndElement()) {
                        reader.next();
                    }
                    reader.next();
                    // now we are at the end element of the first element
                    while (!reader.isStartElement() && !reader.isEndElement()) {
                        reader.next();
                    }

                    // in this step if it is an end element we have found an end element
                    // so have to exit from the loop
                    if (reader.isEndElement()) {
                        loop = false;
                    } else {
                        // now it should be in a start element
                        // check whether still we read the original element attributes. otherwise return
                        if (reader.getName().equals(elementQName)) {
                            nillble = reader.getAttributeValue(Constants.URI_DEFAULT_SCHEMA_XSI, "nil");
                            // since this is a new element we check for extensions
                            actualElementType = elementType;
                            typeQName = getTypeQName(reader);
                            if ((typeQName != null) && !elementType.getXmlType().getQname().equals(typeQName)) {
                                // i.e this is an extension type
                                if (this.qNameToTypeMap.containsKey(typeQName)) {
                                    actualElementType = (Type) this.qNameToTypeMap.get(typeQName);
                                } else {
                                    throw new XmlParsingException("Unknown type found ==> " + typeQName);
                                }
                            }
                            if ("true".equals(nillble) || "1".equals(nillble)) {
                                // this is a nill attribute
                                while (!reader.isEndElement()) {
                                    reader.next();
                                }
                                if (isNillable) {
                                    objectsCollection.add(null);
                                } else {
                                    throw new XmlParsingException("Element " + elementQName + " can not be null");
                                }
                            } else {
                                Object returnObject = getElementObjectFromReader(actualElementType, reader);
                                objectsCollection.add(returnObject);

                                // we have to move the cursor until the end element of this attribute
                                while (!reader.isEndElement() || !reader.getName().equals(elementQName)) {
                                    reader.next();
                                }
                            }
                        } else {
                            loop = false;
                        }

                    }
                }

                // this is very important in handling primitives
                // they can not have null values so if array then we have to return the
                // array object is null
                // for other also it is convenient to assume like that.
                if ((Constants.OTHER_TYPE & classType) == Constants.OTHER_TYPE){
                    // i.e this is not a collection type
                    List objectsList = (List) objectsCollection;
                    if ((objectsCollection.size() == 0) ||
                            ((objectsCollection.size() == 1) && (objectsList.get(0) == null))) {
                        return null;
                    } else {
                        // create an array with the original element type
                        Object objectArray = Array.newInstance(elementType.getJavaClass(), objectsCollection.size());
                        for (int i = 0; i < objectsCollection.size(); i++) {
                            Array.set(objectArray, i, objectsList.get(i));
                        }
                        return objectArray;
                    }
                } else if ((Constants.COLLECTION_TYPE & classType) == Constants.COLLECTION_TYPE){
                     if ((objectsCollection.size() == 0) ||
                             ((objectsCollection.size() == 1) && (objectsCollection.iterator().next() == null))){
                         return null;
                     } else {
                         return objectsCollection;
                     }

                } else if ((Constants.MAP_TYPE & classType) == Constants.MAP_TYPE){

                    if ((objectsCollection.size() == 0) ||
                            ((objectsCollection.size() == 1) && (objectsCollection.iterator().next() == null))) {
                        return null;

                    } else {
                        List mapObjectsList = (List) objectsCollection;
                        MapType mapType = null;
                        Map mapObject = null;
                        if (javaClass.isInterface()) {
                            mapObject = new HashMap();
                        } else {
                            try {
                                mapObject = (Map) javaClass.newInstance();
                            } catch (InstantiationException e) {
                                throw new XmlParsingException("Can not instantiate the java class " + javaClass.getName(), e);
                            } catch (IllegalAccessException e) {
                                throw new XmlParsingException("Can not instantiate the java class " + javaClass.getName(), e);
                            }

                        }
                        for (Iterator iter = mapObjectsList.iterator(); iter.hasNext();) {
                            mapType = (MapType) iter.next();
                            mapObject.put(mapType.getKey(), mapType.getValue());
                        }

                        return mapObject;
                    }

                } else {
                    throw new XmlParsingException("Unknow class type " + classType);
                }

            } else {
                if ("true".equals(nillble) || "1".equals(nillble)) {
                    // this is a nill attribute
                    while (!reader.isEndElement()) {
                        reader.next();
                    }
                    reader.next();
                    if (isNillable) {
                        return null;
                    } else {
                        throw new XmlParsingException("Element " + elementQName + " can not be null");
                    }

                } else {

                    Object returnObject = getElementObjectFromReader(actualElementType, reader);
                    // we have to move the cursor until the end element of this attribute
                    while (!reader.isEndElement() || !reader.getName().equals(elementQName)) {
                        reader.next();
                    }
                    return returnObject;
                }
            }
        } else {
            if (isMinOccurs0) {
                return null;
            } else {
                throw new XmlParsingException("Unexpected Subelement " + reader.getName() + " but " +
                        "expected " + elementQName.getLocalPart());
            }
        }
    }

    private Object getElementObjectFromReader(Type elementType,
                                              StatefulXMLStreamReader reader)
            throws XmlParsingException, XMLStreamException {
        Object returnObject = null;
        if (RMIBean.class.isAssignableFrom(elementType.getJavaClass())) {
            // this is an rmi bean
            // so invoke the static parse method
            try {
                Method parseMethod = elementType.getJavaClass().getMethod("parse", new Class[]{XMLStreamReader.class, XmlStreamParser.class});
                returnObject = parseMethod.invoke(null, new Object[]{reader, this});
            } catch (NoSuchMethodException e) {
                throw new XmlParsingException("parse method has not been implemented correctly for the rmi bean "
                        + elementType.getJavaClass().getName(), e);
            } catch (IllegalAccessException e) {
                throw new XmlParsingException("can not access parse method of the rmi bean "
                        + elementType.getJavaClass().getName(), e);
            } catch (InvocationTargetException e) {
                throw new XmlParsingException("can not invoke parse method of the rmi bean "
                        + elementType.getJavaClass().getName(), e);
            }
        } else {
            // read the attributes.
            Map javaMethodToValueMap = getJavaMethodValueHashMap(elementType, reader);
            reader.next();
            returnObject = getObjectForType(reader, elementType);
            populateObjectAttributes(javaMethodToValueMap, returnObject);
        }

        return returnObject;
    }

    private void populateObjectAttributes(Map javaMethodToValueMap, Object returnObject) throws XmlParsingException {
        Method javaMehtod;
        try {
            for (Iterator iter = javaMethodToValueMap.keySet().iterator();iter.hasNext();){
                javaMehtod = (Method) iter.next();
                javaMehtod.invoke(returnObject,new Object[]{javaMethodToValueMap.get(javaMehtod)});
            }
        } catch (IllegalAccessException e) {
            throw new XmlParsingException("Can not set the attribute value");
        } catch (InvocationTargetException e) {
            throw new XmlParsingException("Can not set the attribute value");
        }
    }

    private Map getJavaMethodValueHashMap(Type actualElementType, StatefulXMLStreamReader reader)
            throws XmlParsingException {
        AttributeField attributeField;
        String attributeVlaue;
        Object attributeObject;
        Map javaMethodToValueMap = new HashMap();
        for (Iterator iter = actualElementType.getAllAttributeFields().iterator();iter.hasNext();){
            attributeField = (AttributeField) iter.next();
            attributeVlaue = reader.getAttributeValue(attributeField.getNamespace(),
                                                    attributeField.getName());
            if (attributeVlaue != null) {
                attributeObject = getSimpleTypeObject(attributeField.getType(), reader, attributeVlaue);
                javaMethodToValueMap.put(attributeField.getSetterMethod(), attributeObject);
            } else if (attributeField.isRequried()){
                throw new XmlParsingException("Required attribute " + attributeField.getName() + " is missing");
            }

        }
        return javaMethodToValueMap;
    }

    /**
     * returns the collection object according to the type.
     * @param classType
     * @param javaClass
     * @return
     * @throws XmlParsingException
     */

    private Collection getCollectionObject(int classType,
                                           Class javaClass)
            throws XmlParsingException {

        Collection objectsCollection = null;
        try {
            if ((Constants.OTHER_TYPE & classType) == Constants.OTHER_TYPE) {
                // i.e this is not a list or a map
                objectsCollection = new ArrayList();
            } else {
                if (javaClass.isInterface()) {
                    if ((Constants.LIST_TYPE & classType) == Constants.LIST_TYPE) {
                       objectsCollection = new ArrayList();
                    } else if ((Constants.SET_TYPE & classType) == Constants.SET_TYPE) {
                       objectsCollection = new HashSet();
                    } else if ((Constants.COLLECTION_TYPE & classType) == Constants.COLLECTION_TYPE) {
                       objectsCollection = new ArrayList();
                    } else if ((Constants.MAP_TYPE & classType) == Constants.MAP_TYPE) {
                       objectsCollection = new ArrayList();
                    }
                } else {
                    if ((Constants.COLLECTION_TYPE & classType) == Constants.COLLECTION_TYPE) {
                        objectsCollection = (Collection) javaClass.newInstance();
                    } else if ((Constants.MAP_TYPE & classType) == Constants.MAP_TYPE) {
                        objectsCollection = new ArrayList();
                    }
                }
            }

        } catch (InstantiationException e) {
            throw new XmlParsingException("Problem with instanciating the element class " +
                        javaClass.getName() , e);
        } catch (IllegalAccessException e) {
            throw new XmlParsingException("Problem with instanciating the element class " +
                        javaClass.getName() , e);
        }
        return objectsCollection;
    }

    /**
     * reader must be at the start of the element
     *
     * @param reader
     * @return qName for the type attribute
     */
    private QName getTypeQName(StatefulXMLStreamReader reader) {
        QName typeQName = null;
        String typeValue = reader.getAttributeValue(Constants.URI_2001_SCHEMA_XSI, "type");
        if ((typeValue != null) && !typeValue.equals("")) {
            int index = typeValue.indexOf(":");
            String nsPrefix = "";
            if (index > -1) {
                nsPrefix = typeValue.substring(0, index);
            }

            String localPart = typeValue.substring(index + 1);
            typeQName = new QName(reader.getNamespaceURI(nsPrefix), localPart);
        }
        return typeQName;
    }

    /** 
     * Determine if the reader has been advanced beyond its initial location, prior 
     * to the methods within XmlStreamParser being called.
     * 
     * @param reader The XMLStreamReader on which the XmlStreamParser is acting.
     * @return true if the reader has been advanced beyond its initial location, false otherwise
     */
    private boolean determinePriorAccess(XMLStreamReader reader) {
        if (!priorAccessDetermined) {
            if (reader.getLocation().getCharacterOffset() == 0)
                readerPriorAccess = false;
            else
                readerPriorAccess = true;

            priorAccessDetermined = true;
        }
        return readerPriorAccess;
    }

    /**
     * Provide a mechanism (a stack) to track the current state of the XMLStream
     * 
     * @param reader_in the XMLStreamReader that the XmlStreamParser is acting on
     * @return a StatefulXMLStreamReader to monitor the state of the XMLStreamReader
     */
    private StatefulXMLStreamReader getStatefulReader(XMLStreamReader reader_in) {
        if (reader_in instanceof StatefulXMLStreamReader)            
            return (StatefulXMLStreamReader) reader_in;
        else
            return new StatefulXMLStreamReader(reader_in, readerPriorAccess);
    }
    
    /**
     * Advance the reader to point to the beginning of the element
     * @param reader the StatefulXMLStreamReader we are using to parse the XMLStream
     */
    
    private void advanceToFirstElement(StatefulXMLStreamReader reader) throws XMLStreamException {
    
        if (readerPriorAccess) {
            while (!reader.isStartElement() && !reader.isEndElement() && reader.hasNext()) {
                reader.next();
            }
        } else {
            while (reader.getDepth() == 0 && reader.hasNext()) {
                reader.next();
            }
        }
    }
}
