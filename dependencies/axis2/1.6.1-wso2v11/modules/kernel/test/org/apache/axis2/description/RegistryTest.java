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

import org.apache.axis2.AbstractTestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.handlers.AbstractHandler;

import javax.xml.namespace.QName;

public class RegistryTest extends AbstractTestCase {
    private AxisConfiguration axisConfiguration = new AxisConfiguration();
    AxisService service = new AxisService("Service1");

    public RegistryTest(String testName) {
        super(testName);
    }


    public void testHandlerMetadata() throws AxisFault {
        HandlerDescription hmd = new HandlerDescription();
        testParameteInClude(hmd);
    }

    public void testService() throws AxisFault {
        axisConfiguration.addService(service);
        testParameteInClude(service);
    }

    public void testModule() throws AxisFault {
        AxisModule module = new AxisModule("module1");
        module.setParent(axisConfiguration);
        testParameteInClude(module);
        testFlowIncludeTest(module);
    }

    public void testOperation() throws AxisFault {
        AxisOperation op = new InOutAxisOperation(new QName("op"));
        op.setParent(service);
        testParameteInClude(op);
    }


    public void testParameteInClude(ParameterInclude parmInclude) throws AxisFault {
        String key = "value1";
        Parameter p = new Parameter(key, "value2");
        parmInclude.addParameter(p);
        assertEquals(p, parmInclude.getParameter(key));
    }

    public void testFlowIncludeTest(AxisModule flowInclude) {
        Flow flow1 = new Flow();
        Flow flow2 = new Flow();
        Flow flow3 = new Flow();

        flowInclude.setInFlow(flow1);
        flowInclude.setFaultInFlow(flow2);
        flowInclude.setOutFlow(flow3);
        assertSame(flow1, flowInclude.getInFlow());
        assertSame(flow2, flowInclude.getFaultInFlow());
        assertSame(flow3, flowInclude.getOutFlow());
    }


    public void testHandlers() throws AxisFault {
        Handler handler = new AbstractHandler() {

            public InvocationResponse invoke(MessageContext msgContext) {
                return InvocationResponse.CONTINUE;
            }
        };
        handler.init(new HandlerDescription());
        assertNull(handler.getName());
        assertNull(handler.getParameter("hello"));
    }


}
