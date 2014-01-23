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

package org.apache.axis2.schema.attribute;

import org.apache.axis2.schema.AbstractTestCase;

public class AttributeTest extends AbstractTestCase {

    public void testElement1() throws Exception {

        TestElement1 testElement = new TestElement1();
        testElement.setAttribute1(1);
        testSerializeDeserialize(testElement);

        testElement = new TestElement1();
        testElement.setAttribute1(Integer.MIN_VALUE);
        testSerializeDeserialize(testElement);
    }

    public void testElement2() throws Exception {
        TestElement2 testElement = new TestElement2();
        testElement.setAttribute1(1);
        testSerializeDeserialize(testElement);

        testElement = new TestElement2();
        testElement.setAttribute1(Integer.MIN_VALUE);
        assertSerializationFailure(testElement);
    }

    public void testElement3() throws Exception {
        TestElement3 testElement = new TestElement3();
        testElement.setAttribute1("test");
        testSerializeDeserialize(testElement);

        testElement = new TestElement3();
        testSerializeDeserialize(testElement);
    }

    public void testElement4() throws Exception {
        TestElement4 testElement = new TestElement4();
        testElement.setAttribute1("test");
        testSerializeDeserialize(testElement);

        testElement = new TestElement4();
        assertSerializationFailure(testElement);
    }

    public void testAttributeSimpleType() throws Exception {
        TestAttributeSimpleType testAttributeSimpleType = new TestAttributeSimpleType();
        Attribute1_type0 attribute1_type0 = new Attribute1_type0();
        attribute1_type0.setAttribute1_type0("test attribute");
        testAttributeSimpleType.setAttribute1(attribute1_type0);
        testSerializeDeserialize(testAttributeSimpleType);
    }

    public void testAttributeReferenceElement() throws Exception {
        TestAttributeReferenceElement testAttributeReferenceElement = new TestAttributeReferenceElement();
        TestAttributeReferenceType testAttributeReferenceType = new TestAttributeReferenceType();
        testAttributeReferenceType.setParam1("param1");
        testAttributeReferenceType.setParam2("param2");
        testAttributeReferenceType.setTestAttribute1("attribute1");

        testAttributeReferenceElement.setTestAttributeReferenceElement(testAttributeReferenceType);
        testSerializeDeserialize(testAttributeReferenceElement, false);
    }
}
