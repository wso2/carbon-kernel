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

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.options.MavenUrlReference;

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
     * Set the carbon distribution maven url option.
     *
     * @return an option to set the path to distribution.
     */
    public static CarbonDistributionBaseOption carbonDistribution(MavenUrlReference mavenUrlReference) {
        return new CarbonDistributionBaseOption().distributionMavenURL(mavenUrlReference);
    }

    /**
     * Set the carbon distribution path option.
     *
     * @return an option to set the path to distribution.
     */
    public static CarbonDistributionBaseOption carbonDistribution(Path path) {
        if (path.toString().endsWith("zip")) {
            return new CarbonDistributionBaseOption().distributionZipPath(path);
        } else {
            return new CarbonDistributionBaseOption().distributionDirectoryPath(path);
        }
    }

    /**
     * Copy a file from  one location to another location in the distribution..
     *
     * @param sourcePath      source path of the file
     * @param destinationPath destination path of the file in the distribution
     * @return carbon file copy option
     */
    public static Option copyFile(Path sourcePath, Path destinationPath) {
        return new CopyFileOption(sourcePath, destinationPath);
    }

    /**
     * Copy a maven bundle to the OSGi-lib directory.
     *
     * @param mavenArtifactUrlReference maven reference of the artifact
     * @return carbon OSGi-lib bundle option
     */
    public static Option copyOSGiLibBundle(MavenArtifactUrlReference mavenArtifactUrlReference) {
        return new CopyOSGiLibBundleOption(mavenArtifactUrlReference);
    }

    /**
     * Per default the folder pax-exam is deleting the test directories after a test is over.
     * To keep those directories (for later evaluation) simply set this option.
     *
     * @return keep runtime folder option
     */
    public static Option keepDirectory() {
        return new KeepDirectoryOption();
    }

    /**
     * Set the debug configuration to default port 5005.
     *
     * @return debug configuration option
     */
    public static Option debug() {
        return new DebugOption();
    }

    /**
     * Set the debug configuration to the given port.
     *
     * @param port port
     * @return debug configuration option
     */
    public static Option debug(int port) {
        return new DebugOption(port);
    }

}
