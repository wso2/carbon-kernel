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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.claim.ClaimMapping;
import org.wso2.carbon.user.core.claim.DefaultClaimManager;
import org.wso2.carbon.user.core.claim.builder.ClaimBuilder;
import org.wso2.carbon.user.core.claim.builder.ClaimBuilderException;
import org.wso2.carbon.user.core.claim.dao.ClaimDAO;
import org.wso2.carbon.user.core.config.RealmConfigXMLProcessor;
import org.wso2.carbon.user.core.profile.ProfileConfiguration;
import org.wso2.carbon.user.core.profile.ProfileConfigurationManager;
import org.wso2.carbon.user.core.profile.builder.ProfileBuilderException;
import org.wso2.carbon.user.core.profile.builder.ProfileConfigurationBuilder;
import org.wso2.carbon.user.core.util.DatabaseUtil;

import javax.sql.DataSource;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class DefaultRealm implements UserRealm {

    private static Log log = LogFactory.getLog(DefaultRealm.class);

    private ClaimManager claimMan = null;
    private DataSource dataSource = null;
    private RealmConfiguration realmConfig = null;
    private int tenantId;

    private UserStoreManager userStoreManager = null;
    private AuthorizationManager authzManager = null;
    private Map<String, Object> properties = null;

    /**
     * Usage of this method is found on tests.
     *
     * @param configBean     - Configuration details of the realm
     * @param claimMappings
     * @param profileConfigs
     * @param tenantId
     * @throws UserStoreException
     */
    public void init(RealmConfiguration configBean, Map<String, ClaimMapping> claimMappings,
                     Map<String, ProfileConfiguration> profileConfigs, int tenantId)
            throws UserStoreException {

        if (claimMappings == null) {
            claimMappings = loadDefaultClaimMapping();
        }

        if (profileConfigs == null) {
            profileConfigs = loadDefaultProfileConfiguration();
        }

        realmConfig = configBean;
        properties = new Hashtable<String, Object>();
        this.tenantId = tenantId;
        dataSource = DatabaseUtil.getRealmDataSource(realmConfig);
        properties.put(UserCoreConstants.DATA_SOURCE, dataSource);
        claimMan = new DefaultClaimManager(claimMappings, dataSource, tenantId);
        initializeObjects();
    }

    public void init(RealmConfiguration configBean, Map<String, Object> propertiesMap, int tenantId)
            throws UserStoreException {

        if (configBean == null) {
            configBean = loadDefaultRealmConfigs();
        }

        realmConfig = configBean;
        properties = new Hashtable<String, Object>();
        this.tenantId = tenantId;
        properties = propertiesMap;
        dataSource = (DataSource) properties.get(UserCoreConstants.DATA_SOURCE);

        Map<String, ClaimMapping> claimMappings = new HashMap<String, ClaimMapping>();
        Map<String, ProfileConfiguration> profileConfigs = new HashMap<String, ProfileConfiguration>();
        populateProfileAndClaimMaps(claimMappings, profileConfigs);

        claimMan = new DefaultClaimManager(claimMappings, dataSource, tenantId);
        initializeObjects();
    }

    public UserStoreManager getUserStoreManager() throws UserStoreException {
        return userStoreManager;
    }

    public void addSecondaryUserStoreManager(RealmConfiguration userStoreRealmConfig) throws UserStoreException {
        String value = userStoreRealmConfig.getUserStoreClass();
        if (value == null) {
            log.error("Unable to add user store. UserStoreManager class name is null.");
        } else {
            try {
                UserStoreManager manager = (UserStoreManager) createObjectWithOptions(
                        value, userStoreRealmConfig, properties);

                String domainName = userStoreRealmConfig
                        .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);

                if (domainName != null) {
                    if (isDuplicateDomain(domainName)) {
                        log.error("Could not initialize new user store manager."
                                + "Duplicate domain names not allowed.");
                        return;
                    } else {
                        // Fulfilled requirements for adding UserStore,

                        // now adding UserStoreManager to end of the UserStoreManager chain
                        UserStoreManager tmpUserStoreManager = this.userStoreManager;
                        while (tmpUserStoreManager.getSecondaryUserStoreManager() != null) {
                            tmpUserStoreManager = tmpUserStoreManager.getSecondaryUserStoreManager();
                        }
                        tmpUserStoreManager.setSecondaryUserStoreManager(manager);

                        // update domainName-USM map to retrieve USM directly by its domain name
                        this.userStoreManager.addSecondaryUserStoreManager(domainName,
                                tmpUserStoreManager.getSecondaryUserStoreManager());

                        if (log.isDebugEnabled()) {
                            log.debug("UserStoreManager : " + domainName + "added to the list");
                        }

                        Boolean isDisabled = false;
                        if (userStoreRealmConfig
                                .getUserStoreProperty(UserCoreConstants.RealmConfig.USER_STORE_DISABLED) != null) {
                            isDisabled = Boolean
                                    .parseBoolean(realmConfig
                                            .getUserStoreProperty(UserCoreConstants.RealmConfig.USER_STORE_DISABLED));
                            if (isDisabled) {
                                log.warn("Secondary user store disabled with domain " + domainName
                                        + ".");
                            }
                        }
                    }
                } else {
                    log.warn("Could not initialize new user store manager.  "
                            + "Domain name is not defined");
                }
            } catch (Exception e) {
                log.error("Could not initialize secondary user store manager", e);
            }
        }
    }

    public AuthorizationManager getAuthorizationManager() throws UserStoreException {
        return authzManager;
    }

    public ClaimManager getClaimManager() throws UserStoreException {
        return claimMan;
    }

    public ProfileConfigurationManager getProfileConfigurationManager() throws UserStoreException {
        return null;
    }

    public void cleanUp() throws UserStoreException {
        // TODO Auto-generated method stub
    }

    public RealmConfiguration getRealmConfiguration() throws UserStoreException {
        return realmConfig;
    }

    private void initializeObjects() throws UserStoreException {
        try {

            String value = realmConfig.getUserStoreClass();
            if (value == null) {
                log.info("System is functioning without user store writing ability. User add/edit/delete will not work");
            } else {
                this.userStoreManager = (UserStoreManager) createObjectWithOptions(value,
                        realmConfig, properties);
            }

            RealmConfiguration tmpRealmConfig = realmConfig.getSecondaryRealmConfig();
            UserStoreManager tmpUserStoreManager = userStoreManager;

            String domainName = realmConfig
                    .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
            if (domainName != null) {
                userStoreManager.addSecondaryUserStoreManager(domainName, userStoreManager);
            }

            boolean isDisabled = false;

            if (realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.USER_STORE_DISABLED) != null) {
                isDisabled = Boolean.parseBoolean(realmConfig
                        .getUserStoreProperty(UserCoreConstants.RealmConfig.USER_STORE_DISABLED));
                if (isDisabled) {
                    log.warn("You cannot disable the primary user store.");
                }
            }

            while (tmpRealmConfig != null) {
                value = tmpRealmConfig.getUserStoreClass();
                if (value == null) {
                    log.info("System is functioning without user store writing ability. User add/edit/delete will not work");
                } else {
                    try {
                        UserStoreManager manager = (UserStoreManager) createObjectWithOptions(
                                value, tmpRealmConfig, properties);

                        domainName = tmpRealmConfig
                                .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);

                        if (domainName != null) {
                            if (userStoreManager.getSecondaryUserStoreManager(domainName) != null) {
                                log.warn("Could not initialize secondary user store manager."
                                        + "Duplicate domain names not allowed.");
                                tmpRealmConfig = tmpRealmConfig.getSecondaryRealmConfig();
                                continue;
                            } else {
                                isDisabled = false;

                                if (tmpRealmConfig
                                        .getUserStoreProperty(UserCoreConstants.RealmConfig.USER_STORE_DISABLED) != null) {
                                    isDisabled = Boolean
                                            .parseBoolean(tmpRealmConfig
                                                    .getUserStoreProperty(UserCoreConstants.RealmConfig.USER_STORE_DISABLED));
                                    if (isDisabled) {
                                        log.warn("Secondary user store disabled with domain " + domainName
                                                + ".");
                                        tmpRealmConfig = tmpRealmConfig.getSecondaryRealmConfig();
                                        continue;
                                    }
                                }

                                tmpUserStoreManager.setSecondaryUserStoreManager(manager);
                                userStoreManager.addSecondaryUserStoreManager(domainName,
                                        tmpUserStoreManager.getSecondaryUserStoreManager());
                            }
                        } else {
                            log.warn("Could not initialize secondary user store manager.  "
                                    + "Domain name is not defined");
                            tmpRealmConfig = tmpRealmConfig.getSecondaryRealmConfig();
                            continue;
                        }


                    } catch (Exception e) {
                        if (tmpRealmConfig.isPrimary()) {
                            log.error(e.getMessage(), e);
                            throw new UserStoreException(
                                    "Cannot create connection to the primary user store. Error message "
                                            + e.getMessage());
                        } else {
                            log.warn("Could not initialize secondary user store manager", e);
                            tmpRealmConfig = tmpRealmConfig.getSecondaryRealmConfig();
                            continue;
                        }
                    }
                }

                tmpUserStoreManager = tmpUserStoreManager.getSecondaryUserStoreManager();
                tmpRealmConfig = tmpRealmConfig.getSecondaryRealmConfig();
            }

            value = realmConfig.getAuthorizationManagerClass();
            if (value == null) {
                String message = "System cannot continue. Authorization writer is null";
                log.error(message);
                throw new UserStoreException(message);
            }
            this.authzManager = (AuthorizationManager) createObjectWithOptions(value, realmConfig,
                    properties);

        } catch (UserStoreException e) {
            // all user store exceptions are logged at the place it is created.
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new UserStoreException(e.getMessage(), e);
        }

    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Object createObjectWithOptions(String className, RealmConfiguration realmConfig,
                                           Map properties) throws UserStoreException {

        Class[] initClassOpt1 = new Class[]{RealmConfiguration.class, Map.class,
                ClaimManager.class, ProfileConfigurationManager.class, UserRealm.class,
                Integer.class};
        Object[] initObjOpt1 = new Object[]{realmConfig, properties, claimMan, null, this,
                tenantId};

        Class[] initClassOpt2 = new Class[]{RealmConfiguration.class, Map.class,
                ClaimManager.class, ProfileConfigurationManager.class, UserRealm.class};
        Object[] initObjOpt2 = new Object[]{realmConfig, properties, claimMan, null, this};

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
                return newObject;
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
                return newObject;
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
                return newObject;
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

    private RealmConfiguration loadDefaultRealmConfigs() throws UserStoreException {
        RealmConfigXMLProcessor processor = new RealmConfigXMLProcessor();
        RealmConfiguration config = processor.buildRealmConfigurationFromFile();
        return config;
    }

    private Map<String, ClaimMapping> loadDefaultClaimMapping() throws UserStoreException {
        try {
            ClaimBuilder claimBuilder = new ClaimBuilder(tenantId);
            Map<String, ClaimMapping> claimMapping = claimBuilder
                    .buildClaimMappingsFromConfigFile();
            return claimMapping;
        } catch (ClaimBuilderException e) {
            log.error(e.getMessage(), e);
            throw new UserStoreException(e.getMessage(), e);
        }
    }

    private Map<String, ProfileConfiguration> loadDefaultProfileConfiguration()
            throws UserStoreException {
        try {
            ProfileConfigurationBuilder profilBuilder = new ProfileConfigurationBuilder(tenantId);
            Map<String, ProfileConfiguration> profileConfig = profilBuilder
                    .buildProfileConfigurationFromConfigFile();
            return profileConfig;
        } catch (ProfileBuilderException e) {
            log.error(e.getMessage(), e);
            throw new UserStoreException(e.getMessage(), e);
        }
    }

    private void populateProfileAndClaimMaps(Map<String, ClaimMapping> claimMappings,
                                             Map<String, ProfileConfiguration> profileConfigs) throws UserStoreException {
        ClaimDAO claimDAO = new ClaimDAO(dataSource, tenantId);
        ClaimBuilder claimBuilder = new ClaimBuilder(tenantId);

        int count = claimDAO.getDialectCount();
        if (count == 0) {
            try {
                claimMappings.putAll(claimBuilder.buildClaimMappingsFromConfigFile());
            } catch (ClaimBuilderException e) {
                String msg = "Error in building claims.";
                log.error(msg);
                throw new UserStoreException(msg, e);
            }
            claimDAO.addCliamMappings(claimMappings.values().toArray(
                    new ClaimMapping[claimMappings.size()]));

        } else {
            try {
                claimMappings.putAll(claimBuilder.buildClaimMappingsFromDatabase(dataSource,
                        UserCoreConstants.INTERNAL_USERSTORE));
            } catch (ClaimBuilderException e) {
                String msg = "Error in building claims.";
                log.error(msg);
                throw new UserStoreException(msg, e);
            }

        }
    }

    public Boolean isDuplicateDomain(String domainName) {
        if (this.userStoreManager.getSecondaryUserStoreManager(domainName) != null) {
            return true;
        } else {
            return false;
        }

    }

}
