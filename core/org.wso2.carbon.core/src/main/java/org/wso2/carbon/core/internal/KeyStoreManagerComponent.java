/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.core.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.keystore.KeyStoreManagementException;
import org.wso2.carbon.core.keystore.KeyStoreManagementService;
import org.wso2.carbon.core.keystore.KeyStoreManagementServiceImpl;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.ServerConstants;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import static org.wso2.carbon.core.keystore.constants.KeyStoreConstants.KEYSTORE_DATASOURCE;

@Component(
        name = "security.mgt.service.component",
        immediate = true
)
public class KeyStoreManagerComponent {

    private static final Log log = LogFactory.getLog(KeyStoreManagerComponent.class);
    private static ConfigurationContextService configContextService = null;
    private static RealmService realmService;

    @Activate
    protected void activate(ComponentContext ctxt) {

        try {
            BundleContext bundleCtx = ctxt.getBundleContext();
            bundleCtx.registerService(KeyStoreManagementService.class.getName(), new KeyStoreManagementServiceImpl(),
                    null);
            initDataSource();
            log.debug("Security Mgt bundle is activated");
        } catch (KeyStoreManagementException e) {
            log.error("Failed to activate SecurityMgtServiceComponent", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {

        log.debug("Security Mgt bundle is deactivated");
    }

    public static ConfigurationContext getServerConfigurationContext() {

        return configContextService.getServerConfigContext();
    }

    @Reference(
            name = "config.context.service",
            service = ConfigurationContextService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationContextService"
    )
    protected void setConfigurationContextService(ConfigurationContextService contextService) {

        if (log.isDebugEnabled()) {
            log.debug("Setting the ConfigurationContext");
        }
        configContextService = contextService;
        KeyStoreManagerDataHolder.setConfigurationContextService(contextService);
    }

    @Reference(
            name = "user.realmservice.default",
            service = RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService"
    )
    protected void setRealmService(RealmService realmService) {

        if (log.isDebugEnabled()) {
            log.debug("Setting the RealmService");
        }
        this.realmService = realmService;
        KeyStoreManagerDataHolder.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {

        if (log.isDebugEnabled()) {
            log.debug("Unsetting the RealmService");
        }
        this.realmService = null;
        KeyStoreManagerDataHolder.setRealmService(null);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {

        if (log.isDebugEnabled()) {
            log.debug("Unsetting the ConfigurationContext");
        }
        this.configContextService = null;
        KeyStoreManagerDataHolder.setConfigurationContextService(contextService);
    }

    private void initDataSource() throws KeyStoreManagementException {

        String dataSourceName = ServerConfiguration.getInstance()
                .getFirstProperty(KEYSTORE_DATASOURCE);
        Context ctx = null;
        try {
            ctx = new InitialContext();
            DataSource dataSource = (DataSource) ctx.lookup(dataSourceName);
            KeyStoreManagerDataHolder.setDataSource(dataSource);
        } catch (NamingException e) {
            throw new KeyStoreManagementException(e.getMessage());
        }
    }
}
