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

package org.apache.axis2.deployment;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.ParameterInclude;
import org.apache.axis2.description.ParameterIncludeImpl;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEvent;
import org.apache.axis2.engine.AxisObserver;

import java.util.ArrayList;

public class AxisObserverImpl implements AxisObserver {

    ParameterInclude parameterimpl;

    // The initilization code will go here
    public void init(AxisConfiguration axisConfig) {
        parameterimpl = new ParameterIncludeImpl();
    }

    public void serviceUpdate(AxisEvent event, AxisService service) {
    }

    public void moduleUpdate(AxisEvent event, AxisModule module) {
    }

    public void addParameter(Parameter param) throws AxisFault {
    }

    public void removeParameter(Parameter param) throws AxisFault {
    }

    public Parameter getParameter(String name) {
        return null;
    }

    public ArrayList getParameters() {
        return null;
    }

    //to check whether the parameter is locked at any levle
    public boolean isParameterLocked(String parameterName) {
        return false;
    }

    public void deserializeParameters(OMElement parameterElement) throws AxisFault {
        this.parameterimpl.deserializeParameters(parameterElement);
    }

    public void serviceGroupUpdate(AxisEvent event, AxisServiceGroup serviceGroup) {
    }
}
