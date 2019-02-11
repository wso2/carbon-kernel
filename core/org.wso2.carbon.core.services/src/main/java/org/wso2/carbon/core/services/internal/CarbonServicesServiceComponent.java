/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.core.services.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.core.common.IFileDownload;
import org.wso2.carbon.core.common.IFileUpload;
import org.wso2.carbon.core.services.callback.LoginSubscriptionManagerService;
import org.wso2.carbon.core.services.callback.LoginSubscriptionManagerServiceImpl;
import org.wso2.carbon.core.services.filedownload.FileDownloadService;
import org.wso2.carbon.core.services.fileupload.FileUploadService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="core.services.dscomponent" immediate="true"
 * @scr.reference name="registry.service"
 *                interface="org.wso2.carbon.registry.core.service.RegistryService"
 *                cardinality="1..1" policy="dynamic" bind="setRegistryService"
 *                unbind="unsetRegistryService"
 * @scr.reference name="user.realmservice.default"
 *                interface="org.wso2.carbon.user.core.service.RealmService"
 *                cardinality="1..1" policy="dynamic" bind="setRealmService"
 *                unbind="unsetRealmService"
 * @scr.reference name="server.configuration"
 *                interface="org.wso2.carbon.base.api.ServerConfigurationService"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setServerConfiguration"
 *                unbind="unsetServerConfiguration"
 * @scr.reference name="config.context.service"
 *                interface="org.wso2.carbon.utils.ConfigurationContextService"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setConfigurationContextService"
 *                unbind="unsetConfigurationContextService"
 */
public class CarbonServicesServiceComponent {

    private static RealmService realmService;
    private static RegistryService registryService;
    private static ServerConfigurationService serverConfiguration;
    private static ConfigurationContextService configContextService;
    private static BundleContext bundleContext;

    private static LoginSubscriptionManagerServiceImpl loginSubscriptionManagerServiceImpl = new LoginSubscriptionManagerServiceImpl();

    private static final Log log = LogFactory.getLog(CarbonServicesServiceComponent.class);

    protected void activate(ComponentContext ctxt) {
        try {
            BundleContext bc = ctxt.getBundleContext();
            CarbonServicesServiceComponent.bundleContext = bc;
            bc.registerService(IFileUpload.class.getName(), new FileUploadService(), null);
            bc.registerService(IFileDownload.class.getName(), new FileDownloadService(), null);
            bc.registerService(LoginSubscriptionManagerService.class.getName(),
                    loginSubscriptionManagerServiceImpl, null);

            log.debug("Carbon Core Services bundle is activated ");
        } catch (Throwable e) {
            log.error("Failed to activate Carbon Core Services bundle ", e);
        }
    }

    protected void setServerConfiguration(ServerConfigurationService configuration) {
        serverConfiguration = configuration;
    }

    protected void unsetServerConfiguration(ServerConfigurationService configuration) {
        serverConfiguration = null;
    }

    protected void deactivate(ComponentContext ctxt) {
        CarbonServicesServiceComponent.bundleContext = null;
        log.debug("Carbon Core Services bundle is deactivated ");
    }

    protected void setRegistryService(RegistryService registryService) {
        CarbonServicesServiceComponent.registryService = registryService;
    }

    protected void unsetRegistryService(RegistryService registryService) {
       CarbonServicesServiceComponent.registryService = null;
    }

    protected void setRealmService(RealmService realmService) {
        CarbonServicesServiceComponent.realmService = realmService;
    }

    protected void unsetRealmService(RealmService realmService) {
        CarbonServicesServiceComponent.realmService = null;
    }

    public static BundleContext getBundleContext() throws Exception {
        if (bundleContext == null) {
            String msg = "System has not been started properly. Bundle Context is null.";
            log.error(msg);
            throw new Exception(msg);
        }
        return bundleContext;
    }

    public static RealmService getRealmService() throws Exception {
        if (realmService == null) {
            String msg = "System has not been started properly. Realm Service is null.";
            log.error(msg);
            throw new Exception(msg);
        }
        return realmService;
    }

    public static RegistryService getRegistryService() throws Exception {
        if (registryService == null) {
            String msg = "Before activating Carbon Services bundle, an instance of "
                    + "RegistryService should be in existance";
            log.error(msg);
            throw new Exception(msg);
        }
        return registryService;
    }

    public static LoginSubscriptionManagerServiceImpl getLoginSubscriptionManagerServiceImpl() {
        return loginSubscriptionManagerServiceImpl;
    }

    public static ServerConfigurationService getServerConfiguration() throws Exception {
        if (serverConfiguration == null) {
            String msg = "Server configuration is null. Some bundles in the system have not started";
            log.error(msg);
            throw new Exception(msg);
        }
        return serverConfiguration;
    }

    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        CarbonServicesServiceComponent.configContextService = contextService;
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        CarbonServicesServiceComponent.configContextService = null;
    }

    public static ConfigurationContextService getConfigurationContextService() throws Exception{
        if (serverConfiguration == null) {
            String msg = "Axis configuration is null. Some bundles in the system have not started";
            log.error(msg);
            throw new Exception(msg);
        }
        return CarbonServicesServiceComponent.configContextService;
    }

}
