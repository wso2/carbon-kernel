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
package org.apache.axiom.ts;

import org.apache.axiom.om.AbstractTestCase;
import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.ts.om.factory.OMElementCreator;

public class OMTestSuiteBuilder extends AxiomTestSuiteBuilder {
    public OMTestSuiteBuilder(OMMetaFactory metaFactory) {
        super(metaFactory);
    }
    
    protected void addTests() {
        String[] conformanceFiles = AbstractTestCase.getConformanceTestFiles();
        addTest(new org.apache.axiom.ts.om.attribute.TestEqualsHashCode(metaFactory));
        addTest(new org.apache.axiom.ts.om.attribute.TestGetQNameWithNamespace(metaFactory));
        addTest(new org.apache.axiom.ts.om.attribute.TestGetQNameWithoutNamespace(metaFactory));
        addTest(new org.apache.axiom.ts.om.builder.TestGetDocumentElement(metaFactory));
        addTest(new org.apache.axiom.ts.om.builder.TestGetDocumentElementWithDiscardDocument(metaFactory));
        for (int i=0; i<conformanceFiles.length; i++) {
            addTest(new org.apache.axiom.ts.om.document.TestGetXMLStreamReader(metaFactory, conformanceFiles[i], true));
            addTest(new org.apache.axiom.ts.om.document.TestGetXMLStreamReader(metaFactory, conformanceFiles[i], false));
        }
        addTest(new org.apache.axiom.ts.om.document.TestIsCompleteAfterAddingIncompleteChild(metaFactory));
        addTest(new org.apache.axiom.ts.om.document.TestSerializeAndConsume(metaFactory));
        addTest(new org.apache.axiom.ts.om.document.TestSerializeAndConsumeWithIncompleteDescendant(metaFactory));
        addTest(new org.apache.axiom.ts.om.element.TestAddAttributeAlreadyOwnedByElement(metaFactory));
        addTest(new org.apache.axiom.ts.om.element.TestAddAttributeAlreadyOwnedByOtherElement(metaFactory));
        addTest(new org.apache.axiom.ts.om.element.TestAddAttributeFromOMAttributeWithExistingName(metaFactory));
        addTest(new org.apache.axiom.ts.om.element.TestAddAttributeReplace1(metaFactory));
        addTest(new org.apache.axiom.ts.om.element.TestAddAttributeReplace2(metaFactory));
        addTest(new org.apache.axiom.ts.om.element.TestAddAttributeWithExistingNamespaceDeclarationInScope(metaFactory));
        addTest(new org.apache.axiom.ts.om.element.TestAddAttributeWithExistingNamespaceDeclarationOnSameElement(metaFactory));
        addTest(new org.apache.axiom.ts.om.element.TestAddAttributeWithMaskedNamespaceDeclaration(metaFactory));
        addTest(new org.apache.axiom.ts.om.element.TestAddAttributeWithoutExistingNamespaceDeclaration(metaFactory));
        addTest(new org.apache.axiom.ts.om.element.TestAddChild(metaFactory));
        addTest(new org.apache.axiom.ts.om.element.TestAddChildWithParent(metaFactory));
        addTest(new org.apache.axiom.ts.om.element.TestGetAllAttributes1(metaFactory));
        addTest(new org.apache.axiom.ts.om.element.TestGetAllAttributes2(metaFactory));
        addTest(new org.apache.axiom.ts.om.element.TestGetAllDeclaredNamespaces(metaFactory));
        addTest(new org.apache.axiom.ts.om.element.TestGetAttributeValueNonExisting(metaFactory));
        addTest(new org.apache.axiom.ts.om.element.TestGetAttributeValueWithXmlPrefix1(metaFactory));
        addTest(new org.apache.axiom.ts.om.element.TestGetAttributeValueWithXmlPrefix2(metaFactory));
        addTest(new org.apache.axiom.ts.om.element.TestGetAttributeWithXmlPrefix1(metaFactory));
        addTest(new org.apache.axiom.ts.om.element.TestGetAttributeWithXmlPrefix2(metaFactory));
        addTest(new org.apache.axiom.ts.om.element.TestGetChildElements(metaFactory));
        addTest(new org.apache.axiom.ts.om.element.TestGetChildren(metaFactory));
        addTest(new org.apache.axiom.ts.om.element.TestGetChildrenRemove1(metaFactory));
        addTest(new org.apache.axiom.ts.om.element.TestGetChildrenRemove2(metaFactory));
        addTest(new org.apache.axiom.ts.om.element.TestGetChildrenRemove3(metaFactory));
        addTest(new org.apache.axiom.ts.om.element.TestGetChildrenRemove4(metaFactory));
        addTest(new org.apache.axiom.ts.om.element.TestGetChildrenWithLocalName(metaFactory));
        addTest(new org.apache.axiom.ts.om.element.TestGetChildrenWithName(metaFactory));
        addTest(new org.apache.axiom.ts.om.element.TestGetFirstChildWithName(metaFactory));
        addTest(new org.apache.axiom.ts.om.element.TestGetFirstChildWithNameOnIncompleteElement(metaFactory));
        addTest(new org.apache.axiom.ts.om.element.TestGetQNameWithoutNamespace(metaFactory));
        addTest(new org.apache.axiom.ts.om.element.TestIsCompleteAfterAddingIncompleteChild(metaFactory));
        addTest(new org.apache.axiom.ts.om.element.TestResolveQNameWithDefaultNamespace(metaFactory));
        addTest(new org.apache.axiom.ts.om.element.TestResolveQNameWithNonDefaultNamespace(metaFactory));
        addTest(new org.apache.axiom.ts.om.element.TestResolveQNameWithoutNamespace(metaFactory));
        addTest(new org.apache.axiom.ts.om.element.TestSerialization(metaFactory, "D", "D",
                "<person xmlns=\"urn:ns\"><name>John</name><age>34</age><weight>50</weight></person>"));
        addTest(new org.apache.axiom.ts.om.element.TestSerialization(metaFactory, "D", "U",
                "<person xmlns=\"urn:ns\"><name xmlns=\"\">John</name><age xmlns=\"\">34</age><weight xmlns=\"\">50</weight></person>"));
        addTest(new org.apache.axiom.ts.om.element.TestSerialization(metaFactory, "D", "Q",
                "<person xmlns=\"urn:ns\"><p:name xmlns:p=\"urn:ns\">John</p:name><p:age xmlns:p=\"urn:ns\">34</p:age><p:weight xmlns:p=\"urn:ns\">50</p:weight></person>"));
        addTest(new org.apache.axiom.ts.om.element.TestSerialization(metaFactory, "Q", "Q",
                "<p:person xmlns:p=\"urn:ns\"><p:name>John</p:name><p:age>34</p:age><p:weight>50</p:weight></p:person>"));
        addTest(new org.apache.axiom.ts.om.element.TestSerialization(metaFactory, "Q", "U",
                "<p:person xmlns:p=\"urn:ns\"><name>John</name><age>34</age><weight>50</weight></p:person>"));
        addTest(new org.apache.axiom.ts.om.element.TestSerialization(metaFactory, "Q", "D",
                "<p:person xmlns:p=\"urn:ns\"><name xmlns=\"urn:ns\">John</name><age xmlns=\"urn:ns\">34</age><weight xmlns=\"urn:ns\">50</weight></p:person>"));
        addTest(new org.apache.axiom.ts.om.element.TestSerialization(metaFactory, "U", "U",
                "<person><name>John</name><age>34</age><weight>50</weight></person>"));
        addTest(new org.apache.axiom.ts.om.element.TestSerialization(metaFactory, "U", "Q",
                "<person><p:name xmlns:p=\"urn:ns\">John</p:name><p:age xmlns:p=\"urn:ns\">34</p:age><p:weight xmlns:p=\"urn:ns\">50</p:weight></person>"));
        addTest(new org.apache.axiom.ts.om.element.TestSerialization(metaFactory, "U", "D",
                "<person><name xmlns=\"urn:ns\">John</name><age xmlns=\"urn:ns\">34</age><weight xmlns=\"urn:ns\">50</weight></person>"));
        addTest(new org.apache.axiom.ts.om.element.TestSerializationWithTwoNonBuiltOMElements(metaFactory));
        addTest(new org.apache.axiom.ts.om.element.TestSerializeAndConsumeWithIncompleteDescendant(metaFactory));
        for (int i=0; i<conformanceFiles.length; i++) {
            addTest(new org.apache.axiom.ts.om.element.TestSerializeToOutputStream(metaFactory, conformanceFiles[i], true));
            addTest(new org.apache.axiom.ts.om.element.TestSerializeToOutputStream(metaFactory, conformanceFiles[i], false));
        }
        addTest(new org.apache.axiom.ts.om.element.TestSetText(metaFactory));
        addTest(new org.apache.axiom.ts.om.element.TestSetTextQName(metaFactory));
        for (int i=0; i<OMElementCreator.INSTANCES.length; i++) {
            addTest(new org.apache.axiom.ts.om.factory.TestCreateOMElement(metaFactory, OMElementCreator.INSTANCES[i]));
        }
        addTest(new org.apache.axiom.ts.om.factory.TestCreateOMElementFromQNameWithDefaultNamespace(metaFactory));
        addTest(new org.apache.axiom.ts.om.factory.TestCreateOMElementFromQNameWithNonDefaultNamespace(metaFactory));
        addTest(new org.apache.axiom.ts.om.factory.TestCreateOMElementFromQNameWithoutNamespace(metaFactory));
        addTest(new org.apache.axiom.ts.om.factory.TestCreateOMNamespace(metaFactory));
        addTest(new org.apache.axiom.ts.om.factory.TestCreateOMNamespaceWithNullURI(metaFactory));
        addTest(new org.apache.axiom.ts.om.factory.TestCreateOMText(metaFactory));
        addTest(new org.apache.axiom.ts.om.factory.TestCreateOMTextFromDataHandlerProvider(metaFactory));
        addTest(new org.apache.axiom.ts.om.node.TestDetach(metaFactory, true));
        addTest(new org.apache.axiom.ts.om.node.TestDetach(metaFactory, false));
        addTest(new org.apache.axiom.ts.om.node.TestInsertSiblingAfter(metaFactory));
        addTest(new org.apache.axiom.ts.om.node.TestInsertSiblingAfterLastChild(metaFactory));
        addTest(new org.apache.axiom.ts.om.node.TestInsertSiblingAfterOnChild(metaFactory));
        addTest(new org.apache.axiom.ts.om.node.TestInsertSiblingAfterOnOrphan(metaFactory));
        addTest(new org.apache.axiom.ts.om.node.TestInsertSiblingAfterOnSelf(metaFactory));
        addTest(new org.apache.axiom.ts.om.node.TestInsertSiblingBefore(metaFactory));
        addTest(new org.apache.axiom.ts.om.node.TestInsertSiblingBeforeOnChild(metaFactory));
        addTest(new org.apache.axiom.ts.om.node.TestInsertSiblingBeforeOnOrphan(metaFactory));
        addTest(new org.apache.axiom.ts.om.node.TestInsertSiblingBeforeOnSelf(metaFactory));
        addTest(new org.apache.axiom.ts.om.text.TestBase64Streaming(metaFactory));
    }
}
