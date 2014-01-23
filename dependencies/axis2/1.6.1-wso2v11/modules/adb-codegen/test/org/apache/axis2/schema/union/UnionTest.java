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

package org.apache.axis2.schema.union;

import org.apache.axis2.databinding.types.URI;
import org.apache.axis2.schema.AbstractTestCase;

import javax.xml.namespace.QName;

public class UnionTest extends AbstractTestCase {

    public void testRecord2() throws Exception {
        testRecord2(new Integer(10));
        testRecord2(new Boolean(true));
    }

    private void testRecord2(Object testObject) throws Exception {
        Record2 record2 = new Record2();
        DateOrDateTimeType dateOrDateTimeType = new DateOrDateTimeType();
        record2.setElem1(dateOrDateTimeType);
        dateOrDateTimeType.setObject(testObject);

        testSerializeDeserialize(record2, false);
    }

    public void testRecord1() throws Exception {
        testRecord1(new URI("http://www.google.com"));
        testRecord1(FooEnum._value1);
    }

    private void testRecord1(Object testObject) throws Exception {
        Record1 record1 = new Record1();
        FooOpenEnum fooOpenEnum = new FooOpenEnum();
        record1.setElem1(fooOpenEnum);
        fooOpenEnum.setObject(testObject);

        testSerializeDeserialize(record1, false);
    }

    public void testUnionQName() throws Exception {
        UnionQNameTestElement unionQNameTestElement = new UnionQNameTestElement();
        UnionQNameTest unionQNameTest = new UnionQNameTest();
        unionQNameTestElement.setUnionQNameTestElement(unionQNameTest);
        unionQNameTest.setObject(new QName("http://www.google.com","test"));

        testSerializeDeserialize(unionQNameTestElement, false);
    }

    public void testInnerSimpleTypes() throws Exception {
        TestInnerUnionType testInnerUnionType = new TestInnerUnionType();
        PackingType_T packingType_t = new PackingType_T();
        testInnerUnionType.setTestInnerUnionType(packingType_t);
        PackingType_T_type0 packingType_t_type0 = new PackingType_T_type0();
        packingType_t_type0.setPackingType_T_type0("MINOR_a");
        packingType_t.setObject(packingType_t_type0);
        
        testSerializeDeserialize(testInnerUnionType, false);

        testInnerUnionType = new TestInnerUnionType();
        packingType_t = new PackingType_T();
        testInnerUnionType.setTestInnerUnionType(packingType_t);
        PackingType_T_type1 packingType_t_type1 = new PackingType_T_type1();
        packingType_t_type1.setPackingType_T_type1("PROP_a");
        packingType_t.setObject(packingType_t_type1);

        testSerializeDeserialize(testInnerUnionType, false);

        testInnerUnionType = new TestInnerUnionType();
        packingType_t = new PackingType_T();
        testInnerUnionType.setTestInnerUnionType(packingType_t);
        packingType_t.setObject(PackingType_T_type2.TAR);

        testSerializeDeserialize(testInnerUnionType, false);
    }
}
