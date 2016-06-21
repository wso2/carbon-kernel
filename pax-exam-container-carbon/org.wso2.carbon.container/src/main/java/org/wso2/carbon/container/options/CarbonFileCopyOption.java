
package org.wso2.carbon.container.options;

import org.ops4j.lang.NullArgumentException;
import org.ops4j.pax.exam.Option;

import java.nio.file.Path;

/**
 * Copy a file from one location to another location inside the distribution directory.
 */
public class CarbonFileCopyOption implements Option {

    private Path sourcePath;
    private Path destinationPath;

    public CarbonFileCopyOption(Path sourcePath, Path destinationPath) {
        NullArgumentException.validateNotNull(sourcePath, "Source path");
        NullArgumentException.validateNotNull(destinationPath, "Destination path");
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
