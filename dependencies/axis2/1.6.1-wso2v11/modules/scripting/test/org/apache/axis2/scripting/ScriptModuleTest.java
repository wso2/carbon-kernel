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
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class ScriptModuleTest extends TestCase {

//    public void testGetScriptForWSDL() throws MalformedURLException, URISyntaxException {
//        ScriptModule module = new ScriptModule();
//        URL wsdlURL = getClass().getClassLoader().getResource("org/apache/axis2/scripting/testrepo/test.wsdl");
//        URL scriptURL = module.getScriptForWSDL(wsdlURL);
//        assertTrue(scriptURL.toString().endsWith("test.js"));
//    }
//
//    public void testGetWSDLsInDir() throws MalformedURLException, URISyntaxException {
//        ScriptModule module = new ScriptModule();
//        URL wsdlURL = getClass().getClassLoader().getResource("org/apache/axis2/scripting/testrepo/test.wsdl");
//        URL scriptsDir = new File(wsdlURL.toURI()).getParentFile().toURL();
//        List wsdls = module.getWSDLsInDir(scriptsDir);
//        assertEquals(2, wsdls.size());
//        assertTrue(wsdls.get(0).toString().endsWith("test.wsdl"));
//    }
//
//    public void testReadScriptSource() throws AxisFault {
//        ScriptModule module = new ScriptModule();
//        URL url = getClass().getClassLoader().getResource("org/apache/axis2/scripting/testrepo/test.js");
//        String s = module.readScriptSource(url);
//        assertEquals("petra", s);
//    }

    public void testGetScriptServicesDirectory() throws AxisFault, MalformedURLException, URISyntaxException, UnsupportedEncodingException {
        ScriptModule module = new ScriptModule();
        AxisConfiguration axisConfig = new AxisConfiguration();
        URL url = getClass().getClassLoader().getResource("org/apache/axis2/scripting/testrepo/test.js");
        File dir = Utils.toFile(url).getParentFile();
        axisConfig.setRepository(dir.getParentFile().toURL());
        axisConfig.addParameter(new Parameter("scriptServicesDir", dir.getName()));
        assertEquals(dir.toURL(), module.getScriptServicesDirectory(axisConfig).toURL());
    }

//    public void testCreateService() throws AxisFault {
//        URL wsdlURL = getClass().getClassLoader().getResource("org/apache/axis2/scripting/testrepo/test.wsdl");
//        URL scriptURL = getClass().getClassLoader().getResource("org/apache/axis2/scripting/testrepo/test.js");
//        ScriptModule module = new ScriptModule();
//        AxisService axisService = module.createService(wsdlURL, scriptURL);
//        assertEquals("petra", axisService.getParameterValue(ScriptReceiver.SCRIPT_SRC_PROP));
//    }

    public void testInit() throws AxisFault, MalformedURLException, URISyntaxException, InterruptedException, UnsupportedEncodingException {
        ScriptModule module = new ScriptModule();
        AxisConfiguration axisConfig = new AxisConfiguration();
        URL url = getClass().getClassLoader().getResource("org/apache/axis2/scripting/testrepo/test.js");
        File dir = Utils.toFile(url).getParentFile();
        axisConfig.setRepository(dir.getParentFile().toURL());
        axisConfig.addParameter(new Parameter("scriptServicesDir", dir.getName()));
        ConfigurationContext configContext = new ConfigurationContext(axisConfig);

        module.init(configContext, null);
        
        Thread.sleep(500);
        
        assertNotNull(axisConfig.getService("test"));
    }

}
