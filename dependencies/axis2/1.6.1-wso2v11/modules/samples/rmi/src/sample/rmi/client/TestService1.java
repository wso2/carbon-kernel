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
package sample.rmi.client;

import org.apache.axis2.rmi.Configurator;
import org.apache.axis2.rmi.client.RMIClientProxy;
import sample.rmi.server.Service1Interface;

import java.util.Date;


public class TestService1 {

    private Configurator configurator;

    public TestService1() {
        this.configurator = new Configurator();
        this.configurator.addPackageToNamespaceMaping("sample.rmi.server", "http://sample/service");
        this.configurator.addPackageToNamespaceMaping("sample.rmi.server.dto", "http://sample/service/types");
        this.configurator.addPackageToNamespaceMaping("sample.rmi.server.exception", "http://sample/service/exception");
    }

    public void testMethod1() {
        try {
            Service1Interface proxy =
                    (Service1Interface) RMIClientProxy.createProxy(Service1Interface.class,
                            this.configurator,
                            "http://localhost:8080/axis2/services/Service1");
            String result = proxy.method1("Hellow"," World");
            System.out.println("Result ==> " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testMethod2() {

        try {
            Service1Interface proxy =
                    (Service1Interface) RMIClientProxy.createProxy(Service1Interface.class,
                            this.configurator,
                            "http://localhost:8080/axis2/services/Service1");
            Integer result = proxy.method2(new Integer(5),new Integer(15));
            System.out.println("Result ==> " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testMethod3(){
         try {
            Service1Interface proxy =
                    (Service1Interface) RMIClientProxy.createProxy(Service1Interface.class,
                            this.configurator,
                            "http://localhost:8080/axis2/services/Service1");
            Date result = proxy.method3(new Date());
            System.out.println("Result ==> " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        TestService1 testService1 = new TestService1();
        testService1.testMethod1();
        testService1.testMethod2();
        testService1.testMethod3();
    }
}
