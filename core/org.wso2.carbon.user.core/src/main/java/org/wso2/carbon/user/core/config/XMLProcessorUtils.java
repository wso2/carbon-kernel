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
package org.wso2.carbon.user.core.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.user.api.Property;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;

import java.util.ArrayList;
import java.util.Map;

public class XMLProcessorUtils {
    private static final Log log = LogFactory.getLog(XMLProcessorUtils.class);

    /**
     * Get a List of existing domain names
     *
     * @return : list of domain names
     * @throws org.wso2.carbon.user.api.UserStoreException
     */
    public ArrayList<String> getDomainNames() throws UserStoreException {
        ArrayList<String> domains = new ArrayList<String>();

        org.wso2.carbon.user.api.RealmConfiguration realmConfiguration =
                CarbonContext.getThreadLocalCarbonContext().getUserRealm().getRealmConfiguration();

        // For primary
        String domain = realmConfiguration.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
        if (domain == null) {
            domain = UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME;
        }
        domains.add(domain);

        org.wso2.carbon.user.api.RealmConfiguration secondaryRealmConfiguration = realmConfiguration.getSecondaryRealmConfig();
        while (secondaryRealmConfiguration != null) {
            domains.add(secondaryRealmConfiguration.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME));
            secondaryRealmConfiguration = secondaryRealmConfiguration.getSecondaryRealmConfig();
        }

        return domains;
    }

    public boolean isValidDomain(String domainName, Boolean isAdd) throws UserStoreException {
        if (domainName == null || "".equals(domainName)) {
            String msg = "User store domain name should not be empty.";
            throw new UserStoreException(msg);
        }

        if (domainName.contains("_")) {
            String msg = "User store domain name should not contain \"_\".";
            throw new UserStoreException(msg);
        }

        if (isAdd) {
            // if add, user store domain name shouldn't already exists
            if (getDomainNames().contains(domainName)) {
                String msg = "Cannot add user store. Domain name already exists.";
                throw new UserStoreException(msg);
            }
        } else {
            //TODO: Actually it should check for domain names exists at userstore folder, not all

            // if edit, user store domain name should already exists
            if (!getDomainNames().contains(domainName)) {
                String msg = "Cannot edit user store, cannot find the domain name " + domainName;
                throw new UserStoreException(msg);
            }
        }

        return true;
    }

    public boolean isMandatoryFieldsProvided(Map<String, String> definedProperties, Property[] mandatories) throws UserStoreException {

        for (int j = 0; j < mandatories.length; j++) {
            if (!definedProperties.containsKey(mandatories[j].getName())) {
                log.error("Required mandatory property " + mandatories[j].getName() + " is not defined!");
                return false;
            } else {

            }
        }

        return true;
    }
}
