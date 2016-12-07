/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.kernel.internal.context;

import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.configprovider.CarbonConfigurationException;
import org.wso2.carbon.kernel.configprovider.ConfigProvider;

import java.util.Map;

/**
 * Custom implementation for CarbonConfigProvider to be used in unit test cases.
 *
 * @since 5.0.0
 */
public class CarbonConfigProviderImpl implements ConfigProvider {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CarbonConfigProviderImpl.class);

    @Override
    public <T> T getConfigurationObject(Class<T> configClass) throws CarbonConfigurationException {
        try {
            return configClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new CarbonConfigurationException("Error while creating configuration Instance : "
                    + configClass.getSimpleName(), e);
        }
    }

    @Override
    public Map getConfigurationMap(String namespace) throws CarbonConfigurationException {
        return null;
    }
}
