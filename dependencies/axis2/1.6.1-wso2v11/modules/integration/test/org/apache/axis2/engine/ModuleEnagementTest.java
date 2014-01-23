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
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Flow;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.PhaseRule;

import javax.xml.namespace.QName;
import java.util.List;

public class ModuleEnagementTest extends TestCase {
    AxisConfiguration axisConfig;

    protected void setUp() throws Exception {
        AxisModule module = new AxisModule();
        module.setName("TestModule");
        module.setInFlow(getFlowWithHandler("H1", "OperationInPhase"));
        module.setOutFlow(getFlowWithHandler("H2", "OperationOutPhase"));
        module.setFaultInFlow(getFlowWithHandler("H3", "OperationInFaultPhase"));
        module.setFaultOutFlow(getFlowWithHandler("H4", "OperationOutFaultPhase"));
        axisConfig = ConfigurationContextFactory.createDefaultConfigurationContext().getAxisConfiguration();
        axisConfig.addModule(module);
    }

    private Flow getFlowWithHandler(String handlername, String phaseName) {
        HandlerDescription handler1 = new HandlerDescription();
        TestHandler moduleHandler = new TestHandler();
        moduleHandler.setName(handlername);
        moduleHandler.setName(handlername);
        moduleHandler.init(handler1);
        PhaseRule rule1 = new PhaseRule();
        rule1.setPhaseName(phaseName);
        handler1.setRules(rule1);
        handler1.setHandler(moduleHandler);
        Flow flow = new Flow();
        flow.addHandler(handler1);
        return flow;
    }

    public void testModuleEngageForAxisService() throws AxisFault {
        AxisService service = AxisService.createService(TestService.class.getName(), axisConfig);
        axisConfig.addService(service);
        AxisModule module = axisConfig.getModule("TestModule");
        assertNotNull(module);
        service.engageModule(module);
        AxisOperation axisOperation = service.getOperation(new QName("testVoid"));
        assertNotNull(axisOperation);
        AxisMessage message = axisOperation.getMessage("In");
        assertNotNull(message);
        List list = message.getMessageFlow();
        boolean found = false;
        for (int i = 0; i < list.size(); i++) {
            Phase phase = (Phase) list.get(i);
            if (phase != null && phase.getName().equals("OperationInPhase")) {
                List handler = phase.getHandlers();
                for (int j = 0; j < handler.size(); j++) {
                    Handler handler1 = (Handler) handler.get(j);
                    if (handler1.getName().equals("H1")) {
                        found = true;
                    }

                }
            }
        }
        assertTrue(found);
    }

    public void engageModuleToAxisMessage1() throws Exception{
        AxisService service = AxisService.createService(TestService.class.getName(), axisConfig);
        axisConfig.addService(service);
        AxisModule module = axisConfig.getModule("TestModule");
        assertNotNull(module);
        AxisOperation axisOperation = service.getOperation(new QName("testVoid"));
        assertNotNull(axisOperation);
        AxisMessage message = axisOperation.getMessage("In");
        message.engageModule(module);
        assertNotNull(message);
        List list = message.getMessageFlow();
        boolean found = false;
        for (int i = 0; i < list.size(); i++) {
            Phase phase = (Phase) list.get(i);
            if (phase != null && phase.getName().equals("OperationInPhase")) {
                List handler = phase.getHandlers();
                for (int j = 0; j < handler.size(); j++) {
                    Handler handler1 = (Handler) handler.get(j);
                    if (handler1.getName().equals("H1")) {
                        found = true;
                    }

                }
            }
        }
        assertTrue(found);
    }

     public void engageModuleToAxisMessage2() throws Exception{
         AxisService service = AxisService.createService(TestService.class.getName(), axisConfig);
         axisConfig.addService(service);
         AxisModule module = axisConfig.getModule("TestModule");
         assertNotNull(module);
         AxisOperation axisOperation = service.getOperation(new QName("testString"));
         assertNotNull(axisOperation);
         AxisMessage message = axisOperation.getMessage("In");
         message.engageModule(module);
         assertNotNull(message);
         message = axisOperation.getMessage("Out");
         List list = message.getMessageFlow();
         boolean found = false;
         for (int i = 0; i < list.size(); i++) {
             Phase phase = (Phase) list.get(i);
             if (phase != null && phase.getName().equals("OperationOutPhase")) {
                 List handler = phase.getHandlers();
                 for (int j = 0; j < handler.size(); j++) {
                     Handler handler1 = (Handler) handler.get(j);
                     if (handler1.getName().equals("H2")) {
                         found = true;
                     }

                 }
             }
         }
         assertFalse(found);
    }
}
