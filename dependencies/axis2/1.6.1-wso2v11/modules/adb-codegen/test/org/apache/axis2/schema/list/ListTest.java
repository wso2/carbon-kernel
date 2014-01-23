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

package org.apache.axis2.schema.list;

import org.apache.axis2.schema.AbstractTestCase;

import javax.xml.namespace.QName;

public class ListTest extends AbstractTestCase {


    public void testListString() throws Exception {

        TestListStringElement testListStringElement = new TestListStringElement();
        TestListString testListString = new TestListString();
        testListStringElement.setTestListStringElement(testListString);
        testListString.setString(new String[]{"string1","string2"});

        testSerializeDeserialize(testListStringElement, false);
    }

    public void testListQName() throws Exception {

        TestListQNameElement testListQNameElement = new TestListQNameElement();
        TestListQName testListQName = new TestListQName();
        testListQName.setQName(new QName[]{new QName("http://www.google.com","test1"),
                        new QName("http://www.google.com","test2"),
                        new QName("http://www.google","test3")});
        testListQNameElement.setTestListQNameElement(testListQName);

        testSerializeDeserialize(testListQNameElement, false);
    }

    public void testListOurs() throws Exception {

        TestListOursElement testListOursElement = new TestListOursElement();
        TestListOurs testListOurs = new TestListOurs();
        testListOursElement.setTestListOursElement(testListOurs);
        TestString testString1 = new TestString();
        testString1.setTestString("test");
        TestString testString2 = new TestString();
        testString2.setTestString("test");
        TestString testString3 = new TestString();
        testString3.setTestString("test");
        testListOurs.setTestString(new TestString[]{testString1,testString2,testString3});

        testSerializeDeserialize(testListOursElement, false);
    }

    public void testListSuper() throws Exception {

        SuperTestListStringElement superTestListStringElement = new SuperTestListStringElement();
        SuperTestListString superTestListString = new SuperTestListString();
        superTestListStringElement.setSuperTestListStringElement(superTestListString);
        superTestListString.setString(new String[]{"test1","test2","test3"});

        testSerializeDeserialize(superTestListStringElement, false);
    }

    public void testInnerSimpleTypes() throws Exception {
        TestInnerListSimpleType testInnerListSimpleType = new TestInnerListSimpleType();
        InnerListSimpleType innerListSimpleType = new InnerListSimpleType();
        testInnerListSimpleType.setTestInnerListSimpleType(innerListSimpleType);
        InnerListSimpleType_type0[] list = new InnerListSimpleType_type0[2];
        list[0] = InnerListSimpleType_type0.Access;
        list[1] = InnerListSimpleType_type0.Exist;

        innerListSimpleType.setInnerListSimpleType_type0(list);

        testSerializeDeserialize(testInnerListSimpleType, false);
    }
}
