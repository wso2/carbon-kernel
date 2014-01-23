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

import org.apache.axis2.rmi.config.ClassInfo;
import org.apache.axis2.rmi.config.FieldInfo;
import org.apache.axis2.rmi.databind.dto.TestClass2;
import org.apache.axis2.rmi.databind.dto.TestClass9;
import org.apache.axis2.rmi.metadata.Parameter;


public class CustomMappingsTest extends DataBindTest {

    public void testTestClass2(){

        ClassInfo classInfo = new ClassInfo(TestClass2.class);
        classInfo.addFieldInfo(new FieldInfo("param1","xmlparam1",false));
        classInfo.addFieldInfo(new FieldInfo("param2","xmlparam2",false));
        configurator.addClassInfo(classInfo);

        TestClass2 testObject = new TestClass2();
        testObject.setParam1(5);
        testObject.setParam2(34.5f);
        testObject.setParam3(34.5);

        Parameter parameter = new Parameter(TestClass2.class,"Param1");
        TestClass2 result = (TestClass2) getReturnObject(parameter,testObject);
        assertEquals(result.getParam1(),5);
        assertTrue(result.getParam2() == 34.5f);
        assertTrue(result.getParam3() == 34.5);

    }

    public void testTestClass9(){
        
        ClassInfo classInfo = new ClassInfo(TestClass2.class);
        classInfo.addFieldInfo(new FieldInfo("param1","xmlparam1",false));
        classInfo.addFieldInfo(new FieldInfo("param2","xmlparam2",false));
        classInfo.addFieldInfo(new FieldInfo("param3","xmlparam3",true));
        configurator.addClassInfo(classInfo);

        classInfo = new ClassInfo(TestClass9.class);
        classInfo.addFieldInfo(new FieldInfo("param1","xmlparam1",true));

        configurator.addClassInfo(classInfo);

        TestClass9 testClass9 = new TestClass9();

        TestClass2[] testClass2 = new TestClass2[3];
        testClass2[0] = new TestClass2();
        testClass2[0].setParam1(6);
        testClass2[0].setParam2(4.5f);
        testClass2[0].setParam3(56.5);

        testClass2[1] = new TestClass2();
        testClass2[1].setParam1(6);
        testClass2[1].setParam2(4.5f);
        testClass2[1].setParam3(56.5);

        testClass2[2] = new TestClass2();
        testClass2[2].setParam1(6);
        testClass2[2].setParam2(4.5f);
        testClass2[2].setParam3(56.5);

        testClass9.setParam1(testClass2);

        Parameter parameter = new Parameter(TestClass9.class,"Param1");
        TestClass9 result = (TestClass9) getReturnObject(parameter,testClass9);

        assertEquals(result.getParam1()[0].getParam1(),6);
        assertTrue(result.getParam1()[0].getParam2() == 4.5f);
        assertTrue(result.getParam1()[0].getParam3() == 56.5);

        assertEquals(result.getParam1()[1].getParam1(),6);
        assertTrue(result.getParam1()[1].getParam2() == 4.5f);
        assertTrue(result.getParam1()[1].getParam3() == 56.5);

        assertEquals(result.getParam1()[2].getParam1(),6);
        assertTrue(result.getParam1()[2].getParam2() == 4.5f);
        assertTrue(result.getParam1()[2].getParam3() == 56.5);


    }

}
