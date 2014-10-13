/*
 * Copyright 2005-2014 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.core.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfigurator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.multitenancy.TenantAxisConfigurator;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

/**
 * This will call all registered deployers during the server start-up.
 *
 *  @since 4.3.0
 */
public class DeploymentAxis2ConfigurationContextObserver extends AbstractAxis2ConfigurationContextObserver {

    private static Log log = LogFactory.getLog(DeploymentAxis2ConfigurationContextObserver.class);

    @Override
    public void createdConfigurationContext(ConfigurationContext configContext) {
        log.debug("Invoke registered deployers");
        super.createdConfigurationContext(configContext);
        AxisConfigurator axisConfigurator = configContext.getAxisConfiguration().getConfigurator();
        if (axisConfigurator instanceof TenantAxisConfigurator) {
            ((TenantAxisConfigurator) axisConfigurator).deployServices();
        }
    }
}
