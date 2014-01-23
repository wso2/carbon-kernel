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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.databinding.ADBBean;
import org.apache.axis2.databinding.ADBException;
import org.apache.axis2.databinding.ADBHelper;

public class ADBBeanUtil {
    private ADBBeanUtil() {}

    private static ADBHelper<?> getHelper(Class<?> beanClass) throws Exception {
        return (ADBHelper<?>)Class.forName(beanClass.getName() + "Helper").getField("INSTANCE").get(null);
    }
    
    public static QName getQName(Class<?> beanClass) throws Exception {
        return (QName)beanClass.getField("MY_QNAME").get(null);
    }
    
    public static <T> T parse(Class<T> beanClass, XMLStreamReader reader) throws Exception {
        if (ADBBean.class.isAssignableFrom(beanClass)) {
            for (Class<?> clazz : beanClass.getDeclaredClasses()) {
                if (clazz.getSimpleName().equals("Factory")) {
                    return beanClass.cast(clazz.getMethod("parse", XMLStreamReader.class).invoke(null, reader));
                }
            }
            return null; // We should never get here
        } else {
            return beanClass.cast(getHelper(beanClass).parse(reader));
        }
    }
    
    public static OMElement getOMElement(Object bean, QName qname, OMFactory factory) throws Exception {
        if (bean instanceof ADBBean) {
            return ((ADBBean)bean).getOMElement(qname, factory);
        } else {
            return getOMElement(bean, getHelper(bean.getClass()), qname, factory);
        }
    }
    
    private static <T> OMElement getOMElement(Object bean, ADBHelper<T> helper, QName qname, OMFactory factory) throws ADBException {
        return helper.getOMElement(helper.getBeanClass().cast(bean), qname, factory);
    }
    
    public static OMElement getOMElement(Object bean) throws Exception {
        return getOMElement(bean, getQName(bean.getClass()), OMAbstractFactory.getOMFactory());
    }
    
    public static XMLStreamReader getPullParser(Object bean, QName qname) throws Exception {
        if (bean instanceof ADBBean) {
            return ((ADBBean)bean).getPullParser(qname);
        } else {
            return getPullParser(bean, getHelper(bean.getClass()), qname);
        }
    }
    
    private static <T> XMLStreamReader getPullParser(Object bean, ADBHelper<T> helper, QName qname) throws XMLStreamException {
        return helper.getPullParser(helper.getBeanClass().cast(bean), qname);
    }
    
    public static XMLStreamReader getPullParser(Object bean) throws Exception {
        return getPullParser(bean, getQName(bean.getClass()));
    }
    
    public static void serialize(Object bean, QName qname, XMLStreamWriter writer) throws Exception {
        if (bean instanceof ADBBean) {
            ((ADBBean)bean).serialize(qname, writer);
        } else {
            serialize(bean, getHelper(bean.getClass()), qname, writer);
        }
    }
    
    private static <T> void serialize(Object bean, ADBHelper<T> helper, QName qname, XMLStreamWriter writer) throws XMLStreamException {
        helper.serialize(helper.getBeanClass().cast(bean), qname, writer);
    }
    
    public static void serialize(Object bean, XMLStreamWriter writer) throws Exception {
        serialize(bean, getQName(bean.getClass()), writer);
    }
}
