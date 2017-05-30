/**
 *  Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.ndatasource.core;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

/**
 * This class represents a configuration context observer, used to load the data sources, 
 * when a new tenant arrives.
 */
public class DataSourceAxis2ConfigurationContextObserver extends
		AbstractAxis2ConfigurationContextObserver {

	private static final Log log = LogFactory.getLog(
			DataSourceAxis2ConfigurationContextObserver.class);

	@Override
	public void createdConfigurationContext(ConfigurationContext configContext) {
		int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
		try {
			DataSourceManager.getInstance().initTenant(tenantId);
		} catch (DataSourceException e) {
			log.error("Error in initializing data sources for tenant: " + 
		            tenantId + " - " + e.getMessage(), e);
		}
	}
	
	public void terminatingConfigurationContext(ConfigurationContext configContext) {
		int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
		try {
			DataSourceManager.getInstance().unloadTenant(tenantId);
		} catch (DataSourceException e) {
			log.error("Error in initializing data sources for tenant: " + 
		            tenantId + " - " + e.getMessage(), e);
		}
	}
	
}
