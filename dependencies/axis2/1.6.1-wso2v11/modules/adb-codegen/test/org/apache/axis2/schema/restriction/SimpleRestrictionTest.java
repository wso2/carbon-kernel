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

package org.apache.axis2.schema.restriction;

import org.apache.axis2.schema.AbstractTestCase;

import javax.xml.namespace.QName;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class SimpleRestrictionTest extends AbstractTestCase {

    public void testSimpleAttribute() throws Exception {

        TestSimpleAttributeElement testSimpleAttributeElement = new TestSimpleAttributeElement();

        TestSimpleAttribute testSimpleAttribute = new TestSimpleAttribute();
        testSimpleAttributeElement.setTestSimpleAttributeElement(testSimpleAttribute);
        testSimpleAttribute.setTestElement1(new QName("http://wso2.com", "test1"));
        testSimpleAttribute.setTestElement2(new QName("http://wso2.com", "test2"));
        testSimpleAttribute.setTestElement3(new QName("http://wso2.com", "test3"));

        ParentSimpleType parentSimpleType1 = new ParentSimpleType();
        parentSimpleType1.setChildSimpleType("test simple type 1");

        ParentSimpleType parentSimpleType2 = new ParentSimpleType();
        parentSimpleType2.setChildSimpleType("test simple type 2");

        testSimpleAttribute.setAttrib1(parentSimpleType1);
        testSimpleAttribute.setAttrib2(parentSimpleType2);

        testSerializeDeserialize(testSimpleAttributeElement, false);
    }

    public void testNormalSimpleTypeElement() throws Exception {

        NormalSimpleTypeElement normalSimpleTypeElement = new NormalSimpleTypeElement();
        ParentNormalSimpleType parentNormalSimpleType = new ParentNormalSimpleType();
        normalSimpleTypeElement.setNormalSimpleTypeElement(parentNormalSimpleType);
        parentNormalSimpleType.setNormalSimpleType(new QName("http://wso2.com", "test"));
        testSerializeDeserialize(normalSimpleTypeElement, false);

    }

    public void testEnumerationSimpleTypeElement() throws Exception {

        EnumerationSimpleTypeElement enumerationSimpleTypeElement = new EnumerationSimpleTypeElement();
        enumerationSimpleTypeElement.setEnumerationSimpleTypeElement(ParentEnumerationSimpleType.value1);
        testSerializeDeserialize(enumerationSimpleTypeElement, false);

    }

    public void testComplexRestrictionType() throws Exception {

        ComplexRestrictionTypeTestElement complexRestrictionTypeTestElement = new ComplexRestrictionTypeTestElement();
        ParentRestrictionType parentRestrictionType = new ParentRestrictionType();
        complexRestrictionTypeTestElement.setComplexRestrictionTypeTestElement(parentRestrictionType);
        parentRestrictionType.setBaseTypeElement1("test 1");
        parentRestrictionType.setBaseTypeElement2(5);
        testSerializeDeserialize(complexRestrictionTypeTestElement, false);

    }

    public void testPersonElement() throws Exception {
        PersonElement personElement = new PersonElement();
        Person person = new Person();
        personElement.setPersonElement(person);
        person.setName("amila");
        person.setAge(23);
        person.setHairColor(HairColor_type1.black);
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.DAY_OF_MONTH, 7);
        cal.set(Calendar.MONTH, Calendar.FEBRUARY);
        cal.set(Calendar.YEAR, 1977);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        person.setBirthDate(cal.getTime());
        Address address = new Address();
        person.setAddress(address);
        address.setCity("Galle");
        address.setLine1("line1");
        address.setLine2("line2");
        address.setState("state");
        Zip_type1 ziptype = new Zip_type1();
        address.setZip(ziptype);
        ziptype.setZip_type0("C");

        testSerializeDeserialize(personElement, false);
    }
}
