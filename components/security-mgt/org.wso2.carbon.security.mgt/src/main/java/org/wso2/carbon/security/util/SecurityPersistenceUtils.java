/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.security.util;

import org.apache.axiom.om.OMAttribute;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.Resources;
import org.wso2.carbon.core.persistence.PersistenceDataNotFoundException;
import org.wso2.carbon.core.persistence.PersistenceUtils;
import org.wso2.carbon.core.persistence.file.ServiceGroupFilePersistenceManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;

import java.util.ArrayList;
import java.util.List;

/**
 * @deprecated  Not for public use, has been replaced.
 */
@Deprecated
public class SecurityPersistenceUtils {

    private static Log log = LogFactory.getLog(SecurityPersistenceUtils.class);

    /**
     * @param serviceGroupId      serviceGroupId
     * @param serviceId           serviceId
     * @param realm               realm
     * @param tenantAwareUserName tenantAwareUserName
     * @param permissionType      Probably UserCoreConstants.INVOKE_SERVICE_PERMISSION is all you need for this
     * @param serviceGroupFilePM  serviceGroupFilePM
     * @return false if any of the roles of user does not have permission to access it or no roles assigned for the service.
     * @throws UserStoreException
     * @deprecated do not use this method
     */
    public static boolean isUserAuthorized(
            String serviceGroupId, String serviceId, UserRealm realm, String tenantAwareUserName, String permissionType,
            ServiceGroupFilePersistenceManager serviceGroupFilePM) throws UserStoreException {
        try {
            String[] rolesList = realm.getUserStoreManager().getRoleListOfUser(tenantAwareUserName);

            String serviceXPath = Resources.ServiceProperties.ROOT_XPATH + PersistenceUtils.
                    getXPathAttrPredicate(Resources.NAME, serviceId);
            String rolesPath = serviceXPath +
                    "/" + Resources.SecurityManagement.ROLE_XML_TAG +
                    PersistenceUtils.getXPathAttrPredicate(
                            Resources.Associations.TYPE, permissionType) +
                    "/@" + Resources.SecurityManagement.ROLENAME_XML_ATTR;

            List tmpAllowedRolesAttr = serviceGroupFilePM.getAll(serviceGroupId, rolesPath);
            List<String> allowedRoles = new ArrayList<>(tmpAllowedRolesAttr.size());
            for (Object attr : tmpAllowedRolesAttr) {
                allowedRoles.add(((OMAttribute) attr).getAttributeValue());
            }

            for (String role : rolesList) {
                if (allowedRoles.contains(role)) {
                    return true;
                }
            }
            return false;
        } catch (PersistenceDataNotFoundException e) {
            log.error("Error occurred while reading allowed roles element. Returning false.", e);
            return false;
        }
    }
}
