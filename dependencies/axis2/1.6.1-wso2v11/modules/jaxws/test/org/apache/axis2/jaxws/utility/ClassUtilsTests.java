/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.utility;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

/**
 * Test the utility methods in the ClassUtils class
 */
public class ClassUtilsTests extends TestCase {
    
    
    public void test1() throws Exception {
        
    	Method m = Sample.class.getMethod("method1", null);
    	Set<Class> set = ClassUtils.getClasses(m.getGenericReturnType(), null);
    	
    	assertTrue(set.contains(int.class));
    	assertTrue(!(set.contains(List.class)));
    	assertTrue(!(set.contains(BigInteger.class)));
    	assertTrue(!(set.contains(Float.class)));
    	assertTrue(!(set.contains(HashMap.class)));
        
    }
    
    public void test2() throws Exception {
        
    	Method m = Sample.class.getMethod("method2", null);
    	Set<Class> set = ClassUtils.getClasses(m.getGenericReturnType(), null);
    	
    	assertTrue(!(set.contains(int.class)));
    	assertTrue((set.contains(List.class)));
    	assertTrue((set.contains(BigInteger.class)));
    	assertTrue(!(set.contains(Float.class)));
    	assertTrue(!(set.contains(HashMap.class)));
    }
    
    public void test3() throws Exception {
        
    	Method m = Sample.class.getMethod("method3", null);
    	Set<Class> set = ClassUtils.getClasses(m.getGenericReturnType(), null);
    	assertTrue(!(set.contains(int.class)));
    	assertTrue((set.contains(List.class)));
    	assertTrue((set.contains(BigInteger.class)));
    	assertTrue(!(set.contains(Float.class)));
    	assertTrue(!(set.contains(HashMap.class)));
    }
    
    public void test4() throws Exception {
        
    	Method m = Sample.class.getMethod("method4", null);
    	Set<Class> set = ClassUtils.getClasses(m.getGenericReturnType(), null);
    	assertTrue(!(set.contains(int.class)));
    	assertTrue((set.contains(List.class)));
    	assertTrue(!(set.contains(BigInteger.class)));
    	assertTrue((set.contains(Float.class)));
    	assertTrue((set.contains(HashMap.class)));
    }
    
    class Sample {
    	public int method1() { return 0;}
    	public List<BigInteger> method2() { return null;}
    	public List<BigInteger[]>[] method3() { return null;}
    	public List<HashMap<Integer, Float[]>>[] method4() { return null;}
    }

}
