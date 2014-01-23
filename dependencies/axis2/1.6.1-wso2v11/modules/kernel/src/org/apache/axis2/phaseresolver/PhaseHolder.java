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


package org.apache.axis2.phaseresolver;

import org.apache.axis2.deployment.DeploymentErrorMsgs;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.PhaseRule;
import org.apache.axis2.engine.Phase;
import org.apache.axis2.i18n.Messages;

import java.util.List;

/**
 * This class hold all the phases found in the services.xml and server.xml
 */
public class PhaseHolder {
    private List phaseList;

    public PhaseHolder() {
    }

    public PhaseHolder(List phases) {
        this.phaseList = phases;
    }

    /**
     * If the phase name is equal to "*" that implies , the handler should be
     * added to each and every phase in the system for a given flow  , and at that
     * point if the phase rule contains any befores or afters then they will be
     * ignored. Phase first and phase last are supported , but make sure you dont
     * break any of the phase rules.
     * <p/>
     * If the phase name is not above then the hadler will be added to the phase
     * specified by  the phase rule , and no rules will be ignored.
     *
     * @param handlerDesc HandlerDescription to deploy
     * @throws PhaseException if there's a problem
     */
    public void addHandler(HandlerDescription handlerDesc) throws PhaseException {
        PhaseRule rule = handlerDesc.getRules();

        // Make sure this rule makes sense (throws PhaseException if not)
        rule.validate();

        String phaseName = rule.getPhaseName();
        if (Phase.ALL_PHASES.equals(phaseName)) {
            handlerDesc.getRules().setBefore("");
            handlerDesc.getRules().setAfter("");
            for (int i = 0; i < phaseList.size(); i++) {
                Phase phase = (Phase) phaseList.get(i);
                phase.addHandler(handlerDesc);
            }
        } else {
            if (phaseExists(phaseName)) {
                getPhase(phaseName).addHandler(handlerDesc);
            } else {
                throw new PhaseException(Messages.getMessage(DeploymentErrorMsgs.INVALID_PHASE,
                                                             phaseName, handlerDesc.getName()));
            }
        }
    }

    /**
     * this method is used to get the actual phase object given in the phase array list
     *
     * @param phaseName the name of the desired Phase
     * @return the matching Phase, or null
     */
    private Phase getPhase(String phaseName) {
        for (int i = 0; i < phaseList.size(); i++) {
            Phase phase = (Phase) phaseList.get(i);

            if (phase.getPhaseName().equals(phaseName)) {
                return phase;
            }
        }

        return null;
    }

    /**
     * Check if a named Phase exists in this holder.
     *
     * @param phaseName name to check
     * @return true if a Phase matching the name was found, false otherwise
     */
    private boolean phaseExists(String phaseName) {
        for (int i = 0; i < phaseList.size(); i++) {
            Phase phase = (Phase) phaseList.get(i);

            if (phase.getPhaseName().equals(phaseName)) {
                return true;
            }
        }

        return false;
    }
}
