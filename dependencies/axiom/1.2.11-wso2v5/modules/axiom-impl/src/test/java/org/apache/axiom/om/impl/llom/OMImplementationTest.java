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
package org.apache.axiom.om.impl.llom;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.axiom.om.impl.llom.factory.OMLinkedListMetaFactory;
import org.apache.axiom.ts.OMTestSuiteBuilder;
import org.apache.axiom.ts.om.element.TestResolveQNameWithDefaultNamespace;
import org.apache.axiom.ts.om.element.TestResolveQNameWithoutNamespace;
import org.apache.axiom.ts.om.node.TestInsertSiblingAfterOnChild;
import org.apache.axiom.ts.om.node.TestInsertSiblingBeforeOnChild;

public class OMImplementationTest extends TestCase {
    public static TestSuite suite() {
        OMTestSuiteBuilder builder = new OMTestSuiteBuilder(new OMLinkedListMetaFactory());
        // TODO: this looks like a bug
        builder.exclude(org.apache.axiom.ts.om.document.TestIsCompleteAfterAddingIncompleteChild.class);
        
        // TODO: resolveQName appears to have issues resolving QNames without prefixes; needs further investigation
        builder.exclude(TestResolveQNameWithDefaultNamespace.class);
        builder.exclude(TestResolveQNameWithoutNamespace.class);
        
        // TODO: Axiom should throw an exception if an attempt is made to create a cyclic parent-child relationship
        builder.exclude(TestInsertSiblingAfterOnChild.class);
        builder.exclude(TestInsertSiblingBeforeOnChild.class);
        
        return builder.build();
    }
}
