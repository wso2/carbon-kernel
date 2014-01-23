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

import java.util.List;

import org.apache.axis2.AbstractTestCase;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.PhaseRule;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.DispatchPhase;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.engine.Phase;
import org.apache.axis2.phaseresolver.PhaseHolder;

public class PreDispatchPhaseRuleTest extends AbstractTestCase {

    PreDispatchPhaseRuleTest phaserul;
    AxisConfiguration axisSytem;

    public PreDispatchPhaseRuleTest(String testName) {
        super(testName);
    }

    public void testPhaseRule() throws Exception {
        //TODO Fix me
        phaserul = new PreDispatchPhaseRuleTest("");
        axisSytem = new AxisConfiguration();
        List inPhase = axisSytem.getInFlowPhases();
        Phase transportIN = new Phase("TransportIn");
        Phase preDispatch = new Phase("PreDispatch");
        DispatchPhase dispatchPhase = new DispatchPhase();
//
        dispatchPhase.setName("Dispatch");
        inPhase.add(transportIN);
        inPhase.add(preDispatch);
        inPhase.add(dispatchPhase);
        PhaseHolder ph = new PhaseHolder(inPhase);


        HandlerDescription pre = new HandlerDescription();
        pre.setClassName("org.apache.axis2.handlers.AbstractHandler");
        Handler h1 = new PhaseRuleHandler();
        h1.init(pre);
        pre.setHandler(h1);
        pre.setName("pre-H1");
        PhaseRule pre_rule1 = new PhaseRule();
        pre_rule1.setPhaseName("PreDispatch");
        pre.setRules(pre_rule1);
        ph.addHandler(pre);

        HandlerDescription pre2 = new HandlerDescription();
        pre2.setClassName("org.apache.axis2.handlers.AbstractHandler");
        Handler h2 = new PhaseRuleHandler();
        h2.init(pre2);
        pre2.setHandler(h2);
        pre2.setName("dispatch");
        PhaseRule prerule2 = new PhaseRule();
        prerule2.setPhaseName("Dispatch");
        pre2.setRules(prerule2);
        ph.addHandler(pre2);


        HandlerDescription hm = new HandlerDescription();
        hm.setClassName("org.apache.axis2.handlers.AbstractHandler");
        Handler h3 = new PhaseRuleHandler();
        h3.init(hm);
        hm.setHandler(h3);
        hm.setName("pre-H2");
        PhaseRule rule = new PhaseRule();
        rule.setPhaseName("PreDispatch");
        rule.setPhaseFirst(true);
        hm.setRules(rule);
        ph.addHandler(hm);

        HandlerDescription hm1 = new HandlerDescription();
        hm1.setClassName("org.apache.axis2.handlers.AbstractHandler");
        Handler h4 = new PhaseRuleHandler();
        h4.init(hm1);
        hm1.setHandler(h4);
        hm1.setName("pre-H3");
        PhaseRule rule1 = new PhaseRule();
        rule1.setPhaseName("PreDispatch");
        rule1.setAfter("pre-H2");
        hm1.setRules(rule1);
        ph.addHandler(hm1);

        HandlerDescription hm2 = new HandlerDescription();
        hm2.setClassName("org.apache.axis2.handlers.AbstractHandler");
        Handler h5 = new PhaseRuleHandler();
        h5.init(hm2);
        hm2.setHandler(h5);
        hm2.setName("H3");
        PhaseRule rule2 = new PhaseRule();
        rule2.setPhaseName("PreDispatch");
        rule2.setAfter("pre-H2");
        rule2.setBefore("pre-H3");
        hm2.setRules(rule2);
        ph.addHandler(hm2);

        /*ArrayList oh = ph.getOrderHandler();
        for (int i = 0; i < oh.size(); i++) {
            HandlerDescription metadata = (HandlerDescription) oh.get(i);
            System.out.println("Name:" + metadata.getName().getLocalPart());
        }*/
    }

    public void testPhaseLastAndAfter() throws Exception {

        Phase phase = new Phase();

        //////////////// handler 1 //////////////////////////
        PhaseRuleHandler h1 = new PhaseRuleHandler("a");
        HandlerDescription hd1 = new HandlerDescription("a");
        h1.init(hd1);
        hd1.setHandler(h1);
        phase.addHandler(hd1);
        /////////////////////////////////////////////////////

        //////////////// handler 4 //////////////////////////
        PhaseRule rule4 = new PhaseRule();
        rule4.setPhaseLast(true);

        PhaseRuleHandler h4 = new PhaseRuleHandler("d");
        HandlerDescription hd4 = new HandlerDescription("d");
        h4.init(hd4);

        hd4.setHandler(h4);
        hd4.setRules(rule4);
        phase.addHandler(hd4);
        ////////////////////////////////////////////////////

        //////////////// handler 2 //////////////////////////
        PhaseRule rule2 = new PhaseRule();
        rule2.setAfter("a");

        HandlerDescription hd2 = new HandlerDescription("b");
        PhaseRuleHandler h2 = new PhaseRuleHandler("b");
        h2.init(hd2);

        hd2.setHandler(h2);
        hd2.setRules(rule2);
        phase.addHandler(hd2);
        //////////////////////////////////////////////////////

        //////////////// handler 3 //////////////////////////
        PhaseRule rule3 = new PhaseRule();
        rule3.setAfter("b");

        HandlerDescription hd3 = new HandlerDescription("c");
        PhaseRuleHandler h3 = new PhaseRuleHandler("c");
        h3.init(hd3);

        hd3.setHandler(h3);
        hd3.setRules(rule3);
        try {
            phase.addHandler(hd3);
        } catch (Exception e) {
            fail("Adding handlers with after attribute to the phase behaviour failed");
        }
        //////////////////////////////////////////////////////
    }
}
