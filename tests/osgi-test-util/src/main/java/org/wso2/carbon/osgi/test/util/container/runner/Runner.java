package org.wso2.carbon.osgi.test.util.container.runner;

import java.io.File;
import java.util.List;

public interface Runner {

    void exec(final String[] environment, final File carbonHome, final String javaHome, List<String> javaOpts);

    /**
     * Shutdown the runner.
     */
    void shutdown();

}
