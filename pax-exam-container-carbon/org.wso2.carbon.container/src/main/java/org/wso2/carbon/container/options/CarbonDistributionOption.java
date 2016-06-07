package org.wso2.carbon.container.options;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.extra.VMOption;

import static java.lang.String.format;

/**
 * Final class to provide an intuitive way to configure the specific carbon distribution
 * options.
 */
public final class CarbonDistributionOption {

    /**
     * Hidden utility class constructor
     */
    private CarbonDistributionOption() {
    }

    /**
     * Per default the Directory pax-exam is deleting the test directories after a test is over. To keep those
     * directories (for later evaluation) simply set this option.
     * 
     * @return keep runtime Directory option
     */
    public static Option keepRuntimeDirectory() {
        return new KeepRuntimeDirectory();
    }

    /**
     * Configures which distribution options to use.
     *
     * @return option
     */
    public static CarbonDistributionConfigurationOption CarbonDistributionConfiguration() {
        return new CarbonDistributionConfigurationOption();
    }

    /**
     * Activates debugging on the Carbon container using the standard 5005 port.
     *
     * @return option
     */
    public static Option debugConfiguration() {
        return debugConfiguration("5005");
    }

    /**
     * Returns an option to activate and configure remote debugging for the Carbon container.
     * 
     * @param port
     *            remote debugger port
     * @return option
     */
    public static Option debugConfiguration(String port) {
        return new VMOption(format("-debug %s", port));
    }
}
