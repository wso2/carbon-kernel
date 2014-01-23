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

package org.apache.axis2.deployment.util;

import junit.framework.TestCase;


public class ExcludeInfoTest extends TestCase {

    public void testGetBeanExcludeInfoForClass(){
        ExcludeInfo excludeInfo = new ExcludeInfo();
        excludeInfo.putBeanInfo("org.kp.anuhak.cdr.test.[A-z]*",new BeanExcludeInfo("[A-z]*Bool","is[A-z]*"));
        excludeInfo.putBeanInfo("org.kp.anuhak.cdr.types.[A-z]*",new BeanExcludeInfo("[A-z]*String",null));
        excludeInfo.putBeanInfo("org.kp.anuhak.meta.[A-z]*",new BeanExcludeInfo("[A-z]*",null));

        BeanExcludeInfo beanExcludeInfo = excludeInfo.getBeanExcludeInfoForClass("org.kp.anuhak.cdr.test.TestClass");
        assertFalse(beanExcludeInfo.isExcludedProperty("TestProperty"));
        assertTrue(beanExcludeInfo.isExcludedProperty("TestBool"));
        assertFalse(beanExcludeInfo.isExcludedProperty("isTestBool"));

        beanExcludeInfo = excludeInfo.getBeanExcludeInfoForClass("org.kp.anuhak.cdr.types.TestClass");
        assertFalse(beanExcludeInfo.isExcludedProperty("TestProperty"));
        assertTrue(beanExcludeInfo.isExcludedProperty("TestString"));
        assertFalse(beanExcludeInfo.isExcludedProperty("isTestBool"));

        beanExcludeInfo = excludeInfo.getBeanExcludeInfoForClass("org.kp.anuhak.meta.TestClass");
        assertTrue(beanExcludeInfo.isExcludedProperty("TestProperty"));

    }
}
