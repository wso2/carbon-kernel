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

import org.apache.axis2.AxisFault;
import org.apache.axis2.rmi.Configurator;
import org.apache.axis2.rmi.client.RMIClientProxy;
import org.apache.axis2.rmi.config.ClassInfo;
import org.apache.axis2.rmi.config.FieldInfo;
import sample.rmi.server.Service1Interface;
import sample.rmi.server.dto.TestClass1;
import sample.rmi.server.dto.TestClass2;


public class TestService1 {

    private Configurator configurator;

    public TestService1() {

        this.configurator = new Configurator();

        this.configurator.addPackageToNamespaceMaping("sample.rmi.server", "http://sample/service");
        this.configurator.addPackageToNamespaceMaping("sample.rmi.server.dto", "http://sample/service/types");

        ClassInfo classInfo = new ClassInfo(TestClass1.class);
        classInfo.addFieldInfo(new FieldInfo("param1", "xmlParam1", false));
        classInfo.addFieldInfo(new FieldInfo("param3", "xmlParam3", false));

        this.configurator.addClassInfo(classInfo);

        classInfo = new ClassInfo(TestClass2.class);
        classInfo.addFieldInfo(new FieldInfo("param1", "xmlParam1", false));
        classInfo.addFieldInfo(new FieldInfo("param2", "xmlParam2", false));
        classInfo.addFieldInfo(new FieldInfo("param3", "xmlParam3", true));

        this.configurator.addClassInfo(classInfo);
    }


    public void testMethod1() {


        try {
            Service1Interface proxy =
                    (Service1Interface) RMIClientProxy.createProxy(Service1Interface.class,
                            this.configurator,
                            "http://localhost:8080/axis2/services/Service1");

            TestClass1 testClass1 =  new TestClass1();
            testClass1.setParam1(5);
            testClass1.setParam2("hellow world");
            testClass1.setParam3(new Integer(6));

            TestClass1 result = proxy.method1(testClass1);
            System.out.println("Param1 ==> " + result.getParam1());
            System.out.println("Param2 ==> " + result.getParam2());
            System.out.println("Param3 ==> " + result.getParam3());
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }


    }

    public void testMethod2() {


        try {
            Service1Interface proxy =
                    (Service1Interface) RMIClientProxy.createProxy(Service1Interface.class,
                            this.configurator,
                            "http://localhost:8080/axis2/services/Service1");

            TestClass2 testClass2 = new TestClass2();
            testClass2.setParam1(5);
            testClass2.setParam2("test String");

            TestClass1[] testClass1s = new TestClass1[2];
            testClass1s[0] =  new TestClass1();
            testClass1s[0].setParam1(5);
            testClass1s[0].setParam2("hellow world");
            testClass1s[0].setParam3(new Integer(6));

            testClass1s[1] =  new TestClass1();
            testClass1s[1].setParam1(5);
            testClass1s[1].setParam2("hellow world");
            testClass1s[1].setParam3(new Integer(6));

            testClass2.setParam3(testClass1s);

            TestClass2 result = proxy.method2(testClass2);
            System.out.println("Param1 ==> " + result.getParam1());
            System.out.println("Param2 ==> " + result.getParam2());
            System.out.println("Param3[0] param1 ==> " + result.getParam3()[0].getParam1());
            System.out.println("Param3[0] param2 ==> " + result.getParam3()[0].getParam2());
            System.out.println("Param3[0] param3 ==> " + result.getParam3()[0].getParam3());

            System.out.println("Param3[1] param1 ==> " + result.getParam3()[1].getParam1());
            System.out.println("Param3[1] param2 ==> " + result.getParam3()[1].getParam2());
            System.out.println("Param3[1] param3 ==> " + result.getParam3()[1].getParam3());
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }


    }

    public static void main(String[] args) {
        TestService1 testService1 = new TestService1();
        testService1.testMethod1();
        testService1.testMethod2();
    }
}
