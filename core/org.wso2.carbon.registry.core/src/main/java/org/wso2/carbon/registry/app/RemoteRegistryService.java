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
package org.wso2.carbon.registry.app;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.core.jdbc.EmbeddedRegistryService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.net.MalformedURLException;

/**
 * This is a core class used by application that use registry in the remote mode. This class is used
 * to create remote registry instances for user sessions. The class acts in a manner that is similar
 * to an {@link EmbeddedRegistryService}.
 */
public class RemoteRegistryService implements RegistryService {

    private static final Log log = LogFactory.getLog(RemoteRegistryService.class);
    private Registry registry;
    private RealmService realmService;
    private String chroot;
    private String url;

    /**
     * Creates a new remote registry service. This method is intended to be used at the remote end.
     *
     * @param registryURL URL to the registry.
     * @param username    the user name.
     * @param password    the password.
     *
     * @throws RegistryException if an error occurred.
     */
    @SuppressWarnings("unused")
    public RemoteRegistryService(String registryURL, String username, String password)
            throws RegistryException {
        this(registryURL, username, password, null);
    }

    /**
     * Creates a new remote registry service. This method is intended to be used at the remote end.
     *
     * @param registryURL the registry url.
     * @param username    the user name.
     * @param password    the password.
     * @param chroot      the chroot.
     *
     * @throws RegistryException if an error occurred.
     */
    public RemoteRegistryService(String registryURL, String username, String password,
                                 String chroot) throws RegistryException {
        this(registryURL, username, password, null, chroot, false);
    }

    /**
     * Creates a new remote registry service. This method is intended to be used at the local end.
     *
     * @param registryURL  the registry url.
     * @param username     the user name.
     * @param password     the password.
     * @param realmService the OSGi user realm service.
     * @param chroot       the chroot.
     *
     * @throws RegistryException if an error occurred.
     */
    public RemoteRegistryService(String registryURL, String username,
                                 String password, RealmService realmService, String chroot)
            throws RegistryException {
        this(registryURL, username, password, realmService, chroot, true);
    }

    /**
     * Creates a new remote registry service.
     *
     * @param registryURL           the registry url.
     * @param username              the user name.
     * @param password              the password.
     * @param realmService          the OSGi user realm service.
     * @param chroot                the chroot.
     * @param populateConfiguration whether the configuration must be populated or not.
     *
     * @throws RegistryException if an error occurred.
     */
    public RemoteRegistryService(String registryURL, String username, String password,
                                 RealmService realmService, String chroot,
                                 boolean populateConfiguration) throws RegistryException {
        try {
            RegistryContext.getBaseInstance(realmService, populateConfiguration);
            this.url = registryURL;
            this.realmService = realmService;
            this.chroot = chroot;

            registry = new RemoteRegistry(url, username, password);

            //Hack to authenticate the user with the remote registry as remote registry
            //doesn't provide a way to log in
            registry.get("/");

            if (realmService != null) {
                RegistryUtils.getBootstrapRealm(realmService);
            }

            Registry systemRegistry = getSystemRegistry();
            RegistryUtils.addMountCollection(systemRegistry);

        } catch (MalformedURLException e) {
            log.fatal("Registry URL is malformed, Registry configuration must be invalid", e);
            throw new RegistryException("URL is malformed", e);
        } catch (Exception e) {
            log.fatal("Error initializing the remote registry, Registry " +
                    "configuration must be invalid", e);
            throw new RegistryException("Error initializing the remote registry", e);
        }
    }

    public UserRegistry getUserRegistry() throws RegistryException {
        return getUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME,
                MultitenantConstants.SUPER_TENANT_ID);
    }

    public UserRegistry getSystemRegistry() throws RegistryException {
        return getSystemRegistry(MultitenantConstants.SUPER_TENANT_ID);
    }

    public UserRegistry getSystemRegistry(int tenantId) throws RegistryException {
        return getSystemRegistry(tenantId, null);
    }


    public UserRegistry getSystemRegistry(int tenantId, String chroot) throws RegistryException {
        String username = CarbonConstants.REGISTRY_SYSTEM_USERNAME;

        return getUserRegistry(username, tenantId, chroot);
    }

    public UserRegistry getUserRegistry(String username, String password) throws RegistryException {
        return getUserRegistry(username, password, MultitenantConstants.SUPER_TENANT_ID);
    }

    public UserRegistry getUserRegistry(String username, String password, int tenantId)
            throws RegistryException {
        return getUserRegistry(username, password, tenantId, null);
    }

    public UserRegistry getUserRegistry(String username, String password, int tenantId,
                                        String chroot)
            throws RegistryException {
        try {

            RemoteRegistry userRemote = new RemoteRegistry(url, username, password);
            return new UserRegistry(username, tenantId, userRemote, realmService,
                    RegistryUtils.concatenateChroot(this.chroot, chroot));

        } catch (MalformedURLException e) {
            log.fatal("Registry URL is malformed, Registry configuration must be invalid", e);
            throw new RegistryException("URL is malformed");
        } catch (Exception e) {
            log.fatal("Error initializing the remote registry, User credentials must be invalid",
                    e);
            throw new RegistryException("Error initializing the remote registry");
        }
    }

    public UserRegistry getUserRegistry(String userName) throws RegistryException {
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(userName);
            userName = MultitenantUtils.getTenantAwareUsername(userName);
            int tenantId = MultitenantConstants.SUPER_TENANT_ID;
            if (tenantDomain != null &&
            		!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                if (realmService == null) {
                    String msg = "Unable to obtain an instance of a UserRegistry. The realm " +
                            "service is not available.";
                    log.error(msg);
                    throw new RegistryException(msg);
                }
                tenantId = realmService.getTenantManager().getTenantId(tenantDomain);

            }
            return getUserRegistry(userName, tenantId);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String msg = "Failed in retrieving the tenant id for the user " + userName;
            log.error(msg);
            throw new RegistryException(msg, e);
        }
    }

    public UserRegistry getUserRegistry(String userName, int tenantId) throws RegistryException {
        return getUserRegistry(userName, tenantId, null);
    }

    public UserRegistry getUserRegistry(String userName, int tenantId, String chroot)
            throws RegistryException {
        return new UserRegistry(userName, tenantId, registry, realmService,
                RegistryUtils.concatenateChroot(this.chroot, chroot));
    }

    public UserRealm getUserRealm(int tenantId) throws RegistryException {
        if (realmService == null) {
            String msg = "Unable to obtain an instance of a UserRealm. The realm service is not " +
                    "available.";
            log.error(msg);
            throw new RegistryException(msg);
        }
        // first we will get an anonymous user registry associated with the tenant
        realmService.getBootstrapRealmConfiguration();
        UserRegistry anonymousUserRegistry = new UserRegistry(
                CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME,
                tenantId,
                registry,
                realmService,
                null);
        return anonymousUserRegistry.getUserRealm();
    }

    public UserRegistry getRegistry(String userName, int tenantId, String chroot)
            throws RegistryException {
        return getUserRegistry(userName, tenantId, chroot);
    }

    public UserRegistry getRegistry(String userName, String password, int tenantId, String chroot)
            throws RegistryException {
        return getUserRegistry(userName, password, tenantId, chroot);
    }

    public UserRegistry getRegistry() throws RegistryException {
        return getRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME);
    }

    public UserRegistry getRegistry(String userName) throws RegistryException {
        return getRegistry(userName, MultitenantConstants.SUPER_TENANT_ID);
    }

    public UserRegistry getRegistry(String userName, int tenantId) throws RegistryException {
        return getRegistry(userName, tenantId, null);
    }

    public UserRegistry getRegistry(String userName, String password) throws RegistryException {
        return getRegistry(userName, password, MultitenantConstants.SUPER_TENANT_ID);
    }

    public UserRegistry getRegistry(String userName, String password, int tenantId)
            throws RegistryException {
        return getRegistry(userName, password, tenantId, null);
    }

    public UserRegistry getLocalRepository() throws RegistryException {
        return getLocalRepository(MultitenantConstants.SUPER_TENANT_ID);
    }

    public UserRegistry getLocalRepository(int tenantId) throws RegistryException {
        return getSystemRegistry(tenantId, RegistryConstants.LOCAL_REPOSITORY_BASE_PATH);
    }

    public UserRegistry getConfigSystemRegistry(int tenantId) throws RegistryException {
        return getSystemRegistry(tenantId, RegistryConstants.CONFIG_REGISTRY_BASE_PATH);
    }

    public UserRegistry getConfigSystemRegistry() throws RegistryException {
        return getConfigSystemRegistry(MultitenantConstants.SUPER_TENANT_ID);
    }

    public UserRegistry getConfigUserRegistry(String userName, int tenantId)
            throws RegistryException {
        return getRegistry(userName, tenantId, RegistryConstants.CONFIG_REGISTRY_BASE_PATH);
    }

    public UserRegistry getConfigUserRegistry(String userName, String password, int tenantId)
            throws RegistryException {
        return getRegistry(userName, password, tenantId,
                RegistryConstants.CONFIG_REGISTRY_BASE_PATH);
    }

    public UserRegistry getConfigUserRegistry() throws RegistryException {
        return getConfigUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME);
    }

    public UserRegistry getConfigUserRegistry(String userName) throws RegistryException {
        return getConfigUserRegistry(userName, MultitenantConstants.SUPER_TENANT_ID);
    }

    public UserRegistry getConfigUserRegistry(String userName, String password)
            throws RegistryException {
        return getConfigUserRegistry(userName, password, MultitenantConstants.SUPER_TENANT_ID);
    }

    public UserRegistry getGovernanceSystemRegistry(int tenantId) throws RegistryException {
        return getSystemRegistry(tenantId, RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);
    }

    public UserRegistry getGovernanceSystemRegistry() throws RegistryException {
        return getGovernanceSystemRegistry(MultitenantConstants.SUPER_TENANT_ID);
    }

    public UserRegistry getGovernanceUserRegistry(String userName, int tenantId)
            throws RegistryException {
        return getRegistry(userName, tenantId, RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);
    }

    public UserRegistry getGovernanceUserRegistry(String userName, String password, int tenantId)
            throws RegistryException {
        return getRegistry(userName, password, tenantId,
                RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);
    }

    public UserRegistry getGovernanceUserRegistry() throws RegistryException {
        return getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME);
    }

    public UserRegistry getGovernanceUserRegistry(String userName) throws RegistryException {
        return getGovernanceUserRegistry(userName, MultitenantConstants.SUPER_TENANT_ID);
    }

    public UserRegistry getGovernanceUserRegistry(String userName, String password)
            throws RegistryException {
        return getGovernanceUserRegistry(userName, password, MultitenantConstants.SUPER_TENANT_ID);
    }
}
