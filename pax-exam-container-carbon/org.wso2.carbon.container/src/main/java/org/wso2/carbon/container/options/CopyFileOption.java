/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.container.options;

import org.ops4j.lang.NullArgumentException;
import org.ops4j.pax.exam.Option;

import java.nio.file.Path;

/**
 * Copy a file from one location to another location inside the distribution directory.
 */
public class CopyFileOption implements Option {

    private Path sourcePath;
    private Path destinationPath;

    public CopyFileOption(Path sourcePath, Path destinationPath) {
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
