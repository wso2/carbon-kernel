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
package org.wso2.carbon.axis2.runtime;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.axis2.runtime.internal.DataHolder;

/**
 * Implementation of Axis2ServiceManager
 *
 * @since 1.0.0
 */
public class Axis2ServiceManagerImpl implements Axis2ServiceManager {
    private static final Logger logger = LoggerFactory.getLogger(Axis2ServiceManagerImpl.class);

    @Override
    public AxisService registerService(Class clazz) {
        ConfigurationContext configurationContext = DataHolder.getInstance().getConfigurationContext();
        if (configurationContext != null) {
            try {
                AxisService axisService = AxisService.createService(clazz.getName(),
                        configurationContext.getAxisConfiguration());
                configurationContext.deployService(axisService);
                return axisService;
            } catch (AxisFault axisFault) {
                logger.error("Failed to create an Axis2 service with given class '{}'", clazz.getName());
            }
        }
        return null;
    }

    @Override
    public AxisService unregisterService(AxisService axisService) {
        //TODO: Unregister service group + service form the Configuration Context
        return null;
    }
}
