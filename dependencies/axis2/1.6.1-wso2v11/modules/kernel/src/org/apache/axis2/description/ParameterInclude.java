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

import java.util.ArrayList;

/**
 * Interface ParameterInclude
 */
public interface ParameterInclude {

    /**
     * Method addParameter.
     *
     * @param param
     */
    public void addParameter(Parameter param) throws AxisFault;

    public void removeParameter(Parameter param) throws AxisFault;

    public void deserializeParameters(OMElement parameterElement) throws AxisFault;

    /**
     * Method getParameter.
     *
     * @param name
     * @return Returns Parameter.
     */
    public Parameter getParameter(String name);

    /**
     * Gets all the parameters in a given description.
     *
     * @return Returns ArrayList.
     */
    ArrayList<Parameter> getParameters();

    /**
     * Checks whether the parameter is locked at any level.
     */
    boolean isParameterLocked(String parameterName);
}
