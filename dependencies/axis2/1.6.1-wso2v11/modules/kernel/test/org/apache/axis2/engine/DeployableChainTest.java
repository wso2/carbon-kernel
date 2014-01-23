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

package org.apache.axis2.engine;

import junit.framework.TestCase;

import java.util.List;

public class DeployableChainTest extends TestCase {
    static final Object A_TARGET = new Object();
    static final Object B_TARGET = new Object();
    static final Object C_TARGET = new Object();
    static final Object D_TARGET = new Object();
    static final Object E_TARGET = new Object();

    private Deployable a;
    private Deployable b;
    private Deployable c;
    private Deployable d;
    private Deployable e;
    DeployableChain ec;

    protected void setUp() throws Exception {
        a = new Deployable("a", A_TARGET);
        b = new Deployable("b", B_TARGET);
        c = new Deployable("c", C_TARGET);
        d = new Deployable("d", D_TARGET);
        e = new Deployable("e", E_TARGET);
        ec = new DeployableChain();
    }

    public void testDuplicateDeploy() throws Exception {
        a.addSuccessor("b");
        ec.deploy(a);

        // Should be ok to do this again as long as everything matches.
        ec.deploy(a);
    }

    public void testOrdering() throws Exception {
        a.addSuccessor("b");
        ec.deploy(b);
        ec.deploy(a);
        ec.rebuild();
        List chain = ec.getChain();
        assertEquals("Wrong number of items", 2, chain.size());
        assertEquals("Wrong order", A_TARGET, chain.get(0));

        ec.addRelationship("b", "a");
        boolean caught = false;
        try {
            ec.rebuild();
        } catch (Exception e1) {
            // Expected Exception, yay!
            caught = true;
        }
        if (!caught) fail("Didn't catch exception for cyclic dependency");

        ec = new DeployableChain();
        b.addSuccessor("c");
        d.addSuccessor("e");
        e.addPredecessor("b");

        e.setLast(true);

        ec.deploy(b);
        ec.deploy(a);
        ec.deploy(c);
        ec.deploy(d);
        ec.deploy(e);
        ec.rebuild();
        chain = ec.getChain();

        assertEquals("Wrong number of items", 5, chain.size());
        int aIndex = chain.indexOf(A_TARGET);
        int bIndex = chain.indexOf(B_TARGET);
        int cIndex = chain.indexOf(C_TARGET);
        int dIndex = chain.indexOf(D_TARGET);
        int eIndex = chain.indexOf(E_TARGET);
        assertTrue(aIndex < bIndex);
        assertTrue(bIndex < cIndex);
        assertTrue(dIndex < eIndex);
        assertTrue(bIndex < eIndex);
    }
}
