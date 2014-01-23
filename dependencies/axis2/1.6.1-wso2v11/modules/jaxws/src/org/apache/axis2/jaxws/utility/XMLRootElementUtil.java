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

package org.apache.axis2.jaxws.utility;

import org.apache.axis2.java.security.AccessController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.namespace.QName;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * 
 */
public class XMLRootElementUtil {

    private static final Log log = LogFactory.getLog(XMLRootElementUtil.class);

    /** Constructor is intentionally private.  This class only provides static utility methods */
    private XMLRootElementUtil() {

    }

    /**
     * @param clazz
     * @return namespace of root element qname or null if this is not object does not represent a
     *         root element
     */
    public static QName getXmlRootElementQNameFromObject(Object obj) {

        // A JAXBElement stores its name
        if (obj instanceof JAXBElement) {
            return ((JAXBElement)obj).getName();
        }

        Class clazz = (obj instanceof java.lang.Class) ? (Class)obj : obj.getClass();
        return getXmlRootElementQName(clazz);
    }

    /**
     * @param clazz
     * @return namespace of root element qname or null if this is not object does not represent a
     *         root element
     */
    public static QName getXmlRootElementQName(Class clazz) {

        // See if the object represents a root element
        XmlRootElement root = (XmlRootElement)
            getAnnotation(clazz,XmlRootElement.class);
        if (root == null) {
            return null;
        }

        String name = root.name();
        String namespace = root.namespace();

        // The name may need to be defaulted
        if (name == null || name.length() == 0 || name.equals("##default")) {
            name = getSimpleName(clazz.getCanonicalName());
        }

        // The namespace may need to be defaulted
        if (namespace == null || namespace.length() == 0 || namespace.equals("##default")) {
            Package pkg = clazz.getPackage();
            XmlSchema schema = (XmlSchema)
                getAnnotation(pkg, XmlSchema.class);
            if (schema != null) {
                namespace = schema.namespace();
            } else {
                namespace = "";
            }
        }

        return new QName(namespace, name);
    }
    
    /**
     * @param clazz
     * @return namespace of root element qname or null if this is object does not represent a root element
     */
    public static String getEnumValue(Enum myEnum){
		Field f;
		String value;
		try {
			f = myEnum.getClass().getField(myEnum.name());
			
			f.setAccessible(true);
			
			XmlEnumValue xev = (XmlEnumValue) getAnnotation(f, XmlEnumValue.class);
			if (xev == null){
				value = f.getName();
			} else {
				value = xev.value();
			}
		} catch (SecurityException e) {
			value = null;
		} catch (NoSuchFieldException e) {
			value = null;
		}
		
		return value;
    }
    
    /**
     * @param clazz
     * @return namespace of @XmlType or null if this is object does not represent a root element
     */
    public static QName getXmlTypeQName(Class clazz) {

        // See if the object has an @XmlType
        XmlType t = (XmlType) getAnnotation(clazz,XmlType.class);
        if (t == null) {
            return null;
        }

        String name = t.name();
        String namespace = t.namespace();

        // The name may need to be defaulted
        if (name == null || name.length() == 0 || name.equals("##default")) {
            name = getSimpleName(clazz.getCanonicalName());
        }

        // The namespace may need to be defaulted
        if (namespace == null || namespace.length() == 0 || namespace.equals("##default")) {
            Package pkg = clazz.getPackage();
            XmlSchema schema = (XmlSchema) getAnnotation(pkg,XmlSchema.class);
            if (schema != null) {
                namespace = schema.namespace();
            } else {
                namespace = "";
            }
        }

        return new QName(namespace, name);
    }

    /**
     * utility method to get the last token in a "."-delimited package+classname string
     *
     * @return
     */
    private static String getSimpleName(String in) {
        if (in == null || in.length() == 0) {
            return in;
        }
        String out = null;
        StringTokenizer tokenizer = new StringTokenizer(in, ".");
        if (tokenizer.countTokens() == 0)
            out = in;
        else {
            while (tokenizer.hasMoreTokens()) {
                out = tokenizer.nextToken();
            }
        }
        return out;
    }

    /**
     * The JAXBClass has a set of bean properties each represented by a PropertyDescriptor Each of
     * the fields of the class has an associated xml name. The method returns a map where the key is
     * the xml name and value is the PropertyDescriptor
     *
     * @param jaxbClass
     * @return map
     */
    public static Map<String, PropertyDescriptorPlus> createPropertyDescriptorMap(Class jaxbClass)
            throws NoSuchFieldException, IntrospectionException {

        if (log.isDebugEnabled()) {
            log.debug("Get the PropertyDescriptor[] for " + jaxbClass);
        }

        PropertyDescriptor[] pds = Introspector.getBeanInfo(jaxbClass).getPropertyDescriptors();
        Map<String, PropertyDescriptorPlus> map = new HashMap<String, PropertyDescriptorPlus>();

        // Unfortunately the element names are stored on the fields.
        // Get all of the fields in the class and super classes

        List<Field> fields = getFields(jaxbClass);

        // Now match up the fields with the property descriptors...Sigh why didn't JAXB put the @XMLElement annotations on the 
        // property methods!
        for (PropertyDescriptor pd : pds) {

            // Skip over the class property..it is never represented as an xml element
            if (pd.getName().equals("class")) {
                continue;
            }

            // For the current property, find a matching field...so that we can get the xml name
            boolean found = false;
            if (log.isDebugEnabled()) {
                log.debug("  Start: Find xmlname for property:" + pd.getName());
            }
            for (Field field : fields) {
                String fieldName = field.getName();

                // Use the name of the field and property to find the match
                if (fieldName.equalsIgnoreCase(pd.getDisplayName()) ||
                        fieldName.equalsIgnoreCase(pd.getName())) {
                    // Get the xmlElement name for this field
                    QName xmlName = getXmlElementRefOrElementQName(field.getDeclaringClass(), field);
                    found = true;
                    if (log.isDebugEnabled()) {
                        log.debug("    Found field " + field.getName() + " which has xmlname=" +
                                xmlName);
                    }
                    if (map.get(xmlName) != null) {
                        if (log.isDebugEnabled()) {
                            log.debug("    ALERT: property " + map.get(xmlName).getPropertyName() +
                                    " already has this same xmlName..this may cause problems.");
                        }
                    }
                    map.put(xmlName.getLocalPart(), new PropertyDescriptorPlus(pd, xmlName));
                    break;
                }

                // Unfortunately, sometimes the field name is preceeded by an underscore
                if (fieldName.startsWith("_")) {
                    fieldName = fieldName.substring(1);
                    if (fieldName.equalsIgnoreCase(pd.getDisplayName()) ||
                            fieldName.equalsIgnoreCase(pd.getName())) {
                        // Get the xmlElement name for this field
                        QName xmlName = getXmlElementRefOrElementQName(field.getDeclaringClass(), field);
                        found = true;
                        if (log.isDebugEnabled()) {
                            log.debug("    Found field " + field.getName() + " which has xmlname=" +
                                    xmlName);
                        }
                        if (map.get(xmlName) != null) {
                            if (log.isDebugEnabled()) {
                                log.debug("    ALERT: property " +
                                        map.get(xmlName).getPropertyName() +
                                        " already has this same xmlName..this may cause problems.");
                            }
                        }
                        map.put(xmlName.getLocalPart(), new PropertyDescriptorPlus(pd, xmlName));
                        break;
                    }
                }
            }

            // We didn't find a field.  Default the xmlname to the property name
            if (!found) {
                String xmlName = pd.getName();
                if (log.isDebugEnabled()) {
                    log.debug("    A matching field was not found.  Defaulting xmlname to " +
                            xmlName);
                }
                if (map.get(xmlName) != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("    ALERT: property " + map.get(xmlName).getPropertyName() +
                                " already has this same xmlName..this may cause problems.");
                    }
                }
                map.put(xmlName, new PropertyDescriptorPlus(pd, xmlName));
            }
            if (log.isDebugEnabled()) {
                log.debug("  End: Find xmlname for property:" + pd.getName());
            }
        }
        return map;
    }

    /**
     * Gets all of the fields in this class and the super classes
     *
     * @param beanClass
     * @return
     */
    static private List<Field> getFields(final Class beanClass) {
        // This class must remain private due to Java 2 Security concerns
        List<Field> fields;
        fields = (List<Field>)AccessController.doPrivileged(
                new PrivilegedAction() {
                    public Object run() {
                        List<Field> fields = new ArrayList<Field>();
                        Class cls = beanClass;
                        while (cls != null) {
                            Field[] fieldArray = cls.getDeclaredFields();
                            for (Field field : fieldArray) {
                                fields.add(field);
                            }
                            cls = cls.getSuperclass();
                        }
                        return fields;
                    }
                }
        );

        return fields;
    }

    /**
     * Get the name of the field by looking at the XmlElement annotation.
     *
     * @param jaxbClass
     * @param fieldName
     * @return
     * @throws NoSuchFieldException
     */
    private static QName getXmlElementRefOrElementQName(Class jaxbClass, Field field)
            throws NoSuchFieldException {
        XmlElementRef xmlElementRef = (XmlElementRef)
                getAnnotation(field, XmlElementRef.class);
        if (xmlElementRef != null) {
            return new QName(xmlElementRef.namespace(),
                    xmlElementRef.name());
        }
        XmlElement xmlElement = (XmlElement)
                getAnnotation(field, XmlElement.class);

        // If XmlElement does not exist, default to using the field name
        if (xmlElement == null ||
                xmlElement.name().equals("##default")) {
            return new QName("", field.getName());
        }
        return new QName(xmlElement.namespace(),
                xmlElement.name());
    }

    /**
     * Get an annotation.  This is wrappered to avoid a Java2Security violation.
     * @param cls Class that contains annotation 
     * @param annotation Class of requrested Annotation
     * @return annotation or null
     */
    private static Annotation getAnnotation(final AnnotatedElement element, final Class annotation) {
        return (Annotation) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return element.getAnnotation(annotation);
            }
        });
    }
}
