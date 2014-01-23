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

package org.apache.axis2.schema.defaultvalue;

import junit.framework.TestCase;

public class DefaultValueTest extends TestCase {

    public void testDefaultValues(){
        TestElement1 testElement1 = new TestElement1();
        assertEquals(testElement1.getTestElement1(),56);
        TestElement2 testElement2 = new TestElement2();
        assertEquals(testElement2.getParam1(), "test");
        assertEquals(testElement2.getAttribute1(),true);
    }
}
