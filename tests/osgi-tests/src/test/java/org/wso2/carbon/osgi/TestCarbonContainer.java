package org.wso2.carbon.osgi;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;
import org.wso2.carbon.osgi.test.util.container.CarbonContainerFactory;
import org.wso2.carbon.osgi.test.util.container.options.CarbonDistributionOption;
import org.wso2.carbon.sample.transport.mgt.TransportManager;

import javax.inject.Inject;
import java.io.File;
import java.util.Arrays;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.repository;
import static org.wso2.carbon.osgi.test.util.container.options.CarbonDistributionOption.CarbonDistributionConfiguration;
import static org.wso2.carbon.osgi.test.util.container.options.CarbonDistributionOption.keepRuntimeFolder;

@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@ExamFactory(CarbonContainerFactory.class)
public class TestCarbonContainer {

    private static final Logger logger = LoggerFactory.getLogger(TestCarbonContainer.class);

    @Inject
    protected BundleContext bundleContext;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Inject
    TransportManager transportManager;

    @Configuration
    public Option[] config() {

//                return new Option[] { repository("http://maven.wso2.org/nexus/content/groups/wso2-public"),
//                        CarbonDistributionConfiguration().distributionMavenURL(
//                                maven().groupId("org.wso2.carbon")
//                                        .artifactId("wso2carbon-kernel")
//                                        .type("zip")
//                                        .version("5.0.0"))
//                                .unpackDirectory(new File("target/pax")),
//                        //                CarbonDistributionOption.debugConfiguration("5005")
//                };


//        return new Option[] {
//                repository("http://maven.wso2.org/nexus/content/groups/wso2-public"),
//                CarbonDistributionConfiguration().distributionURL("target/wso2carbon-kernel-5.1.0-SNAPSHOT"),
//                keepRuntimeFolder(),
//                //                CarbonDistributionOption.debugConfiguration("5005")
//        };

        return new Option[] {
                repository("http://maven.wso2.org/nexus/content/groups/wso2-public"),
                CarbonDistributionConfiguration().distributionURL("target/wso2carbon-kernel-5.1.0-SNAPSHOT"),
                keepRuntimeFolder(),
                mavenBundle().artifactId("org.wso2.carbon.sample.transport.mgt").groupId("org.wso2.carbon")
                        .versionAsInProject(),
                mavenBundle().artifactId("org.wso2.carbon.sample.transport.http").groupId("org.wso2.carbon")
                        .versionAsInProject(),
                mavenBundle().artifactId("org.wso2.carbon.sample.transport.custom").groupId("org.wso2.carbon")
                        .versionAsInProject(),
                mavenBundle().artifactId("org.wso2.carbon.sample.transport.jms").groupId("org.wso2.carbon")
                        .versionAsInProject(),
                mavenBundle().artifactId("org.wso2.carbon.sample.order.resolver").groupId("org.wso2.carbon")
                        .versionAsInProject(),
//                CarbonDistributionOption.debugConfiguration("5005")
        };

//        return new Option[] {
//                repository("http://maven.wso2.org/nexus/content/groups/wso2-public"),
//                CarbonDistributionConfiguration().distributionURL("target/wso2carbon-kernel-5.1.0-SNAPSHOT"),
//                keepRuntimeFolder(),
//                mavenBundle()
//                        .artifactId("carbon-context-test-artifact")
//                        .groupId("org.wso2.carbon")
//                        .versionAsInProject()
//                //                CarbonDistributionOption.debugConfiguration("5005")
//        };
    }

    @Test
    public void testBundles() {
        Arrays.asList(bundleContext.getBundles()).forEach((bundle -> logger.info(bundle.toString())));
    }

    @Test
    public void testCarbonCoreBundleStatus() {
        Bundle coreBundle = null;
        for (Bundle bundle : bundleContext.getBundles()) {
            if (bundle.getSymbolicName().equals("org.wso2.carbon.core")) {
                coreBundle = bundle;
                break;
            }
        }
        Assert.assertNotNull(coreBundle, "Carbon Core bundle not found");
        Assert.assertEquals(coreBundle.getState(), Bundle.ACTIVE, "Carbon Core Bundle is not activated");
    }

}
