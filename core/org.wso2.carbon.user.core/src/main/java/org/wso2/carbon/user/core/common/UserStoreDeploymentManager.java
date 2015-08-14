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

import org.apache.axis2.deployment.DeploymentException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreConfigConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.config.UserStoreConfigXMLProcessor;
import org.wso2.carbon.user.core.tenant.TenantCache;
import org.wso2.carbon.user.core.tenant.TenantIdKey;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.io.File;
import java.util.regex.Pattern;

public class UserStoreDeploymentManager {
    private static Log log = LogFactory.getLog(UserStoreDeploymentManager.class);

    public void deploy(String absoluteFilePath) throws DeploymentException {
        UserStoreConfigXMLProcessor userStoreXMLProcessor = new UserStoreConfigXMLProcessor(absoluteFilePath);
        RealmConfiguration realmConfiguration;
        File userMgtConfigFile = new File(absoluteFilePath);
        AbstractUserStoreManager primaryUSM = null;

        try {
            String pattern = Pattern.quote(System.getProperty("file.separator"));
            String[] filePathSegments = absoluteFilePath.split(pattern);
            //<CARBON_HOME>/repository/tenants/1/userstores/domain_com.xml
            //<CARBON_HOME>/repository/deployment/server/userstores/domain_com.xml


            if (filePathSegments[filePathSegments.length - 2].equals(UserStoreConfigConstants.USER_STORES)) {

                //clear cached configurations
                int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
                UserCoreUtil.getRealmService().clearCachedUserRealm(tenantId);
                TenantCache.getInstance().clearCacheEntry(new TenantIdKey(tenantId));
                realmConfiguration = userStoreXMLProcessor.buildUserStoreConfigurationFromFile();
                UserRealm userRealm = (UserRealm) CarbonContext.getThreadLocalCarbonContext().getUserRealm();


                //tenant admin modified secondary user store configuration
                if (filePathSegments[filePathSegments.length - 4].equals(UserStoreConfigConstants.TENANTS)) {
                    realmConfiguration.setTenantId(tenantId);
                    setSecondaryUserStore(userRealm.getRealmConfiguration(), realmConfiguration);
                    primaryUSM = (AbstractUserStoreManager) userRealm.getUserStoreManager();
                    primaryUSM.addSecondaryUserStoreManager(realmConfiguration, userRealm);

                } else {
                    //super tenant modified secondary user store configuration
                    setSecondaryUserStore(userRealm.getRealmConfiguration(), realmConfiguration);
                    primaryUSM = (AbstractUserStoreManager) userRealm.getUserStoreManager();
                    primaryUSM.addSecondaryUserStoreManager(realmConfiguration, userRealm);
                }

                log.info("Realm configuration of tenant:" + CarbonContext.getThreadLocalCarbonContext().getTenantId() + "  modified with " + absoluteFilePath);
            } else {
                //not a change related to user stores
            }

        } catch (Exception ex) {
            String errorMessage = "The deployment of " + userMgtConfigFile.getName() + " is not valid.";
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, ex);
            }
            throw new DeploymentException(errorMessage, ex);
        }

    }

    /**
     * Set secondary user store at the very end of chain
     *
     * @param parent : primary user store
     * @param child  : secondary user store
     */
    private void setSecondaryUserStore(RealmConfiguration parent, RealmConfiguration child) {
        String parentDomain = parent.getUserStoreProperty(UserStoreConfigConstants.DOMAIN_NAME);
        String addingDomain = child.getUserStoreProperty(UserStoreConfigConstants.DOMAIN_NAME);

        if (parentDomain == null) {
            return;
        }


        while (parent.getSecondaryRealmConfig() != null) {
            if (parentDomain.equals(addingDomain)) {
                return;
            }
            parent = parent.getSecondaryRealmConfig();
            parentDomain = parent.getUserStoreProperty(UserStoreConfigConstants.DOMAIN_NAME);
        }

        if (parentDomain.equals(addingDomain)) {
            return;
        }
        parent.setSecondaryRealmConfig(child);
    }


    /**
     * Trigger un-deploying of a deployed file. Removes the deleted user store from chain
     *
     * @param fileName: domain name --> file name
     * @throws org.apache.axis2.deployment.DeploymentException for any errors
     */
    public void undeploy(String fileName) throws DeploymentException {

        String pattern = Pattern.quote(System.getProperty("file.separator"));
        String[] fileNames = fileName.split(pattern);
        String domainName = fileNames[fileNames.length - 1].replace(".xml", "").replace("_", ".");

        int tenantId;
        RealmConfiguration secondaryRealm;
        Boolean isDisabled = false;
        try {
            tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            org.wso2.carbon.user.api.UserRealm tenantRealm = CarbonContext.getThreadLocalCarbonContext().getUserRealm();
            RealmConfiguration realmConfig = tenantRealm.getRealmConfiguration();
            AbstractUserStoreManager userStoreManager = (AbstractUserStoreManager) tenantRealm.getUserStoreManager();

            while (realmConfig.getSecondaryRealmConfig() != null) {
                secondaryRealm = realmConfig.getSecondaryRealmConfig();
                if (secondaryRealm.getUserStoreProperty(UserStoreConfigConstants.DOMAIN_NAME).equalsIgnoreCase(domainName)) {
                    String disabled = secondaryRealm
                            .getUserStoreProperty(UserCoreConstants.RealmConfig.USER_STORE_DISABLED);
                    if (disabled != null) {
                        isDisabled = Boolean.parseBoolean(disabled);
                    }

                    realmConfig.setSecondaryRealmConfig(secondaryRealm.getSecondaryRealmConfig());
                    log.info("User store: " + domainName + " of tenant:" + tenantId + " is removed from realm chain.");
                    break;
                } else {
                    realmConfig = realmConfig.getSecondaryRealmConfig();
                }
            }

            if (!isDisabled) {
                userStoreManager.removeSecondaryUserStoreManager(domainName);
            }
        } catch (Exception ex) {
            String errorMessage = "Error occurred at undeploying " + domainName + " from tenant:" +
                                  CarbonContext.getThreadLocalCarbonContext().getTenantId();
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, ex);
            }
            throw new DeploymentException(errorMessage, ex);
        }

    }

    /**
     * Builds userstore realm configuration from the file
     *
     * @param absoluteFilePath
     * @return
     * @throws UserStoreException
     */
    public RealmConfiguration getUserStoreConfiguration(String absoluteFilePath) throws UserStoreException {
        UserStoreConfigXMLProcessor userStoreXMLProcessor = new UserStoreConfigXMLProcessor(absoluteFilePath);
        return userStoreXMLProcessor.buildUserStoreConfigurationFromFile();
    }
}
