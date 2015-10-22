/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.identity.authn;

import org.wso2.carbon.identity.authn.spi.ReadWriteIdentityStore;
import org.wso2.carbon.identity.authn.spi.UserSearchCriteria;
import org.wso2.carbon.identity.authz.AuthorizationStoreException;
import org.wso2.carbon.identity.authz.Permission;
import org.wso2.carbon.identity.authz.PrivilegedReadWriteRole;
import org.wso2.carbon.identity.authz.RoleIdentifier;
import org.wso2.carbon.identity.authz.spi.ReadWriteAuthorizationStore;
import org.wso2.carbon.identity.authz.spi.RoleSearchCriteria;
import org.wso2.carbon.identity.commons.EntityTree;
import org.wso2.carbon.identity.commons.EntryIdentifier;

import java.util.Collections;
import java.util.List;

public class PrivilegedReadWriteGroup extends PrivilegedGroup<PrivilegedRWUser, PrivilegedReadWriteRole> {

    private ReadWriteIdentityStore identityStore;
    private ReadWriteAuthorizationStore authzStore;

    /**
     * @param identityStore
     * @param authzStore
     * @param groupIdentifier
     * @throws IdentityStoreException
     */
    public PrivilegedReadWriteGroup(GroupIdentifier groupIdentifier,
                                    ReadWriteIdentityStore identityStore,
                                    ReadWriteAuthorizationStore authzStore)
            throws IdentityStoreException {
        super(groupIdentifier, identityStore, authzStore);
        this.authzStore = authzStore;
        this.identityStore = identityStore;
    }

    /**
     * @return
     * @throws IdentityStoreException
     */
    public EntryIdentifier getEntryId() throws IdentityStoreException {
        return identityStore.getGroupEntryId(getIdentifier());
    }

    /**
     * @return
     * @throws IdentityStoreException
     */
    public List<PrivilegedRWUser> getUsers() throws IdentityStoreException {
        return identityStore.getUsersInGroup(getIdentifier());
    }

    /**
     * @param searchCriteria
     * @return
     * @throws IdentityStoreException
     */
    public List<PrivilegedRWUser> getUsers(UserSearchCriteria searchCriteria)
            throws IdentityStoreException {
        return identityStore.getUsersInGroup(getIdentifier(), searchCriteria);
    }

    /**
     * @return
     * @throws AuthorizationStoreException
     */
    public List<PrivilegedReadWriteRole> getRoles()
            throws AuthorizationStoreException {
        List<PrivilegedReadWriteRole> roles = authzStore
                .getRoles(getIdentifier());
        return Collections.unmodifiableList(roles);
    }

    /**
     * @param searchCriteria
     * @return
     * @throws AuthorizationStoreException
     */
    public List<PrivilegedReadWriteRole> getRoles(
            RoleSearchCriteria searchCriteria)
            throws AuthorizationStoreException {
        List<PrivilegedReadWriteRole> roles = authzStore.getRoles(
                getIdentifier(), searchCriteria);
        return Collections.unmodifiableList(roles);
    }

    /**
     * @return
     * @throws IdentityStoreException
     */
    public List<EntityTree> getChildren() throws IdentityStoreException {
        List<EntityTree> children = identityStore
                .getGroupChildren(getIdentifier());
        return Collections.unmodifiableList(children);
    }

    /**
     * @param childGroupIdentifier
     * @return
     * @throws IdentityStoreException
     */
    public boolean hasChild(GroupIdentifier childGroupIdentifier)
            throws IdentityStoreException {
        return identityStore.hasChildGroup(getIdentifier(),
                childGroupIdentifier);
    }

    /**
     * @param parentGroupIdentifier
     * @return
     * @throws IdentityStoreException
     */
    public boolean hasParent(GroupIdentifier parentGroupIdentifier)
            throws IdentityStoreException {
        return identityStore.hasParentGroup(getIdentifier(),
                parentGroupIdentifier);
    }

    /**
     * @param roleIdentifiers
     * @throws AuthorizationStoreException
     */
    public void assignToRole(List<RoleIdentifier> roleIdentifiers)
            throws AuthorizationStoreException {
        authzStore.assignRolesToGroup(getIdentifier(), roleIdentifiers);
    }

    /**
     * @param userIdentifiers
     * @throws IdentityStoreException
     */
    public void addUsers(List<UserIdentifier> userIdentifiers)
            throws IdentityStoreException {
        identityStore.addUsersToGroup(getIdentifier(), userIdentifiers);
    }

    /**
     * @param roleIdentifier
     * @return
     * @throws AuthorizationStoreException
     */
    public boolean hasRole(RoleIdentifier roleIdentifier)
            throws AuthorizationStoreException {
        return authzStore.isGroupHasRole(getIdentifier(), roleIdentifier);
    }

    /**
     * @param permission
     * @return
     * @throws AuthorizationStoreException
     */
    public boolean hasPermission(Permission permission)
            throws AuthorizationStoreException {
        return authzStore.isGroupHasPermission(getIdentifier(), permission);
    }

    /**
     * @return
     */
    public StoreIdentifier getStoreIdentifier() {
        return identityStore.getStoreIdentifier();
    }

}
