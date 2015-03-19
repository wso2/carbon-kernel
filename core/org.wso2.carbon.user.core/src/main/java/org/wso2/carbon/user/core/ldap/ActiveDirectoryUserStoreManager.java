/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.user.api.Properties;
import org.wso2.carbon.user.api.Property;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreConfigConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.profile.ProfileConfigurationManager;
import org.wso2.carbon.user.core.util.JNDIUtil;

import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InvalidAttributeIdentifierException;
import javax.naming.directory.InvalidAttributeValueException;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.NoSuchAttributeException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * This class is responsible for manipulating Microsoft Active Directory(AD)and Active Directory
 * Light Directory Service (AD LDS)data. This class provides facility to add/delete/modify/view user
 * info in a directory server.
 */
public class ActiveDirectoryUserStoreManager extends ReadWriteLDAPUserStoreManager {

    private static Log logger = LogFactory.getLog(ActiveDirectoryUserStoreManager.class);
    private boolean isADLDSRole = false;
    private boolean isSSLConnection = false;
    private String userAccountControl = "512";


    public ActiveDirectoryUserStoreManager() {

    }

    /**
     * @param realmConfig
     * @param properties
     * @param claimManager
     * @param profileManager
     * @param realm
     * @param tenantId
     * @throws UserStoreException
     */
    public ActiveDirectoryUserStoreManager(RealmConfiguration realmConfig,
                                           Map<String, Object> properties, ClaimManager claimManager,
                                           ProfileConfigurationManager profileManager, UserRealm realm, Integer tenantId)
            throws UserStoreException {

        super(realmConfig, properties, claimManager, profileManager, realm, tenantId);
        checkRequiredUserStoreConfigurations();
    }

    /**
     * @param realmConfig
     * @param claimManager
     * @param profileManager
     * @throws UserStoreException
     */
    public ActiveDirectoryUserStoreManager(RealmConfiguration realmConfig,
                                           ClaimManager claimManager, ProfileConfigurationManager profileManager)
            throws UserStoreException {
        super(realmConfig, claimManager, profileManager);
        checkRequiredUserStoreConfigurations();
    }

    /**
     *
     */
    public void doAddUser(String userName, Object credential, String[] roleList,
                          Map<String, String> claims, String profileName) throws UserStoreException {
        this.addUser(userName, credential, roleList, claims, profileName, false);
    }

    /**
     *
     */
    public void doAddUser(String userName, Object credential, String[] roleList,
                          Map<String, String> claims, String profileName, boolean requirePasswordChange)
            throws UserStoreException {

        boolean isUserBinded = false;

		/* validity checks */
        doAddUserValidityChecks(userName, credential); // / TODO

		/* getting search base directory context */
        DirContext dirContext = getSearchBaseDirectoryContext();

		/* getting add user basic attributes */
        BasicAttributes basicAttributes = getAddUserBasicAttributes(userName);

        if (!isADLDSRole) {
            // creating a disabled user account in AD DS
            BasicAttribute userAccountControl = new BasicAttribute(
                    LDAPConstants.ACTIVE_DIRECTORY_USER_ACCOUNT_CONTROL);
            userAccountControl.add(LDAPConstants.ACTIVE_DIRECTORY_DISABLED_NORMAL_ACCOUNT);
            basicAttributes.put(userAccountControl);
        }

		/* setting claims */
        setUserClaims(claims, basicAttributes, userName);

        Name compoundName = null;
        try {
            NameParser ldapParser = dirContext.getNameParser("");
            compoundName = ldapParser.parse("cn=" + userName);

			/* bind the user. A disabled user account with no password */
            dirContext.bind(compoundName, null, basicAttributes);
            isUserBinded = true;

			/* update the user roles */
            doUpdateRoleListOfUser(userName, null, roleList);

			/* reset the password and enable the account */
            if (!isSSLConnection) {
                logger.warn("Unsecured connection is being used. Enabling user account operation will fail");
            }

            ModificationItem[] mods = new ModificationItem[2];
            mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(
                    LDAPConstants.ACTIVE_DIRECTORY_UNICODE_PASSWORD_ATTRIBUTE,
                    createUnicodePassword((String) credential)));
            if (isADLDSRole) {
                mods[1] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(
                        LDAPConstants.ACTIVE_DIRECTORY_MSDS_USER_ACCOUNT_DISSABLED, "FALSE"));
            } else {
                mods[1] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(
                        LDAPConstants.ACTIVE_DIRECTORY_USER_ACCOUNT_CONTROL, userAccountControl));
            }
            dirContext.modifyAttributes(compoundName, mods);

        } catch (NamingException e) {
            String errorMessage = "Error while adding the user to the Active Directory";
            if (isUserBinded) {
                try {
                    dirContext.unbind(compoundName);
                } catch (NamingException e1) {
                    errorMessage = "Error while accessing the Active Directory";
                    throw new UserStoreException(errorMessage, e);
                }
                errorMessage = "Error while enabling the user account. Please check password policy at DC";
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            JNDIUtil.closeContext(dirContext);
        }
    }

    /**
     * Sets the set of claims provided at adding users
     *
     * @param claims
     * @param basicAttributes
     * @throws UserStoreException
     */
    protected void setUserClaims(Map<String, String> claims, BasicAttributes basicAttributes,
                                 String userName) throws UserStoreException {
        if (claims != null) {
            BasicAttribute claim;

            for (Map.Entry<String, String> entry : claims.entrySet()) {
                // avoid attributes with empty values
                if (EMPTY_ATTRIBUTE_STRING.equals(entry.getValue())) {
                    continue;
                }
                // needs to get attribute name from claim mapping
                String claimURI = entry.getKey();

                // skipping profile configuration attribute
                if (claimURI.equals(UserCoreConstants.PROFILE_CONFIGURATION)) {
                    continue;
                }

                String attributeName = null;
                try {
                    attributeName = getClaimAtrribute(claimURI, userName, null);
                } catch (org.wso2.carbon.user.api.UserStoreException e) {
                    String errorMessage = "Error in obtaining claim mapping.";
                    throw new UserStoreException(errorMessage, e);
                }

                claim = new BasicAttribute(attributeName);
                claim.add(claims.get(entry.getKey()));
                if (logger.isDebugEnabled()) {
                    logger.debug("AttributeName: " + attributeName + " AttributeValue: " +
                            claims.get(entry.getKey()));
                }
                basicAttributes.put(claim);
            }
        }
    }

    /**
     *
     */
    public void doUpdateCredential(String userName, Object newCredential, Object oldCredential)
            throws UserStoreException {

		/* validity checks */
        doUpdateCredentialsValidityChecks(userName, newCredential);

        DirContext dirContext = this.connectionSource.getContext();
        String searchBase = realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);
        String userListFilter = realmConfig
                .getUserStoreProperty(LDAPConstants.USER_NAME_LIST_FILTER);
        String userNameAttribute = realmConfig
                .getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE);
        // String searchFilter =
        // realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_SEARCH_FILTER);
        String searchFilter = "(&" + userListFilter + "(" + userNameAttribute + "=" + userName
                + "))";

        SearchControls searchControl = new SearchControls();
        String[] returningAttributes = {"CN"};
        searchControl.setReturningAttributes(returningAttributes);
        searchControl.setSearchScope(SearchControls.SUBTREE_SCOPE);
        DirContext subDirContext = null;
        try {
            // search the user with UserNameAttribute and obtain its CN attribute
            NamingEnumeration<SearchResult> searchResults = dirContext.search(searchBase,
                    searchFilter, searchControl);
            SearchResult user = null;
            int count = 0;
            while (searchResults.hasMore()) {
                if (count > 0) {
                    throw new UserStoreException(
                            "There are more than one result in the user store " + "for user: "
                                    + userName);
                }
                user = searchResults.next();
                count++;
            }
            String userCNValue = null;
            if (user.getAttributes() != null) {
                Attribute cnAttribute = user.getAttributes().get("CN");
                if (cnAttribute != null) {
                    userCNValue = (String) cnAttribute.get();
                } else {
                    throw new UserStoreException("Can not update credential: CN attribute is null");
                }
            }

            ModificationItem[] mods = null;

            // The user tries to change his own password
            if (oldCredential != null && newCredential != null) {
                mods = new ModificationItem[1];
                /*
				 * byte[] oldUnicodePassword = createUnicodePassword((String) oldCredential); byte[]
				 * newUnicodePassword = createUnicodePassword((String) newCredential);
				 */
                mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(
                        LDAPConstants.ACTIVE_DIRECTORY_UNICODE_PASSWORD_ATTRIBUTE,
                        createUnicodePassword((String) newCredential)));
				/*
				 * mods[1] = new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute(
				 * LDAPConstants.ACTIVE_DIRECTORY_UNICODE_PASSWORD_ATTRIBUTE, newUnicodePassword));
				 */
            }
            subDirContext = (DirContext) dirContext.lookup(searchBase);
            subDirContext.modifyAttributes("CN" + "=" + userCNValue, mods);

        } catch (NamingException e) {
            String error = "Can not access the directory service";
            throw new UserStoreException(error, e);
        } finally {
            JNDIUtil.closeContext(subDirContext);
            JNDIUtil.closeContext(dirContext);
        }

    }

    @Override
    public void doUpdateCredentialByAdmin(String userName, Object newCredential)
            throws UserStoreException {
		/* validity checks */
        doUpdateCredentialsValidityChecks(userName, newCredential);

        DirContext dirContext = this.connectionSource.getContext();
        String searchBase = realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);
        String userListFilter = realmConfig
                .getUserStoreProperty(LDAPConstants.USER_NAME_LIST_FILTER);
        String userNameAttribute = realmConfig
                .getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE);
        String searchFilter = "(&" + userListFilter + "(" + userNameAttribute + "=" + userName
                + "))";
        SearchControls searchControl = new SearchControls();
        String[] returningAttributes = {"CN"};
        searchControl.setReturningAttributes(returningAttributes);
        searchControl.setSearchScope(SearchControls.SUBTREE_SCOPE);

        DirContext subDirContext = null;
        try {
            // search the user with UserNameAttribute and obtain its CN attribute
            NamingEnumeration<SearchResult> searchResults = dirContext.search(searchBase,
                    searchFilter, searchControl);
            SearchResult user = null;
            int count = 0;
            while (searchResults.hasMore()) {
                if (count > 0) {
                    throw new UserStoreException(
                            "There are more than one result in the user store " + "for user: "
                                    + userName);
                }
                user = searchResults.next();
                count++;
            }
            String userCNValue = null;
            if (user.getAttributes() != null) {
                Attribute cnAttribute = user.getAttributes().get("CN");
                if (cnAttribute != null) {
                    userCNValue = (String) cnAttribute.get();
                } else {
                    throw new UserStoreException("Can not update credential: CN attribute is null");
                }
            }

            ModificationItem[] mods = null;

            if (newCredential != null) {
                mods = new ModificationItem[1];
                mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(
                        LDAPConstants.ACTIVE_DIRECTORY_UNICODE_PASSWORD_ATTRIBUTE,
                        createUnicodePassword((String) newCredential)));

                subDirContext = (DirContext) dirContext.lookup(searchBase);
                subDirContext.modifyAttributes("CN" + "=" + userCNValue, mods);
            }

        } catch (NamingException e) {
            String error = "Can not access the directory service";
            throw new UserStoreException(error, e);
        } finally {
            JNDIUtil.closeContext(subDirContext);
            JNDIUtil.closeContext(dirContext);
        }
    }

    /**
     *
     */
    protected void doUpdateCredentialsValidityChecks(String userName, Object newCredential)
            throws UserStoreException {
        super.doUpdateCredentialsValidityChecks(userName, newCredential);
        if (!isSSLConnection) {
            logger.warn("Unsecured connection is being used. Password operations will fail");
        }
    }

    /**
     * This is to read and validate the required user store configuration for this user store
     * manager to take decisions.
     *
     * @throws UserStoreException
     */
    protected void checkRequiredUserStoreConfigurations() throws UserStoreException {

        super.checkRequiredUserStoreConfigurations();

        String is_ADLDSRole = realmConfig
                .getUserStoreProperty(LDAPConstants.ACTIVE_DIRECTORY_LDS_ROLE);
        isADLDSRole = Boolean.parseBoolean(is_ADLDSRole);

        if (!isADLDSRole) {
            userAccountControl = realmConfig
                    .getUserStoreProperty(LDAPConstants.ACTIVE_DIRECTORY_USER_ACCOUNT_CONTROL);
            try {
                Integer.parseInt(userAccountControl);
            } catch (NumberFormatException e) {
                userAccountControl = "512";
            }
        }

        String connectionURL = realmConfig.getUserStoreProperty(LDAPConstants.CONNECTION_URL);
        String[] array = connectionURL.split(":");
        if (array[0].equals("ldaps")) {
            this.isSSLConnection = true;
        } else {
            logger.warn("Connection to the Active Directory is not secure. Passowrd involved operations such as update credentials and adduser operations will fail");
        }
    }

    /**
     * @param password
     * @return
     */
    private byte[] createUnicodePassword(String password) {
        String newQuotedPassword = "\"" + password + "\"";
        byte[] encodedPwd = null;
        try {
            encodedPwd = newQuotedPassword.getBytes("UTF-16LE");
        } catch (UnsupportedEncodingException e) {
            logger.error("Error while encoding the given password", e);
        }
        return encodedPwd;
    }

    /**
     * This method overwrites the method in LDAPUserStoreManager. This implements the functionality
     * of updating user's profile information in LDAP user store.
     *
     * @param userName
     * @param claims
     * @param profileName
     * @throws org.wso2.carbon.user.core.UserStoreException
     */
    @Override
    public void doSetUserClaimValues(String userName, Map<String, String> claims, String profileName)
            throws UserStoreException {
        // get the LDAP Directory context
        DirContext dirContext = this.connectionSource.getContext();
        DirContext subDirContext = null;
        // search the relevant user entry by user name
        String userSearchBase = realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);
        String userSearchFilter = realmConfig
                .getUserStoreProperty(LDAPConstants.USER_NAME_SEARCH_FILTER);
        // if user name contains domain name, remove domain name
        String[] userNames = userName.split(CarbonConstants.DOMAIN_SEPARATOR);
        if (userNames.length > 1) {
            userName = userNames[1];
        }
        userSearchFilter = userSearchFilter.replace("?", userName);

        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchControls.setReturningAttributes(null);

        NamingEnumeration<SearchResult> returnedResultList = null;
        String returnedUserEntry = null;

        boolean cnModified = false;
        String cnValue = null;

        try {

            returnedResultList = dirContext
                    .search(userSearchBase, userSearchFilter, searchControls);
            // assume only one user is returned from the search
            // TODO:what if more than one user is returned
            returnedUserEntry = returnedResultList.next().getName();

        } catch (NamingException e) {
            throw new UserStoreException("Results could not be retrieved from the directory "
                    + "context", e);
        } finally {
            JNDIUtil.closeNamingEnumeration(returnedResultList);
        }

        if (profileName == null) {
            profileName = UserCoreConstants.DEFAULT_PROFILE;
        }

        if (claims.get(UserCoreConstants.PROFILE_CONFIGURATION) == null) {
            claims.put(UserCoreConstants.PROFILE_CONFIGURATION,
                    UserCoreConstants.DEFAULT_PROFILE_CONFIGURATION);
        }

        try {
            Attributes updatedAttributes = new BasicAttributes(true);

            String domainName =
                    userName.indexOf(UserCoreConstants.DOMAIN_SEPARATOR) > -1
                            ? userName.split(UserCoreConstants.DOMAIN_SEPARATOR)[0]
                            : realmConfig.getUserStoreProperty(UserStoreConfigConstants.DOMAIN_NAME);
            for (Map.Entry<String, String> claimEntry : claims.entrySet()) {
                String claimURI = claimEntry.getKey();
                // if there is no attribute for profile configuration in LDAP,
                // skip updating it.
                if (claimURI.equals(UserCoreConstants.PROFILE_CONFIGURATION)) {
                    continue;
                }
                // get the claimMapping related to this claimURI
                String attributeName = getClaimAtrribute(claimURI, userName, null);
                //remove user DN from cache if changing username attribute
                if (realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE).equals
                        (attributeName)) {
                    userCache.remove(userName);
                }
                // if mapped attribute is CN, then skip treating as a modified
                // attribute -
                // it should be an object rename
                if ("CN".toLowerCase().equals(attributeName.toLowerCase())) {
                    cnModified = true;
                    cnValue = claimEntry.getValue();
                    continue;
                }
                Attribute currentUpdatedAttribute = new BasicAttribute(attributeName);
				/* if updated attribute value is null, remove its values. */
                if (EMPTY_ATTRIBUTE_STRING.equals(claimEntry.getValue())) {
                    currentUpdatedAttribute.clear();
                } else {
                    if (claimEntry.getValue() != null && claimEntry.getValue().contains(",")) {
                        String[] values = claimEntry.getValue().split(",");
                        for (String newValue : values) {
                            if (newValue != null && newValue.trim().length() > 0) {
                                currentUpdatedAttribute.add(newValue.trim());
                            }
                        }
                    } else {
                        currentUpdatedAttribute.add(claimEntry.getValue());
                    }
                }
                updatedAttributes.put(currentUpdatedAttribute);
            }
            // update the attributes in the relevant entry of the directory
            // store

            subDirContext = (DirContext) dirContext.lookup(userSearchBase);
            subDirContext.modifyAttributes(returnedUserEntry, DirContext.REPLACE_ATTRIBUTE,
                    updatedAttributes);

            if (cnModified && cnValue != null) {
                subDirContext.rename(returnedUserEntry, "CN=" + cnValue);
            }

        } catch (InvalidAttributeValueException e) {
            String errorMessage = "One or more attribute values provided are incompatible. "
                    + "Please check and try again.";
            throw new UserStoreException(errorMessage, e);
        } catch (InvalidAttributeIdentifierException e) {
            String errorMessage = "One or more attributes you are trying to add/update are not "
                    + "supported by underlying LDAP.";
            throw new UserStoreException(errorMessage, e);
        } catch (NoSuchAttributeException e) {
            String errorMessage = "One or more attributes you are trying to add/update are not "
                    + "supported by underlying LDAP.";
            throw new UserStoreException(errorMessage, e);
        } catch (NamingException e) {
            String errorMessage = "Profile information could not be updated in ApacheDS "
                    + "LDAP user store";
            throw new UserStoreException(errorMessage, e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String errorMessage = "Error in obtaining claim mapping.";
            throw new UserStoreException(errorMessage, e);
        } finally {
            JNDIUtil.closeContext(subDirContext);
            JNDIUtil.closeContext(dirContext);
        }

    }

    @Override
    public void doSetUserClaimValue(String userName, String claimURI, String value,
                                    String profileName) throws UserStoreException {
        // get the LDAP Directory context
        DirContext dirContext = this.connectionSource.getContext();
        DirContext subDirContext = null;
        // search the relevant user entry by user name
        String userSearchBase = realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);
        String userSearchFilter = realmConfig
                .getUserStoreProperty(LDAPConstants.USER_NAME_SEARCH_FILTER);
        userSearchFilter = userSearchFilter.replace("?", userName);

        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchControls.setReturningAttributes(null);

        NamingEnumeration<SearchResult> returnedResultList = null;
        String returnedUserEntry = null;

        try {

            returnedResultList = dirContext
                    .search(userSearchBase, userSearchFilter, searchControls);
            // assume only one user is returned from the search
            // TODO:what if more than one user is returned
            returnedUserEntry = returnedResultList.next().getName();
        } catch (NamingException e) {
            throw new UserStoreException("Results could not be retrieved from the directory "
                    + "context", e);
        } finally {
            JNDIUtil.closeNamingEnumeration(returnedResultList);
        }

        try {
            Attributes updatedAttributes = new BasicAttributes(true);
            // if there is no attribute for profile configuration in LDAP, skip
            // updating it.
            // get the claimMapping related to this claimURI
            String attributeName = getClaimAtrribute(claimURI, userName, null);

            if ("CN".equals(attributeName)) {
                subDirContext = (DirContext) dirContext.lookup(userSearchBase);
                subDirContext.rename(returnedUserEntry, "CN=" + value);
                return;
            }

            Attribute currentUpdatedAttribute = new BasicAttribute(attributeName);
			/* if updated attribute value is null, remove its values. */
            if (EMPTY_ATTRIBUTE_STRING.equals(value)) {
                currentUpdatedAttribute.clear();
            } else {
                if (value.contains(",")) {
                    String[] values = value.split(",");
                    for (String newValue : values) {
                        if (newValue != null && newValue.trim().length() > 0) {
                            currentUpdatedAttribute.add(newValue.trim());
                        }
                    }
                } else {
                    currentUpdatedAttribute.add(value);
                }
            }
            updatedAttributes.put(currentUpdatedAttribute);

            // update the attributes in the relevant entry of the directory
            // store

            subDirContext = (DirContext) dirContext.lookup(userSearchBase);
            subDirContext.modifyAttributes(returnedUserEntry, DirContext.REPLACE_ATTRIBUTE,
                    updatedAttributes);

        } catch (InvalidAttributeValueException e) {
            String errorMessage = "One or more attribute values provided are incompatible. "
                    + "Please check and try again.";
            throw new UserStoreException(errorMessage, e);
        } catch (InvalidAttributeIdentifierException e) {
            String errorMessage = "One or more attributes you are trying to add/update are not "
                    + "supported by underlying LDAP.";
            throw new UserStoreException(errorMessage, e);
        } catch (NoSuchAttributeException e) {
            String errorMessage = "One or more attributes you are trying to add/update are not "
                    + "supported by underlying LDAP.";
            throw new UserStoreException(errorMessage, e);
        } catch (NamingException e) {
            String errorMessage = "Profile information could not be updated in ApacheDS "
                    + "LDAP user store";
            throw new UserStoreException(errorMessage, e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String errorMessage = "Error in obtaining claim mapping.";
            throw new UserStoreException(errorMessage, e);
        } finally {
            JNDIUtil.closeContext(subDirContext);
            JNDIUtil.closeContext(dirContext);
        }

    }

    @Override
    public Properties getDefaultUserStoreProperties() {
        Properties properties = new Properties();
        properties.setMandatoryProperties(ActiveDirectoryUserStoreConstants.ACTIVE_DIRECTORY_UM_PROPERTIES.toArray
                (new Property[ActiveDirectoryUserStoreConstants.ACTIVE_DIRECTORY_UM_PROPERTIES.size()]));
        properties.setOptionalProperties(ActiveDirectoryUserStoreConstants.OPTIONAL_ACTIVE_DIRECTORY_UM_PROPERTIES.toArray
                (new Property[ActiveDirectoryUserStoreConstants.OPTIONAL_ACTIVE_DIRECTORY_UM_PROPERTIES.size()]));
        return properties;
    }

}
