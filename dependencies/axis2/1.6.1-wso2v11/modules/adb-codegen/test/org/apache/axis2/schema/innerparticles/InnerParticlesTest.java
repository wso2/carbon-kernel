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

package org.apache.axis2.schema.innerparticles;

import org.apache.axis2.schema.AbstractTestCase;

public class InnerParticlesTest extends AbstractTestCase {

    public void testInnerParticle1() throws Exception {

        TestInnerParticle1 testInnerParticle1 = new TestInnerParticle1();
        testInnerParticle1.setParam1("Param1");
        TestInnerParticle1Sequence_type0 testInnerParticle1Sequence_type0 = new TestInnerParticle1Sequence_type0();
        testInnerParticle1Sequence_type0.setParam2("Param2");
        testInnerParticle1Sequence_type0.setParam3("Param3");
        testInnerParticle1.setTestInnerParticle1Sequence_type0(testInnerParticle1Sequence_type0);
        testInnerParticle1.setParam4("Param4");

        testSerializeDeserialize(testInnerParticle1, false);
    }

    public void testInnerParticle2() throws Exception {

        TestInnerParticle2 testInnerParticle2 = new TestInnerParticle2();
        testInnerParticle2.setParam1("Param1");
        TestInnerParticle2Choice_type0 testInnerParticle2Choice_type1 = new TestInnerParticle2Choice_type0();
        testInnerParticle2Choice_type1.setParam2("Param2");
        testInnerParticle2Choice_type1.setParam3("Param3");
        testInnerParticle2.setTestInnerParticle2Choice_type0(testInnerParticle2Choice_type1);
        testInnerParticle2.setParam4("Param4");

        TestInnerParticle2 expectedResult = new TestInnerParticle2();
        expectedResult.setParam1("Param1");
        TestInnerParticle2Choice_type0 expectedInnerParticle2Choice_type1 = new TestInnerParticle2Choice_type0();
        expectedInnerParticle2Choice_type1.setParam3("Param3");
        expectedResult.setTestInnerParticle2Choice_type0(expectedInnerParticle2Choice_type1);
        expectedResult.setParam4("Param4");
        
        testSerializeDeserialize(testInnerParticle2, expectedResult, false);
    }

    public void testInnerParticle31() throws Exception {

        TestInnerParticle3 testInnerParticle3 = new TestInnerParticle3();
        testInnerParticle3.setParam1("Param1");

        testSerializeDeserialize(testInnerParticle3, false);
    }

    public void testInnerParticle32() throws Exception {

        TestInnerParticle3 testInnerParticle3 = new TestInnerParticle3();
        TestInnerParticle3Choice_type0 testInnerParticle3Choice_type1 = new TestInnerParticle3Choice_type0();
        testInnerParticle3Choice_type1.setParam2("Param2");
        testInnerParticle3Choice_type1.setParam3("Param3");
        testInnerParticle3.setTestInnerParticle3Choice_type0(testInnerParticle3Choice_type1);

        TestInnerParticle3 expectedResult = new TestInnerParticle3();
        TestInnerParticle3Choice_type0 expectedInnerParticle3Choice_type1 = new TestInnerParticle3Choice_type0();
        expectedInnerParticle3Choice_type1.setParam3("Param3");
        expectedResult.setTestInnerParticle3Choice_type0(expectedInnerParticle3Choice_type1);

        testSerializeDeserialize(testInnerParticle3, expectedResult, false);
    }

    public void testInnerParticle33() throws Exception {

        TestInnerParticle3 testInnerParticle3 = new TestInnerParticle3();
        testInnerParticle3.setParam1("Param1");
        TestInnerParticle3Choice_type0 testInnerParticle3Choice_type1 = new TestInnerParticle3Choice_type0();
        testInnerParticle3Choice_type1.setParam2("Param2");
        testInnerParticle3Choice_type1.setParam3("Param3");
        testInnerParticle3.setTestInnerParticle3Choice_type0(testInnerParticle3Choice_type1);
        testInnerParticle3.setParam4("Param4");

        TestInnerParticle3 expectedResult = new TestInnerParticle3();
        TestInnerParticle3Choice_type0 expectedInnerParticle3Choice_type1 = new TestInnerParticle3Choice_type0();
        // TODO: it is a bit surprising that this property is not null; may be a bug
        expectedResult.setTestInnerParticle3Choice_type0(expectedInnerParticle3Choice_type1);
        expectedResult.setParam4("Param4");
        
        testSerializeDeserialize(testInnerParticle3, expectedResult, false);
    }

    public void testInnerParticle41() throws Exception {

        TestInnerParticle4 testInnerParticle4 = new TestInnerParticle4();
        testInnerParticle4.setParam1("Param1");

        testSerializeDeserialize(testInnerParticle4, false);
    }

    public void testInnerParticle42() throws Exception {

        TestInnerParticle4 testInnerParticle4 = new TestInnerParticle4();
        TestInnerParticle4Sequence_type0 TestInnerParticle4Sequence_type0 = new TestInnerParticle4Sequence_type0();
        TestInnerParticle4Sequence_type0.setParam2("Param2");
        TestInnerParticle4Sequence_type0.setParam3("Param3");
        testInnerParticle4.setTestInnerParticle4Sequence_type0(TestInnerParticle4Sequence_type0);

        testSerializeDeserialize(testInnerParticle4, false);
    }

    public void testInnerParticle43() throws Exception {

        TestInnerParticle4 testInnerParticle4 = new TestInnerParticle4();
        testInnerParticle4.setParam4("Param4");

        testSerializeDeserialize(testInnerParticle4, false);
    }

    public void testInnerParticle5() throws Exception {

        TestInnerParticle5 testInnerParticle5 = new TestInnerParticle5();
        testInnerParticle5.setParam1("Param1");
        TestInnerParticle5Sequence_type1 testInnerParticle2Choice_type1 = new TestInnerParticle5Sequence_type1();
        testInnerParticle2Choice_type1.setParam2("Param2");
        testInnerParticle2Choice_type1.setParam3("Param3");

        TestInnerParticle5Sequence_type0 innerParticle5Sequence_type0 = new TestInnerParticle5Sequence_type0();
        innerParticle5Sequence_type0.setParam4("Param4");
        innerParticle5Sequence_type0.setParam5("Param5");

        testInnerParticle2Choice_type1.setTestInnerParticle5Sequence_type0(innerParticle5Sequence_type0);
        testInnerParticle5.setTestInnerParticle5Sequence_type1(testInnerParticle2Choice_type1);
        testInnerParticle5.setParam6("Param6");

        testSerializeDeserialize(testInnerParticle5, false);
    }

    public void testIntterParticleExtension() throws Exception {
        TestInnerParticleExtension testInnerParticleExtension = new TestInnerParticleExtension();

        TestInnerParticleExtensionChildComplexType testInnerParticleExtensionChildComplexType =
                new TestInnerParticleExtensionChildComplexType();
        testInnerParticleExtension.setTestInnerParticleExtension(testInnerParticleExtensionChildComplexType);

        TestInnerParticleExtensionParentComplexTypeChoice_type0 testInnerParticleExtensionParentComplexTypeChoice_type0 =
                new TestInnerParticleExtensionParentComplexTypeChoice_type0();
        testInnerParticleExtensionChildComplexType.setTestInnerParticleExtensionParentComplexTypeChoice_type0(testInnerParticleExtensionParentComplexTypeChoice_type0);

        testInnerParticleExtensionParentComplexTypeChoice_type0.setParam1("param1");
        testInnerParticleExtensionParentComplexTypeChoice_type0.setParam2("param2");

        
        TestInnerParticleExtension expectedTestInnerParticleExtension = new TestInnerParticleExtension();

        TestInnerParticleExtensionChildComplexType expectedTestInnerParticleExtensionChildComplexType =
                new TestInnerParticleExtensionChildComplexType();
        expectedTestInnerParticleExtension.setTestInnerParticleExtension(expectedTestInnerParticleExtensionChildComplexType);

        TestInnerParticleExtensionParentComplexTypeChoice_type0 expectedTestInnerParticleExtensionParentComplexTypeChoice_type0 =
                new TestInnerParticleExtensionParentComplexTypeChoice_type0();
        expectedTestInnerParticleExtensionChildComplexType.setTestInnerParticleExtensionParentComplexTypeChoice_type0(expectedTestInnerParticleExtensionParentComplexTypeChoice_type0);

        expectedTestInnerParticleExtensionParentComplexTypeChoice_type0.setParam2("param2");
        
        testSerializeDeserialize(testInnerParticleExtension, expectedTestInnerParticleExtension, false);
    }

    public void testTestComplexTypeElement() throws Exception {
        TestComplexTypeElement testComplexTypeElement = new TestComplexTypeElement();

        TestComplexType testComplexType = new TestComplexType();
        testComplexTypeElement.setTestComplexTypeElement(testComplexType);

        TestElement_type0 testElement_type0 = new TestElement_type0();
        testElement_type0.setParam("param");
        testComplexType.setTestElement(testElement_type0);

        TestComplexTypeSequence_type0 testComplexTypeSequence_type0 = new TestComplexTypeSequence_type0();
        testComplexType.setTestComplexTypeSequence_type0(testComplexTypeSequence_type0);


        TestComplexTypeElement expectedTestComplexTypeElement = new TestComplexTypeElement();

        TestComplexType expectedTestComplexType = new TestComplexType();
        expectedTestComplexTypeElement.setTestComplexTypeElement(expectedTestComplexType);

        expectedTestComplexType.setTestElement(testElement_type0);
        
        testSerializeDeserialize(testComplexTypeElement, expectedTestComplexTypeElement, false);
    }
}
