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
package org.wso2.carbon.tools.converter;

import org.testng.annotations.Test;
import org.wso2.carbon.tools.TestConstants;
import org.wso2.carbon.tools.converter.utils.BundleGeneratorUtils;
import org.wso2.carbon.tools.exception.CarbonToolException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * A Java class which tests the listPackages method of BundleGeneratorUtils.java class.
 *
 * @since 5.0.0
 */
public class ListPackagesTest {
    private static final Path sampleJARFile = Paths.
            get(TestConstants.TARGET_FOLDER, "test-resources", "converter", "tool-test-artifact.jar");

    @Test(description = "Attempts to list the java packages defined within a Java Archive (JAR) file")
    public void testListingPackagesFromJar() throws IOException, CarbonToolException {
        List<Object> actual = new ArrayList<>();
        BundleGeneratorUtils.listPackages(sampleJARFile).forEach(actual::add);
        List<Object> expected = new ArrayList<>();
        expectedPackageList().forEach(expected::add);

        assert TestUtils.isMatching(expected, actual);
    }

    private List<String> expectedPackageList() {
        List<String> packages = new ArrayList<>();
        packages.add("org.wso2.carbon.test.implementation");
        packages.add("org.wso2.carbon.test.interfaces");

        return packages;
    }
}
