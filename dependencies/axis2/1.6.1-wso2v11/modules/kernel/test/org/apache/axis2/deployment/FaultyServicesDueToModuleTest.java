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
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.AbstractTestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.util.FaultyServiceData;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.context.ConfigurationContextFactory;

import java.util.HashMap;
import java.util.Map;
import java.io.File;

public class FaultyServicesDueToModuleTest extends TestCase {
    
    AxisConfiguration axisConfig;
    String repo = AbstractTestCase.basedir + "/test-resources/deployment/faultyServiceshandling/repo";

    protected void setUp() throws Exception {
        axisConfig = ConfigurationContextFactory.createConfigurationContextFromFileSystem(repo, repo + "/axis2.xml")
                .getAxisConfiguration();
    }

    public void testFaultyServiceDueToModuleLogging() throws AxisFault {
        AxisService axisService = axisConfig.getService("echo2");
        assertNull(axisService);

        Map<String, FaultyServiceData> faultyServicesMap = axisConfig.getFaultyServicesDuetoModule(
                "sample-logging");
        FaultyServiceData faultyServiceData = faultyServicesMap.get("echo2");
        assertNotNull(faultyServiceData);
    }

    public void testFaultyServicesRecovery() throws AxisFault{
        File moduleFile = new File(AbstractTestCase.basedir +
                "/test-resources/deployment/faultyServiceshandling/sample-logging");
        DeploymentFileData deploymentFileData = new DeploymentFileData(moduleFile, new ModuleDeployer(axisConfig));
        deploymentFileData.deploy();

        AxisService axisService = axisConfig.getService("echo2");
        assertNotNull(axisService);

        Map<String, FaultyServiceData> faultyServicesMap = axisConfig.getFaultyServicesDuetoModule(
                "sample-logging");
        FaultyServiceData faultyServiceData = faultyServicesMap.get("echo2");
        assertNull(faultyServiceData);
    }
}
