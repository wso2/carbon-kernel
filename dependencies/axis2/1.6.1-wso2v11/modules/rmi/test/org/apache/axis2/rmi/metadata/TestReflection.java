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

package org.apache.axis2.rmi.metadata;

import junit.framework.TestCase;
import org.apache.axis2.rmi.metadata.service.BasicService;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class TestReflection extends TestCase {

    public void testInvoke(){
        Class basicServiceClass = BasicService.class;

        try {
            Method method2 = basicServiceClass.getMethod("method2",new Class[]{int.class});
            Object basicService = basicServiceClass.newInstance();

            method2.invoke(basicService,new Object[]{new Integer(1)});
            System.out.println("OK");

            Class integerClass = Integer.class;
            Constructor constructor = integerClass.getConstructor(new Class[]{String.class});
            Object integerObject = constructor.newInstance(new Object[]{"5"});
            Object integer = integerClass.newInstance();
        } catch (NoSuchMethodException e) {
            fail();
        } catch (IllegalAccessException e) {
            fail();
        } catch (InstantiationException e) {
            fail();
        } catch (InvocationTargetException e) {
            fail();
        }
    }
}
