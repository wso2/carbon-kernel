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
import org.apache.axis2.rmi.databind.service.Service2;
import org.apache.axis2.rmi.metadata.Operation;

import java.util.ArrayList;
import java.util.List;


public class ComplexRequestResponseTest extends RequestResponseTest {

    protected void setUp() throws Exception {
        this.serviceClass = Service2.class;
        this.serviceObject = new Service2();
        super.setUp();
    }

    public void testMethod1() {

        try {
            // first create service data

            Operation operation = this.service.getOperation("method1");
            // get objects after serialization and deserialization.
            // this returned objects mustbe identical with the original array list elements
            List inputObjects = new ArrayList();
            inputObjects.add(new TestClass1());
            Object[] objects = getInputObject(inputObjects, operation);

            TestClass1 object = (TestClass1) operation.getJavaMethod().invoke(this.serviceObject, objects);
            TestClass1 returnObject = (TestClass1) getReturnObject(object, operation);

            assertNotNull(returnObject);
        } catch (Exception e) {
            fail();
        }
    }

    public void testMethod21() {

        try {
            // first create service data

            Operation operation = this.service.getOperation("method2");
            // get objects after serialization and deserialization.
            // this returned objects mustbe identical with the original array list elements
            List inputObjects = new ArrayList();
            inputObjects.add(new TestClass1[]{new TestClass1(), new TestClass1()});
            Object[] objects = getInputObject(inputObjects, operation);

            TestClass1[] object = (TestClass1[]) operation.getJavaMethod().invoke(this.serviceObject, objects);
            TestClass1[] returnObject = (TestClass1[]) getReturnObject(object, operation);

            assertNotNull(returnObject[0]);
            assertNotNull(returnObject[1]);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testMethod22() {

        try {
            // first create service data

            Operation operation = this.service.getOperation("method2");
            // get objects after serialization and deserialization.
            // this returned objects mustbe identical with the original array list elements
            List inputObjects = new ArrayList();
            inputObjects.add(new TestClass1[]{new TestClass1(), null, new TestClass1()});
            Object[] objects = getInputObject(inputObjects, operation);

            TestClass1[] object = (TestClass1[]) operation.getJavaMethod().invoke(this.serviceObject, objects);
            TestClass1[] returnObject = (TestClass1[]) getReturnObject(object, operation);

            assertNotNull(returnObject[0]);
            assertNull(returnObject[1]);
            assertNotNull(returnObject[2]);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testMethod31() {

        try {
            // first create service data

            Operation operation = this.service.getOperation("method3");
            // get objects after serialization and deserialization.
            // this returned objects mustbe identical with the original array list elements
            List inputObjects = new ArrayList();
            TestClass2 testClass2 = new TestClass2();
            testClass2.setParam1(1);
            testClass2.setParam2(34.5f);
            testClass2.setParam3(23.5);
            inputObjects.add(testClass2);
            Object[] objects = getInputObject(inputObjects, operation);

            TestClass2 object = (TestClass2) operation.getJavaMethod().invoke(this.serviceObject, objects);
            TestClass2 returnObject = (TestClass2) getReturnObject(object, operation);

            assertEquals(returnObject.getParam1(), 1);
            assertTrue(returnObject.getParam2() == 34.5f);
            assertTrue(returnObject.getParam3() == 23.5);
        } catch (Exception e) {
            fail();
        }
    }

    public void testMethod32() {

        try {
            // first create service data

            Operation operation = this.service.getOperation("method3");
            // get objects after serialization and deserialization.
            // this returned objects mustbe identical with the original array list elements
            List inputObjects = new ArrayList();
            inputObjects.add(null);
            Object[] objects = getInputObject(inputObjects, operation);

            TestClass2 object = (TestClass2) operation.getJavaMethod().invoke(this.serviceObject, objects);
            TestClass2 returnObject = (TestClass2) getReturnObject(object, operation);

            assertNull(returnObject);
        } catch (Exception e) {
            fail();
        }
    }

    public void testMethod41() {

        try {
            // first create service data

            Operation operation = this.service.getOperation("method4");
            // get objects after serialization and deserialization.
            // this returned objects mustbe identical with the original array list elements
            List inputObjects = new ArrayList();

            TestClass2 testClass21 = new TestClass2();
            testClass21.setParam1(1);
            testClass21.setParam2(34.5f);
            testClass21.setParam3(23.5);

            TestClass2 testClass22 = new TestClass2();
            testClass22.setParam1(1);
            testClass22.setParam2(34.5f);
            testClass22.setParam3(23.5);

            TestClass2 testClass23 = new TestClass2();
            testClass23.setParam1(1);
            testClass23.setParam2(34.5f);
            testClass23.setParam3(23.5);

            inputObjects.add(new TestClass2[]{testClass21,testClass22,testClass23});
            Object[] objects = getInputObject(inputObjects, operation);

            TestClass2[] object = (TestClass2[]) operation.getJavaMethod().invoke(this.serviceObject, objects);
            TestClass2[] returnObject = (TestClass2[]) getReturnObject(object, operation);

            assertEquals(returnObject[0].getParam1(), 1);
            assertTrue(returnObject[0].getParam2() == 34.5f);
            assertTrue(returnObject[0].getParam3() == 23.5);

            assertEquals(returnObject[1].getParam1(), 1);
            assertTrue(returnObject[1].getParam2() == 34.5f);
            assertTrue(returnObject[1].getParam3() == 23.5);

            assertEquals(returnObject[2].getParam1(), 1);
            assertTrue(returnObject[2].getParam2() == 34.5f);
            assertTrue(returnObject[2].getParam3() == 23.5);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }


}
