/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.user.api.Properties;
import org.wso2.carbon.user.api.Property;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.common.RoleContext;
import org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager;
import org.wso2.carbon.user.core.profile.ProfileConfigurationManager;
import org.wso2.carbon.user.core.tenant.Tenant;
import org.wso2.carbon.user.core.util.DatabaseUtil;
import org.wso2.carbon.user.core.util.JNDIUtil;
import org.wso2.carbon.user.core.util.LDAPUtil;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import javax.naming.AuthenticationException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.PartialResultException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;
import javax.sql.DataSource;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReadOnlyLDAPUserStoreManager extends AbstractUserStoreManager {

    private static Log log = LogFactory.getLog(ReadOnlyLDAPUserStoreManager.class);
    private final int MAX_USER_CACHE = 200;
    private boolean replaceEscapeCharacters = true;

    // Todo: use a cache provided by carbon kernel
    Map<String, String> userCache = new ConcurrentHashMap<String, String>(MAX_USER_CACHE);
    protected LDAPConnectionContext connectionSource = null;
    protected String userSearchBase = null;
    protected String groupSearchBase = null;

    /*
     * following is by default true since embedded-ldap allows it. If connected
     * to an external ldap
     * where empty roles not allowed, then following property should be set
     * accordingly in
     * user-mgt.xml
     */
    protected boolean emptyRolesAllowed = false;

    public ReadOnlyLDAPUserStoreManager() {

    }

    public ReadOnlyLDAPUserStoreManager(RealmConfiguration realmConfig,
                                        Map<String, Object> properties, ClaimManager claimManager,
                                        ProfileConfigurationManager profileManager,
                                        UserRealm realm, Integer tenantId)
            throws UserStoreException {
        this(realmConfig, properties, claimManager, profileManager, realm, tenantId, false);
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
    public ReadOnlyLDAPUserStoreManager(RealmConfiguration realmConfig,
                                        Map<String, Object> properties, ClaimManager claimManager,
                                        ProfileConfigurationManager profileManager,
                                        UserRealm realm, Integer tenantId, boolean skipInitData)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("Initialization Started " + System.currentTimeMillis());
        }

        this.realmConfig = realmConfig;
        this.claimManager = claimManager;
        this.userRealm = realm;
        this.tenantId = tenantId;

//		if (isReadOnly() && realmConfig.isPrimary()) {
//			String adminRoleName =
//			                       UserCoreUtil.removeDomainFromName(realmConfig.getAdminRoleName());
//			realmConfig.setAdminRoleName(UserCoreUtil.addInternalDomainName(adminRoleName));
//		}

        // check if required configurations are in the user-mgt.xml
        checkRequiredUserStoreConfigurations();

        dataSource = (DataSource) properties.get(UserCoreConstants.DATA_SOURCE);
        if (dataSource == null) {
            // avoid returning null
            dataSource = DatabaseUtil.getRealmDataSource(realmConfig);
        }
        if (dataSource == null) {
            throw new UserStoreException("Data Source is null");
        }
        properties.put(UserCoreConstants.DATA_SOURCE, dataSource);

		/*
         * obtain the ldap connection source that was created in
		 * DefaultRealmService.
		 */

        connectionSource = new LDAPConnectionContext(realmConfig);

        try {
            connectionSource.getContext();
            if (this.isReadOnly()) {
                log.info("LDAP connection created successfully in read-only mode");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new UserStoreException("Cannot create connection to LDAP server. Error message " +
                    e.getMessage());
        }
        this.userRealm = realm;
        this.persistDomain();
        doInitialSetup();
        if (realmConfig.isPrimary()) {
            addInitialAdminData(Boolean.parseBoolean(realmConfig.getAddAdmin()),
                    !isInitSetupDone());
        }
        /*
         * Initialize user roles cache as implemented in
         * AbstractUserStoreManager
         */
        initUserRolesCache();

        if (log.isDebugEnabled()) {
            log.debug("Initialization Ended " + System.currentTimeMillis());
        }
    }

    /**
     * This operates in the pure read-only mode without a connection to a
     * database. No handling of
     * Internal roles.
     */
    public ReadOnlyLDAPUserStoreManager(RealmConfiguration realmConfig, ClaimManager claimManager,
                                        ProfileConfigurationManager profileManager)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("Started " + System.currentTimeMillis());
        }
        this.realmConfig = realmConfig;
        this.claimManager = claimManager;

        // check if required configurations are in the user-mgt.xml
        checkRequiredUserStoreConfigurations();

        this.connectionSource = new LDAPConnectionContext(realmConfig);
    }

    /**
     * @throws UserStoreException
     */
    protected void checkRequiredUserStoreConfigurations() throws UserStoreException {

        log.debug("Checking LDAP configurations ");

        String connectionURL = realmConfig.getUserStoreProperty(LDAPConstants.CONNECTION_URL);
        String DNSURL = realmConfig.getUserStoreProperty(LDAPConstants.DNS_URL);

        if ((connectionURL == null || connectionURL.trim().length() == 0) &&
                ((DNSURL == null || DNSURL.trim().length() == 0))) {
            throw new UserStoreException(
                    "Required ConnectionURL property is not set at the LDAP configurations");
        }
        String connectionName = realmConfig.getUserStoreProperty(LDAPConstants.CONNECTION_NAME);
        if (connectionName == null || connectionName.trim().length() == 0) {
            throw new UserStoreException(
                    "Required ConnectionNme property is not set at the LDAP configurations");
        }
        String connectionPassword =
                realmConfig.getUserStoreProperty(LDAPConstants.CONNECTION_PASSWORD);
        if (connectionPassword == null || connectionPassword.trim().length() == 0) {
            throw new UserStoreException(
                    "Required ConnectionPassword property is not set at the LDAP configurations");
        }
        userSearchBase = realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);
        if (userSearchBase == null || userSearchBase.trim().length() == 0) {
            throw new UserStoreException(
                    "Required UserSearchBase property is not set at the LDAP configurations");
        }
        String usernameListFilter =
                realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_LIST_FILTER);
        if (usernameListFilter == null || usernameListFilter.trim().length() == 0) {
            throw new UserStoreException(
                    "Required UserNameListFilter property is not set at the LDAP configurations");
        }

        String usernameSearchFilter =
                realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_SEARCH_FILTER);
        if (usernameSearchFilter == null || usernameSearchFilter.trim().length() == 0) {
            throw new UserStoreException(
                    "Required UserNameSearchFilter property is not set at the LDAP configurations");
        }

        String usernameAttribute =
                realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE);
        if (usernameAttribute == null || usernameAttribute.trim().length() == 0) {
            throw new UserStoreException(
                    "Required UserNameAttribute property is not set at the LDAP configurations");
        }

        writeGroupsEnabled = false;

        // Groups properties
        if (realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.READ_GROUPS_ENABLED) != null) {
            readGroupsEnabled = Boolean.parseBoolean(realmConfig.
                    getUserStoreProperty(UserCoreConstants.RealmConfig.READ_GROUPS_ENABLED));
        }

        if (readGroupsEnabled) {
            groupSearchBase = realmConfig.getUserStoreProperty(LDAPConstants.GROUP_SEARCH_BASE);
            if (groupSearchBase == null || groupSearchBase.trim().length() == 0) {
                throw new UserStoreException(
                        "Required GroupSearchBase property is not set at the LDAP configurations");
            }
            String groupNameListFilter =
                    realmConfig.getUserStoreProperty(LDAPConstants.GROUP_NAME_LIST_FILTER);
            if (groupNameListFilter == null || groupNameListFilter.trim().length() == 0) {
                throw new UserStoreException(
                        "Required GroupNameListFilter property is not set at the LDAP configurations");
            }

            String groupNameSearchFilter =
                    realmConfig.getUserStoreProperty(LDAPConstants.ROLE_NAME_FILTER);
            if (groupNameSearchFilter == null || groupNameSearchFilter.trim().length() == 0) {
                throw new UserStoreException(
                        "Required GroupNameSearchFilter property is not set at the LDAP configurations");
            }

            String groupNameAttribute =
                    realmConfig.getUserStoreProperty(LDAPConstants.GROUP_NAME_ATTRIBUTE);
            if (groupNameAttribute == null || groupNameAttribute.trim().length() == 0) {
                throw new UserStoreException(
                        "Required GroupNameAttribute property is not set at the LDAP configurations");
            }
            String memebershipAttribute =
                    realmConfig.getUserStoreProperty(LDAPConstants.MEMBERSHIP_ATTRIBUTE);
            if (memebershipAttribute == null || memebershipAttribute.trim().length() == 0) {
                throw new UserStoreException(
                        "Required MembershipAttribute property is not set at the LDAP configurations");
            }
        }
    }

    /**
     *
     */
    public boolean doAuthenticate(String userName, Object credential) throws UserStoreException {

        boolean debug = log.isDebugEnabled();

        if (userName == null || credential == null) {
            return false;
        }

        userName = userName.trim();

        String password = (String) credential;
        password = password.trim();

        if (userName.equals("") || password.equals("")) {
            return false;
        }

        if (debug) {
            log.debug("Authenticating user " + userName);
        }

        boolean bValue = false;
        // check cached user DN first.
        String name = userCache.get(userName);
        if (name != null) {
            try {
                if (debug) {
                    log.debug("Cache hit. Using DN " + name);
                }
                bValue = this.bindAsUser(userName,name, (String) credential);
            } catch (NamingException e) {
                // do nothing if bind fails since we check for other DN
                // patterns as well.
                if (log.isDebugEnabled()) {
                    log.debug("Checking authentication with UserDN " + name + "failed " +
                            e.getMessage(), e);
                }
            }

            if (bValue) {
                return bValue;
            }
        }

        // read list of patterns from user-mgt.xml
        String patterns = realmConfig.getUserStoreProperty(LDAPConstants.USER_DN_PATTERN);

        if (patterns != null && !patterns.isEmpty()) {

            if (debug) {
                log.debug("Using UserDNPatterns " + patterns);
            }

            // if the property is present, split it using # to see if there are
            // multiple patterns specified.
            String[] userDNPatternList = patterns.split("#");
            if (userDNPatternList.length > 0) {
                for (String userDNPattern : userDNPatternList) {
                    name = MessageFormat.format(userDNPattern, userName);
                    if (debug) {
                        log.debug("Authenticating with " + name);
                    }
                    try {
                        if (name != null) {
                            bValue = this.bindAsUser(userName, name, (String) credential);
                            if (bValue) {
                                userCache.put(userName, name);
                                break;
                            }
                        }
                    } catch (NamingException e) {
                        // do nothing if bind fails since we check for other DN
                        // patterns as well.
                        if (log.isDebugEnabled()) {
                            log.debug("Checking authentication with UserDN " + userDNPattern +
                                    "failed " + e.getMessage(), e);
                        }
                    }
                }
            }
        } else {
            name = getNameInSpaceForUserName(userName);
            try {
                if (name != null) {
                    if (debug) {
                        log.debug("Authenticating with " + name);
                    }
                    bValue = this.bindAsUser(userName, name, (String) credential);
                    if (bValue) {
                        userCache.put(userName, name);
                    }
                }
            } catch (NamingException e) {
                log.debug(e.getMessage(), e);
                throw new UserStoreException(e.getMessage());
            }
        }

        return bValue;
    }

    /**
     * We do not have multiple profile support with LDAP.
     */
    public String[] getAllProfileNames() throws UserStoreException {
        return new String[]{UserCoreConstants.DEFAULT_PROFILE};
    }

    /**
     * We do not have multiple profile support with LDAP.
     */
    public String[] getProfileNames(String userName) throws UserStoreException {
        return new String[]{UserCoreConstants.DEFAULT_PROFILE};
    }

    /**
     *
     */
    public Map<String, String> getUserPropertyValues(String userName, String[] propertyNames,
                                                     String profileName) throws UserStoreException {

        String userDN = userCache.get(userName);

        if (userDN == null) {
            // read list of patterns from user-mgt.xml
            String patterns = realmConfig.getUserStoreProperty(LDAPConstants.USER_DN_PATTERN);

            if (patterns != null && !patterns.isEmpty()) {

                if (log.isDebugEnabled()) {
                    log.debug("Using User DN Patterns " + patterns);
                }

                if (patterns.contains("#")) {
                    userDN = getNameInSpaceForUserName(userName);
                } else {
                    userDN = MessageFormat.format(patterns, userName);
                }
            }
        }

        Map<String, String> values = new HashMap<String, String>();
        // if user name contains domain name, remove domain name
        String[] userNames = userName.split(CarbonConstants.DOMAIN_SEPARATOR);
        if (userNames.length > 1) {
            userName = userNames[1];
        }

        DirContext dirContext = this.connectionSource.getContext();
        String userSearchFilter = realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_SEARCH_FILTER);
        String searchFilter = userSearchFilter.replace("?", userName);

        NamingEnumeration<?> answer = null;
        NamingEnumeration<?> attrs = null;
        try {
            if (userDN != null) {
                SearchControls searchCtls = new SearchControls();
                searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
                if (propertyNames != null && propertyNames.length > 0) {
                    searchCtls.setReturningAttributes(propertyNames);
                }
                if (log.isDebugEnabled()) {
                    try {
                        log.debug("Searching for user with SearchFilter: " + searchFilter + " in SearchBase: " + dirContext.getNameInNamespace());
                    } catch (NamingException e) {
                        log.debug("Error while getting DN of search base", e);
                    }
                    if (propertyNames == null) {
                        log.debug("No attributes requested");
                    } else {
                        for (String attribute : propertyNames) {
                            log.debug("Requesting attribute :" + attribute);
                        }
                    }
                }
                try {
                    answer = dirContext.search(replaceEscapeCharacters(userName, userDN, false),
                            escapeLDAPSearchFilter(userName, searchFilter), searchCtls);
                } catch (NamingException e) {
                    log.debug(e.getMessage(), e);
                    throw new UserStoreException(e.getMessage());
                }
            } else {
                answer = this.searchForUser(userName, searchFilter, propertyNames, dirContext);
            }
            while (answer.hasMoreElements()) {
                SearchResult sr = (SearchResult) answer.next();
                Attributes attributes = sr.getAttributes();
                if (attributes != null) {
                    for (String name : propertyNames) {
                        if (name != null) {
                            Attribute attribute = attributes.get(name);
                            if (attribute != null) {
                                StringBuffer attrBuffer = new StringBuffer();
                                for (attrs = attribute.getAll(); attrs.hasMore(); ) {
                                    Object attObject = attrs.next();
                                    String attr = null;
                                    if (attObject instanceof String) {
                                        attr = (String) attObject;
                                    } else if (attObject instanceof byte[]) {
                                        //if the attribute type is binary base64 encoded string will be returned
                                        attr = new String(Base64.encodeBase64((byte[]) attObject));
                                    }

                                    if (attr != null && attr.trim().length() > 0) {
                                        attrBuffer.append(attr + ",");
                                    }
                                }
                                String value = attrBuffer.toString();
								/*
								 * Length needs to be more than one for a valid
								 * attribute, since we
								 * attach ",".
								 */
                                if (value != null && value.trim().length() > 1) {
                                    value = value.substring(0, value.length() - 1);
                                    values.put(name, value);
                                }
                            }
                        }
                    }
                }
            }

        } catch (NamingException e) {
            log.debug(e.getMessage(), e);
            throw new UserStoreException(e.getMessage());
        } finally {
            // close the naming enumeration and free up resources
            JNDIUtil.closeNamingEnumeration(attrs);
            JNDIUtil.closeNamingEnumeration(answer);
            // close directory context
            JNDIUtil.closeContext(dirContext);
        }
        return values;
    }

    /**
     *
     */
    public boolean doCheckExistingRole(String roleName) throws UserStoreException {

        RoleContext roleContext = createRoleContext(roleName);  // TODO if role Name with Shared Role?
        return isExistingLDAPRole(roleContext);

    }

    protected boolean isExistingLDAPRole(RoleContext context) throws UserStoreException {

        boolean debug = log.isDebugEnabled();
        boolean isExisting = false;
        String roleName = context.getRoleName();

        if (debug) {
            log.debug("Searching for role: " + roleName);
        }
        String searchFilter = ((LDAPRoleContext) context).getListFilter();
        String roleNameProperty = ((LDAPRoleContext) context).getRoleNameProperty();
        searchFilter = "(&" + searchFilter + "(" + roleNameProperty + "=" + roleName + "))";
        String searchBases = ((LDAPRoleContext) context).getSearchBase();

        if (debug) {
            log.debug("Using search filter: " + searchFilter);
        }
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchCtls.setReturningAttributes(new String[]{roleNameProperty});
        NamingEnumeration<SearchResult> answer = null;
        DirContext dirContext = null;

        try {
            dirContext = connectionSource.getContext();
            // with DN patterns
            if (((LDAPRoleContext) context).getRoleDNPatterns().size() > 0) {
                for (String pattern : ((LDAPRoleContext) context).getRoleDNPatterns()) {
                    if (debug) {
                        log.debug("Using pattern: " + pattern);
                    }
                    pattern = MessageFormat.format(pattern.trim(), roleName);
                    try {
                        answer = dirContext.search(pattern, searchFilter, searchCtls);
                    } catch (NamingException e) {
                        if (log.isDebugEnabled()) {
                            log.debug(e);
                        }
                        // ignore
                    }
                    if (answer != null && answer.hasMoreElements()) {
                        return true;
                    }
                }
            }
            //try out with handle multiple search bases
            String[] roleSearchBaseArray = searchBases.split("#");
            for (String searchBase : roleSearchBaseArray) {
                // no DN Patterns found
                if (debug) {
                    log.debug("Searching in " + searchBase);
                }
                try {
                    answer = dirContext.search(searchBase, searchFilter, searchCtls);
                    if (answer.hasMoreElements()) {
                        isExisting = true;
                        break;
                    }
                } catch (NamingException e) {
                    if (log.isDebugEnabled()) {
                        log.debug(e);
                    }
                    // ignore
                }
            }
        } finally {
            JNDIUtil.closeNamingEnumeration(answer);
            JNDIUtil.closeContext(dirContext);
        }
        if (debug) {
            log.debug("Is role: " + roleName + " exist: " + isExisting);
        }
        return isExisting;
    }

    public boolean doCheckExistingUser(String userName) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("Searching for user " + userName);
        }
        boolean bFound = false;
        String userSearchFilter = realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_SEARCH_FILTER);
        userSearchFilter = userSearchFilter.replace("?", userName);
        try {
            String searchBase = null;
            String userDN = userCache.get(userName);
            if(userDN == null){
                String userDNPattern = realmConfig.getUserStoreProperty(LDAPConstants.USER_DN_PATTERN);
                if (userDNPattern != null && userDNPattern.trim().length() > 0) {
                    String[] patterns = userDNPattern.split("#");
                    for (String pattern : patterns) {
                        searchBase = MessageFormat.format(pattern, userName);
                        userDN = getNameInSpaceForUserName(userName, searchBase, userSearchFilter);
                        if (userDN != null && userDN.length() > 0) {
                            bFound = true;
                            userCache.put(userName, userDN);
                            break;
                        }
                    }
                }
            } else {
                searchBase = MessageFormat.format(userDN, userName);
                userDN = getNameInSpaceForUserName(userName, searchBase, userSearchFilter);
                if (userDN != null && userDN.length() > 0) {
                    bFound = true;
                } else {
                    userCache.remove(userName);
                }
            }
            if(!bFound){
                searchBase = realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);
                userDN = getNameInSpaceForUserName(userName, searchBase, userSearchFilter);
                if(userDN != null && userDN.length() > 0){
                    bFound = true;
                }
            }
        } catch (Exception e) {
            String errorMessage = "Error occurred while checking existence of user : " + userName;
            throw new UserStoreException(errorMessage, e);
        }
        if (log.isDebugEnabled()) {
            log.debug("User: " + userName + " exist: " + bFound);
        }
        return bFound;
    }

    /**
     *
     */
    public String[] doListUsers(String filter, int maxItemLimit) throws UserStoreException {
        boolean debug = log.isDebugEnabled();
        String[] userNames = new String[0];

        if (maxItemLimit == 0) {
            return userNames;
        }

        int givenMax = UserCoreConstants.MAX_USER_ROLE_LIST;
        int searchTime = UserCoreConstants.MAX_SEARCH_TIME;

        try {
            givenMax =
                    Integer.parseInt(realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_MAX_USER_LIST));
        } catch (Exception e) {
            givenMax = UserCoreConstants.MAX_USER_ROLE_LIST;
        }

        try {
            searchTime =
                    Integer.parseInt(realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_MAX_SEARCH_TIME));
        } catch (Exception e) {
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

        StringBuffer searchFilter =
                new StringBuffer(
                        realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_LIST_FILTER));
        String searchBases = realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);

        String userNameProperty =
                realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE);

        String serviceNameAttribute = "sn";

        StringBuffer finalFilter = new StringBuffer();

        // read the display name attribute - if provided
        String displayNameAttribute =
                realmConfig.getUserStoreProperty(LDAPConstants.DISPLAY_NAME_ATTRIBUTE);

        String[] returnedAtts = null;

        if (displayNameAttribute != null) {
            returnedAtts =
                    new String[]{userNameProperty, serviceNameAttribute,
                            displayNameAttribute};
            finalFilter.append("(&").append(searchFilter).append("(").append(displayNameAttribute)
                    .append("=").append(filter).append("))");
        } else {
            returnedAtts = new String[]{userNameProperty, serviceNameAttribute};
            finalFilter.append("(&").append(searchFilter).append("(").append(userNameProperty).append("=")
                    .append(filter).append("))");
        }

        if (debug) {
            log.debug("Listing users. SearchBase: " + searchBases + " Constructed-Filter: " + finalFilter.toString());
            log.debug("Search controls. Max Limit: " + maxItemLimit + " Max Time: " + searchTime);
        }

        searchCtls.setReturningAttributes(returnedAtts);
        DirContext dirContext = null;
        NamingEnumeration<SearchResult> answer = null;
        List<String> list = new ArrayList<String>();

        try {
            dirContext = connectionSource.getContext();
            // handle multiple search bases
            String[] searchBaseArray = searchBases.split("#");

            for (String searchBase : searchBaseArray) {

                answer = dirContext.search(searchBase, escapeLDAPSearchFilter(filter, finalFilter.toString()), searchCtls);

                while (answer.hasMoreElements()) {
                    SearchResult sr = (SearchResult) answer.next();
                    if (sr.getAttributes() != null) {
                        log.debug("Result found ..");
                        Attribute attr = sr.getAttributes().get(userNameProperty);

						/*
						 * If this is a service principle, just ignore and
						 * iterate rest of the array. The entity is a service if
						 * value of surname is Service
						 */
                        Attribute attrSurname = sr.getAttributes().get(serviceNameAttribute);

                        if (attrSurname != null) {
                            if (debug) {
                                log.debug(serviceNameAttribute + " : " + attrSurname);
                            }
                            String serviceName = (String) attrSurname.get();
                            if (serviceName != null
                                    && serviceName
                                    .equals(LDAPConstants.SERVER_PRINCIPAL_ATTRIBUTE_VALUE)) {
                                continue;
                            }
                        }

						/*
						 * if display name is provided, read that attribute
						 */
                        Attribute displayName = null;
                        if (displayNameAttribute != null) {
                            displayName = sr.getAttributes().get(displayNameAttribute);
                            if (debug) {
                                log.debug(displayNameAttribute + " : " + displayName);
                            }
                        }

                        if (attr != null) {
                            String name = (String) attr.get();
                            String display = null;
                            if (displayName != null) {
                                display = (String) displayName.get();
                            }
                            // append the domain if exist
                            String domain = this.getRealmConfiguration().getUserStoreProperty(
                                    UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
                            // get the name in the format of
                            // domainName/userName|domainName/displayName
                            name = UserCoreUtil.getCombinedName(domain, name, display);
                            list.add(name);
                        }
                    }
                }
            }
            userNames = list.toArray(new String[list.size()]);
            Arrays.sort(userNames);

            if (debug) {
                for (String username : userNames) {
                    log.debug("result: " + username);
                }
            }

        } catch (NamingException e) {
            log.debug(e.getMessage(), e);
            throw new UserStoreException(e.getMessage());
        } finally {
            JNDIUtil.closeNamingEnumeration(answer);
            JNDIUtil.closeContext(dirContext);
        }
        return userNames;
    }

    @Override
    protected String[] doGetDisplayNamesForInternalRole(String[] userNames)
            throws UserStoreException {
        // search the user with UserNameAttribute, retrieve their
        // DisplayNameAttribute combine and return
        String displayNameAttribute =
                this.realmConfig.getUserStoreProperty(LDAPConstants.DISPLAY_NAME_ATTRIBUTE);
        if (displayNameAttribute != null) {
            String userNameAttribute =
                    this.realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE);
            String userSearchBase =
                    this.realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);
            String userNameListFilter =
                    this.realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_LIST_FILTER);

            String[] returningAttributes = {displayNameAttribute};
            SearchControls searchControls = new SearchControls();
            searchControls.setReturningAttributes(returningAttributes);

            List<String> combinedNames = new ArrayList<String>();
            if (userNames != null && userNames.length > 0) {
                for (String userName : userNames) {
                    String searchFilter =
                            "(&" + userNameListFilter + "(" + userNameAttribute +
                                    "=" + userName + "))";
                    List<String> displayNames =
                            this.getListOfNames(userName,userSearchBase, searchFilter,
                                    searchControls,
                                    displayNameAttribute, false);
                    // we expect only one display name
                    String name =
                            UserCoreUtil.getCombinedName(this.realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME),
                                    userName, displayNames.get(0));
                    combinedNames.add(name);
                }
                return combinedNames.toArray(new String[combinedNames.size()]);
            } else {
                return userNames;
            }
        } else {
            return userNames;
        }
    }

    /**
     * @param userName
     * @param dn
     * @param credentials
     * @return
     * @throws NamingException
     * @throws UserStoreException
     */
    protected boolean bindAsUser(String userName, String dn, String credentials) throws NamingException,
            UserStoreException {
        boolean isAuthed = false;
        boolean debug = log.isDebugEnabled();

		/*
		 * Hashtable<String, String> env = new Hashtable<String, String>();
		 * env.put(Context.INITIAL_CONTEXT_FACTORY, LDAPConstants.DRIVER_NAME);
		 * env.put(Context.SECURITY_PRINCIPAL, dn);
		 * env.put(Context.SECURITY_CREDENTIALS, credentials);
		 * env.put("com.sun.jndi.ldap.connect.pool", "true");
		 */
        /**
         * In carbon JNDI context we need to by pass specific tenant context and
         * we need the base
         * context for LDAP operations.
         */
        // env.put(CarbonConstants.REQUEST_BASE_CONTEXT, "true");

		/*
		 * String rawConnectionURL =
		 * realmConfig.getUserStoreProperty(LDAPConstants.CONNECTION_URL);
		 * String portInfo = rawConnectionURL.split(":")[2];
		 *
		 * String connectionURL = null;
		 * String port = null;
		 * // if the port contains a template string that refers to carbon.xml
		 * if ((portInfo.contains("${")) && (portInfo.contains("}"))) {
		 * port =
		 * Integer.toString(CarbonUtils.getPortFromServerConfig(portInfo));
		 * connectionURL = rawConnectionURL.replace(portInfo, port);
		 * }
		 * if (port == null) { // if not enabled, read LDAP url from
		 * user.mgt.xml
		 * connectionURL =
		 * realmConfig.getUserStoreProperty(LDAPConstants.CONNECTION_URL);
		 * }
		 */
		/*
		 * env.put(Context.PROVIDER_URL, connectionURL);
		 * env.put(Context.SECURITY_AUTHENTICATION, "simple");
		 */

        LdapContext cxt = null;
        try {
            // cxt = new InitialLdapContext(env, null);
            cxt = this.connectionSource.getContextWithCredentials(replaceEscapeCharacters(userName, dn, true), credentials);
            isAuthed = true;
        } catch (AuthenticationException e) {
			/*
			 * StringBuilder stringBuilder = new
			 * StringBuilder("Authentication failed for user ");
			 * stringBuilder.append(dn).append(" ").append(e.getMessage());
			 */

            // we avoid throwing an exception here since we throw that exception
            // in a one level above this.
            if (debug) {
                log.debug("Authentication failed " + e);
            }

        } finally {
            JNDIUtil.closeContext(cxt);
        }

        if (debug) {
            log.debug("User: " + dn + " is authenticated: " + isAuthed);
        }
        return isAuthed;
    }

    /**
     * @param value
     * @param searchFilter
     * @param returnedAtts
     * @param dirContext
     * @return
     * @throws UserStoreException
     */
    protected NamingEnumeration<SearchResult> searchForUser(String value, String searchFilter,
                                                            String[] returnedAtts,
                                                            DirContext dirContext)
            throws UserStoreException {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String searchBases = realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);
        if (returnedAtts != null && returnedAtts.length > 0) {
            searchCtls.setReturningAttributes(returnedAtts);
        }

        if (log.isDebugEnabled()) {
            try {
                log.debug("Searching for user with SearchFilter: " + searchFilter + " in SearchBase: " + dirContext.getNameInNamespace());
            } catch (NamingException e) {
                log.debug("Error while getting DN of search base", e);
            }
            if (returnedAtts == null) {
                log.debug("No attributes requested");
            } else {
                for (String attribute : returnedAtts) {
                    log.debug("Requesting attribute :" + attribute);
                }
            }
        }

        String[] searchBaseAraay = searchBases.split("#");
        NamingEnumeration<SearchResult> answer = null;

        try {
            for (String searchBase : searchBaseAraay) {
                answer = dirContext.search(searchBase, escapeLDAPSearchFilter(value, searchFilter), searchCtls);
                if (answer.hasMore()) {
                    return answer;
                }
            }
        } catch (NamingException e) {
            log.debug(e.getMessage(), e);
            throw new UserStoreException(e.getMessage());
        }
        return answer;
    }


    /**
     *
     */
    public void doAddRole(String roleName, String[] userList, boolean shared)
            throws UserStoreException {
        throw new UserStoreException(
                "User store is operating in read only mode. Cannot write into the user store.");
    }

    /**
     *
     */
    public void doUpdateRoleName(String roleName, String newRoleName) throws UserStoreException {
        throw new UserStoreException(
                "User store is operating in read only mode. Cannot write into the user store.");
    }

    /**
     * LDAP user store does not support bulk import.
     *
     * @return Always returns <code>false<code>.
     */
    public boolean isBulkImportSupported() {
        return false;
    }

    /**
     * This method is to check whether multiple profiles are allowed with a
     * particular user-store.
     * For an example, currently, JDBC user store supports multiple profiles and
     * where as ApacheDS
     * does not allow. LDAP currently does not allow multiple profiles.
     *
     * @return boolean
     */
    public boolean isMultipleProfilesAllowed() {
        return false;
    }

    /**
     *
     */
    public void doDeleteRole(String roleName) throws UserStoreException {
        throw new UserStoreException(
                "User store is operating in read only mode. Cannot write into the user store.");
    }

    /**
     * Returns the list of role names for the given search base and other
     * parameters
     *
     * @param searchTime
     * @param filter
     * @param maxItemLimit
     * @param searchFilter
     * @param roleNameProperty
     * @param searchBase
     * @param appendTenantDomain
     * @return
     * @throws UserStoreException
     */
    protected List<String> getLDAPRoleNames(int searchTime, String filter, int maxItemLimit,
                                            String searchFilter, String roleNameProperty,
                                            String searchBase, boolean appendTenantDomain)
            throws UserStoreException {
        boolean debug = log.isDebugEnabled();
        List<String> roles = new ArrayList<String>();

        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchCtls.setCountLimit(maxItemLimit);
        searchCtls.setTimeLimit(searchTime);

        String returnedAtts[] = {roleNameProperty};
        searchCtls.setReturningAttributes(returnedAtts);

        // / search filter TODO
        StringBuffer finalFilter = new StringBuffer();
        finalFilter.append("(&").append(searchFilter).append("(").append(roleNameProperty).append("=")
                .append(filter).append("))");

        if (debug) {
            log.debug("Listing roles. SearchBase: " + searchBase + " ConstructedFilter: " +
                    finalFilter.toString());
        }

        DirContext dirContext = null;
        NamingEnumeration<SearchResult> answer = null;

        try {
            dirContext = connectionSource.getContext();
            answer = dirContext.search(searchBase, escapeLDAPSearchFilter(filter, finalFilter.toString()), searchCtls);
            // append the domain if exist
            String domain =
                    this.getRealmConfiguration()
                            .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);

            while (answer.hasMoreElements()) {
                SearchResult sr = (SearchResult) answer.next();
                if (sr.getAttributes() != null) {
                    Attribute attr = sr.getAttributes().get(roleNameProperty);
                    if (attr != null) {
                        String name = (String) attr.get();
                        name = UserCoreUtil.addDomainToName(name, domain);
                        if (appendTenantDomain) {
                            String dn = sr.getNameInNamespace();
                            name = UserCoreUtil.addTenantDomainToEntry(name,
                                    getTenantDomainFromRoleDN(dn, name));
                        }
                        roles.add(name);
                    }
                }
            }
        } catch (NamingException e) {
            log.debug(e);
            throw new UserStoreException(e.getMessage());
        } finally {
            JNDIUtil.closeNamingEnumeration(answer);
            JNDIUtil.closeContext(dirContext);
        }

        if (debug) {
            Iterator<String> rolesIte = roles.iterator();
            while (rolesIte.hasNext()) {
                log.debug("result: " + rolesIte.next());
            }
        }

        return roles;
    }

    /**
     * Get the tenant domain for the provided distinguished name. If the role is
     * not a shared role returns the super tenant domain
     *
     * @param dn
     * @param roleName
     * @return
     */
    private String getTenantDomainFromRoleDN(String dn, String roleName) {

        dn = dn.toLowerCase();
        roleName = roleName.toLowerCase();
        String sharedSearchBase = realmConfig.getUserStoreProperties().
                get(LDAPConstants.SHARED_GROUP_SEARCH_BASE);

        sharedSearchBase = sharedSearchBase.toLowerCase();
        if (dn.indexOf(sharedSearchBase) > -1) {
            dn = dn.replaceAll(sharedSearchBase, "");
            dn = dn.replace(realmConfig.getUserStoreProperty(LDAPConstants.SHARED_GROUP_NAME_ATTRIBUTE).
                    toLowerCase() + "=" + roleName, "");
            if (dn.indexOf(",") == 0) {
                dn = dn.substring(1);
            }
            int lastIndex = dn.indexOf(",");
            if (lastIndex > -1 && lastIndex == dn.length() - 1) {
                dn = dn.substring(0, dn.length() - 1);
            }

            String groupNameAttributeName = realmConfig.
                    getUserStoreProperty(LDAPConstants.SHARED_TENANT_NAME_ATTRIBUTE).toLowerCase();
            dn = dn.replaceAll(groupNameAttributeName + "=", "");
            if (dn == null || dn.isEmpty()) {
                dn = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
            }
            return dn;
        } else {
            return CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        }
    }

    /**
     * Removes the shared roles relevant to the provided tenant domain
     *
     * @param sharedRoles
     * @param tenantDomain
     */
    protected void filterSharedRoles(List<String> sharedRoles, String tenantDomain) {
        tenantDomain = tenantDomain.toLowerCase();
        if (tenantDomain != null) {
            for (Iterator<String> i = sharedRoles.iterator(); i.hasNext(); ) {
                String role = i.next();
                if (role.toLowerCase().indexOf(tenantDomain) > -1) {
                    i.remove();
                }
            }
        }
    }


    /**
     *
     */
    public String[] doGetRoleNames(String filter, int maxItemLimit) throws UserStoreException {

        if (maxItemLimit == 0) {
            return new String[0];
        }

        int givenMax = UserCoreConstants.MAX_USER_ROLE_LIST;

        int searchTime = UserCoreConstants.MAX_SEARCH_TIME;

        try {
            givenMax = Integer.parseInt(realmConfig.
                    getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_MAX_ROLE_LIST));
        } catch (Exception e) {
            givenMax = UserCoreConstants.MAX_USER_ROLE_LIST;
        }

        try {
            searchTime = Integer.parseInt(realmConfig.
                    getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_MAX_SEARCH_TIME));
        } catch (Exception e) {
            searchTime = UserCoreConstants.MAX_SEARCH_TIME;
        }

        if (maxItemLimit < 0 || maxItemLimit > givenMax) {
            maxItemLimit = givenMax;
        }

        List<String> externalRoles = new ArrayList<String>();

        if (readGroupsEnabled) {

            // handling multiple search bases
            String searchBases = realmConfig.getUserStoreProperty(LDAPConstants.GROUP_SEARCH_BASE);
            String[] searchBaseArray = searchBases.split("#");
            for (String searchBase : searchBaseArray) {
                // get the role list from the group search base
                externalRoles.addAll(getLDAPRoleNames(searchTime, filter, maxItemLimit,
                        realmConfig.getUserStoreProperty(LDAPConstants.GROUP_NAME_LIST_FILTER),
                        realmConfig.getUserStoreProperty(LDAPConstants.GROUP_NAME_ATTRIBUTE),
                        searchBase, false));
            }

            // get the role list from the shared group search base

//			if (isSharedGroupEnabled()) {
//				List<String> sharedRoleNames = new ArrayList<String>();
//				sharedRoleNames.addAll(Arrays.asList(doGetSharedRoleNames(null,filter, maxItemLimit)));
//
//				filterSharedRoles(sharedRoleNames, CarbonContext.getCurrentContext()
//				                                                .getTenantDomain());
//				externalRoles.addAll(sharedRoleNames);
//			}
        }

        return externalRoles.toArray(new String[externalRoles.size()]);
    }


    @Override
    protected String[] doGetSharedRoleNames(String tenantDomain, String filter, int maxItemLimit)
            throws UserStoreException {

        if (!isSharedGroupEnabled()) {
            return new String[0];
        }

        if (maxItemLimit == 0) {
            return new String[0];
        }

        int givenMax = UserCoreConstants.MAX_USER_ROLE_LIST;

        int searchTime = UserCoreConstants.MAX_SEARCH_TIME;

        try {
            givenMax = Integer.parseInt(realmConfig.
                    getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_MAX_ROLE_LIST));
        } catch (Exception e) {
            givenMax = UserCoreConstants.MAX_USER_ROLE_LIST;
        }

        try {
            searchTime = Integer.parseInt(realmConfig.
                    getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_MAX_SEARCH_TIME));
        } catch (Exception e) {
            searchTime = UserCoreConstants.MAX_SEARCH_TIME;
        }

        if (maxItemLimit < 0 || maxItemLimit > givenMax) {
            maxItemLimit = givenMax;
        }

        String searchBase = null;

        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(tenantDomain)) {
            searchBase = realmConfig.getUserStoreProperty(LDAPConstants.SHARED_GROUP_SEARCH_BASE);
        } else {
            String groupNameAttributeName =
                    realmConfig.getUserStoreProperty(LDAPConstants.SHARED_TENANT_NAME_ATTRIBUTE);
            if (groupNameAttributeName == null || groupNameAttributeName.trim().length() == 0) {
                groupNameAttributeName = "ou";
            }
            searchBase = groupNameAttributeName + "=" + tenantDomain + "," +
                    realmConfig.getUserStoreProperty(LDAPConstants.SHARED_GROUP_SEARCH_BASE);
        }

        List<String> sharedRoleNames = getLDAPRoleNames(searchTime, filter, maxItemLimit,
                realmConfig.getUserStoreProperty(LDAPConstants.SHARED_GROUP_NAME_LIST_FILTER),
                realmConfig.getUserStoreProperty(LDAPConstants.SHARED_GROUP_NAME_ATTRIBUTE),
                searchBase, true);

        filterSharedRoles(sharedRoleNames, CarbonContext.getThreadLocalCarbonContext().getTenantDomain());
        return sharedRoleNames.toArray(new String[sharedRoleNames.size()]);

    }

    /**
     *
     */
    public RealmConfiguration getRealmConfiguration() {
        return this.realmConfig;
    }


    /**
     *
     */
    public String[] doGetUserListOfRole(String roleName, String filter) throws UserStoreException {

        RoleContext roleContext = createRoleContext(roleName);
        return getUserListOfLDAPRole(roleContext, filter);
    }

    /**
     *
     */
    public String[] getUserListOfLDAPRole(RoleContext context, String filter) throws UserStoreException {

        boolean debug = log.isDebugEnabled();

        if (debug) {
            log.debug("Getting user list of role: " + context.getRoleName() + " with filter: " + filter);
        }

        List<String> userList = new ArrayList<String>();
        String[] names = new String[0];
        int givenMax = UserCoreConstants.MAX_USER_ROLE_LIST;
        int searchTime = UserCoreConstants.MAX_SEARCH_TIME;

        try {
            givenMax =
                    Integer.parseInt(realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_MAX_USER_LIST));
        } catch (Exception e) {
            givenMax = UserCoreConstants.MAX_USER_ROLE_LIST;
        }

        try {
            searchTime =
                    Integer.parseInt(realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_MAX_SEARCH_TIME));
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
            searchFilter = "(&" + searchFilter + "(" + roleNameProperty + "=" + context.getRoleName() + "))";

            String membershipProperty = realmConfig.getUserStoreProperty(LDAPConstants.MEMBERSHIP_ATTRIBUTE);
            String returnedAtts[] = {membershipProperty};
            searchCtls.setReturningAttributes(returnedAtts);

            List<String> userDNList = new ArrayList<String>();

            SearchResult sr = null;
            dirContext = connectionSource.getContext();

            // with DN patterns
            if (((LDAPRoleContext) context).getRoleDNPatterns().size() > 0) {
                for (String pattern : ((LDAPRoleContext) context).getRoleDNPatterns()) {
                    if (debug) {
                        log.debug("Using pattern: " + pattern);
                    }
                    pattern = MessageFormat.format(pattern.trim(), context.getRoleName());
                    try {
                        answer = dirContext.search(pattern, searchFilter, searchCtls);
                        if (answer.hasMore()) {
                            sr = (SearchResult) answer.next();
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
                    if (debug) {
                        log.debug("Searching role: " + context.getRoleName() + " SearchBase: "
                                + searchBase + " SearchFilter: " + searchFilter);
                    }

                    try {
                        // read the DN of users who are members of the group
                        answer = dirContext.search(searchBase, searchFilter, searchCtls);
                        int count = 0;
                        if (answer.hasMore()) { // to check if there is a result
                            while (answer.hasMore()) { // to check if there are more than one group
                                if (count > 0) {
                                    throw new UserStoreException("More than one group exist with name");
                                }
                                sr = (SearchResult) answer.next();
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

            if (debug) {
                log.debug("Found role: " + sr.getNameInNamespace());
            }

            // read the member attribute and get DNs of the users
            Attributes attributes = sr.getAttributes();
            if (attributes != null) {
                NamingEnumeration attributeEntry = null;
                for (attributeEntry = attributes.getAll(); attributeEntry.hasMore(); ) {
                    Attribute valAttribute = (Attribute) attributeEntry.next();
                    if (membershipProperty == null || membershipProperty.equals(valAttribute.getID())) {
                        NamingEnumeration values = null;
                        for (values = valAttribute.getAll(); values.hasMore(); ) {
                            String value = values.next().toString();
                            userDNList.add(value);

                            if (debug) {
                                log.debug("Found attribute: " + membershipProperty + " value: " + value);
                            }
                        }
                    }
                }
            }

            // iterate over users' DN list and get userName and display name
            // attribute values

            String userNameProperty = realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE);
            String displayNameAttribute = realmConfig
                    .getUserStoreProperty(LDAPConstants.DISPLAY_NAME_ATTRIBUTE);
            String[] returnedAttributes = {userNameProperty, displayNameAttribute};

            for (String user : userDNList) {
                if (debug) {
                    log.debug("Getting name attributes of: " + user);
                }

                Attributes userAttributes;
                try {
                    // '\' and '"' characters need another level of escaping before searching
                    userAttributes = dirContext.getAttributes(user.replace("\\\\", "\\\\\\")
                            .replace("\\\"", "\\\\\""), returnedAttributes);

                    String displayName = null;
                    String userName = null;
                    if (userAttributes != null) {
                        Attribute userNameAttribute = userAttributes.get(userNameProperty);
                        if (userNameAttribute != null) {
                            userName = (String) userNameAttribute.get();
                            if (debug) {
                                log.debug("UserName: " + userName);
                            }
                        }
                        if (displayNameAttribute != null) {
                            Attribute displayAttribute = userAttributes.get(displayNameAttribute);
                            if (displayAttribute != null) {
                                displayName = (String) displayAttribute.get();
                            }
                            if (debug) {
                                log.debug("DisplayName: " + displayName);
                            }
                        }
                    }
                    String domainName =
                            realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);

                    // Username will be null in the special case where the
                    // username attribute has changed to another
                    // and having different userNameProperty than the current
                    // user-mgt.xml
                    if (userName != null) {
                        user = UserCoreUtil.getCombinedName(domainName, userName, displayName);
                        userList.add(user);
                        if (debug) {
                            log.debug(user + " is added to the result list");
                        }
                    }
                    // Skip listing users which are not applicable to current
                    // user-mgt.xml
                    else {
                        if (log.isDebugEnabled()) {
                            log.debug("User " + user + " doesn't have the user name property : " +
                                    userNameProperty);
                        }
                    }

                } catch (NamingException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Error in reading user information in the user store for the user " +
                                user + e.getMessage(), e);
                    }
                }

            }
            names = userList.toArray(new String[userList.size()]);

        } catch (PartialResultException e) {
            // can be due to referrals in AD. so just ignore error
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage(), e);
            }
        } catch (NamingException e) {
            log.debug(e.getMessage(), e);
            throw new UserStoreException("Error in reading user information in the user store.");
        } finally {
            JNDIUtil.closeNamingEnumeration(answer);
            JNDIUtil.closeContext(dirContext);
        }

        return names;
    }

    /**
     * This method will check whether back link support is enabled and will
     * return the effective
     * search base. Read http://www.frickelsoft.net/blog/?p=130 for more
     * details.
     *
     * @param shared whether share search based or not
     * @return The search base based on back link support. If back link support
     * is enabled this will
     * return user search base, else group search base.
     */
    protected String getEffectiveSearchBase(boolean shared) {

        String backLinksEnabled =
                realmConfig.getUserStoreProperty(LDAPConstants.BACK_LINKS_ENABLED);
        boolean isBackLinkEnabled = false;

        if (backLinksEnabled != null && !backLinksEnabled.equals("")) {
            isBackLinkEnabled = Boolean.parseBoolean(backLinksEnabled);
        }

        if (isBackLinkEnabled) {
            return realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);
        } else {
            if (shared) {
                return realmConfig.getUserStoreProperty(LDAPConstants.SHARED_GROUP_SEARCH_BASE);
            } else {
                return realmConfig.getUserStoreProperty(LDAPConstants.GROUP_SEARCH_BASE);
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    protected String[] getLDAPRoleListOfUser(String userName, String filter, String searchBase,
                                             boolean shared) throws UserStoreException {
        boolean debug = log.isDebugEnabled();
        List<String> list = new ArrayList<String>();
		/*
		 * do not search REGISTRY_ANONNYMOUS_USERNAME or
		 * REGISTRY_SYSTEM_USERNAME in LDAP because it
		 * causes warn logs printed from embedded-ldap.
		 */
        if (readGroupsEnabled && (!UserCoreUtil.isRegistryAnnonymousUser(userName)) &&
                (!UserCoreUtil.isRegistrySystemUser(userName))) {

            SearchControls searchCtls = new SearchControls();
            searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);

            String memberOfProperty =
                    realmConfig.getUserStoreProperty(LDAPConstants.MEMBEROF_ATTRIBUTE);
            if (memberOfProperty != null && memberOfProperty.length() > 0) {
                // TODO Handle active directory shared roles logics here

                String userNameProperty =
                        realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE);
                String userSearchFilter = realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_SEARCH_FILTER);
                String searchFilter = userSearchFilter.replace("?", userName);

                String binaryAttribute =
                        realmConfig.getUserStoreProperty(LDAPConstants.LDAP_ATTRIBUTES_BINARY);
                String primaryGroupId =
                        realmConfig.getUserStoreProperty(LDAPConstants.PRIMARY_GROUP_ID);

                String returnedAtts[] = {memberOfProperty};

                if (binaryAttribute != null && primaryGroupId != null) {
                    returnedAtts =
                            new String[]{memberOfProperty, binaryAttribute, primaryGroupId};
                }

                searchCtls.setReturningAttributes(returnedAtts);

                if (debug) {
                    log.debug("Reading roles with the memberOfProperty Property: " + memberOfProperty);
                }

                if (binaryAttribute != null && primaryGroupId != null) {
                    list =
                            this.getAttributeListOfOneElementWithPrimarGroup(userName,
                                    searchBase,
                                    searchFilter,
                                    searchCtls,
                                    binaryAttribute,
                                    primaryGroupId,
                                    userNameProperty,
                                    memberOfProperty);
                } else {
                    // use cache
                    String cachedDN = userCache.get(userName);
                    if (cachedDN != null) {
                        searchBase = cachedDN;
                    } else {
                        // create DN directly   but there is no way when multiple DNs are used. Need to improve letter
                        String userDNPattern = realmConfig.getUserStoreProperty(LDAPConstants.USER_DN_PATTERN);
                        if (userDNPattern != null && !userDNPattern.contains("#")) {
                            searchBase = MessageFormat.format(userDNPattern, userName);
                        }
                    }

                    // get DNs of the groups to which this user belongs
                    List<String> groupDNs = this.getListOfNames(userName,searchBase, searchFilter,
                            searchCtls, memberOfProperty, false);
					/*
					 * to be compatible with AD as well, we need to do a search
					 * over the groups and
					 * find those groups' attribute value defined for group name
					 * attribute and
					 * return
					 */
                    list = this.getGroupNameAttributeValuesOfGroups(groupDNs);
                }
            } else {

                // Load normal roles with the user
                String searchFilter;
                String roleNameProperty;

                if (shared) {
                    searchFilter = realmConfig.
                            getUserStoreProperty(LDAPConstants.SHARED_GROUP_NAME_LIST_FILTER);
                    roleNameProperty =
                            realmConfig.getUserStoreProperty(LDAPConstants.SHARED_GROUP_NAME_ATTRIBUTE);
                } else {
                    searchFilter = realmConfig.getUserStoreProperty(LDAPConstants.GROUP_NAME_LIST_FILTER);
                    roleNameProperty =
                            realmConfig.getUserStoreProperty(LDAPConstants.GROUP_NAME_ATTRIBUTE);
                }

                String membershipProperty =
                        realmConfig.getUserStoreProperty(LDAPConstants.MEMBERSHIP_ATTRIBUTE);
                String userDNPattern = realmConfig.getUserStoreProperty(LDAPConstants.USER_DN_PATTERN);
                String nameInSpace;
                if (userDNPattern != null && !userDNPattern.contains("#")) {
                    nameInSpace = MessageFormat.format(userDNPattern, userName);
                } else {
                    nameInSpace = this.getNameInSpaceForUserName(userName);
                }
                // read the roles with this membership property

                if (membershipProperty == null || membershipProperty.length() < 1) {
                    throw new UserStoreException(
                            "Please set member of attribute or membership attribute");
                }

                searchFilter =
                        "(&" + searchFilter + "(" + membershipProperty + "=" + nameInSpace +
                                "))";
                String returnedAtts[] = {roleNameProperty};
                searchCtls.setReturningAttributes(returnedAtts);

                if (debug) {
                    log.debug("Reading roles with the membershipProperty Property: " + membershipProperty);
                }

                list = this.getListOfNames(userName, searchBase, searchFilter, searchCtls, roleNameProperty, false);
            }
        } else if (UserCoreUtil.isRegistryAnnonymousUser(userName)) {
            // returning a REGISTRY_ANONNYMOUS_ROLE_NAME for
            // REGISTRY_ANONNYMOUS_USERNAME
            list.add(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME);
        }

        String[] result = list.toArray(new String[list.size()]);

        if (result != null) {
            for (String rolename : result) {
                log.debug("Found role: " + rolename);
            }
        }
        return result;
    }

    @Override
    protected String[] doGetExternalRoleListOfUser(String userName, String filter) throws UserStoreException {

        // Get the effective search base
        String searchBase = this.getEffectiveSearchBase(false);
        return getLDAPRoleListOfUser(userName, filter, searchBase, false);
    }


    @Override
    protected String[] doGetSharedRoleListOfUser(String userName,
                                                 String tenantDomain, String filter) throws UserStoreException {
        // Get the effective search base
        String searchBase = this.getEffectiveSearchBase(true);
        if (tenantDomain != null && tenantDomain.trim().length() > 0) {
            if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(tenantDomain.trim())) {
                String groupNameAttributeName =
                        realmConfig.getUserStoreProperty(LDAPConstants.SHARED_TENANT_NAME_ATTRIBUTE);
                if (groupNameAttributeName == null || groupNameAttributeName.trim().length() == 0) {
                    groupNameAttributeName = "ou";
                }
                searchBase = groupNameAttributeName + "=" + tenantDomain + "," + searchBase;
            }
        }
        return getLDAPRoleListOfUser(userName, filter, searchBase, true);
    }

    /**
     * {@inheritDoc}
     */

    public boolean isReadOnly() throws UserStoreException {
        return true;
    }

    /**
     * @param userName
     * @return
     * @throws UserStoreException
     */
    protected String getNameInSpaceForUserName(String userName) throws UserStoreException {
        // check the cache first
        String name = userCache.get(userName);
        if (name != null) {
            return name;
        }

        String searchBase = null;
        String userSearchFilter = realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_SEARCH_FILTER);
        userSearchFilter = userSearchFilter.replace("?", userName);
        String userDNPattern = realmConfig.getUserStoreProperty(LDAPConstants.USER_DN_PATTERN);
        if (userDNPattern != null && userDNPattern.trim().length() > 0) {
            String[] patterns = userDNPattern.split("#");
            for (String pattern : patterns) {
                searchBase = MessageFormat.format(pattern, userName);
                String userDN = getNameInSpaceForUserName(userName, searchBase, userSearchFilter);
                // check in another DN pattern
                if (userDN != null) {
                    return userDN;
                }
            }
        }

        searchBase = realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);

        return getNameInSpaceForUserName(userName, searchBase, userSearchFilter);

    }

    /**
     * @param userName
     * @param searchBase
     * @param searchFilter
     * @return
     * @throws UserStoreException
     */
    protected String getNameInSpaceForUserName(String userName, String searchBase, String searchFilter) throws UserStoreException {
        boolean debug = log.isDebugEnabled();

        String userDN = null;

        DirContext dirContext = this.connectionSource.getContext();
        NamingEnumeration<SearchResult> answer = null;
        try {
            SearchControls searchCtls = new SearchControls();
            searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);

            if (log.isDebugEnabled()) {
                try {
                    log.debug("Searching for user with SearchFilter: " + searchFilter + " in SearchBase: " + dirContext.getNameInNamespace());
                } catch (NamingException e) {
                    log.debug("Error while getting DN of search base", e);
                }
            }
            SearchResult userObj = null;
            String[] searchBases = searchBase.split("#");
            for (String base : searchBases) {
                answer = dirContext.search(replaceEscapeCharacters(userName, base, false),
                        escapeLDAPSearchFilter(userName, searchFilter), searchCtls);
                if (answer.hasMore()) {
                    userObj = (SearchResult) answer.next();
                    if (userObj != null) {
                        userDN = userObj.getNameInNamespace().replace("\\\\", "\\")
                                .replace("\\+", "+")
                                .replace("\\,", ",")
                                .replace("\\;", ";")
                                .replace("\\>", ">")
                                .replace("\\<", "<")
                                .replace("\\\"", "\""); //reverting LDAP escapes before writing the DN to cache
                        break;
                    }
                }
            }
            if (userDN != null) {
                userCache.put(userName, userDN);
            }
            if (debug) {
                log.debug("Name in space for " + userName + " is " + userDN);
            }
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        } finally {
            JNDIUtil.closeNamingEnumeration(answer);
            JNDIUtil.closeContext(dirContext);
        }
        return userDN;
    }

    /**
     * @param sr
     * @param groupAttributeName
     * @return
     */
    private List<String> parseSearchResult(SearchResult sr, String groupAttributeName) {
        List<String> list = new ArrayList<String>();
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

    /**
     * @param userName
     * @param searchBase
     * @param searchFilter
     * @param searchCtls
     * @param objectSid
     * @param primaryGroupID
     * @param userAttributeId
     * @param groupAttributeName
     * @return
     * @throws UserStoreException
     */
    private List<String> getAttributeListOfOneElementWithPrimarGroup(String userName,
                                                                     String searchBase,
                                                                     String searchFilter,
                                                                     SearchControls searchCtls,
                                                                     String objectSid,
                                                                     String primaryGroupID,
                                                                     String userAttributeId,
                                                                     String groupAttributeName)
            throws UserStoreException {
        boolean debug = log.isDebugEnabled();

        List<String> list = new ArrayList<String>();
        DirContext dirContext = null;
        NamingEnumeration<SearchResult> answer = null;

        if (debug) {
            log.debug("GetAttributeListOfOneElementWithPrimarGroup. SearchBase: " + searchBase + " SearchFilter: " + searchFilter);
        }
        try {
            dirContext = connectionSource.getContext();
            answer = dirContext.search(replaceEscapeCharacters(userName, searchBase, false),
                    escapeLDAPSearchFilter(userName, searchFilter), searchCtls);
            int count = 0;
            while (answer.hasMore()) {
                if (count > 0) {
                    log.error("More than one user exist with name");
                    throw new UserStoreException("More than one user exist with name");
                }
                SearchResult sr = (SearchResult) answer.next();
                count++;

                list = parseSearchResult(sr, groupAttributeName);

                String primaryGroupSID = LDAPUtil.getPrimaryGroupSID(sr, objectSid, primaryGroupID);
                String primaryGroupName =
                        LDAPUtil.findGroupBySID(dirContext, searchBase,
                                primaryGroupSID, userAttributeId);
                if (primaryGroupName != null) {
                    list.add(primaryGroupName);
                }
            }

        } catch (PartialResultException e) {
            // can be due to referrals in AD. so just ignore error
            if (log.isDebugEnabled()) {
                log.debug("LDAP", e);
            }
        } catch (NamingException e) {
            log.debug(e.getMessage(), e);
            throw new UserStoreException(e.getMessage());
        } finally {
            JNDIUtil.closeNamingEnumeration(answer);
            JNDIUtil.closeContext(dirContext);
        }

        if (debug) {
            log.debug("GetAttributeListOfOneElementWithPrimarGroup. SearchBase: " + searchBase + " SearchFilter: " + searchFilter);
            Iterator<String> ite = list.iterator();
            while (ite.hasNext()) {
                log.debug("result: " + ite.next());
            }
        }
        return list;
    }

    // ****************************************************

    @SuppressWarnings("rawtypes")
    protected List<String> getAttributeListOfOneElement(String userName, String searchBases,
                                                        String searchFilter, SearchControls searchCtls)
            throws UserStoreException {
        List<String> list = new ArrayList<String>();
        DirContext dirContext = null;
        NamingEnumeration<SearchResult> answer = null;
        try {
            dirContext = connectionSource.getContext();
            // handle multiple search bases
            String[] searchBaseArray = searchBases.split("#");
            for (String searchBase : searchBaseArray) {
                try {
                    answer = dirContext.search(replaceEscapeCharacters(userName, searchBase, false),
                            escapeLDAPSearchFilter(userName, searchFilter), searchCtls);
                    int count = 0;
                    if (answer.hasMore()) {
                        while (answer.hasMore()) {
                            if (count > 0) {
                                log.error("More than one user exist with name");
                                throw new UserStoreException("More than one user exist with name");
                            }
                            SearchResult sr = (SearchResult) answer.next();
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

    /**
     * @param userName
     * @param searchBases
     * @param searchFilter
     * @param searchCtls
     * @param property
     * @return
     * @throws UserStoreException
     */
    private List<String> getListOfNames(String userName, String searchBases, String searchFilter,
                                        SearchControls searchCtls, String property, boolean appendDn)
            throws UserStoreException {
        boolean debug = log.isDebugEnabled();
        List<String> names = new ArrayList<String>();
        DirContext dirContext = null;
        NamingEnumeration<SearchResult> answer = null;

        if (debug) {
            log.debug("Result for searchBase: " + searchBases + " searchFilter: " + searchFilter +
                    " property:" + property + " appendDN: " + appendDn);
        }

        try {
            dirContext = connectionSource.getContext();

            // handle multiple search bases
            String[] searchBaseArray = searchBases.split("#");
            for (String searchBase : searchBaseArray) {

                try {
                    answer = dirContext.search(replaceEscapeCharacters(userName, searchBase, false),
                            escapeLDAPSearchFilter(userName, searchFilter), searchCtls);
                    String domain = this.getRealmConfiguration().getUserStoreProperty(
                            UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);

                    while (answer.hasMoreElements()) {
                        SearchResult sr = (SearchResult) answer.next();
                        if (sr.getAttributes() != null) {
                            Attribute attr = sr.getAttributes().get(property);
                            if (attr != null) {
                                for (Enumeration vals = attr.getAll(); vals.hasMoreElements(); ) {
                                    String name = (String) vals.nextElement();
                                    if (debug) {
                                        log.debug("Found user: " + name);
                                    }
                                    domain = UserCoreUtil.addDomainToName(name,
                                            domain);
                                    names.add(name);
                                }
                            }
                        }
                    }
                } catch (NamingException e) {
                    // ignore
                    if (log.isDebugEnabled()) {
                        log.debug(e);
                    }
                }

                if (debug) {
                    for (String name : names) {
                        log.debug("Result  :  " + name);
                    }
                }

            }

            return names;
        } finally {
            JNDIUtil.closeNamingEnumeration(answer);
            JNDIUtil.closeContext(dirContext);
        }
    }

    /**
     *
     */
    public Map<String, String> getProperties(org.wso2.carbon.user.api.Tenant tenant)
            throws org.wso2.carbon.user.api.UserStoreException {
        return getProperties((Tenant) tenant);
    }

    /**
     *
     */
    public int getTenantId() throws UserStoreException {
        return this.tenantId;
    }

    /* TODO: support for multiple user stores */
    public String[] getUserListFromProperties(String property, String value, String profileName)
            throws UserStoreException {
        boolean debug = log.isDebugEnabled();

        List<String> values = new ArrayList<String>();
        String searchFilter = realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_LIST_FILTER);
        String userPropertyName =
                realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE);

        searchFilter = "(&" + searchFilter + "(" + property + "=" + value + "))";

        DirContext dirContext = this.connectionSource.getContext();
        NamingEnumeration<?> answer = null;
        NamingEnumeration<?> attrs = null;

        if (debug) {
            log.debug("Listing users with Property: " + property + " SearchFilter: " + searchFilter);
        }

        try {
            answer = this.searchForUser(value, searchFilter, new String[]{userPropertyName}, dirContext);
            while (answer.hasMoreElements()) {
                SearchResult sr = (SearchResult) answer.next();
                Attributes attributes = sr.getAttributes();
                if (attributes != null) {
                    Attribute attribute = attributes.get(userPropertyName);
                    if (attribute != null) {
                        StringBuffer attrBuffer = new StringBuffer();
                        for (attrs = attribute.getAll(); attrs.hasMore(); ) {
                            String attr = (String) attrs.next();
                            if (attr != null && attr.trim().length() > 0) {
                                attrBuffer.append(attr + ",");
                                if (debug) {
                                    log.debug(userPropertyName + " : " + attr);
                                }
                            }
                        }
                        String propertyValue = attrBuffer.toString();
                        // Length needs to be more than one for a valid
                        // attribute, since we
                        // attach ",".
                        if (propertyValue != null && propertyValue.trim().length() > 1) {
                            propertyValue = propertyValue.substring(0, propertyValue.length() - 1);
                            values.add(propertyValue);
                        }
                    }
                }
            }

        } catch (NamingException e) {
            log.debug(e.getMessage(), e);
            throw new UserStoreException(e.getMessage());
        } finally {
            // close the naming enumeration and free up resources
            JNDIUtil.closeNamingEnumeration(attrs);
            JNDIUtil.closeNamingEnumeration(answer);
            // close directory context
            JNDIUtil.closeContext(dirContext);
        }

        if (debug) {
            String[] results = values.toArray(new String[values.size()]);
            for (String result : results) {
                log.debug("result: " + result);
            }
        }

        return values.toArray(new String[values.size()]);
    }

    @Override
    public boolean doCheckIsUserInRole(String userName, String roleName) throws UserStoreException {

        boolean debug = log.isDebugEnabled();

        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        LDAPRoleContext context = (LDAPRoleContext) createRoleContext(roleName);
        // Get the effective search base
        String searchBases = this.getEffectiveSearchBase(context.isShared());
        String memberOfProperty = realmConfig.getUserStoreProperty(LDAPConstants.MEMBEROF_ATTRIBUTE);

        if (memberOfProperty != null && memberOfProperty.length() > 0) {
            List<String> list;

            String userNameProperty = realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE);
            String userSearchFilter = realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_SEARCH_FILTER);
            String searchFilter = userSearchFilter.replace("?", userName);
            String binaryAttribute =
                    realmConfig.getUserStoreProperty(LDAPConstants.LDAP_ATTRIBUTES_BINARY);
            String primaryGroupId = realmConfig.getUserStoreProperty(LDAPConstants.PRIMARY_GROUP_ID);

            String returnedAtts[] = {memberOfProperty};

            if (binaryAttribute != null && primaryGroupId != null) {
                returnedAtts = new String[]{memberOfProperty, binaryAttribute, primaryGroupId};
            }
            searchCtls.setReturningAttributes(returnedAtts);

            if (debug) {
                log.debug("Do check whether the user: " + userName + " is in role: " + roleName);
                log.debug("Search filter: " + searchFilter);
                for (String retAttrib : returnedAtts) {
                    log.debug("Requesting attribute: " + retAttrib);
                }
            }


            if (binaryAttribute != null && primaryGroupId != null) {
                list =
                        this.getAttributeListOfOneElementWithPrimarGroup(userName,searchBases, searchFilter,
                                searchCtls, binaryAttribute,
                                primaryGroupId, userNameProperty,
                                memberOfProperty);
            } else {
                // use cache
                String cachedDN = userCache.get(userName);
                if (cachedDN != null) {
                    searchBases = cachedDN;
                } else {
                    // create DN directly   but there is no way when multiple DNs are used. Need to improve letter
                    String userDNPattern = realmConfig.getUserStoreProperty(LDAPConstants.USER_DN_PATTERN);
                    if (userDNPattern != null && !userDNPattern.contains("#")) {
                        searchBases = MessageFormat.format(userDNPattern, userName);
                    }
                }


                // get DNs of the groups to which this user belongs
                List<String> groupDNs = this.getListOfNames(userName, searchBases, searchFilter,
                        searchCtls, memberOfProperty, false);

                list = this.getAttributeListOfOneElement(userName, searchBases, searchFilter, searchCtls);
            }

            if (debug) {
                if (list != null) {
                    boolean isUserInRole = false;
                    for (String item : list) {
                        log.debug("Result: " + item);
                        if (item.equalsIgnoreCase(roleName)) {
                            isUserInRole = true;
                        }
                    }
                    log.debug("Is user: " + userName + " in role: " + roleName + " ? " +
                            isUserInRole);
                } else {
                    log.debug("No results found !");
                }
            }

            // adding roles list in to the cache
            if (list != null) {
                addAllRolesToUserRolesCache(userName, list);
                for (String role : list) {
                    if (role.equalsIgnoreCase(roleName)) {
                        return true;
                    }
                }
            }

        } else {
            // read the roles with this membership property
            String searchFilter = realmConfig.getUserStoreProperty(LDAPConstants.GROUP_NAME_LIST_FILTER);
            String membershipProperty =
                    realmConfig.getUserStoreProperty(LDAPConstants.MEMBERSHIP_ATTRIBUTE);

            if (membershipProperty == null || membershipProperty.length() < 1) {
                throw new UserStoreException("Please set member of attribute or membership attribute");
            }

            String roleNameProperty =
                    realmConfig.getUserStoreProperty(LDAPConstants.GROUP_NAME_ATTRIBUTE);
            String userDNPattern = realmConfig.getUserStoreProperty(LDAPConstants.USER_DN_PATTERN);
            String nameInSpace;
            if (userDNPattern != null && !userDNPattern.contains("#")) {
                nameInSpace = MessageFormat.format(userDNPattern, userName);
            } else {
                nameInSpace = this.getNameInSpaceForUserName(userName);
            }

            searchFilter = "(&" + searchFilter + "(" + membershipProperty + "=" + nameInSpace + "))";
            String returnedAtts[] = {roleNameProperty};
            searchCtls.setReturningAttributes(returnedAtts);

            if (debug) {
                log.debug("Do check whether the user : " + userName + " is in role: " + roleName);
                log.debug("Search filter : " + searchFilter);
                for (String retAttrib : returnedAtts) {
                    log.debug("Requesting attribute: " + retAttrib);
                }
            }

            DirContext dirContext = null;
            NamingEnumeration<SearchResult> answer = null;
            try {
                dirContext = connectionSource.getContext();
                if (context.getRoleDNPatterns().size() > 0) {
                    for (String pattern : context.getRoleDNPatterns()) {

                        if (debug) {
                            log.debug("Using pattern: " + pattern);
                        }
                        searchBases = MessageFormat.format(pattern.trim(), roleName);
                        try {
                            answer = dirContext.search(searchBases, escapeLDAPSearchFilter(userName,
                                    searchFilter), searchCtls);
                        } catch (NamingException e) {
                            if (log.isDebugEnabled()) {
                                log.debug(e);
                            }
                            //ignore
                        }

                        if (answer != null && answer.hasMoreElements()) {
                            if (debug) {
                                log.debug("User: " + userName + " in role: " + roleName);
                            }
                            return true;
                        }
                        if (debug) {
                            log.debug("User: " + userName + " NOT in role: " + roleName);
                        }
                    }
                } else {

                    if (debug) {
                        log.debug("Do check whether the user: " + userName + " is in role: " + roleName);
                        log.debug("Search filter: " + searchFilter);
                        for (String retAttrib : returnedAtts) {
                            log.debug("Requesting attribute: " + retAttrib);
                        }
                    }

                    searchFilter =
                            "(&" + searchFilter + "(" + membershipProperty + "=" + nameInSpace +
                                    ") (" + roleNameProperty + "=" + roleName + "))";

                    // handle multiple search bases 
                    String[] searchBaseArray = searchBases.split("#");

                    for (String searchBase : searchBaseArray) {
                        answer = dirContext.search(searchBase, searchFilter, searchCtls);

                        if (answer.hasMoreElements()) {
                            if (debug) {
                                log.debug("User: " + userName + " in role: " + roleName);
                            }
                            return true;
                        }

                        if (debug) {
                            log.debug("User: " + userName + " NOT in role: " + roleName);
                        }
                    }
                }
            } catch (NamingException e) {
                if (log.isDebugEnabled()) {
                    log.debug(e.getMessage(), e);
                }
            } finally {
                JNDIUtil.closeNamingEnumeration(answer);
                JNDIUtil.closeContext(dirContext);
            }
        }

        return false;
    }

    private void addAllRolesToUserRolesCache(String userName, List<String> roleList) throws UserStoreException {
        String[] internalRoleList = doGetInternalRoleListOfUser(userName, "*");
        String[] combinedRoleList = UserCoreUtil.combineArrays((roleList.toArray(new String[roleList.size()])), internalRoleList);
        addToUserRolesCache(getTenantId(), userName, combinedRoleList);
    }

    // ************** NOT GOING TO IMPLEMENT ***************

    /**
     *
     */
    public Date getPasswordExpirationTime(String username) throws UserStoreException {
        return null;
    }

    /**
     *
     */
    public int getTenantId(String username) throws UserStoreException {
        throw new UserStoreException("Invalid operation");
    }

    /**
     * //TODO:remove this method
     *
     * @param username
     * @return
     * @throws UserStoreException
     * @deprecated
     */
    public int getUserId(String username) throws UserStoreException {
        throw new UserStoreException("Invalid operation");
    }

    /**
     *
     */
    public void doDeleteUserClaimValue(String userName, String claimURI, String profileName)
            throws UserStoreException {
        throw new UserStoreException(
                "User store is operating in read only mode. Cannot write into the user store.");

    }

    /**
     *
     */
    public void doDeleteUserClaimValues(String userName, String[] claims, String profileName)
            throws UserStoreException {
        throw new UserStoreException(
                "User store is operating in read only mode. Cannot write into the user store.");

    }

    /**
     * @param userName
     * @param credential
     * @param roleList
     * @param claims
     * @param profileName
     * @throws UserStoreException
     */
    public void doAddUser(String userName, Object credential, String[] roleList,
                          Map<String, String> claims, String profileName) throws UserStoreException {
        throw new UserStoreException(
                "User store is operating in read only mode. Cannot write into the user store.");
    }

    /**
     *
     */
    public void doAddUser(String userName, Object credential, String[] roleList,
                          Map<String, String> claims, String profileName,
                          boolean requirePasswordChange) throws UserStoreException {
        throw new UserStoreException(
                "User store is operating in read only mode. Cannot write into the user store.");
    }

    /**
     *
     */
    public void doDeleteUser(String userName) throws UserStoreException {
        throw new UserStoreException(
                "User store is operating in read only mode. Cannot write into the user store.");
    }

    /**
     *
     */
    public void doSetUserClaimValue(String userName, String claimURI, String claimValue,
                                    String profileName) throws UserStoreException {
        throw new UserStoreException(
                "User store is operating in read only mode. Cannot write into the user store.");
    }

    /**
     *
     */
    public void doSetUserClaimValues(String userName, Map<String, String> claims, String profileName)
            throws UserStoreException {
        throw new UserStoreException(
                "User store is operating in read only mode. Cannot write into the user store.");

    }

    /**
     *
     */
    public void doUpdateCredential(String userName, Object newCredential, Object oldCredential)
            throws UserStoreException {
        throw new UserStoreException(
                "User store is operating in read only mode. Cannot write into the user store.");
    }

    /**
     *
     */
    public void doUpdateCredentialByAdmin(String userName, Object newCredential)
            throws UserStoreException {
        throw new UserStoreException(
                "User store is operating in read only mode. Cannot write into the user store.");

    }

	/*
	 * ****************Unsupported methods list
	 * over***********************************************
	 */

    /**
     *
     */
    public void doUpdateRoleListOfUser(String userName, String[] deletedRoles, String[] newRoles)
            throws UserStoreException {
        throw new UserStoreException(
                "User store is operating in read only mode. Cannot write into the user store.");
    }

    /**
     *
     */
    public void doUpdateUserListOfRole(String roleName, String[] deletedUsers, String[] newUsers)
            throws UserStoreException {
        throw new UserStoreException(
                "User store is operating in read only mode. Cannot write into the user store.");
    }

    /**
     *
     */
    public Map<String, String> getProperties(Tenant tenant) throws UserStoreException {
        return this.realmConfig.getUserStoreProperties();
    }

    /**
     *
     */
    public void addRememberMe(String userName, String token)
            throws org.wso2.carbon.user.api.UserStoreException {
        JDBCUserStoreManager jdbcUserStore =
                new JDBCUserStoreManager(dataSource, realmConfig,
                        realmConfig.getTenantId(),
                        false);
        jdbcUserStore.addRememberMe(userName, token);
    }

    /**
     *
     */
    public boolean isValidRememberMeToken(String userName, String token)
            throws org.wso2.carbon.user.api.UserStoreException {
        try {
            if (this.isExistingUser(userName)) {
                JDBCUserStoreManager jdbcUserStore =
                        new JDBCUserStoreManager(
                                dataSource,
                                realmConfig,
                                realmConfig.getTenantId(),
                                false);
                return jdbcUserStore.isExistingRememberMeToken(userName, token);
            }
        } catch (Exception e) {
            log.error("Validating remember me token failed for" + userName);
			/*
			 * not throwing exception. because we need to seamlessly direct them
			 * to login uis
			 */
        }
        return false;
    }


    /**
     * @param groupDNs
     * @return
     * @throws UserStoreException
     */
    private List<String> getGroupNameAttributeValuesOfGroups(List<String> groupDNs)
            throws UserStoreException {
        log.debug("GetGroupNameAttributeValuesOfGroups with DN");
        boolean debug = log.isDebugEnabled();
        // get the DNs of the groups to which user belongs to, as per the search
        // parameters
        String groupNameAttribute =
                realmConfig.getUserStoreProperty(LDAPConstants.GROUP_NAME_ATTRIBUTE);
        String[] returnedAttributes = {groupNameAttribute};
        List<String> groupNameAttributeValues = new ArrayList<String>();
        try {
            DirContext dirContext = this.connectionSource.getContext();

            for (String group : groupDNs) {
                if (debug) {
                    log.debug("Using DN: " + group);
                }
                Attributes groupAttributes = dirContext.getAttributes(group, returnedAttributes);
                if (groupAttributes != null) {
                    Attribute groupAttribute = groupAttributes.get(groupNameAttribute);
                    if (groupAttribute != null) {
                        String groupNameAttributeValue = (String) groupAttribute.get();
                        if (debug) {
                            log.debug(groupNameAttribute + " : " + groupNameAttributeValue);
                        }
                        groupNameAttributeValues.add(groupNameAttributeValue);
                    }
                }
            }
        } catch (UserStoreException e) {
            log.debug("LDAPError", e);
            throw new UserStoreException("Error in getting group name attribute values of groups");
        } catch (NamingException e) {
            log.debug("LDAPError", e);
            throw new UserStoreException("Error in getting group name attribute values of groups");
        }
        return groupNameAttributeValues;
    }

    @Override
    public Properties getDefaultUserStoreProperties() {
        Properties properties = new Properties();
        properties.setMandatoryProperties(ReadOnlyLDAPUserStoreConstants.ROLDAP_USERSTORE_PROPERTIES.toArray
                (new Property[ReadOnlyLDAPUserStoreConstants.ROLDAP_USERSTORE_PROPERTIES.size()]));
        properties.setOptionalProperties(ReadOnlyLDAPUserStoreConstants.OPTIONAL_ROLDAP_USERSTORE_PROPERTIES.toArray
                (new Property[ReadOnlyLDAPUserStoreConstants.OPTIONAL_ROLDAP_USERSTORE_PROPERTIES.size()]));
        return properties;
    }


    @Override
    public boolean isSharedRole(String roleName, String roleNameBase) {
        if (super.isSharedRole(roleName, roleNameBase) && roleNameBase != null) {
            String sharedRoleBase =
                    realmConfig.getUserStoreProperties()
                            .get(LDAPConstants.SHARED_GROUP_SEARCH_BASE);
            if (roleNameBase.contains(sharedRoleBase)) {
                return true;
            }
        }
        return false;
    }


    @Override
    protected boolean isOwnRole(String roleName) {
        String[] nameArray = roleName.split(UserCoreConstants.TENANT_DOMAIN_COMBINER);
        if (nameArray.length > 1) {
            String currentTenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            return (currentTenantDomain.equalsIgnoreCase(nameArray[1]));
        }
        return super.isOwnRole(roleName);
    }

    protected RoleContext createRoleContext(String roleName) { // TODO check whether shared roles enable

        LDAPRoleContext roleContext = new LDAPRoleContext();
        String[] rolePortions = roleName.split(UserCoreConstants.TENANT_DOMAIN_COMBINER);
        if (rolePortions.length > 1 && (rolePortions[1] == null || rolePortions[1].equals("null"))) {
            rolePortions = new String[]{rolePortions[0]};
        }
        boolean shared = false;
        if (rolePortions.length == 1) {
            roleContext.setSearchBase(realmConfig.getUserStoreProperty(LDAPConstants.GROUP_SEARCH_BASE));
            roleContext.setTenantDomain(CarbonContext.getThreadLocalCarbonContext().getTenantDomain());
        } else if (rolePortions.length > 1) {
            String tenantDomain = rolePortions[1].toLowerCase();
            roleContext.setTenantDomain(rolePortions[1]);
//            if (tenantDomain.equalsIgnoreCase(CarbonContext.getCurrentContext().getTenantDomain())) {
//                // Role which is created by the logged in tenant. Tenant can be
//                // either super tenant or other sub tenant.
//                roleContext.setSearchBase(realmConfig.getUserStoreProperty(LDAPConstants.GROUP_SEARCH_BASE));
//            } else {
            String base =
                    realmConfig.getUserStoreProperty(LDAPConstants.SHARED_GROUP_SEARCH_BASE);

            if (!rolePortions[1].equalsIgnoreCase(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                String groupNameAttributeName =
                        realmConfig.getUserStoreProperty(LDAPConstants.SHARED_TENANT_NAME_ATTRIBUTE);

                base = groupNameAttributeName + "=" + rolePortions[1] + "," + base;
            }

            String roleDNPattern = realmConfig.
                    getUserStoreProperty(LDAPConstants.SHARED_GROUP_NAME_ATTRIBUTE) + "={0}," + base;
            roleContext.setSearchBase(base);
            roleContext.addRoleDNPatterns(roleDNPattern);
            shared = true;

        }
        if (shared) {
            roleContext.setSearchFilter(realmConfig.getUserStoreProperty(LDAPConstants.SHARED_GROUP_NAME_SEARCH_FILTER));
            roleContext.setRoleNameProperty(realmConfig.getUserStoreProperty(LDAPConstants.SHARED_GROUP_NAME_ATTRIBUTE));
            roleContext.setListFilter(realmConfig.getUserStoreProperty(LDAPConstants.SHARED_GROUP_NAME_LIST_FILTER));
            roleContext.setGroupEntryObjectClass(realmConfig.getUserStoreProperty(LDAPConstants.GROUP_ENTRY_OBJECT_CLASS));
        } else {
            roleContext.setSearchFilter(realmConfig.getUserStoreProperty(LDAPConstants.ROLE_NAME_FILTER));
            roleContext.setRoleNameProperty(realmConfig.getUserStoreProperty(LDAPConstants.GROUP_NAME_ATTRIBUTE));
            roleContext.setListFilter(realmConfig.getUserStoreProperty(LDAPConstants.GROUP_NAME_LIST_FILTER));
            roleContext.setGroupEntryObjectClass(realmConfig.getUserStoreProperty(LDAPConstants.GROUP_ENTRY_OBJECT_CLASS));
            String roleDNPattern = realmConfig.getUserStoreProperty(LDAPConstants.ROLE_DN_PATTERN);
            if (roleDNPattern != null && roleDNPattern.trim().length() > 0) {
                if (roleDNPattern.contains("#")) {
                    String[] patterns = roleDNPattern.split("#");
                    for (String pattern : patterns) {
                        roleContext.addRoleDNPatterns(pattern);
                    }
                } else {
                    roleContext.addRoleDNPatterns(roleDNPattern);
                }
            }
        }
        roleContext.setRoleName(rolePortions[0]);
        roleContext.setShared(shared);
        return roleContext;
    }
    
    
   /**
     * This is to replace escape characters in user name at user login if replace escape characters
     * enabled in user-mgt.xml. Some User Stores like ApacheDS stores user names by replacing escape
     * characters. In that case, we have to parse the username accordingly.
     *
     * @param userName
     * @param dn
     * @param isDirectBind
     */
    protected String replaceEscapeCharacters(String userName, String dn, boolean isDirectBind) {

        if (userName == null || userName.trim().length() == 0) {
            if (log.isDebugEnabled()){
                log.debug("Received an empty username to escape characters.");
            }
            return null;
        }

        if (log.isDebugEnabled()) {
            log.debug("Replacing escape characters in " + userName);
        }
        String replaceEscapeCharactersAtUserLoginString = realmConfig
                .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_REPLACE_ESCAPE_CHARACTERS_AT_USER_LOGIN);

        if (replaceEscapeCharactersAtUserLoginString != null) {
            replaceEscapeCharacters = Boolean
                    .parseBoolean(replaceEscapeCharactersAtUserLoginString);
            if (log.isDebugEnabled()) {
                log.debug("Replace escape characters configured to: "
                        + replaceEscapeCharactersAtUserLoginString);
            }
        }
        if (replaceEscapeCharacters) {
            String escapedUN = escapeUsernameSpecialCharacters(userName, isDirectBind);
            return dn.replace(userName, escapedUN);
        }
        return dn;
    }

    protected String escapeUsernameSpecialCharacters(String userName, boolean isDirectBind) {
        StringBuilder sb = new StringBuilder();
        if ((userName.length() > 0) && ((userName.charAt(0) == ' ') || (userName.charAt(0) == '#'))) {
            sb.append('\\'); // add the leading backslash if needed
        }
        for (int i = 0; i < userName.length(); i++) {
            char currentChar = userName.charAt(i);
            switch (currentChar) {
                case '\\':
                    if (isDirectBind){
                        sb.append("\\\\");
                        break;
                    } else {
                        sb.append("\\\\\\");
                        break;
                    }
                case ',':
                    sb.append("\\,");
                    break;
                case '+':
                    sb.append("\\+");
                    break;
                case '"':
                    if (isDirectBind) {
                        sb.append("\\\"");
                        break;
                    } else {
                        sb.append("\\\\\"");
                        break;
                    }
                case '<':
                    sb.append("\\<");
                    break;
                case '>':
                    sb.append("\\>");
                    break;
                case ';':
                    sb.append("\\;");
                    break;
                default:
                    sb.append(currentChar);
            }
        }
        if ((userName.length() > 1) && (userName.charAt(userName.length() - 1) == ' ')) {
            sb.insert(sb.length() - 1, '\\'); // add the trailing backslash if needed
        }
        return sb.toString();
    }

    /**
     * Replacing special characters in LDAP filter
     * @param userName
     * @param filter
     * @return
     */
    protected String escapeLDAPSearchFilter(String userName, String filter) {

        if (userName == null || userName.trim().length() == 0) {
            if (log.isDebugEnabled()){
                log.debug("Received an empty username to escape characters.");
            }
            return null;
        }

        if (log.isDebugEnabled()) {
            log.debug("Replacing excape characters in " + userName);
        }
        String replaceEscapeCharactersAtUserLoginString = realmConfig
                .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_REPLACE_ESCAPE_CHARACTERS_AT_USER_LOGIN);

        if (replaceEscapeCharactersAtUserLoginString != null) {
            replaceEscapeCharacters = Boolean
                    .parseBoolean(replaceEscapeCharactersAtUserLoginString);
            if (log.isDebugEnabled()) {
                log.debug("Replace escape characters configured to: "
                        + replaceEscapeCharactersAtUserLoginString);
            }
        }
        if (replaceEscapeCharacters) {
            //TODO: implement character escaping for *
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < userName.length(); i++) {
                char currentChar = userName.charAt(i);
                switch (currentChar) {
                    case '\\':
                        sb.append("\\5c");
                        break;
//                case '*':
//                    sb.append("\\2a");
//                    break;
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
            return filter.replace(userName, sb.toString());
        }
        return filter;
    }
}
