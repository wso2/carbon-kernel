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


import org.wso2.carbon.configuration.component.exceptions.ConfigurationException;
import org.wso2.carbon.kernel.configprovider.utils.ConfigurationUtils;

import java.util.Map;

/**
 * This class takes care of parsing the deployment.yaml file and creating the deployment configuration table.
 *
 * @since 5.2.0
 */
public class YAMLBasedConfigFileReader extends AbstractConfigFileReader {

    public YAMLBasedConfigFileReader(String fileName) {
        super(fileName);
    }

    @Override
    public Map<String, String> getConfiguration() throws ConfigurationException {
        String yamlFileString = getFileContent();
        return ConfigurationUtils.getDeploymentConfigMap(yamlFileString);
    }
}
