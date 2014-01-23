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

package org.apache.axis2.schema.anytype;

import org.apache.axiom.om.util.StAXUtils;
import org.apache.axis2.schema.AbstractTestCase;

import javax.xml.namespace.QName;
import java.io.StringReader;

public class AnyTypeTest extends AbstractTestCase {

    public void testAnyTypeElement1_1() throws Exception {
        TestAnyTypeElement1 testAnyTypeElement = new TestAnyTypeElement1();
        testAnyTypeElement.setTestAnyTypeElement1("test");
        testSerializeDeserialize(testAnyTypeElement, false);
    }

    public void testAnyTypeElement1_2() throws Exception {
        TestAnyTypeElement1 testAnyTypeElement = new TestAnyTypeElement1();
        testAnyTypeElement.setTestAnyTypeElement1(null);
        
        assertSerializationFailure(testAnyTypeElement);
    }

    public void testAnyTypeElement2_1() throws Exception {
        TestAnyTypeElement2 testAnyTypeElement = new TestAnyTypeElement2();
        testAnyTypeElement.setTestAnyTypeElement2("test");
        testSerializeDeserialize(testAnyTypeElement, false);
    }

    public void testAnyTypeElement2_2() throws Exception {
        TestAnyTypeElement2 testAnyTypeElement = new TestAnyTypeElement2();
        testAnyTypeElement.setTestAnyTypeElement2(null);
        testSerializeDeserialize(testAnyTypeElement, false);
    }

    public void testAnyTypeElement3_1() throws Exception {
        TestAnyTypeElement3 testAnyTypeElement = new TestAnyTypeElement3();
        testAnyTypeElement.setParam1(new Object[]{"test1","test2"});

        testSerializeDeserialize(testAnyTypeElement, false);
    }

    public void testAnyTypeElement3_2() throws Exception {
        TestAnyTypeElement3 testAnyTypeElement = new TestAnyTypeElement3();
        testAnyTypeElement.setParam1(null);

        TestAnyTypeElement3 expectedResult = new TestAnyTypeElement3();
        expectedResult.setParam1(new Object[] { null });
        
        testSerializeDeserialize(testAnyTypeElement, expectedResult);
    }

    public void testAnyTypeElement3_3() throws Exception {
        TestAnyTypeElement3 testAnyTypeElement = new TestAnyTypeElement3();
        testAnyTypeElement.setParam1(new Object[]{"test",null});

        testSerializeDeserialize(testAnyTypeElement, false);
    }


    public void testAnyTypeElement4_1() throws Exception {
        TestAnyTypeElement4 testAnyTypeElement = new TestAnyTypeElement4();
        testAnyTypeElement.setParam1(new Object[]{"test1","test2"});
        testSerializeDeserialize(testAnyTypeElement, false);
    }

    public void testAnyTypeElement4_2() throws Exception {
        TestAnyTypeElement4 testAnyTypeElement = new TestAnyTypeElement4();
        testAnyTypeElement.setParam1(null);
        testSerializeDeserialize(testAnyTypeElement);
    }

    public void testAnyTypeElement4_3() throws Exception {
        TestAnyTypeElement4 testAnyTypeElement = new TestAnyTypeElement4();
        testAnyTypeElement.setParam1(new Object[]{"test",null});

        TestAnyTypeElement4 expectedResult = new TestAnyTypeElement4();
        expectedResult.setParam1(new Object[] { "test" });
        testSerializeDeserialize(testAnyTypeElement, expectedResult, false);
    }


    public void testAnyTypeElement5_1() throws Exception {
        TestAnyTypeElement5 testAnyTypeElement = new TestAnyTypeElement5();
        testAnyTypeElement.setParam1(new Object[]{"test1","test2"});
        testSerializeDeserialize(testAnyTypeElement, false);
    }

    public void testAnyTypeElement5_2() throws Exception {
        TestAnyTypeElement5 testAnyTypeElement = new TestAnyTypeElement5();
        testAnyTypeElement.setParam1(null);

        TestAnyTypeElement5 expectedResult = new TestAnyTypeElement5();
        expectedResult.setParam1(new Object[] { null });
        testSerializeDeserialize(testAnyTypeElement, expectedResult);
    }

    public void testAnyTypeElement5_3() throws Exception {
        TestAnyTypeElement5 testAnyTypeElement = new TestAnyTypeElement5();
        testAnyTypeElement.setParam1(new Object[]{"test",null});
        testSerializeDeserialize(testAnyTypeElement, false);
    }


    public void testAnyTypeElement6_1() throws Exception {
        TestAnyTypeElement6 testAnyTypeElement = new TestAnyTypeElement6();
        testAnyTypeElement.setParam1(new Object[]{"test1","test2"});
        testSerializeDeserialize(testAnyTypeElement, false);
    }

    public void testAnyTypeElement6_2() throws Exception {
        TestAnyTypeElement6 testAnyTypeElement = new TestAnyTypeElement6();
        testAnyTypeElement.setParam1(null);
        assertSerializationFailure(testAnyTypeElement);
    }

    public void testAnyTypeElement6_3() throws Exception {
        TestAnyTypeElement6 testAnyTypeElement = new TestAnyTypeElement6();
        testAnyTypeElement.setParam1(new Object[]{"test",null});
        assertSerializationFailure(testAnyTypeElement);
    }

    public void testAnyTypeElement6_4() throws Exception {
        TestAnyTypeElement6 testAnyTypeElement6 = new TestAnyTypeElement6();

        TestComplexParent[] testComplexParents = new TestComplexParent[2];
        testComplexParents[0] = new TestComplexParent();
        testComplexParents[0].setParam1("test param1");

        TestComplexChild testComplexChild = new TestComplexChild();
        testComplexChild.setParam1("test param1");
        testComplexChild.setParam2(3);
        testComplexParents[1] = testComplexChild;

        testAnyTypeElement6.setParam1(testComplexParents);

        testSerializeDeserialize(testAnyTypeElement6, false);
    }

    public void testAnyTypeElement7_1() throws Exception {
        TestAnyTypeElement7 testAnyTypeElement = new TestAnyTypeElement7();
        testAnyTypeElement.setParam1("test");
        testSerializeDeserialize(testAnyTypeElement, false);
    }

    public void testAnyTypeElement7_2() throws Exception {
        TestAnyTypeElement7 testAnyTypeElement = new TestAnyTypeElement7();
        testAnyTypeElement.setParam1(null);
        testSerializeDeserialize(testAnyTypeElement);
    }

    public void testAnyTypeElement7_3() throws Exception {
        TestAnyTypeElement7 testAnyTypeElement7 = new TestAnyTypeElement7();
        TestComplexParent testComplexParent = new TestComplexParent();
        testComplexParent.setParam1("test param1");
        testAnyTypeElement7.setParam1(testComplexParent);
        testSerializeDeserialize(testAnyTypeElement7, false);
    }

    public void testAnyTypeElement8_1() throws Exception {
        TestAnyTypeElement8 testAnyTypeElement = new TestAnyTypeElement8();
        testAnyTypeElement.setParam1("test");
        testSerializeDeserialize(testAnyTypeElement, false);
    }

    public void testAnyTypeElement8_2() throws Exception {
        TestAnyTypeElement8 testAnyTypeElement = new TestAnyTypeElement8();
        testAnyTypeElement.setParam1(null);
        testSerializeDeserialize(testAnyTypeElement);
    }

    public void testAnyTypeElement9_1() throws Exception {
        TestAnyTypeElement9 testAnyTypeElement = new TestAnyTypeElement9();
        testAnyTypeElement.setParam1("test");
        testSerializeDeserialize(testAnyTypeElement, false);
    }

    public void testAnyTypeElement9_2() throws Exception {
        TestAnyTypeElement9 testAnyTypeElement = new TestAnyTypeElement9();
        testAnyTypeElement.setParam1(null);
        testSerializeDeserialize(testAnyTypeElement);
    }

    public void testAnyTypeElement10_1() throws Exception {
        TestAnyTypeElement10 testAnyTypeElement = new TestAnyTypeElement10();
        testAnyTypeElement.setParam1("test");
        testSerializeDeserialize(testAnyTypeElement, false);
    }

    public void testAnyTypeElement10_2() throws Exception {
        TestAnyTypeElement10 testAnyTypeElement = new TestAnyTypeElement10();
        testAnyTypeElement.setParam1(null);
        assertSerializationFailure(testAnyTypeElement);
    }

    public void testAnyElementInteger() throws Exception {
        // datatype tests
        TestAnyTypeElement1 testAnyTypeElement = new TestAnyTypeElement1();
        testAnyTypeElement.setTestAnyTypeElement1(new Integer(5));
        testSerializeDeserialize(testAnyTypeElement, false);
    }

    public void testAnyElementQName() throws Exception {
        // datatype tests
        TestAnyTypeElement1 testAnyTypeElement = new TestAnyTypeElement1();
        testAnyTypeElement.setTestAnyTypeElement1(new QName("http://wso2.org","testElement"));
        testSerializeDeserialize(testAnyTypeElement, false);
    }

    public void testTestElement() throws Exception {
        TestElement testElement = new TestElement();

        DynamicProperty[] dynamicProperties = new DynamicProperty[3];
        TestComplexParent testComplexParent = null;

        dynamicProperties[0] = new DynamicProperty();
        dynamicProperties[0].setName("test name");
        dynamicProperties[0].setVal(new Integer(5));

        dynamicProperties[1] = new DynamicProperty();
        dynamicProperties[1].setName("test name");
        testComplexParent = new TestComplexParent();
        testComplexParent.setParam1("test complext type");
        dynamicProperties[1].setVal(testComplexParent);

        TestSimpleType testSimpleType = new TestSimpleType();
        testSimpleType.setTestSimpleType("test simple string");
        dynamicProperties[2] = new DynamicProperty();
        dynamicProperties[2].setName("test name");
        dynamicProperties[2].setVal(testSimpleType);


        testElement.setParam1(dynamicProperties);

        testSerializeDeserialize(testElement, false);
    }
    
    // Regression test for AXIS2-4273
    public void testTestMixedWithNilledAnyType() throws Exception {
        TestMixed test = TestMixed.Factory.parse(StAXUtils.createXMLStreamReader(new StringReader(
                "<TestMixed xmlns='http://apache.org/axis2/schema/anytype' " +
                "           xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>" +
                "  <param1 xsi:nil='true'/>" +
                "  <param2>test</param2>" +
                "</TestMixed>")));
        assertNull(test.getParam1());
        assertEquals("test", test.getParam2());
    }
    
    public void testEmptyString() throws Exception {
        TestAnyTypeElement1 testAnyTypeElement = new TestAnyTypeElement1();
        testAnyTypeElement.setTestAnyTypeElement1("");
        testSerializeDeserialize(testAnyTypeElement, false);
    }
}

