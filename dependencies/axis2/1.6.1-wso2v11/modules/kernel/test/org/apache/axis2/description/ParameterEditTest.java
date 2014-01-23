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

import junit.framework.TestCase;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.engine.AxisConfiguration;

public class ParameterEditTest extends TestCase {

    public void testParameterEdit() throws Exception{
        ConfigurationContext configCtx = ConfigurationContextFactory.createEmptyConfigurationContext();
        AxisConfiguration axisConfig = configCtx.getAxisConfiguration();
        Parameter parameter = new Parameter();
        parameter.setValue("true");
        parameter.setName("enableMTOM");
        axisConfig.addParameter(parameter);
        parameter.setValue("true");
        AxisServiceGroup serviceGroup = new AxisServiceGroup();
        serviceGroup.setServiceGroupName("testServiceGroup");
        AxisService service = new AxisService();
        service.setName("service");
        serviceGroup.addService(service);
        axisConfig.addServiceGroup(serviceGroup);
        parameter = serviceGroup.getParameter("enableMTOM");
        parameter.setValue("true");
        Parameter para2= serviceGroup.getParameter("enableMTOM");
        assertEquals(para2.getValue(),"true");
        Parameter test = new Parameter();
        test.setName("test");
        test.setValue("test");
        serviceGroup.addParameter(test);
        Parameter para = serviceGroup.getParameter("test");
        assertNotNull(para);
        assertEquals(para.getValue(),"test");
        para.setValue("newValue");
        para = serviceGroup.getParameter("test");
        assertNotNull(para);
        assertEquals(para.getValue(),"newValue");

    }
}
