/*
 * Copyright 2015 WSO2, Inc. http://www.wso2.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.osgi.config;

import org.ops4j.pax.exam.ConfigurationFactory;
import org.ops4j.pax.exam.Option;
import org.wso2.carbon.osgi.util.Utils;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.repositories;

/**
 * This class will provide the shared configurations for the OSGi tests
 *
 * In order to this file be affected, full qualified name of this class needs to be put into the
 * META-INF/services/org.ops4j.pax.exam.ConfigurationFactory resource file
 */
public class CarbonOSGiConfiguration implements ConfigurationFactory {

    //setting up the environment
    private void setup() {
        Utils.setCarbonHome();
    }

    @Override
    public Option[] createConfiguration() {

        setup();

        return options(
                repositories("http://maven.wso2.org/nexus/content/groups/wso2-public"),
                //must install the testng bundle
                mavenBundle().artifactId("testng").groupId("org.testng").versionAsInProject(),
                mavenBundle().artifactId("org.wso2.carbon.core").groupId("org.wso2.carbon").versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.equinox.simpleconfigurator").groupId("org.wso2.eclipse.equinox").
                        versionAsInProject(),
                mavenBundle().artifactId("org.apache.felix.gogo.command").groupId("org.apache.felix").
                        versionAsInProject(),
                mavenBundle().artifactId("org.apache.felix.gogo.runtime").groupId("org.apache.felix").
                        versionAsInProject(),
                mavenBundle().artifactId("org.apache.felix.gogo.shell").groupId("org.apache.felix").
                        versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.core.contenttype").groupId("org.wso2.eclipse.core").
                        versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.core.expressions").groupId("org.wso2.eclipse.core").
                        versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.core.jobs").groupId("org.wso2.eclipse.core").versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.core.runtime").groupId("org.wso2.eclipse.core").
                        versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.ecf").groupId("org.wso2.eclipse.ecf").versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.ecf.filetransfer").groupId("org.wso2.eclipse.ecf").
                        versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.ecf.identity").groupId("org.wso2.eclipse.ecf").
                        versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.ecf.provider.filetransfer").groupId("org.wso2.eclipse.ecf").
                        versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.equinox.app").groupId("org.wso2.eclipse.equinox").
                        versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.equinox.common").groupId("org.wso2.eclipse.equinox").
                        versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.equinox.concurrent").groupId("org.wso2.eclipse.equinox").
                        versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.equinox.console").groupId("org.wso2.eclipse.equinox").
                        versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.equinox.ds").groupId("org.wso2.eclipse.equinox").
                        versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.equinox.frameworkadmin").groupId("org.wso2.eclipse.equinox").
                        versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.equinox.frameworkadmin.equinox").
                        groupId("org.wso2.eclipse.equinox").versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.equinox.launcher").groupId("org.wso2.eclipse.equinox").
                        versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.equinox.preferences").groupId("org.wso2.eclipse.equinox").
                        versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.equinox.registry").groupId("org.wso2.eclipse.equinox").
                        versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.equinox.simpleconfigurator.manipulator").
                        groupId("org.wso2.eclipse.equinox").versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.equinox.util").groupId("org.wso2.eclipse.equinox").
                        versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.osgi.services").groupId("org.wso2.eclipse.osgi").
                        versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.equinox.cm").groupId("org.wso2.eclipse.equinox").
                        versionAsInProject(),
                mavenBundle().artifactId("pax-logging-api").groupId("org.ops4j.pax.logging").
                        versionAsInProject(),
                mavenBundle().artifactId("pax-logging-log4j2").groupId("org.ops4j.pax.logging").
                        versionAsInProject()
        );
    }
}
