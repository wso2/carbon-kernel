package org.wso2.carbon.osgi.testcontainer;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.container.CarbonContainerFactory;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;

import javax.inject.Inject;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.wso2.carbon.container.options.CarbonDistributionOption.copyOSGiLibBundle;

/**
 * To test pax exam container option copyOSGiLibBundle.
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@ExamFactory(CarbonContainerFactory.class)
public class CopyOSGiLibTest {

    @Inject
    protected BundleContext bundleContext;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Configuration
    public Option[] config() {
        return new Option[] {
                copyOSGiLibBundle(
                        maven().artifactId("carbon-context-test-artifact").groupId("org.wso2.carbon")
                                .versionAsInProject()) };
    }

    @Test
    public void testArtifactBundle() {
        Bundle testArtifact = null;
        for (Bundle bundle : bundleContext.getBundles()) {
            if (bundle.getSymbolicName().equals("carbon-context-test-artifact")) {
                testArtifact = bundle;
                break;
            }
        }
        Assert.assertNotNull(testArtifact, "Test artifact bundle not found");
        Assert.assertEquals(testArtifact.getState(), Bundle.ACTIVE, "Test artifact Bundle is not activated");
    }

}
