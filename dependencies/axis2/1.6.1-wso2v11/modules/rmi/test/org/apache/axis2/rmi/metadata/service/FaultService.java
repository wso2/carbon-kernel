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

import org.apache.axis2.rmi.metadata.service.exception.Exception1;
import org.apache.axis2.rmi.metadata.service.exception.Exception2;
import org.apache.axis2.rmi.metadata.service.exception.Exception3;
import org.apache.axis2.rmi.metadata.service.exception.Exception4;


public class FaultService {

    public void method1()
            throws Exception1 {
        throw new Exception1();
    }

    public String method2(String param1)
            throws Exception2 {
        throw new Exception2();
    }

    public int method3(int param1)
            throws Exception1, Exception2 {
        throw new Exception1();
    }

    public void method4(int param1)
            throws Exception1, Exception2, Exception3, Exception4 {
        throw new Exception3();
    }

}
