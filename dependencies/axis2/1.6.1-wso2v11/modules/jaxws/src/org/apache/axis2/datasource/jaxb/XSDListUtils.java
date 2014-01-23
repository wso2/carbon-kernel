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

package org.apache.axis2.datasource.jaxb;

import org.apache.axis2.jaxws.i18n.Messages;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.StringTokenizer;

/** Utilities to convert to/from xsd:list String to Object[]/List values. */
public class XSDListUtils {
    /** Constructor is intentionally private */
    private XSDListUtils() {
    }

    //  Example:
    // <xsd:simpleType name="LongList">
    //   <xsd:list>
    //     <xsd:simpleType>
    //       <xsd:restriction base="xsd:unsignedInt"/>
    //     </xsd:simpleType>
    //   </xsd:list>
    // </xsd:simpleType>
    // <element name="myLong" nillable="true" type="impl:LongList"/>
    //
    // LongList will be represented as an int[]
    // On the wire myLong will be represented as a list of integers
    // with intervening whitespace
    //   <myLong>1 2 3</myLong>
    //
    // Unfortunately, sometimes we want to marshal by type.  Therefore
    // we want to marshal an element (foo) that is unknown to schema.
    // If we use the normal marshal code, the wire will look like
    // this (which is incorrect):
    //  <foo><item>1</item><item>2</item><item>3</item></foo>
    //
    // The solution is to detect this situation and marshal the 
    // String instead.  Then we get the correct wire format:
    // <foo>1 2 3</foo>
    // 
    // This utility contains code to convert from Array/List -> String
    // and from String -> Array/List

    /**
     * Convert to String that can be used as an xsd:list content
     *
     * @param container Object
     * @return xsd:list String
     */
    public static String toXSDListString(Object container) throws NoSuchMethodException,
            IllegalArgumentException, InstantiationException, IllegalAccessException,
            InvocationTargetException {
        // TODO only supports arrays right now.  Need to implement this for List
    	if (container !=null && container.getClass().isArray()) {
            String xsdString = "";
            for (int i = 0; i < Array.getLength(container); i++) {
                Object component = Array.get(container, i);
                if (xsdString.length() != 0) {
                    xsdString += " ";
                }
                xsdString += getAsText(component);
            }
            return xsdString;
            
        } else if(container!=null && List.class.isAssignableFrom(container.getClass())){
            String xsdString = "";
            List containerAsList = (List)container;
            for (Object component:containerAsList) {
                if (xsdString.length() != 0) {
                    xsdString += " ";
                }
                xsdString += getAsText(component);
            }
            return xsdString;
            
        }
        else {
            throw new IllegalArgumentException(container.getClass().toString());
        }
    }

    /**
     * Convert from xsdListString to an array/list
     *
     * @param xsdListString
     * @param type          Class of return
     * @return Array or List
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     */
    public static Object fromXSDListString(String xsdListString, Class type) throws
            IllegalArgumentException, NoSuchMethodException, InstantiationException,
            IllegalAccessException, InvocationTargetException, ParseException,
            DatatypeConfigurationException {
        // TODO only supports arrays right now.  Need to implement this for List
        if (type.isArray()) {
            Class componentType = type.getComponentType();
            List list = new ArrayList();

            // Parse the tokens based on whitespace
            StringTokenizer st = new StringTokenizer(xsdListString);
            while (st.hasMoreTokens()) {
                String text = st.nextToken();
                Object componentObject = getFromText(text, componentType);
                list.add(componentObject);
            }
            Class arrayType = componentType;
            if (componentType.isPrimitive()) {
                Class boxedType = getBoxedType(componentType);
                if (boxedType != null) {
                    arrayType = boxedType;
                }
            }
            Object array = Array.newInstance(arrayType, list.size());
            return list.toArray((Object[])array);
        }else {
            throw new IllegalArgumentException(type.toString());
        }
    }

    public static Object fromStringArray(String[] items, Class type) throws Exception {
        if (type.isArray()) {
            Class componentType = type.getComponentType();
            List list = new ArrayList();

            for (String item : items) {
                Object componentObject = getFromText(item, componentType);                
                list.add(componentObject);
            }
    
            Class arrayType = componentType;
            if (componentType.isPrimitive()) {
                Class boxedType = getBoxedType(componentType);
                if (boxedType != null) {
                    arrayType = boxedType;
                }
            }

            Object array = Array.newInstance(arrayType, list.size());
            return list.toArray((Object[])array);
        } else {
            throw new IllegalArgumentException(type.toString());
        }
    }
    
    public static String[] toStringArraay(Object container) throws Exception {
        if (container != null && container.getClass().isArray()) {
            int size = Array.getLength(container);        
            String [] strArray = new String[size];
            for (int i = 0; i < size; i++) {
                Object component = Array.get(container, i);
                strArray[i] = getAsText(component);
            }
            return strArray;    
        } else if(container != null && List.class.isAssignableFrom(container.getClass())){   
            List containerAsList = (List)container;
            int size = containerAsList.size();
            String [] strArray = new String[size];
            for (int i = 0; i < size; i++) {
                strArray[i] = getAsText(containerAsList.get(i));
            }                
            return strArray;    
        } else {
            throw new IllegalArgumentException(container.getClass().toString());
        }
    }
    
    /**
     * @param obj
     * @return xml text for this object
     */
    private static String getAsText(Object obj) throws NoSuchMethodException,
            IllegalArgumentException, InstantiationException, IllegalAccessException,
            InvocationTargetException {
        // TODO Need to upgrade to handle more complicated objects like calendar and qname
        if (obj instanceof QName) {
            throw new RuntimeException(
                    Messages.getMessage("XSDListNotSupported", QName.class.getName()));
        } else if (obj instanceof XMLGregorianCalendar) {
            throw new RuntimeException(Messages.getMessage("XSDListNotSupported",
                                                           XMLGregorianCalendar.class.getName()));
        } else if (obj.getClass().isEnum()) {
            // TODO Method should be cached for performance
            Method method =
                    obj.getClass().getDeclaredMethod("value", new Class[] { });
            return (String)method.invoke(obj, new Object[] { });

        }
        return obj.toString();
    }

    /**
     * @param value
     * @param componentType
     * @return Object constructed from the specified xml text (value)
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private static Object getFromText(String value, Class componentType) throws
            NoSuchMethodException, IllegalArgumentException, InstantiationException,
            IllegalAccessException, InvocationTargetException, ParseException,
            DatatypeConfigurationException {
        // TODO This needs to be upgraded to handle more complicated objects (enum, calendar, primitive, etc.)
        if (componentType == String.class) {
            return value;
        }
        if (componentType.isEnum()) {
            // If you get an exception here, consider adding the code to convert the String value to the required component object
            // Default: Call the constructor
            // TODO Method should be cached for performance
            Method method =
                    componentType.getDeclaredMethod("fromValue", new Class[] { String.class });
            Object obj = method.invoke(null, new Object[] { value });
            return obj;
        }

        if (componentType == byte.class) {
            componentType = Byte.class;
        }
        if (componentType == short.class) {
            componentType = Short.class;
        }
        if (componentType == int.class) {
            componentType = Integer.class;
        }
        if (componentType == float.class) {
            componentType = Float.class;
        }
        if (componentType == double.class) {
            componentType = Double.class;
        }
        if (componentType == char.class) {
            Character ch = null;
            if (value != null && value.length() > 0) {
                ch = Character.valueOf(value.charAt(0));
            }
            return ch;
        }
        if (componentType == boolean.class) {
            componentType = Boolean.class;
        }

        if (componentType.equals(QName.class)) {
            // TODO Missing Support

            throw new IllegalArgumentException(
                    Messages.getMessage("XSDListNotSupported", componentType.getName()));
        } else if (componentType.equals(XMLGregorianCalendar.class)) {
            // TODO Missing Support
            throw new IllegalArgumentException(
                    Messages.getMessage("XSDListNotSupported", componentType.getName()));
        }

        // If you get an exception here, consider adding the code to convert the String value to the required component object
        // Default: Call the constructor
        Constructor constructor = componentType.getConstructor(new Class[] { String.class });
        Object obj = constructor.newInstance(new Object[] { value });
        return obj;
    }

    private static Class getBoxedType(Class primitiveType) {
        if (primitiveType == byte.class) {
            return Byte.class;
        }
        if (primitiveType == short.class) {
            return Short.class;
        }
        if (primitiveType == int.class) {
            return Integer.class;
        }
        if (primitiveType == long.class) {
            return Long.class;
        }
        if (primitiveType == float.class) {
            return Float.class;
        }
        if (primitiveType == double.class) {
            return Double.class;
        }
        if (primitiveType == char.class) {
            return Character.class;
        }
        if (primitiveType == boolean.class) {
            return Boolean.class;
        }
        return null;
    }

    private static GregorianCalendar toGregorianCalendar(String value) throws ParseException {
        Date d = new SimpleDateFormat().parse(value);
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(d);
        return gc;
    }
}
