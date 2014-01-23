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

import org.apache.axis2.AbstractTestCase;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.util.Loader;

import javax.xml.namespace.QName;

public class BuildERWithDeploymentTest extends AbstractTestCase {
    /**
     * @param testName
     */
    public BuildERWithDeploymentTest(String testName) {
        super(testName);
    }

    public void testDeployment() {
        try {
            String filename = AbstractTestCase.basedir + "/target/test-resources/deployment";
            AxisConfiguration er = ConfigurationContextFactory
                    .createConfigurationContextFromFileSystem(filename, filename + "/axis2.xml")
                    .getAxisConfiguration();

            assertNotNull(er);
            AxisService service = er.getService("service2");
            assertNotNull(service);
            //commentd since there is no service based messageReceivers
            /*MessageReceiver provider = service.getMessageReceiver();
          assertNotNull(provider);
          assertTrue(provider instanceof RawXMLINOutMessageReceiver);*/
            ClassLoader cl = service.getClassLoader();
            assertNotNull(cl);
            Loader.loadClass(cl, "org.apache.axis2.Echo2");
            assertNotNull(service.getName());
            assertNotNull(service.getParameter("para2"));

            AxisOperation op = service.getOperation(new QName("opname"));
            assertNotNull(op);
        } catch (Exception e) {
           fail(e.getMessage());
        }

    }
}
