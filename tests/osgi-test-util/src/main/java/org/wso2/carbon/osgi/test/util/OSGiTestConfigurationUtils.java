/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.carbon.osgi.test.util;

import org.ops4j.pax.exam.Option;
import org.wso2.carbon.kernel.Constants;

import java.util.ArrayList;
import java.util.List;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.repositories;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;

/**
 * This class contains utility methods to create PAX Exam options required to boot up a Carbon Kernel with user defined
 * set of bundles and other options.
 *
 * @since 5.1.0
 */
public class OSGiTestConfigurationUtils {

    /**
     * Returns an array of PAX Exam configuration options which are required to boot up a PAX Exam OSGi environment
     * with Carbon Kernel.
     *
     * @param customOptions        {@code Option} list defined by the user. These options will be merged to
     *                                           default set of options.
     * @param sysPropConfiguration Contains system properties required to boot up Carbon Kernel.
     * @return a PAX Exam {@code Option} array.
     */
    public static List<Option> getConfiguration(List<Option> customOptions,
                                                CarbonSysPropConfiguration sysPropConfiguration) {
        List<Option> optionList = new ArrayList<>();
        optionList.addAll(getBaseOptions(sysPropConfiguration.getCarbonHome(), sysPropConfiguration.getServerKey(),
                sysPropConfiguration.getServerName(), sysPropConfiguration.getServerVersion()));
        optionList.addAll(customOptions);
        return optionList;
    }

    /**
     * Base PAX Exam options required to boot up Carbon Kernel.
     *
     * @param carbonHome    Value of the carbon.home system property required to boot up Carbon kernel.
     * @param serverKey     ID of the Carbon server. Default value is carbon-kernel.
     * @param serverName    Name of the Carbon server. Default value is WSO2 Carbon Kernel.
     * @param serverVersion Version of the Carbon server. Default Value is 5.0.0
     * @return a list of PAX Exam options required to boot up Carbon Kernel.
     */
    private static List<Option> getBaseOptions(String carbonHome,
                                               String serverKey,
                                               String serverName,
                                               String serverVersion) {

        List<Option> optionList = new ArrayList<>();

        //Setting carbon.home system property.
        optionList.add(systemProperty("carbon.home").value(carbonHome));

        //Setting required system properties
        optionList.add(systemProperty("server.key").value(serverKey != null ? serverKey : "carbon-kernel"));
        optionList.add(systemProperty("server.name").value(serverName != null ? serverName : "WSO2 Carbon Kernel"));
        optionList.add(systemProperty("server.version").value(serverVersion != null ? serverVersion : "5.0.0"));

        //Setting server start time.
        optionList.add(systemProperty(Constants.START_TIME).value(System.currentTimeMillis() + ""));

        //Adding the set of bundles required to bootup Carbon kernel.
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

        return optionList;
    }
}
