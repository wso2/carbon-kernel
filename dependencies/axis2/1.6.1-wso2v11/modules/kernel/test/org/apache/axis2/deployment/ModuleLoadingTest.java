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
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.engine.AxisConfiguration;

public class ModuleLoadingTest extends TestCase {
    public void testModuleLoading() throws Exception {
        String repoRoot = AbstractTestCase.basedir + "/test-resources/deployment/repositories/moduleLoadTest";
        AxisConfiguration ac =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(repoRoot , repoRoot + "/axis2.xml")
                        .getAxisConfiguration();

        // Make sure we got the exploded module in the repo/modules dir
        AxisModule module = ac.getModule("explodedModule");
        assertNotNull(module);
        String val = (String)module.getParameter("color").getValue();
        assertEquals("green", val);

        // Make sure we got the module that's outside the repo, but in the classpath
        module = ac.getModule("classpathModule");
        assertNotNull("Didn't find classpath module!", module);
        val = (String)module.getParameter("color").getValue();
        assertEquals("Parameter wasn't set correctly", "blue", val);
    }
}
