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

import org.apache.axis2.rmi.databind.dto.TestClass14;
import org.apache.axis2.rmi.databind.dto.TestClass15;
import org.apache.axis2.rmi.databind.dto.TestClass2;
import org.apache.axis2.rmi.databind.dto.TestClass9;
import org.apache.axis2.rmi.metadata.Parameter;

public class SameParamNameTest extends DataBindTest {

    public void testElementArray() {
        Class testClass = TestClass14.class;
        Parameter parameter = new Parameter(testClass, "Param1");
        TestClass14 testObject = null;
        TestClass14 result = null;

        testObject = new TestClass14();
        result = (TestClass14) getReturnObject(parameter, testObject);
        assertNull(result.getParam1());

        testObject = new TestClass14();
        testObject.setParam1(new TestClass9());
        result = (TestClass14) getReturnObject(parameter, testObject);
        assertNotNull(result.getParam1());

        testObject = new TestClass14();
        TestClass2[] testClass2Array = new TestClass2[2];

        testClass2Array[0] = new TestClass2();
        testClass2Array[0].setParam1(5);   
        testClass2Array[0].setParam2((float) 6.1);
        testClass2Array[0].setParam3(7.2);
      
        testClass2Array[1] = new TestClass2();
        testClass2Array[1].setParam1(1);   
        testClass2Array[1].setParam2((float) 2.1);
        testClass2Array[1].setParam3(3.2);
        
        TestClass9 testClass9Object = new TestClass9();
        
        testClass9Object.setParam1(testClass2Array);
        
        testObject.setParam1(testClass9Object);
        
        result = (TestClass14) getReturnObject(parameter, testObject);
        
        assertTrue(result.getParam1().getParam1()[0].getParam1() == 5);
        assertTrue(result.getParam1().getParam1()[0].getParam2() == 6.1f);
        assertTrue(result.getParam1().getParam1()[0].getParam3() == 7.2);
        
        assertTrue(result.getParam1().getParam1()[1].getParam1() == 1);
        assertTrue(result.getParam1().getParam1()[1].getParam2() == 2.1f);
        assertTrue(result.getParam1().getParam1()[1].getParam3() == 3.2);
        
    }
    
    public void testElementElement() {
    	Class testClass = TestClass15.class;
        Parameter parameter = new Parameter(testClass, "Param1");
        TestClass15 testObject = null;
        TestClass15 result = null;

        testObject = new TestClass15();
        result = (TestClass15) getReturnObject(parameter, testObject);
        assertNull(result.getParam1());

        testObject = new TestClass15();
        testObject.setParam1(new TestClass14());
        result = (TestClass15) getReturnObject(parameter, testObject);
        assertNotNull(result.getParam1());

        testObject = new TestClass15();
        TestClass2[] testClass2Array = new TestClass2[2];

        testClass2Array[0] = new TestClass2();
        testClass2Array[0].setParam1(5);   
        testClass2Array[0].setParam2((float) 6.1);
        testClass2Array[0].setParam3(7.2);
      
        testClass2Array[1] = new TestClass2();
        testClass2Array[1].setParam1(1);   
        testClass2Array[1].setParam2((float) 2.1);
        testClass2Array[1].setParam3(3.2);
        
        TestClass9 testClass9Object = new TestClass9();
        
        testClass9Object.setParam1(testClass2Array);
        
        TestClass14 testClass14Object = new TestClass14();
        testClass14Object.setParam1(testClass9Object);
        
        testObject.setParam1(testClass14Object);
        
        result = (TestClass15) getReturnObject(parameter, testObject);
        
        assertTrue(result.getParam1().getParam1().getParam1()[0].getParam1() == 5);
        assertTrue(result.getParam1().getParam1().getParam1()[0].getParam2() == 6.1f);
        assertTrue(result.getParam1().getParam1().getParam1()[0].getParam3() == 7.2);
        
        assertTrue(result.getParam1().getParam1().getParam1()[1].getParam1() == 1);
        assertTrue(result.getParam1().getParam1().getParam1()[1].getParam2() == 2.1f);
        assertTrue(result.getParam1().getParam1().getParam1()[1].getParam3() == 3.2);
        
    }
}
