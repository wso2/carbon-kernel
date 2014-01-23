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

package org.apache.ideaplugin.bean;

public class OperationObj {

    private String OpName;
    private String ReturnValue;
    private Integer parameters;
    private Boolean select;

    public OperationObj(String opName, String returnVale, Integer parameters, Boolean select) {
        OpName = opName;
        ReturnValue = returnVale;
        this.parameters = parameters;
        this.select = select;
    }


    public String getOpName() {
        return OpName;
    }

    public void setOpName(String opName) {
        OpName = opName;
    }

    public String getReturnValue() {
        return ReturnValue;
    }

    public void setReturnValue(String returnValue) {
        ReturnValue = returnValue;
    }

    public Integer getParameters() {
        return parameters;
    }

    public void setParameters(Integer parameters) {
        this.parameters = parameters;
    }

    public Boolean getSelect() {
        return select;
    }

    public void setSelect(Boolean select) {
        this.select = select;
    }

    public void printMe() {
        System.out.println("======== Row =============");
        System.out.println("OpName = " + OpName);
        System.out.println("parameters = " + parameters);
        System.out.println("ReturnValue = " + ReturnValue);
        System.out.println("select = " + select);
        System.out.println("==========================");
    }

}
