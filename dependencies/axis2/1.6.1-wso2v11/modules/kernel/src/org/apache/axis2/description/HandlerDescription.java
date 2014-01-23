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
import org.apache.axis2.engine.Handler;
import org.apache.axis2.i18n.Messages;

import java.util.ArrayList;

/**
 * Represents the deployment information about the handler
 */
public class HandlerDescription implements ParameterInclude {

    /**
     * Field className
     */
    private String className;

    private Handler handler;
    private String name;
    private final ParameterInclude parameterInclude;
    private ParameterInclude parent;
    private PhaseRule rules;

    /**
     * Constructor HandlerDescription.
     */
    public HandlerDescription() {
        this.parameterInclude = new ParameterIncludeImpl();
        this.rules = new PhaseRule();
    }

    /**
     * Constructor HandlerDescription.
     *
     * @param name name of handler
     */
    public HandlerDescription(String name) {
        this();
        this.name = name;
    }

    /**
     * Add a Parameter
     *
     * @param param the Parameter to associate with this HandlerDescription
     */
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

    /**
     * Method getClassName.
     *
     * @return Returns String.
     */
    public String getClassName() {
        return className;
    }

    /**
     * @return Returns Handler.
     */
    public Handler getHandler() {
        return handler;
    }

    /**
     * @return Returns QName.
     */
    public String getName() {
        return name;
    }

    /**
     * Get a named Parameter
     *
     * @param name name of Parameter to search
     * @return a Parameter, which may come from us or from some parent up the tree, or null.
     */
    public Parameter getParameter(String name) {
        Parameter parameter = parameterInclude.getParameter(name);
        if (parameter == null && parent != null) {
            return parent.getParameter(name);
        } else {
            return parameter;
        }
    }

    public ArrayList<Parameter> getParameters() {
        return parameterInclude.getParameters();
    }

    public ParameterInclude getParent() {
        return parent;
    }

    /**
     * Method getRules.
     *
     * @return Returns PhaseRule.
     */
    public PhaseRule getRules() {
        return rules;
    }

    // to check whether the parameter is locked at any level
    public boolean isParameterLocked(String parameterName) {
        if (parent != null) {
            if (parent.isParameterLocked(parameterName)) {
                return true;
            }
        }

        return parameterInclude.isParameterLocked(parameterName);
    }

    /**
     * Method setClassName.
     *
     * @param className the class name of the Handler class
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * Explicitly set the Handler object
     *
     * @param handler a Handler instance, which will be deployed wherever this HandlerDescription is
     */
    public void setHandler(Handler handler) {
        this.handler = handler;
        this.className = handler.getClass().getName();
    }

    /**
     * Set the name
     *
     * @param name the desired name
     */
    public void setName(String name) {
        this.name = name;
    }

    public void setParent(ParameterInclude parent) {
        this.parent = parent;
    }

    /**
     * Set the deployment rules for this HandlerDescription
     *
     * @param rules a PhaseRule object
     */
    public void setRules(PhaseRule rules) {
        this.rules = rules;
    }
}
