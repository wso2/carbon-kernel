package org.wso2.carbon.container.runner;

import java.nio.file.Path;
import java.util.List;

/**
 * Runner interface to implement different ways of running the server.
 */
public interface Runner {

    /**
     * Start the server in a different JVM.
     * @param environment environment arguments of the starting process
     * @param carbonHome path to carbon home
     * @param options options to set in the command line
     */
    void exec(final String[] environment,  Path carbonHome, List<String> options);

    /**
     * Shutdown the runner.
     */
    void shutdown();

}
