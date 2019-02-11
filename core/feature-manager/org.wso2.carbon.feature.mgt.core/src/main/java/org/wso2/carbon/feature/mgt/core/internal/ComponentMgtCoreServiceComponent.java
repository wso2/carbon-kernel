/*
 * Copyright 2009-2010 WSO2, Inc. (http://wso2.com)
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
package org.wso2.carbon.feature.mgt.core.internal;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.feature.mgt.core.ProvCommandProviderExt;
import org.wso2.carbon.feature.mgt.core.ProvisioningException;
import org.wso2.carbon.feature.mgt.core.util.RepositoryUtils;

/**
 * @scr.component name="component.manager.core.service.comp" immediate="true"
 * @scr.reference name="provisioning.agent.provider"
 * interface="org.eclipse.equinox.p2.core.IProvisioningAgentProvider"
 * cardinality="1..1" policy="dynamic" bind="setProvisioningAgentProvider"
 * unbind="unsetProvisioningAgentProvider"
 * @scr.reference name="server.config.service" interface="org.wso2.carbon.base.api.ServerConfigurationService"
 * cardinality="1..1" policy="dynamic"  bind="setServerConfigurationService" unbind="unsetServerConfigurationService"
 */
public class ComponentMgtCoreServiceComponent {

    protected void activate(ComponentContext ctxt) {
        ctxt.getBundleContext().registerService(CommandProvider.class.getName(), new ProvCommandProviderExt(), null);       
    }

    protected void setProvisioningAgentProvider(IProvisioningAgentProvider provisioningAgentProvider) {
        ServiceHolder.setProvisioningAgentProvider(provisioningAgentProvider);
    }

    protected void unsetProvisioningAgentProvider(IProvisioningAgentProvider provisioningAgentProvider) {
        ServiceHolder.setProvisioningAgentProvider(null);
    }
    
    protected void setServerConfigurationService(ServerConfigurationService serverConfigService) {
		ServiceHolder.setServerConfigurationService(serverConfigService);
	}

	protected void unsetServerConfigurationService(ServerConfigurationService serverConfigService) {
		ServiceHolder.setServerConfigurationService(null);
	}

}
