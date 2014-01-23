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

package org.apache.axis2.transport.local;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.Flow;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.Phase;
import org.apache.axis2.transport.TransportSender;

import java.util.ArrayList;

/**
 * This is wrapper class of TransportOutDescription class.
 * In Using Axis2 Local Transport, you can't send SOAP Message successively.
 */

class LocalResponseTransportOutDescription extends TransportOutDescription {

    private TransportSender sender = null;

    private TransportOutDescription tOut = null;

    public LocalResponseTransportOutDescription(
            TransportOutDescription localTransportSenderDescription)
            throws AxisFault {

        super(localTransportSenderDescription.getName());

        this.tOut = localTransportSenderDescription;
    }

    /**
     * Method addParameter.
     * 
     * @param param
     */
    public void addParameter(Parameter param) throws AxisFault {
        tOut.addParameter(param);
    }

    public void removeParameter(Parameter param) throws AxisFault {
        tOut.removeParameter(param);
    }

    public void deserializeParameters(OMElement parameterElement)
            throws AxisFault {
        tOut.deserializeParameters(parameterElement);
    }

    public Flow getFaultFlow() {
        return tOut.getFaultFlow();
    }

    public Phase getFaultPhase() {
        return tOut.getFaultPhase();
    }

    /**
     * @return Returns String.
     */
    public String getName() {
        return tOut.getName();
    }

    public Flow getOutFlow() {
        return tOut.getOutFlow();
    }

    public Phase getOutPhase() {
        return tOut.getOutPhase();
    }

    /**
     * Method getParameter.
     * 
     * @param name
     * @return Returns Parameter.
     */
    public Parameter getParameter(String name) {
        return tOut.getParameter(name);
    }

    public ArrayList getParameters() {
        return tOut.getParameters();
    }

    /**
     * @return Returns TransportSender.
     */
    public TransportSender getSender() {
        return this.sender;
    }

    // to check whether the parameter is locked at any level
    public boolean isParameterLocked(String parameterName) {
        return tOut.isParameterLocked(parameterName);
    }

    public void setFaultFlow(Flow faultFlow) {
        tOut.setFaultFlow(faultFlow);
    }

    public void setFaultPhase(Phase faultPhase) {
        tOut.setFaultPhase(faultPhase);
    }

    /**
     * @param name
     */
    public void setName(String name) {
        tOut.setName(name);
    }

    public void setOutFlow(Flow outFlow) {
        tOut.setOutFlow(outFlow);
    }

    public void setOutPhase(Phase outPhase) {
        tOut.setOutPhase(outPhase);
    }

    /**
     * @param sender
     */
    public void setSender(TransportSender sender) {
        this.sender = sender;
    }
}
