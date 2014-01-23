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

package org.apache.axis2.schema.booleantest;

import org.apache.axis2.schema.AbstractTestCase;

public class BooleanTest extends AbstractTestCase {

    public void testBooleanTest1() throws Exception {
        TestBoolean1 testBoolean = new TestBoolean1();
        testBoolean.setTestBoolean1(true);
        testSerializeDeserialize(testBoolean, false);
    }

    public void testBooleanTest2() throws Exception {
        TestBoolean2 testBoolean = new TestBoolean2();
        testBoolean.setParam1(false);
        testBoolean.setAttribute1(true);
        testSerializeDeserialize(testBoolean, false);
    }

}
