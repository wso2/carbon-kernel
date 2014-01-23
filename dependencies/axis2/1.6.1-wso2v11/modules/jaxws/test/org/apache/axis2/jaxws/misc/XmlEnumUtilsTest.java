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

/**
 * 
 */
package org.apache.axis2.jaxws.misc;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.utility.XmlEnumUtils;

/**
 * Tests Namespace to Package Algorithm
 */
public class XmlEnumUtilsTest extends TestCase {

    public void test01() throws Exception {
        Class cls = XmlEnumUtils.getConversionType(EnumSample.class);
        assertTrue(String.class.equals(cls));
        
        Object value = XmlEnumUtils.fromValue(EnumSample.class, "DATA_C");
        assertTrue(EnumSample.DATA_C == value);
    }
    
    public void test02() throws Exception {
        Class cls = XmlEnumUtils.getConversionType(EnumSample2.class);
        assertTrue(String.class.equals(cls));
        
        
        Object value = XmlEnumUtils.fromValue(EnumSample2.class,"DATA_C2");
        assertTrue(EnumSample2.DATA_C2 == value);
    }
    
    public void test03() throws Exception {
        Class cls = XmlEnumUtils.getConversionType(EnumSample3.class);
        assertTrue(Integer.class.equals(cls));
       
        Object value = XmlEnumUtils.fromValue(EnumSample3.class,30);
        assertTrue(EnumSample3.DATA_C3 == value);
    }
}
