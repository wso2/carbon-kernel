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

import org.apache.axis2.rmi.databind.service.Service1;
import org.apache.axis2.rmi.metadata.Operation;

import java.util.ArrayList;
import java.util.List;


public class SimpleRequestResponseTest extends RequestResponseTest {



    protected void setUp() throws Exception {
       this.serviceClass = Service1.class;
       this.serviceObject = new Service1();
       super.setUp();
    }

    public void testMethod1() {

        try {
            // first create service data

            Operation operation = this.service.getOperation("method1");
            // get objects after serialization and deserialization.
            // this returned objects mustbe identical with the original array list elements
            List inputObjects = new ArrayList();
            Object[] objects = getInputObject(inputObjects, operation);

            Object object = operation.getJavaMethod().invoke(this.serviceObject, objects);
            Object returnObject = getReturnObject(object, operation);

            assertNull(returnObject);
        } catch (Exception e) {
            fail();
        }
    }

    public void testMethod2() {

        try {
            // first create service data

            Operation operation = this.service.getOperation("method2");
            // get objects after serialization and deserialization.
            // this returned objects mustbe identical with the original array list elements
            List inputObjects = new ArrayList();
            inputObjects.add(new Integer(3));
            Object[] objects = getInputObject(inputObjects, operation);

            Integer object = (Integer) operation.getJavaMethod().invoke(this.serviceObject, objects);
            Integer returnObject = (Integer) getReturnObject(object, operation);

            assertEquals(returnObject.intValue(), 3);
        } catch (Exception e) {
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
            inputObjects.add(null);
            Object[] objects = getInputObject(inputObjects, operation);

            Object object = operation.getJavaMethod().invoke(this.serviceObject, objects);
            Object returnObject = getReturnObject(object, operation);

            assertNull(returnObject);
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
            inputObjects.add("test String");
            Object[] objects = getInputObject(inputObjects, operation);

            String object = (String) operation.getJavaMethod().invoke(this.serviceObject, objects);
            String returnObject = (String) getReturnObject(object, operation);

            assertEquals(returnObject, "test String");
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
            inputObjects.add(new int[]{2, 3, 5});
            Object[] objects = getInputObject(inputObjects, operation);

            int[] object = (int[]) operation.getJavaMethod().invoke(this.serviceObject, objects);
            int[] returnObject = (int[]) getReturnObject(object, operation);

            assertEquals(returnObject[0], 2);
            assertEquals(returnObject[1], 3);
            assertEquals(returnObject[2], 5);
        } catch (Exception e) {
            fail();
        }
    }

    public void testMethod42() {
        try {
            // first create service data

            Operation operation = this.service.getOperation("method4");
            // get objects after serialization and deserialization.
            // this returned objects mustbe identical with the original array list elements
            List inputObjects = new ArrayList();
            inputObjects.add(null);
            Object[] objects = getInputObject(inputObjects, operation);

            int[] object = (int[]) operation.getJavaMethod().invoke(this.serviceObject, objects);
            int[] returnObject = (int[]) getReturnObject(object, operation);

            assertNull(returnObject);
        } catch (Exception e) {
            fail();
        }
    }

    public void testMethod51() {
        try {
            // first create service data

            Operation operation = this.service.getOperation("method5");
            // get objects after serialization and deserialization.
            // this returned objects mustbe identical with the original array list elements
            List inputObjects = new ArrayList();
            inputObjects.add(new String[]{"string1", "string2", "string3"});
            Object[] objects = getInputObject(inputObjects, operation);

            String[] object = (String[]) operation.getJavaMethod().invoke(this.serviceObject, objects);
            String[] returnObject = (String[]) getReturnObject(object, operation);

            assertEquals(returnObject[0], "string1");
            assertEquals(returnObject[1], "string2");
            assertEquals(returnObject[2], "string3");
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testMethod52() {
        try {
            // first create service data

            Operation operation = this.service.getOperation("method5");
            // get objects after serialization and deserialization.
            // this returned objects mustbe identical with the original array list elements
            List inputObjects = new ArrayList();
            inputObjects.add(new String[]{null, "string2", null, "string4"});
            Object[] objects = getInputObject(inputObjects, operation);

            String[] object = (String[]) operation.getJavaMethod().invoke(this.serviceObject, objects);
            String[] returnObject = (String[]) getReturnObject(object, operation);

            assertEquals(returnObject[0], null);
            assertEquals(returnObject[1], "string2");
            assertEquals(returnObject[2], null);
            assertEquals(returnObject[3], "string4");
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

}
