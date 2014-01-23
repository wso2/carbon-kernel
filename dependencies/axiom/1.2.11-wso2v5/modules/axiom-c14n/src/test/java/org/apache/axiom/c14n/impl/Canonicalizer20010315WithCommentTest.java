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
public class Canonicalizer20010315WithCommentTest
        extends AbstractCanonicalizerTest {

    public Canonicalizer20010315WithCommentTest(String name){
        super(name);
    }

    public static Test suite(){
        return new TestSuite(Canonicalizer20010315WithCommentTest.class);
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    private byte[] bytes = null;

    private String sample2Result = "<doc>\n" +
            "   <clean>   </clean>\n" +
            "   <dirty>   A   B   </dirty>\n" +
            "   <mixed>\n" +
            "      A\n" +
            "      <clean>   </clean>\n" +
            "      B\n" +
            "      <dirty>   A   B   </dirty>\n" +
            "      C\n" +
            "   </mixed>\n" +
            "</doc>";

    private String sample3Result = "<doc>\n" +
            "   <e1></e1>\n" +
            "   <e2></e2>\n" +
            "   <e3 id=\"elem3\" name=\"elem3\"></e3>\n" +
            "   <e4 id=\"elem4\" name=\"elem4\"></e4>\n" +
            "   <e5 xmlns=\"http://example.org\" xmlns:a=\"http://www.w3.org\" xmlns:b=\"http://www.ietf.org\" attr=\"I'm\" attr2=\"all\" b:attr=\"sorted\" a:attr=\"out\"></e5>\n" +
            "   <e6 xmlns:a=\"http://www.w3.org\">\n" +
            "      <e7 xmlns=\"http://www.ietf.org\">\n" +
            "         <e8 xmlns=\"\">\n" +
            "            <e9 xmlns:a=\"http://www.ietf.org\" attr=\"default\"></e9>\n" +
            "         </e8>\n" +
            "      </e7>\n" +
            "   </e6>\n" +
            "</doc>";

    private String sample4Result = "<doc>\n" +
            "   <text>First line&#xD;\n" +
            "Second line</text>\n" +
            "   <value>2</value>\n" +
            "   <compute>value&gt;\"0\" &amp;&amp; value&lt;\"10\" ?\"valid\":\"error\"</compute>\n" +
            "   <compute expr=\"value>&quot;0&quot; &amp;&amp; value&lt;&quot;10&quot; ?&quot;valid&quot;:&quot;error&quot;\">valid</compute>\n" +
            "   <norm attr=\" '    &#xD;&#xA;&#x9;   ' \"></norm>\n" +
            "   <normNames attr=\"A &#xD;&#xA;&#x9; B\"></normNames>\n" +
            "   <normId id=\"' &#xD;&#xA;&#x9; '\"></normId>\n" +
            "</doc>";

    public void setUp() throws Exception {
        // parse data of sample2.xml
        dp = new DataParser("/sample2.xml");
        // get canonicalizer with comment
        c14n = Canonicalizer.getInstance(
                "http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments");
    }

    public void testSample2() throws Exception{
        // get the canonicalized byte[]
        bytes = c14n.canonicalize(dp.getBytes());
        // check against sample2Result
        assertEquals(sample2Result, new String(bytes));
    }

    public void testSample3() throws Exception{
        // parse data of sample3.xml
        dp.initWithNewFile("/sample3.xml");
        // get the canonicalized byte[]
        bytes = c14n.canonicalize(dp.getBytes());
        // check against sample3Result
        assertEquals(sample3Result, new String(bytes));
    }

    public void testSample4() throws Exception {
        // parse data of sample3.xml
        dp.initWithNewFile("/sample4.xml");
        // get the canonicalized byte[]
        bytes = c14n.canonicalize(dp.getBytes());
        // check against sample3Result
        assertEquals(sample4Result, new String(bytes));
    }
}
