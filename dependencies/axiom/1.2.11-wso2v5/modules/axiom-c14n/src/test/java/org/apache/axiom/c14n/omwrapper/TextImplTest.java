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
import org.apache.axiom.c14n.omwrapper.interfaces.Node;
import org.apache.axiom.c14n.DataParser;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * @author Saliya Ekanayake (esaliya@gmail.com)
 */
public class TextImplTest extends TestCase {
    private DataParser dp = null;
    private Node n = null;

    public TextImplTest(String name){
        super(name);
    }

    public static Test suite() {
        return new TestSuite(TextImplTest.class);
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public void setUp() throws Exception{
        dp = new DataParser("/sample1.xml");
        dp.init();
        NodeList nl = dp.docEle.getChildNodes();
        n = null;
        for (int i = 0; i < nl.getLength(); i++) {
            n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals("e6")){
                break;
            }
        }
    }

    public void testGetNodeType(){
        // here n is e6
        assertEquals(Node.TEXT_NODE, n.getNextSibling().getNodeType());
    }

    // see issue mentioned with the testGetNextSibling() method in ElementImplTest
    public void testGetNodeValue() {
        // here n is e6
        assertEquals("\n    <don't><process><this>:)\n", n.getNextSibling().getNodeValue());
    }

}
