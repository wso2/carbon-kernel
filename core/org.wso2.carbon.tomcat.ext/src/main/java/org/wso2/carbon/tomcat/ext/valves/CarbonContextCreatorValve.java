/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.tomcat.ext.valves;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.registry.api.RegistryService;
import org.wso2.carbon.registry.core.ghostregistry.GhostRegistry;
import org.wso2.carbon.tomcat.ext.internal.CarbonRealmServiceHolder;
import org.wso2.carbon.tomcat.ext.internal.Utils;
import org.wso2.carbon.tomcat.ext.utils.URLMappingHolder;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserRealmService;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletException;
import java.io.IOException;

//import org.wso2.carbon.utils.multitenancy.CarbonContextHolder;

/**
 * This valve handles creation of the CarbonContext when a request comes in
 */
@SuppressWarnings("unused")
public class CarbonContextCreatorValve extends ValveBase {
    private static Log log = LogFactory.getLog(CarbonContextCreatorValve.class);

    public CarbonContextCreatorValve() {
        //enable async support
        super(true);
    }

    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        try {
            initCarbonContext(request);
            getNext().invoke(request, response);
        } catch (Exception e) {
            log.error("Could not handle request: " + request.getRequestURI(), e);
        } finally {
            // This will destroy the carbon context holder on the current thread after
            // invoking subsequent valves.
            PrivilegedCarbonContext.destroyCurrentContext();
        }
    }

    public void initCarbonContext(Request request) throws Exception {
        String tenantDomain;
        String appName;
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        String requestedHostName = request.getHost().getName();
        String defaultHost = URLMappingHolder.getInstance().getDefaultHost();
        //checking for URLMapping request and set tenant domain and App name accordingly
        if(!requestedHostName.equalsIgnoreCase(defaultHost)) {
            tenantDomain = Utils.getTenantDomainFromURLMapping(request);
            appName = Utils.getAppNameForURLMapping(request);
        } else {
            //this will get executed with the default host requests
            tenantDomain = Utils.getTenantDomain(request);
            appName = Utils.getAppNameFromRequest(request);
        }
        carbonContext.setTenantDomain(tenantDomain);
        carbonContext.setApplicationName(appName);

        //String userName = (String) request.getSession().getAttribute(ServerConstants.USER_LOGGED_IN);
        //carbonContext.setUsername(userName);

        if (tenantDomain != null) {
        	UserRealmService userRealmService = CarbonRealmServiceHolder.getRealmService();
            TenantManager tenantManager = userRealmService.getTenantManager();
            int tenantId = tenantManager.getTenantId(tenantDomain);
            carbonContext.setTenantId(tenantId);
            //carbonContext.setUserRealm(userRealmService.getTenantUserRealm(tenantId));

            RegistryService registryService = CarbonRealmServiceHolder.getRegistryService();
            carbonContext.setRegistry( RegistryType.SYSTEM_CONFIGURATION,
                    new GhostRegistry(registryService, tenantId,
                            RegistryType.SYSTEM_CONFIGURATION));
            carbonContext.setRegistry(RegistryType.SYSTEM_GOVERNANCE,
                    new GhostRegistry(registryService, tenantId,
                            RegistryType.SYSTEM_GOVERNANCE));
        }
    }
}
