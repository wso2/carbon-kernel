package org.wso2.carbon.container.options;

import org.ops4j.pax.exam.Option;

/**
 * Start the distribution in debug mode
 */
public class DebugConfigurationOption implements Option {

    private int port;

    /**
     * Activates debugging on the Carbon container using the standard 5005 port.
     */
    public DebugConfigurationOption() {
        port = 5005;
    }

    /**
     * Activates debugging on the Carbon container using the given port.
     *
     * @param port remote debugger port
     */
    public DebugConfigurationOption(int port) {
        this.port = port;
    }

    /**
     * @return the string contains the debug configuration
     */
    public String getDebugConfiguration() {
        return String.format("-debug %s", port);
    }

}
