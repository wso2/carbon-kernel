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

import java.io.File;

public class CarbonDistributionConfigurationOption implements Option {

    private String distributionURL;
    private MavenUrlReference distributionMavenURL;
    private String name;
    private File unpackDirectory;

    public CarbonDistributionConfigurationOption() {
        distributionURL = null;
        distributionMavenURL = null;
        name = null;
    }

    public CarbonDistributionConfigurationOption(File unpackDirectory, String name, String distributionURL) {
        this.unpackDirectory = unpackDirectory;
        this.name = name;
        this.distributionMavenURL = null;
        this.distributionURL = distributionURL;
    }

    public CarbonDistributionConfigurationOption(File unpackDirectory, String name,
            MavenUrlReference distributionMavenURL) {
        this.unpackDirectory = unpackDirectory;
        this.name = name;
        this.distributionMavenURL = distributionMavenURL;
        this.distributionURL = null;
    }

    public CarbonDistributionConfigurationOption distributionURL(String distributionURL) {
        this.distributionURL = distributionURL;
        return this;
    }

    /**
     * Sets the URL of the distribution as a maven reference.
     *
     * @param distributionURL distribution URL
     * @return this for fluent syntax
     */
    public CarbonDistributionConfigurationOption distributionMavenURL(MavenUrlReference distributionURL) {
        distributionMavenURL = distributionURL;
        return this;
    }

    /**
     * Sets the name of the distribution. This is only used for logging.
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
    public CarbonDistributionConfigurationOption unpackDirectory(File unpackDirectory) {
        this.unpackDirectory = unpackDirectory;
        return this;
    }

    public MavenUrlReference getDistributionMavenURL() {
        return distributionMavenURL;
    }

    public String getDistributionURL() {
        return distributionURL;
    }

    public String getName() {
        return name;
    }

    public File getUnpackDirectory() {
        return unpackDirectory;
    }

}
