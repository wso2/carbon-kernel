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

package org.apache.axis2.context;

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.wsdl.WSDLConstants;

import javax.xml.namespace.QName;

public class ContextHierarchyTest extends TestCase {
    private AxisMessage axisMessage;
    private AxisOperation axisOperation;
    private AxisService axisService;
    private AxisConfiguration axisConfiguration;
    private ConfigurationContext configurationContext;
    private MessageContext msgctx;

    protected void setUp() throws Exception {
        axisOperation = new InOutAxisOperation(new QName("Temp"));
        axisMessage = axisOperation.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
        axisService = new AxisService("Temp");
        axisConfiguration = new AxisConfiguration();
        axisService.addOperation(axisOperation);
        axisConfiguration.addService(axisService);
        configurationContext = new ConfigurationContext(axisConfiguration);
        msgctx = configurationContext.createMessageContext();
    }

    public void testCompleteHierarchy() throws AxisFault {
        ServiceGroupContext serviceGroupContext = configurationContext.createServiceGroupContext(
                axisService.getAxisServiceGroup());
        ServiceContext serviceContext = serviceGroupContext.getServiceContext(axisService);
        OperationContext opContext = axisOperation.findOperationContext(msgctx,
                                                                        serviceContext);
        axisOperation.registerOperationContext(msgctx, opContext);
        msgctx.setAxisMessage(axisMessage);
        msgctx.setServiceContext(serviceContext);

        // test the complte Hierarchy built
        assertEquals(msgctx.getParent(), opContext);
        assertEquals(opContext.getParent(), serviceContext);
        assertEquals(serviceContext.getParent(), serviceGroupContext);

        String key = "key1";
        String paramValue1 = "paramValue1";
        String propValue1 = "propValue1";
        String paramValue2 = "paramValue2";
        String propValue2 = "propValue2";
        String paramValue3 = "paramValue3";
        String propValue3 = "propValue3";
        String paramValue4 = "paramValue4";
        String propValue4 = "propValue4";

        configurationContext.setProperty(key, propValue1);
        assertEquals(propValue1, msgctx.getProperty(key));

        axisConfiguration.addParameter(new Parameter(key, paramValue1));
        assertEquals(paramValue1, msgctx.getParameter(key).getValue());
        
        serviceContext.setProperty(key, propValue2);
        assertEquals(propValue2, msgctx.getProperty(key));
        
        axisService.addParameter(new Parameter(key, paramValue2));
        assertEquals(paramValue2, msgctx.getParameter(key).getValue());

        opContext.setProperty(key, propValue3);
        assertEquals(propValue3, msgctx.getProperty(key));
        
        axisOperation.addParameter(new Parameter(key, paramValue3));
        assertEquals(paramValue3, msgctx.getParameter(key).getValue());
        
        msgctx.setProperty(key, propValue4);
        assertEquals(propValue4, msgctx.getProperty(key));
        
        axisMessage.addParameter(new Parameter(key, paramValue4));
        assertEquals(paramValue4, msgctx.getParameter(key).getValue());

        
    }

    public void testDisconntectedHierarchy() throws AxisFault {
        // test the complete Hierarchy built
        assertEquals(msgctx.getParent(), null);

        String key1 = "key1";
        String value1 = "Val1";
        String value2 = "value2";
        String key2 = "key2";

        configurationContext.setProperty(key1, value1);
        assertEquals(value1, msgctx.getProperty(key1));

        axisConfiguration.addParameter(new Parameter(key2, value2));
        assertEquals(value2, msgctx.getParameter(key2).getValue());
    }
    
}
