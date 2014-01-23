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

package org.apache.axis2.schema.group;

import org.apache.axis2.schema.AbstractTestCase;

public class GroupTest extends AbstractTestCase {

    public void testSequenceGroupElement() throws Exception {

        TestSequenceGroupElement testGroupSequenceElement = new TestSequenceGroupElement();
        testGroupSequenceElement.setParam1("param1");
        TestSequenceGroup testSequenceGroup = new TestSequenceGroup();
        testSequenceGroup.setSequenceParam1("sequenceParam1");
        testSequenceGroup.setSequenceParam2("sequenceParam2");
        testGroupSequenceElement.setTestSequenceGroup(testSequenceGroup);

        testSerializeDeserialize(testGroupSequenceElement, false);
    }

    public void testNestedSequenceGroupElement() throws Exception {

        TestSequenceNestedGroupElement testSequenceNestedGroupElement = new TestSequenceNestedGroupElement();
        testSequenceNestedGroupElement.setParam1("param1");

        TestSequenceNestedGroup testSequenceNestedGroup = new TestSequenceNestedGroup();
        testSequenceNestedGroup.setNestedSequenceParam1("nestedSequenceParam1");

        TestSequenceGroup testSequenceGroup = new TestSequenceGroup();
        testSequenceGroup.setSequenceParam1("sequenceParam1");
        testSequenceGroup.setSequenceParam2("sequenceParam2");

        testSequenceNestedGroup.setTestSequenceGroup(testSequenceGroup);

        testSequenceNestedGroupElement.setTestSequenceNestedGroup(testSequenceNestedGroup);

        testSerializeDeserialize(testSequenceNestedGroupElement, false);
    }

    public void testChoiceGroupElement() throws Exception {

        TestChoiceGroupElement testGroupChoiceElement = new TestChoiceGroupElement();
        testGroupChoiceElement.setParam1("param1");
        TestChoiceGroup testChoiceGroup = new TestChoiceGroup();
        testChoiceGroup.setChoiceParam1("choiceParam1");
        testGroupChoiceElement.setTestChoiceGroup(testChoiceGroup);

        TestChoiceGroupElement expectedResult = new TestChoiceGroupElement();
        expectedResult.setTestChoiceGroup(testChoiceGroup);
        
        testSerializeDeserialize(testGroupChoiceElement, expectedResult, false);
    }

    public void testNestedChoiceGroupElement() throws Exception {

        TestChoiceNestedGroupElement testChoiceNestedGroupElement = new TestChoiceNestedGroupElement();
        testChoiceNestedGroupElement.setParam1("param1");

        TestChoiceNestedGroup testChoiceNestedGroup = new TestChoiceNestedGroup();
        testChoiceNestedGroup.setNestedChoiceParam1("nestedChoiceParam1");

        TestChoiceGroup testChoiceGroup = new TestChoiceGroup();
        testChoiceGroup.setChoiceParam1("choiceParam1");

        testChoiceNestedGroup.setTestChoiceGroup(testChoiceGroup);

        testChoiceNestedGroupElement.setTestChoiceNestedGroup(testChoiceNestedGroup);

        TestChoiceNestedGroupElement expectedResult = new TestChoiceNestedGroupElement();
        TestChoiceNestedGroup expectedChoiceNestedGroup = new TestChoiceNestedGroup();

        TestChoiceGroup expectedChoiceGroup = new TestChoiceGroup();
        expectedChoiceGroup.setChoiceParam1("choiceParam1");

        expectedChoiceNestedGroup.setTestChoiceGroup(expectedChoiceGroup);

        expectedResult.setTestChoiceNestedGroup(expectedChoiceNestedGroup);
        
        testSerializeDeserialize(testChoiceNestedGroupElement, expectedResult, false);
    }

     public void testAttributeGroup() throws Exception {
         TestAttributeGroupElement testAttributeGroup = new TestAttributeGroupElement();
         testAttributeGroup.setAttribute1("Attribute1");
         testAttributeGroup.setParam1("Param1");

         testSerializeDeserialize(testAttributeGroup, false);
     }

    public void testNestedAttributeGroup() throws Exception {
         TestNestedAttributeGroupElement testNestedAttributeGroupElement = new TestNestedAttributeGroupElement();
         testNestedAttributeGroupElement.setAttribute1("Attribute1");
         testNestedAttributeGroupElement.setAttribute2("Attribute2");
         testNestedAttributeGroupElement.setParam1("Param1");

         testSerializeDeserialize(testNestedAttributeGroupElement, false);
     }



}
