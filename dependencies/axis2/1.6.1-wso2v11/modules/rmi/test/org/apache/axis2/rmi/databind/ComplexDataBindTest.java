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
import org.apache.axis2.rmi.databind.dto.TestClass5;
import org.apache.axis2.rmi.databind.dto.TestClass6;
import org.apache.axis2.rmi.metadata.Parameter;


public class ComplexDataBindTest extends DataBindTest {

    public void testClass5(){

        Class testClass = TestClass5.class;
        Parameter parameter = new Parameter(testClass,"Param1");
        TestClass5 testObject = new TestClass5();

        TestClass5 result = null;
        result = (TestClass5) getReturnObject(parameter,testObject);
        assertEquals(result.getParam1(),null);

        testObject.setParam1(new TestClass1());
        result = (TestClass5) getReturnObject(parameter,testObject);
        assertNotNull(result.getParam1());

    }

    public void testClass6(){
        Class testClass = TestClass6.class;
        Parameter parameter = new Parameter(testClass,"Param1");

        TestClass6 result = null;

        TestClass6 testObject = new TestClass6();
        result = (TestClass6) getReturnObject(parameter,testObject);
        assertEquals(result.getParam1(),null);
        assertEquals(result.getParam2(),null);
        assertEquals(result.getParam3(),0);

        testObject = new TestClass6();
        testObject.setParam1(new TestClass2());
        testObject.setParam2("test String");
        testObject.setParam3(5);
        result = (TestClass6) getReturnObject(parameter,testObject);
        assertNotNull(result.getParam1());
        assertEquals(result.getParam2(),"test String");
        assertEquals(result.getParam3(),5);

        testObject = new TestClass6();

        testObject.setParam1(new TestClass2());
        testObject.getParam1().setParam1(5);
        testObject.getParam1().setParam2(34.5f);
        testObject.getParam1().setParam3(32.5);
        testObject.setParam2("test String");
        testObject.setParam3(5);
        result = (TestClass6) getReturnObject(parameter,testObject);
        assertEquals(result.getParam1().getParam1(),5);
        assertTrue(result.getParam1().getParam2() == 34.5f);
        assertTrue(result.getParam1().getParam3() == 32.5);
        assertEquals(result.getParam2(),"test String");
        assertEquals(result.getParam3(),5);
    }
}
