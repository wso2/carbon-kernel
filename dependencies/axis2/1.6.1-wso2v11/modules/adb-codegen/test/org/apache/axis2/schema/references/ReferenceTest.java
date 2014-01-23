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

package org.apache.axis2.schema.references;

import org.apache.axis2.schema.AbstractTestCase;

public class ReferenceTest extends AbstractTestCase {

    public void testSingleElementReference() throws Exception {
        CheckEligibility1 echCheckEligibility1 = new CheckEligibility1();
        AtbRequestCheckEligibility_type0 atbRequestCheckEligibility = new AtbRequestCheckEligibility_type0();
        echCheckEligibility1.setAtbRequestCheckEligibility(atbRequestCheckEligibility);
        atbRequestCheckEligibility.setCardNumber("carnumber");
        atbRequestCheckEligibility.setClientId("clientid");
        atbRequestCheckEligibility.setExpirationDate("date");
        atbRequestCheckEligibility.setNameAsOnCard("cardname");
        atbRequestCheckEligibility.setYearOfRedemption(2);
        testSerializeDeserialize(echCheckEligibility1, false);
    }

    public void testMultipleElementReference() throws Exception {
        CheckEligibility2 echCheckEligibility2 = new CheckEligibility2();
        AtbRequestCheckEligibility_type0[] atbRequestCheckEligibility = new AtbRequestCheckEligibility_type0[2];
        echCheckEligibility2.setAtbRequestCheckEligibility(atbRequestCheckEligibility);

        atbRequestCheckEligibility[0] = new AtbRequestCheckEligibility_type0();
        atbRequestCheckEligibility[0].setCardNumber("carnumber");
        atbRequestCheckEligibility[0].setClientId("clientid");
        atbRequestCheckEligibility[0].setExpirationDate("date");
        atbRequestCheckEligibility[0].setNameAsOnCard("cardname");
        atbRequestCheckEligibility[0].setYearOfRedemption(2);

        atbRequestCheckEligibility[1] = new AtbRequestCheckEligibility_type0();
        atbRequestCheckEligibility[1].setCardNumber("carnumber");
        atbRequestCheckEligibility[1].setClientId("clientid");
        atbRequestCheckEligibility[1].setExpirationDate("date");
        atbRequestCheckEligibility[1].setNameAsOnCard("cardname");
        atbRequestCheckEligibility[1].setYearOfRedemption(2);

        testSerializeDeserialize(echCheckEligibility2, false);
    }

    public void testElement11() throws Exception {

        Element1 element1 = new Element1();
        ComplexType1 complexType1 = new ComplexType1();
        element1.setElement1(complexType1);
        testSerializeDeserialize(element1);
    }

    public void testElement12() throws Exception {

        Element1 element1 = new Element1();
        ComplexType1 complexType1 = new ComplexType1();
        element1.setElement1(complexType1);
        ComplexType1 complexType2 = new ComplexType1();
        complexType1.setElement1(complexType2);
        ComplexType1 complexType3 = new ComplexType1();
        complexType2.setElement1(complexType3);
        testSerializeDeserialize(element1);
    }

    public void testElement21() throws Exception {
        Element2 element2 = new Element2();
        Element2_type0 element2_type0 = new Element2_type0();
        element2.setElement2(element2_type0);
        element2_type0.setParam1("test string1");

        Element2_type0 element2_type1 = new Element2_type0();
        element2_type1.setParam1("test string2");
        element2_type0.setElement2(element2_type1);

        testSerializeDeserialize(element2, false);
    }

    public void testSimpleReference() throws Exception {
        TestSimpleReference testSimpleReference = new TestSimpleReference();
        Discard_transferToken discard_transferToken = new Discard_transferToken();
// FIXME: Breaks in IBM JDK 1.5 - the generated code there is looking for AuthInfo_type0 instead of AuthInfo_type1
//        AuthInfo_type1 authInfo_type1 = new AuthInfo_type1();
//        authInfo_type1.setAuthInfo_type1("Simple param");
//        discard_transferToken.setAuthInfo(authInfo_type1);
        discard_transferToken.setParam1("New parm");
        testSimpleReference.setTestSimpleReference(discard_transferToken);

        testSerializeDeserialize(testSimpleReference, false);
    }
}
