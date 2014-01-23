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

package org.apache.axiom.soap.impl.llom;

import org.apache.axiom.om.OMTestCase;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;

import java.util.Iterator;

public class OMHeaderBlockTest extends OMTestCase {
    SOAPHeader soapHeader;
    SOAPHeaderBlock soapHeaderElement;

    public OMHeaderBlockTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        soapHeader = soapEnvelope.getHeader();
        Iterator headerElementIter = soapHeader.examineAllHeaderBlocks();
        if (headerElementIter.hasNext()) {
            soapHeaderElement = (SOAPHeaderBlock) headerElementIter.next();
        }
    }

    public void testSetAndGetActor() {
        String newActorURI = "http://newActor.org";
        soapHeaderElement.setRole(newActorURI);
        assertTrue("Actor was not properly set",
                   soapHeaderElement.getRole().equalsIgnoreCase(newActorURI));
    }

    public void testSetAndGetMustUnderstand() {
        soapHeaderElement.setMustUnderstand(false);
        assertTrue("MustUnderstand was not properly set",
                   !soapHeaderElement.getMustUnderstand());
    }

    public void testGetMustUnderstand() {
        //TODO Implement getMustUnderstand().
    }

}
