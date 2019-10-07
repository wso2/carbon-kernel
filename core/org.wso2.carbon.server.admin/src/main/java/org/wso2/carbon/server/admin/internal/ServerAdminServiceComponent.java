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
package org.wso2.carbon.server.admin.internal;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.core.services.authentication.BasicAccessAuthenticator;
import org.wso2.carbon.core.services.authentication.CookieAuthenticator;
import org.wso2.carbon.core.services.authentication.ServerAuthenticator;
import org.wso2.carbon.registry.core.dataaccess.DataAccessManager;
import org.wso2.carbon.registry.core.jdbc.dataaccess.JDBCDataAccessManager;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.server.admin.auth.AuthenticatorServerRegistry;
import org.wso2.carbon.server.admin.common.IServerAdmin;
import org.wso2.carbon.server.admin.service.ServerAdmin;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.MBeanRegistrar;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Hashtable;

@Component(name = "serveradmin.service.component", immediate = true)
public class ServerAdminServiceComponent {

    private static final Log log = LogFactory.getLog(ServerAdminServiceComponent.class);

    private boolean registeredMBeans;
    public static final String SERVER_ADMIN_MODULE_NAME = "ServerAdminModule";
    private ConfigurationContext configContext;
    private ServerAdminDataHolder dataHolder = ServerAdminDataHolder.getInstance();

    @Activate
    protected void activate(ComponentContext ctxt) {
        try {
            dataHolder.
                    setRestartThreadContextClassloader(Thread.currentThread().getContextClassLoader());
            //Register ServerAdmin MBean
            registerMBeans(dataHolder.getServerConfig());
            // Engaging ServerAdmin as an global module
            configContext.getAxisConfiguration().engageModule(SERVER_ADMIN_MODULE_NAME);

            //setUserManagerDriver(userRealmDefault);
            setRegistryDriver(dataHolder.getRegistryService());
            

            // Registering new Authenticator. This change should be backward compatible as
            // isAuthenticated method handles the logic as same in AuthenticationAdmin.

            BasicAccessAuthenticator basicAccessAuthenticator = new BasicAccessAuthenticator();
            Hashtable<String, String> basicAuthProps = new Hashtable<String, String>();
            basicAuthProps.put(AuthenticatorServerRegistry.AUTHENTICATOR_TYPE,
                    basicAccessAuthenticator.getAuthenticatorName());

            ctxt.getBundleContext().registerService(ServerAuthenticator.class.getName(), basicAccessAuthenticator,
                    basicAuthProps);

            // Registering cookie authenticator
            CookieAuthenticator cookieAuthenticator = new CookieAuthenticator();
            Hashtable<String, String> cookieAuthProps = new Hashtable<String, String>();
            basicAuthProps.put(AuthenticatorServerRegistry.AUTHENTICATOR_TYPE,
                    cookieAuthenticator.getAuthenticatorName());

            ctxt.getBundleContext().registerService(ServerAuthenticator.class.getName(), cookieAuthenticator,
                    cookieAuthProps);


            AuthenticatorServerRegistry.init(ctxt.getBundleContext());

            ctxt.getBundleContext().registerService(IServerAdmin.class.getName(),
                    new ServerAdmin(),
                    null);
            ctxt.getBundleContext().registerService(CommandProvider.class.getName(),
                    new ServerAdminCommandProvider(),
                    null);

            log.debug("ServerAdmin bundle is activated");
        } catch (Throwable e) {
            log.error("Failed to activate ServerAdmin bundle", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {
        log.debug("ServerAdmin bundle is deactivated");
    }

    private void registerMBeans(ServerConfigurationService serverConfig) {
            if (registeredMBeans) {
                return;
            }
            MBeanRegistrar.registerMBean(new ServerAdmin());
            registeredMBeans = true;        
    }

    private void setRegistryDriver(RegistryService registry) {
        try {
            if (registry.getConfigSystemRegistry().getRegistryContext() != null &&
                registry.getConfigSystemRegistry().getRegistryContext().getDataAccessManager()
                        != null) {
                DataAccessManager dataAccessManager =
                        registry.getConfigSystemRegistry().getRegistryContext()
                                .getDataAccessManager();
                if (!(dataAccessManager instanceof JDBCDataAccessManager)) {
                    String msg = "Failed to obtain DB connection. Invalid data access manager.";
                    log.error(msg);
                }
                Connection dbConnection = null;
                try {
                    DataSource dataSource = ((JDBCDataAccessManager)dataAccessManager).getDataSource();
                    dbConnection = dataSource.getConnection();
                    dataHolder.setRegistryDBDriver(dbConnection.getMetaData().getDriverName());
                } finally {
                    if (dbConnection != null) {
                        dbConnection.close();
                    }
                }
            }
        } catch (Exception e) {
            String msg = "Cannot get registry driver";
            log.error(msg, e);
        }
    }

    @Reference(name = "registry.service", cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC, 
            unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {
        dataHolder.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        dataHolder.setRegistryService(null);
        dataHolder.setRegistryDBDriver(null);
    }

    @Reference(name = "user.realmservice.default", cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC, 
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {
        dataHolder.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        dataHolder.setRealmService(null);
        dataHolder.setUserManagerDBDriver(null);
    }

    @Reference(name = "config.context.service", cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC, 
            unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        this.configContext = contextService.getServerConfigContext();
        dataHolder.setConfigContext(contextService.getServerConfigContext());
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        AxisConfiguration axisConf = configContext.getAxisConfiguration();
        if (axisConf != null) {
            AxisModule statModule = axisConf.getModule(ServerAdminServiceComponent.SERVER_ADMIN_MODULE_NAME);
            if (statModule != null) {
                try {
                    axisConf.disengageModule(statModule);
                } catch (AxisFault axisFault) {
                    log.error("Failed disengage module: " + ServerAdminServiceComponent.SERVER_ADMIN_MODULE_NAME);
                }
            }
        }
        this.configContext = null;
        dataHolder.setConfigContext(null);
    }

    @Reference(name = "server.configuration", cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC, 
            unbind = "unsetServerConfigurationService")
    protected void setServerConfigurationService(ServerConfigurationService serverConfiguration) {
        dataHolder.setServerConfig(serverConfiguration);
    }

    protected void unsetServerConfigurationService(ServerConfigurationService serverConfiguration) {
        dataHolder.setServerConfig(null);
    }
}
