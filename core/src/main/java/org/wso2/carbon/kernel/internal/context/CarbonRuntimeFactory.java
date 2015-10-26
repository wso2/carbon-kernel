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

import org.wso2.carbon.kernel.CarbonRuntime;
import org.wso2.carbon.kernel.PrivilegedCarbonRuntime;
import org.wso2.carbon.kernel.config.CarbonConfigProvider;
import org.wso2.carbon.kernel.config.model.CarbonConfiguration;

/**
 * This is a factory class which creates a DefaultCarbonRuntime using a provided CarbonConfigProvider,
 * i.e the carbon.xml configuration model.
 *
 * @since 5.0.0
 */
public class CarbonRuntimeFactory {

    public static CarbonRuntime createCarbonRuntime(CarbonConfigProvider carbonConfigProvider) throws Exception {

        //TODO Remove hardcoded implementations.
        CarbonConfiguration carbonConfiguration = carbonConfigProvider.getCarbonConfiguration();

        PrivilegedCarbonRuntime carbonRuntime = new DefaultCarbonRuntime();
        carbonRuntime.setCarbonConfiguration(carbonConfiguration);
        return carbonRuntime;
    }
}
