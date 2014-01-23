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

import org.apache.axis2.rmi.databind.dto.TestClass12;
import org.apache.axis2.rmi.metadata.Parameter;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;


public class MapTest extends DataBindTest {

     public void testTestClass121(){

        Class testClass = TestClass12.class;
        Parameter parameter = new Parameter(testClass, "Param1");
        TestClass12 testObject = new TestClass12();
        Map param1 = new HashMap();
        param1.put("key1","value1");
        param1.put("key2","value2");
        testObject.setParam1(param1);
        TestClass12 result = (TestClass12) getReturnObject(parameter, testObject);
        assertTrue(result.getParam1().containsKey("key1"));
        assertTrue(result.getParam1().containsKey("key2"));
        assertEquals(result.getParam1().get("key1"),"value1");
        assertEquals(result.getParam1().get("key2"),"value2");
    }

     public void testTestClass122(){

        Class testClass = TestClass12.class;
        Parameter parameter = new Parameter(testClass, "Param1");
        TestClass12 testObject = new TestClass12();
        Hashtable param2 = new Hashtable();
        param2.put("key1","value1");
        param2.put("key2","value2");
        testObject.setParam2(param2);
        TestClass12 result = (TestClass12) getReturnObject(parameter, testObject);
        assertTrue(result.getParam2().containsKey("key1"));
        assertTrue(result.getParam2().containsKey("key2"));
        assertEquals(result.getParam2().get("key1"),"value1");
        assertEquals(result.getParam2().get("key2"),"value2");
    }

}
