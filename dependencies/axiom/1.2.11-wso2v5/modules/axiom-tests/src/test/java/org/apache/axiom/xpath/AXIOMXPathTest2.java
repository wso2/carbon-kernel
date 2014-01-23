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

package org.apache.axiom.xpath;

import junit.framework.TestCase;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.apache.axiom.om.xpath.AXIOMXPath;

/**
 * This class contains Axiom specific tests, while AXIOMXPathTest is
 * used for the Jaxen test harness.
 */
public class AXIOMXPathTest2 extends TestCase {
    public void testAddNamespaces() throws Exception {
        OMElement root1 = AXIOMUtil.stringToOM(
                "<ns1:root xmlns:ns1='urn:ns1'><ns1:child xmlns:ns2='urn:ns2'/></root>");
        OMElement root2 = AXIOMUtil.stringToOM(
                "<root xmlns='urn:ns1'><child xmlns='urn:ns2'>text</child></root>");
        AXIOMXPath xpath = new AXIOMXPath("/ns1:root/ns2:child");
        xpath.addNamespaces(root1.getFirstElement());
        assertEquals("text", xpath.stringValueOf(root2.getParent()));
    }

    public void testAddNamespaces2() throws Exception {
        OMElement root1 = AXIOMUtil.stringToOM(
                "<ns:root xmlns:ns='urn:ns1'><ns:child xmlns:ns='urn:ns2'/></root>");
        OMElement root2 = AXIOMUtil.stringToOM(
                "<root xmlns='urn:ns1'><child xmlns='urn:ns2'>text</child></root>");
        AXIOMXPath xpath = new AXIOMXPath("//ns:child");
        xpath.addNamespaces(root1.getFirstElement());
        assertEquals("text", xpath.stringValueOf(root2.getParent()));
    }
}
