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
import org.apache.axis2.description.AxisModule;
import org.osgi.framework.Constants;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.BaseTest;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.mockito.Mockito.when;

/**
 * Test case to verify Axis2ModuleRegistry functionality.
 */
public class Axis2ModuleRegistryTest extends BaseTest {

    @Test(groups = "org.wso2.carbon.utils.deployment")
    public void testRegister() throws Exception {
        ConfigurationContext configurationContext = createTestConfigurationContext();
        Bundle bundle = Mockito.mock(Bundle.class);
        Axis2ModuleRegistry axis2ModuleRegistry = new Axis2ModuleRegistry(configurationContext.getAxisConfiguration());
        Field field = Axis2ModuleRegistry.class.getDeclaredField("moduleMap");
        field.setAccessible(true);
        Dictionary<String, String> headers = new Hashtable<>();
        String bundleSymbolicName = "org.wso2.carbon.utils.deployment";
        String bundleVersion = "1.0.0";
        headers.put(Constants.BUNDLE_SYMBOLICNAME, bundleSymbolicName);
        headers.put(Constants.BUNDLE_VERSION, bundleVersion);
        File modulesDirectory = Paths.get(testDir, "modules").toFile();
        Assert.assertTrue(modulesDirectory.exists() && modulesDirectory.isDirectory());
        Enumeration<URL> manifestEntries = new ManifestModuleIterator(getFileListFromGiveDirectory(modulesDirectory.
                getAbsolutePath()));
        when(bundle.getHeaders()).thenReturn(headers);
        when(bundle.findEntries("META-INF", "module.xml", true)).thenReturn(manifestEntries);
        axis2ModuleRegistry.register(bundle);
        Map<Bundle, AxisModule> moduleRegistryMap = (Map<Bundle, AxisModule>) field.get(axis2ModuleRegistry);
        Assert.assertTrue(moduleRegistryMap.size() > 0);

        axis2ModuleRegistry.unRegister(bundle);
        Assert.assertTrue(moduleRegistryMap.isEmpty());
    }

    class ManifestModuleIterator implements Enumeration<URL>,
                                                      Iterator<URL> {
        Enumeration<? extends String> urls;

        ManifestModuleIterator(List<String> moduleXmlFiles) {
            urls = Collections.enumeration(moduleXmlFiles);
        }

        public boolean hasNext() {
            return urls.hasMoreElements();
        }

        public URL next() {
            try {
                return new URL(urls.nextElement());
            } catch (MalformedURLException e) {
                throw new NoSuchElementException();
            }
        }

        @Override
        public void remove() {
            //ignore
        }

        public boolean hasMoreElements() {
            return hasNext();
        }

        public URL nextElement() {
            return next();
        }
    }

    List<String> getFileListFromGiveDirectory(String directory) {
        List<String> fileNames = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directory))) {
            for (Path path : directoryStream) {
                fileNames.add(path.toUri().toString());
            }
        } catch (IOException ex) {
            //ignore
        }
        return fileNames;
    }
}
