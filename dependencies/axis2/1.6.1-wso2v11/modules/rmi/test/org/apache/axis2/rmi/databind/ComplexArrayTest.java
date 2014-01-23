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

package org.apache.axis2.rmi.databind;

import org.apache.axis2.rmi.databind.dto.TestClass2;
import org.apache.axis2.rmi.databind.dto.TestClass3;
import org.apache.axis2.rmi.databind.dto.TestClass4;
import org.apache.axis2.rmi.databind.dto.TestClass7;
import org.apache.axis2.rmi.databind.dto.TestClass9;
import org.apache.axis2.rmi.metadata.Parameter;


public class ComplexArrayTest extends DataBindTest {

    public void testTestClass7() {
        Class testClass = TestClass7.class;
        Parameter parameter = new Parameter(testClass, "Param1");
        TestClass7 testObject = null;
        TestClass7 result = null;

        testObject = new TestClass7();
        result = (TestClass7) getReturnObject(parameter, testObject);
        assertEquals(result.getParam1(), null);
        assertEquals(result.getParam2(), null);

        testObject = new TestClass7();
        testObject.setParam1(new TestClass4[]{null, null});
        result = (TestClass7) getReturnObject(parameter, testObject);
        assertEquals(result.getParam1()[0], null);
        assertEquals(result.getParam1()[1], null);
        assertEquals(result.getParam2(), null);

        TestClass4[] teseClassArray = new TestClass4[3];
        teseClassArray[0] = new TestClass4();
        teseClassArray[0].setParam1(new int[]{5, 6});
        teseClassArray[0].setParam2(new Integer[]{new Integer(2), new Integer(3)});
        teseClassArray[0].setParam3(new String[]{"test String"});
        teseClassArray[0].setParam4(new float[]{5.56f});

        teseClassArray[1] = null;

        teseClassArray[2] = new TestClass4();
        teseClassArray[2].setParam1(new int[]{5, 6});
        teseClassArray[2].setParam2(new Integer[]{new Integer(2), new Integer(3)});
        teseClassArray[2].setParam3(new String[]{"test String"});
        teseClassArray[2].setParam4(new float[]{5.56f});

        TestClass3 testClass3 = new TestClass3();
        testClass3.setParam1(new Integer(2));
        testClass3.setParam2(new Float(45.6));
        testClass3.setParam3(new Double(3));
        testClass3.setParam4("test String");


        testObject = new TestClass7();
        testObject.setParam1(teseClassArray);
        testObject.setParam2(testClass3);
        result = (TestClass7) getReturnObject(parameter, testObject);

        assertEquals(result.getParam1()[0].getParam1()[0], 5);
        assertEquals(result.getParam1()[0].getParam1()[1], 6);
        assertEquals(result.getParam1()[0].getParam2()[0].intValue(), 2);
        assertEquals(result.getParam1()[0].getParam2()[1].intValue(), 3);
        assertEquals(result.getParam1()[0].getParam3()[0], "test String");
        assertTrue(result.getParam1()[0].getParam4()[0] == 5.56f);

        assertNull(result.getParam1()[1]);

        assertEquals(result.getParam1()[2].getParam1()[0], 5);
        assertEquals(result.getParam1()[2].getParam1()[1], 6);
        assertEquals(result.getParam1()[2].getParam2()[0].intValue(), 2);
        assertEquals(result.getParam1()[2].getParam2()[1].intValue(), 3);
        assertEquals(result.getParam1()[2].getParam3()[0], "test String");
        assertTrue(result.getParam1()[2].getParam4()[0] == 5.56f);

        assertEquals(result.getParam2().getParam1().intValue(), 2);
        assertTrue(result.getParam2().getParam2().floatValue() == 45.6f);
        assertTrue(result.getParam2().getParam3().doubleValue() == 3);
        assertEquals(result.getParam2().getParam4(), "test String");


    }

    public void testClass91() {
        Class testClass = TestClass9.class;
        Parameter parameter = new Parameter(testClass, "Param1");

        TestClass9 testClass9 = new TestClass9();

        TestClass2[] testClass2 = new TestClass2[3];
        testClass2[0] = new TestClass2();
        testClass2[0].setParam1(5);
        testClass2[0].setParam2(23.45f);
        testClass2[0].setParam3(45);

        testClass2[1] = new TestClass2();
        testClass2[1].setParam1(5);
        testClass2[1].setParam2(23.45f);
        testClass2[1].setParam3(45);

        testClass2[2] = new TestClass2();
        testClass2[2].setParam1(5);
        testClass2[2].setParam2(23.45f);
        testClass2[2].setParam3(45);

        testClass9.setParam1(testClass2);

        TestClass9 returnObject = (TestClass9) getReturnObject(parameter, testClass9);

        assertEquals(returnObject.getParam1()[0].getParam1(), 5);
        assertTrue(returnObject.getParam1()[0].getParam2() == 23.45f);
        assertTrue(returnObject.getParam1()[0].getParam3() == 45);

        assertEquals(returnObject.getParam1()[1].getParam1(), 5);
        assertTrue(returnObject.getParam1()[1].getParam2() == 23.45f);
        assertTrue(returnObject.getParam1()[1].getParam3() == 45.0d);

        assertEquals(returnObject.getParam1()[2].getParam1(), 5);
        assertTrue(returnObject.getParam1()[2].getParam2() == 23.45f);
        assertTrue(returnObject.getParam1()[2].getParam3() == 45.0d);

    }

    public void testClass92() {
        Class testClass = TestClass9.class;
        Parameter parameter = new Parameter(testClass, "Param1");

        TestClass9 testClass9 = new TestClass9();

        TestClass2[] testClass2 = new TestClass2[3];
        testClass2[0] = new TestClass2();
        testClass2[0].setParam1(5);
        testClass2[0].setParam2(23.45f);
        testClass2[0].setParam3(45);

        testClass2[1] = null;

        testClass2[2] = new TestClass2();
        testClass2[2].setParam1(5);
        testClass2[2].setParam2(23.45f);
        testClass2[2].setParam3(45);

        testClass9.setParam1(testClass2);

        TestClass9 returnObject = (TestClass9) getReturnObject(parameter, testClass9);

        assertEquals(returnObject.getParam1()[0].getParam1(), 5);
        assertTrue(returnObject.getParam1()[0].getParam2() == 23.45f);
        assertTrue(returnObject.getParam1()[0].getParam3() == 45);

        assertEquals(returnObject.getParam1()[1],null);

        assertEquals(returnObject.getParam1()[2].getParam1(), 5);
        assertTrue(returnObject.getParam1()[2].getParam2() == 23.45f);
        assertTrue(returnObject.getParam1()[2].getParam3() == 45.0d);

    }

}
