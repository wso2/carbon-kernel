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

import sample.rmi.server.Service3;
import sample.rmi.server.Service2Interface;
import sample.rmi.server.Service3Interface;
import sample.rmi.server.exception.Exception1;
import sample.rmi.server.exception.Exception2;
import sample.rmi.server.exception.Exception3;
import org.apache.axis2.rmi.Configurator;
import org.apache.axis2.rmi.client.RMIClient;
import org.apache.axis2.rmi.client.RMIClientProxy;
import org.apache.axis2.AxisFault;

import java.util.ArrayList;
import java.util.List;


public class TestService3 {

    private Configurator configurator;

    public TestService3() {
        this.configurator = new Configurator();
        this.configurator.addPackageToNamespaceMaping("sample.rmi.server", "http://sample/service");
        this.configurator.addPackageToNamespaceMaping("sample.rmi.server.dto", "http://sample/service/types");
        this.configurator.addPackageToNamespaceMaping("sample.rmi.server.exception", "http://sample/service/exception");
    }

    public void testMethod1() {

        try {
            Service3Interface proxy =
                    (Service3Interface) RMIClientProxy.createProxy(Service3Interface.class,
                            this.configurator,
                            "http://localhost:8080/axis2/services/Service3");
            proxy.method1();
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        } catch (Exception1 exception1) {
            System.out.println("Got the exception 1");
        }

    }

    public void testMethod2() {


        try {
            Service3Interface proxy =
                    (Service3Interface) RMIClientProxy.createProxy(Service3Interface.class,
                            this.configurator,
                            "http://localhost:8080/axis2/services/Service3");
            proxy.method2("test string");
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        } catch (Exception2 exception2) {
            System.out.println("Got the exception 2");
        } catch (Exception1 exception1) {
            exception1.printStackTrace();
        }

    }

    public void testMethod3() {

        try {
            Service3Interface proxy =
                    (Service3Interface) RMIClientProxy.createProxy(Service3Interface.class,
                            this.configurator,
                            "http://localhost:8080/axis2/services/Service3");
            proxy.method3(5);
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        } catch (Exception3 exception3) {
            System.out.println("Got the exception 3");
        } catch (Exception2 exception2) {
            exception2.printStackTrace();
        } catch (Exception1 exception1) {
            exception1.printStackTrace();
        }

    }

    public static void main(String[] args) {
        TestService3 testService3 = new TestService3();
        testService3.testMethod1();
        testService3.testMethod2();
        testService3.testMethod3();
    }
}
