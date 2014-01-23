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

package org.apache.axis2.schema.innerparticleextension;

import org.apache.axis2.schema.AbstractTestCase;

public class TestInnerParticleExtension extends AbstractTestCase {

    public void testTestElement() throws Exception {
        TestElement testElement = new TestElement();

        ChildElement_type0 childElement_type0[] = new ChildElement_type0[2];
        childElement_type0[0] = new ChildElement_type0();
        childElement_type0[0].setTestAttribute("Test Attribute");

        ParentTypeChoice[] parentTypeChoices = new ParentTypeChoice[2];
        parentTypeChoices[0] = new ParentTypeChoice();
        parentTypeChoices[0].setParam1("param1");
        parentTypeChoices[1] = new ParentTypeChoice();
        parentTypeChoices[1].setParam2("param2");

        childElement_type0[0].setParentTypeChoice(parentTypeChoices);

        childElement_type0[1] = new ChildElement_type0();
        childElement_type0[1].setTestAttribute("Test Attribute");

        parentTypeChoices = new ParentTypeChoice[2];
        parentTypeChoices[0] = new ParentTypeChoice();
        parentTypeChoices[0].setParam1("param1");
        parentTypeChoices[1] = new ParentTypeChoice();
        parentTypeChoices[1].setParam2("param2");

        childElement_type0[1].setParentTypeChoice(parentTypeChoices);

        testElement.setChildElement(childElement_type0);

        testSerializeDeserialize(testElement, false);
    }
}
