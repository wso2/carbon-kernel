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

package org.apache.axis2.schema.particlemaxoccurs;

import org.apache.axis2.schema.AbstractTestCase;

public class ParticleMaxOccursTest extends AbstractTestCase {

    private int i = 0;

    public void testParticleSequenceMaxOccursTest1() throws Exception {
        TestParticleSequenceMaxOccurs1 testParticleMaxOccurs1 = new TestParticleSequenceMaxOccurs1();
        TestParticleSequenceMaxOccursType1 testParticleMaxOccursType1 = new TestParticleSequenceMaxOccursType1();
        testParticleMaxOccurs1.setTestParticleSequenceMaxOccurs1(testParticleMaxOccursType1);

        TestParticleSequenceMaxOccursType1Sequence[] testSequences = new TestParticleSequenceMaxOccursType1Sequence[2];

        testSequences[0] = new TestParticleSequenceMaxOccursType1Sequence();
        testSequences[0].setParm1("Param11");
        testSequences[0].setParm2("Param12");

        testSequences[1] = new TestParticleSequenceMaxOccursType1Sequence();
        testSequences[1].setParm1("Param21");
        testSequences[1].setParm2("Param22");

        testParticleMaxOccursType1.setTestParticleSequenceMaxOccursType1Sequence(testSequences);

        testSerializeDeserialize(testParticleMaxOccurs1, false);
    }

    public void testParticleSequenceMaxOccurs2() throws Exception {
        TestParticleSequenceMaxOccurs2 testParticleMaxOccurs2 = new TestParticleSequenceMaxOccurs2();
        TestParticleSequenceMaxOccursType2 testParticleMaxOccursType2 = new TestParticleSequenceMaxOccursType2();
        testParticleMaxOccurs2.setTestParticleSequenceMaxOccurs2(testParticleMaxOccursType2);

        TestParticleSequenceMaxOccursType2Sequence[] testSequences = new TestParticleSequenceMaxOccursType2Sequence[2];

        testSequences[0] = new TestParticleSequenceMaxOccursType2Sequence();
        testSequences[0].setParm1(new String[]{"Param111", "Param112", "Param113"});
        testSequences[0].setParm2(new String[]{"Param111", "Param112", "Param113"});

        testSequences[1] = new TestParticleSequenceMaxOccursType2Sequence();
        testSequences[1].setParm1(new String[]{"Param121", "Param122", "Param123"});
        testSequences[1].setParm2(new String[]{"Param121", "Param122", "Param123"});

        testParticleMaxOccursType2.setTestParticleSequenceMaxOccursType2Sequence(testSequences);

        testSerializeDeserialize(testParticleMaxOccurs2, false);
    }

    public void testParticleSequenceMaxOccurs3() throws Exception {
        TestParticleSequenceMaxOccurs3 testParticleMaxOccures3 = new TestParticleSequenceMaxOccurs3();
        TestParticleSequenceMaxOccursType3 testParticleMaxOccursType3 = new TestParticleSequenceMaxOccursType3();
        testParticleMaxOccures3.setTestParticleSequenceMaxOccurs3(testParticleMaxOccursType3);

        TestParticleSequenceMaxOccursType3Sequence[] testSequences = new TestParticleSequenceMaxOccursType3Sequence[2];

        testSequences[0] = new TestParticleSequenceMaxOccursType3Sequence();
        testSequences[0].setParm2(new String[]{"Param111", null, "Param113"});

        testSequences[1] = new TestParticleSequenceMaxOccursType3Sequence();
        testSequences[1].setParm1(new String[]{"Param121", "Param122", null});

        testParticleMaxOccursType3.setTestParticleSequenceMaxOccursType3Sequence(testSequences);


        TestParticleSequenceMaxOccurs3 expectedTestParticleMaxOccures3 = new TestParticleSequenceMaxOccurs3();
        TestParticleSequenceMaxOccursType3 expectedTestParticleMaxOccursType3 = new TestParticleSequenceMaxOccursType3();
        expectedTestParticleMaxOccures3.setTestParticleSequenceMaxOccurs3(expectedTestParticleMaxOccursType3);

        TestParticleSequenceMaxOccursType3Sequence[] expectedTestSequences = new TestParticleSequenceMaxOccursType3Sequence[2];

        expectedTestSequences[0] = new TestParticleSequenceMaxOccursType3Sequence();
        expectedTestSequences[0].setParm1(new String[]{null});
        expectedTestSequences[0].setParm2(new String[]{"Param111", null, "Param113"});

        expectedTestSequences[1] = new TestParticleSequenceMaxOccursType3Sequence();
        expectedTestSequences[1].setParm1(new String[]{"Param121", "Param122", null});
        expectedTestSequences[1].setParm2(new String[]{null});

        expectedTestParticleMaxOccursType3.setTestParticleSequenceMaxOccursType3Sequence(expectedTestSequences);
        
        
        testSerializeDeserialize(testParticleMaxOccures3, expectedTestParticleMaxOccures3, false);
    }

    public void testParticleSequenceMaxOccurs4() throws Exception {

        TestParticleSequenceMaxOccurs4 testParticleSequenceMaxOccurs4 = new TestParticleSequenceMaxOccurs4();
        TestParticleSequenceMaxOccursType4 testParticleSequenceMaxOccursType4 = new TestParticleSequenceMaxOccursType4();
        testParticleSequenceMaxOccurs4.setTestParticleSequenceMaxOccurs4(testParticleSequenceMaxOccursType4);

        TestParticleSequenceMaxOccursType4Sequence[] testParticleSequenceMaxOccursType4Sequence =
                new TestParticleSequenceMaxOccursType4Sequence[3];

        testParticleSequenceMaxOccursType4.setTestParticleSequenceMaxOccursType4Sequence(testParticleSequenceMaxOccursType4Sequence);
        testParticleSequenceMaxOccursType4Sequence[0] = new TestParticleSequenceMaxOccursType4Sequence();
        testParticleSequenceMaxOccursType4Sequence[0].setParm1(getNewCustomType());

        testParticleSequenceMaxOccursType4Sequence[1] = new TestParticleSequenceMaxOccursType4Sequence();
        testParticleSequenceMaxOccursType4Sequence[1].setParm2("Param2");

        testParticleSequenceMaxOccursType4Sequence[2] = new TestParticleSequenceMaxOccursType4Sequence();
        testParticleSequenceMaxOccursType4Sequence[2].setParm3(getNewCustomType());

        testSerializeDeserialize(testParticleSequenceMaxOccurs4, false);
    }

    public void testParticleSequenceMaxOccurs5() throws Exception {

        TestParticleSequenceMaxOccurs5 testParticleSequenceMaxOccurs5 = new TestParticleSequenceMaxOccurs5();
        TestParticleSequenceMaxOccursType5 testParticleSequenceMaxOccursType5 = new TestParticleSequenceMaxOccursType5();
        testParticleSequenceMaxOccurs5.setTestParticleSequenceMaxOccurs5(testParticleSequenceMaxOccursType5);

        TestParticleSequenceMaxOccursType5Sequence[] testParticleSequenceMaxOccursType5Sequence =
                new TestParticleSequenceMaxOccursType5Sequence[3];

        testParticleSequenceMaxOccursType5.setTestParticleSequenceMaxOccursType5Sequence(testParticleSequenceMaxOccursType5Sequence);

        testParticleSequenceMaxOccursType5Sequence[0] = new TestParticleSequenceMaxOccursType5Sequence();
        testParticleSequenceMaxOccursType5Sequence[0].setParm1(new TestCustomType[]{getNewCustomType()});
        testParticleSequenceMaxOccursType5Sequence[0].setParm3(new TestCustomType[]{getNewCustomType()});

        testParticleSequenceMaxOccursType5Sequence[1] = new TestParticleSequenceMaxOccursType5Sequence();
        testParticleSequenceMaxOccursType5Sequence[1].setParm1(new TestCustomType[]{getNewCustomType()});
        testParticleSequenceMaxOccursType5Sequence[1].setParm2("Param2");
        testParticleSequenceMaxOccursType5Sequence[1].setParm3(new TestCustomType[]{getNewCustomType()});

        testParticleSequenceMaxOccursType5Sequence[2] = new TestParticleSequenceMaxOccursType5Sequence();
        testParticleSequenceMaxOccursType5Sequence[2].setParm1(new TestCustomType[]{getNewCustomType()});
        testParticleSequenceMaxOccursType5Sequence[2].setParm3(new TestCustomType[]{getNewCustomType()});

        testSerializeDeserialize(testParticleSequenceMaxOccurs5, false);
    }

    public void testParticleSequenceMaxOccursTest6() throws Exception {
        TestParticleSequenceMaxOccurs6 testParticleMaxOccures6 = new TestParticleSequenceMaxOccurs6();
        TestParticleSequenceMaxOccursType6 testParticleMaxOccursType6 = new TestParticleSequenceMaxOccursType6();
        testParticleMaxOccures6.setTestParticleSequenceMaxOccurs6(testParticleMaxOccursType6);

        TestParticleSequenceMaxOccursType6Sequence[] testSequences = new TestParticleSequenceMaxOccursType6Sequence[2];

        testSequences[0] = new TestParticleSequenceMaxOccursType6Sequence();
        testSequences[0].setParm1("Param11");
        testSequences[0].setParm2("Param12");

        testSequences[1] = new TestParticleSequenceMaxOccursType6Sequence();
        testSequences[1].setParm1("Param21");
        testSequences[1].setParm2("Param22");

        testParticleMaxOccursType6.setTestParticleSequenceMaxOccursType6Sequence(testSequences);
        testParticleMaxOccursType6.setAttribute1("Attribute1");
        testParticleMaxOccursType6.setAttribute2("Attribute2");

        testSerializeDeserialize(testParticleMaxOccures6, false);
    }

    public void testParticleMaxOccursTest() throws Exception {
        TestParticleChoiceMaxOccurs testParticleChoiceMaxOccurs = new TestParticleChoiceMaxOccurs();
        TestParticleChoiceMaxOccursType testParticleChoiceMaxOccursType = new TestParticleChoiceMaxOccursType();
        testParticleChoiceMaxOccurs.setTestParticleChoiceMaxOccurs(testParticleChoiceMaxOccursType);

        testParticleChoiceMaxOccursType.setParm1("Param1");

        testSerializeDeserialize(testParticleChoiceMaxOccurs, false);
    }

    public void testParticleChoiceMaxOccursTest1() throws Exception {
        TestParticleChoiceMaxOccurs1 testParticleMaxOccures1 = new TestParticleChoiceMaxOccurs1();
        TestParticleChoiceMaxOccursType1 testParticleMaxOccursType1 = new TestParticleChoiceMaxOccursType1();
        testParticleMaxOccures1.setTestParticleChoiceMaxOccurs1(testParticleMaxOccursType1);

        TestParticleChoiceMaxOccursType1Choice[] testChoices = new TestParticleChoiceMaxOccursType1Choice[2];

        testChoices[0] = new TestParticleChoiceMaxOccursType1Choice();
        testChoices[0].setParm1("Param11");

        testChoices[1] = new TestParticleChoiceMaxOccursType1Choice();
        testChoices[1].setParm2("Param12");

        testParticleMaxOccursType1.setTestParticleChoiceMaxOccursType1Choice(testChoices);

        testSerializeDeserialize(testParticleMaxOccures1, false);
    }

    public void testParticleChoiceMaxOccurs2() throws Exception {
        TestParticleChoiceMaxOccurs2 testParticleMaxOccures2 = new TestParticleChoiceMaxOccurs2();
        TestParticleChoiceMaxOccursType2 testParticleMaxOccursType2 = new TestParticleChoiceMaxOccursType2();
        testParticleMaxOccures2.setTestParticleChoiceMaxOccurs2(testParticleMaxOccursType2);

        TestParticleChoiceMaxOccursType2Choice[] testChoices = new TestParticleChoiceMaxOccursType2Choice[2];

        testChoices[0] = new TestParticleChoiceMaxOccursType2Choice();
        testChoices[0].setParm1(new String[]{"Param111", "Param112", "Param113"});

        testChoices[1] = new TestParticleChoiceMaxOccursType2Choice();
        testChoices[1].setParm2(new String[]{"Param121", "Param122", "Param123"});

        testParticleMaxOccursType2.setTestParticleChoiceMaxOccursType2Choice(testChoices);

        testSerializeDeserialize(testParticleMaxOccures2, false);
    }

    public void testParticleChoiceMaxOccurs3() throws Exception {
        TestParticleChoiceMaxOccurs3 testParticleMaxOccures3 = new TestParticleChoiceMaxOccurs3();
        TestParticleChoiceMaxOccursType3 testParticleMaxOccursType3 = new TestParticleChoiceMaxOccursType3();
        testParticleMaxOccures3.setTestParticleChoiceMaxOccurs3(testParticleMaxOccursType3);

        TestParticleChoiceMaxOccursType3Choice[] testChoices = new TestParticleChoiceMaxOccursType3Choice[2];

        testChoices[0] = new TestParticleChoiceMaxOccursType3Choice();
        testChoices[0].setParm1(new String[]{"Param111", null, "Param113"});

        testChoices[1] = new TestParticleChoiceMaxOccursType3Choice();
        testChoices[1].setParm2(new String[]{"Param121", "Param122", null});

        testParticleMaxOccursType3.setTestParticleChoiceMaxOccursType3Choice(testChoices);

        testSerializeDeserialize(testParticleMaxOccures3, false);
    }

    public void testParticleChoiceMaxOccurs4() throws Exception {

        TestParticleChoiceMaxOccurs4 testParticleChoiceMaxOccurs4 = new TestParticleChoiceMaxOccurs4();
        TestParticleChoiceMaxOccursType4 testParticleChoiceMaxOccursType4 = new TestParticleChoiceMaxOccursType4();
        testParticleChoiceMaxOccurs4.setTestParticleChoiceMaxOccurs4(testParticleChoiceMaxOccursType4);

        TestParticleChoiceMaxOccursType4Choice[] testParticleChoiceMaxOccursType4Choice =
                new TestParticleChoiceMaxOccursType4Choice[3];

        testParticleChoiceMaxOccursType4.setTestParticleChoiceMaxOccursType4Choice(testParticleChoiceMaxOccursType4Choice);
        testParticleChoiceMaxOccursType4Choice[0] = new TestParticleChoiceMaxOccursType4Choice();
        testParticleChoiceMaxOccursType4Choice[0].setParm1(getNewCustomType());

        testParticleChoiceMaxOccursType4Choice[1] = new TestParticleChoiceMaxOccursType4Choice();
        testParticleChoiceMaxOccursType4Choice[1].setParm2("Param2");

        testParticleChoiceMaxOccursType4Choice[2] = new TestParticleChoiceMaxOccursType4Choice();
        testParticleChoiceMaxOccursType4Choice[2].setParm3(getNewCustomType());

        testSerializeDeserialize(testParticleChoiceMaxOccurs4, false);
    }

    public void testParticleChoiceMaxOccurs5() throws Exception {

        TestParticleChoiceMaxOccurs5 testParticleChoiceMaxOccurs5 = new TestParticleChoiceMaxOccurs5();
        TestParticleChoiceMaxOccursType5 testParticleChoiceMaxOccursType5 = new TestParticleChoiceMaxOccursType5();
        testParticleChoiceMaxOccurs5.setTestParticleChoiceMaxOccurs5(testParticleChoiceMaxOccursType5);

        TestParticleChoiceMaxOccursType5Choice[] testParticleChoiceMaxOccursType5Choice =
                new TestParticleChoiceMaxOccursType5Choice[3];

        testParticleChoiceMaxOccursType5.setTestParticleChoiceMaxOccursType5Choice(testParticleChoiceMaxOccursType5Choice);

        testParticleChoiceMaxOccursType5Choice[0] = new TestParticleChoiceMaxOccursType5Choice();
        testParticleChoiceMaxOccursType5Choice[0].setParm1(new TestCustomType[]{getNewCustomType()});

        testParticleChoiceMaxOccursType5Choice[1] = new TestParticleChoiceMaxOccursType5Choice();
        testParticleChoiceMaxOccursType5Choice[1].setParm2("Param2");

        testParticleChoiceMaxOccursType5Choice[2] = new TestParticleChoiceMaxOccursType5Choice();
        testParticleChoiceMaxOccursType5Choice[2].setParm3(new TestCustomType[]{getNewCustomType()});

        testSerializeDeserialize(testParticleChoiceMaxOccurs5, false);
    }

    public void testParticleChoiceMaxOccursTest6() throws Exception {
        TestParticleChoiceMaxOccurs6 testParticleMaxOccures6 = new TestParticleChoiceMaxOccurs6();
        TestParticleChoiceMaxOccursType6 testParticleMaxOccursType6 = new TestParticleChoiceMaxOccursType6();
        testParticleMaxOccures6.setTestParticleChoiceMaxOccurs6(testParticleMaxOccursType6);

        TestParticleChoiceMaxOccursType6Choice[] testChoices = new TestParticleChoiceMaxOccursType6Choice[2];

        testChoices[0] = new TestParticleChoiceMaxOccursType6Choice();
        testChoices[0].setParm1("Param11");

        testChoices[1] = new TestParticleChoiceMaxOccursType6Choice();
        testChoices[1].setParm2("Param12");

        testParticleMaxOccursType6.setTestParticleChoiceMaxOccursType6Choice(testChoices);
        testParticleMaxOccursType6.setAttribute1("Attribute1");
        testParticleMaxOccursType6.setAttribute2("Attribute2");

        testSerializeDeserialize(testParticleMaxOccures6, false);
    }

    private TestCustomType getNewCustomType() {
        i++;
        TestCustomType testCustomType = new TestCustomType();
        testCustomType.setParam1("Param" + i + "2");
        testCustomType.setParam2(new String[]{"Param" + i + "21", "Param" + i + "22", "Param" + i + "23"});
        testCustomType.setParam3("Param" + i + "3");
        return testCustomType;
    }
}
