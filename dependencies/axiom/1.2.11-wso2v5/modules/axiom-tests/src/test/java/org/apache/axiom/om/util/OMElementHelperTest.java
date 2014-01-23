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

package org.apache.axiom.om.util;

import org.apache.axiom.om.AbstractTestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.dom.DOOMAbstractFactory;

import javax.xml.stream.XMLStreamReader;

public class OMElementHelperTest extends AbstractTestCase {

    private String testXMLFilePath = "soap/soapmessage.xml";


    public void testImportOMElement() throws Exception {
        XMLStreamReader xmlStreamReader = StAXUtils.createXMLStreamReader(
                getTestResource(testXMLFilePath));
        OMElement documentElement =
                new StAXOMBuilder(OMAbstractFactory.getOMFactory(), xmlStreamReader)
                        .getDocumentElement();

        // first lets try to import an element created from llom in to llom factory. This should return the same element
        assertTrue(ElementHelper
                .importOMElement(documentElement, OMAbstractFactory.getOMFactory()) ==
                documentElement);

        // then lets pass in an OMElement created using llom and pass DOOMFactory
        OMElement importedElement = ElementHelper
                .importOMElement(documentElement, DOOMAbstractFactory.getOMFactory());
        assertTrue(importedElement != documentElement);
        assertTrue(importedElement.getOMFactory().getClass().isInstance(
                DOOMAbstractFactory.getOMFactory()));
        
        documentElement.close(false);
    }
}
