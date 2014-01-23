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

package org.apache.axis2.engine;

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.description.Parameter;

/**
 * To chcek locked is working corrcetly
 */

public class ParameterAddTest extends TestCase {

    private AxisConfiguration reg = new AxisConfiguration();

    public void testAddParameterServiceLockedAtAxisConfig() {
        try {
            Parameter para = new Parameter();
            para.setValue(null);
            para.setName("PARA_NAME");
            para.setLocked(true);
            reg.addParameter(para);

            AxisService service = new AxisService("Service1");
            reg.addService(service);
            service.addParameter(para);
            fail("This should fails with Parmter is locked can not overide");
        } catch (AxisFault axisFault) {

        }
    }

    public void testAddParameterModuleLockedAtAxisConfig() {
        try {
            Parameter para = new Parameter();
            para.setValue(null);
            para.setName("PARA_NAME");
            para.setLocked(true);
            reg.addParameter(para);
            AxisModule module = new AxisModule("Service1");
            module.setParent(reg);
            module.addParameter(para);
            fail("This should fails with Parmter is locked can not overide");
        } catch (AxisFault axisFault) {

        }
    }

    public void testAddParameterOperationlockedByAxisConfig() {
        try {
            Parameter para = new Parameter();
            para.setValue(null);
            para.setName("PARA_NAME");
            para.setLocked(true);
            reg.addParameter(para);

            AxisService service = new AxisService("Service1");
            reg.addService(service);

            AxisOperation opertion = new InOutAxisOperation();
            opertion.setParent(service);
            opertion.addParameter(para);
            fail("This should fails with Parmter is locked can not overide");


        } catch (AxisFault axisFault) {

        }
    }

    public void testAddParameterOperationLockebyService() {
        try {
            Parameter para = new Parameter();
            para.setValue(null);
            para.setName("PARA_NAME");
            para.setLocked(true);

            AxisService service = new AxisService("Service1");
            reg.addService(service);
            service.addParameter(para);

            AxisOperation opertion = new InOutAxisOperation();
            opertion.setParent(service);
            opertion.addParameter(para);
            fail("This should fails with Parmter is locked can not overide");
        } catch (AxisFault axisFault) {

        }
    }


}
