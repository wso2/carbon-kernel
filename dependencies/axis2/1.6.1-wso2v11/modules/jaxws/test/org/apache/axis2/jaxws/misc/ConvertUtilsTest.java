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

package org.apache.axis2.jaxws.misc;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.utility.ConvertUtils;

import javax.xml.ws.WebServiceException;
import java.util.ArrayList;

/**
 * Unit Test for the ConvertUtils utility
 */
public class ConvertUtilsTest extends TestCase {

    public void testByteArraytobyteArray() throws Exception {
        Byte[] input = new Byte[3];
        input[0] = new Byte((byte) 0);
        input[1] = new Byte((byte) 1);
        input[2] = new Byte((byte) 2);
        
        byte[] output = new byte[3];
        
        if (ConvertUtils.isConvertable(input, output.getClass())) {
            output = (byte[]) ConvertUtils.convert(input, 
                                               output.getClass());
        }
        
        assertTrue(output.length == 3);
        assertTrue(output[0] == (byte) 0);
        assertTrue(output[1] == (byte) 1);
        assertTrue(output[2] == (byte) 2);
    }
    
    public void testBListtoBArray() throws Exception {
        ArrayList<B> input = new ArrayList<B>();
        B b = new B();
        b.setData(0);
        input.add(b);
        b = new B();
        b.setData(1);
        input.add(b);
        b = new B();
        b.setData(2);
        input.add(b);
        
        B[] output = new B[0];
        
        if (ConvertUtils.isConvertable(input, output.getClass())) {
            output = (B[]) ConvertUtils.convert(input, output.getClass());
        }
        
        assertTrue(output.length == 3);
        assertTrue(output[0].getData() == 0);
        assertTrue(output[1].getData() == 1);
        assertTrue(output[2].getData() == 2);
    }
    
    public void testBArraytoBList() throws Exception {
        
        B[] input = new B[3];
        input[0] = new B();
        input[0].setData(0);
        input[1] = new B();
        input[1].setData(1);
        input[2] = new B();
        input[2].setData(2);
        
        
        ArrayList<B> output = new ArrayList<B>();
        
        if (ConvertUtils.isConvertable(input, output.getClass())) {
            output = (ArrayList<B>) ConvertUtils.convert(input, 
                                                         output.getClass());
        }
        
        assertTrue(output.size() == 3);
        assertTrue(output.get(0).getData() == 0);
        assertTrue(output.get(1).getData() == 1);
        assertTrue(output.get(2).getData() == 2);
    }
    
    /** 
     * Negative test...can't convert List of B into C[]
     * @throws Exception
     */
    public void testBListtoCArray() throws Exception {
        ArrayList<B> input = new ArrayList<B>();
        B b = new B();
        b.setData(0);
        input.add(b);
        b = new B();
        b.setData(1);
        input.add(b);
        b = new B();
        b.setData(2);
        input.add(b);
        
        C[] output = new C[0];
        
        boolean success = false;
        try {
            output = (C[]) ConvertUtils.convert(input, 
                                                         output.getClass());
        } catch (WebServiceException e) {
            assertTrue(e.getMessage().contains("Cannot convert"));
            success = true;
        } 
        
        assertTrue(success);
    }
    class B {
        private int data = 0;

        public int getData() {
            return data;
        }

        public void setData(int data) {
            this.data = data;
        }
    }
    
    class C {
        private int data = 0;

        public int getData() {
            return data;
        }

        public void setData(int data) {
            this.data = data;
        }
    }
}
