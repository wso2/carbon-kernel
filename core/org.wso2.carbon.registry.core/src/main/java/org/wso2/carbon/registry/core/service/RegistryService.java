/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.core.service;

import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.core.UserRealm;

/**
 * This interface can be used to implement an OSGi Service of the Registry. By doing so, the
 * registry would become accessible beyond the Registry Kernel. The OSGi service also can be used as
 * means to make sure that no registry related logic executes before the registry has been fully
 * initialized, and made operational. Also, when the registry is no longer in service, all external
 * entities that use the registry would also become automatically suspended.
 */
@SuppressWarnings("unused")
public interface RegistryService extends org.wso2.carbon.registry.api.RegistryService {

    /**
     * Creates a UserRegistry instance for anonymous user. Permissions set for anonymous user will
     * be applied for all operations performed using this instance.
     * <p/>
     * This registry instance belongs to the super tenant of the system.
     *
     * @return UserRegistry for the anonymous user.
     * @throws RegistryException if an error occurs
     */
    @Deprecated UserRegistry getUserRegistry() throws RegistryException;

    /**
     * Returns a registry to be used for system operations. Human users should not be allowed log in
     * using this registry.
     * <p/>
     * This registry instance belongs to the super tenant of the system.
     *
     *
     * @return User registry for system user.
     * @throws RegistryException if an error occurs
     */
    @Deprecated UserRegistry getSystemRegistry() throws RegistryException;

    /**
     * Returns a registry to be used for system operations. Human users should not be allowed log in
     * using this registry.
     *
     * @param tenantId the tenant id of the system. The tenant id '0', corresponds to the super
     *                 tenant of the system, whereas identifiers greater than '0' correspond to
     *                 valid tenants.
     *
     * @return User registry for system user.
     * @throws RegistryException if an error occurs
     */
    @Deprecated UserRegistry getSystemRegistry(int tenantId) throws RegistryException;

    /**
     * Returns a registry to be used for system operations. Human users should not be allowed log in
     * using this registry.
     *
     * @param tenantId the tenant id of the system. The tenant id '0', corresponds to the super
     *                 tenant of the system, whereas identifiers greater than '0' correspond to
     *                 valid tenants.
     * @param chroot   to return a chrooted registry. The whole registry can be accessed by using
     *                 the chroot, '/', and a subset of the registry can be accessed by using a
     *                 chroot, '/x/y/z'. For example, the repository of the configuration local
     *                 registry can be obtained from '/_system/config/repository'.
     *
     * @return User registry for system user.
     * @throws RegistryException if an error occurs
     */
    @Deprecated UserRegistry getSystemRegistry(int tenantId, String chroot)
            throws RegistryException;

    /**
     * Creates UserRegistry instances for normal users. Applications should use this method to
     * create UserRegistry instances, unless there is a specific need documented in other methods.
     * User name and the password will be authenticated by the EmbeddedRegistry before creating the
     * requested UserRegistry instance.
     * <p/>
     * This registry instance belongs to the super tenant of the system.
     *
     *
     * @param userName User name of the user.
     * @param password Password of the user.
     *
     * @return UserRegistry instance for the given user.
     * @throws RegistryException if an error occurs
     */
    @Deprecated UserRegistry getUserRegistry(String userName, String password)
            throws RegistryException;

    /**
     * Creates a Registry instance for the given user. This method will NOT authenticate the user
     * before creating the UserRegistry instance. It assumes that the user is authenticated outside
     * the EmbeddedRegistry.
     * <p/>
     * This registry instance belongs to the super tenant of the system.
     *
     *
     * @param userName User name of the user.
     *
     * @return UserRegistry instance for the given user.
     * @throws RegistryException if an error occurs
     */
    @Deprecated UserRegistry getUserRegistry(String userName) throws RegistryException;

    /**
     * Creates UserRegistry instances for normal users. Applications should use this method to
     * create UserRegistry instances, unless there is a specific need documented in other methods.
     * User name and the password will be authenticated by the EmbeddedRegistry before creating the
     * requested UserRegistry instance.
     *
     * @param userName User name of the user.
     * @param password Password of the user.
     * @param tenantId tenant id of the user tenant. The tenant id '0', corresponds to the super
     *                 tenant of the system, whereas identifiers greater than '0' correspond to
     *                 valid tenants.
     *
     * @return UserRegistry instance for the given user.
     * @throws RegistryException if an error occurs
     */
    @Deprecated UserRegistry getUserRegistry(String userName, String password, int tenantId)
            throws RegistryException;

    /**
     * Creates UserRegistry instances for normal users. Applications should use this method to
     * create UserRegistry instances, unless there is a specific need documented in other methods.
     * User name and the password will be authenticated by the EmbeddedRegistry before creating the
     * requested UserRegistry instance.
     *
     * @param userName User name of the user.
     * @param password Password of the user.
     * @param tenantId tenant id of the user tenant. The tenant id '0', corresponds to the super
     *                 tenant of the system, whereas identifiers greater than '0' correspond to
     *                 valid tenants.
     * @param chroot   to return a chrooted registry. The whole registry can be accessed by using
     *                 the chroot, '/', and a subset of the registry can be accessed by using a
     *                 chroot, '/x/y/z'. For example, the repository of the configuration local
     *                 registry can be obtained from '/_system/config/repository'.
     *
     * @return UserRegistry instance for the given user.
     * @throws RegistryException if an error occurs
     */
    @Deprecated UserRegistry getUserRegistry(String userName, String password,
                                             int tenantId, String chroot) throws RegistryException;


    /**
     * Creates a Registry instance for the given user with tenant id. This method will NOT
     * authenticate the user before creating the Registry instance. It assumes that the user is
     * authenticated outside the registry service.
     *
     * @param userName User name of the user.
     * @param tenantId tenant id of the user tenant. The tenant id '0', corresponds to the super
     *                 tenant of the system, whereas identifiers greater than '0' correspond to
     *                 valid tenants.
     *
     * @return UserRegistry instance for the given user.
     * @throws RegistryException if an error occurs
     */
    @Deprecated UserRegistry getUserRegistry(String userName, int tenantId)
            throws RegistryException;

    /**
     * Creates a Registry instance for the given user with tenant id. This method will NOT
     * authenticate the user before creating the Registry instance. It assumes that the user is
     * authenticated outside the registry service.
     *
     * @param userName User name of the user.
     * @param tenantId tenant id of the user tenant. The tenant id '0', corresponds to the super
     *                 tenant of the system, whereas identifiers greater than '0' correspond to
     *                 valid tenants.
     * @param chroot   to return a chrooted registry. The whole registry can be accessed by using
     *                 the chroot, '/', and a subset of the registry can be accessed by using a
     *                 chroot, '/x/y/z'. For example, the repository of the configuration local
     *                 registry can be obtained from '/_system/config/repository'.
     *
     * @return UserRegistry instance for the given user.
     * @throws RegistryException if an error occurs
     */
    @Deprecated UserRegistry getUserRegistry(String userName, int tenantId, String chroot)
            throws RegistryException;

    /**
     * This will return a realm specific to the tenant.
     *
     * @param tenantId tenant id of the user tenant. The tenant id '0', corresponds to the super
     *                 tenant of the system, whereas identifiers greater than '0' correspond to
     *                 valid tenants.
     *
     * @return UserRealm instance associated with the tenant id.
     * @throws RegistryException if an error occurs
     */
    UserRealm getUserRealm(int tenantId) throws RegistryException;

    ////////////////////////////////////////////////////////
    // According to the registry separation concept, there
    // are 3 different registries..
    // 1. Local data repository - to store per instance
    //    data
    // 2. Configuration registry - to store data which
    //    should be shared among all nodes in a cluster
    // 3. Governance registry - to store data which should
    //    be shared through the platform
    //
    // The following methods can be used to access the above
    // three registries separately
    ////////////////////////////////////////////////////////

    /**
     * Creates a Registry instance for anonymous user which contains the entire registry tree
     * starting from '/'. Permissions set for anonymous user will be applied for all operations
     * performed using this instance.
     * <p/>
     * This registry instance belongs to the super tenant of the system.
     *
     *
     * @return Complete Registry for the anonymous user.
     * @throws RegistryException if an error occurs
     */
    UserRegistry getRegistry() throws RegistryException;

    /**
     * Creates a Registry instance for anonymous user which contains the entire registry tree
     * starting from '/'. User name and the password will be authenticated by the EmbeddedRegistry
     * before creating the requested Registry instance.
     * <p/>
     * This registry instance belongs to the super tenant of the system.
     *
     *
     * @param userName User name of the user.
     * @param password Password of the user.
     *
     * @return Complete Registry instance for the given user.
     * @throws RegistryException if an error occurs
     */
    UserRegistry getRegistry(String userName, String password) throws RegistryException;

    /**
     * Creates a Registry instance for anonymous user which contains the entire registry tree
     * starting from '/'. This method will NOT authenticate the user before creating the Registry
     * instance. It assumes that the user is authenticated outside the EmbeddedRegistry.
     * <p/>
     * This registry instance belongs to the super tenant of the system.
     *
     *
     * @param userName User name of the user.
     *
     * @return Complete Registry instance for the given user.
     * @throws RegistryException if an error occurs
     */
    UserRegistry getRegistry(String userName) throws RegistryException;

    /**
     * Creates a Registry instance for anonymous user which contains the entire registry tree
     * starting from '/'. User name and the password will be authenticated by the EmbeddedRegistry
     * before creating the requested Registry instance. This method can be used to obtain instances
     * of Registry belonging to users of multiple tenants.
     *
     * @param userName User name of the user.
     * @param password Password of the user.
     * @param tenantId tenant id of the user tenant. The tenant id '0', corresponds to the super
     *                 tenant of the system, whereas identifiers greater than '0' correspond to
     *                 valid tenants.
     *
     * @return Complete Registry instance for the given user.
     * @throws RegistryException if an error occurs
     */
    UserRegistry getRegistry(String userName, String password, int tenantId)
            throws RegistryException;

    /**
     * Creates a Registry instance for anonymous user which contains the entire registry tree
     * starting from '/'. User name and the password will be authenticated by the EmbeddedRegistry
     * before creating the requested Registry instance. This method can be used to obtain instances
     * of Registry belonging to users of multiple tenants. The returned Registry will be chrooted to
     * the given path, making it possible to use relative paths.
     *
     * @param userName User name of the user.
     * @param password Password of the user.
     * @param tenantId tenant id of the user tenant. The tenant id '0', corresponds to the super
     *                 tenant of the system, whereas identifiers greater than '0' correspond to
     *                 valid tenants.
     * @param chroot   to return a chrooted registry. The whole registry can be accessed by using
     *                 the chroot, '/', and a subset of the registry can be accessed by using a
     *                 chroot, '/x/y/z'. For example, the repository of the configuration local
     *                 registry can be obtained from '/_system/config/repository'.
     *
     * @return Complete Registry instance for the given user.
     * @throws RegistryException if an error occurs
     */
    UserRegistry getRegistry(String userName, String password,
                             int tenantId, String chroot) throws RegistryException;


    /**
     * Creates a Registry instance for anonymous user which contains the entire registry tree
     * starting from '/'. This method will NOT authenticate the user before creating the Registry
     * instance. It assumes that the user is authenticated outside the EmbeddedRegistry. This method
     * can be used to obtain instances of Registry belonging to users of multiple tenants.
     *
     * @param userName User name of the user.
     * @param tenantId tenant id of the user tenant. The tenant id '0', corresponds to the super
     *                 tenant of the system, whereas identifiers greater than '0' correspond to
     *                 valid tenants.
     *
     * @return Complete Registry instance for the given user.
     * @throws RegistryException if an error occurs
     */
    UserRegistry getRegistry(String userName, int tenantId) throws RegistryException;

    /**
     * Creates a Registry instance for anonymous user which contains the entire registry tree
     * starting from '/'. This method will NOT authenticate the user before creating the Registry
     * instance. It assumes that the user is authenticated outside the EmbeddedRegistry. This method
     * can be used to obtain instances of Registry belonging to users of multiple tenants. The
     * returned Registry will be chrooted to the given path, making it possible to use relative
     * paths.
     *
     * @param userName User name of the user.
     * @param tenantId tenant id of the user tenant. The tenant id '0', corresponds to the super
     *                 tenant of the system, whereas identifiers greater than '0' correspond to
     *                 valid tenants.
     * @param chroot   to return a chrooted registry. The whole registry can be accessed by using
     *                 the chroot, '/', and a subset of the registry can be accessed by using a
     *                 chroot, '/x/y/z'. For example, the repository of the configuration local
     *                 registry can be obtained from '/_system/config/repository'.
     *
     * @return Complete Registry instance for the given user.
     * @throws RegistryException if an error occurs
     */
    UserRegistry getRegistry(String userName, int tenantId, String chroot) throws RegistryException;

    /**
     * Returns a registry to be used for node-specific system operations. Human users should not be
     * allowed to log in to this registry. This is the Local Repository which can only be used by
     * the system.
     * <p/>
     * This registry instance belongs to the super tenant of the system.
     *
     * @return Local Repository for system user.
     * @throws RegistryException if an error occurs
     */
    UserRegistry getLocalRepository() throws RegistryException;

    /**
     * Returns a registry to be used for node-specific system operations. Human users should not be
     * allowed to log in to this registry. This is the Local Repository which can only be used by
     * the system.
     * <p/>
     * This registry instance belongs to a valid tenant of the system.
     *
     * @param tenantId tenant id of the user tenant. The tenant id '0', corresponds to the super
     *                 tenant of the system, whereas identifiers greater than '0' correspond to
     *                 valid tenants.
     *
     * @return Local Repository for system user.
     * @throws RegistryException if an error occurs
     */
    UserRegistry getLocalRepository(int tenantId) throws RegistryException;

    /**
     * Returns a registry to be used for system operations. Human users should not be allowed log in
     * using this registry. This is the Configuration registry space which is used by the system.
     * <p/>
     * This registry instance belongs to the super tenant of the system.
     *
     *
     * @return Config Registry for system user.
     * @throws RegistryException if an error occurs
     */
    UserRegistry getConfigSystemRegistry() throws RegistryException;

    /**
     * Returns a registry to be used for system operations. Human users should not be allowed log in
     * using this registry. This is the Configuration registry space which is used by the system.
     *
     * @param tenantId the tenant id of the system. The tenant id '0', corresponds to the super
     *                 tenant of the system, whereas identifiers greater than '0' correspond to
     *                 valid tenants.
     *
     * @return User registry for system user.
     * @throws RegistryException if an error occurs
     */
    UserRegistry getConfigSystemRegistry(int tenantId) throws RegistryException;

    /**
     * Creates a Registry instance for anonymous user from the configuration registry space.
     * Permissions set for anonymous user will be applied for all operations performed using this
     * instance.
     * <p/>
     * This registry instance belongs to the super tenant of the system.
     *
     *
     * @return Config Registry for the anonymous user.
     * @throws RegistryException if an error occurs
     */
    UserRegistry getConfigUserRegistry() throws RegistryException;

    /**
     * Creates Registry instances for normal users from the configuration registry space.
     * Applications should use this method to create Registry instances, unless there is a specific
     * need documented in other methods. User name and the password will be authenticated by the
     * EmbeddedRegistry before creating the requested Registry instance.
     * <p/>
     * This registry instance belongs to the super tenant of the system.
     *
     *
     * @param userName User name of the user.
     * @param password Password of the user.
     *
     * @return Config Registry instance for the given user.
     * @throws RegistryException if an error occurs
     */
    UserRegistry getConfigUserRegistry(String userName, String password) throws RegistryException;

    /**
     * Creates a Registry instance for the given user from the configuration registry space. This
     * method will NOT authenticate the user before creating the Registry instance. It assumes that
     * the user is authenticated outside the EmbeddedRegistry.
     * <p/>
     * This registry instance belongs to the super tenant of the system.
     *
     *
     * @param userName User name of the user.
     *
     * @return Config Registry instance for the given user.
     * @throws RegistryException if an error occurs
     */
    UserRegistry getConfigUserRegistry(String userName) throws RegistryException;

    /**
     * Creates a Registry instance for the given user from the configuration registry space with the
     * tenant id. This method will NOT authenticate the user before creating the Registry instance.
     * It assumes that the user is authenticated outside the registry service.
     *
     * @param userName User name of the user.
     * @param tenantId tenant id of the user tenant. The tenant id '0', corresponds to the super
     *                 tenant of the system, whereas identifiers greater than '0' correspond to
     *                 valid tenants.
     *
     * @return Config Registry instance for the given user.
     * @throws RegistryException if an error occurs
     */
    UserRegistry getConfigUserRegistry(String userName, int tenantId) throws RegistryException;

    /**
     * Creates Registry instances for normal users from the configuration registry space.
     * Applications should use this method to create Registry instances, unless there is a specific
     * need documented in other methods. User name and the password will be authenticated by the
     * EmbeddedRegistry before creating the requested Registry instance.
     *
     * @param userName User name of the user.
     * @param tenantId tenant id of the user tenant. The tenant id '0', corresponds to the super
     *                 tenant of the system, whereas identifiers greater than '0' correspond to
     *                 valid tenants.
     * @param password Password of the user.
     *
     * @return Config Registry instance for the given user.
     * @throws RegistryException if an error occurs
     */
    UserRegistry getConfigUserRegistry(String userName, String password, int tenantId)
            throws RegistryException;

    /**
     * Creates a Registry instance for the Governance space. This is the Governance registry space
     * which is used by the system.
     * <p/>
     * This registry instance belongs to the super tenant of the system.
     *
     *
     * @return Governance Registry for system user.
     * @throws RegistryException if an error occurs
     */
    UserRegistry getGovernanceSystemRegistry() throws RegistryException;

    /**
     * Creates a Registry instance for the Governance space. This is the Governance registry space
     * which is used by the system.
     *
     * @param tenantId the tenant id of the system. The tenant id '0', corresponds to the super
     *                 tenant of the system, whereas identifiers greater than '0' correspond to
     *                 valid tenants.
     *
     * @return Governance registry for system user.
     * @throws RegistryException if an error occurs
     */
    UserRegistry getGovernanceSystemRegistry(int tenantId) throws RegistryException;

    /**
     * Creates a Registry instance for anonymous user from the Governance space. Permissions set for
     * anonymous user will be applied for all operations performed using this instance.
     * <p/>
     * This registry instance belongs to the super tenant of the system.
     *
     *
     * @return Governance Registry for the anonymous user.
     * @throws RegistryException if an error occurs
     */
    UserRegistry getGovernanceUserRegistry() throws RegistryException;

    /**
     * Creates a Registry instance for anonymous user from the Governance space. User name and the
     * password will be authenticated by the EmbeddedRegistry before creating the requested Registry
     * instance.
     * <p/>
     * This registry instance belongs to the super tenant of the system.
     *
     *
     * @param userName User name of the user.
     * @param password Password of the user.
     *
     * @return Governance Registry instance for the given user.
     * @throws RegistryException if an error occurs
     */
    UserRegistry getGovernanceUserRegistry(String userName, String password)
            throws RegistryException;

    /**
     * Creates a Registry instance for anonymous user from the Governance space. This method will
     * NOT authenticate the user before creating the Registry instance. It assumes that the user is
     * authenticated outside the EmbeddedRegistry.
     * <p/>
     * This registry instance belongs to the super tenant of the system.
     *
     *
     * @param userName User name of the user.
     *
     * @return Governance Registry instance for the given user.
     * @throws RegistryException if an error occurs
     */
    UserRegistry getGovernanceUserRegistry(String userName) throws RegistryException;

    /**
     * Creates a Registry instance for anonymous user from the Governance space. User name and the
     * password will be authenticated by the EmbeddedRegistry before creating the requested Registry
     * instance. This method can be used to obtain instances of Registry belonging to users of
     * multiple tenants.
     *
     * @param userName User name of the user.
     * @param password Password of the user.
     * @param tenantId tenant id of the user tenant. The tenant id '0', corresponds to the super
     *                 tenant of the system, whereas identifiers greater than '0' correspond to
     *                 valid tenants.
     *
     * @return Governance Registry instance for the given user.
     * @throws RegistryException if an error occurs
     */
    UserRegistry getGovernanceUserRegistry(String userName, String password, int tenantId)
            throws RegistryException;

    /**
     * Creates a Registry instance for anonymous user from the Governance space. This method will
     * NOT authenticate the user before creating the Registry instance. It assumes that the user is
     * authenticated outside the EmbeddedRegistry. This method can be used to obtain instances of
     * Registry belonging to users of multiple tenants.
     *
     * @param userName User name of the user.
     * @param tenantId tenant id of the user tenant. The tenant id '0', corresponds to the super
     *                 tenant of the system, whereas identifiers greater than '0' correspond to
     *                 valid tenants.
     *
     * @return Governance Registry instance for the given user.
     * @throws RegistryException if an error occurs
     */
    UserRegistry getGovernanceUserRegistry(String userName, int tenantId) throws RegistryException;
}
