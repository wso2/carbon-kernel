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

package org.apache.axis2.schema.restriction;

import org.apache.axis2.schema.AbstractTestCase;

import java.math.BigInteger;

public class ComplexRestrictionTest extends AbstractTestCase {

    public void testGetExemplarResponseTypeElement() throws Exception {
        GetExemplarResponseTypeElement getExemplarResponseTypeElement = new GetExemplarResponseTypeElement();
        GetExemplarResponseType getExemplarResponseType = new GetExemplarResponseType();
        getExemplarResponseType.setExemplar("test string1");
        getExemplarResponseType.setResponseCode(new BigInteger("23"));
        getExemplarResponseType.setResponseMessage("test string2");
        getExemplarResponseType.setSupportedMethods("test string3");

        getExemplarResponseTypeElement.setGetExemplarResponseTypeElement(getExemplarResponseType);

        testSerializeDeserialize(getExemplarResponseTypeElement, false);
    }

}
