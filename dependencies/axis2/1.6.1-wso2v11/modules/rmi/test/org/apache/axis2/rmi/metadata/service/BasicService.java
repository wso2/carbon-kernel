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

package org.apache.axis2.rmi.metadata.service;

import org.apache.axis2.rmi.metadata.service.dto.ComplexType1;

public class BasicService {

    public void method1() {

    }

    public void method2(int param1) {
        System.out.println("Ivoked with " + param1);
    }

    public void method3(String param1) {

    }

    public void method4(ComplexType1 complexType1) {

    }

    public int method5(int param1) {
        return 1;
    }

    public String method6(String param1) {
        return "";
    }

    public ComplexType1 method7(ComplexType1 complexType1) {
        return new ComplexType1();
    }

    public int[] method8(int[] param1) {
        return new int[0];
    }

    public String[] method9(String[] param1) {
        return new String[0];
    }

    public ComplexType1[] method10(ComplexType1[] complexType1) {
        return new ComplexType1[0];
    }

    public void method11(int param1, String param2, ComplexType1 complexType1) {

    }

    public void method12(int[] param1, String[] param2, ComplexType1[] complexType1) {

    }

}
