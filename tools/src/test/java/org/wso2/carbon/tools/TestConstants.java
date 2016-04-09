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

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A Java class which defines constants used in test classes.
 *
 * @since 5.0.0
 */
public class TestConstants {
    protected static final String TEST_DIRECTORY_ONE = "testDirectoryOne";
    protected static final String TEST_DIRECTORY_TWO = "testDirectoryTwo";
    protected static final String CHILD_TEST_FILE_ONE = "sampleOne.txt";
    protected static final String CHILD_TEST_FILE_TWO = "sampleTwo.txt";
    protected static final String CHILD_TEST_DIRECTORY_ONE = "sampleOne";
    protected static final String TARGET_FOLDER = "target";

    protected static final String BUNDLE_SYMBOLIC_NAME = "Bundle-SymbolicName";
    protected static final String EXPORT_PACKAGE = "Export-Package";
    protected static final String IMPORT_PACKAGE = "DynamicImport-Package";

    protected static final Path SAMPLE_JAR_FILE = Paths
                .get(TARGET_FOLDER, "test-resources", "tool-test-artifact.jar");

    /**
     * A constructor which prevents instantiating the TestConstants class.
     */
    private TestConstants() {
    }
}
