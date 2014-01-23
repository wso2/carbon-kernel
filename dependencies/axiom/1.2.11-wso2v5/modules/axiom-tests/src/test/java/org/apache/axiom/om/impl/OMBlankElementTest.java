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

package org.apache.axiom.om.impl;

import junit.framework.TestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;

import javax.xml.stream.XMLStreamException;
import java.io.StringWriter;

public class OMBlankElementTest extends TestCase {

    public OMBlankElementTest(String name) {
        super(name);
    }

    public void testBlankOMElem() throws XMLStreamException {
        try {
            //We should not get anything as the return value here: the output of the serialization
            String value = buildBlankOMElem();
            assertNull(
                    "There's a serialized output for a blank XML element that cannot exist",
                    value);
        } catch (OMException e) {
        }
    }

    String buildBlankOMElem() throws XMLStreamException {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace namespace1 = factory.createOMNamespace("", "");
        OMElement elem1 = factory.createOMElement("", namespace1);

        StringWriter writer = new StringWriter();
        elem1.build();
        elem1.serialize(writer);
        writer.flush();
        return writer.toString();
    }

    public void testOMElemWithWhiteSpace() throws XMLStreamException {
        try {
            //We should not get anything as the return value here: the output of the serialization
            String value = buildWithWhiteSpaceOMElem();
            assertNull(
                    "There's a serialized output for a blank XML element that cannot exist",
                    value);
        } catch (OMException e) {
        }
    }

    String buildWithWhiteSpaceOMElem() throws XMLStreamException {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace namespace1 = factory.createOMNamespace("  ", "");
        OMElement elem1 = factory.createOMElement("  ", namespace1);

        StringWriter writer = new StringWriter();
        elem1.build();
        elem1.serialize(writer);
        writer.flush();
        return writer.toString();
    }
}
