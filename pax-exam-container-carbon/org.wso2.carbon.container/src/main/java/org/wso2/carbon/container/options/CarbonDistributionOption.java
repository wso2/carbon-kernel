package org.wso2.carbon.container.options;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;

import java.nio.file.Path;

/**
 * Utility class to provide an easy and intuitive way to configure the specific carbon distribution options.
 */
public class CarbonDistributionOption {

    /**
     * Hidden utility class constructor.
     */
    private CarbonDistributionOption() {
    }

    /**
     * Set the carbon distribution path option.
     * @return an option to set the path to distribution.
     */
    public static CarbonHomeOption carbonHome() {
        return new CarbonHomeOption();
    }

    /**
     * Copy a file from  one location to another location in the distribution..
     * @param sourcePath
     * @param destinationPath
     * @return carbon file copy option
     */
    public static Option carbonFileCopy(Path sourcePath, Path destinationPath) {
        return new CarbonFileCopyOption(sourcePath, destinationPath);
    }

    /**
     * Copy a maven bundle to the dropins directory.
     * @param mavenArtifactUrlReference
     * @return carbon dropins bundle option
     */
    public static Option carbonDropinsBundle(MavenArtifactUrlReference mavenArtifactUrlReference) {
        return new CarbonDropinsBundleOption(mavenArtifactUrlReference);
    }

    /**
     * Per default the folder pax-exam is deleting the test directories after a test is over.
     * To keep those directories (for later evaluation) simply set this option.
     *
     * @return keep runtime folder option
     */
    public static Option keepTestDistributionDirectory() {
        return new KeepTestDistributionDirectoryOption();
    }

    /**
     * Set the debug configuration to default port 5005.
     * @return debug configuration option
     */
    public static Option debugConfiguration() {
        return new DebugConfigurationOption();
    }

    /**
     * Set the debug configuration to the given port.
     * @param port
     * @return debug configuration option
     */
    public static Option debugConfiguration(int port) {
        return new DebugConfigurationOption(port);
    }

}
