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

package org.apache.axiom.om;

import junit.framework.TestCase;

public class SerializationTest extends TestCase {
    /**
     * Special case when OMElement is created with a null OMNamespace. In this case, that element
     * must always belongs to the default, default namespace
     */
    public void testNullOMNamespace() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace ns = fac.createOMNamespace("http://ws.apache.org/axis2/apacheconasia/06", "");
        OMElement personElem = fac.createOMElement("person", ns);

        //Create and add using null namespace...this should pick up default namespace from parent
        OMElement nameElem = fac.createOMElement("name", null);
        nameElem.setText("John");
        personElem.addChild(nameElem);

        String xml = personElem.toString();

        assertEquals("Incorrect namespace serialization", 2,
                     xml.split("http://ws.apache.org/axis2/apacheconasia/06").length);
        assertEquals("Incorrect serialization", 2, xml.split("xmlns=\"\"").length);

    }
}
