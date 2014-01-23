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

package org.apache.axis2.jaxws.sample.dlwmin.sei;

import org.apache.axis2.jaxws.sample.dlwmin.types.ProcessFault3;

import javax.xml.ws.WebFault;

/**
 * Checked Exception with a WebFault that locates an existing JAXB Bean
 *
 */
@WebFault(name="processFault3", targetNamespace="http://apache.org/axis2/jaxws/sample/dlwmin/types")
public class TestException3 extends Exception {

    public ProcessFault3 faultInfo;
    

    public TestException3(String message, ProcessFault3 faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }
    
    public ProcessFault3 getFaultInfo() {
        return faultInfo;
    }

}
