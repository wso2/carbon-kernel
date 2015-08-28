/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.authz.spi;

import org.wso2.carbon.identity.authn.GroupIdentifier;
import org.wso2.carbon.identity.authn.PrivilegedRWUser;
import org.wso2.carbon.identity.authn.PrivilegedReadWriteGroup;
import org.wso2.carbon.identity.authn.UserIdentifier;
import org.wso2.carbon.identity.authn.spi.GroupSearchCriteria;
import org.wso2.carbon.identity.authn.spi.UserSearchCriteria;
import org.wso2.carbon.identity.authz.AuthorizationStoreException;
import org.wso2.carbon.identity.authz.PermissionIdentifier;
import org.wso2.carbon.identity.authz.PrivilegedReadWriteRole;
import org.wso2.carbon.identity.authz.RoleIdentifier;
import org.wso2.carbon.identity.authz.VirtualReadWriteAuthorizationStore;

import java.util.List;

public interface ReadWriteAuthorizationStore
        extends
        ReadOnlyAuthorizationStore<PrivilegedRWUser,
                PrivilegedReadWriteGroup,
                PrivilegedReadWriteRole>,
        VirtualReadWriteAuthorizationStore {

    /**
     * @param userIdentifier
     * @return
     * @throws AuthorizationStoreException
     */
    public List<PrivilegedReadWriteRole> getRoles(UserIdentifier userIdentifier)
            throws AuthorizationStoreException;

    /**
     * @param userIdentifier
     * @param Criteria
     * @return
     * @throws AuthorizationStoreException
     */
    public List<PrivilegedReadWriteRole> getRoles(
            UserIdentifier userIdentifier, RoleSearchCriteria Criteria)
            throws AuthorizationStoreException;

    /**
     * @param groupIdentifier
     * @return
     * @throws AuthorizationStoreException
     */
    public List<PrivilegedReadWriteRole> getRoles(
            GroupIdentifier groupIdentifier) throws AuthorizationStoreException;

    /**
     * @param groupIdentifier
     * @param searchCriteria
     * @return
     * @throws AuthorizationStoreException
     */
    public List<PrivilegedReadWriteRole> getRoles(
            GroupIdentifier groupIdentifier, RoleSearchCriteria searchCriteria)
            throws AuthorizationStoreException;

    /**
     * @param roleIdentifier
     * @return
     * @throws AuthorizationStoreException
     */
    public List<PrivilegedReadWriteGroup> getGroupsOfRole(
            RoleIdentifier roleIdentifier) throws AuthorizationStoreException;

    /**
     * @param roleIdentifier
     * @param searchCriteria
     * @return
     * @throws AuthorizationStoreException
     */
    public List<PrivilegedReadWriteGroup> getGroupsOfRole(
            RoleIdentifier roleIdentifier, GroupSearchCriteria searchCriteria)
            throws AuthorizationStoreException;

    /**
     * @param roleIdentifier
     * @return
     * @throws AuthorizationStoreException
     */
    public List<PrivilegedRWUser> getUsersOfRole(RoleIdentifier roleIdentifier)
            throws AuthorizationStoreException;

    /**
     * @param roleIdentifier
     * @param searchCriteria
     * @return
     * @throws AuthorizationStoreException
     */
    public List<PrivilegedRWUser> getUsersOfRole(RoleIdentifier roleIdentifier,
                                                 UserSearchCriteria searchCriteria)
            throws AuthorizationStoreException;

    /**
     * @param roleIdentifier
     * @param permissionIdentifier
     * @throws AuthorizationStoreException
     */
    public void assignPermissionToRole(RoleIdentifier roleIdentifier,
                                       PermissionIdentifier permissionIdentifier)
            throws AuthorizationStoreException;

    /**
     * @param roleIdentifier
     * @param permissionIdentifierList
     * @throws AuthorizationStoreException
     */
    public void assignPermissionsToRole(RoleIdentifier roleIdentifier,
                                        List<PermissionIdentifier> permissionIdentifierList)
            throws AuthorizationStoreException;

    /**
     * @param userIdentifier
     * @param roleIdentifier
     * @throws AuthorizationStoreException
     */
    public void assignRoleToUser(UserIdentifier userIdentifier,
                                 RoleIdentifier roleIdentifier) throws AuthorizationStoreException;

    /**
     * @param userIdentifier
     * @param roleIdentifiersList
     * @throws AuthorizationStoreException
     */
    public void assignRolesToUser(UserIdentifier userIdentifier,
                                  List<RoleIdentifier> roleIdentifiersList)
            throws AuthorizationStoreException;

    /**
     * @param groupIdentifier
     * @param roleIdentifier
     * @throws AuthorizationStoreException
     */
    public void assignRoleToGroup(GroupIdentifier groupIdentifier,
                                  RoleIdentifier roleIdentifier) throws AuthorizationStoreException;

    /**
     * @param groupIdentifier
     * @param roleIdentifiersList
     * @throws AuthorizationStoreException
     */
    public void assignRolesToGroup(GroupIdentifier groupIdentifier,
                                   List<RoleIdentifier> roleIdentifiersList)
            throws AuthorizationStoreException;

    /**
     * @param roleIdentifier
     * @param permissionIdentifier
     * @throws AuthorizationStoreException
     */
    public void removePermissionFromRole(RoleIdentifier roleIdentifier,
                                         PermissionIdentifier permissionIdentifier)
            throws AuthorizationStoreException;

    /**
     * @param roleIdentifier
     * @param permissionIdentifierList
     * @throws AuthorizationStoreException
     */
    public void removePermissionsFromRole(RoleIdentifier roleIdentifier,
                                          List<PermissionIdentifier> permissionIdentifierList)
            throws AuthorizationStoreException;

    /**
     * @param userIdentifier
     * @param roleIdentifier
     * @throws AuthorizationStoreException
     */
    public void removeRoleFromUser(UserIdentifier userIdentifier,
                                   RoleIdentifier roleIdentifier) throws AuthorizationStoreException;

    /**
     * @param userIdentifier
     * @param roleIdentifiersList
     * @throws AuthorizationStoreException
     */
    public void removeRolesFromUser(UserIdentifier userIdentifier,
                                    List<RoleIdentifier> roleIdentifiersList)
            throws AuthorizationStoreException;

    /**
     * @param groupIdentifier
     * @param roleIdentifier
     * @throws AuthorizationStoreException
     */
    public void removeRoleFromGroup(GroupIdentifier groupIdentifier,
                                    RoleIdentifier roleIdentifier) throws AuthorizationStoreException;

    /**
     * @param groupIdentifier
     * @param roleIdentifiersList
     * @throws AuthorizationStoreException
     */
    public void removeRolesFromGroup(GroupIdentifier groupIdentifier,
                                     List<RoleIdentifier> roleIdentifiersList)
            throws AuthorizationStoreException;

}
