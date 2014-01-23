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

/**
 * Class FlowIncludeImpl
 */
public class FlowInclude {

    /**
     * Field fault
     */
    private Flow In_fault;
    private Flow Out_fault;

    /**
     * Field in
     */
    private Flow in;

    /**
     * Field out
     */
    private Flow out;

    /**
     * Method getFaultInFlow.
     *
     * @return Returns Flow.
     */
    public Flow getFaultInFlow() {
        return In_fault;
    }

    public Flow getFaultOutFlow() {
        return this.Out_fault;
    }

    /**
     * Method getInFlow.
     *
     * @return Returns Flow.
     */
    public Flow getInFlow() {
        return in;
    }

    /**
     * Method getOutFlow.
     *
     * @return Returns Flow.
     */
    public Flow getOutFlow() {
        return out;
    }

    /**
     * Method setFaultInFlow.
     *
     * @param flow
     */
    public void setFaultInFlow(Flow flow) {
        this.In_fault = flow;
    }

    public void setFaultOutFlow(Flow faultFlow) {
        this.Out_fault = faultFlow;
    }

    /**
     * Method setInFlow.
     *
     * @param flow
     */
    public void setInFlow(Flow flow) {
        this.in = flow;
    }

    /**
     * Method setOutFlow.
     *
     * @param flow
     */
    public void setOutFlow(Flow flow) {
        this.out = flow;
    }
}
