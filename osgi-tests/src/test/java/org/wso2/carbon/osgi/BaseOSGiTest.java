/*
 * Copyright 2005,2015 WSO2, Inc. http://www.wso2.org
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

package org.wso2.carbon.osgi;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.osgi.util.Utils;

import javax.inject.Inject;

import static org.ops4j.pax.exam.CoreOptions.*;


@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class BaseOSGiTest {

    @Inject
    private BundleContext bundleContext;

    //setting up the environment
    private void setup() {
        Utils.setCarbonHome();
    }

    @Configuration
    public Option[] config() {
        setup();

        return options(
                repositories("http://maven.wso2.org/nexus/content/groups/wso2-public"),
                //must install the testng bundle
                mavenBundle().artifactId("testng").groupId("org.testng").versionAsInProject(),
                mavenBundle().artifactId("org.wso2.carbon.core").groupId("org.wso2.carbon").versionAsInProject(),
                mavenBundle().artifactId("hazelcast").groupId("org.wso2.orbit.com.hazelcast").versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.equinox.simpleconfigurator").groupId("org.eclipse.equinox").versionAsInProject(),
                mavenBundle().artifactId("javax.servlet-api").groupId("javax.servlet").versionAsInProject(),
                mavenBundle().artifactId("org.apache.felix.gogo.command").groupId("org.apache.felix").versionAsInProject(),
                mavenBundle().artifactId("org.apache.felix.gogo.runtime").groupId("org.apache.felix").versionAsInProject(),
                mavenBundle().artifactId("org.apache.felix.gogo.shell").groupId("org.apache.felix").versionAsInProject(),
                mavenBundle().artifactId("org.apache.felix.gogo.shell").groupId("org.apache.felix").versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.core.contenttype").groupId("org.eclipse.core").versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.core.contenttype").groupId("org.eclipse.core").versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.core.expressions").groupId("org.eclipse.core").versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.core.jobs").groupId("org.eclipse.core").versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.core.runtime").groupId("org.eclipse.core").versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.ecf").groupId("org.eclipse.ecf").versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.ecf.filetransfer").groupId("org.eclipse.ecf").versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.ecf.identity").groupId("org.eclipse.ecf").versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.ecf.provider.filetransfer").groupId("org.eclipse.ecf").versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.equinox.app").groupId("org.eclipse.equinox").versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.equinox.common").groupId("org.eclipse.equinox").versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.equinox.concurrent").groupId("org.eclipse.equinox").versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.equinox.console").groupId("org.eclipse.equinox").versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.equinox.ds").groupId("org.eclipse.equinox").versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.equinox.frameworkadmin").groupId("org.eclipse.equinox").versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.equinox.frameworkadmin.equinox").groupId("org.eclipse.equinox").versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.equinox.http.servlet").groupId("org.eclipse.equinox").versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.equinox.launcher").groupId("org.eclipse.equinox").versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.equinox.preferences").groupId("org.eclipse.equinox").versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.equinox.registry").groupId("org.eclipse.equinox").versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.equinox.simpleconfigurator.manipulator").groupId("org.eclipse.equinox").versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.equinox.util").groupId("org.eclipse.equinox").versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.osgi.services").groupId("org.eclipse.osgi").versionAsInProject()
        );
    }

    @Test
    public void testBundleContextStatus() {
        Assert.assertNotNull(bundleContext, "Bundle Context is null");
    }

    @Test
    public void testCarbonCoreBundleStatus() {

        Bundle coreBundle = null;

        Bundle bundles[] = bundleContext.getBundles();
        for (int i = 0; i < bundles.length; i++) {
            if (bundles[i].getSymbolicName().equals("org.wso2.carbon.core")) {
                coreBundle = bundles[i];
                break;
            }
        }
        Assert.assertNotNull(coreBundle, "Carbon Core bundle not found");
        Assert.assertEquals(coreBundle.getState(), Bundle.ACTIVE, "Carbon Core Bundle is not activated");
    }
}
