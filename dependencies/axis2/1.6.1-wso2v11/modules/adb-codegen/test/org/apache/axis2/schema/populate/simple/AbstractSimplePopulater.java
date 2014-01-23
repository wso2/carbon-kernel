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

package org.apache.axis2.schema.populate.simple;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axis2.schema.populate.Util;
import org.custommonkey.xmlunit.XMLTestCase;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;

public abstract class AbstractSimplePopulater extends XMLTestCase {

    // force others to implement this method
    public abstract void testPopulate() throws Exception;

    protected  String className= null;
    protected Class propertyClass = null;


    /**
     *  Simple reusable method to make object instances via reflection
     * @param testString
     * @param className
     * @return
     * @throws Exception
     */
    protected Object process(String testString,String className) throws Exception{
        XMLStreamReader reader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(testString.getBytes()));
        Class clazz = Class.forName(className);
        Class innerClazz = Util.getFactory(clazz);
        Method parseMethod = innerClazz.getMethod("parse",new Class[]{XMLStreamReader.class});
        Object obj = parseMethod.invoke(null,new Object[]{reader});
        assertNotNull(obj);

        return obj;

    }

    /**
     * Simple method to compare. May be overridden
     * @param xmlToSet
     * @param value
     * @throws Exception
     */
    protected void checkValue(String xmlToSet, String value) throws Exception {
        Object o = process(xmlToSet, className);
        Class beanClass = Class.forName(className);
        BeanInfo info = Introspector.getBeanInfo(beanClass);
        PropertyDescriptor[] propDescs = info.getPropertyDescriptors();
        for (int i = 0; i < propDescs.length; i++) {
            PropertyDescriptor propDesc = propDescs[i];
            if  (propDesc.getPropertyType().equals(propertyClass)){
                String s = convertToString(propDesc.getReadMethod().invoke(o,
                        (Object[]) null));
                compare(value,s);
            }

        }

    }

     /**
     * Compares serializations - may be overridden
     * @param o
     * @param xmlToSet
     * @throws Exception
     */
    protected void CompareOMElemntSerializations(Object o, String xmlToSet) throws Exception {
        OMElement element = getOMElement(o);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(baos);
        element.serialize(writer);
        writer.flush();

        assertXMLEqual(baos.toString(),xmlToSet);
    }

    /**
     * Get OM element via reflection
     * @param bean
     * @return
     * @throws Exception
     */
    protected OMElement getOMElement(Object bean) throws Exception {
        Method method = bean.getClass().getMethod("getOMElement", new Class[]{
                javax.xml.namespace.QName.class,
                org.apache.axiom.om.OMFactory.class});

        return (OMElement) method.invoke(bean, new Object[]{null, OMAbstractFactory.getOMFactory()});

    }
    /**
     * value comparisom
     * @param val1
     * @param val2
     */
    protected void compare(String val1,String val2){
         assertEquals(val1,val2);
    }

    /**
     * conversion - may be overriddens
     * @param o
     * @return
     */
    protected String convertToString(Object o){
        return o.toString();
    }
}
