/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.user.core.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.internal.UserStoreMgtDSComponent;
import java.util.Map;

public class TenantStatusListener extends AbstractUserOperationEventListener {

    private static final Log LOGGER = LogFactory.getLog(TenantStatusListener.class);

    public boolean doPreAuthenticate(String userName, Object credential,
                                     UserStoreManager userStoreManager) throws UserStoreException {
        LOGGER.debug("Pre authenticate is called in TenantStatusListener");

        int tenantId = userStoreManager.getTenantId();

        try {
            boolean tenantActive = UserStoreMgtDSComponent.getRealmService().getTenantManager()
                    .isTenantActive(tenantId);
            if (!tenantActive) {
                throw new UserStoreException("Tenant has been deactivated. TenantID : " + tenantId);
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException("Error while trying to check Tenant status for Tenant : " + tenantId, e);
        }

        return true;
    }

    @Override
    public boolean doPreAddUser(String userName, Object credential, String[] roleList,
                                Map<String, String> claims, String profile, UserStoreManager
            userStoreManager) throws UserStoreException {
        LOGGER.debug("Pre Add User is called in TenantStatusListener");

        int tenantId = userStoreManager.getTenantId();

        try {
            boolean tenantActive = UserStoreMgtDSComponent.getRealmService().getTenantManager()
                    .isTenantActive(tenantId);
            if (!tenantActive) {
                throw new UserStoreException("Tenant has been deactivated. TenantID : " + tenantId);
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException("Error while trying to check Tenant status for Tenant : " + tenantId, e);
        }

        return true;
    }

    @Override
    public boolean doPreUpdateCredential(String userName, Object newCredential, Object oldCredential,
                                         UserStoreManager userStoreManager) throws UserStoreException {
        LOGGER.debug("Pre Update Credential is called in TenantStatusListener");

        int tenantId = userStoreManager.getTenantId();

        try {
            boolean tenantActive = UserStoreMgtDSComponent.getRealmService().getTenantManager()
                    .isTenantActive(tenantId);
            if (!tenantActive) {
                throw new UserStoreException("Tenant has been deactivated. TenantID : " + tenantId);
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException("Error while trying to check Tenant status for Tenant : " + tenantId, e);
        }

        return true;
    }

    @Override
    public boolean doPreUpdateCredentialByAdmin(String userName, Object newCredential, UserStoreManager
            userStoreManager) throws UserStoreException {
        LOGGER.debug("Pre Update Credential By Admin is called in TenantStatusListener");

        int tenantId = userStoreManager.getTenantId();

        try {
            boolean tenantActive = UserStoreMgtDSComponent.getRealmService().getTenantManager()
                    .isTenantActive(tenantId);
            if (!tenantActive) {
                throw new UserStoreException("Tenant has been deactivated. TenantID : " + tenantId);
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException("Error while trying to check Tenant status for Tenant : " + tenantId, e);
        }

        return true;
    }

    @Override
    public boolean doPreDeleteUser(String userName, UserStoreManager userStoreManager) throws UserStoreException {
        LOGGER.debug("Pre Delete User is called in TenantStatusListener");

        int tenantId = userStoreManager.getTenantId();

        try {
            boolean tenantActive = UserStoreMgtDSComponent.getRealmService().getTenantManager()
                    .isTenantActive(tenantId);
            if (!tenantActive) {
                throw new UserStoreException("Tenant has been deactivated. TenantID : " + tenantId);
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException("Error while trying to check Tenant status for Tenant : " + tenantId, e);
        }

        return true;
    }

    @Override
    public boolean doPreSetUserClaimValue(String userName, String claimURI, String claimValue, String profileName,
                                          UserStoreManager userStoreManager) throws UserStoreException {
        LOGGER.debug("Pre Set User Claim Value is called in TenantStatusListener");

        int tenantId = userStoreManager.getTenantId();

        try {
            boolean tenantActive = UserStoreMgtDSComponent.getRealmService().getTenantManager()
                    .isTenantActive(tenantId);
            if (!tenantActive) {
                throw new UserStoreException("Tenant has been deactivated. TenantID : " + tenantId);
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException("Error while trying to check Tenant status for Tenant : " + tenantId, e);
        }

        return true;
    }

    @Override
    public boolean doPreSetUserClaimValues(String userName, Map<String, String> claims, String profileName,
                                           UserStoreManager userStoreManager) throws UserStoreException {
        LOGGER.debug("Pre Set User Claim Values is called in TenantStatusListener");

        int tenantId = userStoreManager.getTenantId();

        try {
            boolean tenantActive = UserStoreMgtDSComponent.getRealmService().getTenantManager()
                    .isTenantActive(tenantId);
            if (!tenantActive) {
                throw new UserStoreException("Tenant has been deactivated. TenantID : " + tenantId);
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException("Error while trying to check Tenant status for Tenant : " + tenantId, e);
        }

        return true;
    }

    @Override
    public boolean doPreDeleteUserClaimValues(String userName, String[] claims, String profileName, UserStoreManager
            userStoreManager) throws UserStoreException {
        LOGGER.debug("Pre Delete User Claim Values is called in TenantStatusListener");

        int tenantId = userStoreManager.getTenantId();

        try {
            boolean tenantActive = UserStoreMgtDSComponent.getRealmService().getTenantManager()
                    .isTenantActive(tenantId);
            if (!tenantActive) {
                throw new UserStoreException("Tenant has been deactivated. TenantID : " + tenantId);
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException("Error while trying to check Tenant status for Tenant : " + tenantId, e);
        }

        return true;
    }

    @Override
    public boolean doPreDeleteUserClaimValue(String userName, String claimURI, String profileName, UserStoreManager
            userStoreManager) throws UserStoreException {
        LOGGER.debug("Pre Delete User Claim Value is called in TenantStatusListener");

        int tenantId = userStoreManager.getTenantId();

        try {
            boolean tenantActive = UserStoreMgtDSComponent.getRealmService().getTenantManager()
                    .isTenantActive(tenantId);
            if (!tenantActive) {
                throw new UserStoreException("Tenant has been deactivated. TenantID : " + tenantId);
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException("Error while trying to check Tenant status for Tenant : " + tenantId, e);
        }

        return true;
    }

    @Override
    public boolean doPreAddRole(String roleName, String[] userList, Permission[] permissions, UserStoreManager
            userStoreManager) throws UserStoreException {
        LOGGER.debug("Pre Add Role is called in TenantStatusListener");

        int tenantId = userStoreManager.getTenantId();

        try {
            boolean tenantActive = UserStoreMgtDSComponent.getRealmService().getTenantManager()
                    .isTenantActive(tenantId);
            if (!tenantActive) {
                throw new UserStoreException("Tenant has been deactivated. TenantID : " + tenantId);
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException("Error while trying to check Tenant status for Tenant : " + tenantId, e);
        }

        return true;
    }

    @Override
    public boolean doPreDeleteRole(String roleName, UserStoreManager userStoreManager) throws UserStoreException {
        LOGGER.debug("Pre Delete Role is called in TenantStatusListener");

        int tenantId = userStoreManager.getTenantId();

        try {
            boolean tenantActive = UserStoreMgtDSComponent.getRealmService().getTenantManager()
                    .isTenantActive(tenantId);
            if (!tenantActive) {
                throw new UserStoreException("Tenant has been deactivated. TenantID : " + tenantId);
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException("Error while trying to check Tenant status for Tenant : " + tenantId, e);
        }

        return true;
    }

    @Override
    public boolean doPreUpdateRoleName(String roleName, String newRoleName, UserStoreManager userStoreManager)
            throws UserStoreException {
        LOGGER.debug("Pre Update Role Name is called in TenantStatusListener");

        int tenantId = userStoreManager.getTenantId();

        try {
            boolean tenantActive = UserStoreMgtDSComponent.getRealmService().getTenantManager()
                    .isTenantActive(tenantId);
            if (!tenantActive) {
                throw new UserStoreException("Tenant has been deactivated. TenantID : " + tenantId);
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException("Error while trying to check Tenant status for Tenant : " + tenantId, e);
        }

        return true;
    }

    @Override
    public boolean doPreUpdateUserListOfRole(String roleName, String[] deletedUsers, String[] newUsers,
                                             UserStoreManager userStoreManager) throws UserStoreException {
        LOGGER.debug("Pre Update User List of Role is called in TenantStatusListener");

        int tenantId = userStoreManager.getTenantId();

        try {
            boolean tenantActive = UserStoreMgtDSComponent.getRealmService().getTenantManager()
                    .isTenantActive(tenantId);
            if (!tenantActive) {
                throw new UserStoreException("Tenant has been deactivated. TenantID : " + tenantId);
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException("Error while trying to check Tenant status for Tenant : " + tenantId, e);
        }

        return true;
    }

    @Override
    public boolean doPreUpdateRoleListOfUser(String userName, String[] deletedRoles, String[] newRoles,
                                             UserStoreManager userStoreManager) throws UserStoreException {
        LOGGER.debug("Pre Update Role List of User is called in TenantStatusListener");

        int tenantId = userStoreManager.getTenantId();

        try {
            boolean tenantActive = UserStoreMgtDSComponent.getRealmService().getTenantManager()
                    .isTenantActive(tenantId);
            if (!tenantActive) {
                throw new UserStoreException("Tenant has been deactivated. TenantID : " + tenantId);
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException("Error while trying to check Tenant status for Tenant : " + tenantId, e);
        }

        return true;
    }

    @Override
    public int getExecutionOrderId() {
        return -1;
    }

}
