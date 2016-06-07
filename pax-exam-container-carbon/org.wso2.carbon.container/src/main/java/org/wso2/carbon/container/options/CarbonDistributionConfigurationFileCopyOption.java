
package org.wso2.carbon.container.options;

import org.ops4j.pax.exam.Option;

import java.nio.file.Path;

public class CarbonDistributionConfigurationFileCopyOption implements Option {

    Path sourcePath;
    Path destinationPath;

    public CarbonDistributionConfigurationFileCopyOption(Path sourcePath, Path destinationPath) {
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
