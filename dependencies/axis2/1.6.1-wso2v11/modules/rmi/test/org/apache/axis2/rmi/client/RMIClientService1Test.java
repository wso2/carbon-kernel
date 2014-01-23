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

package org.apache.axis2.rmi.client;

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.rmi.server.services.Service1Interface;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class RMIClientService1Test extends TestCase {

    public void testMethod11() {
        try {
            Service1Interface proxy = (Service1Interface) RMIClientProxy.createProxy(Service1Interface.class,
                    "http://localhost:8085/axis2/services/Service1");
            String result = proxy.method1("Hellow world");
            assertEquals(result, "Hellow world");
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }

    }

    public void testMethod12() {

        try {
            Service1Interface proxy = (Service1Interface) RMIClientProxy.createProxy(Service1Interface.class,
                    "http://localhost:8085/axis2/services/Service1");
            String result = proxy.method1(null);
            assertEquals(result, null);
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }

    }

    public void testMethod2() {

        try {
            Service1Interface proxy = (Service1Interface) RMIClientProxy.createProxy(Service1Interface.class,
                    "http://localhost:8085/axis2/services/Service1");
            String[] result = proxy.method2(new String[]{"param1","param2"});
            assertEquals(result[0], "param1");
            assertEquals(result[1], "param2");
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }

    }

    public void testMethod5() {
        try {
            Service1Interface proxy = (Service1Interface) RMIClientProxy.createProxy(Service1Interface.class,
                    "http://localhost:8085/axis2/services/Service1");
            Map param1 = new HashMap();
            param1.put("key1", "value1");
            param1.put("key2", "value2");
            Map result = proxy.method5(param1);
            assertTrue(result.containsKey("key1"));
            assertTrue(result.containsKey("key2"));
            assertEquals(result.get("key1"), "value1");
            assertEquals(result.get("key2"), "value2");
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void testMethod6(){
        try {
            Service1Interface proxy = (Service1Interface) RMIClientProxy.createProxy(Service1Interface.class,
                       "http://localhost:8085/axis2/services/Service1");

            Date date = new Date();
            Date result = proxy.method6(date);
            assertEquals(date.getDate(), result.getDate());
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }
    }
}
