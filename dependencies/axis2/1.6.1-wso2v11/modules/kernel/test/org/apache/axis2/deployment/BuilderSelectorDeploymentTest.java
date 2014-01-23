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
import org.apache.axis2.AxisFault;

import java.io.File;

public class BuilderSelectorDeploymentTest extends AbstractTestCase {
    /**
     * Constructor.
     */
    public BuilderSelectorDeploymentTest(String testName) {
        super(testName);
    }

    public void testBuilderSelection() throws AxisFault {
//        String repositoryName  =System.getProperty("basedir",".")+"/"+"target/test-resources/deployment";
//        File repo = new File(repositoryName);
//        String xmlFile = System.getProperty("basedir",".")+"/"+"test-resources/deployment/builderSelectorTest/axis2.xml";
//        File xml = new File(xmlFile);
//        FileSystemConfigurator fsc = new FileSystemConfigurator(repo.getAbsolutePath(), xml.getAbsolutePath());
//        AxisConfiguration axisConfig = fsc.getAxisConfiguration();
//        String className = axisConfig.getMessageBuilder("text/xml").getClass().getName();
//        assertEquals(className,"org.apache.axiom.om.impl.builder.StAXOMBuilder");
//        className = axisConfig.getMessageBuilder("application/soap+xml").getClass().getName();
//        assertEquals(className,"org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder");
    }

    public void testBuilderSelectionInvalidEntry() throws AxisFault {
        String repositoryName =
                System.getProperty("basedir", ".") + "/" + "target/test-resources/deployment";
        File repo = new File(repositoryName);
        String xmlFile = System.getProperty("basedir", ".") + "/" +
                "test-resources/deployment/builderSelectorTest/bad-axis2.xml";
        File xml = new File(xmlFile);
        FileSystemConfigurator fsc =
                new FileSystemConfigurator(repo.getAbsolutePath(), xml.getAbsolutePath());
        AxisFault testFault = null;
        try {
            fsc.getAxisConfiguration();
        } catch (AxisFault e) {
            testFault = e;
        }
        assertNotNull(testFault);
    }
}
