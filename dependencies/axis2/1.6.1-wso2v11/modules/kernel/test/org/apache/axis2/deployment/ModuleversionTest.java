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
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.util.Utils;

import java.util.Iterator;

public class ModuleversionTest extends TestCase {

    public void testDefautModuleVersion() throws AxisFault {
        String filename = AbstractTestCase.basedir +
                "/test-resources/deployment/moduleVersion/Test1/axis2.xml";
        AxisConfiguration ac =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, filename)
                        .getAxisConfiguration();
        assertNotNull(ac);
        assertEquals(ac.getDefaultModuleVersion("abc"), "1.23");
        assertEquals(ac.getDefaultModuleVersion("foo"), "0.89");
    }

    public void testCalculateDefaultModuleVersions() throws AxisFault {
        AxisConfiguration axisConfiguration = new AxisConfiguration();
        AxisModule module1 = new AxisModule();
        module1.setArchiveName("Module1");
        axisConfiguration.addModule(module1);

        AxisModule module2 = new AxisModule();
        module2.setArchiveName("Module2-0.94");
        axisConfiguration.addModule(module2);

        AxisModule module3 = new AxisModule();
        module3.setArchiveName("Module2-0.95");
        axisConfiguration.addModule(module3);

        AxisModule module4 = new AxisModule();
        module4.setArchiveName("Module2-0.93");
        axisConfiguration.addModule(module4);

        AxisModule module5 = new AxisModule();
        module5.setArchiveName("testModule-1.93");
        axisConfiguration.addModule(module5);

        Utils.calculateDefaultModuleVersion(axisConfiguration.getModules(), axisConfiguration);
        assertEquals(module1, axisConfiguration.getDefaultModule("Module1"));
        assertEquals(module3, axisConfiguration.getDefaultModule("Module2"));
        assertEquals(module5, axisConfiguration.getDefaultModule("testModule"));
        axisConfiguration.engageModule("Module2");
        axisConfiguration.engageModule("Module1");
        axisConfiguration.engageModule("testModule", "1.93");

        Iterator engageModules = axisConfiguration.getEngagedModules().iterator();
        boolean found1 = false;
        boolean found2 = false;
        boolean found3 = false;
        while (engageModules.hasNext()) {
            String name = ((AxisModule)engageModules.next()).getArchiveName();
            if (name.equals("Module2-0.95")) {
                found1 = true;
            }
            if (name.equals("Module1")) {
                found2 = true;
            }
            if (name.equals("testModule-1.93")) {
                found3 = true;
            }
        }

        if (!found1) {
            fail("Didn't find Module2-0.95");
        }
        if (!found2) {
            fail("Didn't find Module1");
        }
        if (!found3) {
            fail("Didn't find testModule-1.93");
        }
    }

     public void testModuleWithSNAPSHOT() throws AxisFault {
        AxisConfiguration axisConfiguration = new AxisConfiguration();
        AxisModule module1 = new AxisModule();
        module1.setArchiveName("Module1");
        axisConfiguration.addModule(module1);

        AxisModule module2 = new AxisModule();
        module2.setArchiveName("Module1-SNAPSHOT");
        axisConfiguration.addModule(module2);

        AxisModule module3 = new AxisModule();
        module3.setArchiveName("Module1-0.95");
        axisConfiguration.addModule(module3);
        Utils.calculateDefaultModuleVersion(axisConfiguration.getModules(), axisConfiguration);
        assertEquals(module2, axisConfiguration.getDefaultModule("Module1"));
    }


     public void testModuleWithSNAPSHOT2() throws AxisFault {
        AxisConfiguration axisConfiguration = new AxisConfiguration();
        AxisModule module1 = new AxisModule();
        module1.setArchiveName("Module1-a");
        axisConfiguration.addModule(module1);

        AxisModule module2 = new AxisModule();
        module2.setArchiveName("Module1-a-SNAPSHOT");
        axisConfiguration.addModule(module2);

        AxisModule module3 = new AxisModule();
        module3.setArchiveName("Module1-a-0.95");
        axisConfiguration.addModule(module3);
        Utils.calculateDefaultModuleVersion(axisConfiguration.getModules(), axisConfiguration);
        Utils.calculateDefaultModuleVersion(axisConfiguration.getModules(), axisConfiguration);
        assertEquals(module2, axisConfiguration.getDefaultModule("Module1-a"));
    }
}
