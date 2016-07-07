package samples;

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
import org.wso2.carbon.container.CarbonContainerFactory;
import org.wso2.carbon.container.options.CarbonDistributionOption;
import org.wso2.carbon.container.options.CopyDropinsBundleOption;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;

import javax.inject.Inject;

import static org.ops4j.pax.exam.CoreOptions.maven;

@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@ExamFactory(CarbonContainerFactory.class)
public class Sample5 {
    private static final Logger logger = LoggerFactory.getLogger(Sample5.class);

    @Inject
    protected BundleContext bundleContext;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Configuration
    public Option[] config() {
        return new Option[] { CarbonDistributionOption.carbonDistribution(
                maven().groupId("org.wso2.carbon").artifactId("wso2carbon-kernel-test").type("zip")
                        .version("5.2.0-SNAPSHOT")),
                new CopyDropinsBundleOption(
                maven().artifactId("org.wso2.carbon.sample.deployer.mgt").groupId("org.wso2.carbon")
                        .versionAsInProject()),
                new CopyDropinsBundleOption(
                        maven().artifactId("org.wso2.carbon.sample.dbs.deployer").groupId("org.wso2.carbon")
                                .versionAsInProject()),
                new CopyDropinsBundleOption(
                        maven().artifactId("org.wso2.carbon.sample.order.resolver").groupId("org.wso2.carbon")
                                .versionAsInProject())
                , };
    }

    //    @Test
    //    public void testBundles() {
    //        logger.info(bundleContext.getBundle().getSymbolicName());
    //        logger.info(System.getProperty("carbon.home"));
    //        Arrays.asList(bundleContext.getBundles()).forEach(bundle -> logger.info(bundle.getSymbolicName()));
    //    }

    @Test
    public void testCarbonCoreBundleStatus() {
        logger.info("Sample 5");
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
