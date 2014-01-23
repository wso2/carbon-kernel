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
import org.apache.axis2.rmi.exception.XmlSerializingException;
import org.apache.axis2.rmi.metadata.AttributeField;
import org.apache.axis2.rmi.metadata.ElementField;
import org.apache.axis2.rmi.metadata.Parameter;
import org.apache.axis2.rmi.metadata.Type;
import org.apache.axis2.rmi.metadata.impl.TypeImpl;
import org.apache.axis2.rmi.metadata.xml.XmlElement;
import org.apache.axis2.rmi.types.MapType;
import org.apache.axis2.rmi.util.Constants;
import org.apache.axis2.rmi.util.NamespacePrefix;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * this class serializes a given java object with its corresponding
 * type object
 */
public class JavaObjectSerializer {

    private Map processedTypeMap;
    private Configurator configurator;
    private Map schemaMap;
    private SimpleTypeHandler simpleTypeHandler;
    private Class simpleTypeHandlerClass;

    public JavaObjectSerializer(Map processedTypeMap,
                                Configurator configurator,
                                Map schemaMap) {
        this.processedTypeMap = processedTypeMap;
        this.configurator = configurator;
        this.schemaMap = schemaMap;
        this.simpleTypeHandler = this.configurator.getSimpleTypeHandler();
        this.simpleTypeHandlerClass = this.simpleTypeHandler.getClass();
    }

    /**
     * serialize all the input parameters
     * @param objects
     * @param inputXmlElement
     * @param inputParameters
     * @param writer
     * @throws XMLStreamException
     * @throws XmlSerializingException
     */

    public void serializeInputElement(Object[] objects,
                                      XmlElement inputXmlElement,
                                      List inputParameters,
                                      XMLStreamWriter writer) throws XMLStreamException,
            XmlSerializingException {
        NamespacePrefix namespacePrefix = new NamespacePrefix();
        writeStartElement(writer,
                inputXmlElement.getNamespace(),
                inputXmlElement.getName(),
                namespacePrefix);
        // input objects null means no parameters
        if (objects != null) {
            for (int i = 0; i < objects.length; i++) {
                serializeParameter(objects[i],
                        (Parameter) inputParameters.get(i),
                        writer,
                        namespacePrefix);
            }
        }

        writer.writeEndElement();
    }

    /**
     * serialize the out put parameter
     * @param object
     * @param outputXmlElement
     * @param outputParameter
     * @param writer
     * @throws XMLStreamException
     * @throws XmlSerializingException
     */

    public void serializeOutputElement(Object object,
                                       XmlElement outputXmlElement,
                                       Parameter outputParameter,
                                       XMLStreamWriter writer)
            throws XMLStreamException, XmlSerializingException {
        NamespacePrefix namespacePrefix = new NamespacePrefix();
        //write output start element
        writeStartElement(writer,
                outputXmlElement.getNamespace(),
                outputXmlElement.getName(),
                namespacePrefix);
        if (outputParameter != null) {
            // null means this is the void type
            serializeParameter(object, outputParameter, writer, namespacePrefix);
        }
        writer.writeEndElement();
    }

    /**
     * serailize the parameter object. parameter represents an xml element.
     * so it basically calls to the serialize element method.
     * @param parameterValue
     * @param parameter
     * @param writer
     * @param namespacePrefix
     * @throws XMLStreamException
     * @throws XmlSerializingException
     */
    public void serializeParameter(Object parameterValue,
                                   Parameter parameter,
                                   XMLStreamWriter writer,
                                   NamespacePrefix namespacePrefix)
            throws XMLStreamException,
            XmlSerializingException {
        QName parameterQName = new QName(parameter.getNamespace(), parameter.getName());
        serializeElement(parameterValue,
                parameterQName,
                parameter.getType(),
                writer,
                namespacePrefix,
                parameter.isArray(),
                parameter.getClassType());
    }

    /**
     * serailizes an element. elements can either be parameter or an attribute.
     * @param elementValue
     * @param elementQName
     * @param elementType
     * @param writer
     * @param namespacePrefix
     * @param isArray
     * @param classType
     * @throws XmlSerializingException
     * @throws XMLStreamException
     */

    private void serializeElement(Object elementValue,
                                  QName elementQName,
                                  Type elementType,
                                  XMLStreamWriter writer,
                                  NamespacePrefix namespacePrefix,
                                  boolean isArray,
                                  int classType)
            throws XmlSerializingException, XMLStreamException {
        if ((elementValue == null) || !isArray) {
            // then we can serializeInputElement it directly either it is array or not
            serialize(elementValue,
                    elementQName,
                    elementType,
                    writer,
                    namespacePrefix);
        } else {

            if ((classType & Constants.MAP_TYPE) == Constants.MAP_TYPE) {
                Map elementMap = (Map) elementValue;
                Object key = null;
                for (Iterator iter = elementMap.keySet().iterator(); iter.hasNext();) {
                    key = iter.next();
                    serialize(new MapType(key, elementMap.get(key)),
                            elementQName,
                            elementType,
                            writer,
                            namespacePrefix);
                }
            } else if ((classType & Constants.COLLECTION_TYPE) == Constants.COLLECTION_TYPE) {
                // if this is a List we convert this to an Object array
                Collection elementCollection = (Collection) elementValue;
                for (Iterator iter = elementCollection.iterator(); iter.hasNext();) {
                    serialize(iter.next(),
                            elementQName,
                            elementType,
                            writer,
                            namespacePrefix);
                }
            } else {
                int length = Array.getLength(elementValue);
                Object object;
                for (int i = 0; i < length; i++) {
                    object = Array.get(elementValue, i);
                    serialize(object,
                            elementQName,
                            elementType,
                            writer,
                            namespacePrefix);
                }
            }

        }
    }

    /**
     * this method serailizes the given object according to the type information
     * given.
     *
     * @param object
     * @param type
     * @param writer
     */

    public void serialize(Object object,
                          QName parentQName,
                          Type type,
                          XMLStreamWriter writer,
                          NamespacePrefix namespacePrefix)
            throws XMLStreamException,
            XmlSerializingException {

        if (object instanceof RMIBean) {
            RMIBean rmiBean = (RMIBean) object;
            rmiBean.serialize(writer, this, parentQName, namespacePrefix);
        } else {
            // first write the start element
            writeStartElement(writer,
                    parentQName.getNamespaceURI(),
                    parentQName.getLocalPart(),
                    namespacePrefix);
            if (object == null) {
                writeNullAttribute(writer, namespacePrefix);
            } else {
                // handle extensions here.
                // primitive can not have excented types
                if (!object.getClass().equals(type.getJavaClass()) && !type.getJavaClass().isPrimitive()) {
                    // i.e this is an extension
                    if (!processedTypeMap.containsKey(object.getClass())) {
                        Type newType = new TypeImpl(object.getClass());
                        processedTypeMap.put(object.getClass(), newType);
                        try {
                            newType.populateMetaData(this.configurator, this.processedTypeMap);
                            newType.generateSchema(this.configurator, this.schemaMap);
                        } catch (MetaDataPopulateException e) {
                            new XmlSerializingException("Problem in processing new type", e);
                        } catch (SchemaGenerationException e) {
                            new XmlSerializingException("Problem in processing new type", e);
                        }
                    }
                    type = (Type) processedTypeMap.get(object.getClass());
                    writeTypeAttribute(writer, type.getXmlType().getQname(), namespacePrefix);
                }

                if (type.getXmlType().isSimpleType()) {
                    // this is a know type for us
                    // get the string represenation of this object using converter util class.
                    if (!type.getJavaClass().equals(Object.class)) {
                        writer.writeCharacters(getSimpleTypeStringValue(type, object));
                    }

                } else {
                    // this is a complex type
                    try {

                        AttributeField attributeField;
                        Method getterMethod;
                        Object attributeFieldValue;
                        QName attributeQName;
                        for (Iterator iter = type.getAllAttributeFields().iterator(); iter.hasNext();) {
                            attributeField = (AttributeField) iter.next();
                            getterMethod = attributeField.getGetterMethod();
                            attributeFieldValue = getterMethod.invoke(object, new Object[]{});
                            attributeQName = new QName(attributeField.getNamespace(), attributeField.getName());
                            // calls to write attribute. for attributes we can have only simple types
                            if (attributeFieldValue != null) {
                                writeAttribute(writer,
                                        getSimpleTypeStringValue(attributeField.getType(), attributeFieldValue),
                                        attributeQName,
                                        namespacePrefix);
                            } else if (attributeField.isRequried()) {
                                throw new XmlSerializingException("Attribute value for attribute "
                                        + attributeField.getName() + " is required");
                            }

                        }

                        // write the element fields
                        ElementField elementField;
                        Object elementFieldValue;
                        for (Iterator iter = type.getAllElementFields().iterator(); iter.hasNext();) {
                            elementField = (ElementField) iter.next();
                            getterMethod = elementField.getGetterMethod();
                            elementFieldValue = getterMethod.invoke(object, new Object[]{});
                            serializeElementField(elementFieldValue,
                                    elementField,
                                    writer,
                                    namespacePrefix);

                        }
                    } catch (IllegalAccessException e) {
                        throw new XmlSerializingException("problem with method inovocation " + type.getName());
                    } catch (InvocationTargetException e) {
                        throw new XmlSerializingException("problem with method inovocation " + type.getName());
                    }

                }
            }
            writer.writeEndElement();
        }
    }

    private String getSimpleTypeStringValue(Type type, Object object) throws XmlSerializingException {
        try {
            Method methodToInvoke = this.simpleTypeHandlerClass.getMethod("convertToString", new Class[]{type.getJavaClass()});
            // these methods are static so use null as the object argument
            return (String) methodToInvoke.invoke(this.simpleTypeHandler, new Object[]{object});
        } catch (NoSuchMethodException e) {
            throw new XmlSerializingException("Can not invoke converter util method convertToString for class "
                    + type.getJavaClass().getName(), e);
        } catch (IllegalAccessException e) {
            throw new XmlSerializingException("Can not invoke converter util method convertToString for class "
                    + type.getJavaClass().getName(), e);
        } catch (InvocationTargetException e) {
            throw new XmlSerializingException("Can not invoke converter util method convertToString for class "
                    + type.getJavaClass().getName(), e);
        }
    }

    /**
     * this method serializes the attributes by calling to serialize element method.
     *
     * @param elementFieldValue
     * @param elementField
     * @param writer
     * @param namespacePrefix
     */
    private void serializeElementField(Object elementFieldValue,
                                       ElementField elementField,
                                       XMLStreamWriter writer,
                                       NamespacePrefix namespacePrefix)
            throws XmlSerializingException, XMLStreamException {

        QName elementFieldQName = new QName(elementField.getElement().getNamespace(),
                elementField.getElement().getName());
                serializeElement(elementFieldValue,
                elementFieldQName,
                elementField.getType(),
                writer,
                namespacePrefix,
                elementField.isArray(),
                elementField.getClassType());

    }

    private void writeStartElement(XMLStreamWriter writer,
                                   String namespace,
                                   String localPart,
                                   NamespacePrefix namespacePrefix)
            throws XMLStreamException {
        if (!namespace.equals("")) {
            String prefix = writer.getPrefix(namespace);
            if (prefix == null) {
                prefix = "ns" + namespacePrefix.getNamesapcePrefix();
                writer.writeStartElement(prefix, localPart, namespace);
                writer.writeNamespace(prefix, namespace);
                writer.setPrefix(prefix, namespace);
            } else {
                writer.writeStartElement(namespace, localPart);
            }
        } else {
            writer.writeStartElement(localPart);
        }

    }

    private void writeTypeAttribute(XMLStreamWriter writer,
                                    QName typeQname,
                                    NamespacePrefix namespacePrefix)
            throws XMLStreamException {
        String xsiPrefix = writer.getPrefix(Constants.URI_DEFAULT_SCHEMA_XSI);
        if (xsiPrefix == null) {
            xsiPrefix = "ns" + namespacePrefix.getNamesapcePrefix();
            writer.writeNamespace(xsiPrefix, Constants.URI_DEFAULT_SCHEMA_XSI);
            writer.setPrefix(xsiPrefix, Constants.URI_DEFAULT_SCHEMA_XSI);
        }

        String typePrefix = writer.getPrefix(typeQname.getNamespaceURI());
        if (typePrefix == null) {
            typePrefix = "ns" + namespacePrefix.getNamesapcePrefix();
            writer.writeNamespace(typePrefix, typeQname.getNamespaceURI());
            writer.setPrefix(typePrefix, typeQname.getNamespaceURI());
        }

        String attributeValue = typeQname.getLocalPart();
        if (!typePrefix.equals("")) {
            attributeValue = typePrefix + ":" + attributeValue;
        }
        writer.writeAttribute(Constants.URI_DEFAULT_SCHEMA_XSI, "type", attributeValue);
    }

    private void writeNullAttribute(XMLStreamWriter writer,
                                    NamespacePrefix namespacePrefix)
            throws XMLStreamException {
        QName attributeQName = new QName(Constants.URI_DEFAULT_SCHEMA_XSI,"nil");
        writeAttribute(writer,"1",attributeQName,namespacePrefix);
    }

    /**
     * this method wrtes the simple attribute to the writer.
     * @param writer
     * @param attributeValue
     * @param attributeQName
     * @param namespacePrefix
     * @throws XMLStreamException
     */
    private void writeAttribute(XMLStreamWriter writer,
                                String attributeValue,
                                QName attributeQName,
                                NamespacePrefix namespacePrefix) throws XMLStreamException {
        if ((attributeQName.getNamespaceURI() != null) && !attributeQName.getNamespaceURI().trim().equals("")){
            String prefix = writer.getPrefix(attributeQName.getNamespaceURI());
            if (prefix == null){
                prefix = "ns" + namespacePrefix.getNamesapcePrefix();
                writer.writeNamespace(prefix, attributeQName.getNamespaceURI());
                writer.setPrefix(prefix, attributeQName.getNamespaceURI());
            }
            writer.writeAttribute(attributeQName.getNamespaceURI(),
                    attributeQName.getLocalPart(),
                    attributeValue);

        } else {
            writer.writeAttribute(attributeQName.getLocalPart(),attributeValue);
        }

    }
}
