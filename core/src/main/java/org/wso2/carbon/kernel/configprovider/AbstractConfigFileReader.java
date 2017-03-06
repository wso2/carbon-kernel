/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.kernel.configprovider;

import org.wso2.carbon.configuration.component.exceptions.ConfigurationException;
import org.wso2.carbon.configuration.component.reader.ConfigFileReader;
import org.wso2.carbon.kernel.configprovider.utils.ConfigurationUtils;

import java.nio.file.Path;
import java.util.Map;

/**
 * Abstract configuration file reader.
 */
public abstract class AbstractConfigFileReader extends ConfigFileReader<Map<String, String>> {

    public AbstractConfigFileReader(String fileName) {
        super(fileName);
    }

    @Override
    public Path getConfigurationFileLocation() throws ConfigurationException {
        return ConfigurationUtils.getConfigurationFileLocation(fileName);
    }
}
