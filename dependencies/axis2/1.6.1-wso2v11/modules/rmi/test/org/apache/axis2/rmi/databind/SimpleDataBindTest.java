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

import org.apache.axis2.rmi.databind.dto.TestClass1;
import org.apache.axis2.rmi.databind.dto.TestClass2;
import org.apache.axis2.rmi.databind.dto.TestClass3;
import org.apache.axis2.rmi.metadata.Parameter;

public class SimpleDataBindTest extends DataBindTest {

    public void testTestClass1() {

        Class testClass = TestClass1.class;
        Parameter parameter = new Parameter(testClass, "Param1");
        TestClass1 testObject = new TestClass1();
        TestClass1 result = (TestClass1) getReturnObject(parameter, testObject);

    }

    public void testTestInt() {

        Parameter parameter = new Parameter(int.class, "Param1");
        Integer result = (Integer) getReturnObject(parameter, new Integer(3));
        assertEquals(result, new Integer(3));

    }

    public void testTestString() {

        Parameter parameter = new Parameter(String.class, "Param1");
        String result = (String) getReturnObject(parameter, "test String");
        assertEquals(result, "test String");
        result = (String) getReturnObject(parameter, null);
        assertEquals(result, null);


    }

    public void testTestClass2() {
        Class testClass = TestClass2.class;
        Parameter parameter = new Parameter(testClass, "Parameter1");
        TestClass2 testObject = new TestClass2();
        testObject.setParam1(3);
        testObject.setParam2(3.45f);
        testObject.setParam3(4.5678);

        TestClass2 result = (TestClass2) getReturnObject(parameter, testObject);
        assertEquals(result.getParam1(), 3);
        assertTrue(result.getParam2() == 3.45f);
        assertTrue(result.getParam3() == 4.5678);

    }

    public void testTestClass3() {
        Class testClass = TestClass3.class;
        Parameter parameter = new Parameter(testClass, "Param1");
        TestClass3 testObject = new TestClass3();

        testObject.setParam1(new Integer(3));
        testObject.setParam2(new Float(34.5f));
        testObject.setParam3(new Double(23.4));
        testObject.setParam4("test String");

        TestClass3 result;
        result = (TestClass3) getReturnObject(parameter, testObject);
        assertEquals(result.getParam1(), new Integer(3));
        assertEquals(result.getParam2(), new Float(34.5f));
        assertEquals(result.getParam3(), new Double(23.4));
        assertEquals(result.getParam4(), "test String");

        testObject = new TestClass3();
        result = (TestClass3) getReturnObject(parameter, testObject);
        assertEquals(result.getParam1(), null);
        assertEquals(result.getParam2(), null);
        assertEquals(result.getParam3(), null);
        assertEquals(result.getParam4(), null);


    }
}
