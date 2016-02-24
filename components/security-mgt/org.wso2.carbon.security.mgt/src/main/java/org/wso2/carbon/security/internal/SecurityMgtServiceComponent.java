/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.security.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.security.SecurityServiceHolder;
import org.wso2.carbon.security.config.SecurityConfigAdmin;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="security.mgt.service.component" immediate="true"
 * @scr.reference name="registry.service"
 * interface=
 * "org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService"
 * unbind="unsetRegistryService"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"
 * unbind="unsetRealmService"
 * @scr.reference name="registry.loader.default"
 * interface="org.wso2.carbon.registry.core.service.TenantRegistryLoader"
 * cardinality="1..1" policy="dynamic" bind="setTenantRegistryLoader" unbind="unsetTenantRegistryLoader"
 */
public class SecurityMgtServiceComponent {
    private static String POX_SECURITY_MODULE = "POXSecurityModule";
    private static Log log = LogFactory.getLog(SecurityMgtServiceComponent.class);
    private static ConfigurationContextService configContextService = null;
    private static RealmService realmService;
    private static RegistryService registryService;

    public static ConfigurationContext getServerConfigurationContext() {
        return configContextService.getServerConfigContext();
    }

    protected void activate(ComponentContext ctxt) {
        try {
            ConfigurationContext mainConfigCtx = configContextService.getServerConfigContext();
            AxisConfiguration mainAxisConfig = mainConfigCtx.getAxisConfiguration();
            BundleContext bundleCtx = ctxt.getBundleContext();
            String enablePoxSecurity = ServerConfiguration.getInstance()
                    .getFirstProperty("EnablePoxSecurity");
            if (enablePoxSecurity == null || "true".equals(enablePoxSecurity)) {
                mainAxisConfig.engageModule(POX_SECURITY_MODULE);
            } else {
                log.info("POX Security Disabled");
            }

            bundleCtx.registerService(SecurityConfigAdmin.class.getName(),
                    new SecurityConfigAdmin(mainAxisConfig,
                            registryService.getConfigSystemRegistry(),
                            null),
                    null);
            bundleCtx.registerService(Axis2ConfigurationContextObserver.class.getName(),
                    new SecurityAxis2ConfigurationContextObserver(),
                    null);
            log.debug("Security Mgt bundle is activated");
        } catch (Throwable e) {
            log.error("Failed to activate SecurityMgtServiceComponent", e);
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        try {
            AxisConfiguration serverAxisConfig =
                    configContextService.getServerConfigContext().getAxisConfiguration();
            if (serverAxisConfig != null) {
                serverAxisConfig.disengageModule(serverAxisConfig.getModule(POX_SECURITY_MODULE));
            }
            log.debug("Security Mgt bundle is deactivated");
        } catch (Throwable e) {
            log.error("Failed to deactivate SecurityMgtServiceComponent", e);
        }
    }

    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the ConfigurationContext");
        }
        configContextService = contextService;
        SecurityServiceHolder.setConfigurationContextService(contextService);
    }

    protected void setRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the RegistryService");
        }
        this.registryService = registryService;
        SecurityServiceHolder.setRegistryService(registryService);
    }

    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the RealmService");
        }
        this.realmService = realmService;
        SecurityServiceHolder.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting the RealmService");
        }
        this.realmService = null;
        SecurityServiceHolder.setRealmService(null);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting the ConfigurationContext");
        }
        this.configContextService = null;
        SecurityServiceHolder.setConfigurationContextService(contextService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting the RegistryService");
        }
        this.registryService = registryService;
        SecurityServiceHolder.setRegistryService(registryService);  // TODO: Serious OSGi bug here. FIXME Thilina
    }

    protected void setTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        if (log.isDebugEnabled()) {
            log.debug("Tenant Registry Loader is set in the SAML SSO bundle");
        }
        SecurityServiceHolder.setTenantRegistryLoader(tenantRegistryLoader);
    }

    protected void unsetTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        if (log.isDebugEnabled()) {
            log.debug("Tenant Registry Loader is unset in the SAML SSO bundle");
        }
        SecurityServiceHolder.setTenantRegistryLoader(null);
    }

    public static RegistryService getRegistryService(){
        return registryService;
    }
}
