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

import org.wso2.carbon.identity.authn.spi.GroupSearchCriteria;
import org.wso2.carbon.identity.authn.spi.UserSearchCriteria;
import org.wso2.carbon.identity.authz.PrivilegedReadWriteRole;
import org.wso2.carbon.identity.credential.spi.Credential;

import java.util.List;

public interface VirtualReadWriteIdentityStore
        extends
        VirtualReadOnlyIdentityStore<PrivilegedRWUser,
                PrivilegedReadWriteGroup,
                PrivilegedReadWriteRole> {

    /**
     * @param credential
     * @return
     * @throws AuthenticationFailureException
     */
    @SuppressWarnings("rawtypes")
    public PrivilegedRWUser authenticate(Credential credential)
            throws AuthenticationFailureException;

    /**
     * @param userIdentifier
     * @return
     * @throws IdentityStoreException
     */
    public PrivilegedRWUser getUser(UserIdentifier userIdentifier)
            throws IdentityStoreException;

    /**
     * @param searchCriteria
     * @return
     * @throws IdentityStoreException
     */
    public List<PrivilegedRWUser> getUsers(UserSearchCriteria searchCriteria)
            throws IdentityStoreException;

    /**
     * @param userIdentifier
     * @return
     * @throws IdentityStoreException
     */
    public PrivilegedReadWriteGroup getGroup(GroupIdentifier userIdentifier)
            throws IdentityStoreException;

    /**
     * @param searchCriteria
     * @return
     * @throws IdentityStoreException
     */
    public List<PrivilegedReadWriteGroup> getGroups(
            GroupSearchCriteria searchCriteria) throws IdentityStoreException;

    /**
     * Creates a user in the underlying user store.
     *
     * @param user
     * @return
     * @throws IdentityStoreException
     */
    public PrivilegedRWUser createUser(User user) throws IdentityStoreException;

    /**
     * Creates a group in the underlying user store.
     *
     * @param group
     * @return
     * @throws IdentityStoreException
     */
    public PrivilegedReadWriteGroup createGroup(Group group)
            throws IdentityStoreException;

}
