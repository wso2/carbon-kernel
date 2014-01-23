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

package org.apache.axis2.scripting;

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.engine.AxisConfiguration;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class ScriptDeploymentEngineTest extends TestCase {
    
    public void test1() throws URISyntaxException, InterruptedException, MalformedURLException, AxisFault, UnsupportedEncodingException {
        AxisConfiguration axisConfig = new AxisConfiguration();
        ScriptDeploymentEngine sde = new ScriptDeploymentEngine(axisConfig);
        URL testScript = getClass().getClassLoader().getResource("org/apache/axis2/scripting/testrepo/test.js");
        File scriptsDir = Utils.toFile(testScript).getParentFile();
        sde.loadRepository(scriptsDir);
        sde.loadServices();
        assertNotNull(axisConfig.getService("test"));
    }

}
