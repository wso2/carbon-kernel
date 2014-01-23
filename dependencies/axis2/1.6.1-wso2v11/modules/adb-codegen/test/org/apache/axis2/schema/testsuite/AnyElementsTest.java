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

package org.apache.axis2.schema.testsuite;

import org.apache.axiom.om.OMElement;

public class AnyElementsTest extends AbstractTest {

    public static final int MIN_EQUALS_ZERO = 0;
    public static final int MIN_EQUALS_ONE = 1;

    public void testAnyArray1() throws Exception {
        TestAny1 testAny = new TestAny1();
        testAny.setExtraElement(null);
        testSerializeDeserialize(testAny);
    }
    
    public void testAnyArray2() throws Exception {
        TestAny1 testAny = new TestAny1();
        testAny.setExtraElement(new OMElement[]{null});
        TestAny1 expected = new TestAny1();
        testAny.setExtraElement(null);
        testSerializeDeserialize(testAny, expected);
    }
    
    public void testAnyArray3() throws Exception {
        TestAny1 testAny = new TestAny1();
        testAny.setExtraElement(new OMElement[]{getOMElement()});
        testSerializeDeserialize(testAny, false);
    }
    
    public void testAnyArray4() throws Exception {
        TestAny1 testAny = new TestAny1();
        testAny.setExtraElement(new OMElement[]{getOMElement(), getOMElement()});
        testSerializeDeserialize(testAny, false);
    }
    
    public void testAnyArray5() throws Exception {
        TestAny3 testAny = new TestAny3();
        testAny.setExtraElement(null);
        assertSerializationFailure(testAny);
    }
    
    public void testAnyArray6() throws Exception {
        TestAny3 testAny = new TestAny3();
        testAny.setExtraElement(new OMElement[]{null});
        assertSerializationFailure(testAny);
    }
    
    public void testAnyArray7() throws Exception {
        TestAny3 testAny = new TestAny3();
        testAny.setExtraElement(new OMElement[]{getOMElement(), getOMElement()});
        testSerializeDeserialize(testAny, false);
    }
    
    public void testAny1() throws Exception {
        TestAny2 testAny = new TestAny2();
        testAny.setExtraElement(null);
        testSerializeDeserialize(testAny);
    }
    
    public void testAny2() throws Exception {
        TestAny2 testAny = new TestAny2();
        testAny.setExtraElement(getOMElement());
        testSerializeDeserialize(testAny);
    }
    
    public void testAny3() throws Exception {
        TestAny4 testAny = new TestAny4();
        testAny.setExtraElement(null);
        assertSerializationFailure(testAny);
    }
    
    public void testAny4() throws Exception {
        TestAny4 testAny = new TestAny4();
        testAny.setExtraElement(getOMElement());
        testSerializeDeserialize(testAny);
    }
}
