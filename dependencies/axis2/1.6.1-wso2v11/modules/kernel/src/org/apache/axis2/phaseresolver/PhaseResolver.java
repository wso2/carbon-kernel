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

import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.Flow;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Phase;
import org.apache.axis2.wsdl.WSDLConstants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class PhaseResolver
 */
public class PhaseResolver {

    private static final int IN_FLOW = 1;
    private static final int OUT_FAULT_FLOW = 5;

    /**
     * Field axisConfig
     */
    private AxisConfiguration axisConfig;

    /**
     * Field phaseHolder
     */
    private PhaseHolder phaseHolder;

    /**
     * default constructor , to obuild chains for GlobalDescription
     *
     * @param axisconfig
     */
    public PhaseResolver(AxisConfiguration axisconfig) {
        this.axisConfig = axisconfig;
    }

    private void engageModuleToFlow(Flow flow, List handlerChain) throws PhaseException {
        phaseHolder = new PhaseHolder(handlerChain);
        if (flow != null) {
            for (int j = 0; j < flow.getHandlerCount(); j++) {
                HandlerDescription metadata = flow.getHandler(j);
                phaseHolder.addHandler(metadata);
            }
        }
    }

    private void engageModuleToOperation(AxisOperation axisOperation,
                                         AxisModule axisModule,
                                         int flowType) throws PhaseException {
        List phases = new ArrayList();
        Flow flow = null;
        switch (flowType) {
            case PhaseMetadata.IN_FLOW : {
                phases.addAll(axisConfig.getInFlowPhases());
                phases.addAll(axisOperation.getRemainingPhasesInFlow());
                flow = axisModule.getInFlow();
                break;
            }
            case PhaseMetadata.OUT_FLOW : {
                phases.addAll(axisOperation.getPhasesOutFlow());
                phases.addAll(axisConfig.getOutFlowPhases());
                flow = axisModule.getOutFlow();
                break;
            }
            case PhaseMetadata.FAULT_OUT_FLOW : {
                phases.addAll(axisOperation.getPhasesOutFaultFlow());
                phases.addAll(axisConfig.getOutFaultFlowPhases());
                flow = axisModule.getFaultOutFlow();
                break;
            }
            case PhaseMetadata.FAULT_IN_FLOW : {
                phases.addAll(axisOperation.getPhasesInFaultFlow());
                phases.addAll(axisConfig.getInFaultFlowPhases());
                flow = axisModule.getFaultInFlow();
                break;
            }
        }
        engageModuleToFlow(flow, phases);
    }

    public void engageModuleToOperation(AxisOperation axisOperation, AxisModule module)
            throws PhaseException {
        for (int type = IN_FLOW; type < OUT_FAULT_FLOW; type++) {
            engageModuleToOperation(axisOperation, module, type);
        }
    }

    /**
     * To remove handlers from global chians this method can be used , first it take inflow
     * of the module and then take handler one by one and then remove those handlers from
     * global inchain ,
     * the same procedure will be carry out for all the other flows as well.
     *
     * @param module
     */
    public void disengageModuleFromGlobalChains(AxisModule module) {
        //INFLOW
        Flow flow = module.getInFlow();
        if (flow != null) {
            for (int j = 0; j < flow.getHandlerCount(); j++) {
                HandlerDescription handler = flow.getHandler(j);
                removeHandlerfromaPhase(handler, axisConfig.getInFlowPhases());
            }
        }
        //OUTFLOW
        flow = module.getOutFlow();
        if (flow != null) {
            for (int j = 0; j < flow.getHandlerCount(); j++) {
                HandlerDescription handler = flow.getHandler(j);
                removeHandlerfromaPhase(handler, axisConfig.getOutFlowPhases());
            }
        }
        //INFAULTFLOW
        flow = module.getFaultInFlow();
        if (flow != null) {
            for (int j = 0; j < flow.getHandlerCount(); j++) {
                HandlerDescription handler = flow.getHandler(j);
                removeHandlerfromaPhase(handler, axisConfig.getInFaultFlowPhases());
            }
        }
        //OUTFAULTFLOW
        flow = module.getFaultOutFlow();
        if (flow != null) {
            for (int j = 0; j < flow.getHandlerCount(); j++) {
                HandlerDescription handler = flow.getHandler(j);
                removeHandlerfromaPhase(handler, axisConfig.getOutFaultFlowPhases());
            }
        }
    }

    /**
     * To remove handlers from operations chians this method can be used , first it take inflow
     * of the module and then take handler one by one and then remove those handlers from
     * global inchain ,
     * the same procedure will be carry out for all the other flows as well.
     *
     * @param module
     */
    public void disengageModuleFromOperationChain(AxisModule module, AxisOperation operation) {
        //INFLOW
        Flow flow = module.getInFlow();
        if (flow != null) {
            for (int j = 0; j < flow.getHandlerCount(); j++) {
                HandlerDescription handler = flow.getHandler(j);
                removeHandlerfromaPhase(handler, operation.getRemainingPhasesInFlow());
            }
        }
        //OUTFLOW
        flow = module.getOutFlow();
        if (flow != null) {
            for (int j = 0; j < flow.getHandlerCount(); j++) {
                HandlerDescription handler = flow.getHandler(j);
                removeHandlerfromaPhase(handler, operation.getPhasesOutFlow());
            }
        }
        //INFAULTFLOW
        flow = module.getFaultInFlow();
        if (flow != null) {
            for (int j = 0; j < flow.getHandlerCount(); j++) {
                HandlerDescription handler = flow.getHandler(j);
                removeHandlerfromaPhase(handler, operation.getPhasesInFaultFlow());
            }
        }
        //OUTFAULTFLOW
        flow = module.getFaultOutFlow();
        if (flow != null) {
            for (int j = 0; j < flow.getHandlerCount(); j++) {
                HandlerDescription handler = flow.getHandler(j);
                removeHandlerfromaPhase(handler, operation.getPhasesOutFaultFlow());
            }
        }
    }

    /**
     * To remove a single handler from a given list of phases
     *
     * @param handler
     * @param phaseList
     */
    private void removeHandlerfromaPhase(HandlerDescription handler, List<Phase> phaseList) {
        String phaseName = handler.getRules().getPhaseName();
        Iterator<Phase> phaseItr = phaseList.iterator();
        while (phaseItr.hasNext()) {
            Phase phase = (Phase) phaseItr.next();
            if (phase.getPhaseName().equals(phaseName)) {
                phase.removeHandler(handler);
                break;
            }
        }
    }

    public void engageModuleToMessage(AxisMessage axisMessage, AxisModule axisModule)
            throws PhaseException {
        String direction = axisMessage.getDirection();
        AxisOperation axisOperation = axisMessage.getAxisOperation();
        if (WSDLConstants.MESSAGE_LABEL_OUT_VALUE.equalsIgnoreCase(direction)) {
            engageModuleToOperation(axisOperation, axisModule, PhaseMetadata.OUT_FLOW);
        } else if (WSDLConstants.MESSAGE_LABEL_IN_VALUE.equalsIgnoreCase(direction)) {
            engageModuleToOperation(axisOperation, axisModule, PhaseMetadata.IN_FLOW);
        } else if (WSDLConstants.MESSAGE_LABEL_FAULT_VALUE.equals(direction)) {
            //TODO : Need to handle fault correctly
        }
    }
}
