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

package org.apache.axis2.jaxws.marshaller;

import org.apache.axis2.jaxws.description.ParameterDescription;

/**
 * Stores Method Parameter as Name and Value. Method Parameter can be an input Method Parameter or
 * output Method parameter. input Method Parameter is a input to a java Method. output Method
 * Parameter is a return parameter from a java Method.
 */
public class MethodParameter {
    private ParameterDescription parameterDescription = null;
    private String webResultName = null;
    private String webResultTNS = null;
    private Class webResultType = null;
    private boolean isWebResult = false;
    private Object value = null;

    public MethodParameter(ParameterDescription parameterDescription, Object value) {
        super();
        this.parameterDescription = parameterDescription;
        this.value = value;
    }

    public MethodParameter(String webResultName, String webResultTNS, Class webResultType,
                           Object value) {
        super();
        this.parameterDescription = null;
        this.webResultName = webResultName;
        this.webResultTNS = webResultTNS;
        this.webResultType = webResultType;
        this.value = value;
        this.isWebResult = true;
    }

    public ParameterDescription getParameterDescription() {
        return parameterDescription;
    }

    public void setParameterDescription(ParameterDescription parameterDescription) {
        this.parameterDescription = parameterDescription;
    }

    public String getWebResultName() {
        return webResultName;
    }

    public void setWebResultName(String webResultName) {
        this.webResultName = webResultName;
    }

    public boolean isWebResult() {
        return isWebResult;
    }

    public void setWebResult(boolean isWebResult) {
        this.isWebResult = isWebResult;
    }

    public Object getValue() {
        return value;
    }

    public String getWebResultTNS() {
        return webResultTNS;
    }

    public void setWebResultTNS(String webResultTNS) {
        this.webResultTNS = webResultTNS;
    }

    public Class getWebResultType() {
        return webResultType;
    }

    public void setWebResultType(Class webResultType) {
        this.webResultType = webResultType;
    }
}
