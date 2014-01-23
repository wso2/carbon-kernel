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

import junit.framework.TestCase;
import org.apache.axis2.AbstractTestCase;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;

import javax.xml.namespace.QName;
import java.util.ArrayList;

public class OperationModuleTest extends TestCase {
    /**
     * Confirm that an operation module a) doesn't cause any deployment problems, and
     * b) correctly configures the AxisOperation.
     *
     * @throws Exception if there is a problem
     */
    public void testOperationModule() throws Exception {
        ConfigurationContext configCtx =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                        AbstractTestCase.basedir +
                                "/test-resources/deployment/AxisMessageTestRepo");
        AxisConfiguration config = configCtx.getAxisConfiguration();
        AxisService service = config.getService("MessagetestService");
        assertNotNull("Couldn't find service", service);
        AxisOperation operation = service.getOperation(new QName("echoString"));
        assertNotNull("Couldn't find operation", operation);
        ArrayList moduleRefs = operation.getModuleRefs();
        assertEquals("Wrong # of modules", 1, moduleRefs.size());
        String moduleName = (String)moduleRefs.get(0);
        assertEquals("Wrong module name", "module1", moduleName);
    }
}
