/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.core;

import org.apache.axis2.context.ConfigurationContext;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.utils.ConfigurationContextService;

//@Component(service = ConfigurationContextService.class)
public class ConfigurationContextServiceImpl implements ConfigurationContextService {
    private ConfigurationContext serverConfigContext;
    private ConfigurationContext clientConfigContext;

    public ConfigurationContextServiceImpl(ConfigurationContext serverConfigContext, ConfigurationContext clientConfigContext){
        this.serverConfigContext = serverConfigContext;
        this.clientConfigContext = clientConfigContext;
    }

    public ConfigurationContext getServerConfigContext() {
        return serverConfigContext;
    }

    public ConfigurationContext getClientConfigContext() {
        return clientConfigContext;
    }
}
