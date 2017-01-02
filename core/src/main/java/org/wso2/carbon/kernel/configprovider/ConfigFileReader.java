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
package org.wso2.carbon.kernel.configprovider;


import java.util.Map;

/**
 * ConfigFileReader allows CarbonRuntime implementations to retrieve a Deployment Configuration map.
 * Configuration map can be populated from different sources. For an example, from a file or from a URL. This
 * class provides a way abstract out the different sources and provide a generic interface to the CarbonRuntime
 * implementers.
 *
 * @since 5.2.0
 */
public interface ConfigFileReader {

    /**
     * Returns a populated Deployment Configuration Map which overrides default configuration.
     *
     * @return a instance of the Configuration Map, key: String, value: YAML string
     * @throws CarbonConfigurationException if error occur while reading the configuration file.
     */
    public Map<String, String> getDeploymentConfiguration() throws CarbonConfigurationException;
}
