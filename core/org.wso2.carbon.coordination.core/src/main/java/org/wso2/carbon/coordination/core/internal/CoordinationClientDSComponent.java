/**
 *  Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.coordination.core.internal;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.carbon.coordination.core.services.CoordinationService;
import org.wso2.carbon.coordination.core.services.impl.ZKCoordinationService;
import org.wso2.carbon.utils.CarbonUtils;

@Component(name = "coordination.client.component", immediate = true)
public class CoordinationClientDSComponent {
	
	private Log log = LogFactory.getLog(CoordinationClientDSComponent.class);
	
	private CoordinationService service = null;
	
	private CoordinationService getService() {
		return service;
	}
	
	@Activate
	protected void activate(ComponentContext ctx) {
		if (log.isDebugEnabled()) {
			log.debug("Starting Coordination component initialization..");
		}
		try {
			if (this.getService() == null) {
				String configPath = CarbonUtils.getCarbonConfigDirPath() + File.separator + 
						"etc" + File.separator + "coordination-client-config.xml";
				this.service = new ZKCoordinationService(configPath);
			}
			BundleContext bundleContext = ctx.getBundleContext();
			bundleContext.registerService(CoordinationService.class.getName(), 
					this.getService(), null);
			if (log.isDebugEnabled()) {
				log.debug("Coordination component initialized");
			}
		} catch (Throwable e) {
			log.error("Eror in initializing Coordination component: " + e.getMessage(), e);
		}
	}
	
	@Deactivate
	protected void deactivate(ComponentContext ctx) {
    	if (log.isDebugEnabled()) {
			log.debug("Coordination component deactivation start..");
		}
    	try {
			this.getService().close();
			if (log.isDebugEnabled()) {
				log.debug("Coordination component deactivated");
			}
		} catch (Exception e) {
			log.error("Eror in deactivating Coordination component: " + e.getMessage(), e);
		}
    }
	
}
