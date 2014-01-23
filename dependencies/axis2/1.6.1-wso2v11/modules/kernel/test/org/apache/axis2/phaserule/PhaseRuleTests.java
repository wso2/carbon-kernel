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

package org.apache.axis2.phaserule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.PhaseRule;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.DispatchPhase;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.engine.Phase;
import org.apache.axis2.phaseresolver.PhaseException;
import org.apache.axis2.phaseresolver.PhaseHolder;

/**
 * Test various PhaseRule configurations
 */
public class PhaseRuleTests extends TestCase {

    // Some shared Handlers and HandlerDescriptions
    // TODO : Factor out shared data for these tests

    public void testBefore() throws AxisFault {
        ArrayList phases = new ArrayList();
        Phase p1 = new Phase("PhaseA");
        phases.add(p1);
        Phase p2 = new Phase("PhaseB");
        phases.add(p2);

        MessageContext msg =
                new ConfigurationContext(new AxisConfiguration()).createMessageContext();

        PhaseHolder ph = new PhaseHolder(phases);
        HandlerDescription hm = new HandlerDescription();
        hm.setClassName("org.apache.axis2.phaserule.PhaseRuleHandler");
        Handler h1 = new PhaseRuleHandler();
        h1.init(hm);
        ((PhaseRuleHandler) h1).setName("First");
        hm.setHandler(h1);
        hm.setName("H1");
        PhaseRule rule = new PhaseRule();
        rule.setPhaseName("PhaseA");
        hm.setRules(rule);
        ph.addHandler(hm);

        HandlerDescription hm1 = new HandlerDescription();
        hm1.setClassName("org.apache.axis2.phaserule.PhaseRuleHandler");
        Handler h2 = new PhaseRuleHandler();
        ((PhaseRuleHandler) h2).setName("Second");
        h2.init(hm1);
        hm1.setHandler(h2);
        hm1.setName("H2");
        PhaseRule rule1 = new PhaseRule();
        rule1.setPhaseName("PhaseA");
        rule1.setBefore("H1");
        hm1.setRules(rule1);
        ph.addHandler(hm1);

        List handlers = p1.getHandlers();
        Handler handler = (Handler) handlers.get(0);
        if (handler != h2) {
            fail("Computed Hnadler order is wrong ");
        }
        handler = (Handler) handlers.get(1);
        if (handler != h1) {
            fail("Computed Hnadler order is wrong ");
        }
        p1.invoke(msg);
    }

    public void testBeforewithNoFirst() throws AxisFault {
        ArrayList phases = new ArrayList();
        Phase p1 = new Phase("PhaseA");
        phases.add(p1);
        Phase p2 = new Phase("PhaseB");
        phases.add(p2);

        MessageContext msg =
                new ConfigurationContext(new AxisConfiguration()).createMessageContext();
        PhaseHolder ph = new PhaseHolder(phases);


        HandlerDescription hm1 = new HandlerDescription();
        hm1.setClassName("org.apache.axis2.phaserule.PhaseRuleHandler");
        Handler h2 = new PhaseRuleHandler();
        ((PhaseRuleHandler) h2).setName("Second");
        h2.init(hm1);
        hm1.setHandler(h2);
        hm1.setName("H2");
        PhaseRule rule1 = new PhaseRule();
        rule1.setPhaseName("PhaseA");
        rule1.setBefore("H1");
        hm1.setRules(rule1);
        ph.addHandler(hm1);

        HandlerDescription hm = new HandlerDescription();
        hm.setClassName("org.apache.axis2.phaserule.PhaseRuleHandler");
        Handler h1 = new PhaseRuleHandler();
        h1.init(hm);
        ((PhaseRuleHandler) h1).setName("First");
        hm.setHandler(h1);
        hm.setName("H1");
        PhaseRule rule = new PhaseRule();
        rule.setPhaseName("PhaseA");
        hm.setRules(rule);
        ph.addHandler(hm);

        List handlers = p1.getHandlers();
        Handler handler = (Handler) handlers.get(0);
        if (handler != h2) {
            fail("Computed Handler order is wrong ");
        }
        handler = (Handler) handlers.get(1);
        if (handler != h1) {
            fail("Computed Handler order is wrong ");
        }
        p1.invoke(msg);
    }

    public void testBeforeAfter() throws Exception {
        ArrayList phases = new ArrayList();
        Phase p1 = new Phase("PhaseA");
        phases.add(p1);
        Phase p2 = new Phase("PhaseB");
        phases.add(p2);

        PhaseHolder ph = new PhaseHolder(phases);
        HandlerDescription hm = new HandlerDescription();
        hm.setClassName("org.apache.axis2.phaserule.PhaseRuleHandler");
        PhaseRuleHandler h1 = new PhaseRuleHandler();
        h1.init(hm);
        h1.setName("First");
        hm.setHandler(h1);
        hm.setName("H1");
        PhaseRule rule = new PhaseRule();
        rule.setPhaseName("PhaseA");
        hm.setRules(rule);
        ph.addHandler(hm);

        HandlerDescription hm1 = new HandlerDescription();
        hm1.setClassName("org.apache.axis2.phaserule.PhaseRuleHandler");
        Handler h2 = new PhaseRuleHandler();
        ((PhaseRuleHandler) h2).setName("Forth");
        h2.init(hm1);
        hm1.setHandler(h2);
        hm1.setName("H2");
        PhaseRule rule1 = new PhaseRule();
        rule1.setPhaseName("PhaseA");
        hm1.setRules(rule1);
        ph.addHandler(hm1);


        HandlerDescription hm3 = new HandlerDescription();
        hm3.setClassName("org.apache.axis2.phaserule.PhaseRuleHandler");
        Handler h3 = new PhaseRuleHandler();
        ((PhaseRuleHandler) h3).setName("Second");
        h3.init(hm3);
        hm3.setHandler(h3);
        hm3.setName("H3");
        PhaseRule rule3 = new PhaseRule();
        rule3.setPhaseName("PhaseA");
        rule3.setAfter("H1");
        hm3.setRules(rule3);
        ph.addHandler(hm3);

        HandlerDescription hm4 = new HandlerDescription();
        hm4.setClassName("org.apache.axis2.phaserule.PhaseRuleHandler");
        Handler h4 = new PhaseRuleHandler();
        ((PhaseRuleHandler) h4).setName("Third");
        h4.init(hm4);
        hm4.setHandler(h4);
        hm4.setName("H4");
        PhaseRule rule4 = new PhaseRule();
        rule4.setPhaseName("PhaseA");
        rule4.setAfter("H1");
        rule4.setBefore("H2");
        hm4.setRules(rule4);
        ph.addHandler(hm4);

        List handlers = p1.getHandlers();
        boolean foundH1 = false;
        boolean foundH4 = false;

        for (Iterator iterator = handlers.iterator(); iterator.hasNext();) {
            Handler handler = (Handler) iterator.next();
            if (h3 == handler) {
                if (!foundH1)
                    fail("H3 found before H1");
            }
            if (h1 == handler)
                foundH1 = true;
            if (h2 == handler) {
                if (!foundH4) {
                    fail("H2 found before H4");
                }
            }
            if (h4 == handler) {
                if (!foundH1) {
                    fail("H4 found before H1");
                }
                foundH4 = true;
            }
        }
    }

    public void testPhaseFirst() throws AxisFault {
        ArrayList phases = new ArrayList();
        Phase p1 = new Phase("PhaseA");
        phases.add(p1);
        Phase p2 = new Phase("PhaseB");
        phases.add(p2);
        PhaseHolder ph = new PhaseHolder(phases);
        HandlerDescription hm = new HandlerDescription();
        hm.setClassName("org.apache.axis2.phaserule.PhaseRuleHandler");
        Handler h1 = new PhaseRuleHandler();
        h1.init(hm);
        ((PhaseRuleHandler) h1).setName("PhaseFirstHandler");
        hm.setHandler(h1);
        hm.setName("H1");
        PhaseRule rule = new PhaseRule();
        rule.setPhaseName("PhaseA");
        rule.setPhaseFirst(true);
        hm.setRules(rule);
        ph.addHandler(hm);

        HandlerDescription hm1 = new HandlerDescription();
        hm1.setClassName("org.apache.axis2.phaserule.PhaseRuleHandler");
        Handler h2 = new PhaseRuleHandler();
        ((PhaseRuleHandler) h2).setName("Second Handler");
        h2.init(hm1);
        hm1.setHandler(h2);
        hm1.setName("H2");
        PhaseRule rule1 = new PhaseRule();
        rule1.setPhaseName("PhaseA");
        rule1.setBefore("H1");
        hm1.setRules(rule1);
        try {
            ph.addHandler(hm1);
        } catch (PhaseException e) {
            return;
        }
        fail("Succeeded in deploying after PhaseFirst handler!");
    }

    public void testPhaseLast() throws AxisFault {
        ArrayList phases = new ArrayList();
        Phase p1 = new Phase("PhaseA");
        phases.add(p1);
        Phase p2 = new Phase("PhaseB");
        phases.add(p2);

        PhaseHolder ph = new PhaseHolder(phases);
        HandlerDescription hm = new HandlerDescription();
        hm.setClassName("org.apache.axis2.phaserule.PhaseRuleHandler");
        Handler h1 = new PhaseRuleHandler();
        h1.init(hm);
        ((PhaseRuleHandler) h1).setName("PhaseLast");
        hm.setHandler(h1);
        hm.setName("H1");
        PhaseRule rule = new PhaseRule();
        rule.setPhaseName("PhaseA");
        rule.setPhaseLast(true);
        hm.setRules(rule);
        ph.addHandler(hm);

        HandlerDescription hm1 = new HandlerDescription();
        hm1.setClassName("org.apache.axis2.phaserule.PhaseRuleHandler");
        Handler h2 = new PhaseRuleHandler();
        ((PhaseRuleHandler) h2).setName("Second Handler");
        h2.init(hm1);
        hm1.setHandler(h2);
        hm1.setName("H2");
        PhaseRule rule1 = new PhaseRule();
        rule1.setPhaseName("PhaseA");
        rule1.setAfter("H1");
        hm1.setRules(rule1);
        try {
            ph.addHandler(hm1);
        } catch (PhaseException e) {
            return;
        }
        fail("Succeeded in deploying after PhaseLast handler!");
    }

    public void testPhaseRules() throws Exception {
        // TODO : What is this testing exactly?
    	List inPhase = new AxisConfiguration().getInFlowPhases();
        Phase transportIN = new Phase("TransportIn");
        Phase preDispatch = new Phase("PreDispatch");
        DispatchPhase dispatchPhase = new DispatchPhase();
        dispatchPhase.setName("Dispatch");

        inPhase.add(transportIN);
        inPhase.add(preDispatch);
        inPhase.add(dispatchPhase);

        PhaseHolder ph = new PhaseHolder(inPhase);

        HandlerDescription hm = new HandlerDescription();
        hm.setClassName("org.apache.axis2.phaserule.PhaseRuleHandler");
        Handler h1 = new PhaseRuleHandler();
        h1.init(hm);
        hm.setHandler(h1);
        hm.setName("H1");
        PhaseRule rule = new PhaseRule();
        rule.setPhaseName("PreDispatch");
        rule.setPhaseFirst(true);
        hm.setRules(rule);
        ph.addHandler(hm);

        HandlerDescription hm1 = new HandlerDescription();
        hm1.setClassName("org.apache.axis2.phaserule.PhaseRuleHandler");
        Handler h2 = new PhaseRuleHandler();
        h2.init(hm1);
        hm1.setHandler(h2);
        hm1.setName("H2");
        PhaseRule rule1 = new PhaseRule();
        rule1.setPhaseName("PreDispatch");
        rule1.setAfter("H1");
        hm1.setRules(rule1);
        ph.addHandler(hm1);

        HandlerDescription hm2 = new HandlerDescription();
        hm2.setClassName("org.apache.axis2.phaserule.PhaseRuleHandler");
        Handler h3 = new PhaseRuleHandler();
        h3.init(hm2);
        hm2.setHandler(h3);
        hm2.setName("H3");
        PhaseRule rule2 = new PhaseRule();
        rule2.setPhaseName("PreDispatch");
        rule2.setAfter("H1");
        rule2.setBefore("H2");
        hm2.setRules(rule2);
        ph.addHandler(hm2);

        HandlerDescription hm3 = new HandlerDescription();
        hm3.setClassName("org.apache.axis2.phaserule.PhaseRuleHandler");
        Handler h4 = new PhaseRuleHandler();
        h4.init(hm3);
        hm3.setHandler(h4);
        hm3.setName("H4");
        PhaseRule rule3 = new PhaseRule();
        rule3.setPhaseName("Dispatch");
        hm3.setRules(rule3);
        ph.addHandler(hm3);
    }

    public void testSingleHandler() throws Exception {
        ArrayList phases = new ArrayList();
        Phase p1 = new Phase("PhaseA");
        phases.add(p1);
        Phase p2 = new Phase("PhaseB");
        phases.add(p2);

        PhaseHolder ph = new PhaseHolder(phases);
        HandlerDescription hm = new HandlerDescription();
        hm.setClassName("org.apache.axis2.phaserule.PhaseRuleHandler");
        Handler h1 = new PhaseRuleHandler();
        h1.init(hm);
        ((PhaseRuleHandler) h1).setName("PhaseFirstHandler");
        hm.setHandler(h1);
        hm.setName("H1");
        PhaseRule rule = new PhaseRule();
        rule.setPhaseName("PhaseA");
        rule.setPhaseFirst(true);
        rule.setPhaseLast(true);
        hm.setRules(rule);
        ph.addHandler(hm);

        HandlerDescription hm1 = new HandlerDescription();
        hm1.setClassName("org.apache.axis2.phaserule.PhaseRuleHandler");
        Handler h2 = new PhaseRuleHandler();
        ((PhaseRuleHandler) h2).setName("Second Handler");
        h2.init(hm1);
        hm1.setHandler(h2);
        hm1.setName("H2");
        PhaseRule rule1 = new PhaseRule();
        rule1.setPhaseName("PhaseA");
        rule1.setAfter("H1");
        hm1.setRules(rule1);
        try {
            ph.addHandler(hm1);
        } catch (PhaseException e) {
            // Caught expected Exception
            return;
        }
        fail("This should fail with : can only have one handler, since there is a " +
                "handler with both phaseFirst and PhaseLast true ");
    }

    public void testInvalidPhaseFirst() {
        ArrayList phases = new ArrayList();
        Phase p1 = new Phase("PhaseA");
        phases.add(p1);
        Phase p2 = new Phase("PhaseB");
        phases.add(p2);

        PhaseHolder ph = new PhaseHolder(phases);
        HandlerDescription hm = new HandlerDescription();
        hm.setClassName("org.apache.axis2.phaserule.PhaseRuleHandler");
        Handler h1 = new PhaseRuleHandler();
        h1.init(hm);
        ((PhaseRuleHandler) h1).setName("PhaseFirstHandler");
        hm.setHandler(h1);
        hm.setName("H1");
        PhaseRule rule = new PhaseRule();
        rule.setPhaseName("PhaseA");
        rule.setPhaseFirst(true);
        rule.setBefore("H2");
        hm.setRules(rule);
        try {
            // This should fail
            ph.addHandler(hm);
            fail("Incorrectly added Handler with both PhaseFirst and before name");
        } catch (PhaseException e) {
            // Perfect, caught the expected Exception
        }
    }

    public void testInvalidPhaseFirst1() {
        try {
            ArrayList phases = new ArrayList();
            Phase p1 = new Phase("PhaseA");
            phases.add(p1);
            Phase p2 = new Phase("PhaseB");
            phases.add(p2);

            PhaseHolder ph = new PhaseHolder(phases);
            HandlerDescription hm = new HandlerDescription();
            hm.setClassName("org.apache.axis2.phaserule.PhaseRuleHandler");
            Handler h1 = new PhaseRuleHandler();
            h1.init(hm);
            ((PhaseRuleHandler) h1).setName("PhaseFirstHandler");
            hm.setHandler(h1);
            hm.setName("H1");
            PhaseRule rule = new PhaseRule();
            rule.setPhaseName("PhaseA");
            rule.setPhaseFirst(true);
            hm.setRules(rule);
            ph.addHandler(hm);

            HandlerDescription hm1 = new HandlerDescription();
            hm1.setClassName("org.apache.axis2.phaserule.PhaseRuleHandler");
            Handler h2 = new PhaseRuleHandler();
            ((PhaseRuleHandler) h2).setName("Second Handler");
            h2.init(hm1);
            hm1.setHandler(h2);
            hm1.setName("H2");
            PhaseRule rule1 = new PhaseRule();
            rule1.setPhaseName("PhaseA");
            rule1.setPhaseFirst(true);
            hm1.setRules(rule1);
            ph.addHandler(hm1);
            fail("This should be faild with PhaseFirst already has been set, cannot have two " +
                    "phaseFirst Handler for same phase ");
        } catch (AxisFault axisFault) {
        }
    }

    public void testPhaseLastErrors() throws Exception {
        ArrayList phases = new ArrayList();
        Phase p1 = new Phase("PhaseA");
        phases.add(p1);
        Phase p2 = new Phase("PhaseB");
        phases.add(p2);

        PhaseHolder ph = new PhaseHolder(phases);
        HandlerDescription hm = new HandlerDescription();
        hm.setClassName("org.apache.axis2.phaserule.PhaseRuleHandler");
        Handler h1 = new PhaseRuleHandler();
        h1.init(hm);
        ((PhaseRuleHandler) h1).setName("PhaseFirstHandler");
        hm.setHandler(h1);
        hm.setName("H1");
        PhaseRule rule = new PhaseRule();
        rule.setPhaseName("PhaseA");
        rule.setPhaseLast(true);
        rule.setBefore("H2");
        hm.setRules(rule);
        try {
            ph.addHandler(hm);
        } catch (PhaseException e) {
            return;
        }

        fail("Handler with PhaseLast cannot also have before/after set");
    }

    public void testInvalidPhaseLast() {
        try {
            ArrayList phases = new ArrayList();
            Phase p1 = new Phase("PhaseA");
            phases.add(p1);
            Phase p2 = new Phase("PhaseB");
            phases.add(p2);

            PhaseHolder ph = new PhaseHolder(phases);
            HandlerDescription hm = new HandlerDescription();
            hm.setClassName("org.apache.axis2.phaserule.PhaseRuleHandler");
            Handler h1 = new PhaseRuleHandler();
            h1.init(hm);
            ((PhaseRuleHandler) h1).setName("PhaseLast");
            hm.setHandler(h1);
            hm.setName("H1");
            PhaseRule rule = new PhaseRule();
            rule.setPhaseName("PhaseA");
            rule.setPhaseLast(true);
            hm.setRules(rule);
            ph.addHandler(hm);

            HandlerDescription hm1 = new HandlerDescription();
            hm1.setClassName("org.apache.axis2.phaserule.PhaseRuleHandler");
            Handler h2 = new PhaseRuleHandler();
            ((PhaseRuleHandler) h2).setName("Second Handler");
            h2.init(hm1);
            hm1.setHandler(h2);
            hm1.setName("H2");
            PhaseRule rule1 = new PhaseRule();
            rule1.setPhaseName("PhaseA");
            rule1.setPhaseLast(true);
            hm1.setRules(rule1);
            ph.addHandler(hm1);
            fail("This should be faild with Phaselast already has been set, cannot have two " +
                    "phaseLast Handler for same phase ");
        } catch (AxisFault axisFault) {
        }
    }

    /**
     * This test makes sure we can't add a Handler to a Phase when the
     * before and after constraints don't make sense.
     *
     * @throws Exception if there's an error
     */
    public void testBadBeforeAndAfters() throws Exception {
        PhaseRuleHandler h1 = new PhaseRuleHandler("a");
        HandlerDescription hd = new HandlerDescription("a");
        h1.init(hd);

        PhaseRuleHandler h2 = new PhaseRuleHandler("c");
        hd = new HandlerDescription("c");
        h2.init(hd);

        Phase phase = new Phase();
        phase.addHandler(h1);
        phase.addHandler(h2);

        PhaseRule badRule = new PhaseRule();
        badRule.setBefore("a");
        badRule.setAfter("c");

        hd = new HandlerDescription("b");
        PhaseRuleHandler h3 = new PhaseRuleHandler("b");
        h3.init(hd);

        hd.setHandler(h3);
        hd.setRules(badRule);
        try {
            phase.addHandler(hd);
            fail("Bad PhaseRule was accepted!");
        } catch (PhaseException e) {
            // Correct - exception caught
        }
    }
}
