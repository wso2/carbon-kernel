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
package org.wso2.carbon.coordination.core.utils;

import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.coordination.common.CoordinationConstants;
import org.wso2.carbon.coordination.common.CoordinationException;
import org.wso2.carbon.coordination.core.CoordinationConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;

/**
 * Utility methods for Coordination service.
 */
public class CoordinationUtils {

	/**
	 * Loads the Coordination configuration.
	 * @return Created CoordinationConfiguration object
	 */
	public static CoordinationConfiguration loadCoordinationClientConfig(String path) throws CoordinationException {
		return new CoordinationConfiguration(path);
	}
	
	public static String createPathFromId(String context, String id) {
		int tenantId;
		try {
		    tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
		} catch (Throwable e) {
			/* when running tests */
			tenantId = MultitenantConstants.SUPER_TENANT_ID;
		}
		if (tenantId == MultitenantConstants.INVALID_TENANT_ID) {
			tenantId = MultitenantConstants.SUPER_TENANT_ID;
		}
		return CoordinationConstants.CONTENT_PATH_ROOT + "/" + tenantId + "/" + context + "/" + id; 		
	}
	
}
