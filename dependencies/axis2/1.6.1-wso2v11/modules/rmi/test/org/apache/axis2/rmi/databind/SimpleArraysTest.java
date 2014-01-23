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

import org.apache.axis2.rmi.databind.dto.TestClass4;
import org.apache.axis2.rmi.databind.dto.TestClass8;
import org.apache.axis2.rmi.metadata.Parameter;

public class SimpleArraysTest extends DataBindTest {

    public void testClass41() {
        Class testClass = TestClass4.class;
        Parameter parameter = new Parameter(testClass, "Param1");
        TestClass4 testObject = new TestClass4();
        TestClass4 result = (TestClass4) getReturnObject(parameter, testObject);
        assertEquals(result.getParam1(), null);
        assertEquals(result.getParam2(), null);
        assertEquals(result.getParam3(), null);
        assertEquals(result.getParam4(), null);

    }

    public void testClass42() {
        Class testClass = TestClass4.class;
        Parameter parameter = new Parameter(testClass, "Param1");
        TestClass4 testObject = new TestClass4();
        testObject.setParam1(new int[]{2, 3});
        TestClass4 result = (TestClass4) getReturnObject(parameter, testObject);
        assertEquals(result.getParam1()[0], 2);
        assertEquals(result.getParam1()[1], 3);
        assertEquals(result.getParam2(), null);
        assertEquals(result.getParam3(), null);
        assertEquals(result.getParam4(), null);

    }

    public void testClass43() {
        Class testClass = TestClass4.class;
        Parameter parameter = new Parameter(testClass, "Param1");
        TestClass4 testObject = new TestClass4();
        testObject.setParam2(new Integer[]{new Integer(2), new Integer(3), null, new Integer(4)});
        TestClass4 result = (TestClass4) getReturnObject(parameter, testObject);
        assertEquals(result.getParam1(), null);
        assertEquals(result.getParam2()[0], new Integer(2));
        assertEquals(result.getParam2()[1], new Integer(3));
        assertEquals(result.getParam2()[2], null);
        assertEquals(result.getParam2()[3], new Integer(4));
        assertEquals(result.getParam3(), null);
        assertEquals(result.getParam4(), null);

    }

    public void testClass432() {
        Class testClass = TestClass8.class;
        Parameter parameter = new Parameter(testClass, "Param1");
        TestClass8 testObject = new TestClass8();
        testObject.setParam1(new Integer[]{null, null});
        TestClass8 result = (TestClass8) getReturnObject(parameter, testObject);
        assertEquals(result.getParam1()[0], null);
        assertEquals(result.getParam1()[1], null);

    }

    public void testClass433() {
        Class testClass = TestClass8.class;
        Parameter parameter = new Parameter(testClass, "Param1");
        TestClass8 testObject = new TestClass8();
        TestClass8 result = (TestClass8) getReturnObject(parameter, testObject);
        assertEquals(result.getParam1(), null);
        assertEquals(result.getParam2(), null);

    }


    public void testClass44() {
        Class testClass = TestClass4.class;
        Parameter parameter = new Parameter(testClass, "Param1");
        TestClass4 testObject = new TestClass4();
        testObject.setParam3(new String[]{"test String1", null,null, "test String2"});
        TestClass4 result = (TestClass4) getReturnObject(parameter, testObject);
        assertEquals(result.getParam1(), null);
        assertEquals(result.getParam2(), null);
        assertEquals(result.getParam3()[0], "test String1");
        assertEquals(result.getParam3()[1], null);
        assertEquals(result.getParam3()[2], null);
        assertEquals(result.getParam3()[3], "test String2");
        assertEquals(result.getParam4(), null);

    }

    public void testClass45() {
        Class testClass = TestClass4.class;
        Parameter parameter = new Parameter(testClass, "Param1");
        TestClass4 testObject = new TestClass4();
        testObject.setParam4(new float[]{34.5f, 44.5f});
        TestClass4 result = (TestClass4) getReturnObject(parameter, testObject);
        assertEquals(result.getParam1(), null);
        assertEquals(result.getParam2(), null);
        assertEquals(result.getParam3(), null);
        assertTrue(result.getParam4()[0] == 34.5);
        assertTrue(result.getParam4()[1] == 44.5);

    }

    public void testClass46() {
        Class testClass = TestClass4.class;
        Parameter parameter = new Parameter(testClass, "Param1");
        TestClass4 testObject = new TestClass4();
        testObject.setParam1(new int[]{2, 3});
        testObject.setParam2(new Integer[]{new Integer(2), new Integer(3)});
        testObject.setParam3(new String[]{"test String1", "test String2"});
        testObject.setParam4(new float[]{34.5f, 44.5f});
        TestClass4 result = (TestClass4) getReturnObject(parameter, testObject);
        assertEquals(result.getParam1()[0], 2);
        assertEquals(result.getParam1()[1], 3);
        assertEquals(result.getParam2()[0], new Integer(2));
        assertEquals(result.getParam2()[1], new Integer(3));
        assertEquals(result.getParam3()[0], "test String1");
        assertEquals(result.getParam3()[1], "test String2");
        assertTrue(result.getParam4()[0] == 34.5);
        assertTrue(result.getParam4()[1] == 44.5);

    }


}
