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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.mime.MultipartWriter;
import org.apache.axiom.mime.impl.javamail.JavaMailMultipartWriterFactory;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.impl.OMMultipartWriter;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.builder.MTOMStAXSOAPModelBuilder;
import org.apache.axis2.databinding.ADBBean;
import org.apache.axis2.databinding.ADBException;
import org.apache.axis2.databinding.types.HexBinary;
import org.apache.axis2.databinding.types.Language;
import org.apache.axis2.databinding.types.URI;
import org.apache.axis2.util.XMLPrettyPrinter;

import junit.framework.TestCase;

public abstract class AbstractTestCase extends TestCase {
    // This is the set of property types that can be compared using Object#equals:
    private static final Set<Class<?>> simpleJavaTypes = new HashSet<Class<?>>(Arrays.asList(new Class<?>[] {
            String.class, Boolean.class, Boolean.TYPE, Integer.class, Integer.TYPE,
            BigInteger.class, BigDecimal.class, Date.class, QName.class,
            URI.class, Language.class, HexBinary.class
    }));
    
    private static boolean isADBBean(Class<?> beanClass) {
        return ADBBean.class.isAssignableFrom(beanClass) || beanClass.getName().startsWith("helper.");
    }
    
    private static boolean isEnum(Class<?> beanClass) {
        try {
            beanClass.getDeclaredField("_table_");
            return true;
        } catch (NoSuchFieldException ex) {
            return false;
        }
    }
    
    private static BeanInfo getBeanInfo(Class<?> beanClass) {
        try {
            return Introspector.getBeanInfo(beanClass, Object.class);
        } catch (IntrospectionException ex) {
            fail("Failed to introspect " + beanClass);
            return null; // Make compiler happy
        }
    }
    
    /**
     * Assert that two ADB beans are equal. This method recursively compares properties
     * in the bean. It supports comparison of various property types, including arrays
     * and DataHandler.
     * 
     * @param expected
     * @param actual
     */
    public static void assertBeanEquals(Object expected, Object actual) {
        if (expected == null) {
            assertNull(actual);
            return;
        }
        Class<?> beanClass = expected.getClass();
        assertEquals(beanClass, actual.getClass());
        for (PropertyDescriptor desc : getBeanInfo(beanClass).getPropertyDescriptors()) {
            String propertyName = desc.getName();
//            System.out.println("Comparing property " + propertyName);
            Method readMethod = desc.getReadMethod();
            Object expectedValue;
            Object actualValue;
            try {
                expectedValue = readMethod.invoke(expected);
                actualValue = readMethod.invoke(actual);
            } catch (Exception ex) {
                fail("Failed to get property " + propertyName + " from " + beanClass);
                return;
            }
            assertPropertyValueEquals("property " + propertyName + " in bean " + beanClass, expectedValue, actualValue);
        }
    }
    
    private static void assertPropertyValueEquals(String message, Object expected, Object actual) {
        if (expected == null) {
            assertNull(message, actual);
        } else {
            assertNotNull(message, actual);
            Class<?> type = expected.getClass();
            if (type.isArray()) {
                int expectedLength = Array.getLength(expected);
                int actualLength = Array.getLength(actual);
                assertEquals("array length for " + message, expectedLength, actualLength);
                for (int i=0; i<expectedLength; i++) {
                    assertPropertyValueEquals(message, Array.get(expected, i), Array.get(actual, i));
                }
            } else if (simpleJavaTypes.contains(type)) {
                assertEquals("value for " + message, expected, actual);
            } else if (DataHandler.class.isAssignableFrom(type)) {
                assertDataHandlerEquals((DataHandler)expected, (DataHandler)actual);
            } else if (OMElement.class.isAssignableFrom(type)) {
                assertTrue(isOMElementsEqual((OMElement)expected, (OMElement)actual));
            } else if (isADBBean(type)) {
                if (isEnum(type)) {
                    assertSame("enum value for " + message, expected, actual);
                } else {
                    assertBeanEquals(expected, actual);
                }
            } else {
                fail("Don't know how to compare values of type " + type.getName() + " for " + message);
            }
        }
    }
    
    protected static boolean isOMElementsEqual(OMElement omElement1,OMElement omElement2){
        boolean isEqual = false;
        if ((omElement1 == null) || (omElement2 == null)){
            isEqual = (omElement1 == omElement2);
        } else {
            isEqual = omElement1.getLocalName().equals(omElement2.getLocalName());
        }
        return isEqual;
    }

    private static int countDataHandlers(Object bean) throws Exception {
        int count = 0;
        for (PropertyDescriptor desc : getBeanInfo(bean.getClass()).getPropertyDescriptors()) {
            Object value = desc.getReadMethod().invoke(bean);
            if (value != null) {
                if (value instanceof DataHandler) {
                    count++;
                } else if (value.getClass().isArray()) {
                    int length = Array.getLength(value);
                    for (int i=0; i<length; i++) {
                        Object item = Array.get(value, i);
                        if (item != null) {
                            if (item instanceof DataHandler) {
                                count++;
                            } else if (isADBBean(item.getClass())) {
                                count += countDataHandlers(item);
                            }
                        }
                    }
                } else if (isADBBean(value.getClass())) {
                    count += countDataHandlers(value);
                }
            }
        }
        return count;
    }
    
    private static void assertDataHandlerEquals(DataHandler expected, DataHandler actual) {
        try {
            InputStream in1 = expected.getInputStream();
            InputStream in2 = actual.getInputStream();
            int b;
            do {
                b = in1.read();
                assertEquals(b, in2.read());
            } while (b != -1);
        } catch (IOException ex) {
            fail("Failed to read data handler");
        }
    }
    
    public static Object toHelperModeBean(ADBBean bean) throws Exception {
        Class<?> beanClass = bean.getClass();
        Object helperModeBean = null;
        do {
            Class<?> helperModeBeanClass = Class.forName("helper." + beanClass.getName());
            if (helperModeBean == null) {
                helperModeBean = helperModeBeanClass.newInstance();
            }
            for (Field field : beanClass.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    field.setAccessible(true);
                    Object value = field.get(bean);
                    if (value instanceof ADBBean) {
                        // Try to get the _table_ field if this is an enumeration
                        Map<?,?> enumValues;
                        try {
                            Field tableField = value.getClass().getDeclaredField("_table_");
                            tableField.setAccessible(true);
                            enumValues = (Map<?,?>)tableField.get(null);
                        } catch (NoSuchFieldException ex) {
                            enumValues = null;
                        }
                        if (enumValues == null) {
                            // Not an enumeration => translate is as a bean
                            value = toHelperModeBean((ADBBean)value);
                        } else {
                            Field tableField = Class.forName("helper." + value.getClass().getName()).getDeclaredField("_table_");
                            tableField.setAccessible(true);
                            Map<?,?> destEnumValues = (Map<?,?>)tableField.get(null);
                            for (Map.Entry<?,?> entry : enumValues.entrySet()) {
                                if (entry.getValue() == value) {
                                    value = destEnumValues.get(entry.getKey());
                                    break;
                                }
                            }
                        }
                    }
                    Field destField = helperModeBeanClass.getDeclaredField(field.getName());
                    destField.setAccessible(true);
                    destField.set(helperModeBean, value);
                }
            }
            beanClass = beanClass.getSuperclass();
        } while (!beanClass.equals(Object.class));
        return helperModeBean;
    }
    
    /**
     * Serialize a bean to XML and then deserialize the XML.
     * 
     * @param bean the bean to serialize
     * @return the deserialized bean
     * @throws Exception
     */
    public static ADBBean serializeDeserialize(ADBBean bean) throws Exception {
        Class<? extends ADBBean> beanClass = bean.getClass();
        OMElement omElement = bean.getOMElement(ADBBeanUtil.getQName(beanClass), OMAbstractFactory.getOMFactory());
        String omElementString = omElement.toStringWithConsume();
//        System.out.println("om string ==> " + omElementString);
        XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
        return ADBBeanUtil.parse(beanClass, xmlReader);
    }
    
    /**
     * Serialize a bean to XML, then deserialize the XML and compare the resulting bean to
     * the original. This will actually do the serialization and deserialization several times
     * using different approaches in order to increase the test coverage.
     * 
     * @param bean the bean to serialize
     * @throws Exception
     */
    public static void testSerializeDeserialize(ADBBean bean) throws Exception {
        testSerializeDeserialize(bean, bean);
    }
    
    public static void testSerializeDeserialize(ADBBean bean, boolean testGetPullParser) throws Exception {
        testSerializeDeserialize(bean, bean, testGetPullParser);
    }
    
    public static void testSerializeDeserialize(ADBBean bean, ADBBean expectedResult) throws Exception {
        testSerializeDeserialize(bean, expectedResult, true);
    }
    
    public static void testSerializeDeserialize(ADBBean bean, ADBBean expectedResult, boolean testGetPullParser) throws Exception {
        testSerializeDeserializeUsingStAX(bean, expectedResult);
        testSerializeDeserializeUsingOMStAXWrapper(bean, expectedResult);
        
        if (testGetPullParser) {
            // TODO: this badly fails for many of the test cases => there are still issues to solve!!!
            testSerializeDeserializeUsingPullParser(bean, expectedResult);
        }
        
        testSerializeDeserializeWrapped(bean, expectedResult);
        testSerializeDeserializeUsingMTOM(bean, expectedResult, true);
        testSerializeDeserializeUsingMTOM(bean, expectedResult, false);
        testSerializeDeserializeUsingMTOMWithoutOptimize(bean, expectedResult);
        testSerializeDeserializePrettified(bean, expectedResult);
        
        try {
            Class.forName("helper." + bean.getClass().getName());
        } catch (ClassNotFoundException ex) {
            // Code has not been compiled in helper mode; skip the rest of the tests.
            return;
        }
        
        Object helperModeBean = toHelperModeBean(bean);
        Object helperModeExpectedResult = toHelperModeBean(expectedResult);
        
        testSerializeDeserializeUsingStAX(helperModeBean, helperModeExpectedResult);
        testSerializeDeserializeUsingOMStAXWrapper(helperModeBean, helperModeExpectedResult);
        testSerializeDeserializeWrapped(helperModeBean, helperModeExpectedResult);
        testSerializeDeserializeUsingMTOM(helperModeBean, helperModeExpectedResult, true);
        testSerializeDeserializeUsingMTOM(helperModeBean, helperModeExpectedResult, false);
        testSerializeDeserializeUsingMTOMWithoutOptimize(helperModeBean, helperModeExpectedResult);
        testSerializeDeserializePrettified(helperModeBean, helperModeExpectedResult);
    }
    
    // Deserialization approach 1: use an XMLStreamReader produced by the StAX parser.
    private static void testSerializeDeserializeUsingStAX(Object bean, Object expectedResult) throws Exception {
        OMElement omElement = ADBBeanUtil.getOMElement(bean);
        String omElementString = omElement.toStringWithConsume();
//        System.out.println(omElementString);
        assertBeanEquals(expectedResult, ADBBeanUtil.parse(bean.getClass(),
                StAXUtils.createXMLStreamReader(new StringReader(omElementString))));
    }
    
    // Deserialization approach 2: use an Axiom tree with caching. In this case the
    // XMLStreamReader implementation is OMStAXWrapper and we test interoperability
    // between ADB and Axiom's OMStAXWrapper.
    private static void testSerializeDeserializeUsingOMStAXWrapper(Object bean, Object expectedResult) throws Exception {
        OMElement omElement = ADBBeanUtil.getOMElement(bean);
        String omElementString = omElement.toStringWithConsume();
        OMElement omElement2 = new StAXOMBuilder(StAXUtils.createXMLStreamReader(
                new StringReader(omElementString))).getDocumentElement();
        assertBeanEquals(expectedResult, ADBBeanUtil.parse(bean.getClass(), omElement2.getXMLStreamReader()));
    }
    
    // Deserialization approach 3: use the pull parser produced by ADB.
    private static void testSerializeDeserializeUsingPullParser(Object bean, Object expectedResult) throws Exception {
        assertBeanEquals(expectedResult, ADBBeanUtil.parse(bean.getClass(), ADBBeanUtil.getPullParser(bean)));
    }
    
    // Approach 4: Serialize the bean as the child of an element that declares a default namespace.
    // If ADB behaves correctly, this should not have any impact. A failure here may be an indication
    // of an incorrect usage of XMLStreamWriter#writeStartElement(String).
    private static void testSerializeDeserializeWrapped(Object bean, Object expectedResult) throws Exception {
        StringWriter sw = new StringWriter();
        XMLStreamWriter writer = StAXUtils.createXMLStreamWriter(sw);
        writer.writeStartElement("", "root", "urn:test");
        writer.writeDefaultNamespace("urn:test");
        ADBBeanUtil.serialize(bean, writer);
        writer.writeEndElement();
        writer.flush();
        OMElement omElement3 = new StAXOMBuilder(StAXUtils.createXMLStreamReader(new StringReader(sw.toString()))).getDocumentElement();
        assertBeanEquals(expectedResult, ADBBeanUtil.parse(bean.getClass(), omElement3.getFirstElement().getXMLStreamReader()));
    }
    
    private static void testSerializeDeserializeUsingMTOM(Object bean, Object expectedResult, boolean cache) throws Exception {
        SOAPEnvelope envelope = OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();
        envelope.getBody().addChild(ADBBeanUtil.getOMElement(bean));
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        OMOutputFormat format = new OMOutputFormat();
        format.setDoOptimize(true);
        envelope.serialize(buffer, format);
//        envelope.serialize(System.out, format);
        String contentType = format.getContentTypeForMTOM("text/xml");
        Attachments attachments = new Attachments(new ByteArrayInputStream(buffer.toByteArray()), contentType);
        assertEquals(countDataHandlers(bean) + 1, attachments.getAllContentIDs().length);
        MTOMStAXSOAPModelBuilder builder = new MTOMStAXSOAPModelBuilder(StAXUtils.createXMLStreamReader(attachments.getSOAPPartInputStream()), attachments);
        OMElement bodyElement = builder.getSOAPEnvelope().getBody().getFirstElement();
        assertBeanEquals(expectedResult, ADBBeanUtil.parse(bean.getClass(), cache ? bodyElement.getXMLStreamReader() : bodyElement.getXMLStreamReaderWithoutCaching()));
    }
    
    // This is a bit special: it serializes the message using MTOM, but without using any xop:Include. This checks
    // that MTOM decoding works properly even if the client uses unoptimized base64.
    private static void testSerializeDeserializeUsingMTOMWithoutOptimize(Object bean, Object expectedResult) throws Exception {
        SOAPEnvelope envelope = OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();
        envelope.getBody().addChild(ADBBeanUtil.getOMElement(bean));
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        OMOutputFormat format = new OMOutputFormat();
        MultipartWriter mpWriter = JavaMailMultipartWriterFactory.INSTANCE.createMultipartWriter(buffer, format.getMimeBoundary());
        OutputStream rootPartWriter = mpWriter.writePart("application/xop+xml; charset=UTF-8; type=\"text/xml\"", "binary", format.getRootContentId());
        envelope.serialize(rootPartWriter, format);
        rootPartWriter.close();
        mpWriter.complete();
//        System.out.write(buffer.toByteArray());
        String contentType = format.getContentTypeForMTOM("text/xml");
        Attachments attachments = new Attachments(new ByteArrayInputStream(buffer.toByteArray()), contentType);
        MTOMStAXSOAPModelBuilder builder = new MTOMStAXSOAPModelBuilder(StAXUtils.createXMLStreamReader(attachments.getSOAPPartInputStream()), attachments);
        OMElement bodyElement = builder.getSOAPEnvelope().getBody().getFirstElement();
        assertBeanEquals(expectedResult, ADBBeanUtil.parse(bean.getClass(), bodyElement.getXMLStreamReaderWithoutCaching()));
    }
    
    // This is used to check that ADB correctly handles element whitespace
    private static void testSerializeDeserializePrettified(Object bean, Object expectedResult) throws Exception {
        OMElement omElement = ADBBeanUtil.getOMElement(bean);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLPrettyPrinter.prettify(omElement, baos);
//        System.out.write(baos.toByteArray());
        assertBeanEquals(expectedResult, ADBBeanUtil.parse(bean.getClass(),
                StAXUtils.createXMLStreamReader(new ByteArrayInputStream(baos.toByteArray()))));
    }
    
    /**
     * Assert that serializing the given bean should result in an {@link ADBException}.
     * 
     * @param bean the bean to serialize
     * @throws Exception
     */
    public static void assertSerializationFailure(ADBBean bean) throws Exception {
        try {
            OMElement omElement = bean.getOMElement(ADBBeanUtil.getQName(bean.getClass()), OMAbstractFactory.getOMFactory());
            omElement.toStringWithConsume();
            fail("Expected ADBException");
        } catch (ADBException ex) {
            // OK: expected
        }
    }
}
