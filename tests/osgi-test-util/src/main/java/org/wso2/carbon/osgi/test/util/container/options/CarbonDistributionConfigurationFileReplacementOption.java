
package org.wso2.carbon.osgi.test.util.container.options;

import org.ops4j.pax.exam.Option;

import java.nio.file.Path;

public class CarbonDistributionConfigurationFileReplacementOption implements Option {

    Path sourcePath;
    Path destinationPath;

    public CarbonDistributionConfigurationFileReplacementOption(Path sourcePath, Path destinationPath) {
        this.sourcePath = sourcePath;
        this.destinationPath = destinationPath;
    }

    public Path getSourcePath() {
        return sourcePath;
    }

    public Path getDestinationPath() {
        return destinationPath;
    }
}
