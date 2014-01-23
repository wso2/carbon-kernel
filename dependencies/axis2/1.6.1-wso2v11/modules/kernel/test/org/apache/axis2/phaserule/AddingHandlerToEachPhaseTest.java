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

import org.apache.axis2.AbstractTestCase;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.DispatchPhase;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.engine.Phase;
import org.apache.axis2.phaseresolver.PhaseHolder;

import java.util.ArrayList;
import java.util.List;

public class AddingHandlerToEachPhaseTest extends AbstractTestCase {
    AxisConfiguration axisConfig;

    public AddingHandlerToEachPhaseTest(String testName) {
        super(testName);
    }

    public void testPhaseRules() throws Exception {
        //TODO fix me
        axisConfig = new AxisConfiguration();
        List inPhase = axisConfig.getInFlowPhases();
        Phase transportIN = new Phase("TransportIn");
        Phase preDispatch = new Phase("PreDispatch");
        DispatchPhase dispatchPhase = new DispatchPhase();
//
        dispatchPhase.setName("Dispatch");
        inPhase.add(transportIN);
        inPhase.add(preDispatch);
        inPhase.add(dispatchPhase);

        HandlerDescription hm = new HandlerDescription();
        hm.setClassName("org.apache.axis2.handlers.AbstractHandler");
        Handler h1 = new PhaseRuleHandler();
        hm.setHandler(h1);
        hm.getRules().setPhaseName("*");

        PhaseHolder ph = new PhaseHolder(inPhase);
        ph.addHandler(hm);
        boolean found;
        for (int i = 0; i < inPhase.size(); i++) {
            found = false;
            Phase phase = (Phase) inPhase.get(i);
            List handlers = phase.getHandlers();
            for (int j = 0; j < handlers.size(); j++) {
                Handler handler = (Handler) handlers.get(j);
                if (h1.equals(handler)) {
                    found = true;
                }
            }
            if (!found) {
                fail("Some thing has gone wrong hnadler does not exit in the phase :"
                        + phase.getPhaseName());
            }
        }
    }

    public void testPhaseRulesWithPhaseFirst() throws Exception {
        super.setUp();
        //TODO fix me
        axisConfig = new AxisConfiguration();
        List inPhase = axisConfig.getInFlowPhases();
        Phase transportIN = new Phase("TransportIn");
        Phase preDispatch = new Phase("PreDispatch");
        DispatchPhase dispatchPhase = new DispatchPhase();
//
        dispatchPhase.setName("Dispatch");
        inPhase.add(transportIN);
        inPhase.add(preDispatch);
        inPhase.add(dispatchPhase);

        HandlerDescription hm = new HandlerDescription();
        hm.setClassName("org.apache.axis2.handlers.AbstractHandler");
        Handler h1 = new PhaseRuleHandler();
        hm.setHandler(h1);
        hm.getRules().setPhaseName("*");
        hm.getRules().setPhaseFirst(true);

        PhaseHolder ph = new PhaseHolder(inPhase);
        ph.addHandler(hm);
        for (int i = 0; i < inPhase.size(); i++) {
            Phase phase = (Phase) inPhase.get(i);
            List handlers = phase.getHandlers();
            Handler handler = (Handler) handlers.get(0);
            if (!h1.equals(handler)) {
                fail("Some thing has gone wrong hnadler does not exit as phase " +
                        "first handler the phase :"
                        + phase.getPhaseName());
            }
        }
    }

    public void testPhaseRulesWithAfter() throws Exception {
        //TODO fix me
        axisConfig = new AxisConfiguration();
        List inPhase = axisConfig.getInFlowPhases();
        Phase transportIN = new Phase("TransportIn");
        Phase preDispatch = new Phase("PreDispatch");
        DispatchPhase dispatchPhase = new DispatchPhase();
//
        dispatchPhase.setName("Dispatch");
        inPhase.add(transportIN);
        inPhase.add(preDispatch);
        inPhase.add(dispatchPhase);

        HandlerDescription hm = new HandlerDescription();
        hm.setClassName("org.apache.axis2.handlers.AbstractHandler");
        Handler h1 = new PhaseRuleHandler();
        hm.setHandler(h1);
        hm.getRules().setPhaseName("*");
        hm.getRules().setPhaseFirst(true);

        PhaseHolder ph = new PhaseHolder(inPhase);
        ph.addHandler(hm);
        for (int i = 0; i < inPhase.size(); i++) {
            Phase phase = (Phase) inPhase.get(i);
            List handlers = phase.getHandlers();
            Handler handler = (Handler) handlers.get(0);
            assertNull(handler.getHandlerDesc().getRules().getAfter());
        }
    }
}
