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
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


/**
 * Parent class for all admin services
 *
 * Note: This has to be extended by Carbon admin services only. Such services should have the
 * "adminService" parameter.
 */
public abstract class AbstractAdmin {

    protected AxisConfiguration axisConfig;
    protected ConfigurationContext configurationContext;

    protected AbstractAdmin() {
        // Need permissions in order to instantiate AbstractAdmin
        CarbonUtils.checkSecurity();
    }

    protected AbstractAdmin(AxisConfiguration axisConfig) throws Exception {
        this();
        this.axisConfig = axisConfig;
    }

    protected AxisConfiguration getAxisConfig() {
        checkAdminService();
        return (axisConfig != null) ? axisConfig : getConfigContext().getAxisConfiguration();
    }

    protected ConfigurationContext getConfigContext() {
        checkAdminService();
        if(configurationContext != null) {
            return configurationContext;
        }
        MessageContext msgContext = MessageContext.getCurrentMessageContext();
        if (msgContext != null) {
            ConfigurationContext mainConfigContext = msgContext.getConfigurationContext();

            // If a tenant has been set, then try to get the ConfigurationContext of that tenant
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();

            String domain = carbonContext.getTenantDomain();
            if (domain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(domain)) {
                return  TenantAxisUtils.getTenantConfigurationContext(domain, mainConfigContext);
            } else if(carbonContext.getTenantId() == MultitenantConstants.SUPER_TENANT_ID) {
                return mainConfigContext;
            } else {
                throw new UnsupportedOperationException("Tenant domain unidentified. " +
                        "Upstream code needs to identify & set the tenant domain & tenant ID. " +
                        " The TenantDomain SOAP header could be set by the clients or " +
                        "tenant authentication should be carried out.");
            }
        } else {
            return CarbonConfigurationContextFactory.getConfigurationContext();
        }
    }

    protected String getTenantDomain(){
        checkAdminService();
        return CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
    }

    protected void setConfigurationContext(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
        this.axisConfig = configurationContext.getAxisConfiguration();
    }

    /**
     * @deprecated
     * @return config registry
     */
    protected Registry getRegistry() {
        return getConfigSystemRegistry();
    }

    protected Registry getConfigSystemRegistry() {
        return (Registry) PrivilegedCarbonContext.getThreadLocalCarbonContext().
                getRegistry(RegistryType.SYSTEM_CONFIGURATION);
    }

    protected Registry getConfigUserRegistry() {
        return (Registry) CarbonContext.getThreadLocalCarbonContext().getRegistry(
                RegistryType.USER_CONFIGURATION);
    }

    protected UserRealm getUserRealm() {
        return (UserRealm) CarbonContext.getThreadLocalCarbonContext().getUserRealm();
    }
    
    protected String getUsername() {
    	return (String) CarbonContext.getThreadLocalCarbonContext().getUsername();
    }

    protected Registry getLocalRepo() {
        return (Registry) PrivilegedCarbonContext.getThreadLocalCarbonContext().getRegistry(
                RegistryType.LOCAL_REPOSITORY);
    }

    /**
     * @deprecated Use either getGovernanceSystemRegistry or getGovernanceUserRegistry
     * @return Governance User Registry
     */
    @Deprecated protected Registry getGovernanceRegistry() {
        return getGovernanceUserRegistry();
    }

    protected Registry getGovernanceUserRegistry() {
        return (Registry) CarbonContext.getThreadLocalCarbonContext().getRegistry(
                RegistryType.USER_GOVERNANCE);
    }

    protected Registry getGovernanceSystemRegistry() {
        return (Registry) CarbonContext.getThreadLocalCarbonContext().getRegistry(
                RegistryType.SYSTEM_GOVERNANCE);
    }

    protected HttpSession getHttpSession() {
        checkAdminService();
        MessageContext msgCtx = MessageContext.getCurrentMessageContext();
        HttpSession httpSession = null;
        if (msgCtx != null) {
            HttpServletRequest request =
                    (HttpServletRequest) msgCtx.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
            httpSession = request.getSession();
        }
        return httpSession;
    }

    protected void setPermissionUpdateTimestamp() throws RegistryException {
        checkAdminService();
        Registry registry = getGovernanceSystemRegistry();
        Resource resource = registry.newResource();
        resource.setProperty("timestamp", Long.toString(System.currentTimeMillis()));
        registry.put("/repository/components/org.wso2.carbon.user.mgt/updatedTime", resource);
    }

    private void checkAdminService() {
        MessageContext msgCtx = MessageContext.getCurrentMessageContext();
        if (msgCtx == null) {
            return;
        }
        AxisService axisService = msgCtx.getAxisService();
        if (axisService.getParameter(CarbonConstants.ADMIN_SERVICE_PARAM_NAME) == null ){
            throw new RuntimeException("AbstractAdmin can only be extended by Carbon admin services. " +
                                        getClass().getName() + " is not an admin service. Service name " +
                                        axisService.getName() + ". The service should have defined the " +
                                       CarbonConstants.ADMIN_SERVICE_PARAM_NAME + " parameter");
        }
    }
}
