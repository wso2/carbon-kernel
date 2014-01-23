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

package org.apache.axis2.deployment;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.axis2.AbstractTestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Phase;

public class ModuleDisengagementTest extends TestCase {
    AxisConfiguration config;
    String serviceName = "testService";
    QName opName = new QName("testOperation");

    protected void setUp() throws Exception {
        String filename =
                AbstractTestCase.basedir + "/test-resources/deployment/moduleDisEngegeRepo";
        config = ConfigurationContextFactory.
                createConfigurationContextFromFileSystem(filename, filename + "/axis2.xml").getAxisConfiguration();
        AxisService testService = new AxisService();
        testService.setName(serviceName);
        AxisOperation testOperation = new InOutAxisOperation();
        testOperation.setName(opName);
        testService.addOperation(testOperation);

        testOperation = new InOutAxisOperation();
        testOperation.setName(new QName("oper2"));
        testService.addOperation(testOperation);
        
        config.addService(testService);
    }

    public void testGlobalDisengagement() throws AxisFault {
        AxisModule module = config.getModule("testModule");
        assertNotNull(module);
        Phase phase;
        Phase userPhase;
        List globalinflow = config.getInFlowPhases();
        assertNotNull(globalinflow);
        phase = (Phase) globalinflow.get(3);
        assertNotNull(phase);
        assertEquals(phase.getHandlerCount(), 0);
        AxisService service = config.getService(serviceName);
        assertNotNull(service);
        AxisOperation operation = service.getOperation(opName);
        assertNotNull(operation);
        userPhase = (Phase) operation.getRemainingPhasesInFlow().get(1);
        assertNotNull(userPhase);
        assertEquals(0, userPhase.getHandlerCount());

        config.engageModule(module.getName());
        assertEquals(2, phase.getHandlerCount());
        assertEquals(1, userPhase.getHandlerCount());

        config.disengageModule(module);
        assertEquals(0, phase.getHandlerCount());
        assertEquals(0, userPhase.getHandlerCount());
    }

    public void testServiceDisengagement() throws AxisFault {
        AxisModule module = config.getModule("testModule");
        assertNotNull(module);
        Phase phase;
        Phase userPhase;
        List globalinflow = config.getInFlowPhases();
        assertNotNull(globalinflow);
        phase = (Phase) globalinflow.get(3);
        assertNotNull(phase);
        assertEquals(0, phase.getHandlerCount());
        AxisService service = config.getService(serviceName);
        assertNotNull(service);
        AxisOperation operation = service.getOperation(opName);
        assertNotNull(operation);

        userPhase = (Phase) operation.getRemainingPhasesInFlow().get(1);
        assertNotNull(userPhase);
        assertEquals(0, userPhase.getHandlerCount());

        config.engageModule(module.getName());
        assertEquals(2, phase.getHandlerCount());
        assertEquals(1, userPhase.getHandlerCount());

        service.disengageModule(module);
        assertEquals(2, phase.getHandlerCount());
        assertEquals(0, userPhase.getHandlerCount());
    }


    public void testGlobalCheck() throws AxisFault {
        AxisModule module = config.getModule("testModule");
        assertNotNull(module);
        config.engageModule(module.getName());
        config.disengageModule(module);
        config.engageModule(module.getName());
    }

    public void testOperationDisengagement() throws AxisFault {
        AxisModule module = config.getModule("testModule");
        assertNotNull(module);
        Phase phase;
        Phase userPhase;
        List globalinflow = config.getInFlowPhases();
        assertNotNull(globalinflow);
        phase = (Phase) globalinflow.get(3);
        assertNotNull(phase);
        assertEquals(phase.getHandlerCount(), 0);
        AxisService service = config.getService(serviceName);
        assertNotNull(service);
        AxisOperation operation = service.getOperation(opName);
        assertNotNull(operation);
        userPhase = (Phase) operation.getRemainingPhasesInFlow().get(1);
        assertNotNull(userPhase);
        assertEquals(0, userPhase.getHandlerCount());

        config.engageModule(module.getName());
        assertEquals(2, phase.getHandlerCount());
        assertEquals(1, userPhase.getHandlerCount());

        operation.disengageModule(module);
        assertEquals(2, phase.getHandlerCount());
        assertEquals(0, userPhase.getHandlerCount());
    }

    public void testServiceEngageServiceDisengage() throws AxisFault {
        AxisModule module = config.getModule("testModule");
        assertNotNull(module);
        Phase predisptah;
        Phase userPhase;
        List globalinflow = config.getInFlowPhases();
        assertNotNull(globalinflow);
        predisptah = (Phase) globalinflow.get(3);
        assertNotNull(predisptah);
        assertEquals(predisptah.getHandlerCount(), 0);
        AxisService service = config.getService(serviceName);
        assertNotNull(service);
        AxisOperation operation = service.getOperation(opName);
        assertNotNull(operation);
        userPhase = (Phase) operation.getRemainingPhasesInFlow().get(1);
        assertNotNull(userPhase);
        assertEquals(0, userPhase.getHandlerCount());

        service.engageModule(module);
        assertEquals(2, predisptah.getHandlerCount());
        assertEquals(1, userPhase.getHandlerCount());

        service.disengageModule(module);
        assertEquals(0, predisptah.getHandlerCount());
        assertEquals(0, userPhase.getHandlerCount());
    }

    public void testServiceEngageOperationDisengage() throws AxisFault {
        AxisModule module = config.getModule("testModule");
        assertNotNull(module);
        Phase phase;
        Phase userPhase;
        List globalinflow = config.getInFlowPhases();
        assertNotNull(globalinflow);
        phase = (Phase) globalinflow.get(3);
        assertNotNull(phase);
        assertEquals(phase.getHandlerCount(), 0);
        AxisService service = config.getService(serviceName);
        assertNotNull(service);
        AxisOperation operation = service.getOperation(opName);
        assertNotNull(operation);
        userPhase = (Phase) operation.getRemainingPhasesInFlow().get(1);
        assertNotNull(userPhase);
        assertEquals(0, userPhase.getHandlerCount());

        service.engageModule(module);
        assertEquals(2, phase.getHandlerCount());
        assertEquals(1, userPhase.getHandlerCount());

        operation.disengageModule(module);
        assertEquals(2, phase.getHandlerCount());
        assertEquals(0, userPhase.getHandlerCount());
    }

    public void testOperationEngageOperationDisengage() throws AxisFault {
        AxisModule module = config.getModule("testModule");
        assertNotNull(module);
        Phase phase;
        Phase userPhase;
        List globalinflow = config.getInFlowPhases();
        assertNotNull(globalinflow);
        phase = (Phase) globalinflow.get(3);
        assertNotNull(phase);
        assertEquals(phase.getHandlerCount(), 0);
        AxisService service = config.getService(serviceName);
        assertNotNull(service);
        AxisOperation operation = service.getOperation(opName);
        assertNotNull(operation);
        userPhase = (Phase) operation.getRemainingPhasesInFlow().get(1);
        assertNotNull(userPhase);
        assertEquals(0, userPhase.getHandlerCount());
        operation.engageModule(module);
        assertEquals(2, phase.getHandlerCount());
        assertEquals(1, userPhase.getHandlerCount());
        operation.disengageModule(module);
        assertEquals(0, phase.getHandlerCount());
        assertEquals(0, userPhase.getHandlerCount());
    }


}
