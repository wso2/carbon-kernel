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
package org.wso2.carbon.kernel;

import org.wso2.carbon.kernel.config.model.CarbonConfiguration;

/**
 * CarbonRuntime represents the complete server space. In previous Carbon kernel versions, we used the term
 * “Super Tenant” for this space. But most of us believe that this term is bit confusing. Its more clearer
 * if we call it the “Server space”. Because its actually the server space. There are separate tenant spaces.
 * All the applications deployed in the server space are privileged applications which are capable of managing
 * the Carbon runtime as well tenants.
 * <p>
 * CarbonRuntime allows you to retrieve CarbonConfiguration instance, TenantRuntime instance etc.
 *
 * @since 5.0.0
 */
public interface CarbonRuntime {

    /**
     * Returns the CarbonConfiguration instance. It holds static configuration items specified
     * in the carbon.yml file.
     *
     * @return the carbon configuration
     * @see CarbonConfiguration
     */
    public CarbonConfiguration getConfiguration();

}
