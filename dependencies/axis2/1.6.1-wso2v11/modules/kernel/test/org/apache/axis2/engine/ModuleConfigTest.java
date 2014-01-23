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
import org.apache.axis2.AbstractTestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.ServiceBuilder;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.ModuleConfiguration;
import org.apache.axis2.description.Parameter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class ModuleConfigTest extends TestCase {

    AxisConfiguration ar;
    String axis2xml = AbstractTestCase.basedir +
            "/target/test-resources/deployment/moduleConfig/axis2.xml";
    String repo = AbstractTestCase.basedir + "/target/test-resources/deployment/moduleConfig";

    public void testModuleConfigAtAxisConfig() {
        try {
            ar = ConfigurationContextFactory
                    .createConfigurationContextFromFileSystem(null, axis2xml)
                    .getAxisConfiguration();
            ModuleConfiguration moduleConfiguration =
                    ar.getModuleConfig("testModule");
            assertNotNull(moduleConfiguration);
            Parameter para = moduleConfiguration.getParameter("testModulePara");
            assertNotNull(para);

            moduleConfiguration =
                    ar.getModuleConfig("testModule2");
            assertNotNull(moduleConfiguration);
            para = moduleConfiguration.getParameter("testModulePara2");
            assertNotNull(para);
        } catch (AxisFault e) {
            fail("This can not fail with this DeploymentException " + e);
        }
    }


    public void testModuleConfigAtService() {
        try {
            ConfigurationContext configurationContext = ConfigurationContextFactory
                    .createConfigurationContextFromFileSystem(null, axis2xml);
            ar = configurationContext.getAxisConfiguration();


            AxisService service = new AxisService();
            service.setName("testService");
            ar.addService(service);
            InputStream in = new FileInputStream(repo + "/service1.xml");
            ServiceBuilder sbuilder = new ServiceBuilder(in, configurationContext, service);
            sbuilder.populateService(sbuilder.buildOM());

            ModuleConfiguration moduleConfiguration =
                    service.getModuleConfig("Servie_module");
            assertNotNull(moduleConfiguration);
            Parameter para = moduleConfiguration.getParameter("Servie_module_para");
            assertNotNull(para);

            AxisOperation op = service.getOperation(new QName("echoString"));
            assertNotNull(op);

            moduleConfiguration = op.getModuleConfig("Op_Module");
            assertNotNull(moduleConfiguration);
            para = moduleConfiguration.getParameter("Op_Module_para");
            assertNotNull(para);


        } catch (DeploymentException e) {
            fail("This can not fail with this DeploymentException " + e);
        } catch (FileNotFoundException e) {
            fail("This can not fail with this FileNotFoundException  " + e);
        } catch (AxisFault axisFault) {
            fail("This can not fail with this AxisFault  " + axisFault);
        } catch (XMLStreamException e) {
            fail("This can not fail with this AxisFault  " + e);
        }
    }
}
