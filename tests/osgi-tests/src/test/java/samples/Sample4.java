package samples;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.container.CarbonContainerFactory;
import org.wso2.carbon.container.options.EnvironmentPropertyOption;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;

import javax.inject.Inject;
import java.nio.file.Paths;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.wso2.carbon.container.options.CarbonDistributionOption.CarbonDistributionConfiguration;

@Listeners(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(CarbonContainerFactory.class)
public class Sample4 {

    protected static final String COVERAGE_COMMAND = "coverage.command";
    private static final Logger logger = LoggerFactory.getLogger(Sample4.class);

    @Inject
    protected BundleContext bundleContext;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Configuration
    public Option[] config() {
        return new Option[] { CarbonDistributionConfiguration().distributionMavenURL(
                maven().groupId("org.wso2.carbon").artifactId("wso2carbon-kernel-test").type("zip")
                        .versionAsInProject()).unpackDirectory(Paths.get("target", "pax")),
                addCoverageOption(),
        };
    }

    private EnvironmentPropertyOption addCoverageOption() {
        String coverageCommand = System.getProperty(COVERAGE_COMMAND);
        if (coverageCommand != null) {
            return new EnvironmentPropertyOption("JAVA_OPTS=" + coverageCommand);
        } else {
            return null;
        }
    }

        @Test
        public void testBundles4() {
            logger.info("Sample 4-1");
//            logger.info(bundleContext.getBundle().getSymbolicName());
//            logger.info(System.getProperty("carbon.home"));
//            Arrays.asList(bundleContext.getBundles()).forEach(bundle -> logger.info(bundle.getSymbolicName()));
        }

    @Test
    public void testCarbonCoreBundleStatus() {
        logger.info("Sample 4");
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
