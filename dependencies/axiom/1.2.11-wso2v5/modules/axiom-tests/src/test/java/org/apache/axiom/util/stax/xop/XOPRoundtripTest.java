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

package org.apache.axiom.util.stax.xop;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.testutils.activation.TestDataSource;

public class XOPRoundtripTest extends TestCase {
    public void test() {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        DataHandler dh = new DataHandler(new TestDataSource('x', Runtime.getRuntime().maxMemory()));
        OMElement element1 = factory.createOMElement(new QName("test"));
        element1.addChild(factory.createOMText(dh, true));
        XMLStreamReader originalReader = element1.getXMLStreamReader();
        XOPEncodingStreamReader encodedReader = new XOPEncodingStreamReader(originalReader,
                ContentIDGenerator.DEFAULT, OptimizationPolicy.DEFAULT);
        XMLStreamReader decodedReader = new XOPDecodingStreamReader(encodedReader, encodedReader);
        OMElement element2 = new StAXOMBuilder(decodedReader).getDocumentElement();
        OMText child = (OMText)element2.getFirstOMChild();
        assertNotNull(child);
        assertTrue(child.isBinary());
        assertTrue(child.isOptimized());
        assertSame(dh, child.getDataHandler());
    }
}
