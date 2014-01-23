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

import junit.framework.TestCase;
import org.apache.axiom.c14n.DataParser;
import org.apache.axiom.c14n.Canonicalizer;
import org.apache.axiom.c14n.exceptions.AlgorithmAlreadyRegisteredException;

/**
 * @author Saliya Ekanayake (esaliya@gmail.com)
 */
public abstract class AbstractCanonicalizerTest extends TestCase {
    protected DataParser dp = null;
    protected Canonicalizer c14n = null;

    public AbstractCanonicalizerTest(){}

    public AbstractCanonicalizerTest(String name){
        super(name);
    }

       static {
        Canonicalizer.init();
        try {
            Canonicalizer.register("http://www.w3.org/TR/2001/REC-xml-c14n-20010315",
                    "org.apache.axiom.c14n.impl.Canonicalizer20010315OmitComments");

            Canonicalizer.register("http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments",
                    "org.apache.axiom.c14n.impl.Canonicalizer20010315WithComments");


            Canonicalizer.register("http://www.w3.org/2001/10/xml-exc-c14n#",
                    "org.apache.axiom.c14n.impl.Canonicalizer20010315ExclOmitComments");

            Canonicalizer.register("http://www.w3.org/2001/10/xml-exc-c14n#WithComments",
                    "org.apache.axiom.c14n.impl.Canonicalizer20010315ExclWithComments");

        } catch (AlgorithmAlreadyRegisteredException e) {
            fail(e.getMessage());
        }
    }
}
