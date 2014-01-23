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


package org.apache.axis2.description;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.Phase;
import org.apache.axis2.phaseresolver.PhaseMetadata;
import org.apache.axis2.transport.TransportSender;

import java.util.ArrayList;

/**
 * Represents a transport deployed in AXis2
 */
public class TransportOutDescription implements ParameterInclude {

    /**
     * Field flowInclude
     */
    private Flow faultFlow;
    private Phase faultPhase;

    /**
     * Field name
     */
    protected String name;

    /**
     * Field outFlow
     */
    private Flow outFlow;
    private Phase outPhase;

    /**
     * Field paramInclude
     */
    protected final ParameterInclude paramInclude;
    protected TransportSender sender;

    /**
     * Constructor AxisTransport.
     *
     * @param name
     */
    public TransportOutDescription(String name) {
        paramInclude = new ParameterIncludeImpl();
        this.name = name;
        outPhase = new Phase(PhaseMetadata.TRANSPORT_PHASE);
        faultPhase = new Phase(PhaseMetadata.TRANSPORT_PHASE);
    }

    /**
     * Method addParameter.
     *
     * @param param
     */
    public void addParameter(Parameter param) throws AxisFault {
        paramInclude.addParameter(param);
    }

    public void removeParameter(Parameter param) throws AxisFault {
        paramInclude.removeParameter(param);
    }

    public void deserializeParameters(OMElement parameterElement) throws AxisFault {
        this.paramInclude.deserializeParameters(parameterElement);
    }

    public Flow getFaultFlow() {
        return faultFlow;
    }

    public Phase getFaultPhase() {
        return faultPhase;
    }

    /**
     * @return Returns QName.
     */
    public String getName() {
        return name;
    }

    public Flow getOutFlow() {
        return outFlow;
    }

    public Phase getOutPhase() {
        return outPhase;
    }

    /**
     * Method getParameter.
     *
     * @param name
     * @return Returns Parameter.
     */
    public Parameter getParameter(String name) {
        return paramInclude.getParameter(name);
    }

    public ArrayList<Parameter> getParameters() {
        return paramInclude.getParameters();
    }

    /**
     * @return Returns TransportSender.
     */
    public TransportSender getSender() {
        return sender;
    }

    // to check whether the parameter is locked at any level
    public boolean isParameterLocked(String parameterName) {
        return paramInclude.isParameterLocked(parameterName);
    }

    public void setFaultFlow(Flow faultFlow) {
        this.faultFlow = faultFlow;
    }

    public void setFaultPhase(Phase faultPhase) {
        this.faultPhase = faultPhase;
    }

    /**
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    public void setOutFlow(Flow outFlow) {
        this.outFlow = outFlow;
    }

    public void setOutPhase(Phase outPhase) {
        this.outPhase = outPhase;
    }

    /**
     * @param sender
     */
    public void setSender(TransportSender sender) {
        this.sender = sender;
    }
}
