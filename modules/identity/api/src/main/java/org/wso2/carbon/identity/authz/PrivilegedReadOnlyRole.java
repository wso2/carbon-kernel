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
package org.wso2.carbon.identity.authz;

import org.wso2.carbon.identity.authn.PrivilegedROUser;
import org.wso2.carbon.identity.authn.PrivilegedReadOnlyGroup;
import org.wso2.carbon.identity.authn.spi.GroupSearchCriteria;
import org.wso2.carbon.identity.authn.spi.UserSearchCriteria;
import org.wso2.carbon.identity.authz.spi.ReadOnlyAuthorizationStore;

import java.util.Collections;
import java.util.List;

public class PrivilegedReadOnlyRole extends
        PrivilegedRole<PrivilegedROUser, PrivilegedReadOnlyGroup> {

    private ReadOnlyAuthorizationStore<PrivilegedROUser, PrivilegedReadOnlyGroup, PrivilegedReadOnlyRole> authzStore;

    /**
     * @param authzStore
     * @param roleIdentifier
     */
    public PrivilegedReadOnlyRole(
            RoleIdentifier roleIdentifier,
            ReadOnlyAuthorizationStore<PrivilegedROUser, PrivilegedReadOnlyGroup, PrivilegedReadOnlyRole> authzStore)
            throws AuthorizationStoreException {
        super(roleIdentifier, authzStore);
        this.authzStore = authzStore;
    }

    /**
     * @return
     * @throws AuthorizationStoreException
     */
    public List<PrivilegedReadOnlyGroup> getGroups()
            throws AuthorizationStoreException {
        return authzStore.getGroupsOfRole(getRoleIdentifier());
    }

    /**
     * @param searchCriteria
     * @return
     * @throws AuthorizationStoreException
     */
    public List<PrivilegedReadOnlyGroup> getGroups(
            GroupSearchCriteria searchCriteria)
            throws AuthorizationStoreException {
        List<PrivilegedReadOnlyGroup> groups = authzStore.getGroupsOfRole(
                getRoleIdentifier(), searchCriteria);
        return Collections.unmodifiableList(groups);
    }

    /**
     * @return
     * @throws AuthorizationStoreException
     */
    public List<PrivilegedROUser> getUsers() throws AuthorizationStoreException {
        List<PrivilegedROUser> users = authzStore
                .getUsersOfRole(getRoleIdentifier());
        return Collections.unmodifiableList(users);
    }

    /**
     * @param searchCriteria
     * @return
     * @throws AuthorizationStoreException
     */
    public List<PrivilegedROUser> getUsers(UserSearchCriteria searchCriteria)
            throws AuthorizationStoreException {
        List<PrivilegedROUser> users = authzStore.getUsersOfRole(
                getRoleIdentifier(), searchCriteria);
        return Collections.unmodifiableList(users);
    }
}
