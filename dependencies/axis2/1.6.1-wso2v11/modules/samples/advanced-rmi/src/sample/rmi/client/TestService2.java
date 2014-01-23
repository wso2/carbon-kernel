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
import org.apache.axis2.AxisFault;
import sample.rmi.server.Service2Interface;
import sample.rmi.server.dto.TestRestrictionBean;
import sample.rmi.server.dto.TestComplexBean;
import sample.rmi.server.dto.TestBean;

import java.util.Iterator;


public class TestService2 {

    private Configurator configurator;

    public TestService2() {

        this.configurator = new Configurator();
        this.configurator.addPackageToNamespaceMaping("sample.rmi.server", "http://sample/service");
        this.configurator.addPackageToNamespaceMaping("sample.rmi.server.dto", "http://sample/service/types");
    }

    public void testMethod1(){


        try {
            Service2Interface proxy =
                    (Service2Interface) RMIClientProxy.createProxy(
                            Service2Interface.class,
                            configurator,
                            "http://localhost:8080/axis2/services/Service2");
            TestRestrictionBean testRestrictionBean = new TestRestrictionBean("testvalue");
            TestRestrictionBean result = proxy.method1(testRestrictionBean);
            System.out.println("Result ==> " + result.getParam1());
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }
    }

    public void testMethod2(){


        try {
            Service2Interface proxy =
                    (Service2Interface) RMIClientProxy.createProxy(
                            Service2Interface.class,
                            configurator,
                            "http://localhost:8080/axis2/services/Service2");
            TestComplexBean testComplexBean = new TestComplexBean();
            testComplexBean.addTestBean(new TestBean(5,"teststring1"));
            testComplexBean.addTestBean(new TestBean(6,"teststring2"));
            testComplexBean.addTestBean(new TestBean(7,"teststring3"));
            TestComplexBean result = proxy.method2(testComplexBean);
            TestBean testBean;
            for (Iterator iter = result.getTestBeans().iterator();iter.hasNext();){
                testBean = (TestBean) iter.next();
                System.out.println("Parm1 ==> " + testBean.getParam1());
                System.out.println("Parm2 ==> " + testBean.getParam2());
            }
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }
    }



    public static void main(String[] args) {
        TestService2 testService2 = new TestService2();
        testService2.testMethod1();
        testService2.testMethod2();
    }
}
