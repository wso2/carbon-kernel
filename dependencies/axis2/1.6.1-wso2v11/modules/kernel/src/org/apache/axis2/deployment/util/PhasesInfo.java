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


package org.apache.axis2.deployment.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.deployment.DeploymentErrorMsgs;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.engine.Phase;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.phaseresolver.PhaseException;
import org.apache.axis2.phaseresolver.PhaseMetadata;

public class PhasesInfo {
    private List<Phase> INPhases;
    private List<Phase> IN_FaultPhases;
    private List<Phase> OUTPhases;
    private List<Phase> OUT_FaultPhases;

    public PhasesInfo() {
        INPhases = new ArrayList<Phase>();
        IN_FaultPhases = new ArrayList<Phase>();
        OUTPhases = new ArrayList<Phase>();
        OUT_FaultPhases = new ArrayList<Phase>();
    }

    /**
     * To copy phase information from one to another
     *
     * @param phase
     */
    private Phase copyPhase(Phase phase) throws DeploymentException {
        Phase newPhase = new Phase(phase.getPhaseName());
        Iterator<Handler> handlers = phase.getHandlers().iterator();

        while (handlers.hasNext()) {
            try {
                Handler handlerDescription = (Handler) handlers.next();

                newPhase.addHandler(handlerDescription.getHandlerDesc());
            } catch (PhaseException e) {
                throw new DeploymentException(e);
            }
        }

        return newPhase;
    }

    HandlerDescription makeHandler(OMElement handlerElement) {
        String name = handlerElement.getAttributeValue(new QName("name"));
        QName qname = handlerElement.resolveQName(name);
        HandlerDescription desc = new HandlerDescription(qname.getLocalPart());
        String className = handlerElement.getAttributeValue(new QName("class"));

        desc.setClassName(className);

        return desc;
    }

    public Phase makePhase(OMElement phaseElement) throws PhaseException {
        String phaseName = phaseElement.getAttributeValue(new QName("name"));
        Phase phase = new Phase(phaseName);
        Iterator children = phaseElement.getChildElements();

        while (children.hasNext()) {
            OMElement handlerElement = (OMElement) children.next();
            HandlerDescription handlerDesc = makeHandler(handlerElement);

            phase.addHandler(handlerDesc);
        }

        return phase;
    }

    public List<Phase> getGlobalInflow() throws DeploymentException {
        ArrayList<Phase> globalphase = new ArrayList<Phase>();
        boolean foundDispatchPhase = false;
        for (int i = 0; i < INPhases.size(); i++) {
            Phase phase = (Phase) INPhases.get(i);
            String phaseName = phase.getPhaseName();
            if (!foundDispatchPhase) {
                if (PhaseMetadata.PHASE_DISPATCH.equals(phaseName)) {
                    foundDispatchPhase = true;
                }
                globalphase.add(phase);
            }
        }
        if (!foundDispatchPhase) {
            throw new DeploymentException(
                    Messages.getMessage(DeploymentErrorMsgs.DISPATCH_PHASE_NOT_FOUND));
        }
        return globalphase;
    }

    public List<Phase> getGlobalOutPhaseList() throws DeploymentException {
        /**
         * I have assumed that     PolicyDetermination and  MessageProcessing are global out phase
         */
        ArrayList<Phase> globalPhaseList = new ArrayList<Phase>();

        boolean messageOut = false;
        for (int i = 0; i < OUTPhases.size(); i++) {
            Phase phase = (Phase) OUTPhases.get(i);
            String phaseName = phase.getPhaseName();
            if (!messageOut) {
                if (PhaseMetadata.PHASE_MESSAGE_OUT.equals(phaseName)) {
                    messageOut = true;
                    globalPhaseList.add(copyPhase(phase));
                }
            } else {
                globalPhaseList.add(phase);
            }
        }
        return globalPhaseList;
    }

    public List<Phase> getINPhases() {
        return INPhases;
    }

    public List<Phase> getIN_FaultPhases() {
        return IN_FaultPhases;
    }

    public List<Phase> getOUTPhases() {
        return OUTPhases;
    }

    public List<Phase> getOutFaultPhaseList(){
        return OUT_FaultPhases;
    }

    public List<Phase> getOUT_FaultPhases() throws DeploymentException {
        ArrayList<Phase> globalPhaseList = new ArrayList<Phase>();
        boolean messageOut = false;
        for (int i = 0; i < OUT_FaultPhases.size(); i++) {
            Phase phase = (Phase) OUT_FaultPhases.get(i);
            String phaseName = phase.getPhaseName();
            if (!messageOut) {
                if (PhaseMetadata.PHASE_MESSAGE_OUT.equals(phaseName)) {
                    messageOut = true;
                    globalPhaseList.add(copyPhase(phase));
                }
            } else {
                globalPhaseList.add(copyPhase(phase));
            }
        }
        return globalPhaseList;
    }

    public List<Phase> getOperationInFaultPhases() throws DeploymentException {
        ArrayList<Phase> operationINPhases = new ArrayList<Phase>();
        boolean foundDispathPhase = false;
        for (int i = 0; i < IN_FaultPhases.size(); i++) {
            Phase phase = (Phase) IN_FaultPhases.get(i);
            String phaseName = phase.getPhaseName();
            if (foundDispathPhase) {
                operationINPhases.add(copyPhase(phase));
            }
            if (PhaseMetadata.PHASE_DISPATCH.equals(phaseName)) {
                foundDispathPhase = true;
            }
        }
        return operationINPhases;
    }

    public List<Phase> getGlobalInFaultPhases() throws DeploymentException {
        ArrayList<Phase> globalInfaultphase = new ArrayList<Phase>();
        boolean foundDispatchPhase = false;
        for (int i = 0; i < IN_FaultPhases.size(); i++) {
            Phase phase = (Phase) IN_FaultPhases.get(i);
            String phaseName = phase.getPhaseName();
            if (!foundDispatchPhase) {
                if (PhaseMetadata.PHASE_DISPATCH.equals(phaseName)) {
                    foundDispatchPhase = true;
                }
                globalInfaultphase.add(phase);
            }
        }
        if (!foundDispatchPhase) {
            throw new DeploymentException(
                    Messages.getMessage(DeploymentErrorMsgs.DISPATCH_PHASE_NOT_FOUND));
        }
        return globalInfaultphase;
    }


    public ArrayList<Phase> getOperationInPhases() throws DeploymentException {
        ArrayList<Phase> operationINPhases = new ArrayList<Phase>();
        boolean foundDispathPhase = false;
        for (int i = 0; i < INPhases.size(); i++) {
            Phase phase = (Phase) INPhases.get(i);
            String phaseName = phase.getPhaseName();
            if (foundDispathPhase) {
                operationINPhases.add(copyPhase(phase));
            }
            if (PhaseMetadata.PHASE_DISPATCH.equals(phaseName)) {
                foundDispathPhase = true;
            }
        }
        return operationINPhases;
    }

    public ArrayList<Phase> getOperationOutFaultPhases() throws DeploymentException {
        ArrayList<Phase> operationFaultOutPhases = new ArrayList<Phase>();
        for (int i = 0; i < OUT_FaultPhases.size(); i++) {
            Phase phase = (Phase) OUT_FaultPhases.get(i);
            String phaseName = phase.getPhaseName();
            if (PhaseMetadata.PHASE_MESSAGE_OUT.equals(phaseName)) {
                break;
            }
            operationFaultOutPhases.add(copyPhase(phase));

        }
        return operationFaultOutPhases;
    }

    public ArrayList<Phase> getOperationOutPhases() throws DeploymentException {
        ArrayList<Phase> oprationOUTPhases = new ArrayList<Phase>();

        for (int i = 0; i < OUTPhases.size(); i++) {
            Phase phase = (Phase) OUTPhases.get(i);
            String phaseName = phase.getPhaseName();
            if (PhaseMetadata.PHASE_MESSAGE_OUT.equals(phaseName)) {
                break;
            }
            oprationOUTPhases.add(copyPhase(phase));
        }

        return oprationOUTPhases;
    }

    public void setINPhases(List<Phase> INPhases) {
        this.INPhases = INPhases;
    }

    public void setIN_FaultPhases(List<Phase> IN_FaultPhases) {
        this.IN_FaultPhases = IN_FaultPhases;
    }

    public void setOUTPhases(List<Phase> OUTPhases) {
        this.OUTPhases = OUTPhases;
    }

    public void setOUT_FaultPhases(List<Phase> OUT_FaultPhases) {
        this.OUT_FaultPhases = OUT_FaultPhases;
    }

    public void setOperationPhases(AxisOperation axisOperation) throws AxisFault {
        if (axisOperation != null) {
            try {
                axisOperation.setRemainingPhasesInFlow(getOperationInPhases());
                axisOperation.setPhasesOutFlow(getOperationOutPhases());
                axisOperation.setPhasesInFaultFlow(new ArrayList(getOperationInFaultPhases()));
                axisOperation.setPhasesOutFaultFlow(getOperationOutFaultPhases());
            } catch (DeploymentException e) {
                throw AxisFault.makeFault(e);
            }
        }
    }
}
