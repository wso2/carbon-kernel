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

package org.apache.axis2.databinding.utils;


import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.*;
import org.apache.axiom.om.util.Base64;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.databinding.typemapping.SimpleTypeMapper;
import org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl;
import org.apache.axis2.deployment.util.BeanExcludeInfo;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.java2wsdl.TypeTable;
import org.apache.axis2.engine.ObjectSupplier;
import org.apache.axis2.util.Loader;
import org.apache.axis2.util.StreamWrapper;


public class BeanUtil {
    private static int nsCount = 1;

    /**
     * To Serilize Bean object this method is used, this will create an object array using given
     * bean object
     */
    public static XMLStreamReader getPullParser(Object beanObject,
                                                QName beanName,
                                                TypeTable typeTable,
                                                boolean qualified,
                                                boolean processingDocLitBare) {

        Class beanClass = beanObject.getClass();
        List<Object> propertyQnameValueList = getPropertyQnameList(beanObject,
                                                           beanClass, beanName, typeTable, qualified, processingDocLitBare);

        ArrayList<QName> objectAttributes = new ArrayList<QName>();

        if ((typeTable != null)) {
            QName qNamefortheType = typeTable.getQNamefortheType(getClassName(beanClass));
            if (qNamefortheType != null) {
                objectAttributes.add(new QName(Constants.XSI_NAMESPACE, "type", "xsi"));
                objectAttributes.add(qNamefortheType);
            }
        }

        return new ADBXMLStreamReaderImpl(beanName, propertyQnameValueList.toArray(), objectAttributes.toArray(),
                                          typeTable, qualified);

    }

    private static String getClassName(Class type) {
        String name = type.getName();
        if (name.indexOf("$") > 0) {
            name = name.replace('$', '_');
        }
        return name;
    }


    private static BeanInfo getBeanInfo(Class beanClass, Class beanSuperclass) throws IntrospectionException {
        BeanInfo beanInfo; 
        try {
            if (beanSuperclass != null)
            	beanInfo = Introspector.getBeanInfo(beanClass, beanSuperclass);
            else
                beanInfo = Introspector.getBeanInfo(beanClass);
        }
        catch (IntrospectionException e) {
            throw e;
        }

         
        return beanInfo;
    }

    private static BeanInfo getBeanInfo(Class beanClass) throws IntrospectionException {
        return getBeanInfo(beanClass, null);
    }

    private static List<Object> getPropertyQnameList(Object beanObject,
                                                     Class<?> beanClass,
                                                     QName beanName,
                                                     TypeTable typeTable,
                                                     boolean qualified,
                                                     boolean processingDocLitBare) {
        List<Object> propertyQnameValueList;
        Class<?> supperClass = beanClass.getSuperclass();

        if (!getQualifiedName(supperClass.getPackage()).startsWith("java.")) {
            propertyQnameValueList = getPropertyQnameList(beanObject,
                                                          supperClass, beanName, typeTable, qualified, processingDocLitBare);
        } else {
            propertyQnameValueList = new ArrayList<Object>();
        }

        try {
            QName elemntNameSpace = null;
            if (typeTable != null && qualified) {
                QName qNamefortheType = typeTable.getQNamefortheType(beanClass.getName());
                if (qNamefortheType == null) {
                    qNamefortheType = typeTable.getQNamefortheType(beanClass.getPackage().getName());
                }
                if (qNamefortheType == null) {
                    throw new AxisFault("Mapping qname not fond for the package: " +
                                        beanObject.getClass().getPackage().getName());
                }

                elemntNameSpace = new QName(qNamefortheType.getNamespaceURI(), "elementName");
            }
            AxisService axisService = null;
            if (MessageContext.getCurrentMessageContext() != null) {
                axisService = MessageContext.getCurrentMessageContext().getAxisService();
            }

            BeanExcludeInfo beanExcludeInfo = null;
            if (axisService != null && axisService.getExcludeInfo() != null) {
                beanExcludeInfo = axisService.getExcludeInfo().getBeanExcludeInfoForClass(beanClass.getName());
            }
            BeanInfo beanInfo = getBeanInfo(beanClass, beanClass.getSuperclass());
            PropertyDescriptor[] properties = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor property : properties) {
                String propertyName = property.getName();
                if (propertyName.equals("class") ||
                    beanExcludeInfo != null && beanExcludeInfo.isExcludedProperty(propertyName)) {
                    continue;
                }

                Class<?> ptype = property.getPropertyType();
                Method readMethod = property.getReadMethod();
                if (readMethod == null) {
                    Class propertyType = property.getPropertyType();
                    if (propertyType == java.lang.Boolean.class) {
                        Method writeMethod = property.getWriteMethod();
                        if (writeMethod != null) {
                            String tmpWriteMethodName = writeMethod.getName();
                            PropertyDescriptor tmpPropDesc =
                                    new PropertyDescriptor(property.getName(),
                                            beanObject.getClass(),
                                            "is" + tmpWriteMethodName.substring(3),
                                            tmpWriteMethodName);
                            readMethod = tmpPropDesc.getReadMethod();
                        }
                    }
                }
                Object value;
                if (readMethod != null) {
                    readMethod.setAccessible(true);
                    value = readMethod.invoke(beanObject);
                } else {
                    throw new AxisFault("Property '" + propertyName + "' in bean class '"
                                        + beanClass.getName() + "'is not readable.");
                }

                if (SimpleTypeMapper.isSimpleType(ptype)) {
                    addTypeQname(elemntNameSpace, propertyQnameValueList, property,
                                 beanName, processingDocLitBare);
                    propertyQnameValueList.add(
                            value == null ? null : SimpleTypeMapper.getStringValue(value));
                } else if (ptype.isArray()) {
                    if (SimpleTypeMapper.isSimpleType(ptype.getComponentType())) {
                        if (value != null) {
                            if (Byte.TYPE.equals(ptype.getComponentType())) {
                                addTypeQname(elemntNameSpace, propertyQnameValueList,
                                             property, beanName, processingDocLitBare);
                                propertyQnameValueList.add(Base64.encode((byte[]) value));
                            } else {
                                int i1 = Array.getLength(value);
                                for (int j = 0; j < i1; j++) {
                                    Object o = Array.get(value, j);
                                    addTypeQname(elemntNameSpace, propertyQnameValueList,
                                                 property, beanName, processingDocLitBare);
                                    propertyQnameValueList.add(o == null ? null :
                                                               SimpleTypeMapper.getStringValue(o));
                                }
                            }
                        } else {
                            addTypeQname(elemntNameSpace, propertyQnameValueList, property,
                                         beanName, processingDocLitBare);
                            propertyQnameValueList.add(value);
                        }
                    } else {
                        if (value != null) {
                            for (Object o : (Object[]) value) {
                                addTypeQname(elemntNameSpace, propertyQnameValueList,
                                             property, beanName, processingDocLitBare);
                                propertyQnameValueList.add(o);
                            }
                        } else {
                            addTypeQname(elemntNameSpace, propertyQnameValueList, property,
                                         beanName, processingDocLitBare);
                            propertyQnameValueList.add(value);
                        }
                    }
                } else if (SimpleTypeMapper.isCollection(ptype)) {
                    Collection<?> objList = (Collection<?>) value;
                    if (objList != null && objList.size() > 0) {
                        //this was given error , when the array.size = 0
                        // and if the array contain simple type , then the ADBPullParser asked
                        // PullParser from That simpel type
                        for (Object o : objList) {
                            if (SimpleTypeMapper.isSimpleType(o)) {
                                addTypeQname(elemntNameSpace, propertyQnameValueList,
                                             property, beanName, processingDocLitBare);
                                propertyQnameValueList.add(o);
                            } else {
                                addTypeQname(elemntNameSpace, propertyQnameValueList,
                                             property, beanName, processingDocLitBare);
                                propertyQnameValueList.add(o);
                            }
                        }

                    } else {
                        addTypeQname(elemntNameSpace, propertyQnameValueList, property,
                                     beanName, processingDocLitBare);
                        propertyQnameValueList.add(value);
                    }
                } else {
                    addTypeQname(elemntNameSpace, propertyQnameValueList, property,
                                 beanName, processingDocLitBare);
                    if (Object.class.equals(ptype)) {
                        if ((value instanceof Integer) ||
                            (value instanceof Short) ||
                            (value instanceof Long) ||
                            (value instanceof Float)) {
                            propertyQnameValueList.add(value.toString());
                            continue;
                        }
                    }

                    propertyQnameValueList.add(value);
                }
            }

            return propertyQnameValueList;

        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        } catch (java.beans.IntrospectionException e) {
            throw new RuntimeException(e);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (java.lang.IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static void addTypeQname(QName elemntNameSpace,
                                     List<Object> propertyQnameValueList,
                                     PropertyDescriptor propDesc,
                                     QName beanName,
                                     boolean processingDocLitBare) {
        if (elemntNameSpace != null) {
            propertyQnameValueList.add(new QName(elemntNameSpace.getNamespaceURI(),
                                                 propDesc.getName(), elemntNameSpace.getPrefix()));
        } else {
            if (processingDocLitBare) {
                propertyQnameValueList.add(new QName(propDesc.getName()));
            } else {
                propertyQnameValueList.add(new QName(beanName.getNamespaceURI(), propDesc.getName(), beanName.getPrefix()));
            }

        }
    }

    /**
     * to get the pull parser for a given bean object , generate the wrpper element using class
     * name
     *
     * @param beanObject
     */
    public static XMLStreamReader getPullParser(Object beanObject) {
        String className = beanObject.getClass().getName();
        if (className.indexOf(".") > 0) {
            className = className.substring(className.lastIndexOf('.') + 1,
                                            className.length());
        }
        return getPullParser(beanObject, new QName(className), null, false, false);
    }

    public static Object deserialize(Class beanClass,
                                     OMElement beanElement,
                                     ObjectSupplier objectSupplier,
                                     String arrayLocalName)
            throws AxisFault {
        Object beanObj;
        try {
            // Added this block as a fix for issues AXIS2-2055 and AXIS2-1899
            // to support polymorphism in POJO approach.
            // Retrieve the type name of the instance from the 'type' attribute
            // and retrieve the class.
            
            String instanceTypeName = beanElement.getAttributeValue(new QName(Constants.XSI_NAMESPACE, "type"));
            if (instanceTypeName != null) {
                MessageContext messageContext = MessageContext.getCurrentMessageContext();
                // we can have this support only at the server side. we need to find the axisservice
                // to get the type table.
                if (messageContext != null) {
                    AxisService axisService = messageContext.getAxisService();
                    if (axisService != null) {
                        QName typeQName = beanElement.resolveQName(instanceTypeName);
                        TypeTable typeTable = axisService.getTypeTable();
                        String className = typeTable.getClassNameForQName(typeQName);
                        if (className != null) {
                            try {
                                beanClass = Loader.loadClass(beanClass.getClassLoader(), className);
                            } catch (ClassNotFoundException ce) {
                                throw AxisFault.makeFault(ce);
                            }
                        } else {
                            throw new AxisFault("Unknow type " + typeQName);
                        }
                    }
                }
            }

            // check for nil attribute:
            QName nilAttName = new QName(Constants.XSI_NAMESPACE, Constants.NIL, "xsi");
            if (beanElement.getAttribute(nilAttName) != null) {
                return null;
            }

            if (beanClass.isArray()) {
                ArrayList<Object> valueList = new ArrayList<Object>();
                Class arrayClassType = beanClass.getComponentType();
                if ("byte".equals(arrayClassType.getName())) {
                    // find the part first and decode it
                    OMElement partElement = null;
                    for (Iterator iter = beanElement.getChildElements(); iter.hasNext();) {
                        partElement = (OMElement) iter.next();
                        if (partElement.getLocalName().equals(arrayLocalName)) {
                            break;
                        }
                    }
                    return Base64.decode(partElement.getText());
                } else {
                    Iterator parts = beanElement.getChildElements();
                    OMElement omElement;
                    while (parts.hasNext()) {
                        Object objValue = parts.next();
                        if (objValue instanceof OMElement) {
                            omElement = (OMElement) objValue;
                            if ((arrayLocalName != null) && !arrayLocalName.equals(omElement.getLocalName())) {
                                continue;
                            }
                            // this is a multi dimentional array so always inner element is array
                            Object obj = deserialize(arrayClassType,
                                                     omElement,
                                                     objectSupplier, "array");

                            valueList.add(obj);
                        }
                    }
                    return ConverterUtil.convertToArray(arrayClassType, valueList);
                }
            } else {
                if (SimpleTypeMapper.isSimpleType(beanClass)) {
                    return getSimpleTypeObjectChecked(beanClass, beanElement);
                } else if ("java.lang.Object".equals(beanClass.getName())) {
                    return beanElement.getFirstOMChild();
                }

                //use a comaprator to ignore the case of the bean element
                //names eg. if the property descriptor is getServiceName it
                //should accept child element with ServiceName as well.
                //but currently accepts only serviceName
                Comparator comparator = new Comparator() {
                    public int compare(Object o1, Object o2) {
                        String string1 = (String) o1;
                        String string2 = (String) o2;
                        return string1.compareToIgnoreCase(string2);
                    }
                };
                Map<String, PropertyDescriptor> properties = new TreeMap<String, PropertyDescriptor>(comparator);


                BeanInfo beanInfo = getBeanInfo(beanClass);
                PropertyDescriptor[] propDescs = beanInfo.getPropertyDescriptors();
                for (PropertyDescriptor proprty : propDescs) {
                    properties.put(proprty.getName(), proprty);
                }
                Iterator elements = beanElement.getChildren();
                beanObj = objectSupplier.getObject(beanClass);
                while (elements.hasNext()) {
                    // the beanClass could be an abstract one.
                    // so create an instance only if there are elements, in
                    // which case a concrete subclass is available to instantiate.
                    OMElement parts;
                    Object objValue = elements.next();
                    if (objValue instanceof OMElement) {
                        parts = (OMElement) objValue;
                    } else {
                        continue;
                    }
                    OMAttribute attribute = parts.getAttribute(
                            new QName("http://www.w3.org/2001/XMLSchema-instance", "nil", "xsi"));

                    // if parts/@href != null then need to find element with id and deserialize.
                    // before that first check whether we already have it in the hashtable
                    String partsLocalName = parts.getLocalName();
                    PropertyDescriptor prty = properties.remove(partsLocalName);
                    if (prty != null) {
                        Class parameters = prty.getPropertyType();
                        if (prty.getName().equals("class"))
                            continue;

                        Object partObj;
                        boolean isNil = false;
                        if (attribute != null) {
                            String nilValue = attribute.getAttributeValue();
                            if ("true".equals(nilValue) || "1".equals(nilValue)) {
                                isNil = true;
                            }
                        }
                        if (isNil) {
                            partObj = null;
                        } else {
                            if (SimpleTypeMapper.isSimpleType(parameters)) {
                                partObj = SimpleTypeMapper.getSimpleTypeObject(parameters, parts);
                            } else if (SimpleTypeMapper.isHashSet(parameters)) {
                                partObj = SimpleTypeMapper.getHashSet((OMElement)
                                        parts.getParent(), prty.getName());
                            } else if (SimpleTypeMapper.isCollection(parameters)) {
                                partObj = SimpleTypeMapper.getArrayList((OMElement)
                                        parts.getParent(), prty.getName());
                            } else if (SimpleTypeMapper.isDataHandler(parameters)) {
                                partObj = SimpleTypeMapper.getDataHandler(parts);
                            } else if (parameters.isArray()) {
                                partObj = deserialize(parameters, (OMElement) parts.getParent(),
                                                      objectSupplier, prty.getName());
                            } else {
                                partObj = deserialize(parameters, parts, objectSupplier, null);
                            }
                        }
                        Object[] parms = new Object[]{partObj};
                        Method writeMethod = prty.getWriteMethod();
                        if (writeMethod != null) {
                            writeMethod.setAccessible(true);
                            writeMethod.invoke(beanObj, parms);
                        }
                    }
                }
                return beanObj;
            }
        } catch (IllegalAccessException e) {
            throw new AxisFault("IllegalAccessException : " + e);
        } catch (InvocationTargetException e) {
            throw new AxisFault("InvocationTargetException : " + e);
        } catch (IntrospectionException e) {
            throw new AxisFault("IntrospectionException : " + e);
        }


    }

    public static Object deserialize(Class beanClass,
                                     OMElement beanElement,
                                     MultirefHelper helper,
                                     ObjectSupplier objectSupplier) throws AxisFault {
        Object beanObj;
        try {
            HashMap<String, PropertyDescriptor> properties = new HashMap<String, PropertyDescriptor>();
            BeanInfo beanInfo = getBeanInfo(beanClass);
            PropertyDescriptor[] propDescs = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor proprty : propDescs) {
                properties.put(proprty.getName(), proprty);
            }

            beanObj = objectSupplier.getObject(beanClass);
            Iterator elements = beanElement.getChildren();
            while (elements.hasNext()) {
                Object child = elements.next();
                OMElement parts;
                if (child instanceof OMElement) {
                    parts = (OMElement) child;
                } else {
                    continue;
                }
                String partsLocalName = parts.getLocalName();
                PropertyDescriptor prty = properties.get(
                        partsLocalName.toLowerCase());
                if (prty != null) {
                    Class parameters = prty.getPropertyType();
                    if (prty.getName().equals("class"))
                        continue;
                    Object partObj;
                    OMAttribute attr = MultirefHelper.processRefAtt(parts);
                    if (attr != null) {
                        String refId = MultirefHelper.getAttvalue(attr);
                        partObj = helper.getObject(refId);
                        if (partObj == null) {
                            partObj = helper.processRef(parameters, refId, objectSupplier);
                        }
                    } else {
                        partObj = SimpleTypeMapper.getSimpleTypeObject(parameters, parts);
                        if (partObj == null) {
                            partObj = deserialize(parameters, parts, objectSupplier, null);
                        }
                    }
                    Object[] parms = new Object[]{partObj};
                    Method writeMethod = prty.getWriteMethod();
                    if (writeMethod != null) {
                        writeMethod.setAccessible(true);
                        writeMethod.invoke(beanObj, parms);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new AxisFault("IllegalAccessException : " + e);
        } catch (InvocationTargetException e) {
            throw new AxisFault("InvocationTargetException : " + e);
        } catch (IntrospectionException e) {
            throw new AxisFault("IntrospectionException : " + e);
        }
        return beanObj;
    }


    /**
     * To get JavaObjects from XML element , the element most of the time contains only one element
     * in that case that element will be converted to the JavaType specified by the javaTypes array
     * The algo is as follows, get the childerns of the response element , and if it conatian more
     * than one element then check the retuen type of that element and conver that to corresponding
     * JavaType
     *
     * @param response  OMElement
     * @param javaTypes Array of JavaTypes
     * @return Array of objects
     * @throws AxisFault
     */
    public static Object[] deserialize(OMElement response,
                                       Object[] javaTypes,
                                       ObjectSupplier objectSupplier) throws AxisFault {
        return BeanUtil.deserialize(response, javaTypes, objectSupplier, null, null);
    }

    public static Object[] deserialize(OMElement response,
                                       Object[] javaTypes,
                                       ObjectSupplier objectSupplier,
                                       String[] parameterNames,
                                       Method method) throws AxisFault {
        /*
         * Take the number of parameters in the method and , only take that much of child elements
         * from the OMElement , other are ignore , as an example
         * if the method is , foo(String a , int b)
         * and if the OMElemet
         * <foo>
         *  <arg0>Val1</arg0>
         *  <arg1>Val2</arg1>
         *  <arg2>Val3</arg2>
         *
         * only the val1 and Val2 take into account
         */
        int length = javaTypes.length;
        int count = 0;
        Object[] retObjs = new Object[length];

        /*
        * If the body first child contains , then there can not be any other element withot
        * refs , so I can assume if the first child of the body first element has ref then
        * the message has to handle as mutiref message.
        * as an exmple if the body is like below
        * <foo>
        *  <arg0 href="#0"/>
        * </foo>
        *
        * then there can not be any element without refs , meaning following we are not handling
        * <foo>
        *  <arg0 href="#0"/>
        *  <arg1>absbsbs</arg1>
        * </foo>
        */
        Iterator parts = response.getChildren();
        //to handle multirefs
        //have to check the instanceof
        MultirefHelper helper = new MultirefHelper((OMElement) response.getParent());
        //to support array . if the parameter type is array , then all the omelemnts with that paramtre name
        // has to  get and add to the list
        Class classType;
        String currentLocalName;
        Type[] genericParameterTypes = null;
        if (method != null) {
            genericParameterTypes = method.getGenericParameterTypes();
        }
        Type genericType = null;
        while (parts.hasNext() && count < length) {
            Object objValue = parts.next();
            OMElement omElement;
            if (objValue instanceof OMElement) {
                omElement = (OMElement) objValue;
            } else {
                continue;
            }

            // if the local part is not match. this means element is not present
            // due to min occurs zero.
            // we need to hard code arg and item since that has been used in RPCService client
            // and some test cases
            while ((parameterNames != null) &&
                   (!omElement.getQName().getLocalPart().startsWith("arg")) &&
                   (!omElement.getQName().getLocalPart().startsWith("item")) &&
                   !omElement.getQName().getLocalPart().equals(parameterNames[count])) {
                // POJO handles OMElement in a differnt way so need this check for OMElement
                Class paramClassType = (Class) javaTypes[count];
                if (!paramClassType.getName().equals(OMElement.class.getName())) {
                    count++;
                } else {
                    break;
                }
            }

            currentLocalName = omElement.getLocalName();
            classType = (Class) javaTypes[count];
            if (genericParameterTypes != null) {
                genericType = genericParameterTypes[count];
            }
            omElement = ProcessElement(classType, omElement, helper, parts,
                                       currentLocalName, retObjs, count, objectSupplier, genericType);
            while (omElement != null) {
                count++;
                // if the local part is not match. this means element is not present
                // due to min occurs zero.
                // we need to hard code arg and item since that has been used in RPCService client
                // and some test cases
                while ((parameterNames != null) &&
                       (!omElement.getQName().getLocalPart().startsWith("arg")) &&
                       (!omElement.getQName().getLocalPart().startsWith("item")) &&
                       !omElement.getQName().getLocalPart().equals(parameterNames[count])) {
                    // POJO handles OMElement in a differnt way so need this check for OMElement
                    Class paramClassType = (Class) javaTypes[count];
                    if (!paramClassType.getName().equals(OMElement.class.getName())) {
                        count++;
                    } else {
                        break;
                    }
                }

                currentLocalName = omElement.getLocalName();
                classType = (Class) javaTypes[count];
                if (genericParameterTypes != null) {
                    genericType = genericParameterTypes[count];
                }
                omElement = ProcessElement((Class) javaTypes[count], omElement,
                                           helper, parts, omElement.getLocalName(), retObjs, count,
                                           objectSupplier, genericType);
            }
            count++;
        }

        // Ensure that we have at least a zero element array
        for (int i = 0; i < length; i++) {
            Class clazz = (Class) javaTypes[i];
            if (retObjs[i] == null && clazz.isArray()) {
                retObjs[i] = Array.newInstance(clazz.getComponentType(), 0);
            }
        }

        helper.clean();
        return retObjs;
    }

    private static OMElement ProcessElement(Class classType, OMElement omElement,
                                            MultirefHelper helper, Iterator parts,
                                            String currentLocalName,
                                            Object[] retObjs,
                                            int count,
                                            ObjectSupplier objectSupplier,
                                            Type genericType) throws AxisFault {
        Object objValue;
        if (classType.isArray()) {
            boolean done = true;
            ArrayList<Object> valueList = new ArrayList<Object>();
            Class arrayClassType = classType.getComponentType();
            if ("byte".equals(arrayClassType.getName())) {
                retObjs[count] =
                        processObject(omElement, arrayClassType, helper, true, objectSupplier, genericType);
                return null;
            } else {
                valueList.add(processObject(omElement, arrayClassType, helper, true,
                                            objectSupplier, genericType));
            }
            while (parts.hasNext()) {
                objValue = parts.next();
                if (objValue instanceof OMElement) {
                    omElement = (OMElement) objValue;
                } else {
                    continue;
                }
                if (!currentLocalName.equals(omElement.getLocalName())) {
                    done = false;
                    break;
                }
                Object o = processObject(omElement, arrayClassType,
                                         helper, true, objectSupplier, genericType);
                valueList.add(o);
            }
            if (valueList.get(0) == null) {
                retObjs[count] = null;
            } else {
                retObjs[count] = ConverterUtil.convertToArray(arrayClassType,
                                                              valueList);
            }
            if (!done) {
                return omElement;
            }
        } else {
            //handling refs
            retObjs[count] = processObject(omElement, classType, helper, false, objectSupplier, genericType);
        }
        return null;
    }

    private static List<Object> ProcessGenericsElement(Class classType, OMElement omElement,
                                               MultirefHelper helper, Iterator parts,
                                               ObjectSupplier objectSupplier,
                                               Type genericType) throws AxisFault {
        Object objValue;
        ArrayList<Object> valueList = new ArrayList<Object>();
        while (parts.hasNext()) {
            objValue = parts.next();
            if (objValue instanceof OMElement) {
                omElement = (OMElement) objValue;
            } else {
                continue;
            }
            Object o = processObject(omElement, classType,
                                     helper, true, objectSupplier, genericType);
            valueList.add(o);
        }
        return valueList;
    }


    public static Object processObject(OMElement omElement,
                                       Class classType,
                                       MultirefHelper helper,
                                       boolean isArrayType,
                                       ObjectSupplier objectSupplier,
                                       Type generictype) throws AxisFault {
        boolean hasRef = false;
        OMAttribute omatribute = MultirefHelper.processRefAtt(omElement);
        String ref = null;
        if (omatribute != null) {
            hasRef = true;
            ref = MultirefHelper.getAttvalue(omatribute);
        }
        if (OMElement.class.isAssignableFrom(classType)) {
            if (hasRef) {
                OMElement elemnt = helper.getOMElement(ref);
                if (elemnt == null) {
                    return helper.processOMElementRef(ref);
                } else {
                    return elemnt;
                }
            } else
                return omElement;
        } else {
            if (hasRef) {
                if (helper.getObject(ref) != null) {
                    return helper.getObject(ref);
                } else {
                    return helper.processRef(classType, ref, objectSupplier);
                }
            } else {
                OMAttribute attribute = omElement.getAttribute(
                        new QName("http://www.w3.org/2001/XMLSchema-instance", "nil", "xsi"));
                if (attribute != null) {
                    return null;
                }
                if (SimpleTypeMapper.isSimpleType(classType)) {
                    if (isArrayType && "byte".equals(classType.getName())) {
                        String value = omElement.getText();
                        return Base64.decode(value);
                    } else {
                        return getSimpleTypeObjectChecked(classType, omElement);
                    }
                } else if (SimpleTypeMapper.isCollection(classType)) {
                    if (generictype != null && (generictype instanceof ParameterizedType)) {
                        ParameterizedType aType = (ParameterizedType) generictype;
                        Type[] parameterArgTypes = aType.getActualTypeArguments();
                        Type parameter = parameterArgTypes[0];
                        Iterator parts = omElement.getChildElements();
                        return ProcessGenericsElement((Class) parameter, omElement, helper, parts, objectSupplier, generictype);
                    }
                    return SimpleTypeMapper.getArrayList(omElement);
                } else if (SimpleTypeMapper.isDataHandler(classType)) {
                    return SimpleTypeMapper.getDataHandler(omElement);
                } else {
                    return BeanUtil.deserialize(classType, omElement, objectSupplier, null);
                }
            }
        }
    }


    public static OMElement getOMElement(QName opName,
                                         Object[] args,
                                         QName partName,
                                         boolean qualifed,
                                         TypeTable typeTable) {
        ArrayList<Object> objects;
        objects = new ArrayList<Object>();
        int argCount = 0;
        for (Object arg : args) {
            if (arg == null) {
                if (partName == null) {
                    objects.add("item" + argCount);
                } else {
                    objects.add(partName);
                }
                objects.add(arg);
                continue;
            }

            if (arg instanceof Object[]) {
                // at the client side the partname is always null. At client side this means user try to
                // invoke a service with an array argument.
                if (partName == null) {
                    Object array[] = (Object[]) arg;
                    for (Object o : array) {
                        if (o == null) {
                            objects.add("item" + argCount);
                            objects.add(o);
                        } else {
                            if (SimpleTypeMapper.isSimpleType(o)) {
                                objects.add("item" + argCount);
                                objects.add(SimpleTypeMapper.getStringValue(o));
                            } else {
                                objects.add(new QName("item" + argCount));
                                if (o instanceof OMElement) {
                                    OMFactory fac = OMAbstractFactory.getOMFactory();
                                    OMElement wrappingElement;
                                    wrappingElement = fac.createOMElement("item" + argCount, null);
                                    wrappingElement.addChild((OMElement) o);
                                    objects.add(wrappingElement);
                                } else {
                                    objects.add(o);
                                }
                            }
                        }
                    }
                } else {
                    // this happens at the server side. this means it is an multidimentional array.
                    objects.add(partName);
                    objects.add(arg);
                }

            } else {
                if (SimpleTypeMapper.isSimpleType(arg)) {
                    if (partName == null) {
                        objects.add("arg" + argCount);
                    } else {
                        objects.add(partName);
                    }
                    objects.add(SimpleTypeMapper.getStringValue(arg));
                } else {
                    if (partName == null) {
                        objects.add(new QName("arg" + argCount));
                    } else {
                        objects.add(partName);
                    }
                    if (arg instanceof OMElement) {
                        OMFactory fac = OMAbstractFactory.getOMFactory();
                        OMElement wrappingElement;
                        if (partName == null) {
                            wrappingElement = fac.createOMElement("arg" + argCount, null);
                            wrappingElement.addChild((OMElement) arg);
                        } else {
                            wrappingElement = fac.createOMElement(partName, null);
                            wrappingElement.addChild((OMElement) arg);
                        }
                        objects.add(wrappingElement);
                    } else if (arg instanceof byte[]) {
                        objects.add(Base64.encode((byte[]) arg));
                    } else if (SimpleTypeMapper.isDataHandler(arg.getClass())) {
                        OMFactory fac = OMAbstractFactory.getOMFactory();
                        OMElement wrappingElement;
                        if (partName == null) {
                            wrappingElement = fac.createOMElement("arg" + argCount, null);
                        } else {
                            wrappingElement = fac.createOMElement(partName, null);
                        }
                        OMText text = fac.createOMText(arg, true);
                        wrappingElement.addChild(text);
                        objects.add(wrappingElement);
                    } else {
                        objects.add(arg);
                    }
                }
            }
            argCount++;
        }

        XMLStreamReader xr =
                new ADBXMLStreamReaderImpl(opName, objects.toArray(), null, typeTable, qualifed);

        StreamWrapper parser = new StreamWrapper(xr);
        OMXMLParserWrapper stAXOMBuilder =
                OMXMLBuilderFactory.createStAXOMBuilder(
                        OMAbstractFactory.getSOAP11Factory(), parser);
        return stAXOMBuilder.getDocumentElement();
    }

    /**
     * @deprecated Please use getUniquePrefix
     */
    public static synchronized String getUniquePrifix() {
        return getUniquePrefix();
    }

    /**
     * increments the namespace counter and returns a new prefix
     *
     * @return unique prefix
     */
    public static synchronized String getUniquePrefix() {
        if (nsCount > 1000) {
            nsCount = 1;
        }
        return "s" + nsCount++;
    }


    private static String getQualifiedName(Package packagez) {
        if (packagez != null) {
            return packagez.getName();
        } else {
            return "";
        }
    }

    private static Object getSimpleTypeObjectChecked(Class classType,
                                                     OMElement omElement) throws AxisFault {
        try {
            return SimpleTypeMapper.getSimpleTypeObject(classType, omElement);
        } catch (NumberFormatException e) {
            MessageContext msgContext = MessageContext.getCurrentMessageContext();
            QName faultCode = msgContext != null ?
                              msgContext.getEnvelope().getVersion().getSenderFaultCode() :
                              null;

            throw new AxisFault("Invalid value \"" + omElement.getText() + "\" for element " +
                                omElement.getLocalName(), faultCode, e);
        }
    }

}
