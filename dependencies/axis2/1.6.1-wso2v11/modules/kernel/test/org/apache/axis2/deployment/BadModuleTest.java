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
import org.apache.axis2.engine.AxisConfiguration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class BadModuleTest extends AbstractTestCase {
    /**
     * Constructor.
     */
    public BadModuleTest(String testName) {
        super(testName);
    }

    public void testBadModuleXML() {
        try {
            InputStream in = new FileInputStream(
                    getTestResourceFile("deployment/Badmodule.xml"));
            AxisConfiguration glabl = new AxisConfiguration();
            AxisConfigBuilder builder = new AxisConfigBuilder(in, glabl, null);
            builder.populateConfig();
            fail(
                    "this must failed gracefully with DeploymentException or FileNotFoundException");
        } catch (FileNotFoundException e) {
            return;
        } catch (DeploymentException e) {
            return;
        } catch (Exception e) {
            return;
        }

    }
}
