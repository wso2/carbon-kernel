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
package org.wso2.carbon.user.core.common;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.Permission;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreConfigConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.authorization.AuthorizationCache;
import org.wso2.carbon.user.core.authorization.JDBCAuthorizationManager;
import org.wso2.carbon.user.core.authorization.PermissionTree;
import org.wso2.carbon.user.core.claim.Claim;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.claim.ClaimMapping;
import org.wso2.carbon.user.core.dto.RoleDTO;
import org.wso2.carbon.user.core.hybrid.HybridRoleManager;
import org.wso2.carbon.user.core.internal.UMListenerServiceComponent;
import org.wso2.carbon.user.core.ldap.LDAPConstants;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;
import org.wso2.carbon.user.core.listener.UserStoreManagerConfigurationListener;
import org.wso2.carbon.user.core.listener.UserStoreManagerListener;
import org.wso2.carbon.user.core.profile.ProfileConfigurationManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.system.SystemUserRoleManager;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

public abstract class AbstractUserStoreManager implements UserStoreManager {

	private static Log log = LogFactory.getLog(AbstractUserStoreManager.class);

	protected int tenantId;
	protected DataSource dataSource = null;
	protected RealmConfiguration realmConfig = null;
	protected ClaimManager claimManager = null;
	protected UserRealm userRealm = null;
	protected HybridRoleManager hybridRoleManager = null;

	// User roles cache
	protected UserRolesCache userRolesCache = null;
	protected SystemUserRoleManager systemUserRoleManager = null;
	protected boolean readGroupsEnabled = false;
	protected boolean writeGroupsEnabled = false;
	private UserStoreManager secondaryUserStoreManager;
	private boolean userRolesCacheEnabled = true;
	private String cacheIdentifier;
	private boolean replaceEscapeCharactersAtUserLogin = true;
	private Map<String, UserStoreManager> userStoreManagerHolder = new HashMap<String, UserStoreManager>();
	private Map<String, Integer> maxUserListCount = null;
	private Map<String, Integer> maxRoleListCount = null;
	private List<UserStoreManagerConfigurationListener> listener = new ArrayList<UserStoreManagerConfigurationListener>();

	private static final String MAX_LIST_LENGTH = "100";

	protected static final String TRUE_VALUE = "true";
	protected static final String FALSE_VALUE = "false";

        private static final String MULIPLE_ATTRIBUTE_ENABLE = "MultipleAttributeEnable";

	/**
	 * This method is used by the support system to read properties
	 */
	protected abstract Map<String, String> getUserPropertyValues(String userName,
			String[] propertyNames, String profileName) throws UserStoreException;

	/**
	 * 
	 * @param roleName
	 * @return
	 */
	protected abstract boolean doCheckExistingRole(String roleName) throws UserStoreException;

    /**
     * Creates the search base and other relevant parameters for the provided role name
     * @param roleName
     * @return
     */
    protected abstract RoleContext createRoleContext(String roleName) throws UserStoreException;

	/**
	 * 
	 * @param userName
	 * @return
	 * @throws UserStoreException
	 */
	protected abstract boolean doCheckExistingUser(String userName) throws UserStoreException;

	/**
	 * Retrieves a list of user names for given user's property in user profile
	 * 
	 * @param property user property in user profile
	 * @param value value of property
	 * @param profileName profile name, can be null. If null the default profile is considered.
	 * @return An array of user names
	 * @throws UserStoreException if the operation failed
	 */
	protected abstract String[] getUserListFromProperties(String property, String value,
			String profileName) throws UserStoreException;

	/**
	 * Given the user name and a credential object, the implementation code must validate whether
	 * the user is authenticated.
	 * 
	 * @param userName The user name
	 * @param credential The credential of a user
	 * @return If the value is true the provided credential match with the user name. False is
	 *         returned for invalid credential, invalid user name and mismatching credential with
	 *         user name.
	 * @throws UserStoreException An unexpected exception has occurred
	 */
	protected abstract boolean doAuthenticate(String userName, Object credential)
			throws UserStoreException;

	/**
	 * Add a user to the user store.
	 * 
	 * @param userName User name of the user
	 * @param credential The credential/password of the user
	 * @param roleList The roles that user belongs
	 * @param claims Properties of the user
	 * @param profileName profile name, can be null. If null the default profile is considered.
	 * @param requirePasswordChange whether password required is need
	 * @throws UserStoreException An unexpected exception has occurred
	 */
	protected abstract void doAddUser(String userName, Object credential, String[] roleList,
			Map<String, String> claims, String profileName, boolean requirePasswordChange)
			throws UserStoreException;

	/**
	 * Update the credential/password of the user
	 * 
	 * @param userName The user name
	 * @param newCredential The new credential/password
	 * @param oldCredential The old credential/password
	 * @throws UserStoreException An unexpected exception has occurred
	 */
	protected abstract void doUpdateCredential(String userName, Object newCredential,
			Object oldCredential) throws UserStoreException;

	/**
	 * Update credential/password by the admin of another user
	 * 
	 * @param userName The user name
	 * @param newCredential The new credential
	 * @throws UserStoreException An unexpected exception has occurred
	 */
	protected abstract void doUpdateCredentialByAdmin(String userName, Object newCredential)
			throws UserStoreException;

	/**
	 * Delete the user with the given user name
	 * 
	 * @param userName The user name
	 * @throws UserStoreException An unexpected exception has occurred
	 */
	protected abstract void doDeleteUser(String userName) throws UserStoreException;

	/**
	 * Set a single user claim value
	 * 
	 * @param userName The user name
	 * @param claimURI The claim URI
	 * @param claimValue The value
	 * @param profileName The profile name, can be null. If null the default profile is considered.
	 * @throws UserStoreException An unexpected exception has occurred
	 */
	protected abstract void doSetUserClaimValue(String userName, String claimURI,
			String claimValue, String profileName) throws UserStoreException;

	/**
	 * Set many user claim values
	 * 
	 * @param userName The user name
	 * @param claims Map of claim URIs against values
	 * @param profileName The profile name, can be null. If null the default profile is considered.
	 * @throws UserStoreException An unexpected exception has occurred
	 */
	protected abstract void doSetUserClaimValues(String userName, Map<String, String> claims,
			String profileName) throws UserStoreException;

	/**
	 * o * Delete a single user claim value
	 * 
	 * @param userName The user name
	 * @param claimURI Name of the claim
	 * @param profileName The profile name, can be null. If null the default profile is considered.
	 * @throws UserStoreException An unexpected exception has occurred
	 */
	protected abstract void doDeleteUserClaimValue(String userName, String claimURI,
			String profileName) throws UserStoreException;

	/**
	 * Delete many user claim values.
	 * 
	 * @param userName The user name
	 * @param claims URIs of the claims to be deleted.
	 * @param profileName The profile name, can be null. If null the default profile is considered.
	 * @throws UserStoreException An unexpected exception has occurred
	 */
	protected abstract void doDeleteUserClaimValues(String userName, String[] claims,
			String profileName) throws UserStoreException;

	/**
	 * Update user list of a particular role
	 * 
	 * @param roleName The role name
	 * @param deletedUsers Array of user names, that is going to be removed from the role
	 * @param newUsers Array of user names, that is going to be added to the role
	 * @throws UserStoreException An unexpected exception has occurred
	 */
	protected abstract void doUpdateUserListOfRole(String roleName, String[] deletedUsers,
			String[] newUsers) throws UserStoreException;

	/**
	 * Update role list of a particular user
	 * 
	 * @param userName The user name
	 * @param deletedRoles Array of role names, that is going to be removed from the user
	 * @param newRoles Array of role names, that is going to be added to the user
	 * @throws UserStoreException An unexpected exception has occurred
	 */
	protected abstract void doUpdateRoleListOfUser(String userName, String[] deletedRoles,
			String[] newRoles) throws UserStoreException;

	/**
	 * Only gets the internal roles of the user with internal domain name
	 * 
	 * @param userName Name of the user - who we need to find roles.
	 * @return
	 * @throws UserStoreException
	 */
        protected String[] doGetInternalRoleListOfUser(String userName, String filter) throws UserStoreException {
            if(Boolean.parseBoolean(realmConfig.getUserStoreProperty(MULIPLE_ATTRIBUTE_ENABLE))){
                String userNameAttribute = realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE);
                if(userNameAttribute != null && userNameAttribute.trim().length() > 0) {
                    Map<String, String> map = getUserPropertyValues(userName, new String[]{userNameAttribute}, null);
                    userName = map.get(userNameAttribute);
                }
            }
            log.debug("Retrieving internals roles for user name :  " + userName + " and search filter " + filter);
            return hybridRoleManager.getHybridRoleListOfUser(userName, filter);
        }

	/**
	 * Only gets the external roles of the user.
	 * 
	 * @param userName Name of the user - who we need to find roles.
	 * @return
	 * @throws UserStoreException
	 */
	protected abstract String[] doGetExternalRoleListOfUser(String userName, String filter)
			throws UserStoreException;
	

	/**
	 * Returns the shared roles list of the user
	 * 
	 * @param userName
	 * @return
	 * @throws UserStoreException
	 */
	protected abstract String[] doGetSharedRoleListOfUser(String userName,
                                    String tenantDomain, String filter) throws UserStoreException;

	/**
	 * Add role with a list of users and permissions provided.
	 * 
	 * @param roleName
	 * @param userList
	 * @throws UserStoreException
	 */
	protected abstract void doAddRole(String roleName, String[] userList, boolean shared) throws UserStoreException;


	/**
	 * delete the role.
	 * 
	 * @param roleName
	 * @throws UserStoreException
	 */
	protected abstract void doDeleteRole(String roleName) throws UserStoreException;

	/**
	 * update the role name with the new name
	 * 
	 * @param roleName
	 * @param newRoleName
	 * @throws UserStoreException
	 */
	protected abstract void doUpdateRoleName(String roleName, String newRoleName)
			throws UserStoreException;

	/**
	 * This method would returns the role Name actually this must be implemented in interface. As it
	 * is not good to change the API in point release. This has been added to Abstract class
	 * 
	 * @param filter
	 * @param maxItemLimit
	 * @return
	 * @throws .UserStoreException
	 */
	protected abstract String[] doGetRoleNames(String filter, int maxItemLimit)
			throws UserStoreException;
		
	/**
	 * 
	 * @param filter
	 * @param maxItemLimit
	 * @return
	 * @throws UserStoreException
	 */
	protected abstract String[] doListUsers(String filter, int maxItemLimit)
			throws UserStoreException;

    /*This is to get the display names of users in hybrid role according to the underlying user store, to be shown in UI*/
    protected abstract String[] doGetDisplayNamesForInternalRole(String[] userNames)
            throws UserStoreException;

	/**
	 * {@inheritDoc}
	 */
	public final boolean authenticate(String userName, Object credential) throws UserStoreException {
        if (userName == null || credential == null) {
            log.error("Authentication failure. Either Username or Password is null");
            return false;
        }
        int index = userName != null ? userName.indexOf(CarbonConstants.DOMAIN_SEPARATOR) : -1;
		boolean domainProvided = index > 0;
		return authenticate(userName, credential, domainProvided);
	}

	/**
	 * 
	 * @param userName
	 * @param credential
	 * @param domainProvided
	 * @return
	 * @throws UserStoreException
	 */
	protected boolean authenticate(String userName, Object credential, boolean domainProvided)
			throws UserStoreException {

		boolean authenticated = false;

		UserStore userStore = getUserStore(userName);
		if (userStore.isRecurssive() && userStore.getUserStoreManager() instanceof AbstractUserStoreManager ) {
			return ((AbstractUserStoreManager)userStore.getUserStoreManager()).authenticate(userStore.getDomainFreeName(),
					credential, domainProvided);
		}

		// #################### Domain Name Free Zone Starts Here ################################

		// #################### <Listeners> #####################################################
		for (UserStoreManagerListener listener : UMListenerServiceComponent
				.getUserStoreManagerListeners()) {
			if (!listener.authenticate(userName, credential, this)) {
				return true;
			}
		}

		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPreAuthenticate(userName, credential, this)) {
				return false;
			}
		}
		// #################### </Listeners> #####################################################

        int tenantId = getTenantId();

        try {
            RealmService realmService = UserCoreUtil.getRealmService();
            if (realmService != null) {
                boolean tenantActive = realmService.getTenantManager().isTenantActive(tenantId);

                if (!tenantActive) {
                    throw new UserStoreException("Tenant has been deactivated. TenantID : "
                            + tenantId);
                }
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException("Error while trying to check Tenant status for Tenant : "
                    + tenantId, e);
        }

		// We are here due to two reason. Either there is no secondary UserStoreManager or no
		// domain name provided with user name.
		
		try {
			// Let's authenticate with the primary UserStoreManager.
			authenticated = doAuthenticate(userName, credential);
		} catch (Exception e) {
			// We can ignore and proceed. Ignore the results from this user store.
			log.error(e);
			authenticated = false;
		}
		
		if (authenticated) {
			// Set domain in thread local variable for subsequent operations
            String domain = UserCoreUtil.getDomainName(this.realmConfig);
            if (domain != null) {
                UserCoreUtil.setDomainInThreadLocal(domain.toUpperCase());
            }
        }

		// If authentication fails in the previous step and if the user has not specified a
		// domain- then we need to execute chained UserStoreManagers recursively.
		if (!authenticated && !domainProvided && this.getSecondaryUserStoreManager() != null) {
			authenticated = ((AbstractUserStoreManager) this.getSecondaryUserStoreManager())
					.authenticate(userName, credential, domainProvided);
		}

		// You cannot change authentication decision in post handler to TRUE
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPostAuthenticate(userName, authenticated, this)) {
				return false;
			}
		}

		if (log.isDebugEnabled()) {
			if (!authenticated) {
				log.debug("Authentication failure. Wrong username or password is provided.");
			}
		}

		return authenticated;
	}

	/**
	 * {@inheritDoc}
	 */
	public final String getUserClaimValue(String userName, String claim, String profileName)
			throws UserStoreException {

        // If user does not exist, just return
        if (!isExistingUser(userName)) {
            return null;
        }

		UserStore userStore = getUserStore(userName);
		if (userStore.isRecurssive()) {
			return userStore.getUserStoreManager().getUserClaimValue(userStore.getDomainFreeName(),
					claim, profileName);
		}

		// #################### Domain Name Free Zone Starts Here ################################



        Map<String, String> finalValues = doGetUserClaimValues(userName, new String[] {claim}, 
                userStore.getDomainName(), profileName);

        String value = null;

        if(finalValues != null){
		    value = finalValues.get(claim);
        }

		// #################### <Listeners> #####################################################

        List<String> list = new ArrayList<String>();
		if (value != null) {
			list.add(value);
		}

		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (listener instanceof AbstractUserOperationEventListener) {
				AbstractUserOperationEventListener newListener = (AbstractUserOperationEventListener) listener;
				if (!newListener.doPostGetUserClaimValue(userName, claim, list, profileName, this)) {
					break;
				}
			}
		}
		// #################### </Listeners> #####################################################

		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	public final Claim[] getUserClaimValues(String userName, String profileName)
			throws UserStoreException {

        // If user does not exist, just return
        if (!isExistingUser(userName)) {
            return null;
        }

		UserStore userStore = getUserStore(userName);
		if (userStore.isRecurssive()) {
			return userStore.getUserStoreManager().getUserClaimValues(
					userStore.getDomainFreeName(), profileName);
		}

		// #################### Domain Name Free Zone Starts Here ################################

		if (profileName == null || profileName.trim().length() == 0) {
			profileName = UserCoreConstants.DEFAULT_PROFILE;
		}

		String[] claims;
		try {
			claims = claimManager.getAllClaimUris();
		} catch (org.wso2.carbon.user.api.UserStoreException e) {
			throw new UserStoreException(e);
		}

		Map<String, String> values = this.getUserClaimValues(userName, claims, profileName);
		Claim[] finalValues = new Claim[values.size()];
		int i = 0;
		for (Iterator<Map.Entry<String, String>> ite = values.entrySet().iterator(); ite.hasNext();) {
			Map.Entry<String, String> entry = ite.next();
			Claim claim = new Claim();
			claim.setValue(entry.getValue());
			claim.setClaimUri(entry.getKey());
			String displayTag;
			try {
				displayTag = claimManager.getClaim(entry.getKey()).getDisplayTag();
			} catch (org.wso2.carbon.user.api.UserStoreException e) {
				throw new UserStoreException(e);
			}
			claim.setDisplayTag(displayTag);
			finalValues[i] = claim;
			i++;
		}

		return finalValues;
	}

	/**
	 * {@inheritDoc}
	 */
	public final Map<String, String> getUserClaimValues(String userName, String[] claims,
			String profileName) throws UserStoreException {

		UserStore userStore = getUserStore(userName);
		if (userStore.isRecurssive()) {
			return userStore.getUserStoreManager().getUserClaimValues(
					userStore.getDomainFreeName(), claims, profileName);
		}

		// #################### Domain Name Free Zone Starts Here ################################

		Map<String, String> finalValues = doGetUserClaimValues(userName, claims,
                userStore.getDomainName(), profileName);

		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (listener instanceof AbstractUserOperationEventListener) {
				AbstractUserOperationEventListener newListener = (AbstractUserOperationEventListener) listener;
				if (!newListener.doPostGetUserClaimValues(userStore.getDomainFreeName(), claims, profileName,
						finalValues, this)) {
					break;
				}
			}
		}
		// #################### </Listeners> #####################################################

		return finalValues;
	}

    /**
     * If the claim is domain qualified, search the users respective user store. Else we
     * return the users in all the user-stores recursively
     * {@inheritDoc}
     */
    public final String[] getUserList(String claim, String claimValue, String profileName)
            throws UserStoreException {
        try {
            String property;
            //extracting the domain from claimValue. Not introducing a new method due to carbon patch process..
            String extractedDomain = null;
            int index;
            index = claimValue.indexOf(CarbonConstants.DOMAIN_SEPARATOR);
            if (index > 0){
                String names[] = claimValue.split(CarbonConstants.DOMAIN_SEPARATOR);
                extractedDomain = names[0].trim();
            }
            claimValue = UserCoreUtil.removeDomainFromName(claimValue);
            //if domain is present, then we search within that domain only
            if (extractedDomain != null && !extractedDomain.isEmpty()) {
                property = claimManager.getAttributeName(extractedDomain, claim);
                if (property == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Could not find matching property for\n" +
                                "claim :" + claim +
                                "domain :" + extractedDomain);
                    }
                    return new String[0];
                }
                // get the user list and return with domain appended
                AbstractUserStoreManager userStoreManager = (AbstractUserStoreManager)
                        getSecondaryUserStoreManager(extractedDomain);
                String[] userArray = userStoreManager.getUserListFromProperties(property, claimValue, profileName);
                return UserCoreUtil.addDomainToNames(userArray, extractedDomain);
            }
            //if no domain is given then search all the user stores
            List<String> usersFromAllStoresList = new LinkedList<String>();
            AbstractUserStoreManager currentUserStoreManager = this;
            if (log.isDebugEnabled()) {
                log.debug("No domain name found in claim value. Searching through all user stores for possible matches");
            }
            do {
                String currentDomain = currentUserStoreManager.getMyDomainName();
                property = claimManager.getAttributeName(currentDomain, claim);
                if (property == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Could not find matching property for\n" +
                                "claim :" + claim +
                                "domain :" + currentDomain);
                    }
                    continue; // continue look in other stores
                }
                String[] userArray2 = currentUserStoreManager.getUserListFromProperties(property, claimValue, profileName);
                if (log.isDebugEnabled()) {
                    log.debug("searching the property :" + property + "in user store" + currentDomain +
                            "for given claim value : " + claimValue);
                }
                String[] userWithDomainArray = UserCoreUtil.addDomainToNames(userArray2, currentDomain);
                usersFromAllStoresList.addAll(Arrays.asList(userWithDomainArray));
            } while ((currentUserStoreManager = (AbstractUserStoreManager) currentUserStoreManager.
                    getSecondaryUserStoreManager()) != null);
            //done with all user store processing. Return the user array if not empty
            String[] fullUserList = usersFromAllStoresList.toArray(new String[0]);
            Arrays.sort(fullUserList);
            return fullUserList;
        } catch (org.wso2.carbon.user.api.UserStoreException exception) {
            log.error("Error while searching the user stores", exception);
            throw new UserStoreException(exception);
        }
    }
    /**
	 * {@inheritDoc}
	 */
	public final void updateCredential(String userName, Object newCredential, Object oldCredential)
			throws UserStoreException {

		UserStore userStore = getUserStore(userName);
		if (userStore.isRecurssive()) {
			userStore.getUserStoreManager().updateCredential(userStore.getDomainFreeName(),
					newCredential, oldCredential);
			return;
		}

		// #################### Domain Name Free Zone Starts Here ################################

		if (isReadOnly()) {
			throw new UserStoreException("Invalid operation. User store is read only");
		}

		// #################### <Listeners> #####################################################
		for (UserStoreManagerListener listener : UMListenerServiceComponent
				.getUserStoreManagerListeners()) {
			if (!listener.updateCredential(userName, newCredential, oldCredential, this)) {
				return;
			}
		}

		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPreUpdateCredential(userName, newCredential, oldCredential, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################

		// This user name here is domain-less.
		// We directly authenticate user against the selected UserStoreManager.
		boolean isAuth = this.doAuthenticate(userName, oldCredential);

		if (isAuth) {

			this.doUpdateCredential(userName, newCredential, oldCredential);

			// #################### <Listeners> ##################################################
			for (UserOperationEventListener listener : UMListenerServiceComponent
					.getUserOperationEventListeners()) {
				if (!listener.doPostUpdateCredential(userName, newCredential, this)) {
					return;
				}
			}
			// #################### </Listeners> ##################################################

			return;
		} else {
			throw new UserStoreException(
					"Old credential does not match with the existing credentials.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final void updateCredentialByAdmin(String userName, Object newCredential)
			throws UserStoreException {

		UserStore userStore = getUserStore(userName);
		if (userStore.isRecurssive()) {
			userStore.getUserStoreManager().updateCredentialByAdmin(userStore.getDomainFreeName(),
					newCredential);
			return;
		}

		// #################### Domain Name Free Zone Starts Here ################################

		if (isReadOnly()) {
			throw new UserStoreException("Invalid operation. User store is read only");
		}

		// #################### <Listeners> #####################################################
		for (UserStoreManagerListener listener : UMListenerServiceComponent
				.getUserStoreManagerListeners()) {
			if (!listener.updateCredentialByAdmin(userName, newCredential, this)) {
				return;
			}
		}
		// using string buffers to allow the password to be changed by listener
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if(newCredential == null) { // a default password will be set
				StringBuffer credBuff = new StringBuffer();
				if (!listener.doPreUpdateCredentialByAdmin(userName, newCredential, this)) {
					return;
				}
				newCredential = credBuff.toString(); // reading the modified value
			} else if (newCredential instanceof String) {
				StringBuffer credBuff = new StringBuffer((String)newCredential);
				if (!listener.doPreUpdateCredentialByAdmin(userName, credBuff, this)) {
					return;
				}
				newCredential = credBuff.toString(); // reading the modified value
			}
		}
		// #################### </Listeners> #####################################################

		doUpdateCredentialByAdmin(userName, newCredential);

		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPostUpdateCredentialByAdmin(userName, newCredential, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################

	}
	
	/**
	 * Get the attribute for the provided claim uri and identifier.
	 * 
	 * @param claimURI
	 * @param identifier
	 *            user name or role.
	 * @param domainName TODO
	 * @return claim attribute value. NULL if attribute is not defined for the
	 *         claim uri
	 * @throws org.wso2.carbon.user.api.UserStoreException
	 */
	protected String getClaimAtrribute(String claimURI, String identifier, String domainName)
	                                                                      throws org.wso2.carbon.user.api.UserStoreException {
		domainName =
		             (domainName == null || domainName.isEmpty())
		                                                         ? (identifier.indexOf(UserCoreConstants.DOMAIN_SEPARATOR) > -1
		                                                                                                                       ? identifier.split(UserCoreConstants.DOMAIN_SEPARATOR)[0]
		                                                                                                                       : realmConfig.getUserStoreProperty(UserStoreConfigConstants.DOMAIN_NAME))
		                                                         : domainName;
		String attributeName = null;
		if (domainName != null && !domainName.equals(UserStoreConfigConstants.PRIMARY)) {
			attributeName = claimManager.getAttributeName(domainName, claimURI);
		}
		if (attributeName == null || attributeName.isEmpty()) {
			attributeName = claimManager.getAttributeName(claimURI);
		}
		return attributeName != null ? attributeName : claimURI;
	}

	/**
	 * {@inheritDoc}
	 */
	public final void deleteUser(String userName) throws UserStoreException {
		
		String loggedInUser = CarbonContext.getThreadLocalCarbonContext().getUsername();
		if(loggedInUser != null){
			loggedInUser = UserCoreUtil.addDomainToName(loggedInUser , UserCoreUtil.getDomainFromThreadLocal());
			if ((loggedInUser.indexOf(UserCoreConstants.DOMAIN_SEPARATOR)) < 0) {
				loggedInUser = UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME +
				           CarbonConstants.DOMAIN_SEPARATOR + loggedInUser;
			}	
		}	
		
		String deletingUser = UserCoreUtil.addDomainToName(userName, getMyDomainName());
		if ((deletingUser.indexOf(UserCoreConstants.DOMAIN_SEPARATOR)) < 0) {
			deletingUser = UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME +
			               CarbonConstants.DOMAIN_SEPARATOR + deletingUser;
		}

		if(loggedInUser!= null && loggedInUser.equals(deletingUser)) {
			log.debug("User " + loggedInUser + " tried to delete him/her self");
			throw new UserStoreException("Cannot delete logged in user");
		}

		UserStore userStore = getUserStore(userName);
		if (userStore.isRecurssive()) {
			userStore.getUserStoreManager().deleteUser(userStore.getDomainFreeName());
			return;
		}

		// #################### Domain Name Free Zone Starts Here ################################

		if (UserCoreUtil.isPrimaryAdminUser(userName, realmConfig)) {
			throw new UserStoreException("Cannot delete admin user");
		}

		if (UserCoreUtil.isRegistryAnnonymousUser(userName)) {
			throw new UserStoreException("Cannot delete anonymous user");
		}

		if (isReadOnly()) {
			throw new UserStoreException("Invalid operation. User store is read only");
		}

		// #################### <Listeners> #####################################################
		for (UserStoreManagerListener listener : UMListenerServiceComponent
				.getUserStoreManagerListeners()) {
			if (!listener.deleteUser(userName, this)) {
				return;
			}
		}

		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPreDeleteUser(userName, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################

		if (!doCheckExistingUser(userName)) {
			throw new UserStoreException("Cannot delete user who is not exist");
		}

		// Remove users from internal role mapping
		hybridRoleManager.deleteUser(UserCoreUtil.addDomainToName(userName, getMyDomainName()));

		doDeleteUser(userName);

		// Needs to clear roles cache upon deletion of a user
        clearUserRolesCache(UserCoreUtil.addDomainToName(userName, getMyDomainName()));

		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPostDeleteUser(userName, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################

	}

	/**
	 * {@inheritDoc}
	 */
	public final void setUserClaimValue(String userName, String claimURI, String claimValue,
			String profileName) throws UserStoreException {

		UserStore userStore = getUserStore(userName);
		if (userStore.isRecurssive()) {
			userStore.getUserStoreManager().setUserClaimValue(userStore.getDomainFreeName(),
					claimURI, claimValue, profileName);
			return;
		}

		if (isReadOnly()) {
			throw new UserStoreException("Invalid operation. User store is read only");
		}

		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPreSetUserClaimValue(userName, claimURI, claimValue, profileName, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################

		doSetUserClaimValue(userName, claimURI, claimValue, profileName);

		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPostSetUserClaimValue(userName, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################

	}

	/**
	 * {@inheritDoc}
	 */
	public final void setUserClaimValues(String userName, Map<String, String> claims,
			String profileName) throws UserStoreException {

		UserStore userStore = getUserStore(userName);
		if (userStore.isRecurssive()) {
			userStore.getUserStoreManager().setUserClaimValues(userStore.getDomainFreeName(),
					claims, profileName);
			return;
		}

		if (isReadOnly()) {
			throw new UserStoreException("Invalid operation. User store is read only");
		}

		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPreSetUserClaimValues(userName, claims, profileName, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################

		doSetUserClaimValues(userName, claims, profileName);

		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPostSetUserClaimValues(userName, claims, profileName, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################

	}

	/**
	 * {@inheritDoc}
	 */
	public final void deleteUserClaimValue(String userName, String claimURI, String profileName)
			throws UserStoreException {

		UserStore userStore = getUserStore(userName);
		if (userStore.isRecurssive()) {
			userStore.getUserStoreManager().deleteUserClaimValue(userStore.getDomainFreeName(),
					claimURI, profileName);
			return;
		}

		if (isReadOnly()) {
			throw new UserStoreException("Invalid operation. User store is read only");
		}

		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPreDeleteUserClaimValue(userName, claimURI, profileName, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################

		doDeleteUserClaimValue(userName, claimURI, profileName);

		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPostDeleteUserClaimValue(userName, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################
	}

	/**
	 * {@inheritDoc}
	 */
	public final void deleteUserClaimValues(String userName, String[] claims, String profileName)
			throws UserStoreException {

		UserStore userStore = getUserStore(userName);
		if (userStore.isRecurssive()) {
			userStore.getUserStoreManager().deleteUserClaimValues(userStore.getDomainFreeName(),
					claims, profileName);
			return;
		}

		if (isReadOnly()) {
			throw new UserStoreException("Invalid operation. User store is read only");
		}

		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPreDeleteUserClaimValues(userName, claims, profileName, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################

		doDeleteUserClaimValues(userName, claims, profileName);

		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPostDeleteUserClaimValues(userName, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################

	}

	/**
	 * {@inheritDoc}
	 */
	public final void addUser(String userName, Object credential, String[] roleList,
			Map<String, String> claims, String profileName, boolean requirePasswordChange)
			throws UserStoreException {

		UserStore userStore = getUserStore(userName);
		if (userStore.isRecurssive()) {
			userStore.getUserStoreManager().addUser(userStore.getDomainFreeName(), credential,
					roleList, claims, profileName, requirePasswordChange);
			return;
		}
        
        if(userStore.isSystemStore()){
            systemUserRoleManager.addSystemUser(userName, credential, roleList);
            return;
        }

		// #################### Domain Name Free Zone Starts Here ################################

		if (isReadOnly()) {
			throw new UserStoreException("Invalid operation. User store is read only");
		}

		// This happens only once during first startup - adding administrator user/role.
		if (userName.indexOf(CarbonConstants.DOMAIN_SEPARATOR) > 0) {
			userName = userStore.getDomainFreeName();
			roleList = UserCoreUtil.removeDomainFromNames(roleList);
		}

		// #################### <Listeners> #####################################################
		for (UserStoreManagerListener listener : UMListenerServiceComponent
				.getUserStoreManagerListeners()) {
			if (!listener.addUser(userName, credential, roleList, claims, profileName, this)) {
				return;
			}
		}
		// String buffers are used to let listeners to modify passwords
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if(credential == null) { // a default password will be set
				StringBuffer credBuff = new StringBuffer();
				if (!listener.doPreAddUser(userName, credBuff, roleList, claims, profileName,
				                           this)) {
					return;
				}
				credential = credBuff.toString(); // reading the modified value
			} else if (credential instanceof String) {
				StringBuffer credBuff = new StringBuffer((String)credential);
				if (!listener.doPreAddUser(userName, credBuff, roleList, claims, profileName,
				                           this)) {
					return;
				}
				credential = credBuff.toString(); // reading the modified value
			}
		}
		// #################### </Listeners> #####################################################

		if (!checkUserNameValid(userStore.getDomainFreeName())) {
			String message = "Username "+ userStore.getDomainFreeName() +" is not valid. User name must be a non null string with following format, ";
			String regEx = realmConfig
					.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USER_NAME_JAVA_REG_EX);
			throw new UserStoreException(message + regEx);
		}

		if (!checkUserPasswordValid(credential)) {
			String message = "Credential not valid. Credential must be a non null string with following format, ";
			String regEx = realmConfig
					.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_JAVA_REG_EX);
			throw new UserStoreException(message + regEx);
		}

		if (doCheckExistingUser(userStore.getDomainFreeName())) {
			throw new UserStoreException("Username '" + userName
					+ "' already exists in the system. Please pick another username.");
		}
        
        
   
		List<String> internalRoles = new ArrayList<String>();
		List<String> externalRoles = new ArrayList<String>();
		int index;
		if (roleList != null) {
			for (String role : roleList) {
                if(role != null && role.trim().length() > 0){
                    index = role.indexOf(CarbonConstants.DOMAIN_SEPARATOR);
                    if (index > 0) {
                        String domain = role.substring(0, index);
                        if (UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(domain)) {
                            internalRoles.add(UserCoreUtil.removeDomainFromName(role));
                            continue;
                        }
                    }
                    externalRoles.add(UserCoreUtil.removeDomainFromName(role));
                }
			}
		}
        
        // check existance of roles and claims before user is adding
        for(String internalRole : internalRoles){
            if(!hybridRoleManager.isExistingRole(internalRole)){
                throw new UserStoreException("Internal role is not exist : " + internalRole);
            }
        }

        for(String externalRole : externalRoles){
            if(!doCheckExistingRole(externalRole)){
                throw new UserStoreException("External role is not exist : " + externalRole);
            }
        }
        
        if(claims != null){
            for(Map.Entry<String, String> entry : claims.entrySet()){
                ClaimMapping claimMapping = null;
                try {
                    claimMapping = (ClaimMapping)claimManager.getClaimMapping(entry.getKey());
                } catch (org.wso2.carbon.user.api.UserStoreException e) {
                    String errorMessage = "Error in obtaining claim mapping for persisting user attributes.";
                    throw new UserStoreException(errorMessage, e);                    
                }
                if(claimMapping == null){
                    String errorMessage = "Invalid claim uri has been provided.";
                    throw new UserStoreException(errorMessage);
                }
            }
        }
        
		doAddUser(userName, credential, externalRoles.toArray(new String[externalRoles.size()]),
				claims, profileName, requirePasswordChange);

		if (internalRoles.size() > 0) {
			hybridRoleManager.updateHybridRoleListOfUser(userName, null,
					internalRoles.toArray(new String[internalRoles.size()]));
		}

		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPostAddUser(userName, credential, roleList, claims, profileName, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################
		
		try {
			roleList = UserCoreUtil
					.combine(doGetInternalRoleListOfUser(userName, "*"), Arrays.asList(roleList));
			addToUserRolesCache(tenantId, UserCoreUtil.addDomainToName(userName, getMyDomainName()),
			                    roleList);
		} catch (Exception e) {
			//if adding newly created user's roles to the user roles cache fails, do nothing. It will read 
			//from the database upon updating user.
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void addUser(String userName, Object credential, String[] roleList,
			Map<String, String> claims, String profileName) throws UserStoreException {
		this.addUser(userName, credential, roleList, claims, profileName, false);
	}

	/**
	 * {@inheritDoc}
	 */
	public final void updateUserListOfRole(String roleName, String[] deletedUsers, String[] newUsers)
			throws UserStoreException {

		String primaryDomain = getMyDomainName();
		if (primaryDomain != null) {
			primaryDomain += CarbonConstants.DOMAIN_SEPARATOR;
		}

		if (deletedUsers != null && deletedUsers.length > 0) {
			Arrays.sort(deletedUsers);
			// Updating the user list of a role belong to the primary domain.
			if (UserCoreUtil.isPrimaryAdminRole(roleName, realmConfig)) {
				for (int i = 0; i < deletedUsers.length; i++) {
					if (deletedUsers[i].equalsIgnoreCase(realmConfig.getAdminUserName())
							|| (primaryDomain + deletedUsers[i]).equalsIgnoreCase(realmConfig
									.getAdminUserName())) {
						throw new UserStoreException("Cannot remove Admin user from Admin role");
					}

				}
			}
		}

		UserStore userStore = getUserStore(roleName);

        if (userStore.isHybridRole()) {
			// Check whether someone is trying to update Everyone role.
			if (UserCoreUtil.isEveryoneRole(roleName, realmConfig)) {
				throw new UserStoreException("Cannot update everyone role");
			}

            hybridRoleManager.updateUserListOfHybridRole(userStore.getDomainFreeName(),
                                                         deletedUsers, newUsers);
            clearUserRolesCacheByTenant(this.tenantId);
			return;
		}

        if(userStore.isSystemStore()){
            systemUserRoleManager.updateUserListOfSystemRole(userStore.getDomainFreeName(),
                                                UserCoreUtil.removeDomainFromNames(deletedUsers),
                                                UserCoreUtil.removeDomainFromNames(newUsers));
            return;
        }

		if (userStore.isRecurssive()) {
			userStore.getUserStoreManager().updateUserListOfRole(userStore.getDomainFreeName(),
					UserCoreUtil.removeDomainFromNames(deletedUsers),
					UserCoreUtil.removeDomainFromNames(newUsers));
			return;
		}

		// #################### Domain Name Free Zone Starts Here ################################

		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPreUpdateUserListOfRole(roleName, deletedUsers,
			                                        newUsers, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################

		if ((deletedUsers != null && deletedUsers.length > 0)
				|| (newUsers != null && newUsers.length > 0)) {
			if (!isReadOnly() && writeGroupsEnabled) {
				doUpdateUserListOfRole(userStore.getDomainFreeName(),
						UserCoreUtil.removeDomainFromNames(deletedUsers),
						UserCoreUtil.removeDomainFromNames(newUsers));
			} else {
				throw new UserStoreException(
						"Read-only user store.Roles cannot be added or modfified");
			}
		}

		// need to clear user roles cache upon roles update
		clearUserRolesCacheByTenant(this.tenantId);

		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
.getUserOperationEventListeners()) {
			if (!listener.doPostUpdateUserListOfRole(roleName, deletedUsers,
			                                         newUsers, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################

	}

	/**
	 * {@inheritDoc}
	 */
	public final void updateRoleListOfUser(String userName, String[] deletedRoles, String[] newRoles)
			throws UserStoreException {

		String primaryDomain = realmConfig
				.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
		if (primaryDomain != null) {
			primaryDomain += CarbonConstants.DOMAIN_SEPARATOR;
		}

		if (deletedRoles != null && deletedRoles.length > 0) {
			Arrays.sort(deletedRoles);
			if (UserCoreUtil.isPrimaryAdminUser(userName, realmConfig)) {
				for (int i = 0; i < deletedRoles.length; i++) {
					if (deletedRoles[i].equalsIgnoreCase(realmConfig.getAdminRoleName())
							|| (primaryDomain + deletedRoles[i]).equalsIgnoreCase(realmConfig
									.getAdminRoleName())) {
						throw new UserStoreException("Cannot remove Admin user from Admin role");
					}
				}
			}
		}

		UserStore userStore = getUserStore(userName);
		if (userStore.isRecurssive()) {
			userStore.getUserStoreManager().updateRoleListOfUser(userStore.getDomainFreeName(),
					UserCoreUtil.removeDomainFromNames(deletedRoles),
					UserCoreUtil.removeDomainFromNames(newRoles));
			return;
		}

        if(userStore.isSystemStore()){
            systemUserRoleManager.updateSystemRoleListOfUser(userStore.getDomainFreeName(),
                    UserCoreUtil.removeDomainFromNames(deletedRoles),
                    UserCoreUtil.removeDomainFromNames(newRoles));
            return;
        }

		// #################### Domain Name Free Zone Starts Here ################################

		// This happens only once during first startup - adding administrator user/role.
		if (userName.indexOf(CarbonConstants.DOMAIN_SEPARATOR) > 0) {
			userName = userStore.getDomainFreeName();
			deletedRoles = UserCoreUtil.removeDomainFromNames(deletedRoles);
			newRoles = UserCoreUtil.removeDomainFromNames(newRoles);
		}

		List<String> internalRoleDel = new ArrayList<String>();
		List<String> internalRoleNew = new ArrayList<String>();

		List<String> roleDel = new ArrayList<String>();
		List<String> roleNew = new ArrayList<String>();

		if (deletedRoles != null && deletedRoles.length > 0) {
			for (String deleteRole : deletedRoles) {
                if (UserCoreUtil.isEveryoneRole(deleteRole, realmConfig)) {
					throw new UserStoreException("Everyone role cannot be updated");
				}
				String domain = null;
				int index1 = deleteRole.indexOf(CarbonConstants.DOMAIN_SEPARATOR);
				if (index1 > 0) {
					domain = deleteRole.substring(0, index1);
				}
				if (UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(domain) || this.isReadOnly()) {
					internalRoleDel.add(UserCoreUtil.removeDomainFromName(deleteRole));
				} else {
					// This is domain free role name.
					roleDel.add(UserCoreUtil.removeDomainFromName(deleteRole));
				}
			}
			deletedRoles = roleDel.toArray(new String[roleDel.size()]);
		}

		if (newRoles != null && newRoles.length > 0) {
			for (String newRole : newRoles) {
				if (UserCoreUtil.isEveryoneRole(newRole, realmConfig)) {
					throw new UserStoreException("Everyone role cannot be updated");
				}
				String domain = null;
				int index2 = newRole.indexOf(CarbonConstants.DOMAIN_SEPARATOR);
				if (index2 > 0) {
					domain = newRole.substring(0, index2);
				}
				if (UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(domain) || this.isReadOnly()) {
					internalRoleNew.add(UserCoreUtil.removeDomainFromName(newRole));
				} else {
					roleNew.add(UserCoreUtil.removeDomainFromName(newRole));
				}
			}
			newRoles = roleNew.toArray(new String[roleNew.size()]);
		}

		if (internalRoleDel.size() > 0 || internalRoleNew.size() > 0) {
			hybridRoleManager.updateHybridRoleListOfUser(userStore.getDomainFreeName(),
					internalRoleDel.toArray(new String[internalRoleDel.size()]),
					internalRoleNew.toArray(new String[internalRoleNew.size()]));
		}

		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPreUpdateRoleListOfUser(userName, deletedRoles, newRoles, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################

		if ((deletedRoles != null && deletedRoles.length > 0)
				|| (newRoles != null && newRoles.length > 0)) {
			if (!isReadOnly() && writeGroupsEnabled) {
				doUpdateRoleListOfUser(userName, deletedRoles, newRoles);
			} else {
				throw new UserStoreException("Read-only user store. Cannot add/modify roles.");
			}
		}

        clearUserRolesCache(UserCoreUtil.addDomainToName(userName, getMyDomainName()));

		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPostUpdateRoleListOfUser(userName, deletedRoles, newRoles, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################

	}

	/**
	 * {@inheritDoc}
	 */
	public final void updateRoleName(String roleName, String newRoleName) throws UserStoreException {

		if (UserCoreUtil.isPrimaryAdminRole(newRoleName, realmConfig)) {
			throw new UserStoreException("Cannot rename admin role");
		}

		if (UserCoreUtil.isEveryoneRole(newRoleName, realmConfig)) {
			throw new UserStoreException("Cannot rename everyone role");
		}

		UserStore userStore = getUserStore(roleName);
		UserStore userStoreNew = getUserStore(newRoleName);

		if (!UserCoreUtil.canRoleBeRenamed(userStore, userStoreNew, realmConfig)) {
			throw new UserStoreException("The role cannot be renamed");
		}

		if (userStore.isRecurssive()) {
			userStore.getUserStoreManager().updateRoleName(userStore.getDomainFreeName(),
					userStoreNew.getDomainFreeName());
			return;
		}

		// #################### Domain Name Free Zone Starts Here ################################

		if (userStore.isHybridRole()) {
			hybridRoleManager.updateHybridRoleName(userStore.getDomainFreeName(),
					userStoreNew.getDomainFreeName());

			// This is a special case. We need to pass roles with domains.
			userRealm.getAuthorizationManager().resetPermissionOnUpdateRole(
					userStore.getDomainAwareName(), userStoreNew.getDomainAwareName());

			// Need to update user role cache upon update of role names
			clearUserRolesCacheByTenant(this.tenantId);
			return;
		}
//
//		RoleContext ctx = createRoleContext(roleName);
//        if (isOthersSharedRole(roleName)) {          // TODO do we need this
//            throw new UserStoreException(
//                    "Logged in user doesn't have permission to delete a role belong to other tenant");
//        }

        if (isExistingRole(newRoleName)) {
            throw new UserStoreException("Role name: " + newRoleName
                    + " in the system. Please pick another role name.");
        }

		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPreUpdateRoleName(roleName, newRoleName, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################

		if (!isReadOnly() && writeGroupsEnabled) {
			doUpdateRoleName(userStore.getDomainFreeName(), userStoreNew.getDomainFreeName());
		} else {
			throw new UserStoreException(
					"Read-only UserStoreManager. Roles cannot be added or modified.");
		}

		// This is a special case. We need to pass domain aware name.
		userRealm.getAuthorizationManager().resetPermissionOnUpdateRole(
				userStore.getDomainAwareName(), userStoreNew.getDomainAwareName());

		// need to update user role cache upon update of role names
		clearUserRolesCacheByTenant(tenantId);

		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPostUpdateRoleName(roleName, newRoleName, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################

	}


    @Override
    public boolean isExistingRole(String roleName, boolean shared) throws org.wso2.carbon.user.api.UserStoreException {
        if(shared){
            return isExistingShareRole(roleName);
        } else {
            return isExistingRole(roleName);
        }
    }

    /**
	 * {@inheritDoc}
	 */
	public boolean isExistingRole(String roleName) throws UserStoreException {

		UserStore userStore = getUserStore(roleName);

		if (userStore.isRecurssive()) {
			return userStore.getUserStoreManager().isExistingRole(userStore.getDomainFreeName());
		}

		// #################### Domain Name Free Zone Starts Here ################################

        if(userStore.isSystemStore()){
            return systemUserRoleManager.isExistingRole(userStore.getDomainFreeName());
        }

		if (userStore.isHybridRole()) {
			boolean exist = hybridRoleManager.isExistingRole(userStore.getDomainFreeName());
			if(exist) {
				return true;
			} else {
				return false;
			}
		}

		// This happens only once during first startup - adding administrator user/role.
		roleName = userStore.getDomainFreeName();

        // you can not check existence of shared role using this method.
        if(isSharedGroupEnabled() && roleName.contains(UserCoreConstants.TENANT_DOMAIN_COMBINER)){
            return false;
        }
        
		boolean isExisting = doCheckExistingRole(roleName);

		if (!isExisting && (isReadOnly() || !readGroupsEnabled)) {
			isExisting = hybridRoleManager.isExistingRole(roleName);
		}

		if (!isExisting) {
			if (systemUserRoleManager.isExistingRole(roleName)) {
				isExisting = true;
			}
		}

		return isExisting;
	}

//////////////////////////////////// Shared role APIs start //////////////////////////////////////////
    /**
     * TODO move to API
     * @param roleName
     * @return
     * @throws UserStoreException
     */
    public boolean isExistingShareRole(String roleName) throws UserStoreException {

        UserStoreManager manager  = getUserStoreWithSharedRoles();

        if(manager == null){
            throw new UserStoreException("Share Groups are not supported by this realm");
        }
        
        return ((AbstractUserStoreManager)manager).doCheckExistingRole(roleName);
    }

    /**
     * TODO  move to API
     * @param roleName
     * @param deletedUsers
     * @param newUsers
     * @throws UserStoreException
     */
    public void updateUsersOfSharedRole(String roleName,
                                String[] deletedUsers, String[] newUsers) throws UserStoreException{

        UserStoreManager manager  = getUserStoreWithSharedRoles();

        if(manager == null){
            throw new UserStoreException("Share Groups are not supported by this realm");
        }

        ((AbstractUserStoreManager)manager).doUpdateUserListOfRole(roleName, deletedUsers, newUsers);
    }
    /**
     * TODO move to API
     * @return
     * @throws UserStoreException
     */
    public String[] getSharedRolesOfUser(String userName,
                                    String tenantDomain, String filter) throws UserStoreException {

        UserStore userStore = getUserStore(userName);
        UserStoreManager manager  = userStore.getUserStoreManager();

        if(!((AbstractUserStoreManager)manager).isSharedGroupEnabled()){
            throw new UserStoreException("Share Groups are not supported by user store");
        }

        String[] sharedRoles = ((AbstractUserStoreManager)manager).
                doGetSharedRoleListOfUser(userStore.getDomainFreeName(), tenantDomain, filter);
        return UserCoreUtil. removeDomainFromNames(sharedRoles);
    }

    /**
     * TODO move to API
     * @return
     * @throws UserStoreException
     */
    public String[] getUsersOfSharedRole(String roleName, String filter) throws UserStoreException {

        UserStoreManager manager  = getUserStoreWithSharedRoles();

        if(manager == null){
            throw new UserStoreException("Share Groups are not supported by this realm");
        }

        String[] users = ((AbstractUserStoreManager)manager).doGetUserListOfRole(roleName, filter);
        return UserCoreUtil. removeDomainFromNames(users);
    }

    /**
     * TODO move to API
     * @return
     * @throws UserStoreException
     */
    public String[] getSharedRoleNames(String tenantDomain, String filter,
                                                    int maxItemLimit) throws UserStoreException {


        UserStoreManager manager  = getUserStoreWithSharedRoles();

        if(manager == null){
            throw new UserStoreException("Share Groups are not supported by this realm");
        }

        String[] sharedRoles = null;
        try{
            sharedRoles = ((AbstractUserStoreManager)manager).
                                        doGetSharedRoleNames(tenantDomain, filter, maxItemLimit);
        } catch (UserStoreException e){
            throw new UserStoreException("Error while retrieving shared roles", e);
        }
        return UserCoreUtil. removeDomainFromNames(sharedRoles);
    }


    /**
     * TODO move to API
     * @return
     * @throws UserStoreException
     */
    public String[] getSharedRoleNames(String filter, int maxItemLimit) throws UserStoreException {

        UserStoreManager manager  = getUserStoreWithSharedRoles();

        if(manager == null){
            throw new UserStoreException("Share Groups are not supported by this realm");
        }

        String[] sharedRoles = null;
        try{
            sharedRoles = ((AbstractUserStoreManager)manager).
                    doGetSharedRoleNames(null, filter, maxItemLimit);
        } catch (UserStoreException e){
            throw new UserStoreException("Error while retrieving shared roles", e);
        }
        return UserCoreUtil. removeDomainFromNames(sharedRoles);
    }


    public void addInternalRole(String roleName, String[] userList,
                    org.wso2.carbon.user.api.Permission[] permission) throws UserStoreException {
        doAddInternalRole(roleName, userList, permission);
    }

    private UserStoreManager getUserStoreWithSharedRoles() throws UserStoreException {
        
        UserStoreManager sharedRoleManager = null;
        
        if(isSharedGroupEnabled()) {
            return this;
        }
        
        for(Map.Entry<String, UserStoreManager> entry : userStoreManagerHolder.entrySet()){
            UserStoreManager manager = entry.getValue();
            if(manager != null && ((AbstractUserStoreManager)manager).isSharedGroupEnabled()){
                if(sharedRoleManager != null){
                    throw new UserStoreException("There can not be more than one user store that support" +
                            "shared groups");
                }
                sharedRoleManager =  manager;               
            }
        }
        
        return  sharedRoleManager;
    }

    /**
     * TODO move to API
     * @param userName
     * @param roleName
     * @return
     * @throws UserStoreException
     */
    public boolean isUserInRole(String userName, String roleName) throws UserStoreException {

        if(roleName == null || roleName.trim().length() == 0 || userName == null ||
                userName.trim().length() == 0){
            return false;
        }
        
        // anonymous user is always assigned to  anonymous role
        if(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME.equalsIgnoreCase(roleName) &&
                        CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equalsIgnoreCase(userName)){
            return true;
        }

        if(!CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equalsIgnoreCase(userName) &&
                realmConfig.getEveryOneRoleName().equalsIgnoreCase(roleName) &&
                !systemUserRoleManager.isExistingSystemUser(UserCoreUtil.
                        removeDomainFromName(userName))) {
            return true;
        }
        
        

        String[] roles = null;

        try{
            roles = getRoleListOfUserFromCache(tenantId, userName);
        } catch (Exception e){
            //ignore
        }

        if(roles != null){
            if(UserCoreUtil.isContain(roleName, roles)){
                return true;
            }
        }

        // TODO create new cache for this method
        String  modifiedUserName =  UserCoreConstants.IS_USER_IN_ROLE_CACHE_IDENTIFIER + userName;
        try{
            roles = getRoleListOfUserFromCache(tenantId, modifiedUserName);
        } catch (Exception e){
            //ignore
        }

        if(roles != null){
            if(UserCoreUtil.isContain(roleName, roles)){
                return true;
            }
        }

        if(UserCoreConstants.INTERNAL_DOMAIN.
                                equalsIgnoreCase(UserCoreUtil.extractDomainFromName(roleName))){
            String[] internalRoles = doGetInternalRoleListOfUser(userName, "*");
            if(UserCoreUtil.isContain(roleName, internalRoles)){
                addToIsUserHasRole(modifiedUserName, roleName, roles);
                return true;
            }
        }

        UserStore userStore = getUserStore(userName);
        if (userStore.isRecurssive()
                && (userStore.getUserStoreManager() instanceof AbstractUserStoreManager)) {
            return ((AbstractUserStoreManager) userStore.getUserStoreManager()).isUserInRole(
                    userStore.getDomainFreeName(), roleName);
        }

        // #################### Domain Name Free Zone Starts Here ################################

        if(userStore.isSystemStore()){
            return systemUserRoleManager.isUserInRole(userStore.getDomainFreeName(),
                                                    UserCoreUtil.removeDomainFromName(roleName));
        }
        // admin user is always assigned to admin role if it is in primary user store
        if(realmConfig.isPrimary() && roleName.equalsIgnoreCase(realmConfig.getAdminRoleName()) &&
                                userName.equalsIgnoreCase(realmConfig.getAdminUserName())){
            return true;
        }

        String roleDomainName = UserCoreUtil.extractDomainFromName(roleName);

        String roleDomainNameForForest = realmConfig.
                getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_GROUP_SEARCH_DOMAINS);
        if(roleDomainNameForForest != null && roleDomainNameForForest.trim().length() > 0){
            String[] values = roleDomainNameForForest.split("#");
            for(String value : values){
                if(value != null && !value.trim().equalsIgnoreCase(roleDomainName)){
                    return false;
                }
            }
        } else if (!userStore.getDomainName().equalsIgnoreCase(roleDomainName)) {
            return false;
        }
        
        boolean success = false;
        if(readGroupsEnabled){        
            success = doCheckIsUserInRole(userStore.getDomainFreeName(),
                                                    UserCoreUtil.removeDomainFromName(roleName));
        }

        // add to cache
        if(success){
            addToIsUserHasRole(modifiedUserName, roleName, roles);
        }
        return success;
    }

    /**
     * 
     * @param userName
     * @param roleName
     * @return
     * @throws UserStoreException
     */
    public abstract boolean doCheckIsUserInRole(String userName, String roleName) throws UserStoreException;

    /**
     * Helper method
     * @param userName
     * @param roleName
     * @param currentRoles
     */
    private void addToIsUserHasRole(String userName, String roleName, String[] currentRoles){
        List<String> roles;
        if(currentRoles != null){
            roles = new ArrayList<String>(Arrays.asList(currentRoles));
        } else {
            roles = new ArrayList<String>();
        }
        roles.add(roleName);
        addToUserRolesCache(tenantId, UserCoreUtil.addDomainToName(userName, getMyDomainName()),
                roles.toArray(new String[roles.size()]));
    }    

//////////////////////////////////// Shared role APIs finish //////////////////////////////////////////

	/**
	 * {@inheritDoc}
	 */
	public boolean isExistingUser(String userName) throws UserStoreException {

		if (UserCoreUtil.isRegistrySystemUser(userName)) {
			return true;
		}

		UserStore userStore = getUserStore(userName);
		if (userStore.isRecurssive()) {
			return userStore.getUserStoreManager().isExistingUser(userStore.getDomainFreeName());
		}

		// #################### Domain Name Free Zone Starts Here ################################

		if(userStore.isSystemStore()){
            return systemUserRoleManager.isExistingSystemUser(userStore.getDomainFreeName());
        }
        
        
        return doCheckExistingUser(userStore.getDomainFreeName());

	}

	/**
	 * {@inheritDoc}
	 */
	public final String[] listUsers(String filter, int maxItemLimit) throws UserStoreException {

		int index;
		index = filter.indexOf(CarbonConstants.DOMAIN_SEPARATOR);

		// Check whether we have a secondary UserStoreManager setup.
		if (index > 0) {
			// Using the short-circuit. User name comes with the domain name.
			String domain = filter.substring(0, index);

			UserStoreManager secManager = getSecondaryUserStoreManager(domain);
			if (secManager != null) {
				// We have a secondary UserStoreManager registered for this domain.
				filter = filter.substring(index + 1);
				if (secManager instanceof AbstractUserStoreManager) {
					return ((AbstractUserStoreManager) secManager)
							.doListUsers(filter, maxItemLimit);
				} else {
				    return secManager.listUsers(filter, maxItemLimit);
				}
			} else {
				// Exception is not need to as listing of users
				// throw new UserStoreException("Invalid Domain Name");
			}
		} else if (index == 0) {
			return doListUsers(filter.substring(index + 1), maxItemLimit);
		}

		String[] userList = doListUsers(filter, maxItemLimit);

		String primaryDomain = realmConfig
				.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);

		if (this.getSecondaryUserStoreManager() != null) {
			for (Map.Entry<String, UserStoreManager> entry : userStoreManagerHolder.entrySet()) {
				if (entry.getKey().equalsIgnoreCase(primaryDomain)) {
					continue;
				}
				UserStoreManager storeManager = entry.getValue();
				if (storeManager instanceof AbstractUserStoreManager) {
					try {
						String[] secondUserList = ((AbstractUserStoreManager) storeManager)
								.doListUsers(filter, maxItemLimit);
						userList = UserCoreUtil.combineArrays(userList, secondUserList);
					} catch (UserStoreException ex) {
						// We can ignore and proceed. Ignore the results from this user store.
						log.error(ex);
					}
                } else {
                    String[] secondUserList = storeManager.listUsers(filter, maxItemLimit);
                    userList = UserCoreUtil.combineArrays(userList, secondUserList);
                }
			}
		}

		return userList;
	}

	/**
	 * {@inheritDoc}
	 */
	public final String[] getUserListOfRole(String roleName) throws UserStoreException {

		String[] userNames = new String[0];

		// If role does not exit, just return
		if (!isExistingRole(roleName)) {
			return userNames;
		}

		UserStore userStore = getUserStore(roleName);

		if (userStore.isRecurssive()) {
			return userStore.getUserStoreManager().getUserListOfRole(userStore.getDomainFreeName());
		}


		// #################### Domain Name Free Zone Starts Here
		// ################################

        if(userStore.isSystemStore()){
            return systemUserRoleManager.getUserListOfSystemRole(userStore.getDomainFreeName());
        }

        String[] userNamesInHybrid = new String[0];
		if (userStore.isHybridRole()) {
			userNamesInHybrid =
			                    hybridRoleManager.getUserListOfHybridRole(userStore.getDomainFreeName());
			// remove domain
			List<String> finalNameList = new ArrayList<String>();
            String displayNameAttribute =
                    this.realmConfig.getUserStoreProperty(LDAPConstants.DISPLAY_NAME_ATTRIBUTE);

            if(userNamesInHybrid != null  && userNamesInHybrid.length > 0){
                if(displayNameAttribute != null && displayNameAttribute.trim().length() > 0){
                    for (String userName : userNamesInHybrid) {
                        String domainName = UserCoreUtil.extractDomainFromName(userName);
                        if(domainName == null || domainName.trim().length() == 0){
                            finalNameList.add(userName);
                        }
                        UserStoreManager userManager = userStoreManagerHolder.get(domainName);
                        userName = UserCoreUtil.removeDomainFromName(userName);
                        if(userManager != null){
                            String[] displayNames = null;
                            if (userManager instanceof AbstractUserStoreManager) {
                                // get displayNames
                                displayNames = ((AbstractUserStoreManager) userManager)
                                        .doGetDisplayNamesForInternalRole(new String[] { userName });
                            } else {
                                displayNames = userManager.getRoleNames();
                            }

                            for (String displayName : displayNames) {
                                // if domain names are not added by above method, add it
                                // here
                                String nameWithDomain = UserCoreUtil.addDomainToName(displayName, domainName);
                                finalNameList.add(nameWithDomain);
                            }
                        }
                    }
                } else {
                    return userNamesInHybrid;
                }
            }
			return finalNameList.toArray(new String[finalNameList.size()]);
			// return
			// hybridRoleManager.getUserListOfHybridRole(userStore.getDomainFreeName());
		}

		if (readGroupsEnabled) {
			userNames = doGetUserListOfRole(roleName, "*");
		}

		return userNames;
	}
	
	public String[] getRoleListOfUser(String userName) throws UserStoreException {
		String[] roleNames = null;


        // anonymous user is only assigned to  anonymous role
        if(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equalsIgnoreCase(userName)){
            return new String[] {CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME};
        }       

        // Check whether roles exist in cache
        try {
            roleNames = getRoleListOfUserFromCache(this.tenantId, userName);
            if (roleNames != null) {
                return roleNames;
            }
        } catch (Exception e) {
            // If not exist in cache, continue
        }

		UserStore userStore = getUserStore(userName);
		if (userStore.isRecurssive()) {
			return userStore.getUserStoreManager().getRoleListOfUser(userStore.getDomainFreeName());
		}

        if(userStore.isSystemStore()){
            return systemUserRoleManager.getSystemRoleListOfUser(userStore.getDomainFreeName());
        }
		// #################### Domain Name Free Zone Starts Here ################################

        roleNames = doGetRoleListOfUser(userName, "*");

		return roleNames;

	}

	/**
	 * Getter method for claim manager property specifically to be used in the implementations of
	 * UserOperationEventListener implementations
	 * 
	 * @return
	 */
	public ClaimManager getClaimManager() {
		return claimManager;
	}

	/**
	 * 
	 */
	public void addRole(String roleName, String[] userList,
			org.wso2.carbon.user.api.Permission[] permissions, boolean isSharedRole)
			throws org.wso2.carbon.user.api.UserStoreException {

		UserStore userStore = getUserStore(roleName);
		
		if (isSharedRole && !isSharedGroupEnabled()) {
			throw new org.wso2.carbon.user.api.UserStoreException(
			                                                      "User store doesn't support shared user roles functionality");
		}

        if (userStore.isHybridRole()) {
			doAddInternalRole(userStore.getDomainFreeName(), userList, permissions);
			return;
		}
        
		if (userStore.isRecurssive()) {
			userStore.getUserStoreManager().addRole(userStore.getDomainFreeName(),
					UserCoreUtil.removeDomainFromNames(userList), permissions, isSharedRole);
			return;
		}

		// #################### Domain Name Free Zone Starts Here ################################

		// This happens only once during first startup - adding administrator user/role.
		if (roleName.indexOf(CarbonConstants.DOMAIN_SEPARATOR) > 0) {
			roleName = userStore.getDomainFreeName();
			userList = UserCoreUtil.removeDomainFromNames(userList);
		}


		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPreAddRole(roleName, userList, permissions, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################

		// Check for validations
		if (isReadOnly()) {
			throw new UserStoreException(
					"Cannot add role to Read Only user store unless it is primary");
		}

		if (!isRoleNameValid(roleName)) {
			String regEx = realmConfig
					.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_ROLE_NAME_JAVA_REG_EX);
			throw new UserStoreException(
					"Role name not valid. Role name must be a non null string with following format, "
							+ regEx);
		}

		if (doCheckExistingRole(roleName)) {
			throw new UserStoreException("Role name: " + roleName +
			                             " in the system. Please pick another role name.");
		}

		String roleWithDomain = null;
		if (!isReadOnly() && writeGroupsEnabled) {
			// add role in to actual user store
			doAddRole(roleName, userList,isSharedRole);

			roleWithDomain = UserCoreUtil.addDomainToName(roleName, getMyDomainName());
		} else {
			throw new UserStoreException(
                         "Role cannot be added. User store is read only or cannot write groups.");
		}

		// add permission in to the the permission store
		if (permissions != null) {
			for (org.wso2.carbon.user.api.Permission permission : permissions) {
				String resourceId = permission.getResourceId();
				String action = permission.getAction();
                if(resourceId == null || resourceId.trim().length() ==0 ){
                    continue;
                }

                if(action == null || action.trim().length() ==0 ){
                    // default action value // TODO
                    action = "read";
                }
				// This is a special case. We need to pass domain aware name.
				userRealm.getAuthorizationManager().authorizeRole(roleWithDomain, resourceId,
						action);
			}
		}

		// if existing users are added to role, need to update user role cache
		if ((userList != null) && (userList.length > 0)) {
			clearUserRolesCacheByTenant(tenantId);
		}

		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPostAddRole(roleName, userList, permissions, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################

	}

    /**
     * TODO move to API
     *
     * @return
     */
	public boolean isSharedGroupEnabled() {
		String value = realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.SHARED_GROUPS_ENABLED);
		try {
			return realmConfig.isPrimary() && !isReadOnly() && TRUE_VALUE.equalsIgnoreCase(value);
		} catch (UserStoreException e) {
			log.error(e);
		}
		return false;
	}

	/**
	 * Removes the shared roles relevant to the provided tenant domain
	 * 
	 * @param sharedRoles
	 * @param tenantDomain
	 */
	protected void filterSharedRoles(List<String> sharedRoles, String tenantDomain) {
		if (tenantDomain != null) {
			for (Iterator<String> i = sharedRoles.iterator(); i.hasNext();) {
				String role = i.next();
				if (role.indexOf(tenantDomain) > -1) {
					i.remove();
				}
			}
		}
	}

	/**
	 * Delete the role with the given role name
	 * 
	 * @param roleName The role name
	 * @throws org.wso2.carbon.user.core.UserStoreException
	 * 
	 */
	public final void deleteRole(String roleName) throws UserStoreException {

		if (UserCoreUtil.isPrimaryAdminRole(roleName, realmConfig)) {
			throw new UserStoreException("Cannot delete admin role");
		}
		if (UserCoreUtil.isEveryoneRole(roleName, realmConfig)) {
			throw new UserStoreException("Cannot delete everyone role");
		}

		UserStore userStore = getUserStore(roleName);
		if (userStore.isRecurssive()) {
			userStore.getUserStoreManager().deleteRole(userStore.getDomainFreeName());
			return;
		}

		String roleWithDomain = UserCoreUtil.addDomainToName(roleName, getMyDomainName());
		// #################### Domain Name Free Zone Starts Here ################################

		if (userStore.isHybridRole()) {
			hybridRoleManager.deleteHybridRole(userStore.getDomainFreeName());
            clearUserRolesCacheByTenant(tenantId);
			return;
		}
//
//		RoleContext ctx = createRoleContext(roleName);
//		if (isOthersSharedRole(roleName)) {
//			throw new UserStoreException(
//			                             "Logged in user doesn't have permission to delete a role belong to other tenant");
//		}


        if (!doCheckExistingRole(roleName)) {
            throw new UserStoreException("Can not delete non exiting role");
        }

		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPreDeleteRole(roleName, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################

		if (!isReadOnly() && writeGroupsEnabled) {
			doDeleteRole(roleName);
		} else {
			throw new UserStoreException(
			                             "Role cannot be deleted. User store is read only or cannot write groups.");
		}

        // clear role authorization
        userRealm.getAuthorizationManager().clearRoleAuthorization(roleWithDomain);

        // clear cache
		clearUserRolesCacheByTenant(tenantId);

		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPostDeleteRole(roleName, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################

	}

	/**
	 *
	 * @return
	 * @throws UserStoreException
	 */
	private UserStore getUserStore(String user) throws UserStoreException {

		int index;
		index = user.indexOf(CarbonConstants.DOMAIN_SEPARATOR);
		UserStore userStore = new UserStore();
		String domainFreeName = null;

		// Check whether we have a secondary UserStoreManager setup.
		if (index > 0) {
			// Using the short-circuit. User name comes with the domain name.
			String domain = user.substring(0, index);
			UserStoreManager secManager = getSecondaryUserStoreManager(domain);
			domainFreeName = user.substring(index + 1);

			if (secManager != null) {
				userStore.setUserStoreManager(secManager);
				userStore.setDomainAwareName(user);
				userStore.setDomainFreeName(domainFreeName);
				userStore.setDomainName(domain);
				userStore.setRecurssive(true);
				return userStore;
			} else {
				if (!domain.equalsIgnoreCase(getMyDomainName())) {
					if ((UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(domain))) {
						userStore.setHybridRole(true);
                    } else if(UserCoreConstants.SYSTEM_DOMAIN_NAME.equalsIgnoreCase(domain)) {
                        userStore.setSystemStore(true);
					} else {
                        throw new UserStoreException("Invalid Domain Name");
					}
				}
			}
		}

		String domain = UserCoreUtil.getDomainName(realmConfig);
		userStore.setUserStoreManager(this);
		if (index > 0) {
			userStore.setDomainAwareName(user);
			userStore.setDomainFreeName(domainFreeName);
		} else {
			userStore.setDomainAwareName(domain + CarbonConstants.DOMAIN_SEPARATOR + user);
			userStore.setDomainFreeName(user);
		}
		userStore.setRecurssive(false);
		userStore.setDomainName(domain);

        return userStore;
	}

	/**
	 * {@inheritDoc}
	 */
	public final UserStoreManager getSecondaryUserStoreManager() {
		return secondaryUserStoreManager;
	}

	/**
	 * {@inheritDoc}
	 */
	public final UserStoreManager getSecondaryUserStoreManager(String userDomain) {
		if (userDomain == null) {
			return null;
		}
		return userStoreManagerHolder.get(userDomain.toUpperCase());
	}

	/**
	 * 
	 */
	public final void setSecondaryUserStoreManager(UserStoreManager secondaryUserStoreManager) {
		this.secondaryUserStoreManager = secondaryUserStoreManager;
	}

	/**
	 * {@inheritDoc}
	 */
	public final void addSecondaryUserStoreManager(String userDomain,
			UserStoreManager userStoreManager) {
		if (userDomain != null) {
			userStoreManagerHolder.put(userDomain.toUpperCase(), userStoreManager);
		}
	}
	
    public final void clearAllSecondaryUserStores() {
        userStoreManagerHolder.clear();
        
        if (getMyDomainName() != null) {
            userStoreManagerHolder.put(getMyDomainName().toUpperCase(), this);
        }
    }

	/**
	 * {@inheritDoc}
	 */
	public final String[] getAllSecondaryRoles() throws UserStoreException {
		UserStoreManager secondary = this.getSecondaryUserStoreManager();
		List<String> roleList = new ArrayList<String>();
		while (secondary != null) {
			String[] roles = secondary.getRoleNames(true);
			if (roles != null && roles.length > 0) {
				Collections.addAll(roleList, roles);
			}
			secondary = secondary.getSecondaryUserStoreManager();
		}
		return roleList.toArray(new String[roleList.size()]);
	}

	/**
	 * 
	 * @return
	 */
	public boolean isSCIMEnabled() {
		String scimEnabled = realmConfig
				.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_SCIM_ENABLED);
		if (scimEnabled != null) {
			return Boolean.parseBoolean(scimEnabled);
		} else {
			return false;
		}
	}

	/**
	 * {@inheritDoc}                  doAddInternalRole
	 */
	public final String[] getHybridRoles() throws UserStoreException {
		return hybridRoleManager.getHybridRoles("*");
	}

	/**
	 * {@inheritDoc}
	 */
	public final String[] getRoleNames() throws UserStoreException {
		return getRoleNames(false);
	}

	/**
	 * {@inheritDoc}
	 */
	public final String[] getRoleNames(boolean noHybridRoles) throws UserStoreException {
		return getRoleNames("*", -1, noHybridRoles, true, true);
	}

	/**
	 * 
	 * @param roleName
	 * @param userList
	 * @param permissions
	 * @throws UserStoreException
	 */
	protected void doAddInternalRole(String roleName, String[] userList,
                                                org.wso2.carbon.user.api.Permission [] permissions)
			throws UserStoreException {

		// #################### Domain Name Free Zone Starts Here ################################

		if (hybridRoleManager.isExistingRole(UserCoreUtil.removeDomainFromName(roleName))) {
			throw new UserStoreException("Role name: " + roleName
					+ " in the system. Please pick another role name.");
		}

		hybridRoleManager.addHybridRole(UserCoreUtil.removeDomainFromName(roleName), userList);

		if (permissions != null) {
			for (org.wso2.carbon.user.api.Permission  permission : permissions) {
				String resourceId = permission.getResourceId();
				String action = permission.getAction();
				// This is a special case. We need to pass domain aware name.
				userRealm.getAuthorizationManager().authorizeRole(
						UserCoreUtil.addInternalDomainName(roleName), resourceId, action);
			}
		}

		if ((userList != null) && (userList.length > 0)) {
			clearUserRolesCacheByTenant(this.tenantId);
		}
	}
	
	/**
	 * Returns the set of shared roles which applicable for the logged in tenant
	 * 
	 * @param tenantDomain tenant domain of the shared roles. If this is null, 
     *                     returns all shared roles of available tenant domains
     * @param filter
	 * @param maxItemLimit
	 * @return
	 */
	protected abstract String[] doGetSharedRoleNames(String tenantDomain, String filter, 
                                                     int maxItemLimit) throws UserStoreException;

	/**
	 * TODO This method would returns the role Name actually this must be implemented in interface.
	 * As it is not good to change the API in point release. This has been added to Abstract class
	 * 
	 * @param filter
	 * @param maxItemLimit
	 * @param noInternalRoles
     * @return
	 * @throws UserStoreException
	 */
	public final String[] getRoleNames(String filter, int maxItemLimit, boolean noInternalRoles,
                                                    boolean noSystemRole, boolean  noSharedRoles)
			throws UserStoreException {

		String[] roleList = new String[0];

		if (!noInternalRoles) {
			roleList = hybridRoleManager.getHybridRoles(UserCoreUtil.removeDomainFromName(filter));
		}
        
        if(!noSystemRole){
            String[] systemRoles = systemUserRoleManager.getSystemRoles();
            roleList = UserCoreUtil.combineArrays(roleList, systemRoles);
        }

		int index;
		index = filter.indexOf(CarbonConstants.DOMAIN_SEPARATOR);

		// Check whether we have a secondary UserStoreManager setup.
		if (index > 0) {
			// Using the short-circuit. User name comes with the domain name.
			String domain = filter.substring(0, index);

			UserStoreManager secManager = getSecondaryUserStoreManager(domain);
			if (UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(domain)) {
				return new String[0];
			}
			if (secManager != null) {
				// We have a secondary UserStoreManager registered for this domain.
				filter = filter.substring(index + 1);
				if (secManager instanceof AbstractUserStoreManager) {
                    if(readGroupsEnabled){
                        String[] externalRoles = ((AbstractUserStoreManager) secManager)
                                .doGetRoleNames(filter, maxItemLimit);
                        return UserCoreUtil.combineArrays(roleList, externalRoles);
                    }
				} else {
				    String[] externalRoles = secManager.getRoleNames();
                    return UserCoreUtil.combineArrays(roleList, externalRoles);
				}
			} else {
				throw new UserStoreException("Invalid Domain Name");
			}
		} else if (index == 0) {
            if(readGroupsEnabled){
                String[] externalRoles = doGetRoleNames(filter.substring(index + 1), maxItemLimit);
                return UserCoreUtil.combineArrays(roleList, externalRoles);
            }
		}

        if(readGroupsEnabled){
            String[] externalRoles = doGetRoleNames(filter, maxItemLimit);
            roleList = UserCoreUtil.combineArrays(externalRoles, roleList);
        }

		String primaryDomain = getMyDomainName();

		if (this.getSecondaryUserStoreManager() != null) {
			for (Map.Entry<String, UserStoreManager> entry : userStoreManagerHolder.entrySet()) {
				if (entry.getKey().equalsIgnoreCase(primaryDomain)) {
					continue;
				}
				UserStoreManager storeManager = entry.getValue();
				if (storeManager instanceof AbstractUserStoreManager) {
					try {
                        if(readGroupsEnabled){
                            String[] secondRoleList = ((AbstractUserStoreManager) storeManager)
                                    .doGetRoleNames(filter, maxItemLimit);
                            roleList = UserCoreUtil.combineArrays(roleList, secondRoleList);
                        }
					} catch (UserStoreException e) {
						// We can ignore and proceed. Ignore the results from this user store.
						log.error(e);
					}
				} else {
				    storeManager.getRoleNames();
				}
			}
		}
		return roleList;
	}

	/**
	 * 
	 * @param userName
	 * @param claims
	 * @param domainName
	 * @return
	 * @throws UserStoreException
	 */
	private Map<String, String> doGetUserClaimValues(String userName, String[] claims,
			String domainName, String profileName) throws UserStoreException {

		// Here the user name should be domain-less.
		boolean requireRoles = false;
		boolean requireIntRoles = false;
		boolean requireExtRoles = false;
		String roleClaim = null;

        if(profileName == null || profileName.trim().length() == 0){
            profileName = UserCoreConstants.DEFAULT_PROFILE;
        }

		Set<String> propertySet = new HashSet<String>();
		for (String claim : claims) {

			// There can be cases some claim values being requested for claims
			// we don't have.
			String property = null;
			try {
	            property = getClaimAtrribute(claim, userName, domainName);
            } catch (org.wso2.carbon.user.api.UserStoreException e) {
	            throw new UserStoreException(e);
            }
			if (property != null
					&& (!UserCoreConstants.ROLE_CLAIM.equalsIgnoreCase(claim)
							|| !UserCoreConstants.INT_ROLE_CLAIM.equalsIgnoreCase(claim) ||
                               !UserCoreConstants.EXT_ROLE_CLAIM.equalsIgnoreCase(claim))) {
				propertySet.add(property);
			}

			if (UserCoreConstants.ROLE_CLAIM.equalsIgnoreCase(claim)) {
				requireRoles = true;
				roleClaim = claim;
			} else if (UserCoreConstants.INT_ROLE_CLAIM.equalsIgnoreCase(claim)) {
				requireIntRoles = true;
				roleClaim = claim;
			} else if (UserCoreConstants.EXT_ROLE_CLAIM.equalsIgnoreCase(claim)) {
				requireExtRoles = true;
				roleClaim = claim;
			}
		}

		String[] properties = propertySet.toArray(new String[propertySet.size()]);
		Map<String, String> uerProperties = this.getUserPropertyValues(userName, properties,
				profileName);

		List<String> getAgain = new ArrayList<String>();
		Map<String, String> finalValues = new HashMap<String, String>();

		for (String claim : claims) {
			ClaimMapping mapping;
			try {
				mapping = (ClaimMapping) claimManager.getClaimMapping(claim);
			} catch (org.wso2.carbon.user.api.UserStoreException e) {
				throw new UserStoreException(e);
			}
			if (mapping != null) {
				String property = null;

				if (domainName != null) {
					Map<String, String> attrMap = mapping.getMappedAttributes();
					if (attrMap != null) {
						String attr = null;
						if ((attr = attrMap.get(domainName.toUpperCase())) != null) {
							property = attr;
						} else {
							property = mapping.getMappedAttribute();
						}
					}
				} else {
					property = mapping.getMappedAttribute();
				}

				String value = uerProperties.get(property);

				if (profileName.equals(UserCoreConstants.DEFAULT_PROFILE)) {
					// Check whether we have a value for the requested attribute
					if (value != null && value.trim().length() > 0) {
						finalValues.put(claim, value);
					}
				} else {
					if (value != null && value.trim().length() > 0) {
						finalValues.put(claim, value);
					}
				}
			}
		}

		if (getAgain.size() > 0) {
			// oh the beautiful recursion
			Map<String, String> mapClaimValues = this.getUserClaimValues(userName,
					(String[]) getAgain.toArray(new String[getAgain.size()]),
					profileName);

			Iterator<Map.Entry<String, String>> ite3 = mapClaimValues.entrySet().iterator();
			while (ite3.hasNext()) {
				Map.Entry<String, String> entry = ite3.next();
				if (entry.getValue() != null) {
					finalValues.put(entry.getKey(), entry.getValue());
				}
			}
		}

		// We treat roles claim in special way.
		String[] roles = null;

		if (requireRoles) {
			roles = getRoleListOfUser(userName);
		} else if (requireIntRoles) {
			roles = doGetInternalRoleListOfUser(userName, "*");
		} else if (requireExtRoles) {

			List<String> rolesList = new ArrayList<String>();
			String[] externalRoles = doGetExternalRoleListOfUser(userName, "*");
			rolesList.addAll(Arrays.asList(externalRoles));
            //if only shared enable
            if(isSharedGroupEnabled()){
                String[] sharedRoles = doGetSharedRoleListOfUser(userName, null, "*");
                if (sharedRoles != null) {
                    rolesList.addAll(Arrays.asList(sharedRoles));
                }
            }

			roles = rolesList.toArray(new String[rolesList.size()]);
		}

		if (roles != null && roles.length > 0) {
			String delim = "";
			StringBuffer roleBf = new StringBuffer();
			for (String role : roles) {
				roleBf.append(delim).append(role);
				delim = ",";
			}
			finalValues.put(roleClaim, roleBf.toString());
		}

		return finalValues;
	}

	/**
	 * 
	 * @return
	 */
	protected String getEveryOneRoleName() {
		return realmConfig.getEveryOneRoleName();
	}

	/**
	 * 
	 * @return
	 */
	protected String getAdminRoleName() {
		return realmConfig.getAdminRoleName();
	}

	/**
	 * 
	 * @param credential
	 * @return
	 * @throws UserStoreException
	 */
	protected boolean checkUserPasswordValid(Object credential) throws UserStoreException {

		if (credential == null) {
			return false;
		}

		if (!(credential instanceof String)) {
			throw new UserStoreException("Can handle only string type credentials");
		}

		String password = ((String) credential).trim();

		if (password.length() < 1) {
			return false;
		}

		String regularExpression = realmConfig
				.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_JAVA_REG_EX);
		return regularExpression == null || isFormatCorrect(regularExpression, password);
	}

	/**
	 * 
	 * @param userName
	 * @return
	 * @throws UserStoreException
	 */
	protected boolean checkUserNameValid(String userName) throws UserStoreException {

		if (userName == null || CarbonConstants.REGISTRY_SYSTEM_USERNAME.equals(userName)) {
			return false;
		}

		userName = userName.trim();

		if (userName.length() < 1) {
			return false;
		}

        String regularExpression = realmConfig
				.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USER_NAME_JAVA_REG_EX);
        
        if (MultitenantUtils.isEmailUserName()) {
            regularExpression = realmConfig
                    .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USER_NAME_WITH_EMAIL_JS_REG_EX);
            if (regularExpression == null) {
                regularExpression = UserCoreConstants.RealmConfig.EMAIL_VALIDATION_REGEX;
            }
        }
        
        if (regularExpression != null){
             regularExpression = regularExpression.trim();
        }

		return regularExpression == null || regularExpression.equals("")
				|| isFormatCorrect(regularExpression, userName);

	}

	/**
	 * 
	 * @param roleName
	 * @return
	 */
	protected boolean isRoleNameValid(String roleName) {
		if (roleName == null) {
			return false;
		}

		if (roleName.length() < 1) {
			return false;
		}

		String regularExpression = realmConfig
				.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_ROLE_NAME_JAVA_REG_EX);
		if (regularExpression != null) {
			if (!isFormatCorrect(regularExpression, roleName)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * 
	 * @param tenantID
	 * @param userName
	 * @return
	 */
	protected String[] getRoleListOfUserFromCache(int tenantID, String userName) {
		if (userRolesCache != null) {
			return userRolesCache.getRolesListOfUser(cacheIdentifier, tenantID, userName);
		}
		return null;
	}

	/**
	 * 
	 * @param tenantID
	 */
	protected void clearUserRolesCacheByTenant(int tenantID) {
		if (userRolesCache != null) {
			userRolesCache.clearCacheByTenant(tenantID);
		}
		AuthorizationCache authorizationCache = AuthorizationCache.getInstance();
		authorizationCache.clearCacheByTenant(tenantID);
	}

    /**
     *
     * @param userName
     */
    protected void clearUserRolesCache(String userName) {
        if (userRolesCache != null) {
            userRolesCache.clearCacheEntry(cacheIdentifier, tenantId, userName);
        }
        AuthorizationCache authorizationCache = AuthorizationCache.getInstance();
        authorizationCache.clearCacheByUser(tenantId, userName);
    }

	/**
	 * 
	 * @param tenantID
	 * @param userName
	 * @param roleList
	 */
	protected void addToUserRolesCache(int tenantID, String userName, String[] roleList) {
		if (userRolesCache != null) {
			userRolesCache.addToCache(cacheIdentifier, tenantID, userName, roleList);
			AuthorizationCache authorizationCache = AuthorizationCache.getInstance();
			authorizationCache.clearCacheByTenant(tenantID);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected void initUserRolesCache() {

		String userRolesCacheEnabledString = (realmConfig
				.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_ROLES_CACHE_ENABLED));

		String userCoreCacheIdentifier = realmConfig
				.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USER_CORE_CACHE_IDENTIFIER);

		if (userCoreCacheIdentifier != null && userCoreCacheIdentifier.trim().length() > 0) {
			cacheIdentifier = userCoreCacheIdentifier;
		} else {
            cacheIdentifier = UserCoreConstants.DEFAULT_CACHE_IDENTIFIER;
        }

		if (userRolesCacheEnabledString != null && !userRolesCacheEnabledString.equals("")) {
			userRolesCacheEnabled = Boolean.parseBoolean(userRolesCacheEnabledString);
			if (log.isDebugEnabled()) {
				log.debug("User Roles Cache is configured to:" + userRolesCacheEnabledString);
			}
		} else {
			if (log.isDebugEnabled()) {
				log.info("User Roles Cache is not configured. Default value: "
						+ userRolesCacheEnabled + " is taken.");
			}
		}

		if (userRolesCacheEnabled) {
            int timeOut = UserCoreConstants.USER_ROLE_CACHE_DEFAULT_TIME_OUT;
            String timeOutString = realmConfig.
                getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USER_ROLE_CACHE_TIME_OUT);
            if(timeOutString != null){
                timeOut = Integer.parseInt(timeOutString);
            }
			userRolesCache = UserRolesCache.getInstance();
            userRolesCache.setTimeOut(timeOut);
		}

	}

	/**
	 * 
	 * @param regularExpression
	 * @param attribute
	 * @return
	 */
	private boolean isFormatCorrect(String regularExpression, String attribute) {
		Pattern p2 = Pattern.compile(regularExpression);
		Matcher m2 = p2.matcher(attribute);
		return m2.matches();
	}

	/**
	 * This is to replace escape characters in user name at user login if replace escape characters
	 * enabled in user-mgt.xml. Some User Stores like ApacheDS stores user names by replacing escape
	 * characters. In that case, we have to parse the username accordingly.
	 * 
	 * @param userName
	 */
	protected String replaceEscapeCharacters(String userName) {
		
		if(log.isDebugEnabled()) {
			log.debug("Replacing excape characters in " + userName);
		}
		String replaceEscapeCharactersAtUserLoginString = realmConfig
				.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_REPLACE_ESCAPE_CHARACTERS_AT_USER_LOGIN);

		if (replaceEscapeCharactersAtUserLoginString != null) {
			replaceEscapeCharactersAtUserLogin = Boolean
					.parseBoolean(replaceEscapeCharactersAtUserLoginString);
			if (log.isDebugEnabled()) {
				log.debug("Replace escape characters at userlogin is condifured to: "
						+ replaceEscapeCharactersAtUserLoginString);
			}
			if (replaceEscapeCharactersAtUserLogin) {
				// Currently only '\' & '\\' are identified as escape characters
				// that needs to be
				// replaced.
				return userName.replaceAll("\\\\", "\\\\\\\\");
			}
		}
		return userName;
	}

	/**
	 * TODO: Remove this method. We should not use DTOs
	 * 
	 * @return
	 * @throws UserStoreException
	 */
	public RoleDTO[] getAllSecondaryRoleDTOs() throws UserStoreException {
		UserStoreManager secondary = this.getSecondaryUserStoreManager();
		List<RoleDTO> roleList = new ArrayList<RoleDTO>();
		while (secondary != null) {
			String domain = secondary.getRealmConfiguration().getUserStoreProperty(
					UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
			String[] roles = secondary.getRoleNames(true);
			if (roles != null && roles.length > 0) {
				Collections.addAll(roleList, UserCoreUtil.convertRoleNamesToRoleDTO(roles, domain));
			}
			secondary = secondary.getSecondaryUserStoreManager();
		}
		return roleList.toArray(new RoleDTO[roleList.size()]);
	}

	/**
	 * 
	 * @param roleName
	 * @param userList
	 * @param permissions
	 * @throws UserStoreException
	 */
	public void addSystemRole(String roleName, String[] userList, Permission[] permissions)
			throws UserStoreException {

		if (!isRoleNameValid(roleName)) {
			String regEx = realmConfig
					.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_ROLE_NAME_JAVA_REG_EX);
			throw new UserStoreException(
					"Role name not valid. Role name must be a non null string with following format, "
							+ regEx);
		}

		if (systemUserRoleManager.isExistingRole(roleName)) {
			throw new UserStoreException("Role name: " + roleName
					+ " in the system. Please pick another role name.");
		}
		systemUserRoleManager.addSystemRole(roleName, userList);
	}


	/**
	 * 
	 * 
	 * @param roleName
	 * @param filter
	 * @return
	 * @throws UserStoreException
	 */
	protected abstract String[] doGetUserListOfRole(String roleName, String filter)
			throws UserStoreException;

	/**
	 * 
	 * 
	 * @param userName
	 * @param filter
	 * @return
	 * @throws UserStoreException
	 */
	public final String[] doGetRoleListOfUser(String userName, String filter)
			throws UserStoreException {

        String[] roleList;

		String[] internalRoles = doGetInternalRoleListOfUser(userName, filter);

		String[] modifiedExternalRoleList = new String[0];

		if (readGroupsEnabled && doCheckExistingUser(userName)) {
			List<String> roles = new ArrayList<String>();
			String[] externalRoles = doGetExternalRoleListOfUser(userName, "*");
			roles.addAll(Arrays.asList(externalRoles));
            if(isSharedGroupEnabled()){
                String[] sharedRoles = doGetSharedRoleListOfUser(userName, null, "*");
                if (sharedRoles != null) {
                    roles.addAll(Arrays.asList(sharedRoles));
                }
            }
			modifiedExternalRoleList =
			                           UserCoreUtil.addDomainToNames(roles.toArray(new String[roles.size()]),
			                                                         getMyDomainName());
		}

		roleList = UserCoreUtil.combine(internalRoles, Arrays.asList(modifiedExternalRoleList));

        addToUserRolesCache(this.tenantId,
                UserCoreUtil.addDomainToName(userName, getMyDomainName()), roleList);

		return roleList;
	}

	/**
	 * 
	 * @param filter
	 * @return
	 * @throws UserStoreException
	 */
	public final String[] getHybridRoles(String filter) throws UserStoreException {
		return hybridRoleManager.getHybridRoles(filter);
	}

	/**
	 * 
	 * @param claimList
	 * @return
	 * @throws UserStoreException
	 */
	protected List<String> getMappingAttributeList(List<String> claimList)
			throws UserStoreException {
		ArrayList<String> attributeList = null;
		Iterator<String> claimIter = null;

		attributeList = new ArrayList<String>();
		if (claimList == null) {
			return attributeList;
		}
		claimIter = claimList.iterator();
		while (claimIter.hasNext()) {
			try {
				attributeList.add(claimManager.getAttributeName(claimIter.next()));
			} catch (org.wso2.carbon.user.api.UserStoreException e) {
				throw new UserStoreException(e);
			}
		}
		return attributeList;
	}

    protected  void doInitialSetup() throws UserStoreException {
        systemUserRoleManager = new SystemUserRoleManager(dataSource, tenantId);
        hybridRoleManager = new HybridRoleManager(dataSource, tenantId, realmConfig, userRealm);
    }
    /**
     *
     * @throws UserStoreException
     * @return whether this is the initial startup
     */
    protected void doInitialUserAdding() throws UserStoreException {

        String systemUser = UserCoreUtil.removeDomainFromName(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME);
        String systemRole = UserCoreUtil.removeDomainFromName(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME);

        if (!systemUserRoleManager.isExistingSystemUser(systemUser)) {
            systemUserRoleManager.addSystemUser(systemUser,
                    UserCoreUtil.getPolicyFriendlyRandomPassword(systemUser), null);
        }

        if (!systemUserRoleManager.isExistingRole(systemRole)) {
            systemUserRoleManager.addSystemRole(systemRole, new String[]{systemUser});
        }

        if (!hybridRoleManager.isExistingRole(UserCoreUtil.removeDomainFromName(realmConfig
                .getEveryOneRoleName()))) {
            hybridRoleManager.addHybridRole(
                    UserCoreUtil.removeDomainFromName(realmConfig.getEveryOneRoleName()), null);
        }
    }




    protected boolean isInitSetupDone() throws UserStoreException {

        boolean isInitialSetUp = false;
        String systemUser = UserCoreUtil.removeDomainFromName(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME);
        String systemRole = UserCoreUtil.removeDomainFromName(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME);

        if (systemUserRoleManager.isExistingSystemUser(systemUser)) {
            isInitialSetUp = true;
        }

        if (systemUserRoleManager.isExistingRole(systemRole)) {
            isInitialSetUp = true;
        }

        return isInitialSetUp;
    }

    /**
     *
     * @throws UserStoreException
     */
    protected void addInitialAdminData(boolean addAdmin, boolean initialSetup) throws UserStoreException {

        if (realmConfig.getAdminRoleName() == null || realmConfig.getAdminUserName() == null) {
            log.error("Admin user name or role name is not valid. Please provide valid values.");
            throw new UserStoreException(
                    "Admin user name or role name is not valid. Please provide valid values.");
        }
        String adminUserName = UserCoreUtil.removeDomainFromName(realmConfig.getAdminUserName());
        String adminRoleName = UserCoreUtil.removeDomainFromName(realmConfig.getAdminRoleName());
        boolean userExist = false;
        boolean roleExist = false;
        boolean isInternalRole = false;

        try{
        	if(Boolean.parseBoolean(this.getRealmConfiguration().getUserStoreProperty(
        			UserCoreConstants.RealmConfig.READ_GROUPS_ENABLED))){
            	roleExist = doCheckExistingRole(adminRoleName);
        	}
        } catch (Exception e){
            //ignore
        }

        if(!roleExist){
            try{
                roleExist = hybridRoleManager.isExistingRole(adminRoleName);
            } catch (Exception e){
                //ignore
            }
            if(roleExist){
                isInternalRole = true;
            }
        }

        try{
            userExist = doCheckExistingUser(adminUserName);
        } catch (Exception e){
            //ignore
        }

        if (!userExist) {
            if (isReadOnly()) {
                String message = "Admin user can not be created in primary user store. " +
                        "User store is read only. " +
                        "Please pick a user name which is exist in the primary user store as Admin user";
                if(initialSetup){
                    throw new UserStoreException(message);
                } else if(log.isDebugEnabled()){
                    log.error(message);
                }
            } else if(addAdmin){
                try {
                    this.doAddUser(adminUserName, realmConfig.getAdminPassword(),
                            null, null, null, false);
                } catch (Exception e){
                    String message = "Admin user has not been created. " +
                            "Error occurs while creating Admin user in primary user store." ;
                    if(initialSetup){
                        throw new UserStoreException(message, e);
                    } else if(log.isDebugEnabled()){
                        log.error(message, e);
                    }
                }
            } else {
                if(initialSetup){
                    String message = "Admin user can not be created in primary user store. " +
                            "Add-Admin has been set to false. " +
                            "Please pick a User name which is exist in the primary user store as Admin user";
                    if(initialSetup){
                        throw new UserStoreException(message);
                    } else if(log.isDebugEnabled()){
                        log.error(message);
                    }
                }
            }
        }
        

        if (!roleExist) {
            if(addAdmin){
                if(!isReadOnly() && writeGroupsEnabled){
                    try {
                        this.doAddRole(adminRoleName, new String[]{adminUserName}, false);
                    } catch (org.wso2.carbon.user.api.UserStoreException e) {
                        String message = "Admin role has not been created. " +
                                "Error occurs while creating Admin role in primary user store." ;
                        if(initialSetup){
                            throw new UserStoreException(message, e);
                        } else if(log.isDebugEnabled()){
                            log.error(message, e);
                        }
                    }
                } else {
                    // creates internal role
                    try{
                        hybridRoleManager.addHybridRole(adminRoleName, new String[]{adminUserName});
                        isInternalRole = true;
                    } catch (Exception e){
                        String message = "Admin role has not been created. " +
                                "Error occurs while creating Admin role in primary user store.";
                        if(initialSetup){
                            throw new UserStoreException(message, e);
                        } else if(log.isDebugEnabled()){
                            log.error(message, e);
                        }
                    }
                }
            } else {
                String message = "Admin role can not be created in primary user store. " +
                        "Add-Admin has been set to false. " +
                        "Please pick a Role name which is exist in the primary user store as Admin Role";
                if(initialSetup){
                    throw new UserStoreException(message);
                } else if(log.isDebugEnabled()){
                    log.error(message);
                }
            }
        }


        if(isInternalRole){
            if(!hybridRoleManager.isUserInRole(adminUserName, adminRoleName)){
                try{
                    hybridRoleManager.updateHybridRoleListOfUser(adminUserName, null,
                            new String[] { adminRoleName });
                } catch (Exception e){
                    String message = "Admin user has not been assigned to Admin role. " +
                            "Error while assignment is done";
                    if(initialSetup){
                        throw new UserStoreException(message, e);
                    } else if(log.isDebugEnabled()){
                        log.error(message, e);
                    }
                }
            }
            realmConfig.setAdminRoleName(UserCoreUtil.addInternalDomainName(adminRoleName));
        } else if(!isReadOnly() && writeGroupsEnabled){
            if(!this.doCheckIsUserInRole(adminUserName, adminRoleName)){
                if(addAdmin){
                    try {
                        this.doUpdateRoleListOfUser(adminUserName, null,
                                new String[] { adminRoleName });
                    } catch (Exception e){
                        String message = "Admin user has not been assigned to Admin role. " +
                                "Error while assignment is done";
                        if(initialSetup){
                            throw new UserStoreException(message, e);
                        } else if(log.isDebugEnabled()){
                            log.error(message, e);
                        }
                    }
                } else {
                    String message = "Admin user can not be assigned to Admin role " +
                            "Add-Admin has been set to false. Please do the assign it in user store level";
                    if(initialSetup){
                        throw new UserStoreException(message);
                    } else if(log.isDebugEnabled()){
                        log.error(message);
                    }
                }
            }
        }

        doInitialUserAdding();
    }
	/**
	 * 
	 * @param type
	 * @return
	 * @throws UserStoreException
	 */
	public Map<String, Integer> getMaxListCount(String type) throws UserStoreException {

		if (!type.equals(UserCoreConstants.RealmConfig.PROPERTY_MAX_USER_LIST)
				&& !type.equals(UserCoreConstants.RealmConfig.PROPERTY_MAX_ROLE_LIST)) {
			throw new UserStoreException("Invalid count parameter");
		}

		if (type.equals(UserCoreConstants.RealmConfig.PROPERTY_MAX_USER_LIST)
				&& maxUserListCount != null) {
			return maxUserListCount;
		}

		if (type.equals(UserCoreConstants.RealmConfig.PROPERTY_MAX_ROLE_LIST)
				&& maxRoleListCount != null) {
			return maxRoleListCount;
		}

		Map<String, Integer> maxListCount = new HashMap<String, Integer>();
        for (Map.Entry<String, UserStoreManager> entry : userStoreManagerHolder.entrySet()) {
            UserStoreManager storeManager = entry.getValue();
            String maxConfig = storeManager.getRealmConfiguration().getUserStoreProperty(type);

            if (maxConfig == null) {
                // set a default value
                maxConfig = MAX_LIST_LENGTH;
            }
            maxListCount.put(entry.getKey(), Integer.parseInt(maxConfig));
        }

		if (realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME) == null) {
			String maxConfig = realmConfig.getUserStoreProperty(type);
			if (maxConfig == null) {
				// set a default value
				maxConfig = MAX_LIST_LENGTH;
			}
			maxListCount.put(null, Integer.parseInt(maxConfig));
		}

		if (type.equals(UserCoreConstants.RealmConfig.PROPERTY_MAX_USER_LIST)) {
			this.maxUserListCount = maxListCount;
			return this.maxUserListCount;
		} else if (type.equals(UserCoreConstants.RealmConfig.PROPERTY_MAX_ROLE_LIST)) {
			this.maxRoleListCount = maxListCount;
			return this.maxRoleListCount;
		} else {
			throw new UserStoreException("Invalid count parameter");
		}
	}

	/**
	 * 
	 * @return
	 */
	protected String getMyDomainName() {
		return UserCoreUtil.getDomainName(realmConfig);
	}

    protected void persistDomain() throws UserStoreException {
        String domain = UserCoreUtil.getDomainName(this.realmConfig);
        if (domain != null) {
            UserCoreUtil.persistDomain(domain, this.tenantId, this.dataSource);
        }
    }

    public void deletePersistedDomain(String domain) throws UserStoreException {
        if (domain != null) {
        	if(log.isDebugEnabled()) {
        		log.debug("Deleting persisted domain " + domain);
        	}
            UserCoreUtil.deletePersistedDomain(domain, this.tenantId, this.dataSource);
        }
    }
    
    public void updatePersistedDomain(String oldDomain, String newDomain) throws UserStoreException {
        if (oldDomain != null && newDomain != null) {
        	// Checks for the newDomain exists already
        	// Traverse through realm configuration chain since USM chain doesn't contains the disabled USMs
        	RealmConfiguration realmConfigTmp = this.getRealmConfiguration();
        	while (realmConfigTmp != null) {
        		String domainName = realmConfigTmp.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
        		if (newDomain.equalsIgnoreCase(domainName)) {
        			throw new UserStoreException("Cannot update persisted domain name "+oldDomain+" into "+newDomain+". New domain name already in use");
        		}
        		realmConfigTmp = realmConfigTmp.getSecondaryRealmConfig();
        	}
        	
        	if(log.isDebugEnabled()) {
        		log.debug("Renaming persisted domain " + oldDomain + " to " + newDomain);
        	}
            UserCoreUtil.updatePersistedDomain(oldDomain, newDomain, this.tenantId, this.dataSource);

        }
    }

	/**
	 * Checks whether the role is a shared role or not
	 * 
	 * @param roleName
	 * @param roleNameBase
	 * @return
	 */
	public boolean isSharedRole(String roleName, String roleNameBase) {

		// Only checks the shared groups are enabled
		return isSharedGroupEnabled();
	}	
	
	/**
	 * Checks whether the provided role name belongs to the logged in tenant.
	 * This check is done using the domain name which is appended at the end of
	 * the role name
	 * 
	 * @param roleName
	 * @return
	 */
	protected boolean isOwnRole(String roleName) {
		return true;
	}

	@Override
    public void addRole(String roleName, String[] userList,
                        org.wso2.carbon.user.api.Permission[] permissions)
                                              throws org.wso2.carbon.user.api.UserStoreException {
		addRole(roleName, userList, permissions, false);
	    
    }

    public boolean isOthersSharedRole(String roleName) {
	    return false;
    }
    public void notifyListeners(String domainName) {
        for (UserStoreManagerConfigurationListener aListener : listener) {
            aListener.propertyChange(domainName);
        }
    }

    public void addChangeListener(UserStoreManagerConfigurationListener newListener) {
        listener.add(newListener);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private UserStoreManager createSecondaryUserStoreManager(RealmConfiguration realmConfig,
                                                    UserRealm realm) throws UserStoreException {

        // setting global realm configurations such as everyone role, admin role and admin user
        realmConfig.setEveryOneRoleName(this.realmConfig.getEveryOneRoleName());
        realmConfig.setAdminUserName(this.realmConfig.getAdminUserName());
        realmConfig.setAdminRoleName(this.realmConfig.getAdminRoleName());

        String className = realmConfig.getUserStoreClass();
        if (className == null) {
            String errmsg = "Unable to add user store. UserStoreManager class name is null.";
            log.error(errmsg);
            throw new UserStoreException(errmsg);
        }

        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put(UserCoreConstants.DATA_SOURCE, this.dataSource);
        properties.put(UserCoreConstants.FIRST_STARTUP_CHECK, false);
        
        Class[] initClassOpt1 = new Class[]{RealmConfiguration.class, Map.class,
                ClaimManager.class, ProfileConfigurationManager.class, UserRealm.class,
                Integer.class};
        Object[] initObjOpt1 = new Object[]{realmConfig, properties, realm.getClaimManager(), null, realm,
                tenantId};

	// These two methods won't be used
        Class[] initClassOpt2 = new Class[]{RealmConfiguration.class, Map.class,
                ClaimManager.class, ProfileConfigurationManager.class, UserRealm.class};
        Object[] initObjOpt2 = new Object[]{realmConfig, properties, realm.getClaimManager(), null, realm};

        Class[] initClassOpt3 = new Class[]{RealmConfiguration.class, Map.class};
        Object[] initObjOpt3 = new Object[]{realmConfig, properties};

        try {
            Class clazz = Class.forName(className);
            Constructor constructor = null;
            Object newObject = null;

            if (log.isDebugEnabled()) {
                log.debug("Start initializing class with the first option");
            }

            try {
                constructor = clazz.getConstructor(initClassOpt1);
                newObject = constructor.newInstance(initObjOpt1);
                return (UserStoreManager) newObject;
            } catch (NoSuchMethodException e) {
		// if not found try again.
                if (log.isDebugEnabled()) {
                    log.debug("Cannont initialize " + className + " using the option 1");
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("End initializing class with the first option");
            }

            try {
                constructor = clazz.getConstructor(initClassOpt2);
                newObject = constructor.newInstance(initObjOpt2);
                return (UserStoreManager) newObject;
            } catch (NoSuchMethodException e) {
		// if not found try again.
                if (log.isDebugEnabled()) {
                    log.debug("Cannont initialize " + className + " using the option 2");
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("End initializing class with the second option");
            }

            try {
                constructor = clazz.getConstructor(initClassOpt3);
                newObject = constructor.newInstance(initObjOpt3);
                return (UserStoreManager) newObject;
            } catch (NoSuchMethodException e) {
		// cannot initialize in any of the methods. Throw exception.
                String message = "Cannot initialize " + className + ". Error " + e.getMessage();
                log.error(message);
                throw new UserStoreException(message);
            }

        } catch (Throwable e) {
            log.error("Cannot create " + className, e);
            throw new UserStoreException(e.getMessage() + "Type " + e.getClass(), e);
        }

    }

    /**
     * Adding new User Store Manager to USM chain
     * 
     * @param userStoreRealmConfig
     * @param realm 
     * @throws UserStoreException
     */
    public void addSecondaryUserStoreManager(RealmConfiguration userStoreRealmConfig,
                                                    UserRealm realm) throws UserStoreException {
    	// Creating new UserStoreManager
        UserStoreManager manager = createSecondaryUserStoreManager(userStoreRealmConfig, realm);

        String domainName = userStoreRealmConfig
                .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);

        if (domainName != null) {
            if (this.getSecondaryUserStoreManager(domainName) != null) {
                String errmsg = "Could not initialize new user store manager : "+domainName
                        + " Duplicate domain names not allowed.";
                if(log.isDebugEnabled()) {
                	log.debug(errmsg);
                }
                throw new UserStoreException(errmsg);
            } else {
            	Boolean isDisabled = false;
                if (userStoreRealmConfig
                        .getUserStoreProperty(UserCoreConstants.RealmConfig.USER_STORE_DISABLED) != null) {
                    isDisabled = Boolean
                            .parseBoolean(userStoreRealmConfig
                                    .getUserStoreProperty(UserCoreConstants.RealmConfig.USER_STORE_DISABLED));
                    if (isDisabled) {
                        log.warn("Secondary user store disabled with domain "
                                + domainName + ".");
                    }
                    else {
                    	// Fulfilled requirements for adding UserStore,
                    	
                    	// Now adding UserStoreManager to end of the UserStoreManager chain
                        UserStoreManager tmpUserStoreManager = this;
                        while (tmpUserStoreManager.getSecondaryUserStoreManager() != null) {
                            tmpUserStoreManager = tmpUserStoreManager
                                    .getSecondaryUserStoreManager();
                        }
                        tmpUserStoreManager.setSecondaryUserStoreManager(manager);

                        // update domainName-USM map to retrieve USM directly by its domain name
                        this.addSecondaryUserStoreManager(domainName.toUpperCase(),
                                tmpUserStoreManager.getSecondaryUserStoreManager());

                        if (log.isDebugEnabled()) {
                            log.debug("UserStoreManager : " + domainName
                                    + "added to the list");
                        }
                    }
                }
            }
        } else {
            log.warn("Could not initialize new user store manager.  "
                    + "Domain name is not defined");
        }
    }

    /**
     * Remove given User Store Manager from USM chain
     * 
     * @param userStoreDomainName
     * @throws UserStoreException
     */
    public void removeSecondaryUserStoreManager(String userStoreDomainName) throws UserStoreException {
    	
    	if(userStoreDomainName == null) {
    		throw new UserStoreException("Cannot remove user store. User store domain name is null");
    	}
    	if("".equals(userStoreDomainName)) {
    		throw new UserStoreException("Cannot remove user store. User store domain name is empty");
    	}  
//    	if(!this.userStoreManagerHolder.containsKey(userStoreDomainName.toUpperCase())) {
//    		throw new UserStoreException("Cannot remove user store. User store domain name does not exists");
//    	}
    	
    	userStoreDomainName = userStoreDomainName.toUpperCase();
    	
    	boolean isUSMContainsInMap = false;
    	if(this.userStoreManagerHolder.containsKey(userStoreDomainName.toUpperCase())) {
    		isUSMContainsInMap = true;
        	this.userStoreManagerHolder.remove(userStoreDomainName.toUpperCase());
        	if (log.isDebugEnabled()) {
                log.debug("UserStore: " + userStoreDomainName+ " removed from map");
            }
    	}
    	
    	boolean isUSMConatainsInChain = false;
        UserStoreManager prevUserStoreManager = this;
        while (prevUserStoreManager.getSecondaryUserStoreManager() != null) {
            UserStoreManager secondaryUSM =  prevUserStoreManager.getSecondaryUserStoreManager();
            if (secondaryUSM.getRealmConfiguration().getUserStoreProperty(UserStoreConfigConstants.DOMAIN_NAME).equalsIgnoreCase(userStoreDomainName)) {
            	isUSMConatainsInChain = true;
            	// Omit deleting user store manager from the chain
            	prevUserStoreManager.setSecondaryUserStoreManager(secondaryUSM.getSecondaryUserStoreManager());
                log.info("User store: " + userStoreDomainName + " of tenant:" + tenantId + " is removed from user store chain.");
                return;
            }
            prevUserStoreManager = secondaryUSM;
        }
        
	    if(!isUSMContainsInMap && isUSMConatainsInChain ) {
	       	throw new UserStoreException("Removed user store manager : "+userStoreDomainName+" didnt exists in userStoreManagerHolder map");
	    }
        else if(isUSMContainsInMap && !isUSMConatainsInChain) {
        	throw new UserStoreException("Removed user store manager : "+userStoreDomainName+" didnt exists in user store manager chain");
        }
    }

    public HybridRoleManager getInternalRoleManager() {
        return hybridRoleManager;
    }
}
