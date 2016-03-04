/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.osgi.testing.utils;

import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.Constants;
import org.wso2.carbon.osgi.testing.CarbonOSGiTestEnvConfigs;

import java.util.ArrayList;
import java.util.List;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.repositories;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;

/**
 * This class contains Utility methods to configure PAX-EXAM container.
 *
 * @since 5.0.0
 */
public class CarbonOSGiTestUtils {

    private static final Logger logger = LoggerFactory.getLogger(CarbonOSGiTestUtils.class);


    /**
     * Setup the test environment.
     * <p>
     * Includes setting the carbon.home system property, setting the required system properties,
     * setting the maven local repo directory, etc.
     */
    private static void setupOSGiTestEnvironment(CarbonOSGiTestEnvConfigs configs, List<Option> optionList) {
        setCarbonHome(configs, optionList);
        setRequiredSystemProperties(configs, optionList);
        setStartupTime(configs, optionList);
//        copyCarbonYAML();
//        copyLog4jXMLFile();
    }

    /**
     * Sets default PAX-EXAM options.
     *
     */
    private static void setDefaultPaxOptions(CarbonOSGiTestEnvConfigs configs, List<Option> optionList) {
        optionList.add(repositories("http://maven.wso2.org/nexus/content/groups/wso2-public"));
        optionList.add(mavenBundle().artifactId("testng").groupId("org.testng").versionAsInProject());
        optionList.add(mavenBundle().artifactId("org.eclipse.osgi.services").groupId("org.wso2.eclipse.osgi")
                .versionAsInProject());
        optionList.add(mavenBundle().artifactId("pax-logging-api").groupId("org.ops4j.pax.logging")
                .versionAsInProject());
        optionList.add(mavenBundle().artifactId("pax-logging-log4j2").groupId("org.ops4j.pax.logging")
                .versionAsInProject());
        optionList.add(mavenBundle().artifactId("org.eclipse.equinox.simpleconfigurator")
                .groupId("org.wso2.eclipse.equinox").versionAsInProject());
        optionList.add(mavenBundle().artifactId("org.apache.felix.gogo.command").groupId("org.apache.felix")
                .versionAsInProject());
        optionList.add(mavenBundle().artifactId("org.apache.felix.gogo.runtime").groupId("org.apache.felix")
                .versionAsInProject());
        optionList.add(mavenBundle().artifactId("org.apache.felix.gogo.shell").groupId("org.apache.felix")
                .versionAsInProject());
        optionList.add(mavenBundle().artifactId("org.eclipse.equinox.app").groupId("org.wso2.eclipse.equinox")
                .versionAsInProject());
        optionList.add(mavenBundle().artifactId("org.eclipse.equinox.common").groupId("org.wso2.eclipse.equinox")
                .versionAsInProject());
        optionList.add(mavenBundle().artifactId("org.eclipse.equinox.concurrent").groupId("org.wso2.eclipse.equinox")
                .versionAsInProject());
        optionList.add(mavenBundle().artifactId("org.eclipse.equinox.console").groupId("org.wso2.eclipse.equinox")
                .versionAsInProject());
        optionList.add(mavenBundle().artifactId("org.eclipse.equinox.ds").groupId("org.wso2.eclipse.equinox")
                .versionAsInProject());
        optionList.add(mavenBundle().artifactId("org.eclipse.equinox.frameworkadmin")
                .groupId("org.wso2.eclipse.equinox").versionAsInProject());
        optionList.add(mavenBundle().artifactId("org.eclipse.equinox.frameworkadmin.equinox")
                .groupId("org.wso2.eclipse.equinox").versionAsInProject());
        optionList.add(mavenBundle().artifactId("org.eclipse.equinox.launcher").groupId("org.wso2.eclipse.equinox")
                .versionAsInProject());
        optionList.add(mavenBundle().artifactId("org.eclipse.equinox.preferences").groupId("org.wso2.eclipse.equinox")
                .versionAsInProject());
        optionList.add(mavenBundle().artifactId("org.eclipse.equinox.registry").groupId("org.wso2.eclipse.equinox")
                .versionAsInProject());
        optionList.add(mavenBundle().artifactId("org.eclipse.equinox.simpleconfigurator.manipulator")
                .groupId("org.wso2.eclipse.equinox").versionAsInProject());
        optionList.add(mavenBundle().artifactId("org.eclipse.equinox.util").groupId("org.wso2.eclipse.equinox")
                .versionAsInProject());
        optionList.add(mavenBundle().artifactId("org.eclipse.equinox.cm").groupId("org.wso2.eclipse.equinox")
                .versionAsInProject());
        optionList.add(mavenBundle().artifactId("snakeyaml").groupId("org.wso2.orbit.org.yaml")
                .versionAsInProject());
        optionList.add(mavenBundle().artifactId("org.wso2.carbon.core").groupId("org.wso2.carbon")
                .versionAsInProject());
    }

    /**
     * Set the carbon home for execute tests.
     * Carbon home is set to the value specified in the CarbonOSGiTestEnvConfigs
     */
    private static void setCarbonHome(CarbonOSGiTestEnvConfigs configs, List<Option> optionList) {
        optionList.add(systemProperty("carbon.home").value(configs.getCarbonHome()));
    }

    private static void setRequiredSystemProperties(CarbonOSGiTestEnvConfigs configs, List<Option> optionList) {
        optionList.add(systemProperty("server.key").value(configs.getServerKey()));
        optionList.add(systemProperty("server.name").value(configs.getServerName()));
        optionList.add(systemProperty("server.version").value(configs.getServerVersion()));
    }

    /**
     * Set the startup time to calculate the server startup time.
     */
    private static void setStartupTime(CarbonOSGiTestEnvConfigs configs, List<Option> optionList) {
        if (systemProperty(Constants.START_TIME).getValue() == null) {
            optionList.add(systemProperty(Constants.START_TIME).value(System.currentTimeMillis() + ""));
        }
    }

    public static Option[] getAllPaxOptions(CarbonOSGiTestEnvConfigs configs, List<Option> customOptions) {
        List<Option> allOptions = new ArrayList<>();
        CarbonOSGiTestUtils.setupOSGiTestEnvironment(configs, allOptions);
        CarbonOSGiTestUtils.setDefaultPaxOptions(configs, allOptions);
        allOptions.addAll(customOptions);

        Option[] options = CoreOptions.options(
                allOptions.toArray(new Option[allOptions.size()])
        );
        return options;
    }
}
