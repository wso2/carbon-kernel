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

package org.apache.axiom.c14n.omwrapper;

import org.apache.axiom.c14n.omwrapper.interfaces.NodeList;
import org.apache.axiom.c14n.DataParser;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * @author Saliya Ekanayake (esaliya@gmail.com)
 */
public class NodeListImplTest extends TestCase {
    private DataParser dp = null;
    private NodeList nl = null;

    public NodeListImplTest(String name){
        super(name);
    }

    public static Test suite() {
        return new TestSuite(NodeListImplTest.class);
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public void setUp() throws Exception{
        dp = new DataParser("/sample1.xml");
        dp.init();
        nl = dp.docEle.getChildNodes();
    }

    public void testGetLength() {
        assertEquals(15, nl.getLength());
    }

    public void testItem(){
        assertNotNull("valid index should not return null", nl.item(nl.getLength() - 1));
        assertNull("invalid index should return null", nl.item(-2));
        assertNull("invalid index should return null", nl.item(nl.getLength()));
    }
}
