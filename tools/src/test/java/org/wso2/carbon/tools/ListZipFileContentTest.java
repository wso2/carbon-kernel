/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.tools;

import org.testng.annotations.Test;
import org.wso2.carbon.tools.exceptions.JarToBundleConverterException;
import org.wso2.carbon.tools.utils.BundleGeneratorUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * A Java class which tests the listZipFileContent method of BundleGeneratorUtils.java class.
 *
 * @since 5.0.0
 */
public class ListZipFileContentTest {

    @Test public void listZipFileContentOfExistingFileTest() throws IOException, JarToBundleConverterException {
        Path jarFilePath = Paths.get("target", "test-resources", "tool-test-artifact-5.0.0-SNAPSHOT.jar");
        List<String> expectedPaths = expectedPaths();
        List<Path> actualPaths = BundleGeneratorUtils.listZipFileContent(jarFilePath);
        assert isMatching(expectedPaths, actualPaths);
    }

    @Test(expectedExceptions = { IOException.class,
            JarToBundleConverterException.class }) public void listZipFileContentOfTextFileTest()
            throws IOException, JarToBundleConverterException {
        Path textFilePath = TestUtils.loadResourceFile(ListZipFileContentTest.class,
                Paths.get("test-artifact", "sample.txt").toString());
        if (!Files.exists(textFilePath)) {
            Files.createFile(textFilePath);
        }
        BundleGeneratorUtils.listZipFileContent(textFilePath);
        Files.deleteIfExists(textFilePath);
    }

    @Test(expectedExceptions = { IOException.class,
            JarToBundleConverterException.class }) public void listZipFileContentOfNonExistingFileTest()
            throws IOException, JarToBundleConverterException {
        BundleGeneratorUtils.listZipFileContent(Paths.get(System.getProperty("java.io.tmpdir"), "temp.zip"));
    }

    @Test(expectedExceptions = { IOException.class,
            JarToBundleConverterException.class }) public void listZipFileContentOfDirectoryTest()
            throws IOException, JarToBundleConverterException {
        BundleGeneratorUtils.listZipFileContent(Paths.get(System.getProperty("java.io.tmpdir")));
    }

    private static boolean isMatching(List<String> expectedAbsolutePaths, List<Path> actual) {
        if ((expectedAbsolutePaths != null) && (actual != null)) {
            if (expectedAbsolutePaths.size() == actual.size()) {
                for (Path actualPath : actual) {
                    boolean exists = false;
                    for (String expectedPath : expectedAbsolutePaths) {
                        if (expectedPath.equals(actualPath.toAbsolutePath().toString())) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        return false;
                    }
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    private List<String> expectedPaths() {
        List<String> paths = new ArrayList<>();

        paths.add(File.separator);
        String metaInfDirectory = File.separator + "META-INF" + File.separator;
        paths.add(metaInfDirectory);
        paths.add(metaInfDirectory + "maven" + File.separator);
        paths.add(metaInfDirectory + "maven" + File.separator + "org.wso2.carbon" + File.separator);

        String metaInfSubDirectory =
                metaInfDirectory + "maven" + File.separator + "org.wso2.carbon" + File.separator + "tool-test-artifact"
                        + File.separator;
        paths.add(metaInfSubDirectory);
        paths.add(metaInfSubDirectory + "pom.properties");
        paths.add(metaInfSubDirectory + "pom.xml");

        paths.add(metaInfDirectory + "DEPENDENCIES");
        paths.add(metaInfDirectory + "LICENSE");
        paths.add(metaInfDirectory + "MANIFEST.MF");
        paths.add(metaInfDirectory + "NOTICE");

        paths.add(File.separator + "org" + File.separator);
        paths.add(File.separator + "org" + File.separator + "wso2" + File.separator);
        paths.add(File.separator + "org" + File.separator + "wso2" + File.separator + "carbon" + File.separator);

        String rootPackage =
                File.separator + "org" + File.separator + "wso2" + File.separator + "carbon" + File.separator + "test"
                        + File.separator;
        paths.add(rootPackage);
        paths.add(rootPackage + "implementation" + File.separator);
        paths.add(rootPackage + "implementation" + File.separator + "HelloWorld.class");
        paths.add(rootPackage + "interfaces" + File.separator);
        paths.add(rootPackage + "interfaces" + File.separator + "Greeting.class");

        return paths;
    }

}
