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

/**
 * A Java class which defines constants used in JAR-to-OSGi converter tool test classes.
 *
 * @since 5.0.0
 */
public class TestConstants {
    public static final String TARGET_FOLDER = System.getProperty("target.directory");
    public static final String TEST_RESOURCES = "test-resources";
    public static final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");

    public static final String TEST_DIRECTORY_ONE = "testDirectoryOne";
    public static final String CHILD_TEST_DIRECTORY_ONE = "sampleOne";

    public static final String EQUINOX_OSGI_VERSION = System.getProperty("equinox.osgi.version");
    public static final String EQUINOX_SMP_CONFIGURATOR_VERSION = System.
            getProperty("equinox.simpleconfigurator.version");
    public static final String EQUINOX_UTIL_VERSION = System.getProperty("equinox.util.version");
    public static final String EQUINOX_LAUNCHER_VERSION = System.getProperty("equinox.launcher.version");
    public static final String KERNEL_VERSION = System.getProperty("carbon.kernel.version");

    public static final String ARTIFACT_ONE = "org.eclipse.osgi_" + EQUINOX_OSGI_VERSION + ".jar";
    public static final String ARTIFACT_TWO =
            "org.eclipse.equinox.simpleconfigurator_" + EQUINOX_SMP_CONFIGURATOR_VERSION + ".jar";
    public static final String ARTIFACT_THREE = "org.eclipse.equinox.util_" + EQUINOX_UTIL_VERSION + ".jar";
    public static final String ARTIFACT_FOUR = "org.eclipse.equinox.launcher_" + EQUINOX_LAUNCHER_VERSION + ".jar";
    public static final String ARTIFACT_FIVE = "tool-test-artifact-" + KERNEL_VERSION + ".jar";

    /**
     * A constructor which prevents instantiating the TestConstants class.
     */
    private TestConstants() {
    }
}
