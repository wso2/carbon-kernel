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

package org.apache.axiom.om.impl.dom;

import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.Assert;

import org.apache.axiom.om.impl.dom.jaxp.DOOMDocumentBuilderFactory;
import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;

/**
 * Utility to execute DOM tests.
 * It executes test cases twice: once against a standard DOM implementation
 * (Xerces) and once against DOOM. This allows us to cross-check the validity
 * of these tests, i.e. to check whether we are testing the right thing.
 */
public class DOMTestUtil {
    public interface Test {
        void execute(DocumentBuilderFactory dbf) throws Exception;
    }
    
    private DOMTestUtil() {}
    
    public static void execute(Test test) throws Exception {
        try {
            DocumentBuilderFactory dbf = new DocumentBuilderFactoryImpl();
            dbf.setNamespaceAware(true);
            test.execute(dbf);
        } catch (Throwable ex) {
            Assert.fail("Invalid test case; execution failed with standard DOM implementation: "
                    + ex.getMessage());
        }
        test.execute(new DOOMDocumentBuilderFactory());
    }
}
