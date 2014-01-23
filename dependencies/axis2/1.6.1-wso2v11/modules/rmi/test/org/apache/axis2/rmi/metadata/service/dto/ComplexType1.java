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

package org.apache.axis2.rmi.metadata.service.dto;


public class ComplexType1 {

    private int param1;
    private String param2;
    private ComplexType2 param3;
    private int[] param4;
    private String[] param5;
    private ComplexType2[] param6;

    public int getParam1() {
        return param1;
    }

    public void setParam1(int param1) {
        this.param1 = param1;
    }

    public String getParam2() {
        return param2;
    }

    public void setParam2(String param2) {
        this.param2 = param2;
    }

    public ComplexType2 getParam3() {
        return param3;
    }

    public void setParam3(ComplexType2 param3) {
        this.param3 = param3;
    }

    public int[] getParam4() {
        return param4;
    }

    public void setParam4(int[] param4) {
        this.param4 = param4;
    }

    public String[] getParam5() {
        return param5;
    }

    public void setParam5(String[] param5) {
        this.param5 = param5;
    }

    public ComplexType2[] getParam6() {
        return param6;
    }

    public void setParam6(ComplexType2[] param6) {
        this.param6 = param6;
    }


}
