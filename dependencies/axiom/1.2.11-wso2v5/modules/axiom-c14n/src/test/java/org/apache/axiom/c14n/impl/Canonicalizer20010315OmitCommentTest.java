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

package org.apache.axiom.c14n.impl;

import org.apache.axiom.c14n.DataParser;
import org.apache.axiom.c14n.Canonicalizer;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * @author Saliya Ekanayake (esaliya@gmail.com)
 */
public class Canonicalizer20010315OmitCommentTest
        extends AbstractCanonicalizerTest {

    public Canonicalizer20010315OmitCommentTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(Canonicalizer20010315OmitCommentTest.class);
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    private byte[] bytes = null;

    private String sample5Result = "<doc attrExtEnt=\"entExt\">\n" +
            "   Hello, world!\n" +
            "</doc>";

    private String sample6Result = "<doc>©</doc>";

    public void setUp() throws Exception {
        // parse data of sample5.xml
        dp = new DataParser("/sample5.xml");
        // get canonicalizer which omits comments
        c14n = Canonicalizer.getInstance(
                "http://www.w3.org/TR/2001/REC-xml-c14n-20010315");
    }

    public void testSample5() throws Exception {
        // get the canonicalized byte[]
        bytes = c14n.canonicalize(dp.getBytes());
        // check against sample2Result
        assertEquals(sample5Result, new String(bytes));
    }

     public void testSample6() throws Exception {
         // parse data of sample6.xml
        dp.initWithNewFile("/sample6.xml");
        // get the canonicalized byte[]
        bytes = c14n.canonicalize(dp.getBytes());
        // check against sample6Result
        assertEquals(sample6Result, new String(bytes, "UTF-8"));
    }


}
