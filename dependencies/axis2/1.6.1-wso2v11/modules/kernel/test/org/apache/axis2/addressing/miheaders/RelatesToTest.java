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

package org.apache.axis2.addressing.miheaders;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import org.apache.axis2.addressing.RelatesTo;


public class RelatesToTest extends TestCase {
    private RelatesTo relatesTo;
    String address = "www.someaddress.com";
    String relationshipType = "Reply";


    public static void main(String[] args) {
        TestRunner.run(RelatesToTest.class);

    }

    protected void setUp() throws Exception {

    }

    public void testGetAddress() {
        relatesTo = new RelatesTo(address, relationshipType);

        assertEquals(
                "RelatesTo address has not been set properly in the constructor",
                relatesTo.getValue(),
                address);

        String newAddress = "www.newRelation.org";
        relatesTo.setValue(newAddress);
        assertEquals("RelatesTo address has not been get/set properly",
                     relatesTo.getValue(),
                     newAddress);

    }

    public void testGetRelationshipType() {
        relatesTo = new RelatesTo(address, relationshipType);

        assertEquals(
                "RelatesTo RelationshipType has not been set properly in the constructor",
                relatesTo.getRelationshipType(),
                relationshipType);

        String newRelationshipType = "AnyOtherType";
        relatesTo.setRelationshipType(newRelationshipType);
        assertEquals("RelatesTo address has not been get/set properly",
                     relatesTo.getRelationshipType(),
                     newRelationshipType);
    }

    public void testSingleArgumentConstructor() {
        relatesTo = new RelatesTo(address);
        assertEquals(
                "RelatesTo address has not been set properly in the constructor",
                relatesTo.getValue(),
                address);

    }

}
