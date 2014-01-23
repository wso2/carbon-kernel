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
package org.apache.axiom.om.impl.dom;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.axiom.om.impl.dom.factory.OMDOMMetaFactory;
import org.apache.axiom.ts.SOAPTestSuiteBuilder;
import org.apache.axiom.ts.soap.factory.TestGetDefaultFaultEnvelope;
import org.apache.axiom.ts.soap.faultdetail.TestWSCommons202;
import org.apache.axiom.ts.soap12.fault.TestMoreChildrenAddition;

public class SOAPImplementationTest extends TestCase {
    public static TestSuite suite() {
        SOAPTestSuiteBuilder builder = new SOAPTestSuiteBuilder(new OMDOMMetaFactory());
        builder.exclude(TestWSCommons202.class);
        builder.exclude(TestGetDefaultFaultEnvelope.class);
        
        // TODO: not sure if this is an issue in DOOM or if the test case is wrong
        builder.exclude(TestMoreChildrenAddition.class);
        
        return builder.build();
    }
}
