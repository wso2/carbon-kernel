/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.axis2.transport.http.util;

import junit.framework.TestCase;

public class QueryStringParserTest extends TestCase {
    public void testSingleParameter() {
        QueryStringParser p = new QueryStringParser("name=value");
        assertTrue(p.next());
        assertEquals("name", p.getName());
        assertEquals("value", p.getValue());
        assertFalse(p.next());
    }
    
    public void testMultipleParameters() {
        QueryStringParser p = new QueryStringParser("name1=value1&name2=value2");
        assertTrue(p.next());
        assertEquals("name1", p.getName());
        assertEquals("value1", p.getValue());
        assertTrue(p.next());
        assertEquals("name2", p.getName());
        assertEquals("value2", p.getValue());
        assertFalse(p.next());
    }
    
    public void testSingleParameterWithoutValue() {
        QueryStringParser p = new QueryStringParser("name");
        assertTrue(p.next());
        assertEquals("name", p.getName());
        assertNull(p.getValue());
        assertFalse(p.next());
    }
    
    public void testMultipleParametersWithoutValue1() {
        QueryStringParser p = new QueryStringParser("name&name2");
        assertTrue(p.next());
        assertEquals("name", p.getName());
        assertNull(p.getValue());
        assertTrue(p.next());
        assertEquals("name2", p.getName());
        assertNull(p.getValue());
        assertFalse(p.next());
    }
    
    public void testMultipleParametersWithoutValue2() {
        QueryStringParser p = new QueryStringParser("name=value&name2");
        assertTrue(p.next());
        assertEquals("name", p.getName());
        assertEquals("value", p.getValue());
        assertTrue(p.next());
        assertEquals("name2", p.getName());
        assertNull(p.getValue());
        assertFalse(p.next());
    }
    
    public void testEncodedValue() {
        QueryStringParser p = new QueryStringParser("name=20%25%20down");
        assertTrue(p.next());
        assertEquals("name", p.getName());
        assertEquals("20% down", p.getValue());
        assertFalse(p.next());
    }
}
