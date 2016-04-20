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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * A Java class which tests the listFiles method of BundleGeneratorUtils.java class.
 *
 * @since 5.0.0
 */
public class ListFilesTest {
    private static final Path directory = Paths.get(TestConstants.TEST_DIRECTORY_ONE);

    static {
        TestUtils.createDirectoryWithChildren(directory);
    }

    @Test(description = "Attempts to list the content of the specified directory")
    public void testListingFiles() throws IOException {
        List<Object> expected = new ArrayList<>();
        expectedPaths().forEach(expected::add);
        List<Object> actual = new ArrayList<>();
        BundleGeneratorUtils.listFiles(directory).forEach(actual::add);
        assert TestUtils.isMatching(expected, actual);
    }

    private List<Path> expectedPaths() {
        List<Path> paths = new ArrayList<>();
        paths.addAll(TestUtils.getChildPaths(directory));

        return paths;
    }
}
