package org.wso2.carbon.osgi.test.util.container.runner;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public interface Runner {

    void exec(final String[] environment,  Path carbonHome, List<String> javaOpts);

    /**
     * Shutdown the runner.
     */
    void shutdown();

}
