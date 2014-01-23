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
package org.apache.axis2.schema.testsuite;

import junit.framework.TestCase;

// NOTE: Please keep this test case in sync with the one in helper.org.apache.axis2.schema.testsuite!
public class TrackerTest extends TestCase {
    public void testInt1WithNonNullValue() {
        TestInt1 bean = new TestInt1();
        assertFalse(bean.localTestValueTracker);
        bean.setTestValue(new int[] { 123 });
        assertTrue(bean.localTestValueTracker);
    }
    
    public void testInt1WithNullValue() {
        TestInt1 bean = new TestInt1();
        assertFalse(bean.localTestValueTracker);
        bean.setTestValue(null);
        assertTrue(bean.localTestValueTracker);
    }
    
    public void testInt2() {
        TestInt2 bean = new TestInt2();
        assertFalse(bean.localTestValueTracker);
        bean.setTestValue(1234);
        assertTrue(bean.localTestValueTracker);
    }
    
    public void testInt2WithMinValue() {
        TestInt2 bean = new TestInt2();
        assertFalse(bean.localTestValueTracker);
        bean.setTestValue(Integer.MIN_VALUE);
        assertTrue(bean.localTestValueTracker);
    }
    
    public void testInt3WithNonNullValue() {
        TestInt3 bean = new TestInt3();
        assertFalse(bean.localTestValueTracker);
        bean.setTestValue(new int[] { 123 });
        assertTrue(bean.localTestValueTracker);
    }
    
    public void testInt3WithNullValue() {
        TestInt3 bean = new TestInt3();
        assertFalse(bean.localTestValueTracker);
        bean.setTestValue(null);
        assertFalse(bean.localTestValueTracker);
    }
    
    public void testInt4() {
        TestInt4 bean = new TestInt4();
        assertFalse(bean.localTestValueTracker);
        bean.setTestValue(1234);
        assertTrue(bean.localTestValueTracker);
    }
    
    public void testInt4WithMinValue() {
        TestInt4 bean = new TestInt4();
        assertFalse(bean.localTestValueTracker);
        bean.setTestValue(Integer.MIN_VALUE);
        assertFalse(bean.localTestValueTracker);
    }
    
    public void testString2WithNonNullValue() {
        TestString2 bean = new TestString2();
        assertFalse(bean.localTestValueTracker);
        bean.setTestValue("test");
        assertTrue(bean.localTestValueTracker);
    }

    public void testString2WithNullValue() {
        TestString2 bean = new TestString2();
        assertFalse(bean.localTestValueTracker);
        bean.setTestValue(null);
        assertTrue(bean.localTestValueTracker);
    }
    
    public void testString4WithNonNullValue() {
        TestString4 bean = new TestString4();
        assertFalse(bean.localTestValueTracker);
        bean.setTestValue("test");
        assertTrue(bean.localTestValueTracker);
    }

    public void testString4WithNullValue() {
        TestString4 bean = new TestString4();
        assertFalse(bean.localTestValueTracker);
        bean.setTestValue(null);
        assertFalse(bean.localTestValueTracker);
    }
}
