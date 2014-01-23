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

import org.apache.axis2.rmi.databind.dto.TestClass10;
import org.apache.axis2.rmi.metadata.Parameter;


public class ExtensionTest extends DataBindTest {

    public void testTestClass101() {

        Class testClass = TestClass10.class;
        Parameter parameter = new Parameter(testClass, "Param1");
        TestClass10 testObject = new TestClass10();
        testObject.setParam1("Test String");
        TestClass10 result = (TestClass10) getReturnObject(parameter, testObject);
        assertEquals(result.getParam1(), "Test String");
    }

    public void testTestClass102() {

        Class testClass = TestClass10.class;
        Parameter parameter = new Parameter(testClass, "Param1");
        TestClass10 testObject = new TestClass10();
        testObject.setParam1(new Integer(5));
        TestClass10 result = (TestClass10) getReturnObject(parameter, testObject);
        assertEquals(result.getParam1(), new Integer(5));
    }

    public void testTestClass103() {

        Class testClass = TestClass10.class;
        Parameter parameter = new Parameter(testClass, "Param1");
        TestClass10 testObject = new TestClass10();
        testObject.setParam2(new Object[]{"test String", new Integer(5)});
        TestClass10 result = (TestClass10) getReturnObject(parameter, testObject);
        assertEquals(result.getParam2()[0], "test String");
        assertEquals(result.getParam2()[1], new Integer(5));

    }

    
}
