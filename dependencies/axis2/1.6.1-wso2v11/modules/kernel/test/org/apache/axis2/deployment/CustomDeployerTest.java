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
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.deployment.deployers.CustomDeployer;
import org.apache.axis2.engine.AxisConfiguration;

public class CustomDeployerTest extends TestCase {
    public void testCustomDeployer() throws Exception {
        String filename =
                AbstractTestCase.basedir + "/test-resources/deployment/CustomDeployerRepo";
        AxisConfiguration axisConfig = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(filename)
                .getAxisConfiguration();

        // OK, let's see what we've got here...
        assertTrue("Init was not called", CustomDeployer.initCalled);
        assertEquals("Wrong directory", "widgets", CustomDeployer.directory);
        assertEquals("Wrong extension", "svc", CustomDeployer.extension);
        assertEquals("Wrong number of deployed items", 1, CustomDeployer.deployedItems);
        assertTrue("Mary wasn't found", CustomDeployer.maryDeployed);

        assertEquals("Parameter not set correctly",
                     CustomDeployer.PARAM_VAL,
                     axisConfig.getParameterValue(CustomDeployer.PARAM_NAME));
    }
}
