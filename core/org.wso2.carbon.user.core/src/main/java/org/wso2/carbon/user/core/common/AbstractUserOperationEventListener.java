/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.user.core.common;

import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;

import java.util.List;
import java.util.Map;

/**
 *
 */
public class AbstractUserOperationEventListener implements UserOperationEventListener{

    @Override
    public int getExecutionOrderId() {
        return 0; 
    }

    @Override
    public boolean doPreAuthenticate(String userName, Object credential,
                                     UserStoreManager userStoreManager) throws UserStoreException {
        return true; 
    }

    @Override
    public boolean doPostAuthenticate(String userName, boolean authenticated,
                                      UserStoreManager userStoreManager) throws UserStoreException {
        return true;  
    }

    @Override
    public boolean doPreAddUser(String userName, Object credential, String[] roleList,
                Map<String, String> claims, String profile, UserStoreManager userStoreManager)
                                                                        throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPostAddUser(String userName, Object credential, String[] roleList,
                                Map<String, String> claims, String profile,
                                UserStoreManager userStoreManager)
                                                                        throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPreUpdateCredential(String userName, Object newCredential, Object oldCredential,
                                         UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPostUpdateCredential(String userName, Object credential, UserStoreManager userStoreManager)
                                                                        throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPreUpdateCredentialByAdmin(String userName, Object newCredential,
                                    UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPostUpdateCredentialByAdmin(String userName,
                                                 Object credential,
                                                 UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPreDeleteUser(String userName,
                                   UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPostDeleteUser(String userName,
                                    UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPreSetUserClaimValue(String userName, String claimURI, String claimValue,
              String profileName, UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPostSetUserClaimValue(String userName, UserStoreManager userStoreManager)
                                                                    throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPreSetUserClaimValues(String userName, Map<String, String> claims,
               String profileName, UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPostSetUserClaimValues(String userName, Map<String, String> claims,
                                            String profileName, UserStoreManager userStoreManager)
                                                                    throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPreDeleteUserClaimValues(String userName, String[] claims, String profileName,
                                  UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPostDeleteUserClaimValues(String userName, UserStoreManager userStoreManager)
                                                                    throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPreDeleteUserClaimValue(String userName, String claimURI, String profileName,
                                 UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPostDeleteUserClaimValue(String userName, UserStoreManager userStoreManager)
                                                                    throws UserStoreException {
        return true;
    }

    /**
     * Defines any additional actions before adding a role.
     *
     * @param roleName
     * @param userList
     * @param permissions
     * @param userStoreManager
     * @return
     * @throws org.wso2.carbon.user.core.UserStoreException
     *
     */
    public boolean doPreAddRole(String roleName, String[] userList, Permission[] permissions,
                                UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    /**
     * Defines any additional actions after adding a role.
     *
     * @param roleName
     * @param userList
     * @param permissions
     * @param userStoreManager
     * @return
     * @throws org.wso2.carbon.user.core.UserStoreException
     *
     */
    public boolean doPostAddRole(String roleName, String[] userList, Permission[] permissions,
                                 UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    /**
     * Defines any additional actions before deleting a role.
     *
     * @param roleName
     * @param userStoreManager
     * @return
     * @throws org.wso2.carbon.user.core.UserStoreException
     *
     */
    public boolean doPreDeleteRole(String roleName, UserStoreManager userStoreManager)
            throws UserStoreException {
        return true;
    }

    /**
     * Defines any additional actions before deleting a role.
     *
     * @param roleName
     * @param userStoreManager
     * @return
     * @throws org.wso2.carbon.user.core.UserStoreException
     *
     */
    public boolean doPostDeleteRole(String roleName, UserStoreManager userStoreManager)
            throws UserStoreException {
        return true;
    }

    /**
     * Defines any additional actions before updating a role name.
     *
     * @param roleName
     * @param newRoleName
     * @return
     * @throws org.wso2.carbon.user.core.UserStoreException
     *
     */
    public boolean doPreUpdateRoleName(String roleName, String newRoleName,
                                       UserStoreManager userStoreManager)
            throws UserStoreException {
        return true;
    }

    /**
     * Defines any additional actions after updating a role name.
     *
     * @param roleName
     * @param newRoleName
     * @return
     * @throws org.wso2.carbon.user.core.UserStoreException
     *
     */
    public boolean doPostUpdateRoleName(String roleName, String newRoleName,
                                        UserStoreManager userStoreManager)
            throws UserStoreException {
        return true;
    }

    /**
     * Defines any additional actions before updating a role.
     *
     * @param roleName
     * @param deletedUsers
     * @param newUsers
     * @return
     * @throws org.wso2.carbon.user.core.UserStoreException
     *
     */
    public boolean doPreUpdateUserListOfRole(String roleName, String[] deletedUsers,
                                             String[] newUsers, UserStoreManager userStoreManager)
            throws UserStoreException {
        return true;
    }

    /**
     * Defines any additional actions after updating a role.
     *
     * @param roleName
     * @param deletedUsers
     * @param newUsers
     * @return
     * @throws org.wso2.carbon.user.core.UserStoreException
     *
     */
    public boolean doPostUpdateUserListOfRole(String roleName, String[] deletedUsers,
                                              String[] newUsers, UserStoreManager userStoreManager)
            throws UserStoreException {
        return true;
    }

    /**
     * Define any additional actions before updating role list of user.
     *
     * @param userName
     * @param deletedRoles
     * @param newRoles
     * @param userStoreManager
     * @return
     * @throws org.wso2.carbon.user.core.UserStoreException
     *
     */
    public boolean doPreUpdateRoleListOfUser(String userName, String[] deletedRoles,
                                             String[] newRoles, UserStoreManager userStoreManager)
            throws UserStoreException {
        return true;
    }

    /**
     * Define any additional actions after updating role list of user.
     *
     * @param userName
     * @param deletedRoles
     * @param newRoles
     * @param userStoreManager
     * @return
     * @throws org.wso2.carbon.user.core.UserStoreException
     *
     */
    public boolean doPostUpdateRoleListOfUser(String userName, String[] deletedRoles,
                                              String[] newRoles, UserStoreManager userStoreManager)
            throws UserStoreException {
        return true;
    }

    public boolean doPreGetUserClaimValue(String userName, String claim, String profileName,
                                                                UserStoreManager storeManager) throws UserStoreException{
        return true;
    }

    public boolean doPreGetUserClaimValues(String userName, String[] claims,
                String profileName, Map<String, String> claimMap, UserStoreManager storeManager) throws UserStoreException{
        return true;
    }

    public boolean doPostGetUserClaimValue(String userName, String claim, List<String> claimValue,
                                           String profileName, UserStoreManager storeManager) throws UserStoreException{
        return true;
    }

    public boolean doPostGetUserClaimValues(String userName, String[] claims,
                String profileName, Map<String, String> claimMap, UserStoreManager storeManager) throws UserStoreException{
        return true;
    }
}
