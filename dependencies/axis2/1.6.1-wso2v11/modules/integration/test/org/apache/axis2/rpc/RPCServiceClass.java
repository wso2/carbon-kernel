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

package org.apache.axis2.rpc;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.databinding.utils.BeanUtil;
import org.apache.axis2.engine.DefaultObjectSupplier;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class RPCServiceClass {

    public MyBean editBean(MyBean bean, int a) {
        bean.setAge(a);
        return bean;
    }


    public MyBean echoBean(MyBean bean) {
        return bean;
    }

    public String echoString(String in) {
        return in;
    }

    public int echoInt(int i) {
        return i;
    }

    public int add(int a, int b) {
        return a + b;
    }

    public boolean echoBool(boolean b) {
        return b;
    }

    public byte echoByte(byte b) {
        return b;
    }

    public OMElement echoOM(OMElement b) {
        b.build();
//        SOAPFactory fac = OMAbstractFactory.getSOAP12Factory();
//        OMNamespace ns = fac.createOMNamespace(
//                "http://soapenc/", "res");
//        OMElement bodyContent = fac.createOMElement(
//                "echoOMResponse", ns);
//        OMElement child = fac.createOMElement("return", null);
//        child.addChild(fac.createOMText(child, b.getText()));
//        bodyContent.addChild(child);
////        bodyContent.addChild(b);
        return (OMElement)b.detach();
    }

    public double divide(double a, double b) {
        return (a / b);
    }

    public Calendar echoCalander(Calendar in) {
        return in;
    }

    public OMElement multireturn(OMElement ele) throws XMLStreamException {
        SOAPFactory fac = OMAbstractFactory.getSOAP12Factory();
        OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "res");
        OMElement method = fac.createOMElement("multiretuenResponse", omNs);
        OMElement value1 = fac.createOMElement("return0", null);
        value1.addChild(
                fac.createOMText(value1, "10"));
        method.addChild(value1);
        OMElement value2 = fac.createOMElement("return1", null);
        value2.addChild(
                fac.createOMText(value2, "foo"));
        method.addChild(value2);
        return method;
    }


    /**
     * This methods return mutiple object , so it creat an Object array and retuen that so , if a
     * method want to return mutiple value , this way can be used
     *
     * @param obj
     * @return Object []
     */
    public Object[] mulReturn(OMElement obj) {
        ArrayList objs = new ArrayList();
        objs.add(new Integer(100));
        MyBean bean = new MyBean();
        bean.setAge(100);
        bean.setName("Deepal");
        bean.setValue(false);
        AddressBean ab = new AddressBean();
        ab.setNumber(1010);
        ab.setTown("Colombo3");
        bean.setAddress(ab);
        objs.add(bean);
        return objs.toArray();
    }

    public MyBean beanOM(OMElement element, int val) throws AxisFault {
        MyBean bean = (MyBean)BeanUtil
                .deserialize(MyBean.class, element, new DefaultObjectSupplier(), null);
        bean.setAge(val);
        return bean;
    }

    public boolean omrefs(OMElement element, OMElement element2) throws AxisFault {
        MyBean bean = (MyBean)BeanUtil
                .deserialize(MyBean.class, element, new DefaultObjectSupplier(), null);
        MyBean bean2 = (MyBean)BeanUtil
                .deserialize(MyBean.class, element2, new DefaultObjectSupplier(), null);
        return bean2 != null && bean != null;
    }

    public String handleArrayList(ArrayList list, int b) {
        String str = "";
        for (int i = 0; i < list.size(); i++) {
            Object obj = list.get(i);
            if (obj instanceof OMElement) {
                OMElement omElement = (OMElement)obj;
                str = str + omElement.getText();
            }
        }
        return str + b;
    }

    public Employee echoEmployee(Employee em) {
        return em;
    }

    public int testCompanyArray(Company [] com) {
        return com.length;
    }

    public Company [] CompanyArray(Company [] com) {
        ArrayList res = new ArrayList();
        for (int i = 0; i < com.length; i++) {
            Company company = com[i];
            res.add(company);
        }
        return (Company [])res.toArray(new Company[res.size()]);
    }


    public Company echoCompany(Company com) throws AxisFault {
        ArrayList pss = com.getPersons();
        ArrayList tems = new ArrayList();
        for (int i = 0; i < pss.size(); i++) {
            OMElement omElement = (OMElement)pss.get(i);
            Person p = (Person)BeanUtil
                    .deserialize(Person.class, omElement, new DefaultObjectSupplier(), null);
            tems.add(p);
        }
        com.setPersons(tems);
        return com;
    }

    public void handlAnyThing(String value1, int abc, Date date) {

    }

    public boolean handleStringArray(String [] value) {
        return value.length > 0;
    }

    public int omElementArray(OMElement [] omElement) {
        return omElement.length;
    }

    public Mail echoMail(Mail mail) {
        return mail;
    }

    public int multiArrays(String [] a, String b [], String d [], int c) {
        return a.length + b.length + d.length + c;
    }

    public String testByteArray(byte [] value) {
        return new String(value);
    }
}
