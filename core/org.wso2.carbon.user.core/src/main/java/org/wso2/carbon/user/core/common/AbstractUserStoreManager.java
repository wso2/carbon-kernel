/*
 * Copyright (c) 2005-2024, WSO2 LLC. (http://www.wso2.com).
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
package org.wso2.carbon.user.core.common;

import java.util.Objects;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.CircuitBreakerOpenException;
import org.wso2.carbon.user.core.NotImplementedException;
import org.wso2.carbon.user.core.PaginatedUserStoreManager;
import org.wso2.carbon.user.core.Permission;
import org.wso2.carbon.user.core.UniqueIDUserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreClientException;
import org.wso2.carbon.user.core.UserStoreConfigConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.authorization.AuthorizationCache;
import org.wso2.carbon.user.core.claim.Claim;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.claim.ClaimMapping;
import org.wso2.carbon.user.core.config.UserStorePreferenceOrderSupplier;
import org.wso2.carbon.user.core.constants.UserCoreClaimConstants;
import org.wso2.carbon.user.core.constants.UserCoreErrorConstants;
import org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages;
import org.wso2.carbon.user.core.dto.RoleDTO;
import org.wso2.carbon.user.core.hash.HashProvider;
import org.wso2.carbon.user.core.hybrid.HybridRoleManager;
import org.wso2.carbon.user.core.internal.UMListenerServiceComponent;
import org.wso2.carbon.user.core.internal.UserStoreMgtDSComponent;
import org.wso2.carbon.user.core.internal.UserStoreMgtDataHolder;
import org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager;
import org.wso2.carbon.user.core.ldap.LDAPConstants;
import org.wso2.carbon.user.core.listener.GroupResolver;
import org.wso2.carbon.user.core.listener.GroupManagementErrorEventListener;
import org.wso2.carbon.user.core.listener.GroupOperationEventListener;
import org.wso2.carbon.user.core.listener.SecretHandleableListener;
import org.wso2.carbon.user.core.listener.UniqueIDUserManagementErrorEventListener;
import org.wso2.carbon.user.core.listener.UniqueIDUserOperationEventListener;
import org.wso2.carbon.user.core.listener.UserManagementErrorEventListener;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;
import org.wso2.carbon.user.core.listener.UserStoreManagerConfigurationListener;
import org.wso2.carbon.user.core.listener.UserStoreManagerListener;
import org.wso2.carbon.user.core.model.Condition;
import org.wso2.carbon.user.core.model.ExpressionAttribute;
import org.wso2.carbon.user.core.model.ExpressionCondition;
import org.wso2.carbon.user.core.model.ExpressionOperation;
import org.wso2.carbon.user.core.model.OperationalCondition;
import org.wso2.carbon.user.core.model.OperationalOperation;
import org.wso2.carbon.user.core.model.UniqueIDUserClaimSearchEntry;
import org.wso2.carbon.user.core.model.UserClaimSearchEntry;
import org.wso2.carbon.user.core.model.UserMgtContext;
import org.wso2.carbon.user.core.profile.ProfileConfigurationManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.system.SystemUserRoleManager;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.Secret;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.utils.UnsupportedSecretTypeException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.CharBuffer;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sql.DataSource;

import static org.wso2.carbon.user.core.UserCoreConstants.ClaimTypeURIs.IDENTITY_CLAIM_URI;
import static org.wso2.carbon.user.core.UserCoreConstants.DOMAIN_SEPARATOR;
import static org.wso2.carbon.user.core.UserCoreConstants.INTERNAL_DOMAIN;
import static org.wso2.carbon.user.core.UserCoreConstants.INTERNAL_SYSTEM_ROLE_PREFIX;
import static org.wso2.carbon.user.core.UserCoreConstants.INTERNAL_ROLES_CLAIM;
import static org.wso2.carbon.user.core.UserCoreConstants.ROLE_CLAIM;
import static org.wso2.carbon.user.core.UserCoreConstants.SYSTEM_DOMAIN_NAME;
import static org.wso2.carbon.user.core.UserCoreConstants.USER_STORE_GROUPS_CLAIM;
import static org.wso2.carbon.user.core.UserStoreConfigConstants.RESOLVE_GROUP_NAME_FROM_USER_ID_CACHE_NAME;
import static org.wso2.carbon.user.core.UserStoreConfigConstants.RESOLVE_USER_ID_FROM_USER_NAME_CACHE_NAME;
import static org.wso2.carbon.user.core.UserStoreConfigConstants.RESOLVE_USER_NAME_FROM_UNIQUE_USER_ID_CACHE_NAME;
import static org.wso2.carbon.user.core.UserStoreConfigConstants.RESOLVE_USER_NAME_FROM_USER_ID_CACHE_NAME;
import static org.wso2.carbon.user.core.UserStoreConfigConstants.RESOLVE_USER_UNIQUE_ID_FROM_USER_NAME_CACHE_NAME;
import static org.wso2.carbon.user.core.constants.UserCoreClaimConstants.USER_ID_CLAIM_URI;
import static org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_CODE_DUPLICATE_WHILE_ADDING_A_HYBRID_ROLE;
import static org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_CODE_DUPLICATE_WHILE_ADDING_A_SYSTEM_ROLE;
import static org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_CODE_DUPLICATE_WHILE_ADDING_A_SYSTEM_USER;
import static org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_CODE_DUPLICATE_WHILE_ADDING_A_USER;
import static org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_CODE_DUPLICATE_WHILE_ADDING_ROLE;
import static org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_CODE_INVALID_DOMAIN_NAME;
import static org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_CODE_ROLE_ALREADY_EXISTS;
import static org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_DURING_POST_GET_GROUP;
import static org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_DURING_POST_GET_GROUPS_LIST_BY_USER_ID;
import static org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_DURING_POST_GET_GROUP_BY_ID;
import static org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_DURING_POST_GET_GROUP_BY_NAME;
import static org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_DURING_POST_GET_GROUP_ID_BY_NAME;
import static org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_DURING_POST_GET_GROUP_NAME_BY_ID;
import static org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_DURING_PRE_GET_GROUP;
import static org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_DURING_PRE_GET_GROUPS_LIST_BY_USER_ID;
import static org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_DURING_PRE_GET_GROUP_BY_ID;
import static org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_DURING_PRE_GET_GROUP_BY_NAME;
import static org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_DURING_PRE_GET_GROUP_ID_BY_NAME;
import static org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_DURING_PRE_GET_GROUP_NAME_BY_ID;
import static org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_EMPTY_GROUP_ID;
import static org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_EMPTY_GROUP_NAME;
import static org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_EMPTY_USER_ID;
import static org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_NO_GROUP_FOUND_WITH_ID;
import static org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_NO_GROUP_FOUND_WITH_NAME;
import static org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_SORTING_NOT_SUPPORTED;
import static org.wso2.carbon.user.core.util.UserCoreUtil.isGroupsVsRolesSeparationImprovementsEnabled;
import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_ID;

public abstract class AbstractUserStoreManager implements PaginatedUserStoreManager,
        UniqueIDUserStoreManager {

    protected static final String TRUE_VALUE = "true";
    protected static final String FALSE_VALUE = "false";
    protected static final String QUERY_FILTER_STRING_ANY = "*";
    protected static final int QUERY_MAX_ITEM_LIMIT_ANY = -1;
    private static final String MAX_LIST_LENGTH = "100";
    private static final int MAX_ITEM_LIMIT_UNLIMITED = -1;
    private static final String MULIPLE_ATTRIBUTE_ENABLE = "MultipleAttributeEnable";
    private static final String DISAPLAY_NAME_CLAIM = "http://wso2.org/claims/displayName";
    private static final String SCIM_USERNAME_CLAIM_URI = "urn:scim:schemas:core:1.0:userName";
    private static final String SCIM2_USERNAME_CLAIM_URI = "urn:ietf:params:scim:schemas:core:2.0:User:userName";
    protected static final String USERNAME_CLAIM_URI = "http://wso2.org/claims/username";
    private static final String APPLICATION_DOMAIN = "Application";
    private static final String WORKFLOW_DOMAIN = "Workflow";
    private static final String INVALID_CLAIM_URL = "InvalidClaimUrl";
    private static final String INVALID_USER_NAME = "InvalidUserName";
    private static final String READ_ONLY_STORE = "ReadOnlyUserStoreManager";
    private static final String READ_ONLY_PRIMARY_STORE = "ReadOnlyPrimaryUserStoreManager";
    private static final String ADMIN_USER = "AdminUser";
    private static final String PROPERTY_PASSWORD_ERROR_MSG = "PasswordJavaRegExViolationErrorMsg";
    private static final String MULTI_ATTRIBUTE_SEPARATOR = "MultiAttributeSeparator";
    private static final String LOCATION_CLAIM_URI = "http://wso2.org/claims/location";
    private static final String CREATED_CLAIM_URI = "http://wso2.org/claims/created";
    private static final String MODIFIED_CLAIM_URI = "http://wso2.org/claims/modified";
    private static Log log = LogFactory.getLog(AbstractUserStoreManager.class);
    private static final int DEFAULT_PASSWORD_VALIDITY_PERIOD_VALUE = 24;
    protected static int pwValidityTimeoutInt = getDefaultPasswordValidityPeriodInHours();
    protected int tenantId;
    protected DataSource dataSource = null;
    protected RealmConfiguration realmConfig = null;
    protected ClaimManager claimManager = null;
    protected UserRealm userRealm = null;
    protected HybridRoleManager hybridRoleManager = null;
    protected HashProvider hashProvider = null;
    // User roles cache
    protected UserRolesCache userRolesCache = null;
    protected SystemUserRoleManager systemUserRoleManager = null;
    protected boolean readGroupsEnabled = false;
    protected boolean writeGroupsEnabled = false;
    private UserStoreManager secondaryUserStoreManager;
    private boolean userRolesCacheEnabled = true;
    private String cacheIdentifier;
    private boolean replaceEscapeCharactersAtUserLogin = true;
    protected Map<String, UserStoreManager> userStoreManagerHolder = new HashMap<String, UserStoreManager>();
    private Map<String, Integer> maxUserListCount = null;
    private Map<String, Integer> maxRoleListCount = null;
    private List<UserStoreManagerConfigurationListener> listener = new ArrayList<UserStoreManagerConfigurationListener>();
    private static final ThreadLocal<Boolean> isSecureCall = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return Boolean.FALSE;
        }
    };

    private UserUniqueIDManger userUniqueIDManger = new UserUniqueIDManger();
    protected UserUniqueIDDomainResolver userUniqueIDDomainResolver;
    private GroupUniqueIDDomainResolver groupUniqueIDDomainResolver;

    private void setClaimManager(ClaimManager claimManager) throws IllegalAccessException {
        if (Boolean.parseBoolean(realmConfig.getRealmProperty(UserCoreClaimConstants.INITIALIZE_NEW_CLAIM_MANAGER))) {
            this.claimManager = claimManager;
        } else {
            throw new IllegalAccessException("Set claim manager is not allowed");
        }
    }

    /**
     * This method is used by the APIs' in the AbstractUserStoreManager
     * to make compatible with Java Security Manager.
     */
    private Object callSecure(final String methodName, final Object[] objects, final Class[] argTypes)
            throws UserStoreException {

        final AbstractUserStoreManager instance = this;

        isSecureCall.set(Boolean.TRUE);
        final Method method;
        try {
            Class clazz = Class.forName("org.wso2.carbon.user.core.common.AbstractUserStoreManager");
            method = clazz.getDeclaredMethod(methodName, argTypes);

        } catch (NoSuchMethodException e) {
            log.error("Error occurred when calling method " + methodName, e);
            throw new UserStoreException(e);
        } catch (ClassNotFoundException e) {
            log.error("Error occurred when calling class " + methodName, e);
            throw new UserStoreException(e);
        }

        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                @Override
                public Object run() throws Exception {
                    return method.invoke(instance, objects);
                }
            });
        } catch (PrivilegedActionException e) {
            if (e.getCause() != null && e.getCause().getCause() != null && e.getCause().getCause() instanceof
                    UserStoreException) {
                boolean isClientException = e.getCause().getCause() instanceof UserStoreClientException;
                String errorCode = ((UserStoreException) e.getCause().getCause()).getErrorCode();
                String errorMessage = e.getCause().getCause().getMessage();
                if (isClientException) {
                    if (StringUtils.isBlank(errorCode)) {
                        throw new UserStoreClientException(errorMessage, e);
                    }
                    throw new UserStoreClientException(errorMessage, errorCode, e);
                } else {
                    if (StringUtils.isBlank(errorCode)) {
                        throw new UserStoreException(errorMessage, e);
                    }
                    throw new UserStoreException(errorMessage, errorCode, e);
                }
            } else {
                String msg;
                if (objects != null && argTypes != null) {
                    msg = "Error occurred while accessing Java Security Manager Privilege Block when called by " +
                            "method " + methodName + " with " + objects.length + " length of Objects and argTypes " +
                            Arrays.toString(argTypes);
                } else {
                    msg = "Error occurred while accessing Java Security Manager Privilege Block";
                }
                log.error(msg, e);
                throw new UserStoreException(msg, e);
            }
        } finally {
            isSecureCall.set(Boolean.FALSE);
        }
    }

    /**
     * This method is used by the support system to read properties
     */
    protected abstract Map<String, String> getUserPropertyValues(String userName,
                                                                 String[] propertyNames, String profileName)
            throws UserStoreException;

    /**
     * This method is used to read properties of the given user.
     *
     * @param userID        user ID.
     * @param propertyNames property names.
     * @param profileName   profile name.
     * @return user properties of the given user.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    protected Map<String, String> getUserPropertyValuesWithID(String userID, String[] propertyNames, String profileName)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("getUserPropertyValuesWithID operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException(
                "getUserPropertyValuesWithID operation is not implemented in: " + this.getClass());
    }
    /**
     * @param roleName
     * @return
     */
    protected abstract boolean doCheckExistingRole(String roleName) throws UserStoreException;

    /**
     * Creates the search base and other relevant parameters for the provided role name
     *
     * @param roleName
     * @return
     */
    protected abstract RoleContext createRoleContext(String roleName) throws UserStoreException;

    /**
     * @param userName
     * @return
     * @throws UserStoreException
     */
    protected abstract boolean doCheckExistingUser(String userName) throws UserStoreException;

    /**
     * Check whether the username exists in the systems which supports unique user ID feature.
     *
     * @param userName user name.
     * @return Whether the user is existing in the user store.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    protected boolean doCheckExistingUserNameWithIDImpl(String userName) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doCheckExistingUserNameWithIDImpl operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException(
                "doCheckExistingUserNameWithIDImpl operation is not implemented in: " + this.getClass());
    }

    /**
     * Check whether the userID exists in the system.
     *
     * @param userID user ID.
     * @return Whether the user is existing in the user store.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    protected boolean doCheckExistingUserWithID(String userID) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doCheckExistingUserWithID operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException(
                "doCheckExistingUserWithID operation is not implemented in: " + this.getClass());
    }

    /**
     * Retrieves a list of user names for given user's property in user profile
     *
     * @param property    user property in user profile
     * @param value       value of property
     * @param profileName profile name, can be null. If null the default profile is considered.
     * @return An array of user names
     * @throws UserStoreException if the operation failed
     */
    protected abstract String[] getUserListFromProperties(String property, String value,
                                                          String profileName) throws UserStoreException;

    /**
     * Retrieves a list of user IDs for given user's property in user profile.
     *
     * @param property    user property in user profile
     * @param value       value of property
     * @param profileName profile name, can be null. If null the default profile is considered.
     * @return An array of user names
     * @throws UserStoreException if the operation failed
     */
    protected List<String> doGetUserListFromPropertiesWithID(String property, String value, String profileName)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doGetUserListFromPropertiesWithID operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException(
                "doGetUserListFromPropertiesWithID operation is not implemented in: " + this.getClass());
    }

    /**
     * Given the user name and a credential object, the implementation code must validate whether
     * the user is authenticated.
     *
     * @param userName   The user name
     * @param credential The credential of a user
     * @return If the value is true the provided credential match with the user name. False is
     * returned for invalid credential, invalid user name and mismatching credential with
     * user name.
     * @throws UserStoreException An unexpected exception has occurred
     */
    protected abstract boolean doAuthenticate(String userName, Object credential)
            throws UserStoreException;

    /**
     * Given the preferred user name and a credential object, the implementation code must
     * validate whether the user is authenticated.
     *
     * @param preferredUserNameProperty The preferred user name property.
     * @param preferredUserNameValue    The preferred user name value.
     * @param credential                The credential of a user.
     * @return @see AuthenticationResult.
     * @throws UserStoreException An unexpected exception has occurred.
     */
    protected AuthenticationResult doAuthenticateWithID(String preferredUserNameProperty, String preferredUserNameValue,
            Object credential, String profileName) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doAuthenticateWithID operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException("doAuthenticateWithID operation is not implemented in: " + this.getClass());
    }

    /**
     * Given the login identifiers and a credential object, the implementation code must
     * validate whether the user is authenticated.
     *
     * @param loginIdentifiers The login identifiers.
     * @param credential       The credential of a user.
     * @return @see AuthenticationResult.
     * @throws UserStoreException An unexpected exception has occurred.
     */
    protected AuthenticationResult doAuthenticateWithID(List<LoginIdentifier> loginIdentifiers, Object credential)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doAuthenticateWithID operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException("doAuthenticateWithID operation is not implemented in: " + this.getClass());
    }

    /**
     * Given the user ID and a credential object, the implementation code must validate whether
     * the user is authenticated.
     *
     * @param userID     The user ID.
     * @param credential The credential of a user.
     * @return @see AuthenticationResult.
     * @throws UserStoreException An unexpected exception has occurred.
     */
    protected AuthenticationResult doAuthenticateWithID(String userID, Object credential) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doAuthenticateWithID operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException("doAuthenticateWithID operation is not implemented in: " + this.getClass());
    }

    /**
     * Add a user to the user store.
     *
     * @param userName              User name of the user
     * @param credential            The credential/password of the user
     * @param roleList              The roles that user belongs
     * @param claims                Properties of the user
     * @param profileName           profile name, can be null. If null the default profile is considered.
     * @param requirePasswordChange whether password required is need
     * @throws UserStoreException An unexpected exception has occurred
     */
    protected abstract void  doAddUser(String userName, Object credential, String[] roleList,
                                      Map<String, String> claims, String profileName, boolean requirePasswordChange)
            throws UserStoreException;

    /**
     * Add a user to the user store.
     *
     * @param userName              User name of the user.
     * @param credential            The credential/password of the user.
     * @param roleList              The roles that user belongs.
     * @param claims                Properties of the user.
     * @param profileName           profile name, can be null. If null the default profile is considered.
     * @param requirePasswordChange whether password required is need.
     * @throws UserStoreException An unexpected exception has occurred.
     */
    protected User doAddUserWithID(String userName, Object credential, String[] roleList, Map<String, String> claims,
            String profileName, boolean requirePasswordChange) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doAddUserWithID operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException("doAddUserWithID operation is not implemented in: " + this.getClass());
    }

    /**
     * Update location claim in claims that contains invalid user ID with generated user ID.
     *
     * @param userID new user ID of the user.
     * @param claims claim map for the user.
     */
    protected void updateLocationClaimWithUserId(String userID, Map<String, String> claims) {

        if (MapUtils.isEmpty(claims)) {
            return;
        }

        String locationClaim = claims.get(LOCATION_CLAIM_URI);

        // Update location claim with new user ID.
        if (locationClaim != null && locationClaim.contains("/Users/")) {
            claims.put(LOCATION_CLAIM_URI,
                    locationClaim.substring(0, locationClaim.indexOf("/Users/") + 7) + userID);
        }
    }

    /**
     * Update the credential/password of the user
     *
     * @param userName      The user name
     * @param newCredential The new credential/password
     * @param oldCredential The old credential/password
     * @throws UserStoreException An unexpected exception has occurred
     */
    protected abstract void doUpdateCredential(String userName, Object newCredential,
                                               Object oldCredential) throws UserStoreException;

    /**
     * Update the credential/password of the user.
     *
     * @param userID        The user ID.
     * @param newCredential The new credential/password.
     * @param oldCredential The old credential/password.
     * @throws UserStoreException An unexpected exception has occurred.
     */
    protected void doUpdateCredentialWithID(String userID, Object newCredential, Object oldCredential)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doUpdateCredentialWithID operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException(
                "doUpdateCredentialWithID operation is not implemented in: " + this.getClass());
    }

    /**
     * Update credential/password by the admin of another user
     *
     * @param userName      The user name
     * @param newCredential The new credential
     * @throws UserStoreException An unexpected exception has occurred
     */
    protected abstract void doUpdateCredentialByAdmin(String userName, Object newCredential)
            throws UserStoreException;

    /**
     * Update credential/password by the admin of another user.
     *
     * @param userID        The user ID.
     * @param newCredential The new credential.
     * @throws UserStoreException An unexpected exception has occurred.
     */
    protected void doUpdateCredentialByAdminWithID(String userID, Object newCredential) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doUpdateCredentialByAdminWithID operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException(
                "doUpdateCredentialByAdminWithID operation is not implemented in: " + this.getClass());
    }

    /**
     * Delete the user with the given user name
     *
     * @param userName The user name
     * @throws UserStoreException An unexpected exception has occurred
     */
    protected abstract void doDeleteUser(String userName) throws UserStoreException;

    /**
     * Delete the user with the given user ID.
     *
     * @param userID The user ID.
     * @throws UserStoreException An unexpected exception has occurred.
     */
    protected void doDeleteUserWithID(String userID) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doDeleteUserWithID operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException("doDeleteUserWithID operation is not implemented in: " + this.getClass());
    }

    /**
     * Set a single user claim value.
     *
     * @param userName    The user name.
     * @param claimURI    The claim URI.
     * @param claimValue  The value.
     * @param profileName The profile name, can be null. If null the default profile is considered.
     * @throws UserStoreException An unexpected exception has occurred.
     */
    protected void doSetUserClaimValue(String userName, String claimURI,
                                       String claimValue, String profileName) throws UserStoreException {

        try {
            String attributeName = getClaimAtrribute(claimURI, userName, null);
            Map<String, String> userStoreAttributeValueMap = new HashMap<>();

            userStoreAttributeValueMap.put(attributeName, claimValue);
            processAttributesBeforeUpdate(userName, userStoreAttributeValueMap, profileName);

            for (Map.Entry<String, String> entry : userStoreAttributeValueMap.entrySet()) {
                doSetUserAttribute(userName, entry.getKey(), entry.getValue(), profileName);
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException(
                    "Error occurred while getting the claim attribute for claimURI: " + claimURI + " of the user: "
                            + userName, e);
        }
    }

    /**
     * Set the user attribute of the user.
     *
     * @param userName      User name.
     * @param attributeName Attribute name.
     * @param value         Attribute value.
     * @param profileName   profile name.
     */
    protected void doSetUserAttribute(String userName, String attributeName, String value, String profileName)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doSetUserAttribute operation is not implemented in: " + this.getClass());
        }

        throw new NotImplementedException("doSetUserAttribute operation is not implemented in: " + this.getClass());
    }

    /**
     * Set the user attribute of the user.
     *
     * @param userID        User ID.
     * @param attributeName Attribute name.
     * @param value         Attribute value.
     * @param profileName   Profile Name.
     * @throws UserStoreException Thrown if the operation is not implemented in the underlying user store.
     */
    protected void doSetUserAttributeWithID(String userID, String attributeName, String value, String profileName)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doSetUserAttributeWithID operation is not implemented in: " + this.getClass());
        }

        throw new NotImplementedException("doSetUserAttributeWithID operation is not implemented in: "
                + this.getClass());
    }

    /**
     * Set the user attributes of a user.
     *
     * @param userName                 UserName of the user.
     * @param processedClaimAttributes A processed map of user store attribute values.
     * @param profileName              The profile name, can be null. If null the default profile is considered.
     * @throws UserStoreException Thrown if the operation is not implemented in the underlying user store.
     */
    protected void doSetUserAttributes(String userName, Map<String, String> processedClaimAttributes,
                                       String profileName) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doSetUserAttributes operation is not implemented in: " + this.getClass());
        }

        throw new NotImplementedException("doSetUserAttributes operation is not implemented in: " + this.getClass());
    }

    /**
     * Set the user attributes of a user.
     *
     * @param userName                 UserName of the user.
     * @param claimAttributesToAdd     A processed map of userstore attribute values to add.
     * @param claimAttributesToDelete  A processed map of userstore attribute values to delte.
     * @param claimAttributesToReplace A processed map of userstore attribute values to replace.
     * @param profileName              The profile name, can be null. If null the default profile is considered.
     * @throws UserStoreException      Thrown if the userstore operation fails.
     * @throws NotImplementedException Thrown if the operation is not implemented in the underlying userstore.
     */
    protected void doSetUserAttributes(String userName, Map<String, List<String>> claimAttributesToAdd,
                                             Map<String, List<String>> claimAttributesToDelete,
                                             Map<String, List<String>> claimAttributesToReplace, String profileName)
            throws UserStoreException, NotImplementedException {

        throw new NotImplementedException("doSetUserAttributes operation is not implemented in: " + this.getClass());
    }

    /**
     * Set the user attributes of a user.
     *
     * @param processedClaimAttributes A processed map of user store attribute values.
     * @param userID                   UserID of the user.
     * @param profileName              The profile name, can be null. If null the default profile is considered.
     * @throws UserStoreException Thrown if the operation is not implemented in the underlying user store.
     */
    protected void doSetUserAttributesWithID(String userID,
                                             Map<String, String> processedClaimAttributes, String profileName)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doSetUserAttributesWithID operation is not implemented in: " + this.getClass());
        }

        throw new NotImplementedException("doSetUserAttributesWithID operation is not implemented in: "
                + this.getClass());
    }

    /**
     * Set the user attributes of a user.
     *
     * @param userID                   UserID of the user.
     * @param claimAttributesToAdd     A processed map of userstore attribute values to add.
     * @param claimAttributesToDelete  A processed map of userstore attribute values to delete.
     * @param claimAttributesToReplace A processed map of userstore attribute values to replace.
     * @param profileName              The profile name, can be null. If null the default profile is considered.
     * @throws UserStoreException      Thrown if the userstore operation fails.
     * @throws NotImplementedException Thrown if the operation is not implemented in the underlying userstore.
     */
    protected void doSetUserAttributesWithID(String userID, Map<String, List<String>> claimAttributesToAdd,
                                             Map<String, List<String>> claimAttributesToDelete,
                                             Map<String, List<String>> claimAttributesToReplace, String profileName)
            throws UserStoreException, NotImplementedException {

        throw new NotImplementedException("doSetUserAttributesWithID operation is not implemented in: "
                + this.getClass());
    }

    /**
     * Set a single user claim value.
     *
     * @param userID      The user ID.
     * @param claimURI    The claim URI.
     * @param claimValue  The value.
     * @param profileName The profile name, can be null. If null the default profile is considered.
     * @throws UserStoreException An unexpected exception has occurred.
     */
    protected void doSetUserClaimValueWithID(String userID, String claimURI, String claimValue, String profileName)
            throws UserStoreException {

        try {
            String attributeName = getClaimAtrribute(claimURI, userID, null);
            Map<String, String> userStoreAttributeValueMap = new HashMap<>();
            userStoreAttributeValueMap.put(attributeName, claimValue);
            processAttributesBeforeUpdateWithID(userID, userStoreAttributeValueMap, profileName);

            for (Map.Entry<String, String> entry : userStoreAttributeValueMap.entrySet()) {
                doSetUserAttributeWithID(userID, entry.getKey(), entry.getValue(), profileName);
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException(
                    "Error occurred while getting the claim attribute for claimURI: " + claimURI + " of the user: "
                            + userID, e);
        }
    }

    /**
     * Set many user claim values.
     *
     * @param userName    The user name.
     * @param claims      Map of claim URIs against values.
     * @param profileName The profile name, can be null. If null the default profile is considered.
     * @throws UserStoreException An unexpected exception has occurred.
     */
    public void doSetUserClaimValues(String userName, Map<String, String> claims,
                                        String profileName) throws UserStoreException {

        if (profileName == null) {
            profileName = UserCoreConstants.DEFAULT_PROFILE;
        }

        // Resolving claims to user store attributes.
        Map<String, String> claimAttributeValueMapForPersist = resolveUserStoreAttributeValueMap(userName, claims);

        processAttributesBeforeUpdate(userName, claimAttributeValueMapForPersist, profileName);

        // Persist the attribute values map.
        doSetUserAttributes(userName, claimAttributeValueMapForPersist, profileName);
    }

    /**
     * Set many user claim values by treating multi-valued claims independently from simple claims.
     *
     * @param userName                         User's username.
     * @param multiValuedClaimsToAdd           Map of multi-valued claim URIs against values to be added.
     * @param multiValuedClaimsToDelete        Map of multi-valued claim URIs against values to be deleted.
     * @param claimsExcludingMultiValuedClaims Map of claim URIs excluding multi-valued claims against values
     *                                         to be modified.
     * @param profileName                      The profile name, can be null. If null the default profile is considered.
     * @throws UserStoreException      An unexpected exception has occurred.
     * @throws NotImplementedException Functionality is not implemented exception.
     */
    protected void doSetUserClaimValues(String userName, Map<String, List<String>> multiValuedClaimsToAdd,
                                        Map<String, List<String>> multiValuedClaimsToDelete,
                                        Map<String, List<String>> claimsExcludingMultiValuedClaims,
                                        String profileName)
            throws UserStoreException, NotImplementedException {

        if (StringUtils.isBlank(profileName)) {
            profileName = UserCoreConstants.DEFAULT_PROFILE;
        }

        // Resolving claims to user store attributes.
        Map<String, List<String>> claimAttributeValueMapToAdd =
                resolveUserStoreAttributeValueMaps(userName, multiValuedClaimsToAdd);
        Map<String, List<String>> claimAttributeValueMapToDelete =
                resolveUserStoreAttributeValueMaps(userName, multiValuedClaimsToDelete);
        Map<String, List<String>> claimAttributeValueMapToModify =
                resolveUserStoreAttributeValueMaps(userName, claimsExcludingMultiValuedClaims);

        processAttributesBeforeUpdate(userName, claimAttributeValueMapToAdd, profileName);
        processAttributesBeforeUpdate(userName, claimAttributeValueMapToDelete, profileName);
        processAttributesBeforeUpdate(userName, claimAttributeValueMapToModify, profileName);

        // Persist the attribute values map.
        doSetUserAttributes(userName, claimAttributeValueMapToAdd, claimAttributeValueMapToDelete,
                claimAttributeValueMapToModify, profileName);
    }

    /**
     * Resolves claim URIs as user store properties.
     *
     * @param userIdentifier Username of the user.
     * @param claims         A map of claim URIs to be resolved.
     * @return A map of user store property values.
     * @throws UserStoreException Thrown if a particular claim URI could not be resolved.
     */
    private Map<String, String> resolveUserStoreAttributeValueMap(String userIdentifier, Map<String, String> claims)
            throws UserStoreException {

        Map<String, String> userStoreAttributeValueMap = new HashMap<>();

        try {
            for (Map.Entry<String, String> claimEntry : claims.entrySet()) {
                String claimURI = claimEntry.getKey();
                String attributeName = getClaimAtrribute(claimURI, userIdentifier, null);
                userStoreAttributeValueMap.put(attributeName, claimEntry.getValue());
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String errorMessage = "Error occurred while getting claim attribute for user : " + userIdentifier;

            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }

            if (e instanceof UserStoreClientException) {
                throw new UserStoreClientException(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        }
        return userStoreAttributeValueMap;
    }

    private Map<String, List<String>> resolveUserStoreAttributeValueMaps(String userIdentifier,
                                                                         Map<String, List<String>> claims)
            throws UserStoreException {

        Map<String, List<String>> userStoreAttributeValueMap = new HashMap<>();
        try {
            for (Map.Entry<String, List<String>> claimEntry : claims.entrySet()) {
                String claimURI = claimEntry.getKey();
                String attributeName = getClaimAtrribute(claimURI, userIdentifier, null);
                userStoreAttributeValueMap.put(attributeName, claimEntry.getValue());
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String errorMessage = "Error occurred while getting claim attribute for user : " + userIdentifier;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        }
        return userStoreAttributeValueMap;
    }

    /**
     * Set many user claim values.
     *
     * @param userID      The user ID.
     * @param claims      Map of claim URIs against values.
     * @param profileName The profile name, can be null. If null the default profile is considered.
     * @throws UserStoreException An unexpected exception has occurred.
     */
    protected void doSetUserClaimValuesWithID(String userID, Map<String, String> claims, String profileName)
            throws UserStoreException {

        if (profileName == null) {
            profileName = UserCoreConstants.DEFAULT_PROFILE;
        }

        // Resolving claims to user store attributes.
        Map<String, String> claimAttributeValueMapForPersist = resolveUserStoreAttributeValueMap(userID, claims);

        processAttributesBeforeUpdateWithID(userID, claimAttributeValueMapForPersist, profileName);

        // Persist the attribute values map.
        doSetUserAttributesWithID(userID, claimAttributeValueMapForPersist, profileName);
    }

    /**
     * Set many user claim values.
     *
     * @param userID                           The user ID.
     * @param multiValuedClaimsToAdd           Map of multi-valued claim URIs against values to add.
     * @param multiValuedClaimsToDelete        Map of multi-valued claim URIs against values to delete.
     * @param claimsExcludingMultiValuedClaims Map of non-multi-valued claim URIs against values to replace.
     * @param profileName                      The profile name, can be null. If null the default profile is considered.
     * @throws UserStoreException      Thrown if an unexpected exception has occurred in userstore operation.
     * @throws NotImplementedException Thrown if the operation is not implemented in the underlying userstore.
     */
    protected void doSetUserClaimValuesWithID(String userID, Map<String, List<String>> multiValuedClaimsToAdd,
                                              Map<String, List<String>> multiValuedClaimsToDelete,
                                              Map<String, List<String>> claimsExcludingMultiValuedClaims,
                                              String profileName) throws UserStoreException, NotImplementedException {

        if (profileName == null) {
            profileName = UserCoreConstants.DEFAULT_PROFILE;
        }
        // Resolving claims to user store attributes.
        Map<String, List<String>> claimAttributeValueMapToAdd =
                resolveUserStoreAttributeValueMaps(userID, multiValuedClaimsToAdd);
        Map<String, List<String>> claimAttributeValueMapToDelete =
                resolveUserStoreAttributeValueMaps(userID, multiValuedClaimsToDelete);
        Map<String, List<String>> claimAttributeValueMapToModify =
                resolveUserStoreAttributeValueMaps(userID, claimsExcludingMultiValuedClaims);

        processAttributesBeforeUpdateWithID(userID, claimAttributeValueMapToAdd, profileName);
        processAttributesBeforeUpdateWithID(userID, claimAttributeValueMapToDelete, profileName);
        processAttributesBeforeUpdateWithID(userID, claimAttributeValueMapToModify, profileName);
        // Persist the attribute values map.
        doSetUserAttributesWithID(userID, claimAttributeValueMapToAdd, claimAttributeValueMapToDelete,
                claimAttributeValueMapToModify, profileName);
    }

    /**
     * Delete a single user claim value
     *
     * @param userName    The user name
     * @param claimURI    Name of the claim
     * @param profileName The profile name, can be null. If null the default profile is considered.
     * @throws UserStoreException An unexpected exception has occurred
     */
    protected abstract void doDeleteUserClaimValue(String userName, String claimURI,
                                                   String profileName) throws UserStoreException;

    /**
     * Delete a single user claim value.
     *
     * @param userID      The user ID.
     * @param claimURI    Name of the claim.
     * @param profileName The profile name, can be null. If null the default profile is considered.
     * @throws UserStoreException An unexpected exception has occurred.
     */
    protected void doDeleteUserClaimValueWithID(String userID, String claimURI, String profileName)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doDeleteUserClaimValueWithID operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException(
                "doDeleteUserClaimValueWithID operation is not implemented in: " + this.getClass());
    }

    /**
     * Delete many user claim values.
     *
     * @param userName    The user name
     * @param claims      URIs of the claims to be deleted.
     * @param profileName The profile name, can be null. If null the default profile is considered.
     * @throws UserStoreException An unexpected exception has occurred
     */
    protected abstract void doDeleteUserClaimValues(String userName, String[] claims,
                                                    String profileName) throws UserStoreException;

    /**
     * Delete many user claim values.
     *
     * @param userID      The user ID.
     * @param claims      URIs of the claims to be deleted.
     * @param profileName The profile name, can be null. If null the default profile is considered.
     * @throws UserStoreException An unexpected exception has occurred.
     */
    protected void doDeleteUserClaimValuesWithID(String userID, String[] claims, String profileName)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doDeleteUserClaimValuesWithID operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException(
                "doDeleteUserClaimValuesWithID operation is not implemented in: " + this.getClass());
    }

    /**
     * Update user list of a particular role
     *
     * @param roleName     The role name
     * @param deletedUsers Array of user names, that is going to be removed from the role
     * @param newUsers     Array of user names, that is going to be added to the role
     * @throws UserStoreException An unexpected exception has occurred
     */
    protected abstract void doUpdateUserListOfRole(String roleName, String[] deletedUsers,
                                                   String[] newUsers) throws UserStoreException;

    /**
     * Update user list of a particular role.
     *
     * @param roleName     The role name.
     * @param deletedUsers Array of user IDs, that is going to be removed from the role.
     * @param newUsers     Array of user IDs, that is going to be added to the role.
     * @throws UserStoreException An unexpected exception has occurred.
     */
    protected void doUpdateUserListOfRoleWithID(String roleName, String[] deletedUsers, String[] newUsers)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doUpdateUserListOfRoleWithID operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException(
                "doUpdateUserListOfRoleWithID operation is not implemented in: " + this.getClass());
    }

    /**
     * Update role list of a particular user.
     *
     * @param userName     The user name
     * @param deletedRoles Array of role names, that is going to be removed from the user
     * @param newRoles     Array of role names, that is going to be added to the user
     * @throws UserStoreException An unexpected exception has occurred
     */
    protected abstract void doUpdateRoleListOfUser(String userName, String[] deletedRoles,
                                                   String[] newRoles) throws UserStoreException;

    /**
     * Update role list of a particular user.
     *
     * @param userID       The user ID.
     * @param deletedRoles Array of role names, that is going to be removed from the user.
     * @param newRoles     Array of role names, that is going to be added to the user.
     * @throws UserStoreException An unexpected exception has occurred.
     */
    protected void doUpdateRoleListOfUserWithID(String userID, String[] deletedRoles, String[] newRoles)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doUpdateRoleListOfUserWithID operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException(
                "doUpdateRoleListOfUserWithID operation is not implemented in: " + this.getClass());
    }

    /**
     * Only gets the internal roles of the user with internal domain name
     *
     * @param userName Name of the user - who we need to find roles.
     * @return
     * @throws UserStoreException
     */
    protected String[] doGetInternalRoleListOfUser(String userName, String filter) throws UserStoreException {

        if (Boolean.parseBoolean(realmConfig.getUserStoreProperty(MULIPLE_ATTRIBUTE_ENABLE))) {
            String userNameAttribute = realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE);
            if (userNameAttribute != null && userNameAttribute.trim().length() > 0) {
                Map<String, String> map = getUserPropertyValues(userName, new String[]{userNameAttribute}, null);
                String tempUserName = map.get(userNameAttribute);
                if (tempUserName != null) {
                    userName = tempUserName;
                    if (log.isDebugEnabled()) {
                        log.debug("Replaced user name : " + userName + " from user property value : " + tempUserName);
                    }
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Retrieving internal roles for user name :  " + userName + " and search filter : " + filter);
        }
        return hybridRoleManager.getHybridRoleListOfUser(userName, filter);
    }

    /**
     * Only gets the internal roles of the user with internal domain name.
     *
     * @param userID ID of the user.
     * @return internal roles list of the user.
     * @throws UserStoreException An unexpected exception has occurred.
     */
    protected List<String> doGetInternalRoleListOfUserWithID(String userID, String filter) throws UserStoreException {

        String username = doGetUserNameFromUserID(userID);
        if (StringUtils.isEmpty(username)) {
            throw new UserStoreException("No user found with UserID: " + userID);
        }
        return Arrays.asList(hybridRoleManager.getHybridRoleListOfUser(username, filter));
    }

    protected Map<String, List<String>> doGetInternalRoleListOfUsers(List<String> userNames, String domainName)
            throws UserStoreException {

        if (Boolean.parseBoolean(realmConfig.getUserStoreProperty(MULIPLE_ATTRIBUTE_ENABLE))) {
            List<String> updatedUserNameList = new ArrayList<>();
            for (String userName : userNames) {
                String userNameAttribute = realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE);
                if (userNameAttribute != null && userNameAttribute.trim().length() > 0) {
                    Map<String, String> map = getUserPropertyValues(userName, new String[] { userNameAttribute }, null);
                    String tempUserName = map.get(userNameAttribute);
                    if (tempUserName != null) {
                        updatedUserNameList.add(tempUserName);
                        if (log.isDebugEnabled()) {
                            log.debug(
                                    "Replaced user name : " + userName + " from user property value : " + tempUserName);
                        }
                    } else {
                        updatedUserNameList.add(userName);
                    }
                } else {
                    updatedUserNameList.add(userName);
                }
            }
            userNames = updatedUserNameList;
        }
        return hybridRoleManager.getHybridRoleListOfUsers(userNames, domainName);
    }

    protected Map<String, List<String>> doGetInternalRoleListOfUsersWithID(List<String> userIDs, String domainName)
            throws UserStoreException {

        List<String> userNamesFromUserIDs = getUserNamesFromUserIDs(userIDs);
        Map<String, List<String>> hybridRoleList = new HashMap<>();
        Map<String, List<String>> hybridRoleListOfUsers =
                hybridRoleManager.getHybridRoleListOfUsers(userNamesFromUserIDs, domainName);
        for (Map.Entry<String, List<String>> hybridRoleListOfUser : hybridRoleListOfUsers.entrySet()) {
            hybridRoleList.put(getUserIDFromUserName(hybridRoleListOfUser.getKey()), hybridRoleListOfUser.getValue());
        }
        return hybridRoleList;
    }

    /**
     * Get hybrid role list of groups.
     *
     * @param groupNames Group names list.
     * @param domainName Domain name of the groups.
     * @return Map of hybrid role list of groups.
     * @throws UserStoreException userStoreException.
     */
    public Map<String, List<String>> getHybridRoleListOfGroups(List<String> groupNames, String domainName)
            throws UserStoreException {

        // Filter hybrid roles if there are any.
        List<String> externalGroupNames = new ArrayList<>();
        for (String groupName : groupNames) {
            String roleDomainName = UserCoreUtil.extractDomainFromName(groupName);
            if (UserCoreConstants.INTERNAL_DOMAIN.
                    equalsIgnoreCase(roleDomainName) || APPLICATION_DOMAIN.equalsIgnoreCase(roleDomainName)
                    || WORKFLOW_DOMAIN.equalsIgnoreCase(roleDomainName) || SYSTEM_DOMAIN_NAME
                    .equalsIgnoreCase(roleDomainName)) {
                continue;
            }
            externalGroupNames.add(groupName);
        }

        return hybridRoleManager.getHybridRoleListOfGroups(externalGroupNames, domainName);
    }

    /**
     * Get hybrid role list of a group.
     *
     * @param groupName Group name.
     * @param domainName Domain name of the group.
     * @return List of hybrid roles of the group.
     * @throws UserStoreException userStoreException.
     */
    public List<String> getHybridRoleListOfGroup(String groupName, String domainName) throws UserStoreException {

        return getHybridRoleListOfGroups(new ArrayList<>(Collections.singleton(groupName)), domainName)
                .getOrDefault(groupName, new ArrayList<>());
    }

    /**
     * Get hybrid role list of users.
     *
     * @param userNames User names list.
     * @param domainName Domain name of the users.
     * @return Map of hybrid role list of users.
     * @throws UserStoreException userStoreException.
     */
    public Map<String, List<String>> getHybridRoleListOfUsers(List<String> userNames, String domainName)
            throws UserStoreException {

        return hybridRoleManager.getHybridRoleListOfUsers(userNames, domainName);
    }

    /**
     * Get hybrid role list of a user.
     *
     * @param userName User name.
     * @param domainName Domain name of the user.
     * @return List of hybrid roles of the user.
     * @throws UserStoreException userStoreException.
     */
    public List<String> getHybridRoleListOfUser(String userName, String domainName) throws UserStoreException {

        return getHybridRoleListOfUsers(new ArrayList<>(Collections.singleton(userName)), domainName)
                .getOrDefault(userName, new ArrayList<>());
    }

    /**
     * Check whether the given hybrid role is exist in the system.
     *
     * @param roleName Role name.
     * @return {@code true} if the given role is exist in the system.
     * @throws UserStoreException UserStoreException.
     */
    public boolean isExistingHybridRole(String roleName) throws UserStoreException {

        return hybridRoleManager.isExistingRole(removeInternalDomain(roleName));
    }

    /**
     * Update group list of role.
     *
     * @param roleName      Role name.
     * @param deletedGroups Deleted groups.
     * @param newGroups     New groups.
     * @throws UserStoreException UserStoreException.
     */
    public void updateGroupListOfHybridRole(String roleName, String[] deletedGroups, String[] newGroups)
            throws UserStoreException {

        hybridRoleManager.updateGroupListOfHybridRole(removeInternalDomain(roleName), deletedGroups, newGroups);
    }

    private String removeInternalDomain(String roleName) {

        if (UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(UserCoreUtil.extractDomainFromName(roleName))) {
            return UserCoreUtil.removeDomainFromName(roleName);
        }
        return roleName;
    }

    /**
     * Convert a map of lists to a set of unique elements.
     *
     * @param mapOfLists Map of lists.
     * @return list with unique elements.
     */
    private Set<String> getUniqueSet(Map<String, List<String>> mapOfLists) {

        Set<String> fullSet = new HashSet<>();
        for (List<String> list : mapOfLists.values()) {
            fullSet.addAll(list);
        }
        return fullSet;
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
     * Only gets the external roles of the user.
     *
     * @param userID user ID of the user.
     * @return external roles list of the user.
     * @throws UserStoreException An unexpected exception has occurred.
     */
    protected String[] doGetExternalRoleListOfUserWithID(String userID, String filter) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doGetExternalRoleListOfUserWithID operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException(
                "doGetExternalRoleListOfUserWithID operation is not implemented in: " + this.getClass());
    }


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
     * Only gets the shared roles of the user.
     *
     * @param userID user ID.
     * @return roles list.
     * @throws UserStoreException An unexpected exception has occurred.
     */
    protected String[] doGetSharedRoleListOfUserWithID(String userID, String tenantDomain, String filter)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doGetSharedRoleListOfUserWithID operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException(
                "doGetSharedRoleListOfUserWithID operation is not implemented in: " + this.getClass());
    }

    /**
     * Add role with a list of users and permissions provided.
     *
     * @param roleName
     * @param userList
     * @throws UserStoreException
     */
    protected abstract void doAddRole(String roleName, String[] userList, boolean shared) throws UserStoreException;

    /**
     * Add role with a list of users and permissions provided.
     *
     * @param roleName role name.
     * @param userList userIDs list.
     * @throws UserStoreException An unexpected exception has occurred.
     */
    protected void doAddRoleWithID(String roleName, String[] userList, boolean shared) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doAddRoleWithID operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException("doAddRoleWithID operation is not implemented in: " + this.getClass());

    }

    /**
     * Update the name of the group with id.
     *
     * @param groupId   Group ID.
     * @param newGroupName New group name.
     * @throws UserStoreException If an error occurred while updating the group name.
     */
    protected void doUpdateGroupNameByGroupId(String groupId, String newGroupName) throws UserStoreException {

        throw new NotImplementedException(
                "doUpdateGroupNameByGroupId operation is not implemented in: " + this.getClass());
    }

    /**
     * Update the name of the group with the given group name.
     *
     * @param currentGroupName Current name of the group that needs to be updated.
     * @param newGroupName     New name of the group.
     * @throws UserStoreException If an error occurred while updating the group name.
     */
    protected void doUpdateGroupName(String currentGroupName, String newGroupName)
            throws UserStoreException {

        // Overriding method to maintain backward compatibility.
        doUpdateRoleName(currentGroupName, newGroupName);
    }

    /**
     * Generate unique identifier for the group.
     *
     * @return
     */
    protected String generateGroupUUID() {

        return UUID.randomUUID().toString();
    }

    /**
     * Add group with a list of users and claims provided.
     *
     * @param groupName Name of the group.
     * @param groupId   Group ID.
     * @param userIds   User IDs list.
     * @param claims    Claims.
     * @return Group object.
     * @throws UserStoreException If an error occurred while adding the group.
     */
    protected Group doAddGroup(String groupName, String groupId, List<String> userIds, Map<String, String> claims)
            throws UserStoreException {

        throw new NotImplementedException("doAddGroup operation is not implemented in: " + this.getClass());
    }

    /**
     * Add group with the group name and the user list. NOTE: Implement this if user store DOES NOT have the
     * capability to manage group related attributes such group id, created timestamp, modified timestamp etc and
     * user store DOES SUPPORT user unique IDs.
     *
     * @param groupName Name of the group.
     * @param userIds   User id list.
     * @throws UserStoreException If an error occurred while adding the group.
     */
    protected void doAddGroupWithUserIds(String groupName, List<String> userIds) throws UserStoreException {

        // No need to have a return since the user store is unable to manage group related attributes. Therefore, from
        // the user store level there is only the name that can return, and it is already there in the parameters.
        doAddRoleWithID(groupName, userIds.toArray(new String[0]), false);
    }

    /**
     * Add group with the group name and the usernames list. NOTE: Implement this if user store DOES NOT HAVE the
     * capability to manage group related attributes such group id, created timestamp, modified timestamp etc and
     * user store DOES NOT SUPPORT user unique IDs.
     *
     * @param groupName Name of the group.
     * @param userNames User name list.
     * @throws UserStoreException If an error occurred while adding the group.
     */
    protected void doAddGroupWithUserNames(String groupName, List<String> userNames) throws UserStoreException {

        // No need to have a return since the user store is unable to manage group related attributes. Therefore, from
        // the user store level there is only the name that can return and it is already there in the parameters.
        doAddRole(groupName, userNames.toArray(new String[0]), false);
    }

    /**
     * Delete group with the given group groupID.
     *
     * @param groupId Group Id.
     * @throws UserStoreException If an error occurred while deleting the group.
     */
    protected void doDeleteGroupByGroupId(String groupId) throws UserStoreException {

        throw new NotImplementedException("doDeleteGroupByGroupID operation is not implemented in: " + this.getClass());
    }

    /**
     * Delete group with the given group name.
     *
     * @param groupName Group name.
     * @throws UserStoreException If an error occurred while deleting the group.
     */
    protected void doDeleteGroupByGroupName(String groupName) throws UserStoreException {

        // Overriding method to maintain backward compatibility.
        doDeleteRole(UserCoreUtil.removeDomainFromName(groupName));
    }

    /**
     * Update the user list of a group.
     *
     * @param groupId        Group ID.
     * @param deletedUserIds User IDs list to be deleted.
     * @param newUserIds     User IDs list to be added.
     * @throws UserStoreException If an error occurred while updating the user list of the group.
     */
    protected void doUpdateUserListOfGroup(String groupId, List<String> deletedUserIds, List<String> newUserIds)
            throws UserStoreException {

        throw new NotImplementedException(
                "doUpdateUserListOfGroup operation is not implemented in: " + this.getClass());
    }

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
     * @param filter
     * @param maxItemLimit
     * @return
     * @throws UserStoreException
     */
    protected abstract String[] doListUsers(String filter, int maxItemLimit)
            throws UserStoreException;

    /**
     * Get the user list as for the given filter and max item limit.
     *
     * @param filter       filter.
     * @param maxItemLimit max items limit.
     * @return list of users.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    protected List<User> doListUsersWithID(String filter, int maxItemLimit)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doListUsersWithID operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException("doListUsersWithID operation is not implemented in: " + this.getClass());
    }

    @Override
    public String[] getProfileNamesWithID(String userID) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("getProfileNamesWithID operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException("getProfileNamesWithID operation is not implemented in: " + this.getClass());
    }

    /**
     * Count users with claims.
     *
     * @param claimURI Claim uri
     * @param valueFilter Filter
     * @throws UserStoreException UserStoreException
     */
    public long doCountUsersWithClaims(String claimURI, String valueFilter) throws UserStoreException {

        throw new UserStoreException("Operation is not supported");
    }

    /*This is to get the display names of users in hybrid role according to the underlying user store, to be shown in UI*/
    protected abstract String[] doGetDisplayNamesForInternalRole(String[] userNames)
            throws UserStoreException;

    /**
     * To validate username and credential that is given for authentication.
     *
     * @param userName   Name of the user.
     * @param credential Credential of the user.
     * @return false if the validation fails.
     * @throws UserStoreException UserStore Exception.
     */
    private boolean validateUserNameAndCredential(String userName, Object credential) throws UserStoreException {

        boolean isValid = true;
        if (userName == null || credential == null) {
            String message = String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION.getMessage(),
                    "Authentication failure. Either Username or Password is null");
            handleOnAuthenticateFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION.getCode(), message,
                    userName, credential);
            log.error(message);
            isValid = false;
        }
        return isValid;
    }

    private boolean validateUserIDAndCredential(String userID, Object credential) throws UserStoreException {

        boolean isValid = true;
        if (userID == null || credential == null) {
            String message = String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION.getMessage(),
                    "Authentication failure. Either Username or Password is null");
            handleOnAuthenticateFailureWithID(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION.getCode(),
                    message, userID, credential);
            log.error(message);
            isValid = false;
        }
        return isValid;
    }

    private boolean validateUserNameAndCredential(String claimURI, String claimValue, Object credential) throws UserStoreException {

        boolean isValid = true;
        if (claimURI == null || claimValue == null || credential == null) {
            String message = String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION.getMessage(),
                    "Authentication failure. One of the credential element is null.");
            handleOnAuthenticateFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION.getCode(), message,
                    claimValue, credential);
            log.error(message);
            isValid = false;
        }
        return isValid;
    }

    private boolean validateUserNameAndCredentials(List<LoginIdentifier> loginIdentifiers, Object credential)
            throws UserStoreException {

        boolean isValid = true;
        for (LoginIdentifier loginIdentifier : loginIdentifiers) {
            if (credential == null || loginIdentifier.getLoginKey() == null
                    || loginIdentifier.getLoginValue() == null) {
                String message = String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION.getMessage(),
                        "Authentication failure. One of the credential element is null.");
                handleOnAuthenticateFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION.getCode(), message,
                        loginIdentifier.getLoginValue(), credential);
                log.error(message);
                isValid = false;
                break;
            }
        }
        return isValid;
    }


    /**
     * {@inheritDoc}
     */
    public final boolean authenticate(final String userName, final Object credential) throws UserStoreException {

        try {
            return AccessController.doPrivileged((PrivilegedExceptionAction<Boolean>)
                    () -> {
                        if (!validateUserNameAndCredential(userName, credential)) {
                            return false;
                        }
                        int index = userName.indexOf(CarbonConstants.DOMAIN_SEPARATOR);
                        boolean domainProvided = index > 0;
                        return authenticate(userName, credential, domainProvided);
                    });
        } catch (PrivilegedActionException e) {
            if (!(e.getException() instanceof UserStoreException)) {
                handleOnAuthenticateFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_AUTHENTICATION.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_AUTHENTICATION.getMessage(), e.getMessage()),
                        userName, credential);
            }
            throw (UserStoreException) e.getException();
        }
    }

    protected boolean authenticate(final String userName, final Object credential, final boolean domainProvided)
            throws UserStoreException {

        try {
            return AccessController.doPrivileged((PrivilegedExceptionAction<Boolean>)
                    () -> authenticateInternalIteration(userName, credential, domainProvided));
        } catch (PrivilegedActionException e) {
            throw (UserStoreException) e.getException();
        }
    }

    private boolean authenticateInternalIteration(String userName, Object credential, boolean domainProvided)
            throws UserStoreException {

        List<String> userStorePreferenceOrder = new ArrayList<>();
        // Check whether user store chain needs to be generated or not.
        if (isUserStoreChainNeeded(userStorePreferenceOrder)) {
            if (log.isDebugEnabled()) {
                log.debug("User store chain generation is needed hence generating the user store chain using the user" +
                        " store preference order: " + userStorePreferenceOrder);
            }
            return generateUserStoreChain(userName, credential, domainProvided, userStorePreferenceOrder);
        } else {
            // Authenticate the user.
            return authenticateInternal(userName, credential, domainProvided);
        }
    }

    /**
     * This method is responsible for calling the relevant method from error listeners when there is a failure while
     * authenticating.
     *
     * @param errorCode    Error Code.
     * @param errorMessage Error Message.
     * @param userName     Name of the user.
     * @param credential   Relevant credential provided for authentication.
     * @throws UserStoreException Exception that will be thrown by relevant listener methods.
     */
    private void handleOnAuthenticateFailure(String errorCode, String errorMessage, String userName, Object credential)
            throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !listener
                    .onAuthenticateFailure(errorCode, errorMessage, userName, credential, this)) {
                return;
            }
        }
    }

    /**
     * This method is responsible for calling the relevant method from error listeners when there is a failure while
     * authenticating.
     *
     * @param errorCode    Error Code.
     * @param errorMessage Error Message.
     * @param userID       Name of the user.
     * @param credential   Relevant credential provided for authentication.
     * @throws UserStoreException Exception that will be thrown by relevant listener methods.
     */
    private void handleOnAuthenticateFailureWithID(String errorCode, String errorMessage, String userID,
            Object credential) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !((AbstractUserManagementErrorListener) listener)
                    .onAuthenticateFailureWithID(errorCode, errorMessage, userID, credential, this)) {
                return;
            }
        }
    }

    /**
     * This method is responsible for calling the relevant method from error listeners when there is a failure while
     * authenticating.
     *
     * @param errorCode        Error Code.
     * @param errorMessage     Error Message.
     * @param loginIdentifiers Login identifiers.
     * @param credential       Relevant credential provided for authentication.
     * @throws UserStoreException Exception that will be thrown by relevant listener methods.
     */
    private void handleOnAuthenticateFailureWithID(String errorCode, String errorMessage,
            List<LoginIdentifier> loginIdentifiers, Object credential) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !((AbstractUserManagementErrorListener) listener)
                    .onAuthenticateFailureWithID(errorCode, errorMessage, loginIdentifiers, credential, this)) {
                return;
            }
        }
    }

    /**
     * This method is responsible for calling the relevant method from error listeners when there is a failure while
     * authenticating.
     *
     * @param errorCode              Error Code.
     * @param errorMessage           Error Message.
     * @param preferredUserNameClaim Preferred username claim.
     * @param preferredUserNameValue Preferred username value.
     * @param credential             Relevant credential provided for authentication.
     * @throws UserStoreException Exception that will be thrown by relevant listener methods.
     */
    private void handleOnAuthenticateFailureWithID(String errorCode, String errorMessage, String preferredUserNameClaim,
            String preferredUserNameValue, Object credential) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !((AbstractUserManagementErrorListener) listener)
                    .onAuthenticateFailureWithID(errorCode, errorMessage, preferredUserNameClaim,
                            preferredUserNameValue, credential, this)) {
                return;
            }
        }
    }

    /**
     * @param userName
     * @param credential
     * @param domainProvided
     * @return
     * @throws UserStoreException
     */
    private boolean authenticateInternal(String userName, Object credential, boolean domainProvided)
            throws UserStoreException {

        boolean iterative = false;
        AbstractUserStoreManager abstractUserStoreManager = this;
        if (this instanceof IterativeUserStoreManager) {
            iterative = true;
            abstractUserStoreManager = ((IterativeUserStoreManager) this).getAbstractUserStoreManager();
        }

        boolean authenticated = false;

        UserStore userStore = abstractUserStoreManager.getUserStore(userName);

        if (domainProvided && iterative) {
            userName = userStore.getDomainFreeName();
            userStore.setRecurssive(false);
        }

        if (userStore.isRecurssive() && userStore.getUserStoreManager() instanceof AbstractUserStoreManager) {
            return ((AbstractUserStoreManager) userStore.getUserStoreManager()).authenticate(userStore.getDomainFreeName(),
                    credential, domainProvided);
        }

        Secret credentialObj;
        try {
            credentialObj = Secret.getSecret(credential);
        } catch (UnsupportedSecretTypeException e) {
            handleOnAuthenticateFailure(ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.getCode(),
                    ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.getMessage(), userName, credential);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.toString(), e);
        }

        // #################### Domain Name Free Zone Starts Here ################################

        // #################### <Listeners> #####################################################
        try {
            for (UserStoreManagerListener listener : UMListenerServiceComponent.getUserStoreManagerListeners()) {
                Object credentialArgument;
                if (listener instanceof SecretHandleableListener) {
                    credentialArgument = credentialObj;
                } else {
                    credentialArgument = credential;
                }

                if (!listener.authenticate(userName, credentialArgument, abstractUserStoreManager)) {
                    handleOnAuthenticateFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_AUTHENTICATION.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_AUTHENTICATION.getMessage(),
                            StringUtils.EMPTY), userName,
                            credentialArgument);
                    return false;
                }
            }

            try {
                for (UserOperationEventListener listener : UMListenerServiceComponent
                        .getUserOperationEventListeners()) {
                    Object credentialArgument;
                    if (listener instanceof SecretHandleableListener) {
                        credentialArgument = credentialObj;
                    } else {
                        credentialArgument = credential;
                    }
                    try {
                        if (!listener.doPreAuthenticate(userName, credentialArgument, abstractUserStoreManager)) {
                            handleOnAuthenticateFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION.getCode(),
                                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION.getMessage(),
                                            UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userName,
                                    credentialArgument);
                            return false;
                        }
                    /* Added for compatibility with old pre-listeners. */
                    } catch (CircuitBreakerOpenException circuitBreakerOpenEx) {
                        if (log.isDebugEnabled()) {
                            log.debug("Circuit Breaker is in open state for " + userStore.getDomainName()
                                    + " domain. Hence ignore the userstore and proceed", circuitBreakerOpenEx);
                        }
                        log.error("Error occurred while obtaining user store connection for: "
                                + userStore.getDomainName());
                    }
                }
            } catch (UserStoreException ex) {
                handleOnAuthenticateFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION.getMessage(),
                                ex.getMessage()), userName, credential);
                throw ex;
            }
            // #################### </Listeners> #####################################################

            int tenantId = abstractUserStoreManager.getTenantId();

            try {
                RealmService realmService = UserCoreUtil.getRealmService();
                if (realmService != null) {
                    boolean tenantActive = realmService.getTenantManager().isTenantActive(tenantId);

                    if (!tenantActive) {
                        String errorCode = ErrorMessages.ERROR_CODE_TENANT_DEACTIVATED.getCode();
                        String errorMessage = String
                                .format(ErrorMessages.ERROR_CODE_TENANT_DEACTIVATED.getMessage(), tenantId);
                        log.warn(errorCode + " - " + errorMessage);
                        handleOnAuthenticateFailure(errorCode, errorMessage, userName, credential);
                        return false;
                    }
                }
            } catch (org.wso2.carbon.user.api.UserStoreException e) {
                handleOnAuthenticateFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION.getMessage(),
                                e.getMessage()), userName, credential);
                throw new UserStoreException("Error while trying to check tenant status for Tenant : " + tenantId, e);
            }

            // We are here due to two reason. Either there is no secondary UserStoreManager or no
            // domain name provided with user name.
            try {
                // Validate whether circuit breaker is enabled and open.
                if (isCircuitBreakerEnabledAndOpen()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Avoiding searching the " + abstractUserStoreManager.getMyDomainName()
                                + " domain as Circuit Breaker is in open state");
                    }
                } else {
                    // Let's authenticate with the primary UserStoreManager.
                    if (abstractUserStoreManager.isUniqueUserIdEnabled()) {
                        String userNameProperty = abstractUserStoreManager.getUsernameProperty();
                        AuthenticationResult authenticationResult = abstractUserStoreManager
                                .doAuthenticateWithID(userNameProperty, userName, credential, null);
                        if (authenticationResult.getAuthenticationStatus()
                                == AuthenticationResult.AuthenticationStatus.SUCCESS) {
                            authenticated = true;
                        }
                    } else {
                        authenticated = abstractUserStoreManager.doAuthenticate(userName, credentialObj);
                    }
                }
            } catch (Exception e) {
                handleOnAuthenticateFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_AUTHENTICATION.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_AUTHENTICATION.getMessage(), e.getMessage()),
                        userName, credential);
                // We can ignore and proceed. Ignore the results from this user store.
                // But throw the message to the upper level if it is a client exception.
                if (e instanceof UserStoreClientException) {
                    if (log.isDebugEnabled()) {
                        log.debug("Error occurred while authenticating user: " + userName, e);
                    }
                    throw (UserStoreClientException) e;
                }
                log.error("Error occurred while authenticating user: " + userName, e);
            }
        } finally {
            credentialObj.clear();
        }

        if (authenticated) {
            // Set domain in thread local variable for subsequent operations
            UserCoreUtil.setDomainInThreadLocal(UserCoreUtil.getDomainName(abstractUserStoreManager.realmConfig));
        }

        // If authentication fails in the previous step and if the user has not specified a
        // domain- then we need to execute chained UserStoreManagers recursively.
        if (!authenticated && !domainProvided) {
            AbstractUserStoreManager userStoreManager;
            if (this instanceof IterativeUserStoreManager) {
                IterativeUserStoreManager iterativeUserStoreManager = (IterativeUserStoreManager) this;
                userStoreManager = iterativeUserStoreManager.nextUserStoreManager();
            } else {
                userStoreManager = (AbstractUserStoreManager) abstractUserStoreManager.getSecondaryUserStoreManager();
            }
            if (userStoreManager != null) {
                authenticated = userStoreManager.authenticate(userName, credential, domainProvided);
            }
        }

        if (!authenticated) {
            handleOnAuthenticateFailure(ErrorMessages.ERROR_CODE_ERROR_INCORRECT_CREDENTIAL.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_INCORRECT_CREDENTIAL.getMessage(),
                            "Authentication failed"), userName, credential);
        }

        try {
            // You cannot change authentication decision in post handler to TRUE
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!listener.doPostAuthenticate(userName, authenticated, abstractUserStoreManager)) {
                    handleOnAuthenticateFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_POST_AUTHENTICATION.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_POST_AUTHENTICATION.getMessage(),
                                    UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), userName, credential);
                    return false;
                }
            }
        } catch (UserStoreException ex) {
            /* Added for compatibility with old pre-listeners. */
            if (ex instanceof CircuitBreakerOpenException) {
                if (log.isDebugEnabled()) {
                    log.debug("Circuit Breaker is in open state for " + userStore.getDomainName()
                            + " domain. Hence ignore the userstore and proceed", ex);
                }
                log.error("Error occurred while obtaining user store connection for: "
                        + userStore.getDomainName());
            } else {
                handleOnAuthenticateFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_POST_AUTHENTICATION.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_POST_AUTHENTICATION.getMessage(),
                                ex.getMessage()), userName, credential);
                throw ex;
            }
        }

        if (log.isDebugEnabled()) {
            if (!authenticated) {
                log.debug("Authentication failure. Wrong username or password is provided.");
            }
        }

        return authenticated;
    }

    private String getUsernameProperty() throws org.wso2.carbon.user.api.UserStoreException {

        String userNameProperty = realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE);
        if (StringUtils.isBlank(userNameProperty)) {
             userNameProperty = claimManager
                    .getAttributeName(getMyDomainName(), UserCoreClaimConstants.USERNAME_CLAIM_URI);
        }
        return userNameProperty;
    }

    /**
     * This method calls the relevant methods when there is a failure while trying to get the claim value of a user.
     *
     * @param errorCode    Error code.
     * @param errorMessage Error message.
     * @param userName     Name of the user.
     * @param claim        Relevant claim.
     * @param profileName  Name of the profile.
     * @throws UserStoreException Exception that will be thrown by relevant listeners.
     */
    private void handleGetUserClaimValueFailure(String errorCode, String errorMessage, String userName, String claim,
            String profileName) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !listener
                    .onGetUserClaimValueFailure(errorCode, errorMessage, userName, claim, profileName, this)) {
                return;
            }
        }
    }

    /**
     * This method calls the relevant methods when there is a failure while trying to get the claim value of a user.
     *
     * @param errorCode    Error code.
     * @param errorMessage Error message.
     * @param userID       Name of the user.
     * @param claim        Relevant claim.
     * @param profileName  Name of the profile.
     * @throws UserStoreException Exception that will be thrown by relevant listeners.
     */
    private void handleGetUserClaimValueFailureWithID(String errorCode, String errorMessage, String userID,
            String claim, String profileName) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !((AbstractUserManagementErrorListener) listener)
                    .onGetUserClaimValueFailureWithID(errorCode, errorMessage, userID, claim, profileName, this)) {
                return;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public final String getUserClaimValue(String userName, String claim, String profileName)
            throws UserStoreException {

        if (!isSecureCall.get()) {
            Class[] argTypes = new Class[]{String.class, String.class, String.class};
            Object object = callSecure("getUserClaimValue", new Object[]{userName, claim, profileName}, argTypes);
            return (String) object;
        }

        UserStore userStore = getUserStore(userName);
        if (userStore.isRecurssive()) {
            return userStore.getUserStoreManager().getUserClaimValue(userStore.getDomainFreeName(),
                    claim, profileName);
        }

        // #################### Domain Name Free Zone Starts Here ################################
        // If user does not exist, throw an

        boolean isUserExist;
        String userID = null;
        if (isUniqueUserIdEnabledInUserStore(userStore)) {
            userID = getUserIDFromUserName(userName);
            isUserExist = userID != null;
        } else {
            isUserExist = doCheckExistingUser(userName);
        }

        if (!isUserExist) {
            handleGetNonExistentUser(userName, claim, profileName);
        }

        Map<String, String> finalValues;
        try {
            if (isUniqueUserIdEnabledInUserStore(userStore)) {
                finalValues = doGetUserClaimValuesWithID(userID, new String[]{claim},
                        userStore.getDomainName(), profileName);
            } else {
                finalValues = doGetUserClaimValues(userName, new String[]{claim}, userStore.getDomainName(),
                        profileName);
            }
        } catch (UserStoreException ex) {
            handleGetUserClaimValueFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_USER_CLAIM_VALUE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_USER_CLAIM_VALUE.getMessage(),
                            ex.getMessage()), userName, claim, profileName);
            throw ex;
        }

        String value = null;

        if (finalValues != null) {
            value = finalValues.get(claim);
        }

        // #################### <Listeners> #####################################################

        List<String> list = new ArrayList<>();
        if (value != null) {
            list.add(value);
        }

        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (listener instanceof AbstractUserOperationEventListener) {
                    AbstractUserOperationEventListener newListener = (AbstractUserOperationEventListener) listener;
                    if (!newListener.doPostGetUserClaimValue(userName, claim, list, profileName, this)) {
                        handleGetUserClaimValueFailure(
                                ErrorMessages.ERROR_CODE_ERROR_DURING_POST_GET_USER_CLAIM_VALUE.getCode(),
                                String.format(
                                        ErrorMessages.ERROR_CODE_ERROR_DURING_POST_GET_USER_CLAIM_VALUE.getMessage(),
                                        UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), userName, claim,
                                profileName);
                        break;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleGetUserClaimValueFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_GET_USER_CLAIM_VALUE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_GET_USER_CLAIM_VALUE.getMessage(),
                            ex.getMessage()), userName, claim, profileName);
            throw ex;
        }
        // #################### </Listeners> #####################################################

        if (!list.isEmpty()) {
            return list.get(0);
        }
        return value;
    }

    private boolean isUniqueUserIdEnabledInUserStore(UserStore userStore) {

        return isUniqueUserIdEnabled(userStore.getUserStoreManager());
    }

    /**
     * Checks whether groups and roles separation feature enabled.
     *
     * @return {@code true} if the groups and roles separation feature enabled.
     */
    public boolean isRoleAndGroupSeparationEnabled() {

        return Boolean.parseBoolean(realmConfig.getAuthorizationManagerProperty(
                UserCoreConstants.RealmConfig.PROPERTY_GROUP_AND_ROLE_SEPARATION_ENABLED));
    }

    /**
     * This method is responsible for calling relevant listener methods when there is a failure while trying to get
     * user claim values.
     *
     * @param errorCode    Relevant error code.
     * @param errorMessage Relevant error message.
     * @param userName     Name of the user.
     * @param claims       Claims requested.
     * @param profileName  Name of the profile.
     * @throws UserStoreException Exception that will be thrown by the relevant listeners.
     */
    private void handleGetUserClaimValuesFailure(String errorCode, String errorMessage, String userName,
            String[] claims, String profileName) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !listener
                    .onGetUserClaimValuesFailure(errorCode, errorMessage, userName, claims, profileName, this)) {
                return;
            }
        }
    }

    /**
     * This method is responsible for calling relevant listener methods when there is a failure while trying to get
     * user claim values.
     *
     * @param errorCode    Relevant error code.
     * @param errorMessage Relevant error message.
     * @param userID       ID of the user.
     * @param claims       Claims requested.
     * @param profileName  Name of the profile.
     * @throws UserStoreException Exception that will be thrown by the relevant listeners.
     */
    private void handleGetUserClaimValuesFailureWithID(String errorCode, String errorMessage, String userID,
            String[] claims, String profileName) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !((AbstractUserManagementErrorListener) listener)
                    .onGetUserClaimValuesFailureWithID(errorCode, errorMessage, userID, claims, profileName, this)) {
                return;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public final Claim[] getUserClaimValues(String userName, String profileName)
            throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, String.class};
            Object object = callSecure("getUserClaimValues", new Object[]{userName, profileName}, argTypes);
            return (Claim[]) object;
        }

        UserStore userStore = getUserStore(userName);
        if (userStore.isRecurssive()) {
            return userStore.getUserStoreManager().getUserClaimValues(
                    userStore.getDomainFreeName(), profileName);
        }

        // #################### Domain Name Free Zone Starts Here ################################
        // If user does not exist, throw exception
        // Property to check whether this user store supports new APIs with unique user id.
        boolean isUniqueUserIdEnabled = isUniqueUserIdEnabledInUserStore(userStore);
        String userID = null;
        if (isUniqueUserIdEnabled) {
            userID = getUserIDFromUserName(userName);
        }

        boolean isUserExists;
        if (isUniqueUserIdEnabled) {
            isUserExists = userID != null;
        } else {
            isUserExists = doCheckExistingUser(userStore.getDomainFreeName());
        }

        if (!isUserExists) {
            String errorMessage = String.format(ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getMessage(), userName,
                    realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME));
            String errorCode = ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getCode();
            handleGetUserClaimValuesFailure(errorCode, errorMessage, userName, null, profileName);
            throw new UserStoreException(errorCode + " - " + errorMessage);
        }

        if (profileName == null || profileName.trim().length() == 0) {
            profileName = UserCoreConstants.DEFAULT_PROFILE;
        }

        String[] claims;
        try {
            claims = claimManager.getAllClaimUris();
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            handleGetUserClaimValuesFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_CLAIM_URI.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_CLAIM_URI.getMessage(), e.getMessage()),
                    userName, null, profileName);
            throw new UserStoreException(e);
        }

        Map<String, String> values = this.getUserClaimValues(userName, claims, profileName);
        List<Claim> finalValues = new ArrayList<>();
        addClaimValues(values, finalValues);

        return finalValues.toArray(new Claim[0]);
    }

    /**
     * {@inheritDoc}
     */
    public final Map<String, String> getUserClaimValues(String userName, String[] claims,
                                                        String profileName) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, String[].class, String.class};
            Object object = callSecure("getUserClaimValues", new Object[]{userName, claims, profileName}, argTypes);
            return (Map<String, String>) object;
        }

        UserStore userStore = getUserStore(userName);
        if (userStore.isRecurssive()) {
            return userStore.getUserStoreManager().getUserClaimValues(
                    userStore.getDomainFreeName(), claims, profileName);
        }

        String userID = null;
        boolean isUserExists;

        boolean isUniqueIdEnabled = isUniqueUserIdEnabledInUserStore(userStore);
        if (isUniqueIdEnabled) {
            userID = getUserIDFromUserName(userName);
            isUserExists = userID != null;
        } else{
            isUserExists = doCheckExistingUser(userStore.getDomainFreeName());
        }

        // #################### Domain Name Free Zone Starts Here ################################
        if (!isUserExists) {
            String errorMessage = String.format(ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getMessage(), userName,
                    realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME));
            String errorCode = ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getCode();
            handleGetUserClaimValuesFailure(errorCode, errorMessage, userName, null, profileName);
            throw new UserStoreException(errorCode + " - " + errorMessage);
        }
        // check for null claim list
        if (claims == null) {
            claims = new String[0];
        }
        Map<String, String> finalValues = new HashMap<>();
        String[] allClaims = claims.clone();

        // #################### <PreListeners> ###################################################
        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (listener instanceof AbstractUserOperationEventListener) {
                    AbstractUserOperationEventListener newListener = (AbstractUserOperationEventListener) listener;
                    if (!newListener
                            .doPreGetUserClaimValues(userStore.getDomainFreeName(), claims, profileName, finalValues,
                                    this)) {
                        handleGetUserClaimValuesFailure(
                                ErrorMessages.ERROR_CODE_ERROR_IN_PRE_GET_CLAIM_VALUES.getCode(),
                                String.format(ErrorMessages.ERROR_CODE_ERROR_IN_PRE_GET_CLAIM_VALUES.getMessage(),
                                        UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), userName, claims,
                                profileName);
                        break;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleGetUserClaimValuesFailure(ErrorMessages.ERROR_CODE_ERROR_IN_PRE_GET_CLAIM_VALUES.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_IN_PRE_GET_CLAIM_VALUES.getMessage(),
                            ex.getMessage()), userName, claims, profileName);
            throw ex;
        }
        // #################### </PreListeners> ###################################################

        claims = removeNullElements(claims);

        try {
            if (isUniqueIdEnabled) {
                finalValues = doGetUserClaimValuesWithID(userID, claims, userStore.getDomainName(), profileName);
            } else {
                finalValues = doGetUserClaimValues(userStore.getDomainFreeName(), claims, userStore.getDomainName(),
                        profileName);
            }
        } catch (UserStoreException ex) {
            handleGetUserClaimValuesFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_CLAIM_VALUES.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_CLAIM_VALUES.getMessage(),
                            ex.getMessage()), userName, claims, profileName);
            throw ex;
        }

        // #################### <PostListeners> #####################################################
        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (listener instanceof AbstractUserOperationEventListener) {
                    AbstractUserOperationEventListener newListener = (AbstractUserOperationEventListener) listener;
                    if (!newListener
                            .doPostGetUserClaimValues(userStore.getDomainFreeName(), allClaims, profileName,
                                    finalValues, this)) {
                        handleGetUserClaimValuesFailure(
                                ErrorMessages.ERROR_CODE_ERROR_IN_POST_GET_CLAIM_VALUES.getCode(),
                                String.format(ErrorMessages.ERROR_CODE_ERROR_IN_POST_GET_CLAIM_VALUES.getMessage(),
                                        UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), userName, claims,
                                profileName);
                        break;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleGetUserClaimValuesFailure(ErrorMessages.ERROR_CODE_ERROR_IN_POST_GET_CLAIM_VALUES.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_IN_POST_GET_CLAIM_VALUES.getMessage(),
                            ex.getMessage()), userName, claims, profileName);
            throw ex;
        }
        // #################### </PostListeners> #####################################################

        return finalValues;
    }

    /**
     * Removes all null elements from the given String array.
     *
     * @param array Array The input String array.
     * @return A new String array containing only non-null elements.
     */
    private static String[] removeNullElements(String[] array) {

        return Arrays.stream(array).filter(Objects::nonNull).toArray(String[]::new);
    }

    /**
     * This method is responsible for calling the relevant methods when there is a failure while trying to get the
     * user list.
     *
     * @param errorCode    Error Code.
     * @param errorMessage Error Message.
     * @param claim        Claim URI.
     * @param claimValue   Claim Value.
     * @param profileName  Name of the profile.
     * @throws UserStoreException Exception that will be thrown by relevant listner methods.
     */
    private void handleGetUserListFailure(String errorCode, String errorMessage, String claim, String claimValue,
            String profileName) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !listener
                    .onGetUserListFailure(errorCode, errorMessage, claim, claimValue, profileName, this)) {
                return;
            }
        }
    }

    /**
     * This method is responsible for calling the relevant methods when there is a failure while trying to get the
     * user count.
     *
     * @param errorCode    Error Code.
     * @param errorMessage Error Message.
     * @param claim        Claim URI.
     * @param claimValue   Claim Value.
     * @throws UserStoreException Exception that will be thrown by relevant listner methods.
     */
    protected void handleGetUserCountFailure(String errorCode, String errorMessage, String claim, String claimValue
    ) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !listener
                    .onGetUserCountFailure(errorCode, errorMessage, claim, claimValue, this)) {
                return;
            }
        }
    }

    /**
     * This method is responsible for calling the relevant methods when there is a failure while trying to get the
     * user list.
     *
     * @param errorCode    Error Code.
     * @param errorMessage Error Message.
     * @param claim        Claim URI.
     * @param claimValue   Claim Value.
     * @param profileName  Name of the profile.
     * @throws UserStoreException Exception that will be thrown by relevant listener methods.
     */
    private void handleGetUserListFailureWithID(String errorCode, String errorMessage, String claim, String claimValue,
            String profileName) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !((AbstractUserManagementErrorListener) listener)
                    .onGetUserListFailureWithID(errorCode, errorMessage, claim, claimValue, profileName, this)) {
                return;
            }
        }
    }

    /**
     * This method is responsible for calling the relevant methods when there is a failure while trying to get the
     * user.
     *
     * @param errorCode       Error Code.
     * @param errorMessage    Error Message.
     * @param userID          User ID.
     * @param requestedClaims Requested Claims.
     * @param profileName     Profile Name.
     * @throws UserStoreException Exception that will be thrown by relevant listener methods.
     */
    private void handleGetUserFailureWithID(String errorCode, String errorMessage, String userID,
            String[] requestedClaims, String profileName) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !((AbstractUserManagementErrorListener) listener)
                    .onGetUserFailureWithID(errorCode, errorMessage, userID, requestedClaims, profileName, this)) {
                return;
            }
        }
    }

    /**
     * This method is responsible for calling the relevant methods when there is a failure while trying to get the
     * user list.
     *
     * @param errorCode    Error Code.
     * @param errorMessage Error Message.
     * @param claim        Claim URI.
     * @param claimValue   Claim Value.
     * @param limit        No of search records.
     * @param offset       Start index of the search.
     * @param profileName  Name of the profile.
     * @throws UserStoreException Exception that will be thrown by relevant listner methods.
     */
    private void handleGetUserListFailure(String errorCode, String errorMessage, String claim, String claimValue,
            int limit, int offset, String profileName) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && listener instanceof AbstractUserManagementErrorListener
                    && !listener
                    .onGetUserListFailure(errorCode, errorMessage, claim, claimValue, limit, offset, profileName,
                            this)) {
                return;
            }
        }
    }

    /**
     * This method is responsible for calling the relevant methods when there is a failure while trying to get the
     * user list.
     *
     * @param errorCode    Error Code.
     * @param errorMessage Error Message.
     * @param claim        Claim URI.
     * @param claimValue   Claim Value.
     * @param limit        No of search records.
     * @param offset       Start index of the search.
     * @param profileName  Name of the profile.
     * @throws UserStoreException Exception that will be thrown by relevant listner methods.
     */
    private void handleGetUserListFailureWithID(String errorCode, String errorMessage, String claim, String claimValue,
            int limit, int offset, String profileName) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && listener instanceof AbstractUserManagementErrorListener
                    && !((AbstractUserManagementErrorListener) listener)
                    .onGetUserListFailureWithID(errorCode, errorMessage, claim, claimValue, limit, offset, profileName,
                            this)) {
                return;
            }
        }
    }

    private void handleGetUserListFailure(String errorCode, String errorMassage, Condition condition, String domain,
            String profileName, int limit, int offset, String sortBy, String sortOrder) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && listener instanceof AbstractUserManagementErrorListener
                    && !listener
                    .onGetUserListFailure(errorCode, errorMassage, condition, domain, profileName, limit, offset,
                            sortBy, sortOrder, this)) {
                return;
            }
        }
    }

    private void handleGetUserListFailureWithID(String errorCode, String errorMassage, Condition condition,
            String domain, String profileName, int limit, int offset, String sortBy, String sortOrder)
            throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && listener instanceof AbstractUserManagementErrorListener
                    && !((AbstractUserManagementErrorListener) listener)
                    .onGetUserListFailureWithID(errorCode, errorMassage, condition, domain, profileName, limit, offset,
                            sortBy, sortOrder, this)) {
                return;
            }
        }
    }

    /**
     * This method is responsible for calling the relevant methods when there is a failure while trying to get the
     * paginated user list.
     *
     * @param errorCode    Error Code.
     * @param errorMessage Error Message.
     * @param claim        Claim URI.
     * @param claimValue   Claim Value.
     * @param profileName  Name of the profile.
     * @throws UserStoreException Exception that will be thrown by relevant listner methods.
     */
    private void handleGetPaginatedUserListFailure(String errorCode, String errorMessage, String claim, String
            claimValue, String profileName) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && listener instanceof AbstractUserManagementErrorListener
                    && !listener.onGetPaginatedUserListFailure(errorCode, errorMessage, claim, claimValue,
                    profileName, this)) {
                return;
            }
        }
    }

    /**
     * This method is responsible for calling the relevant methods when there is a failure while trying to get the
     * paginated user list.
     *
     * @param errorCode    Error Code.
     * @param errorMessage Error Message.
     * @param claim        Claim URI.
     * @param claimValue   Claim Value.
     * @param profileName  Name of the profile.
     * @throws UserStoreException Exception that will be thrown by relevant listner methods.
     */
    private void handleGetPaginatedUserListFailureWithID(String errorCode, String errorMessage, String claim,
            String claimValue, String profileName) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && listener instanceof AbstractUserManagementErrorListener
                    && !((AbstractUserManagementErrorListener) listener)
                    .onGetPaginatedUserListFailureWithID(errorCode, errorMessage, claim, claimValue, profileName,
                            this)) {
                return;
            }
        }
    }

    /**
     * This method is responsible for calling the relevant methods when there is a failure while trying to list the
     * paginated users.
     *
     * @param errorCode    Error Code.
     * @param errorMessage Error Message.
     * @param filter       Username Filter.
     * @param limit        No of search results.
     * @param offset       Start index of the search.
     * @throws UserStoreException Exception that will be thrown by relevant listner methods.
     */
    private void handleListPaginatedUsersFailure(String errorCode, String errorMessage, String filter, int limit, int
            offset) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && listener instanceof AbstractUserManagementErrorListener
                    && !listener.onListUsersFailure(errorCode, errorMessage, filter, limit, offset, this)) {
                return;
            }
        }
    }

    /**
     * This method is responsible for calling the relevant methods when there is a failure while trying to list the
     * paginated users.
     *
     * @param errorCode    Error Code.
     * @param errorMessage Error Message.
     * @param filter       Username Filter.
     * @param limit        No of search results.
     * @param offset       Start index of the search.
     * @throws UserStoreException Exception that will be thrown by relevant listner methods.
     */
    private void handleListPaginatedUsersFailureWithID(String errorCode, String errorMessage, String filter, int limit,
            int offset) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && listener instanceof AbstractUserManagementErrorListener
                    && !((AbstractUserManagementErrorListener) listener)
                    .onListUsersFailureWithID(errorCode, errorMessage, filter, limit, offset, this)) {
                return;
            }
        }
    }

    /**
     * To call the postGetUserList of relevant listeners.
     *
     * @param claim            Claim requested.
     * @param claimValue       Claim values.
     * @param filteredUserList List of filtered users.
     * @param isAuditLogOnly   To indicate whether to call only audit log listener.
     * @throws UserStoreException User Store Exception.
     */
    private void handlePostGetUserList(String claim, String claimValue, List<String> filteredUserList,
            boolean isAuditLogOnly) throws UserStoreException {

        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (listener instanceof AbstractUserOperationEventListener) {
                    if (isAuditLogOnly && !listener.getClass().getName()
                            .endsWith(UserCoreErrorConstants.AUDIT_LOGGER_CLASS_NAME)) {
                        continue;
                    }
                    AbstractUserOperationEventListener newListener = (AbstractUserOperationEventListener) listener;
                    if (!newListener.doPostGetUserList(claim, claimValue, filteredUserList, this)) {
                        break;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleGetUserListFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_GET_USER_LIST.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_GET_USER_LIST.getMessage(),
                            ex.getMessage()), claim, claimValue, null);
            throw ex;
        }
    }

    /**
     * To call the postGetUserListWithID of relevant listeners.
     *
     * @param claim            Claim requested.
     * @param claimValue       Claim values.
     * @param filteredUserList List of filtered users.
     * @param isAuditLogOnly   To indicate whether to call only audit log listener.
     * @throws UserStoreException User Store Exception.
     */
    private void handlePostGetUserListWithID(String claim, String claimValue, List<User> filteredUserList,
            boolean isAuditLogOnly) throws UserStoreException {

        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (listener instanceof AbstractUserOperationEventListener) {
                    if (isAuditLogOnly && !listener.getClass().getName()
                            .endsWith(UserCoreErrorConstants.AUDIT_LOGGER_CLASS_NAME)) {
                        continue;
                    }
                    AbstractUserOperationEventListener newListener = (AbstractUserOperationEventListener) listener;
                    if (!newListener.doPostGetUserListWithID(claim, claimValue, filteredUserList, this)) {
                        break;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleGetUserListFailureWithID(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_GET_USER_LIST.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_GET_USER_LIST.getMessage(),
                            ex.getMessage()), claim, claimValue, null);
            throw ex;
        }
    }

    /**
     * To call the postGetUserList of relevant listeners.
     *
     * @param claim            Claim requested.
     * @param claimValue       Claim values.
     * @param filteredUserList List of filtered users.
     * @param limit            No of search results.
     * @param offset           Start index of the search.
     * @param isAuditLogOnly   To indicate whether to call only audit log listener.
     * @throws UserStoreException User Store Exception.
     */
    private void handlePostGetUserList(String claim, String claimValue, List<String> filteredUserList, int limit, int
            offset, boolean isAuditLogOnly) throws UserStoreException {

        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (listener instanceof AbstractUserOperationEventListener) {
                    if (isAuditLogOnly && !listener.getClass().getName()
                            .endsWith(UserCoreErrorConstants.AUDIT_LOGGER_CLASS_NAME)) {
                        continue;
                    }
                    AbstractUserOperationEventListener newListener = (AbstractUserOperationEventListener) listener;
                    if (!newListener.doPostGetUserList(claim, claimValue, filteredUserList, limit, offset, this)) {
                        break;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleGetUserListFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_GET_USER_LIST.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_GET_USER_LIST.getMessage(),
                            ex.getMessage()), claim, claimValue, limit, offset, null);
            throw ex;
        }
    }

    /**
     * To call the postGetUserList of relevant listeners.
     *
     * @param claim            Claim requested.
     * @param claimValue       Claim values.
     * @param filteredUserList List of filtered users.
     * @param limit            No of search results.
     * @param offset           Start index of the search.
     * @param isAuditLogOnly   To indicate whether to call only audit log listener.
     * @throws UserStoreException User Store Exception.
     */
    private void handlePostGetUserListWithID(String claim, String claimValue, List<User> filteredUserList, int limit,
            int offset, boolean isAuditLogOnly) throws UserStoreException {

        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (listener instanceof AbstractUserOperationEventListener) {
                    if (isAuditLogOnly && !listener.getClass().getName()
                            .endsWith(UserCoreErrorConstants.AUDIT_LOGGER_CLASS_NAME)) {
                        continue;
                    }
                    AbstractUserOperationEventListener newListener = (AbstractUserOperationEventListener) listener;
                    if (!newListener
                            .doPostGetUserListWithID(claim, claimValue, filteredUserList, limit, offset, this)) {
                        break;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleGetUserListFailureWithID(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_GET_USER_LIST.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_GET_USER_LIST.getMessage(),
                            ex.getMessage()), claim, claimValue, limit, offset, null);
            throw ex;
        }
    }

    private void handlePostGetUserList(Condition condition, String domain, String profileName, int limit, int offset,
                                       String sortBy, String sortOrder, String[] users, boolean isAuditLogOnly)
            throws UserStoreException {

        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (listener instanceof AbstractUserOperationEventListener) {
                    if (isAuditLogOnly && !listener.getClass().getName()
                            .endsWith(UserCoreErrorConstants.AUDIT_LOGGER_CLASS_NAME)) {
                        continue;
                    }
                    AbstractUserOperationEventListener newListener = (AbstractUserOperationEventListener) listener;
                    if (!newListener.doPostGetUserList(condition, domain, profileName, limit, offset, sortBy,
                            sortOrder, users, this)) {
                        break;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleGetUserListFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_GET_CONDITIONAL_USER_LIST.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_GET_CONDITIONAL_USER_LIST.getMessage(),
                            ex.getMessage()), condition, domain, profileName, limit, offset, sortBy, sortOrder);
            throw ex;
        }
    }

    private void handlePostGetUserListWithID(Condition condition, String domain, String profileName, int limit,
                                             int offset, String sortBy, String sortOrder, List<User> users, boolean isAuditLogOnly)
            throws UserStoreException {

        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (listener instanceof AbstractUserOperationEventListener) {
                    if (isAuditLogOnly && !listener.getClass().getName()
                            .endsWith(UserCoreErrorConstants.AUDIT_LOGGER_CLASS_NAME)) {
                        continue;
                    }
                    AbstractUserOperationEventListener newListener = (AbstractUserOperationEventListener) listener;
                    if (!newListener.doPostGetUserListWithID(condition, domain, profileName, limit, offset, sortBy,
                            sortOrder, users, this)) {
                        break;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleGetUserListFailureWithID(
                    ErrorMessages.ERROR_CODE_ERROR_DURING_POST_GET_CONDITIONAL_USER_LIST.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_GET_CONDITIONAL_USER_LIST.getMessage(),
                            ex.getMessage()), condition, domain, profileName, limit, offset, sortBy, sortOrder);
            throw ex;
        }
    }

    private void handlePreGetUserList(Condition condition, String domain, String profileName, int limit, int offset,
                                      String sortBy, String sortOrder) throws UserStoreException {

        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent
                    .getUserOperationEventListeners()) {
                if (listener instanceof AbstractUserOperationEventListener) {
                    AbstractUserOperationEventListener newListener = (AbstractUserOperationEventListener) listener;
                    if (!newListener.doPreGetUserList(condition, domain, profileName, limit, offset, sortBy,
                            sortOrder, this)) {

                        handleGetUserListFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET__CONDITIONAL_USER_LIST
                                        .getCode(),
                                String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET__CONDITIONAL_USER_LIST
                                                .getMessage(),
                                        UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), condition, domain,
                                profileName, limit, offset, sortBy, sortOrder);
                        break;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleGetUserListFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET__CONDITIONAL_USER_LIST.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET__CONDITIONAL_USER_LIST.getMessage(),
                            ex.getMessage()), condition, domain,
                    profileName, limit, offset, sortBy, sortOrder);
            throw ex;
        }
    }

    private void handlePreGetUserListWithID(Condition condition, String domain, String profileName, int limit,
            int offset,
            String sortBy, String sortOrder) throws UserStoreException {

        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent
                    .getUserOperationEventListeners()) {
                if (listener instanceof AbstractUserOperationEventListener) {
                    AbstractUserOperationEventListener newListener = (AbstractUserOperationEventListener) listener;
                    if (!newListener.doPreGetUserListWithID(condition, domain, profileName, limit, offset, sortBy,
                            sortOrder, this)) {

                        handleGetUserListFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET__CONDITIONAL_USER_LIST.getCode(),
                                String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET__CONDITIONAL_USER_LIST.getMessage(),
                                        UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), condition, domain,
                                profileName, limit, offset, sortBy, sortOrder);
                        break;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleGetUserListFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET__CONDITIONAL_USER_LIST.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET__CONDITIONAL_USER_LIST.getMessage(),
                            ex.getMessage()), condition, domain,
                    profileName, limit, offset, sortBy, sortOrder);
            throw ex;
        }
    }


    /**
     * To call the postGetPaginatedUserList of relevant listeners.
     *
     * @param claim            Claim requested.
     * @param claimValue       Claim values.
     * @param filteredUserList List of filtered users.
     * @param isAuditLogOnly   To indicate whether to call only audit log listener.
     * @throws UserStoreException User Store Exception.
     */
    private void handlePostGetPaginatedUserList(String claim, String claimValue, List<String> filteredUserList,
                                       boolean isAuditLogOnly) throws UserStoreException {

        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (listener instanceof AbstractUserOperationEventListener) {
                    if (isAuditLogOnly && !listener.getClass().getName()
                            .endsWith(UserCoreErrorConstants.AUDIT_LOGGER_CLASS_NAME)) {
                        continue;
                    }
                    AbstractUserOperationEventListener newListener = (AbstractUserOperationEventListener) listener;
                    if (!newListener.doPostGetPaginatedUserList(claim, claimValue, filteredUserList, this)) {
                        break;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleGetPaginatedUserListFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_GET_PAGINATED_USER_LIST.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_GET_PAGINATED_USER_LIST.getMessage(),
                            ex.getMessage()), claim, claimValue, null);
            throw ex;
        }
    }

    /**
     * To call the List paginated users of relevant listeners.
     *
     * @param filter           Username filter.
     * @param limit            No of search results.
     * @param offset           start index of the search.
     * @param filteredUserList List of filtered users.
     * @param isAuditLogOnly   To indicate whether to call only audit log listener.
     * @throws UserStoreException User Store Exception.
     */
    private void handlePostListPaginatedUsers(String filter, int limit, int offset, List<String> filteredUserList,
                                                boolean isAuditLogOnly) throws UserStoreException {

        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (listener instanceof AbstractUserOperationEventListener) {
                    if (isAuditLogOnly && !listener.getClass().getName()
                            .endsWith(UserCoreErrorConstants.AUDIT_LOGGER_CLASS_NAME)) {
                        continue;
                    }
                    AbstractUserOperationEventListener newListener = (AbstractUserOperationEventListener) listener;
                    if (!newListener.doPostListUsers(filter, limit, offset, filteredUserList, this)) {
                        break;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleListPaginatedUsersFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_LIST_PAGINATED_USER.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_LIST_PAGINATED_USER.getMessage(),
                            ex.getMessage()), filter, limit, offset);
            throw ex;
        }
    }

    /**
     * To call the List paginated users of relevant listeners.
     *
     * @param filter           Username filter.
     * @param limit            No of search results.
     * @param offset           start index of the search.
     * @param filteredUserList List of filtered users.
     * @param isAuditLogOnly   To indicate whether to call only audit log listener.
     * @throws UserStoreException User Store Exception.
     */
    private void handlePostListPaginatedUsersWithID(String filter, int limit, int offset, List<User> filteredUserList,
            boolean isAuditLogOnly) throws UserStoreException {

        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (listener instanceof AbstractUserOperationEventListener) {
                    if (isAuditLogOnly && !listener.getClass().getName()
                            .endsWith(UserCoreErrorConstants.AUDIT_LOGGER_CLASS_NAME)) {
                        continue;
                    }
                    AbstractUserOperationEventListener newListener = (AbstractUserOperationEventListener) listener;
                    if (!newListener.doPostListUsersWithID(filter, limit, offset, filteredUserList, this)) {
                        break;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleListPaginatedUsersFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_LIST_PAGINATED_USER.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_LIST_PAGINATED_USER.getMessage(),
                            ex.getMessage()), filter, limit, offset);
            throw ex;
        }
    }

    @Override
    public final String[] getUserList(String claim, String claimValue, String profileName) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[] { String.class, String.class, String.class };
            Object object = callSecure("getUserList", new Object[] { claim, claimValue, profileName }, argTypes);
            return (String[]) object;
        }

        if (claim == null) {
            String errorCode = ErrorMessages.ERROR_CODE_INVALID_CLAIM_URI.getCode();
            String errorMessage = String.format(ErrorMessages.ERROR_CODE_INVALID_CLAIM_URI.getMessage(), "");
            handleGetUserListFailure(errorCode, errorMessage, null, claimValue, profileName);
            throw new IllegalArgumentException(ErrorMessages.ERROR_CODE_INVALID_CLAIM_URI.toString());
        }

        if (claimValue == null) {
            handleGetUserListFailure(ErrorMessages.ERROR_CODE_INVALID_CLAIM_VALUE.getCode(),
                    ErrorMessages.ERROR_CODE_INVALID_CLAIM_VALUE.getMessage(), claim, null, profileName);
            throw new IllegalArgumentException(ErrorMessages.ERROR_CODE_INVALID_CLAIM_VALUE.toString());
        }

        if (log.isDebugEnabled()) {
            log.debug("Listing users who having value as " + claimValue + " for the claim " + claim);
        }

        if (!isUniqueUserIdEnabled() && (USERNAME_CLAIM_URI.equalsIgnoreCase(claim) || SCIM_USERNAME_CLAIM_URI
                .equalsIgnoreCase(claim) || SCIM2_USERNAME_CLAIM_URI.equalsIgnoreCase(claim))) {

            if (log.isDebugEnabled()) {
                log.debug("Switching to list users using username");
            }

            String[] filteredUsers = listUsers(claimValue, MAX_ITEM_LIMIT_UNLIMITED);

            if (log.isDebugEnabled()) {
                log.debug("Filtered users: " + Arrays.toString(filteredUsers));
            }

            return filteredUsers;
        }

        // Extracting the domain from claimValue.
        String extractedDomain = null;
        int index;
        index = claimValue.indexOf(CarbonConstants.DOMAIN_SEPARATOR);
        if (index > 0) {
            String[] names = claimValue.split(CarbonConstants.DOMAIN_SEPARATOR);
            extractedDomain = names[0].trim();
        }

        UserStoreManager userManager = this;
        if (StringUtils.isNotEmpty(extractedDomain) && !StringUtils.equalsIgnoreCase(getMyDomainName(), extractedDomain)) {
            UserStoreManager secondaryUserStoreManager = getSecondaryUserStoreManager(extractedDomain);
            if (secondaryUserStoreManager != null) {
                userManager = secondaryUserStoreManager;
                if (log.isDebugEnabled()) {
                    log.debug("Domain: " + extractedDomain + " is passed with the claim and user store manager is loaded"
                            + " for the given domain name.");
                }
            } else {
                throw new UserStoreClientException("Invalid Domain Name: " + extractedDomain);
            }
        }

        if (userManager instanceof JDBCUserStoreManager && (SCIM_USERNAME_CLAIM_URI.equalsIgnoreCase(claim)
                || SCIM2_USERNAME_CLAIM_URI.equalsIgnoreCase(claim))) {
            if (userManager.isExistingUser(claimValue)) {
                return new String[] { claimValue };
            } else {
                return new String[0];
            }
        }

        claimValue = UserCoreUtil.removeDomainFromName(claimValue);

        final List<String> filteredUserList = new ArrayList<>();

        if (StringUtils.isNotEmpty(extractedDomain)) {
            try {
                for (UserOperationEventListener listener : UMListenerServiceComponent
                        .getUserOperationEventListeners()) {
                    if (listener instanceof AbstractUserOperationEventListener) {
                        AbstractUserOperationEventListener newListener = (AbstractUserOperationEventListener) listener;
                        if (!newListener.doPreGetUserList(claim, claimValue, filteredUserList, userManager)) {
                            handleGetUserListFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST.getCode(),
                                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST.getMessage(),
                                            UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), claim,
                                    claimValue, profileName);
                            break;
                        }
                    }
                }
            } catch (UserStoreException ex) {
                handleGetUserListFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST.getMessage(),
                                ex.getMessage()), claim, claimValue, profileName);
                throw ex;
            }
            if (log.isDebugEnabled()) {
                log.debug("Pre listener user list: " + filteredUserList + " for domain: " + extractedDomain);
            }
        }

        // Iterate through user stores and check for users for this claim.
        List<User> usersFromUserStore;
        List<String> userNamesFromUserStore;
        if (isUniqueUserIdEnabled(userManager)) {
            usersFromUserStore = doGetUserListWithID(claim, claimValue, profileName, extractedDomain, userManager);
            userNamesFromUserStore = usersFromUserStore.stream()
                    .map(User::getDomainQualifiedUsername).collect(Collectors.toList());
        } else {
            userNamesFromUserStore = doGetUserList(claim, claimValue, profileName, extractedDomain, userManager);
        }
        if (log.isDebugEnabled()) {
            if (StringUtils.isNotEmpty(extractedDomain)) {
                log.debug("Users from user store: " + extractedDomain + " : " + userNamesFromUserStore);
            } else {
                log.debug("Users from all the user stores: " + userNamesFromUserStore);
            }
        }
        filteredUserList.addAll(userNamesFromUserStore);

        if (StringUtils.isNotEmpty(extractedDomain)) {
            handlePostGetUserList(claim, claimValue, filteredUserList, false);
            if (log.isDebugEnabled()) {
                log.debug("Post listener user list: " + filteredUserList + " for domain: " + extractedDomain);
            }
        }

        Collections.sort(filteredUserList);
        return filteredUserList.toArray(new String[0]);
    }

    private boolean isUniqueUserIdEnabled(UserStoreManager userManager) {

        return userManager instanceof AbstractUserStoreManager && ((AbstractUserStoreManager) userManager).isUniqueUserIdEnabled();
    }

    /**
     * This is to check whether the unique group id is enabled for the userstore.
     *
     * @param userManager UserStoreManager/
     * @return True if unique group id is enabled for the userstore.
     */
    private boolean isUniqueGroupIdEnabled(UserStoreManager userManager) {

        if (!(userManager instanceof AbstractUserStoreManager)) {
            return false;
        }
        return ((AbstractUserStoreManager) userManager).isUniqueGroupIdEnabled();
    }

    private List<String> doGetUserList(String claim, String claimValue, String profileName, String extractedDomain,
                                       UserStoreManager userManager)
            throws UserStoreException {

        String property;

        // If domain is present, then we search within that domain only.
        if (StringUtils.isNotEmpty(extractedDomain)) {

            if (userManager == null) {
                if (log.isDebugEnabled()) {
                    log.debug("No user store manager found for domain: " + extractedDomain);
                }
                return Collections.emptyList();
            }

            if (log.isDebugEnabled()) {
                log.debug("Domain found in claim value. Searching only in the " + extractedDomain + " for possible " +
                        "matches");
            }

            try {
                property = claimManager.getAttributeName(extractedDomain, claim);
            } catch (org.wso2.carbon.user.api.UserStoreException e) {
                handleGetUserListFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST.getMessage(),
                                e.getMessage()), claim, claimValue, profileName);
                throw new UserStoreException(
                        "Error occurred while retrieving attribute name for domain : " + extractedDomain + " and claim "
                                + claim, e);
            }
            if (property == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Could not find matching property for\n" +
                            "claim :" + claim +
                            "domain :" + extractedDomain);
                }
                return Collections.emptyList();
            }

            if (userManager instanceof AbstractUserStoreManager) {
                // Get the user list and return with domain appended.
                try {
                    AbstractUserStoreManager userStoreManager = (AbstractUserStoreManager) userManager;

                    // Validate whether circuit breaker is enabled and open.
                    if (userStoreManager.isCircuitBreakerEnabledAndOpen()) {
                        if (log.isDebugEnabled()) {
                            log.debug("Circuit Breaker is in open state for:  " + extractedDomain);
                        }
                        return Collections.emptyList();
                    }
                    String[] userArray = userStoreManager.getUserListFromProperties(property, claimValue, profileName);
                    if (log.isDebugEnabled()) {
                        log.debug("List of filtered users for: " + extractedDomain + " : " + Arrays.asList(userArray));
                    }
                    return Arrays.asList(UserCoreUtil.addDomainToNames(userArray, extractedDomain));
                } catch (CircuitBreakerOpenException ex) {
                    if (log.isDebugEnabled()) {
                        log.debug("Circuit Breaker is in open state for " + extractedDomain
                                + " domain. Hence ignore the userstore and proceed", ex);
                    }
                    log.error("Error occurred while obtaining user store connection.");
                    return Collections.emptyList();

                } catch (UserStoreException ex) {
                    handleGetUserListFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_USER_LIST.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_USER_LIST.getMessage(),
                                    ex.getMessage()), claim, claimValue, profileName);
                    throw ex;
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("getUserListFromProperties is not supported by this user store: " +
                            userManager.getClass());
                }
                return Collections.emptyList();
            }
        }

        // If domain is not given then search all the user stores.
        if (log.isDebugEnabled()) {
            log.debug("No domain name found in claim value. Searching through all user stores for possible matches");
        }

        List<String> usersFromAllStoresList = new ArrayList<>();
        List<UserStoreManager> userStoreManagers = getUserStoreMangers();

        // Iterate through all of available user store managers.
        for (UserStoreManager userStoreManager : userStoreManagers) {

            // If this is not an instance of Abstract User Store Manger we can ignore the flow since we can't get the
            // domain name.
            if (!(userStoreManager instanceof AbstractUserStoreManager)) {
                continue;
            }

            // For all the user stores append the domain name to the claim and pass it recursively (Including PRIMARY).
            String domainName = ((AbstractUserStoreManager) userStoreManager).getMyDomainName();
            String claimValueWithDomain;
            if (StringUtils.equalsIgnoreCase(domainName, UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME)) {
                claimValueWithDomain = domainName + CarbonConstants.DOMAIN_SEPARATOR + claimValue;
            } else {
                claimValueWithDomain = UserCoreUtil.addDomainToName(claimValue, domainName);
            }

            if (log.isDebugEnabled()) {
                log.debug("Invoking the get user list for domain: " + domainName + " for claim: " + claim +
                        " value: " + claimValueWithDomain);
            }

            // Recursively call the getUserList method appending the domain to claim value.
            List<String> userList = Arrays.asList(userStoreManager.getUserList(claim,
                    claimValueWithDomain, profileName));
            if (log.isDebugEnabled()) {
                log.debug("Secondary user list for domain: " + domainName + " : " + userList);
            }

            usersFromAllStoresList.addAll(userList);
        }

        // Done with all user store processing. Return the user array if not empty.
        return usersFromAllStoresList;
    }

    @Override
    public final List<User> getUserListWithID(String claim, String claimValue, String profileName)
            throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[] { String.class, String.class, String.class };
            Object object = callSecure("getUserListWithID", new Object[] { claim, claimValue, profileName }, argTypes);
            return (List<User>) object;
        }

        if (claim == null) {
            String errorCode = ErrorMessages.ERROR_CODE_INVALID_CLAIM_URI.getCode();
            String errorMessage = String.format(ErrorMessages.ERROR_CODE_INVALID_CLAIM_URI.getMessage(), "");
            handleGetUserListFailureWithID(errorCode, errorMessage, null, claimValue, profileName);
            throw new IllegalArgumentException(ErrorMessages.ERROR_CODE_INVALID_CLAIM_URI.toString());
        }

        if (claimValue == null) {
            handleGetUserListFailureWithID(ErrorMessages.ERROR_CODE_INVALID_CLAIM_VALUE.getCode(),
                    ErrorMessages.ERROR_CODE_INVALID_CLAIM_VALUE.getMessage(), claim, null, profileName);
            throw new IllegalArgumentException(ErrorMessages.ERROR_CODE_INVALID_CLAIM_VALUE.toString());
        }

        if (log.isDebugEnabled()) {
            log.debug("Listing users who having value as " + claimValue + " for the claim " + claim);
        }

        // Extracting the domain from claimValue.
        String extractedDomain = null;
        int index;
        index = claimValue.indexOf(CarbonConstants.DOMAIN_SEPARATOR);
        if (index > 0) {
            String[] names = claimValue.split(CarbonConstants.DOMAIN_SEPARATOR);
            extractedDomain = names[0].trim();
        }

        UserStoreManager userManager = this;
        /*
        * This method("getUserListWithID") can be called for secondary userstore managers.
        * At that time the "extractedDomain" is the name of the "this" usertore manager.
        */
        if (StringUtils.isNotEmpty(extractedDomain) && !StringUtils.equalsIgnoreCase(getMyDomainName(), extractedDomain)) {
            UserStoreManager secondaryUserStoreManager = getSecondaryUserStoreManager(extractedDomain);
            if (secondaryUserStoreManager != null) {
                userManager = secondaryUserStoreManager;
                if (log.isDebugEnabled()) {
                    log.debug("Domain: " + extractedDomain + " is passed with the claim and user store manager is loaded"
                            + " for the given domain name.");
                }
            } else {
                throw new UserStoreClientException("Invalid Domain Name: " + extractedDomain);
            }
        }

        claimValue = UserCoreUtil.removeDomainFromName(claimValue);
        final List<User> filteredUserList = new ArrayList<>();

        if (StringUtils.isNotEmpty(extractedDomain)) {
            try {
                for (UserOperationEventListener listener : UMListenerServiceComponent
                        .getUserOperationEventListeners()) {
                    if (listener instanceof AbstractUserOperationEventListener) {
                        AbstractUserOperationEventListener newListener = (AbstractUserOperationEventListener) listener;
                        if (!newListener.doPreGetUserListWithID(claim, claimValue, filteredUserList, userManager)) {
                            handleGetUserListFailureWithID(
                                    ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST.getCode(),
                                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST.getMessage(),
                                            UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), claim,
                                    claimValue, profileName);
                            break;
                        }
                    }
                }
            } catch (UserStoreException ex) {
                handleGetUserListFailureWithID(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST.getMessage(),
                                ex.getMessage()), claim, claimValue, profileName);
                throw ex;
            }
            if (log.isDebugEnabled()) {
                log.debug("Pre listener user list: " + filteredUserList + " for domain: " + extractedDomain);
            }
        }

        // Iterate through user stores and check for users for this claim.
        List<User> usersFromUserStore;
        if (isUniqueUserIdEnabled(userManager)) {
            usersFromUserStore = doGetUserListWithID(claim, claimValue, profileName, extractedDomain, userManager);
        } else {
            List<String> userNamesFromUserStore = doGetUserList(claim, claimValue, profileName, extractedDomain,
                    userManager);
            usersFromUserStore = userUniqueIDManger
                    .listUsers(userNamesFromUserStore, (AbstractUserStoreManager) userManager);
        }
        if (log.isDebugEnabled()) {
            if (StringUtils.isNotEmpty(extractedDomain)) {
                log.debug("Users from user store: " + extractedDomain + " : " + usersFromUserStore.stream()
                        .map(User::getUsername).collect(Collectors.toList()));
            } else {
                log.debug("Users from all the user stores: " + usersFromUserStore.stream().map(User::getUsername)
                        .collect(Collectors.toList()));
            }
        }
        filteredUserList.addAll(usersFromUserStore);

        if (StringUtils.isNotEmpty(extractedDomain)) {
            handlePostGetUserListWithID(claim, claimValue, filteredUserList, false);
            if (log.isDebugEnabled()) {
                log.debug("Post listener user list: " + filteredUserList.stream().map(User::getUsername)
                        .collect(Collectors.toList()) + " for domain: " + extractedDomain);
            }
        }

        for (org.wso2.carbon.user.core.common.User  user:filteredUserList) {
            if (StringUtils.isBlank(user.getUserStoreDomain())) {
                user.setUserStoreDomain(UserCoreUtil.extractDomainFromName(user.getUsername()));
            }
        }
        return filteredUserList;
    }

    private List<User> doGetUserListWithID(String claim, String claimValue, String profileName, String extractedDomain,
            UserStoreManager userManager) throws UserStoreException {

        String property;
        // If domain is present, then we search within that domain only.
        if (StringUtils.isNotEmpty(extractedDomain)) {

            if (userManager == null) {
                if (log.isDebugEnabled()) {
                    log.debug("No user store manager found for domain: " + extractedDomain);
                }
                return Collections.emptyList();
            }

            if (log.isDebugEnabled()) {
                log.debug("Domain found in claim value. Searching only in the " + extractedDomain + " for possible "
                        + "matches");
            }

            try {
                property = claimManager.getAttributeName(extractedDomain, claim);
            } catch (org.wso2.carbon.user.api.UserStoreException e) {
                handleGetUserListFailureWithID(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST.getMessage(),
                                e.getMessage()), claim, claimValue, profileName);
                throw new UserStoreException(
                        "Error occurred while retrieving attribute name for domain : " + extractedDomain + " and claim "
                                + claim, e);
            }
            if (property == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Could not find matching property for\n" + "claim :" + claim + "domain :"
                            + extractedDomain);
                }
                return Collections.emptyList();
            }

            if (userManager instanceof AbstractUserStoreManager) {
                // Get the user list and return with domain appended.
                try {
                    AbstractUserStoreManager userStoreManager = (AbstractUserStoreManager) userManager;

                    // Validate whether circuit breaker is enabled and open.
                    if (userStoreManager.isCircuitBreakerEnabledAndOpen()) {
                        if (log.isDebugEnabled()) {
                            log.debug("Avoiding user listing as the Circuit Breaker is in open state for domain: "
                                    + extractedDomain);
                        }
                        return Collections.emptyList();
                    }

                    List<String> userIDs = userStoreManager
                            .doGetUserListFromPropertiesWithID(property, claimValue, profileName);
                    if (log.isDebugEnabled()) {
                        log.debug("List of filtered users for: " + extractedDomain + " : " + Arrays.asList(userIDs));
                    }
                    return userStoreManager.getUsersFromIDs(userIDs, null, extractedDomain, profileName);

                } catch (CircuitBreakerOpenException ex) {
                    if (log.isDebugEnabled()) {
                        log.debug("Circuit Breaker is in open state for " + extractedDomain
                                + " domain. Hence ignore the userstore and proceed", ex);
                    }
                    log.error("Error occurred while obtaining user store connection.");
                    return Collections.emptyList();
                } catch (UserStoreException ex) {
                    handleGetUserListFailureWithID(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_USER_LIST.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_USER_LIST.getMessage(),
                                    ex.getMessage()), claim, claimValue, profileName);
                    throw ex;
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("doGetUserListFromPropertiesWithID is not supported by this user store: " + userManager
                            .getClass());
                }
                return Collections.emptyList();
            }
        }

        // If domain is not given then search all the user stores.
        if (log.isDebugEnabled()) {
            log.debug("No domain name found in claim value. Searching through all user stores for possible matches");
        }

        List<User> usersFromAllStoresList = new ArrayList<>();
        List<UserStoreManager> userStoreManagers = getUserStoreMangers();

        // Iterate through all of available user store managers.
        for (UserStoreManager userStoreManager : userStoreManagers) {

            // If this is not an instance of Abstract User Store Manger we can ignore the flow since we can't get the
            // domain name.
            if (!(userStoreManager instanceof AbstractUserStoreManager)) {
                continue;
            }

            // For all the user stores append the domain name to the claim and pass it recursively (Including PRIMARY).
            String domainName = ((AbstractUserStoreManager) userStoreManager).getMyDomainName();
            String claimValueWithDomain;
            if (StringUtils.equalsIgnoreCase(domainName, UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME)) {
                claimValueWithDomain = domainName + CarbonConstants.DOMAIN_SEPARATOR + claimValue;
            } else {
                claimValueWithDomain = UserCoreUtil.addDomainToName(claimValue, domainName);
            }

            if (log.isDebugEnabled()) {
                log.debug("Invoking the get user list for domain: " + domainName + " for claim: " + claim + " value: "
                        + claimValueWithDomain);
            }

            try {
                // Recursively call the getUserList method appending the domain to claim value.
                List<User> userList = ((AbstractUserStoreManager) userStoreManager).getUserListWithID(claim,
                        claimValueWithDomain, profileName);
                if (log.isDebugEnabled()) {
                    log.debug("Secondary user list for domain: " + domainName + " : " + userList);
                }
                usersFromAllStoresList.addAll(userList);
            } catch (UserStoreException e) {
                log.error(String.format("Error occurred while getting the users list for domain: %s Therefore, " +
                        "proceeding remaining domains", domainName), e);
            }
        }

        // Done with all user store processing. Return the user array if not empty.
        return usersFromAllStoresList;
    }

    private List<String> doGetUserList(String claim, String claimValue, String profileName, int limit, int offset,
                                       String extractedDomain, UserStoreManager userManager)
            throws UserStoreException {

        String property;

        // If domain is present, then we search within that domain only.
        if (StringUtils.isNotEmpty(extractedDomain)) {

            if (userManager == null) {
                if (log.isDebugEnabled()) {
                    log.debug("No user store manager found for domain: " + extractedDomain);
                }
                return Collections.emptyList();
            }

            if (log.isDebugEnabled()) {
                log.debug("Domain found in claim value. Searching only in the " + extractedDomain + " for possible " +
                        "matches");
            }

            try {
                property = claimManager.getAttributeName(extractedDomain, claim);
            } catch (org.wso2.carbon.user.api.UserStoreException e) {
                handleGetUserListFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST.getMessage(),
                                e.getMessage()), claim, claimValue, limit, offset, profileName);
                throw new UserStoreException(
                        "Error occurred while retrieving attribute name for domain : " + extractedDomain + " and claim "
                                + claim, e);
            }
            if (property == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Could not find matching property for\n" +
                            "claim :" + claim +
                            "domain :" + extractedDomain);
                }
                return Collections.emptyList();
            }

            if (userManager instanceof AbstractUserStoreManager) {
                // Get the user list and return with domain appended.
                try {
                    AbstractUserStoreManager userStoreManager = (AbstractUserStoreManager) userManager;
                    PaginatedSearchResult result = userStoreManager.getUserListFromProperties(property, claimValue,
                            profileName, limit, offset);
                    if (log.isDebugEnabled()) {
                        log.debug("List of filtered paginated users for: " + extractedDomain + " : " + Arrays.asList
                                (result.getUsers()));
                    }
                    return Arrays.asList(UserCoreUtil.addDomainToNames(result.getUsers(), extractedDomain));
                } catch (UserStoreException ex) {
                    handleGetUserListFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_USER_LIST.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_USER_LIST.getMessage(),
                                    ex.getMessage()), claim, claimValue, limit, offset, profileName);
                    throw ex;
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("getUserListFromProperties is not supported by this user store: " +
                            userManager.getClass());
                }
                return Collections.emptyList();
            }
        }

        // If domain is not given then search all the user stores.
        if (log.isDebugEnabled()) {
            log.debug("No domain name found in claim value. Searching through all user stores for possible matches");
        }

        List<String> usersFromAllStoresList = new ArrayList<>();
        List<UserStoreManager> userStoreManagers = getUserStoreMangers();
        int nonPaginatedUserCount = 0;

        // Iterate through all of available user store managers.
        for (UserStoreManager userStoreManager : userStoreManagers) {

            // If this is not an instance of Abstract User Store Manger we can ignore the flow since we can't get the
            // domain name.
            if (!(userStoreManager instanceof AbstractUserStoreManager)) {
                continue;
            }

            if (limit <= 0) {
                return usersFromAllStoresList;
            }

            // For all the user stores append the domain name to the claim and pass it recursively (Including PRIMARY).
            String domainName = ((AbstractUserStoreManager) userStoreManager).getMyDomainName();

            try {
                property = claimManager.getAttributeName(domainName, claim);
            } catch (org.wso2.carbon.user.api.UserStoreException e) {
                handleGetUserListFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST.getMessage(),
                                e.getMessage()), claim, claimValue, limit, offset, profileName);
                throw new UserStoreException(
                        "Error occurred while retrieving attribute name for domain : " + extractedDomain + " and claim "
                                + claim, e);
            }

            // Recursively call the getUserList method appending the domain to claim value.
            PaginatedSearchResult userList = getUserListFromProperties(property, claimValue, profileName, limit, offset);
            if (log.isDebugEnabled()) {
                log.debug("Secondary user list for domain: " + domainName + " : " + userList);
            }
            limit = limit - userList.getUsers().length;
            nonPaginatedUserCount = userList.getSkippedUserCount();

            if (userList.getUsers().length > 0) {
                offset = 1;
            } else {
                offset = offset - nonPaginatedUserCount;
            }

            usersFromAllStoresList.addAll(Arrays.asList(UserCoreUtil.addDomainToNames(userList.getUsers(), domainName)));
        }

        // Done with all user store processing. Return the user array if not empty.
        return usersFromAllStoresList;
    }

    /**
     * Get the list of user store managers available including primary user store manger.
     * @return List of user store managers available.
     */
    private List<UserStoreManager> getUserStoreMangers() {

        List<UserStoreManager> userStoreManagers = new ArrayList<>();
        UserStoreManager currentUserStoreManager = this;

        // Get the list of user store managers(Including PRIMARY). Later we have to iterate through them.
        while (currentUserStoreManager != null) {
            userStoreManagers.add(currentUserStoreManager);
            currentUserStoreManager = currentUserStoreManager.getSecondaryUserStoreManager();
        }

        return userStoreManagers;
    }

    /**
     * This method calls the relevant listener methods when there is a failure while trying to update credentials.
     *
     * @param errorCode     Relevant error code.
     * @param errorMessage  Error message.
     * @param userID        ID of the user.
     * @param newCredential New credential.
     * @param oldCredential Old credential.
     */
    private void handleUpdateCredentialFailureWithID(String errorCode, String errorMessage, String userID,
            Object newCredential, Object oldCredential) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !((AbstractUserManagementErrorListener) listener)
                    .onUpdateCredentialFailureWithID(errorCode, errorMessage, userID, newCredential, oldCredential,
                            this)) {
                return;
            }
        }
    }

    /**
     * This method calls the relevant listener methods when there is a failure while trying to update credentials.
     *
     * @param errorCode     Relevant error code.
     * @param errorMessage  Error message.
     * @param userName      Name of the user.
     * @param newCredential New credential.
     * @param oldCredential Old credential.
     */
    private void handleUpdateCredentialFailure(String errorCode, String errorMessage, String userName,
            Object newCredential, Object oldCredential) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !listener
                    .onUpdateCredentialFailure(errorCode, errorMessage, userName, newCredential, oldCredential, this)) {
                return;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public final void updateCredential(String userName, Object newCredential, Object oldCredential)
            throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, Object.class, Object.class};
            callSecure("updateCredential", new Object[]{userName, newCredential, oldCredential}, argTypes);
            return;
        }

        UserStore userStore = getUserStore(userName);
        if (userStore.isRecurssive()) {
            userStore.getUserStoreManager().updateCredential(userStore.getDomainFreeName(),
                    newCredential, oldCredential);
            return;
        }

        // #################### Domain Name Free Zone Starts Here ################################

        if (isReadOnly()) {
            handleUpdateCredentialFailure(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode(),
                    ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage(), userName, newCredential, oldCredential);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.toString());
        }

        Secret newCredentialObj;
        Secret oldCredentialObj;
        try {
            newCredentialObj = Secret.getSecret(newCredential);
            oldCredentialObj = Secret.getSecret(oldCredential);
        } catch (UnsupportedSecretTypeException e) {
            handleUpdateCredentialFailure(ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.getCode(),
                    ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.getMessage(), userName, newCredential,
                    oldCredential);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.toString());
        }

        // #################### <Listeners> #####################################################
        try {
            // This user name here is domain-less.
            // We directly authenticate user against the selected UserStoreManager.

            // Property to check whether this user store supports new APIs with unique user id.
            boolean isUniqueUserIdEnabled = isUniqueUserIdEnabledInUserStore(userStore);
            String userID = null;
            if (isUniqueUserIdEnabled) {
                userID = getUserIDFromUserName(userName);
            }

            boolean isAuth;
            if (isUniqueUserIdEnabled) {
                String preferredUserNameProperty = getUsernameProperty();
                isAuth = this.doAuthenticateWithID(preferredUserNameProperty, userName, oldCredentialObj, null)
                        .getAuthenticationStatus() == AuthenticationResult.AuthenticationStatus.SUCCESS;
            } else {
                isAuth = this.doAuthenticate(userName, oldCredentialObj);
            }

            if (!isAuth) {
                handleUpdateCredentialFailure(ErrorMessages.ERROR_CODE_OLD_CREDENTIAL_DOES_NOT_MATCH.getCode(),
                        ErrorMessages.ERROR_CODE_OLD_CREDENTIAL_DOES_NOT_MATCH.getMessage(), userName, newCredential,
                        oldCredential);
                throw new UserStoreException(ErrorMessages.ERROR_CODE_OLD_CREDENTIAL_DOES_NOT_MATCH.toString());
            }

            try {
                for (UserStoreManagerListener listener : UMListenerServiceComponent.getUserStoreManagerListeners()) {
                    if (listener instanceof SecretHandleableListener) {
                        if (!listener.updateCredential(userName, newCredentialObj, oldCredentialObj, this)) {
                            handleUpdateCredentialFailure(
                                    ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL.getCode(),
                                    String.format(
                                            ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL.getMessage(),
                                            UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userName,
                                    newCredential, oldCredential);
                            return;
                        }
                    } else {
                        if (!listener.updateCredential(userName, newCredential, oldCredential, this)) {
                            handleUpdateCredentialFailure(
                                    ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL.getCode(),
                                    String.format(
                                            ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL.getMessage(),
                                            UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userName,
                                    newCredential, oldCredential);
                            return;
                        }
                    }
                }

                for (UserOperationEventListener listener : UMListenerServiceComponent
                        .getUserOperationEventListeners()) {

                    if (listener instanceof SecretHandleableListener) {
                        if (!listener.doPreUpdateCredential(userName, newCredentialObj, oldCredentialObj, this)) {
                            handleUpdateCredentialFailure(
                                    ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL.getCode(),
                                    String.format(
                                            ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL.getMessage(),
                                            UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userName,
                                    newCredential, oldCredential);
                            return;
                        }
                    } else {
                        if (!listener.doPreUpdateCredential(userName, newCredential, oldCredential, this)) {
                            handleUpdateCredentialFailure(
                                    ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL.getCode(),
                                    String.format(
                                            ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL.getMessage(),
                                            UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userName,
                                    newCredential, oldCredential);
                            return;
                        }
                    }
                }
            } catch (UserStoreException e) {
                handleUpdateCredentialFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL.getMessage(),
                                e.getMessage()), userName, newCredential, oldCredential);
                throw e;
            }
            // #################### </Listeners> #####################################################

            if (!checkUserPasswordValid(newCredential)) {
                String errorMsg = realmConfig.getUserStoreProperty(PROPERTY_PASSWORD_ERROR_MSG);

                if (errorMsg != null) {
                    String errorMessage = String
                            .format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL.getMessage(),
                                    errorMsg);
                    String errorCode = ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL.getCode();
                    handleUpdateCredentialFailure(errorCode, errorMessage, userName, newCredential, oldCredential);
                    throw new UserStoreException(errorCode + " - " + errorMessage);
                }

                String errorMessage = String.format(ErrorMessages.ERROR_CODE_INVALID_PASSWORD.getMessage(),
                        realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_JAVA_REG_EX));
                String errorCode = ErrorMessages.ERROR_CODE_INVALID_PASSWORD.getCode();
                handleUpdateCredentialFailure(errorCode, errorMessage, userName, newCredential, oldCredential);
                throw new UserStoreException(errorCode + " - " + errorMessage);
            }

            try {
                if (isUniqueUserIdEnabled) {
                    this.doUpdateCredentialWithID(userID, newCredential, oldCredential);
                } else {
                    this.doUpdateCredential(userName, newCredentialObj, oldCredentialObj);
                }
            } catch (UserStoreException ex) {
                handleUpdateCredentialFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_UPDATING_CREDENTIAL.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_UPDATING_CREDENTIAL.getMessage(),
                                ex.getMessage()), userName, newCredential, oldCredential);
                throw ex;
            }

            // #################### <Listeners> ##################################################
            try {
                for (UserOperationEventListener listener : UMListenerServiceComponent
                        .getUserOperationEventListeners()) {
                    if (listener instanceof SecretHandleableListener) {
                        if (!listener.doPostUpdateCredential(userName, newCredentialObj, this)) {
                            handleUpdateCredentialFailure(
                                    ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_CREDENTIAL.getCode(),
                                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_CREDENTIAL
                                            .getMessage(), "Post update credential tasks failed"), userName,
                                    newCredentialObj, oldCredentialObj);
                            return;
                        }
                    } else {
                        if (!listener.doPostUpdateCredential(userName, newCredential, this)) {
                            handleUpdateCredentialFailure(
                                    ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_CREDENTIAL.getCode(),
                                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_CREDENTIAL
                                            .getMessage(), "Post update credential tasks failed"), userName,
                                    newCredential, oldCredential);
                            return;
                        }
                    }
                }
            } catch (UserStoreException ex) {
                handleUpdateCredentialFailure(
                        ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_CREDENTIAL.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_CREDENTIAL.getMessage(),
                                ex.getMessage()), userName, newCredential, oldCredential);
                throw ex;
            }
            // #################### </Listeners> ##################################################
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            newCredentialObj.clear();
            oldCredentialObj.clear();
            // This value is set in the validation lister if the password gets validated
            // against the configured set of rules.
            UserCoreUtil.removeSkipPasswordPatternValidationThreadLocal();
        }
    }

    /**
     * Handles the failure while there is a failure while update of credentials is done by the admin.
     *
     * @param errorCode     Relevant error code.
     * @param errorMessage  Error message.
     * @param userName      Name of the user.
     * @param newCredential New credential.
     * @throws UserStoreException Exception that could be thrown by the listeners.
     */
    private void handleUpdateCredentialByAdminFailure(String errorCode, String errorMessage, String userName,
            Object newCredential) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !listener
                    .onUpdateCredentialByAdminFailure(errorCode, errorMessage, userName, newCredential, this)) {
                return;
            }
        }
    }

    /**
     * Handles the failure while there is a failure while update of credentials is done by the admin.
     *
     * @param errorCode     Relevant error code.
     * @param errorMessage  Error message.
     * @param userID        ID of the user.
     * @param newCredential New credential.
     * @throws UserStoreException Exception that could be thrown by the listeners.
     */
    private void handleUpdateCredentialByAdminFailureWithID(String errorCode, String errorMessage, String userID,
            Object newCredential) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !((AbstractUserManagementErrorListener) listener)
                    .onUpdateCredentialByAdminFailureWithID(errorCode, errorMessage, userID, newCredential, this)) {
                return;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public final void updateCredentialByAdmin(String userName, Object newCredential)
            throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, Object.class};
            callSecure("updateCredentialByAdmin", new Object[]{userName, newCredential}, argTypes);
            return;
        }

        UserStore userStore = getUserStore(userName);
        if (userStore.isRecurssive()) {
            userStore.getUserStoreManager().updateCredentialByAdmin(userStore.getDomainFreeName(),
                    newCredential);
            return;
        }

        // #################### Domain Name Free Zone Starts Here ################################

        if (isReadOnly()) {
            handleUpdateCredentialByAdminFailure(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode(),
                    ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage(), userName, newCredential);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.toString());
        }

        Secret newCredentialObj;
        try {
            newCredentialObj = Secret.getSecret(newCredential);
        } catch (UnsupportedSecretTypeException e) {
            handleUpdateCredentialByAdminFailure(ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.getCode(),
                    ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.getMessage() + " " + e.getMessage(), userName,
                    newCredential);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.toString(), e);
        }

        try {
            try {
                // #################### <Listeners> #####################################################
                for (UserStoreManagerListener listener : UMListenerServiceComponent.getUserStoreManagerListeners()) {
                    Object credentialArgument;
                    if (listener instanceof SecretHandleableListener) {
                        credentialArgument = newCredentialObj;
                    } else {
                        credentialArgument = newCredential;
                    }

                    if (!listener.updateCredentialByAdmin(userName, credentialArgument, this)) {
                        handleUpdateCredentialByAdminFailure(
                                ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL_BY_ADMIN.getCode(),
                                String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL_BY_ADMIN
                                        .getMessage(), UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE),
                                userName, credentialArgument);
                        return;
                    }
                }

                // using string buffers to allow the password to be changed by listener
                for (UserOperationEventListener listener : UMListenerServiceComponent
                        .getUserOperationEventListeners()) {

                    if (listener instanceof SecretHandleableListener) {
                        if (!listener.doPreUpdateCredentialByAdmin(userName, newCredentialObj, this)) {
                            handleUpdateCredentialByAdminFailure(
                                    ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL_BY_ADMIN.getCode(),
                                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL_BY_ADMIN
                                            .getMessage(), UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE),
                                    userName, newCredentialObj);
                            return;
                        }
                    } else {
                        // using string buffers to allow the password to be changed by listener
                        StringBuffer credBuff = null;
                        if (newCredential == null) { // a default password will be set
                            credBuff = new StringBuffer();
                        } else if (newCredential instanceof String) {
                            credBuff = new StringBuffer((String) newCredential);
                        }

                        if (credBuff != null) {
                            if (!listener.doPreUpdateCredentialByAdmin(userName, credBuff, this)) {
                                handleUpdateCredentialByAdminFailure(
                                        ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL_BY_ADMIN.getCode(),
                                        String.format(
                                                ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL_BY_ADMIN
                                                        .getMessage(),
                                                UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userName,
                                        credBuff);
                                return;
                            }
                            // reading the modified value
                            newCredential = credBuff.toString();
                            newCredentialObj.clear();
                            try {
                                newCredentialObj = Secret.getSecret(newCredential);
                            } catch (UnsupportedSecretTypeException e) {
                                handleUpdateCredentialByAdminFailure(
                                        ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.getCode(),
                                        ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.getMessage() + " " + e
                                                .getMessage(), userName, newCredential);
                                throw new UserStoreException(
                                        ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.toString(), e);
                            }
                        }
                    }
                }
            } catch (UserStoreException ex) {
                handleUpdateCredentialByAdminFailure(
                        ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL_BY_ADMIN.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL_BY_ADMIN.getMessage(),
                                ex.getMessage()), userName, newCredential);
                throw ex;
            }
            // #################### </Listeners> #####################################################

            if (!checkUserPasswordValid(newCredential)) {
                String errorMsg = realmConfig.getUserStoreProperty(PROPERTY_PASSWORD_ERROR_MSG);

                if (errorMsg != null) {
                    String errorCode = ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL_BY_ADMIN.getCode();
                    String errorMessage = String
                            .format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL_BY_ADMIN.getMessage(),
                                    errorMsg);
                    handleUpdateCredentialByAdminFailure(errorCode, errorMessage, userName, newCredential);
                    throw new UserStoreException(errorCode + " - " + errorMessage);
                }

                String errorCode = ErrorMessages.ERROR_CODE_INVALID_PASSWORD.getCode();
                String errorMessage = String.format(ErrorMessages.ERROR_CODE_INVALID_PASSWORD.getMessage(),
                        realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_JAVA_REG_EX));
                handleUpdateCredentialByAdminFailure(errorCode, errorMessage, userName, newCredential);
                throw new UserStoreException(errorCode + " - " + errorMessage);
            }

            // Property to check whether this user store supports new APIs with unique user id.
            boolean isUniqueUserIdEnabled = isUniqueUserIdEnabledInUserStore(userStore);
            String userID = null;
            if (isUniqueUserIdEnabled) {
                userID = getUserIDFromUserName(userName);
            }

            boolean isUserExists;
            if (isUniqueUserIdEnabled) {
                isUserExists = userID != null;
            } else {
                isUserExists = doCheckExistingUser(userStore.getDomainFreeName());
            }

            if (!isUserExists) {
                String errorMessage = String.format(ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getMessage(), userName,
                        realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME));
                String errorCode = ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getCode();
                handleUpdateCredentialByAdminFailure(errorCode, errorMessage, userName, newCredential);
                throw new UserStoreException(errorCode + "-" + errorMessage);
            }

            try {
                if (isUniqueUserIdEnabled) {
                    doUpdateCredentialByAdminWithID(userID, newCredentialObj);
                } else {
                    doUpdateCredentialByAdmin(userName, newCredentialObj);
                }
            } catch (UserStoreException ex) {
                handleUpdateCredentialByAdminFailure(
                        ErrorMessages.ERROR_CODE_ERROR_WHILE_UPDATING_CREDENTIAL_BY_ADMIN.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_UPDATING_CREDENTIAL_BY_ADMIN.getMessage(),
                                ex.getMessage()), userName, newCredentialObj);
                throw ex;
            }

            // #################### <Listeners> #####################################################
            try {
                for (UserOperationEventListener listener : UMListenerServiceComponent
                        .getUserOperationEventListeners()) {
                    Object credentialArgument;
                    if (listener instanceof SecretHandleableListener) {
                        credentialArgument = newCredentialObj;
                    } else {
                        credentialArgument = newCredential;
                    }

                    if (!listener.doPostUpdateCredentialByAdmin(userName, credentialArgument, this)) {
                        handleUpdateCredentialByAdminFailure(
                                ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_CREDENTIAL_BY_ADMIN.getCode(),
                                String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_CREDENTIAL_BY_ADMIN
                                        .getMessage(), UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE),
                                userName, newCredential);
                        return;
                    }
                }
            } catch (UserStoreException ex) {
                handleUpdateCredentialByAdminFailure(
                        ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_CREDENTIAL_BY_ADMIN.getCode(), String.format(
                                ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_CREDENTIAL_BY_ADMIN.getMessage(),
                                ex.getMessage()), userName, newCredential);
                throw ex;
            }
        } finally {
            newCredentialObj.clear();
            // This value is set in the validation lister if the password gets validated
            // against the configured set of rules.
            UserCoreUtil.removeSkipPasswordPatternValidationThreadLocal();
        }
        // #################### </Listeners> #####################################################

    }

    /**
     * Get the attribute for the provided claim uri and identifier.
     *
     * @param claimURI
     * @param identifier user name or role.
     * @param domainName TODO
     * @return claim attribute value. NULL if attribute is not defined for the
     * claim uri
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
        if (domainName != null) {
            attributeName = claimManager.getAttributeName(domainName, claimURI);
        }
        if (attributeName == null || attributeName.isEmpty()) {
            attributeName = claimManager.getAttributeName(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME, claimURI);
        }

        if (attributeName == null) {
            if (UserCoreConstants.PROFILE_CONFIGURATION.equals(claimURI)) {
                attributeName = claimURI;
            } else if (DISAPLAY_NAME_CLAIM.equals(claimURI)) {
                attributeName = this.realmConfig.getUserStoreProperty(LDAPConstants.DISPLAY_NAME_ATTRIBUTE);
            } else {
                throw new UserStoreClientException("Mapped attribute cannot be found for claim : " + claimURI +
                        " in user store : " + getMyDomainName());
            }
        }

        return attributeName;
    }

    /**
     * This method handles the follow up actions when there is a failure while deleting a user.
     *
     * @param errorCode    Relevant error code.
     * @param errorMessage Relevant error message.
     * @param userName     Name of the user.
     * @throws UserStoreException User Store Exception that could be thrown while doing follow-up actions.
     */
    private void handleDeleteUserFailure(String errorCode, String errorMessage, String userName)
            throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !listener.onDeleteUserFailure(errorCode, errorMessage, userName, this)) {
                return;
            }
        }
    }

    /**
     * This method handles the follow up actions when there is a failure while deleting a user.
     *
     * @param errorCode    Relevant error code.
     * @param errorMessage Relevant error message.
     * @param userID       ID of the user.
     * @throws UserStoreException User Store Exception that could be thrown while doing follow-up actions.
     */
    private void handleDeleteUserFailureWithID(String errorCode, String errorMessage, String userID)
            throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !((AbstractUserManagementErrorListener) listener)
                    .onDeleteUserFailureWithID(errorCode, errorMessage, userID, this)) {
                return;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public final void deleteUser(String userName) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class};
            callSecure("deleteUser", new Object[]{userName}, argTypes);
            return;
        }

        String loggedInUser = CarbonContext.getThreadLocalCarbonContext().getUsername();
        if (loggedInUser != null) {
            loggedInUser = UserCoreUtil.addDomainToName(loggedInUser, UserCoreUtil.getDomainFromThreadLocal());
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

        if (loggedInUser != null && loggedInUser.equals(deletingUser)) {
            log.debug("User " + loggedInUser + " tried to delete him/her self");
            handleDeleteUserFailure(ErrorMessages.ERROR_CODE_DELETE_LOGGED_IN_USER.getCode(),
                    ErrorMessages.ERROR_CODE_DELETE_LOGGED_IN_USER.getMessage(), userName);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_DELETE_LOGGED_IN_USER.toString());
        }

        UserStore userStore = getUserStore(userName);
        if (userStore.isRecurssive()) {
            userStore.getUserStoreManager().deleteUser(userStore.getDomainFreeName());
            return;
        }

        // #################### Domain Name Free Zone Starts Here ################################

        if (UserCoreUtil.isPrimaryAdminUser(userName, realmConfig)) {
            handleDeleteUserFailure(ErrorMessages.ERROR_CODE_DELETE_ADMIN_USER.getCode(),
                    ErrorMessages.ERROR_CODE_DELETE_ADMIN_USER.getMessage(), userName);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_DELETE_ADMIN_USER.toString());
        }

        if (UserCoreUtil.isRegistryAnnonymousUser(userName)) {
            handleDeleteUserFailure(ErrorMessages.ERROR_CODE_DELETE_ANONYMOUS_USER.getCode(),
                    ErrorMessages.ERROR_CODE_DELETE_ANONYMOUS_USER.getMessage(), userName);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_DELETE_ANONYMOUS_USER.toString());
        }

        if (isReadOnly()) {
            handleDeleteUserFailure(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode(),
                    ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage(), userName);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.toString());
        }

        // #################### <Listeners> #####################################################
        try {
            for (UserStoreManagerListener listener : UMListenerServiceComponent.getUserStoreManagerListeners()) {
                if (!listener.deleteUser(userName, this)) {
                    handleDeleteUserFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_USER.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_USER.getMessage(),
                                    UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userName);
                    return;
                }
            }
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!listener.doPreDeleteUser(userName, this)) {
                    handleDeleteUserFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_USER.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_USER.getMessage(),
                                    UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userName);

                    return;
                }
            }
        } catch (UserStoreException e) {
            handleDeleteUserFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_USER.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_USER.getMessage(), e.getMessage()),
                    userName);
            throw e;
        }
        // #################### </Listeners> #####################################################

        // Property to check whether this user store supports new APIs with unique user id.
        boolean isUniqueUserIdEnabled = isUniqueUserIdEnabledInUserStore(userStore);
        String userID = null;
        boolean isUserExists;
        if (isUniqueUserIdEnabled) {
            userID = getUserIDFromUserName(userName);
            isUserExists = userID != null;
        } else {
            isUserExists = doCheckExistingUser(userName);
        }

        if (!isUserExists) {
            String errorMessage = String.format(ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getMessage(), userName,
                    realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME));
            String errorCode = ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getCode();
            handleDeleteUserFailure(errorCode, errorMessage, userName);
            throw new UserStoreException(errorCode + " - " + errorMessage);
        }

        // Remove users from internal role mapping
        try {

            clearUserIDResolverCache(userID, userName, userStore);
            if (isUniqueUserIdEnabled) {
                hybridRoleManager.deleteUser(UserCoreUtil.addDomainToName(userName, getMyDomainName()));
                doDeleteUserWithID(userID);
            } else {
                hybridRoleManager.deleteUser(UserCoreUtil.addDomainToName(userName, getMyDomainName()));
                doDeleteUser(userName);
            }
        } catch (UserStoreException e) {
            handleDeleteUserFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETING_USER.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETING_USER.getMessage(), e.getMessage()),
                    userName);
            throw e;
        }

        // Needs to clear roles cache upon deletion of a user
        clearUserRolesCache(userName);

        // #################### <Listeners> #####################################################
        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!listener.doPostDeleteUser(userName, this)) {
                    handleDeleteUserFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_USER.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_USER.getMessage(),
                                    UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), userName);
                    return;
                }
            }
        } catch (UserStoreException ex) {
            handleDeleteUserFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_USER.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_USER.getMessage(), ex.getMessage()),
                    userName);
            throw ex;
        }
        // #################### </Listeners> #####################################################

    }

    /**
     * This is method is to call the relevant listeners when there is a failure while setting user claim value.
     *
     * @param errorCode    Error code.
     * @param errorMessage Error message.
     * @param userName     Name of the user.
     * @param claimURI     Claim URI.
     * @param claimValue   Claim Value.
     * @param profileName  Name of the profile.
     * @throws UserStoreException UserStore Exception that would be thrown within the listeners.
     */
    private void handleSetUserClaimValueFailure(String errorCode, String errorMessage, String userName, String claimURI,
            String claimValue, String profileName) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !listener
                    .onSetUserClaimValueFailure(errorCode, errorMessage, userName, claimURI, claimValue, profileName,
                            this)) {
                return;
            }
        }
    }

    /**
     * This is method is to call the relevant listeners when there is a failure while setting user claim value.
     *
     * @param errorCode    Error code.
     * @param errorMessage Error message.
     * @param userID       ID of the user.
     * @param claimURI     Claim URI.
     * @param claimValue   Claim Value.
     * @param profileName  Name of the profile.
     * @throws UserStoreException UserStore Exception that would be thrown within the listeners.
     */
    private void handleSetUserClaimValueFailureWithID(String errorCode, String errorMessage, String userID,
            String claimURI, String claimValue, String profileName) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !((AbstractUserManagementErrorListener) listener)
                    .onSetUserClaimValueFailureWithID(errorCode, errorMessage, userID, claimURI, claimValue,
                            profileName, this)) {
                return;
            }
        }
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

        // #################### Domain Name Free Zone Starts Here ################################

        // Property to check whether this user store supports new APIs with unique user id.
        boolean isUniqueUserIdEnabled = isUniqueUserIdEnabledInUserStore(userStore);
        String userID = null;
        if (isUniqueUserIdEnabled) {
            userID = getUserIDFromUserName(userName);
        }

        boolean isUserExists;
        if (isUniqueUserIdEnabled) {
            isUserExists = userID != null;
        } else {
            isUserExists = doCheckExistingUser(userName);
        }

        if (!isUserExists) {
            String errorMessage = String.format(ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getMessage(), userName,
                    realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME));
            String errorCode = ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getCode();
            handleSetUserClaimValueFailure(errorCode, errorMessage, userName, claimURI, claimValue, profileName);
            throw new UserStoreException(errorCode + " - " + errorMessage);
        }

        // #################### <Listeners> #####################################################
        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!listener.doPreSetUserClaimValue(userName, claimURI, claimValue, profileName, this)) {
                    handleSetUserClaimValueFailure(
                            ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_SET_USER_CLAIM_VALUE.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_SET_USER_CLAIM_VALUE.getMessage(),
                                    UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userName, claimURI,
                            claimValue, profileName);
                    return;
                }
            }
        } catch (UserStoreException e) {
            handleSetUserClaimValueFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_SET_USER_CLAIM_VALUE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_SET_USER_CLAIM_VALUE.getMessage(),
                            e.getMessage()), userName, claimURI, claimValue, profileName);
            throw e;
        }
        // #################### </Listeners> #####################################################

        // Check userstore is readonly or not.
        if (isReadOnly()) {
            handleSetUserClaimValueFailure(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode(),
                    ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage(), userName, claimURI, claimValue,
                    profileName);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.toString());
        }

        try {
            if (isUniqueUserIdEnabled) {
                doSetUserClaimValueWithID(userID, claimURI, claimValue, profileName);
            } else {
                doSetUserClaimValue(userName, claimURI, claimValue, profileName);
            }
        } catch (UserStoreException e) {
            handleSetUserClaimValueFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_SETTING_USER_CLAIM_VALUE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_SETTING_USER_CLAIM_VALUE.getMessage(),
                            e.getMessage()), userName, claimURI, claimValue, profileName);
            throw e;
        }

        // #################### <Listeners> #####################################################
        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!listener.doPostSetUserClaimValue(userName, this)) {
                    handleSetUserClaimValueFailure(
                            ErrorMessages.ERROR_CODE_ERROR_DURING_POST_SET_USER_CLAIM_VALUE.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_SET_USER_CLAIM_VALUE.getMessage(),
                                    UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), userName, claimURI,
                            claimValue, profileName);
                    return;
                }
            }
        } catch (UserStoreException e) {
            handleSetUserClaimValueFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_SET_USER_CLAIM_VALUE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_SET_USER_CLAIM_VALUE.getMessage(),
                            e.getMessage()), userName, claimURI, claimValue, profileName);
            throw e;
        }
        // #################### </Listeners> #####################################################

    }

    /**
     * This method is responsible for calling relevant methods when there is a failure while setting user claim values.
     *
     * @param errorCode    Error code.
     * @param errorMessage Error message.
     * @param userName     Name of the user.
     * @param claims       Relevant claims.
     * @param profileName  Name of the profile.
     * @throws UserStoreException Exception that will be thrown by relevant listeners.
     */
    private void handleSetUserClaimValuesFailure(String errorCode, String errorMessage, String userName,
            Map<String, String> claims, String profileName) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !listener
                    .onSetUserClaimValuesFailure(errorCode, errorMessage, userName, claims, profileName, this)) {
                return;
            }
        }
    }

    /**
     * This method is responsible for calling relevant methods when there is a failure while setting user claim values.
     *
     * @param errorCode    Error code.
     * @param errorMessage Error message.
     * @param userName     Name of the user.
     * @param claims       Relevant claims.
     * @param profileName  Name of the profile.
     * @throws UserStoreException Exception that will be thrown by relevant listeners.
     */
    private void handleSetUserClaimValuesFailureWithID(String errorCode, String errorMessage, String userName,
            Map<String, String> claims, String profileName) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !((AbstractUserManagementErrorListener) listener)
                    .onSetUserClaimValuesFailureWithID(errorCode, errorMessage, userName, claims, profileName, this)) {
                return;
            }
        }
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

        // #################### Domain Name Free Zone Starts Here ################################

        // Property to check whether this user store supports new APIs with unique user id.
        boolean isUniqueUserIdEnabled = isUniqueUserIdEnabledInUserStore(userStore);
        String userID = null;
        if (isUniqueUserIdEnabled) {
            userID = getUserIDFromUserName(userName);
        }

        boolean isUserExists;
        if (isUniqueUserIdEnabled) {
            isUserExists = userID != null;
        } else {
            isUserExists = doCheckExistingUser(userName);
        }

        if (!isUserExists) {
            String errorMessage = String.format(ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getMessage(), userName,
                    realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME));
            String errorCode = ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getCode();
            handleSetUserClaimValuesFailure(errorCode, errorMessage, userName, claims, profileName);
            throw new UserStoreException(errorCode + " - " + errorMessage);
        }
        if (claims == null) {
            claims = new HashMap<>();
        }
        // #################### <Listeners> #####################################################
        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!listener.doPreSetUserClaimValues(userName, claims, profileName, this)) {
                    handleSetUserClaimValuesFailure(
                            ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_SET_USER_CLAIM_VALUES.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_SET_USER_CLAIM_VALUES.getMessage(),
                                    UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userName, claims,
                            profileName);
                    return;
                }
            }
        } catch (UserStoreException e) {
            handleSetUserClaimValuesFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_SET_USER_CLAIM_VALUES.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_SET_USER_CLAIM_VALUES.getMessage(),
                            e.getMessage()), userName, claims, profileName);
            throw e;
        }
        // #################### </Listeners> #####################################################

        //If user store is readonly this method should not get invoked with non empty claim set.

        if (isReadOnly() && !claims.isEmpty()) {
            handleSetUserClaimValuesFailure(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode(),
                    ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage(), userName, claims, profileName);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage(),
                    ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode());
        }

        // set claim values if user store is not read only.

        try {
            if (!isReadOnly()) {
                if (isUniqueUserIdEnabled) {
                    doSetUserClaimValuesWithID(userID, claims, profileName);
                } else {
                    doSetUserClaimValues(userName, claims, profileName);
                }
            }
        } catch (UserStoreException e) {
            handleSetUserClaimValuesFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_SETTING_USER_CLAIM_VALUES.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_SETTING_USER_CLAIM_VALUES.getMessage(),
                            e.getMessage()), userName, claims, profileName);
            throw e;
        }

        // #################### <Listeners> #####################################################
        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!listener.doPostSetUserClaimValues(userName, claims, profileName, this)) {
                    handleSetUserClaimValuesFailure(
                            ErrorMessages.ERROR_CODE_ERROR_DURING_POST_SET_USER_CLAIM_VALUES.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_SET_USER_CLAIM_VALUES.getMessage(),
                                    UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), userName, claims,
                            profileName);
                    return;
                }
            }
        } catch (UserStoreException e) {
            handleSetUserClaimValuesFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_SET_USER_CLAIM_VALUES.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_SET_USER_CLAIM_VALUES.getMessage(),
                            e.getMessage()), userName, claims, profileName);
            throw e;
        }
        // #################### </Listeners> #####################################################

    }

    /**
     * To handle the listener events when there is a failure while trying to delete the user  claim value.
     *
     * @param errorCode    Relevant error code.
     * @param errorMessage error message
     * @param userName     Name of the user.
     * @param claimURI     Claim URI
     * @param profileName  Name of the profile.
     * @throws UserStoreException Exception that will thrown from listeners.
     */
    private void handleDeleteUserClaimValueFailure(String errorCode, String errorMessage, String userName,
            String claimURI, String profileName) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !listener
                    .onDeleteUserClaimValueFailure(errorCode, errorMessage, userName, claimURI, profileName, this)) {
                return;
            }
        }
    }

    /**
     * To handle the listener events when there is a failure while trying to delete the user  claim value.
     *
     * @param errorCode    Relevant error code.
     * @param errorMessage error message.
     * @param userID       ID of the user.
     * @param claimURI     Claim URI.
     * @param profileName  Name of the profile.
     * @throws UserStoreException Exception that will thrown from listeners.
     */
    private void handleDeleteUserClaimValueFailureWithID(String errorCode, String errorMessage, String userID,
            String claimURI, String profileName) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !((AbstractUserManagementErrorListener) listener)
                    .onDeleteUserClaimValueFailureWithID(errorCode, errorMessage, userID, claimURI, profileName,
                            this)) {
                return;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public final void deleteUserClaimValue(String userName, String claimURI, String profileName)
            throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, String.class, String.class};
            callSecure("deleteUserClaimValue", new Object[]{userName, claimURI, profileName}, argTypes);
            return;
        }

        UserStore userStore = getUserStore(userName);
        if (userStore.isRecurssive()) {
            userStore.getUserStoreManager().deleteUserClaimValue(userStore.getDomainFreeName(),
                    claimURI, profileName);
            return;
        }

        if (isReadOnly()) {
            handleDeleteUserClaimValueFailure(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode(),
                    ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage(), userName, claimURI, profileName);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.toString());
        }

        // Property to check whether this user store supports new APIs with unique user id.
        boolean isUniqueUserIdEnabled = isUniqueUserIdEnabledInUserStore(userStore);
        String userID = null;
        boolean isUserExists;
        if (isUniqueUserIdEnabled) {
            userID = getUserIDFromUserName(userName);
            isUserExists = userID != null;
        } else {
            isUserExists = doCheckExistingUser(userName);
        }

        if (!isUserExists) {
            String errorMessage = String.format(ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getMessage(), userName,
                    realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME));
            String errorCode = ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getCode();
            handleDeleteUserClaimValueFailure(errorCode, errorMessage, userName, claimURI, profileName);
            throw new UserStoreException(errorCode + " - " + errorMessage);
        }

        // #################### <Listeners> #####################################################
        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!listener.doPreDeleteUserClaimValue(userName, claimURI, profileName, this)) {
                    handleDeleteUserClaimValueFailure(
                            ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_USER_CLAIM_VALUE.getCode(), String.format(
                                    ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_USER_CLAIM_VALUE.getMessage(),
                                    UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userName, claimURI,
                            profileName);
                    return;
                }
            }
            // #################### </Listeners> #####################################################
        } catch (UserStoreException ex) {
            handleDeleteUserClaimValueFailure(
                    ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_USER_CLAIM_VALUE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_USER_CLAIM_VALUE.getMessage(),
                            ex.getMessage()), userName, claimURI, profileName);
            throw ex;
        }

        try {
            if (isUniqueUserIdEnabled) {
                doDeleteUserClaimValueWithID(userID, claimURI, profileName);
            } else {
                doDeleteUserClaimValue(userName, claimURI, profileName);
            }
        } catch (UserStoreException ex) {
            handleDeleteUserClaimValueFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETING_USER_CLAIM_VALUE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETING_USER_CLAIM_VALUE.getMessage(),
                            ex.getMessage()), userName, claimURI, profileName);
            throw ex;
        }

        // #################### <Listeners> #####################################################
        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!listener.doPostDeleteUserClaimValue(userName, this)) {
                    handleDeleteUserClaimValueFailure(
                            ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_USER_CLAIM_VALUE.getCode(), String.format(
                                    ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_USER_CLAIM_VALUE.getMessage(),
                                    UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), userName, claimURI,
                            profileName);
                    return;
                }
            }
        } catch (UserStoreException ex) {
            handleDeleteUserClaimValueFailure(
                    ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_USER_CLAIM_VALUE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_USER_CLAIM_VALUE.getMessage(),
                            ex.getMessage()), userName, claimURI, profileName);
            throw ex;
        }
        // #################### </Listeners> #####################################################
    }

    /**
     * This method handles a failure when trying to delete user claim values.
     *
     * @param errorCode    Error code.
     * @param errorMessage Error message
     * @param userName     Name of the user.
     * @param claims       Claims
     * @param profileName  Name of the profile.
     * @throws UserStoreException User Store Exception that will be thrown from the relevant listeners.
     */
    private void handleDeleteUserClaimValuesFailure(String errorCode, String errorMessage, String userName,
            String[] claims, String profileName) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !listener
                    .onDeleteUserClaimValuesFailure(errorCode, errorMessage, userName, claims, profileName, this)) {
                return;
            }
        }
    }

    /**
     * This method handles a failure when trying to delete user claim values.
     *
     * @param errorCode    Error code.
     * @param errorMessage Error message
     * @param userID       ID of the user.
     * @param claims       Claims.
     * @param profileName  Name of the profile.
     * @throws UserStoreException User Store Exception that will be thrown from the relevant listeners.
     */
    private void handleDeleteUserClaimValuesFailureWithID(String errorCode, String errorMessage, String userID,
            String[] claims, String profileName) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !listener
                    .onDeleteUserClaimValuesFailure(errorCode, errorMessage, userID, claims, profileName, this)) {
                return;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public final void deleteUserClaimValues(String userName, String[] claims, String profileName)
            throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[] { String.class, String[].class, String.class };
            callSecure("deleteUserClaimValues", new Object[] { userName, claims, profileName }, argTypes);
            return;
        }

        UserStore userStore = getUserStore(userName);
        if (userStore.isRecurssive()) {
            userStore.getUserStoreManager().deleteUserClaimValues(userStore.getDomainFreeName(), claims, profileName);
            return;
        }

        if (isReadOnly()) {
            handleDeleteUserClaimValuesFailure(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode(),
                    ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage(), userName, claims, profileName);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.toString());
        }

        // Property to check whether this user store supports new APIs with unique user id.
        boolean isUniqueUserIdEnabled = isUniqueUserIdEnabledInUserStore(userStore);
        String userID = null;
        boolean isUserExists;
        if (isUniqueUserIdEnabled) {
            userID = getUserIDFromUserName(userName);
            isUserExists = userID != null;
        } else {
            isUserExists = doCheckExistingUser(userName);
        }

        if (!isUserExists) {
            String errorMessage = String.format(ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getMessage(), userName,
                    realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME));
            String errorCode = ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getCode();
            handleDeleteUserClaimValuesFailure(errorCode, errorMessage, userName, claims, profileName);
            throw new UserStoreException(errorCode + " - " + errorMessage);
        }

        if (claims == null) {
            claims = new String[0];
        }
        // #################### <Listeners> #####################################################
        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!listener.doPreDeleteUserClaimValues(userName, claims, profileName, this)) {
                    handleDeleteUserClaimValuesFailure(
                            ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_USER_CLAIM_VALUES.getCode(), String.format(
                                    ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_USER_CLAIM_VALUES.getMessage(),
                                    UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userName, claims,
                            profileName);
                    return;
                }
            }
            // #################### </Listeners> #####################################################
        } catch (UserStoreException ex) {
            handleDeleteUserClaimValuesFailure(
                    ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_USER_CLAIM_VALUES.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_USER_CLAIM_VALUES.getMessage(),
                            ex.getMessage()), userName, claims, profileName);
            throw ex;
        }

        try {
            if (isUniqueUserIdEnabled) {
                doDeleteUserClaimValuesWithID(userID, claims, profileName);
            } else {
                doDeleteUserClaimValues(userName, claims, profileName);
            }
        } catch (UserStoreException ex) {
            handleDeleteUserClaimValuesFailure(
                    ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETING_USER_CLAIM_VALUES.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETING_USER_CLAIM_VALUES.getMessage(),
                            ex.getMessage()), userName, claims, profileName);
            throw ex;
        }
        // #################### <Listeners> #####################################################
        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!listener.doPostDeleteUserClaimValues(userName, this)) {
                    handleDeleteUserClaimValuesFailure(
                            ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_USER_CLAIM_VALUES.getCode(),
                            String.format(
                                    ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_USER_CLAIM_VALUES.getMessage(),
                                    UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), userName, claims,
                            profileName);
                    return;
                }
            }
        } catch (UserStoreException ex) {
            handleDeleteUserClaimValuesFailure(
                    ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_USER_CLAIM_VALUES.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_USER_CLAIM_VALUES.getMessage(),
                            ex.getMessage()), userName, claims, profileName);
            throw ex;
        }
        // #################### </Listeners> #####################################################
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
    public final void addUser(String userName, Object credential, String[] roleList,
                              Map<String, String> claims, String profileName, boolean requirePasswordChange)
            throws UserStoreException {

        // We have to make sure this call is going through the Java Security Manager.
        if (!isSecureCall.get()) {
            Class[] argTypes = new Class[]{String.class, Object.class, String[].class, Map.class, String.class,
                    boolean.class};
            callSecure("addUser", new Object[]{userName, credential, roleList, claims, profileName,
                    requirePasswordChange}, argTypes);
            return;
        }

        User user = null;
        // If we don't have a username, we cannot proceed.
        if (StringUtils.isEmpty(userName)) {
            String regEx = realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig
                    .PROPERTY_USER_NAME_JAVA_REG_EX);
            // Inorder to support both UsernameJavaRegEx and UserNameJavaRegEx.
            if (StringUtils.isEmpty(regEx) || StringUtils.isEmpty(regEx.trim())) {
                regEx = realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USER_NAME_JAVA_REG);
            }
            String message = String.format(ErrorMessages.ERROR_CODE_INVALID_USER_NAME.getMessage(), null, regEx);
            String errorCode = ErrorMessages.ERROR_CODE_INVALID_USER_NAME.getCode();
            handleAddUserFailure(errorCode, message, null, credential, roleList, claims, profileName);
            throw new UserStoreException(errorCode + " - " + message);
        }

        String userNameWithoutDomain = UserCoreUtil.removeDomainFromName(userName);
        // If the username claims presents, the value should be equal to the username attribute.
        if (claims != null && claims.containsKey(USERNAME_CLAIM_URI) &&
                !claims.get(USERNAME_CLAIM_URI).equals(userNameWithoutDomain)) {
            // If not we cannot continue.
            throw new UserStoreException("Username and the username claim value should be same.");
        }

        // Get the user store that this user should be added from the domain name that is appended to the username.
        UserStore userStore = getUserStore(userName);
        boolean isUniqueUserIdEnabled = isUniqueUserIdEnabledInUserStore(userStore);

        if (userStore.isRecurssive()) {
            userStore.getUserStoreManager()
                    .addUser(userStore.getDomainFreeName(), credential, roleList, claims, profileName,
                            requirePasswordChange);
            return;
        }

        // Convert the credential (Password) to a Secret.
        Secret credentialObj;
        try {
            credentialObj = Secret.getSecret(credential);
        } catch (UnsupportedSecretTypeException e) {
            handleAddUserFailure(ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.getCode(),
                    ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.getMessage(), userName, credential, roleList,
                    claims, profileName);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.toString(), e);
        }

        try {
            if (userStore.isSystemStore()) {
                systemUserRoleManager.addSystemUser(userName, credentialObj, roleList);
                return;
            }

            // #################### Domain Name Free Zone Starts Here ################################

            // First check whether this user store is a readonly one. If so we cannot continue.
            if (isReadOnly()) {
                handleAddUserFailure(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode(),
                        ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage(), userName, credential, roleList,
                        claims, profileName);
                throw new UserStoreException(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.toString());
            }
            // Set skipPasswordPolicyValidation thread local if the user creation flow is ask password enabled.
            if (claims != null && claims.containsKey(UserCoreClaimConstants.ASK_PASSWORD_CLAIM_URI) &&
                    Boolean.parseBoolean(claims.get(UserCoreClaimConstants.ASK_PASSWORD_CLAIM_URI))) {
                UserCoreUtil.setSkipPasswordPatternValidationThreadLocal(true);
            }

            // This happens only once during first startup - adding administrator user/role.
            if (userName.indexOf(CarbonConstants.DOMAIN_SEPARATOR) > 0) {
                userName = userStore.getDomainFreeName();
                roleList = UserCoreUtil.removeDomainFromNames(roleList);
            }
            if (roleList == null) {
                roleList = new String[0];
            }
            if (claims == null) {
                claims = new HashMap<>();
            }

            // #################### <Pre-Listeners> #####################################################
            try {
                // First we are going to call all the registered User Store Manager Listeners.
                for (UserStoreManagerListener listener : UMListenerServiceComponent.getUserStoreManagerListeners()) {
                    Object credentialArgument;
                    if (listener instanceof SecretHandleableListener) {
                        credentialArgument = credentialObj;
                    } else {
                        credentialArgument = credential;
                    }

                    // Call the listener, and if it returns false, then it is an error scenario.
                    if (!listener.addUser(userName, credentialArgument, roleList, claims, profileName, this)) {
                        handleAddUserFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_USER.getCode(),
                                String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_USER.getMessage(),
                                        UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userName, credential,
                                roleList, claims, profileName);
                        return;
                    }
                }
            } catch (UserStoreException ex) {
                handleAddUserFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_USER.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_USER.getMessage(), ex.getMessage()),
                        userName, credential, roleList, claims, profileName);
                throw ex;
            }

            // Then call all the registered User Operation Event Listeners.
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                // This is to call all new listeners. All listeners should support the Secret object as the credential
                // for security reasons.
                if (listener instanceof SecretHandleableListener) {
                    try {
                        // Call pre add user listener.
                        if (!listener.doPreAddUser(userName, credentialObj, roleList, claims, profileName, this)) {
                            handleAddUserFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_USER.getCode(),
                                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_USER.getMessage(),
                                            UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userName,
                                    credential, roleList, claims, profileName);
                            return;
                        }
                    } catch (UserStoreException ex) {
                        String message = String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_USER.getMessage(),
                                ex.getMessage());
                        handleAddUserFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_USER.getCode(), message,
                                userName, credential, roleList, claims, profileName);
                        throw ex;
                    }
                } else {
                    // This is to support the legacy listeners which does not know how to handle the Secret object as
                    // the credentials.

                    // String buffers are used to let the listeners to modify the password.
                    StringBuffer credBuff = null;
                    if (credential == null) {
                        // No credentials passed. A default password will be set.
                        credBuff = new StringBuffer();
                    } else if (credential instanceof String) {
                        credBuff = new StringBuffer((String) credential);
                    }

                    // If the credential is not null and not an instance of "String".
                    if (credBuff == null) {
                        continue;
                    }

                    try {
                        // Call pre add user listener.
                        if (!listener.doPreAddUser(userName, credBuff, roleList, claims, profileName, this)) {
                            handleAddUserFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_USER.getCode(),
                                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_USER.getMessage(),
                                            UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userName,
                                    credential, roleList, claims, profileName);
                            return;
                        }
                    } catch (UserStoreException e) {
                        handleAddUserFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_USER.getCode(),
                                String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_USER.getMessage(),
                                        e.getMessage()), userName, credential, roleList, claims, profileName);
                        throw e;
                    }
                    // Reading the modified value and update the credential object (Secret) with the new values.
                    credential = credBuff.toString();
                    credentialObj.clear();
                    try {
                        credentialObj = Secret.getSecret(credential);
                    } catch (UnsupportedSecretTypeException e) {
                        handleAddUserFailure(ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.getCode(),
                                ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.getMessage(), userName,
                                credential, roleList, claims, profileName);
                        throw new UserStoreException(
                                ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.toString(), e);
                    }
                }
            }

            // #################### </Pre-Listeners> #####################################################

            // Validate the username against provided regular expressions.
            if (!checkUserNameValid(userStore.getDomainFreeName()) &&
                    !UserCoreUtil.getSkipUsernamePatternValidationThreadLocal()) {
                String regEx = realmConfig
                        .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USER_NAME_JAVA_REG_EX);
                // Inorder to support both UsernameJavaRegEx and UserNameJavaRegEx.
                if (StringUtils.isEmpty(regEx) || StringUtils.isEmpty(regEx.trim())) {
                    regEx = realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USER_NAME_JAVA_REG);
                }
                String message = String
                        .format(ErrorMessages.ERROR_CODE_INVALID_USER_NAME.getMessage(), userStore.getDomainFreeName(),
                                regEx);
                String errorCode = ErrorMessages.ERROR_CODE_INVALID_USER_NAME.getCode();
                handleAddUserFailure(errorCode, message, userName, credential, roleList, claims, profileName);
                throw new UserStoreException(errorCode + " - " + message);
            }

            // Validate the password against provided regular expressions.
            if (!checkUserPasswordValid(credentialObj)) {
                String regEx = realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_JAVA_REG_EX);
                String message = String.format(ErrorMessages.ERROR_CODE_INVALID_PASSWORD.getMessage(), regEx);
                String errorCode = ErrorMessages.ERROR_CODE_INVALID_PASSWORD.getCode();
                handleAddUserFailure(errorCode, message, userName, credential, roleList, claims, profileName);
                throw new UserStoreException(errorCode + " - " + message);
            }

            boolean isExistingUser;
            if (isUniqueUserIdEnabled) {
                isExistingUser = getUserIDFromUserName(userName) != null;
            } else {
                isExistingUser = doCheckExistingUser(userName);
            }
            // Property to check whether this user store supports new APIs with unique user id.
            if (isExistingUser) {
                String message = String.format(ErrorMessages.ERROR_CODE_USER_ALREADY_EXISTS.getMessage(), userName);
                String errorCode = ErrorMessages.ERROR_CODE_USER_ALREADY_EXISTS.getCode();
                handleAddUserFailure(errorCode, message, userName, credential, roleList, claims, profileName);
                throw new UserStoreException(errorCode + " - " + message);
            }

            // Categorize roles according to the internal and external roles.
            List<String> internalRoles = new ArrayList<>();
            List<String> externalRoles = new ArrayList<>();
            filterRoles(roleList, internalRoles, externalRoles);

            // Check existence of internal roles.
            for (String internalRole : internalRoles) {
                if (!hybridRoleManager.isExistingRole(internalRole)) {
                    String message = String
                            .format(ErrorMessages.ERROR_CODE_INTERNAL_ROLE_NOT_EXISTS.getMessage(), internalRole);
                    String errorCode = ErrorMessages.ERROR_CODE_INTERNAL_ROLE_NOT_EXISTS.getCode();
                    handleAddUserFailure(errorCode, message, userName, credential, roleList, claims, profileName);
                    throw new UserStoreException(errorCode + " - " + message);
                }
            }

            // Check existence of external roles.
            for (String externalRole : externalRoles) {
                if (!doCheckExistingRole(externalRole)) {
                    String message = String
                            .format(ErrorMessages.ERROR_CODE_EXTERNAL_ROLE_NOT_EXISTS.getMessage(), externalRole);
                    String errorCode = ErrorMessages.ERROR_CODE_EXTERNAL_ROLE_NOT_EXISTS.getCode();
                    handleAddUserFailure(errorCode, message, userName, credential, roleList, claims, profileName);
                    throw new UserStoreException(errorCode + " - " + message);
                }
            }

            // Check for the existence of the claims.
            for (Map.Entry<String, String> entry : claims.entrySet()) {
                ClaimMapping claimMapping;
                try {
                    claimMapping = (ClaimMapping) claimManager.getClaimMapping(entry.getKey());
                } catch (org.wso2.carbon.user.api.UserStoreException e) {
                    String errorMessage = String
                            .format(ErrorMessages.ERROR_CODE_UNABLE_TO_FETCH_CLAIM_MAPPING.getMessage(),
                                    "persisting user attributes.");
                    String errorCode = ErrorMessages.ERROR_CODE_UNABLE_TO_FETCH_CLAIM_MAPPING.getCode();
                    handleAddUserFailure(errorCode, errorMessage, userName, credential, roleList, claims,
                            profileName);
                    throw new UserStoreException(errorCode + " - " + errorMessage, e);
                }
                if (claimMapping == null) {
                    String errorMessage = String
                            .format(ErrorMessages.ERROR_CODE_INVALID_CLAIM_URI.getMessage(), entry.getKey());
                    String errorCode = ErrorMessages.ERROR_CODE_INVALID_CLAIM_URI.getCode();
                    handleAddUserFailure(errorCode, errorMessage, userName, credential, roleList, claims,
                            profileName);
                    throw new UserStoreException(errorCode + " - " + errorMessage);
                }
            }

            // Call the do add user method of the underlying user store to add the user.
            try {
                // If this is an user store that that supports the APIs with unique user ID, then we can call the new
                // APIs. However, we don't need the returned values as this API does not require those values.
                // Ex. Generated unique id.
                if (isUniqueUserIdEnabled) {
                    // Ignore the return value as we don't need it.
                    user = doAddUserWithID(userName, credential, externalRoles.toArray(new String[0]), claims,
                            profileName, requirePasswordChange);
                } else {
                    // Call the old API since this user store does not support the unique user id related APIs.
                    doAddUser(userName, credentialObj, externalRoles.toArray(new String[0]), claims, profileName,
                            requirePasswordChange);
                }
            } catch (UserStoreException ex) {
                handleAddUserFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_ADDING_USER.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_ADDING_USER.getMessage(), ex.getMessage()),
                        userName, credential, roleList, claims, profileName);
                throw ex;
            }

            if (internalRoles.size() > 0) {
                hybridRoleManager.updateHybridRoleListOfUser(userName, null, internalRoles.toArray(new String[0]));
            }

            // #################### <Post-Listeners> #####################################################
            try {
                for (UserOperationEventListener listener : UMListenerServiceComponent
                        .getUserOperationEventListeners()) {
                    Object credentialArgument;
                    if (listener instanceof SecretHandleableListener) {
                        credentialArgument = credentialObj;
                    } else {
                        credentialArgument = credential;
                    }

                    if (!listener.doPostAddUser(userName, credentialArgument, roleList, claims, profileName, this)) {
                        handleAddUserFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_ADD_USER.getCode(),
                                String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_ADD_USER.getMessage(),
                                        UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), userName,
                                credential, roleList, claims, profileName);
                        return;
                    }
                }
            } catch (UserStoreException ex) {
                handleAddUserFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_ADD_USER.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_ADD_USER.getMessage(),
                                ex.getMessage()), userName, credential, roleList, claims, profileName);
                throw ex;
            }
            // #################### </Post-Listeners> #####################################################
        } finally {
            UserCoreUtil.removeSkipPasswordPatternValidationThreadLocal();
            UserCoreUtil.removeSkipUsernamePatternValidationThreadLocal();
            credentialObj.clear();
        }

        // Clean the role cache since it contains old role information.
        clearUserRolesCache(userName);
    }

    /**
     * Checks whether this user store supports new user unique id feature.
     *
     * @return True if this user store supports unique user id feature.
     */
    public boolean isUniqueUserIdEnabled() {

        return Boolean.parseBoolean(realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USER_ID_ENABLED));
    }

    /**
     * Checks whether this user store supports group id feature.
     *
     * @return True if this user store supports group id feature.
     */
    public boolean isUniqueGroupIdEnabled() {

        return Boolean.parseBoolean(realmConfig.getUserStoreProperty(UserStoreConfigConstants.GROUP_ID_ENABLED));
    }

    /**
     * To handle the erroneous scenario in add user flow.
     *
     * @param errorCode    Relevant error code.
     * @param errorMessage Relevant error message.
     * @param userName     Name of the user.
     * @param credential   Credential
     * @param roleList     List of roles.
     * @param claims       Claims
     * @param profileName  Name of the profile.
     * @throws UserStoreException UserStore Exception that could be thrown during the execution.
     */
    private void handleAddUserFailure(String errorCode, String errorMessage, String userName, Object credential,
            String[] roleList, Map<String, String> claims, String profileName) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !listener
                    .onAddUserFailure(errorCode, errorMessage, userName, credential, roleList, claims, profileName,
                            this)) {
                return;
            }
        }
    }

    /**
     * To handle the erroneous scenario in add user flow.
     *
     * @param errorCode    Relevant error code.
     * @param errorMessage Relevant error message.
     * @param userName     Name of the user.
     * @param credential   Credential
     * @param roleList     List of roles.
     * @param claims       Claims.
     * @param profileName  Name of the profile.
     * @throws UserStoreException UserStore Exception that could be thrown during the execution.
     */
    private void handleAddUserFailureWithID(String errorCode, String errorMessage, String userName, Object credential,
            String[] roleList, Map<String, String> claims, String profileName) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !((AbstractUserManagementErrorListener) listener)
                    .onAddUserFailureWithID(errorCode, errorMessage, userName, credential, roleList, claims,
                            profileName, this)) {
                return;
            }
        }
    }

    public final void updateUserListOfRole(final String roleName, final String[] deletedUsers, final String[] newUsers)
            throws UserStoreException {
        try {
            AccessController.doPrivileged((PrivilegedExceptionAction<String>) () -> {
                updateUserListOfRoleInternal(roleName, deletedUsers, newUsers);
                return null;
            });
        } catch (PrivilegedActionException e) {
            if (!(e.getException() instanceof UserStoreException)) {
                handleUpdateUserListOfRoleFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_UPDATING_ROLE_OF_USER.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_UPDATING_ROLE_OF_USER.getMessage(),
                                e.getMessage()), roleName, deletedUsers, newUsers);
            }
            throw (UserStoreException) e.getException();
        }
    }

    /**
     * This method calls the relevant listeners that handle failure during update user list of a role.
     *
     * @param errorCode    Relevant error code.
     * @param errorMessage Relevant error message.
     * @param roleName     Name of the role.
     * @param deletedUsers Removed users from a particular role.
     * @param newUsers     Added users from a particular role.
     * @throws UserStoreException Exception that will be thrown from the relevant listeners.
     */
    private void handleUpdateUserListOfRoleFailure(String errorCode, String errorMessage, String roleName,
            String deletedUsers[], String[] newUsers) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !listener
                    .onUpdateUserListOfRoleFailure(errorCode, errorMessage, roleName, deletedUsers, newUsers, this)) {
                return;
            }
        }
    }

    /**
     * This method calls the relevant listeners that handle failure during update user list of a role.
     *
     * @param errorCode      Relevant error code.
     * @param errorMessage   Relevant error message.
     * @param roleName       Name of the role.
     * @param deletedUserIDs Removed users from a particular role.
     * @param newUserIDs     Added users from a particular role.
     * @throws UserStoreException Exception that will be thrown from the relevant listeners.
     */
    private void handleUpdateUserListOfRoleFailureWithID(String errorCode, String errorMessage, String roleName,
            String[] deletedUserIDs, String[] newUserIDs) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !((AbstractUserManagementErrorListener) listener)
                    .onUpdateUserListOfRoleFailureWithID(errorCode, errorMessage, roleName, deletedUserIDs, newUserIDs,
                            this)) {
                return;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    private final void updateUserListOfRoleInternal(String roleName, String[] deletedUsers, String[] newUsers)
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
                    if (deletedUsers[i].equalsIgnoreCase(realmConfig.getAdminUserName()) || (primaryDomain
                            + deletedUsers[i]).equalsIgnoreCase(realmConfig.getAdminUserName())) {
                        handleUpdateUserListOfRoleFailure(
                                ErrorMessages.ERROR_CODE_CANNOT_REMOVE_ADMIN_ROLE_FROM_ADMIN.getCode(),
                                ErrorMessages.ERROR_CODE_CANNOT_REMOVE_ADMIN_ROLE_FROM_ADMIN.getMessage(), roleName,
                                deletedUsers, newUsers);
                        throw new UserStoreException(
                                ErrorMessages.ERROR_CODE_CANNOT_REMOVE_ADMIN_ROLE_FROM_ADMIN.toString());
                    }

                }
            }
        }

        UserStore userStore = getUserStore(roleName);

        if (userStore.isHybridRole()) {
            // Check whether someone is trying to update Everyone role.
            if (UserCoreUtil.isEveryoneRole(roleName, realmConfig)) {
                handleUpdateUserListOfRoleFailure(ErrorMessages.ERROR_CODE_CANNOT_UPDATE_EVERYONE_ROLE.getCode(),
                        ErrorMessages.ERROR_CODE_CANNOT_UPDATE_EVERYONE_ROLE.getMessage(), roleName, deletedUsers,
                        newUsers);
                throw new UserStoreException(ErrorMessages.ERROR_CODE_CANNOT_UPDATE_EVERYONE_ROLE.toString());
            }
            if (!handlePreUpdateUserListOfRole(roleName, deletedUsers, newUsers, false, true)) {
                handleUpdateUserListOfRoleFailure(
                        ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_UPDATE_USERS_OF_ROLE.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_UPDATE_USERS_OF_ROLE.getMessage(),
                                UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), roleName, deletedUsers,
                        newUsers);
                return;
            }
            if (UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(userStore.getDomainName())) {
                hybridRoleManager.updateUserListOfHybridRole(userStore.getDomainFreeName(), deletedUsers, newUsers);
                handleDoPostUpdateUserListOfRole(roleName, deletedUsers, newUsers, false, true);
            } else {
                hybridRoleManager.updateUserListOfHybridRole(userStore.getDomainAwareName(), deletedUsers, newUsers);
                handleDoPostUpdateUserListOfRole(roleName, deletedUsers, newUsers, false, true);
            }
            clearUserRolesCacheByTenant(this.tenantId);
            return;
        }

        if (userStore.isSystemStore()) {
            systemUserRoleManager.updateUserListOfSystemRole(userStore.getDomainFreeName(),
                    UserCoreUtil.removeDomainFromNames(deletedUsers),
                    UserCoreUtil.removeDomainFromNames(newUsers));
            handleDoPostUpdateUserListOfRole(roleName, deletedUsers, newUsers, true, false);
            return;
        }

        if (userStore.isRecurssive()) {
            userStore.getUserStoreManager().updateUserListOfRole(userStore.getDomainFreeName(),
                    UserCoreUtil.removeDomainFromNames(deletedUsers),
                    UserCoreUtil.removeDomainFromNames(newUsers));
            return;
        }

        // #################### Domain Name Free Zone Starts Here ################################
        if (deletedUsers == null) {
            deletedUsers = new String[0];
        }
        if (newUsers == null) {
            newUsers = new String[0];
        }
        if (!handlePreUpdateUserListOfRole(roleName, deletedUsers, newUsers, false, false)) {
            handleUpdateUserListOfRoleFailure(
                    ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_UPDATE_USERS_OF_ROLE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_UPDATE_USERS_OF_ROLE.getMessage(),
                            UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), roleName, deletedUsers,
                    newUsers);
            return;
        }
        if (deletedUsers.length > 0 || newUsers != null && newUsers.length > 0) {
            if (!isReadOnly() && writeGroupsEnabled) {
                try {
                    // No need to check for group uuid enabled since all userstores should support this. Retrieving
                    // group id and moving in different direction is not efficient.
                    if (isUniqueUserIdEnabledInUserStore(userStore)) {
                        List<String> newUserIds = getUserIDsFromUserNames(Arrays.asList(newUsers));
                        List<String> deletedUserIds = getUserIDsFromUserNames(Arrays.asList(deletedUsers));
                        doUpdateUserListOfRoleWithID(userStore.getDomainFreeName(),
                                UserCoreUtil.removeDomainFromNames(deletedUserIds.toArray(new String[0])),
                                UserCoreUtil.removeDomainFromNames(newUserIds.toArray(new String[0])));
                    } else {
                        doUpdateUserListOfRole(userStore.getDomainFreeName(),
                                UserCoreUtil.removeDomainFromNames(deletedUsers),
                                UserCoreUtil.removeDomainFromNames(newUsers));
                    }
                } catch (UserStoreException ex) {
                    handleUpdateUserListOfRoleFailure(
                            ErrorMessages.ERROR_CODE_ERROR_DURING_UPDATE_USERS_OF_ROLE.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_UPDATE_USERS_OF_ROLE.getMessage(),
                                    ex.getMessage()), roleName, deletedUsers, newUsers);
                    throw ex;
                }
            } else {
                handleUpdateUserListOfRoleFailure(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode(),
                        ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage(), roleName, deletedUsers, newUsers);
                throw new UserStoreException(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.toString());
            }
        }

        // need to clear user roles cache upon roles update
        clearUserRolesCacheByTenant(this.tenantId);

        // Call relevant listeners after updating user list of role.
        handleDoPostUpdateUserListOfRole(roleName, deletedUsers, newUsers, false, false);
    }

    /**
     * This method is responsible for calling the listeners after updating user list of role.
     *
     * @param roleName       Name of the role.
     * @param deletedUsers   Removed users
     * @param newUsers       Added users.
     * @param isAuditLogOnly Indicate whether to call only the audit log listener.
     * @throws UserStoreException Exception that will be thrown by relevant listeners.
     */
    private void handleDoPostUpdateUserListOfRole(String roleName, String[] deletedUsers, String[] newUsers,
                                                  boolean isAuditLogOnly, boolean isInternalRole) throws UserStoreException {

        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (isAuditLogOnly && !listener.getClass().getName()
                        .endsWith(UserCoreErrorConstants.AUDIT_LOGGER_CLASS_NAME)) {
                    continue;
                }
                boolean success;
                if (isInternalRole) {
                    success = listener.doPostUpdateUserListOfInternalRole(
                            roleName, deletedUsers, newUsers, this);
                } else {
                    success = listener.doPostUpdateUserListOfRole(roleName, deletedUsers, newUsers, this);
                }

                if (!success) {
                    return;
                }
            }
        } catch (UserStoreException ex) {
            handleUpdateUserListOfRoleFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_POST_UPDATE_USERS_OF_ROLE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_POST_UPDATE_USERS_OF_ROLE.getMessage(),
                            ex.getMessage()), roleName, deletedUsers, newUsers);
            throw ex;
        }
    }

    /**
     * This method is responsible for calling the listeners after updating user list of role.
     *
     * @param roleName       Name of the role.
     * @param deletedUserIDs Removed users.
     * @param newUserIDs     Added users.
     * @param isAuditLogOnly Indicate whether to call only the audit log listener.
     * @throws UserStoreException Exception that will be thrown by relevant listeners.
     */
    private void handleDoPostUpdateUserListOfRoleWithID(String roleName, String[] deletedUserIDs, String[] newUserIDs,
                                                        boolean isAuditLogOnly, boolean isInternalRole)
            throws UserStoreException {

        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (isAuditLogOnly && !listener.getClass().getName()
                        .endsWith(UserCoreErrorConstants.AUDIT_LOGGER_CLASS_NAME)) {
                    continue;
                }
                boolean success = true;
                if (listener instanceof AbstractUserOperationEventListener) {
                    if (isInternalRole) {
                        success = ((AbstractUserOperationEventListener) listener).
                                doPostUpdateUserListOfInternalRoleWithID(
                                        roleName, deletedUserIDs, newUserIDs, this);
                    } else {
                        success = ((AbstractUserOperationEventListener) listener).
                                doPostUpdateUserListOfRoleWithID(roleName, deletedUserIDs, newUserIDs, this);
                    }
                }
                if (!success) {
                    return;
                }
            }
        } catch (UserStoreException ex) {
            handleUpdateUserListOfRoleFailureWithID(
                    ErrorMessages.ERROR_CODE_ERROR_WHILE_POST_UPDATE_USERS_OF_ROLE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_POST_UPDATE_USERS_OF_ROLE.getMessage(),
                            ex.getMessage()), roleName, deletedUserIDs, newUserIDs);
            throw ex;
        }
    }

    public final void updateRoleListOfUser(final String username, final String[] deletedRoles, final String[] newRoles)
            throws UserStoreException {
        try {
            AccessController.doPrivileged((PrivilegedExceptionAction<String>) () -> {
                updateRoleListOfUserInternal(username, deletedRoles, newRoles);
                return null;
            });
        } catch (PrivilegedActionException e) {
            if (!(e.getException() instanceof UserStoreException)) {
                handleUpdateRoleListOfUserFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_UPDATE_USERS_OF_ROLE.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_UPDATE_USERS_OF_ROLE.getMessage(),
                                e.getMessage()), username, deletedRoles, newRoles);
            }
            throw (UserStoreException) e.getException();
        }
    }

    /**
     * This method is responsible for calling the methods of listeners after a failure while trying to update role
     * list of users.
     *
     * @param errorCode    Error code.
     * @param errorMessage Error message.
     * @param userName     User Name
     * @param deletedRoles Removed roles
     * @param newRoles     Assigned roles
     * @throws UserStoreException Exception that will be thrown by relevant listeners.
     */
    private void handleUpdateRoleListOfUserFailure(String errorCode, String errorMessage, String userName,
            String[] deletedRoles, String[] newRoles) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !listener
                    .onUpdateRoleListOfUserFailure(errorCode, errorMessage, userName, deletedRoles, newRoles, this)) {
                return;
            }
        }
    }

    /**
     * This method is responsible for calling the methods of listeners after a failure while trying to update role
     * list of users.
     *
     * @param errorCode    Error code.
     * @param errorMessage Error message.
     * @param userID       User ID.
     * @param deletedRoles Removed roles.
     * @param newRoles     Assigned roles.
     * @throws UserStoreException Exception that will be thrown by relevant listeners.
     */
    private void handleUpdateRoleListOfUserFailureWithID(String errorCode, String errorMessage, String userID,
            String[] deletedRoles, String[] newRoles) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !((AbstractUserManagementErrorListener) listener)
                    .onUpdateRoleListOfUserFailureWithID(errorCode, errorMessage, userID, deletedRoles, newRoles,
                            this)) {
                return;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    private final void updateRoleListOfUserInternal(String userName, String[] deletedRoles, String[] newRoles)
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
                    if (deletedRoles[i].equalsIgnoreCase(realmConfig.getAdminRoleName()) || (primaryDomain
                            + deletedRoles[i]).equalsIgnoreCase(realmConfig.getAdminRoleName())) {
                        handleUpdateRoleListOfUserFailure(
                                ErrorMessages.ERROR_CODE_CANNOT_REMOVE_ADMIN_ROLE_FROM_ADMIN.getCode(),
                                ErrorMessages.ERROR_CODE_CANNOT_REMOVE_ADMIN_ROLE_FROM_ADMIN.getMessage(), userName,
                                deletedRoles, newRoles);
                        throw new UserStoreException(
                                ErrorMessages.ERROR_CODE_CANNOT_REMOVE_ADMIN_ROLE_FROM_ADMIN.toString());
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

        if (userStore.isSystemStore()) {
            systemUserRoleManager.updateSystemRoleListOfUser(userStore.getDomainFreeName(),
                    UserCoreUtil.removeDomainFromNames(deletedRoles),
                    UserCoreUtil.removeDomainFromNames(newRoles));
            return;
        }

        // #################### Domain Name Free Zone Starts Here ################################
        if (deletedRoles == null) {
            deletedRoles = new String[0];
        }
        if (newRoles == null) {
            newRoles = new String[0];
        }
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

        List<String> internalRoleDelWithDomain = new ArrayList<>();
        List<String> internalRoleNewWithDomain = new ArrayList<>();

        if (deletedRoles != null && deletedRoles.length > 0) {
            for (String deleteRole : deletedRoles) {
                if (UserCoreUtil.isEveryoneRole(deleteRole, realmConfig)) {
                    handleUpdateRoleListOfUserFailure(ErrorMessages.ERROR_CODE_CANNOT_UPDATE_EVERYONE_ROLE.getCode(),
                            ErrorMessages.ERROR_CODE_CANNOT_UPDATE_EVERYONE_ROLE.getMessage(), userName, deletedRoles,
                            newRoles);
                    throw new UserStoreException(ErrorMessages.ERROR_CODE_CANNOT_UPDATE_EVERYONE_ROLE.toString());
                }
                String domain = null;
                int index1 = deleteRole.indexOf(CarbonConstants.DOMAIN_SEPARATOR);
                if (index1 > 0) {
                    domain = deleteRole.substring(0, index1);
                }
                processDeletedRoles(internalRoleDel, roleDel, deleteRole, domain, internalRoleDelWithDomain);
            }
            deletedRoles = roleDel.toArray(new String[roleDel.size()]);
        }

        if (newRoles != null && newRoles.length > 0) {
            for (String newRole : newRoles) {
                if (UserCoreUtil.isEveryoneRole(newRole, realmConfig)) {
                    handleUpdateRoleListOfUserFailure(ErrorMessages.ERROR_CODE_CANNOT_UPDATE_EVERYONE_ROLE.getCode(),
                            ErrorMessages.ERROR_CODE_CANNOT_UPDATE_EVERYONE_ROLE.getMessage(), userName, deletedRoles,
                            newRoles);
                    throw new UserStoreException(ErrorMessages.ERROR_CODE_CANNOT_UPDATE_EVERYONE_ROLE.toString());
                }
                String domain = null;
                int index2 = newRole.indexOf(CarbonConstants.DOMAIN_SEPARATOR);
                if (index2 > 0) {
                    domain = newRole.substring(0, index2);
                }

                processNewRoles(internalRoleNew, roleNew, newRole, domain, internalRoleNewWithDomain);
            }
            newRoles = roleNew.toArray(new String[roleNew.size()]);
        }

        boolean isPreUpdateInternalRoleListOfUserSuccess = true;
        boolean isPreUpdateRoleListOfUserSuccess = true;
        String[] deletedInternalRolesArray = new String[0];
        String[] deletedInternalRolesArrayWithDomain = new String[0];
        String[] addInternalRolesArray = new String[0];
        String[] newInternalRolesArrayWithDomain = new String[0];

        if (CollectionUtils.isNotEmpty(internalRoleDel) || CollectionUtils.isNotEmpty(internalRoleNew)) {
            deletedInternalRolesArray = internalRoleDel.toArray(new String[internalRoleDel.size()]);
            deletedInternalRolesArrayWithDomain = internalRoleDelWithDomain.toArray(new
                    String[internalRoleDelWithDomain.size()]);
            addInternalRolesArray = internalRoleNew.toArray(new String[internalRoleNew.size()]);
            newInternalRolesArrayWithDomain =
                    internalRoleNewWithDomain.toArray(new String[internalRoleNewWithDomain.size()]);
            isPreUpdateInternalRoleListOfUserSuccess =
                    handlePreUpdateRoleListOfUser(userName, deletedInternalRolesArrayWithDomain,
                                                  newInternalRolesArrayWithDomain, false, true);
        }

        if (ArrayUtils.isNotEmpty(deletedRoles) || ArrayUtils.isNotEmpty(newRoles)) {
            isPreUpdateRoleListOfUserSuccess = handlePreUpdateRoleListOfUser(userName, deletedRoles, newRoles,
                    false, false);
        }

        if (!isPreUpdateInternalRoleListOfUserSuccess || !isPreUpdateRoleListOfUserSuccess) {
            handleUpdateRoleListOfUserFailure(
                    ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_ROLE_OF_USER.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_ROLE_OF_USER.getMessage(),
                            UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userName, deletedRoles,
                    newRoles);
            return;
        }
        if (ArrayUtils.isNotEmpty(deletedInternalRolesArray) || ArrayUtils.isNotEmpty(addInternalRolesArray)) {
            hybridRoleManager.updateHybridRoleListOfUser(userStore.getDomainFreeName(), deletedInternalRolesArray,
                    addInternalRolesArray);
        }

        if (ArrayUtils.isNotEmpty(deletedRoles) || ArrayUtils.isNotEmpty(newRoles)) {
            if (!isReadOnly() && writeGroupsEnabled) {
                try {
                    // Property to check whether this user store supports new APIs with unique user id.
                    boolean isUniqueUserIdEnabled = isUniqueUserIdEnabledInUserStore(userStore);
                    if (isUniqueUserIdEnabled) {
                        String userID = getUserIDFromUserName(userName);
                        doUpdateRoleListOfUserWithID(userID, deletedRoles, newRoles);
                    } else {
                        doUpdateRoleListOfUser(userName, deletedRoles, newRoles);
                    }
                } catch (UserStoreException ex) {
                    handleUpdateRoleListOfUserFailure(
                            ErrorMessages.ERROR_CODE_ERROR_WHILE_UPDATING_ROLE_OF_USER.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_UPDATING_ROLE_OF_USER.getMessage(),
                                    ex.getMessage()), userName, deletedRoles, newRoles);
                    throw ex;
                }
            } else {
                handleUpdateRoleListOfUserFailure(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode(),
                        ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage(), userName, deletedRoles, newRoles);
                throw new UserStoreException(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.toString());
            }
        }

        clearUserRolesCache(userName);

        // Call the relevant listeners after updating the role list of user.
        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!listener.doPostUpdateRoleListOfUser(userName, deletedRoles, newRoles, this)) {
                    handleUpdateRoleListOfUserFailure(
                            ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_ROLE_OF_USER.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_ROLE_OF_USER.getMessage(),
                                    UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), userName, deletedRoles,
                            newRoles);
                    return;
                }
            }
        } catch (UserStoreException ex) {
            handleUpdateRoleListOfUserFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_ROLE_OF_USER.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_ROLE_OF_USER.getMessage(),
                            ex.getMessage()), userName, deletedRoles, newRoles);
            throw ex;
        }

        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!listener.doPostUpdateInternalRoleListOfUser(userName, deletedInternalRolesArrayWithDomain,
                                                                 newInternalRolesArrayWithDomain, this)) {
                    handleUpdateRoleListOfUserFailure(
                            ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_ROLE_OF_USER.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_ROLE_OF_USER.getMessage(),
                                          UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE),
                            userName, deletedRoles, newRoles);
                    return;
                }
            }
        } catch (UserStoreException ex) {
            handleUpdateRoleListOfUserFailure(
                    ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_ROLE_OF_USER.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_ROLE_OF_USER.getMessage(),
                                                            ex.getMessage()),
                    userName, deletedRoles, newRoles);
            throw ex;
        }
    }

    /**
     * This method is responsible for calling the relevant methods when there is a failure while trying to update the
     * role name.
     *
     * @param errorCode    Error code.
     * @param errorMessage Error message
     * @param roleName     Role Name
     * @param newRoleName  New Role Name
     * @throws UserStoreException Exception that will be thrown by relevant methods in listener.
     */
    private void handleUpdateRoleNameFailure(String errorCode, String errorMessage, String roleName, String newRoleName)
            throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !listener
                    .onUpdateRoleNameFailure(errorCode, errorMessage, roleName, newRoleName, this)) {
                return;
            }
        }
    }

    /**
     * This method is responsible for calling post update role name methods in listeners.
     *
     * @param roleName       Name of the role.
     * @param newRoleName    New role name
     * @param isAuditLogOnly to indicate whether to call only the audit log listener.
     * @throws UserStoreException Exception that will be thrown by relevant listeners.
     */
    private void handlePostUpdateRoleName(String roleName, String newRoleName, boolean isAuditLogOnly)
            throws UserStoreException {

        try {
            boolean internalRole = isAnInternalRole(roleName);
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (isAuditLogOnly && !listener.getClass().getName()
                        .endsWith(UserCoreErrorConstants.AUDIT_LOGGER_CLASS_NAME)) {
                    continue;
                }

                boolean success = false;
                if (internalRole && listener instanceof AbstractUserOperationEventListener) {
                    success = ((AbstractUserOperationEventListener) listener).doPostUpdateInternalRoleName(roleName,
                            newRoleName, this);
                } else if (internalRole && !(listener instanceof AbstractUserOperationEventListener)) {
                    success = true;
                } else if (!internalRole) {
                    success = listener.doPostUpdateRoleName(roleName, newRoleName, this);
                }

                if (!success) {
                    handleUpdateRoleNameFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_ROLE_NAME.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_ROLE_NAME.getMessage(),
                                    UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), roleName, newRoleName);
                    return;
                }
            }
        } catch (UserStoreException ex) {
            handleUpdateRoleNameFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_ROLE_NAME.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_ROLE_NAME.getMessage(),
                            ex.getMessage()), roleName, newRoleName);
            throw ex;
        }
    }

    /**
     * This method is responsible for calling pre update role name methods in listeners.
     *
     * @param roleName       Name of the internal role.
     * @param newRoleName    New internal role name
     * @param isAuditLogOnly to indicate whether to call only the audit log listener.
     * @throws UserStoreException Exception that will be thrown by relevant listeners.
     */
    private boolean handlePreUpdateRoleName(String roleName, String newRoleName, boolean isAuditLogOnly)
            throws UserStoreException {

        try {
            boolean internalRole = isAnInternalRole(roleName);
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (isAuditLogOnly && !listener.getClass().getName()
                        .endsWith(UserCoreErrorConstants.AUDIT_LOGGER_CLASS_NAME)) {
                    continue;
                }

                boolean success = false;
                if (internalRole && listener instanceof AbstractUserOperationEventListener) {
                    success = ((AbstractUserOperationEventListener) listener).doPreUpdateInternalRoleName(roleName,
                            newRoleName, this);
                }
                if (internalRole && !(listener instanceof AbstractUserOperationEventListener)) {
                    success = true;
                } else if (!internalRole) {
                    success = listener.doPreUpdateRoleName(roleName, newRoleName, this);
                }

                if (!success) {
                    handleUpdateRoleNameFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_ROLE_NAME.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_ROLE_NAME.getMessage(),
                                    UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), roleName, newRoleName);
                    return false;
                }
            }
        } catch (UserStoreException ex) {
            handleUpdateRoleNameFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_ROLE_NAME.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_ROLE_NAME.getMessage(),
                            ex.getMessage()), roleName, newRoleName);
            throw ex;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public final void updateRoleName(String roleName, String newRoleName) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, String.class};
            callSecure("updateRoleName", new Object[]{roleName, newRoleName}, argTypes);
            return;
        }

        if (UserCoreUtil.isPrimaryAdminRole(newRoleName, realmConfig)) {
            handleUpdateRoleNameFailure(ErrorMessages.ERROR_CODE_CANNOT_UPDATE_ADMIN_ROLE.getCode(),
                    ErrorMessages.ERROR_CODE_CANNOT_UPDATE_ADMIN_ROLE.getMessage(), roleName, newRoleName);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_CANNOT_UPDATE_ADMIN_ROLE.toString());
        }

        if (UserCoreUtil.isEveryoneRole(newRoleName, realmConfig)) {
            handleUpdateRoleNameFailure(ErrorMessages.ERROR_CODE_CANNOT_UPDATE_EVERYONE_ROLE.getCode(),
                    ErrorMessages.ERROR_CODE_CANNOT_UPDATE_EVERYONE_ROLE.getMessage(), roleName, newRoleName);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_CANNOT_UPDATE_EVERYONE_ROLE.toString());
        }

        UserStore userStore = getUserStoreOfRoles(roleName);
        UserStore userStoreNew = getUserStore(newRoleName);

        if (!UserCoreUtil.canRoleBeRenamed(userStore, userStoreNew, realmConfig)) {
            handleUpdateRoleNameFailure(ErrorMessages.ERROR_CODE_CANNOT_RENAME_ROLE.getCode(),
                    ErrorMessages.ERROR_CODE_CANNOT_RENAME_ROLE.getMessage(), roleName, newRoleName);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_CANNOT_RENAME_ROLE.toString());
        }

        if (userStore.isRecurssive()) {
            userStore.getUserStoreManager()
                    .updateRoleName(userStore.getDomainFreeName(), userStoreNew.getDomainFreeName());
            return;
        }

        // #################### Domain Name Free Zone Starts Here ################################

        if (userStore.isHybridRole()) {
            //Invoke pre listeners.
            if (!handlePreUpdateRoleName(roleName, newRoleName, false)) {
                return;
            }
            if (UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(userStore.getDomainName())) {
                hybridRoleManager.updateHybridRoleName(userStore.getDomainFreeName(),
                        userStoreNew.getDomainFreeName());
            } else {
                hybridRoleManager.updateHybridRoleName(userStore.getDomainAwareName(),
                        userStoreNew.getDomainAwareName());
            }

            // To make sure to maintain the back-ward compatibility, only audit log listener will be called.
            handlePostUpdateRoleName(roleName, newRoleName, false);
            // Need to update user role cache upon update of role names
            clearUserRolesCacheByTenant(this.tenantId);
            return;
        }

        if (!isRoleNameValid(newRoleName)) {
            String regEx = realmConfig
                    .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_ROLE_NAME_JAVA_REG_EX);
            String errorMessage = String
                    .format(ErrorMessages.ERROR_CODE_INVALID_ROLE_NAME.getMessage(), newRoleName, regEx);
            String errorCode = ErrorMessages.ERROR_CODE_INVALID_ROLE_NAME.getCode();
            handleUpdateRoleNameFailure(errorCode, errorMessage, roleName, newRoleName);
            throw new UserStoreException(errorCode + " - " + errorMessage);
        }

        /* Adding two different roles case-sensitively is not possible. Therefore the possibility to have an
        existing role in the newRoleName's name is zero. Hence case sensitively updating the role name will
        not give any error. */
        if (!StringUtils.equalsIgnoreCase(roleName, newRoleName) && isExistingRole(newRoleName)) {
            String errorMessage = String.format(ErrorMessages.ERROR_CODE_ROLE_ALREADY_EXISTS.getMessage(), newRoleName);
            String errorCode = ErrorMessages.ERROR_CODE_ROLE_ALREADY_EXISTS.getCode();
            handleUpdateRoleNameFailure(errorCode, errorMessage, roleName, newRoleName);
            throw new UserStoreException(errorCode + " - " + errorMessage);
        }

        // #################### <Listeners> #####################################################
        if (!handlePreUpdateRoleName(roleName, newRoleName, false)) {
            return;
        }
        // #################### </Listeners> #####################################################
        if (!isReadOnly() && writeGroupsEnabled) {
            try {
                if (isUniqueGroupIdEnabled()) {
                    String groupID = getGroupIdByGroupName(UserCoreUtil.removeDomainFromName(roleName));
                    clearGroupIDResolverCache(groupID, tenantId);
                    doUpdateGroupNameByGroupId(groupID, UserCoreUtil.removeDomainFromName(newRoleName));
                    addGroupNameToGroupIdCache(groupID, newRoleName, getMyDomainName());
                } else {
                    doUpdateRoleName(userStore.getDomainFreeName(), userStoreNew.getDomainFreeName());
                }
            } catch (UserStoreException ex) {
                handleUpdateRoleNameFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_UPDATING_ROLE_NAME.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_UPDATING_ROLE_NAME.getMessage(),
                                ex.getMessage()), roleName, newRoleName);
            }
        } else {
            handleUpdateRoleNameFailure(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode(),
                    ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage(), roleName, newRoleName);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.toString());
        }

        // This is a special case. We need to pass domain aware name.
        userRealm.getAuthorizationManager().resetPermissionOnUpdateRole(
                userStore.getDomainAwareName(), userStoreNew.getDomainAwareName());

        // need to update user role cache upon update of role names
        clearUserRolesCacheByTenant(tenantId);

        // #################### <Listeners> #####################################################
        handlePostUpdateRoleName(roleName, newRoleName, false);
        // #################### </Listeners> #####################################################
    }


    @Override
    public boolean isExistingRole(String roleName, boolean shared) throws org.wso2.carbon.user.api.UserStoreException {
        if (shared) {
            return isExistingShareRole(roleName);
        } else {
            return isExistingRole(roleName);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isExistingRole(String roleName) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class};
            Object object = callSecure("isExistingRole", new Object[]{roleName}, argTypes);
            return (Boolean) object;
        }

        UserStore userStore = getUserStoreOfRoles(roleName);

        if (userStore.isRecurssive()) {
            return userStore.getUserStoreManager().isExistingRole(userStore.getDomainFreeName());
        }

        // #################### Domain Name Free Zone Starts Here ################################

        if (userStore.isSystemStore()) {
            return systemUserRoleManager.isExistingRole(userStore.getDomainFreeName());
        }

        if (userStore.isHybridRole()) {
            boolean exist;

            if (!UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(userStore.getDomainName())) {
                exist = hybridRoleManager.isExistingRole(userStore.getDomainAwareName());
            } else {
                exist = hybridRoleManager.isExistingRole(userStore.getDomainFreeName());
            }

            return exist;
        }

        // This happens only once during first startup - adding administrator user/role.
        roleName = userStore.getDomainFreeName();

        // you can not check existence of shared role using this method.
        if (isSharedGroupEnabled() && roleName.contains(UserCoreConstants.TENANT_DOMAIN_COMBINER)) {
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
     *
     * @param roleName
     * @return
     * @throws UserStoreException
     */
    public boolean isExistingShareRole(String roleName) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class};
            Object object = callSecure("isExistingShareRole", new Object[]{roleName}, argTypes);
            return (Boolean) object;
        }

        UserStoreManager manager = getUserStoreWithSharedRoles();

        if (manager == null) {
            throw new UserStoreException("Share Groups are not supported by this realm");
        }

        return ((AbstractUserStoreManager) manager).doCheckExistingRole(roleName);
    }

    /**
     * TODO  move to API
     *
     * @param roleName
     * @param deletedUsers
     * @param newUsers
     * @throws UserStoreException
     */
    public void updateUsersOfSharedRole(String roleName,
                                        String[] deletedUsers, String[] newUsers) throws UserStoreException {

        UserStoreManager manager = getUserStoreWithSharedRoles();

        if (manager == null) {
            throw new UserStoreException("Share Groups are not supported by this realm");
        }
        if (isUniqueUserIdEnabled(manager)) {
            ((AbstractUserStoreManager) manager).doUpdateUserListOfRoleWithID(roleName,
                    getUserIDsFromUserNames(Arrays.asList(deletedUsers)).toArray(new String[0]),
                    getUserIDsFromUserNames(Arrays.asList(newUsers)).toArray(new String[0]));
        } else {
            ((AbstractUserStoreManager) manager).doUpdateUserListOfRole(roleName, deletedUsers, newUsers);
        }
    }

    /**
     * TODO move to API
     *
     * @return
     * @throws UserStoreException
     */
    public String[] getSharedRolesOfUser(String userName, String tenantDomain, String filter)
            throws UserStoreException {

        UserStore userStore = getUserStore(userName);
        UserStoreManager manager = userStore.getUserStoreManager();

        if (!((AbstractUserStoreManager) manager).isSharedGroupEnabled()) {
            throw new UserStoreException("Share Groups are not supported by user store");
        }
        String[] sharedRoles;
        if (((AbstractUserStoreManager) manager).isUniqueUserIdEnabled()) {
            sharedRoles = ((AbstractUserStoreManager) manager).
                    doGetSharedRoleListOfUserWithID(getUserIDFromUserName(userStore.getDomainFreeName()), tenantDomain,
                            filter);
        } else {
            sharedRoles = ((AbstractUserStoreManager) manager).
                    doGetSharedRoleListOfUser(userStore.getDomainFreeName(), tenantDomain, filter);
        }
        return UserCoreUtil.removeDomainFromNames(sharedRoles);
    }

    /**
     * TODO move to API
     *
     * @return
     * @throws UserStoreException
     */
    public String[] getUsersOfSharedRole(String roleName, String filter) throws UserStoreException {

        UserStoreManager manager = getUserStoreWithSharedRoles();

        if (manager == null) {
            throw new UserStoreException("Share Groups are not supported by this realm");
        }

        if (isUniqueUserIdEnabled(manager)) {
            List<User> users = ((AbstractUserStoreManager) manager).doGetUserListOfRoleWithID(roleName, filter);
            return users.stream().map(User::getDomainQualifiedUsername).toArray(String[]::new);
        } else {
            String[] users = ((AbstractUserStoreManager) manager).doGetUserListOfRole(roleName, filter);
            return UserCoreUtil.removeDomainFromNames(users);
        }
    }

    /**
     * TODO move to API
     *
     * @return
     * @throws UserStoreException
     */
    public String[] getSharedRoleNames(String tenantDomain, String filter,
                                       int maxItemLimit) throws UserStoreException {


        UserStoreManager manager = getUserStoreWithSharedRoles();

        if (manager == null) {
            throw new UserStoreException("Share Groups are not supported by this realm");
        }

        String[] sharedRoles = null;
        try {
            sharedRoles = ((AbstractUserStoreManager) manager).
                    doGetSharedRoleNames(tenantDomain, filter, maxItemLimit);
        } catch (UserStoreException e) {
            throw new UserStoreException("Error while retrieving shared roles", e);
        }
        return UserCoreUtil.removeDomainFromNames(sharedRoles);
    }


    /**
     * TODO move to API
     *
     * @return
     * @throws UserStoreException
     */
    public String[] getSharedRoleNames(String filter, int maxItemLimit) throws UserStoreException {

        UserStoreManager manager = getUserStoreWithSharedRoles();

        if (manager == null) {
            throw new UserStoreException("Share Groups are not supported by this realm");
        }

        String[] sharedRoles = null;
        try {
            sharedRoles = ((AbstractUserStoreManager) manager).
                    doGetSharedRoleNames(null, filter, maxItemLimit);
        } catch (UserStoreException e) {
            throw new UserStoreException("Error while retrieving shared roles", e);
        }
        return UserCoreUtil.removeDomainFromNames(sharedRoles);
    }


    public void addInternalRole(String roleName, String[] userList,
                                org.wso2.carbon.user.api.Permission[] permission) throws UserStoreException {
        doAddInternalRole(roleName, userList, permission);
    }

    private UserStoreManager getUserStoreWithSharedRoles() throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{};
            Object object = callSecure("getUserStoreWithSharedRoles", new Object[]{}, argTypes);
            return (UserStoreManager) object;
        }

        UserStoreManager sharedRoleManager = null;

        if (isSharedGroupEnabled()) {
            return this;
        }

        for (Map.Entry<String, UserStoreManager> entry : userStoreManagerHolder.entrySet()) {
            UserStoreManager manager = entry.getValue();
            if (manager != null && ((AbstractUserStoreManager) manager).isSharedGroupEnabled()) {
                if (sharedRoleManager != null) {
                    throw new UserStoreException("There can not be more than one user store that support" +
                            "shared groups");
                }
                sharedRoleManager = manager;
            }
        }

        return sharedRoleManager;
    }

    /**
     * TODO move to API
     *
     * @param userName
     * @param roleName
     * @return
     * @throws UserStoreException
     */
    public boolean isUserInRole(String userName, String roleName) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, String.class};
            Object object = callSecure("isUserInRole", new Object[]{userName, roleName}, argTypes);
            return (Boolean) object;
        }

        if (roleName == null || roleName.trim().length() == 0 || userName == null ||
                userName.trim().length() == 0) {
            return false;
        }

        // anonymous user is always assigned to  anonymous role
        if (CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME.equalsIgnoreCase(roleName) &&
                CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equalsIgnoreCase(userName)) {
            return true;
        }

        if (!CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equalsIgnoreCase(userName) &&
                realmConfig.getEveryOneRoleName().equalsIgnoreCase(roleName) &&
                !systemUserRoleManager.isExistingSystemUser(UserCoreUtil.
                        removeDomainFromName(userName))) {
            return true;
        }


        String[] roles = null;

        roles = getRoleListOfUserFromCache(tenantId, userName);
        if (roles != null && roles.length > 0) {
            if (UserCoreUtil.isContain(roleName, roles)) {
                return true;
            }
        }

        // TODO create new cache for this method
        String modifiedUserName = UserCoreConstants.IS_USER_IN_ROLE_CACHE_IDENTIFIER + userName;
        roles = getRoleListOfUserFromCache(tenantId, modifiedUserName);
        if (roles != null && roles.length > 0) {
            if (UserCoreUtil.isContain(roleName, roles)) {
                return true;
            }
        }

        if (UserCoreConstants.INTERNAL_DOMAIN.
                equalsIgnoreCase(UserCoreUtil.extractDomainFromName(roleName))
                || APPLICATION_DOMAIN.equalsIgnoreCase(UserCoreUtil.extractDomainFromName(roleName)) ||
                WORKFLOW_DOMAIN.equalsIgnoreCase(UserCoreUtil.extractDomainFromName(roleName))) {

            String[] internalRoles = doGetInternalRoleListOfUser(userName, roleName);
            if (UserCoreUtil.isContain(roleName, internalRoles)) {
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

        if (userStore.isSystemStore()) {
            return systemUserRoleManager.isUserInRole(userStore.getDomainFreeName(),
                    UserCoreUtil.removeDomainFromName(roleName));
        }
        // admin user is always assigned to admin role if it is in primary user store
        if (realmConfig.isPrimary() && roleName.equalsIgnoreCase(realmConfig.getAdminRoleName()) &&
                userName.equalsIgnoreCase(realmConfig.getAdminUserName())) {
            return true;
        }

        String roleDomainName = UserCoreUtil.extractDomainFromName(roleName);

        String roleDomainNameForForest = realmConfig.
                getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_GROUP_SEARCH_DOMAINS);
        if (roleDomainNameForForest != null && roleDomainNameForForest.trim().length() > 0) {
            String[] values = roleDomainNameForForest.split("#");
            for (String value : values) {
                if (value != null && !value.trim().equalsIgnoreCase(roleDomainName)) {
                    return false;
                }
            }
        } else if (!userStore.getDomainName().equalsIgnoreCase(roleDomainName) && !UserCoreConstants.INTERNAL_DOMAIN.
                equalsIgnoreCase(roleDomainName)) {
            return false;
        }

        boolean success = false;
        if (readGroupsEnabled) {
            if (isUniqueUserIdEnabledInUserStore(userStore)) {
                success = doCheckIsUserInRoleWithID(getUserIDFromUserName(userName),
                        UserCoreUtil.removeDomainFromName(roleName));
            } else {
                success = doCheckIsUserInRole(userStore.getDomainFreeName(),
                        UserCoreUtil.removeDomainFromName(roleName));
            }

            if (isRoleAndGroupSeparationEnabled()) {
                String[] rolesList;
                if (isUniqueUserIdEnabledInUserStore(userStore)) {
                    rolesList = doGetExternalRoleListOfUserWithID(getUserIDFromUserName(userName), "*");
                } else {
                    rolesList = doGetExternalRoleListOfUser(userName, "*");
                }

                Map<String, List<String>> rolesOfGroups = getHybridRoleListOfGroups(Arrays.asList(rolesList),
                        userStore.getDomainName());
                Set<String> roleListOfGroups = getUniqueSet(rolesOfGroups);
                if (roleListOfGroups.stream().anyMatch(roleName::equalsIgnoreCase)) {
                    success = true;
                }
            }
        }

        // add to cache
        if (success) {
            addToIsUserHasRole(modifiedUserName, roleName, roles);
        }
        return success;
    }

    /**
     * Check whether the group name existing in the user store.
     *
     * @param groupName Group name.
     * @return True if the group name exists.
     * @throws UserStoreException If an error occurred.
     */
    protected boolean doCheckExistingGroupName(String groupName) throws UserStoreException {

        // Default implementation to ensure backward compatibility.
        return doCheckExistingRole(UserCoreUtil.removeDomainFromName(groupName));
    }

    /**
     * @param userName
     * @param roleName
     * @return
     * @throws UserStoreException
     */
    public abstract boolean doCheckIsUserInRole(String userName, String roleName) throws UserStoreException;

    /**
     * @param userID   user ID.
     * @param roleName role name.
     * @return true if user uis in the given role.
     * @throws UserStoreException An unexpected exception has occurred.
     */
    public boolean doCheckIsUserInRoleWithID(String userID, String roleName) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doCheckIsUserInRoleWithID operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException(
                "doCheckIsUserInRoleWithID operation is not implemented in: " + this.getClass());
    }

    /**
     * Helper method
     *
     * @param userName
     * @param roleName
     * @param currentRoles
     */
    private void addToIsUserHasRole(String userName, String roleName, String[] currentRoles) {
        List<String> roles;
        if (currentRoles != null) {
            roles = new ArrayList<>(Arrays.asList(currentRoles));
        } else {
            roles = new ArrayList<>();
        }
        roles.add(roleName);
        addToUserRolesCache(tenantId, userName, roles.toArray(new String[0]));
    }

//////////////////////////////////// Shared role APIs finish //////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public boolean isExistingUser(String userName) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class[] argTypes = new Class[]{String.class};
            Object object = callSecure("isExistingUser", new Object[]{userName}, argTypes);
            return (Boolean) object;
        }

        if (UserCoreUtil.isRegistrySystemUser(userName)) {
            return true;
        }

        UserStore userStore = getUserStore(userName);
        if (userStore.isRecurssive()) {
            return userStore.getUserStoreManager().isExistingUser(userStore.getDomainFreeName());
        }

        // #################### Domain Name Free Zone Starts Here ################################

        /* Validate whether circuit breaker is enabled and open for compatibility with old pre-listeners. */
        if (((AbstractUserStoreManager) userStore.getUserStoreManager()).isCircuitBreakerEnabledAndOpen()) {
            if (log.isDebugEnabled()) {
                log.debug("Avoiding user listing as the Circuit Breaker is in open state for domain: "
                        + userStore.getDomainName());
            }
        } else {
            if (userStore.isSystemStore()) {
                return systemUserRoleManager.isExistingSystemUser(userStore.getDomainFreeName());
            }

            if (!isUniqueUserIdEnabledInUserStore(userStore)) {
                return doCheckExistingUser(userStore.getDomainFreeName());
            } else {
                return getUserIDFromUserName(userName) != null;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public final String[] listUsers(String filter, int maxItemLimit) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class[] argTypes = new Class[]{String.class, int.class};
            Object object = callSecure("listUsers", new Object[]{filter, maxItemLimit}, argTypes);
            return (String[]) object;
        }

        int index;
        index = filter.indexOf(CarbonConstants.DOMAIN_SEPARATOR);
        String[] userList;

        try {
            // Validate whether circuit breaker is enabled and open.
            if (this.isCircuitBreakerEnabledAndOpen()) {
                if (log.isDebugEnabled()) {
                    log.debug("Circuit Breaker is in open state for " + this.getMyDomainName()
                            + " domain. Hence ignore the userstore and proceed");
                }
                return new String[0];
            }
            // Check whether we have a secondary UserStoreManager setup.
            if (index > 0) {
                // Using the short-circuit. User name comes with the domain name.
                String domain = filter.substring(0, index);

                UserStoreManager secManager = this;
                if (!StringUtils.equalsIgnoreCase(getMyDomainName(), domain)) {
                    secManager = getSecondaryUserStoreManager(domain);
                }

                if (secManager != null) {
                    // We have a secondary UserStoreManager registered for this domain.
                    filter = filter.substring(index + 1);
                    if (secManager instanceof AbstractUserStoreManager) {

                        if (!((AbstractUserStoreManager) secManager).isUniqueUserIdEnabled()) {
                            userList = ((AbstractUserStoreManager) secManager).doListUsers(filter, maxItemLimit);
                        } else {
                            userList = ((AbstractUserStoreManager) secManager).doListUsersWithID(filter, maxItemLimit)
                                    .stream()
                                    .map(User::getDomainQualifiedUsername)
                                    .toArray(String[]::new);
                        }
                        handlePostGetUserList(null, null, new ArrayList<>(Arrays.asList(userList)), true);
                        return userList;
                    } else {
                        userList = secManager.listUsers(filter, maxItemLimit);
                        handlePostGetUserList(null, null, new ArrayList<>(Arrays.asList(userList)), true);
                        return userList;
                    }
                }
            } else if (index == 0) {
                if (!isUniqueUserIdEnabled()) {
                    userList = doListUsers(filter.substring(1), maxItemLimit);
                } else {
                    userList = doListUsersWithID(filter.substring(1), maxItemLimit)
                            .stream()
                            .map(User::getDomainQualifiedUsername)
                            .toArray(String[]::new);
                }
                handlePostGetUserList(null, null, new ArrayList<>(Arrays.asList(userList)), true);
                return userList;
            }

            if (!isUniqueUserIdEnabled()) {
                userList = doListUsers(filter, maxItemLimit);
            } else {
                userList = doListUsersWithID(filter, maxItemLimit)
                        .stream()
                        .map(User::getUsername)
                        .toArray(String[]::new);
            }
        } catch (CircuitBreakerOpenException ex) {
            if (log.isDebugEnabled()) {
                log.debug("Circuit Breaker is in open state for " + this.getMyDomainName()
                        + " domain. Hence ignore " + "the userstore and proceed", ex);
            }
            log.error("Error occurred while obtaining user store connection.");
            return new String[0];
        } catch (UserStoreException ex) {
            handleGetUserListFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_USER_LIST.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_USER_LIST.getMessage(), ex.getMessage()),
                    null, null, null);
            throw ex;
        }

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
                        String[] secondUserList;
                        if (!((AbstractUserStoreManager) storeManager).isUniqueUserIdEnabled()) {
                            secondUserList = ((AbstractUserStoreManager) storeManager)
                                    .doListUsers(filter, maxItemLimit);
                        } else {
                            secondUserList = ((AbstractUserStoreManager) storeManager)
                                    .doListUsersWithID(filter, maxItemLimit)
                                    .stream()
                                    .map(User::getDomainQualifiedUsername)
                                    .toArray(String[]::new);
                        }
                        userList = UserCoreUtil.combineArrays(userList, secondUserList);
                    } catch (UserStoreException ex) {
                        handleGetUserListFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_USER_LIST.getCode(),
                                String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_USER_LIST.getMessage(),
                                        ex.getMessage()), null, null, null);

                        // We can ignore and proceed. Ignore the results from this user store.
                        log.error(ex);
                    }
                } else {
                    String[] secondUserList = storeManager.listUsers(filter, maxItemLimit);
                    userList = UserCoreUtil.combineArrays(userList, secondUserList);
                }
            }
        }

        handlePostGetUserList(null, null, new ArrayList<>(Arrays.asList(userList)), true);
        return userList;
    }

    /**
     * Count roles in user stores
     *
     * @param filter The string to filter out roles
     * @return countRoles
     * @throws UserStoreException UserStoreException
     */
    public long countRoles(String filter) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class[] argTypes = new Class[]{String.class};
            Object object = callSecure("countRoles", new Object[]{filter}, argTypes);
            return (long) object;
        }

        int index;
        index = filter.indexOf(CarbonConstants.DOMAIN_SEPARATOR);

        // Check whether we have a secondary UserStoreManager setup.
        if (index > 0) {
            String domain = filter.substring(0, index);
            if (isInternalRole(domain)) {
                if (log.isDebugEnabled()) {
                    log.debug("Internal domain is provided. Thus calling the hybrid role manager to get the " +
                            "internal role count.");
                }
                return this.hybridRoleManager.countHybridRoles(filter);
            }
            filter = filter.substring(index + 1);
            UserStoreManager secondaryUserStoreManager = getSecondaryUserStoreManager(domain);
            if (secondaryUserStoreManager != null) {
                // We have a secondary UserStoreManager registered for this domain.
                if (secondaryUserStoreManager instanceof AbstractUserStoreManager) {
                    return ((AbstractUserStoreManager) secondaryUserStoreManager).doCountRoles(filter);
                } else {
                    throw new UserStoreException("User store not supported");
                }
            }
        } else if (index == 0) {
            return doCountRoles(filter.substring(1));
        }
        return doCountRoles(filter);
    }

    /**
     * Count Claims in user stores
     *
     * @return claim count value
     * @throws UserStoreException
     */
    public final long countUsersWithClaims(String claimURI, String valueFilter) throws UserStoreException {

        return doCountUsersWithClaims(claimURI, valueFilter);
    }

    /**
     * This is to call the relevant post methods in listeners after successful retrieval of user list of a role.
     *
     * @param roleName Name of the role.
     * @param userList List of users.
     * @throws UserStoreException User Store Exception.
     */
    private void handleDoPostGetUserListOfRoleWithID(String roleName, List<User> userList) throws UserStoreException {

        for (UserOperationEventListener userOperationEventListener : UMListenerServiceComponent
                .getUserOperationEventListeners()) {
            if (userOperationEventListener instanceof AbstractUserOperationEventListener) {
                if (!((AbstractUserOperationEventListener) userOperationEventListener)
                        .doPostGetUserListOfRoleWithID(roleName, userList, this)) {
                    return;
                }
            }
        }
    }

    /**
     * This is to call the relevant post methods in listeners after successful retrieval of user list of a role.
     *
     * @param roleName Name of the role.
     * @param userList List of users.
     * @throws UserStoreException User Store Exception.
     */
    private void handleDoPostGetUserListOfRole(String roleName, String[] userList) throws UserStoreException {

        for (UserOperationEventListener userOperationEventListener : UMListenerServiceComponent
                .getUserOperationEventListeners()) {
            if (userOperationEventListener instanceof AbstractUserOperationEventListener) {
                if (!((AbstractUserOperationEventListener) userOperationEventListener)
                        .doPostGetUserListOfRole(roleName, userList, this)) {
                    return;
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public final String[] getUserListOfRole(String roleName) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class};
            Object object = callSecure("getUserListOfRole", new Object[]{roleName}, argTypes);
            return (String[]) object;
        }

        return getUserListOfRole(roleName, QUERY_FILTER_STRING_ANY, QUERY_MAX_ITEM_LIMIT_ANY);
    }

    /**
     * Retrieves a list of user names belongs to the given role and matches the given string filter.
     *
     * @param roleName Name of the role.
     * @param filter The string to filter out names of users belong to the given role.
     * @param maxItemLimit Maximum number of users returned.
     * @return User name list.
     * @throws UserStoreException
     */
    public final String[] getUserListOfRole(String roleName, String filter, int maxItemLimit) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, String.class, int.class};
            Object object = callSecure("getUserListOfRole", new Object[]{roleName, filter, maxItemLimit}, argTypes);
            return (String[]) object;
        }

        String[] userNames = new String[0];

        // If role does not exit, just return
        if (!isExistingRole(roleName)) {
            handleDoPostGetUserListOfRole(roleName, userNames);
            return userNames;
        }

        UserStore userStore = getUserStoreOfRoles(roleName);

        if (userStore.isRecurssive()) {
            UserStoreManager resolvedUserStoreManager = userStore.getUserStoreManager();
            if (resolvedUserStoreManager instanceof AbstractUserStoreManager) {
                return ((AbstractUserStoreManager) resolvedUserStoreManager)
                        .getUserListOfRole(userStore.getDomainFreeName(), filter, maxItemLimit);
            } else {
                return resolvedUserStoreManager.getUserListOfRole(userStore.getDomainFreeName());
            }
        }


        // #################### Domain Name Free Zone Starts Here
        // ################################

        if (userStore.isSystemStore()) {
            String[] userList = systemUserRoleManager.getUserListOfSystemRole(userStore.getDomainFreeName());
            handleDoPostGetUserListOfRole(roleName, userList);
            return userList;
        }

        String[] userNamesInHybrid;
        if (userStore.isHybridRole()) {
            if (UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(userStore.getDomainName())) {
                userNamesInHybrid = hybridRoleManager.getUserListOfHybridRole(userStore.getDomainFreeName());
            } else {
                userNamesInHybrid = hybridRoleManager.getUserListOfHybridRole(userStore.getDomainAwareName());
            }

            // Get the users of associated groups of the role.
            if (isRoleAndGroupSeparationEnabled()) {
                Set<String> userListOfGroups = new HashSet<>();
                String[] groupsOfRole;
                if (UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(userStore.getDomainName())) {
                    groupsOfRole = hybridRoleManager.getGroupListOfHybridRole(userStore.getDomainFreeName());
                } else {
                    groupsOfRole = hybridRoleManager.getGroupListOfHybridRole(userStore.getDomainAwareName());
                }
                for (String group : groupsOfRole) {
                    userListOfGroups.addAll(Arrays.asList(getUserListOfRole(group, filter, maxItemLimit)));
                }
                userNamesInHybrid = UserCoreUtil.combine(userNamesInHybrid, new ArrayList<>(userListOfGroups));
            }

            // remove domain
            List<String> finalNameList = new ArrayList<>();
            String displayNameAttribute = this.realmConfig.getUserStoreProperty(LDAPConstants.DISPLAY_NAME_ATTRIBUTE);

            if (userNamesInHybrid != null && userNamesInHybrid.length > 0) {
                if (displayNameAttribute != null && displayNameAttribute.trim().length() > 0) {
                    for (String userName : userNamesInHybrid) {
                        String domainName = UserCoreUtil.extractDomainFromName(userName);
                        if (domainName == null || domainName.trim().length() == 0) {
                            finalNameList.add(userName);
                        }
                        UserStoreManager userManager = userStoreManagerHolder.get(domainName);
                        userName = UserCoreUtil.removeDomainFromName(userName);
                        if (userManager != null) {
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
                    handleDoPostGetUserListOfRole(roleName, userNamesInHybrid);
                    return userNamesInHybrid;
                }
            }
            String[] userList = finalNameList.toArray(new String[0]);
            handleDoPostGetUserListOfRole(roleName, userList);
            return userList;
        }

        if (readGroupsEnabled) {
            if (isUniqueUserIdEnabledInUserStore(userStore)) {
                List<User> users = doGetUserListOfRoleWithID(roleName, filter, maxItemLimit);
                userNames = users.stream().map(User::getDomainQualifiedUsername).toArray(String[]::new);
            } else {
                userNames = doGetUserListOfRole(roleName, filter, maxItemLimit);
            }
            handleDoPostGetUserListOfRole(roleName, userNames);
        }
        return userNames;

    }

    public String[] getRoleListOfUser(String userName) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class};
            Object object = callSecure("getRoleListOfUser", new Object[]{userName}, argTypes);
            return (String[]) object;
        }

        String[] roleNames;
        String userID;

        // anonymous user is only assigned to  anonymous role
        if (CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equalsIgnoreCase(userName)) {
            return new String[]{CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME};
        }

        UserStore userStore = getUserStore(userName);
        if (userStore.isRecurssive()) {
            return userStore.getUserStoreManager().getRoleListOfUser(userStore.getDomainFreeName());
        }

        roleNames = getRoleListOfUserFromCache(this.tenantId, userName);

        if (roleNames != null && roleNames.length > 0) {
            return roleNames;
        }

        if (userStore.isSystemStore()) {
            return systemUserRoleManager.getSystemRoleListOfUser(userStore.getDomainFreeName());
        }
        // #################### Domain Name Free Zone Starts Here ################################

        if (isUniqueUserIdEnabledInUserStore(userStore)) {
            userID = getUserIDFromUserName(userName);
            if (userID == null) {
                // According to implementation, getRoleListOfUser method would return everyone role name for all users.
                return new String[]{realmConfig.getEveryOneRoleName()};
            }
            roleNames = doGetRoleListOfUserWithID(userID, "*").toArray(new String[0]);
        } else {
            roleNames = doGetRoleListOfUser(userName, "*");
        }
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
     * This method is responsible for calling relevant listener methods when there is a failure while trying to add
     * role.
     *
     * @param errorCode    Error code.
     * @param errorMessage Error message.
     * @param roleName     Name of the role.
     * @param userList     List of users to be assigned to the role.
     * @param permissions  Permissions of the role role.
     * @throws UserStoreException Exception that will be thrown by relevant listeners.
     */
    private void handleAddRoleFailure(String errorCode, String errorMessage, String roleName, String[] userList,
            org.wso2.carbon.user.api.Permission[] permissions) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !listener
                    .onAddRoleFailure(errorCode, errorMessage, roleName, userList, permissions, this)) {
                return;
            }
        }
    }

    /**
     * This method is responsible for calling relevant listener methods when there is a failure while trying to add
     * role.
     *
     * @param errorCode    Error code.
     * @param errorMessage Error message.
     * @param roleName     Name of the role.
     * @param userIDList     List of users to be assigned to the role.
     * @param permissions  Permissions of the role role.
     * @throws UserStoreException Exception that will be thrown by relevant listeners.
     */
    private void handleAddRoleFailureWithID(String errorCode, String errorMessage, String roleName,
                                            String[] userIDList,  org.wso2.carbon.user.api.Permission[]
                                                    permissions) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !((UniqueIDUserManagementErrorEventListener) listener)
                    .onAddRoleFailureWithID(errorCode, errorMessage, roleName, userIDList, permissions, this)) {
                return;
            }
        }
    }

    /**
     * This method is responsible for calling relevant listener methods when there is a failure while trying to add a
     * group.
     *
     * @param errorCode    Error code.
     * @param errorMessage Error message.
     * @param groupName    Name of the group.
     * @param groupId      ID of the group.
     * @param usersIDs     List of users to be assigned to the group.
     * @param claims       Claims of the group.
     * @throws UserStoreException Exception that will be thrown by relevant listeners.
     */
    private void handleAddGroupFailure(String errorCode, String errorMessage, String groupName, String groupId,
                                       List<String> usersIDs, List<org.wso2.carbon.user.core.common.Claim> claims)
            throws UserStoreException {

        for (GroupManagementErrorEventListener listener : UMListenerServiceComponent
                .getGroupManagementErrorEventListeners()) {
            if (listener.isEnable() && listener instanceof AbstractGroupManagementErrorEventListener
                    && !listener.onAddGroupFailure(errorCode, errorMessage, groupName, usersIDs, claims,
                    this)) {
                log.error("'onAddGroupFailure' event invocation failed for listener: " +
                        listener.getClass().getName());
                return;
            }
        }
    }

    /**
     * This method is responsible for calling relevant listener methods when there is a failure while trying to
     * pre add a group.
     *
     * @param errorCode    Error code.
     * @param errorMessage Error message.
     * @param groupName    Name of the group.
     * @param usersIDs     List of users to be assigned to the group.
     * @param claims       Claims of the group.
     * @throws UserStoreException Exception that will be thrown by relevant listeners.
     */
    private void handlePreAddGroupFailure(String errorCode, String errorMessage, String groupName,
                                          List<String> usersIDs, List<org.wso2.carbon.user.core.common.Claim> claims)
            throws UserStoreException {

        for (GroupManagementErrorEventListener listener : UMListenerServiceComponent
                .getGroupManagementErrorEventListeners()) {
            if (listener.isEnable() && listener instanceof AbstractGroupManagementErrorEventListener
                    && !listener.onPreAddGroupFailure(errorCode, errorMessage, groupName, usersIDs, claims,
                    this)) {
                log.error("'onPreAddGroupFailure' event invocation failed for listener: " +
                        listener.getClass().getName());
                return;
            }
        }
    }

    /**
     * This method is responsible for calling relevant listener methods when there is a failure while trying to post
     * delete a group.
     *
     * @param errorCode    Error code.
     * @param errorMessage Error message.
     * @param groupName    Name of the group.
     * @param groupID      ID of the group.
     * @param usersIDs     List of users to be assigned to the group.
     * @param claims       Claims of the group.
     * @throws UserStoreException Exception that will be thrown by relevant listeners.
     */
    private void handlePostAddGroupFailure(String errorCode, String errorMessage, String groupName, String groupID,
                                           List<String> usersIDs, List<org.wso2.carbon.user.core.common.Claim> claims)
            throws UserStoreException {

        for (GroupManagementErrorEventListener listener : UMListenerServiceComponent
                .getGroupManagementErrorEventListeners()) {
            if (listener.isEnable() && listener instanceof AbstractGroupManagementErrorEventListener &&
                    !listener.onPostAddGroupFailure(errorCode, errorMessage, groupName, groupID, usersIDs, claims,
                    this)) {
                log.error("'onPostAddGroupFailure' event invocation failed for listener: " +
                        listener.getClass().getName());
                return;
            }
        }
    }

    /**
     * This method is responsible for calling relevant listener methods deleting a group.
     *
     * @param groupId ID of the group.
     * @return True if the group is operation is successful.
     * @throws UserStoreException Exception that will be thrown by relevant listeners.
     */
    private boolean handlePreDeleteGroup(String groupId) throws UserStoreException {

        try {
            for (GroupOperationEventListener listener : UMListenerServiceComponent
                    .getGroupOperationEventListeners()) {
                if (listener instanceof AbstractGroupOperationEventListener) {
                    AbstractGroupOperationEventListener newListener = (AbstractGroupOperationEventListener) listener;
                    if (!newListener.preDeleteGroup(groupId, this)) {
                        handlePreDeleteGroupFailure(ErrorMessages.ERROR_DURING_PRE_DELETE_GROUP.getCode(),
                                String.format(ErrorMessages.ERROR_DURING_PRE_DELETE_GROUP.getMessage(),
                                        UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), groupId);
                        return false;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleDeleteGroupFailure(ErrorMessages.ERROR_DURING_PRE_DELETE_GROUP.getCode(),
                    String.format(ErrorMessages.ERROR_DURING_PRE_DELETE_GROUP.getMessage(), ex.getMessage()), groupId);
            throw ex;
        }
        return true;
    }

    /**
     * This method is responsible for calling relevant listener methods after deleting a group.
     *
     * @param groupId ID of the group.
     * @throws UserStoreException Exception that will be thrown by relevant listeners.
     */
    private void handlePostDeleteGroup(String groupId, String groupName) throws UserStoreException {

        try {
            for (GroupOperationEventListener listener : UMListenerServiceComponent
                    .getGroupOperationEventListeners()) {
                if (listener instanceof AbstractGroupOperationEventListener) {
                    AbstractGroupOperationEventListener newListener = (AbstractGroupOperationEventListener) listener;
                    if (!newListener.postDeleteGroup(groupId, groupName, this)) {
                        handlePostDeleteGroupFailure(ErrorMessages.ERROR_DURING_POST_DELETE_GROUP.getCode(),
                                String.format(ErrorMessages.ERROR_DURING_POST_DELETE_GROUP.getMessage(),
                                        UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), groupId, groupName);
                        return;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleDeleteGroupFailure(ErrorMessages.ERROR_DURING_POST_DELETE_GROUP.getCode(),
                    String.format(ErrorMessages.ERROR_DURING_POST_DELETE_GROUP.getMessage(), ex.getMessage()),
                    groupName);
            throw ex;
        }
    }

    /**
     * This method is responsible for calling relevant listener methods when there is a failure while trying to
     * pre delete a group.
     *
     * @param errorCode    Error code.
     * @param errorMessage Error message.
     * @param groupId      ID of the group.
     * @throws UserStoreException Exception that will be thrown by relevant listeners.
     */
    private void handlePreDeleteGroupFailure(String errorCode, String errorMessage, String groupId)
            throws UserStoreException {

        for (GroupManagementErrorEventListener listener : UMListenerServiceComponent
                .getGroupManagementErrorEventListeners()) {
            if (listener.isEnable() && listener instanceof AbstractGroupManagementErrorEventListener
                    && !listener.onPreDeleteGroupFailure(errorCode, errorMessage, groupId, this)) {
                log.error("'onPreDeleteGroupFailure' event invocation failed for listener: " +
                        listener.getClass().getName());
                return;
            }
        }
    }

    /**
     * This method is responsible for calling relevant listener methods when there is a failure while trying to
     * post delete a group.
     *
     * @param errorCode    Error code.
     * @param errorMessage Error message.
     * @param groupId      ID of the group.
     * @param groupName    Name of the group.
     * @throws UserStoreException Exception that will be thrown by relevant listeners.
     */
    private void handlePostDeleteGroupFailure(String errorCode, String errorMessage, String groupId, String groupName)
            throws UserStoreException {

        for (GroupManagementErrorEventListener listener : UMListenerServiceComponent
                .getGroupManagementErrorEventListeners()) {
            if (listener.isEnable() && listener instanceof AbstractGroupManagementErrorEventListener
                    && !listener.onPostDeleteGroupFailure(errorCode, errorMessage, groupId, groupName, this)) {
                log.error("'onPostDeleteGroupFailure' event invocation failed for listener: " +
                        listener.getClass().getName());
                return;
            }
        }
    }

    /**
     * This method is responsible for calling relevant listener methods when there is a failure while trying to
     * delete a group.
     *
     * @param errorCode    Error code.
     * @param errorMessage Error message.
     * @param groupId      ID of the group.
     * @throws UserStoreException Exception that will be thrown by relevant listeners.
     */
    private void handleDeleteGroupFailure(String errorCode, String errorMessage, String groupId)
            throws UserStoreException {

        for (GroupManagementErrorEventListener listener : UMListenerServiceComponent
                .getGroupManagementErrorEventListeners()) {
            if (listener.isEnable() && listener instanceof AbstractGroupManagementErrorEventListener
                    && !listener.onDeleteGroupFailure(errorCode, errorMessage, groupId, this)) {
                log.error("'onDeleteGroupFailure' event invocation failed for listener: " +
                        listener.getClass().getName());
                return;
            }
        }
    }

    /**
     * This method is responsible for calling relevant listener methods deleting a group.
     *
     * @param groupId      ID of the group.
     * @param newGroupName Name of the group.
     * @return True if the group is operation is successful.
     * @throws UserStoreException Exception that will be thrown by relevant listeners.
     */
    private boolean handlePreRenameGroup(String groupId, String newGroupName) throws UserStoreException {

        try {
            for (GroupOperationEventListener listener : UMListenerServiceComponent
                    .getGroupOperationEventListeners()) {
                if (listener instanceof AbstractGroupOperationEventListener) {
                    AbstractGroupOperationEventListener newListener = (AbstractGroupOperationEventListener) listener;
                    if (!newListener.preRenameGroup(groupId, newGroupName, this)) {
                        handlePreRenameGroupFailure(ErrorMessages.ERROR_DURING_PRE_RENAME_GROUP.getCode(),
                                String.format(ErrorMessages.ERROR_DURING_PRE_RENAME_GROUP.getMessage(),
                                        UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE),
                                groupId, newGroupName);
                        return false;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleRenameGroupFailure(ErrorMessages.ERROR_DURING_PRE_RENAME_GROUP.getCode(),
                    String.format(ErrorMessages.ERROR_DURING_PRE_RENAME_GROUP.getMessage(), ex.getMessage()),
                    groupId, newGroupName);
            throw ex;
        }
        return true;
    }

    /**
     * This method is responsible for calling relevant listener methods after deleting a group.
     *
     * @param groupId      ID of the group.
     * @param newGroupName New name of the group.
     * @throws UserStoreException Exception that will be thrown by relevant listeners.
     */
    private void handlePostRenameGroup(String groupId, String newGroupName) throws UserStoreException {

        try {
            for (GroupOperationEventListener listener : UMListenerServiceComponent
                    .getGroupOperationEventListeners()) {
                if (listener instanceof AbstractGroupOperationEventListener) {
                    AbstractGroupOperationEventListener newListener = (AbstractGroupOperationEventListener) listener;
                    if (!newListener.postRenameGroup(groupId, newGroupName, this)) {
                        handlePostRenameGroupFailure(ErrorMessages.ERROR_DURING_POST_RENAME_GROUP.getCode(),
                                String.format(ErrorMessages.ERROR_DURING_POST_RENAME_GROUP.getMessage(),
                                        UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE),
                                groupId, newGroupName);
                        return;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleRenameGroupFailure(ErrorMessages.ERROR_DURING_POST_RENAME_GROUP.getCode(),
                    String.format(ErrorMessages.ERROR_DURING_POST_RENAME_GROUP.getMessage(), ex.getMessage()), groupId,
                    newGroupName);
            throw ex;
        }
    }

    /**
     * This method is responsible for calling relevant listener methods when there is a failure while trying to
     * pre rename a group.
     *
     * @param errorCode    Error code.
     * @param errorMessage Error message.
     * @param groupId      ID of the group.
     * @param newGroupName Name of the group.
     * @throws UserStoreException Exception that will be thrown by relevant listeners.
     */
    private void handlePreRenameGroupFailure(String errorCode, String errorMessage, String groupId, String newGroupName)
            throws UserStoreException {

        for (GroupManagementErrorEventListener listener : UMListenerServiceComponent
                .getGroupManagementErrorEventListeners()) {
            if (listener.isEnable() && listener instanceof AbstractGroupManagementErrorEventListener
                    && !listener.onPreRenameGroupFailure(errorCode, errorMessage, groupId, newGroupName,
                    this)) {
                log.error("'onPreRenameGroupFailure' event invocation failed for listener: " +
                        listener.getClass().getName());
                return;
            }
        }
    }

    /**
     * This method is responsible for calling relevant listener methods when there is a failure while trying to
     * post rename a group.
     *
     * @param errorCode    Error code.
     * @param errorMessage Error message.
     * @param groupId      ID of the group.
     * @param newGroupName Name of the group.
     * @throws UserStoreException Exception that will be thrown by relevant listeners.
     */
    private void handlePostRenameGroupFailure(String errorCode, String errorMessage, String groupId,
                                              String newGroupName)
            throws UserStoreException {

        for (GroupManagementErrorEventListener listener : UMListenerServiceComponent
                .getGroupManagementErrorEventListeners()) {
            if (listener.isEnable() && listener instanceof AbstractGroupManagementErrorEventListener
                    && !listener.onPostRenameGroupFailure(errorCode, errorMessage, groupId, newGroupName,
                    this)) {
                log.error("'handlePostRenameGroupFailure' event invocation failed for listener: " +
                        listener.getClass().getName());
                return;
            }
        }
    }

    /**
     * This method is responsible for calling relevant listener methods when there is a failure while trying to
     * rename a group.
     *
     * @param errorCode    Error code.
     * @param errorMessage Error message.
     * @param groupId      ID of the group.
     * @param newGroupName New name of the group.
     * @throws UserStoreException Exception that will be thrown by relevant listeners.
     */
    private void handleRenameGroupFailure(String errorCode, String errorMessage, String groupId, String newGroupName)
            throws UserStoreException {

        for (GroupManagementErrorEventListener listener : UMListenerServiceComponent
                .getGroupManagementErrorEventListeners()) {
            if (listener.isEnable() && listener instanceof AbstractGroupManagementErrorEventListener
                    && !listener.onRenameGroupFailure(errorCode, errorMessage, groupId, newGroupName,
                    this)) {
                log.error("'onRenameGroupFailure' event invocation failed for listener: " +
                        listener.getClass().getName());
                return;
            }
        }
    }

    /**
     * This method is responsible for calling relevant listener methods before updating user list of a group.
     *
     * @param groupId        ID of the group.
     * @param deletedUserIds List of users to be removed from the group.
     * @param newUserIds     List of users to be added to the group.
     * @throws UserStoreException Exception that will be thrown by relevant listeners.
     */
    private boolean handlePreUpdateUserListOfGroup(String groupId, List<String> deletedUserIds,
                                                   List<String> newUserIds) throws UserStoreException {

        try {
            for (GroupOperationEventListener listener : UMListenerServiceComponent.getGroupOperationEventListeners()) {
                if (listener instanceof AbstractGroupOperationEventListener) {
                    AbstractGroupOperationEventListener newListener = (AbstractGroupOperationEventListener) listener;
                    if (!newListener.preUpdateUserListOfGroup(groupId, deletedUserIds, newUserIds,
                            this)) {
                        handlePreUpdateUserListOfGroupFailure(
                                ErrorMessages.ERROR_DURING_PRE_UPDATE_USER_LIST_OF_GROUP.getCode(),
                                String.format(ErrorMessages.ERROR_DURING_PRE_UPDATE_USER_LIST_OF_GROUP.getMessage(),
                                        UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE),
                                groupId, deletedUserIds, newUserIds);
                        return false;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleUpdateUserListOfGroupFailure(ErrorMessages.ERROR_DURING_PRE_UPDATE_USER_LIST_OF_GROUP.getCode(),
                    String.format(ErrorMessages.ERROR_DURING_PRE_UPDATE_USER_LIST_OF_GROUP.getMessage(),
                            ex.getMessage()), groupId, deletedUserIds, newUserIds);
            throw ex;
        }
        return true;
    }

    /**
     * This method is responsible for calling relevant listener methods after updating user list of a group.
     *
     * @param groupId        ID of the group.
     * @param deletedUserIds List of users to be removed from the group.
     * @param newUserIds     List of users to be added to the group.
     * @throws UserStoreException Exception that will be thrown by relevant listeners.
     */
    private void handlePostUpdateUserListOfGroup(String groupId, List<String> deletedUserIds, List<String> newUserIds)
            throws UserStoreException {

        try {
            for (GroupOperationEventListener listener : UMListenerServiceComponent.getGroupOperationEventListeners()) {
                if (listener instanceof AbstractGroupOperationEventListener) {
                    AbstractGroupOperationEventListener newListener = (AbstractGroupOperationEventListener) listener;
                    if (!newListener.postUpdateUserListOfGroup(groupId, deletedUserIds, newUserIds,
                            this)) {
                        handlePostUpdateUserListOfGroupFailure(
                                ErrorMessages.ERROR_DURING_POST_UPDATE_USER_LIST_OF_GROUP.getCode(),
                                String.format(ErrorMessages.ERROR_DURING_POST_UPDATE_USER_LIST_OF_GROUP.getMessage(),
                                        UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE),
                                groupId, deletedUserIds, newUserIds);
                        return;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleUpdateUserListOfGroupFailure(ErrorMessages.ERROR_DURING_POST_UPDATE_USER_LIST_OF_GROUP.getCode(),
                    String.format(ErrorMessages.ERROR_DURING_POST_UPDATE_USER_LIST_OF_GROUP.getMessage(),
                            ex.getMessage()), groupId, deletedUserIds, newUserIds);
            throw ex;
        }
    }

    /**
     * This method is responsible for calling relevant listener methods when there is a failure while trying to
     * pre rename a group.
     *
     * @param errorCode      Error code.
     * @param errorMessage   Error message.
     * @param groupId        ID of the group.
     * @param deletedUserIds List of users to be removed from the group.
     * @param newUserIds     List of users to be added to the group.
     * @throws UserStoreException Exception that will be thrown by relevant listeners.
     */
    private void handlePreUpdateUserListOfGroupFailure(String errorCode, String errorMessage, String groupId,
                                                       List<String> deletedUserIds, List<String> newUserIds)
            throws UserStoreException {

        for (GroupManagementErrorEventListener listener : UMListenerServiceComponent
                .getGroupManagementErrorEventListeners()) {
            if (listener.isEnable() && listener instanceof AbstractGroupManagementErrorEventListener
                    && !listener.onPreUpdateUserListOfGroupFailure(errorCode, errorMessage, groupId, deletedUserIds,
                    newUserIds, this)) {
                log.error("'onPreUpdateUserListOfGroupFailure' event invocation failed for listener: " +
                        listener.getClass().getName());
                return;
            }
        }
    }

    /**
     * This method is responsible for calling relevant listener methods when there is a failure while trying to
     * post rename a group.
     *
     * @param errorCode      Error code.
     * @param errorMessage   Error message.
     * @param groupId        ID of the group.
     * @param deletedUserIds List of users to be removed from the group.
     * @param newUserIds     List of users to be added to the group.
     * @throws UserStoreException Exception that will be thrown by relevant listeners.
     */
    private void handlePostUpdateUserListOfGroupFailure(String errorCode, String errorMessage, String groupId,
                                                        List<String> deletedUserIds, List<String> newUserIds)
            throws UserStoreException {

        for (GroupManagementErrorEventListener listener :
                UMListenerServiceComponent.getGroupManagementErrorEventListeners()) {
            if (listener.isEnable() && listener instanceof AbstractGroupManagementErrorEventListener &&
                    !listener.onPostUpdateUserListOfGroupFailure(errorCode, errorMessage, groupId, deletedUserIds,
                            newUserIds, this)) {
                log.error("'onPostUpdateUserListOfGroupFailure' event invocation failed for listener: " +
                        listener.getClass().getName());
                return;
            }
        }
    }

    /**
     * This method is responsible for calling relevant listener methods when there is a failure while trying to
     * rename a group.
     *
     * @param errorCode      Error code.
     * @param errorMessage   Error message.
     * @param groupId        ID of the group.
     * @param deletedUserIds List of users to be removed from the group.
     * @param newUserIds     List of users to be added to the group.
     * @throws UserStoreException Exception that will be thrown by relevant listeners.
     */
    private void handleUpdateUserListOfGroupFailure(String errorCode, String errorMessage, String groupId,
                                                    List<String> deletedUserIds, List<String> newUserIds)
            throws UserStoreException {

        for (GroupManagementErrorEventListener listener : UMListenerServiceComponent
                .getGroupManagementErrorEventListeners()) {
            if (listener.isEnable() && listener instanceof AbstractGroupManagementErrorEventListener
                    && !listener.onUpdateUserListOfGroupFailure(errorCode, errorMessage, groupId, deletedUserIds,
                    newUserIds, this)) {
                log.error("'onUpdateUserListOfGroupFailure' event invocation failed for listener: " +
                        listener.getClass().getName());
                return;
            }
        }
    }

    /**
     * This method is responsible for calling relevant postAddRole listener methods after successfully adding role.
     *
     * @param roleName       Name of the role.
     * @param userList       List of users.
     * @param permissions    Permissions that are assigned to the role.
     * @param isAuditLogOnly To indicate whether to only call the relevant audit logger.
     * @throws UserStoreException Exception that will be thrown by relevant listeners.
     */
    private void handlePostAddRole(String roleName, String[] userList, org.wso2.carbon.user.api.Permission[]
            permissions, boolean isAuditLogOnly) throws UserStoreException {

        try {
            boolean internalRole = isAnInternalRole(roleName);
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (isAuditLogOnly && !listener.getClass().getName().endsWith(UserCoreErrorConstants.AUDIT_LOGGER_CLASS_NAME)) {
                    continue;
                }

                boolean success = false;
                if (internalRole && listener instanceof AbstractUserOperationEventListener) {
                    success = ((AbstractUserOperationEventListener) listener).doPostAddInternalRole(roleName,
                            userList, permissions, this);
                } else if (internalRole && !(listener instanceof AbstractUserOperationEventListener)) {
                    success = true;
                } else if (!internalRole) {
                    success = listener.doPostAddRole(roleName, userList, permissions, this);
                }

                if (!success) {
                    handleAddRoleFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_ADD_ROLE.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_ADD_ROLE.getMessage(),
                                    UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), roleName, userList,
                            permissions);
                    return;
                }
            }
        } catch (UserStoreException ex) {
            handleAddRoleFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_ADD_ROLE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_ADD_ROLE.getMessage(), ex.getMessage()),
                    roleName, userList, permissions);
            throw ex;
        }
    }

    private boolean handlePreAddRole(String roleName, String[] userList, org.wso2.carbon.user.api.Permission[]
            permissions, boolean isAuditLogOnly) throws UserStoreException {

        try {
            boolean internalRole = isAnInternalRole(roleName);
            String internalSystemRolePrefix = INTERNAL_DOMAIN + DOMAIN_SEPARATOR + INTERNAL_SYSTEM_ROLE_PREFIX;
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (isAuditLogOnly && !listener.getClass().getName().endsWith(UserCoreErrorConstants.AUDIT_LOGGER_CLASS_NAME)) {
                    continue;
                }

                boolean success;
                if (internalRole && roleName.startsWith(internalSystemRolePrefix)) {
                    success = true;
                } else if (internalRole && listener instanceof AbstractUserOperationEventListener) {
                    success = ((AbstractUserOperationEventListener) listener).doPreAddInternalRole(roleName,
                            userList, permissions, this);
                } else if (internalRole) {
                    success = true;
                } else {
                    success = listener.doPreAddRole(roleName, userList, permissions, this);
                }

                if (!success) {
                    handleAddRoleFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_ROLE.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_ROLE.getMessage(),
                                    UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), roleName, userList,
                            permissions);
                    return false;
                }
            }
        } catch (UserStoreException ex) {
            handleAddRoleFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_ROLE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_ROLE.getMessage(), ex.getMessage()),
                    roleName, userList, permissions);
            throw ex;
        }
        return true;
    }

    /**
     *
     */
    public void addRole(String roleName, String[] userList, org.wso2.carbon.user.api.Permission[] permissions,
                        boolean isSharedRole) throws org.wso2.carbon.user.api.UserStoreException {

        if (StringUtils.isEmpty(roleName)) {
            handleAddRoleFailure(ErrorMessages.ERROR_CODE_CANNOT_ADD_EMPTY_ROLE.getCode(),
                    ErrorMessages.ERROR_CODE_CANNOT_ADD_EMPTY_ROLE.getMessage(), roleName, userList, permissions);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_CANNOT_ADD_EMPTY_ROLE.toString());
        }

        if (userList == null) {
            userList = new String[0];
        }

        UserStore userStore = getUserStoreOfRoles(roleName);

        if (isSharedRole && !isSharedGroupEnabled()) {
            handleAddRoleFailure(ErrorMessages.ERROR_CODE_SHARED_ROLE_NOT_SUPPORTED.getCode(),
                    ErrorMessages.ERROR_CODE_SHARED_ROLE_NOT_SUPPORTED.getMessage(), roleName, userList, permissions);
            throw new org.wso2.carbon.user.api.UserStoreException(
                    ErrorMessages.ERROR_CODE_SHARED_ROLE_NOT_SUPPORTED.toString());
        }

        if (userStore.isHybridRole()) {
            //Invoke Pre listeners for hybrid roles.
            if (!handlePreAddRole(roleName, userList, permissions, false)) {
                return;
            }

            doAddInternalRole(roleName, userList, permissions);

            // Calling only the audit logger, to maintain the back-ward compatibility
            handlePostAddRole(roleName, userList, permissions, false);
            return;
        }

        if (userStore.isRecurssive()) {
            userStore.getUserStoreManager().addRole(userStore.getDomainFreeName(),
                    UserCoreUtil.removeDomainFromNames(userList), permissions, isSharedRole);
            return;
        }

        // #################### Domain Name Free Zone Starts Here ################################
        if (permissions == null) {
            permissions = new org.wso2.carbon.user.api.Permission[0];
        }
        // This happens only once during first startup - adding administrator user/role.
        if (roleName.indexOf(CarbonConstants.DOMAIN_SEPARATOR) > 0) {
            roleName = userStore.getDomainFreeName();
            userList = UserCoreUtil.removeDomainFromNames(userList);
        }

        // #################### <Listeners> #####################################################
        if (!handlePreAddRole(roleName, userList, permissions, false)) {
            return;
        }
        // #################### </Listeners> #####################################################

        // Check for validations
        if (isReadOnly()) {
            handleAddRoleFailure(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode(),
                    ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage(), roleName, userList, permissions);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.toString());
        }

        if (!isRoleNameValid(roleName)) {
            String regEx = realmConfig
                    .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_ROLE_NAME_JAVA_REG_EX);
            String errorMessage = String
                    .format(ErrorMessages.ERROR_CODE_INVALID_ROLE_NAME.getMessage(), roleName, regEx);
            String errorCode = ErrorMessages.ERROR_CODE_INVALID_ROLE_NAME.getCode();
            handleAddRoleFailure(errorCode, errorMessage, roleName, userList, permissions);
            throw new UserStoreException(errorCode + " - " + errorMessage);
        }

        if (doCheckExistingRole(roleName)) {
            handleRoleAlreadyExistException(roleName, userList, permissions);
        }

        String roleWithDomain = null;
        if (writeGroupsEnabled) {
            try {
                // add role in to actual user store
                if (!isUniqueUserIdEnabledInUserStore(userStore)) {
                    doAddRole(roleName, userList, isSharedRole);
                } else {
                    List<String> userIDs = getUserIDsFromUserNames(Arrays.asList(userList));
                    if (isUniqueGroupIdEnabled()) {
                        Group group = doAddGroup(roleName, generateGroupUUID(), userIDs, null);
                        groupUniqueIDDomainResolver.setDomainForGroupId(group.getGroupID(), getMyDomainName(), tenantId,
                                false);
                    } else {
                        doAddRoleWithID(roleName, userIDs.toArray(new String[0]), isSharedRole);
                    }
                }
                roleWithDomain = UserCoreUtil.addDomainToName(roleName, getMyDomainName());
            } catch (UserStoreException ex) {
                handleAddRoleFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_ADDING_ROLE.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_ADDING_ROLE.getMessage(), ex.getMessage()),
                        roleName, userList, permissions);
                throw ex;
            }
        } else {
            handleAddRoleFailure(ErrorMessages.ERROR_CODE_WRITE_GROUPS_NOT_ENABLED.getCode(),
                    ErrorMessages.ERROR_CODE_WRITE_GROUPS_NOT_ENABLED.getMessage(), roleName, userList, permissions);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_WRITE_GROUPS_NOT_ENABLED.toString());
        }

        // add permission in to the the permission store
        if (permissions != null) {
            for (org.wso2.carbon.user.api.Permission permission : permissions) {
                String resourceId = permission.getResourceId();
                String action = permission.getAction();
                if (resourceId == null || resourceId.trim().length() == 0) {
                    continue;
                }

                if (action == null || action.trim().length() == 0) {
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
        handlePostAddRole(roleName, userList, permissions, false);
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
            for (Iterator<String> i = sharedRoles.iterator(); i.hasNext(); ) {
                String role = i.next();
                if (role.indexOf(tenantDomain) > -1) {
                    i.remove();
                }
            }
        }
    }

    /**
     * This method calls the relevant methods when there is a failure while trying to delete the role.
     *
     * @param errorCode    Error code.
     * @param errorMessage Error message.
     * @param roleName     Name of the roles.
     * @throws UserStoreException Exception that will be thrown by relevant listener methods.
     */
    private void handleDeleteRoleFailure(String errorCode, String errorMessage, String roleName)
            throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !listener.onDeleteRoleFailure(errorCode, errorMessage, roleName, this)) {
                return;
            }
        }
    }

    /**
     * This method is responsible for calling post delete methods of relevant listeners.
     *
     * @param roleName       Name of the role
     * @param isAuditLogOnly To indicate whether to call only the audit logger.
     * @throws UserStoreException Exception that will be thrown by relevant listener methods.
     */
    private void handleDoPostDeleteRole(String roleName, boolean isAuditLogOnly) throws UserStoreException {

        try {
            boolean internalRole = isAnInternalRole(roleName);
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (isAuditLogOnly && !listener.getClass().getName()
                        .endsWith(UserCoreErrorConstants.AUDIT_LOGGER_CLASS_NAME)) {
                    continue;
                }

                boolean success = false;
                if (internalRole && listener instanceof AbstractUserOperationEventListener) {
                    success = ((AbstractUserOperationEventListener) listener).doPostDeleteInternalRole(roleName, this);
                } else if (internalRole && !(listener instanceof AbstractUserOperationEventListener)) {
                    success = true;
                } else if (!internalRole) {
                    success = listener.doPostDeleteRole(roleName, this);
                }

                if (!success) {
                    handleDeleteRoleFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_ROLE.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_ROLE.getMessage(),
                                    UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), roleName);
                    return;
                }
            }
        } catch (UserStoreException ex) {
            handleDeleteRoleFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_ROLE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_ROLE.getMessage(), ex.getMessage()),
                    roleName);
            throw ex;
        }
    }

    /**
     * This method is responsible for calling pre delete methods of relevant listeners.
     *
     * @param roleName       Name of the role
     * @param isAuditLogOnly To indicate whether to call only the audit logger.
     * @throws UserStoreException Exception that will be thrown by relevant listener methods.
     */
    private boolean handleDoPreDeleteRole(String roleName, boolean isAuditLogOnly) throws UserStoreException {

        try {
            boolean internalRole = isAnInternalRole(roleName);
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (isAuditLogOnly && !listener.getClass().getName()
                        .endsWith(UserCoreErrorConstants.AUDIT_LOGGER_CLASS_NAME)) {
                    continue;
                }
                boolean success;
                if (internalRole && listener instanceof AbstractUserOperationEventListener) {
                    success = ((AbstractUserOperationEventListener) listener).doPreDeleteInternalRole(roleName, this);
                } else if (internalRole) {
                    success = true;
                } else {
                    success = listener.doPreDeleteRole(roleName, this);
                }

                if (!success) {
                    handleDeleteRoleFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_ROLE.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_ROLE.getMessage(),
                                    UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), roleName);
                    return false;
                }
            }
        } catch (UserStoreException ex) {
            handleDeleteRoleFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_ROLE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_ROLE.getMessage(), ex.getMessage()),
                    roleName);
            throw ex;
        }
        return true;
    }

    /**
     * Delete the role with the given role name
     *
     * @param roleName The role name
     * @throws org.wso2.carbon.user.core.UserStoreException
     */
    public final void deleteRole(String roleName) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class};
            callSecure("deleteRole", new Object[]{roleName}, argTypes);
            return;
        }
        if (UserCoreUtil.isPrimaryAdminRole(roleName, realmConfig)
                && !StringUtils.equals(getTenantDomain(tenantId), realmConfig.getAssociatedOrganizationUUID())) {
            handleDeleteRoleFailure(ErrorMessages.ERROR_CODE_CANNOT_DELETE_ADMIN_ROLE.getCode(),
                    ErrorMessages.ERROR_CODE_CANNOT_DELETE_ADMIN_ROLE.getMessage(), roleName);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_CANNOT_DELETE_ADMIN_ROLE.toString());
        }
        if (UserCoreUtil.isEveryoneRole(roleName, realmConfig)) {
            handleDeleteRoleFailure(ErrorMessages.ERROR_CODE_CANNOT_DELETE_EVERYONE_ROLE.getCode(),
                    ErrorMessages.ERROR_CODE_CANNOT_DELETE_EVERYONE_ROLE.getMessage(), roleName);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_CANNOT_DELETE_EVERYONE_ROLE.toString());
        }

        UserStore userStore = getUserStoreOfRoles(roleName);
        if (userStore.isRecurssive()) {
            userStore.getUserStoreManager().deleteRole(userStore.getDomainFreeName());
            return;
        }
        String roleWithDomain = UserCoreUtil.addDomainToName(roleName, getMyDomainName());
        // #################### Domain Name Free Zone Starts Here ################################
        if (userStore.isHybridRole()) {
            // Invoke pre listeners.
            if (!handleDoPreDeleteRole(roleName, false)) {
                return;
            }
            try {
                if (APPLICATION_DOMAIN.equalsIgnoreCase(userStore.getDomainName()) || WORKFLOW_DOMAIN
                        .equalsIgnoreCase(userStore.getDomainName())) {
                    hybridRoleManager.deleteHybridRole(roleName);
                } else {
                    hybridRoleManager.deleteHybridRole(userStore.getDomainFreeName());
                }
            } catch (UserStoreException ex) {
                handleDeleteRoleFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETE_ROLE.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETE_ROLE.getMessage(), ex.getMessage()),
                        roleName);
                throw ex;
            }
            handleDoPostDeleteRole(roleName, false);
            clearUserRolesCacheByTenant(tenantId);
            return;
        }
        if (!doCheckExistingRole(roleName)) {
            handleDeleteRoleFailure(ErrorMessages.ERROR_CODE_CANNOT_DELETE_NON_EXISTING_ROLE.getCode(),
                    ErrorMessages.ERROR_CODE_CANNOT_DELETE_NON_EXISTING_ROLE.getMessage(), roleName);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_CANNOT_DELETE_NON_EXISTING_ROLE.toString());
        }

        // #################### <Listeners> #####################################################
        if (!handleDoPreDeleteRole(roleName, false)) {
            return;
        }
        // #################### </Listeners> #####################################################
        if (isReadOnly()) {
            handleDeleteRoleFailure(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode(),
                    ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage(), roleName);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.toString());
        }
        if (!writeGroupsEnabled) {
            handleDeleteRoleFailure(ErrorMessages.ERROR_CODE_WRITE_GROUPS_NOT_ENABLED.getCode(),
                    ErrorMessages.ERROR_CODE_WRITE_GROUPS_NOT_ENABLED.getMessage(), roleName);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_WRITE_GROUPS_NOT_ENABLED.toString());
        }
        try {
            if (isUniqueGroupIdEnabled()) {
                String groupID = doGetGroupIdFromGroupName(UserCoreUtil.removeDomainFromName(roleName));
                groupUniqueIDDomainResolver.removeDomainForGroupId(groupID, getMyDomainName(), tenantId, false);
                doDeleteGroupByGroupId(groupID);
            } else {
                doDeleteRole(roleName);
            }
        } catch (UserStoreException ex) {
            handleDeleteRoleFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETE_ROLE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETE_ROLE.getMessage(), ex.getMessage()),
                    roleName);
            throw ex;
        }
        // clear role authorization
        userRealm.getAuthorizationManager().clearRoleAuthorization(roleWithDomain);
        // clear cache
        clearUserRolesCacheByTenant(tenantId);
        // Call relevant listeners after deleting the role.
        handleDoPostDeleteRole(roleName, false);
    }

    /**
     * Method to get the password expiration time.
     *
     * @param userName the user name.
     * @return the password expiration time.
     * @throws UserStoreException throw if the operation failed.
     */
    @Override
    public Date getPasswordExpirationTime(String userName) throws UserStoreException {

        UserStore userStore = getUserStore(userName);
        if (userStore.isRecurssive()) {
            return userStore.getUserStoreManager().getPasswordExpirationTime(userStore.getDomainFreeName());
        }

        if (isUniqueUserIdEnabledInUserStore(userStore)) {
            String userIDFromUserName = getUserIDFromUserName(userName);
            if (userIDFromUserName == null) {
                throw new UserStoreException("No UserId found for user:" + userName);
            }
            return doGetPasswordExpirationTimeWithID(getUserIDFromUserName(userName));
        } else {
            return doGetPasswordExpirationTime(userName);
        }
    }

    protected Date doGetPasswordExpirationTime(String userName) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doGetPasswordExpirationTime operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException("doGetPasswordExpirationTime operation is not implemented in: " + this.getClass());
    }

    protected Date doGetPasswordExpirationTimeWithID(String userName) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doGetPasswordExpirationTimeWithId operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException("doGetPasswordExpirationTimeWithId operation is not implemented in: " + this.getClass());
    }


    private UserStore getUserStore(final String user) throws UserStoreException {

        try {
            return AccessController
                    .doPrivileged((PrivilegedExceptionAction<UserStore>) () -> getUserStoreInternal(user));
        } catch (PrivilegedActionException e) {
            throw (UserStoreException) e.getException();
        }
    }

    protected UserStore getUserStoreWithID(final String userID) throws UserStoreException {

        try {
            return AccessController
                    .doPrivileged((PrivilegedExceptionAction<UserStore>) () -> getUserStoreInternalWithId(userID));
        } catch (PrivilegedActionException e) {
            throw (UserStoreException) e.getException();
        }
    }

    protected UserStore getUserStoreWithGroupId(final String groupId) throws UserStoreException {

        try {
            return AccessController.doPrivileged((PrivilegedExceptionAction<UserStore>)
                    () -> getUserStoreInternalWithGroupId(groupId));
        } catch (PrivilegedActionException e) {
            throw (UserStoreException) e.getException();
        }
    }

    protected UserStore getUserStoreWithGroupName(final String groupName) throws UserStoreException {

        try {
            return AccessController.doPrivileged((PrivilegedExceptionAction<UserStore>) () ->
                            getUserStoreInternalWithGroupName(groupName));
        } catch (PrivilegedActionException e) {
            throw (UserStoreException) e.getException();
        }
    }

    private UserStore getUserStoreOfRoles(final String role) throws UserStoreException {

        return getUserStore(role);
    }

    /**
     * @return
     * @throws UserStoreException
     */
    private UserStore getUserStoreInternal(String user) throws UserStoreException {

        int index;
        UserStore userStore = new UserStore();
        if (user == null) {
            userStore.setUserStoreManager(this);
            userStore.setRecurssive(false);
            userStore.setDomainName(getMyDomainName());
            return userStore;
        }
        index = user.indexOf(CarbonConstants.DOMAIN_SEPARATOR);
        String domainFreeName = null;

        // Check whether we have a secondary UserStoreManager setup.
        if (index > 0) {
            // Using the short-circuit. User name comes with the domain name.
            String domain = user.substring(0, index);
            UserStoreManager secManager = getSecondaryUserStoreManager(domain);
            if (secManager == null) {
                secManager = getSecondaryUserStore(domain);
            }
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
                    if ((UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(domain)
                            || APPLICATION_DOMAIN.equalsIgnoreCase(domain) || WORKFLOW_DOMAIN.equalsIgnoreCase(domain))) {
                        userStore.setHybridRole(true);
                    } else if (SYSTEM_DOMAIN_NAME.equalsIgnoreCase(domain)) {
                        userStore.setSystemStore(true);
                    } else {
                        throw new UserStoreException(String.format(ERROR_CODE_INVALID_DOMAIN_NAME.getMessage(), domain),
                                ERROR_CODE_INVALID_DOMAIN_NAME.getCode());
                    }
                }

                userStore.setDomainAwareName(user);
                userStore.setDomainFreeName(domainFreeName);
                userStore.setDomainName(domain);
                userStore.setRecurssive(false);
                userStore.setUserStoreManager(this);
                return userStore;
            }
        }

        String domain = getMyDomainName();
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

    private UserStore getUserStoreInternalWithGroupName(String groupName) throws UserStoreException {

        // If the group name is null, we set current user store manger as the selected one and return.
        UserStore userStore = new UserStore();
        if (groupName == null) {
            userStore.setUserStoreManager(this);
            userStore.setRecurssive(false);
            userStore.setDomainName(getMyDomainName());
            return userStore;
        }
        int index = groupName.indexOf(CarbonConstants.DOMAIN_SEPARATOR);
        String domainFreeName = null;

        // Check whether we have a secondary UserStoreManager setup.
        if (index > 0) {
            // Using the short-circuit. Here the group name comes with the domain name.
            String domain = UserCoreUtil.extractDomainFromName(groupName);
            UserStoreManager secManager = getSecondaryUserStoreManager(domain);
            if (secManager == null) {
                secManager = getSecondaryUserStore(domain);
            }
            domainFreeName = UserCoreUtil.removeDomainFromName(groupName);

            if (secManager != null) {
                userStore.setUserStoreManager(secManager);
                userStore.setRecurssive(true);
            } else {
                if (!domain.equalsIgnoreCase(getMyDomainName())) {
                    if ((UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(domain)
                            || APPLICATION_DOMAIN.equalsIgnoreCase(domain)
                            || WORKFLOW_DOMAIN.equalsIgnoreCase(domain))) {
                        userStore.setHybridRole(true);
                    } else if (SYSTEM_DOMAIN_NAME.equalsIgnoreCase(domain)) {
                        userStore.setSystemStore(true);
                    } else {
                        throw new UserStoreException("Invalid Domain Name: " + domain);
                    }
                }
                userStore.setRecurssive(false);
                userStore.setUserStoreManager(this);
            }
            userStore.setDomainAwareGroupName(groupName);
            userStore.setDomainFreeGroupName(domainFreeName);
            userStore.setDomainName(domain);
            return userStore;
        }
        String domain = getMyDomainName();
        userStore.setUserStoreManager(this);
        if (index > 0) {
            userStore.setDomainAwareGroupName(groupName);
            userStore.setDomainFreeGroupName(domainFreeName);
        } else {
            userStore.setDomainAwareGroupName(domain + CarbonConstants.DOMAIN_SEPARATOR + groupName);
            userStore.setDomainFreeGroupName(groupName);
        }
        userStore.setRecurssive(false);
        userStore.setDomainName(domain);
        return userStore;
    }

    private UserStore getUserStoreInternalWithGroupId(String groupId) throws UserStoreException {

        // If the group id is null, we set current user store manger as the selected one and return.
        UserStore userStore = new UserStore();
        if (groupId == null) {
            userStore.setUserStoreManager(this);
            userStore.setRecurssive(false);
            userStore.setDomainName(getMyDomainName());
            userStore.setDomainFreeGroupId(UserCoreUtil.removeDomainFromName(groupId));
            return userStore;
        }

        // Get the domain if it is already resolved in our cache or in local database.
        String domainName = groupUniqueIDDomainResolver.getDomainForGroupId(groupId, tenantId);
        if (domainName == null) {
            if (log.isDebugEnabled()) {
                log.debug("Iterating though scim2 tables tenant: " + tenantId);
            }
            // Trigger group resolvers to get domain name from the scim tables to maintain backward compatibility.
            domainName = resolveDomainFromGroupResolvers(groupId, tenantId);
            if (StringUtils.isNotBlank(domainName)) {
                // We don't need this to be persisted in the DB.
                groupUniqueIDDomainResolver.setDomainForGroupId(groupId, domainName, tenantId, true);
            } else {
                /*
                 * This means the domain name is not in our side. We need to iterate through all userstores until we
                 * encounter a matching userstore.
                 */
                if (log.isDebugEnabled()) {
                    log.debug("Iterating though group id enabled userstores in tenant: " + tenantId);
                }
                for (Map.Entry<String, UserStoreManager> entry : userStoreManagerHolder.entrySet()) {
                    if (entry.getValue() instanceof AbstractUserStoreManager) {
                        AbstractUserStoreManager abstractUserStoreManager = (AbstractUserStoreManager) entry.getValue();
                        if (!abstractUserStoreManager.isUniqueGroupIdEnabled()) {
                            /*
                             * The domain names of the userstores which not have isUniqueGroupIdEnabled will be handled
                             * from the above steps. Those will not read here unless the given group id is invalid.
                             * Invalid group id will be handled from proceeding steps.
                             */
                            continue;
                        }
                        if (isGroupExistsWithGivenDomain(groupId, abstractUserStoreManager, entry.getKey())) {
                            /*
                             * If we found a domain name for the give group id, update the domain resolver by
                             * updating both cache and the DB.
                             */
                            domainName = entry.getKey();
                            groupUniqueIDDomainResolver.setDomainForGroupId(groupId, domainName,
                                    tenantId, false);
                            break;
                        }
                    }
                }
            }
        }
        /*
         * Okay we didn't find the domain from there previous steps. So this should either the PRIMARY domain or an
         * invalid group id. So we need to set the current user store manager domain as the domain name.
         */
        if (domainName == null || domainName.equals(getMyDomainName())) {
            userStore.setUserStoreManager(this);
            userStore.setDomainAwareGroupId(UserCoreUtil.addDomainToName(groupId, domainName));
            userStore.setDomainFreeGroupId(UserCoreUtil.removeDomainFromName(groupId));
            userStore.setRecurssive(false);
            userStore.setDomainName(getMyDomainName());
            return userStore;
        }
        // We have found an domain for the given group Id.
        return getUserStoreInternalWithUserstoreDomainName(domainName, groupId, false);
    }

    /**
     * Invoke the GroupResolver listeners and resolve the domain.
     *
     * @param groupId  Group id.
     * @param tenantId Tenant id.
     * @return Resolved domain name.
     */
    private String resolveDomainFromGroupResolvers(String groupId, int tenantId) throws UserStoreException {

        GroupResolver groupResolver = UserStoreMgtDataHolder.getInstance().getGroupResolver();
        if (groupResolver == null) {
            return null;
        }
        Group group = new Group(groupId);
        if (groupResolver.isEnable()) {
            groupResolver.resolveGroupDomainByGroupId(group, tenantId);
        }

        String resolvedDomain = group.getUserStoreDomain();
        if (StringUtils.isBlank(resolvedDomain)) {
            /*
             * This means the group info cannot be resolved from GroupResolver listeners (Mainly from SCIM).
             * This might be due to an invalid group id or the userstore does not support resolving group domain name
             * from listeners.
             */
            if (log.isDebugEnabled()) {
                log.debug(String.format("Domain not resolved by GroupResolvers for group with id: %s " +
                        "in tenant: %s", groupId, tenantId));
            }
            return null;
        }
        return resolvedDomain;
    }

    private UserStore getUserStoreInternalWithId(String userId) throws UserStoreException {

        // If the user id is null, we set current user store manger as the selected one and return.
        UserStore userStore = new UserStore();
        if (userId == null) {
            userStore.setUserStoreManager(this);
            userStore.setRecurssive(false);
            userStore.setDomainName(getMyDomainName());
            return userStore;
        }

        // First we have to check whether this user store is already resolved and we have it either in the cache or
        // in our local database. If so we can use that.
        String domainName = null;
        if (userUniqueIDDomainResolver != null) {
            domainName = userUniqueIDDomainResolver.getDomainForUserId(userId, tenantId);
        }

        // If we don't have the domain name in our side, then we have to iterate through each user store and find
        // where is this user id from and mark it as the user store domain.
        if (domainName == null) {
            // Iterate through each registered user stores.
            for (Map.Entry<String, UserStoreManager> entry : userStoreManagerHolder.entrySet()) {
                if (entry.getValue() instanceof AbstractUserStoreManager) {
                    // If there is a user for the give user id, then that is the correct domain.
                    AbstractUserStoreManager abstractUserStoreManager = (AbstractUserStoreManager) entry.getValue();
                    if (abstractUserStoreManager.isUniqueUserIdEnabled()) {
                        try {
                            if (isUserExistsWithGivenDomain(userId, abstractUserStoreManager, entry.getKey())) {
                                // If we found a domain name for the give user id, update the domain resolver with
                                // the name.
                                domainName = entry.getKey();
                                userUniqueIDDomainResolver.setDomainForUserIdIfNotExists(userId, domainName, tenantId);
                                break;
                            }
                        } catch (UserStoreException e) {
                            String errorMessage =
                                    "Error occurred while checking the user with the userId: %s for " + "domain: %s. ";
                            log.warn(String.format(errorMessage , userId, entry.getKey()));
                            if (log.isDebugEnabled()) {
                                log.debug("Therefore, proceeding remaining domains", e);
                            }
                        }
                    } else {
                        // This is happening when the user store is not supporting uniqueID.
                        try {
                            if (abstractUserStoreManager.getUserListFromProperties(claimManager.getAttributeName(entry
                                    .getKey(), USER_ID_CLAIM_URI), userId, null).length > 0) {
                                domainName = entry.getKey();
                                userUniqueIDDomainResolver.setDomainForUserIdIfNotExists(userId, domainName, tenantId);
                                break;
                            }
                        } catch (org.wso2.carbon.user.api.UserStoreException e) {
                            String errorMessage =
                                    "Unable retrieve users from getUserListFromProperties method from the userstore: "
                                            + entry.getKey();
                            log.warn(errorMessage);
                            if (log.isDebugEnabled()) {
                                log.debug("Therefore, proceeding remaining domains", e);
                            }

                        }
                    }
                }
            }
        }

        // Okay we didn't find the domain from there either. So this should either the PRIMARY domain or an invalid
        // user id. So set the current user store manager domain as the domain name.
        if (domainName == null || domainName.equals(getMyDomainName())) {
            String domain = getMyDomainName();
            userStore.setUserStoreManager(this);
            userStore.setDomainAwareUserId(UserCoreUtil.addDomainToName(userId, domainName));
            userStore.setDomainFreeUserId(userId);
            userStore.setRecurssive(false);
            userStore.setDomainName(domain);
            return userStore;
        }
        return getUserStoreInternalWithUserstoreDomainName(domainName, userId, true);
    }

    /**
     * Get the secondary userstore for the given domain. The userstore attributes will be set differently for user
     * related flows and group related flows.
     *
     * @param domainName        Userstore domain name.
     * @param resourceId        User UUID or group Id.
     * @param isUserRelatedFlow Whether this is a user related flow.
     * @return UserStore.
     * @throws UserStoreException If an error occurred while getting the UserStore.
     */
    private UserStore getUserStoreInternalWithUserstoreDomainName(String domainName, String resourceId,
                                                                  boolean isUserRelatedFlow) throws UserStoreException {

        UserStore userStore = new UserStore();
        UserStoreManager secManager = getSecondaryUserStoreManager(domainName);
        if (secManager == null) {
            secManager = getSecondaryUserStore(domainName);
        }
        if (secManager != null) {
            userStore.setUserStoreManager(secManager);
            if (isUserRelatedFlow) {
                userStore.setDomainAwareUserId(UserCoreUtil.addDomainToName(resourceId, domainName));
                userStore.setDomainFreeUserId(resourceId);
            } else {
                userStore.setDomainAwareGroupId(UserCoreUtil.addDomainToName(resourceId, domainName));
                userStore.setDomainFreeGroupId(UserCoreUtil.removeDomainFromName(resourceId));
            }
            userStore.setDomainName(domainName);
            userStore.setRecurssive(true);
        } else {
            if (!domainName.equalsIgnoreCase(getMyDomainName())) {
                if ((UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(domainName)
                        || APPLICATION_DOMAIN.equalsIgnoreCase(domainName)
                        || WORKFLOW_DOMAIN.equalsIgnoreCase(domainName))) {
                    userStore.setHybridRole(true);
                } else if (SYSTEM_DOMAIN_NAME.equalsIgnoreCase(domainName)) {
                    userStore.setSystemStore(true);
                } else {
                    throw new UserStoreException("Invalid Domain Name");
                }
            }
            if (isUserRelatedFlow) {
                userStore.setDomainAwareUserId(UserCoreUtil.addDomainToName(resourceId, domainName));
                userStore.setDomainFreeUserId(resourceId);
            } else {
                userStore.setDomainAwareGroupId(UserCoreUtil.addDomainToName(resourceId, domainName));
                userStore.setDomainFreeGroupId(resourceId);
            }
            userStore.setDomainName(domainName);
            userStore.setRecurssive(false);
            userStore.setUserStoreManager(this);
        }
        return userStore;
    }

    /**
     * Check if a user with the given ID exists in the cache. If so check if user domain matches the given domain.
     * If the user does not exist in the cache, search the user in the underlying user store.
     *
     * @param userId                    User ID.
     * @param abstractUserStoreManager  Corresponding user store manager instance.
     * @param domainName                User store manager domain.
     * @return True if a username is found, false otherwise.
     * @throws UserStoreException       Thrown by the underlying UserStoreManager.
     */
    private Boolean isUserExistsWithGivenDomain(String userId, AbstractUserStoreManager abstractUserStoreManager,
                                                String domainName) throws UserStoreException {

        String userName = getFromUserNameCache(userId);
        if (StringUtils.isNotEmpty(userName)) {
            return StringUtils.equals(UserCoreUtil.extractDomainFromName(userName), domainName);
        }
        return (abstractUserStoreManager.doGetUserNameFromUserIDWithID(userId) != null);
    }

    /**
     * Check if a group with the given ID exists in the cache. If so check if group domain matches the given domain. If
     * the group does not exist in the cache, search the group in the underlying user store.
     *
     * @param groupId                  Unique id of the group.
     * @param abstractUserStoreManager Corresponding user store manager instance.
     * @param domainName               User store manager domain.
     * @return True if a group name is found, false otherwise.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    private Boolean isGroupExistsWithGivenDomain(String groupId, AbstractUserStoreManager abstractUserStoreManager,
                                                 String domainName) throws UserStoreException {

        String groupNameFromCache = getGroupNameFromGroupIdCache(groupId);
        if (StringUtils.isNotEmpty(groupNameFromCache)) {
            return StringUtils.equals(UserCoreUtil.extractDomainFromName(groupNameFromCache), domainName);
        }
        String groupName = abstractUserStoreManager.doGetGroupNameFromGroupId(groupId);
        if (StringUtils.isBlank(groupName)) {
            return false;
        }

        /*
         * According to the implementation of the abstract userstore manager, it might append a domain name that is
         * not the same as the name we are passing here. Therefore, we need to remove the domain to be stay in the
         * safe side.
         */
        addGroupNameToGroupIdCache(groupId, UserCoreUtil.removeDomainFromName(groupName),
                abstractUserStoreManager.getMyDomainName());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public final UserStoreManager getSecondaryUserStoreManager() {

        return secondaryUserStoreManager;
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
    public final UserStoreManager getSecondaryUserStoreManager(String userDomain) {
        if (userDomain == null) {
            return null;
        }
        return userStoreManagerHolder.get(userDomain.toUpperCase());
    }

    private UserStoreManager getSecondaryUserStore(String userDomain) throws UserStoreException {

        org.wso2.carbon.user.api.UserStoreManager userStoreManager;
        try {
            RealmService realmService = UserStoreMgtDSComponent.getRealmService();
            if (realmService == null) {
                return null;
            }
            userStoreManager = realmService.getTenantUserRealm(tenantId).getUserStoreManager();
            return ((AbstractUserStoreManager) userStoreManager).getSecondaryUserStoreManager(userDomain);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException(e.getMessage(), e);
        }
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

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{};
            Object object = callSecure("getAllSecondaryRoles", new Object[]{}, argTypes);
            return (String[]) object;
        }

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
     * @return
     */
    public boolean isSCIMEnabled() {

        return true;
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
        return getRoleNames("*", MAX_ITEM_LIMIT_UNLIMITED, noHybridRoles, true, true);
    }

    /**
     * Add internal roles for the given users.
     *
     * @param roleName
     * @param userList
     * @param permissions
     * @throws UserStoreException
     */
    protected void doAddInternalRole(String roleName, String[] userList,
                                     org.wso2.carbon.user.api.Permission[] permissions)
            throws UserStoreException {

        // #################### Domain Name Free Zone Starts Here ################################
        String domainModeratedRoleName = removeDomainIfNotApplicationRole(roleName);
        if (hybridRoleManager.isExistingRole(domainModeratedRoleName)) {
            handleRoleAlreadyExistException(domainModeratedRoleName, userList, permissions);
        }
        createHybridRole(domainModeratedRoleName, userList, permissions);

        if (permissions != null) {
            for (org.wso2.carbon.user.api.Permission permission : permissions) {
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
     * This method handles role already exists exception.
     *
     * @param roleName    Name of teh role.
     * @param userList    list of users.
     * @param permissions Relevant permissions added for new role.
     * @throws UserStoreException User Store Exception.
     */
    private void handleRoleAlreadyExistException(String roleName, String[] userList,
            org.wso2.carbon.user.api.Permission[] permissions) throws UserStoreException {

        String errorCode = ErrorMessages.ERROR_CODE_ROLE_ALREADY_EXISTS.getCode();
        String errorMessage = String.format(ErrorMessages.ERROR_CODE_ROLE_ALREADY_EXISTS.getMessage(), roleName);
        handleAddRoleFailure(errorCode, errorMessage, roleName, userList, permissions);
        throw new UserStoreException(errorCode + " - " + errorMessage, errorCode, null);
    }

    /**
     * This method handles role already exists exception.
     *
     * @param roleName    Name of teh role.
     * @param userIDList    list of users.
     * @param permissions Relevant permissions added for new role.
     * @throws UserStoreException User Store Exception.
     */
    private void handleRoleAlreadyExistExceptionWithID(String roleName, String[] userIDList,
                                                       org.wso2.carbon.user.api.Permission[] permissions)
            throws UserStoreException {

        String errorCode = ErrorMessages.ERROR_CODE_ROLE_ALREADY_EXISTS.getCode();
        String errorMessage = String.format(ErrorMessages.ERROR_CODE_ROLE_ALREADY_EXISTS.getMessage(), roleName);
        handleAddRoleFailureWithID(errorCode, errorMessage, roleName, userIDList, permissions);
        throw new UserStoreException(errorCode + " - " + errorMessage);
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
                                       boolean noSystemRole, boolean noSharedRoles)
            throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, int.class, boolean.class, boolean.class, boolean.class};
            Object object = callSecure("getRoleNames", new Object[]{filter, maxItemLimit, noInternalRoles,
                    noSystemRole, noSharedRoles}, argTypes);
            return (String[]) object;
        }

        String[] roleList = new String[0];

        if (!noInternalRoles && (filter.toLowerCase().startsWith(APPLICATION_DOMAIN.toLowerCase()))) {
            roleList = hybridRoleManager.getHybridRoles(filter);
        } else if (!noInternalRoles && !isAnInternalRole(filter) && !filter
                .contains(UserCoreConstants.DOMAIN_SEPARATOR)) {
            // When domain name is not present in the filter value.
            if (filter == "*") {
                roleList = hybridRoleManager.getHybridRoles(filter);
            } else {
                // Since Application domain roles are stored in db with the "Application/" prefix, when domain is not
                // present in the filter, need to append the "Application/" before sending for db query.
                String[] applicationDomainRoleArray = hybridRoleManager
                        .getHybridRoles(APPLICATION_DOMAIN + UserCoreConstants.DOMAIN_SEPARATOR + filter);
                String[] internalDomainRoleArray = hybridRoleManager.getHybridRoles(filter);
                List<String> internalOnlyList = new ArrayList<>();
                // When filtering with sw, ew and co there is a possibility of returning results belonging to
                // Application domain.
                for (String filteredRole : internalDomainRoleArray) {
                    if (filteredRole != null && !filteredRole.matches("Application/(.*)")) {
                        // Create Internal domain only list.
                        internalOnlyList.add(filteredRole);
                    }
                }
                roleList = UserCoreUtil.combineArrays(applicationDomainRoleArray,
                        internalOnlyList.toArray(new String[internalOnlyList.size()]));
            }
        } else if (!noInternalRoles) {
            roleList = hybridRoleManager.getHybridRoles(UserCoreUtil.removeDomainFromName(filter));
        }

        if (!noSystemRole) {
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
            if (UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(domain)
                    || APPLICATION_DOMAIN.equalsIgnoreCase(domain) || WORKFLOW_DOMAIN.equalsIgnoreCase(domain)) {
                return roleList;
            }
            if (secManager != null) {
                // We have a secondary UserStoreManager registered for this domain.
                filter = filter.substring(index + 1);
                if (secManager instanceof AbstractUserStoreManager) {
                    if (readGroupsEnabled) {
                        String[] externalRoles = ((AbstractUserStoreManager) secManager)
                                .doGetRoleNames(filter, maxItemLimit);
                        return UserCoreUtil.combineArrays(roleList, externalRoles);
                    }
                } else {
                    String[] externalRoles = secManager.getRoleNames();
                    return UserCoreUtil.combineArrays(roleList, externalRoles);
                }
            } else {
                throw new UserStoreClientException("Invalid Domain Name.");
            }
        } else if (index == 0) {
            if (readGroupsEnabled) {
                String[] externalRoles = doGetRoleNames(filter.substring(index + 1), maxItemLimit);
                return UserCoreUtil.combineArrays(roleList, externalRoles);
            }
        }

        if (readGroupsEnabled) {
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
                        if (readGroupsEnabled) {
                            String[] secondRoleList = ((AbstractUserStoreManager) storeManager)
                                    .doGetRoleNames(filter, maxItemLimit);
                            roleList = UserCoreUtil.combineArrays(roleList, secondRoleList);
                        }
                    } catch (UserStoreException e) {
                        // We can ignore and proceed. Ignore the results from this user store.
                        log.error(e);
                    }
                } else {
                    roleList = UserCoreUtil.combineArrays(roleList, storeManager.getRoleNames());
                }
            }
        }
        return roleList;
    }

    /**
     * @param userName
     * @param claims
     * @param domainName
     * @return
     * @throws UserStoreException
     */
    private Map<String, String> doGetUserClaimValues(String userName, String[] claims,
            String domainName, String profileName) throws UserStoreException {

        // Here the user name should be domain-less.
        boolean requireRolesAndGroups = false;
        boolean requireIntRole = false;
        boolean requireExtRole = false;
        boolean requireRoles = false;
        boolean requireGroups = false;
        String rolesAndGroupsClaim = null;
        String rolesClaim = null;
        String groupsClaim = null;


        if (profileName == null || profileName.trim().length() == 0) {
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
            if (property != null && isNotARoleOrGroupClaim(claim)) {
                propertySet.add(property);
            }

            if (UserCoreConstants.ROLE_CLAIM.equalsIgnoreCase(claim)) {
                requireRolesAndGroups = true;
                rolesAndGroupsClaim = claim;
            } else if (UserCoreConstants.INT_ROLE_CLAIM.equalsIgnoreCase(claim)) {
                requireIntRole = true;
                rolesAndGroupsClaim = claim;
            } else if (UserCoreConstants.EXT_ROLE_CLAIM.equalsIgnoreCase(claim)) {
                requireExtRole = true;
                rolesAndGroupsClaim = claim;
            }

            if (isGroupsVsRolesSeparationImprovementsEnabled(realmConfig)) {
                if (UserCoreConstants.INTERNAL_ROLES_CLAIM.equalsIgnoreCase(claim)) {
                    requireRoles = true;
                    rolesClaim = claim;
                } else if (UserCoreConstants.USER_STORE_GROUPS_CLAIM.equalsIgnoreCase(claim)) {
                    requireGroups = true;
                    groupsClaim = claim;
                }
            }
        }

        String[] properties = propertySet.toArray(new String[propertySet.size()]);
        Map<String, String> userPropertyValues = this.getUserPropertyValues(userName, properties,
                profileName);

        processAttributesAfterRetrieval(userName, userPropertyValues, profileName);

        List<String> getAgain = new ArrayList<>();
        Map<String, String> finalValues = new HashMap<>();

        boolean isOverrideUsernameClaimEnabled = false;
        if (!isUniqueUserIdEnabled()) {
            isOverrideUsernameClaimEnabled = Boolean
                    .parseBoolean(realmConfig.getIsOverrideUsernameClaimFromInternalUsername());
        }

        for (String claim : claims) {
            ClaimMapping mapping;
            try {
                mapping = (ClaimMapping) claimManager.getClaimMapping(claim);
            } catch (org.wso2.carbon.user.api.UserStoreException e) {
                throw new UserStoreException(e);
            }
            String property = null;
            String value = null;
            if (mapping != null) {
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

                value = userPropertyValues.get(property);

                if (isOverrideUsernameClaimEnabled && USERNAME_CLAIM_URI.equals(mapping.getClaim()
                        .getClaimUri())) {
                    if (log.isDebugEnabled()) {
                        log.debug("The username claim value is overridden by the username :" + userName);
                    }
                    value = userName;
                }
                if (value != null && value.trim().length() > 0) {
                    finalValues.put(claim, value);
                }

            } else {
                if (property == null && claim.equals(DISAPLAY_NAME_CLAIM)) {
                    property = this.realmConfig.getUserStoreProperty(LDAPConstants.DISPLAY_NAME_ATTRIBUTE);
                }

                value = userPropertyValues.get(property);
                if (value != null && value.trim().length() > 0) {
                    finalValues.put(claim, value);
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
        String[] rolesAndGroups = null;
        String[] roles = null;
        String[] groups = null;

        if (requireRolesAndGroups) {
            rolesAndGroups = getRoleListOfUser(userName);
        } else if (requireIntRole) {
            rolesAndGroups = doGetInternalRoleListOfUser(userName, "*");
        } else if (requireExtRole) {

            List<String> rolesList = new ArrayList<String>();
            String[] externalRoles = doGetExternalRoleListOfUser(userName, "*");
            rolesList.addAll(Arrays.asList(externalRoles));
            //if only shared enable
            if (isSharedGroupEnabled()) {
                String[] sharedRoles = doGetSharedRoleListOfUser(userName, null, "*");
                if (sharedRoles != null) {
                    rolesList.addAll(Arrays.asList(sharedRoles));
                }
            }

            rolesAndGroups = rolesList.toArray(new String[rolesList.size()]);
        }

        if (rolesAndGroups != null && rolesAndGroups.length > 0) {
            finalValues.put(rolesAndGroupsClaim, getMultiValuedString(Arrays.asList(rolesAndGroups)));
        }

        if (isGroupsVsRolesSeparationImprovementsEnabled(realmConfig)) {
            if (requireRoles) {
                roles = doGetInternalRoleListOfUser(userName, "*");
            }

            if (requireGroups) {
                groups = doGetExternalRoleListOfUser(userName, "*");
            }

            if (roles != null && roles.length > 0) {
                finalValues.put(rolesClaim, getMultiValuedString(Arrays.asList(roles)));
            }

            if (groups != null && groups.length > 0) {
                finalValues.put(groupsClaim, getMultiValuedString(Arrays.asList(groups)));
            }
        }
        return finalValues;
    }

    /**
     * Handles the processing of any special user store attribute values after retrieval.
     *
     * @param userName       Username of the user.
     * @param userAttributes Un-processed map (user store attribute name -> attribute value) of user store.
     * @param profileName    Profile name of the user.
     */
    protected void processAttributesAfterRetrieval(String userName, Map<String, String> userAttributes,
                                                   String profileName) {
        // Not implemented for AbstractUserStoreManager, may have implementations at subclasses.
    }

    /**
     * Handles the processing of any special user store attribute values before update.
     *
     * @param userName       Username of the user.
     * @param userAttributes Un-processed map (user store attribute name -> attribute value) of user store.
     * @param profileName    Profile name of the user.
     */
    protected void processAttributesBeforeUpdate(String userName, Map<String, ? extends Object> userAttributes,
                                                 String profileName) {
        // Not implemented for AbstractUserStoreManager, may have implementations at subclasses.
    }

    /**
     * Handles the processing of any special user store attribute values after retrieval.
     *
     * @param userID         User ID of the user.
     * @param userAttributes Un-processed map (user store attribute name -> attribute value) of user store.
     * @param profileName    Profile name of the user.
     */
    protected void processAttributesAfterRetrievalWithID(String userID, Map<String, String> userAttributes,
                                                         String profileName) {
        // Not implemented for AbstractUserStoreManager, may have implementations at subclasses.
    }

    /**
     * Handles the processing of any special user store attribute values before update.
     *
     * @param userID         User ID of the user.
     * @param userAttributes Un-processed map (user store attribute name -> attribute value) of user store.
     * @param profileName    Profile name of the user.
     */
    protected void processAttributesBeforeUpdateWithID(String userID, Map<String, ? extends Object> userAttributes,
                                                       String profileName) {
        // Not implemented for AbstractUserStoreManager, may have implementations at subclasses.
    }

    /**
     * @return
     */
    protected String getEveryOneRoleName() {
        return realmConfig.getEveryOneRoleName();
    }

    /**
     * @return
     */
    protected String getAdminRoleName() {
        return realmConfig.getAdminRoleName();
    }

    /**
     * @param credential
     * @return
     * @throws UserStoreException
     */
    protected boolean checkUserPasswordValid(Object credential) throws UserStoreException {

        // Skip password pattern validation if the skipPasswordValidationThreadLocal is set to true.
        if (UserCoreUtil.getSkipPasswordPatternValidationThreadLocal()) {
            return true;
        }
        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{Object.class};
            Object object = callSecure("checkUserPasswordValid", new Object[]{credential}, argTypes);
            return (Boolean) object;
        }

        if (credential == null) {
            return false;
        }

        Secret credentialObj;
        try {
            credentialObj = Secret.getSecret(credential);
        } catch (UnsupportedSecretTypeException e) {
            throw new UserStoreException("Unsupported credential type", e);
        }

        try {
            if (credentialObj.getChars().length < 1) {
                return false;
            }

            String regularExpression =
                    realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_JAVA_REG_EX);
            if (regularExpression != null) {
                if (isFormatCorrect(regularExpression, credentialObj.getChars())) {
                    return true;
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Submitted password does not match with the regex " + regularExpression);
                    }
                    return false;
                }
            }
            return true;
        } finally {
            credentialObj.clear();
        }

    }

    /**
     * Validate credential object.
     *
     * @param credential credentials object to be validated
     * @return is valid credentials object
     * @throws UserStoreException
     */
    protected boolean isValidCredentials(Object credential) throws UserStoreException {
        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[] { Object.class };
            Object object = callSecure("isValidCredentials", new Object[] { credential }, argTypes);
            return (Boolean) object;
        }

        if (credential == null) {
            return false;
        }

        Secret credentialObj = null;
        try {
            credentialObj = Secret.getSecret(credential);
            return credentialObj.getChars().length >= 1;
        } catch (UnsupportedSecretTypeException e) {
            throw new UserStoreException("Unsupported credential type", e);
        } finally {
            if (credentialObj != null) {
                credentialObj.clear();
            }
        }
    }

    /**
     * @param userName
     * @return
     * @throws UserStoreException
     */
    protected boolean checkUserNameValid(String userName) throws UserStoreException {

        if (UserCoreUtil.getSkipUsernamePatternValidationThreadLocal()) {
            return true;
        }
        if (isValidUserName(userName)) {
            String regularExpression = realmConfig
                    .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USER_NAME_JAVA_REG_EX);
            //Inorder to support both UsernameJavaRegEx and UserNameJavaRegEx.
            if (StringUtils.isEmpty(regularExpression) || StringUtils.isEmpty(regularExpression.trim())) {
                regularExpression = realmConfig
                        .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USER_NAME_JAVA_REG);
            }

            if (MultitenantUtils.isEmailUserName()) {
                regularExpression = realmConfig
                        .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USER_NAME_WITH_EMAIL_JS_REG_EX);

                if (StringUtils.isEmpty(regularExpression) || StringUtils.isEmpty(regularExpression.trim())) {
                    regularExpression = realmConfig
                            .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USER_NAME_JAVA_REG_EX);
                }

                //Inorder to support both UsernameJavaRegEx and UserNameJavaRegEx.
                if (StringUtils.isEmpty(regularExpression) || StringUtils.isEmpty(regularExpression.trim())) {
                    regularExpression = realmConfig
                            .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USER_NAME_JAVA_REG);
                }

                if (StringUtils.isEmpty(regularExpression) || StringUtils.isEmpty(regularExpression.trim())) {
                    regularExpression = UserCoreConstants.RealmConfig.EMAIL_VALIDATION_REGEX;
                }
            }

            if (regularExpression != null) {
                regularExpression = regularExpression.trim();
            }

            if (StringUtils.isNotEmpty(regularExpression)) {
                if (isFormatCorrect(regularExpression, userName)) {
                    return true;
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Username " + userName + " does not match with the regex " + regularExpression);
                    }
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Validate username. It should be non-null, non-blank value which should not equal to REGISTRY_SYSTEM_USERNAME.
     *
     * @param userName username to be validated
     * @return is username valid
     * @throws UserStoreException when checking the call is secure or not
     */
    protected boolean isValidUserName(String userName) throws UserStoreException {
        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[] { String.class };
            Object object = callSecure("isValidUserName", new Object[] { userName }, argTypes);
            return (Boolean) object;
        }

        if (userName == null || CarbonConstants.REGISTRY_SYSTEM_USERNAME.equals(userName)) {
            return false;
        }

        String leadingOrTrailingSpaceAllowedInUserName = realmConfig
                .getUserStoreProperty(UserCoreConstants.RealmConfig.LEADING_OR_TRAILING_SPACE_ALLOWED_IN_USERNAME);
        if (StringUtils.isEmpty(leadingOrTrailingSpaceAllowedInUserName)) {
            // Keeping old behavior for backward-compatibility.
            userName = userName.trim();
        } else {
            if (log.isDebugEnabled()) {
                log.debug("'LeadingOrTrailingSpaceAllowedInUserName' property is set to : "
                        + leadingOrTrailingSpaceAllowedInUserName + ". Hence username trimming will be skipped during "
                        + "validation for the username: " + userName);
            }
        }

        return !userName.isEmpty();
    }

    /**
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
     * Check whether the given group name is valid.
     *
     * @param groupName Name of the group.
     * @return True if the group name is valid.
     */
    protected boolean isGroupNameValid(String groupName) {

        if (StringUtils.isBlank(groupName)) {
            return false;
        }
        String regularExpression = realmConfig
                .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_ROLE_NAME_JAVA_REG_EX);
        if (StringUtils.isBlank(regularExpression)) {
            return true;
        }
        return isFormatCorrect(regularExpression, groupName);
    }

    protected String[] getRoleListOfUserFromCache(int tenantID, String userName) {

        if (userRolesCache != null) {
            String usernameWithDomain = UserCoreUtil.addDomainToName(userName, getMyDomainName());
            return userRolesCache.getRolesListOfUser(cacheIdentifier, tenantID, usernameWithDomain);
        }
        return null;
    }

    /**
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
     * @param tenantID
     * @param userName
     * @param roleList
     */
    protected void addToUserRolesCache(int tenantID, String userName, String[] roleList) {

        if (userRolesCache != null) {
            String usernameWithDomain = UserCoreUtil.addDomainToName(userName, getMyDomainName());
            String[] rolesWithDomain = UserCoreUtil.addDomainToNames(roleList, getMyDomainName());
            userRolesCache.addToCache(cacheIdentifier, tenantID, usernameWithDomain, rolesWithDomain);
            AuthorizationCache authorizationCache = AuthorizationCache.getInstance();
            authorizationCache.clearCacheByTenant(tenantID);
        }
    }

    protected void clearUserRolesCache(String userIdentifier) {

        String usernameWithDomain = UserCoreUtil.addDomainToName(userIdentifier, getMyDomainName());
        if (userRolesCache != null) {
            userRolesCache.clearCacheEntry(cacheIdentifier, tenantId, usernameWithDomain);
        }
        AuthorizationCache authorizationCache = AuthorizationCache.getInstance();
        authorizationCache.clearCacheByUser(tenantId, usernameWithDomain);
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
            if (timeOutString != null) {
                timeOut = Integer.parseInt(timeOutString);
            }
            userRolesCache = UserRolesCache.getInstance();
            userRolesCache.setTimeOut(timeOut);
        }

    }

    /**
     * @param regularExpression
     * @param attribute
     * @return
     */
    private boolean isFormatCorrect(String regularExpression, String attribute) {
        Pattern p2 = Pattern.compile(regularExpression);
        Matcher m2 = p2.matcher(attribute);
        return m2.matches();
    }

    private boolean isFormatCorrect(String regularExpression, char[] attribute) {

        boolean matches;
        CharBuffer charBuffer = CharBuffer.wrap(attribute);

        Pattern p2 = Pattern.compile(regularExpression);
        Matcher m2 = p2.matcher(charBuffer);
        matches = m2.matches();

        return matches;
    }

    /**
     * This is to replace escape characters in user name at user login if replace escape characters
     * enabled in user-mgt.xml. Some User Stores like ApacheDS stores user names by replacing escape
     * characters. In that case, we have to parse the username accordingly.
     *
     * @param userName
     */
    protected String replaceEscapeCharacters(String userName) {

        if (log.isDebugEnabled()) {
            log.debug("Replacing escape characters in " + userName);
        }
        String replaceEscapeCharactersAtUserLoginString = realmConfig
                .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_REPLACE_ESCAPE_CHARACTERS_AT_USER_LOGIN);

        if (replaceEscapeCharactersAtUserLoginString != null) {
            replaceEscapeCharactersAtUserLogin = Boolean
                    .parseBoolean(replaceEscapeCharactersAtUserLoginString);
            if (log.isDebugEnabled()) {
                log.debug("Replace escape characters at userlogin is configured to: "
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

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{};
            Object object = callSecure("getAllSecondaryRoleDTOs", new Object[]{}, argTypes);
            return (RoleDTO[]) object;
        }

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
            String errorCode = ErrorMessages.ERROR_CODE_INVALID_ROLE_NAME.getCode();
            String errorMessage = String
                    .format(ErrorMessages.ERROR_CODE_INVALID_ROLE_NAME.getMessage(), roleName, regEx);
            handleAddRoleFailure(errorCode, errorMessage, roleName, userList, permissions);
            throw new UserStoreException(errorCode + " - " + errorMessage);
        }
        createSystemRole(roleName, userList, permissions);
    }


    /**
     * @param roleName
     * @param filter
     * @return
     * @throws UserStoreException
     */
    protected abstract String[] doGetUserListOfRole(String roleName, String filter)
            throws UserStoreException;

    /**
     * Return the list of users belong to the given role for the given filter and max item limit.
     *
     * @param roleName Name of the role.
     * @param filter String filter value.
     * @param maxItemLimit Maximum number of users in the returned array. A negative value return all users and zero
     *                     returns zero users.
     * @return An array of users.
     * @throws UserStoreException
     */
    protected String[] doGetUserListOfRole(String roleName, String filter, int maxItemLimit)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("Using the default implementation of retrieving users in the role: " + roleName + " only with " +
                    "the filter: " + filter + ". The provided value: " + maxItemLimit + " for the maximum limit " +
                    "of returning users is ignored");
        }
        return doGetUserListOfRole(roleName, filter);
    }

    /**
     * Return the list of users belong to the given role for the given filter.
     *
     * @param roleName role name.
     * @param filter   filter.
     * @return user list of the given role.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    protected List<User> doGetUserListOfRoleWithID(String roleName, String filter) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doGetUserListOfRoleWithID operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException(
                "doGetUserListOfRoleWithID operation is not implemented in: " + this.getClass());
    }

    /**
     * Return the count of users belong to the given role for the given filter when unique id feature is not enabled.
     *
     * @param roleName role name.
     * @return user count for the given role.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    protected int doGetUserCountOfRole(String roleName) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doGetUserCountOfRole operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException("doGetUserCountOfRole operation is not implemented in: " + this.getClass());
    }

    /**
     * Return the count of users belong to the given role.
     *
     * @param roleName Name of the role.
     * @return User count for the given role.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    protected int doGetUserCountOfRoleWithID(String roleName) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doGetUserCountOfRoleWithID operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException(
                "doGetUserCountOfRoleWithID operation is not implemented in: " + this.getClass());
    }

    /**
     * Return the list of users belong to the given role for the given filter.
     *
     * @param roleName     role name.
     * @param filter       filter.
     * @param maxItemLimit Maximum number of users in the returned array. A negative value return all users and zero
     *                     returns zero users.
     * @return user list of the given role.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    protected List<User> doGetUserListOfRoleWithID(String roleName, String filter, int maxItemLimit)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("Using the default implementation of retrieving users in the role: " + roleName + " only with "
                    + "the filter: " + filter + ". The provided value: " + maxItemLimit + " for the maximum limit "
                    + "of returning users is ignored");
        }
        return doGetUserListOfRoleWithID(roleName, filter);
    }

    /**
     * This will return the roles list of given user ID.
     *
     * @param userID user ID.
     * @param filter filter.
     * @return user list of the given role.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    public List<String> doGetRoleListOfUserWithID(String userID, String filter) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[] { String.class, String.class };
            Object object = callSecure("doGetRoleListOfUserWithID", new Object[] { userID, filter }, argTypes);
            return (List<String>) object;
        }

        String username = getUserNameFromUserID(userID);
        if (username != null) {
            String[] roleListOfUserFromCache = getRoleListOfUserFromCache(this.tenantId, username);
            if (roleListOfUserFromCache != null) {
                List<String> roleList = Arrays.asList(roleListOfUserFromCache);
                if (!roleList.isEmpty()) {
                    return roleList;
                }
            }
        }

        return getUserRolesWithID(userID, filter);
    }

    private List<String> getUserRolesWithID(String userID, String filter) throws UserStoreException {

        List<String> internalRoles = doGetInternalRoleListOfUserWithID(userID, filter);
        Set<String> modifiedInternalRoles = new HashSet<>();
        String[] modifiedExternalRoleList = new String[0];

        if (readGroupsEnabled && doCheckExistingUserWithID(userID)) {
            List<String> roles = new ArrayList<>();
            String[] externalRoles = doGetExternalRoleListOfUserWithID(userID, "*");
            roles.addAll(Arrays.asList(externalRoles));
            if (isSharedGroupEnabled()) {
                String[] sharedRoles = doGetSharedRoleListOfUserWithID(userID, null, "*");
                if (sharedRoles != null) {
                    roles.addAll(Arrays.asList(sharedRoles));
                }
            }
            modifiedExternalRoleList = UserCoreUtil.addDomainToNames(roles.toArray(new String[0]), getMyDomainName());

            // Get the associated internal roles of the groups.
            if (isRoleAndGroupSeparationEnabled()) {
                Set<String> rolesOfGroups = getUniqueSet(getHybridRoleListOfGroups(roles, getMyDomainName()));
                modifiedInternalRoles.addAll(rolesOfGroups);
            }
        }
        modifiedInternalRoles.addAll(internalRoles);
        String[] roleList = UserCoreUtil.combine(modifiedExternalRoleList, new ArrayList<>(modifiedInternalRoles));

        for (UserOperationEventListener userOperationEventListener : UMListenerServiceComponent
                .getUserOperationEventListeners()) {
            if (userOperationEventListener instanceof AbstractUserOperationEventListener) {
                if (!((AbstractUserOperationEventListener) userOperationEventListener)
                        .doPostGetRoleListOfUserWithID(userID, filter, roleList, this)) {
                    break;
                }
            }
        }

        // Add to user role cache uisng username.
        String username = getUserNameFromUserID(userID);
        if (username != null) {
            addToUserRolesCache(this.tenantId, username, roleList);
        }
        return Arrays.asList(roleList);
    }

    /**
     * This will return the roles list of given user name.
     *
     * @param userName user name.
     * @param filter   filter.
     * @return user list of the given role.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    public final String[] doGetRoleListOfUser(String userName, String filter) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[] { String.class, String.class };
            Object object = callSecure("doGetRoleListOfUser", new Object[] { userName, filter }, argTypes);
            return (String[]) object;
        }

        String[] roleList = getRoleListOfUserFromCache(this.tenantId, userName);
        if (roleList != null && roleList.length > 0) {
            return roleList;
        }

        return getUserRoles(userName, filter);
    }

    private String[] getUserRoles(String username, String filter) throws UserStoreException {

        String[] internalRoles = doGetInternalRoleListOfUser(username, filter);
        String[] modifiedExternalRoleList = new String[0];

        if (readGroupsEnabled && doCheckExistingUser(username)) {
            String[] externalRoles = doGetExternalRoleListOfUser(username, "*");
            List<String> roles = Arrays.asList(externalRoles);
            if (isSharedGroupEnabled()) {
                String[] sharedRoles = doGetSharedRoleListOfUser(username, null, "*");
                if (sharedRoles != null) {
                    roles.addAll(Arrays.asList(sharedRoles));
                }
            }
            modifiedExternalRoleList = UserCoreUtil
                    .addDomainToNames(roles.toArray(new String[0]), getMyDomainName());

            // Get the associated internal roles of the groups.
            if (isRoleAndGroupSeparationEnabled()) {
                Set<String> rolesOfGroups = getUniqueSet(getHybridRoleListOfGroups(roles, getMyDomainName()));
                internalRoles = UserCoreUtil.combine(internalRoles, new ArrayList<>(rolesOfGroups));
            }
        }

        String[] roleList = UserCoreUtil.combine(internalRoles, Arrays.asList(modifiedExternalRoleList));

        for (UserOperationEventListener userOperationEventListener : UMListenerServiceComponent
                .getUserOperationEventListeners()) {
            if (userOperationEventListener instanceof AbstractUserOperationEventListener) {
                if (!((AbstractUserOperationEventListener) userOperationEventListener)
                        .doPostGetRoleListOfUser(username, filter, roleList, this)) {
                    break;
                }
            }
        }
        addToUserRolesCache(this.tenantId, username, roleList);
        return roleList;
    }

    /**
     * Retrieve the list of users directly from the database,
     * without using the cache.
     * @param username username of the user
     * @param filter filter to be used when searching for roles
     * @return the list of roles which the specified users belongs to
     * @throws UserStoreException
     */
    public final String[] getRoleListOfUserFromDatabase(String username, String filter)
            throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, String.class};
            Object object = callSecure("getRoleListOfUserFromDatabase", new Object[]{username, filter}, argTypes);
            return (String[]) object;
        }

        if (isUniqueUserIdEnabledInUserStore(getUserStore(username))) {
            String userID = getUserIDFromUserName(username);
            if (userID == null) {
                // According to implementation, getRoleListOfUser method would return everyone role name for all users.
                return new String[]{realmConfig.getEveryOneRoleName()};
            }
            return getUserRolesWithID(userID, filter).toArray(new String[0]);
        } else {
            return getUserRoles(username, filter);
        }
    }


    /**
     * @param filter
     * @return
     * @throws UserStoreException
     */
    public final String[] getHybridRoles(String filter) throws UserStoreException {
        return hybridRoleManager.getHybridRoles(filter);
    }

    /**
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
                attributeList.add(claimManager.getAttributeName(getMyDomainName(), claimIter.next()));
            } catch (org.wso2.carbon.user.api.UserStoreException e) {
                throw new UserStoreException(e);
            }
        }
        return attributeList;
    }

    protected void doInitialSetup() throws UserStoreException {

        systemUserRoleManager = new SystemUserRoleManager(dataSource, tenantId);
        hybridRoleManager = new HybridRoleManager(dataSource, tenantId, realmConfig, userRealm);
        userUniqueIDDomainResolver = new UserUniqueIDDomainResolver(dataSource);
        groupUniqueIDDomainResolver = new GroupUniqueIDDomainResolver(dataSource);
    }

    /**
     * @return whether this is the initial startup
     * @throws UserStoreException
     */
    protected void doInitialUserAdding() throws UserStoreException {

        String systemUser = UserCoreUtil.removeDomainFromName(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME);
        String systemRole = UserCoreUtil.removeDomainFromName(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME);

        if (!systemUserRoleManager.isExistingSystemUser(systemUser)) {
            try {
                systemUserRoleManager.addSystemUser(systemUser,
                        UserCoreUtil.getPolicyFriendlyRandomPassword(systemUser), null);
            } catch (UserStoreException e) {
                if (ERROR_CODE_DUPLICATE_WHILE_ADDING_A_SYSTEM_USER.getCode().equals(e.getErrorCode())) {
                    log.warn(String.format("System User :%s has already added. Hence, continue without adding the " +
                                    "user.", systemUser));
                } else {
                    throw e;
                }
            }

        }

        if (!systemUserRoleManager.isExistingRole(systemRole)) {
            try {
                systemUserRoleManager.addSystemRole(systemRole, new String[]{systemUser});
            } catch (UserStoreException e) {
                if (ERROR_CODE_DUPLICATE_WHILE_ADDING_A_SYSTEM_ROLE.getCode().equals(e.getErrorCode())) {
                    log.warn(String.format("System Role :%s is already added. Hence, continue without adding the " +
                                    "role.", systemRole), e);
                } else {
                    throw e;
                }
            }
        }

        if (!hybridRoleManager.isExistingRole(UserCoreUtil.removeDomainFromName(realmConfig
                .getEveryOneRoleName()))) {
            try {
                hybridRoleManager.addHybridRole(
                        UserCoreUtil.removeDomainFromName(realmConfig.getEveryOneRoleName()), null);
            } catch (UserStoreException e) {
                if (ERROR_CODE_DUPLICATE_WHILE_ADDING_A_HYBRID_ROLE.getCode().equals(e.getErrorCode())) {
                    log.warn(String.format("Hybrid Role :%s is already added. Hence, continue without adding the " +
                            "role.", systemRole));
                } else {
                    throw e;
                }
            }
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
        boolean isReadGroupsEnabled = Boolean.parseBoolean(this.getRealmConfiguration()
                .getUserStoreProperty(UserCoreConstants.RealmConfig.READ_GROUPS_ENABLED));
        boolean userExist = false;
        boolean roleExist = false;
        boolean isInternalRole = false;
        String adminUserID = null;
        User user = null;

        try {
            if (!isRoleAndGroupSeparationEnabled() && isReadGroupsEnabled) {
                roleExist = doCheckExistingRole(adminRoleName);
            }
        } catch (Exception e) {
            //ignore
        }

        if (!roleExist) {
            try {
                roleExist = hybridRoleManager.isExistingRole(adminRoleName);
            } catch (Exception e) {
                //ignore
            }
            if (roleExist) {
                isInternalRole = true;
            }
        }

        try {
            if (isUniqueUserIdEnabled()) {
                userExist = doCheckExistingUserNameWithIDImpl(adminUserName);
                if (userExist) {
                    String userIDFromUserName = getUserIDFromUserName(adminUserName);
                    String userIDAttribute = realmConfig.getUserStoreProperty(LDAPConstants.USER_ID_ATTRIBUTE);
                    String userID = getUniqueUserID();
                    if (StringUtils.isEmpty(userIDFromUserName) && !this.isReadOnly()) {
                        doSetUserAttribute(adminUserName, userIDAttribute, userID, null);
                    }
                }
            } else {
                userExist = doCheckExistingUser(adminUserName);
            }
        } catch (Exception e) {
            //ignore
        }

        if (!userExist) {
            if (isReadOnly()) {
                String message = "Admin user can not be created in primary user store. " +
                        "User store is read only. " +
                        "Please pick a user name which is exist in the primary user store as Admin user";
                handleInitialSetup(initialSetup, message);
            } else if (addAdmin) {
                try {
                    if (isUniqueUserIdEnabled()) {
                        // Ignore the return value as we don't need it.
                        user = doAddUserWithID(adminUserName, realmConfig.getAdminPassword(), null, null, null, false);
                    } else {
                        // Call the old API since this user store does not support the unique user id related APIs.
                        Map<String, String> claims = new HashMap<>();
                        claims.put(USER_ID_CLAIM_URI, getUniqueUserID());
                        this.doAddUser(adminUserName, realmConfig.getAdminPassword(), null, claims, null, false);
                    }
                } catch (Exception e) {
                    String message = "Admin user has not been created. " +
                            "Error occurs while creating Admin user in primary user store.";
                    String warnMessage = String.format("Admin User :%s is already added. Hence, continue without" +
                            " adding the user.", adminUserName);
                    handleInitialSetupException(initialSetup, e,
                            ERROR_CODE_DUPLICATE_WHILE_ADDING_A_USER.getCode(), message, warnMessage);
                }
            } else {
                if (initialSetup) {
                    String message = "Admin user can not be created in primary user store. " +
                            "Add-Admin has been set to false. " +
                            "Please pick a User name which is exist in the primary user store as Admin user";
                    handleInitialSetup(initialSetup, message);
                }
            }
        }

        if (isUniqueUserIdEnabled()) {
            if (user != null && user.getUserID() != null) {
                adminUserID = user.getUserID();
            } else {
                adminUserID = getUserIDFromUserName(adminUserName);
            }
        }

        if (!roleExist) {
            if (addAdmin) {
                if (!isRoleAndGroupSeparationEnabled() && !isReadOnly() && writeGroupsEnabled) {
                    try {
                        if (isUniqueUserIdEnabled()) {
                            this.doAddRoleWithID(adminRoleName, new String[] { adminUserID }, false);
                        } else {
                            this.doAddRole(adminRoleName, new String[] { adminUserName }, false);
                        }
                    } catch (org.wso2.carbon.user.api.UserStoreException e) {
                        String message = "Admin role has not been created. " +
                                "Error occurs while creating Admin role in primary user store.";
                        String warnMessage = String.format("Admin Role :%s is already added. Hence, continue without"
                                + " adding the role.", adminRoleName);
                        handleInitialSetupException(initialSetup, e,
                                ERROR_CODE_DUPLICATE_WHILE_ADDING_ROLE.getCode(), message, warnMessage);
                    }
                } else {
                    // Creates internal role.
                    try {
                        hybridRoleManager.addHybridRole(adminRoleName, new String[] { adminUserName });
                        isInternalRole = true;

                        if (isRoleAndGroupSeparationEnabled()) {
                            // Create a new admin group with the same role name if not exist.
                            boolean groupExist = false;
                            if (isReadGroupsEnabled) {
                                groupExist = doCheckExistingRole(adminRoleName);
                            }
                            if (!groupExist && !isReadOnly() && writeGroupsEnabled) {
                                if (isUniqueUserIdEnabled()) {
                                    if (isUniqueGroupIdEnabled()) {
                                        List<String> members = new ArrayList<>();
                                        members.add(adminUserID);
                                        this.doAddGroup(adminRoleName, generateGroupUUID(), members, null);
                                    } else {
                                        this.doAddRoleWithID(adminRoleName, new String[]{adminUserID}, false);
                                    }
                                } else {
                                    this.doAddRole(adminRoleName, new String[] { adminUserName }, false);
                                }
                                groupExist = true;
                            }
                            // Assign the admin group to the admin role.
                            if (groupExist) {
                                this.updateGroupListOfHybridRole(adminRoleName, null, new String[] { adminRoleName });
                            }
                        }
                    } catch (Exception e) {
                        String message = "Admin role has not been created. " +
                                "Error occurs while creating Admin role in primary user store.";
                        String warnMessage = String.format("Hybrid Admin Role :%s is already added. Hence, continue"
                                + " without adding the hybrid role.", adminRoleName);
                        handleInitialSetupException(initialSetup, e,
                                ERROR_CODE_DUPLICATE_WHILE_ADDING_A_HYBRID_ROLE.getCode(), message, warnMessage);
                    }
                }
            } else if (isRoleAndGroupSeparationEnabled()) {
                // The adminRoleName variable refers to the group here, not the role.
                // This variable was named as such prior to the group-role separation.
                boolean groupExist = doesGroupExist(isReadGroupsEnabled, adminRoleName);
                if (createAdminRole(initialSetup, groupExist, adminRoleName, adminUserName)) {
                    isInternalRole = Boolean.TRUE;
                }
            } else {
                String message = "Admin role can not be created in primary user store. " +
                        "Add-Admin has been set to false. " +
                        "Please pick a Role name which is exist in the primary user store as Admin Role";
                handleInitialSetup(initialSetup, message);
            }
        }

        if (isInternalRole) {

            updateHybridRoleListOfUserInternal(initialSetup, adminRoleName, adminUserName);
            realmConfig.setAdminRoleName(UserCoreUtil.addInternalDomainName(adminRoleName));
        } else if (!isReadOnly() && writeGroupsEnabled) {

            boolean doCheckIsUserInRole;
            if (isUniqueUserIdEnabled()) {
                doCheckIsUserInRole = this.doCheckIsUserInRoleWithID(adminUserID, adminRoleName);
            } else {
                doCheckIsUserInRole = this.doCheckIsUserInRole(adminUserName, adminRoleName);
            }
            if (!doCheckIsUserInRole) {
                if (addAdmin) {
                    try {
                        if (isUniqueUserIdEnabled()) {
                            this.doUpdateRoleListOfUserWithID(adminUserID, null, new String[] { adminRoleName });
                        } else {
                            this.doUpdateRoleListOfUser(adminUserName, null, new String[] { adminRoleName });
                        }
                    } catch (Exception e) {
                        String message = "Admin user has not been assigned to Admin role. " +
                                "Error while assignment is done";
                        handleInitialSetup(initialSetup, message);
                    }
                } else {
                    String message = "Admin user can not be assigned to Admin role " +
                            "Add-Admin has been set to false. Please do the assign it in user store level";
                    handleInitialSetup(initialSetup, message);
                }
            }
        }

        doInitialUserAdding();
    }

    /**
     * Creates the admin role when the create_admin_account configuration is set to false
     * in the deployment.toml when group-role separation is enabled (admin account creation
     * when the config is set to true is handled separately).
     *
     * When group-role separation is enabled, it is expected that the admin role is created in
     * the system and that the necessary user associations are established, regardless of whether
     * create_admin_account is enabled, because roles are managed separately in the system
     * database after the group-role separation.
     *
     * @param initialSetup         A flag to indicate whether this is the initial set up of the server.
     * @param groupExist           A flag to indicate whether the group exists.
     * @param adminRoleName        The name of the admin role being created exists.
     * @param adminUserName        The name of the admin user.
     * @throws UserStoreException  when an error occurs during the initial set up.
     */
    private boolean createAdminRole(boolean initialSetup, boolean groupExist,
                                    String adminRoleName, String adminUserName)
            throws UserStoreException {

        boolean created = Boolean.FALSE;
        if (groupExist) {
            // Create admin role since roles are maintained separately in the
            // database after the separation of groups and roles.
            try {
                hybridRoleManager.addHybridRole(adminRoleName, new String[]{adminUserName});
                created = Boolean.TRUE;
                // Assign the admin group to the admin role.
                this.updateGroupListOfHybridRole(adminRoleName, null, new String[]{adminRoleName});
            } catch (Exception e) {
                String message = "Admin role has not been created. " +
                        "Error occurs while creating Admin role in primary user store.";
                String warnMessage = String.format("Hybrid Admin Role :%s is already added. Hence, continue " +
                        "without adding the hybrid role.", adminRoleName);
                handleInitialSetupException(initialSetup, e,
                        ERROR_CODE_DUPLICATE_WHILE_ADDING_A_HYBRID_ROLE.getCode(), message, warnMessage);
            }
        } else {
            String message = "Admin group can not be created in primary user store. " +
                    "Add-Admin has been set to false. " +
                    "Please pick a Group name which is exist in the primary user store as Admin group";
            handleInitialSetup(initialSetup, message);
        }
        return created;
    }

    /**
     * Handles error scenarios in the initial set up when admin accounts are created.
     *
     * @param initialSetup         A flag to indicate whether this is the initial set up of the server.
     * @param message              The error message to be used in the exception or error log.
     * @throws UserStoreException  If this method is called during the initial set up.
     */
    private void handleInitialSetup(boolean initialSetup, String message)
            throws UserStoreException {

        if (initialSetup) {
            throw new UserStoreException(message);
        } else {
            log.error(message);
        }
    }

    /**
     * Handles caught exception scenarios in the initial set up when admin accounts are created.
     *
     * @param initialSetup         A flag to indicate whether this is the initial set up of the server.
     * @param e                    The exception being handled.
     * @param errorCode            The error code to be checked for in the exception being handled.
     * @param message              The message to be printed with the exception or error log.
     * @param warnMessage          The warn message to be printed if the error code of the exception.
     *                             matches the error code being checked for.
     * @throws UserStoreException  If this is called during the initial set up.
     */
    private void handleInitialSetupException(boolean initialSetup, Exception e, String errorCode, String message,
                                             String warnMessage)
            throws UserStoreException {

        if (initialSetup) {
            if (errorCode.equals(((UserStoreException) e).getErrorCode())) {
                log.warn(warnMessage);
            } else {
                throw new UserStoreException(message, e);
            }
        } else {
            log.error(message, e);
        }
    }

    /**
     * Checks whether a group exists.
     *
     * @param isReadGroupEnabled   A flag to indicate whether read groups is enabled.
     * @param adminGroupName       The group being checked for.
     * @return                     true if the groups exists and false otherwise.
     */
    private boolean doesGroupExist(boolean isReadGroupEnabled, String adminGroupName) {

        // Check whether the admin group exists in the user store.
        boolean groupExist = false;
        if (isReadGroupEnabled) {
            try {
                groupExist = doCheckExistingRole(adminGroupName);
            } catch (Exception e) {
                log.warn(String.format("Error occurs while checking the existence of the %s group " +
                        "in the user store.", adminGroupName), e);
            }
        }
        return groupExist;
    }

    private void updateHybridRoleListOfUserInternal(boolean initialSetup, String adminRoleName, String adminUserID)
            throws UserStoreException {

        if (!hybridRoleManager.isUserInRole(adminUserID, adminRoleName)) {
            try {
                hybridRoleManager.updateHybridRoleListOfUser(adminUserID, null, new String[] { adminRoleName });
            } catch (Exception e) {
                String message = "Admin user has not been assigned to Admin role. " + "Error while assignment is done";
                if (initialSetup) {
                    throw new UserStoreException(message, e);
                } else if (log.isDebugEnabled()) {
                    log.error(message, e);
                }
            }
        }
    }

    /**
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
            if (log.isDebugEnabled()) {
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
                    throw new UserStoreException("Cannot update persisted domain name " + oldDomain + " into " + newDomain + ". New domain name already in use");
                }
                realmConfigTmp = realmConfigTmp.getSecondaryRealmConfig();
            }

            if (log.isDebugEnabled()) {
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

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{RealmConfiguration.class, UserRealm.class};
            Object object = callSecure("createSecondaryUserStoreManager", new Object[]{realmConfig, realm}, argTypes);
            return (UserStoreManager) object;
        }

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
                    log.debug("Cannot initialize " + className + " using the option 2");
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

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{RealmConfiguration.class, UserRealm.class};
            callSecure("addSecondaryUserStoreManager", new Object[]{userStoreRealmConfig, realm}, argTypes);
            return;
        }

        boolean isDisabled = Boolean.parseBoolean(userStoreRealmConfig
                .getUserStoreProperty(UserCoreConstants.RealmConfig.USER_STORE_DISABLED));

        String domainName = userStoreRealmConfig
                .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);

        if (isDisabled) {
            log.warn("Secondary user store disabled with domain " + domainName + ".");
        } else {
            // Creating new UserStoreManager
            UserStoreManager manager = createSecondaryUserStoreManager(userStoreRealmConfig, realm);

            if (domainName != null) {
                if (this.getSecondaryUserStoreManager(domainName) != null) {
                    String errmsg = "Could not initialize new user store manager : " + domainName
                            + " Duplicate domain names not allowed.";
                    if (log.isDebugEnabled()) {
                        log.debug(errmsg);
                    }
                    throw new UserStoreException(errmsg);
                } else {
                    // Fulfilled requirements for adding UserStore,

                    // Now adding UserStoreManager to end of the UserStoreManager chain
                    UserStoreManager tmpUserStoreManager = this;
                    while (tmpUserStoreManager.getSecondaryUserStoreManager() != null) {
                        tmpUserStoreManager = tmpUserStoreManager.getSecondaryUserStoreManager();
                    }
                    tmpUserStoreManager.setSecondaryUserStoreManager(manager);

                    // update domainName-USM map to retrieve USM directly by its domain name
                    this.addSecondaryUserStoreManager(domainName.toUpperCase(), tmpUserStoreManager.getSecondaryUserStoreManager());

                    if (log.isDebugEnabled()) {
                        log.debug("UserStoreManager : " + domainName + "added to the list");
                    }
                }
            } else {
                log.warn("Could not initialize new user store manager.  "
                        + "Domain name is not defined");
            }
        }
    }

    @Override
    public Map<String, List<String>> getRoleListOfUsers(String[] userNames) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[] { String[].class };
            Object object = callSecure("getRoleListOfUsers", new Object[] { userNames }, argTypes);
            return (Map<String, List<String>>) object;
        }

        Map<String, List<String>> allRoleNames = new HashMap<>();
        Map<String, List<String>> domainFreeUsers = getDomainFreeUsers(userNames);

        for (Map.Entry<String, List<String>> entry : domainFreeUsers.entrySet()) {
            UserStoreManager secondaryUserStoreManager = getSecondaryUserStoreManager(entry.getKey());
            if (secondaryUserStoreManager instanceof AbstractUserStoreManager) {
                if (((AbstractUserStoreManager) secondaryUserStoreManager).isUniqueUserIdEnabled()) {
                    Map<String, List<String>> userRoles = ((AbstractUserStoreManager) secondaryUserStoreManager)
                            .doGetRoleListOfUsersWithID(getUserIDsFromUserNames(entry.getValue()), entry.getKey());
                    userRoles.forEach((key, value) -> {
                        try {
                            allRoleNames.put(getUserNameFromUserID(key), value);
                        } catch (UserStoreException ignored) {
                            // Ignore
                        }
                    });
                } else {
                    Map<String, List<String>> roleNames = ((AbstractUserStoreManager) secondaryUserStoreManager)
                            .doGetRoleListOfUsers(entry.getValue(), entry.getKey());
                    allRoleNames.putAll(roleNames);
                }
            }
        }

        for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
            if (listener instanceof AbstractUserOperationEventListener) {
                AbstractUserOperationEventListener newListener = (AbstractUserOperationEventListener) listener;
                if (!newListener.doPostGetRoleListOfUsers(userNames, allRoleNames)) {
                    break;
                }
            }
        }

        for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
            if (listener instanceof AbstractUserOperationEventListener) {
                AbstractUserOperationEventListener newListener = (AbstractUserOperationEventListener) listener;
                if (!newListener.doPostGetRoleListOfUsers(userNames, allRoleNames, this)) {
                    break;
                }
            }
        }

        return allRoleNames;
    }

    public Map<String, List<String>> doGetRoleListOfUsers(List<String> userNames, String domainName)
            throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[] { List.class, String.class };
            Object object = callSecure("doGetRoleListOfUsers", new Object[] { userNames, domainName }, argTypes);
            return (Map<String, List<String>>) object;
        }

        Map<String, List<String>> internalRoles = doGetInternalRoleListOfUsers(userNames, domainName);

        Map<String, List<String>> externalRoles = new HashMap<>();
        if (readGroupsEnabled) {
            externalRoles = doGetExternalRoleListOfUsers(userNames);
        }

        Map<String, List<String>> combinedRoles = new HashMap<>();
        if (!internalRoles.isEmpty() && !externalRoles.isEmpty()) {
            for (String userName : userNames) {
                List<String> roles = new ArrayList<>();
                if (internalRoles.get(userName) != null) {
                    roles.addAll(internalRoles.get(userName));
                }
                if (externalRoles.get(userName) != null) {
                    List<String> domainQualifiedRoleNames = getNamesWithDomain(externalRoles.get(userName), domainName);
                    roles.addAll(domainQualifiedRoleNames);
                }
                if (!roles.isEmpty()) {
                    combinedRoles.put(userName, roles);
                }
            }
        } else if (!internalRoles.isEmpty()) {
            combinedRoles = internalRoles;
        } else if (!externalRoles.isEmpty()) {
            combinedRoles = externalRoles;
        }

        return combinedRoles;
    }

    protected Map<String, List<String>> doGetExternalRoleListOfUsers(List<String> userNames) throws UserStoreException {

        Map<String, List<String>> externalRoleListOfUsers = new HashMap<>();
        for (String userName : userNames) {
            String[] externalRoles = doGetExternalRoleListOfUser(userName, null);
            if (!ArrayUtils.isEmpty(externalRoles)) {
                externalRoleListOfUsers.put(userName, Arrays.asList(externalRoles));
            }
        }
        return externalRoleListOfUsers;
    }

    protected Map<String, List<String>> doGetExternalRoleListOfUsersWithID(List<String> userIDs) throws UserStoreException {

        Map<String, List<String>> externalRoleListOfUsers = new HashMap<>();
        for (String userID : userIDs) {
            String[] externalRoles = doGetExternalRoleListOfUserWithID(userID, null);
            if (!ArrayUtils.isEmpty(externalRoles)) {
                externalRoleListOfUsers.put(userID, Arrays.asList(externalRoles));
            }
        }
        return externalRoleListOfUsers;
    }

    @Override
    public UserClaimSearchEntry[] getUsersClaimValues(String[] userNames, String[] claims, String profileName) throws
            UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String[].class, String[].class, String.class};
            Object object = callSecure("getUsersClaimValues", new Object[]{userNames, claims, profileName},
                    argTypes);
            return (UserClaimSearchEntry[]) object;
        }

        if (StringUtils.isEmpty(profileName)) {
            profileName = UserCoreConstants.DEFAULT_PROFILE;
        }

        if (claims == null) {
            claims = new String[0];
        }

        UserClaimSearchEntry[] allUsers = new UserClaimSearchEntry[0];
        Map<String, List<String>> domainFreeUsers = getDomainFreeUsers(userNames);

        for (Map.Entry<String, List<String>> entry : domainFreeUsers.entrySet()) {
            UserStoreManager secondaryUserStoreManager = getSecondaryUserStoreManager(entry.getKey());
            if (secondaryUserStoreManager instanceof AbstractUserStoreManager) {
                List<String> usersWithDomain = getNamesWithDomain(entry.getValue(), entry.getKey());
                if (((AbstractUserStoreManager) secondaryUserStoreManager).isUniqueUserIdEnabled()) {
                    List<UniqueIDUserClaimSearchEntry> uniqueIDUserClaimSearchEntries = ((AbstractUserStoreManager)
                            secondaryUserStoreManager).doGetUsersClaimValuesWithID(getUserIDsFromUserNames(
                            usersWithDomain), Arrays.asList(claims), entry.getKey(), profileName);

                    List<UserClaimSearchEntry> userClaimSearchEntries = getUserClaimSearchEntries(
                            uniqueIDUserClaimSearchEntries);
                    allUsers = (UserClaimSearchEntry[]) ArrayUtils
                            .addAll(userClaimSearchEntries.toArray(new UserClaimSearchEntry[0]), allUsers);
                } else {
                    UserClaimSearchEntry[] users = ((AbstractUserStoreManager) secondaryUserStoreManager)
                            .doGetUsersClaimValues(entry.getValue(), claims, entry.getKey(), profileName);
                    allUsers = (UserClaimSearchEntry[]) ArrayUtils.addAll(users, allUsers);
                }

            }
        }

        for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
            if (listener instanceof AbstractUserOperationEventListener) {
                AbstractUserOperationEventListener newListener = (AbstractUserOperationEventListener) listener;
                if (!newListener.doPostGetUsersClaimValues(userNames, claims, profileName, allUsers)) {
                    break;
                }
            }
        }

        for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
            if (listener instanceof AbstractUserOperationEventListener) {
                AbstractUserOperationEventListener newListener = (AbstractUserOperationEventListener) listener;
                if (!newListener.doPostGetUsersClaimValues(userNames, claims, profileName, allUsers, this)) {
                    break;
                }
            }
        }
        return allUsers;
    }

    public List<UserClaimSearchEntry> getUserClaimSearchEntries(
            List<UniqueIDUserClaimSearchEntry> uniqueIDUserClaimSearchEntries) {

        List<UserClaimSearchEntry> userClaimSearchEntries = new ArrayList<>();
        for (UniqueIDUserClaimSearchEntry uniqueIDUserClaimSearchEntry : uniqueIDUserClaimSearchEntries) {
            userClaimSearchEntries.add(uniqueIDUserClaimSearchEntry.getUserClaimSearchEntry());
        }
        return userClaimSearchEntries;
    }

    public UserClaimSearchEntry[] doGetUsersClaimValues(List<String> users, String[] claims, String domainName,
            String profileName) throws UserStoreException {

        Set<String> propertySet = new HashSet<>();
        Map<String, String> claimToAttributeMap = new HashMap<>();
        List<UserClaimSearchEntry> userClaimSearchEntryList = new ArrayList<>();
        for (String claim : claims) {
            String property;
            try {
                property = getClaimAtrribute(claim, null, domainName);
            } catch (org.wso2.carbon.user.api.UserStoreException e) {
                throw new UserStoreException(e);
            }
            propertySet.add(property);
            claimToAttributeMap.put(claim, property);
        }

        List<String> properties = new ArrayList<>(propertySet);

        List<String> roleAndGroupProperties = null;
        if (isGroupsVsRolesSeparationImprovementsEnabled(realmConfig)) {
            roleAndGroupProperties = getRolesAndGroupsClaimURIs().stream().map(claimToAttributeMap::get)
                    .filter(StringUtils::isNotBlank).collect(Collectors.toList());
            properties.removeAll(roleAndGroupProperties);
        }

        Map<String, Map<String, String>> userProperties = this.getUsersPropertyValues(users,
                properties.toArray(new String[]{}), profileName);

        if (isGroupsVsRolesSeparationImprovementsEnabled(realmConfig)) {
            // Inject group and roles attributes.
            if (CollectionUtils.isNotEmpty(roleAndGroupProperties)) {
                for (Map.Entry<String, Map<String, String>> userEntry : userProperties.entrySet()) {
                    List<String> claimsList = Arrays.asList(claims);
                    populateRoleGroupAttributes(claimsList, claimToAttributeMap, userEntry, Arrays.asList(
                            getRoleListOfUser(userEntry.getKey())), ROLE_CLAIM);
                    populateRoleGroupAttributes(claimsList, claimToAttributeMap, userEntry, Arrays.asList(
                            doGetInternalRoleListOfUser(userEntry.getKey(), "*")), INTERNAL_ROLES_CLAIM);
                    populateRoleGroupAttributes(claimsList, claimToAttributeMap, userEntry, Arrays.asList(
                            doGetExternalRoleListOfUser(userEntry.getKey(), "*")), USER_STORE_GROUPS_CLAIM);
                }
            }
        }

        for (Map.Entry<String, Map<String, String>> entry : userProperties.entrySet()) {
            UserClaimSearchEntry userClaimSearchEntry = new UserClaimSearchEntry();
            userClaimSearchEntry.setUserName(UserCoreUtil.addDomainToName(entry.getKey(), domainName));
            Map<String, String> userClaims = new HashMap<>();

            for (String claim : claims) {
                for (Map.Entry<String, String> userAttribute : entry.getValue().entrySet()) {
                    if (claimToAttributeMap.get(claim) != null && claimToAttributeMap.get(claim)
                            .equals(userAttribute.getKey())) {
                        userClaims.put(claim, userAttribute.getValue());
                    }
                }
            }
            userClaimSearchEntry.setClaims(userClaims);
            userClaimSearchEntryList.add(userClaimSearchEntry);

        }

        return userClaimSearchEntryList.toArray(new UserClaimSearchEntry[0]);
    }

    private Map<String, List<String>> getDomainFreeUsers(String[] userNames) {

        Map<String, List<String>> domainAwareUsers = new HashMap<>();
        if (ArrayUtils.isNotEmpty(userNames)) {
            for (String username : userNames) {
                String domainName = UserCoreUtil.extractDomainFromName(username);
                if (StringUtils.isEmpty(domainName)) {
                    domainName = UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME;
                }

                List<String> users = domainAwareUsers.get(domainName);
                if (users == null) {
                    users = new ArrayList<>();
                    domainAwareUsers.put(domainName.toUpperCase(), users);
                }
                users.add(UserCoreUtil.removeDomainFromName(username));
            }
        }

        return domainAwareUsers;
    }

    protected Map<String, Map<String, String>> getUsersPropertyValues(List<String> users, String[] propertyNames,
            String profileName) throws UserStoreException {

        Map<String, Map<String, String>> usersPropertyValuesMap = new HashMap<>();
        for (String userName : users) {
            Map<String, String> propertyValuesMap = getUserPropertyValues(userName, propertyNames, profileName);
            processAttributesAfterRetrieval(userName, propertyValuesMap, profileName);
            if (propertyValuesMap != null && !propertyValuesMap.isEmpty()) {
                usersPropertyValuesMap.put(userName, propertyValuesMap);
            }
        }
        return usersPropertyValuesMap;
    }

    /**
     * Remove given User Store Manager from USM chain
     *
     * @param userStoreDomainName
     * @throws UserStoreException
     */
    public void removeSecondaryUserStoreManager(String userStoreDomainName) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class};
            callSecure("removeSecondaryUserStoreManager", new Object[]{userStoreDomainName}, argTypes);
            return;
        }

        if (userStoreDomainName == null) {
            throw new UserStoreException("Cannot remove user store. User store domain name is null");
        }
        if ("".equals(userStoreDomainName)) {
            throw new UserStoreException("Cannot remove user store. User store domain name is empty");
        }
//    	if(!this.userStoreManagerHolder.containsKey(userStoreDomainName.toUpperCase())) {
//    		throw new UserStoreException("Cannot remove user store. User store domain name does not exists");
//    	}

        userStoreDomainName = userStoreDomainName.toUpperCase();

        boolean isUSMContainsInMap = false;
        if (this.userStoreManagerHolder.containsKey(userStoreDomainName.toUpperCase())) {
            isUSMContainsInMap = true;
            this.userStoreManagerHolder.remove(userStoreDomainName.toUpperCase());
            if (log.isDebugEnabled()) {
                log.debug("UserStore: " + userStoreDomainName + " removed from map");
            }
        }

        boolean isUSMConatainsInChain = false;
        UserStoreManager prevUserStoreManager = this;
        while (prevUserStoreManager.getSecondaryUserStoreManager() != null) {
            UserStoreManager secondaryUSM = prevUserStoreManager.getSecondaryUserStoreManager();
            if (secondaryUSM.getRealmConfiguration().getUserStoreProperty(UserStoreConfigConstants.DOMAIN_NAME).equalsIgnoreCase(userStoreDomainName)) {
                isUSMConatainsInChain = true;
                // Omit deleting user store manager from the chain
                prevUserStoreManager.setSecondaryUserStoreManager(secondaryUSM.getSecondaryUserStoreManager());
                log.info("User store: " + userStoreDomainName + " of tenant:" + tenantId + " is removed from user store chain.");
                return;
            }
            prevUserStoreManager = secondaryUSM;
        }

        if (!isUSMContainsInMap && isUSMConatainsInChain) {
            throw new UserStoreException("Removed user store manager : " + userStoreDomainName + " didnt exists in userStoreManagerHolder map");
        } else if (isUSMContainsInMap && !isUSMConatainsInChain) {
            throw new UserStoreException("Removed user store manager : " + userStoreDomainName + " didnt exists in user store manager chain");
        }
    }

    public HybridRoleManager getInternalRoleManager() {
        return hybridRoleManager;
    }

    @Override
    public String[] getUserList(String claim, String claimValue, String profileName, int limit, int offset) throws
            UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, String.class, String.class, int.class, int.class};
            Object object = callSecure("getUserList", new Object[]{claim, claimValue, profileName, limit, offset},
                    argTypes);
            return (String[]) object;
        }

        if (claim == null) {
            String errorCode = ErrorMessages.ERROR_CODE_INVALID_CLAIM_URI.getCode();
            String errorMessage = String.format(ErrorMessages.ERROR_CODE_INVALID_CLAIM_URI.getMessage(), "");
            handleGetUserListFailure(errorCode, errorMessage, null, claimValue, limit, offset, profileName);
            throw new IllegalArgumentException(ErrorMessages.ERROR_CODE_INVALID_CLAIM_URI.toString());
        }

        if (claimValue == null) {
            handleGetUserListFailure(ErrorMessages.ERROR_CODE_INVALID_CLAIM_VALUE.getCode(), ErrorMessages.
                    ERROR_CODE_INVALID_CLAIM_VALUE.getMessage(), claim, null, limit, offset, profileName);
            throw new IllegalArgumentException(ErrorMessages.ERROR_CODE_INVALID_CLAIM_VALUE.toString());
        }

        if (log.isDebugEnabled()) {
            log.debug("Listing and paginate users who having value as " + claimValue + " for the claim " + claim);
        }

        if (USERNAME_CLAIM_URI.equalsIgnoreCase(claim) || SCIM_USERNAME_CLAIM_URI.equalsIgnoreCase(claim) ||
                SCIM2_USERNAME_CLAIM_URI.equalsIgnoreCase(claim)) {

            if (log.isDebugEnabled()) {
                log.debug("Switching to paginate users using username");
            }

            String[] filteredUsers = listUsers(claimValue, limit, offset);

            if (log.isDebugEnabled()) {
                log.debug("Filtered users: " + Arrays.toString(filteredUsers));
            }

            return filteredUsers;
        }

        // Extracting the domain from claimValue.
        String extractedDomain = null;
        int index;
        index = claimValue.indexOf(CarbonConstants.DOMAIN_SEPARATOR);
        if (index > 0) {
            String names[] = claimValue.split(CarbonConstants.DOMAIN_SEPARATOR);
            extractedDomain = names[0].trim();
        }

        UserStoreManager userManager = this;
        if (StringUtils.isNotEmpty(extractedDomain) && !StringUtils.equalsIgnoreCase(getMyDomainName(), extractedDomain)) {
            UserStoreManager secondaryUserStoreManager = getSecondaryUserStoreManager(extractedDomain);
            if (secondaryUserStoreManager != null) {
                userManager = secondaryUserStoreManager;
                if (log.isDebugEnabled()) {
                    log.debug("Domain: " + extractedDomain + " is passed with the claim and user store manager is loaded"
                            + " for the given domain name.");
                }
            } else {
                throw new UserStoreClientException("Invalid Domain Name: " + extractedDomain);
            }
        }

        if (userManager instanceof JDBCUserStoreManager && (SCIM_USERNAME_CLAIM_URI.equalsIgnoreCase(claim) ||
                SCIM2_USERNAME_CLAIM_URI.equalsIgnoreCase(claim))) {
            if (userManager.isExistingUser(claimValue)) {
                return new String[]{claimValue};
            } else {
                return new String[0];
            }
        }

        claimValue = UserCoreUtil.removeDomainFromName(claimValue);

        final List<String> filteredUserList = new ArrayList<>();

        if (StringUtils.isNotEmpty(extractedDomain)) {
            try {
                for (UserOperationEventListener listener : UMListenerServiceComponent
                        .getUserOperationEventListeners()) {
                    if (listener instanceof AbstractUserOperationEventListener) {
                        AbstractUserOperationEventListener newListener = (AbstractUserOperationEventListener) listener;
                        if (!newListener.doPreGetUserList(claim, claimValue, limit, offset, filteredUserList, userManager)) {
                            handleGetUserListFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST.getCode(),
                                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST.getMessage(),
                                            UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), claim,
                                    claimValue, limit, offset, profileName);
                            break;
                        }
                    }
                }
            } catch (UserStoreException ex) {
                handleGetUserListFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST.getMessage(),
                                ex.getMessage()), claim, claimValue, limit, offset, profileName);
                throw ex;
            }
            if (log.isDebugEnabled()) {
                log.debug("Pre listener user list: " + filteredUserList + " for domain: " + extractedDomain);
            }
        }

        List<String> userNamesFromUserStore;
        if (isUniqueUserIdEnabled(userManager)) {
            List<User> usersFromUserStore = doGetUserListWithID(claim, claimValue, profileName, limit,
                    offset, extractedDomain, userManager);
            userNamesFromUserStore = usersFromUserStore.stream()
                    .map(User::getDomainQualifiedUsername).collect(Collectors.toList());
        } else {
            userNamesFromUserStore = doGetUserList(claim, claimValue, profileName, limit,
                    offset, extractedDomain, userManager);
        }

        if (log.isDebugEnabled()) {
            if (StringUtils.isNotEmpty(extractedDomain)) {
                log.debug("Users from user store: " + extractedDomain + " : " + userNamesFromUserStore);
            } else {
                log.debug("Users from all the user stores: " + userNamesFromUserStore);
            }
        }
        filteredUserList.addAll(userNamesFromUserStore);

        if (StringUtils.isNotEmpty(extractedDomain)) {
            handlePostGetUserList(claim, claimValue, filteredUserList, limit, offset, false);
            if (log.isDebugEnabled()) {
                log.debug("Post listener user list pagination: " + filteredUserList + " for domain: " + extractedDomain);
            }
        }

        return filteredUserList.toArray(new String[0]);
    }

    /**
     * Get the user count with claim value as a filter.
     *
     * @param claimUri  claim uri
     * @param filter filter or filter value with domain name (PRIMARY/* or *)
     * @return usersCountInUserStore
     * @throws UserStoreException UserStoreException
     */
    public long getUserCountWithClaims(String claimUri, String filter) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, String.class};
            Object object = callSecure("getUserCountWithClaims", new Object[]{claimUri,filter}, argTypes);
            return (long) object;
        }

        if (claimUri == null) {
            String errorCode = ErrorMessages.ERROR_CODE_NULL_CLAIM_URI.getCode();
            String errorMessage = String.format(ErrorMessages.ERROR_CODE_NULL_CLAIM_URI.getMessage());
            handleGetUserCountFailure(errorCode, errorMessage, null, filter);
            throw new IllegalArgumentException(ErrorMessages.ERROR_CODE_NULL_CLAIM_URI.toString());
        }

        if (filter == null) {
            handleGetUserCountFailure(ErrorMessages.ERROR_CODE_DOMAIN_VALUE_WITH_FILTER_EMPTY.getCode(),
                    ErrorMessages.ERROR_CODE_DOMAIN_VALUE_WITH_FILTER_EMPTY.getMessage(), claimUri, null);
            throw new IllegalArgumentException(ErrorMessages.ERROR_CODE_DOMAIN_VALUE_WITH_FILTER_EMPTY.toString());
        }

        if (log.isDebugEnabled()) {
            log.debug("Count users who having filter and domain " + filter + " for the claim " + claimUri);
        }

        String extractedDomain = null;
        int index;
        index = filter.indexOf(CarbonConstants.DOMAIN_SEPARATOR);
        if (index > 0) {
            String names[] = filter.split(CarbonConstants.DOMAIN_SEPARATOR);
            extractedDomain = names[0].trim();
        }

        if (StringUtils.isEmpty(extractedDomain)) {
            extractedDomain = UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME;
        }
        UserStoreManager userManager = getSecondaryUserStoreManager(extractedDomain);
        if (log.isDebugEnabled()) {
            log.debug("Domain: " + extractedDomain + " is passed with the claim and user store manager is loaded"
                    + " for the given domain name.");
        }

        String filterValue = UserCoreUtil.removeDomainFromName(filter);
        return getCountUsers(claimUri, filterValue, userManager);
    }

    @Override
    public String[] getUserList(Condition condition, String domain, String profileName, int limit, int offset, String sortBy, String
            sortOrder) throws UserStoreException {

        validateCondition(condition);
        if (StringUtils.isNotEmpty(sortBy) && StringUtils.isNotEmpty(sortOrder)) {
            throw new UserStoreException("Sorting is not supported.");
        }

        if (StringUtils.isEmpty(domain)) {
            domain = UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME;
        }

        if (StringUtils.isEmpty(profileName)) {
            profileName = UserCoreConstants.DEFAULT_PROFILE;
        }

        handlePreGetUserList(condition, domain, profileName, limit, offset, sortBy, sortOrder);

        if (log.isDebugEnabled()) {
            log.debug("Pre listener get conditional  user list for domain: " + domain);
        }

        String[] filteredUsers = new String[0];
        UserStoreManager secManager = getSecondaryUserStoreManager(domain);
        if (secManager != null) {
            if (secManager instanceof AbstractUserStoreManager) {
                if (((AbstractUserStoreManager) secManager).isUniqueUserIdEnabled()) {
                    UniqueIDPaginatedSearchResult users = ((AbstractUserStoreManager) secManager).doGetUserListWithID(condition,
                            profileName, limit, offset, sortBy, sortOrder);
                    addUsersToUserIdCache(users.getUsers());
                    addUsersToUserNameCache(users.getUsers());
                    filteredUsers = users.getUsers().stream().map(User::getUsername).toArray(String[]::new);
                } else {
                    PaginatedSearchResult users = ((AbstractUserStoreManager) secManager).doGetUserList(condition,
                            profileName, limit, offset, sortBy, sortOrder);
                    filteredUsers = users.getUsers();
                }
            }
        }

        handlePostGetUserList(condition, domain, profileName, limit, offset, sortBy, sortOrder, filteredUsers, false);

        if (log.isDebugEnabled()) {
            log.debug("post listener get conditional  user list for domain: " + domain);
        }
        return filteredUsers;
    }

    protected PaginatedSearchResult doGetUserList(Condition condition, String profileName, int limit, int offset,
                                                  String sortBy, String sortOrder) throws UserStoreException {

        return new PaginatedSearchResult();
    }

    protected UniqueIDPaginatedSearchResult doGetUserListWithID(Condition condition, String profileName, int limit,
            int offset, String sortBy, String sortOrder) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doGetUserListWithID operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException("doGetUserListWithID operation is not implemented in: " + this.getClass());
    }

    /**
     * Get username list which matches the condition.
     *
     * @param condition   Condition.
     * @param profileName Profile name.
     * @param limit       Limit.
     * @param offset      Offset.
     * @param sortBy      Sort by.
     * @param sortOrder   Sort order
     * @return List of usernames that matches the given condition.
     * @throws UserStoreException If an error occurred while listing the usernames based on the condition.
     */
    protected UniqueIDPaginatedUsernameSearchResult doGetUsernameListWithID(Condition condition, String profileName,
                                                                            int limit, int offset, String sortBy,
                                                                            String sortOrder)
            throws UserStoreException {

        UniqueIDPaginatedSearchResult uniqueIDPaginatedSearchResult =
                doGetUserListWithID(condition, profileName, limit, offset, sortBy, sortOrder);
        UniqueIDPaginatedUsernameSearchResult uniqueIDPaginatedUsernameSearchResult =
                new UniqueIDPaginatedUsernameSearchResult();
        uniqueIDPaginatedUsernameSearchResult.setUsers(
                uniqueIDPaginatedSearchResult.getUsers().stream().map(User::getUsername).collect(
                        Collectors.toList()));
        uniqueIDPaginatedUsernameSearchResult.setPaginatedSearchResult(
                uniqueIDPaginatedSearchResult.getPaginatedSearchResult());
        uniqueIDPaginatedUsernameSearchResult.setSkippedUserCount(
                uniqueIDPaginatedUsernameSearchResult.getSkippedUserCount());
        return uniqueIDPaginatedUsernameSearchResult;
    }


    @Override
    public String[] listUsers(String filter, int limit, int offset) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, int.class, int.class};
            Object object = callSecure("listUsers", new Object[]{filter, limit, offset}, argTypes);
            return (String[]) object;
        }

        int index = filter.indexOf(CarbonConstants.DOMAIN_SEPARATOR);
        PaginatedSearchResult userList;
        String[] users = new String[0];

        if (offset <= 0) {
            offset = 1;
        }

        if (index > 0) {
            String domain = filter.substring(0, index);

            UserStoreManager secManager = getSecondaryUserStoreManager(domain);
            if (secManager != null) {
                // Secondary UserStoreManager registered for this domain.
                filter = filter.substring(index + 1);
                if (secManager instanceof AbstractUserStoreManager) {
                    if (!((AbstractUserStoreManager) secManager).isUniqueUserIdEnabled()) {
                        userList = ((AbstractUserStoreManager) secManager).doListUsers(filter, limit, offset);
                    } else {
                        userList = ((AbstractUserStoreManager) secManager).doListUsersWithID(filter, limit, offset)
                                .getPaginatedSearchResult();
                    }
                    handlePostListPaginatedUsers(filter, limit, offset, new ArrayList<>(Arrays.asList(userList.getUsers())),
                            true);
                    return userList.getUsers();
                }
            }
        } else if (index == 0) {
            if (!isUniqueUserIdEnabled()) {
                userList = doListUsers(filter.substring(1), limit, offset);
            } else {
                userList = doListUsersWithID(filter.substring(1), limit, offset).getPaginatedSearchResult();
            }
            handlePostListPaginatedUsers(filter, limit, offset, new ArrayList<>(Arrays.asList(userList.getUsers())),
                    true);
            return userList.getUsers();
        }

        try {
            if (!isUniqueUserIdEnabled()) {
                userList = doListUsers(filter, limit, offset);
            } else {
                userList = doListUsersWithID(filter, limit, offset).getPaginatedSearchResult();
            }
            users = UserCoreUtil.combineArrays(users, userList.getUsers());
            limit = limit - users.length;
        } catch (UserStoreException ex) {
            handleGetPaginatedUserListFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_LISTING_PAGINATED_USERS.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_LISTING_PAGINATED_USERS.getMessage(),
                            ex.getMessage()), null, null, null);
            throw ex;
        }

        String primaryDomain = realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);

        int nonPaginatedUserCount = userList.getSkippedUserCount();
        if (this.getSecondaryUserStoreManager() != null) {
            for (Map.Entry<String, UserStoreManager> entry : userStoreManagerHolder.entrySet()) {
                if (limit <= 0) {
                    return users;
                }
                if (entry.getKey().equalsIgnoreCase(primaryDomain)) {
                    continue;
                }
                UserStoreManager storeManager = entry.getValue();
                if (storeManager instanceof AbstractUserStoreManager) {
                    try {
                        if (userList.getUsers().length > 0) {
                            offset = 1;
                        } else {
                            offset = offset - nonPaginatedUserCount;
                        }

                        PaginatedSearchResult secondUserList;
                        if (!((AbstractUserStoreManager) storeManager).isUniqueUserIdEnabled()) {
                          secondUserList  = ((AbstractUserStoreManager) storeManager)
                                    .doListUsers(filter, limit, offset);
                        } else {
                            secondUserList  = ((AbstractUserStoreManager) storeManager)
                                    .doListUsersWithID(filter, limit, offset).getPaginatedSearchResult();
                        }
                        nonPaginatedUserCount = secondUserList.getSkippedUserCount();
                        users = UserCoreUtil.combineArrays(users, secondUserList.getUsers());
                        limit = limit - users.length;
                    } catch (UserStoreException ex) {
                        handleGetPaginatedUserListFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_LISTING_PAGINATED_USERS
                                        .getCode(),
                                String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_LISTING_PAGINATED_USERS.getMessage(),
                                        ex.getMessage()), null, null, null);
                        // We can ignore and proceed. Ignore the results from this user store.
                        log.error(ex);
                    }
                }
            }
        }

        handlePostListPaginatedUsers(filter, limit, offset, new ArrayList<>(Arrays.asList(users)), true);
        return users;
    }

    protected PaginatedSearchResult doListUsers(String filter, int limit, int offset)
            throws UserStoreException{

        if (log.isDebugEnabled()) {
            log.debug("Operation is not implemented in: " + this.getClass());
        }
        return new PaginatedSearchResult();
    }

    protected UniqueIDPaginatedSearchResult doListUsersWithID(String filter, int limit, int offset)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doListUsersWithID operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException("doListUsersWithID operation is not implemented in: " + this.getClass());
    }

    /**
     * Get the count of Roles having a matching user name for the filter.
     *
     * @param filter the filter for the user name. Use '*' to have all.
     * @throws  UserStoreException UserStoreException
     */
    protected long doCountRoles(String filter) throws UserStoreException{

        throw new UserStoreException("Operation is not supported");
    }

    protected PaginatedSearchResult getUserListFromProperties(String property, String value, String profileName, int
            limit, int offset) throws UserStoreException {

        return new PaginatedSearchResult();
    }

    protected UniqueIDPaginatedSearchResult doGetUserListFromPropertiesWithID(String property, String value, String profileName,
            int limit, int offset) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doGetUserListFromPropertiesWithID operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException(
                "doGetUserListFromPropertiesWithID operation is not implemented in: " + this.getClass());
    }

    private void validateCondition(Condition condition) throws UserStoreException {

        if (condition instanceof ExpressionCondition) {
            if (isNotSupportedExpressionOperation(condition)) {
                throw new UserStoreException("Unsupported expression operation: " + condition.getOperation());
            }
        } else if (condition instanceof OperationalCondition) {
            Condition leftCondition = ((OperationalCondition) condition).getLeftCondition();
            validateCondition(leftCondition);
            Condition rightCondition = ((OperationalCondition) condition).getRightCondition();
            String operation = condition.getOperation();
            if (!OperationalOperation.AND.toString().equals(operation)) {
                throw new UserStoreException("Unsupported Conditional operation: " + condition.getOperation());
            }
            validateCondition(rightCondition);
        }
    }

    private boolean isNotSupportedExpressionOperation(Condition condition) {

        return !(ExpressionOperation.EQ.toString().equals(condition.getOperation()) ||
                ExpressionOperation.CO.toString().equals(condition.getOperation()) ||
                ExpressionOperation.SW.toString().equals(condition.getOperation()) ||
                ExpressionOperation.EW.toString().equals(condition.getOperation()) ||
                ExpressionOperation.GE.toString().equals(condition.getOperation()) ||
                ExpressionOperation.LE.toString().equals(condition.getOperation()));
    }

    private boolean isAnInternalRole(String roleName) {

        return roleName.toLowerCase().startsWith(APPLICATION_DOMAIN.toLowerCase()) || roleName.toLowerCase()
                .startsWith(UserCoreConstants.INTERNAL_DOMAIN.toLowerCase()) || roleName.toLowerCase()
                .startsWith(WORKFLOW_DOMAIN.toLowerCase());
    }

    private List<String> getUserStorePreferenceOrder() throws UserStoreException {

        UserMgtContext userMgtContext = UserCoreUtil.getUserMgtContextFromThreadLocal();
        if (userMgtContext != null) {
            // Retrieve the relevant supplier to generate the user store preference order.
            UserStorePreferenceOrderSupplier<List<String>> userStorePreferenceSupplier = userMgtContext.
                    getUserStorePreferenceOrderSupplier();
            if (userStorePreferenceSupplier != null) {
                // Generate the user store preference order.
                List<String> userStorePreferenceOrder = userStorePreferenceSupplier.get();
                if (userStorePreferenceOrder != null) {
                    return userStorePreferenceOrder;
                }
            }
        }
        return Collections.emptyList();
    }

    private boolean hasUserStorePreferenceChainGenerated() throws UserStoreException {

        return this instanceof IterativeUserStoreManager;
    }

    private boolean isUserStoreChainNeeded(List<String> userStorePreferenceOrder) throws UserStoreException {

        if (this instanceof IterativeUserStoreManager) {
            return false;
        }
        userStorePreferenceOrder.addAll(getUserStorePreferenceOrder());
        return CollectionUtils.isNotEmpty(userStorePreferenceOrder) && !hasUserStorePreferenceChainGenerated();
    }

    private boolean generateUserStoreChain(String userName, Object credential, boolean domainProvided,
                                           List<String> userStorePreferenceOrder) throws UserStoreException {

        // If domain name is provided, directly authenticate using the corresponding user store.
        if (domainProvided) {
            return authenticateFromProvidedUserStore(userName, credential, userStorePreferenceOrder);
        }
        // If domain is not provided, generate a user store chain.
        IterativeUserStoreManager initialUserStoreManager = null;
        IterativeUserStoreManager prevUserStoreManager = null;
        for (String domainName : userStorePreferenceOrder) {
            UserStoreManager userStoreManager = this.getSecondaryUserStoreManager(domainName);
            // If the user store manager is instance of AbstractUserStoreManager then generate a user store chain using
            // IterativeUserStoreManager.
            if (userStoreManager instanceof AbstractUserStoreManager) {
                if (initialUserStoreManager == null) {
                    prevUserStoreManager = new IterativeUserStoreManager((AbstractUserStoreManager) userStoreManager);
                    initialUserStoreManager = prevUserStoreManager;
                } else {
                    IterativeUserStoreManager currentUserStoreManager = new IterativeUserStoreManager(
                            (AbstractUserStoreManager) userStoreManager);
                    prevUserStoreManager.setNextUserStoreManager(currentUserStoreManager);
                    prevUserStoreManager = currentUserStoreManager;
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("UserStoreManager is not an instance of AbstractUserStoreManager hence authenticate the" +
                            " user through all the available user store list.");
                }
                return authenticateInternal(userName, credential, domainProvided);
            }
        }
        // Authenticate using the initial user store from the user store preference list.
        return initialUserStoreManager.authenticate(userName, credential);
    }

    /**
     * When authentication based on the user store preference order, if the username contains the user store domain,
     * directly authenticate using the corresponding user store instead of generating the user store chain.
     *
     * @param userName                  Username.
     * @param credential                Credential for authentication.
     * @param userStorePreferenceOrder  List of preferred user stores to be considered for authentication.
     * @return Authentication result
     * @throws UserStoreException
     */
    private boolean authenticateFromProvidedUserStore(String userName, Object credential,
                                                      List<String> userStorePreferenceOrder) throws UserStoreException {

        String providedDomainName = UserCoreUtil.extractDomainFromName(userName);
        // Check whether provided domain is in the preference list.
        if (!userStorePreferenceOrder.contains(providedDomainName)) {
            if (log.isDebugEnabled()) {
                log.debug("Authentication failure. Invalid username or password is provided.");
            }
            handleOnAuthenticateFailure(ErrorMessages.ERROR_CODE_ERROR_INCORRECT_CREDENTIAL.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_INCORRECT_CREDENTIAL.getMessage(),
                            "Authentication failed"), userName, credential);
            throw new UserStoreException("Authentication failed. Invalid username or password.");
        }
        UserStoreManager userStoreManager = this.getSecondaryUserStoreManager(providedDomainName);
        if (!(userStoreManager instanceof AbstractUserStoreManager)) {
            if (log.isDebugEnabled()) {
                log.debug("UserStoreManager is not an instance of AbstractUserStoreManager. Hence " +
                        "authenticate the user through all the available user store list.");
            }
            return authenticateInternal(userName, credential, true);
        }
        IterativeUserStoreManager iterativeUserStoreManager = new IterativeUserStoreManager(
                (AbstractUserStoreManager) userStoreManager);
        return iterativeUserStoreManager.authenticate(userName, credential);
    }

    private long getCountUsers(String claimUri, String filterValue, UserStoreManager userManager)
            throws UserStoreException {

        if (userManager instanceof AbstractUserStoreManager) {

            return ((AbstractUserStoreManager) userManager).countUsersWithClaims(claimUri, filterValue);

        } else {
            String msg = "Get user count is not supported by this user store: ";
            if (log.isDebugEnabled()) {
                log.debug(msg + userManager.getClass());
            }
            throw new UserStoreException(msg + userManager.getClass());
        }
    }

    private boolean isInternalRole(String domain) {

        return domain.equals("Internal") || domain.equals("Application");
    }

    private String removeDomainIfNotApplicationRole(String roleName) {

        String formattedRoleName;
        if (roleName.contains(UserCoreConstants.DOMAIN_SEPARATOR)
                && roleName.toLowerCase().startsWith(APPLICATION_DOMAIN.toLowerCase())) {
            formattedRoleName = roleName;
        } else {
            formattedRoleName = UserCoreUtil.removeDomainFromName(roleName);
        }
        return formattedRoleName;
    }

    private void createHybridRole(String roleName, String[] userList, org.wso2.carbon.user.api.Permission[] permissions)
            throws UserStoreException {

        // It is possible that the adding role could already exists at the table. But if concurrent requests were made,
        // it is possible that the adding role does not exists at this moment, but it still could exists at the
        // moment when DB query is called.
        try {
            hybridRoleManager.addHybridRole(roleName, userList);
        } catch (UserStoreException e) {
            // In case of a unique constraint violation.
            if (ERROR_CODE_ROLE_ALREADY_EXISTS.getCode().equals(e.getErrorCode())) {
                handleRoleAlreadyExistException(roleName, userList, permissions);
            }
            // Otherwise, the error is propagated.
            throw e;
        }
    }

    private void createSystemRole(String roleName, String[] userList, Permission[] permissions) throws UserStoreException {

        if (systemUserRoleManager.isExistingRole(roleName)) {
            handleRoleAlreadyExistException(roleName, userList, permissions);
        }

        // It is possible that the adding role could already exists at the table. But if concurrent requests were made,
        // it is possible that the adding role does not exists at this moment, but it still could exists at the
        // moment when DB query is called.
        try {
            systemUserRoleManager.addSystemRole(roleName, userList);
        } catch (UserStoreException e) {
            if (ERROR_CODE_DUPLICATE_WHILE_ADDING_A_SYSTEM_ROLE.getCode().contains(e.getErrorCode())) {
                // A unique constraint violation due to already existing role.
                handleRoleAlreadyExistException(roleName, userList, permissions);
            }
            throw e;
        }
    }

    @Override
    public final AuthenticationResult authenticateWithID(final List<LoginIdentifier> loginIdentifiers,
            final String domain, final Object credential) throws UserStoreException {

        try {
            return AccessController.doPrivileged((PrivilegedExceptionAction<AuthenticationResult>) () -> {
                if (!validateUserNameAndCredentials(loginIdentifiers, credential)) {
                    AuthenticationResult authenticationResult = new AuthenticationResult(
                            AuthenticationResult.AuthenticationStatus.FAIL);
                    authenticationResult.setFailureReason(new FailureReason("Invalid Credentials"));
                    return authenticationResult;
                }

                List<String> userStorePreferenceOrder = new ArrayList<>();
                // Check whether user store chain needs to be generated or not.
                if (isUserStoreChainNeeded(userStorePreferenceOrder)) {
                    if (log.isDebugEnabled()) {
                        log.debug("User store chain generation is needed hence generating the user store chain using "
                                + "the user" + " store preference order: " + userStorePreferenceOrder);
                    }
                    return generateUserStoreChainWithID(loginIdentifiers, domain, credential, userStorePreferenceOrder);
                } else {
                    // Authenticate the user.
                    return authenticateInternalWithID(loginIdentifiers, domain, credential);
                }

            });
        } catch (PrivilegedActionException e) {
            if (!(e.getException() instanceof UserStoreException)) {
                handleOnAuthenticateFailureWithID(ErrorMessages.ERROR_CODE_ERROR_WHILE_AUTHENTICATION.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_AUTHENTICATION.getMessage(), e.getMessage()),
                        loginIdentifiers, credential);
            }
            throw (UserStoreException) e.getException();
        }
    }

    private AuthenticationResult generateUserStoreChainWithID(List<LoginIdentifier> loginIdentifiers, String domain,
            Object credential, List<String> userStorePreferenceOrder) throws UserStoreException {

        IterativeUserStoreManager initialUserStoreManager = null;
        IterativeUserStoreManager prevUserStoreManager = null;
        for (String domainName : userStorePreferenceOrder) {
            UserStoreManager userStoreManager = this.getSecondaryUserStoreManager(domainName);
            // If the user store manager is instance of AbstractUserStoreManager then generate a user store chain using
            // IterativeUserStoreManager.
            if (userStoreManager instanceof AbstractUserStoreManager) {
                if (initialUserStoreManager == null) {
                    prevUserStoreManager = new IterativeUserStoreManager((AbstractUserStoreManager) userStoreManager);
                    initialUserStoreManager = prevUserStoreManager;
                } else {
                    IterativeUserStoreManager currentUserStoreManager = new IterativeUserStoreManager(
                            (AbstractUserStoreManager) userStoreManager);
                    prevUserStoreManager.setNextUserStoreManager(currentUserStoreManager);
                    prevUserStoreManager = currentUserStoreManager;
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("UserStoreManager is not an instance of AbstractUserStoreManager hence authenticate the"
                            + " user through all the available user store list.");
                }
                return authenticateInternalWithID(loginIdentifiers, domain, credential);
            }
        }
        // Authenticate using the initial user store from the user store preference list.
        return initialUserStoreManager.authenticateWithID(loginIdentifiers, domain, credential);
    }

    private AuthenticationResult authenticateInternalWithID(List<LoginIdentifier> loginIdentifiers, String domain,
            Object credential) throws UserStoreException {

        AbstractUserStoreManager abstractUserStoreManager = this;
        if (this instanceof IterativeUserStoreManager) {
            abstractUserStoreManager = ((IterativeUserStoreManager) this).getAbstractUserStoreManager();
        }
        boolean authenticated = false;
        AuthenticationResult authenticationResult = new AuthenticationResult(
                AuthenticationResult.AuthenticationStatus.FAIL);

        Secret credentialObj;
        try {
            credentialObj = Secret.getSecret(credential);
        } catch (UnsupportedSecretTypeException e) {
            handleOnAuthenticateFailureWithID(ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.getCode(),
                    ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.getMessage(), loginIdentifiers, credential);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.toString(), e);
        }

        try {
            try {
                for (UserOperationEventListener listener : UMListenerServiceComponent
                        .getUserOperationEventListeners()) {
                    Object credentialArgument;
                    if (listener instanceof SecretHandleableListener) {
                        credentialArgument = credentialObj;
                    } else {
                        credentialArgument = credential;
                    }

                    if (!((AbstractUserOperationEventListener) listener)
                            .doPreAuthenticateWithID(loginIdentifiers, credentialArgument, abstractUserStoreManager)) {
                        handleOnAuthenticateFailureWithID(
                                ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION.getCode(),
                                String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION.getMessage(),
                                        UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), loginIdentifiers,
                                credentialArgument);
                        authenticationResult.setAuthenticationStatus(AuthenticationResult.AuthenticationStatus.FAIL);
                        authenticationResult.setFailureReason(new FailureReason(
                                ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION.getMessage()));
                        return authenticationResult;
                    }
                }
            } catch (UserStoreException ex) {
                handleOnAuthenticateFailureWithID(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION.getMessage(),
                                ex.getMessage()), loginIdentifiers, credential);
                throw ex;
            }

            int tenantId = abstractUserStoreManager.getTenantId();

            try {

                for (LoginIdentifier loginIdentifier : loginIdentifiers) {
                    if (loginIdentifier.getLoginIdentifierType()
                            .equals(LoginIdentifier.LoginIdentifierType.CLAIM_URI)) {
                        String mappedAttribute = claimManager
                                .getAttributeName(getMyDomainName(), loginIdentifier.getLoginKey());
                        if (mappedAttribute != null) {
                            loginIdentifier.setLoginIdentifierType(LoginIdentifier.LoginIdentifierType.ATTRIBUTE);
                            loginIdentifier.setLoginKey(mappedAttribute);
                        }
                    }
                }

                RealmService realmService = UserCoreUtil.getRealmService();
                if (realmService != null) {
                    boolean tenantActive = realmService.getTenantManager().isTenantActive(tenantId);

                    if (!tenantActive) {
                        String errorCode = ErrorMessages.ERROR_CODE_TENANT_DEACTIVATED.getCode();
                        String errorMessage = String
                                .format(ErrorMessages.ERROR_CODE_TENANT_DEACTIVATED.getMessage(), tenantId);
                        log.warn(errorCode + " - " + errorMessage);
                        handleOnAuthenticateFailureWithID(errorCode, errorMessage, loginIdentifiers, credential);
                        authenticationResult.setAuthenticationStatus(AuthenticationResult.AuthenticationStatus.FAIL);
                        authenticationResult.setFailureReason(new FailureReason("Inactive Tenant: " + tenantId));
                        return authenticationResult;
                    }
                }
            } catch (org.wso2.carbon.user.api.UserStoreException e) {
                handleOnAuthenticateFailureWithID(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION.getMessage(),
                                e.getMessage()), loginIdentifiers, credential);
                throw new UserStoreException("Error while trying to check tenant status for Tenant : " + tenantId, e);
            }

            if (StringUtils.isNotEmpty(domain)) {
                UserStoreManager secUserStoreManager = abstractUserStoreManager.getSecondaryUserStoreManager(domain);
                if (isUniqueUserIdEnabled(secUserStoreManager)) {
                    authenticationResult = ((AbstractUserStoreManager) secUserStoreManager)
                            .doAuthenticateWithID(loginIdentifiers, credential);
                } else {
                    String userName = getUsernameByClaims(loginIdentifiers);
                    String userID = userUniqueIDManger.getUniqueId(userName, this);
                    boolean status = ((AbstractUserStoreManager) secUserStoreManager)
                            .doAuthenticate(userName, credential);
                    if (status) {
                        User user = getUser(userID, userName);
                        authenticationResult.setAuthenticationStatus(AuthenticationResult.AuthenticationStatus.SUCCESS);
                        authenticationResult.setAuthenticatedUser(user);
                    } else {
                        authenticationResult.setAuthenticationStatus(AuthenticationResult.AuthenticationStatus.FAIL);
                        authenticationResult.setFailureReason(new FailureReason("Authentication failed."));
                    }
                }

                if (authenticationResult.getAuthenticationStatus()
                        == AuthenticationResult.AuthenticationStatus.SUCCESS) {
                    authenticated = true;
                }
                if (authenticated) {
                    // Set domain in thread local variable for subsequent operations
                    UserCoreUtil.setDomainInThreadLocal(domain);
                }
            } else {
                // Domain is not provided. Try to authenticate with the current user store manager.
                // Validate whether circuit breaker is enabled and open.
                if (abstractUserStoreManager.isCircuitBreakerEnabledAndOpen()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Avoiding searching the " + abstractUserStoreManager.getMyDomainName()
                                + " domain as Circuit Breaker is in open state");
                    }
                    authenticated = false;
                } else {

                    if (abstractUserStoreManager.isUniqueUserIdEnabled()) {
                        authenticationResult = abstractUserStoreManager.doAuthenticateWithID(loginIdentifiers, credential);
                    } else {
                        String userName = getUsernameByClaims(loginIdentifiers);
                        String userID = userUniqueIDManger.getUniqueId(userName, abstractUserStoreManager);
                        boolean status = abstractUserStoreManager.doAuthenticate(userName, credential);
                        if (status) {
                            User user = getUser(userID, userName);
                            authenticationResult.setAuthenticationStatus(AuthenticationResult.AuthenticationStatus.SUCCESS);
                            authenticationResult.setAuthenticatedUser(user);
                        } else {
                            authenticationResult.setAuthenticationStatus(AuthenticationResult.AuthenticationStatus.FAIL);
                            authenticationResult.setFailureReason(new FailureReason("Authentication failed."));
                        }
                    }

                    if (authenticationResult.getAuthenticationStatus()
                            == AuthenticationResult.AuthenticationStatus.SUCCESS) {
                        authenticated = true;
                    }
                    if (authenticated) {
                        // Set domain in thread local variable for subsequent operations
                        UserCoreUtil
                                .setDomainInThreadLocal(UserCoreUtil.getDomainName(abstractUserStoreManager.realmConfig));
                    }
                }
            }
        } finally {
            credentialObj.clear();
        }

        // If authentication fails in the previous step and if the user has not specified a
        // domain- then we need to execute chained UserStoreManagers recursively.
        if (!authenticated && StringUtils.isEmpty(domain)) {
            AbstractUserStoreManager userStoreManager;
            if (this instanceof IterativeUserStoreManager) {
                IterativeUserStoreManager iterativeUserStoreManager = (IterativeUserStoreManager) this;
                userStoreManager = iterativeUserStoreManager.nextUserStoreManager();
            } else {
                userStoreManager = (AbstractUserStoreManager) abstractUserStoreManager.getSecondaryUserStoreManager();
            }
            if (userStoreManager != null) {
                authenticationResult = userStoreManager.authenticateWithID(loginIdentifiers, null, credential);
                if (authenticationResult.getAuthenticationStatus()
                        == AuthenticationResult.AuthenticationStatus.SUCCESS) {
                    authenticated = true;
                }
            }
        }

        if (!authenticated) {
            handleOnAuthenticateFailureWithID(ErrorMessages.ERROR_CODE_ERROR_INCORRECT_CREDENTIAL.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_INCORRECT_CREDENTIAL.getMessage(),
                            "Authentication failed"), loginIdentifiers, credential);
        }

        try {
            // You cannot change authentication decision in post handler to TRUE
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!((AbstractUserOperationEventListener) listener)
                        .doPostAuthenticateWithID(loginIdentifiers, authenticationResult, abstractUserStoreManager)) {
                    handleOnAuthenticateFailureWithID(
                            ErrorMessages.ERROR_CODE_ERROR_WHILE_POST_AUTHENTICATION.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_POST_AUTHENTICATION.getMessage(),
                                    UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), loginIdentifiers,
                            credential);

                    authenticationResult.setAuthenticationStatus(AuthenticationResult.AuthenticationStatus.FAIL);
                    authenticationResult.setFailureReason(
                            new FailureReason(ErrorMessages.ERROR_CODE_ERROR_WHILE_POST_AUTHENTICATION.getMessage()));
                    return authenticationResult;
                }
            }
        } catch (UserStoreException ex) {
            handleOnAuthenticateFailureWithID(ErrorMessages.ERROR_CODE_ERROR_WHILE_POST_AUTHENTICATION.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_POST_AUTHENTICATION.getMessage(),
                            ex.getMessage()), loginIdentifiers, credential);
            throw ex;
        }

        if (log.isDebugEnabled()) {
            if (!authenticated) {
                log.debug("Authentication failure. Wrong userID or password is provided.");
            }
        }

        return authenticationResult;
    }

    public String getUsernameByClaims(List<LoginIdentifier> loginIdentifiers) throws UserStoreException {

        if (loginIdentifiers.isEmpty()) {
            return null;
        }

        for (LoginIdentifier loginIdentifier : loginIdentifiers) {
            if (loginIdentifier.getLoginIdentifierType()
                    .equals(LoginIdentifier.LoginIdentifierType.CLAIM_URI)) {
                String mappedAttribute;
                try {
                    mappedAttribute = claimManager.getAttributeName(getMyDomainName(), loginIdentifier.getLoginKey());
                } catch (org.wso2.carbon.user.api.UserStoreException e) {
                    throw new UserStoreException(e);
                }
                if (mappedAttribute != null) {
                    loginIdentifier.setLoginIdentifierType(LoginIdentifier.LoginIdentifierType.ATTRIBUTE);
                    loginIdentifier.setLoginKey(mappedAttribute);
                }
            }
        }

        String userName = null;
        String[] resultedUserList = null;
        // Need to populate the claim email as the first element in the
        // passed array.
        for (LoginIdentifier loginIdentifier : loginIdentifiers) {

            String attribute = loginIdentifier.getLoginKey();
            String attributeValue = loginIdentifier.getLoginValue();

            if (attribute != null && attributeValue != null) {

                if (log.isDebugEnabled()) {
                    log.debug("Searching users for " + attribute + " with the value :" + attributeValue);
                }
                String[] matchedUserList = getUserListFromProperties(attribute, attributeValue, null);

                if (!ArrayUtils.isEmpty(matchedUserList)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Matched userList : " + Arrays.toString(matchedUserList));
                    }
                    // If more than one user find the first matching user list. Hence need to define unique claims
                    if (resultedUserList != null) {
                        List<String> users = new ArrayList<>();
                        for (String user : resultedUserList) {
                            for (String matchedUser : matchedUserList) {
                                if (user.equals(matchedUser)) {
                                    users.add(matchedUser);
                                }
                            }
                        }
                        if (users.size() > 0) {
                            resultedUserList = new String[users.size()];
                            users.toArray(resultedUserList);
                            if (log.isDebugEnabled()) {
                                log.debug("Current matching temporary userlist :" + Arrays.toString(resultedUserList));
                            }
                        } else {
                            if (log.isDebugEnabled()) {
                                log.debug("There are no users for " + attribute + " with the value : " + attributeValue
                                        + " in the previously filtered user list");
                            }
                            return null;
                        }
                    } else {
                        resultedUserList = matchedUserList;
                        if (log.isDebugEnabled()) {
                            log.debug("Current matching temporary userlist :" + Arrays.toString(resultedUserList));
                        }
                    }

                } else {
                    if (log.isDebugEnabled()) {
                        log.debug(
                                "There are no matching users for " + attribute + " with the value : " + attributeValue);
                    }
                    return null;
                }
            }
        }

        if (resultedUserList.length == 1) {
            userName = resultedUserList[0];
        } else {
            if (log.isDebugEnabled()) {
                log.debug("There are more than one user in the result set : " + Arrays.toString(resultedUserList));
            }
        }
        return userName;
    }

    @Override
    public final AuthenticationResult authenticateWithID(final String preferredUserNameClaim,
            final String preferredUserNameValue, final Object credential, final String profileName)
            throws UserStoreException {

        try {
            return AccessController.doPrivileged((PrivilegedExceptionAction<AuthenticationResult>) () -> {
                if (!validateUserNameAndCredential(preferredUserNameClaim, preferredUserNameValue, credential)) {
                    AuthenticationResult authenticationResult = new AuthenticationResult(
                            AuthenticationResult.AuthenticationStatus.FAIL);
                    authenticationResult.setFailureReason(new FailureReason("Invalid Credentials"));
                    return authenticationResult;
                }
                int index = preferredUserNameValue.indexOf(CarbonConstants.DOMAIN_SEPARATOR);
                boolean domainProvided = index > 0;
                return authenticateWithID(preferredUserNameClaim, preferredUserNameValue, credential, profileName,
                        domainProvided);
            });
        } catch (PrivilegedActionException e) {
            if (!(e.getException() instanceof UserStoreException)) {
                handleOnAuthenticateFailureWithID(ErrorMessages.ERROR_CODE_ERROR_WHILE_AUTHENTICATION.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_AUTHENTICATION.getMessage(), e.getMessage()),
                        preferredUserNameClaim, preferredUserNameValue, credential);
            }
            throw (UserStoreException) e.getException();
        }
    }

    /**
     * Given the preferred user name and a credential object, the method will validate whether the user can be
     * authenticated.
     *
     * @param preferredUserNameClaim The preferred user name claim.
     * @param preferredUserNameValue The preferred user name value.
     * @param credential             The credential of a user.
     * @param profileName            profile name.
     * @param domainProvided         Whether the domain is provided.
     * @return authenticated user.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    protected AuthenticationResult authenticateWithID(final String preferredUserNameClaim,
                                                      final String preferredUserNameValue,
                                                      final Object credential, final String profileName,
                                                      final boolean domainProvided) throws UserStoreException {

        try {
            return AccessController.doPrivileged(
                    (PrivilegedExceptionAction<AuthenticationResult>) ()
                            -> authenticateInternalIterationWithID(preferredUserNameClaim,
                            preferredUserNameValue, credential, profileName, domainProvided));
        } catch (PrivilegedActionException e) {
            throw (UserStoreException) e.getException();
        }

    }

    private AuthenticationResult authenticateInternalIterationWithID(String preferredUserNameClaim,
                                                                     String preferredUserNameValue,
                                                                     Object credential, String profileName,
                                                                     boolean domainProvided) throws UserStoreException {

        List<String> userStorePreferenceOrder = new ArrayList<>();
        // Check whether user store chain needs to be generated or not.
        if (isUserStoreChainNeeded(userStorePreferenceOrder)) {
            if (log.isDebugEnabled()) {
                log.debug("User store chain generation is needed hence generating the user store chain using the user"
                        + " store preference order: " + userStorePreferenceOrder);
            }
            return generateUserStoreChainWithID(preferredUserNameClaim, preferredUserNameValue, credential, profileName,
                    domainProvided, userStorePreferenceOrder);
        } else {
            // Authenticate the user.
            return authenticateInternalWithID(preferredUserNameClaim, preferredUserNameValue, credential, profileName,
                    domainProvided);
        }
    }

    private AuthenticationResult generateUserStoreChainWithID(String preferredUserNameClaim,
            String preferredUserNameValue, Object credential, String profileName, boolean domainProvided,
            List<String> userStorePreferenceOrder) throws UserStoreException {

        // If domain name is provided, directly authenticate using the corresponding user store.
        if (domainProvided) {
            String providedDomainName = UserCoreUtil.extractDomainFromName(preferredUserNameValue);
            // Check whether provided domain is in the preference list.
            if (!userStorePreferenceOrder.contains(providedDomainName)) {
                if (log.isDebugEnabled()) {
                    log.debug("Authentication failure. Invalid username or password is provided.");
                }
                handleOnAuthenticateFailure(ErrorMessages.ERROR_CODE_ERROR_INCORRECT_CREDENTIAL.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_INCORRECT_CREDENTIAL.getMessage(),
                                "Authentication failed"), preferredUserNameValue, credential);
                throw new UserStoreException("Authentication failed. Invalid username or password.");
            }
            UserStoreManager userStoreManager = this.getSecondaryUserStoreManager(providedDomainName);
            if (!(userStoreManager instanceof AbstractUserStoreManager)) {
                if (log.isDebugEnabled()) {
                    log.debug("UserStoreManager is not an instance of AbstractUserStoreManager. Hence " +
                            "authenticate the user through all the available user store list.");
                }
                return authenticateInternalWithID(preferredUserNameClaim, preferredUserNameValue, credential,
                        profileName, domainProvided);
            }
            IterativeUserStoreManager iterativeUserStoreManager = new IterativeUserStoreManager(
                    (AbstractUserStoreManager) userStoreManager);
            return iterativeUserStoreManager.
                    authenticateWithID(preferredUserNameClaim, preferredUserNameValue, credential, profileName);
        }
        // If domain is not provided, generate a user store chain.
        IterativeUserStoreManager initialUserStoreManager = null;
        IterativeUserStoreManager prevUserStoreManager = null;
        for (String domainName : userStorePreferenceOrder) {
            UserStoreManager userStoreManager = this.getSecondaryUserStoreManager(domainName);
            // If the user store manager is instance of AbstractUserStoreManager then generate a user store chain using
            // IterativeUserStoreManager.
            if (userStoreManager instanceof AbstractUserStoreManager) {
                if (initialUserStoreManager == null) {
                    prevUserStoreManager = new IterativeUserStoreManager((AbstractUserStoreManager) userStoreManager);
                    initialUserStoreManager = prevUserStoreManager;
                } else {
                    IterativeUserStoreManager currentUserStoreManager = new IterativeUserStoreManager(
                            (AbstractUserStoreManager) userStoreManager);
                    prevUserStoreManager.setNextUserStoreManager(currentUserStoreManager);
                    prevUserStoreManager = currentUserStoreManager;
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("UserStoreManager is not an instance of AbstractUserStoreManager hence authenticate the"
                            + " user through all the available user store list.");
                }
                return authenticateInternalWithID(preferredUserNameClaim, preferredUserNameValue, credential,
                        profileName, domainProvided);
            }
        }
        // Authenticate using the initial user store from the user store preference list.
        return initialUserStoreManager
                .authenticateWithID(preferredUserNameClaim, preferredUserNameValue, credential, profileName);
    }

    private AuthenticationResult authenticateInternalWithID(String preferredUserNameClaim,
            String preferredUserNameValue, Object credential, String profileName, boolean domainProvided)
            throws UserStoreException {

        AbstractUserStoreManager abstractUserStoreManager = this;
        if (this instanceof IterativeUserStoreManager) {
            abstractUserStoreManager = ((IterativeUserStoreManager) this).getAbstractUserStoreManager();
        }

        boolean authenticated = false;
        AuthenticationResult authenticationResult = new AuthenticationResult(
                AuthenticationResult.AuthenticationStatus.FAIL);

        UserStore userStore = abstractUserStoreManager.getUserStore(preferredUserNameValue);
        if (userStore.isRecurssive() && userStore.getUserStoreManager() instanceof AbstractUserStoreManager) {
            return ((AbstractUserStoreManager) userStore.getUserStoreManager())
                    .authenticateWithID(preferredUserNameClaim, userStore.getDomainFreeName(), credential, profileName,
                            domainProvided);
        }

        Secret credentialObj;
        try {
            credentialObj = Secret.getSecret(credential);
        } catch (UnsupportedSecretTypeException e) {
            handleOnAuthenticateFailureWithID(ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.getCode(),
                    ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.getMessage(), preferredUserNameClaim,
                    preferredUserNameValue, credential);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.toString(), e);
        }

        // #################### Domain Name Free Zone Starts Here ################################

        // #################### <Listeners> #####################################################
        try {
            for (UserStoreManagerListener listener : UMListenerServiceComponent.getUserStoreManagerListeners()) {
                Object credentialArgument;
                if (listener instanceof SecretHandleableListener) {
                    credentialArgument = credentialObj;
                } else {
                    credentialArgument = credential;
                }

                if (!((AbstractUserStoreManagerListener) listener)
                        .authenticateWithID(preferredUserNameClaim, preferredUserNameValue, credentialArgument,
                                abstractUserStoreManager)) {
                    handleOnAuthenticateFailureWithID(ErrorMessages.ERROR_CODE_ERROR_WHILE_AUTHENTICATION.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_AUTHENTICATION.getMessage(),
                            StringUtils.EMPTY), preferredUserNameClaim,
                            preferredUserNameValue, credentialArgument);

                    authenticationResult.setAuthenticationStatus(AuthenticationResult.AuthenticationStatus.FAIL);
                    authenticationResult.setFailureReason(
                            new FailureReason(String.format(
                                    ErrorMessages.ERROR_CODE_ERROR_WHILE_AUTHENTICATION.getMessage(),
                                    StringUtils.EMPTY)));
                    return authenticationResult;
                }
            }

            try {
                for (UserOperationEventListener listener : UMListenerServiceComponent
                        .getUserOperationEventListeners()) {
                    Object credentialArgument;
                    if (listener instanceof SecretHandleableListener) {
                        credentialArgument = credentialObj;
                    } else {
                        credentialArgument = credential;
                    }

                    if (!((AbstractUserOperationEventListener) listener)
                            .doPreAuthenticateWithID(preferredUserNameClaim, preferredUserNameValue, credentialArgument,
                                    abstractUserStoreManager)) {
                        handleOnAuthenticateFailureWithID(
                                ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION.getCode(),
                                String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION.getMessage(),
                                        UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE),
                                preferredUserNameClaim, preferredUserNameValue, credentialArgument);

                        authenticationResult.setAuthenticationStatus(AuthenticationResult.AuthenticationStatus.FAIL);
                        authenticationResult.setFailureReason(new FailureReason(
                                ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION.getMessage()));
                        return authenticationResult;
                    }
                }
            } catch (UserStoreException ex) {
                handleOnAuthenticateFailureWithID(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION.getMessage(),
                                ex.getMessage()), preferredUserNameClaim, preferredUserNameValue, credential);
                throw ex;
            }
            // #################### </Listeners> #####################################################

            int tenantId = abstractUserStoreManager.getTenantId();

            try {
                RealmService realmService = UserCoreUtil.getRealmService();
                if (realmService != null) {
                    boolean tenantActive = realmService.getTenantManager().isTenantActive(tenantId);

                    if (!tenantActive) {
                        String errorCode = ErrorMessages.ERROR_CODE_TENANT_DEACTIVATED.getCode();
                        String errorMessage = String
                                .format(ErrorMessages.ERROR_CODE_TENANT_DEACTIVATED.getMessage(), tenantId);
                        log.warn(errorCode + " - " + errorMessage);
                        handleOnAuthenticateFailureWithID(errorCode, errorMessage, preferredUserNameClaim,
                                preferredUserNameValue, credential);

                        authenticationResult.setAuthenticationStatus(AuthenticationResult.AuthenticationStatus.FAIL);
                        authenticationResult.setFailureReason(
                                new FailureReason(ErrorMessages.ERROR_CODE_TENANT_DEACTIVATED.getMessage()));
                        return authenticationResult;
                    }
                }
            } catch (org.wso2.carbon.user.api.UserStoreException e) {
                handleOnAuthenticateFailureWithID(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION.getMessage(),
                                e.getMessage()), preferredUserNameClaim, preferredUserNameValue, credential);
                throw new UserStoreException("Error while trying to check tenant status for Tenant : " + tenantId, e);
            }

            // We are here due to two reason. Either there is no secondary UserStoreManager or no
            // domain name provided with user name.

            try {
                String preferredUserNameProperty = abstractUserStoreManager.getClaimManager()
                        .getAttributeName(abstractUserStoreManager.getMyDomainName(), preferredUserNameClaim);
                // Let's authenticate with the primary UserStoreManager.

                // Validate whether circuit breaker is enabled and open.
                if (abstractUserStoreManager.isCircuitBreakerEnabledAndOpen()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Avoiding searching the " + abstractUserStoreManager.getMyDomainName()
                                + " domain as Circuit Breaker is in open state");
                    }
                    authenticated = false;
                } else {
                    if (abstractUserStoreManager.isUniqueUserIdEnabled()) {
                    authenticationResult = abstractUserStoreManager
                            .doAuthenticateWithID(preferredUserNameProperty, preferredUserNameValue, credentialObj,
                                    profileName);
                    } else {
                    List<String> users = new ArrayList<>();
                        if (preferredUserNameProperty.equals(getUserNameMappedAttribute())) {
                            users.add(UserCoreUtil.addDomainToName(preferredUserNameValue,
                                    abstractUserStoreManager.getMyDomainName()));
                        } else {
                            users = doGetUserList(preferredUserNameClaim, preferredUserNameValue, profileName,
                                    abstractUserStoreManager.getMyDomainName(), abstractUserStoreManager);
                        }
                        if (users.size() != 1) {
                            String message = "Users count matching to claim: " + preferredUserNameClaim + " and value: "
                                    + preferredUserNameValue + " is: " + users.size();
                            authenticationResult.setAuthenticationStatus(AuthenticationResult.AuthenticationStatus.FAIL);
                            authenticationResult.setFailureReason(new FailureReason(message));
                            if (log.isDebugEnabled()) {
                                log.debug(message);
                            }
                        } else {
                            boolean status = abstractUserStoreManager.doAuthenticate(UserCoreUtil.removeDomainFromName(
                                    users.get(0)), credentialObj);
                            authenticationResult = new AuthenticationResult(status ?
                                    AuthenticationResult.AuthenticationStatus.SUCCESS :
                                    AuthenticationResult.AuthenticationStatus.FAIL);
                            if (status) {
                                String userID = userUniqueIDManger.getUniqueId(users.get(0), this);
                                User user = userUniqueIDManger.
                                        getUser(userID, this, abstractUserStoreManager.getMyDomainName());
                                user.setTenantDomain(getTenantDomain(tenantId));
                                authenticationResult.setAuthenticatedUser(user);
                            } else {
                                authenticationResult.setFailureReason(new FailureReason("Invalid credentials."));
                            }
                        }
                    }
                    if (authenticationResult.getAuthenticationStatus()
                            == AuthenticationResult.AuthenticationStatus.SUCCESS) {
                        authenticated = true;
                    }
                }
            } catch (Exception e) {
                handleOnAuthenticateFailureWithID(ErrorMessages.ERROR_CODE_ERROR_WHILE_AUTHENTICATION.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_AUTHENTICATION.getMessage(), e.getMessage()),
                        preferredUserNameClaim, preferredUserNameValue, credential);
                // We can ignore and proceed. Ignore the results from this user store.
                // But throw the message to the upper level if it is a client exception.
                if (e instanceof UserStoreClientException) {
                    if (log.isDebugEnabled()) {
                        log.debug("Error occurred while authenticating user: " + preferredUserNameValue, e);
                    }
                    throw (UserStoreClientException) e;
                }
                log.error("Error occurred while authenticating user: " + preferredUserNameValue, e);
                authenticated = false;
            }

        } finally {
            credentialObj.clear();
        }

        if (authenticated) {
            // Set domain in thread local variable for subsequent operations
            UserCoreUtil.setDomainInThreadLocal(UserCoreUtil.getDomainName(abstractUserStoreManager.realmConfig));
        }

        // If authentication fails in the previous step and if the user has not specified a
        // domain- then we need to execute chained UserStoreManagers recursively.
        if (!authenticated && !domainProvided) {
            AbstractUserStoreManager userStoreManager;
            if (this instanceof IterativeUserStoreManager) {
                IterativeUserStoreManager iterativeUserStoreManager = (IterativeUserStoreManager) this;
                userStoreManager = iterativeUserStoreManager.nextUserStoreManager();
            } else {
                userStoreManager = (AbstractUserStoreManager) abstractUserStoreManager.getSecondaryUserStoreManager();
            }
            if (userStoreManager != null) {
                authenticationResult = userStoreManager
                        .authenticateWithID(preferredUserNameClaim, preferredUserNameValue, credential, profileName,
                                domainProvided);
            }
        }

        if (!authenticated) {
            handleOnAuthenticateFailureWithID(ErrorMessages.ERROR_CODE_ERROR_INCORRECT_CREDENTIAL.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_INCORRECT_CREDENTIAL.getMessage(),
                            "Authentication failed"), preferredUserNameClaim, preferredUserNameValue, credential);
        }

        try {
            // You cannot change authentication decision in post handler to TRUE
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!((AbstractUserOperationEventListener) listener)
                        .doPostAuthenticateWithID(preferredUserNameClaim, preferredUserNameValue, authenticationResult,
                                abstractUserStoreManager)) {
                    handleOnAuthenticateFailureWithID(
                            ErrorMessages.ERROR_CODE_ERROR_WHILE_POST_AUTHENTICATION.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_POST_AUTHENTICATION.getMessage(),
                                    UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), preferredUserNameClaim,
                            preferredUserNameValue, credential);
                    authenticationResult.setAuthenticationStatus(AuthenticationResult.AuthenticationStatus.FAIL);
                    authenticationResult.setFailureReason(
                            new FailureReason(ErrorMessages.ERROR_CODE_ERROR_WHILE_POST_AUTHENTICATION.getMessage()));
                    return authenticationResult;
                }
            }
        } catch (UserStoreException ex) {
            handleOnAuthenticateFailureWithID(ErrorMessages.ERROR_CODE_ERROR_WHILE_POST_AUTHENTICATION.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_POST_AUTHENTICATION.getMessage(),
                            ex.getMessage()), preferredUserNameClaim, preferredUserNameValue, credential);
            throw ex;
        }

        if (log.isDebugEnabled()) {
            if (!authenticated) {
                log.debug("Authentication failure. Wrong username or password is provided.");
            }
        }
        return authenticationResult;
    }

    @Override
    public final AuthenticationResult authenticateWithID(final String userID, final Object credential)
            throws UserStoreException {

        try {
            return AccessController.doPrivileged((PrivilegedExceptionAction<AuthenticationResult>) () -> {
                if (!validateUserIDAndCredential(userID, credential)) {
                    AuthenticationResult authenticationResult = new AuthenticationResult(
                            AuthenticationResult.AuthenticationStatus.FAIL);
                    authenticationResult.setFailureReason(new FailureReason("Invalid Credentials"));
                    return authenticationResult;
                }

                List<String> userStorePreferenceOrder = new ArrayList<>();
                // Check whether user store chain needs to be generated or not.
                if (isUserStoreChainNeeded(userStorePreferenceOrder)) {
                    if (log.isDebugEnabled()) {
                        log.debug(
                                "User store chain generation is needed hence generating the user store chain using "
                                        + "the user"
                                        + " store preference order: " + userStorePreferenceOrder);
                    }
                    return generateUserStoreChainWithID(userID, credential, userStorePreferenceOrder);
                } else {
                    // Authenticate the user.
                    return authenticateInternalWithID(userID, credential);
                }

            });
        } catch (PrivilegedActionException e) {
            if (!(e.getException() instanceof UserStoreException)) {
                handleOnAuthenticateFailureWithID(ErrorMessages.ERROR_CODE_ERROR_WHILE_AUTHENTICATION.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_AUTHENTICATION.getMessage(), e.getMessage()),
                        userID, credential);
            }
            throw (UserStoreException) e.getException();
        }
    }

    private AuthenticationResult generateUserStoreChainWithID(String userID, Object credential,
            List<String> userStorePreferenceOrder) throws UserStoreException {

        IterativeUserStoreManager initialUserStoreManager = null;
        IterativeUserStoreManager prevUserStoreManager = null;
        for (String domainName : userStorePreferenceOrder) {
            UserStoreManager userStoreManager = this.getSecondaryUserStoreManager(domainName);
            // If the user store manager is instance of AbstractUserStoreManager then generate a user store chain using
            // IterativeUserStoreManager.
            if (userStoreManager instanceof AbstractUserStoreManager) {
                if (initialUserStoreManager == null) {
                    prevUserStoreManager = new IterativeUserStoreManager((AbstractUserStoreManager) userStoreManager);
                    initialUserStoreManager = prevUserStoreManager;
                } else {
                    IterativeUserStoreManager currentUserStoreManager = new IterativeUserStoreManager(
                            (AbstractUserStoreManager) userStoreManager);
                    prevUserStoreManager.setNextUserStoreManager(currentUserStoreManager);
                    prevUserStoreManager = currentUserStoreManager;
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("UserStoreManager is not an instance of AbstractUserStoreManager hence authenticate the"
                            + " user through all the available user store list.");
                }
                return authenticateInternalWithID(userID, credential);
            }
        }
        // Authenticate using the initial user store from the user store preference list.
        return initialUserStoreManager.authenticateWithID(userID, credential);
    }

    private AuthenticationResult authenticateInternalWithID(String userID, Object credential)
            throws UserStoreException {

        UserStore userStoreWithID = getUserStoreWithID(userID);
        AbstractUserStoreManager abstractUserStoreManager = (AbstractUserStoreManager) userStoreWithID.getUserStoreManager();

        if (userStoreWithID.getUserStoreManager() instanceof IterativeUserStoreManager) {
            abstractUserStoreManager = ((IterativeUserStoreManager) userStoreWithID.getUserStoreManager()).getAbstractUserStoreManager();
        }
        boolean authenticated = false;
        AuthenticationResult authenticationResult = new AuthenticationResult(
                AuthenticationResult.AuthenticationStatus.FAIL);

        Secret credentialObj;
        try {
            credentialObj = Secret.getSecret(credential);
        } catch (UnsupportedSecretTypeException e) {
            handleOnAuthenticateFailureWithID(ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.getCode(),
                    ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.getMessage(), userID, credential);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.toString(), e);
        }

        try {
            try {
                for (UserOperationEventListener listener : UMListenerServiceComponent
                        .getUserOperationEventListeners()) {
                    Object credentialArgument;
                    if (listener instanceof SecretHandleableListener) {
                        credentialArgument = credentialObj;
                    } else {
                        credentialArgument = credential;
                    }

                    if (!((AbstractUserOperationEventListener) listener)
                            .doPreAuthenticateWithID(userID, credentialArgument, abstractUserStoreManager)) {
                        handleOnAuthenticateFailureWithID(
                                ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION.getCode(),
                                String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION.getMessage(),
                                        UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userID,
                                credentialArgument);
                        authenticationResult.setAuthenticationStatus(AuthenticationResult.AuthenticationStatus.FAIL);
                        authenticationResult.setFailureReason(new FailureReason(
                                ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION.getMessage()));
                        return authenticationResult;
                    }
                }
            } catch (UserStoreException ex) {
                handleOnAuthenticateFailureWithID(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION.getMessage(),
                                ex.getMessage()), userID, credential);
                throw ex;
            }

            int tenantId = abstractUserStoreManager.getTenantId();

            try {
                RealmService realmService = UserCoreUtil.getRealmService();
                if (realmService != null) {
                    boolean tenantActive = realmService.getTenantManager().isTenantActive(tenantId);

                    if (!tenantActive) {
                        String errorCode = ErrorMessages.ERROR_CODE_TENANT_DEACTIVATED.getCode();
                        String errorMessage = String
                                .format(ErrorMessages.ERROR_CODE_TENANT_DEACTIVATED.getMessage(), tenantId);
                        log.warn(errorCode + " - " + errorMessage);
                        handleOnAuthenticateFailureWithID(errorCode, errorMessage, userID, credential);
                        authenticationResult.setAuthenticationStatus(AuthenticationResult.AuthenticationStatus.FAIL);
                        authenticationResult.setFailureReason(new FailureReason("Inactive Tenant: " + tenantId));
                        return authenticationResult;
                    }
                }
            } catch (org.wso2.carbon.user.api.UserStoreException e) {
                handleOnAuthenticateFailureWithID(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION.getMessage(),
                                e.getMessage()), userID, credential);
                throw new UserStoreException("Error while trying to check tenant status for Tenant : " + tenantId, e);
            }

            // Validate whether circuit breaker is enabled and open.
            if (abstractUserStoreManager.isCircuitBreakerEnabledAndOpen()) {
                if (log.isDebugEnabled()) {
                    log.debug("Avoiding searching the " + abstractUserStoreManager.getMyDomainName()
                            + " domain as Circuit Breaker is in open state");
                }
                authenticated = false;
            } else {
                if (isUniqueUserIdEnabled(abstractUserStoreManager)) {
                    authenticationResult = ((AbstractUserStoreManager) abstractUserStoreManager)
                            .doAuthenticateWithID(userID, credential);
                } else {
                    User user = userUniqueIDManger
                            .getUser(userID, (AbstractUserStoreManager) abstractUserStoreManager);
                    boolean status = ((AbstractUserStoreManager) abstractUserStoreManager)
                            .doAuthenticate(user.getUsername(), credential);
                    if (status) {
                        user.setTenantDomain(getTenantDomain(tenantId));
                        authenticationResult.setAuthenticationStatus(AuthenticationResult.AuthenticationStatus.SUCCESS);
                        authenticationResult.setAuthenticatedUser(user);
                    } else {
                        authenticationResult.setAuthenticationStatus(AuthenticationResult.AuthenticationStatus.FAIL);
                        authenticationResult
                                .setFailureReason(new FailureReason("Authentication failed for userID: " + userID));
                    }
                }
                if (authenticationResult.getAuthenticationStatus()
                        == AuthenticationResult.AuthenticationStatus.SUCCESS) {
                    authenticated = true;
                }
                if (authenticated) {
                    // Set domain in thread local variable for subsequent operations
                    UserCoreUtil.setDomainInThreadLocal(userStoreWithID.getDomainName());
                }
            }
        } finally {
            credentialObj.clear();
        }

        if (!authenticated) {
            handleOnAuthenticateFailureWithID(ErrorMessages.ERROR_CODE_ERROR_INCORRECT_CREDENTIAL.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_INCORRECT_CREDENTIAL.getMessage(),
                            "Authentication failed"), userID, credential);
        }

        try {
            // You cannot change authentication decision in post handler to TRUE
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!((AbstractUserOperationEventListener) listener)
                        .doPostAuthenticateWithID(userID, authenticationResult, abstractUserStoreManager)) {
                    handleOnAuthenticateFailureWithID(
                            ErrorMessages.ERROR_CODE_ERROR_WHILE_POST_AUTHENTICATION.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_POST_AUTHENTICATION.getMessage(),
                                    UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), userID, credential);

                    authenticationResult.setAuthenticationStatus(AuthenticationResult.AuthenticationStatus.FAIL);
                    authenticationResult.setFailureReason(
                            new FailureReason(ErrorMessages.ERROR_CODE_ERROR_WHILE_POST_AUTHENTICATION.getMessage()));
                    return authenticationResult;
                }
            }
        } catch (UserStoreException ex) {
            handleOnAuthenticateFailureWithID(ErrorMessages.ERROR_CODE_ERROR_WHILE_POST_AUTHENTICATION.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_POST_AUTHENTICATION.getMessage(),
                            ex.getMessage()), userID, credential);
            throw ex;
        }

        if (log.isDebugEnabled()) {
            if (!authenticated) {
                log.debug("Authentication failure. Wrong userID or password is provided.");
            }
        }

        return authenticationResult;
    }

    @Override
    public final List<User> listUsersWithID(String filter, int maxItemLimit) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[] { String.class, int.class };
            Object object = callSecure("listUsersWithID", new Object[] { filter, maxItemLimit }, argTypes);
            return (List<User>) object;
        }

        int index;
        index = filter.indexOf(CarbonConstants.DOMAIN_SEPARATOR);
        List<User> userList;

        // Check whether we have a secondary UserStoreManager setup.
        if (index > 0) {
            // Using the short-circuit. User name comes with the domain name.
            String domain = filter.substring(0, index);

            UserStoreManager secManager = getSecondaryUserStoreManager(domain);
            if (secManager != null) {
                // We have a secondary UserStoreManager registered for this domain.
                filter = filter.substring(index + 1);
                if (secManager instanceof AbstractUserStoreManager) {
                    // If unique id feature is not enabled, we have to call the legacy methods.
                    if (!isUniqueUserIdEnabled(secManager)) {
                        userList = userUniqueIDManger.listUsers(doListUsers(filter, maxItemLimit), this);
                    } else {
                        userList = ((AbstractUserStoreManager) secManager)
                                .doListUsersWithID(filter, maxItemLimit);
                    }
                    handlePostGetUserListWithID(null, null, userList, true);
                    return userList;
                }
            }
        } else if (index == 0) {
            if (!isUniqueUserIdEnabled()) {
                userList = userUniqueIDManger.listUsers(doListUsers(filter.substring(1), maxItemLimit), this);
            } else {
                userList = listUsersWithID(filter.substring(1), maxItemLimit);
            }

            handlePostGetUserListWithID(null, null, userList, true);
            return userList;
        }

        try {
            if (!isUniqueUserIdEnabled()) {
                userList = userUniqueIDManger.listUsers(doListUsers(filter, maxItemLimit), this);
            } else {
                userList = doListUsersWithID(filter, maxItemLimit);
            }
        } catch (UserStoreException ex) {
            handleGetUserListFailureWithID(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_USER_LIST.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_USER_LIST.getMessage(), ex.getMessage()),
                    null, null, null);
            throw ex;
        }

        String primaryDomain = realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);

        if (this.getSecondaryUserStoreManager() != null) {
            for (Map.Entry<String, UserStoreManager> entry : userStoreManagerHolder.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(primaryDomain)) {
                    continue;
                }
                UserStoreManager storeManager = entry.getValue();
                if (storeManager instanceof AbstractUserStoreManager) {
                    try {
                        List<User> secondUserList;
                        if (!isUniqueUserIdEnabled(storeManager)) {
                            secondUserList = userUniqueIDManger.listUsers(((AbstractUserStoreManager) storeManager)
                                    .doListUsers(filter, maxItemLimit), this);
                        } else {
                            secondUserList = ((AbstractUserStoreManager) storeManager)
                                    .doListUsersWithID(filter, maxItemLimit);
                        }
                        userList.addAll(secondUserList);
                    } catch (UserStoreException ex) {
                        handleGetUserListFailureWithID(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_USER_LIST.getCode(),
                                String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_USER_LIST.getMessage(),
                                        ex.getMessage()), null, null, null);

                        // We can ignore and proceed. Ignore the results from this user store.
                        log.error(ex);
                    }
                } else {
                    List<User> secondUserList = ((UniqueIDUserStoreManager) storeManager)
                            .listUsersWithID(filter, maxItemLimit);
                    userList.addAll(secondUserList);
                }
            }
        }

        handlePostGetUserListWithID(null, null, userList, true);
        return userList;
    }

    @Override
    public User updateUserName(String userID, String newUserName) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("updateUserName operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException("updateUserName operation is not implemented in: " + this.getClass());
    }

    @Override
    public User getUserWithID(String userID, String[] requestedClaims, String profileName) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class[] argTypes = new Class[] { String.class, String[].class, String.class };
            Object object = callSecure("getUserWithID", new Object[] { userID, requestedClaims, profileName },
                    argTypes);
            return (User) object;
        }

        UserStore userStore = getUserStoreWithID(userID);
        if (userStore.isRecurssive()) {
            return ((AbstractUserStoreManager) userStore.getUserStoreManager())
                    .getUserWithID(userStore.getDomainFreeUserId(), requestedClaims, profileName);
        }

        boolean isUniqueIdEnabled = isUniqueUserIdEnabledInUserStore(userStore);
        User user = null;
        boolean isUserExists;
        if (isUniqueIdEnabled) {
            isUserExists = doCheckExistingUserWithID(userID);
        } else {
            user = userUniqueIDManger.getUser(userID, this, userStore.getDomainName());
            isUserExists = user != null;
        }

        // #################### Domain Name Free Zone Starts Here ################################
        if (!isUserExists) {
            String errorMessage = String.format(ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getMessage(), userID,
                    realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME));
            String errorCode = ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getCode();
            handleGetUserFailureWithID(errorCode, errorMessage, userID, requestedClaims, profileName);
            throw new UserStoreException(errorCode + " - " + errorMessage, errorCode);
        }
        // check for null claim list
        if (requestedClaims == null) {
            requestedClaims = new String[0];
        }

        try {
            // If unique id feature is not enabled, we have to call the legacy methods.
            if (!isUniqueUserIdEnabledInUserStore(userStore)) {
                if (user == null) {
                    user = userUniqueIDManger.getUser(userID, this, userStore.getDomainName());
                }
            } else {
                user = getUserFromID(userID, requestedClaims, userStore.getDomainName(), profileName);
            }
        } catch (UserStoreException ex) {
            handleGetUserFailureWithID(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_CLAIM_VALUES.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_CLAIM_VALUES.getMessage(),
                            ex.getMessage()), userID, requestedClaims, profileName);
            throw ex;
        }

        // #################### <Listeners> #####################################################
        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (listener instanceof AbstractUserOperationEventListener) {
                    AbstractUserOperationEventListener newListener = (AbstractUserOperationEventListener) listener;
                    if (!newListener.doPostGetUserWithID(userID, requestedClaims, profileName, user, this)) {
                        handleGetUserFailureWithID(ErrorMessages.ERROR_CODE_ERROR_IN_POST_GET_CLAIM_VALUES.getCode(),
                                String.format(ErrorMessages.ERROR_CODE_ERROR_IN_POST_GET_CLAIM_VALUES.getMessage(),
                                        UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), userID,
                                requestedClaims, profileName);
                        break;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleGetUserFailureWithID(ErrorMessages.ERROR_CODE_ERROR_IN_POST_GET_CLAIM_VALUES.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_IN_POST_GET_CLAIM_VALUES.getMessage(),
                            ex.getMessage()), userID, requestedClaims, profileName);
            throw ex;
        }
        // #################### </Listeners> #####################################################

        return user;
    }

    @Override
    public boolean isExistingUserWithID(String userID) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[] { String.class };
            Object object = callSecure("isExistingUserWithID", new Object[] { userID }, argTypes);
            return (Boolean) object;
        }

        if (UserCoreUtil.isRegistrySystemUser(userID)) {
            return true;
        }

        UserStore userStore = getUserStoreWithID(userID);
        if (userStore.isRecurssive()) {
            return ((AbstractUserStoreManager) userStore.getUserStoreManager())
                    .isExistingUserWithID(userStore.getDomainFreeUserId());
        }

        // #################### Domain Name Free Zone Starts Here ################################

        if (userStore.isSystemStore()) {
            return systemUserRoleManager.isExistingSystemUser(userID);
        }

        // If unique id feature is not enabled, we have to call the legacy methods.
        if (!isUniqueUserIdEnabledInUserStore(userStore)) {
            User user = userUniqueIDManger.getUser(userID, this, userStore.getDomainName());
            if (user == null) {
                return false;
            }
            return doCheckExistingUser(user.getUsername());
        } else {
            return doCheckExistingUserWithID(userID);
        }
    }

    @Override
    public List<String> getRoleListOfUserWithID(String userID) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[] { String.class };
            Object object = callSecure("getRoleListOfUserWithID", new Object[] { userID }, argTypes);
            return (List<String>) object;
        }

        List<String> roleNames;

        // anonymous user is only assigned to  anonymous role
        if (CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equalsIgnoreCase(userID)) {
            return new ArrayList<String>(){{ add(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME); }};
        }

        UserStore userStore = getUserStoreWithID(userID);
        if (userStore.isRecurssive()) {
            return ((AbstractUserStoreManager) userStore.getUserStoreManager())
                    .getRoleListOfUserWithID(userStore.getDomainFreeUserId());
        }

        return getRolesListOfUserWithId(userID, userStore);
    }

    /**
     * Get the roles list of user with user id when the UserStore is resolved.
     *
     * @param userID User Id.
     * @param userStore Resolved userstore.
     * @return Roles list of the user.
     * @throws UserStoreException If an error occurred while getting the roles from the given userstore.
     */
    private List<String> getRolesListOfUserWithId(String userID, UserStore userStore) throws UserStoreException {

        List<String> roleNames;
        // Check whether roles exist in cache
        String userName = this.getUserNameFromUserID(userID);
        if (StringUtils.isNotEmpty(userName)) {
            String[] roleListOfUserFromCache = getRoleListOfUserFromCache(this.tenantId, userName);
            if (roleListOfUserFromCache != null) {
                roleNames = Arrays.asList(roleListOfUserFromCache);
                if (roleNames.size() > 0) {
                    return roleNames;
                }
            }
        }

        if (userStore.isSystemStore()) {
            return Arrays.asList(systemUserRoleManager.getSystemRoleListOfUser(userStore.getDomainFreeUserId()));
        }
        // #################### Domain Name Free Zone Starts Here ################################

        // If unique id feature is not enabled, we have to call the legacy methods.
        if (!isUniqueUserIdEnabledInUserStore(userStore)) {
            if (StringUtils.isEmpty(userName)) {
                return Arrays.asList(realmConfig.getEveryOneRoleName());
            }
            return Arrays.asList(doGetRoleListOfUser(userName, "*"));
        } else {
            return doGetRoleListOfUserWithID(userID, "*");
        }
    }

    @Override
    public final List<User> getUserListOfRoleWithID(String roleName) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[] { String.class };
            Object object = callSecure("getUserListOfRoleWithID", new Object[] { roleName }, argTypes);
            return (List<User>) object;
        }

        return getUserListOfRoleWithID(roleName, QUERY_FILTER_STRING_ANY, QUERY_MAX_ITEM_LIMIT_ANY);
    }

    @Override
    public final List<User> getUserListOfRoleWithID(String roleName, String filter, int maxItemLimit)
            throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[] { String.class, String.class, int.class };
            Object object = callSecure("getUserListOfRoleWithID", new Object[] { roleName, filter, maxItemLimit },
                    argTypes);
            return (List<User>) object;
        }

        List<User> users = new ArrayList<>();

        // If role does not exit, just return
        if (!isExistingRole(roleName)) {
            handleDoPostGetUserListOfRoleWithID(roleName, users);
            return users;
        }

        UserStore userStore = getUserStoreOfRoles(roleName);

        if (userStore.isRecurssive()) {
            UserStoreManager resolvedUserStoreManager = userStore.getUserStoreManager();
            if (resolvedUserStoreManager instanceof AbstractUserStoreManager) {
                return ((AbstractUserStoreManager) resolvedUserStoreManager)
                        .getUserListOfRoleWithID(userStore.getDomainFreeName(), filter, maxItemLimit);
            } else {
                return ((UniqueIDUserStoreManager) resolvedUserStoreManager)
                        .getUserListOfRoleWithID(userStore.getDomainFreeName());
            }
        }

        // #################### Domain Name Free Zone Starts Here
        // ################################

        if (userStore.isSystemStore()) {
            String[] userArray = systemUserRoleManager.getUserListOfSystemRole(userStore.getDomainFreeName());
            List<User> userList = UserCoreUtil.getUserList(userArray);
            handleDoPostGetUserListOfRoleWithID(roleName, userList);
            return userList;
        }

        String[] userNamesInHybrid;
        if (userStore.isHybridRole()) {
            if (UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(userStore.getDomainName())) {
                userNamesInHybrid = hybridRoleManager.getUserListOfHybridRole(userStore.getDomainFreeName());
            } else {
                userNamesInHybrid = hybridRoleManager.getUserListOfHybridRole(userStore.getDomainAwareName());
            }

            // Get the users of associated groups of the role.
            if (isRoleAndGroupSeparationEnabled()) {
                Set<String> userListOfGroups = new HashSet<>();
                String[] groupsOfRole;
                if (UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(userStore.getDomainName())) {
                    groupsOfRole = hybridRoleManager.getGroupListOfHybridRole(userStore.getDomainFreeName());
                } else {
                    groupsOfRole = hybridRoleManager.getGroupListOfHybridRole(userStore.getDomainAwareName());
                }
                for (String group : groupsOfRole) {
                    userListOfGroups.addAll(Arrays.asList(getUserListOfRole(group, filter, maxItemLimit)));
                }
                userNamesInHybrid = UserCoreUtil.combine(userNamesInHybrid, new ArrayList<>(userListOfGroups));
            }

            // remove domain
            List<String> finalNameList = new ArrayList<>();
            String displayNameAttribute = this.realmConfig.getUserStoreProperty(LDAPConstants.DISPLAY_NAME_ATTRIBUTE);

            if (userNamesInHybrid != null && userNamesInHybrid.length > 0) {
                if (displayNameAttribute != null && displayNameAttribute.trim().length() > 0) {
                    for (String userName : userNamesInHybrid) {
                        String domainName = UserCoreUtil.extractDomainFromName(userName);
                        if (domainName == null || domainName.trim().length() == 0) {
                            finalNameList.add(userName);
                        }
                        UserStoreManager userManager = userStoreManagerHolder.get(domainName);
                        userName = UserCoreUtil.removeDomainFromName(userName);
                        if (userManager != null) {
                            String[] displayNames;
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
                    List<User> usersInHybrid = userUniqueIDManger.listUsers(userNamesInHybrid, this);
                    handleDoPostGetUserListOfRoleWithID(roleName, usersInHybrid);
                    return usersInHybrid;
                }
            }
            List<User> usersList = userUniqueIDManger.listUsers(finalNameList, this);
            handleDoPostGetUserListOfRoleWithID(roleName, usersList);
            return usersList;
        }
        if (readGroupsEnabled) {
            // If unique id feature is not enabled, we have to call the legacy methods.
            if (!isUniqueUserIdEnabledInUserStore(userStore)) {
                users = userUniqueIDManger.listUsers(doGetUserListOfRole(roleName, filter, maxItemLimit), this);
            } else {
                users = doGetUserListOfRoleWithID(roleName, filter, maxItemLimit);
            }
            handleDoPostGetUserListOfRoleWithID(roleName, users);
        }
        return users;
    }

    @Override
    public final List<User> getUserListOfGroupWithID(String groupName) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class};
            Object object = callSecure("getUserListOfGroupWithID", new Object[]{groupName}, argTypes);
            return (List<User>) object;
        }

        return getUserListOfGroupWithID(groupName, QUERY_FILTER_STRING_ANY, QUERY_MAX_ITEM_LIMIT_ANY);
    }

    @Override
    public final List<User> getUserListOfGroupWithID(String groupName, String filter, int maxItemLimit)
            throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, String.class, int.class};
            Object object = callSecure("getUserListOfGroupWithID", new Object[]{groupName, filter, maxItemLimit},
                    argTypes);
            return (List<User>) object;
        }

        List<User> users = new ArrayList<>();

        // If group does not exit, just return
        if (!isExistingRole(groupName)) {
            handleDoPostGetUserListOfRoleWithID(groupName, users);
            return users;
        }

        UserStore userStore = getUserStoreOfRoles(groupName);

        if (userStore.isRecurssive()) {
            UserStoreManager resolvedUserStoreManager = userStore.getUserStoreManager();
            if (resolvedUserStoreManager instanceof AbstractUserStoreManager) {
                return ((AbstractUserStoreManager) resolvedUserStoreManager)
                        .getUserListOfGroupWithID(userStore.getDomainFreeName(), filter, maxItemLimit);
            }
            return ((UniqueIDUserStoreManager) resolvedUserStoreManager)
                    .getUserListOfGroupWithID(userStore.getDomainFreeName());
        }

        // #################### Domain Name Free Zone Starts Here ################################

        if (userStore.isSystemStore() || userStore.isHybridRole()) {
            // If the passed group is a role and if role, group separation is not enabled,
            // call the user listing method for roles.
            if (!isRoleAndGroupSeparationEnabled()) {
                return getUserListOfRoleWithID(groupName, filter, maxItemLimit);
            }
            // If the passed group is a role and if role, group separation is enabled, just return.
            return users;
        }

        if (readGroupsEnabled) {
            // If unique id feature is not enabled, we have to call the legacy methods.
            if (!isUniqueUserIdEnabledInUserStore(userStore)) {
                users = userUniqueIDManger.listUsers(doGetUserListOfRole(groupName, filter, maxItemLimit), this);
            } else {
                users = doGetUserListOfRoleWithID(groupName, filter, maxItemLimit);
            }
            handleDoPostGetUserListOfRoleWithID(groupName, users);
        }
        return users;
    }

    @Override
    public final String getUserClaimValueWithID(String userID, String claim, String profileName)
            throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[] { String.class, String.class, String.class };
            Object object = callSecure("getUserClaimValueWithID", new Object[] { userID, claim, profileName },
                    argTypes);
            return (String) object;
        }

        UserStore userStore = getUserStoreWithID(userID);
        if (userStore.isRecurssive()) {
            return ((AbstractUserStoreManager) userStore.getUserStoreManager())
                    .getUserClaimValueWithID(userStore.getDomainFreeUserId(), claim, profileName);
        }

        // #################### Domain Name Free Zone Starts Here ################################
        boolean isUniqueIdEnabled = isUniqueUserIdEnabledInUserStore(userStore);
        boolean isUserExists;
        User user = null;
        if (isUniqueIdEnabled) {
            isUserExists = doCheckExistingUserWithID(userID);
        } else{
            user = userUniqueIDManger.getUser(userID, this, userStore.getDomainName());
            isUserExists = user != null;
        }

        if (!isUserExists) {
            String errorCode = ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getCode();
            String errorMessage = String.format(ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getMessage(), userID,
                    realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME));
            handleGetUserClaimValueFailureWithID(errorCode, errorMessage, userID, claim, profileName);
            throw new UserStoreException(errorCode + " - " + errorMessage);
        }

        Map<String, String> finalValues;
        try {
            // If unique id feature is not enabled, we have to call the legacy methods.
            if (!isUniqueIdEnabled) {
                finalValues = doGetUserClaimValues(user.getUsername(), new String[]{claim},
                        userStore.getDomainName(), profileName);
            } else {
                finalValues = doGetUserClaimValuesWithID(userID, new String[]{claim}, userStore.getDomainName(),
                        profileName);
            }
        } catch (UserStoreException ex) {
            handleGetUserClaimValueFailureWithID(
                    ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_USER_CLAIM_VALUE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_USER_CLAIM_VALUE.getMessage(),
                            ex.getMessage()), userID, claim, profileName);
            throw ex;
        }

        String value = null;

        if (finalValues != null) {
            value = finalValues.get(claim);
        }

        // #################### <Listeners> #####################################################

        List<String> list = new ArrayList<>();
        if (value != null) {
            list.add(value);
        }

        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (listener instanceof AbstractUserOperationEventListener) {
                    AbstractUserOperationEventListener newListener = (AbstractUserOperationEventListener) listener;
                    if (!newListener.doPostGetUserClaimValueWithID(userID, claim, list, profileName, this)) {
                        handleGetUserClaimValueFailureWithID(
                                ErrorMessages.ERROR_CODE_ERROR_DURING_POST_GET_USER_CLAIM_VALUE.getCode(),
                                String.format(
                                        ErrorMessages.ERROR_CODE_ERROR_DURING_POST_GET_USER_CLAIM_VALUE.getMessage(),
                                        UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), userID, claim,
                                profileName);
                        break;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleGetUserClaimValueFailureWithID(
                    ErrorMessages.ERROR_CODE_ERROR_DURING_POST_GET_USER_CLAIM_VALUE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_GET_USER_CLAIM_VALUE.getMessage(),
                            ex.getMessage()), userID, claim, profileName);
            throw ex;
        }
        // #################### </Listeners> #####################################################

        if (!list.isEmpty()) {
            return list.get(0);
        }
        return value;
    }

    @Override
    public final Map<String, String> getUserClaimValuesWithID(String userID, String[] claims, String profileName)
            throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[] { String.class, String[].class, String.class };
            Object object = callSecure("getUserClaimValuesWithID", new Object[] { userID, claims, profileName },
                    argTypes);
            return (Map<String, String>) object;
        }

        UserStore userStore = getUserStoreWithID(userID);
        if (userStore.isRecurssive()) {
            return ((AbstractUserStoreManager) userStore.getUserStoreManager())
                    .getUserClaimValuesWithID(userStore.getDomainFreeUserId(), claims, profileName);
        }

        // #################### Domain Name Free Zone Starts Here ################################
        boolean isUniqueIdEnabled = isUniqueUserIdEnabledInUserStore(userStore);
        boolean isUserExists;
        User user = null;
        if (isUniqueIdEnabled) {
            isUserExists = doCheckExistingUserWithID(userID);
        } else {
            if (userStore != null && StringUtils.isNotBlank(userStore.getDomainName())) {
                user = userUniqueIDManger.getUser(userID, this, userStore.getDomainName());
            } else {
                user = userUniqueIDManger.getUser(userID, this);
            }
            isUserExists = user != null;
        }

        if (!isUserExists) {
            String errorMessage = String.format(ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getMessage(), userID,
                    realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME));
            String errorCode = ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getCode();
            handleGetUserClaimValuesFailureWithID(errorCode, errorMessage, userID, claims, profileName);
            throw new UserStoreException(errorCode + " - " + errorMessage);
        }

        // check for null claim list
        if (claims == null) {
            claims = new String[0];
        }

        Map<String, String> finalValues;
        try {
            // If unique id feature is not enabled, we have to call the legacy methods.
            if (!isUniqueIdEnabled) {
                finalValues = doGetUserClaimValues(user.getUsername(), claims, userStore.getDomainName(),
                        profileName);
            } else {
                finalValues = doGetUserClaimValuesWithID(userID, claims, userStore.getDomainName(), profileName);
            }
        } catch (UserStoreException ex) {
            handleGetUserClaimValuesFailureWithID(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_CLAIM_VALUES.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_CLAIM_VALUES.getMessage(),
                            ex.getMessage()), userID, claims, profileName);
            throw ex;
        }

        // #################### <Listeners> #####################################################
        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (listener instanceof AbstractUserOperationEventListener) {
                    AbstractUserOperationEventListener newListener = (AbstractUserOperationEventListener) listener;
                    if (!newListener.doPostGetUserClaimValuesWithID(userStore.getDomainFreeUserId(), claims, profileName,
                            finalValues, this)) {
                        handleGetUserClaimValuesFailureWithID(
                                ErrorMessages.ERROR_CODE_ERROR_IN_POST_GET_CLAIM_VALUES.getCode(),
                                String.format(ErrorMessages.ERROR_CODE_ERROR_IN_POST_GET_CLAIM_VALUES.getMessage(),
                                        UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), userID, claims,
                                profileName);
                        break;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleGetUserClaimValuesFailureWithID(ErrorMessages.ERROR_CODE_ERROR_IN_POST_GET_CLAIM_VALUES.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_IN_POST_GET_CLAIM_VALUES.getMessage(),
                            ex.getMessage()), userID, claims, profileName);
            throw ex;
        }
        // #################### </Listeners> #####################################################

        return finalValues;
    }

    @Override
    public final List<Claim> getUserClaimValuesWithID(String userID, String profileName) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[] { String.class, String.class };
            Object object = callSecure("getUserClaimValuesWithID", new Object[] { userID, profileName }, argTypes);
            return (List<Claim>) object;
        }

        UserStore userStore = getUserStoreWithID(userID);
        if (userStore.isRecurssive()) {
            return ((AbstractUserStoreManager) userStore.getUserStoreManager())
                    .getUserClaimValuesWithID(userStore.getDomainFreeUserId(), profileName);
        }

        boolean isUniqueIdEnabled = isUniqueUserIdEnabledInUserStore(userStore);
        boolean isUserExists;
        if (isUniqueIdEnabled) {
            isUserExists = doCheckExistingUserWithID(userID);
        } else {
            String userNameFromUserID = doGetUserNameFromUserID(userID);
            isUserExists = userNameFromUserID != null;
        }

        if (!isUserExists) {
            String errorMessage = String.format(ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getMessage(), userID,
                    realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME));
            String errorCode = ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getCode();
            handleGetUserClaimValuesFailureWithID(errorCode, errorMessage, userID, null, profileName);
            throw new UserStoreException(errorCode + " - " + errorMessage);
        }

        // If unique id feature is not enabled, we have to call the legacy methods.
        if (!isUniqueIdEnabled) {
            User user = userUniqueIDManger.getUser(userID, this, userStore.getDomainName());
            return Arrays.asList(getUserClaimValues(user.getDomainQualifiedUsername(), profileName));
        }

        if (StringUtils.isEmpty(profileName)) {
            profileName = UserCoreConstants.DEFAULT_PROFILE;
        }

        String[] claims;
        try {
            claims = claimManager.getAllClaimUris();
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            handleGetUserClaimValuesFailureWithID(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_CLAIM_URI.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_CLAIM_URI.getMessage(), e.getMessage()),
                    userID, null, profileName);
            throw new UserStoreException(e);
        }

        Map<String, String> values = doGetUserClaimValuesWithID(userID, claims, userStore.getDomainName(), profileName);
        List<Claim> finalValues = new ArrayList<>();
        addClaimValues(values, finalValues);

        return finalValues;
    }

    protected Map<String, String> doGetUserClaimValuesWithID(String userID, String[] claims, String domainName,
            String profileName) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[] { String.class, String[].class, String.class, String.class };
            Object object = callSecure("doGetUserClaimValuesWithID", new Object[] {
                    userID, claims, domainName, profileName
            }, argTypes);
            return (Map<String, String>) object;
        }

        if (ArrayUtils.isEmpty(claims)) {
            return new HashMap<>();
        }

        // Here the user name should be domain-less.
        boolean requireRolesAndGroups = false;
        boolean requireIntRole = false;
        boolean requireExtRole = false;
        boolean requireRoles = false;
        boolean requireGroups = false;
        String rolesAndGroupsClaim = null;
        String rolesClaim = null;
        String groupsClaim = null;

        if (StringUtils.isEmpty(profileName)) {
            profileName = UserCoreConstants.DEFAULT_PROFILE;
        }

        Set<String> propertySet = new HashSet<>();
        for (String claim : claims) {

            // There can be cases some claim values being requested for claims
            // we don't have.
            String property;
            try {
                property = getClaimAtrribute(claim, userID, domainName);
            } catch (org.wso2.carbon.user.api.UserStoreException e) {
                throw new UserStoreException(e);
            }
            if (property != null && isNotARoleOrGroupClaim(claim)) {
                propertySet.add(property);
            }

            if (UserCoreConstants.ROLE_CLAIM.equalsIgnoreCase(claim)) {
                requireRolesAndGroups = true;
                rolesAndGroupsClaim = claim;
            } else if (UserCoreConstants.INT_ROLE_CLAIM.equalsIgnoreCase(claim)) {
                requireIntRole = true;
                rolesAndGroupsClaim = claim;
            } else if (UserCoreConstants.EXT_ROLE_CLAIM.equalsIgnoreCase(claim)) {
                requireExtRole = true;
                rolesAndGroupsClaim = claim;
            }

            if (isGroupsVsRolesSeparationImprovementsEnabled(realmConfig)) {
                if (UserCoreConstants.INTERNAL_ROLES_CLAIM.equalsIgnoreCase(claim)) {
                    requireRoles = true;
                    rolesClaim = claim;
                } else if (UserCoreConstants.USER_STORE_GROUPS_CLAIM.equalsIgnoreCase(claim)) {
                    requireGroups = true;
                    groupsClaim = claim;
                }
            }
        }

        String[] properties = propertySet.toArray(new String[0]);
        Map<String, String> userPropertyValues = this.getUserPropertyValuesWithID(userID, properties, profileName);
        processAttributesAfterRetrievalWithID(userID, userPropertyValues, profileName);

        List<String> getAgain = new ArrayList<>();
        Map<String, String> finalValues = new HashMap<>();

        for (String claim : claims) {
            ClaimMapping mapping;
            try {
                mapping = (ClaimMapping) claimManager.getClaimMapping(claim);
            } catch (org.wso2.carbon.user.api.UserStoreException e) {
                throw new UserStoreException(e);
            }
            String property = null;
            String value;
            if (mapping != null) {
                if (domainName != null) {
                    Map<String, String> attrMap = mapping.getMappedAttributes();
                    if (attrMap != null) {
                        String attr;
                        if ((attr = attrMap.get(domainName.toUpperCase())) != null) {
                            property = attr;
                        } else {
                            property = mapping.getMappedAttribute();
                        }
                    }
                } else {
                    property = mapping.getMappedAttribute();
                }

                if (StringUtils.isNotBlank(value = userPropertyValues.get(property))) {
                    finalValues.put(claim, value);
                }

            } else {
                if (property == null && claim.equals(DISAPLAY_NAME_CLAIM)) {
                    property = this.realmConfig.getUserStoreProperty(LDAPConstants.DISPLAY_NAME_ATTRIBUTE);
                }

                if (StringUtils.isNotBlank(value = userPropertyValues.get(property))) {
                    finalValues.put(claim, value);
                }
            }
        }

        if (getAgain.size() > 0) {
            // oh the beautiful recursion
            Map<String, String> mapClaimValues = this
                    .getUserClaimValuesWithID(userID, getAgain.toArray(new String[0]), profileName);

            Iterator<Map.Entry<String, String>> ite3 = mapClaimValues.entrySet().iterator();
            while (ite3.hasNext()) {
                Map.Entry<String, String> entry = ite3.next();
                if (entry.getValue() != null) {
                    finalValues.put(entry.getKey(), entry.getValue());
                }
            }
        }

        // We treat following claims in a special way.
        List<String> rolesAndGroups = null;
        List<String> roles = null;
        List<String> groups = null;

        if (requireRolesAndGroups) {
            rolesAndGroups = getRoleListOfUserWithID(userID);
        } else if (requireIntRole) {
            rolesAndGroups = doGetInternalRoleListOfUserWithID(userID, "*");
        } else if (requireExtRole) {
            List<String> rolesList = new ArrayList<>();
            String[] externalRoles = doGetExternalRoleListOfUserWithID(userID, "*");
            rolesList.addAll(Arrays.asList(externalRoles));
            //if only shared enable
            if (isSharedGroupEnabled()) {
                String[] sharedRoles = doGetSharedRoleListOfUserWithID(userID, null, "*");
                if (sharedRoles != null) {
                    rolesList.addAll(Arrays.asList(sharedRoles));
                }
            }

            rolesAndGroups = rolesList;
        }

        if (rolesAndGroups != null && rolesAndGroups.size() > 0) {
            finalValues.put(rolesAndGroupsClaim, getMultiValuedString(rolesAndGroups));
        }

        if (isGroupsVsRolesSeparationImprovementsEnabled(realmConfig)) {
            if (requireRoles) {
                roles = doGetInternalRoleListOfUserWithID(userID, "*");
            }

            if (requireGroups) {
                groups = Arrays.asList(doGetExternalRoleListOfUserWithID(userID, "*"));
            }

            if (roles != null && roles.size() > 0) {
                finalValues.put(rolesClaim, getMultiValuedString(roles));
            }

            if (groups != null && groups.size() > 0) {
                finalValues.put(groupsClaim, getMultiValuedString(groups));
            }
        }
        return finalValues;
    }

    private User getUserFromID(String userID, String[] requestedClaims, String domainName, String profileName)
            throws UserStoreException {

        User user = getUser(userID, null);
        if (ArrayUtils.isNotEmpty(requestedClaims)) {
            Map<String, String> claimValues = doGetUserClaimValuesWithID(userID, requestedClaims, domainName, profileName);
            user.setAttributes(claimValues);
        }
        return user;
    }

    private void addClaimValues(Map<String, String> values, List<Claim> finalValues) throws UserStoreException {

        for (Map.Entry<String, String> entry : values.entrySet()) {
            Claim claim = new Claim();
            claim.setValue(entry.getValue());
            claim.setClaimUri(entry.getKey());
            String displayTag = null;
            try {
                if (entry.getKey() != null && claimManager.getClaim(entry.getKey()) != null) {
                    displayTag = claimManager.getClaim(entry.getKey()).getDisplayTag();
                }
            } catch (org.wso2.carbon.user.api.UserStoreException e) {
                throw new UserStoreException(e);
            }
            claim.setDisplayTag(displayTag);
            finalValues.add(claim);
        }
    }

    @Override
    public final void updateCredentialWithID(String userID, Object newCredential, Object oldCredential)
            throws UserStoreException {

        if (!isSecureCall.get()) {
            Class[] argTypes = new Class[]{String.class, Object.class, Object.class};
            callSecure("updateCredentialWithID", new Object[] { userID, newCredential, oldCredential }, argTypes);
            return;
        }

        UserStore userStore = getUserStoreWithID(userID);
        if (userStore.isRecurssive()) {
            ((AbstractUserStoreManager) userStore.getUserStoreManager())
                    .updateCredentialWithID(userStore.getDomainFreeUserId(), newCredential, oldCredential);
            return;
        }

        // #################### Domain Name Free Zone Starts Here ################################

        if (isReadOnly()) {
            handleUpdateCredentialFailureWithID(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode(),
                    ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage(), userID, newCredential, oldCredential);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.toString());
        }

        Secret newCredentialObj;
        Secret oldCredentialObj;
        try {
            newCredentialObj = Secret.getSecret(newCredential);
            oldCredentialObj = Secret.getSecret(oldCredential);
        } catch (UnsupportedSecretTypeException e) {
            handleUpdateCredentialFailureWithID(ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.getCode(),
                    ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.getMessage(), userID, newCredential,
                    oldCredential);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.toString());
        }

        // #################### <Listeners> #####################################################
        try {
            // This user name here is domain-less.
            // We directly authenticate user against the selected UserStoreManager.

            AuthenticationResult authenticationResult;
            try {
                if (!isUniqueUserIdEnabledInUserStore(userStore)) {
                    User user = userUniqueIDManger.getUser(userID, this, userStore.getDomainName());
                    boolean auth = this.doAuthenticate(user.getUsername(), oldCredentialObj);
                    authenticationResult = new AuthenticationResult(auth ?
                            AuthenticationResult.AuthenticationStatus.SUCCESS :
                            AuthenticationResult.AuthenticationStatus.FAIL);
                } else {
                    authenticationResult = this.doAuthenticateWithID(userID, oldCredentialObj);
                }
            } catch (org.wso2.carbon.user.api.UserStoreException e) {
                throw new UserStoreException(e);
            }

            if (authenticationResult.getAuthenticationStatus() != AuthenticationResult.AuthenticationStatus.SUCCESS) {
                handleUpdateCredentialFailureWithID(ErrorMessages.ERROR_CODE_OLD_CREDENTIAL_DOES_NOT_MATCH.getCode(),
                        ErrorMessages.ERROR_CODE_OLD_CREDENTIAL_DOES_NOT_MATCH.getMessage(), userID, newCredential,
                        oldCredential);
                throw new UserStoreException(ErrorMessages.ERROR_CODE_OLD_CREDENTIAL_DOES_NOT_MATCH.toString());
            }

            try {

                for (UserOperationEventListener listener : UMListenerServiceComponent
                        .getUserOperationEventListeners()) {
                    if (listener instanceof AbstractUserOperationEventListener &&
                            !((AbstractUserOperationEventListener) listener)
                                    .doPreUpdateCredentialWithID(userID, newCredential, oldCredential, this)) {
                        handleUpdateCredentialFailureWithID(
                                ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL.getCode(),
                                String.format(
                                        ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL.getMessage(),
                                        UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userID,
                                newCredential, oldCredential);
                        return;
                    }
                }
            } catch (UserStoreException e) {
                handleUpdateCredentialFailureWithID(
                        ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL.getMessage(),
                                e.getMessage()), userID, newCredential, oldCredential);
                throw e;
            }
            // #################### </Listeners> #####################################################

            if (!checkUserPasswordValid(newCredential)) {
                String errorMsg = realmConfig.getUserStoreProperty(PROPERTY_PASSWORD_ERROR_MSG);

                if (errorMsg != null) {
                    String errorMessage = String
                            .format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL.getMessage(),
                                    errorMsg);
                    String errorCode = ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL.getCode();
                    handleUpdateCredentialFailureWithID(errorCode, errorMessage, userID, newCredential,
                            oldCredential);
                    throw new UserStoreException(errorCode + " - " + errorMessage);
                }

                String errorMessage = String.format(ErrorMessages.ERROR_CODE_INVALID_PASSWORD.getMessage(),
                        realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_JAVA_REG_EX));
                String errorCode = ErrorMessages.ERROR_CODE_INVALID_PASSWORD.getCode();
                handleUpdateCredentialFailureWithID(errorCode, errorMessage, userID, newCredential, oldCredential);
                throw new UserStoreException(errorCode + " - " + errorMessage);
            }

            try {
                // If unique id feature is not enabled, we have to call the legacy methods.
                if (!isUniqueUserIdEnabledInUserStore(userStore)) {
                    User user = userUniqueIDManger.getUser(userID, this, userStore.getDomainName());
                    // If we don't have a record for this user, let's try to call directly using the user id.
                    if (user == null) {
                        updateCredential(userID, newCredential, oldCredential);
                    } else {
                        updateCredential(user.getUsername(), newCredential, oldCredential);
                    }
                } else {
                    this.doUpdateCredentialWithID(userID, newCredentialObj, oldCredentialObj);
                }
            } catch (UserStoreException ex) {
                handleUpdateCredentialFailureWithID(
                        ErrorMessages.ERROR_CODE_ERROR_WHILE_UPDATING_CREDENTIAL.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_UPDATING_CREDENTIAL.getMessage(),
                                ex.getMessage()), userID, newCredential, oldCredential);
                throw ex;
            }

            // #################### <Listeners> ##################################################
            try {
                for (UserOperationEventListener listener : UMListenerServiceComponent
                        .getUserOperationEventListeners()) {
                    if (listener instanceof AbstractUserOperationEventListener &&
                            !((AbstractUserOperationEventListener) listener)
                                    .doPostUpdateCredentialWithID(userID, newCredential, this)) {
                        handleUpdateCredentialFailureWithID(
                                ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_CREDENTIAL.getCode(),
                                String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_CREDENTIAL
                                        .getMessage(), "Post update credential tasks failed"), userID,
                                newCredential, oldCredential);
                        return;
                    }
                }
            } catch (UserStoreException ex) {
                handleUpdateCredentialFailureWithID(
                        ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_CREDENTIAL.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_CREDENTIAL.getMessage(),
                                ex.getMessage()), userID, newCredential, oldCredential);
                throw ex;
            }
            // #################### </Listeners> ##################################################
        } finally {
            newCredentialObj.clear();
            oldCredentialObj.clear();
            // This value is set in the validation lister if the password gets validated
            // against the configured set of rules.
            UserCoreUtil.removeSkipPasswordPatternValidationThreadLocal();
        }
    }

    /**
     * Get the user.
     *
     * @param userID   user ID.
     * @param userName user name.
     * @return User.
     * @throws UserStoreException User Store Exception.
     */
    public User getUser(String userID, String userName) throws UserStoreException {

        if (userID == null && userName == null) {
            throw new UserStoreException("Both userID and UserName cannot be null.");
        }

        String domain = getMyDomainName();
        if (userID == null) {
            userID = getUserIDFromUserName(userName);
        }

        if (userName == null) {
            userName = getUserNameFromUserID(userID);
        }

        if (StringUtils.isEmpty(userID) && StringUtils.isEmpty(userName)) {
            throw new UserStoreClientException("User not found in the cache or database");
        }

        if (StringUtils.isNotEmpty(userName) && userName.contains(UserCoreConstants.DOMAIN_SEPARATOR)) {
            domain = UserCoreUtil.extractDomainFromName(userName);
            userName = UserCoreUtil.removeDomainFromName(userName);
        }
        User user = new User(userID, userName, userName);
        user.setTenantDomain(getTenantDomain(tenantId));
        user.setUserStoreDomain(domain);

        if (StringUtils.isNotEmpty(userID) &&
                StringUtils.isEmpty(userUniqueIDDomainResolver.getDomainForUserId(userID, tenantId))) {
            userUniqueIDDomainResolver.setDomainForUserIdIfNotExists(userID, domain, tenantId);
        }
        return user;
    }

    /**
     * Get the tenant domain.
     *
     * @return tenant domain.
     * @throws UserStoreException User Store Exception.
     */
    protected String getTenantDomain(int tenantID) throws UserStoreException {

        String tenantDomain;
        RealmService realmService = UserCoreUtil.getRealmService();
        try {
            if (realmService != null) {
                tenantDomain = realmService.getTenantManager().getDomain(tenantID);
            } else {
                tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException("Error occured while getting the tenant domain.", e);
        }
        return tenantDomain;
    }

    /**
     * provides the unique user ID of the user.
     *
     * @return unique user ID.
     */
    protected String getUniqueUserID() {

        return UUID.randomUUID().toString();
    }

    /**
     * Check whether the userID attribute is generated/maintained by the user store itself.
     *
     * @param userName       User's userName.
     * @param userAttributes A map user attribute values.
     * @return True if generated, else false.
     */
    protected boolean isUserIdGeneratedByUserStore(String userName, Map<String, String> userAttributes) {

        return false;
    }

    /**
     * provides the unique user ID of the given user.
     *
     * @param userName username of the user.
     * @return user ID of the user.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    public String getUserIDFromUserName(String userName) throws UserStoreException {

        UserStore userStore = getUserStore(userName);
        if (userStore.isRecurssive()) {
            return ((AbstractUserStoreManager) userStore.getUserStoreManager())
                    .getUserIDFromUserName(userStore.getDomainFreeName());
        }
        userName = userStore.getDomainFreeName();
        String userID = getFromUserIDCache(userName, userStore);
        // Validate whether circuit breaker is enabled and open.
        if (((AbstractUserStoreManager) userStore.getUserStoreManager()).isCircuitBreakerEnabledAndOpen()) {
            if (log.isDebugEnabled()) {
                log.debug("Avoiding user listing as the Circuit Breaker is in open state for domain: "
                        + userStore.getDomainName());
            }
            return null;
        } else {
            if (StringUtils.isEmpty(userID)) {
                // Return null for the system user to prevent redundant DB queries, as it would return null anyway.
                if (UserCoreUtil.isRegistrySystemUser(userName)) {
                    return null;
                }
                if (isUniqueUserIdEnabledInUserStore(userStore)) {
                    userID = doGetUserIDFromUserNameWithID(userName);
                    if (StringUtils.isEmpty(userID)) {
                        log.debug("User is not available in cache or database.");
                        return null;
                    }
                    addToUserIDCache(userID, userName, userStore);
                    addToUserNameCache(userID, userName, userStore);
                    return userID;
                }

                Map<String, String> claims = doGetUserClaimValues(userName,
                        new String[]{USER_ID_CLAIM_URI},
                        userStore.getDomainName(), null);
                if (claims != null && claims.size() == 1) {
                    userID = claims.get(USER_ID_CLAIM_URI);
                    addToUserIDCache(userID, userName, userStore);
                    addToUserNameCache(userID, userName, userStore);
                    return userID;
                }
            }
        }
        return userID;
    }

    /**
     * provides the unique user ID of the given user.
     *
     * @param userName username of the user.
     * @return user ID of the user.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    protected String doGetUserIDFromUserNameWithID(String userName) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doGetUserIDFromUserName operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException(
                "doGetUserIDFromUserName operation is not implemented in: " + this.getClass());
    }

    /**
     * Get the user name of the given user ID in the user store.
     *
     * @param userID userID of the user.
     * @return user name.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    protected String doGetUserNameFromUserID(String userID) throws UserStoreException {

        if (isUniqueUserIdEnabled()) {
            String userName = getFromUserNameCache(userID);
            if (StringUtils.isNotEmpty(userName)) {
                return UserCoreUtil.removeDomainFromName(userName);
            }
            return doGetUserNameFromUserIDWithID(userID);
        }
        User user = userUniqueIDManger.getUser(userID, this);
        return user.getUsername();
    }

    /**
     * Get the user name of the given user.
     *
     * @param userID userID of the user.
     * @return user name.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    public String getUserNameFromUserID(String userID) throws UserStoreException {

        UserStore userStore = getUserStoreWithID(userID);
        if (userStore.isRecurssive()) {
            return ((AbstractUserStoreManager) userStore.getUserStoreManager())
                    .getUserNameFromUserID(userStore.getDomainFreeUserId());
        }

        if (isUniqueUserIdEnabledInUserStore(userStore)) {
            return getUserNameFromCurrentUserStore(userID, userStore);
        } else {
            User user = userUniqueIDManger.getUser(userID, this, userStore.getDomainName());
            if (user == null) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("User with user id %s is not available in cache or database.", userID));
                }
                return null;
            }
            return user.getDomainQualifiedUsername();
        }
    }

    /**
     * Get the user name of the given user from a unique user id natively supported user store.
     *
     * @param userID userID of the user.
     * @param userStore user store of the user.
     * @return user name.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    private String getUserNameFromCurrentUserStore(String userID, UserStore userStore) throws UserStoreException {

        String userName = getFromUserNameCache(userID);
        if (StringUtils.isEmpty(userName)) {
            userName = doGetUserNameFromUserIDWithID(userID);
            if (StringUtils.isEmpty(userName)) {
                // This is possible when over lapping delete calls with get calls.
                if (log.isDebugEnabled()) {
                    log.debug(String.format("User with userid %s is not available in cache or database.", userID));
                }
                return null;
            }
            addToUserNameCache(userID, userName, userStore);
            addToUserIDCache(userID, userName, userStore);
        }
        return UserCoreUtil.addDomainToName(userName, userStore.getDomainName());
    }

    private String getFromUserNameCache(String userID) {

        return UserIdResolverCache.getInstance().getValueFromCache(userID,
                RESOLVE_USER_NAME_FROM_USER_ID_CACHE_NAME, tenantId);
    }

    private String getGroupNameFromGroupIdCache(String groupId) {

        return GroupIdResolverCache.getInstance().getValueFromCache(groupId,
                RESOLVE_GROUP_NAME_FROM_USER_ID_CACHE_NAME, tenantId);
    }

    private void addGroupNameToGroupIdCache(String groupId, String groupName, String domainName) {

        String groupWithDomain = UserCoreUtil.addDomainToName(groupName, domainName);
        GroupIdResolverCache.getInstance().addToCache(groupId, groupWithDomain,
                RESOLVE_GROUP_NAME_FROM_USER_ID_CACHE_NAME, tenantId);
    }

    private void clearGroupIDResolverCache(String groupId, int tenantId) {

        GroupIdResolverCache.getInstance()
                .clearCacheEntry(groupId, RESOLVE_GROUP_NAME_FROM_USER_ID_CACHE_NAME, tenantId);

    }

    private String getFromUserIDCache(String userName, UserStore userStore) {

        return UserIdResolverCache.getInstance()
                .getValueFromCache(UserCoreUtil.addDomainToName(userName, userStore.getDomainName()),
                        RESOLVE_USER_ID_FROM_USER_NAME_CACHE_NAME, tenantId);
    }

    private void addToUserIDCache(String userID, String userName, UserStore userStore) {

        UserIdResolverCache.getInstance()
                .addToCache(UserCoreUtil.addDomainToName(userName, userStore.getDomainName()), userID,
                        RESOLVE_USER_ID_FROM_USER_NAME_CACHE_NAME, tenantId);
    }

    private void addToUserNameCache(String userID, String userName, UserStore userStore) {

        UserIdResolverCache.getInstance()
                .addToCache(userID, UserCoreUtil.addDomainToName(userName, userStore.getDomainName()),
                        RESOLVE_USER_NAME_FROM_USER_ID_CACHE_NAME, tenantId);
    }

    private void clearUserIDResolverCache(String userID, String userName, UserStore userStore) {

        UserIdResolverCache.getInstance()
                .clearCacheEntry(UserCoreUtil.addDomainToName(userName, userStore.getDomainName()),
                        RESOLVE_USER_ID_FROM_USER_NAME_CACHE_NAME, tenantId);
        UserIdResolverCache.getInstance().clearCacheEntry(userID, RESOLVE_USER_NAME_FROM_USER_ID_CACHE_NAME, tenantId);
        UserIdResolverCache.getInstance()
                .clearCacheEntry(UserCoreUtil.addDomainToName(userName, userStore.getDomainName()),
                        RESOLVE_USER_UNIQUE_ID_FROM_USER_NAME_CACHE_NAME, SUPER_TENANT_ID);
        UserIdResolverCache.getInstance()
                .clearCacheEntry(userID, RESOLVE_USER_NAME_FROM_UNIQUE_USER_ID_CACHE_NAME, SUPER_TENANT_ID);
    }

    private void addUsersToUserIdCache(List<User> userList) {

        UserIdResolverCache userIdResolverCacheInstance = UserIdResolverCache.getInstance();
        for (User user : userList) {
            userIdResolverCacheInstance.addToCache(
                    UserCoreUtil.addDomainToName(user.getUsername(), user.getUserStoreDomain()), user.getUserID(),
                    RESOLVE_USER_ID_FROM_USER_NAME_CACHE_NAME, tenantId);
        }
    }

    private void addUsersToUserNameCache(List<User> userList) {

        UserIdResolverCache userIdResolverCacheInstance = UserIdResolverCache.getInstance();
        for (User user : userList) {
            userIdResolverCacheInstance.addToCache(
                    user.getUserID(), UserCoreUtil.addDomainToName(user.getUsername(), user.getUserStoreDomain()),
                    RESOLVE_USER_NAME_FROM_USER_ID_CACHE_NAME, tenantId);
        }
    }

    /**
     * provides the userName of the given user.
     *
     * @param userID userID of the user.
     * @return userName of the user.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    protected String doGetUserNameFromUserIDWithID(String userID) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doGetUserNameFromUserIDWithID operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException(
                "doGetUserNameFromUserIDWithID operation is not implemented in: " + this.getClass());
    }

    /**
     * provides the unique user IDs of the given users.
     *
     * @param userIDs userIDs of the users.
     * @return list of user IDs.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    public List<String> getUserNamesFromUserIDs(List<String> userIDs) throws UserStoreException {

        List<String> userNames = new ArrayList<>();
        for (String userID : userIDs) {
            if (StringUtils.isBlank(userID)) {
                continue;
            }
            userNames.add(getUserNameFromUserID(userID));
        }
        return userNames;
    }

    /**
     * provides the unique user IDs of the given users.
     *
     * @param userNames user names of the users.
     * @return list of user IDs.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    public List<String> getUserIDsFromUserNames(List<String> userNames) throws UserStoreException {

        List<String> userIDs = new ArrayList<>();
        for (String userName : userNames) {
            String userId = getUserIDFromUserName(userName);
            if (userId == null) {
                throw new UserStoreException("User " + userName + " does not exit in the system.");
            }
            userIDs.add(userId);
        }
        return userIDs;
    }

    /**
     * Provide the users list for a given usernames list.
     *
     * @param userNamesList user names list.
     * @return list of users.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    public List<User> getUsersFromUserNames(List<String> userNamesList) throws UserStoreException {

        List<User> usersList = new ArrayList<>();
        for (String userName : userNamesList) {
            String userID = getUserIDFromUserName(userName);
            User user = new User(userID, userName, userName);
            usersList.add(user);
        }
        return usersList;
    }

    /**
     * provides the unique user ID of the given user.
     *
     * @param claimURI    Claim naURIme.
     * @param claimValue  Claim value.
     * @param profileName Profile name.
     * @return user ID.
     * @throws UserStoreException UserStoreException Thrown by the underlying UserStoreManager.
     */
    public String getUserIDFromProperties(String claimURI, String claimValue, String profileName)
            throws UserStoreException {

        String domain = this.getMyDomainName();
        if (isUniqueUserIdEnabled()) {
            List<User> users = doGetUserListWithID(claimURI, claimValue, profileName, domain, this);
            if (users.isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("No userID found for the claim: " + claimURI + ", value: " + claimValue + ", in domain:"
                            + " " + getMyDomainName());
                }
                return null;
            } else if (users.size() > 1) {
                throw new UserStoreException(
                        "Invalid scenario. Multiple users cannot be found for the given value: " + claimValue + "of "
                                + "the " + "claim: " + claimURI);
            }
            return users.get(0).getUserID();
        } else {
            List<String> userNames = doGetUserList(claimURI, claimValue, profileName, domain, this);
            if (userNames.isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("No userID found for the claim: " + claimURI + ", value: " + claimValue + ", in domain:"
                            + " " + getMyDomainName());
                }
                return null;
            } else if (userNames.size() > 1) {
                throw new UserStoreException(
                        "Invalid scenario. Multiple users cannot be found for the given value: " + claimValue + "of "
                                + "the " + "claim: " + claimURI);
            }
            return userUniqueIDManger.getUniqueId(userNames.get(0), this);
        }
    }

    /**
     * Get Users list from userIDs.
     *
     * @param userIDs     user IDs.
     * @param claims      Requested claims.
     * @param domainName  Domain name.
     * @param profileName Profile name.
     * @return User list.
     * @throws UserStoreException UserStoreException.
     */
    protected List<User> getUsersFromIDs(List<String> userIDs, String[] claims, String domainName, String profileName)
            throws UserStoreException {

        List<User> users = new ArrayList<>();
        for (String userID : userIDs) {
            users.add(getUserFromID(userID, claims, domainName, profileName));
        }
        return users;
    }


    /**
     * Get the mapped user store attribute name for the user name.
     *
     * @return mapped attribute for the user name.
     * @throws UserStoreException
     */
    protected String getUserNameMappedAttribute() throws UserStoreException {

        try {
            return claimManager.getAttributeName(getMyDomainName(), UserCoreClaimConstants.USERNAME_CLAIM_URI);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException(e);
        }
    }

    /**
     * Add username as a user claim.
     *
     * @param userName username.
     * @param claims   claims map.
     */
    protected Map<String, String> addUserNameAttribute(String userName, Map<String, String> claims) {

        if (claims == null) {
            claims = new HashMap<>();
        }
        claims.put(UserCoreClaimConstants.USERNAME_CLAIM_URI, userName);
        return claims;
    }

    /**
     * Add username as a user claim.
     *
     * @param userID user ID.
     * @param claims claims map.
     */
    protected Map<String, String> addUserIDAttribute(String userID, Map<String, String> claims) {

        if (claims == null) {
            claims = new HashMap<>();
        }
        claims.put(USER_ID_CLAIM_URI, userID);
        return claims;
    }

    @Override
    public final void deleteUserWithID(String userID) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class[] argTypes = new Class[]{String.class};
            callSecure("deleteUserWithID", new Object[] { userID }, argTypes);
            return;
        }

        String loggedInUser = CarbonContext.getThreadLocalCarbonContext().getUsername();
        if (loggedInUser != null) {
            loggedInUser = UserCoreUtil.addDomainToName(loggedInUser, UserCoreUtil.getDomainFromThreadLocal());
            if ((loggedInUser.indexOf(UserCoreConstants.DOMAIN_SEPARATOR)) < 0) {
                loggedInUser =
                        UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME + CarbonConstants.DOMAIN_SEPARATOR + loggedInUser;
            }
        }

        String deletingUser = UserCoreUtil.addDomainToName(userID, getMyDomainName());
        if ((deletingUser.indexOf(UserCoreConstants.DOMAIN_SEPARATOR)) < 0) {
            deletingUser =
                    UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME + CarbonConstants.DOMAIN_SEPARATOR + deletingUser;
        }

        if (loggedInUser != null && loggedInUser.equals(deletingUser)) {
            log.debug("User " + loggedInUser + " tried to delete him/her self");
            handleDeleteUserFailureWithID(ErrorMessages.ERROR_CODE_DELETE_LOGGED_IN_USER.getCode(),
                    ErrorMessages.ERROR_CODE_DELETE_LOGGED_IN_USER.getMessage(), userID);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_DELETE_LOGGED_IN_USER.toString());
        }

        UserStore userStore = getUserStoreWithID(userID);
        if (userStore.isRecurssive()) {
            ((AbstractUserStoreManager) userStore.getUserStoreManager())
                    .deleteUserWithID(userStore.getDomainFreeUserId());
            return;
        }

        // #################### Domain Name Free Zone Starts Here ################################

        if (UserCoreUtil.isPrimaryAdminUser(userID, realmConfig)) {
            handleDeleteUserFailureWithID(ErrorMessages.ERROR_CODE_DELETE_ADMIN_USER.getCode(),
                    ErrorMessages.ERROR_CODE_DELETE_ADMIN_USER.getMessage(), userID);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_DELETE_ADMIN_USER.toString());
        }

        if (UserCoreUtil.isRegistryAnnonymousUser(userID)) {
            handleDeleteUserFailureWithID(ErrorMessages.ERROR_CODE_DELETE_ANONYMOUS_USER.getCode(),
                    ErrorMessages.ERROR_CODE_DELETE_ANONYMOUS_USER.getMessage(), userID);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_DELETE_ANONYMOUS_USER.toString());
        }

        if (isReadOnly()) {
            handleDeleteUserFailureWithID(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode(),
                    ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage(), userID);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.toString());
        }

        // #################### <Listeners> #####################################################
        try {
            for (UserStoreManagerListener listener : UMListenerServiceComponent.getUserStoreManagerListeners()) {
                if (!((AbstractUserStoreManagerListener) listener).deleteUserWithID(userID, this)) {
                    handleDeleteUserFailureWithID(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_USER.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_USER.getMessage(),
                                    UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userID);
                    return;
                }
            }
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!((AbstractUserOperationEventListener) listener).doPreDeleteUserWithID(userID, this)) {
                    handleDeleteUserFailureWithID(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_USER.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_USER.getMessage(),
                                    UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userID);

                    return;
                }
            }
        } catch (UserStoreException e) {
            handleDeleteUserFailureWithID(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_USER.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_USER.getMessage(), e.getMessage()),
                    userID);
            throw e;
        }
        // #################### </Listeners> #####################################################

        User user = null;
        boolean isUserEixisting;
        String userName;
        if (isUniqueUserIdEnabledInUserStore(userStore)) {
            userName = doGetUserNameFromUserID(userID);
            isUserEixisting = userName != null;
        } else {
            user = userUniqueIDManger.getUser(userID, this, userStore.getDomainName());
            isUserEixisting = user != null;
            userName = user.getUsername();
        }

        if (!isUserEixisting) {
            String errorMessage = String.format(ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getMessage(), userID,
                    realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME));
            String errorCode = ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getCode();
            handleDeleteUserFailureWithID(errorCode, errorMessage, userID);
            throw new UserStoreException(errorCode + " - " + errorMessage);
        }

        try {

            clearUserIDResolverCache(userID, userName, userStore);
            // If unique id feature is not enabled, we have to call the legacy methods.
            if (isUniqueUserIdEnabledInUserStore(userStore)) {
                hybridRoleManager.deleteUser(UserCoreUtil.addDomainToName(userName, getMyDomainName()));
                doDeleteUserWithID(userID);
            } else {
                hybridRoleManager.deleteUser(user.getDomainQualifiedUsername());
                doDeleteUser(userName);
            }
        } catch (UserStoreException e) {
            handleDeleteUserFailureWithID(ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETING_USER.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETING_USER.getMessage(), e.getMessage()),
                    userID);
            throw e;
        }

        // Needs to clear roles cache upon deletion of a user
        clearUserRolesCache(userName);

        // #################### <Listeners> #####################################################
        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!((AbstractUserOperationEventListener) listener).doPostDeleteUserWithID(userID, this)) {
                    handleDeleteUserFailureWithID(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_USER.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_USER.getMessage(),
                                    UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), userID);
                    return;
                }
            }
        } catch (UserStoreException ex) {
            handleDeleteUserFailureWithID(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_USER.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_USER.getMessage(), ex.getMessage()),
                    userID);
            throw ex;
        }
        // #################### </Listeners> #####################################################

    }

    @Override
    public final void setUserClaimValueWithID(String userID, String claimURI, String claimValue, String profileName)
            throws UserStoreException {

        UserStore userStore = getUserStoreWithID(userID);
        if (userStore.isRecurssive()) {
            ((AbstractUserStoreManager) userStore.getUserStoreManager())
                    .setUserClaimValueWithID(userStore.getDomainFreeUserId(), claimURI, claimValue, profileName);
            return;
        }

        // #################### Domain Name Free Zone Starts Here ################################
        boolean isUniqueIdEnabled = isUniqueUserIdEnabledInUserStore(userStore);
        boolean isUserExists;
        User user = null;
        if (isUniqueIdEnabled) {
            isUserExists = doCheckExistingUserWithID(userID);
        } else {
            user = userUniqueIDManger.getUser(userID, this, userStore.getDomainName());
            isUserExists = user != null;
        }

        if (!isUserExists) {
            String errorMessage = String.format(ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getMessage(), userID,
                    realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME));
            String errorCode = ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getCode();
            handleSetUserClaimValueFailureWithID(errorCode, errorMessage, userID, claimURI, claimValue, profileName);
            throw new UserStoreException(errorCode + " - " + errorMessage);
        }

        // #################### <Listeners> #####################################################
        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!((AbstractUserOperationEventListener) listener)
                        .doPreSetUserClaimValueWithID(userID, claimURI, claimValue, profileName, this)) {
                    handleSetUserClaimValueFailureWithID(
                            ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_SET_USER_CLAIM_VALUE.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_SET_USER_CLAIM_VALUE.getMessage(),
                                    UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userID, claimURI,
                            claimValue, profileName);
                    return;
                }
            }
        } catch (UserStoreException e) {
            handleSetUserClaimValueFailureWithID(
                    ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_SET_USER_CLAIM_VALUE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_SET_USER_CLAIM_VALUE.getMessage(),
                            e.getMessage()), userID, claimURI, claimValue, profileName);
            throw e;
        }
        // #################### </Listeners> #####################################################

        //Check userstore is readonly or not

        if (isReadOnly()) {
            handleSetUserClaimValueFailureWithID(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode(),
                    ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage(), userID, claimURI, claimValue,
                    profileName);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.toString());
        }

        try {
            // If unique id feature is not enabled, we have to call the legacy methods.
            if (!isUniqueUserIdEnabledInUserStore(userStore)) {
                doSetUserClaimValue(user.getUsername(), claimURI, claimValue, profileName);
            } else {
                doSetUserClaimValueWithID(userID, claimURI, claimValue, profileName);
            }
        } catch (UserStoreException e) {
            handleSetUserClaimValueFailureWithID(
                    ErrorMessages.ERROR_CODE_ERROR_WHILE_SETTING_USER_CLAIM_VALUE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_SETTING_USER_CLAIM_VALUE.getMessage(),
                            e.getMessage()), userID, claimURI, claimValue, profileName);
            throw e;
        }

        // #################### <Listeners> #####################################################
        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!((AbstractUserOperationEventListener) listener).doPostSetUserClaimValueWithID(userID, this)) {
                    handleSetUserClaimValueFailureWithID(
                            ErrorMessages.ERROR_CODE_ERROR_DURING_POST_SET_USER_CLAIM_VALUE.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_SET_USER_CLAIM_VALUE.getMessage(),
                                    UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), userID, claimURI,
                            claimValue, profileName);
                    return;
                }
            }
        } catch (UserStoreException e) {
            handleSetUserClaimValueFailureWithID(
                    ErrorMessages.ERROR_CODE_ERROR_DURING_POST_SET_USER_CLAIM_VALUE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_SET_USER_CLAIM_VALUE.getMessage(),
                            e.getMessage()), userID, claimURI, claimValue, profileName);
            throw e;
        }
        // #################### </Listeners> #####################################################

    }

    @Override
    public final void setUserClaimValuesWithID(String userID, Map<String, String> claims, String profileName)
            throws UserStoreException {

        UserStore userStore = getUserStoreWithID(userID);
        if (userStore.isRecurssive()) {
            ((AbstractUserStoreManager) userStore.getUserStoreManager())
                    .setUserClaimValuesWithID(userStore.getDomainFreeUserId(), claims, profileName);
            return;
        }

        // #################### Domain Name Free Zone Starts Here ################################

        boolean isUniqueIdEnabled = isUniqueUserIdEnabledInUserStore(userStore);
        boolean isUserExists;
        if (isUniqueIdEnabled) {
            isUserExists = doCheckExistingUserWithID(userID);
        } else {
            String userNameFromUserID = doGetUserNameFromUserID(userID);
            isUserExists = userNameFromUserID != null;
        }

        if (!isUserExists) {
            String errorMessage = String.format(ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getMessage(), userID,
                    realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME));
            String errorCode = ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getCode();
            handleSetUserClaimValuesFailureWithID(errorCode, errorMessage, userID, claims, profileName);
            throw new UserStoreException(errorCode + " - " + errorMessage);
        }
        if (claims == null) {
            claims = new HashMap<>();
        }
        // #################### <Listeners> #####################################################
        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!((AbstractUserOperationEventListener) listener)
                        .doPreSetUserClaimValuesWithID(userID, claims, profileName, this)) {
                    handleSetUserClaimValuesFailureWithID(
                            ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_SET_USER_CLAIM_VALUES.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_SET_USER_CLAIM_VALUES.getMessage(),
                                    UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userID, claims,
                            profileName);
                    return;
                }
            }
        } catch (UserStoreException e) {
            handleSetUserClaimValuesFailureWithID(
                    ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_SET_USER_CLAIM_VALUES.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_SET_USER_CLAIM_VALUES.getMessage(),
                            e.getMessage()), userID, claims, profileName);
            throw e;
        }
        // #################### </Listeners> #####################################################

        //If user store is readonly this method should not get invoked with non empty claim set.

        if (isReadOnly() && !claims.isEmpty()) {
            handleSetUserClaimValuesFailureWithID(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode(),
                    ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage(), userID, claims, profileName);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage(),
                    ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode());
        }

        // set claim values if user store is not read only.

        try {
            if (!isReadOnly()) {
                // If unique id feature is not enabled, we have to call the legacy methods.
                if (!isUniqueUserIdEnabledInUserStore(userStore)) {
                    User user = userUniqueIDManger.getUser(userID, this, userStore.getDomainName());
                    doSetUserClaimValues(user.getUsername(), claims, profileName);
                } else {
                    doSetUserClaimValuesWithID(userID, claims, profileName);
                }
            }
        } catch (UserStoreException e) {
            handleSetUserClaimValuesFailureWithID(
                    ErrorMessages.ERROR_CODE_ERROR_WHILE_SETTING_USER_CLAIM_VALUES.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_SETTING_USER_CLAIM_VALUES.getMessage(),
                            e.getMessage()), userID, claims, profileName);
            throw e;
        }

        // #################### <Listeners> #####################################################
        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!((AbstractUserOperationEventListener) listener)
                        .doPostSetUserClaimValuesWithID(userID, claims, profileName, this)) {
                    handleSetUserClaimValuesFailureWithID(
                            ErrorMessages.ERROR_CODE_ERROR_DURING_POST_SET_USER_CLAIM_VALUES.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_SET_USER_CLAIM_VALUES.getMessage(),
                                    UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), userID, claims,
                            profileName);
                    return;
                }
            }
        } catch (UserStoreException e) {
            handleSetUserClaimValuesFailureWithID(
                    ErrorMessages.ERROR_CODE_ERROR_DURING_POST_SET_USER_CLAIM_VALUES.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_SET_USER_CLAIM_VALUES.getMessage(),
                            e.getMessage()), userID, claims, profileName);
            throw e;
        }
        // #################### </Listeners> #####################################################

    }

    @Override
    public final void setUserClaimValuesWithID(String userID, Map<String, List<String>> oldClaimMap,
                                               Map<String, List<String>> multiValuedClaimsToAdd,
                                               Map<String, List<String>> multiValuedClaimsToDelete,
                                               Map<String, List<String>> claimsExcludingMultiValuedClaims,
                                               String profileName) throws UserStoreException {

        UserStore userStore = getUserStoreWithID(userID);
        if (userStore.isRecurssive()) {
            ((AbstractUserStoreManager) userStore.getUserStoreManager())
                    .setUserClaimValuesWithID(userStore.getDomainFreeUserId(), oldClaimMap, multiValuedClaimsToAdd,
                            multiValuedClaimsToDelete, claimsExcludingMultiValuedClaims, profileName);
            return;
        }
        Map<String, String> claims =
                getModifiedClaims(oldClaimMap, multiValuedClaimsToAdd, multiValuedClaimsToDelete,
                        claimsExcludingMultiValuedClaims);

        // #################### Domain Name Free Zone Starts Here ################################

        boolean isUniqueIdEnabled = isUniqueUserIdEnabledInUserStore(userStore);
        boolean isUserExists;
        if (isUniqueIdEnabled) {
            isUserExists = doCheckExistingUserWithID(userID);
        } else {
            String userNameFromUserID = doGetUserNameFromUserID(userID);
            isUserExists = userNameFromUserID != null;
        }

        if (!isUserExists) {
            String errorMessage = String.format(ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getMessage(), userID,
                    realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME));
            String errorCode = ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getCode();
            handleSetUserClaimValuesFailureWithID(errorCode, errorMessage, userID, claims, profileName);
            throw new UserStoreException(errorCode + " - " + errorMessage);
        }

        // #################### <Pre Listeners> #####################################################
        invokeDoPreSetUserClaimsWithIDListeners(userID, claims, profileName);
        // #################### </Pre Listeners> #####################################################

        // If userstore is readonly this method should not get invoked with non empty claim set.
        if (isReadOnly() && !claims.isEmpty()) {
            handleSetUserClaimValuesFailureWithID(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode(),
                    ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage(), userID, claims, profileName);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.toString());
        }

        // Any additional simple claim modified due to pre listeners are taken into claimsExcludingMultiValuedClaims map.
        String separator = ",";
        if (StringUtils.isNotEmpty(realmConfig.getUserStoreProperty(MULTI_ATTRIBUTE_SEPARATOR))) {
            separator = realmConfig.getUserStoreProperty(MULTI_ATTRIBUTE_SEPARATOR);
        }
        if (claimsExcludingMultiValuedClaims != null) {
            for (Map.Entry<String, String> claim : claims.entrySet()) {
                claimsExcludingMultiValuedClaims.put(claim.getKey(), Arrays.asList(claim.getValue().split(separator)));
            }
        }
        (claimsExcludingMultiValuedClaims.keySet()).removeAll(multiValuedClaimsToAdd.keySet());
        (claimsExcludingMultiValuedClaims.keySet()).removeAll(multiValuedClaimsToDelete.keySet());

        // Set claim values if user store is not read only.
        try {
            if (!isReadOnly()) {
                // If unique id feature is not enabled, we have to call the legacy methods.
                if (!isUniqueUserIdEnabledInUserStore(userStore)) {
                    User user = userUniqueIDManger.getUser(userID, this, userStore.getDomainName());
                    doSetUserClaimValues(user.getUsername(), multiValuedClaimsToAdd, multiValuedClaimsToDelete,
                            claimsExcludingMultiValuedClaims, profileName);
                } else {
                    doSetUserClaimValuesWithID(userID, multiValuedClaimsToAdd, multiValuedClaimsToDelete,
                            claimsExcludingMultiValuedClaims, profileName);
                }
            }
        } catch (NotImplementedException e) {
            if (!isUniqueUserIdEnabledInUserStore(userStore)) {
                User user = userUniqueIDManger.getUser(userID, this, userStore.getDomainName());
                doSetUserClaimValues(user.getUsername(), claims, profileName);
            } else {
                doSetUserClaimValuesWithID(userID, claims, profileName);
            }
        } catch (UserStoreException e) {
            handleSetUserClaimValuesFailureWithID(
                    ErrorMessages.ERROR_CODE_ERROR_WHILE_SETTING_USER_CLAIM_VALUES.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_SETTING_USER_CLAIM_VALUES.getMessage(),
                            e.getMessage()), userID, claims, profileName);
            throw e;
        }

        // #################### <Post Listeners> #####################################################
        invokeDoPostSetUserClaimsWithIDListeners(userID, claims, profileName);
        // #################### </Post Listeners> #####################################################
    }

    @Override
    public final void updateCredentialByAdminWithID(String userID, Object newCredential) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[] { String.class, Object.class };
            callSecure("updateCredentialByAdminWithID", new Object[] { userID, newCredential }, argTypes);
            return;
        }

        UserStore userStore = getUserStoreWithID(userID);
        if (userStore.isRecurssive()) {
            ((AbstractUserStoreManager) userStore.getUserStoreManager())
                    .updateCredentialByAdminWithID(userStore.getDomainFreeUserId(), newCredential);
            return;
        }

        // #################### Domain Name Free Zone Starts Here ################################

        if (isReadOnly()) {
            handleUpdateCredentialByAdminFailureWithID(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode(),
                    ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage(), userID, newCredential);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.toString());
        }

        Secret newCredentialObj;
        try {
            newCredentialObj = Secret.getSecret(newCredential);
        } catch (UnsupportedSecretTypeException e) {
            handleUpdateCredentialByAdminFailureWithID(ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.getCode(),
                    ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.getMessage() + " " + e.getMessage(), userID,
                    newCredential);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.toString(), e);
        }

        try {
            try {
                // #################### <Listeners> #####################################################
                for (UserStoreManagerListener listener : UMListenerServiceComponent.getUserStoreManagerListeners()) {
                    Object credentialArgument;
                    if (listener instanceof SecretHandleableListener) {
                        credentialArgument = newCredentialObj;
                    } else {
                        credentialArgument = newCredential;
                    }

                    if (!((AbstractUserStoreManagerListener) listener)
                            .updateCredentialByAdminWithID(userID, credentialArgument, this)) {
                        handleUpdateCredentialByAdminFailureWithID(
                                ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL_BY_ADMIN.getCode(),
                                String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL_BY_ADMIN
                                        .getMessage(), UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE),
                                userID, credentialArgument);
                        return;
                    }
                }

                // using string buffers to allow the password to be changed by listener
                for (UserOperationEventListener listener : UMListenerServiceComponent
                        .getUserOperationEventListeners()) {

                    if (listener instanceof SecretHandleableListener) {
                        if (!((AbstractUserOperationEventListener) listener)
                                .doPreUpdateCredentialByAdminWithID(userID, newCredentialObj, this)) {
                            handleUpdateCredentialByAdminFailureWithID(
                                    ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL_BY_ADMIN.getCode(),
                                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL_BY_ADMIN
                                            .getMessage(), UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE),
                                    userID, newCredentialObj);
                            return;
                        }
                    } else {
                        // using string buffers to allow the password to be changed by listener
                        StringBuffer credBuff = null;
                        if (newCredential == null) { // a default password will be set
                            credBuff = new StringBuffer();
                        } else if (newCredential instanceof String) {
                            credBuff = new StringBuffer((String) newCredential);
                        }

                        if (credBuff != null) {
                            if (!((AbstractUserOperationEventListener) listener)
                                    .doPreUpdateCredentialByAdminWithID(userID, credBuff, this)) {
                                handleUpdateCredentialByAdminFailureWithID(
                                        ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL_BY_ADMIN.getCode(),
                                        String.format(
                                                ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL_BY_ADMIN
                                                        .getMessage(),
                                                UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userID,
                                        credBuff);
                                return;
                            }
                            // reading the modified value
                            newCredential = credBuff.toString();
                            newCredentialObj.clear();
                            try {
                                newCredentialObj = Secret.getSecret(newCredential);
                            } catch (UnsupportedSecretTypeException e) {
                                handleUpdateCredentialByAdminFailureWithID(
                                        ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.getCode(),
                                        ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.getMessage() + " " + e
                                                .getMessage(), userID, newCredential);
                                throw new UserStoreException(
                                        ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.toString(), e);
                            }
                        }
                    }
                }
            } catch (UserStoreException ex) {
                handleUpdateCredentialByAdminFailureWithID(
                        ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL_BY_ADMIN.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL_BY_ADMIN.getMessage(),
                                ex.getMessage()), userID, newCredential);
                throw ex;
            }
            // #################### </Listeners> #####################################################

            if (!checkUserPasswordValid(newCredential)) {
                String errorMsg = realmConfig.getUserStoreProperty(PROPERTY_PASSWORD_ERROR_MSG);

                if (errorMsg != null) {
                    String errorCode = ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL_BY_ADMIN.getCode();
                    String errorMessage = String
                            .format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL_BY_ADMIN.getMessage(),
                                    errorMsg);
                    handleUpdateCredentialByAdminFailureWithID(errorCode, errorMessage, userID, newCredential);
                    throw new UserStoreException(errorCode + " - " + errorMessage);
                }

                String errorCode = ErrorMessages.ERROR_CODE_INVALID_PASSWORD.getCode();
                String errorMessage = String.format(ErrorMessages.ERROR_CODE_INVALID_PASSWORD.getMessage(),
                        realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_JAVA_REG_EX));
                handleUpdateCredentialByAdminFailureWithID(errorCode, errorMessage, userID, newCredential);
                throw new UserStoreException(errorCode + " - " + errorMessage);
            }

            boolean isUniqueIdEnabled = isUniqueUserIdEnabledInUserStore(userStore);
            boolean isUserExists;
            if (isUniqueIdEnabled) {
                isUserExists = doCheckExistingUserWithID(userID);
            } else {
                String userNameFromUserID = doGetUserNameFromUserID(userID);
                isUserExists = userNameFromUserID != null;
            }

            if (!isUserExists) {
                String errorMessage = String.format(ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getMessage(), userID,
                        realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME));
                String errorCode = ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getCode();
                handleUpdateCredentialByAdminFailureWithID(errorCode, errorMessage, userID, newCredential);
                throw new UserStoreException(errorCode + "-" + errorMessage);
            }

            try {
                // If unique id feature is not enabled, we have to call the legacy methods.
                if (!isUniqueUserIdEnabledInUserStore(userStore)) {
                    User user = userUniqueIDManger.getUser(userID, this, userStore.getDomainName());
                    doUpdateCredentialByAdmin(user.getUsername(), newCredential);
                } else {
                    doUpdateCredentialByAdminWithID(userID, newCredentialObj);
                }
            } catch (UserStoreException ex) {
                handleUpdateCredentialByAdminFailureWithID(
                        ErrorMessages.ERROR_CODE_ERROR_WHILE_UPDATING_CREDENTIAL_BY_ADMIN.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_UPDATING_CREDENTIAL_BY_ADMIN.getMessage(),
                                ex.getMessage()), userID, newCredentialObj);
                throw ex;
            }

            // #################### <Listeners> #####################################################
            try {
                for (UserOperationEventListener listener : UMListenerServiceComponent
                        .getUserOperationEventListeners()) {
                    Object credentialArgument;
                    if (listener instanceof SecretHandleableListener) {
                        credentialArgument = newCredentialObj;
                    } else {
                        credentialArgument = newCredential;
                    }

                    if (!((AbstractUserOperationEventListener) listener)
                            .doPostUpdateCredentialByAdminWithID(userID, credentialArgument, this)) {
                        handleUpdateCredentialByAdminFailureWithID(
                                ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_CREDENTIAL_BY_ADMIN.getCode(),
                                String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_CREDENTIAL_BY_ADMIN
                                        .getMessage(), UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE),
                                userID, newCredential);
                        return;
                    }
                }
            } catch (UserStoreException ex) {
                handleUpdateCredentialByAdminFailureWithID(
                        ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_CREDENTIAL_BY_ADMIN.getCode(), String.format(
                                ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_CREDENTIAL_BY_ADMIN.getMessage(),
                                ex.getMessage()), userID, newCredential);
                throw ex;
            }
        } finally {
            newCredentialObj.clear();
            // This value is set in the validation lister if the password gets validated
            // against the configured set of rules.
            UserCoreUtil.removeSkipPasswordPatternValidationThreadLocal();
        }
        // #################### </Listeners> #####################################################

    }

    @Override
    public final void deleteUserClaimValueWithID(String userID, String claimURI, String profileName)
            throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[] { String.class, String.class, String.class };
            callSecure("deleteUserClaimValueWithID", new Object[] { userID, claimURI, profileName }, argTypes);
            return;
        }

        UserStore userStore = getUserStoreWithID(userID);
        if (userStore.isRecurssive()) {
            ((AbstractUserStoreManager) userStore.getUserStoreManager())
                    .deleteUserClaimValueWithID(userStore.getDomainFreeUserId(), claimURI, profileName);
            return;
        }

        if (isReadOnly()) {
            handleDeleteUserClaimValueFailureWithID(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode(),
                    ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage(), userID, claimURI, profileName);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.toString());
        }

        User user = null;
        boolean isUserEixisting;
        if (isUniqueUserIdEnabledInUserStore(userStore)) {
            isUserEixisting = doCheckExistingUserWithID(userID);
        } else {
            user = userUniqueIDManger.getUser(userID, this, userStore.getDomainName());
            isUserEixisting = user != null;
        }

        if (!isUserEixisting) {
            String errorMessage = String.format(ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getMessage(), userID,
                    realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME));
            String errorCode = ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getCode();
            handleDeleteUserClaimValueFailureWithID(errorCode, errorMessage, userID, claimURI, profileName);
            throw new UserStoreException(errorCode + " - " + errorMessage);
        }

        // #################### <Listeners> #####################################################
        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!((AbstractUserOperationEventListener) listener)
                        .doPreDeleteUserClaimValueWithID(userID, claimURI, profileName, this)) {
                    handleDeleteUserClaimValueFailureWithID(
                            ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_USER_CLAIM_VALUE.getCode(), String.format(
                                    ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_USER_CLAIM_VALUE.getMessage(),
                                    UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userID, claimURI,
                            profileName);
                    return;
                }
            }
            // #################### </Listeners> #####################################################
        } catch (UserStoreException ex) {
            handleDeleteUserClaimValueFailureWithID(
                    ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_USER_CLAIM_VALUE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_USER_CLAIM_VALUE.getMessage(),
                            ex.getMessage()), userID, claimURI, profileName);
            throw ex;
        }

        try {

            // If unique id feature is not enabled, we have to call the legacy methods.
            if (!isUniqueUserIdEnabledInUserStore(userStore)) {
                doDeleteUserClaimValue(user.getUsername(), claimURI, profileName);
            } else {
                doDeleteUserClaimValueWithID(userID, claimURI, profileName);
            }
        } catch (UserStoreException ex) {
            handleDeleteUserClaimValueFailureWithID(
                    ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETING_USER_CLAIM_VALUE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETING_USER_CLAIM_VALUE.getMessage(),
                            ex.getMessage()), userID, claimURI, profileName);
            throw ex;
        }

        // #################### <Listeners> #####################################################
        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!((AbstractUserOperationEventListener) listener).doPostDeleteUserClaimValueWithID(userID, this)) {
                    handleDeleteUserClaimValueFailureWithID(
                            ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_USER_CLAIM_VALUE.getCode(), String.format(
                                    ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_USER_CLAIM_VALUE.getMessage(),
                                    UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), userID, claimURI,
                            profileName);
                    return;
                }
            }
        } catch (UserStoreException ex) {
            handleDeleteUserClaimValueFailureWithID(
                    ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_USER_CLAIM_VALUE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_USER_CLAIM_VALUE.getMessage(),
                            ex.getMessage()), userID, claimURI, profileName);
            throw ex;
        }
        // #################### </Listeners> #####################################################
    }

    @Override
    public final void deleteUserClaimValuesWithID(String userID, String[] claims, String profileName)
            throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[] { String.class, String[].class, String.class };
            callSecure("deleteUserClaimValuesWithID", new Object[] { userID, claims, profileName }, argTypes);
            return;
        }

        UserStore userStore = getUserStoreWithID(userID);
        if (userStore.isRecurssive()) {
            ((AbstractUserStoreManager) userStore.getUserStoreManager())
                    .deleteUserClaimValuesWithID(userStore.getDomainFreeUserId(), claims, profileName);
            return;
        }

        if (isReadOnly()) {
            handleDeleteUserClaimValuesFailureWithID(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode(),
                    ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage(), userID, claims, profileName);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.toString());
        }

        User user = null;
        boolean isUserExisting;
        if (isUniqueUserIdEnabledInUserStore(userStore)) {
            isUserExisting = doCheckExistingUserWithID(userID);
        } else {
            user = userUniqueIDManger.getUser(userID, this, userStore.getDomainName());
            isUserExisting = user != null;
        }

        if (!isUserExisting) {
            String errorMessage = String.format(ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getMessage(), userID,
                    realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME));
            String errorCode = ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getCode();
            handleDeleteUserClaimValuesFailureWithID(errorCode, errorMessage, userID, claims, profileName);
            throw new UserStoreException(errorCode + " - " + errorMessage);
        }

        if (claims == null) {
            claims = new String[0];
        }
        // #################### <Listeners> #####################################################
        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!((AbstractUserOperationEventListener) listener)
                        .doPreDeleteUserClaimValuesWithID(userID, claims, profileName, this)) {
                    handleDeleteUserClaimValuesFailureWithID(
                            ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_USER_CLAIM_VALUES.getCode(), String.format(
                                    ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_USER_CLAIM_VALUES.getMessage(),
                                    UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userID, claims,
                            profileName);
                    return;
                }
            }
            // #################### </Listeners> #####################################################
        } catch (UserStoreException ex) {
            handleDeleteUserClaimValuesFailureWithID(
                    ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_USER_CLAIM_VALUES.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_USER_CLAIM_VALUES.getMessage(),
                            ex.getMessage()), userID, claims, profileName);
            throw ex;
        }

        try {
            // If unique id feature is not enabled, we have to call the legacy methods.
            if (!isUniqueUserIdEnabledInUserStore(userStore)) {
                doDeleteUserClaimValues(user.getUsername(), claims, profileName);
            } else {
                doDeleteUserClaimValuesWithID(userID, claims, profileName);
            }
        } catch (UserStoreException ex) {
            handleDeleteUserClaimValuesFailureWithID(
                    ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETING_USER_CLAIM_VALUES.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETING_USER_CLAIM_VALUES.getMessage(),
                            ex.getMessage()), userID, claims, profileName);
            throw ex;
        }
        // #################### <Listeners> #####################################################
        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!((AbstractUserOperationEventListener) listener).doPostDeleteUserClaimValuesWithID(userID, this)) {
                    handleDeleteUserClaimValuesFailureWithID(
                            ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_USER_CLAIM_VALUES.getCode(),
                            String.format(
                                    ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_USER_CLAIM_VALUES.getMessage(),
                                    UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), userID, claims,
                            profileName);
                    return;
                }
            }
        } catch (UserStoreException ex) {
            handleDeleteUserClaimValuesFailureWithID(
                    ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_USER_CLAIM_VALUES.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_USER_CLAIM_VALUES.getMessage(),
                            ex.getMessage()), userID, claims, profileName);
            throw ex;
        }
        // #################### </Listeners> #####################################################
    }

    @Override
    public Date getPasswordExpirationTimeWithID(String userID) throws UserStoreException {

        UserStore userStore = getUserStoreWithID(userID);
        if (userStore.isRecurssive()) {
            return ((AbstractUserStoreManager) userStore.getUserStoreManager())
                    .getPasswordExpirationTimeWithID(userStore.getDomainFreeUserId());
        }
        return null;
    }

    @Override
    public final void updateRoleListOfUserWithID(final String userID, final String[] deletedRoles,
            final String[] newRoles) throws UserStoreException {

        try {
            AccessController.doPrivileged((PrivilegedExceptionAction<String>) () -> {
                // If unique id feature is not enabled, we have to call the legacy methods.
                if (!isUniqueUserIdEnabled()) {
                    User user = userUniqueIDManger.getUser(userID, this);
                    if (user == null) {
                        throw new UserStoreException("User cannot be found.");
                    }
                    updateRoleListOfUserInternal(user.getDomainQualifiedUsername(), deletedRoles, newRoles);
                    return null;
                } else {
                    updateRoleListOfUserInternalWithID(userID, deletedRoles, newRoles);
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            if (!(e.getException() instanceof UserStoreException)) {
                handleUpdateRoleListOfUserFailureWithID(
                        ErrorMessages.ERROR_CODE_ERROR_DURING_UPDATE_USERS_OF_ROLE.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_UPDATE_USERS_OF_ROLE.getMessage(),
                                e.getMessage()), userID, deletedRoles, newRoles);
            }
            throw (UserStoreException) e.getException();
        }
    }

    /**
     * Update role list of user.
     */
    private final void updateRoleListOfUserInternalWithID(String userID, String[] deletedRoles, String[] newRoles)
            throws UserStoreException {

        String primaryDomain = realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
        if (primaryDomain != null) {
            primaryDomain += CarbonConstants.DOMAIN_SEPARATOR;
        }

        if (deletedRoles != null && deletedRoles.length > 0) {
            Arrays.sort(deletedRoles);
            if (UserCoreUtil.isPrimaryAdminUser(userID, realmConfig)) {
                for (int i = 0; i < deletedRoles.length; i++) {
                    if (deletedRoles[i].equalsIgnoreCase(realmConfig.getAdminRoleName()) || (primaryDomain
                            + deletedRoles[i]).equalsIgnoreCase(realmConfig.getAdminRoleName())) {
                        handleUpdateRoleListOfUserFailureWithID(
                                ErrorMessages.ERROR_CODE_CANNOT_REMOVE_ADMIN_ROLE_FROM_ADMIN.getCode(),
                                ErrorMessages.ERROR_CODE_CANNOT_REMOVE_ADMIN_ROLE_FROM_ADMIN.getMessage(), userID,
                                deletedRoles, newRoles);
                        throw new UserStoreException(
                                ErrorMessages.ERROR_CODE_CANNOT_REMOVE_ADMIN_ROLE_FROM_ADMIN.toString());
                    }
                }
            }
        }

        UserStore userStore = getUserStoreWithID(userID);
        if (userStore.isRecurssive()) {
            ((AbstractUserStoreManager) userStore.getUserStoreManager())
                    .updateRoleListOfUserWithID(userStore.getDomainFreeUserId(),
                            UserCoreUtil.removeDomainFromNames(deletedRoles),
                            UserCoreUtil.removeDomainFromNames(newRoles));
            return;
        }

        if (userStore.isSystemStore()) {
            systemUserRoleManager.updateSystemRoleListOfUser(userStore.getDomainAwareUserId(),
                    UserCoreUtil.removeDomainFromNames(deletedRoles), UserCoreUtil.removeDomainFromNames(newRoles));
            return;
        }

        // #################### Domain Name Free Zone Starts Here ################################
        if (deletedRoles == null) {
            deletedRoles = new String[0];
        }
        if (newRoles == null) {
            newRoles = new String[0];
        }
        // This happens only once during first startup - adding administrator user/role.
        if (userID.indexOf(CarbonConstants.DOMAIN_SEPARATOR) > 0) {
            userID = userStore.getDomainFreeName();
            deletedRoles = UserCoreUtil.removeDomainFromNames(deletedRoles);
            newRoles = UserCoreUtil.removeDomainFromNames(newRoles);
        }

        List<String> internalRoleDel = new ArrayList<>();
        List<String> internalRoleNew = new ArrayList<>();

        List<String> roleDel = new ArrayList<>();
        List<String> roleNew = new ArrayList<>();

        List<String> internalRoleDelWithDomain = new ArrayList<>();
        List<String> internalRoleNewWithDomain = new ArrayList<>();

        if (deletedRoles != null && deletedRoles.length > 0) {
            for (String deleteRole : deletedRoles) {
                if (UserCoreUtil.isEveryoneRole(deleteRole, realmConfig)) {
                    handleUpdateRoleListOfUserFailureWithID(
                            ErrorMessages.ERROR_CODE_CANNOT_UPDATE_EVERYONE_ROLE.getCode(),
                            ErrorMessages.ERROR_CODE_CANNOT_UPDATE_EVERYONE_ROLE.getMessage(), userID, deletedRoles,
                            newRoles);
                    throw new UserStoreException(ErrorMessages.ERROR_CODE_CANNOT_UPDATE_EVERYONE_ROLE.toString());
                }
                String domain = null;
                int index1 = deleteRole.indexOf(CarbonConstants.DOMAIN_SEPARATOR);
                if (index1 > 0) {
                    domain = deleteRole.substring(0, index1);
                }
                processDeletedRoles(internalRoleDel, roleDel, deleteRole, domain, internalRoleDelWithDomain);
            }
            deletedRoles = roleDel.toArray(new String[0]);
        }

        if (newRoles != null && newRoles.length > 0) {
            for (String newRole : newRoles) {
                if (UserCoreUtil.isEveryoneRole(newRole, realmConfig)) {
                    handleUpdateRoleListOfUserFailureWithID(
                            ErrorMessages.ERROR_CODE_CANNOT_UPDATE_EVERYONE_ROLE.getCode(),
                            ErrorMessages.ERROR_CODE_CANNOT_UPDATE_EVERYONE_ROLE.getMessage(), userID, deletedRoles,
                            newRoles);
                    throw new UserStoreException(ErrorMessages.ERROR_CODE_CANNOT_UPDATE_EVERYONE_ROLE.toString());
                }
                String domain = null;
                int index2 = newRole.indexOf(CarbonConstants.DOMAIN_SEPARATOR);
                if (index2 > 0) {
                    domain = newRole.substring(0, index2);
                }

                processNewRoles(internalRoleNew, roleNew, newRole, domain, internalRoleNewWithDomain);
            }
            newRoles = roleNew.toArray(new String[0]);
        }

        boolean isPreUpdateInternalRoleListOfUserSuccess = true;
        boolean isPreUpdateRoleListOfUserSuccess = true;
        String[] deletedInternalRolesArray = new String[0];
        String[] addInternalRolesArray = new String[0];

        if (CollectionUtils.isNotEmpty(internalRoleDel) || CollectionUtils.isNotEmpty(internalRoleNew)) {
            deletedInternalRolesArray = internalRoleDel.toArray(new String[internalRoleDel.size()]);
            String[] deletedRolesArrayWithDomain = internalRoleDelWithDomain.toArray(new
                    String[internalRoleDelWithDomain.size()]);
            addInternalRolesArray = internalRoleNew.toArray(new String[internalRoleNew.size()]);
            String[] addRolesArrayWithDomain =
                    internalRoleNewWithDomain.toArray(new String[internalRoleNewWithDomain.size()]);

            isPreUpdateInternalRoleListOfUserSuccess = handlePreUpdateRoleListOfUserWithID(userID,
                    deletedRolesArrayWithDomain, addRolesArrayWithDomain, false, true);
        }

        if (ArrayUtils.isNotEmpty(deletedRoles) || ArrayUtils.isNotEmpty(newRoles)) {
            isPreUpdateRoleListOfUserSuccess = handlePreUpdateRoleListOfUserWithID(userID, deletedRoles, newRoles,
                    false, false);
        }

        if (!isPreUpdateInternalRoleListOfUserSuccess || !isPreUpdateRoleListOfUserSuccess) {
            handleUpdateRoleListOfUserFailureWithID(
                    ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_ROLE_OF_USER.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_ROLE_OF_USER.getMessage(),
                            UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userID, deletedRoles,
                    newRoles);
            return;
        }
        if (ArrayUtils.isNotEmpty(deletedInternalRolesArray) || ArrayUtils.isNotEmpty(addInternalRolesArray)) {
            hybridRoleManager.updateHybridRoleListOfUser(doGetUserNameFromUserID(userID), deletedInternalRolesArray,
                    addInternalRolesArray);
        }

        // #################### <Listeners> #####################################################
        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!((AbstractUserOperationEventListener) listener)
                        .doPreUpdateRoleListOfUserWithID(userID, deletedRoles, newRoles, this)) {
                    handleUpdateRoleListOfUserFailureWithID(
                            ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_ROLE_OF_USER.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_ROLE_OF_USER.getMessage(),
                                    UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userID, deletedRoles,
                            newRoles);
                    return;
                }
            }
        } catch (UserStoreException ex) {
            handleUpdateRoleListOfUserFailureWithID(
                    ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_ROLE_OF_USER.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_ROLE_OF_USER.getMessage(),
                            ex.getMessage()), userID, deletedRoles, newRoles);
            throw ex;
        }
        // #################### </Listeners> #####################################################

        if ((deletedRoles != null && deletedRoles.length > 0) || (newRoles != null && newRoles.length > 0)) {
            if (!isReadOnly() && writeGroupsEnabled) {
                try {
                    if (isUniqueUserIdEnabledInUserStore(userStore)) {
                        doUpdateRoleListOfUserWithID(userID, deletedRoles, newRoles);
                    } else {
                        doUpdateRoleListOfUser(doGetUserNameFromUserID(userID), deletedRoles, newRoles);
                    }
                } catch (UserStoreException ex) {
                    handleUpdateRoleListOfUserFailureWithID(
                            ErrorMessages.ERROR_CODE_ERROR_WHILE_UPDATING_ROLE_OF_USER.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_UPDATING_ROLE_OF_USER.getMessage(),
                                    ex.getMessage()), userID, deletedRoles, newRoles);
                    throw ex;
                }
            } else {
                handleUpdateRoleListOfUserFailureWithID(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode(),
                        ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage(), userID, deletedRoles, newRoles);
                throw new UserStoreException(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.toString());
            }
        }

        // Clear user role cache from username.
        String username = doGetUserNameFromUserID(userID);
        if (username != null) {
            clearUserRolesCache(username);
        }

        // Call the relevant listeners after updating the role list of user.
        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!((AbstractUserOperationEventListener) listener)
                        .doPostUpdateRoleListOfUserWithID(userID, deletedRoles, newRoles, this)) {
                    handleUpdateRoleListOfUserFailureWithID(
                            ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_ROLE_OF_USER.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_ROLE_OF_USER.getMessage(),
                                    UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), userID, deletedRoles,
                            newRoles);
                    return;
                }
            }
        } catch (UserStoreException ex) {
            handleUpdateRoleListOfUserFailureWithID(
                    ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_ROLE_OF_USER.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_ROLE_OF_USER.getMessage(),
                            ex.getMessage()), userID, deletedRoles, newRoles);
            throw ex;
        }
    }

    private boolean handlePreUpdateUserListOfRole(String roleName, String[] deletedUsers, String[] newUsers,
                                                  boolean isAuditLogOnly, boolean isInternalRole) throws
            UserStoreException {

        // Calling pre listeners.
        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (isAuditLogOnly &&
                        !listener.getClass().getName().endsWith(UserCoreErrorConstants.AUDIT_LOGGER_CLASS_NAME)) {
                    continue;
                }

                boolean success;
                if (isInternalRole && listener instanceof AbstractUserOperationEventListener) {
                    success = ((AbstractUserOperationEventListener) listener).doPreUpdateUserListOfInternalRole(
                            roleName, deletedUsers, newUsers, this);
                } else if (isInternalRole) {
                    success = true;
                } else {
                    success = listener.doPreUpdateUserListOfRole(roleName, deletedUsers, newUsers, this);
                }

                if (!success) {
                    return false;
                }
            }
        } catch (UserStoreException ex) {
            handleUpdateUserListOfRoleFailure(
                    ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_UPDATE_USERS_OF_ROLE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_UPDATE_USERS_OF_ROLE.getMessage(),
                            ex.getMessage()), roleName, deletedUsers,
                    newUsers);
            throw ex;
        }
        return true;
    }

    private boolean handlePreUpdateUserListOfRoleWithID(String roleName, String[] deletedUsersIDs, String[] newUsersIDs,
                                                        boolean isAuditLogOnly, boolean isInternalRole) throws
            UserStoreException {

        // Calling pre listeners.
        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (isAuditLogOnly &&
                        !listener.getClass().getName().endsWith(UserCoreErrorConstants.AUDIT_LOGGER_CLASS_NAME)) {
                    continue;
                }
                boolean success;
                if (isInternalRole && listener instanceof AbstractUserOperationEventListener) {
                    success = ((AbstractUserOperationEventListener) listener).doPreUpdateUserListOfInternalRoleWithID
                            (roleName, deletedUsersIDs, newUsersIDs, this);
                } else if (isInternalRole) {
                    success = true;
                } else {
                    success = ((AbstractUserOperationEventListener) listener).doPreUpdateUserListOfRoleWithID(roleName,
                            deletedUsersIDs, newUsersIDs, this);
                }

                if (!success) {
                    return false;
                }
            }
        } catch (UserStoreException ex) {
            handleUpdateUserListOfRoleFailureWithID(
                    ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_UPDATE_USERS_OF_ROLE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_UPDATE_USERS_OF_ROLE.getMessage(),
                            ex.getMessage()), roleName, deletedUsersIDs, newUsersIDs);
            throw ex;
        }
        return true;
    }

    private boolean handlePreUpdateRoleListOfUser(String userName, String[] deletedRoles, String[] newRoles,
                                                  boolean isAuditLogOnly, boolean isInternalRole) throws
            UserStoreException {

        // Calling pre listeners.
        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (isAuditLogOnly &&
                        !listener.getClass().getName().endsWith(UserCoreErrorConstants.AUDIT_LOGGER_CLASS_NAME)) {
                    continue;
                }

                boolean success;
                if (isInternalRole && listener instanceof AbstractUserOperationEventListener) {
                    success =
                            ((AbstractUserOperationEventListener) listener).doPreUpdateInternalRoleListOfUser(userName,
                                    deletedRoles, newRoles, this);
                } else if (isInternalRole) {
                    success = true;
                } else {
                    success = listener.doPreUpdateRoleListOfUser(userName, deletedRoles, newRoles, this);
                }

                if (!success) {
                    return false;
                }
            }
        } catch (UserStoreException ex) {
            handleUpdateRoleListOfUserFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_ROLE_OF_USER.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_ROLE_OF_USER.getMessage(),
                            ex.getMessage()), userName, deletedRoles, newRoles);
            throw ex;
        }
        return true;
    }

    private boolean handlePreUpdateRoleListOfUserWithID(String userID, String[] deletedRoles, String[] newRoles,
                                                        boolean isAuditLogOnly, boolean isInternalRole) throws
            UserStoreException {

        // Calling pre listeners.
        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (isAuditLogOnly &&
                        !listener.getClass().getName().endsWith(UserCoreErrorConstants.AUDIT_LOGGER_CLASS_NAME)) {
                    continue;
                }

                boolean success = true;
                if (isInternalRole && listener instanceof AbstractUserOperationEventListener) {
                    success = ((AbstractUserOperationEventListener) listener).doPreUpdateInternalRoleListOfUserWithID(
                            userID, deletedRoles, newRoles, this);
                } else if (isInternalRole) {
                    success = true;
                } else if (listener instanceof AbstractUserOperationEventListener) {
                    success = ((AbstractUserOperationEventListener) listener).doPreUpdateRoleListOfUserWithID(userID,
                            deletedRoles, newRoles, this);
                }

                if (!success) {
                    return false;
                }
            }
        } catch (UserStoreException ex) {
            handleUpdateRoleListOfUserFailureWithID(
                    ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_ROLE_OF_USER.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_ROLE_OF_USER.getMessage(),
                            ex.getMessage()), userID, deletedRoles, newRoles);
            throw ex;
        }
        return true;
    }

    private void processNewRoles(List<String> internalRoleNew, List<String> roleNew, String newRole, String domain,
                                 List<String> internalRoleNewWithDomain)
            throws UserStoreException {
        if (UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(domain)) {
            // If this is an internal role.
            internalRoleNew.add(UserCoreUtil.removeDomainFromName(newRole));
            internalRoleNewWithDomain.add(newRole);
        } else if (APPLICATION_DOMAIN.equalsIgnoreCase(domain) || WORKFLOW_DOMAIN.equalsIgnoreCase(domain)) {
            // If this is an application role or workflow role.
            internalRoleNew.add(newRole);
            internalRoleNewWithDomain.add(newRole);
        } else if (this.isReadOnly()) {
            // If this is a readonly user store, we add even normal roles as internal roles.
            internalRoleNew.add(UserCoreUtil.removeDomainFromName(newRole));
            internalRoleNewWithDomain.add(newRole);
        } else {
            roleNew.add(UserCoreUtil.removeDomainFromName(newRole));
        }
    }

    private void processDeletedRoles(List<String> internalRoleDel, List<String> roleDel, String deleteRole,
            String domain, List<String> internalRoleDelWithDomain) throws
            UserStoreException {
        if (APPLICATION_DOMAIN.equalsIgnoreCase(domain) || WORKFLOW_DOMAIN.equalsIgnoreCase(domain)) {
            internalRoleDel.add(deleteRole);
            internalRoleDelWithDomain.add(deleteRole);
        } else if (UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(domain) || this.isReadOnly()) {
            internalRoleDel.add(UserCoreUtil.removeDomainFromName(deleteRole));
            internalRoleDelWithDomain.add(deleteRole);
        } else {
            // This is domain free role name.
            roleDel.add(UserCoreUtil.removeDomainFromName(deleteRole));
        }
    }

    @Override
    public final void updateUserListOfRoleWithID(final String roleName,  String[] deletedUserIDs,
             String[] newUserIDs) throws UserStoreException {

        try {
            AccessController.doPrivileged((PrivilegedExceptionAction<String>) () -> {
                // If unique id feature is not enabled, we have to call the legacy methods.
                UserStore userStore = getUserStore(roleName);
                if (!isUniqueUserIdEnabledInUserStore(userStore)) {
                    List<User> deletedUsers = new ArrayList<>();
                    List<User> newUsers = new ArrayList<>();
                    if (deletedUserIDs != null) {
                        deletedUsers = userUniqueIDManger.getUsers(Arrays.asList(deletedUserIDs), this);
                    }
                    if (newUserIDs != null) {
                        newUsers = userUniqueIDManger.getUsers(Arrays.asList(newUserIDs), this);
                    }
                    // If we don't have a record for this user, let's try to call directly using the user id.
                    updateUserListOfRoleInternal(roleName, deletedUsers.stream().map(User::getDomainQualifiedUsername)
                            .toArray(String[]::new), newUsers.stream().map(User::getDomainQualifiedUsername).toArray(String[]::new));
                    return null;
                }
                updateUserListOfRoleInternalWithID(roleName, deletedUserIDs, newUserIDs);
                return null;
            });
        } catch (PrivilegedActionException e) {
            if (!(e.getException() instanceof UserStoreException)) {
                handleUpdateRoleListOfUserFailureWithID(
                        ErrorMessages.ERROR_CODE_ERROR_WHILE_UPDATING_ROLE_OF_USER.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_UPDATING_ROLE_OF_USER.getMessage(),
                                e.getMessage()), roleName, deletedUserIDs, newUserIDs);
            }
            throw (UserStoreException) e.getException();
        }
    }

    /**
     * update user list of role.
     */
    private final void updateUserListOfRoleInternalWithID(String roleName, String[] deletedUserIDs, String[] newUserIDs)
            throws UserStoreException {

        String[] deletedUsernames = new String[0];
        if (ArrayUtils.isNotEmpty(deletedUserIDs)) {
            List<String> deletedUsernameList = getUserNamesFromUserIDs(Arrays.asList(deletedUserIDs));
            deletedUsernames = deletedUsernameList.toArray(new String[0]);
        }

        String[] newUsernames = new String[0];
        if (ArrayUtils.isNotEmpty(newUserIDs)) {
            List<String> newUsernameList = getUserNamesFromUserIDs(Arrays.asList(newUserIDs));
            newUsernames = newUsernameList.toArray(new String[0]);
        }

        String primaryDomain = getMyDomainName();
        if (primaryDomain != null) {
            primaryDomain += CarbonConstants.DOMAIN_SEPARATOR;
        }

        if (deletedUsernames.length > 0) {
            Arrays.sort(deletedUsernames);
            // Updating the user list of a role belong to the primary domain.
            if (UserCoreUtil.isPrimaryAdminRole(roleName, realmConfig)) {
                for (int i = 0; i < deletedUsernames.length; i++) {
                    if (deletedUsernames[i].equalsIgnoreCase(realmConfig.getAdminUserName()) || (primaryDomain
                            + deletedUsernames[i]).equalsIgnoreCase(realmConfig.getAdminUserName())) {
                        handleUpdateRoleListOfUserFailureWithID(
                                ErrorMessages.ERROR_CODE_CANNOT_REMOVE_ADMIN_ROLE_FROM_ADMIN.getCode(),
                                ErrorMessages.ERROR_CODE_CANNOT_REMOVE_ADMIN_ROLE_FROM_ADMIN.getMessage(), roleName,
                                deletedUserIDs, newUserIDs);
                        throw new UserStoreException(
                                ErrorMessages.ERROR_CODE_CANNOT_REMOVE_ADMIN_ROLE_FROM_ADMIN.toString());
                    }

                }
            }
        }

        UserStore userStore = getUserStoreOfRoles(roleName);

        if (userStore.isHybridRole()) {
            // Check whether someone is trying to update Everyone role.
            if (UserCoreUtil.isEveryoneRole(roleName, realmConfig)) {
                handleUpdateRoleListOfUserFailureWithID(ErrorMessages.ERROR_CODE_CANNOT_UPDATE_EVERYONE_ROLE.getCode(),
                        ErrorMessages.ERROR_CODE_CANNOT_UPDATE_EVERYONE_ROLE.getMessage(), roleName, deletedUserIDs,
                        newUserIDs);
                throw new UserStoreException(ErrorMessages.ERROR_CODE_CANNOT_UPDATE_EVERYONE_ROLE.toString());
            }

            if (!handlePreUpdateUserListOfRoleWithID(roleName, deletedUserIDs, newUserIDs, false,
                    true)) {
                handleUpdateUserListOfRoleFailureWithID(
                        ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_UPDATE_USERS_OF_ROLE.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_UPDATE_USERS_OF_ROLE.getMessage(),
                                UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), roleName, deletedUserIDs,
                        newUserIDs);
                return;
            }
            if (UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(userStore.getDomainName())) {
                hybridRoleManager.updateUserListOfHybridRole(userStore.getDomainFreeName(), deletedUsernames,
                        newUsernames);
                handleDoPostUpdateUserListOfRoleWithID(roleName, deletedUserIDs, newUserIDs, false, true);
            } else {
                hybridRoleManager.updateUserListOfHybridRole(userStore.getDomainAwareName(), deletedUsernames,
                        newUsernames);
                handleDoPostUpdateUserListOfRoleWithID(roleName, deletedUserIDs, newUserIDs, false, true);
            }
            clearUserRolesCacheByTenant(this.tenantId);
            return;
        }

        if (userStore.isSystemStore()) {
            systemUserRoleManager.updateUserListOfSystemRole(userStore.getDomainFreeName(),
                    UserCoreUtil.removeDomainFromNames(deletedUsernames),
                    UserCoreUtil.removeDomainFromNames(newUsernames));
            handleDoPostUpdateUserListOfRoleWithID(roleName, deletedUserIDs, newUserIDs, true,
                    true);
            return;
        }

        if (userStore.isRecurssive()) {
            ((AbstractUserStoreManager) userStore.getUserStoreManager())
                    .updateUserListOfRoleWithID(userStore.getDomainFreeName(),
                            UserCoreUtil.removeDomainFromNames(deletedUserIDs),
                            UserCoreUtil.removeDomainFromNames(newUserIDs));
            return;
        }

        // #################### Domain Name Free Zone Starts Here ################################
        if (deletedUserIDs == null) {
            deletedUserIDs = new String[0];
        }
        if (newUserIDs == null) {
            newUserIDs = new String[0];
        }
        if (!handlePreUpdateUserListOfRoleWithID(roleName, deletedUserIDs, newUserIDs, false,
                false)) {
            handleUpdateUserListOfRoleFailureWithID(
                    ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_UPDATE_USERS_OF_ROLE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_UPDATE_USERS_OF_ROLE.getMessage(),
                            UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), roleName, deletedUserIDs,
                    newUserIDs);
            return;
        }
        // #################### </Listeners> #####################################################

        if (deletedUserIDs.length > 0 || newUserIDs.length > 0) {
            if (!isReadOnly() && writeGroupsEnabled) {
                try {
                    // No need to check for group unique id feature here. Because any userstore should have this
                    // feature OOTB.
                    doUpdateUserListOfRoleWithID(userStore.getDomainFreeName(),
                            UserCoreUtil.removeDomainFromNames(deletedUserIDs),
                            UserCoreUtil.removeDomainFromNames(newUserIDs));
                } catch (UserStoreException ex) {
                    handleUpdateRoleListOfUserFailureWithID(
                            ErrorMessages.ERROR_CODE_ERROR_DURING_UPDATE_USERS_OF_ROLE.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_UPDATE_USERS_OF_ROLE.getMessage(),
                                    ex.getMessage()), roleName, deletedUserIDs, newUserIDs);
                    throw ex;
                }
            } else {
                handleUpdateRoleListOfUserFailureWithID(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode(),
                        ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage(), roleName, deletedUserIDs,
                        newUserIDs);
                throw new UserStoreException(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.toString());
            }
        }

        // need to clear user roles cache upon roles update
        clearUserRolesCacheByTenant(this.tenantId);

        // Call relevant listeners after updating user list of role.
        handleDoPostUpdateUserListOfRoleWithID(roleName, deletedUserIDs, newUserIDs, false, false);
    }

    @Override
    public final User addUserWithID(String userName, Object credential, String[] roleList, Map<String, String> claims,
            String profileName) throws UserStoreException {

        // We have to make sure this call is going through the Java Security Manager.
        if (!isSecureCall.get()) {
            Class[] argTypes = new Class[] {
                    String.class, Object.class, String[].class, Map.class, String.class
            };
            Object object = callSecure("addUserWithID",
                    new Object[] { userName, credential, roleList, claims, profileName }, argTypes);
            return (User) object;
        }

        // If we don't have a username, we cannot proceed.
        if (StringUtils.isEmpty(userName)) {
            String message = ErrorMessages.ERROR_CODE_USERNAME_CANNOT_BE_EMPTY.getMessage();
            String errorCode = ErrorMessages.ERROR_CODE_USERNAME_CANNOT_BE_EMPTY.getCode();
            handleAddUserFailureWithID(errorCode, message, null, credential, roleList, claims, profileName);
            throw new UserStoreException(errorCode + " - " + message, errorCode);
        }

        // If the username claims presents, the value should be equal to the username attribute.
        String userNameWithoutDomain = UserCoreUtil.removeDomainFromName(userName);
        if (claims != null && claims.containsKey(USERNAME_CLAIM_URI) &&
                !claims.get(USERNAME_CLAIM_URI).equals(userNameWithoutDomain)) {
            // If not we cannot continue.
            throw new UserStoreException("Username and the username claim value should be same.");
        }

        UserStore userStore = getUserStore(userName);
        if (userStore.isRecurssive()) {
            return ((AbstractUserStoreManager) userStore.getUserStoreManager())
                    .addUserWithID(userStore.getDomainFreeName(), credential, roleList, claims, profileName);
        }

        // Convert the credential (Password) to a Secret.
        Secret credentialObj;
        try {
            credentialObj = Secret.getSecret(credential);
        } catch (UnsupportedSecretTypeException e) {
            handleAddUserFailureWithID(ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.getCode(),
                    ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.getMessage(), userName, credential, roleList,
                    claims, profileName);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.toString(), e);
        }

        User user;
        try {
            if (userStore.isSystemStore()) {
                systemUserRoleManager.addSystemUser(userName, credentialObj, roleList);
                return null;
            }

            // #################### Domain Name Free Zone Starts Here ################################

            if (isReadOnly()) {
                handleAddUserFailureWithID(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode(),
                        ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage(), userName, credential, roleList,
                        claims, profileName);
                throw new UserStoreException(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.toString());
            }
            // Set skipPasswordPolicyValidation thread local if the user creation flow is ask password enabled.
            if (claims != null && claims.containsKey(UserCoreClaimConstants.ASK_PASSWORD_CLAIM_URI) &&
                    Boolean.parseBoolean(claims.get(UserCoreClaimConstants.ASK_PASSWORD_CLAIM_URI))) {
                UserCoreUtil.setSkipPasswordPatternValidationThreadLocal(true);
            }

            // This happens only once during first startup - adding administrator user/role.
            if (userName.indexOf(CarbonConstants.DOMAIN_SEPARATOR) > 0) {
                userName = userStore.getDomainFreeName();
                roleList = UserCoreUtil.removeDomainFromNames(roleList);
            }
            if (roleList == null) {
                roleList = new String[0];
            }
            if (claims == null) {
                claims = new HashMap<>();
            }

            // #################### <Pre-Listeners> #####################################################
            try {
                // First call user store manager listeners.
                for (UserStoreManagerListener listener : UMListenerServiceComponent.getUserStoreManagerListeners()) {
                    Object credentialArgument;
                    if (listener instanceof SecretHandleableListener) {
                        credentialArgument = credentialObj;
                    } else {
                        credentialArgument = credential;
                    }

                    if (!((AbstractUserStoreManagerListener) listener)
                            .addUserWithID(userName, credentialArgument, roleList, claims, profileName, this)) {
                        handleAddUserFailureWithID(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_USER.getCode(),
                                String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_USER.getMessage(),
                                        UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userName, credential,
                                roleList, claims, profileName);
                        return null;
                    }
                }
            } catch (UserStoreException ex) {
                handleAddUserFailureWithID(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_USER.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_USER.getMessage(), ex.getMessage()),
                        userName, credential, roleList, claims, profileName);
                throw ex;
            }

            // Then call the user operation listeners.
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {

                // This is to call all new listeners. All listeners should support the Secret object as the credential
                // for security reasons.
                if (listener instanceof SecretHandleableListener) {
                    try {
                        if (!((AbstractUserOperationEventListener) listener)
                                .doPreAddUserWithID(userName, credentialObj, roleList, claims, profileName, this)) {
                            handleAddUserFailureWithID(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_USER.getCode(),
                                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_USER.getMessage(),
                                            UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userName,
                                    credential, roleList, claims, profileName);
                            return null;
                        }
                    } catch (UserStoreException ex) {
                        String message = String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_USER.getMessage(),
                                ex.getMessage());
                        handleAddUserFailureWithID(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_USER.getCode(),
                                message, userName, credential, roleList, claims, profileName);
                        throw ex;
                    }
                } else {
                    // This is to support the legacy listeners which does not know how to handle the Secret object as
                    // the credentials.

                    // String buffers are used to let listeners to modify passwords
                    StringBuffer credBuff = null;
                    if (credential == null) {
                        // A default password will be set if the credential is null.
                        credBuff = new StringBuffer();
                    } else if (credential instanceof String) {
                        credBuff = new StringBuffer((String) credential);
                    }

                    if (credBuff != null) {
                        try {
                            if (!((AbstractUserOperationEventListener) listener)
                                    .doPreAddUserWithID(userName, credBuff, roleList, claims, profileName, this)) {
                                handleAddUserFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_USER.getCode(),
                                        String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_USER.getMessage(),
                                                UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userName,
                                        credential, roleList, claims, profileName);
                                return null;
                            }
                        } catch (UserStoreException e) {
                            handleAddUserFailureWithID(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_USER.getCode(),
                                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_USER.getMessage(),
                                            e.getMessage()), userName, credential, roleList, claims, profileName);
                            throw e;
                        }
                        // Reading the modified value.
                        credential = credBuff.toString();
                        credentialObj.clear();
                        try {
                            // Create the Secret from the modified credential.
                            credentialObj = Secret.getSecret(credential);
                        } catch (UnsupportedSecretTypeException e) {
                            handleAddUserFailureWithID(ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.getCode(),
                                    ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.getMessage(), userName,
                                    credential, roleList, claims, profileName);
                            throw new UserStoreException(
                                    ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.toString(), e);
                        }
                    }
                }
            }

            // #################### </Pre-Listeners> #####################################################

            // Validate the username against provided regular expressions.
            if (!checkUserNameValid(userStore.getDomainFreeName())) {
                String regEx = realmConfig
                        .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USER_NAME_JAVA_REG_EX);
                // Inorder to support both UsernameJavaRegEx and UserNameJavaRegEx.
                if (StringUtils.isEmpty(regEx) || StringUtils.isEmpty(regEx.trim())) {
                    regEx = realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USER_NAME_JAVA_REG);
                }
                String message = String
                        .format(ErrorMessages.ERROR_CODE_INVALID_USER_NAME.getMessage(), userStore.getDomainFreeName(),
                                regEx);
                String errorCode = ErrorMessages.ERROR_CODE_INVALID_USER_NAME.getCode();
                handleAddUserFailureWithID(errorCode, message, userName, credential, roleList, claims, profileName);
                throw new UserStoreException(errorCode + " - " + message);
            }

            // Validate the password against provided regular expressions.
            if (!checkUserPasswordValid(credentialObj)) {
                String regEx = realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_JAVA_REG_EX);
                String message = String.format(ErrorMessages.ERROR_CODE_INVALID_PASSWORD.getMessage(), regEx);
                String errorCode = ErrorMessages.ERROR_CODE_INVALID_PASSWORD.getCode();
                handleAddUserFailureWithID(errorCode, message, userName, credential, roleList, claims, profileName);
                throw new UserStoreException(errorCode + " - " + message);
            }

            // Property to check whether this user store supports new APIs with unique user id.
            boolean isUniqueUserIdEnabled = isUniqueUserIdEnabledInUserStore(userStore);

            boolean isExistingUser;
            if (isUniqueUserIdEnabled) {
                isExistingUser = getUserIDFromUserName(userName) != null;
            } else {
                isExistingUser = doCheckExistingUser(userName);
            }
            // Check if the user already exists in the user store.
            if (isExistingUser) {
                String message = String.format(ErrorMessages.ERROR_CODE_USER_ALREADY_EXISTS.getMessage(), userName);
                String errorCode = ErrorMessages.ERROR_CODE_USER_ALREADY_EXISTS.getCode();
                handleAddUserFailureWithID(errorCode, message, userName, credential, roleList, claims, profileName);
                throw new UserStoreException(errorCode + " - " + message);
            }

            // Filter roles into internal roles and external roles.
            List<String> internalRoles = new ArrayList<>();
            List<String> externalRoles = new ArrayList<>();
            filterRoles(roleList, internalRoles, externalRoles);

            // Check existence of roles and claims before adding user.
            for (String internalRole : internalRoles) {
                if (!hybridRoleManager.isExistingRole(internalRole)) {
                    String message = String
                            .format(ErrorMessages.ERROR_CODE_INTERNAL_ROLE_NOT_EXISTS.getMessage(), internalRole);
                    String errorCode = ErrorMessages.ERROR_CODE_INTERNAL_ROLE_NOT_EXISTS.getCode();
                    handleAddUserFailureWithID(errorCode, message, userName, credential, roleList, claims, profileName);
                    throw new UserStoreException(errorCode + " - " + message);
                }
            }

            // Check whether external roles are existing in the user store.
            for (String externalRole : externalRoles) {
                if (!doCheckExistingRole(externalRole)) {
                    String message = String
                            .format(ErrorMessages.ERROR_CODE_EXTERNAL_ROLE_NOT_EXISTS.getMessage(), externalRole);
                    String errorCode = ErrorMessages.ERROR_CODE_EXTERNAL_ROLE_NOT_EXISTS.getCode();
                    handleAddUserFailureWithID(errorCode, message, userName, credential, roleList, claims, profileName);
                    throw new UserStoreException(errorCode + " - " + message);
                }
            }

            // Check whether the claims are existing.
            for (Map.Entry<String, String> entry : claims.entrySet()) {
                ClaimMapping claimMapping;
                try {
                    claimMapping = (ClaimMapping) claimManager.getClaimMapping(entry.getKey());
                } catch (org.wso2.carbon.user.api.UserStoreException e) {
                    String errorMessage = String
                            .format(ErrorMessages.ERROR_CODE_UNABLE_TO_FETCH_CLAIM_MAPPING.getMessage(),
                                    "persisting user attributes.");
                    String errorCode = ErrorMessages.ERROR_CODE_UNABLE_TO_FETCH_CLAIM_MAPPING.getCode();
                    handleAddUserFailureWithID(errorCode, errorMessage, userName, credential, roleList, claims,
                            profileName);
                    throw new UserStoreException(errorCode + " - " + errorMessage, e);
                }
                if (claimMapping == null) {
                    String errorMessage = String
                            .format(ErrorMessages.ERROR_CODE_INVALID_CLAIM_URI.getMessage(), entry.getKey());
                    String errorCode = ErrorMessages.ERROR_CODE_INVALID_CLAIM_URI.getCode();
                    handleAddUserFailureWithID(errorCode, errorMessage, userName, credential, roleList, claims,
                            profileName);
                    throw new UserStoreException(errorCode + " - " + errorMessage);
                }
            }

            // Call the do add user method of the underlying user store to add the user.
            try {
                // If unique user id property is enabled, then we can call the new methods in the user store.
                if (isUniqueUserIdEnabled) {
                    user = doAddUserWithID(userName, credentialObj, externalRoles.toArray(new String[0]), claims,
                            profileName, false);
                } else {
                    // If the underlying user store does not support the unique ID generation, then we have to generate
                    // the ID and keep the mapping in our side.
                    String uniqueId = UUID.randomUUID().toString();
                    claims.put(UserCoreClaimConstants.USER_ID_CLAIM_URI, uniqueId);
                    doAddUser(userName, credentialObj, externalRoles.toArray(new String[0]), claims, profileName,
                            false);
                    user = new User();
                    user.setUserID(uniqueId);
                    user.setUsername(userStore.getDomainFreeName());
                    user.setUserStoreDomain(this.getMyDomainName());
                }
            } catch (UserStoreException ex) {
                handleAddUserFailureWithID(ErrorMessages.ERROR_CODE_ERROR_WHILE_ADDING_USER.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_ADDING_USER.getMessage(), ex.getMessage()),
                        userName, credential, roleList, claims, profileName);
                throw ex;
            }

            if (internalRoles.size() > 0) {
                hybridRoleManager.updateHybridRoleListOfUser(userName, null, internalRoles.toArray(new String[0]));
            }

            // #################### <Post-Listeners> #####################################################
            try {
                for (UserOperationEventListener listener : UMListenerServiceComponent
                        .getUserOperationEventListeners()) {
                    Object credentialArgument;
                    if (listener instanceof SecretHandleableListener) {
                        credentialArgument = credentialObj;
                    } else {
                        credentialArgument = credential;
                    }

                    if (!((AbstractUserOperationEventListener) listener)
                            .doPostAddUserWithID(user, credentialArgument, roleList, claims, profileName, this)) {
                        handleAddUserFailureWithID(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_ADD_USER.getCode(),
                                String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_ADD_USER.getMessage(),
                                        UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), userName,
                                credential, roleList, claims, profileName);
                        return null;
                    }
                }
            } catch (UserStoreException ex) {
                handleAddUserFailureWithID(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_ADD_USER.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_ADD_USER.getMessage(),
                                ex.getMessage()), userName, credential, roleList, claims, profileName);
                throw ex;
            }
            // #################### </Post-Listeners> #####################################################
        } finally {
            UserCoreUtil.removeSkipPasswordPatternValidationThreadLocal();
            UserCoreUtil.removeSkipUsernamePatternValidationThreadLocal();
            credentialObj.clear();
        }

        // Clean the role cache since it contains old role information.
        clearUserRolesCache(userName);
        return user;
    }

    @Override
    public void addRoleWithID(String roleName, String[] userIDList, Permission[] permissions, boolean isSharedRole)
            throws UserStoreException {

        if (StringUtils.isEmpty(roleName)) {
            handleAddRoleFailureWithID(ErrorMessages.ERROR_CODE_CANNOT_ADD_EMPTY_ROLE.getCode(),
                    ErrorMessages.ERROR_CODE_CANNOT_ADD_EMPTY_ROLE.getMessage(), roleName, userIDList, permissions);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_CANNOT_ADD_EMPTY_ROLE.toString());
        }

        if (userIDList == null) {
            userIDList = new String[0];
        }
        UserStore userStore = getUserStore(roleName);

        if (isSharedRole && !isSharedGroupEnabled()) {
            handleAddRoleFailureWithID(ErrorMessages.ERROR_CODE_SHARED_ROLE_NOT_SUPPORTED.getCode(),
                    ErrorMessages.ERROR_CODE_SHARED_ROLE_NOT_SUPPORTED.getMessage(), roleName, userIDList, permissions);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_SHARED_ROLE_NOT_SUPPORTED.toString());
        }
        String[] userList = new String[0];
        if (!isUniqueUserIdEnabledInUserStore(userStore)) {
            userList = userUniqueIDManger.getUsers(Arrays.asList(userIDList), this)
                    .stream()
                    .map(User::getDomainQualifiedUsername)
                    .toArray(String[]::new);
        }

        if (userStore.isHybridRole()) {
            //Invoke Pre listeners for hybrid roles.
            if (!handlePreAddRoleWithID(roleName, userIDList, permissions, false)) {
                return;
            }

            if (isUniqueUserIdEnabledInUserStore(userStore)) {
                doAddInternalRoleWithID(roleName, userIDList, permissions);
            } else {
                doAddInternalRole(roleName, userList, permissions);
            }
            // Calling only the audit logger, to maintain the back-ward compatibility
            handlePostAddRoleWithID(roleName, userIDList, permissions, false);
            return;
        }

        if (userStore.isRecurssive()) {
            ((UniqueIDUserStoreManager) userStore.getUserStoreManager())
                    .addRoleWithID(userStore.getDomainFreeName(), UserCoreUtil.removeDomainFromNames(userIDList),
                            permissions, isSharedRole);
            return;
        }

        // #################### Domain Name Free Zone Starts Here ################################
        if (userIDList == null) {
            userIDList = new String[0];
        }
        if (permissions == null) {
            permissions = new Permission[0];
        }
        // This happens only once during first startup - adding administrator user/role.
        if (roleName.indexOf(CarbonConstants.DOMAIN_SEPARATOR) > 0) {
            roleName = userStore.getDomainFreeName();
            userIDList = UserCoreUtil.removeDomainFromNames(userIDList);
        }

        // #################### <Listeners> #####################################################
        if (!handlePreAddRoleWithID(roleName, userIDList, permissions, false)) {
            return;
        }
        // #################### </Listeners> #####################################################

        // Check for validations
        if (isReadOnly()) {
            handleAddRoleFailureWithID(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode(),
                    ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage(), roleName, userIDList, permissions);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.toString());
        }

        if (!isRoleNameValid(roleName)) {
            String regEx = realmConfig
                    .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_ROLE_NAME_JAVA_REG_EX);
            String errorMessage = String
                    .format(ErrorMessages.ERROR_CODE_INVALID_ROLE_NAME.getMessage(), roleName, regEx);
            String errorCode = ErrorMessages.ERROR_CODE_INVALID_ROLE_NAME.getCode();
            handleAddRoleFailureWithID(errorCode, errorMessage, roleName, userIDList, permissions);
            throw new UserStoreException(errorCode + " - " + errorMessage);
        }

        if (doCheckExistingRole(roleName)) {
            handleRoleAlreadyExistExceptionWithID(roleName, userIDList, permissions);
        }

        String roleWithDomain;
        if (writeGroupsEnabled) {
            try {
                // add role in to actual user store
                if (!isUniqueUserIdEnabledInUserStore(userStore)) {
                    List<User> users = userUniqueIDManger.getUsers(Arrays.asList(userIDList), this);
                    doAddRole(roleName, users.stream().map(User::getUsername).toArray(String[]::new), isSharedRole);
                } else {
                    if (isUniqueGroupIdEnabled()) {
                        Group group = doAddGroup(roleName, generateGroupUUID(), Arrays.asList(userIDList), null);
                        groupUniqueIDDomainResolver.setDomainForGroupId(group.getGroupID(), getMyDomainName(), tenantId,
                                false);
                    } else {
                        doAddRoleWithID(roleName, userIDList, isSharedRole);
                    }
                }
                roleWithDomain = UserCoreUtil.addDomainToName(roleName, getMyDomainName());
            } catch (UserStoreException ex) {
                handleAddRoleFailureWithID(ErrorMessages.ERROR_CODE_ERROR_WHILE_ADDING_ROLE.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_ADDING_ROLE.getMessage(), ex.getMessage()),
                        roleName, userIDList, permissions);
                throw ex;
            }
        } else {
            handleAddRoleFailureWithID(ErrorMessages.ERROR_CODE_WRITE_GROUPS_NOT_ENABLED.getCode(),
                    ErrorMessages.ERROR_CODE_WRITE_GROUPS_NOT_ENABLED.getMessage(), roleName, userIDList,
                    permissions);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_WRITE_GROUPS_NOT_ENABLED.toString());
        }

        // add permission in to the the permission store
        for (org.wso2.carbon.user.api.Permission permission : permissions) {
            String resourceId = permission.getResourceId();
            String action = permission.getAction();
            if (resourceId == null || resourceId.trim().length() == 0) {
                continue;
            }

            if (action == null || action.trim().length() == 0) {
                // default action value
                action = "read";
            }
            // This is a special case. We need to pass domain aware name.
            userRealm.getAuthorizationManager().authorizeRole(roleWithDomain, resourceId, action);
        }

        // if existing users are added to role, need to update user role cache
        if ((userIDList != null) && (userIDList.length > 0)) {
            clearUserRolesCacheByTenant(tenantId);
        }

        // #################### <Listeners> #####################################################
        handlePostAddRoleWithID(roleName, userIDList, permissions, false);
        // #################### </Listeners> #####################################################
    }

    /**
     * This method is responsible for calling relevant postAddRole listener methods after successfully adding role.
     *
     * @param roleName       Name of the role.
     * @param userList       List of users.
     * @param permissions    Permissions that are assigned to the role.
     * @param isAuditLogOnly To indicate whether to only call the relevant audit logger.
     * @throws UserStoreException Exception that will be thrown by relevant listeners.
     */
    private void handlePostAddRoleWithID(String roleName, String[] userList, Permission[] permissions,
            boolean isAuditLogOnly) throws UserStoreException {

        try {
            boolean internalRole = isAnInternalRole(roleName);
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (isAuditLogOnly && !listener.getClass().getName()
                        .endsWith(UserCoreErrorConstants.AUDIT_LOGGER_CLASS_NAME)) {
                    continue;
                }

                boolean success = false;
                if (internalRole && listener instanceof AbstractUserOperationEventListener) {
                    success = ((AbstractUserOperationEventListener) listener)
                            .doPostAddInternalRoleWithID(roleName, userList, permissions, this);
                } else if (internalRole && !(listener instanceof AbstractUserOperationEventListener)) {
                    success = true;
                } else if (!internalRole) {
                    success = ((UniqueIDUserOperationEventListener) listener)
                            .doPostAddRoleWithID(roleName, userList, permissions, this);
                }

                if (!success) {
                    handleAddRoleFailureWithID(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_ADD_ROLE.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_ADD_ROLE.getMessage(),
                                    UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), roleName, userList,
                            permissions);
                    return;
                }
            }
        } catch (UserStoreException ex) {
            handleAddRoleFailureWithID(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_ADD_ROLE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_ADD_ROLE.getMessage(), ex.getMessage()),
                    roleName, userList, permissions);
            throw ex;
        }
    }

    /**
     * This method is responsible for calling relevant preAddGroup listener methods before adding a group.
     *
     * @param groupName Name of the group.
     * @param userIds   List of users.
     * @param claims    List of claims.
     * @return True if all the listeners are executed successfully.
     * @throws UserStoreException Exception that will be thrown by relevant listeners.
     */
    private boolean handlePreAddGroup(String groupName, List<String> userIds,
                                      List<org.wso2.carbon.user.core.common.Claim> claims) throws UserStoreException {

        try {
            for (GroupOperationEventListener listener : UMListenerServiceComponent
                    .getGroupOperationEventListeners()) {
                if (listener instanceof AbstractGroupOperationEventListener) {
                    AbstractGroupOperationEventListener newListener = (AbstractGroupOperationEventListener) listener;
                    if (!newListener.preAddGroup(groupName, userIds, claims, this)) {
                        handlePreAddGroupFailure(ErrorMessages.ERROR_DURING_PRE_ADD_GROUP.getCode(),
                                String.format(ErrorMessages.ERROR_DURING_PRE_ADD_GROUP.getMessage(),
                                        UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), groupName, userIds,
                                claims);
                        return false;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleAddGroupFailure(ErrorMessages.ERROR_DURING_PRE_ADD_GROUP.getCode(),
                    String.format(ErrorMessages.ERROR_DURING_PRE_ADD_GROUP.getMessage(), ex.getMessage()), groupName,
                    null, userIds, claims);
            throw ex;
        }
        return true;
    }

    /**
     * This method is responsible for calling relevant postAddGroup listener methods after successfully adding a group.
     *
     * @param groupName Name of the group.
     * @param groupId   Id of the group.
     * @param userIds   List of users.
     * @param claims    List of claims.
     * @throws UserStoreException Exception that will be thrown by relevant listeners.
     */
    private void handlePostAddGroup(String groupName, String groupId, List<String> userIds,
                                    List<org.wso2.carbon.user.core.common.Claim> claims) throws UserStoreException {

        try {
            for (GroupOperationEventListener listener : UMListenerServiceComponent
                    .getGroupOperationEventListeners()) {
                if (listener instanceof AbstractGroupOperationEventListener) {
                    AbstractGroupOperationEventListener newListener = (AbstractGroupOperationEventListener) listener;
                    if (!newListener.postAddGroup(groupName, groupId, userIds, claims, this)) {
                        handlePostAddGroupFailure(ErrorMessages.ERROR_DURING_POST_ADD_GROUP.getCode(),
                                String.format(ErrorMessages.ERROR_DURING_POST_ADD_GROUP.getMessage(),
                                        UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), groupName, groupId,
                                userIds, claims);
                        return;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleAddGroupFailure(ErrorMessages.ERROR_DURING_POST_ADD_GROUP.getCode(),
                    String.format(ErrorMessages.ERROR_DURING_POST_ADD_GROUP.getMessage(), ex.getMessage()), groupName,
                    groupId, userIds, claims);
            throw ex;
        }
    }

    /**
     * Handle pre add role tasks.
     */
    private boolean handlePreAddRoleWithID(String roleName, String[] userList, Permission[] permissions,
            boolean isAuditLogOnly) throws UserStoreException {

        try {
            boolean internalRole = isAnInternalRole(roleName);
            String internalSystemRolePrefix = INTERNAL_DOMAIN + DOMAIN_SEPARATOR + INTERNAL_SYSTEM_ROLE_PREFIX;
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (isAuditLogOnly && !listener.getClass().getName()
                        .endsWith(UserCoreErrorConstants.AUDIT_LOGGER_CLASS_NAME)) {
                    continue;
                }

                boolean success;
                if (internalRole && roleName.startsWith(internalSystemRolePrefix)) {
                    success = true;
                } else if (internalRole && listener instanceof AbstractUserOperationEventListener) {
                    success = ((AbstractUserOperationEventListener) listener)
                            .doPreAddInternalRoleWithID(roleName, userList, permissions, this);
                } else if (internalRole) {
                    success = true;
                } else {
                    success = ((UniqueIDUserOperationEventListener) listener)
                            .doPreAddRoleWithID(roleName, userList, permissions, this);
                }
                if (!success) {
                    handleAddRoleFailureWithID(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_ROLE.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_ROLE.getMessage(),
                                    UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), roleName, userList,
                            permissions);
                    return false;
                }
            }
        } catch (UserStoreException ex) {
            handleAddRoleFailureWithID(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_ROLE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_ROLE.getMessage(), ex.getMessage()),
                    roleName, userList, permissions);
            throw ex;
        }
        return true;
    }

    /**
     * Add the internal roles for the users.
     *
     * @param roleName    role name.
     * @param userIDList    user List.
     * @param permissions permissions.
     * @throws UserStoreException An unexpected exception has occurred.
     */
    protected void doAddInternalRoleWithID(String roleName, String[] userIDList,
                                           org.wso2.carbon.user.api.Permission[] permissions)
            throws UserStoreException {

        // #################### Domain Name Free Zone Starts Here ################################

        String[] users = getUserNamesFromUserIDs(Arrays.asList(userIDList)).toArray(new String[0]);
        if (roleName.contains(UserCoreConstants.DOMAIN_SEPARATOR) && roleName.toLowerCase()
                .startsWith(APPLICATION_DOMAIN.toLowerCase())) {
            if (hybridRoleManager.isExistingRole(roleName)) {
                handleRoleAlreadyExistExceptionWithID(roleName, userIDList, permissions);
            }
            hybridRoleManager.addHybridRole(roleName, users);
        } else {
            if (hybridRoleManager.isExistingRole(UserCoreUtil.removeDomainFromName(roleName))) {
                handleRoleAlreadyExistExceptionWithID(roleName, userIDList, permissions);
            }
            hybridRoleManager.addHybridRole(UserCoreUtil.removeDomainFromName(roleName), users);
        }

        if (permissions != null) {
            for (org.wso2.carbon.user.api.Permission permission : permissions) {
                String resourceId = permission.getResourceId();
                String action = permission.getAction();
                // This is a special case. We need to pass domain aware name.
                userRealm.getAuthorizationManager()
                        .authorizeRole(UserCoreUtil.addInternalDomainName(roleName), resourceId, action);
            }
        }

        if ((userIDList != null) && (userIDList.length > 0)) {
            clearUserRolesCacheByTenant(this.tenantId);
        }
    }

    /**
     * Categorize roles to the internal and external.
     */
    private void filterRoles(String[] roleList, List<String> internalRoles, List<String> externalRoles) {

        if (roleList == null) {
            return;
        }

        int index;
        for (String role : roleList) {
            if (role.trim().length() == 0) {
                continue;
            }
            index = role.indexOf(CarbonConstants.DOMAIN_SEPARATOR);
            if (index > 0) {
                String domain = role.substring(0, index);
                if (UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(domain)) {
                    internalRoles.add(UserCoreUtil.removeDomainFromName(role));
                    continue;
                } else if (APPLICATION_DOMAIN.equalsIgnoreCase(domain) || WORKFLOW_DOMAIN
                        .equalsIgnoreCase(domain)) {
                    internalRoles.add(role);
                    continue;
                }
            }
            externalRoles.add(UserCoreUtil.removeDomainFromName(role));
        }
    }

    @Override
    public boolean isUserInRoleWithID(String userID, String roleName) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class[] argTypes = new Class[]{String.class, String.class};
            Object object = callSecure("isUserInRoleWithID", new Object[] { userID, roleName }, argTypes);
            return (Boolean) object;
        }

        UserStore userStore = getUserStoreWithID(userID);

        if (userStore.isRecurssive() && (userStore.getUserStoreManager() instanceof AbstractUserStoreManager)) {
            return ((AbstractUserStoreManager) userStore.getUserStoreManager())
                    .isUserInRoleWithID(userStore.getDomainFreeUserId(), roleName);
        }

        // #################### Domain Name Free Zone Starts Here ################################
        // If unique id feature is not enabled, we have to call the legacy methods.
        if (!isUniqueUserIdEnabledInUserStore(userStore)) {
            User user = userUniqueIDManger.getUser(userID, this, userStore.getDomainName());
            // If we don't have a record for this user, let's try to call directly using the user id.
            if (user == null) {
                return false;
            }
            return isUserInRole(user.getDomainQualifiedUsername(), roleName);
        }

        if (roleName == null || roleName.trim().length() == 0 || userID == null || userID.trim().length() == 0) {
            return false;
        }

        // anonymous user is always assigned to  anonymous role
        if (CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME.equalsIgnoreCase(roleName)
                && CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equalsIgnoreCase(userID)) {
            return true;
        }

        if (!CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equalsIgnoreCase(userID) && realmConfig.getEveryOneRoleName()
                .equalsIgnoreCase(roleName) && !systemUserRoleManager.isExistingSystemUser(UserCoreUtil.
                removeDomainFromName(userID))) {
            return true;
        }

        String[] roles;

        String username = getUserNameFromUserID(userID);
        if (username == null) {
            roles = getRoleListOfUserFromCache(tenantId, userID);
            if (roles != null && roles.length > 0) {
                if (UserCoreUtil.isContain(roleName, roles)) {
                    return true;
                }
            }
        }

        String modifiedUserName = UserCoreConstants.IS_USER_IN_ROLE_CACHE_IDENTIFIER + username;
        roles = getRoleListOfUserFromCache(tenantId, modifiedUserName);
        if (roles != null && roles.length > 0) {
            if (UserCoreUtil.isContain(roleName, roles)) {
                return true;
            }
        }

        if (UserCoreConstants.INTERNAL_DOMAIN.
                equalsIgnoreCase(UserCoreUtil.extractDomainFromName(roleName)) || APPLICATION_DOMAIN
                .equalsIgnoreCase(UserCoreUtil.extractDomainFromName(roleName)) || WORKFLOW_DOMAIN
                .equalsIgnoreCase(UserCoreUtil.extractDomainFromName(roleName))) {

            List<String> internalRoles = doGetInternalRoleListOfUserWithID(userID, roleName);
            if (internalRoles.contains(roleName)) {
                addToIsUserHasRole(modifiedUserName, roleName, roles);
                return true;
            }
        }

        if (userStore.isSystemStore()) {
            return systemUserRoleManager
                    .isUserInRole(userStore.getDomainFreeUserId(), UserCoreUtil.removeDomainFromName(roleName));
        }
        // admin user is always assigned to admin role if it is in primary user store
        if (realmConfig.isPrimary() && roleName.equalsIgnoreCase(realmConfig.getAdminRoleName()) && userID
                .equalsIgnoreCase(realmConfig.getAdminUserName())) {
            return true;
        }

        String roleDomainName = UserCoreUtil.extractDomainFromName(roleName);

        String roleDomainNameForForest = realmConfig.
                getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_GROUP_SEARCH_DOMAINS);
        if (roleDomainNameForForest != null && roleDomainNameForForest.trim().length() > 0) {
            String[] values = roleDomainNameForForest.split("#");
            for (String value : values) {
                if (value != null && !value.trim().equalsIgnoreCase(roleDomainName)) {
                    return false;
                }
            }
        } else if (!userStore.getDomainName().equalsIgnoreCase(roleDomainName)) {
            return false;
        }

        boolean success = false;
        if (readGroupsEnabled) {
            success = doCheckIsUserInRoleWithID(userStore.getDomainFreeUserId(),
                    UserCoreUtil.removeDomainFromName(roleName));
        }

        // add to cache
        if (success) {
            addToIsUserHasRole(modifiedUserName, roleName, roles);
        }
        return success;
    }

    @Override
    public List<User> listUsersWithID(String filter, int limit, int offset) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class[] argTypes = new Class[]{String.class, int.class, int.class};
            Object object = callSecure("listUsersWithID", new Object[] { filter, limit, offset }, argTypes);
            return (List<User>) object;
        }

        int index = filter.indexOf(CarbonConstants.DOMAIN_SEPARATOR);
        UniqueIDPaginatedSearchResult userList;

        List<User> users;
        if (offset <= 0) {
            offset = 1;
        }

        if (index > 0) {
            String domain = filter.substring(0, index);

            UserStoreManager secManager = getSecondaryUserStoreManager(domain);
            if (secManager != null) {
                // Secondary UserStoreManager registered for this domain.
                filter = filter.substring(index + 1);
                if (secManager instanceof AbstractUserStoreManager) {
                    if (((AbstractUserStoreManager) secManager).isUniqueUserIdEnabled()) {
                        userList = ((AbstractUserStoreManager) secManager).doListUsersWithID(filter, limit, offset);
                        handlePostListPaginatedUsersWithID(filter, limit, offset, userList.getUsers(), true);
                    } else {
                        PaginatedSearchResult paginatedSearchResult = ((AbstractUserStoreManager) secManager)
                                .doListUsers(filter, limit, offset);
                        handlePostListPaginatedUsers(filter, limit, offset,
                                Arrays.asList(paginatedSearchResult.getUsers()), true);
                        userList = userUniqueIDManger.listUsers(paginatedSearchResult, this);
                    }
                    return userList.getUsers();
                }
            }
        } else if (index == 0) {
            if (isUniqueUserIdEnabled()) {
                userList = doListUsersWithID(filter.substring(1), limit, offset);
                handlePostListPaginatedUsersWithID(filter, limit, offset, userList.getUsers(), true);
            } else {
                PaginatedSearchResult paginatedSearchResult = doListUsers(filter.substring(1), limit, offset);
                handlePostListPaginatedUsers(filter, limit, offset, Arrays.asList(paginatedSearchResult.getUsers()),
                        true);
                userList = userUniqueIDManger.listUsers(paginatedSearchResult, this);
            }

            return userList.getUsers();
        }

        try {
            if (isUniqueUserIdEnabled()) {
                userList = doListUsersWithID(filter, limit, offset);
            } else {
                userList = userUniqueIDManger.listUsers(doListUsers(filter, limit, offset), this);
            }
            users = new ArrayList<>(userList.getUsers());
            limit = limit - users.size();
        } catch (UserStoreException ex) {
            handleGetPaginatedUserListFailureWithID(
                    ErrorMessages.ERROR_CODE_ERROR_WHILE_LISTING_PAGINATED_USERS.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_LISTING_PAGINATED_USERS.getMessage(),
                            ex.getMessage()), null, null, null);
            throw ex;
        }

        String primaryDomain = realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);

        int nonPaginatedUserCount = userList.getSkippedUserCount();
        if (this.getSecondaryUserStoreManager() != null) {
            for (Map.Entry<String, UserStoreManager> entry : userStoreManagerHolder.entrySet()) {
                if (limit <= 0) {
                    return users;
                }
                if (entry.getKey().equalsIgnoreCase(primaryDomain)) {
                    continue;
                }
                UserStoreManager storeManager = entry.getValue();
                if (storeManager instanceof AbstractUserStoreManager) {
                    try {

                        if (userList.getUsers().size() > 0) {
                            offset = 1;
                        } else {
                            offset = offset - nonPaginatedUserCount;
                        }

                        UniqueIDPaginatedSearchResult secondUserList;
                        if (((AbstractUserStoreManager) storeManager).isUniqueUserIdEnabled()) {
                            secondUserList = ((AbstractUserStoreManager) storeManager).doListUsersWithID(filter, limit, offset);
                            nonPaginatedUserCount = secondUserList.getSkippedUserCount();
                        } else {
                            PaginatedSearchResult paginatedSearchResult =
                                    ((AbstractUserStoreManager) storeManager).doListUsers(filter.substring(1), limit, offset);
                            secondUserList = userUniqueIDManger.listUsers(paginatedSearchResult, this);
                        }
                        users.addAll(secondUserList.getUsers());
                        limit = limit - users.size();
                    } catch (UserStoreException ex) {
                        handleGetPaginatedUserListFailure(
                                ErrorMessages.ERROR_CODE_ERROR_WHILE_LISTING_PAGINATED_USERS.getCode(),
                                String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_LISTING_PAGINATED_USERS.getMessage(),
                                        ex.getMessage()), null, null, null);

                        // We can ignore and proceed. Ignore the results from this user store.
                        log.error(ex);
                    }
                }
            }
        }
        handlePostListPaginatedUsersWithID(filter, limit, offset, users, true);
        return users;
    }

    @Override
    public List<User> getUserListWithID(String claim, String claimValue, String profileName, int limit, int offset)
            throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[] { String.class, String.class, String.class, int.class, int.class };
            Object object = callSecure("getUserListWithID",
                    new Object[] { claim, claimValue, profileName, limit, offset }, argTypes);
            return (List<User>) object;
        }

        if (claim == null) {
            String errorCode = ErrorMessages.ERROR_CODE_INVALID_CLAIM_URI.getCode();
            String errorMessage = String.format(ErrorMessages.ERROR_CODE_INVALID_CLAIM_URI.getMessage(), "");
            handleGetUserListFailureWithID(errorCode, errorMessage, null, claimValue, limit, offset, profileName);
            throw new IllegalArgumentException(ErrorMessages.ERROR_CODE_INVALID_CLAIM_URI.toString());
        }

        if (claimValue == null) {
            handleGetUserListFailureWithID(ErrorMessages.ERROR_CODE_INVALID_CLAIM_VALUE.getCode(), ErrorMessages.
                    ERROR_CODE_INVALID_CLAIM_VALUE.getMessage(), claim, null, limit, offset, profileName);
            throw new IllegalArgumentException(ErrorMessages.ERROR_CODE_INVALID_CLAIM_VALUE.toString());
        }

        if (log.isDebugEnabled()) {
            log.debug("Listing and paginate users who having value as " + claimValue + " for the claim " + claim);
        }

        if (USERNAME_CLAIM_URI.equalsIgnoreCase(claim) || SCIM_USERNAME_CLAIM_URI.equalsIgnoreCase(claim)
                || SCIM2_USERNAME_CLAIM_URI.equalsIgnoreCase(claim)) {

            if (log.isDebugEnabled()) {
                log.debug("Switching to paginate users using username");
            }

            List<User> filteredUsers;
            if (isUniqueUserIdEnabled()) {
                filteredUsers = listUsersWithID(claimValue, limit, offset);
            } else {
                filteredUsers = userUniqueIDManger.listUsers(listUsers(claimValue, limit, offset), this);
            }

            return filteredUsers;
        }

        // Extracting the domain from claimValue.
        String extractedDomain = null;
        int index;
        index = claimValue.indexOf(CarbonConstants.DOMAIN_SEPARATOR);
        if (index > 0) {
            String names[] = claimValue.split(CarbonConstants.DOMAIN_SEPARATOR);
            extractedDomain = names[0].trim();
        }

        UserStoreManager userManager = this;
        if (StringUtils.isNotEmpty(extractedDomain) && !StringUtils.equalsIgnoreCase(getMyDomainName(), extractedDomain)) {
            userManager = getSecondaryUserStoreManager(extractedDomain);
            if (userManager == null) {
                throw new UserStoreClientException("Invalid Domain Name: " + extractedDomain);
            }
            if (log.isDebugEnabled()) {
                log.debug("Domain: " + extractedDomain + " is passed with the claim and user store manager is loaded"
                        + " for the given domain name.");
            }
        }

        claimValue = UserCoreUtil.removeDomainFromName(claimValue);

        final List<User> filteredUserList = new ArrayList<>();

        if (StringUtils.isNotEmpty(extractedDomain)) {
            try {
                for (UserOperationEventListener listener : UMListenerServiceComponent
                        .getUserOperationEventListeners()) {
                    if (listener instanceof AbstractUserOperationEventListener) {
                        AbstractUserOperationEventListener newListener = (AbstractUserOperationEventListener) listener;
                        if (!newListener.doPreGetUserListWithID(claim, claimValue, limit, offset, filteredUserList,
                                userManager)) {
                            handleGetUserListFailureWithID(
                                    ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST.getCode(),
                                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST
                                                    .getMessage(),
                                            UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), claim,
                                    claimValue, limit, offset, profileName);
                            break;
                        }
                    }
                }
            } catch (UserStoreException ex) {
                handleGetUserListFailureWithID(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST.getMessage(),
                                ex.getMessage()), claim, claimValue, limit, offset, profileName);
                throw ex;
            }
        }

        // Iterate through user stores and check for users for this claim.
        List<User> usersFromUserStore;
        if (isUniqueUserIdEnabled()) {
            usersFromUserStore = doGetUserListWithID(claim, claimValue, profileName, limit, offset,
                    extractedDomain, userManager);
        } else {
            usersFromUserStore = userUniqueIDManger.listUsers(doGetUserList(claim, claimValue, profileName, limit,
                    offset, extractedDomain, userManager), this);
        }
        if (log.isDebugEnabled()) {
            if (StringUtils.isNotEmpty(extractedDomain)) {
                log.debug("Users from user store: " + extractedDomain + " : " + usersFromUserStore);
            }  else {
                log.debug("Users from all the user stores: " + usersFromUserStore);
            }
        }
        filteredUserList.addAll(usersFromUserStore);

        handlePostGetUserListWithID(claim, claimValue, filteredUserList, limit, offset, false);

        return filteredUserList;
    }

    @Override
    public List<User> getUserListWithID(Condition condition, String domain, String profileName, int limit, int offset,
                                        String sortBy, String sortOrder) throws UserStoreException {

        validateCondition(condition);
        if (StringUtils.isNotEmpty(sortBy) && StringUtils.isNotEmpty(sortOrder)) {
            throw new UserStoreException("Sorting is not supported.");
        }

        if (StringUtils.isEmpty(domain)) {
            domain = UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME;
        }

        if (StringUtils.isEmpty(profileName)) {
            profileName = UserCoreConstants.DEFAULT_PROFILE;
        }

        handlePreGetUserListWithID(condition, domain, profileName, limit, offset, sortBy, sortOrder);
        if (log.isDebugEnabled()) {
            log.debug("Pre listener get conditional  user list for domain: " + domain);
        }

        UserStoreManager secondaryUserStoreManager = getSecondaryUserStoreManager(domain);
        List<User> identityClaimFilteredUsers = new ArrayList<>();
        List<String> identityClaimFilteredUserNames = new ArrayList<>();
        List<User> filteredUsers = new ArrayList<>();
        boolean hasNonIdentityClaimFilterConditions = false;
        boolean isIdentityClaimsInIdentityStore = false;
        boolean isIdentityClaimFilterExistInPostCondition;
        List<ExpressionCondition> expressionConditions = new ArrayList<>();

        /* Duplicating condition object to ensure that nullifying the conditions in the flow does not affect the
        validation in the next iteration of flow when the domain name is not in query params.*/
        Condition duplicateCondition = getDuplicateCondition(condition);
        getExpressionConditions(duplicateCondition, expressionConditions);

        // Check whether the request has IdentityClaims in filters.
        mapAttributesToLocalIdentityClaims(expressionConditions);
        boolean identityClaimsExistsInInitialCondition = containsIdentityClaims(expressionConditions);

        if (identityClaimsExistsInInitialCondition) {
            if (expressionConditions.size() != countIdentityClaims(expressionConditions)) {
                hasNonIdentityClaimFilterConditions = true;
            }

            // Call the listeners to get the filtered users from relevant identity store.
            if (secondaryUserStoreManager != null) {
                handlePreGetUserListWithIdentityClaims(duplicateCondition, domain, profileName, limit, offset, sortBy,
                        sortOrder, secondaryUserStoreManager, identityClaimFilteredUserNames,
                        hasNonIdentityClaimFilterConditions);
            }

            // Check whether the request has IdentityClaims in filters after trying to filter at Identity Data Store.
            expressionConditions = new ArrayList<>();
            getExpressionConditions(duplicateCondition, expressionConditions);
            isIdentityClaimFilterExistInPostCondition = containsIdentityClaims(expressionConditions);

            // Means the identity claims are identity store based. (Else it is user store based)
            if (!isIdentityClaimFilterExistInPostCondition) {
                isIdentityClaimsInIdentityStore = true;
            } else {
                updateCondition(duplicateCondition, domain);
            }

            /* If identity claims are in JDBCIdentityDataStore, and filtering in JDBCIdentityDataStore returned an empty
         list, we can skip filtering in user store.*/
            if (isIdentityClaimsInIdentityStore && identityClaimFilteredUserNames.isEmpty()) {
                return filteredUsers;
            }

            // After filtering based on identity claims, if there are no other filters can return the list.
            if (expressionConditions.isEmpty()) {
                for (String username : identityClaimFilteredUserNames) {
                    User user = getUser(getUserIDFromUserName(username), username);
                    user.setUserStoreDomain(UserCoreUtil.extractDomainFromName(username));
                    identityClaimFilteredUsers.add(user);
                    addToUserIDCache(user.getUserID(), user.getUsername(), user.getUserStoreDomain());
                    addToUserNameCache(user.getUserID(), user.getUsername(), user.getUserStoreDomain());
                }
                return identityClaimFilteredUsers;
            }
        }

        if (identityClaimsExistsInInitialCondition && isIdentityClaimsInIdentityStore) {
            // The identity claims are not user store based.
            List<String> tempFilteredUsernames;
            List<String> aggregateUsernameList = new ArrayList<>();
            int offsetCounter = 0;
            int paginationLimit;

            if (offset <= 0) {
                paginationLimit = limit;
            } else {
                paginationLimit = (offset - 1) + limit;
            }

            Set<String> prevIterationFilteredUsernames = new HashSet<>();
            while (aggregateUsernameList.size() < paginationLimit) {
                tempFilteredUsernames =
                        getFilteredUsernames(duplicateCondition, profileName, limit, offsetCounter, sortBy,
                        sortOrder, secondaryUserStoreManager);

                if (tempFilteredUsernames.isEmpty()) {
                    // Means no users has been filtered in this particular iteration and hence can exit the flow.
                    break;
                }

                // Prevent same set of users being returned and break the loop if so.
                if (isExactSameFilteredUsernames(tempFilteredUsernames, prevIterationFilteredUsernames)) {
                    break;
                }

                prevIterationFilteredUsernames.clear();
                prevIterationFilteredUsernames.addAll(tempFilteredUsernames);

                // For next iteration consider the offset from last fetched size of users.
                offsetCounter += limit;

                // Taking the interception of the user list.
                tempFilteredUsernames.retainAll(identityClaimFilteredUserNames);
                aggregateUsernameList.addAll(tempFilteredUsernames);
            }

            // Removing duplicates.
            aggregateUsernameList = aggregateUsernameList.stream().distinct().collect(Collectors.toList());

            // Pagination
            if (offset <= 0) {
                offset = 0;
            } else {
                offset = offset - 1;
            }

            List<String> filteredUsernames;
            if (aggregateUsernameList.isEmpty()) {
                filteredUsernames = aggregateUsernameList;
            } else if (offset > aggregateUsernameList.size()) {
                filteredUsernames = new ArrayList<>();
            } else if (aggregateUsernameList.size() < paginationLimit) {
                filteredUsernames = aggregateUsernameList.subList(offset, aggregateUsernameList.size());
            } else {
                filteredUsernames = aggregateUsernameList.subList(offset, paginationLimit);
            }
            for (String username : filteredUsernames) {
                User user = getUser(getUserIDFromUserName(username), username);
                user.setUserStoreDomain(UserCoreUtil.extractDomainFromName(username));
                filteredUsers.add(user);
            }
        } else {
            /* When the filters has only the non-identity claims or if Identity claims are persisted in User store
            based Identity Data store.*/
            filteredUsers = getFilteredUsers(duplicateCondition, profileName, limit, offset, sortBy, sortOrder,
                    secondaryUserStoreManager);
        }

        handlePostGetUserListWithID(condition, domain, profileName, limit, offset, sortBy, sortOrder, filteredUsers,
                false);

        if (log.isDebugEnabled()) {
            log.debug("post listener get conditional  user list for domain: " + domain);
        }
        return filteredUsers;
    }

    @Override
    public PaginatedUserResponse getPaginatedUserListWithID(Condition condition, String domain, String profileName,
                                                            int limit, int offset, String sortBy, String sortOrder)
            throws UserStoreException {

        PaginatedUserResponse paginatedUserResponse = new PaginatedUserResponse();
        validateCondition(condition);
        if (StringUtils.isNotEmpty(sortBy) && StringUtils.isNotEmpty(sortOrder)) {
            throw new UserStoreException("Sorting is not supported.");
        }

        if (StringUtils.isEmpty(domain)) {
            domain = UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME;
        }

        if (StringUtils.isEmpty(profileName)) {
            profileName = UserCoreConstants.DEFAULT_PROFILE;
        }

        handlePreGetUserListWithID(condition, domain, profileName, limit, offset, sortBy, sortOrder);
        if (log.isDebugEnabled()) {
            log.debug("Pre listener get conditional  user list for domain: " + domain);
        }

        UserStoreManager secondaryUserStoreManager = getSecondaryUserStoreManager(domain);
        List<ExpressionCondition> expressionConditions = new ArrayList<>();

        /* Duplicating condition object to ensure that nullifying the conditions in the flow does not affect the
        validation in the next iteration of flow when the domain name is not in query params.*/
        Condition duplicateCondition = getDuplicateCondition(condition);
        getExpressionConditions(duplicateCondition, expressionConditions);
        mapAttributesToLocalIdentityClaims(expressionConditions);

        /* *****************************************************
         * Logic to Filter Users Based on Identity & Non Identity Claims
         ***************************************************** */

        int identityClaimFilterCount = countIdentityClaims(expressionConditions);

        /*
         * Case 1: Only Non Identity Claims.
         * If there are only non identity claims, filter users from the user store.
         */
        if (identityClaimFilterCount == 0) {
            paginatedUserResponse = new PaginatedUserResponse(getFilteredUsers(duplicateCondition, profileName, limit,
                    offset, sortBy, sortOrder, secondaryUserStoreManager));
        }

        /*
         * Case 2: Both Identity and Non Identity claims exist.
         * If identity claims are stored in user store, filter users from the user store.
         * If identity claims are stored in identity store,
         */
        if (identityClaimFilterCount > 0) {
            boolean nonIdentityClaimExist = expressionConditions.size() > identityClaimFilterCount;
            List<String> identityClaimFilteredUserNames = new ArrayList<>();
            // Call the listeners to get the filtered users from relevant identity store.
            if (secondaryUserStoreManager != null) {
                handlePreGetUserListWithIdentityClaims(condition, domain, profileName, limit, offset, sortBy,
                        sortOrder, secondaryUserStoreManager, identityClaimFilteredUserNames,
                        nonIdentityClaimExist);
            }

            /* If identity claims are stored in the identity store, all the identity claim filters should be removed
               from the filter condition. */
            expressionConditions = new ArrayList<>();
            getExpressionConditions(duplicateCondition, expressionConditions);
            if (countIdentityClaims(expressionConditions) > 0) {
                /*
                 * Case 2.1: Identity and Non Identity claims stored in user store.
                 * Both identity & non identity claim filters can be applied for the user store.
                 */
                updateCondition(duplicateCondition, domain);
                paginatedUserResponse = new PaginatedUserResponse(getFilteredUsers(duplicateCondition, profileName,
                        limit, offset, sortBy, sortOrder, secondaryUserStoreManager));
            } else {
                if (!nonIdentityClaimExist) {
                    /*
                     * Case 2.2: Only Identity claims exist and stored in the identity store.
                     */
                    paginatedUserResponse =
                            new PaginatedUserResponse(getUserListByUsernames(identityClaimFilteredUserNames));
                } else {
                    /*
                     * Case 2.3: Identity claims stored in identity store and non identity claims stored in user store.
                     */
                    if (secondaryUserStoreManager == null) {
                        return paginatedUserResponse;
                    }
                    List<String> nonIdentityClaimFilteredUsers = getFilteredUsernames(condition, profileName,
                            Integer.MAX_VALUE, 0, sortBy, sortOrder, secondaryUserStoreManager);

                    // Consider max user list configured in the user store.
                    int maxUserListCount = limit;
                    String maxUserListValue = secondaryUserStoreManager.getRealmConfiguration()
                            .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_MAX_USER_LIST);
                    if (NumberUtils.isNumber(maxUserListValue) && limit > Integer.parseInt(maxUserListValue)) {
                        maxUserListCount = Integer.parseInt(maxUserListValue);
                    }
                    paginatedUserResponse = mergeIdentityNonIdentityClaimFilteredUsers(identityClaimFilteredUserNames,
                            nonIdentityClaimFilteredUsers, offset, maxUserListCount);
                }
            }
        }

        handlePostGetUserListWithID(condition, domain, profileName, limit, offset, sortBy, sortOrder,
                paginatedUserResponse.getFilteredUsers(), false);
        if (log.isDebugEnabled()) {
            log.debug("post listener get conditional  user list for domain: " + domain);
        }
        return paginatedUserResponse;
    }

    /**
     * Retrieves the count of users based on the specified filtering conditions.
     *
     * @param condition   The filtering condition to be applied.
     * @param domain      The domain in which to search for users. Defaults to the primary domain if not provided.
     * @param profileName The profile name. Defaults to the default profile if not provided.
     * @param limit       The maximum number of users to retrieve.
     * @param offset      The starting index of the count.
     * @return The count of users that match the filtering conditions.
     * @throws UserStoreException If an error occurs while accessing the user store or if an unsupported operation is encountered.
     */
    public int getUsersCount(Condition condition, String domain, String profileName, int limit, int offset,
                             boolean isRemoveDuplicateUsersEnabled) throws UserStoreException {

        validateCondition(condition);

        if (StringUtils.isEmpty(domain)) {
            domain = UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME;
        }

        if (StringUtils.isEmpty(profileName)) {
            profileName = UserCoreConstants.DEFAULT_PROFILE;
        }

        UserStoreManager secondaryUserStoreManager = getSecondaryUserStoreManager(domain);
        List<String> identityClaimFilteredUserNames = new ArrayList<>();
        List<String> filteredUsernames;
        boolean hasNonIdentityClaimFilterConditions = false;
        boolean isIdentityClaimsInIdentityStore = false;
        boolean isIdentityClaimFilterExistInPostCondition;
        List<ExpressionCondition> expressionConditions = new ArrayList<>();

        /* Duplicating condition object to ensure that nullifying the conditions in the flow does not affect the
        validation in the next iteration of flow when the domain name is not in query params.*/
        Condition duplicateCondition = getDuplicateCondition(condition);
        getExpressionConditions(duplicateCondition, expressionConditions);

        // Check whether the request has IdentityClaims in filters.
        mapAttributesToLocalIdentityClaims(expressionConditions);
        boolean identityClaimsExistsInInitialCondition = countIdentityClaims(expressionConditions) > 0;

        if (identityClaimsExistsInInitialCondition) {
            if (expressionConditions.size() != countIdentityClaims(expressionConditions)) {
                hasNonIdentityClaimFilterConditions = true;
            }

            // Call the listeners to get the filtered users from relevant identity store.
            if (secondaryUserStoreManager != null) {
                handlePreGetUserListWithIdentityClaims(duplicateCondition, domain, profileName, limit, offset, null,
                        null, secondaryUserStoreManager, identityClaimFilteredUserNames,
                        hasNonIdentityClaimFilterConditions);
            }

            // Check whether the request has IdentityClaims in filters after trying to filter at Identity Data Store.
            expressionConditions = new ArrayList<>();
            getExpressionConditions(duplicateCondition, expressionConditions);
            isIdentityClaimFilterExistInPostCondition = containsIdentityClaims(expressionConditions);

            // Means the identity claims are identity store based. (Else it is user store based)
            if (!isIdentityClaimFilterExistInPostCondition) {
                isIdentityClaimsInIdentityStore = true;
            } else {
                updateCondition(duplicateCondition, domain);
            }

            /* If identity claims are in JDBCIdentityDataStore, and filtering in JDBCIdentityDataStore returned an empty
         list, we can skip filtering in user store.*/
            if (isIdentityClaimsInIdentityStore && identityClaimFilteredUserNames.isEmpty()) {
                return 0;
            }

            // After filtering based on identity claims, if there are no other filters can return the list.
            if (expressionConditions.isEmpty()) {
                return identityClaimFilteredUserNames.size();
            }
        }

        /* When the filters have non-identity claims or if Identity claims are persisted in User store based
        Identity Data store.*/
        filteredUsernames = getFilteredUsernames(duplicateCondition, profileName, limit, offset, null, null,
                secondaryUserStoreManager);

        if (isRemoveDuplicateUsersEnabled) {
            filteredUsernames = new ArrayList<>(new TreeSet<>(filteredUsernames));
        }

        if (identityClaimsExistsInInitialCondition && isIdentityClaimsInIdentityStore) {
            // The identity claims are not user store based.
            filteredUsernames.retainAll(identityClaimFilteredUserNames);
        }

        return filteredUsernames.size();
    }

    private boolean isExactSameFilteredUsernames(List<String> tempFilteredUsernames,
                                                 Set<String> prevIterationFilteredUsernames) {

        return CollectionUtils.isEqualCollection(tempFilteredUsernames, prevIterationFilteredUsernames);
    }

    private List<User> getFilteredUsers(Condition condition, String profileName, int limit, int offset, String sortBy,
                                        String sortOrder, UserStoreManager secManager) throws UserStoreException {

        List<User> filteredUsers = new ArrayList<>();
        if (secManager != null) {
            if (secManager instanceof AbstractUserStoreManager) {
                if (isUniqueUserIdEnabled(secManager)) {
                    UniqueIDPaginatedSearchResult users = ((AbstractUserStoreManager) secManager)
                            .doGetUserListWithID(condition, profileName, limit, offset, sortBy, sortOrder);
                    addUsersToUserIdCache(users.getUsers());
                    addUsersToUserNameCache(users.getUsers());
                    filteredUsers = users.getUsers();
                } else {
                    PaginatedSearchResult users = ((AbstractUserStoreManager) secManager)
                            .doGetUserList(condition, profileName, limit, offset, sortBy, sortOrder);
                    filteredUsers = userUniqueIDManger.listUsers(users.getUsers(), this);
                }
            }
        }
        return filteredUsers;
    }

    private List<String> getFilteredUsernames(Condition condition, String profileName, int limit, int offset,
                                              String sortBy, String sortOrder, UserStoreManager secManager)
            throws UserStoreException {

        List<String> filteredUsernames = new ArrayList<>();
        if (secManager != null) {
            if (secManager instanceof AbstractUserStoreManager) {
                if (isUniqueUserIdEnabled(secManager)) {
                    UniqueIDPaginatedUsernameSearchResult users = ((AbstractUserStoreManager) secManager)
                            .doGetUsernameListWithID(condition, profileName, limit, offset, sortBy, sortOrder);
                    filteredUsernames = users.getUsers();
                } else {
                    PaginatedSearchResult users = ((AbstractUserStoreManager) secManager)
                            .doGetUserList(condition, profileName, limit, offset, sortBy, sortOrder);
                    filteredUsernames = Arrays.asList(users.getUsers());
                }
            }
        }
        return filteredUsernames;
    }

    /**
     * Add userID and username to UserID cache
     *
     * @param userID          User id.
     * @param userName        Username.
     * @param userStoreDomain User store Domain.
     */
    protected void addToUserIDCache(String userID, String userName, String userStoreDomain) {

        UserIdResolverCache.getInstance()
                .addToCache(UserCoreUtil.addDomainToName(userName, userStoreDomain), userID,
                        RESOLVE_USER_ID_FROM_USER_NAME_CACHE_NAME, tenantId);
    }

    /**
     * Add userID and username to Username cache
     *
     * @param userID          User id.
     * @param userName        Username.
     * @param userStoreDomain User store Domain.
     */
    protected void addToUserNameCache(String userID, String userName, String userStoreDomain) {

        UserIdResolverCache.getInstance()
                .addToCache(userID, UserCoreUtil.addDomainToName(userName, userStoreDomain),
                        RESOLVE_USER_NAME_FROM_USER_ID_CACHE_NAME, tenantId);
    }

    /**
     * Get duplicate condition object.
     *
     * @param condition Condition.
     * @throws UserStoreException User store exception.
     */
    private Condition getDuplicateCondition(Condition condition) {

        Condition duplicateCondition;
        if (condition instanceof ExpressionCondition) {
            duplicateCondition = new ExpressionCondition(condition.getOperation(),
                    ((ExpressionCondition) condition).getAttributeName(),
                    ((ExpressionCondition) condition).getAttributeValue());
        } else if (condition instanceof OperationalCondition) {
            duplicateCondition = new OperationalCondition(condition.getOperation(),
                    ((OperationalCondition) condition).getLeftCondition(),
                    ((OperationalCondition) condition).getRightCondition());
        } else {
            /* Have not duplicated the remaining condition objects.
             If it is essential to duplicate, handle it with an if-else.*/
            duplicateCondition = condition;
            if (log.isDebugEnabled()) {
                log.debug(" Condition object is not duplicated. This might end up in failures when domain names are " +
                        "not provided in the request as the flows nullify the conditions in due process.");
            }
        }
        return duplicateCondition;
    }

    /**
     * Pre listener for getting user list with identity claim filters.
     *
     * @param condition                  Condition.
     * @param domain                     Domain name.
     * @param limit                      Limit for the result.
     * @param offset                     Off set for the result.
     * @param secManager                 Secondary user store manager.
     * @param hasNonIdentityClaimFilters Count of non-identity claim filters.
     * @throws UserStoreException User store exception.
     */
    private void handlePreGetUserListWithIdentityClaims(Condition condition, String domain, String profileName,
                                                        int limit, int offset, String sortBy, String sortOrder,
                                                        UserStoreManager secManager,
                                                        List<String> identityClaimFilteredUserNames,
                                                        boolean hasNonIdentityClaimFilters) throws UserStoreException {

        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent
                    .getUserOperationEventListeners()) {
                if (listener instanceof AbstractUserOperationEventListener) {
                    AbstractUserOperationEventListener newListener =
                            (AbstractUserOperationEventListener) listener;

                    if (!hasNonIdentityClaimFilters) {
                        // In this case, can filter with Identity claim filters and paginate at DB level.
                        if (!newListener.doPreGetPaginatedUserList(condition, identityClaimFilteredUserNames,
                                domain, secManager, limit, offset)) {
                            handleGetUserListFailure(
                                    ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_PAGINATED_USER_LIST.getCode(),
                                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_PAGINATED_USER_LIST
                                                    .getMessage(),
                                            UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE),
                                    condition, domain, profileName, limit, offset, sortBy, sortOrder);
                            break;
                        }
                    } else {
                        // In this case, can filter with Identity claim filters and fetch whole filtered list.
                        if (!newListener.doPreGetUserList(condition, identityClaimFilteredUserNames,
                                secManager, domain)) {
                            handleGetUserListFailure(
                                    ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET__CONDITIONAL_USER_LIST.getCode(),
                                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET__CONDITIONAL_USER_LIST
                                                    .getMessage(),
                                            UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE),
                                    condition, domain, profileName, limit, offset, sortBy, sortOrder);
                            break;
                        }
                    }
                }
            }
        } catch (UserStoreException ex) {
            throw new UserStoreException("Error occurred while retrieving users for Identity Claim Filters " +
                    "with pagination parameters.", ex);
        }
    }

    /**
     * If the condition contains expressions with identity claims, the claims are in the form of local claim dialect
     * URIs. Hence, they are converted to the mapped attribute.
     *
     * @param condition Condition.
     * @param domain    Domain name.
     * @throws UserStoreException
     */
    private void updateCondition(Condition condition, String domain) throws UserStoreException {

        if (condition instanceof ExpressionCondition) {
            ExpressionCondition expressionCondition = (ExpressionCondition) condition;
            if (expressionCondition.getAttributeName().
                    contains(IDENTITY_CLAIM_URI)) {
                String claimUri = expressionCondition.getAttributeName();
                try {
                    ClaimMapping mapping = (ClaimMapping) claimManager.getClaimMapping(claimUri);
                    String attribute = mapping.getMappedAttribute(domain);
                    expressionCondition.setAttributeName(attribute);
                } catch (org.wso2.carbon.user.api.UserStoreException e) {
                    throw new UserStoreException(e);
                }
            }
        } else if (condition instanceof OperationalCondition) {
            Condition leftCondition = ((OperationalCondition) condition).getLeftCondition();
            updateCondition(leftCondition, domain);
            Condition rightCondition = ((OperationalCondition) condition).getRightCondition();
            updateCondition(rightCondition, domain);
        }
    }

    /**
     * Extract filter expressions form the condition as a list.
     *
     * @param condition            condition.
     * @param expressionConditions list of expression conditions.
     */
    private void getExpressionConditions(Condition condition, List<ExpressionCondition> expressionConditions) {

        if (condition instanceof ExpressionCondition) {
            ExpressionCondition expressionCondition = (ExpressionCondition) condition;
            if (isConditionExist(expressionCondition)) {
                expressionConditions.add(expressionCondition);
            }
        } else if (condition instanceof OperationalCondition) {
            Condition leftCondition = ((OperationalCondition) condition).getLeftCondition();
            getExpressionConditions(leftCondition, expressionConditions);
            Condition rightCondition = ((OperationalCondition) condition).getRightCondition();
            getExpressionConditions(rightCondition, expressionConditions);
        }
    }

    private boolean isConditionExist(ExpressionCondition expressionCondition) {

        if (StringUtils.isNotEmpty(expressionCondition.getAttributeName()) ||
                StringUtils.isNotEmpty(expressionCondition.getAttributeValue()) ||
                StringUtils.isNotEmpty(expressionCondition.getOperation())) {
            return true;
        }
        return false;
    }

    /**
     * Map to corresponding identity claim for the attributes of the given filter condition.
     *
     * @param expressionConditions List of expression conditions.
     * @throws UserStoreException
     */
    private void mapAttributesToLocalIdentityClaims(List<ExpressionCondition> expressionConditions)
            throws UserStoreException {

        List<org.wso2.carbon.user.api.ClaimMapping> claimMapping;
        try {
            claimMapping = Arrays.asList(claimManager.getAllClaimMappings());
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException("Error occurred while checking the existence of identity claim filters " +
                    "from the expression nodes.", e);
        }
        for (ExpressionCondition expressionCondition : expressionConditions) {
            org.wso2.carbon.user.api.ClaimMapping mappedClaim = claimMapping.stream()
                    .filter(mapping -> StringUtils.equals(mapping.getMappedAttribute(),
                            expressionCondition.getAttributeName()))
                    .findFirst().orElse(null);

            if (mappedClaim == null) {
                continue;
            }

            // Check if the claimURI are of type 'identity claims'.
            if (mappedClaim.getClaim().getClaimUri().contains(IDENTITY_CLAIM_URI)) {
                expressionCondition.setAttributeName(mappedClaim.getClaim().getClaimUri());
                if (log.isDebugEnabled()) {
                    log.debug("Obtained the ClaimURI " + mappedClaim.getClaim().getClaimUri() +
                            " from the map for the attribute : " + expressionCondition.getAttributeName());
                }
            }
        }
    }

    /**
     * Check if the expression list contains identity claims.
     *
     * @param expressionConditions list of expression conditions.
     * @return true if contains identity claims, false otherwise.
     */
    private boolean containsIdentityClaims(List<ExpressionCondition> expressionConditions) {

        for (ExpressionCondition expressionCondition : expressionConditions) {
            if (expressionCondition.getAttributeName() != null &&
                    expressionCondition.getAttributeName().contains(IDENTITY_CLAIM_URI)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Count the no. of identity claims.
     *
     * @param expressionConditions List of expression conditions.
     * @return true if contains identity claims, false otherwise.
     */
    private int countIdentityClaims(List<ExpressionCondition> expressionConditions) {

        int identityClaimsCount = 0;
        for (ExpressionCondition expressionCondition : expressionConditions) {
            if (expressionCondition.getAttributeName() != null &&
                    expressionCondition.getAttributeName().contains(IDENTITY_CLAIM_URI)) {
                identityClaimsCount += 1;
            }
        }
        return identityClaimsCount;
    }

    private List<User> doGetUserListWithID(String claim, String claimValue, String profileName, int limit, int offset,
            String extractedDomain, UserStoreManager userManager) throws UserStoreException {

        String property;

        // If domain is present, then we search within that domain only.
        if (StringUtils.isNotEmpty(extractedDomain)) {

            if (userManager == null) {
                if (log.isDebugEnabled()) {
                    log.debug("No user store manager found for domain: " + extractedDomain);
                }
                return Collections.emptyList();
            }

            if (log.isDebugEnabled()) {
                log.debug("Domain found in claim value. Searching only in the " + extractedDomain + " for possible "
                        + "matches");
            }

            try {
                property = claimManager.getAttributeName(extractedDomain, claim);
            } catch (org.wso2.carbon.user.api.UserStoreException e) {
                handleGetUserListFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST.getMessage(),
                                e.getMessage()), claim, claimValue, limit, offset, profileName);
                throw new UserStoreException(
                        "Error occurred while retrieving attribute name for domain : " + extractedDomain + " and claim "
                                + claim, e);
            }
            if (property == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Could not find matching property for\n" + "claim :" + claim + "domain :"
                            + extractedDomain);
                }
                return Collections.emptyList();
            }

            if (userManager instanceof AbstractUserStoreManager) {
                // Get the user list and return with domain appended.
                try {
                    AbstractUserStoreManager userStoreManager = (AbstractUserStoreManager) userManager;
                    UniqueIDPaginatedSearchResult result = userStoreManager
                            .doGetUserListFromPropertiesWithID(property, claimValue, profileName, limit, offset);
                    if (log.isDebugEnabled()) {
                        log.debug("List of filtered paginated users for: " + extractedDomain + " : " + Arrays
                                .asList(result.getUsers()));
                    }
                    return result.getUsers();
                } catch (UserStoreException ex) {
                    handleGetUserListFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_USER_LIST.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_USER_LIST.getMessage(),
                                    ex.getMessage()), claim, claimValue, limit, offset, profileName);
                    throw ex;
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "getUserListFromProperties is not supported by this user store: " + userManager.getClass());
                }
                return Collections.emptyList();
            }
        }

        // If domain is not given then search all the user stores.
        if (log.isDebugEnabled()) {
            log.debug("No domain name found in claim value. Searching through all user stores for possible matches");
        }

        List<User> usersFromAllStoresList = new ArrayList<>();
        List<UserStoreManager> userStoreManagers = getUserStoreMangers();
        int nonPaginatedUserCount = 0;

        // Iterate through all of available user store managers.
        for (UserStoreManager userStoreManager : userStoreManagers) {

            // If this is not an instance of Abstract User Store Manger we can ignore the flow since we can't get the
            // domain name.
            if (!(userStoreManager instanceof AbstractUserStoreManager)) {
                continue;
            }

            if (limit <= 0) {
                return usersFromAllStoresList;
            }

            // For all the user stores append the domain name to the claim and pass it recursively (Including PRIMARY).
            String domainName = ((AbstractUserStoreManager) userStoreManager).getMyDomainName();

            try {
                property = claimManager.getAttributeName(domainName, claim);
            } catch (org.wso2.carbon.user.api.UserStoreException e) {
                handleGetUserListFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST.getMessage(),
                                e.getMessage()), claim, claimValue, limit, offset, profileName);
                throw new UserStoreException(
                        "Error occurred while retrieving attribute name for domain : " + extractedDomain + " and claim "
                                + claim, e);
            }

            // Recursively call the getUserList method appending the domain to claim value.
            UniqueIDPaginatedSearchResult userList = doGetUserListFromPropertiesWithID(property, claimValue,
                    profileName, limit, offset);
            if (log.isDebugEnabled()) {
                log.debug("Secondary user list for domain: " + domainName + " : " + userList);
            }
            limit = limit - userList.getUsers().size();
            nonPaginatedUserCount = userList.getSkippedUserCount();

            if (userList.getUsers().size() > 0) {
                offset = 1;
            } else {
                offset = offset - nonPaginatedUserCount;
            }

            usersFromAllStoresList.addAll(userList.getUsers());
        }

        // Done with all user store processing. Return the user array if not empty.
        return usersFromAllStoresList;
    }

    @Override
    public List<UniqueIDUserClaimSearchEntry> getUsersClaimValuesWithID(List<String> userIDs, List<String> claims,
            String profileName) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class[] argTypes = new Class[] { List.class, List.class, String.class };
            Object object = callSecure("getUsersClaimValuesWithID", new Object[] { userIDs, claims, profileName },
                    argTypes);
            return (List<UniqueIDUserClaimSearchEntry>) object;
        }

        if (StringUtils.isEmpty(profileName)) {
            profileName = UserCoreConstants.DEFAULT_PROFILE;
        }

        List<UniqueIDUserClaimSearchEntry> allUsers = new ArrayList<>();
        Map<String, List<String>> domainFreeUsers = getDomainFreeUsersWithID(userIDs);

        for (Map.Entry<String, List<String>> entry : domainFreeUsers.entrySet()) {
            UserStoreManager secondaryUserStoreManager = getSecondaryUserStoreManager(entry.getKey());
            if (secondaryUserStoreManager instanceof AbstractUserStoreManager) {
                if (isUniqueUserIdEnabled(secondaryUserStoreManager)) {
                    List<UniqueIDUserClaimSearchEntry> users = ((AbstractUserStoreManager) secondaryUserStoreManager)
                            .doGetUsersClaimValuesWithID(entry.getValue(), claims, entry.getKey(), profileName);
                    allUsers.addAll(users);
                } else {
                    List<String> userNamesFromUserIDs = getUserNamesFromUserIDs(entry.getValue())
                            .stream().map(UserCoreUtil::removeDomainFromName)
                            .collect(Collectors.toList());
                    UserClaimSearchEntry[] users = ((AbstractUserStoreManager) secondaryUserStoreManager)
                            .doGetUsersClaimValues(userNamesFromUserIDs,
                                    claims.toArray(new String[0]), entry.getKey(), profileName);
                    List<UniqueIDUserClaimSearchEntry> uniqueIDUserClaimSearchEntries =
                            getUniqueIDUserClaimSearchEntries(users);
                    allUsers.addAll(uniqueIDUserClaimSearchEntries);
                }
            }
        }

        for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
            if (listener instanceof AbstractUserOperationEventListener) {
                AbstractUserOperationEventListener newListener = (AbstractUserOperationEventListener) listener;
                if (!newListener.doPostGetUsersClaimValuesWithID(userIDs, claims, profileName, allUsers, this)) {
                    break;
                }
            }
        }

        return allUsers;
    }

    public List<UniqueIDUserClaimSearchEntry> getUniqueIDUserClaimSearchEntries(UserClaimSearchEntry[] users)
            throws UserStoreException {

        List<UniqueIDUserClaimSearchEntry> uniqueIDUserClaimSearchEntries = new ArrayList<>();
        for (UserClaimSearchEntry userClaimSearchEntry : users) {
            UniqueIDUserClaimSearchEntry uniqueIDUserClaimSearchEntry = new UniqueIDUserClaimSearchEntry();
            String userName = userClaimSearchEntry.getUserName();
            String userID = getUserIDFromUserName(userName);
            User user = getUser(userID, userName);
            uniqueIDUserClaimSearchEntry.setUser(user);
            uniqueIDUserClaimSearchEntry.setClaims(userClaimSearchEntry.getClaims());
            uniqueIDUserClaimSearchEntry.setUserClaimSearchEntry(userClaimSearchEntry);
            uniqueIDUserClaimSearchEntries.add(uniqueIDUserClaimSearchEntry);
        }
        return uniqueIDUserClaimSearchEntries;
    }

    public List<UniqueIDUserClaimSearchEntry> doGetUsersClaimValuesWithID(List<String> userIDs, List<String> claims,
            String domainName, String profileName) throws UserStoreException {

        Set<String> propertySet = new HashSet<>();
        Map<String, String> claimToAttributeMap = new HashMap<>();
        List<UniqueIDUserClaimSearchEntry> userClaimSearchEntryList = new ArrayList<>();
        for (String claim : claims) {
            String property;
            try {
                property = getClaimAtrribute(claim, null, domainName);
            } catch (org.wso2.carbon.user.api.UserStoreException e) {
                throw new UserStoreException(e);
            }
            propertySet.add(property);
            claimToAttributeMap.put(claim, property);
        }

        List<String> properties = new ArrayList<>(propertySet);

        List<String> roleAndGroupProperties = null;
        if (isGroupsVsRolesSeparationImprovementsEnabled(realmConfig)) {
            roleAndGroupProperties = getRolesAndGroupsClaimURIs().stream().map(claimToAttributeMap::get)
                    .filter(StringUtils::isNotBlank).collect(Collectors.toList());
            properties.removeAll(roleAndGroupProperties);
        }

        Map<String, Map<String, String>> userProperties = this
                .getUsersPropertyValuesWithID(userIDs, properties.toArray(new String[]{}), profileName);

        if (isGroupsVsRolesSeparationImprovementsEnabled(realmConfig)) {
            // Inject group and roles attributes.
            if (CollectionUtils.isNotEmpty(roleAndGroupProperties)) {
                for (Map.Entry<String, Map<String, String>> userEntry : userProperties.entrySet()) {
                    populateRoleGroupAttributes(claims, claimToAttributeMap, userEntry,
                            getRoleListOfUserWithID(userEntry.getKey()), ROLE_CLAIM);
                    populateRoleGroupAttributes(claims, claimToAttributeMap, userEntry,
                            doGetInternalRoleListOfUserWithID(userEntry.getKey(), "*"), INTERNAL_ROLES_CLAIM);
                    populateRoleGroupAttributes(claims, claimToAttributeMap, userEntry, Arrays.asList(
                            doGetExternalRoleListOfUserWithID(userEntry.getKey(), "*")), USER_STORE_GROUPS_CLAIM);
                }
            }
        }


        for (Map.Entry<String, Map<String, String>> entry : userProperties.entrySet()) {
            UniqueIDUserClaimSearchEntry uniqueIDUserClaimSearchEntry = new UniqueIDUserClaimSearchEntry();
            UserClaimSearchEntry userClaimSearchEntry = new UserClaimSearchEntry();
            String userID = entry.getKey();
            String userName = doGetUserNameFromUserID(userID);
            User user = getUser(userID, userName);
            uniqueIDUserClaimSearchEntry.setUser(user);
            userClaimSearchEntry.setUserName(user.getDomainQualifiedUsername());
            Map<String, String> userClaims = new HashMap<>();

            for (String claim : claims) {
                for (Map.Entry<String, String> userAttribute : entry.getValue().entrySet()) {
                    String value;
                    if (claimToAttributeMap.get(claim) != null && claimToAttributeMap.get(claim)
                            .equals(userAttribute.getKey()) && StringUtils.isNotBlank(value = userAttribute.getValue())
                    ) {
                        userClaims.put(claim, value);
                    }
                }
            }
            uniqueIDUserClaimSearchEntry.setClaims(userClaims);
            userClaimSearchEntry.setClaims(userClaims);
            uniqueIDUserClaimSearchEntry.setUserClaimSearchEntry(userClaimSearchEntry);
            userClaimSearchEntryList.add(uniqueIDUserClaimSearchEntry);
        }
        return userClaimSearchEntryList;
    }

    private void populateRoleGroupAttributes(List<String> claims, Map<String, String> claimToAttributeMap,
                                             Map.Entry<String, Map<String, String>> userEntry,
                                             List<String> roleGroupList, String roleGroupClaimURI) {

        if (claims.contains(roleGroupClaimURI)) {
            if (CollectionUtils.isNotEmpty(roleGroupList)) {
                userEntry.getValue().put(claimToAttributeMap.get(roleGroupClaimURI),
                        getMultiValuedString(roleGroupList));
            }
        }
    }

    private Map<String, List<String>> getDomainFreeUsersWithID(List<String> userIDs) throws UserStoreException {

        Map<String, List<String>> domainAwareUsers = new HashMap<>();
        if (!userIDs.isEmpty()) {
            for (String userID : userIDs) {
                if (StringUtils.isBlank(userID)) {
                    continue;
                }
                try {
                    User user = getUser(userID, null);
                    String domainName = user.getUserStoreDomain();
                    List<String> users = domainAwareUsers.get(domainName);
                    if (users == null) {
                        users = new ArrayList<>();
                        domainAwareUsers.put(domainName.toUpperCase(), users);
                    }
                    users.add(UserCoreUtil.removeDomainFromName(userID));
                } catch (UserStoreClientException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Could not resolve the user for user id: " + userID);
                    }
                }
            }
        }

        return domainAwareUsers;
    }

    protected Map<String, Map<String, String>> getUsersPropertyValuesWithID(List<String> userIDs,
            String[] propertyNames, String profileName) throws UserStoreException {

        Map<String, Map<String, String>> usersPropertyValuesMap = new HashMap<>();
        for (String userID : userIDs) {
            Map<String, String> propertyValuesMap = getUserPropertyValuesWithID(userID, propertyNames, profileName);
            processAttributesAfterRetrievalWithID(userID, propertyValuesMap, profileName);
            if (propertyValuesMap != null && !propertyValuesMap.isEmpty()) {
                usersPropertyValuesMap.put(userID, propertyValuesMap);
            }
        }
        return usersPropertyValuesMap;
    }

    @Override
    public Map<String, List<String>> getRoleListOfUsersWithID(List<String> userIDs) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class[] argTypes = new Class[] { List.class };
            Object object = callSecure("getRoleListOfUsersWithID", new Object[] { userIDs }, argTypes);
            return (Map<String, List<String>>) object;
        }

        Map<String, List<String>> allRoleNames = new HashMap<>();
        Map<String, List<String>> domainFreeUsers = getDomainFreeUsersWithID(userIDs);

        for (Map.Entry<String, List<String>> entry : domainFreeUsers.entrySet()) {
            UserStoreManager secondaryUserStoreManager = getSecondaryUserStoreManager(entry.getKey());
            if (secondaryUserStoreManager instanceof AbstractUserStoreManager) {
                if (((AbstractUserStoreManager) secondaryUserStoreManager).isUniqueUserIdEnabled()) {
                    Map<String, List<String>> roleNames = ((AbstractUserStoreManager) secondaryUserStoreManager)
                            .doGetRoleListOfUsersWithID(entry.getValue(), entry.getKey());
                    allRoleNames.putAll(roleNames);
                } else {
                    List<User> users = userUniqueIDManger
                            .getUsers(entry.getValue(), (AbstractUserStoreManager) secondaryUserStoreManager);
                    Map<String, List<String>> userRoles = ((AbstractUserStoreManager) secondaryUserStoreManager)
                            .doGetRoleListOfUsers(users.stream().map(User::getUsername).collect(Collectors.toList()),
                                    entry.getKey());

                    for (User user : users) {
                        allRoleNames.put(user.getUserID(), userRoles.get(user.getUsername()));
                    }
                }
            }
        }

        for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
            if (listener instanceof AbstractUserOperationEventListener) {
                AbstractUserOperationEventListener newListener = (AbstractUserOperationEventListener) listener;
                if (!newListener.doPostGetRoleListOfUsersWithID(userIDs, allRoleNames, this)) {
                    break;
                }
            }
        }

        return allRoleNames;
    }

    public Map<String, List<String>> doGetRoleListOfUsersWithID(List<String> userIDs, String domainName)
            throws UserStoreException {

        if (!isSecureCall.get()) {
            Class[] argTypes = new Class[] { List.class, String.class };
            Object object = callSecure("doGetRoleListOfUsersWithID", new Object[] { userIDs, domainName }, argTypes);
            return (Map<String, List<String>>) object;
        }

        Map<String, List<String>> internalRoles = doGetInternalRoleListOfUsersWithID(userIDs, domainName);

        Map<String, List<String>> externalRoles = new HashMap<>();
        if (readGroupsEnabled) {
            externalRoles = doGetExternalRoleListOfUsersWithID(userIDs);
        }

        Map<String, List<String>> combinedRoles = new HashMap<>();
        if (!internalRoles.isEmpty() && !externalRoles.isEmpty()) {
            for (String userID : userIDs) {
                List<String> roles = new ArrayList<>();
                if (internalRoles.get(userID) != null) {
                    roles.addAll(internalRoles.get(userID));
                }
                if (externalRoles.get(userID) != null) {
                    List<String> domainQualifiedRoleNames = getNamesWithDomain(externalRoles.get(userID), domainName);
                    roles.addAll(domainQualifiedRoleNames);
                }
                if (!roles.isEmpty()) {
                    combinedRoles.put(userID, roles);
                }
            }
        } else if (!internalRoles.isEmpty()) {
            combinedRoles = internalRoles;
        } else if (!externalRoles.isEmpty()) {
            combinedRoles = externalRoles;
        }

        return combinedRoles;
    }

    @Override
    public List<Group> listGroups(Condition condition, String domain, int limit, int offset, String sortBy,
                                  String sortOrder) throws UserStoreException {

        validateCondition(condition);
        if (StringUtils.isNotBlank(sortBy) && StringUtils.isNotBlank(sortOrder)) {
            throw new UserStoreException(ERROR_SORTING_NOT_SUPPORTED.getMessage(),
                    ERROR_SORTING_NOT_SUPPORTED.getCode());
        }
        if (StringUtils.isBlank(domain)) {
            if (log.isDebugEnabled()) {
                log.debug("domain parameter is empty. Setting 'Primary' as the default domain.");
            }
            domain = UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME;
        }
        UserStoreManager userManager = this;
        if (!StringUtils.equalsIgnoreCase(getMyDomainName(), domain)) {
            userManager = getSecondaryUserStoreManager(domain);
        }
        // #################### Invoking pre-listeners ####################
        try {
            for (GroupOperationEventListener listener : UMListenerServiceComponent
                    .getGroupOperationEventListeners()) {
                if (listener instanceof AbstractGroupOperationEventListener) {
                    AbstractGroupOperationEventListener newListener = (AbstractGroupOperationEventListener) listener;
                    if (!newListener.preListGroups(condition, limit, offset, domain, sortBy, sortOrder, userManager)) {
                        handlePreListGroupsFailure(ERROR_DURING_PRE_GET_GROUP.getCode(),
                                String.format(ERROR_DURING_PRE_GET_GROUP.getMessage(),
                                        UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), condition, limit,
                                offset, domain, sortBy, sortOrder, userManager);
                        break;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleListGroupsFailure(ERROR_DURING_PRE_GET_GROUP.getCode(),
                    String.format(ERROR_DURING_PRE_GET_GROUP.getMessage(), ex.getMessage()), condition, limit, offset,
                    domain, sortBy, sortOrder, userManager);
            throw ex;
        }
        // #################### Invoke userstore methods ####################
        List<Group> groupsList = new ArrayList<>();
        if (isUniqueGroupIdEnabled(userManager)) {
            groupsList = ((AbstractUserStoreManager)userManager).doListGroups(condition, limit, offset,
                    sortBy, sortOrder);
        } else {
            // Invoking group resolver to get data from the legacy approach.
            GroupResolver groupResolver = UserStoreMgtDataHolder.getInstance().getGroupResolver();
            if (groupResolver != null && groupResolver.isEnable()) {
                groupResolver.listGroups(condition, limit, offset, domain, sortBy, sortOrder, groupsList, userManager);
            }
        }

        // #################### Invoking post-listeners ####################
        try {
            for (GroupOperationEventListener listener : UMListenerServiceComponent
                    .getGroupOperationEventListeners()) {
                if (listener instanceof AbstractGroupOperationEventListener) {
                    AbstractGroupOperationEventListener newListener = (AbstractGroupOperationEventListener) listener;
                    if (!newListener.postListGroups(condition, limit, offset, domain, sortBy, sortOrder,
                            groupsList, userManager)) {
                        handlePostListGroupsFailure(ERROR_DURING_POST_GET_GROUP.getCode(),
                                String.format(ERROR_DURING_POST_GET_GROUP.getMessage(),
                                        UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), condition, limit,
                                offset, domain, sortBy, sortOrder, userManager);
                        break;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleListGroupsFailure(ERROR_DURING_POST_GET_GROUP.getCode(),
                    String.format(ERROR_DURING_POST_GET_GROUP.getMessage(), ex.getMessage()), condition, limit, offset,
                    domain, sortBy, sortOrder, userManager);
            throw ex;
        }
        return groupsList;
    }

    @Override
    public List<Group> listGroups(Condition condition, int limit, int offset, String sortBy,
                                  String sortOrder) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("listGroups operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException(
                "listGroups operation is not implemented in: " + this.getClass());
    }

    @Override
    public List<Group> listGroups(Condition condition, int limit, int offset) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("listGroups operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException(
                "listGroups operation is not implemented in: " + this.getClass());
    }

    @Override
    public List<Group> listGroups(Condition condition) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("listGroups operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException(
                "listGroups operation is not implemented in: " + this.getClass());
    }

    /**
     * Check whether the given group name contain system reserved domain name such as application or internal.
     *
     * @param groupName String group name.
     * @return True if the group name contain system reserved domain name, false otherwise.
     */
    private boolean isInvalidGroupDomain(String groupName) {

        return isAnInternalRole(groupName);
    }

    @Override
    public Group addGroup(String groupName, List<String> usersIds, List<org.wso2.carbon.user.core.common.Claim> claims)
            throws UserStoreException {

        if (StringUtils.isEmpty(groupName)) {
            String errorCode = ErrorMessages.ERROR_EMPTY_GROUP_NAME.getCode();
            String errorMessage = ErrorMessages.ERROR_EMPTY_GROUP_NAME.getMessage();
            handleAddGroupFailure(errorCode, errorMessage, groupName, null, usersIds, claims);
            throw new UserStoreClientException(errorMessage, errorCode);
        }
        if (isInvalidGroupDomain(groupName)) {
            String errorCode = ErrorMessages.ERROR_SYSTEM_RESERVED_DOMAIN_IN_GROUP.getCode();
            String errorMessage = String.format(ErrorMessages.ERROR_SYSTEM_RESERVED_DOMAIN_IN_GROUP.getMessage(),
                    groupName);
            handleAddGroupFailure(errorCode, errorMessage, groupName, null, usersIds, claims);
            throw new UserStoreClientException(errorMessage, errorCode);
        }
        if (usersIds == null) {
            usersIds = Collections.emptyList();
        }
        UserStore userStore = getUserStoreWithGroupName(groupName);
        if (userStore.isRecurssive()) {
            return ((UniqueIDUserStoreManager) userStore.getUserStoreManager())
                    .addGroup(UserCoreUtil.removeDomainFromName(groupName),
                            UserCoreUtil.removeDomainFromNames(usersIds), claims);
        }
        // #################### Domain Name Free Zone Starts Here ################################
        claims = CollectionUtils.isEmpty(claims) ? new ArrayList<>() : claims;
        // This happens only once during first startup - adding administrator user/group.
        if (groupName.indexOf(CarbonConstants.DOMAIN_SEPARATOR) > 0) {
            groupName = userStore.getDomainFreeName();
            usersIds = UserCoreUtil.removeDomainFromNames(usersIds);
        }

        // #################### <Pre-Listeners> #####################################################
        if (!handlePreAddGroup(groupName, usersIds, claims)) {
            return null;
        }
        // Invoke legacy listeners to maintain backward compatibility.
        if (!handlePreAddRoleWithID(groupName, usersIds.toArray(new String[0]), null, false)) {
            return null;
        }
        // #################### </Pre-Listeners> #####################################################

        // Check whether groups can be added.
        if (isReadOnly()) {
            String errorCode = ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode();
            String errorMessage = ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage();
            handleAddGroupFailure(errorCode, errorMessage, groupName, null, usersIds, claims);
            throw new UserStoreClientException(errorMessage, errorCode);
        }
        if (!writeGroupsEnabled) {
            String errorCode = ErrorMessages.ERROR_CODE_WRITE_GROUPS_NOT_ENABLED.getCode();
            String errorMessage = ErrorMessages.ERROR_CODE_WRITE_GROUPS_NOT_ENABLED.getMessage();
            handleAddGroupFailure(errorCode, errorMessage, groupName, null, usersIds, claims);
            throw new UserStoreClientException(errorMessage, errorCode);
        }
        // Perform validation on the name.
        if (!isGroupNameValid(groupName)) {
            String regEx = realmConfig
                    .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_ROLE_NAME_JAVA_REG_EX);
            String errorMessage = String
                    .format(ErrorMessages.ERROR_CODE_INVALID_GROUP_NAME.getMessage(), groupName, regEx);
            String errorCode = ErrorMessages.ERROR_CODE_INVALID_GROUP_NAME.getCode();
            handleAddGroupFailure(errorCode, errorMessage, groupName, null, usersIds, claims);
            throw new UserStoreClientException(errorMessage, errorCode);
        }
        if (doCheckExistingGroupName(groupName)) {
            String errorCode = ErrorMessages.ERROR_CODE_GROUP_ALREADY_EXISTS.getCode();
            String errorMessage = String.format(ErrorMessages.ERROR_CODE_GROUP_ALREADY_EXISTS.getMessage(), groupName);
            handleAddGroupFailure(errorCode, errorMessage, groupName, null, usersIds, claims);
            throw new UserStoreClientException(errorMessage, errorCode);
        }
        Group group;
        String groupId = generateGroupUUID();
        if (isUniqueGroupIdEnabled(this)) {
            if (!isUniqueUserIdEnabledInUserStore(userStore)) {
                String errorCode = ErrorMessages.ERROR_CODE_GROUP_UUID_NOT_SUPPORTED.getCode();
                String errorMessage = ErrorMessages.ERROR_CODE_GROUP_UUID_NOT_SUPPORTED.getMessage();
                handleAddGroupFailure(errorCode, errorMessage, groupName, null, usersIds, claims);
                throw new UserStoreException(errorCode + "-" + errorMessage);
            }
            group = doAddGroup(groupName, groupId, usersIds, buildClaimsList(claims));
            groupUniqueIDDomainResolver.setDomainForGroupId(group.getGroupID(), getMyDomainName(), tenantId,
                    false);
        } else {
            // Backward compatibility support. Use group resolver to update the other required places.
            GroupResolver groupResolver = UserStoreMgtDataHolder.getInstance().getGroupResolver();
            try {
                group = groupResolver.addGroup(groupName, groupId, claims, this);
                if (isUniqueUserIdEnabledInUserStore(userStore)) {
                    doAddGroupWithUserIds(groupName, usersIds);
                } else {
                    List<User> users = userUniqueIDManger.getUsers(usersIds, this);
                    doAddGroupWithUserNames(groupName,
                            users.stream().map(User::getUsername).collect(Collectors.toList()));
                }
                // Update only the cache since the ID can be found in our side.
                groupUniqueIDDomainResolver.setDomainForGroupId(group.getGroupID(), getMyDomainName(), tenantId,
                        true);
            } catch (UserStoreException e) {
                log.debug("error occurred while adding group:" + groupName, e);
                groupResolver.deleteGroupByName(groupName, this);
                throw e;
            }
        }
        updateClaimsWithGroupAttributes(group, claims);
        // #################### <Post-Listeners> #####################################################
        handlePostAddGroup(group.getGroupName(), group.getGroupID(), usersIds, claims);
        // Invoke legacy listeners to maintain backward compatibility.
        handlePostAddRoleWithID(groupName, usersIds.toArray(new String[0]), null, false);
        // #################### </Post-Listeners> #####################################################
        return group;
    }

    private Map<String, String> buildClaimsList(List<org.wso2.carbon.user.core.common.Claim> claims) {

        Map<String, String> claimsMap = new HashMap<>();
        for (org.wso2.carbon.user.core.common.Claim claim : claims) {
            claimsMap.put(claim.getClaimUrl(), claim.getClaimValue());
        }
        return claimsMap;
    }

    private void updateClaimsWithGroupAttributes(Group group, List<org.wso2.carbon.user.core.common.Claim> claims) {

        if (CollectionUtils.isEmpty(claims)) {
            return;
        }
        List<org.wso2.carbon.user.core.common.Claim> groupClaims = group.getClaims();
        for (org.wso2.carbon.user.core.common.Claim claim : claims) {
            String claimUrl = claim.getClaimUrl();
            switch (claimUrl) {
                case LOCATION_CLAIM_URI:
                    claim.setClaimValue(group.getLocation());
                    break;
                case CREATED_CLAIM_URI:
                    claim.setClaimValue(group.getCreatedDate());
                    break;
                case MODIFIED_CLAIM_URI:
                    claim.setClaimValue(group.getLastModifiedDate());
                    break;
                default:
                    if (CollectionUtils.isEmpty(groupClaims)) {
                        break;
                    }
                    for (org.wso2.carbon.user.core.common.Claim groupClaim : groupClaims) {
                        if (StringUtils.equals(groupClaim.getClaimUrl(), claimUrl)) {
                            claim.setClaimValue(groupClaim.getClaimValue());
                            break;
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public List<Group> getGroupListOfUser(String userId, int limit, int offset, String sortBy, String sortOrder)
            throws UserStoreException {

        if (!isSecureCall.get()) {
            Class[] argTypes = new Class[]{String.class, int.class, int.class, String.class, String.class};
            Object object = callSecure("getGroupListOfUser",
                    new Object[]{userId, limit, offset, sortBy, sortOrder}, argTypes);
            return (List<Group>) object;
        }
        if (StringUtils.isBlank(userId)) {
            throw new UserStoreClientException(ERROR_EMPTY_USER_ID.getMessage(), ERROR_EMPTY_USER_ID.getCode());
        }
        if (StringUtils.isNotBlank(sortBy) && StringUtils.isNotBlank(sortOrder)) {
            throw new UserStoreClientException(ERROR_SORTING_NOT_SUPPORTED.getMessage(),
                    ERROR_SORTING_NOT_SUPPORTED.getCode());
        }
        if (limit == 0) {
            return new ArrayList<>();
        }
        UserStore userStore = getUserStoreWithID(userId);
        if (userStore.isRecurssive()) {
            return ((AbstractUserStoreManager) userStore.getUserStoreManager()).
                    getGroupListOfUser(userId, limit, offset, sortBy, sortOrder);
        }
        // #################### Domain Name Free Zone Starts Here ################################
        // #################### Invoking pre-listeners ####################
        try {
            for (GroupOperationEventListener listener : UMListenerServiceComponent
                    .getGroupOperationEventListeners()) {
                if (listener instanceof AbstractGroupOperationEventListener) {
                    AbstractGroupOperationEventListener newListener = (AbstractGroupOperationEventListener) listener;
                    if (!newListener.preGetGroupsListOfUserByUserId(userId, this)) {
                        handlePreGetGroupsListByUserIdFailure(ERROR_DURING_PRE_GET_GROUPS_LIST_BY_USER_ID.getCode(),
                                String.format(ERROR_DURING_PRE_GET_GROUPS_LIST_BY_USER_ID.getMessage(),
                                        UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userId);
                        break;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleGetGroupsListByUserIdFailure(ERROR_DURING_PRE_GET_GROUPS_LIST_BY_USER_ID.getCode(),
                    String.format(ERROR_DURING_PRE_GET_GROUPS_LIST_BY_USER_ID.getMessage(), ex.getMessage()), userId);
            throw ex;
        }

        // #################### Invoke userstore methods ####################
        List<Group> groupsList = null;
        if (isUniqueGroupIdEnabled(this)) {
            groupsList = this.doGetGroupListOfUser(userId, limit, offset, sortBy, sortOrder);
        } else {
            // This is to support backward compatibility.
            List<String> groupNames = getRolesListOfUserWithId(userId, userStore);
            if (CollectionUtils.isNotEmpty(groupNames)) {
                // We need to do pagination here since we do not support pagination for groups.
                groupNames = paginateGroupsList(offset, limit, groupNames);
                groupsList = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(groupNames)) {
                    /*
                     * We need to create a list of Groups with group name only since other details will be added by the
                     * Group resolvers.
                     */
                    for (String groupName : groupNames) {
                        Group group = new Group(null, groupName);
                        group.setUserStoreDomain(getMyDomainName());
                        groupsList.add(group);
                    }
                    // Invoking group resolver to get data from the legacy approach.
                    GroupResolver groupResolver = UserStoreMgtDataHolder.getInstance().getGroupResolver();
                    if (groupResolver != null && groupResolver.isEnable()) {
                        groupResolver.getGroupsListOfUserByUserId(userId, groupsList, this);
                    }

                }
            }
        }

        // #################### Invoking post-listeners ####################
        try {
            for (GroupOperationEventListener listener : UMListenerServiceComponent
                    .getGroupOperationEventListeners()) {
                if (listener instanceof AbstractGroupOperationEventListener) {
                    AbstractGroupOperationEventListener newListener = (AbstractGroupOperationEventListener) listener;
                    if (!newListener.postGetGroupsListOfUserByUserId(userId, groupsList, this)) {
                        handlePostGetGroupsListByUserIdFailure(ERROR_DURING_POST_GET_GROUPS_LIST_BY_USER_ID.getCode(),
                                String.format(ERROR_DURING_POST_GET_GROUPS_LIST_BY_USER_ID.getMessage(),
                                        UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), userId);
                        break;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleGetGroupsListByUserIdFailure(ERROR_DURING_POST_GET_GROUPS_LIST_BY_USER_ID.getCode(),
                    String.format(ERROR_DURING_POST_GET_GROUPS_LIST_BY_USER_ID.getMessage(), ex.getMessage()), userId);
            throw ex;
        }
        return groupsList;
    }

    private List<String> paginateGroupsList(int givenOffset, int givenLimit, List<String> groupsList) {

        if (CollectionUtils.isEmpty(groupsList)) {
            groupsList = new ArrayList<>();
        }
        // Resolve with the default values
        int startIndex = resolveListOffset(givenOffset);
        int resolvedLimit = resolveGroupListLimit(givenLimit);
        int numberOfResults = groupsList.size();

        // We cannot return more than the available results. Therefore, max would be the available results.
        if (numberOfResults < resolvedLimit) {
            resolvedLimit = numberOfResults;
        }
        // We need to subtract 1 since indexes are starting from 0.
        int lastIndexOfTheResultsList = numberOfResults - 1;
        // When the offset is larger the available results
        if (lastIndexOfTheResultsList < startIndex) {
            return new ArrayList<>();
        }
        if (lastIndexOfTheResultsList == startIndex) {
            return groupsList.subList(lastIndexOfTheResultsList, lastIndexOfTheResultsList + 1);
        }
        int endIndex = resolvedLimit + startIndex - 1;
        if (lastIndexOfTheResultsList <= endIndex) {
            // Return from the start to the end of the list.
            return groupsList.subList(startIndex, lastIndexOfTheResultsList + 1);
        }
        return groupsList.subList(startIndex, endIndex + 1);
    }

    /**
     * Calculate the array offset needed to pagination.
     *
     * @param givenOffset Given offset value.
     * @return Resolved offset value.
     */
    private int resolveListOffset(int givenOffset) {

        if (givenOffset <= 1) {
            return 0;
        }
        // We need to subtract 1 since indexes are starting from 0.
        return givenOffset - 1;
    }

    @Override
    public List<User> getUserListOfGroup(String groupID, int limit, int offset, String sortBy, String sortOrder)
            throws UserStoreException {

        if (!isSecureCall.get()) {
            Class[] argTypes = new Class[]{String.class, int.class, int.class, String.class, String.class};
            Object object = callSecure("getUserListOfGroup",
                    new Object[]{groupID, limit, offset, sortBy, sortOrder}, argTypes);
            return (List<User>) object;
        }
        if (StringUtils.isBlank(groupID)) {
            throw new UserStoreClientException(ERROR_EMPTY_GROUP_ID.getMessage(), ERROR_EMPTY_GROUP_ID.getCode());
        }
        if (StringUtils.isNotBlank(sortBy) && StringUtils.isNotBlank(sortOrder)) {
            throw new UserStoreClientException(ERROR_SORTING_NOT_SUPPORTED.getMessage(),
                    ERROR_SORTING_NOT_SUPPORTED.getCode());
        }
        UserStore userStore = getUserStoreWithGroupId(groupID);
        if (userStore.isRecurssive()) {
            return ((AbstractUserStoreManager) userStore.getUserStoreManager()).
                    getUserListOfGroup(userStore.getDomainFreeGroupId(), limit, offset, sortBy, sortOrder);
        }
        // #################### Domain Name Free Zone Starts Here ################################
        String groupName = getGroupNameById(groupID);
        if (StringUtils.isBlank(groupName)) {
            throw new UserStoreClientException(String.format(ERROR_NO_GROUP_FOUND_WITH_ID.getMessage(), groupID,
                    tenantId), ERROR_NO_GROUP_FOUND_WITH_ID.getCode());
        }
        if (limit == 0) {
            // This is similar to requesting for no users.
            return new ArrayList<>();
        }
        // #################### Invoke userstore specific methods ################################
        List<User> filteredUsers;
        Condition condition = buildGroupFilterCondition(UserCoreUtil.removeDomainFromName(groupName));
        if (isUniqueUserIdEnabled()) {
            UniqueIDPaginatedSearchResult users = this.doGetUserListWithID(condition,
                    UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME, resolveUserListLimit(limit), offset,
                    sortBy, sortOrder);
            addUsersToUserIdCache(users.getUsers());
            addUsersToUserNameCache(users.getUsers());
            filteredUsers = users.getUsers();
        } else {
            PaginatedSearchResult users = this.doGetUserList(condition, UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME,
                    resolveUserListLimit(limit), offset, sortBy, sortOrder);
            filteredUsers = userUniqueIDManger.listUsers(users.getUsers(), this);
        }
        return filteredUsers;
    }

    private ExpressionCondition buildGroupFilterCondition(String groupName) {

        String domainFreeName = UserCoreUtil.removeDomainFromName(groupName);
        return new ExpressionCondition(ExpressionOperation.EQ.toString(),
                ExpressionAttribute.ROLE.toString(), domainFreeName);
    }

    @Override
    public void updateUserListOfGroup(String groupID, List<String> deletedUserIds, List<String> newUserIds)
            throws UserStoreException {

        String groupName = getGroupNameByGroupId(groupID);
        if (StringUtils.isBlank(groupName)) {
            throw new UserStoreClientException(String.format(ERROR_NO_GROUP_FOUND_WITH_ID.getMessage(), groupID,
                    tenantId), ERROR_NO_GROUP_FOUND_WITH_ID.getCode());
        }
        UserStore userStore = getUserStoreInternalWithGroupId(groupID);
        if (userStore.isRecurssive()) {
            ((AbstractUserStoreManager) userStore.getUserStoreManager())
                    .updateUserListOfGroup(groupID, UserCoreUtil.removeDomainFromNames(deletedUserIds),
                            UserCoreUtil.removeDomainFromNames(newUserIds));
            return;
        }
        if (isReadOnly()) {
            String errorCode = ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode();
            String errorMessage = ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage();
            handleUpdateUserListOfGroupFailure(errorCode, errorMessage, groupID, deletedUserIds, newUserIds);
            throw new UserStoreClientException(errorMessage, errorCode);
        }
        if (!writeGroupsEnabled) {
            String errorCode = ErrorMessages.ERROR_CODE_WRITE_GROUPS_NOT_ENABLED.getCode();
            String errorMessage = ErrorMessages.ERROR_CODE_WRITE_GROUPS_NOT_ENABLED.getMessage();
            handleUpdateUserListOfGroupFailure(errorCode, errorMessage, groupID, deletedUserIds, newUserIds);
            throw new UserStoreClientException(errorMessage, errorCode);
        }
        try {
            AccessController.doPrivileged((PrivilegedExceptionAction<String>) () -> {
                // If group Id feature is not enabled, we need to call the legacy method.
                if (isUniqueGroupIdEnabled()) {
                    updateUserListOfGroupByGroupId(groupID, UserCoreUtil.removeDomainFromName(groupName),
                            deletedUserIds, newUserIds);
                } else {
                    // Need to attach the user store domain name to the group name.
                    updateUserListOfRoleWithID(UserCoreUtil.addDomainToName(groupName, getMyDomainName()),
                            deletedUserIds.toArray(new String[0]), newUserIds.toArray(new String[0]));
                }
                return null;
            });
        } catch (PrivilegedActionException e) {
            if (!(e.getException() instanceof UserStoreException)) {
                handleUpdateUserListOfGroupFailure(
                        ErrorMessages.ERROR_WHILE_UPDATE_USER_LIST_OF_GROUP.getCode(),
                        String.format(ErrorMessages.ERROR_WHILE_UPDATE_USER_LIST_OF_GROUP.getMessage(),
                                e.getMessage()), groupID, deletedUserIds, newUserIds);
            }
            throw (UserStoreException) e.getException();
        }
    }

    /**
     * Update user list of group by group id in group uuid enabled userstores.
     *
     * @param groupId        Group id.
     * @param groupName      Group name.
     * @param deletedUserIds Deleted user ids.
     * @param newUserIds     New user ids.
     * @throws UserStoreException If an error occurs while updating user list of group.
     */
    private void updateUserListOfGroupByGroupId(String groupId, String groupName, List<String> deletedUserIds,
                                                List<String> newUserIds) throws UserStoreException {

        // ################################### Domain Name Free Zone Starts Here ####################################
        if (CollectionUtils.isEmpty(deletedUserIds)) {
            deletedUserIds = Collections.emptyList();
        }
        if (CollectionUtils.isEmpty(newUserIds)) {
            newUserIds = Collections.emptyList();
        }
        // ########################################## </Pre-Listeners> ##############################################
        if (!handlePreUpdateUserListOfGroup(groupName, deletedUserIds, newUserIds)) {
            handleUpdateUserListOfGroupFailure(
                    ErrorMessages.ERROR_WHILE_UPDATE_USER_LIST_OF_GROUP.getCode(),
                    String.format(ErrorMessages.ERROR_WHILE_UPDATE_USER_LIST_OF_GROUP.getMessage(),
                            UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), groupName, deletedUserIds,
                    newUserIds);
            return;
        }

        // Backward compatible listeners.
        if (!handlePreUpdateUserListOfRoleWithID(groupName, deletedUserIds.toArray(new String[0]),
                newUserIds.toArray(new String[0]), false, false)) {
            handleUpdateUserListOfRoleFailureWithID(
                    ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_UPDATE_USERS_OF_ROLE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_UPDATE_USERS_OF_ROLE.getMessage(),
                            UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), groupName,
                    deletedUserIds.toArray(new String[0]), newUserIds.toArray(new String[0]));
            return;
        }
        // ########################################## </Pre-Listeners> ##############################################
        try {
            doUpdateUserListOfGroup(groupId, deletedUserIds, newUserIds);
        } catch (UserStoreException ex) {
            handleUpdateUserListOfGroupFailure(
                    ErrorMessages.ERROR_WHILE_UPDATE_USER_LIST_OF_GROUP.getCode(),
                    String.format(ErrorMessages.ERROR_WHILE_UPDATE_USER_LIST_OF_GROUP.getMessage(), ex.getMessage()),
                    groupId, deletedUserIds, newUserIds);
            throw ex;
        }
        // Need to clear user roles cache upon roles update. Here role cache needs to be cleared to maintain backward
        // compatibility.
        clearUserRolesCacheByTenant(this.tenantId);
        // ########################################## </Post-Listeners> ##############################################
        handlePostUpdateUserListOfGroup(groupId, deletedUserIds, newUserIds);

        // Backward compatible listeners after updating user list of role.
        handleDoPostUpdateUserListOfRoleWithID(groupName, deletedUserIds.toArray(new String[0]),
                newUserIds.toArray(new String[0]), false, false);

        // ########################################## </Post-Listeners> ##############################################
    }

    @Override
    public void updateGroupListOfUser(String userID, List<String> deletedGroupIds, List<String> newGroupIds)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("updateGroupListOfUser operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException(
                "updateGroupListOfUser operation is not implemented in: " + this.getClass());
    }

    @Override
    public boolean isUserInGroup(String userID, String groupID) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class[] argTypes = new Class[]{String.class, String.class};
            Object object = callSecure("isUserInGroup", new Object[]{userID, groupID}, argTypes);
            return (boolean) object;
        }
        if (StringUtils.isBlank(groupID)) {
            throw new UserStoreClientException(ERROR_EMPTY_GROUP_ID.getMessage(), ERROR_EMPTY_GROUP_ID.getCode());
        }
        if (StringUtils.isBlank(userID)) {
            throw new UserStoreClientException(ERROR_EMPTY_USER_ID.getMessage(), ERROR_EMPTY_USER_ID.getCode());
        }
        String groupName = getGroupNameByGroupId(groupID);
        if (StringUtils.isBlank(groupName)) {
            throw new UserStoreClientException(String.format(ERROR_NO_GROUP_FOUND_WITH_ID.getMessage(), groupID,
                    tenantId), ERROR_NO_GROUP_FOUND_WITH_ID.getCode());
        }
        return isUserInRoleWithID(userID, groupName);
    }

    @Override
    public Map<String, List<Group>> getGroupListOfUsers(List<String> userIDs) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class[] argTypes = new Class[]{List.class};
            Object object = callSecure("getGroupListOfUsers", new Object[]{userIDs}, argTypes);
            return (Map<String, List<Group>>) object;
        }
        if (CollectionUtils.isEmpty(userIDs)) {
            return new HashMap<>();
        }
        Map<String, List<Group>> listOfGroups = new HashMap<>();
        for (String userId : userIDs) {
            List<Group> groupsForUser = getGroupListOfUser(userId, null, null);
            listOfGroups.put(userId, groupsForUser);
        }
        return listOfGroups;
    }

    @Override
    public boolean isGroupExist(String groupID) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class[] argTypes = new Class[]{String.class};
            Object object = callSecure("isGroupExist", new Object[]{groupID}, argTypes);
            return (boolean) object;
        }

        if (StringUtils.isBlank(groupID)) {
            throw new UserStoreClientException(ERROR_EMPTY_GROUP_ID.getMessage(), ERROR_EMPTY_GROUP_ID.getCode());
        }
        UserStore userStore = getUserStoreWithGroupId(groupID);
        if (userStore.isRecurssive()) {
            return ((AbstractUserStoreManager) userStore.getUserStoreManager()).isGroupExist(groupID);
        }

        // #################### Domain Name Free Zone Starts Here ################################
        return StringUtils.isNotBlank(getGroupNameById(groupID));
    }

    @Override
    public boolean isGroupExistWithName(String groupName) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class[] argTypes = new Class[]{String.class};
            Object object = callSecure("isGroupExistWithName", new Object[]{groupName}, argTypes);
            return (boolean) object;
        }
        if (StringUtils.isBlank(groupName)) {
            throw new UserStoreClientException(ERROR_EMPTY_GROUP_NAME.getMessage(), ERROR_EMPTY_GROUP_NAME.getCode());
        }
        UserStore userStore = getUserStoreWithGroupName(groupName);
        if (userStore.isRecurssive()) {
            return ((AbstractUserStoreManager) userStore.getUserStoreManager()).isGroupExistWithName(
                    UserCoreUtil.removeDomainFromName(groupName));
        }
        // #################### Domain Name Free Zone Starts Here ################################
        return StringUtils.isNotBlank(getGroupIdByGroupName(groupName));
    }

    @Override
    public void deleteGroup(String groupID) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class};
            callSecure("deleteGroup", new Object[]{groupID}, argTypes);
            return;
        }
        UserStore userStore = getUserStoreWithGroupId(groupID);
        if (userStore.isRecurssive()) {
            ((AbstractUserStoreManager) userStore.getUserStoreManager()).deleteGroup(userStore.getDomainFreeGroupId());
            return;
        }

        String groupName = getGroupNameByGroupId(groupID);
        String domain = UserCoreUtil.extractDomainFromName(groupName);
        // ############################# Domain Name Free Zone Starts Here ################################
        groupName = UserCoreUtil.removeDomainFromName(groupName);
        if (StringUtils.isBlank(groupName)) {
            throw new UserStoreClientException(String.format(ERROR_NO_GROUP_FOUND_WITH_ID.getMessage(), groupID,
                    tenantId), ERROR_NO_GROUP_FOUND_WITH_ID.getCode());
        }
        if (isReadOnly()) {
            handleDeleteGroupFailure(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode(),
                    ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage(), groupID);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.toString());
        }
        if (!writeGroupsEnabled) {
            handleDeleteRoleFailure(ErrorMessages.ERROR_CODE_WRITE_GROUPS_NOT_ENABLED.getCode(),
                    ErrorMessages.ERROR_CODE_WRITE_GROUPS_NOT_ENABLED.getMessage(), groupID);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_WRITE_GROUPS_NOT_ENABLED.toString());
        }
        // #################### <Pre-Listeners> #####################################################
        if (!handlePreDeleteGroup(groupName)) {
            return;
        }
        // Invoke legacy listeners to maintain backward compatibility.
        if (!handleDoPreDeleteRole(groupName, false)) {
            return;
        }
        // #################### </Pre-Listeners> #####################################################
        // Clear cache and mapper tables.
        groupUniqueIDDomainResolver.removeDomainForGroupId(groupID, domain, tenantId, false);
        try {
            if (isUniqueGroupIdEnabled()) {
                doDeleteGroupByGroupId(groupID);
            } else {
                doDeleteGroupByGroupName(groupName);
            }
        } catch (UserStoreException e) {
            // Add the deleted mapping back to the cache and the DB.
            groupUniqueIDDomainResolver.setDomainForGroupId(groupID, domain, tenantId, false);
            handleDeleteGroupFailure(ErrorMessages.ERROR_WHILE_DELETE_GROUP.getCode(),
                    String.format(ErrorMessages.ERROR_WHILE_DELETE_GROUP.getMessage(), e.getMessage()), groupID);
            throw e;
        }
        // #################### <Post-Listeners> #####################################################
        handlePostDeleteGroup(groupID, groupName);
        // Invoke legacy listeners to maintain backward compatibility.
        handleDoPostDeleteRole(groupName, false);
        // #################### </Post-Listeners> #####################################################

        // Clear the userRole cache.
        clearUserRolesCacheByTenant(tenantId);
    }

    @Override
    public Group renameGroup(String groupID, String newGroupName) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, String.class};
            Object object = callSecure("renameGroup", new Object[]{groupID, newGroupName}, argTypes);
            return (Group) object;
        }
        UserStore userStore = getUserStoreWithGroupId(groupID);
        UserStore newUserStore = getUserStoreWithGroupName(newGroupName);
        if (userStore.isRecurssive()) {
            return ((AbstractUserStoreManager) userStore.getUserStoreManager())
                    .renameGroup(userStore.getDomainFreeGroupId(), UserCoreUtil.removeDomainFromName(newGroupName));
        }
        // #################### Domain Name Free Zone Starts Here ################################
        // For non recursive user stores, we need to check the domain of the new group name.
        newGroupName = UserCoreUtil.removeDomainFromName(newGroupName);
        if (!isGroupNameValid(newGroupName)) {
            String regEx = realmConfig
                    .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_ROLE_NAME_JAVA_REG_EX);
            String errorMessage = String
                    .format(ErrorMessages.ERROR_CODE_INVALID_GROUP_NAME.getMessage(), newGroupName, regEx);
            String errorCode = ErrorMessages.ERROR_CODE_INVALID_GROUP_NAME.getCode();
            handleRenameGroupFailure(errorCode, errorMessage, groupID, newGroupName);
            throw new UserStoreClientException(errorMessage, errorCode);
        }
        String currentGroupName = UserCoreUtil.removeDomainFromName(getGroupNameByGroupId(groupID));
        // When the existing group name is same as the new group name, we do not fail the operation.
        if (!StringUtils.equalsIgnoreCase(currentGroupName, newGroupName) &&
                doCheckExistingGroupName(newGroupName)) {
            String errorCode = ErrorMessages.ERROR_CODE_GROUP_ALREADY_EXISTS.getCode();
            String errorMessage =
                    String.format(ErrorMessages.ERROR_CODE_GROUP_ALREADY_EXISTS.getMessage(), newGroupName);
            handleRenameGroupFailure(errorCode, errorMessage, groupID, newGroupName);
            throw new UserStoreClientException(errorMessage, errorCode);
        }
        if (isReadOnly()) {
            String errorCode = ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode();
            String errorMessage = ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage();
            handleRenameGroupFailure(errorCode, errorMessage, groupID, newGroupName);
            throw new UserStoreClientException(errorMessage, errorCode);
        }
        if (!writeGroupsEnabled) {
            String errorCode = ErrorMessages.ERROR_CODE_WRITE_GROUPS_NOT_ENABLED.getCode();
            String errorMessage = ErrorMessages.ERROR_CODE_WRITE_GROUPS_NOT_ENABLED.getMessage();
            handleRenameGroupFailure(errorCode, errorMessage, groupID, newGroupName);
            throw new UserStoreClientException(errorMessage, errorCode);
        }
        // ############################### <Pre-Listeners> ##########################################

        if (!handlePreRenameGroup(groupID, newGroupName)) {
            return null;
        }
        // Invoking legacy listeners.
        if (!handlePreUpdateRoleName(currentGroupName, newGroupName, false)) {
            return null;
        }

        // ############################### </Pre-Listeners> ##########################################
        clearGroupIDResolverCache(groupID, tenantId);
        try {
            if (isUniqueGroupIdEnabled()) {
                doUpdateGroupNameByGroupId(groupID, newGroupName);
            } else {
                // Current group name does not have the domain here.
                doUpdateGroupName(currentGroupName, UserCoreUtil.removeDomainFromName(newGroupName));
            }
        } catch (UserStoreException e) {
            // Add the deleted mapping back to the cache and the DB.
            addGroupNameToGroupIdCache(groupID, UserCoreUtil.removeDomainFromName(currentGroupName), getMyDomainName());
            handleRenameGroupFailure(ErrorMessages.ERROR_WHILE_RENAME_GROUP.getCode(),
                    String.format(ErrorMessages.ERROR_WHILE_RENAME_GROUP.getMessage(), e.getMessage()), groupID,
                    newGroupName);
            throw e;
        }
        addGroupNameToGroupIdCache(groupID, UserCoreUtil.removeDomainFromName(newGroupName), this.getMyDomainName());
        // ############################### <Post-Listeners> ##########################################

        handlePostRenameGroup(groupID, newGroupName);
        // Invoking legacy listeners.
        handlePostUpdateRoleName(currentGroupName, newGroupName, false);

        // ############################### </Post-Listeners> ##########################################
        return new Group(groupID, newUserStore.getDomainAwareGroupName());
    }

    @Override
    public User addUser(String userName, Object credential, List<Claim> claims, List<String> groupsIds,
                        String profileName) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("addUser operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException(
                "addUser operation is not implemented in: " + this.getClass());
    }

    @Override
    public String getGroupNameByGroupId(String groupId) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class[] argTypes = new Class[]{String.class};
            Object object = callSecure("getGroupNameByGroupId", new Object[]{groupId}, argTypes);
            return (String) object;
        }
        if (StringUtils.isBlank(groupId)) {
            throw new UserStoreClientException(ERROR_EMPTY_GROUP_ID.getMessage(), ERROR_EMPTY_GROUP_ID.getCode());
        }
        UserStore userStore = getUserStoreWithGroupId(groupId);
        if (userStore.isRecurssive()) {
            return ((AbstractUserStoreManager) userStore.getUserStoreManager()).getGroupNameByGroupId(groupId);
        }

        // #################### Domain Name Free Zone Starts Here ################################
        String groupName = getGroupNameById(groupId);
        if (StringUtils.isBlank(groupName)) {
            throw new UserStoreClientException(String.format(ERROR_NO_GROUP_FOUND_WITH_ID.getMessage(), groupId,
                    tenantId), ERROR_NO_GROUP_FOUND_WITH_ID.getCode());
        }
        return groupName;
    }

    /**
     * Get the group name by the group id.
     * NOTE: Userstore needs to be resolved before using this method.
     *
     * @param groupId Group id.
     * @return Name of the group with the given id.
     * @throws UserStoreException If an error occurred.
     */
    private String getGroupNameById(String groupId) throws UserStoreException {

        // #################### Invoking pre-listeners ####################
        try {
            for (GroupOperationEventListener listener : UMListenerServiceComponent
                    .getGroupOperationEventListeners()) {
                if (listener instanceof AbstractGroupOperationEventListener) {
                    AbstractGroupOperationEventListener newListener = (AbstractGroupOperationEventListener) listener;
                    if (!newListener.preGetGroupNameById(groupId, this)) {
                        handlePreGetGroupNameByIdFailure(ERROR_DURING_PRE_GET_GROUP_NAME_BY_ID.getCode(),
                                String.format(ERROR_DURING_PRE_GET_GROUP_NAME_BY_ID.getMessage(),
                                        UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), groupId);
                        break;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleGetGroupIdByNameFailure(ERROR_DURING_PRE_GET_GROUP_NAME_BY_ID.getCode(),
                    String.format(ERROR_DURING_PRE_GET_GROUP_NAME_BY_ID.getMessage(), ex.getMessage()), groupId);
            throw ex;
        }

        // #################### Invoke userstore methods ####################
        Group group = new Group(groupId);
        if (isUniqueGroupIdEnabled(this)) {
            String groupName = this.doGetGroupNameFromGroupId(groupId);
            group.setGroupName(groupName);
            group.setUserStoreDomain(getMyDomainName());
        } else {
            // Invoking group resolver to get data from the legacy approach.
            GroupResolver groupResolver = UserStoreMgtDataHolder.getInstance().getGroupResolver();
            if (groupResolver != null && groupResolver.isEnable()) {
                groupResolver.getGroupNameById(groupId, group, this);
            }
        }

        // #################### Invoking post-listeners ####################
        try {
            for (GroupOperationEventListener listener : UMListenerServiceComponent
                    .getGroupOperationEventListeners()) {
                if (listener instanceof AbstractGroupOperationEventListener) {
                    AbstractGroupOperationEventListener newListener = (AbstractGroupOperationEventListener) listener;
                    if (!newListener.postGetGroupNameById(groupId, group, this)) {
                        handlePostGetGroupNameByIdFailure(ERROR_DURING_POST_GET_GROUP_NAME_BY_ID.getCode(),
                                String.format(ERROR_DURING_POST_GET_GROUP_NAME_BY_ID.getMessage(),
                                        UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), groupId);
                        break;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleGetGroupNameByIdFailure(ERROR_DURING_POST_GET_GROUP_NAME_BY_ID.getCode(),
                    String.format(ERROR_DURING_POST_GET_GROUP_NAME_BY_ID.getMessage(), ex.getMessage()), groupId);
            throw ex;
        }
        return group.getGroupName();
    }

    @Override
    public String getGroupIdByGroupName(String groupName) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class[] argTypes = new Class[]{String.class};
            Object object = callSecure("getGroupIdByGroupName", new Object[]{groupName}, argTypes);
            return (String) object;
        }
        if (StringUtils.isBlank(groupName)) {
            throw new UserStoreClientException(ERROR_EMPTY_GROUP_NAME.getMessage(), ERROR_EMPTY_GROUP_NAME.getCode());
        }
        UserStore userStore = getUserStoreWithGroupName(groupName);
        if (userStore.isRecurssive()) {
            return ((AbstractUserStoreManager) userStore.getUserStoreManager()).
                    getGroupIdByGroupName(userStore.getDomainFreeGroupName());
        }

        // #################### Domain Name Free Zone Starts Here ################################
        // #################### Invoking pre-listeners ####################
        try {
            for (GroupOperationEventListener listener : UMListenerServiceComponent
                    .getGroupOperationEventListeners()) {
                if (listener instanceof AbstractGroupOperationEventListener) {
                    AbstractGroupOperationEventListener newListener = (AbstractGroupOperationEventListener) listener;
                    if (!newListener.preGetGroupIdByName(groupName, this)) {
                        handlePreGetGroupIdByNameFailure(ERROR_DURING_PRE_GET_GROUP_ID_BY_NAME.getCode(),
                                String.format(ERROR_DURING_PRE_GET_GROUP_ID_BY_NAME.getMessage(),
                                        UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), groupName);
                        break;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleGetGroupIdByNameFailure(ERROR_DURING_PRE_GET_GROUP_ID_BY_NAME.getCode(),
                    String.format(ERROR_DURING_PRE_GET_GROUP_ID_BY_NAME.getMessage(), ex.getMessage()), groupName);
            throw ex;
        }

        // #################### Invoke userstore methods ####################
        Group group = new Group(null, groupName);
        group.setUserStoreDomain(getMyDomainName());
        if (isUniqueGroupIdEnabled(this)) {
            String groupId = this.doGetGroupIdFromGroupName(groupName);
            group.setGroupID(groupId);
        } else {
            // Invoking group resolver to get data from the legacy approach.
            GroupResolver groupResolver = UserStoreMgtDataHolder.getInstance().getGroupResolver();
            if (groupResolver != null && groupResolver.isEnable()) {
                groupResolver.getGroupIdByName(groupName, group, this);
            }
        }

        // #################### Invoking post-listeners ####################
        try {
            for (GroupOperationEventListener listener : UMListenerServiceComponent
                    .getGroupOperationEventListeners()) {
                if (listener instanceof AbstractGroupOperationEventListener) {
                    AbstractGroupOperationEventListener newListener = (AbstractGroupOperationEventListener) listener;
                    if (!newListener.postGetGroupIdByName(groupName, group, this)) {
                        handlePostGetGroupIdByNameFailure(ERROR_DURING_POST_GET_GROUP_ID_BY_NAME.getCode(),
                                String.format(ERROR_DURING_POST_GET_GROUP_ID_BY_NAME.getMessage(),
                                        UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), groupName);
                        break;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleGetGroupIdByNameFailure(ERROR_DURING_POST_GET_GROUP_ID_BY_NAME.getCode(),
                    String.format(ERROR_DURING_POST_GET_GROUP_ID_BY_NAME.getMessage(), ex.getMessage()), groupName);
            throw ex;
        }
        if (StringUtils.isBlank(group.getGroupID())) {
            throw new UserStoreClientException(String.format(ERROR_NO_GROUP_FOUND_WITH_NAME.getMessage(), groupName,
                    tenantId), ERROR_NO_GROUP_FOUND_WITH_NAME.getCode());
        }
        return group.getGroupID();
    }

    @Override
    public Group getGroupByGroupName(String groupName, List<String> requiredAttributes) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class[] argTypes = new Class[]{String.class, List.class};
            Object object = callSecure("getGroupByGroupName",
                    new Object[]{groupName, requiredAttributes}, argTypes);
            return (Group) object;
        }
        if (StringUtils.isBlank(groupName)) {
            throw new UserStoreClientException(ERROR_EMPTY_GROUP_NAME.getMessage(), ERROR_EMPTY_GROUP_NAME.getCode());
        }
        UserStore userStore = getUserStoreWithGroupName(groupName);
        if (userStore.isRecurssive()) {
            return ((AbstractUserStoreManager) userStore.getUserStoreManager()).
                    getGroupByGroupName(userStore.getDomainFreeGroupName(), requiredAttributes);
        }

        // #################### Domain Name Free Zone Starts Here ################################
        // #################### Invoking pre-listeners ####################
        try {
            for (GroupOperationEventListener listener : UMListenerServiceComponent
                    .getGroupOperationEventListeners()) {
                if (listener instanceof AbstractGroupOperationEventListener) {
                    AbstractGroupOperationEventListener newListener = (AbstractGroupOperationEventListener) listener;
                    if (!newListener.preGetGroupByName(groupName, requiredAttributes, this)) {
                        handlePreGetGroupByNameFailure(ERROR_DURING_PRE_GET_GROUP_BY_NAME.getCode(),
                                String.format(ERROR_DURING_PRE_GET_GROUP_BY_NAME.getMessage(),
                                        UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), groupName,
                                requiredAttributes);
                        break;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleGetGroupByNameFailure(ERROR_DURING_PRE_GET_GROUP_BY_NAME.getCode(),
                    String.format(ERROR_DURING_PRE_GET_GROUP_BY_NAME.getMessage(), ex.getMessage()),
                    groupName, requiredAttributes);
            throw ex;
        }

        // #################### Invoke userstore methods ####################
        Group group = new Group(null, groupName);
        if (isUniqueGroupIdEnabled(this)) {
            group = this.doGetGroupFromGroupName(groupName, requiredAttributes);
        } else {
            // Invoking group resolver to get data from the legacy approach.
            GroupResolver groupResolver = UserStoreMgtDataHolder.getInstance().getGroupResolver();
            if (groupResolver != null && groupResolver.isEnable()) {
                groupResolver.getGroupByName(groupName, requiredAttributes, group, this);
            }
        }

        // #################### Invoking post-listeners ####################
        try {
            for (GroupOperationEventListener listener : UMListenerServiceComponent
                    .getGroupOperationEventListeners()) {
                if (listener instanceof AbstractGroupOperationEventListener) {
                    AbstractGroupOperationEventListener newListener = (AbstractGroupOperationEventListener) listener;
                    if (!newListener.postGetGroupByName(groupName, requiredAttributes, group, this)) {
                        handlePostGetGroupByNameFailure(ERROR_DURING_POST_GET_GROUP_BY_NAME.getCode(),
                                String.format(ERROR_DURING_POST_GET_GROUP_BY_NAME.getMessage(),
                                        UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), groupName,
                                requiredAttributes);
                        break;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleGetGroupByNameFailure(ERROR_DURING_POST_GET_GROUP_BY_NAME.getCode(),
                    String.format(ERROR_DURING_POST_GET_GROUP_BY_NAME.getMessage(), ex.getMessage()),
                    groupName, requiredAttributes);
            throw ex;
        }
        if (isUniqueGroupIdEnabled() && StringUtils.isBlank(group.getGroupID())) {
            throw new UserStoreClientException(String.format(ERROR_NO_GROUP_FOUND_WITH_NAME.getMessage(), groupName,
                    tenantId), ERROR_NO_GROUP_FOUND_WITH_NAME.getCode());
        }
        return group;
    }

    @Override
    public Group getGroup(String groupID, List<String> requiredAttributes) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class[] argTypes = new Class[]{String.class, List.class};
            Object object = callSecure("getGroup", new Object[]{groupID, requiredAttributes}, argTypes);
            return (Group) object;
        }

        if (StringUtils.isBlank(groupID)) {
            throw new UserStoreClientException(ERROR_EMPTY_GROUP_ID.getMessage(), ERROR_EMPTY_GROUP_ID.getCode());
        }

        UserStore userStore = getUserStoreWithGroupId(groupID);
        if (userStore.isRecurssive()) {
            return ((AbstractUserStoreManager) userStore.getUserStoreManager()).getGroup(
                    userStore.getDomainFreeGroupId(), requiredAttributes);
        }
        // #################### Domain Name Free Zone Starts Here ################################
        // #################### Invoking pre-listeners ####################
        try {
            for (GroupOperationEventListener listener : UMListenerServiceComponent
                    .getGroupOperationEventListeners()) {
                if (listener instanceof AbstractGroupOperationEventListener) {
                    AbstractGroupOperationEventListener newListener = (AbstractGroupOperationEventListener) listener;
                    if (!newListener.preGetGroupById(groupID, requiredAttributes, this)) {
                        handlePreGetGroupByIdFailure(ERROR_DURING_PRE_GET_GROUP_BY_ID.getCode(),
                                String.format(ERROR_DURING_PRE_GET_GROUP_BY_ID.getMessage(),
                                        UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), groupID,
                                requiredAttributes);
                        break;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleGetGroupByIdFailure(ERROR_DURING_PRE_GET_GROUP_BY_ID.getCode(),
                    String.format(ERROR_DURING_PRE_GET_GROUP_BY_ID.getMessage(), ex.getMessage()),
                    groupID, requiredAttributes);
            throw ex;
        }

        // #################### Invoke userstore methods ####################
        Group group = new Group(groupID);
        if (isUniqueGroupIdEnabled(this)) {
            group = this.doGetGroupFromGroupId(groupID, requiredAttributes);
        } else {
            // Invoking group resolver to get data from the legacy approach.
            GroupResolver groupResolver = UserStoreMgtDataHolder.getInstance().getGroupResolver();
            if (groupResolver != null && groupResolver.isEnable()) {
                groupResolver.getGroupById(groupID, requiredAttributes, group, this);
            }
        }
        // #################### Invoking post-listeners ####################
        try {
            for (GroupOperationEventListener listener : UMListenerServiceComponent
                    .getGroupOperationEventListeners()) {
                if (listener instanceof AbstractGroupOperationEventListener) {
                    AbstractGroupOperationEventListener newListener = (AbstractGroupOperationEventListener) listener;
                    if (!newListener.postGetGroupById(groupID, requiredAttributes, group, this)) {
                        handlePostGetGroupByIdFailure(ERROR_DURING_POST_GET_GROUP_BY_ID.getCode(),
                                String.format(ERROR_DURING_POST_GET_GROUP_BY_ID.getMessage(),
                                        UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), groupID,
                                requiredAttributes);
                        break;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleGetGroupByIdFailure(ERROR_DURING_POST_GET_GROUP_BY_ID.getCode(),
                    String.format(ERROR_DURING_POST_GET_GROUP_BY_ID.getMessage(), ex.getMessage()),
                    groupID, requiredAttributes);
            throw ex;
        }
        return group;
    }

    @Override
    public Group updateGroup(String groupID, List<org.wso2.carbon.user.core.common.Claim> claims)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("updateGroup operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException("updateGroup operation is not implemented in: " + this.getClass());
    }

    @Override
    public List<User> getUserListOfGroup(String groupID, String sortBy, String sortOrder) throws UserStoreException {

        // Limit of -1 will indicate that to return user store configured number of results.
        return getUserListOfGroup(groupID, -1, 1, sortBy, sortOrder);
    }

    @Override
    public List<Group> getGroupListOfUser(String userId, String sortBy, String sortOrder) throws UserStoreException {

        // Limit of -1 will be used to denote max limit for the userstore.
        return getGroupListOfUser(userId, -1, 0, sortBy, sortOrder);
    }

    private List<String> getNamesWithDomain(List<String> identifiers, String domain) {

        List<String> usersWithDomain = new ArrayList<>();
        for (String identifier : identifiers) {
            usersWithDomain.add(UserCoreUtil.addDomainToName(identifier, domain));
        }
        return usersWithDomain;
    }

    private String getUserStoreDomainName(UserStoreManager userStoreManager) {

        String domainNameProperty = userStoreManager.getRealmConfiguration()
                .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
        if (StringUtils.isEmpty(domainNameProperty)) {
            domainNameProperty = UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME;
        }
        return domainNameProperty;
    }

    /**
     * Process and return the modifed claim values against claim URI. Add or remove specific claim values
     * against old claim values of multi-valued claims. Replace old claim values from modified values for
     * non multi-valued claims.
     *
     * @param oldClaimMap                      Map of claim URIs against old claim values of user.
     * @param multiValuedClaimsToAdd           Map of multi-valued claim URIs against values need to be added to
     *                                         old claim value.
     * @param multiValuedClaimsToDelete        Map of multi-valued claim URIs against values need to be removed from
     *                                         old claim value.
     * @param claimsExcludingMultiValuedClaims Map of non multi-valued claim URIs against modified values to be stred.
     * @return Map of claim URIs against the modified claim values.
     */
    private Map<String, String> getModifiedClaims(Map<String, List<String>> oldClaimMap,
                                                  Map<String, List<String>> multiValuedClaimsToAdd,
                                                  Map<String, List<String>> multiValuedClaimsToDelete,
                                                  Map<String, List<String>> claimsExcludingMultiValuedClaims) {

        Map<String, String> claims = new HashMap<>();
        String separator = ",";
        if (StringUtils.isNotEmpty(realmConfig.getUserStoreProperty(MULTI_ATTRIBUTE_SEPARATOR))) {
            separator = realmConfig.getUserStoreProperty(MULTI_ATTRIBUTE_SEPARATOR);
        }
        if (claimsExcludingMultiValuedClaims != null) {
            for (String claimURI : claimsExcludingMultiValuedClaims.keySet()) {
                claims.put(claimURI,
                        StringUtils.join(claimsExcludingMultiValuedClaims.get(claimURI).iterator(), separator));
            }
        }

        // Get modified claim values for multi-valued claims.
        if (multiValuedClaimsToAdd != null) {
            for (String claimURI : multiValuedClaimsToAdd.keySet()) {
                List<String> modifiedValue = new ArrayList<>();
                if (oldClaimMap.containsKey(claimURI)) {
                    modifiedValue.addAll(oldClaimMap.get(claimURI));
                    modifiedValue.addAll(multiValuedClaimsToAdd.get(claimURI));
                } else {
                    modifiedValue.addAll(multiValuedClaimsToAdd.get(claimURI));
                }
                claims.put(claimURI, StringUtils.join(modifiedValue.iterator(), separator));
            }
        }
        if (multiValuedClaimsToDelete != null) {
            for (String claimURI : multiValuedClaimsToDelete.keySet()) {
                List<String> values = null;
                if (claims.containsKey(claimURI)) {
                    values = Arrays.asList(claims.get(claimURI).split(separator));
                } else if (oldClaimMap.containsKey(claimURI)) {
                    values = oldClaimMap.get(claimURI);
                }
                if (!CollectionUtils.isEmpty(values)) {
                    List<String> modifiedValue =
                            (List<String>) CollectionUtils.subtract(values, multiValuedClaimsToDelete.get(claimURI));
                    claims.put(claimURI, StringUtils.join(modifiedValue.iterator(), separator));
                }
            }
        }
        return claims;
    }

    private void invokeDoPreSetUserClaimsWithIDListeners(String userID, Map<String, String> claims, String profileName)
            throws UserStoreException {

        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!((AbstractUserOperationEventListener) listener)
                        .doPreSetUserClaimValuesWithID(userID, claims, profileName, this)) {
                    handleSetUserClaimValuesFailureWithID(
                            ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_SET_USER_CLAIM_VALUES.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_SET_USER_CLAIM_VALUES.getMessage(),
                                    UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userID, claims,
                            profileName);
                    return;
                }
            }
        } catch (UserStoreException e) {
            handleSetUserClaimValuesFailureWithID(
                    ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_SET_USER_CLAIM_VALUES.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_SET_USER_CLAIM_VALUES.getMessage(),
                            e.getMessage()), userID, claims, profileName);
            throw e;
        }
    }

    private void invokeDoPostSetUserClaimsWithIDListeners(String userID, Map<String, String> claims, String profileName)
            throws UserStoreException {

        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!((AbstractUserOperationEventListener) listener)
                        .doPostSetUserClaimValuesWithID(userID, claims, profileName, this)) {
                    handleSetUserClaimValuesFailureWithID(
                            ErrorMessages.ERROR_CODE_ERROR_DURING_POST_SET_USER_CLAIM_VALUES.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_SET_USER_CLAIM_VALUES.getMessage(),
                                    UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), userID, claims,
                            profileName);
                    return;
                }
            }
        } catch (UserStoreException e) {
            handleSetUserClaimValuesFailureWithID(
                    ErrorMessages.ERROR_CODE_ERROR_DURING_POST_SET_USER_CLAIM_VALUES.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_SET_USER_CLAIM_VALUES.getMessage(),
                            e.getMessage()), userID, claims, profileName);
            throw e;
        }
    }

    /**
     * Throw an error if the requested user does not exist.
     *
     * @param userName              Username.
     * @param profileName           Profile name.
     * @throws UserStoreException   Exception when the user does not exist.
     */
    protected void handleGetNonExistentUser(String userName, String claim, String profileName)
            throws UserStoreException {

        String errorCode = ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getCode();
        String errorMessage = String.format(ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getMessage(), userName,
                realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME));
        handleGetUserClaimValueFailure(errorCode, errorMessage, userName, claim, profileName);
        throw new UserStoreException(errorCode + " - " + errorMessage);
    }

    private boolean isNotARoleClaim(String claim) {

        return !UserCoreConstants.ROLE_CLAIM.equalsIgnoreCase(claim)
                || !UserCoreConstants.INT_ROLE_CLAIM.equalsIgnoreCase(claim)
                || !UserCoreConstants.EXT_ROLE_CLAIM.equalsIgnoreCase(claim);
    }

    private boolean isNotARolesClaim(String claim) {

        return !UserCoreConstants.INTERNAL_ROLES_CLAIM.equalsIgnoreCase(claim);
    }

    private boolean isNotAGroupsClaim(String claim) {

        return !UserCoreConstants.USER_STORE_GROUPS_CLAIM.equalsIgnoreCase(claim);
    }

    private boolean isNotARoleOrGroupClaim(String claim) {

        return isNotARoleClaim(claim)
                || (isGroupsVsRolesSeparationImprovementsEnabled(realmConfig) && (isNotARolesClaim(claim) || isNotAGroupsClaim(claim)));
    }

    private String getMultiValuedString(List<String> values) {

        String userAttributeSeparator = ",";
        String claimSeparator = realmConfig.getUserStoreProperty(MULTI_ATTRIBUTE_SEPARATOR);
        if (claimSeparator != null && !claimSeparator.trim().isEmpty()) {
            userAttributeSeparator = claimSeparator;
        }
        String delim = "";
        StringBuffer multiValuedStringBf = new StringBuffer();
        for (String eachValue : values) {
            multiValuedStringBf.append(delim).append(eachValue);
            delim = userAttributeSeparator;
        }
        return multiValuedStringBf.toString();
    }

    private Set<String> getRolesAndGroupsClaimURIs() {

        return Stream.of(INTERNAL_ROLES_CLAIM, USER_STORE_GROUPS_CLAIM, ROLE_CLAIM).collect(Collectors.toSet());
    }

    /**
     * Returns the group which has the provided id.
     *
     * @param groupId            Group id of the user group.
     * @param requiredAttributes Requested attributes.
     * @return Name of the group.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    protected Group doGetGroupFromGroupId(String groupId, List<String> requiredAttributes)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doGetGroupFromGroupID operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException(
                "doGetGroupFromGroupID operation is not implemented in: " + this.getClass());
    }

    /**
     * Returns the group which has the provided group name.
     *
     * @param groupName          Group id of the user group.
     * @param requiredAttributes Requested attributes.
     * @return Name of the group.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    protected Group doGetGroupFromGroupName(String groupName, List<String> requiredAttributes)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doGetGroupFromGroupName operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException(
                "doGetGroupFromGroupName operation is not implemented in: " + this.getClass());
    }

    /**
     * Returns the id of the group which has the provided group name.
     *
     * @param groupName Group name of the user group.
     * @return Name of the group.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    protected String doGetGroupIdFromGroupName(String groupName) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doGetGroupIdFromGroupName operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException(
                "doGetGroupIdFromGroupName operation is not implemented in: " + this.getClass());
    }

    /**
     * Retrieves list of groups of a given user ID.
     *
     * @param userId    User ID.
     * @param limit     No of search results. If the given value is greater than the system configured max limit
     *                  it will be reset to the system configured max limit.
     * @param offset    Start index of the user search.
     * @param sortBy    Sorted by.
     * @param sortOrder Sorted order.
     * @return List of Group objects.
     * @throws UserStoreException If an error occurs while getting groups list of a user.
     */
    protected List<Group> doGetGroupListOfUser(String userId, int limit, int offset, String sortBy, String sortOrder)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doGetGroupListOfUser operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException("doGetGroupListOfUser operation is not implemented in: " + this.getClass());
    }

    /**
     * Retrieves list of groups evaluating the condition.
     *
     * @param condition Conditional filter.
     * @param limit     Number of search results. If the given value is greater than the system configured max limit
     *                  it will be reset to the system configured max limit.
     * @param offset    Start index of the user search.
     * @param sortBy    Sorted by.
     * @param sortOrder Sorted order.
     * @return List of Group objects.
     * @throws UserStoreException If an error occurs while getting groups list.
     */
    protected List<Group> doListGroups(Condition condition, int limit, int offset, String sortBy, String sortOrder)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doGetGroupListOfUser operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException("doGetGroupListOfUser operation is not implemented in: " + this.getClass());
    }

    /**
     * Returns the name of the group which has the provided group id.
     *
     * @param groupId Group id of the user group.
     * @return Name of the group.
     * @throws UserStoreException Thrown by the underlying UserStoreManager.
     */
    protected String doGetGroupNameFromGroupId(String groupId) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doGetGroupNameFromGroupId operation is not implemented in: " + this.getClass());
        }
        throw new NotImplementedException(
                "doGetGroupNameFromGroupId operation is not implemented in: " + this.getClass());
    }

    /**
     * This method is responsible for calling the relevant methods when there is a failure while pre-trying to get the
     * group by id.
     *
     * @param errorCode          Error Code.
     * @param errorMessage       Error Message.
     * @param groupId            Group unique id.
     * @param requiredAttributes Requested Claims.
     * @throws UserStoreException Exception that will be thrown by relevant listener methods.
     */
    private void handlePreGetGroupByIdFailure(String errorCode, String errorMessage, String groupId,
                                              List<String> requiredAttributes)
            throws UserStoreException {

        for (GroupManagementErrorEventListener listener : UMListenerServiceComponent
                .getGroupManagementErrorEventListeners()) {
            if (listener.isEnable() && listener instanceof AbstractGroupManagementErrorEventListener
                    && !listener.onPreGetGroupByIdFailure(errorCode, errorMessage, groupId,
                    requiredAttributes, this)) {
                log.error("'onPreGetGroupByIdFailure' event invocation failed for listener: " +
                        listener.getClass().getName());
                return;
            }
        }
    }

    /**
     * This method is responsible for calling the relevant methods when there is a failure while pre-trying to get the
     * group by group name.
     *
     * @param errorCode          Error Code.
     * @param errorMessage       Error Message.
     * @param groupName          Group name.
     * @param requiredAttributes Requested Claims.
     * @throws UserStoreException Exception that will be thrown by relevant listener methods.
     */
    private void handlePreGetGroupByNameFailure(String errorCode, String errorMessage, String groupName,
                                                List<String> requiredAttributes)
            throws UserStoreException {

        for (GroupManagementErrorEventListener listener : UMListenerServiceComponent
                .getGroupManagementErrorEventListeners()) {
            if (listener.isEnable() && listener instanceof AbstractGroupManagementErrorEventListener
                    && !listener.onPreGetGroupByNameFailure(errorCode, errorMessage, groupName,
                    requiredAttributes, this)) {
                log.error("'onPreGetGroupByNameFailure' event invocation failed for listener: " +
                        listener.getClass().getName());
                return;
            }
        }
    }

    /**
     * This method is responsible for calling the relevant methods when there is a failure while pre-trying to get the
     * group id by group name.
     *
     * @param errorCode    Error Code.
     * @param errorMessage Error Message.
     * @param groupName    Group name.
     * @throws UserStoreException Exception that will be thrown by relevant listener methods.
     */
    private void handlePreGetGroupIdByNameFailure(String errorCode, String errorMessage, String groupName)
            throws UserStoreException {

        for (GroupManagementErrorEventListener listener : UMListenerServiceComponent
                .getGroupManagementErrorEventListeners()) {
            if (listener.isEnable() && listener instanceof AbstractGroupManagementErrorEventListener
                    && !listener.onPreGetGroupIdByNameFailure(errorCode, errorMessage, groupName, this)) {
                log.error("'onPreGetGroupIdByNameFailure' event invocation failed for listener: " +
                        listener.getClass().getName());
                return;
            }
        }
    }

    /**
     * This method is responsible for calling the relevant methods when there is a failure while pre-trying to get the
     * group name by group id.
     *
     * @param errorCode    Error Code.
     * @param errorMessage Error Message.
     * @param groupId      Group id.
     * @throws UserStoreException Exception that will be thrown by relevant listener methods.
     */
    private void handlePreGetGroupNameByIdFailure(String errorCode, String errorMessage, String groupId)
            throws UserStoreException {

        for (GroupManagementErrorEventListener listener : UMListenerServiceComponent
                .getGroupManagementErrorEventListeners()) {
            if (listener.isEnable() && listener instanceof AbstractGroupManagementErrorEventListener
                    && !listener.onPreGetGroupNameByIdFailure(errorCode, errorMessage, groupId, this)) {
                log.error("'handlePreGetGroupNameByIdFailure' event invocation failed for listener: " +
                        listener.getClass().getName());
                return;
            }
        }
    }

    /**
     * This method is responsible for calling the relevant methods when there is a failure while pre-trying to get the
     * groups list by user id.
     *
     * @param errorCode    Error Code.
     * @param errorMessage Error Message.
     * @param userId       User id.
     * @throws UserStoreException Exception that will be thrown by relevant listener methods.
     */
    private void handlePreGetGroupsListByUserIdFailure(String errorCode, String errorMessage, String userId)
            throws UserStoreException {

        for (GroupManagementErrorEventListener listener : UMListenerServiceComponent
                .getGroupManagementErrorEventListeners()) {
            if (listener.isEnable() && listener instanceof AbstractGroupManagementErrorEventListener
                    && !listener.onPreGetGroupsListByUserIdFailure(errorCode, errorMessage,
                    userId, this)) {
                log.error("'onPreGetGroupsListByUserIdFailure' event invocation failed for listener: " +
                        listener.getClass().getName());
                return;
            }
        }
    }

    /**
     * This method is responsible for calling the relevant methods when there is a failure while pre-trying to get
     * the groups list.
     *
     * @param errorCode        Error Code.
     * @param errorMessage     Error Message.
     * @param condition        Conditional filter.
     * @param limit            Number of search results.
     * @param offset           Start index of the user search.
     * @param domain           Userstore domain.
     * @param sortBy           Sorted by.
     * @param sortOrder        Sorted order.
     * @param userStoreManager Userstore manager.
     * @return True if the handling succeeded.
     * @throws UserStoreException Exception that will be thrown by relevant listener methods.
     */
    private void handlePreListGroupsFailure(String errorCode, String errorMessage, Condition condition, int limit,
                                            int offset, String domain, String sortBy, String sortOrder,
                                            UserStoreManager userStoreManager) throws UserStoreException {

        for (GroupManagementErrorEventListener listener : UMListenerServiceComponent
                .getGroupManagementErrorEventListeners()) {
            if (listener.isEnable() && listener instanceof AbstractGroupManagementErrorEventListener
                    && !listener.onPreListGroupsFailure(errorCode, errorMessage, condition, limit, offset, domain,
                    sortBy, sortOrder, userStoreManager)) {
                log.error("'handlePreListGroupsFailure' event invocation failed for listener: " +
                        listener.getClass().getName());
                return;
            }
        }
    }

    /**
     * This method is responsible for calling the relevant methods when there is a failure while post-trying to get the
     * groups list by user id.
     *
     * @param errorCode    Error Code.
     * @param errorMessage Error Message.
     * @param userId       User id.
     * @throws UserStoreException Exception that will be thrown by relevant listener methods.
     */
    private void handlePostGetGroupsListByUserIdFailure(String errorCode, String errorMessage, String userId)
            throws UserStoreException {

        for (GroupManagementErrorEventListener listener : UMListenerServiceComponent
                .getGroupManagementErrorEventListeners()) {
            if (listener.isEnable() && listener instanceof AbstractGroupManagementErrorEventListener
                    && !listener.onPostGetGroupsListByUserIdFailure(errorCode, errorMessage,
                    userId, this)) {
                log.error("'onPostGetGroupsListByUserIdFailure' event invocation failed for listener: " +
                        listener.getClass().getName());
                return;
            }
        }
    }

    /**
     * This method is responsible for calling the relevant methods when there is a failure while post-trying to get the
     * group with id.
     *
     * @param errorCode          Error Code.
     * @param errorMessage       Error Message.
     * @param groupId            Group unique id.
     * @param requiredAttributes Requested Claims.
     * @throws UserStoreException Exception that will be thrown by relevant listener methods.
     */
    private void handlePostGetGroupByIdFailure(String errorCode, String errorMessage, String groupId,
                                               List<String> requiredAttributes)
            throws UserStoreException {

        for (GroupManagementErrorEventListener listener : UMListenerServiceComponent
                .getGroupManagementErrorEventListeners()) {
            if (listener.isEnable() && listener instanceof AbstractGroupManagementErrorEventListener
                    && !listener.onPostGetGroupByIdFailure(errorCode, errorMessage, groupId,
                    requiredAttributes, this)) {
                log.error("'handlePostGetGroupWithIdFailure' event invocation failed for listener: " +
                        listener.getClass().getName());
                return;
            }
        }
    }

    /**
     * This method is responsible for calling the relevant methods when there is a failure while post-trying to get the
     * group with group name.
     *
     * @param errorCode          Error Code.
     * @param errorMessage       Error Message.
     * @param groupName          Group name.
     * @param requiredAttributes Requested Claims.
     * @throws UserStoreException Exception that will be thrown by relevant listener methods.
     */
    private void handlePostGetGroupByNameFailure(String errorCode, String errorMessage, String groupName,
                                                 List<String> requiredAttributes)
            throws UserStoreException {

        for (GroupManagementErrorEventListener listener : UMListenerServiceComponent
                .getGroupManagementErrorEventListeners()) {
            if (listener.isEnable() && listener instanceof AbstractGroupManagementErrorEventListener
                    && !listener.onPostGetGroupByNameFailure(errorCode, errorMessage, groupName,
                    requiredAttributes, this)) {
                log.error("'onPostGetGroupByNameFailure' event invocation failed for listener: " +
                        listener.getClass().getName());
                return;
            }
        }
    }

    /**
     * This method is responsible for calling the relevant methods when there is a failure while post-trying to get the
     * group id with group name.
     *
     * @param errorCode    Error Code.
     * @param errorMessage Error Message.
     * @param groupName    Group name.
     * @throws UserStoreException Exception that will be thrown by relevant listener methods.
     */
    private void handlePostGetGroupIdByNameFailure(String errorCode, String errorMessage, String groupName)
            throws UserStoreException {

        for (GroupManagementErrorEventListener listener : UMListenerServiceComponent
                .getGroupManagementErrorEventListeners()) {
            if (listener.isEnable() && listener instanceof AbstractGroupManagementErrorEventListener
                    && !listener.onPostGetGroupIdByNameFailure(errorCode, errorMessage, groupName,
                    this)) {
                log.error("'onPostGetGroupIdByNameFailure' event invocation failed for listener: " +
                        listener.getClass().getName());
                return;
            }
        }
    }

    /**
     * This method is responsible for calling the relevant methods when there is a failure while post-trying to get the
     * group name with group id.
     *
     * @param errorCode    Error Code.
     * @param errorMessage Error Message.
     * @param groupId      Group id.
     * @throws UserStoreException Exception that will be thrown by relevant listener methods.
     */
    private void handlePostGetGroupNameByIdFailure(String errorCode, String errorMessage, String groupId)
            throws UserStoreException {

        for (GroupManagementErrorEventListener listener : UMListenerServiceComponent
                .getGroupManagementErrorEventListeners()) {
            if (listener.isEnable() && listener instanceof AbstractGroupManagementErrorEventListener
                    && !listener.onPostGetGroupNameByIdFailure(errorCode, errorMessage, groupId,
                    this)) {
                log.error("'onPostGetGroupNameByIdFailure' event invocation failed for listener: " +
                        listener.getClass().getName());
                return;
            }
        }
    }

    /**
     * This method is responsible for calling the relevant methods when there is a failure while post-trying to get
     * the groups list.
     *
     * @param errorCode        Error Code.
     * @param errorMessage     Error Message.
     * @param condition        Conditional filter.
     * @param limit            Number of search results.
     * @param offset           Start index of the user search.
     * @param domain           Userstore domain.
     * @param sortBy           Sorted by.
     * @param sortOrder        Sorted order.
     * @param userStoreManager Userstore manager.
     * @return True if the handling succeeded.
     * @throws UserStoreException Exception that will be thrown by relevant listener methods.
     */
    private void handlePostListGroupsFailure(String errorCode, String errorMessage, Condition condition, int limit,
                                            int offset, String domain, String sortBy, String sortOrder,
                                            UserStoreManager userStoreManager) throws UserStoreException {

        for (GroupManagementErrorEventListener listener : UMListenerServiceComponent
                .getGroupManagementErrorEventListeners()) {
            if (listener.isEnable() && listener instanceof AbstractGroupManagementErrorEventListener
                    && !listener.onPostListGroupsFailure(errorCode, errorMessage, condition, limit, offset, domain,
                    sortBy, sortOrder, userStoreManager)) {
                log.error("'handlePostListGroupsFailure' event invocation failed for listener: " +
                        listener.getClass().getName());
                return;
            }
        }
    }

    /**
     * This method is responsible for calling the relevant methods when there is a failure while getting the
     * groups list by user id.
     *
     * @param errorCode    Error Code.
     * @param errorMessage Error Message.
     * @param userId       User id.
     * @throws UserStoreException Exception that will be thrown by relevant listener methods.
     */
    private void handleGetGroupsListByUserIdFailure(String errorCode, String errorMessage, String userId)
            throws UserStoreException {

        for (GroupManagementErrorEventListener listener : UMListenerServiceComponent
                .getGroupManagementErrorEventListeners()) {
            if (listener.isEnable() && listener instanceof AbstractGroupManagementErrorEventListener
                    && !listener.onGetGroupsListByUserIdFailure(errorCode, errorMessage,
                    userId, this)) {
                log.error("'onGetGroupsListByUserIdFailure' event invocation failed for listener: " +
                        listener.getClass().getName());
                return;
            }
        }
    }

    /**
     * This method is responsible for calling the relevant methods when there is a failure while trying to get the
     * group with id.
     *
     * @param errorCode          Error Code.
     * @param errorMessage       Error Message.
     * @param groupId            Group unique id.
     * @param requiredAttributes Requested Claims.
     * @throws UserStoreException Exception that will be thrown by relevant listener methods.
     */
    private void handleGetGroupByIdFailure(String errorCode, String errorMessage, String groupId,
                                           List<String> requiredAttributes) throws UserStoreException {

        for (GroupManagementErrorEventListener listener : UMListenerServiceComponent
                .getGroupManagementErrorEventListeners()) {
            if (listener.isEnable() && listener instanceof AbstractGroupManagementErrorEventListener
                    && !listener.onGetGroupByIdFailure(errorCode,
                    errorMessage, groupId, requiredAttributes, this)) {
                log.error("'onGetGroupWithIdFailure' event invocation failed for listener: " +
                        listener.getClass().getName());
                return;
            }
        }
    }

    /**
     * This method is responsible for calling the relevant methods when there is a failure while trying to get the
     * group with id.
     *
     * @param errorCode          Error Code.
     * @param errorMessage       Error Message.
     * @param groupName          Group name.
     * @param requiredAttributes Requested Claims.
     * @throws UserStoreException Exception that will be thrown by relevant listener methods.
     */
    private void handleGetGroupByNameFailure(String errorCode, String errorMessage, String groupName,
                                             List<String> requiredAttributes) throws UserStoreException {

        for (GroupManagementErrorEventListener listener : UMListenerServiceComponent
                .getGroupManagementErrorEventListeners()) {
            if (listener.isEnable() && listener instanceof AbstractGroupManagementErrorEventListener
                    && !listener.onGetGroupByNameFailure(errorCode,
                    errorMessage, groupName, requiredAttributes, this)) {
                log.error("'onGetGroupWithIdFailure' event invocation failed for listener: " +
                        listener.getClass().getName());
                return;
            }
        }
    }

    /**
     * This method is responsible for calling the relevant methods when there is a failure while trying to get the
     * group id with id.
     *
     * @param errorCode    Error Code.
     * @param errorMessage Error Message.
     * @param groupName    Group name.
     * @throws UserStoreException Exception that will be thrown by relevant listener methods.
     */
    private void handleGetGroupIdByNameFailure(String errorCode, String errorMessage, String groupName)
            throws UserStoreException {

        for (GroupManagementErrorEventListener listener : UMListenerServiceComponent
                .getGroupManagementErrorEventListeners()) {
            if (listener.isEnable() && listener instanceof AbstractGroupManagementErrorEventListener
                    && !listener.onGetGroupIdByNameFailure(errorCode, errorMessage, groupName, this)) {
                log.error("'onGetGroupIdByNameFailure' event invocation failed for listener: " +
                        listener.getClass().getName());
                return;
            }
        }
    }

    /**
     * This method is responsible for calling the relevant methods when there is a failure while trying to get the
     * group id with id.
     *
     * @param errorCode    Error Code.
     * @param errorMessage Error Message.
     * @param groupId      Group id.
     * @throws UserStoreException Exception that will be thrown by relevant listener methods.
     */
    private void handleGetGroupNameByIdFailure(String errorCode, String errorMessage, String groupId)
            throws UserStoreException {

        for (GroupManagementErrorEventListener listener : UMListenerServiceComponent
                .getGroupManagementErrorEventListeners()) {
            if (listener.isEnable() && listener instanceof AbstractGroupManagementErrorEventListener
                    && !listener.onGetGroupIdByNameFailure(errorCode, errorMessage, groupId, this)) {
                log.error("'handleGetGroupNameByIdFailure' event invocation failed for listener: " +
                        listener.getClass().getName());
                return;
            }
        }
    }

    /**
     * This method is responsible for calling the relevant methods when there is a failure while trying to get
     * the groups list.
     *
     * @param errorCode        Error Code.
     * @param errorMessage     Error Message.
     * @param condition        Conditional filter.
     * @param limit            Number of search results.
     * @param offset           Start index of the user search.
     * @param domain           Userstore domain.
     * @param sortBy           Sorted by.
     * @param sortOrder        Sorted order.
     * @param userStoreManager Userstore manager.
     * @return True if the handling succeeded.
     * @throws UserStoreException Exception that will be thrown by relevant listener methods.
     */
    private void handleListGroupsFailure(String errorCode, String errorMessage, Condition condition, int limit,
                                             int offset, String domain, String sortBy, String sortOrder,
                                             UserStoreManager userStoreManager) throws UserStoreException {

        for (GroupManagementErrorEventListener listener : UMListenerServiceComponent
                .getGroupManagementErrorEventListeners()) {
            if (listener.isEnable() && listener instanceof AbstractGroupManagementErrorEventListener
                    && !listener.onListGroupsFailure(errorCode, errorMessage, condition, limit, offset, domain,
                    sortBy, sortOrder, userStoreManager)) {
                log.error("'handleListGroupsFailure' event invocation failed for listener: " +
                        listener.getClass().getName());
                return;
            }
        }
    }

    /**
     * Resolve the given user list limit with the max configs defined for the userstore.
     *
     * @param givenLimit Given user list limit.
     * @return Resolved user list limit.
     */
    private int resolveUserListLimit(int givenLimit) {

        int definedMax;
        try {
            definedMax = Integer
                    .parseInt(realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_MAX_USER_LIST));
        } catch (NumberFormatException e) {
            definedMax = UserCoreConstants.MAX_USER_ROLE_LIST;
        }
        if (givenLimit < 0 || givenLimit > definedMax) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Using the userstore defined max user list limit: %s instead " +
                        "of given limit: %s ", definedMax, givenLimit));
            }
            return definedMax;
        }
        return givenLimit;
    }

    /**
     * Resolve the given group list limit with the max configs defined for the userstore.
     *
     * @param givenLimit Given user list limit.
     * @return Resolved group list limit.
     */
    private int resolveGroupListLimit(int givenLimit) {

        int definedMax;
        try {
            definedMax = Integer
                    .parseInt(realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_MAX_ROLE_LIST));
        } catch (NumberFormatException e) {
            definedMax = UserCoreConstants.MAX_USER_ROLE_LIST;
        }
        if (givenLimit < 0 || givenLimit > definedMax) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Using the userstore defined max group list limit: %s instead of given " +
                        "limit: %s ", definedMax, givenLimit));
            }
            return definedMax;
        }
        return givenLimit;
    }

    /**
     * Update group name in the UM_HYBRID_GROUP_ROLE table.
     *
     * @param groupName        The current group name.
     * @param newGroupName     The new group name.
     * @throws UserStoreException An unexpected exception has occurred.
     */
    public void updateGroupName(String groupName, String newGroupName) throws UserStoreException {

        hybridRoleManager.updateGroupName(groupName, newGroupName);
    }

    /**
     * Delete group from the UM_HYBRID_GROUP_ROLE table.
     *
     * @param groupName        The group name.
     * @throws UserStoreException An unexpected exception has occurred.
     */
    public void removeGroupRoleMappingByGroupName(String groupName) throws UserStoreException {

        hybridRoleManager.removeGroupRoleMappingByGroupName(groupName);
    }

    /**
     * The password validity timeout value is set by server configuration value from carbon.xml file.
     * If value is not present the default value of DEFAULT_PASSWORD_VALIDITY_PERIOD_VALUE is returned.
     * @return password validity timeout in hours.
     */
    private static int getDefaultPasswordValidityPeriodInHours() {

        String pwValidityTimeoutStr = ServerConfiguration.getInstance()
                .getFirstProperty(ServerConstants.DEFAULT_PASSWORD_VALIDITY_PERIOD);
        if (!StringUtils.isBlank(pwValidityTimeoutStr)) {
            return Integer.parseInt(pwValidityTimeoutStr);
        }
        return DEFAULT_PASSWORD_VALIDITY_PERIOD_VALUE;
    }

    /**
     * Getter for the user store manager holder.
     *
     * @return Map of user store managers.
     */
    public Map<String, UserStoreManager> getUserStoreManagerHolder() {

        return userStoreManagerHolder;
    }

    /**
     * Getter for the user unique ID domain resolver.
     *
     * @return UserUniqueIDDomainResolver.
     */
    public UserUniqueIDDomainResolver getUserUniqueIDDomainResolver() {

        return userUniqueIDDomainResolver;
    }

    /**
     * Retrieves the user count that belongs to a given group.
     *
     * @param groupName Name of the group.
     * @return User count of the given group.
     * @throws UserStoreException If an unexpected error occurs while accessing user store.
     */
    public int getUserCountForGroup(String groupName) throws UserStoreException {

        int count = 0;
        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class};
            Object object = callSecure("getUserCountForGroup", new Object[]{groupName}, argTypes);
            return (int) object;
        }

        // If group does not exit, just return.
        if (!isExistingRole(groupName)) {
            return count;
        }

        UserStore userStore = getUserStoreOfRoles(groupName);

        if (userStore.isRecurssive()) {
            UserStoreManager resolvedUserStoreManager = userStore.getUserStoreManager();
            if (resolvedUserStoreManager instanceof AbstractUserStoreManager) {
                return ((AbstractUserStoreManager) resolvedUserStoreManager)
                        .getUserCountForGroup(userStore.getDomainFreeName());
            }
        }

        // #################### Domain Name Free Zone Starts Here ################################

        if (userStore.isSystemStore() || userStore.isHybridRole()) {
            // If the passed group is a role and if role, group separation is not enabled,
            // call the user listing method for roles.
            if (!isRoleAndGroupSeparationEnabled()) {
                return getUserListOfRoleWithID(groupName).size();
            }
            // If the passed group is a role and if role, group separation is enabled, just return.
            return count;
        }

        if (readGroupsEnabled) {
            // If unique id feature is not enabled, we have to call the legacy methods.
            if (!isUniqueUserIdEnabledInUserStore(userStore)) {
                count += doGetUserCountOfRole(groupName);
            } else {
                count += doGetUserCountOfRoleWithID(groupName);
            }
        }

        return count;
    }

    // Default assigned as false.
    protected boolean isCircuitBreakerEnabledAndOpen() throws UserStoreException {

        return false;
    }

    /**
     * Merge all identity claim filtered usernames and non identity claim filtered usernames in an efficient manner.
     *
     * @param identityClaimFilteredUsers    The username list of all the identity claim filtered users from identity
     *                                      store.
     * @param nonIdentityClaimFilteredUsers The username list of all the non identity claim filtered users from user
     *                                      store.
     * @param offset                        The offset requested for pagination.
     * @param maxUserListCount                   The maximum user list count.
     * @return A PaginatedUserResponse object including the paginated user list and the total user count match
     * for both identity and non identity filter.
     * @throws UserStoreException
     */
    private PaginatedUserResponse mergeIdentityNonIdentityClaimFilteredUsers(List<String> identityClaimFilteredUsers,
                                                                             List<String> nonIdentityClaimFilteredUsers,
                                                                             int offset, int maxUserListCount)
            throws UserStoreException {

        List<String> mergeUserList;
        PaginatedUserResponse paginatedUserResponse = new PaginatedUserResponse();

        // Joining two lists based on the minimum size list will give the higher performance.
        if (identityClaimFilteredUsers.size() < nonIdentityClaimFilteredUsers.size()) {
            Set<String> userHashSet = new HashSet<>(nonIdentityClaimFilteredUsers);
            mergeUserList = identityClaimFilteredUsers.stream()
                    .filter(userHashSet::contains)
                    .collect(Collectors.toList());
        } else {
            Set<String> userHashSet = new HashSet<>(identityClaimFilteredUsers);
            mergeUserList = nonIdentityClaimFilteredUsers.stream()
                    .filter(userHashSet::contains)
                    .collect(Collectors.toList());
        }

        /* The total user count which matches for both identity and non identity claim filter. Storing that information
            to be used for downstream tasks without re calculating as it is costly operation. */
        paginatedUserResponse.setTotalResults(mergeUserList.size());

        // Pagination
        if (offset <= 0) {
            offset = 0;
        } else {
            offset = offset - 1;
        }

        int paginationLimit;
        if (offset <= 0) {
            paginationLimit = maxUserListCount;
        } else {
            paginationLimit = offset + maxUserListCount;
        }

        if (offset > mergeUserList.size()) {
            return new PaginatedUserResponse();
        }
        if (mergeUserList.size() < paginationLimit) {
            mergeUserList = mergeUserList.subList(offset, mergeUserList.size());
        } else {
            mergeUserList = mergeUserList.subList(offset, paginationLimit);
        }
        paginatedUserResponse.setFilteredUsers(getUserListByUsernames(mergeUserList));
        return paginatedUserResponse;
    }

    private List<User> getUserListByUsernames(List<String> usernames) throws UserStoreException {

        List<User> userList = new ArrayList<>();
        for (String username : usernames) {
            User user = getUser(getUserIDFromUserName(username), username);
            user.setUserStoreDomain(UserCoreUtil.extractDomainFromName(username));
            userList.add(user);
            addToUserIDCache(user.getUserID(), user.getUsername(), user.getUserStoreDomain());
            addToUserNameCache(user.getUserID(), user.getUsername(), user.getUserStoreDomain());
        }
        return userList;
    }
}
