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

import org.apache.axis2.rmi.databind.dto.TestClass13;
import org.apache.axis2.rmi.databind.dto.TestRestrictionBean;
import org.apache.axis2.rmi.metadata.Parameter;


public class CustomTypeTest extends DataBindTest {

    public void testTestClass131() {

        TestClass13 testClass13 = new TestClass13();
        TestRestrictionBean testRestrictionBean = new TestRestrictionBean("test string");
        testClass13.setParam1(testRestrictionBean);

        Parameter parameter = new Parameter(TestClass13.class, "Param1");

        TestClass13 result = (TestClass13) getReturnObject(parameter, testClass13);
        assertEquals(result.getParam1().getParam1(), "test string");

    }

    public void testTestClass132() {

        TestClass13 testClass13 = new TestClass13();
        TestRestrictionBean[] testRestrictionBeans = new TestRestrictionBean[3];
        testRestrictionBeans[0] = new TestRestrictionBean("test string 1");
        testRestrictionBeans[1] = new TestRestrictionBean("test string 2");
        testRestrictionBeans[2] = new TestRestrictionBean("test string 3");
        testClass13.setParam2(testRestrictionBeans);

        Parameter parameter = new Parameter(TestClass13.class, "Param1");

        TestClass13 result = (TestClass13) getReturnObject(parameter, testClass13);
        assertEquals(result.getParam2()[0].getParam1(), "test string 1");
        assertEquals(result.getParam2()[1].getParam1(), "test string 2");
        assertEquals(result.getParam2()[2].getParam1(), "test string 3");

    }
}
