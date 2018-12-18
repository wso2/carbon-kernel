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
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.feature.mgt.core.ProvCommandProviderExt;
import org.wso2.carbon.feature.mgt.core.ProvisioningException;
import org.wso2.carbon.feature.mgt.core.util.RepositoryUtils;

@Component(name = "component.manager.core.service.comp", immediate = true)
public class ComponentMgtCoreServiceComponent {

    @Activate
    protected void activate(ComponentContext ctxt) {
        ctxt.getBundleContext().registerService(CommandProvider.class.getName(), new ProvCommandProviderExt(), null);       
    }
    
    @Reference(name = "provisioning.agent.provider", cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC, 
            unbind = "unsetProvisioningAgentProvider")
    protected void setProvisioningAgentProvider(IProvisioningAgentProvider provisioningAgentProvider) {
        ServiceHolder.setProvisioningAgentProvider(provisioningAgentProvider);
    }

    protected void unsetProvisioningAgentProvider(IProvisioningAgentProvider provisioningAgentProvider) {
        ServiceHolder.setProvisioningAgentProvider(null);
    }
    
    @Reference(name = "server.config.service", cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC, 
            unbind = "unsetServerConfigurationService")
    protected void setServerConfigurationService(ServerConfigurationService serverConfigService) {
		ServiceHolder.setServerConfigurationService(serverConfigService);
	}

	protected void unsetServerConfigurationService(ServerConfigurationService serverConfigService) {
		ServiceHolder.setServerConfigurationService(null);
	}

}
