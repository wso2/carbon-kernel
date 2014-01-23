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

import org.apache.axis2.rmi.databind.dto.TestClass11;
import org.apache.axis2.rmi.metadata.Parameter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class CollectionTest extends DataBindTest {

    public void testTestClass111(){

        Class testClass = TestClass11.class;
        Parameter parameter = new Parameter(testClass, "Param1");
        TestClass11 testObject = new TestClass11();
        List testList = new ArrayList();
        testList.add("Test String");
        testObject.setParam1(testList);
        TestClass11 result = (TestClass11) getReturnObject(parameter, testObject);
        assertEquals(result.getParam1().get(0), "Test String");
    }

    public void testTestClass112(){

        Class testClass = TestClass11.class;
        Parameter parameter = new Parameter(testClass, "Param1");
        TestClass11 testObject = new TestClass11();
        List testList = new ArrayList();
        testList.add("Test String0");
        testList.add("Test String1");
        testList.add(null);
        testList.add("Test String3");
        testList.add(null);

        testObject.setParam1(testList);
        TestClass11 result = (TestClass11) getReturnObject(parameter, testObject);
        assertEquals(result.getParam1().get(0), "Test String0");
        assertEquals(result.getParam1().get(1), "Test String1");
        assertEquals(result.getParam1().get(2), null);
        assertEquals(result.getParam1().get(3), "Test String3");
        assertEquals(result.getParam1().get(4), null);
    }

    public void testTestClass113(){

        Class testClass = TestClass11.class;
        Parameter parameter = new Parameter(testClass, "Param1");
        TestClass11 testObject = new TestClass11();
        List testList = new ArrayList();
        testObject.setParam1(testList);
        TestClass11 result = (TestClass11) getReturnObject(parameter, testObject);
        assertEquals(result.getParam1(), null);

    }

    public void testTestClass114(){

        Class testClass = TestClass11.class;
        Parameter parameter = new Parameter(testClass, "Param1");
        TestClass11 testObject = new TestClass11();
        testObject.setParam1(null);
        TestClass11 result = (TestClass11) getReturnObject(parameter, testObject);
        assertEquals(result.getParam1(), null);

    }

    public void testTestClass115(){

        Class testClass = TestClass11.class;
        Parameter parameter = new Parameter(testClass, "Param1");
        TestClass11 testObject = new TestClass11();
        ArrayList testList = new ArrayList();
        testList.add("Test String");
        testObject.setParam2(testList);
        TestClass11 result = (TestClass11) getReturnObject(parameter, testObject);
        assertEquals(result.getParam2().get(0), "Test String");
    }

    public void testTestClass116(){

        Class testClass = TestClass11.class;
        Parameter parameter = new Parameter(testClass, "Param1");
        TestClass11 testObject = new TestClass11();
        ArrayList testList = new ArrayList();
        testList.add("Test String0");
        testList.add("Test String1");
        testList.add(null);
        testList.add("Test String3");
        testList.add(null);

        testObject.setParam2(testList);
        TestClass11 result = (TestClass11) getReturnObject(parameter, testObject);
        assertEquals(result.getParam2().get(0), "Test String0");
        assertEquals(result.getParam2().get(1), "Test String1");
        assertEquals(result.getParam2().get(2), null);
        assertEquals(result.getParam2().get(3), "Test String3");
        assertEquals(result.getParam2().get(4), null);
    }

    public void testTestClass117(){

        Class testClass = TestClass11.class;
        Parameter parameter = new Parameter(testClass, "Param1");
        TestClass11 testObject = new TestClass11();
        ArrayList testList = new ArrayList();
        testObject.setParam2(testList);
        TestClass11 result = (TestClass11) getReturnObject(parameter, testObject);
        assertEquals(result.getParam2(), null);

    }

    public void testTestClass118(){

        Class testClass = TestClass11.class;
        Parameter parameter = new Parameter(testClass, "Param1");
        TestClass11 testObject = new TestClass11();
        testObject.setParam2(null);
        TestClass11 result = (TestClass11) getReturnObject(parameter, testObject);
        assertEquals(result.getParam2(), null);

    }

    public void testTestClass119(){

        Class testClass = TestClass11.class;
        Parameter parameter = new Parameter(testClass, "Param1");

        TestClass11 testObject = new TestClass11();
        List testList = new ArrayList();
        testList.add("test string");
        testList.add(null);
        testObject.setParam1(testList);

        Set testSet = new HashSet();
        testSet.add("test string");
        testSet.add(null);
        testObject.setParam3(testSet);

        TestClass11 result = (TestClass11) getReturnObject(parameter, testObject);
        assertEquals(result.getParam1().get(0),"test string");
        assertEquals(result.getParam1().get(1), null);
        assertEquals(result.getParam2(), null);
        assertTrue(result.getParam3().contains("test string"));
        assertTrue(result.getParam3().contains(null));
    }
}
