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

package org.apache.axis2.rmi.databind.service;


public class Service1 {

    public void method1() {
        System.out.println("method1 invoked");
    }

    public int method2(int param1) {
        System.out.println("Got int ==> " + param1);
        return param1;
    }

    public String method3(String param1) {
        System.out.println("Got String ==> " + param1);
        return param1;
    }

    public int[] method4(int[] param1) {
        if (param1 != null) {
            System.out.println("Got int ==> " + param1.length);
        } else {
            System.out.println("Got a null");
        }

        return param1;
    }

    public String[] method5(String[] param1) {
        if (param1 != null) {
            System.out.println("Got String ==> " + param1.length);
        } else {
            System.out.println("Got a null");
        }

        return param1;
    }
}
