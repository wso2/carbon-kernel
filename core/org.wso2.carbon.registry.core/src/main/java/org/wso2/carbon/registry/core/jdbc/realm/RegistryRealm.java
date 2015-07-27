/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.core.jdbc.realm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.claim.ClaimMapping;
import org.wso2.carbon.user.core.profile.ProfileConfiguration;
import org.wso2.carbon.user.core.profile.ProfileConfigurationManager;

import java.util.Map;


/**
 * This class makes sure that the user store of the registry is always in consistent state. It wraps
 * the given realm and performs validations for all required actions before delegating the requests
 * to the wrapped realm.
 * <p/>
 * Realm is only used for getting other user store related classes. Therefore, registry provides a
 * wrappers for all such classes where ever a validation is necessary.
 */
public class RegistryRealm implements UserRealm {

    private static final Log log = LogFactory.getLog(RegistryRealm.class);

    private RegistryAuthorizationManager authorizationManager;
    private UserRealm coreRealm;

    /**
     * This constructor is provided for extending the RegistryRealm.
     */
    @SuppressWarnings("unused")
    public RegistryRealm() {
        // This constructor is used in components of the Carbon Platform
    }

    /**
     * Construct a realm wrapping a user realm.
     *
     * @param userRealm the user realm.
     *
     * @throws RegistryException throws if the operation failed.
     */
    public RegistryRealm(UserRealm userRealm) throws RegistryException {
        initialize(userRealm);
    }

    /**
     * Initialize realm
     *
     * @param userRealm the user realm
     *
     * @throws RegistryException throws if the operation failed.
     */
    private void initialize(UserRealm userRealm) throws RegistryException {

        /*try {*/
        if (log.isTraceEnabled()) {
            log.trace("Retrieving user manager components from the realm.");
        }
        this.authorizationManager = new RegistryAuthorizationManager(userRealm);

        this.coreRealm = userRealm;
        /*if (log.isTraceEnabled()) {
                log.trace("Populating the initial user store.");
            }
            AuthorizationUtils.populateUserStore(userRealm);
        } catch (UserStoreException e) {
            String msg = "Failed to initialize the registry user store. " + e.getMessage();
            log.fatal(msg, e);
            throw new RegistryException(msg, e);
        }*/
    }

    /**
     * Method to initialize the realm.
     *
     * @param configBean     the realm configuration.
     * @param claimMapping   the claims as a map
     * @param profileConfigs profile configuration.
     * @param tenantId       tenant id.
     *
     * @throws UserStoreException throws if the operation failed.
     */
    public void init(RealmConfiguration configBean, Map<String, ClaimMapping> claimMapping,
                     Map<String, ProfileConfiguration> profileConfigs, int tenantId)
            throws UserStoreException {
        //do nothing
    }
    

	public void init(RealmConfiguration configBean, Map<String, Object> properties,int tenantId)
			throws UserStoreException {
		// do nothing	
	}

	/**
     * Method to get the authorization manager
     *
     * @return the authorization manager.
     * @throws UserStoreException
     */
    public AuthorizationManager getAuthorizationManager() throws UserStoreException {
        return authorizationManager;
    }

    /**
     * Method to get the user store manager.
     *
     * @return the user store manager
     * @throws UserStoreException throws if the user store manager failed.
     */
    public UserStoreManager getUserStoreManager() throws UserStoreException {
        return coreRealm.getUserStoreManager();
    }

    /**
     * Method to get the realm.
     *
     * @return the realm object.
     * @throws UserStoreException throws if the operation failed.
     */
    public UserRealm getRealm() throws UserStoreException {
        if (this.coreRealm == null) {
            String msg = "Realm service is not available. Make sure that the required "
                    + "version of the User Manager component is properly installed.";
            log.error(msg);
            throw new UserStoreException(msg);
        }

        return coreRealm;
    }

    /**
     * Clean up the realm
     *
     * @throws UserStoreException if the operation is failed.
     */
    public void cleanUp() throws UserStoreException {
        // we are not cleaning up the core realm here, so this is an empty method.
    }

    /**
     * Get the realm configuration
     *
     * @return the realm configuration.
     * @throws UserStoreException throws if the operation failed.
     */
    public RealmConfiguration getRealmConfiguration() throws UserStoreException {
        return coreRealm.getRealmConfiguration();
    }

    /**
     * Method to get the profile configuration manager.
     *
     * @return the profile configuration manager.
     * @throws UserStoreException throws if the operation failed.
     */
    public ProfileConfigurationManager getProfileConfigurationManager() throws UserStoreException {
        return getRealm().getProfileConfigurationManager();
    }

    /**
     * Method to get the claim manager.
     *
     * @return the claim manager.
     * @throws UserStoreException throws if the operation failed.
     */
    public ClaimManager getClaimManager() throws UserStoreException {
        return getRealm().getClaimManager();
    }

    
}
