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
import org.apache.axis2.i18n.Messages;

import java.util.ArrayList;

/**
 * This is to store deployment time data , described by
 * <moduleConfig module="modulename">
 * <parameter> ....</parameter>
 * </moduleConfig>
 * <p/>
 * Right now this just keeps stores the set of parameters
 */
public class ModuleConfiguration implements ParameterInclude {
    private String moduleName;
    private ParameterInclude parameterInclude;

    // to keep the pointer to its parent , only to access parameters
    private ParameterInclude parent;

    public ModuleConfiguration(String moduleName, ParameterInclude parent) {
        this.moduleName = moduleName;
        this.parent = parent;
        parameterInclude = new ParameterIncludeImpl();
    }

    public void addParameter(Parameter param) throws AxisFault {
        if (isParameterLocked(param.getName())) {
            throw new AxisFault(Messages.getMessage("paramterlockedbyparent", param.getName()));
        } else {
            parameterInclude.addParameter(param);
        }
    }

    public void removeParameter(Parameter param) throws AxisFault {
        if (isParameterLocked(param.getName())) {
            throw new AxisFault(Messages.getMessage("paramterlockedbyparent", param.getName()));
        } else {
            parameterInclude.removeParameter(param);
        }
    }

    public void deserializeParameters(OMElement parameterElement) throws AxisFault {
        this.parameterInclude.deserializeParameters(parameterElement);
    }

    public String getModuleName() {
        return moduleName;
    }

    public Parameter getParameter(String name) {
        return parameterInclude.getParameter(name);
    }

    public ArrayList<Parameter> getParameters() {
        return parameterInclude.getParameters();
    }

    public boolean isParameterLocked(String parameterName) {

        // checking the locked value of parent
        boolean loscked = false;

        if (parent != null) {
            loscked = parent.isParameterLocked(parameterName);
        }

        if (loscked) {
            return true;
        } else {
            Parameter parameter = getParameter(parameterName);

            return (parameter != null) && parameter.isLocked();
        }
    }
}
