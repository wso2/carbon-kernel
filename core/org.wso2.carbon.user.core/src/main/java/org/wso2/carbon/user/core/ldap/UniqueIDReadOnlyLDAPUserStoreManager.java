/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.user.core.ldap;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.caching.impl.CachingConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.Properties;
import org.wso2.carbon.user.api.Property;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreConfigConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.common.AuthenticationResult;
import org.wso2.carbon.user.core.common.FailureReason;
import org.wso2.carbon.user.core.common.LoginIdentifier;
import org.wso2.carbon.user.core.common.PaginatedSearchResult;
import org.wso2.carbon.user.core.common.RoleContext;
import org.wso2.carbon.user.core.common.UniqueIDPaginatedSearchResult;
import org.wso2.carbon.user.core.common.User;
import org.wso2.carbon.user.core.internal.UserStoreMgtDSComponent;
import org.wso2.carbon.user.core.model.Condition;
import org.wso2.carbon.user.core.model.ExpressionAttribute;
import org.wso2.carbon.user.core.model.ExpressionCondition;
import org.wso2.carbon.user.core.model.ExpressionOperation;
import org.wso2.carbon.user.core.profile.ProfileConfigurationManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.JNDIUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.cache.Cache;
import javax.cache.CacheBuilder;
import javax.cache.CacheConfiguration;
import javax.cache.Caching;
import javax.naming.CompositeName;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.PartialResultException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.SortControl;

import static org.wso2.carbon.user.core.ldap.ActiveDirectoryUserStoreConstants.TRANSFORM_OBJECTGUID_TO_UUID;

public class UniqueIDReadOnlyLDAPUserStoreManager extends ReadOnlyLDAPUserStoreManager {

    private static Log logger = LogFactory.getLog(UniqueIDReadOnlyLDAPUserStoreManager.class);
    public static final String MEMBER_UID = "memberUid";
    protected static final String OBJECT_GUID = "objectGUID";
    protected static final String MEMBERSHIP_ATTRIBUTE_RANGE = "MembershipAttributeRange";
    protected static final String MEMBERSHIP_ATTRIBUTE_RANGE_DISPLAY_NAME = "Membership Attribute Range";
    private static final String USER_CACHE_NAME_PREFIX = CachingConstants.LOCAL_CACHE_PREFIX + "UserCache-";
    private static final String USER_CACHE_MANAGER = "UserCacheManager";
    private static Log log = LogFactory.getLog(UniqueIDReadOnlyLDAPUserStoreManager.class);

    private static final String MULTI_ATTRIBUTE_SEPARATOR_DESCRIPTION =
            "This is the separator for multiple claim " + "values";
    private static final String MULTI_ATTRIBUTE_SEPARATOR = "MultiAttributeSeparator";
    private static final ArrayList<Property> UNIQUE_ID_RO_LDAP_UM_ADVANCED_PROPERTIES = new ArrayList<>();
    private static final String PROPERTY_REFERRAL_IGNORE = "ignore";
    private static final String LDAPConnectionTimeout = "LDAPConnectionTimeout";
    private static final String LDAPConnectionTimeoutDescription = "LDAP Connection Timeout";
    private static final String readTimeout = "ReadTimeout";
    private static final String readTimeoutDescription =
            "Configure this to define the read timeout for LDAP " + "operations";
    private static final String RETRY_ATTEMPTS = "RetryAttempts";
    private static final String LDAPBinaryAttributesDescription =
            "Configure this to define the LDAP binary attributes " + "seperated by a space. Ex:mpegVideo mySpecialKey";
    protected static final String USER_CACHE_EXPIRY_TIME_ATTRIBUTE_NAME = "User Cache Expiry milliseconds";
    protected static final String USER_DN_CACHE_ENABLED_ATTRIBUTE_NAME = "Enable User DN Cache";
    protected static final String USER_CACHE_EXPIRY_TIME_ATTRIBUTE_DESCRIPTION =
            "Configure the user cache expiry in milliseconds. "
                    + "Values  {0: expire immediately, -1: never expire, '': i.e. empty, system default}.";
    protected static final String USER_DN_CACHE_ENABLED_ATTRIBUTE_DESCRIPTION =
            "Enables the user cache. Default true," + " Unless set to false. Empty value is interpreted as true.";
    //Authenticating to LDAP via Anonymous Bind
    private static final String USE_ANONYMOUS_BIND = "AnonymousBind";
    protected static final int MEMBERSHIP_ATTRIBUTE_RANGE_VALUE = 0;
    private static final int MAX_ITEM_LIMIT_UNLIMITED = -1;

    private String cacheExpiryTimeAttribute = ""; //Default: expire with default system wide cache expiry
    private long userDnCacheExpiryTime = 0; //Default: No cache
    private CacheBuilder userDnCacheBuilder = null; //Use cache manager if not null to get cache
    private String userDnCacheName;
    private boolean userDnCacheEnabled = true;

    static {
        setAdvancedProperties();
    }

    public UniqueIDReadOnlyLDAPUserStoreManager() {
    }

    public UniqueIDReadOnlyLDAPUserStoreManager(RealmConfiguration realmConfig, Map<String, Object> properties,
            ClaimManager claimManager, ProfileConfigurationManager profileManager, UserRealm realm, Integer tenantId)
            throws UserStoreException {

        super(realmConfig, properties, claimManager, profileManager, realm, tenantId, false);
    }

    /**
     * Constructor with Hybrid Role Manager
     *
     * @param realmConfig
     * @param properties
     * @param claimManager
     * @param profileManager
     * @param realm
     * @param tenantId
     * @throws UserStoreException
     */
    public UniqueIDReadOnlyLDAPUserStoreManager(RealmConfiguration realmConfig, Map<String, Object> properties,
            ClaimManager claimManager, ProfileConfigurationManager profileManager, UserRealm realm, Integer tenantId,
            boolean skipInitData) throws UserStoreException {

        super(realmConfig, properties, claimManager, profileManager, realm, tenantId, skipInitData);
    }

    /**
     * This operates in the pure read-only mode without a connection to a
     * database. No handling of
     * Internal roles.
     */
    public UniqueIDReadOnlyLDAPUserStoreManager(RealmConfiguration realmConfig, ClaimManager claimManager,
            ProfileConfigurationManager profileManager) throws UserStoreException {

        super(realmConfig, claimManager, profileManager);
    }

    @Override
    protected void checkRequiredUserStoreConfigurations() throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("Checking LDAP configurations ");
        }

        String connectionURL = realmConfig.getUserStoreProperty(LDAPConstants.CONNECTION_URL);
        String DNSURL = realmConfig.getUserStoreProperty(LDAPConstants.DNS_URL);
        String AnonymousBind = realmConfig.getUserStoreProperty(USE_ANONYMOUS_BIND);

        if ((connectionURL == null || connectionURL.trim().length() == 0) && ((DNSURL == null
                || DNSURL.trim().length() == 0))) {
            throw new UserStoreException("Required ConnectionURL property is not set at the LDAP configurations");
        }
        if (!Boolean.parseBoolean(AnonymousBind)) {
            String connectionName = realmConfig.getUserStoreProperty(LDAPConstants.CONNECTION_NAME);
            if (StringUtils.isEmpty(connectionName)) {
                throw new UserStoreException("Required ConnectionNme property is not set at the LDAP configurations");
            }
            String connectionPassword = realmConfig.getUserStoreProperty(LDAPConstants.CONNECTION_PASSWORD);
            if (StringUtils.isEmpty(connectionPassword)) {
                throw new UserStoreException(
                        "Required ConnectionPassword property is not set at the LDAP configurations");
            }
        }
        userSearchBase = realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);
        if (userSearchBase == null || userSearchBase.trim().length() == 0) {
            throw new UserStoreException("Required UserSearchBase property is not set at the LDAP configurations");
        }
        String usernameListFilter = realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_LIST_FILTER);
        if (usernameListFilter == null || usernameListFilter.trim().length() == 0) {
            throw new UserStoreException("Required UserNameListFilter property is not set at the LDAP configurations");
        }

        String usernameSearchFilter = realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_SEARCH_FILTER);
        if (usernameSearchFilter == null || usernameSearchFilter.trim().length() == 0) {
            throw new UserStoreException(
                    "Required UserNameSearchFilter property is not set at the LDAP configurations");
        }

        String usernameAttribute = realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE);
        if (usernameAttribute == null || usernameAttribute.trim().length() == 0) {
            throw new UserStoreException("Required UserNameAttribute property is not set at the LDAP configurations");
        }

        String userIDAttribute = realmConfig.getUserStoreProperty(LDAPConstants.USER_ID_ATTRIBUTE);
        if (userIDAttribute == null || userIDAttribute.trim().length() == 0) {
            throw new UserStoreException("Required userIDAttribute property is not set at the LDAP configurations");
        }

        writeGroupsEnabled = false;

        // Groups properties
        if (realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.READ_GROUPS_ENABLED) != null) {
            readGroupsEnabled = Boolean.parseBoolean(realmConfig.
                    getUserStoreProperty(UserCoreConstants.RealmConfig.READ_GROUPS_ENABLED));
        }

        if (log.isDebugEnabled()) {
            if (readGroupsEnabled) {
                log.debug("ReadGroups is enabled for " + getMyDomainName());
            } else {
                log.debug("ReadGroups is disabled for " + getMyDomainName());
            }
        }

        if (readGroupsEnabled) {
            groupSearchBase = realmConfig.getUserStoreProperty(LDAPConstants.GROUP_SEARCH_BASE);
            if (groupSearchBase == null || groupSearchBase.trim().length() == 0) {
                throw new UserStoreException("Required GroupSearchBase property is not set at the LDAP configurations");
            }
            String groupNameListFilter = realmConfig.getUserStoreProperty(LDAPConstants.GROUP_NAME_LIST_FILTER);
            if (groupNameListFilter == null || groupNameListFilter.trim().length() == 0) {
                throw new UserStoreException(
                        "Required GroupNameListFilter property is not set at the LDAP configurations");
            }

            String groupNameSearchFilter = realmConfig.getUserStoreProperty(LDAPConstants.ROLE_NAME_FILTER);
            if (groupNameSearchFilter == null || groupNameSearchFilter.trim().length() == 0) {
                throw new UserStoreException(
                        "Required GroupNameSearchFilter property is not set at the LDAP configurations");
            }

            String groupNameAttribute = realmConfig.getUserStoreProperty(LDAPConstants.GROUP_NAME_ATTRIBUTE);
            if (groupNameAttribute == null || groupNameAttribute.trim().length() == 0) {
                throw new UserStoreException(
                        "Required GroupNameAttribute property is not set at the LDAP configurations");
            }
            String memebershipAttribute = realmConfig.getUserStoreProperty(LDAPConstants.MEMBERSHIP_ATTRIBUTE);
            if (memebershipAttribute == null || memebershipAttribute.trim().length() == 0) {
                throw new UserStoreException(
                        "Required MembershipAttribute property is not set at the LDAP configurations");
            }
        }

        // User DN cache properties
        cacheExpiryTimeAttribute = realmConfig.getUserStoreProperty(LDAPConstants.USER_CACHE_EXPIRY_MILLISECONDS);
        String userDnCacheEnabledAttribute = realmConfig.getUserStoreProperty(LDAPConstants.USER_DN_CACHE_ENABLED);
        if (StringUtils.isNotEmpty(userDnCacheEnabledAttribute)) {
            userDnCacheEnabled = Boolean.parseBoolean(userDnCacheEnabledAttribute);
        }
    }

    @Override
    public AuthenticationResult doAuthenticateWithID(String preferredUserNameProperty, String preferredUserNameValue,
            Object credential, String profileName) throws UserStoreException {

        AuthenticationResult authenticationResult;
        if (!validateForWildCardCharacters(preferredUserNameValue)) {
            String reason = "preferredUserNameValue is not valid. It contains LDAP special character/characters: "
                    + preferredUserNameValue;
            return handleAuthenticationFailure(reason);
        }
        User user;
        String[] users;
        String userPropertyName =
                realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE);
        if (preferredUserNameProperty.equals(userPropertyName)) {
            users = new String[] { preferredUserNameValue };
        } else {
            users = super
                    .getUserListFromProperties(preferredUserNameProperty, preferredUserNameValue, profileName);
        }

        if (ArrayUtils.isEmpty(users)) {
            String reason =
                    "Invalid scenario. No users found for the given username property: " + preferredUserNameValue
                            + " and value: " + preferredUserNameValue;
            return handleAuthenticationFailure(reason);

        } else if (users.length > 1) {
            String reason =
                    "Invalid scenario. Multiple users found for the given username property: " + preferredUserNameValue
                            + " and value: " + preferredUserNameValue;
            return handleAuthenticationFailure(reason);
        }

        if (super.doAuthenticate(users[0], credential)) {
            String userName = users[0];
            String userID = getUserIDFromUserName(userName);
            user = getUser(userID, userName);
            user.setPreferredUsername(preferredUserNameValue);
            authenticationResult = new AuthenticationResult(AuthenticationResult.AuthenticationStatus.SUCCESS);
            authenticationResult.setAuthenticatedUser(user);
            return authenticationResult;

        } else {
            String reason =
                    "Authentication failed for the given username property: " + preferredUserNameValue + " and value: "
                            + preferredUserNameValue;
            return handleAuthenticationFailure(reason);
        }
    }

    @Override
    public AuthenticationResult doAuthenticateWithID(List<LoginIdentifier> loginIdentifiers, Object credential)
            throws UserStoreException {

        AuthenticationResult authenticationResult;
        User user;
        List<String> users = doGetUserListFromProperties(loginIdentifiers);

        if (users.isEmpty()) {
            String reason = "Invalid scenario. No users found for the given username properties";
            return handleAuthenticationFailure(reason);

        } else if (users.size() > 1) {
            String reason = "Invalid scenario. Multiple users found for the given username properties";
            return handleAuthenticationFailure(reason);
        }

        if (super.doAuthenticate(users.get(0), credential)) {
            String userName = users.get(0);
            String userID = getUserIDFromUserName(userName);
            user = getUser(userID, userName);
            authenticationResult = new AuthenticationResult(AuthenticationResult.AuthenticationStatus.SUCCESS);
            authenticationResult.setAuthenticatedUser(user);
            return authenticationResult;

        } else {
            String reason = "Authentication failed for the given username properties";
            return handleAuthenticationFailure(reason);
        }
    }

    protected List<String> doGetUserListFromProperties(List<LoginIdentifier> loginIdentifiers)
            throws UserStoreException {

        boolean debug = log.isDebugEnabled();
        String userAttributeSeparator = ",";
        String serviceNameAttribute = "sn";
        List<String> results = new ArrayList<>();
        String searchFilter;
        String userPropertyName = realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE);
        searchFilter = getSearchFilter(loginIdentifiers);

        DirContext dirContext = this.connectionSource.getContext();
        NamingEnumeration<?> answer = null;
        NamingEnumeration<?> attrs = null;

        if (debug) {
            log.debug("Listing users with SearchFilter: " + searchFilter);
        }
        String[] returnedAttributes = new String[]{userPropertyName, serviceNameAttribute};
        try {
            String searchBases = realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);
            String[] searchBaseArray = searchBases.split("#");

            for (String searchBase : searchBaseArray) {
                answer = this.searchUsersForASearchBase(searchFilter, returnedAttributes, dirContext,
                        searchBase, MAX_ITEM_LIMIT_UNLIMITED);
                if (answer.hasMore()) {
                    break;
                }
            }

            while (answer != null && answer.hasMoreElements()) {
                SearchResult sr = (SearchResult) answer.next();
                Attributes attributes = sr.getAttributes();
                if (attributes != null) {
                    Attribute attribute = attributes.get(userPropertyName);
                    if (attribute != null) {
                        StringBuffer attrBuffer = new StringBuffer();
                        for (attrs = attribute.getAll(); attrs.hasMore(); ) {
                            String attr = (String) attrs.next();
                            if (attr != null && attr.trim().length() > 0) {

                                String attrSeparator = realmConfig.getUserStoreProperty(MULTI_ATTRIBUTE_SEPARATOR);
                                if (attrSeparator != null && !attrSeparator.trim().isEmpty()) {
                                    userAttributeSeparator = attrSeparator;
                                }
                                attrBuffer.append(attr + userAttributeSeparator);
                                if (debug) {
                                    log.debug(userPropertyName + " : " + attr);
                                }
                            }
                        }
                        String propertyValue = attrBuffer.toString();
                        Attribute serviceNameObject = attributes.get(serviceNameAttribute);
                        String serviceNameAttributeValue = null;
                        if (serviceNameObject != null) {
                            serviceNameAttributeValue = (String) serviceNameObject.get();
                        }
                        // Length needs to be more than userAttributeSeparator.length() for a valid
                        // attribute, since we
                        // attach userAttributeSeparator.
                        if (propertyValue != null && propertyValue.trim().length() > userAttributeSeparator.length()) {
                            if (LDAPConstants.SERVER_PRINCIPAL_ATTRIBUTE_VALUE.equals(serviceNameAttributeValue)) {
                                continue;
                            }
                            propertyValue = propertyValue
                                    .substring(0, propertyValue.length() - userAttributeSeparator.length());
                            results.add(propertyValue);
                        }
                    }
                }
            }
        } catch (PartialResultException e) {
            // can be due to referrals in AD. so just ignore error
            String errorMessage =
                    "Error occurred while getting user list for SearchFilter : " + searchFilter;
            if (isIgnorePartialResultException()) {
                if (log.isDebugEnabled()) {
                    log.debug(errorMessage, e);
                }
            } else {
                throw new UserStoreException(errorMessage, e);
            }
        } catch (NamingException e) {
            String errorMessage = "Error occurred while getting user list with SearchFilter: " + searchFilter;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            // close the naming enumeration and free up resources
            JNDIUtil.closeNamingEnumeration(attrs);
            JNDIUtil.closeNamingEnumeration(answer);
            // close directory context
            JNDIUtil.closeContext(dirContext);
        }

        if (debug) {
            for (String result : results) {
                log.debug("result: " + result);
            }
        }
        return results;
    }

    private String getSearchFilter(List<LoginIdentifier> loginIdentifiers) {

        String userNameListFilter = realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_LIST_FILTER);
        StringBuilder searchFilter = new StringBuilder("(&" + userNameListFilter);

        for (LoginIdentifier loginIdentifier : loginIdentifiers) {

            String property = loginIdentifier.getLoginKey();
            String value = loginIdentifier.getLoginValue();
            if (OBJECT_GUID.equalsIgnoreCase(property)) {
                String transformObjectGuidToUuidProperty = realmConfig
                        .getUserStoreProperty(TRANSFORM_OBJECTGUID_TO_UUID);

                boolean transformObjectGuidToUuid = StringUtils.isEmpty(transformObjectGuidToUuidProperty) || Boolean
                        .parseBoolean(transformObjectGuidToUuidProperty);

                String convertedValue;
                if (transformObjectGuidToUuid) {
                    convertedValue = transformUUIDToObjectGUID(value);
                } else {
                    byte[] bytes = Base64.decodeBase64(value.getBytes());
                    convertedValue = convertBytesToHexString(bytes);
                }
                searchFilter.append("(").append(property).append("=").append(convertedValue).append(")");
            } else {
                searchFilter.append("(").append(property).append("=")
                        .append(escapeSpecialCharactersForFilterWithStarAsRegex(value)).append(")");
            }
        }
        searchFilter.append(")");
        return searchFilter.toString();
    }

    @Override
    public AuthenticationResult doAuthenticateWithID(String userID, Object credential) throws UserStoreException {

        AuthenticationResult authenticationResult;
        if (!validateForWildCardCharacters(userID)) {
            String reason =
                    "preferredUserNameValue is not valid. It contains LDAP special character/characters: " + userID;
            return handleAuthenticationFailure(reason);
        }
        User user;
        String userIDAttribute = realmConfig.getUserStoreProperty(LDAPConstants.USER_ID_ATTRIBUTE);
        String[] users = super.getUserListFromProperties(userIDAttribute, userID, null);

        if (ArrayUtils.isEmpty(users)) {
            String reason = "Invalid scenario. No users found for the given userID: " + userID;
            return handleAuthenticationFailure(reason);

        } else if (users.length > 1) {
            String reason = "Invalid scenario. Multiple users found for the given userID: " + userID;
            return handleAuthenticationFailure(reason);
        }

        if (super.doAuthenticate(users[0], credential)) {
            String userName = users[0];
            user = getUser(userID, userName);
            authenticationResult = new AuthenticationResult(AuthenticationResult.AuthenticationStatus.SUCCESS);
            authenticationResult.setAuthenticatedUser(user);
            return authenticationResult;
        } else {
            String reason = "Authentication failed for the given userID: " + userID;
            return handleAuthenticationFailure(reason);
        }
    }

    private AuthenticationResult handleAuthenticationFailure(String reason) {

        AuthenticationResult authenticationResult;
        if (log.isDebugEnabled()) {
            log.debug(reason);
        }
        authenticationResult = new AuthenticationResult(AuthenticationResult.AuthenticationStatus.FAIL);
        authenticationResult.setFailureReason(new FailureReason(reason));
        return authenticationResult;
    }

    private boolean validateForWildCardCharacters(String preferredUserNameValue) {

        String[] ldapSpecialCharacters = { "*", "<", ">", "~", "!", ")", "(" };
        if (StringUtils.isNotEmpty(preferredUserNameValue)) {
            for (String ldapSpecialCharacter : ldapSpecialCharacters) {
                if (preferredUserNameValue.contains(ldapSpecialCharacter)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean doAuthenticate(String userName, Object credential) throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
    }

    @Override
    protected String doGetUserIDFromUserNameWithID(String userName) throws UserStoreException {

        String userNameProperty = realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE);
        return getUserIDFromProperty(userNameProperty, userName);
    }

    private String getUserIDFromProperty(String property, String claimValue) throws UserStoreException {

        try {
            List<String> userIds = this.doGetUserListFromPropertiesWithID(property, claimValue, null);
            if (userIds.isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "No UserID found for the property: " + property + ", value: " + claimValue + ", in domain:"
                                    + " " + getMyDomainName());
                }
                return null;
            } else if (userIds.size() > 1) {
                throw new UserStoreException(
                        "Invalid scenario. Multiple users cannot be found for the given value: " + claimValue
                                + "of the " + "property: " + property);
            } else {
                // username can have only one userId. Take the first element.
                return userIds.get(0);
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException(
                    "Error occurred while retrieving the userId of domain : " + getMyDomainName() + " and " + "property"
                            + property + " value: " + claimValue, e);
        }
    }

    @Override
    public String getUserIDFromProperties(String claimURI, String claimValue, String profileName)
            throws UserStoreException {

        try {
            String property = claimManager.getAttributeName(getMyDomainName(), claimURI);
            if (property == null) {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "Could not find the matching property for claim URI: " + claimURI + " in user " + "domain: "
                                    + getMyDomainName());
                }
                return null;
            }
            return getUserIDFromProperty(property, claimValue);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException(
                    "Error occurred while retrieving the userId of domain : " + getMyDomainName() + " and " + "claim"
                            + claimURI + " value: " + claimValue, e);
        }
    }

    @Override
    public String doGetUserNameFromUserIDWithID(String userID) throws UserStoreException {

        String userIDAttribute = realmConfig.getUserStoreProperty(LDAPConstants.USER_ID_ATTRIBUTE);
        String[] userNames = super.getUserListFromProperties(userIDAttribute, userID, null);
        if (userNames.length > 1) {
            throw new UserStoreException(
                    "Invalid scenario. Multiple users cannot be found for the given userID: " + userID);
        }

        if (userNames.length == 0) {
            if (log.isDebugEnabled()) {
                log.debug("No user ID found for the given userID: " + userID);
            }
            return null;
        }

        return userNames[0];
    }

    @Override
    public String[] getProfileNamesWithID(String userID) throws UserStoreException {

        return new String[] { UserCoreConstants.DEFAULT_PROFILE };
    }

    @Override
    protected Map<String, String> getUserPropertyValuesWithID(String userID, String[] propertyNames, String profileName)
            throws UserStoreException {

        return super.getUserPropertyValues(doGetUserNameFromUserID(userID), propertyNames, profileName);
    }

    @Override
    public boolean doCheckExistingUserNameWithIDImpl(String userName) throws UserStoreException {

        return super.doCheckExistingUser(userName);
    }

    @Override
    public boolean doCheckExistingUserWithID(String userID) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("Searching for userID " + userID);
        }
        if (userID == null) {
            return false;
        }
        return doGetUserNameFromUserID(userID) != null;
    }

    @Override
    public String[] doListUsers(String filter, int maxItemLimit) throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
    }

    @Override
    public List<User> doListUsersWithID(String filter, int maxItemLimit) throws UserStoreException {

        List<User> userNames = new ArrayList<>();
        if (maxItemLimit == 0) {
            return userNames;
        }

        int givenMax = UserCoreConstants.MAX_USER_ROLE_LIST;
        int searchTime = UserCoreConstants.MAX_SEARCH_TIME;

        try {
            givenMax = Integer
                    .parseInt(realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_MAX_USER_LIST));
        } catch (Exception e) {
            givenMax = UserCoreConstants.MAX_USER_ROLE_LIST;
        }

        try {
            searchTime = Integer
                    .parseInt(realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_MAX_SEARCH_TIME));
        } catch (NumberFormatException e) {
            searchTime = UserCoreConstants.MAX_SEARCH_TIME;
        }

        if (maxItemLimit < 0 || maxItemLimit > givenMax) {
            maxItemLimit = givenMax;
        }

        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchCtls.setCountLimit(maxItemLimit);
        searchCtls.setTimeLimit(searchTime);

        if (filter.contains("?") || filter.contains("**")) {
            throw new UserStoreException(
                    "Invalid character sequence entered for user serch. Please enter valid sequence.");
        }

        StringBuilder searchFilter = new StringBuilder(
                realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_LIST_FILTER));
        String searchBases = realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);

        String userNameProperty = realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE);
        String userIDProperty = realmConfig.getUserStoreProperty(LDAPConstants.USER_ID_ATTRIBUTE);

        String serviceNameAttribute = "sn";

        StringBuilder finalFilter = new StringBuilder();

        // read the display name attribute - if provided
        String displayNameAttribute = realmConfig.getUserStoreProperty(LDAPConstants.DISPLAY_NAME_ATTRIBUTE);

        String[] returnedAttributes;

        if (StringUtils.isNotEmpty(displayNameAttribute)) {
            returnedAttributes = new String[] {
                    userNameProperty, serviceNameAttribute, displayNameAttribute, userIDProperty
            };
            finalFilter.append("(&").append(searchFilter).append("(").append(displayNameAttribute).append("=")
                    .append(escapeSpecialCharactersForFilterWithStarAsRegex(filter)).append("))");
        } else {
            returnedAttributes = new String[] { userNameProperty, serviceNameAttribute, userIDProperty };
            finalFilter.append("(&").append(searchFilter).append("(").append(userNameProperty).append("=")
                    .append(escapeSpecialCharactersForFilterWithStarAsRegex(filter)).append("))");
        }

        if (log.isDebugEnabled()) {
            log.debug("Listing users. SearchBase: " + searchBases + " Constructed-Filter: " + finalFilter.toString());
            log.debug("Search controls. Max Limit: " + maxItemLimit + " Max Time: " + searchTime);
        }

        searchCtls.setReturningAttributes(returnedAttributes);
        DirContext dirContext = null;
        NamingEnumeration<SearchResult> answer = null;
        List<User> list = new ArrayList<>();

        try {
            dirContext = connectionSource.getContext();
            // handle multiple search bases
            String[] searchBaseArray = searchBases.split("#");

            for (String searchBase : searchBaseArray) {

                answer = dirContext.search(escapeDNForSearch(searchBase), finalFilter.toString(), searchCtls);

                while (answer.hasMoreElements()) {
                    SearchResult sr = answer.next();
                    if (sr.getAttributes() != null) {
                        log.debug("Result found ..");
                        Attribute userName = sr.getAttributes().get(userNameProperty);
                        Attribute userID = sr.getAttributes().get(userIDProperty);

                        /*
                         * If this is a service principle, just ignore and
                         * iterate rest of the array. The entity is a service if
                         * value of surname is Service
                         */
                        Attribute attrSurname = sr.getAttributes().get(serviceNameAttribute);

                        if (attrSurname != null) {
                            if (log.isDebugEnabled()) {
                                log.debug(serviceNameAttribute + " : " + attrSurname);
                            }
                            String serviceName = (String) attrSurname.get();
                            if (serviceName != null && serviceName
                                    .equals(LDAPConstants.SERVER_PRINCIPAL_ATTRIBUTE_VALUE)) {
                                continue;
                            }
                        }

                        /*
                         * if display name is provided, read that attribute
                         */
                        Attribute displayName = null;
                        if (StringUtils.isNotEmpty(displayNameAttribute)) {
                            displayName = sr.getAttributes().get(displayNameAttribute);
                            if (log.isDebugEnabled()) {
                                log.debug(displayNameAttribute + " : " + displayName);
                            }
                        }

                        String name = null;
                        String display = null;
                        String id = null;
                        String domain = null;
                        if (userName != null) {
                            name = resolveLdapAttributeValue(userName.get());
                            if (displayName != null) {
                                display = resolveLdapAttributeValue(displayName.get());
                            }
                            domain = this.getRealmConfiguration()
                                    .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
                        }
                        if (userID != null) {
                            id = resolveLdapAttributeValue(userID.get());
                        }

                        User user = getUser(id, name);
                        user.setDisplayName(display);
                        user.setUserStoreDomain(domain);
                        user.setTenantDomain(getTenantDomain(tenantId));
                        list.add(user);
                    }
                }
            }
            userNames = list;
            if (log.isDebugEnabled()) {
                for (User userName : userNames) {
                    log.debug("result: " + userName.getUsername());
                }
            }
        } catch (PartialResultException e) {
            // can be due to referrals in AD. so just ignore error
            String errorMessage =
                    "Error occurred while getting user list for filter : " + filter + "max limit : " + maxItemLimit;
            if (isIgnorePartialResultException()) {
                if (log.isDebugEnabled()) {
                    log.debug(errorMessage, e);
                }
            } else {
                throw new UserStoreException(errorMessage, e);
            }
        } catch (NamingException e) {
            String errorMessage =
                    "Error occurred while getting user list for filter : " + filter + "max limit : " + maxItemLimit;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            JNDIUtil.closeNamingEnumeration(answer);
            JNDIUtil.closeContext(dirContext);
        }
        return userNames;
    }

    @Override
    public String[] doGetUserListOfRole(String roleName, String filter) throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
    }

    @Override
    public List<User> doGetUserListOfRoleWithID(String roleName, String filter) throws UserStoreException {

        RoleContext roleContext = createRoleContext(roleName);
        return getUserListOfLDAPRoleWithID(roleContext, filter);
    }

    /**
     * Resolves the value of a LDAP attribute to a string based on the data type.
     *
     * @param attributeObject Attribute Value.
     * @return Resolved string value.
     */
    protected String resolveLdapAttributeValue(Object attributeObject) {

        String resolvedStringValue = null;
        if (attributeObject instanceof String) {
            resolvedStringValue = (String) attributeObject;
        } else if (attributeObject instanceof byte[]) {
            // Return canonical representation of UUIDs or base64 encoded string of other binary data.
            final byte[] bytes = (byte[]) attributeObject;
            if (bytes.length == 16) {
                 /*
                 ObjectGUID byte order is not big-endian.
                 https://msdn.microsoft.com/en-us/library/aa373931%28v=vs.85%29.aspx
                 https://community.oracle.com/thread/1157698
                  */
                final ByteBuffer byteBuffer = ByteBuffer.wrap(swapBytes(bytes));
                resolvedStringValue = new UUID(byteBuffer.getLong(), byteBuffer.getLong()).toString();
            } else {
                resolvedStringValue = new String(Base64.encodeBase64((byte[]) attributeObject));
            }
        }

        return resolvedStringValue;
    }

    protected List<User> getUserListOfLDAPRoleWithID(RoleContext context, String filter) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("Getting user list of role: " + context.getRoleName() + " with filter: " + filter);
        }

        List<User> userList = new ArrayList<>();
        int givenMax = UserCoreConstants.MAX_USER_ROLE_LIST;
        int searchTime = UserCoreConstants.MAX_SEARCH_TIME;

        try {
            givenMax = Integer
                    .parseInt(realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_MAX_USER_LIST));
        } catch (Exception e) {
            givenMax = UserCoreConstants.MAX_USER_ROLE_LIST;
        }

        try {
            searchTime = Integer
                    .parseInt(realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_MAX_SEARCH_TIME));
        } catch (Exception e) {
            searchTime = UserCoreConstants.MAX_SEARCH_TIME;
        }

        DirContext dirContext = null;
        NamingEnumeration<SearchResult> answer = null;
        try {
            SearchControls searchCtls = new SearchControls();
            searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            searchCtls.setTimeLimit(searchTime);
            searchCtls.setCountLimit(givenMax);

            String searchFilter = ((LDAPRoleContext) context).getListFilter();
            String roleNameProperty = ((LDAPRoleContext) context).getRoleNameProperty();
            searchFilter = "(&" + searchFilter + "(" + roleNameProperty + "=" + escapeSpecialCharactersForFilter(
                    context.getRoleName()) + "))";

            // Iterate the by intervals of range defined (if range > 0) and get the complete list of users
            int offset = 0;
            int lastRecord = 0;
            int attributeValuesRange = 0;
            boolean isEndOfAttributes = false;

            String roleListRange = realmConfig.getUserStoreProperty(MEMBERSHIP_ATTRIBUTE_RANGE);
            if (StringUtils.isNotEmpty(roleListRange)) {
                attributeValuesRange = Integer.parseInt(roleListRange);
            }
            if (attributeValuesRange > 0) {
                lastRecord = attributeValuesRange - 1;
            }
            String membershipProperty = realmConfig.getUserStoreProperty(LDAPConstants.MEMBERSHIP_ATTRIBUTE);
            List<String> userDNList = new ArrayList<>();
            String rangedMembershipProperty = membershipProperty;

            while (!isEndOfAttributes) {
                if (lastRecord > 0 && StringUtils.isNotEmpty(membershipProperty)) {
                    rangedMembershipProperty =
                            membershipProperty + String.format(";range=%1$d-%2$d", offset, lastRecord);
                }
                String[] returnedAtts = { rangedMembershipProperty };
                searchCtls.setReturningAttributes(returnedAtts);

                SearchResult sr = null;
                dirContext = connectionSource.getContext();

                // with DN patterns
                if (!((LDAPRoleContext) context).getRoleDNPatterns().isEmpty()) {
                    for (String pattern : ((LDAPRoleContext) context).getRoleDNPatterns()) {
                        if (log.isDebugEnabled()) {
                            log.debug("Using pattern: " + pattern);
                        }
                        pattern = MessageFormat
                                .format(pattern.trim(), escapeSpecialCharactersForDN(context.getRoleName()));
                        try {
                            answer = dirContext.search(escapeDNForSearch(pattern), searchFilter, searchCtls);
                            if (answer.hasMore()) {
                                sr = answer.next();
                                break;
                            }
                        } catch (NamingException e) {
                            // ignore
                            if (log.isDebugEnabled()) {
                                log.debug(e);
                            }
                        }
                    }
                }

                if (sr == null) {
                    // handling multiple search bases
                    String searchBases = ((LDAPRoleContext) context).getSearchBase();
                    String[] roleSearchBaseArray = searchBases.split("#");
                    for (String searchBase : roleSearchBaseArray) {
                        if (log.isDebugEnabled()) {
                            log.debug("Searching role: " + context.getRoleName() + " SearchBase: " + searchBase
                                    + " SearchFilter: " + searchFilter);
                        }

                        try {
                            // read the DN of users who are members of the group
                            answer = dirContext.search(escapeDNForSearch(searchBase), searchFilter, searchCtls);
                            int count = 0;
                            if (answer.hasMore()) { // to check if there is a result
                                while (answer.hasMore()) { // to check if there are more than one group
                                    if (count > 0) {
                                        throw new UserStoreException("More than one group exist with name");
                                    }
                                    sr = answer.next();
                                    count++;
                                }
                                break;
                            }
                        } catch (NamingException e) {
                            // ignore
                            if (log.isDebugEnabled()) {
                                log.debug(e);
                            }
                        }
                    }
                }

                if (log.isDebugEnabled()) {
                    log.debug("Found role: " + sr.getNameInNamespace());
                }

                // read the member attribute and get DNs of the users
                Attributes attributes = sr.getAttributes();
                if (attributes != null) {
                    NamingEnumeration attributeEntry;
                    int recordCount = 0;
                    for (attributeEntry = attributes.getAll(); attributeEntry.hasMore(); ) {
                        Attribute valAttribute = (Attribute) attributeEntry.next();
                        if (membershipProperty == null || isAttributeEqualsProperty(membershipProperty,
                                valAttribute.getID())) {
                            NamingEnumeration values = null;
                            for (values = valAttribute.getAll(); values.hasMore(); ) {
                                String value = values.next().toString();
                                userDNList.add(value);
                                recordCount++;

                                if (log.isDebugEnabled()) {
                                    log.debug("Found attribute: " + membershipProperty + " value: " + value);
                                }
                            }
                        }
                    }
                    if (attributeValuesRange == 0 || recordCount < attributeValuesRange) {
                        isEndOfAttributes = true;
                    } else {
                        offset += attributeValuesRange;
                        lastRecord += attributeValuesRange;
                    }
                }
            }

            if (MEMBER_UID.equals(realmConfig.getUserStoreProperty(LDAPConstants.MEMBERSHIP_ATTRIBUTE))) {
                /* when the GroupEntryObjectClass is posixGroup, membership attribute is memberUid. We have to
                   retrieve the DN using the memberUid.
                   This procedure has to make an extra call to ldap. alternatively this can be done with a single ldap
                   search using the memberUid and retrieving the display name and username. */
                List<String> userDNListNew = new ArrayList<>();

                for (String user : userDNList) {
                    String userDN = getNameInSpaceForUserName(user);
                    userDNListNew.add(userDN);
                }

                userDNList = userDNListNew;
            }

            // iterate over users' DN list and get userName and display name
            // attribute values

            String userNameProperty = realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE);
            String displayNameAttribute = realmConfig.getUserStoreProperty(LDAPConstants.DISPLAY_NAME_ATTRIBUTE);
            String userIDAttribute = realmConfig.getUserStoreProperty(LDAPConstants.USER_ID_ATTRIBUTE);
            String[] returnedAttributes = { userNameProperty, displayNameAttribute, userIDAttribute };
            User userObject = null;

            for (String user : userDNList) {
                if (log.isDebugEnabled()) {
                    log.debug("Getting name attributes of: " + user);
                }

                Attributes userAttributes;
                try {
                    // '\' and '"' characters need another level of escaping before searching
                    userAttributes = dirContext.getAttributes(escapeDNForSearch(user), returnedAttributes);

                    String displayName = null;
                    String userName = null;
                    String id = null;
                    if (userAttributes != null) {
                        Attribute userNameAttribute = userAttributes.get(userNameProperty);
                        if (userNameAttribute != null) {
                            userName = (String) userNameAttribute.get();
                            if (log.isDebugEnabled()) {
                                log.debug("UserName: " + userName);
                            }
                        }
                        if (StringUtils.isNotEmpty(displayNameAttribute)) {
                            Attribute displayAttribute = userAttributes.get(displayNameAttribute);
                            if (displayAttribute != null) {
                                displayName = (String) displayAttribute.get();
                            }
                            if (log.isDebugEnabled()) {
                                log.debug("DisplayName: " + displayName);
                            }
                        }
                        if (StringUtils.isNotEmpty(userIDAttribute)) {
                            Attribute userID = userAttributes.get(userIDAttribute);
                            if (userID != null) {
                                id = resolveLdapAttributeValue(userID.get());
                            }
                            if (log.isDebugEnabled()) {
                                log.debug("UserID: " + id);
                            }
                        }
                    }
                    String domainName = realmConfig
                            .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);

                    userObject = getUser(id, userName);
                    userObject.setDisplayName(displayName);
                    userObject.setUserStoreDomain(domainName);
                    userObject.setTenantDomain(getTenantDomain(tenantId));

                    userList.add(userObject);
                    if (log.isDebugEnabled()) {
                        log.debug(userObject.getUsername() + " is added to the result list");
                    }

                } catch (NamingException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Error in reading user information in the user store for the user " + userObject
                                .getUsername() + e.getMessage(), e);
                    }
                }

            }

        } catch (PartialResultException e) {
            // can be due to referrals in AD. so just ignore error
            String errorMessage = "Error in reading user information in the user store for filter : " + filter;
            if (isIgnorePartialResultException()) {
                if (log.isDebugEnabled()) {
                    log.debug(errorMessage, e);
                }
            } else {
                throw new UserStoreException(errorMessage, e);
            }
        } catch (NamingException e) {
            String errorMessage = "Error in reading user information in the user store for filter : " + filter;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            JNDIUtil.closeNamingEnumeration(answer);
            JNDIUtil.closeContext(dirContext);
        }

        return userList;
    }

    private boolean isAttributeEqualsProperty(String property, String attribute) {

        if (StringUtils.isEmpty(property) || StringUtils.isEmpty(attribute)) {
            return false;
        }
        return property.equals(attribute) || property.equals(attribute.substring(0, attribute.indexOf(";")));
    }

    @Override
    protected String[] doGetExternalRoleListOfUser(String userName, String filter) throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
    }

    @Override
    protected String[] doGetExternalRoleListOfUserWithID(String userID, String filter) throws UserStoreException {

        // Get the effective search base
        String searchBase = this.getEffectiveSearchBase(false);
        String userName = doGetUserNameFromUserID(userID);
        return getLDAPRoleListOfUser(userName, filter, searchBase, false);
    }

    @Override
    public String[] doGetSharedRoleListOfUser(String userName, String tenantDomain, String filter)
            throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
    }

    @Override
    public String[] doGetSharedRoleListOfUserWithID(String userID, String tenantDomain, String filter)
            throws UserStoreException {

        // Get the effective search base
        String searchBase = this.getEffectiveSearchBase(true);
        if (tenantDomain != null && tenantDomain.trim().length() > 0) {
            if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(tenantDomain.trim())) {
                String groupNameAttributeName = realmConfig
                        .getUserStoreProperty(LDAPConstants.SHARED_TENANT_NAME_ATTRIBUTE);
                if (groupNameAttributeName == null || groupNameAttributeName.trim().length() == 0) {
                    groupNameAttributeName = "ou";
                }
                searchBase = groupNameAttributeName + "=" + tenantDomain + "," + searchBase;
            }
        }
        String userName = doGetUserNameFromUserID(userID);
        return getLDAPRoleListOfUser(userName, filter, searchBase, true);
    }

    public boolean isReadOnly() throws UserStoreException {

        return true;
    }

    /**
     * @param sr
     * @param groupAttributeName
     * @return
     */
    private List<String> parseSearchResult(SearchResult sr, String groupAttributeName) {

        List<String> list = new ArrayList<>();
        Attributes attrs = sr.getAttributes();
        if (attrs != null) {
            try {
                NamingEnumeration ae = null;
                for (ae = attrs.getAll(); ae.hasMore(); ) {
                    Attribute attr = (Attribute) ae.next();
                    if (groupAttributeName == null || groupAttributeName.equals(attr.getID())) {
                        NamingEnumeration e = null;
                        for (e = attr.getAll(); e.hasMore(); ) {
                            String value = e.next().toString();
                            int begin = value.indexOf("=") + 1;
                            int end = value.indexOf(",");
                            if (begin > -1 && end > -1) {
                                value = value.substring(begin, end);
                            }
                            list.add(value);
                        }
                        JNDIUtil.closeNamingEnumeration(e);
                    }
                }
                JNDIUtil.closeNamingEnumeration(ae);
            } catch (NamingException e) {
                log.debug(e.getMessage(), e);
            }
        }
        return list;
    }

    // ****************************************************

    protected List<String> getAttributeListOfOneElement(String searchBases, String searchFilter,
            SearchControls searchCtls) throws UserStoreException {

        List<String> list = new ArrayList<>();
        DirContext dirContext = null;
        NamingEnumeration<SearchResult> answer = null;
        try {
            dirContext = connectionSource.getContext();
            // handle multiple search bases
            String[] searchBaseArray = searchBases.split("#");
            for (String searchBase : searchBaseArray) {
                try {
                    answer = dirContext.search(escapeDNForSearch(searchBase), searchFilter, searchCtls);
                    int count = 0;
                    if (answer.hasMore()) {
                        while (answer.hasMore()) {
                            if (count > 0) {
                                log.error("More than element user exist with name");
                                throw new UserStoreException("More than element user exist with name");
                            }
                            SearchResult sr = answer.next();
                            count++;
                            list = parseSearchResult(sr, null);
                        }
                        break;
                    }
                } catch (NamingException e) {
                    //ignore
                    if (log.isDebugEnabled()) {
                        log.debug(e);
                    }
                }
            }
        } finally {
            JNDIUtil.closeNamingEnumeration(answer);
            JNDIUtil.closeContext(dirContext);
        }
        return list;
    }

    @Override
    public List<String> doGetUserListFromPropertiesWithID(String property, String value, String profileName)
            throws UserStoreException {

        if (value == null) {
            return new ArrayList<>();
        }
        String userAttributeSeparator = ",";
        String serviceNameAttribute = "sn";
        List<String> values = new ArrayList<>();
        String searchFilter = realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_LIST_FILTER);
        String userIDProperty = realmConfig.getUserStoreProperty(LDAPConstants.USER_ID_ATTRIBUTE);

        if (OBJECT_GUID.equalsIgnoreCase(property) && (isGUIDValue(value) || StringUtils.equals(value, "*"))) {
            String transformObjectGuidToUuidProperty = realmConfig.getUserStoreProperty(TRANSFORM_OBJECTGUID_TO_UUID);

            boolean transformObjectGuidToUuid = StringUtils.isEmpty(transformObjectGuidToUuidProperty) || Boolean
                    .parseBoolean(transformObjectGuidToUuidProperty);

            String convertedValue;
            if (StringUtils.equals(value, "*")) {
                convertedValue = value;
            } else if (transformObjectGuidToUuid) {
                convertedValue = transformUUIDToObjectGUID(value);
            } else {
                byte[] bytes = Base64.decodeBase64(value.getBytes());
                convertedValue = convertBytesToHexString(bytes);
            }
            searchFilter = "(&" + searchFilter + "(" + property + "=" + convertedValue + "))";
        } else {
            searchFilter =
                    "(&" + searchFilter + "(" + property + "=" + escapeSpecialCharactersForFilterWithStarAsRegex(value)
                            + "))";
        }

        DirContext dirContext = this.connectionSource.getContext();
        NamingEnumeration<?> answer = null;
        NamingEnumeration<?> attrs = null;

        if (log.isDebugEnabled()) {
            log.debug("Listing users with Property: " + property + " SearchFilter: " + searchFilter);
        }
        String[] returnedAttributes = new String[]{userIDProperty, serviceNameAttribute};
        try {
            String searchBases = realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);
            String[] searchBaseArray = searchBases.split("#");

            for (String searchBase : searchBaseArray) {
                answer = this.searchUsersForASearchBase(searchFilter, returnedAttributes, dirContext,
                        searchBase, MAX_ITEM_LIMIT_UNLIMITED);
                if (answer.hasMore()) {
                    break;
                }
            }

            while (answer != null && answer.hasMoreElements()) {
                SearchResult sr = (SearchResult) answer.next();
                Attributes attributes = sr.getAttributes();
                if (attributes != null) {
                    Attribute attribute = attributes.get(userIDProperty);
                    if (attribute != null) {
                        StringBuilder attrBuffer = new StringBuilder();
                        for (attrs = attribute.getAll(); attrs.hasMore(); ) {
                            Object attObject = attrs.next();
                            String attr = null;

                            if (attObject instanceof String) {
                                attr = (String) attObject;
                            } else if (attObject instanceof byte[]) {
                                 /*
                                 Return canonical representation of UUIDs or base64 encoded string of other binary data.
                                 Active Directory attribute: objectGUID
                                 RFC 4530 attribute: entryUUID
                                  */
                                final byte[] bytes = (byte[]) attObject;

                                if (bytes.length == 16 && userIDProperty.toLowerCase().endsWith(LDAPConstants.UID)) {
                                     /*
                                     ObjectGUID byte order is not big-endian.
                                     https://msdn.microsoft.com/en-us/library/aa373931%28v=vs.85%29.aspx
                                     https://community.oracle.com/thread/1157698
                                      */
                                    if (userIDProperty.equalsIgnoreCase(OBJECT_GUID)) {
                                        // check the property for objectGUID transformation
                                        String transformToObjectGuidProperty =
                                                realmConfig.getUserStoreProperty(TRANSFORM_OBJECTGUID_TO_UUID);

                                        boolean transformObjectGuidToUuid =
                                                StringUtils.isEmpty(transformToObjectGuidProperty) ||
                                                        Boolean.parseBoolean(transformToObjectGuidProperty);

                                        if (transformObjectGuidToUuid) {
                                            final ByteBuffer bb = ByteBuffer.wrap(swapBytes(bytes));
                                            attr = new UUID(bb.getLong(), bb.getLong()).toString();
                                        } else {
                                            // Ignore transforming objectGUID to UUID canonical format.
                                            attr = new String(Base64.encodeBase64((byte[]) attObject));
                                        }
                                    }
                                } else {
                                    attr = new String(Base64.encodeBase64((byte[]) attObject));
                                }
                            }
                            if (StringUtils.isNotEmpty(attr)) {

                                String attrSeparator = realmConfig.getUserStoreProperty(MULTI_ATTRIBUTE_SEPARATOR);
                                if (attrSeparator != null && !attrSeparator.trim().isEmpty()) {
                                    userAttributeSeparator = attrSeparator;
                                }
                                attrBuffer.append(attr).append(userAttributeSeparator);
                                if (log.isDebugEnabled()) {
                                    log.debug(userIDProperty + " : " + attr);
                                }
                            }
                        }
                        String propertyValue = attrBuffer.toString();
                        Attribute serviceNameObject = attributes.get(serviceNameAttribute);
                        String serviceNameAttributeValue = null;
                        if (serviceNameObject != null) {
                            serviceNameAttributeValue = (String) serviceNameObject.get();
                        }
                        // Length needs to be more than userAttributeSeparator.length() for a valid
                        // attribute, since we
                        // attach userAttributeSeparator.
                        if (propertyValue != null && propertyValue.trim().length() > userAttributeSeparator.length()) {
                            if (LDAPConstants.SERVER_PRINCIPAL_ATTRIBUTE_VALUE.equals(serviceNameAttributeValue)) {
                                continue;
                            }
                            propertyValue = propertyValue
                                    .substring(0, propertyValue.length() - userAttributeSeparator.length());
                            values.add(propertyValue);
                        }
                    }
                }
            }
        } catch (PartialResultException e) {
            // can be due to referrals in AD. so just ignore error
            String errorMessage = "Error occurred while getting user list from search Filter : " + searchFilter;
            if (isIgnorePartialResultException()) {
                if (log.isDebugEnabled()) {
                    log.debug(errorMessage, e);
                }
            } else {
                throw new UserStoreException(errorMessage, e);
            }
        } catch (NamingException e) {
            String errorMessage =
                    "Error occurred while getting user list from property : " + property + " & value : " + value
                            + " & profile name : " + profileName;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            // close the naming enumeration and free up resources
            JNDIUtil.closeNamingEnumeration(attrs);
            JNDIUtil.closeNamingEnumeration(answer);
            // close directory context
            JNDIUtil.closeContext(dirContext);
        }

        if (log.isDebugEnabled()) {
            String[] results = values.toArray(new String[0]);
            for (String result : results) {
                log.debug("result: " + result);
            }
        }

        return values;
    }

    /**
     * This method supports multi-attribute filters with paginated search for user(s).
     *
     * @param condition   Validated Condition tree.
     * @param profileName Default profile name.
     * @param limit       The number of entries to return in a page.
     * @param offset      Start index.
     * @param sortBy      Sort according to the given attribute name.
     * @param sortOrder   Sorting order.
     * @return A non-null UniqueIDPaginatedSearchResult instance. Typically contains users list with pagination.
     * @throws UserStoreException If an UserStoreException is encountered
     *                            while searching for users in a given condition.
     */
    @Override
    protected UniqueIDPaginatedSearchResult doGetUserListWithID(Condition condition, String profileName, int limit,
                                                                int offset, String sortBy, String sortOrder)
            throws UserStoreException {

        UniqueIDPaginatedSearchResult result = new UniqueIDPaginatedSearchResult();
        List<ExpressionCondition> expressionConditions = getExpressionConditions(condition);
        LDAPSearchSpecification ldapSearchSpecification = new LDAPSearchSpecification(realmConfig,
                expressionConditions);
        boolean isMemberShipPropertyFound = ldapSearchSpecification.isMemberShipPropertyFound();
        limit = getLimit(limit, isMemberShipPropertyFound);
        offset = getOffset(offset);
        if (limit == 0) {
            return result;
        }
        int pageSize = limit;
        DirContext dirContext = this.connectionSource.getContext();
        LdapContext ldapContext = (LdapContext) dirContext;
        List<User> users;
        String userNameAttribute = realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE);
        try {
            ldapContext.setRequestControls(new Control[] { new PagedResultsControl(pageSize, Control.CRITICAL),
                    new SortControl(userNameAttribute, Control.NONCRITICAL) });
            users = performLDAPSearch(ldapContext, ldapSearchSpecification, pageSize, offset, expressionConditions);
            result.setUsers(users);
            return result;
        } catch (NamingException e) {
            log.error(String.format("Error occurred while performing paginated search, %s", e.getMessage()));
            throw new UserStoreException(e.getMessage(), e);
        } catch (IOException e) {
            log.error(String.format("Error occurred while setting paged results controls for paginated search, %s",
                    e.getMessage()));
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            JNDIUtil.closeContext(dirContext);
            JNDIUtil.closeContext(ldapContext);
        }
    }

    @Override
    protected PaginatedSearchResult doGetUserList(Condition condition, String profileName, int limit, int offset,
            String sortBy, String sortOrder) throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
    }

    @Override
    public boolean doCheckIsUserInRole(String userName, String roleName) throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
    }

    @Override
    public boolean doCheckIsUserInRoleWithID(String userID, String roleName) throws UserStoreException {

        return super.doCheckIsUserInRole(this.doGetUserNameFromUserID(userID), roleName);
    }

    @Override
    public Date getPasswordExpirationTimeWithID(String userID) throws UserStoreException {

        String username = getUserNameFromUserID(userID);
        return super.getPasswordExpirationTime(username);
    }

    // ************** NOT GOING TO IMPLEMENT ***************

    @Override
    public int getTenantId(String username) throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
    }

    @Override
    public int getUserId(String username) throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
    }

    @Override
    protected void doSetUserAttributeWithID(String userID, String attributeName, String value, String profileName)
            throws UserStoreException {

        throw new UserStoreException(
                "User store is operating in read only mode. Cannot write into the user store.");
    }

    @Override
    protected void doSetUserAttributesWithID(String userID, Map<String, String> processedClaimAttributes,
                                             String profileName) throws UserStoreException {

        throw new UserStoreException(
                "User store is operating in read only mode. Cannot write into the user store.");
    }

    @Override
    protected void doSetUserAttributesWithID(String userID, Map<String, List<String>> claimAttributesToAdd,
                                             Map<String, List<String>> claimAttributesToDelete,
                                             Map<String, List<String>> claimAttributesToReplace, String profileName)
            throws UserStoreException {

        throw new UserStoreException("User store is operating in read only mode. Cannot write into the user store.");
    }

    @Override
    public void doDeleteUserClaimValue(String userName, String claimURI, String profileName) throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");

    }

    @Override
    public void doDeleteUserClaimValueWithID(String userID, String claimURI, String profileName)
            throws UserStoreException {

        throw new UserStoreException("User store is operating in read only mode. Cannot write into the user store.");

    }

    @Override
    public void doDeleteUserClaimValuesWithID(String userID, String[] claims, String profileName)
            throws UserStoreException {

        throw new UserStoreException("User store is operating in read only mode. Cannot write into the user store.");

    }

    @Override
    public void doDeleteUserClaimValues(String userName, String[] claims, String profileName)
            throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");

    }

    @Override
    public void doAddUser(String userName, Object credential, String[] roleList, Map<String, String> claims,
            String profileName) throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
    }

    @Override
    public void doAddUser(String userName, Object credential, String[] roleList, Map<String, String> claims,
            String profileName, boolean requirePasswordChange) throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
    }

    @Override
    public User doAddUserWithID(String userName, Object credential, String[] roleList, Map<String, String> claims,
            String profileName, boolean requirePasswordChange) throws UserStoreException {

        throw new UserStoreException("User store is operating in read only mode. Cannot write into the user store.");
    }

    @Override
    public void doDeleteUser(String userName) throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
    }

    @Override
    public void doDeleteUserWithID(String userID) throws UserStoreException {

        throw new UserStoreException("User store is operating in read only mode. Cannot write into the user store.");
    }

    @Override
    public void doSetUserClaimValue(String userName, String claimURI, String claimValue, String profileName)
            throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
    }

    @Override
    public void doSetUserClaimValues(String userName, Map<String, String> claims, String profileName)
            throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");

    }

    @Override
    public void doSetUserClaimValues(String userName, Map<String, List<String>> multiValuedClaimsToAdd,
                                     Map<String, List<String>> multiValuedClaimsToDelete,
                                     Map<String, List<String>> claimsExcludingMultiValuedClaims,
                                     String profileName) throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
    }

    @Override
    public void doUpdateCredential(String userName, Object newCredential, Object oldCredential)
            throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
    }

    @Override
    public void doUpdateCredentialWithID(String userID, Object newCredential, Object oldCredential)
            throws UserStoreException {

        throw new UserStoreException("User store is operating in read only mode. Cannot write into the user store.");
    }

    @Override
    public void doUpdateCredentialByAdmin(String userName, Object newCredential) throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
    }

    @Override
    public void doUpdateCredentialByAdminWithID(String userID, Object newCredential) throws UserStoreException {

        throw new UserStoreException("User store is operating in read only mode. Cannot write into the user store.");
    }

    @Override
    public void doUpdateRoleListOfUser(String userName, String[] deletedRoles, String[] newRoles)
            throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
    }

    @Override
    public void doUpdateRoleListOfUserWithID(String userID, String[] deletedRoles, String[] newRoles)
            throws UserStoreException {

        throw new UserStoreException("User store is operating in read only mode. Cannot write into the user store.");
    }

    @Override
    public void doUpdateUserListOfRole(String roleName, String[] deletedUsers, String[] newUsers)
            throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
    }

    @Override
    public void doUpdateUserListOfRoleWithID(String roleName, String[] deletedUsers, String[] newUsers)
            throws UserStoreException {

        throw new UserStoreException("User store is operating in read only mode. Cannot write into the user store.");
    }

    /**
     * Escaping ldap search filter special characters in a string
     *
     * @param dnPartial
     * @return
     */
    private String escapeSpecialCharactersForFilterWithStarAsRegex(String dnPartial) {

        boolean replaceEscapeCharacters = true;
        String replaceEscapeCharactersAtUserLoginString = realmConfig
                .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_REPLACE_ESCAPE_CHARACTERS_AT_USER_LOGIN);

        if (replaceEscapeCharactersAtUserLoginString != null) {
            replaceEscapeCharacters = Boolean.parseBoolean(replaceEscapeCharactersAtUserLoginString);
            if (log.isDebugEnabled()) {
                log.debug("Replace escape characters configured to: " + replaceEscapeCharactersAtUserLoginString);
            }
        }

        if (replaceEscapeCharacters) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < dnPartial.length(); i++) {
                char currentChar = dnPartial.charAt(i);
                switch (currentChar) {
                case '\\':
                    if (dnPartial.charAt(i + 1) == '*') {
                        sb.append("\\2a");
                        i++;
                        break;
                    }
                    sb.append("\\5c");
                    break;
                case '(':
                    sb.append("\\28");
                    break;
                case ')':
                    sb.append("\\29");
                    break;
                case '\u0000':
                    sb.append("\\00");
                    break;
                default:
                    sb.append(currentChar);
                }
            }
            return sb.toString();
        } else {
            return dnPartial;
        }
    }

    /**
     * Escaping ldap search filter special characters in a string
     *
     * @param dnPartial String to replace special characters of
     * @return
     */
    private String escapeSpecialCharactersForFilter(String dnPartial) {

        boolean replaceEscapeCharacters = true;
        dnPartial.replace("\\*", "*");

        String replaceEscapeCharactersAtUserLoginString = realmConfig
                .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_REPLACE_ESCAPE_CHARACTERS_AT_USER_LOGIN);

        if (replaceEscapeCharactersAtUserLoginString != null) {
            replaceEscapeCharacters = Boolean.parseBoolean(replaceEscapeCharactersAtUserLoginString);
            if (log.isDebugEnabled()) {
                log.debug("Replace escape characters configured to: " + replaceEscapeCharactersAtUserLoginString);
            }
        }
        if (replaceEscapeCharacters) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < dnPartial.length(); i++) {
                char currentChar = dnPartial.charAt(i);
                switch (currentChar) {
                case '\\':
                    sb.append("\\5c");
                    break;
                case '*':
                    sb.append("\\2a");
                    break;
                case '(':
                    sb.append("\\28");
                    break;
                case ')':
                    sb.append("\\29");
                    break;
                case '\u0000':
                    sb.append("\\00");
                    break;
                default:
                    sb.append(currentChar);
                }
            }
            return sb.toString();
        } else {
            return dnPartial;
        }
    }

    /**
     * Escaping ldap DN special characters in a String value
     *
     * @param text String to replace special characters of
     * @return
     */
    private String escapeSpecialCharactersForDN(String text) {

        boolean replaceEscapeCharacters = true;
        text.replace("\\*", "*");

        String replaceEscapeCharactersAtUserLoginString = realmConfig
                .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_REPLACE_ESCAPE_CHARACTERS_AT_USER_LOGIN);

        if (replaceEscapeCharactersAtUserLoginString != null) {
            replaceEscapeCharacters = Boolean.parseBoolean(replaceEscapeCharactersAtUserLoginString);
            if (log.isDebugEnabled()) {
                log.debug("Replace escape characters configured to: " + replaceEscapeCharactersAtUserLoginString);
            }
        }

        if (replaceEscapeCharacters) {
            StringBuilder sb = new StringBuilder();
            if ((text.length() > 0) && ((text.charAt(0) == ' ') || (text.charAt(0) == '#'))) {
                sb.append('\\'); // add the leading backslash if needed
            }
            for (int i = 0; i < text.length(); i++) {
                char currentChar = text.charAt(i);
                switch (currentChar) {
                case '\\':
                    sb.append("\\\\");
                    break;
                case ',':
                    sb.append("\\,");
                    break;
                case '+':
                    sb.append("\\+");
                    break;
                case '"':
                    sb.append("\\\"");
                    break;
                case '<':
                    sb.append("\\<");
                    break;
                case '>':
                    sb.append("\\>");
                    break;
                case ';':
                    sb.append("\\;");
                    break;
                case '*':
                    sb.append("\\2a");
                    break;
                default:
                    sb.append(currentChar);
                }
            }
            if ((text.length() > 1) && (text.charAt(text.length() - 1) == ' ')) {
                sb.insert(sb.length() - 1, '\\'); // add the trailing backslash if needed
            }
            if (log.isDebugEnabled()) {
                log.debug("value after escaping special characters in " + text + " : " + sb.toString());
            }
            return sb.toString();
        } else {
            return text;
        }

    }

    /**
     * This method performs the additional level escaping for ldap search. In ldap search / and " characters
     * have to be escaped again
     *
     * @param dn DN
     * @return composite name
     * @throws InvalidNameException failed to build composite name
     */
    protected Name escapeDNForSearch(String dn) throws InvalidNameException {

        // This is done to escape '/' which is not a LDAP special character but a JNDI special character.
        // Refer: https://bugs.java.com/bugdatabase/view_bug.do?bug_id=4307193
        return new CompositeName().add(dn);
    }

    @Override
    public Properties getDefaultUserStoreProperties() {

        Properties properties = new Properties();
        properties.setMandatoryProperties(
                Stream.concat(ReadOnlyLDAPUserStoreConstants.ROLDAP_USERSTORE_PROPERTIES.stream(),
                        ReadOnlyLDAPUserStoreConstants.UNIQUE_ID_ROLDAP_USERSTORE_PROPERTIES.stream())
                        .toArray(Property[]::new));
        properties.setOptionalProperties(
                ReadOnlyLDAPUserStoreConstants.OPTIONAL_ROLDAP_USERSTORE_PROPERTIES.toArray(new Property[0]));
        properties.setAdvancedProperties(UNIQUE_ID_RO_LDAP_UM_ADVANCED_PROPERTIES.toArray(new Property[0]));
        return properties;
    }

    private static void setAdvancedProperties() {

        // Set Advanced Properties.
        UNIQUE_ID_RO_LDAP_UM_ADVANCED_PROPERTIES.clear();
        setAdvancedProperty(UserStoreConfigConstants.passwordHashMethod, "Password Hashing Algorithm", "PLAIN_TEXT",
                UserStoreConfigConstants.passwordHashMethodDescription);
        setAdvancedProperty(MULTI_ATTRIBUTE_SEPARATOR, "Multiple Attribute Separator", ",",
                MULTI_ATTRIBUTE_SEPARATOR_DESCRIPTION);

        setAdvancedProperty(UserStoreConfigConstants.maxUserNameListLength, "Maximum User List Length", "100",
                UserStoreConfigConstants.maxUserNameListLengthDescription);
        setAdvancedProperty(UserStoreConfigConstants.maxRoleNameListLength, "Maximum Role List Length", "100",
                UserStoreConfigConstants.maxRoleNameListLengthDescription);

        setAdvancedProperty(UserStoreConfigConstants.userRolesCacheEnabled, "Enable User Role Cache", "true",
                UserStoreConfigConstants.userRolesCacheEnabledDescription);

        setAdvancedProperty(UserStoreConfigConstants.connectionPoolingEnabled, "Enable LDAP Connection Pooling",
                "false", UserStoreConfigConstants.connectionPoolingEnabledDescription);
        setAdvancedProperty(LDAPConnectionTimeout, "LDAP Connection Timeout", "5000", LDAPConnectionTimeoutDescription);

        setAdvancedProperty(readTimeout, "LDAP Read Timeout", "5000", readTimeoutDescription);
        setAdvancedProperty(RETRY_ATTEMPTS, "Retry Attempts", "0",
                "Number of retries for" + " authentication in case ldap read timed out.");
        setAdvancedProperty("CountRetrieverClass", "Count Implementation", "",
                "Name of the class that implements the count functionality");
        setAdvancedProperty(LDAPConstants.LDAP_ATTRIBUTES_BINARY, "LDAP binary attributes", " ",
                LDAPBinaryAttributesDescription);
        setAdvancedProperty(UserStoreConfigConstants.claimOperationsSupported,
                UserStoreConfigConstants.getClaimOperationsSupportedDisplayName, "false",
                UserStoreConfigConstants.claimOperationsSupportedDescription);
        setAdvancedProperty(MEMBERSHIP_ATTRIBUTE_RANGE, MEMBERSHIP_ATTRIBUTE_RANGE_DISPLAY_NAME,
                String.valueOf(MEMBERSHIP_ATTRIBUTE_RANGE_VALUE),
                "Number of maximum users of role returned by the LDAP");
        setAdvancedProperty(LDAPConstants.USER_CACHE_EXPIRY_MILLISECONDS, USER_CACHE_EXPIRY_TIME_ATTRIBUTE_NAME, "",
                USER_CACHE_EXPIRY_TIME_ATTRIBUTE_DESCRIPTION);
        setAdvancedProperty(LDAPConstants.USER_DN_CACHE_ENABLED, USER_DN_CACHE_ENABLED_ATTRIBUTE_NAME, "true",
                USER_DN_CACHE_ENABLED_ATTRIBUTE_DESCRIPTION);
        setAdvancedProperty(UserStoreConfigConstants.STARTTLS_ENABLED,
                UserStoreConfigConstants.STARTTLS_ENABLED_DISPLAY_NAME, "false",
                UserStoreConfigConstants.STARTTLS_ENABLED_DESCRIPTION);
        setAdvancedProperty(UserStoreConfigConstants.CONNECTION_RETRY_DELAY,
                UserStoreConfigConstants.CONNECTION_RETRY_DELAY_DISPLAY_NAME,
                String.valueOf(UserStoreConfigConstants.DEFAULT_CONNECTION_RETRY_DELAY_IN_MILLISECONDS),
                UserStoreConfigConstants.CONNECTION_RETRY_DELAY_DESCRIPTION);
        setAdvancedProperty(UserStoreConfigConstants.immutableAttributes,
                UserStoreConfigConstants.immutableAttributesDisplayName, " ",
                UserStoreConfigConstants.immutableAttributesDescription);
        setAdvancedProperty(UserStoreConfigConstants.timestampAttributes,
                UserStoreConfigConstants.timestampAttributesDisplayName, " ",
                UserStoreConfigConstants.timestampAttributesDescription);
    }

    private static void setAdvancedProperty(String name, String displayName, String value, String description) {

        Property property = new Property(name, value, displayName + "#" + description, null);
        UNIQUE_ID_RO_LDAP_UM_ADVANCED_PROPERTIES.add(property);

    }

    /**
     * Initialize the user cache.
     * Uses Javax cache. Any existing cache with the same name will be removed and re-attach an new one.
     */
    protected void initUserCache() throws UserStoreException {

        if (!userDnCacheEnabled) {
            if (log.isDebugEnabled()) {
                log.debug(
                        "User DN cache is disabled in configuration on UserStore having SearchBase: " + userSearchBase);
            }
            return;
        }
        boolean isUserDnCacheCustomExpiryValuePresent = false;

        if (StringUtils.isNotEmpty(cacheExpiryTimeAttribute)) {
            if (log.isDebugEnabled()) {
                log.debug("Cache expiry time : " + cacheExpiryTimeAttribute
                        + " configured for the user DN cache having search base: " + userSearchBase);
            }
            try {
                userDnCacheExpiryTime = Long.parseLong(cacheExpiryTimeAttribute);
                isUserDnCacheCustomExpiryValuePresent = true;
            } catch (NumberFormatException nfe) {
                log.error("Could not convert the cache expiry time to Number (long) : " + cacheExpiryTimeAttribute
                        + " . Will default to system wide expiry settings.", nfe);
            }
        }

        RealmService realmService = UserStoreMgtDSComponent.getRealmService();
        if (realmService != null && realmService.getTenantManager() != null) {
            try {
                tenantDomain = realmService.getTenantManager().getDomain(tenantId);
                if (log.isDebugEnabled()) {
                    log.debug("Tenant domain : " + tenantDomain + " found for the tenant ID : " + tenantId);
                }
            } catch (org.wso2.carbon.user.api.UserStoreException e) {
                throw new UserStoreException("Could not get the tenant domain for tenant id : " + tenantId, e);
            }
        }

        if (tenantDomain == null && tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            // Assign super-tenant domain, If this is super tenant and the tenant domain is not yet known.
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }

        if (tenantDomain == null) {
            // Do not create the cache if there is no tenant domain, which means the given tenant ID is invalid.
            // Any cache access i.e. getX, putX or deleteX will simply behave as no-op in this case.
            if (log.isDebugEnabled()) {
                log.debug("Could not find a tenant domain for the tenant ID : " + tenantId
                        + ". Not initializing the User DN cache.");
            }
            return;
        }
        try {
            startTenantFlow();
            userDnCacheName = USER_CACHE_NAME_PREFIX + this.hashCode();
            cacheManager = Caching.getCacheManagerFactory().getCacheManager(USER_CACHE_MANAGER);

            // Unconditionally remove the cache, so that it can be reconfigured.
            cacheManager.removeCache(userDnCacheName);

            if (isUserDnCacheCustomExpiryValuePresent) {
                // We use cache builder to create the cache with custom expiry values.
                if (log.isDebugEnabled()) {
                    log.debug("Using cache expiry time : " + userDnCacheExpiryTime
                            + " configured for the user DN cache having search base: " + userSearchBase);
                }
                userDnCacheBuilder = cacheManager.createCacheBuilder(userDnCacheName);
                userDnCacheBuilder.setExpiry(CacheConfiguration.ExpiryType.ACCESSED,
                        new CacheConfiguration.Duration(TimeUnit.MILLISECONDS, userDnCacheExpiryTime)).
                        setExpiry(CacheConfiguration.ExpiryType.MODIFIED,
                                new CacheConfiguration.Duration(TimeUnit.MILLISECONDS, userDnCacheExpiryTime)).
                        setStoreByValue(false);
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Puts the DN into the cache.
     *
     * @param name  the user name.
     * @param value the LDAP name (DN)
     */
    protected void putToUserCache(String name, LdapName value) {

        try {
            startTenantFlow();
            Cache<String, LdapName> userDnCache = createOrGetUserDnCache();
            if (userDnCache == null) {
                // User cache may be null while initializing.
                return;
            }
            userDnCache.put(name, value);
        } catch (IllegalStateException e) {
            // There is no harm ignoring the put, as the cache(local) is already is of no use. Mis-penalty is low.
            log.error("Error occurred while putting User DN to the cache having search base : " + userSearchBase, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Returns the LDAP Name (DN) for the given user name, if it exists in the cache.
     *
     * @param userName
     * @return cached DN, if exists. null if the cache does not contain the DN for the userName.
     */
    protected LdapName getFromUserCache(String userName) {

        try {
            startTenantFlow();
            Cache<String, LdapName> userDnCache = createOrGetUserDnCache();
            if (userDnCache == null) {
                // User cache may be null while initializing.
                return null;
            }
            return userDnCache.get(userName);
        } catch (IllegalStateException e) {
            log.error("Error occurred while getting User DN from cache having search base : " + userSearchBase, e);
            return null;
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Removes the cache entry given the user name.
     *
     * @param userName the User name to remove.
     * @return true if removal was successful.
     */
    protected boolean removeFromUserCache(String userName) {

        try {
            startTenantFlow();
            Cache<String, LdapName> userDnCache = createOrGetUserDnCache();
            if (userDnCache == null) {
                // User cache may be null while initializing.
                // Return true as removal result is successful when there is no cache. Nothing was held.
                return true;
            }
            return userDnCache.remove(userName);
        } catch (IllegalStateException e) {
            // There is no harm ignoring the removal, as the cache(local) is already is of no use.
            log.error("Error occurred while removing User DN from cache having search base : " + userSearchBase, e);
            return true;
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Common utility method to start the Super tenant flow.
     */
    private void startTenantFlow() {

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        carbonContext.setTenantId(tenantId);
        carbonContext.setTenantDomain(tenantDomain);
    }

    /**
     * Returns the User DN Cache. Creates one if not exists in the cache manager.
     * Cache manager removes the cache if it is idle and empty for some time. Hence we need to create,
     * with our owen settings if needed.
     *
     * @return
     */
    private Cache<String, LdapName> createOrGetUserDnCache() {

        if (cacheManager == null || !userDnCacheEnabled) {
            if (log.isDebugEnabled()) {
                log.debug("Not using the cache on UserDN. cacheManager: " + cacheManager + " , Enabled : "
                        + userDnCacheEnabled);
            }
            return null;
        }

        Cache<String, LdapName> userDnCache;

        if (userDnCacheBuilder != null) {
            // We use cache builder to create the cache with custom expiry values.
            if (log.isDebugEnabled()) {
                log.debug("Using cache bulder to get the cache, for UserSearchBase: " + userSearchBase);
            }
            userDnCache = userDnCacheBuilder.build();
        } else {
            // We use system-wide settings to build the cache.
            if (log.isDebugEnabled()) {
                log.debug("Using default configurations for the user DN cache, having search base : " + userSearchBase);
            }
            userDnCache = cacheManager.getCache(userDnCacheName);
        }

        return userDnCache;
    }

    /**
     * Removes
     * 1. Current User cache from the respective cache manager.
     *
     * @throws Throwable
     */
    @Override
    protected void finalize() throws Throwable {

        if (cacheManager != null && userDnCacheName != null) {
            // Remove the userDN cache, as we created a DN cache per an instance of this class.
            // Any change in LDAP User Store config, too should invalidate the cache and remove it from memory.
            try {
                startTenantFlow();
                cacheManager.removeCache(userDnCacheName);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        super.finalize();
    }

    @Override
    public boolean isUniqueUserIdEnabled() {

        return true;
    }

    protected void processAttributesAfterRetrievalWithID(String userID, Map<String, String> userStorePropertyValues,
                                                         String profileName) {

        String timestampAttributesProperty = Optional.ofNullable(realmConfig
                .getUserStoreProperty(UserStoreConfigConstants.timestampAttributes)).orElse(StringUtils.EMPTY);

        String[] timestampAttributes = StringUtils.split(timestampAttributesProperty, ",");

        if (logger.isDebugEnabled()) {
            logger.debug("Unique read only timestamp attributes: " + Arrays.toString(timestampAttributes));
        }

        if (ArrayUtils.isNotEmpty(timestampAttributes)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Retrieved user store properties before type conversions: " + userStorePropertyValues);
            }

            Map<String, String> convertedTimestampAttributeValues = Arrays.stream(timestampAttributes)
                    .filter(attribute -> userStorePropertyValues.get(attribute) != null)
                    .collect(Collectors.toMap(Function.identity(),
                            attribute -> convertDateFormatFromLDAP(userStorePropertyValues.get(attribute))));

            if (logger.isDebugEnabled()) {
                logger.debug("Converted timestamp attribute values: " + convertedTimestampAttributeValues);
            }

            userStorePropertyValues.putAll(convertedTimestampAttributeValues);

            if (logger.isDebugEnabled()) {
                logger.debug("Retrieved user store properties after type conversions: " + userStorePropertyValues);
            }
        }
    }

    /**
     * Do LDAP paginated search and return user objects as a list.
     *
     * @param ldapContext             LDAP connection context.
     * @param ldapSearchSpecification Contains LDAP context search parameters.
     * @param pageSize                Number of results per page.
     * @param offset                  Start index.
     * @param expressionConditions    List of input expressions.
     * @return List of users.
     * @throws UserStoreException
     */
    private List<User> performLDAPSearch(LdapContext ldapContext, LDAPSearchSpecification ldapSearchSpecification,
                                           int pageSize, int offset, List<ExpressionCondition> expressionConditions)
            throws UserStoreException {

        byte[] cookie;
        int pageIndex = -1;
        boolean isGroupFiltering = ldapSearchSpecification.isGroupFiltering();
        boolean isUsernameFiltering = ldapSearchSpecification.isUsernameFiltering();
        boolean isClaimFiltering = ldapSearchSpecification.isClaimFiltering();
        boolean isMemberShipPropertyFound = ldapSearchSpecification.isMemberShipPropertyFound();

        String searchBases = ldapSearchSpecification.getSearchBases();
        String[] searchBaseArray = searchBases.split("#");
        String searchFilter = ldapSearchSpecification.getSearchFilterQuery();
        SearchControls searchControls = ldapSearchSpecification.getSearchControls();
        List<String> returnedAttributes = Arrays.asList(searchControls.getReturningAttributes());
        NamingEnumeration<SearchResult> answer = null;
        List<User> users = new ArrayList<>();

        if (log.isDebugEnabled()) {
            log.debug(String.format("Searching for user(s) with SearchFilter: %s and page size %d", searchFilter,
                    pageSize));
        }
        try {
            for (String searchBase : searchBaseArray) {
                do {
                    List<User> tempUsersList = new ArrayList<>();
                    answer = ldapContext.search(escapeDNForSearch(searchBase), searchFilter, searchControls);
                    //DirContext.search never returns null
                    if (answer.hasMore()) {
                        tempUsersList = getUserListFromSearch(isGroupFiltering, returnedAttributes, answer,
                                isSingleAttributeFilterOperation(expressionConditions));
                        pageIndex++;
                    }
                    if (CollectionUtils.isNotEmpty(tempUsersList)) {
                        if (isMemberShipPropertyFound) {
                            /*
                            Pagination is not supported for 'member' attribute group filtering. Also,
                            we need do post-processing if we found username filtering or claim filtering,
                            because can't apply claim filtering with memberShip group filtering and
                            can't apply username filtering with 'CO', 'EW', 'SW' filter operations.
                             */
                            users = membershipGroupFilterPostProcessing(isUsernameFiltering, isClaimFiltering,
                                    expressionConditions, tempUsersList);
                            break;
                        } else {
                            // Handle pagination depending on given offset, i.e. start index.
                            generatePaginatedUserList(pageIndex, offset, pageSize, tempUsersList, users);
                            int needMore = pageSize - users.size();
                            if (needMore == 0) {
                                break;
                            }
                        }
                    }
                    cookie = parseControls(ldapContext.getResponseControls());
                    String userNameAttribute = realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE);
                    ldapContext.setRequestControls(new Control[]{new PagedResultsControl(pageSize, cookie,
                            Control.CRITICAL), new SortControl(userNameAttribute, Control.NONCRITICAL)});
                } while ((cookie != null) && (cookie.length != 0));
            }
        } catch (PartialResultException e) {
            // Can be due to referrals in AD. So just ignore error.
            if (isIgnorePartialResultException()) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Error occurred while searching for user(s) for filter: %s", searchFilter));
                }
            } else {
                log.error(String.format("Error occurred while searching for user(s) for filter: %s", searchFilter));
                throw new UserStoreException(e.getMessage(), e);
            }
        } catch (NamingException e) {
            log.error(String.format("Error occurred while searching for user(s) for filter: %s, %s",
                    searchFilter, e.getMessage()));
            throw new UserStoreException(e.getMessage(), e);
        } catch (IOException e) {
            log.error(String.format("Error occurred while doing paginated search, %s", e.getMessage()));
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            JNDIUtil.closeNamingEnumeration(answer);
        }
        return users;
    }

    /**
     * Get user list from multi attribute search filter.
     *
     * @param isGroupFiltering        Whether the filtering has the group attribute name.
     * @param returnedAttributes      Returned Attributes.
     * @param answer                  Answer.
     * @param isSingleAttributeFilter Whether the original request is from a single attribute filter or a multi
     *                                attribute filter, so that AND operation can be omitted during the filtering
     *                                process.
     * @return A users list.
     * @throws UserStoreException
     * @throws NamingException
     */
    private List<User> getUserListFromSearch(boolean isGroupFiltering, List<String> returnedAttributes,
                                               NamingEnumeration<SearchResult> answer, boolean isSingleAttributeFilter)
            throws UserStoreException {

        List<User> tempUsersList;
        if (isGroupFiltering) {
            if (returnedAttributes.contains(realmConfig.getUserStoreProperty(LDAPConstants.MEMBERSHIP_ATTRIBUTE))) {
                tempUsersList = getUserListFromMembershipGroupFilterResult
                        (answer, returnedAttributes, isSingleAttributeFilter);
            }
            else {
                tempUsersList = getUserListFromMemberOfGroupFilterResult(answer);
            }
        } else {
            tempUsersList = getUserListFromNonGroupFilterResult(answer, returnedAttributes);
        }
        return tempUsersList;
    }

    /**
     * Parse the search result of non group filtering and get the user list.
     *
     * @param answer                Answer from LDAP search.
     * @param returnedAttributes    Returned attributes.
     * @return  A users list.
     * @throws UserStoreException
     */
    private List<User> getUserListFromNonGroupFilterResult(NamingEnumeration<SearchResult> answer,
                                                             List<String> returnedAttributes)
            throws UserStoreException {

        List<User> finalUserList = new ArrayList<>();
        String userAttributeSeparator = ",";
        NamingEnumeration<?> attrs = null;

        try {
            while (answer.hasMoreElements()) {
                SearchResult searchResult = answer.next();
                Attributes attributes = searchResult.getAttributes();
                if (attributes == null) {
                    continue;
                }
                Attribute attribute = attributes.get(returnedAttributes.get(0));
                if (attribute == null) {
                    continue;
                }
                StringBuffer attrBuffer = new StringBuffer();
                for (attrs = attribute.getAll(); attrs.hasMore(); ) {
                    String attr = (String) attrs.next();
                    if (StringUtils.isNotEmpty(attr.trim())) {
                        String attrSeparator = realmConfig.getUserStoreProperty(MULTI_ATTRIBUTE_SEPARATOR);
                        if (StringUtils.isNotEmpty(attrSeparator.trim())) {
                            userAttributeSeparator = attrSeparator;
                        }
                        attrBuffer.append(attr + userAttributeSeparator);
                        if (log.isDebugEnabled()) {
                            log.debug(returnedAttributes.get(0) + " : " + attr);
                        }
                    }
                }
                String userNamePropertyValue = attrBuffer.toString();
                Attribute serviceNameObject = attributes.get(returnedAttributes.get(1));
                String serviceNameAttributeValue = null;
                if (serviceNameObject != null) {
                    serviceNameAttributeValue = (String) serviceNameObject.get();
                }
                /* Length needs to be more than userAttributeSeparator.length() for a valid attribute,
                since we attach userAttributeSeparator. */
                if (userNamePropertyValue.trim().length() > userAttributeSeparator.length()) {
                    if (LDAPConstants.SERVER_PRINCIPAL_ATTRIBUTE_VALUE.equals(serviceNameAttributeValue)) {
                        continue;
                    }
                    userNamePropertyValue = userNamePropertyValue.substring(0, userNamePropertyValue.length() -
                            userAttributeSeparator.length());

                    Attribute userIdObject =
                            attributes.get(realmConfig.getUserStoreProperty(LDAPConstants.USER_ID_ATTRIBUTE));
                    String userIdAttributeValue = null;
                    if (userIdObject != null) {
                        userIdAttributeValue = resolveLdapAttributeValue(userIdObject.get());
                    }

                    String domain = this.getRealmConfiguration()
                            .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);

                    User user = getUser(userIdAttributeValue, userNamePropertyValue);
                    user.setDisplayName(null);
                    user.setUserStoreDomain(domain);
                    user.setTenantDomain(getTenantDomain(tenantId));
                    finalUserList.add(user);
                }
            }
        } catch (NamingException e) {
            log.error(String.format("Error occurred while getting user list from non group filter %s", e.getMessage()));
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            // Close the naming enumeration and free up resources
            JNDIUtil.closeNamingEnumeration(attrs);
        }
        return finalUserList;
    }

    /**
     * Parse the search result of group filtering and get the user list.
     * As it's membership group filtering, we retrieve all members of the requested group(s) and then
     * get the mutual members' out of it as a DN list.
     *
     * @param answer                  Answer.
     * @param returnedAttributes      Returned Attributes.
     * @param isSingleAttributeFilter Whether the original request is from a single attribute filter or a multi
     *                                attribute filter, so that AND operation can be omitted during the filtering
     *                                process.
     * @return A users list.
     * @throws UserStoreException
     */
    private List<User> getUserListFromMembershipGroupFilterResult(NamingEnumeration<SearchResult> answer,
                                                        List<String> returnedAttributes, boolean
                                                                isSingleAttributeFilter) throws UserStoreException {

        // User DN list.
        List<String> userListFromSearch = new ArrayList<>();
        // Multi group retrieval.
        int count = 0;
        NamingEnumeration<?> attrs = null;
        List<User> finalUserList;

        try {
            while (answer.hasMoreElements()) {
                count++;
                List<String> tempUserList = new ArrayList<>();
                SearchResult searchResult = answer.next();
                Attributes attributes = searchResult.getAttributes();
                if (attributes == null)
                    continue;
                NamingEnumeration attributeEntry;
                for (attributeEntry = attributes.getAll(); attributeEntry.hasMore(); ) {
                    Attribute valAttribute = (Attribute) attributeEntry.next();
                    if (isAttributeEqualsProperty(returnedAttributes.get(0), valAttribute.getID())) {
                        NamingEnumeration values;
                        for (values = valAttribute.getAll(); values.hasMore(); ) {
                            tempUserList.add(values.next().toString());
                        }
                    }
                }
                /*
                 When singleAttributeFilter is true, that implies that the request is a single attribute filter. In
                 this case, the intersection (AND operation) should not be performed on the filtered results.
                 Following IF block handles the single attribute filter.
                 */
                if (isSingleAttributeFilter) {
                    userListFromSearch.addAll(tempUserList);
                } else {
                    if (count == 1) {
                        userListFromSearch.addAll(tempUserList);
                    } else {
                        userListFromSearch.retainAll(tempUserList);
                    }
                }
            }
        } catch (NamingException e) {
            log.error(String.format("Error occurred while getting user list from group filter %s", e.getMessage()));
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            JNDIUtil.closeNamingEnumeration(attrs);
        }

        // We need iterate over users' DN list and get users.
        finalUserList = getUserListFromDNList(userListFromSearch);
        return finalUserList;
    }

    /**
     * Parse the search result of group filtering and get the user list.
     * As it's memberOf group filtering, directly get the user name list from search result.
     *
     * @param answer        LDAP search answer.
     * @return A users list.
     * @throws UserStoreException
     */
    private List<User> getUserListFromMemberOfGroupFilterResult(NamingEnumeration<SearchResult> answer)
            throws UserStoreException {

        List<User> finalUserList = new ArrayList<>();
        try {
            while (answer.hasMoreElements()) {
                SearchResult searchResult = answer.next();
                if (searchResult.getAttributes() != null) {
                    Attribute userName = searchResult.getAttributes().
                            get(realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE));
                    Attribute userID = searchResult.getAttributes().
                            get(realmConfig.getUserStoreProperty(LDAPConstants.USER_ID_ATTRIBUTE));
                    /*
                     * If this is a service principle, just ignore and
                     * iterate rest of the array. The entity is a service if
                     * value of surname is Service.
                     */
                    String serviceNameAttribute = "sn";
                    Attribute attrSurname = searchResult.getAttributes().get(serviceNameAttribute);

                    if (attrSurname != null) {
                        if (log.isDebugEnabled()) {
                            log.debug(serviceNameAttribute + " : " + attrSurname);
                        }
                        String serviceName = (String) attrSurname.get();
                        if (serviceName != null && serviceName
                                .equals(LDAPConstants.SERVER_PRINCIPAL_ATTRIBUTE_VALUE)) {
                            continue;
                        }
                    }
                    String name = null;
                    String displayName = null;
                    String id = null;
                    String domain = null;
                    if (userName != null) {
                        name = resolveLdapAttributeValue(userName.get());
                        domain = this.getRealmConfiguration()
                                .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
                    }
                    if (userID != null) {
                        id = resolveLdapAttributeValue(userID.get());
                    }
                    User user = getUser(id, name);
                    user.setDisplayName(displayName);
                    user.setUserStoreDomain(domain);
                    user.setTenantDomain(getTenantDomain(tenantId));
                    finalUserList.add(user);
                }
            }
        } catch (NamingException e) {
            log.error(String.format("Error occurred while getting user list from non group filter %s", e.getMessage()));
            throw new UserStoreException(e.getMessage(), e);
        }
        return finalUserList;
    }

    /**
     * Get user name list from DN list.
     *
     * @param userListFromSearch    User DN list obtained from search.
     * @return List of user objects.
     * @throws UserStoreException
     */
    private List<User> getUserListFromDNList(List<String> userListFromSearch) throws UserStoreException {

        List<User> usersList = new ArrayList<>();
        DirContext dirContext = this.connectionSource.getContext();
        String userNameProperty = realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE);
        String displayNameAttribute = realmConfig.getUserStoreProperty(LDAPConstants.DISPLAY_NAME_ATTRIBUTE);
        String userIdProperty = realmConfig.getUserStoreProperty(LDAPConstants.USER_ID_ATTRIBUTE);
        String[] requiredAttributes = {userNameProperty, displayNameAttribute, userIdProperty};

        for (String userFromSearch : userListFromSearch) {
            try {
                String displayName = null;
                String userName = null;
                String userId = null;
                Attributes userAttributes = dirContext.getAttributes
                        (escapeDNForSearch(userFromSearch), requiredAttributes);

                if (userAttributes != null) {
                    Attribute userNameAttribute = userAttributes.get(userNameProperty);
                    if (userNameAttribute != null) {
                        userName = (String) userNameAttribute.get();
                    }
                    if (StringUtils.isNotEmpty(displayNameAttribute)) {
                        Attribute displayAttribute = userAttributes.get(displayNameAttribute);
                        if (displayAttribute != null) {
                            displayName = (String) displayAttribute.get();
                        }
                    }
                    Attribute userIdAttribute = userAttributes.get(userIdProperty);
                    if (userIdAttribute != null) {
                        userId = resolveLdapAttributeValue(userIdAttribute.get());
                    }
                }
                String domainName =
                        realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
                /* Username will be null in the special case where the username attribute has changed to another
                and having different userNameProperty than the current user-mgt.xml. */
                if (userName != null) {
                    User user = getUser(userId, userName);
                    user.setDisplayName(displayName);
                    user.setUserStoreDomain(domainName);
                    user.setTenantDomain(getTenantDomain(tenantId));
                    usersList.add(user);
                } else {
                    // Skip listing users which are not applicable to current user-mgt.xml
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("User %s doesn't have the user name property %s", userFromSearch,
                                userNameProperty));
                    }
                }
            } catch (NamingException e) {
                log.error(String.format("Error in reading user information in the user store for the user %s, %s",
                        userFromSearch, e.getMessage()));
                throw new UserStoreException(e.getMessage(), e);
            }
        }
        return usersList;
    }

    /**
     * Generate paginated user list. Since LDAP doesn't support pagination with start index.
     * So we need to process the page results according to the requested start index.
     *
     * @param pageIndex    Index of the paginated page.
     * @param offset       Start index.
     * @param pageSize     Number of results per page which is equal to count/limit.
     * @param tempUserList Users in the particular indexed page.
     * @param users        Final paginated user list.
     */
    protected void generatePaginatedUserList(int pageIndex, int offset, int pageSize, List<User> tempUserList,
                                             List<User> users) {

        int needMore;
        // Handle pagination depends on given offset, i.e. start index.
        if (pageIndex == (offset / pageSize)) {
            int startPosition = (offset % pageSize);
            if (startPosition < tempUserList.size() - 1) {
                users.addAll(tempUserList.subList(startPosition, tempUserList.size()));
            } else if (startPosition == tempUserList.size() - 1) {
                users.add(tempUserList.get(tempUserList.size() - 1));
            }
        } else if (pageIndex == (offset / pageSize) + 1) {
            needMore = pageSize - users.size();
            if (tempUserList.size() >= needMore) {
                users.addAll(tempUserList.subList(0, needMore));
            } else {
                users.addAll(tempUserList);
            }
        }
    }

    /**
     * Post processing the user list, when found membership group filtering.
     *
     * @param isUsernameFiltering   Whether condition contains username filtering.
     * @param isClaimFiltering      Whether condition contains claim filtering.
     * @param expressionConditions  Expression conditions.
     * @param tempUserList          User list to be processed.
     * @return Final list of filtered users.
     * @throws UserStoreException
     */
    private List<User> membershipGroupFilterPostProcessing(boolean isUsernameFiltering, boolean isClaimFiltering,
                                                             List<ExpressionCondition> expressionConditions,
                                                             List<User> tempUserList) throws UserStoreException {

        List<User> users;
        if (isUsernameFiltering) {
            tempUserList = getMatchUsersFromMemberList(expressionConditions, tempUserList);
        }
        if (isClaimFiltering) {
            users = getUserListFromClaimFiltering(expressionConditions, tempUserList);
        } else {
            users = tempUserList;
        }
        return users;
    }

    /**
     * Post processing the user list, when found membership group filter with user name filtering.
     * Get match users from member list. When found username filtering.
     *
     * @param expressionConditions  Expression conditions.
     * @param userList              List of users to be filtered.
     * @return Filtered user list.
     */
    private List<User> getMatchUsersFromMemberList(List<ExpressionCondition> expressionConditions,
                                                     List<User> userList) {
        /*
        If group filtering and username filtering found, we need to get match users names only.
        'member' filtering retrieve all the members once the conditions matched because 'member' is a
        multi valued attribute.
        */
        List<User> derivedUserList = new ArrayList<>();

        for (ExpressionCondition expressionCondition : expressionConditions) {
            if (ExpressionAttribute.USERNAME.toString().equals(expressionCondition.getAttributeName())) {
                derivedUserList.addAll(getMatchUsers(expressionCondition, userList));
            }
        }
        LinkedHashSet<User> linkedHashSet = new LinkedHashSet<>();
        linkedHashSet.addAll(derivedUserList);
        derivedUserList.clear();
        derivedUserList.addAll(linkedHashSet);
        return derivedUserList;
    }

    /**
     * Get match users from given expression condition.
     *
     * @param expressionCondition   Expression condition.
     * @param users List of users to be filtered.
     * @return Filtered user list.
     */
    private List<User> getMatchUsers(ExpressionCondition expressionCondition, List<User> users) {

        List<User> newUsersList = new ArrayList<>();
        for (User user : users) {
            if (ExpressionOperation.SW.toString().equals(expressionCondition.getOperation())
                    && user.getUsername().startsWith(expressionCondition.getAttributeValue()) && !newUsersList.contains(user)) {
                newUsersList.add(user);
            } else if (ExpressionOperation.EQ.toString().equals(expressionCondition.getOperation())
                    && user.getUsername().equals(expressionCondition.getAttributeValue()) && !newUsersList.contains(user)) {
                newUsersList.add(user);
            } else if (ExpressionOperation.CO.toString().equals(expressionCondition.getOperation())
                    && user.getUsername().contains(expressionCondition.getAttributeValue()) && !newUsersList.contains(user)) {
                newUsersList.add(user);
            } else if (ExpressionOperation.EW.toString().equals(expressionCondition.getOperation())
                    && user.getUsername().endsWith(expressionCondition.getAttributeValue()) && !newUsersList.contains(user)) {
                newUsersList.add(user);
            }
        }
        return newUsersList;
    }

    /**
     * Post processing the user list, when membership group filter with claim filtering is found.
     *
     * @param expressionConditions  Expression conditions.
     * @param tempUserList          User list to be filtered.
     * @return Filtered user list.
     * @throws UserStoreException
     */
    private List<User> getUserListFromClaimFiltering(List<ExpressionCondition> expressionConditions,
                                                       List<User> tempUserList) throws UserStoreException {

        List<User> claimSearchUserList = new ArrayList<>();
        List<ExpressionCondition> derivedConditionList = expressionConditions;
        Iterator<ExpressionCondition> iterator = derivedConditionList.iterator();

        while (iterator.hasNext()) {
            ExpressionCondition expressionCondition = iterator.next();
            if (ExpressionAttribute.ROLE.toString().equals(expressionCondition.getAttributeName())) {
                iterator.remove();
            }
        }
        LDAPSearchSpecification claimSearch = new LDAPSearchSpecification(realmConfig, derivedConditionList);
        SearchControls claimSearchControls = claimSearch.getSearchControls();
        DirContext claimSearchDirContext = this.connectionSource.getContext();
        NamingEnumeration<SearchResult> tempAnswer = null;
        try {
            tempAnswer = claimSearchDirContext.search(claimSearch.getSearchBases(),
                    claimSearch.getSearchFilterQuery(), claimSearchControls);
            if (tempAnswer.hasMore()) {
                claimSearchUserList = getUserListFromNonGroupFilterResult(tempAnswer,
                        Arrays.asList(claimSearchControls.getReturningAttributes()));
            }
        } catch (NamingException e) {
            log.error(String.format("Error occurred while doing claim filtering for user(s) with filter: %s, %s",
                    claimSearch.getSearchFilterQuery(), e.getMessage()));
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            JNDIUtil.closeContext(claimSearchDirContext);
            JNDIUtil.closeNamingEnumeration(tempAnswer);
        }
        tempUserList.retainAll(claimSearchUserList);
        return tempUserList;
    }
}
