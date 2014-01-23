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

package org.apache.axis2.schema.union2;

import org.apache.axis2.schema.AbstractTestCase;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class Union2Test extends AbstractTestCase {

    public void testUnionElement2() throws Exception {
        TestUnionElement2 testUnionElement2 = new TestUnionElement2();

        TestUnion1 param1 = new TestUnion1();
        param1.setObject(Boolean.TRUE);
        testUnionElement2.setParam1(param1);

        TestUnion1 attribute1 = new TestUnion1();
        attribute1.setObject("test attribute");

        testUnionElement2.setAttribute1(attribute1);

        testSerializeDeserialize(testUnionElement2, false);
    }

    public void testListElement2() throws Exception {
        TestListElement1 testListElement1 = new TestListElement1();

        TestList1 param1 = new TestList1();
        param1.setString(new String[]{"param1", "param2"});
        testListElement1.setParam1(param1);

        TestList1 attribute1 = new TestList1();
        attribute1.setString(new String[]{"attribute1","attribute2"});
        testListElement1.setAttribute1(attribute1);

        testSerializeDeserialize(testListElement1, false);
    }

    public void testFuzzDateType() throws Exception {
        TestFuzzyDateType testFuzzyDateType = new TestFuzzyDateType();
        FuzzyDateType fuzzyDateType = new FuzzyDateType();
        fuzzyDateType.setObject(new Date());
        testFuzzyDateType.setTestFuzzyDateType(fuzzyDateType);

        // java.util.Date maps to xs:date, so we expect to loose the time information
        TestFuzzyDateType expectedResult = new TestFuzzyDateType();
        FuzzyDateType expectedFuzzyDateType = new FuzzyDateType();
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        expectedFuzzyDateType.setObject(cal.getTime());
        expectedResult.setTestFuzzyDateType(expectedFuzzyDateType);
        
        testSerializeDeserialize(testFuzzyDateType, expectedResult, false);
    }
}
