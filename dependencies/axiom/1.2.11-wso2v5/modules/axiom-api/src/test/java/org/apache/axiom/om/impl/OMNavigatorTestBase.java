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

import org.apache.axiom.om.AbstractTestCase;
import org.apache.axiom.om.OMConstants;
import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.om.OMSerializable;
import org.apache.axiom.om.TestConstants;
import org.apache.axiom.om.impl.OMNavigator;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import java.io.ByteArrayOutputStream;

public class OMNavigatorTestBase extends AbstractTestCase {
    private final OMMetaFactory omMetaFactory;
    private SOAPEnvelope envelope = null;
    private StAXSOAPModelBuilder builder;

    public OMNavigatorTestBase(OMMetaFactory omMetaFactory) {
        this.omMetaFactory = omMetaFactory;
    }

    protected void setUp() throws Exception {
        XMLStreamReader xmlStreamReader = StAXUtils.
                createXMLStreamReader(getTestResource(TestConstants.SOAP_SOAPMESSAGE1));
        builder = new StAXSOAPModelBuilder(omMetaFactory, xmlStreamReader, null);
        envelope = (SOAPEnvelope) builder.getDocumentElement();
    }

    protected void tearDown() throws Exception {
        builder.close();
    }

    public void testnavigatorFullyBuilt() throws Exception {
        assertNotNull(envelope);
        //dump the out put to a  temporary file
        XMLStreamWriter output =
            StAXUtils.createXMLStreamWriter(
                    new ByteArrayOutputStream(), OMConstants.DEFAULT_CHAR_SET_ENCODING);
        envelope.serialize(output);

        //now the OM is fully created -> test the navigation
        OMNavigator navigator = new OMNavigator(envelope);
        OMSerializable node = null;
        while (navigator.isNavigable()) {
            node = navigator.next();
            assertNotNull(node);
        }
    }

    public void testnavigatorHalfBuilt() {
        assertNotNull(envelope);
        //now the OM is not fully created. Try to navigate it
        OMNavigator navigator = new OMNavigator(envelope);
        OMSerializable node = null;
        while (navigator.isNavigable()) {
            node = navigator.next();
            assertNotNull(node);
        }
    }

    public void testnavigatorHalfBuiltStep() {
        assertNotNull(envelope);

        //now the OM is not fully created
        OMNavigator navigator = new OMNavigator(envelope);
        OMSerializable node = null;
        while (!navigator.isCompleted()) {
            if (navigator.isNavigable()) {
                node = navigator.next();
            } else {
                builder.next();
                navigator.step();
                node = navigator.next();
            }
            assertNotNull(node);

        }

    }
}
