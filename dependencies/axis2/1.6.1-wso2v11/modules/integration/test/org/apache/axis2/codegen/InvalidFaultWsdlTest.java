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

package org.apache.axis2.codegen;

import junit.framework.TestCase;
import org.apache.axis2.wsdl.WSDL2Code;

public class InvalidFaultWsdlTest extends TestCase {
    public void testInvaidWsdl() {
        try {
            String wsdluri = "test-resources/Invalid.wsdl";
            String args[] = new String[] { "-uri", wsdluri, "-o", "target/invalidwsdloutlocation" };
            new WSDL2Code().main(args);
            fail("This must fail with the excetion :" +
                    "No element reference found for the part in fault message " +
                    "InvalidIsbnFault. Fault message part should always be refer " +
                    "to an element.");
        } catch (Exception e) {
            assertTrue(e.getCause().getMessage().startsWith(
                    "Part 'message' of fault message '{http://www.Monson-Haefel.com/jwsbook/BookQuote}InvalidIsbnFault'"));
        }
    }
}
