/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.utils.deployment;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisServiceGroup;
import org.osgi.framework.Constants;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

/**
 * Test case to verify Axis2ServiceRegistry functionality.
 */
public class Axis2ServiceRegistryTest extends Axis2ModuleRegistryTest {

    @Test(groups = "org.wso2.carbon.utils.deployment")
    public void testRegister() throws Exception {
        ConfigurationContext configurationContext = createTestConfigurationContext();
        String fqcn = EchoService.class.getName();
        Bundle bundle = Mockito.mock(Bundle.class);
        Axis2ServiceRegistry axis2ServiceRegistry = new Axis2ServiceRegistry(configurationContext);
        Field field = Axis2ServiceRegistry.class.getDeclaredField("serviceGroupMap");
        field.setAccessible(true);
        Dictionary<String, String> headers = new Hashtable<>();
        String bundleSymbolicName = "org.wso2.carbon.utils.deployment";
        String bundleVersion = "1.0.0";
        headers.put(Constants.BUNDLE_SYMBOLICNAME, bundleSymbolicName);
        headers.put(Constants.BUNDLE_VERSION, bundleVersion);
        File serviceDirectory = Paths.get(testDir, "services").toFile();
        File wsdlDirectory = Paths.get(testDir, "wsdls").toFile();
        Assert.assertTrue(serviceDirectory.exists() && serviceDirectory.isDirectory() &&
        wsdlDirectory.exists() && wsdlDirectory.isDirectory());
        Enumeration<URL> serviceEntries = new Axis2ModuleRegistryTest.
                ManifestModuleIterator(getFileListFromGiveDirectory(serviceDirectory.getAbsolutePath()));
        Enumeration<URL> wsdlEntries = new Axis2ModuleRegistryTest.
                ManifestModuleIterator(getFileListFromGiveDirectory(serviceDirectory.getAbsolutePath()));
        when(bundle.getHeaders()).thenReturn(headers);
        when(bundle.findEntries("META-INF", "*services.xml", true)).thenReturn(serviceEntries);
        when(bundle.findEntries("META-INF", "*.wsdl", true)).thenReturn(wsdlEntries);
        when(bundle.getBundleId()).thenReturn(1L);
        when(bundle.getLastModified()).thenReturn(System.currentTimeMillis());
        when(bundle.getLocation()).thenReturn(wsdlDirectory.toURI().toString());
        Class echoServiceClass = Axis2ServiceRegistryTest.class.getClassLoader().loadClass(fqcn);
        when(bundle.loadClass(fqcn)).thenReturn(echoServiceClass);
        axis2ServiceRegistry.register(bundle);
        Map<Bundle, List<AxisServiceGroup>> serviceRegistryMap =
                (Map<Bundle, List<AxisServiceGroup>>) field.get(axis2ServiceRegistry);
        Assert.assertTrue(serviceRegistryMap.size() > 0 && serviceRegistryMap.get(bundle).size() == 2);

        axis2ServiceRegistry.unregister(bundle);
        Assert.assertTrue(serviceRegistryMap.isEmpty());
    }
}
