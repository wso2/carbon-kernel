/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.osgi.test.util.container.options;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.MavenUrlReference;

import java.nio.file.Path;

public class CarbonDistributionConfigurationOption implements Option {

    private Path distributionFolderURL;
    private Path distributionZipURL;
    private MavenUrlReference distributionMavenURL;
    private String name;
    private Path unpackDirectory;

    public CarbonDistributionConfigurationOption() {
        distributionFolderURL = null;
        distributionMavenURL = null;
        name = null;
    }

    public CarbonDistributionConfigurationOption distributionZipURL(Path distributionZipURL) {
        this.distributionZipURL = distributionZipURL;
        return this;
    }

    public CarbonDistributionConfigurationOption distributionFolderURL(Path distributionFolderURL) {
        this.distributionFolderURL = distributionFolderURL;
        return this;
    }

    /**
     * Sets the URL of the frameworks as a maven reference.
     *
     * @param distributionURL framework URL
     * @return this for fluent syntax
     */
    public CarbonDistributionConfigurationOption distributionMavenURL(MavenUrlReference distributionURL) {
        distributionMavenURL = distributionURL;
        return this;
    }

    /**
     * Sets the name of the framework. This is only used for logging.
     *
     * @param name distribution name
     * @return this for fluent syntax
     */
    public CarbonDistributionConfigurationOption name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Define the unpack directory for the carbon distribution. In this directory a UUID named
     * directory will be created for each environment.
     *
     * @param unpackDirectory unpack directory
     * @return this for fluent syntax
     */
    public CarbonDistributionConfigurationOption unpackDirectory(Path unpackDirectory) {
        this.unpackDirectory = unpackDirectory;
        return this;
    }

    public MavenUrlReference getDistributionMavenURL() {
        return distributionMavenURL;
    }

    public Path getDistributionFolderURL() {
        return distributionFolderURL;
    }

    public Path getDistributionZipURL() {
        return distributionZipURL;
    }

    public String getName() {
        return name;
    }

    public Path getUnpackDirectory() {
        return unpackDirectory;
    }

}
