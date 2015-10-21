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
package org.wso2.carbon.identity.authn.spi;

import org.wso2.carbon.identity.account.spi.ReadWriteLinkedAccountStore;
import org.wso2.carbon.identity.authn.GroupIdentifier;
import org.wso2.carbon.identity.authn.IdentityStoreException;
import org.wso2.carbon.identity.authn.PrivilegedRWUser;
import org.wso2.carbon.identity.authn.PrivilegedReadWriteGroup;
import org.wso2.carbon.identity.authn.UserIdentifier;
import org.wso2.carbon.identity.authn.VirtualReadWriteIdentityStore;
import org.wso2.carbon.identity.authz.PrivilegedReadWriteRole;
import org.wso2.carbon.identity.claim.Claim;
import org.wso2.carbon.identity.claim.ClaimIdentifier;
import org.wso2.carbon.identity.claim.DialectIdentifier;
import org.wso2.carbon.identity.credential.spi.Credential;
import org.wso2.carbon.identity.profile.ProfileIdentifier;

import java.util.List;

public interface ReadWriteIdentityStore
        extends
        ReadOnlyIdentityStore<PrivilegedRWUser,
                PrivilegedReadWriteGroup,
                PrivilegedReadWriteRole>,
        VirtualReadWriteIdentityStore {

    /**
     * @param userIdentifier
     * @throws IdentityStoreException
     */
    public void dropUser(UserIdentifier userIdentifier)
            throws IdentityStoreException;

    /**
     * @param userIdentifier
     * @param dialectIdentifier
     * @param claims
     * @param profileIdentifier
     * @throws IdentityStoreException
     */
    public void addUserAttributes(UserIdentifier userIdentifier,
                                  DialectIdentifier dialectIdentifier, List<Claim> claims,
                                  ProfileIdentifier profileIdentifier) throws IdentityStoreException;

    /**
     * @param userIdentifier
     * @param dialectIdentifier
     * @param claims
     * @param profileIdentifier
     * @throws IdentityStoreException
     */
    public void updateUserAttributes(UserIdentifier userIdentifier,
                                     DialectIdentifier dialectIdentifier, List<Claim> claims,
                                     ProfileIdentifier profileIdentifier) throws IdentityStoreException;

    /**
     * @param userIdentifier
     * @param dialectIdentifier
     * @param claimIdentifiers
     * @param profileIdentifier
     * @throws IdentityStoreException
     */
    public void removeUserAttributes(UserIdentifier userIdentifier,
                                     DialectIdentifier dialectIdentifier,
                                     List<ClaimIdentifier> claimIdentifiers,
                                     ProfileIdentifier profileIdentifier) throws IdentityStoreException;

    /**
     * @param userIdentifier
     * @param profileIdentifier
     * @throws IdentityStoreException
     */
    public void createUserProfile(UserIdentifier userIdentifier,
                                  ProfileIdentifier profileIdentifier) throws IdentityStoreException;

    /**
     * @param userIdentifier
     * @param oldIdentifier
     * @param newIdentifier
     * @throws IdentityStoreException
     */
    public void updateUserProfileIdentifier(UserIdentifier userIdentifier,
                                            ProfileIdentifier oldIdentifier, ProfileIdentifier newIdentifier)
            throws IdentityStoreException;

    /**
     * @param groupIdentifier
     * @param dialectIdentifier
     * @param claims
     * @throws IdentityStoreException
     */
    public void addGroupAttributes(GroupIdentifier groupIdentifier,
                                   DialectIdentifier dialectIdentifier, List<Claim> claims)
            throws IdentityStoreException;

    /**
     * @param groupIdentifier
     * @param dialectIdentifier
     * @param claims
     * @throws IdentityStoreException
     */
    public void updateGroupAttributes(GroupIdentifier groupIdentifier,
                                      DialectIdentifier dialectIdentifier, List<Claim> claims)
            throws IdentityStoreException;

    /**
     * @param groupIdentifier
     * @param dialectIdentifier
     * @param attributes
     * @throws IdentityStoreException
     */
    public void removeGroupAttributes(GroupIdentifier groupIdentifier,
                                      DialectIdentifier dialectIdentifier,
                                      List<ClaimIdentifier> attributes) throws IdentityStoreException;

    /**
     * @param groupIdentifier
     * @throws IdentityStoreException
     */
    public void dropGroup(GroupIdentifier groupIdentifier)
            throws IdentityStoreException;

    /**
     * @param groupIdentifier
     * @param group
     * @throws IdentityStoreException
     */
    public void updateGroupIdentifier(GroupIdentifier oldGroupIdentifier,
                                      GroupIdentifier newGroupIdentifier);

    /**
     * @param groupIdentifier
     * @param userIdentifiers
     * @throws IdentityStoreException
     */
    public void addUsersToGroup(GroupIdentifier groupIdentifier,
                                List<UserIdentifier> userIdentifiers) throws IdentityStoreException;

    /**
     * @param groupIdentifier
     * @param userIdentifiers
     * @throws IdentityStoreException
     */
    public void addUsersToGroups(List<GroupIdentifier> groupIdentifier,
                                 List<UserIdentifier> userIdentifiers) throws IdentityStoreException;

    /**
     * @param groupIdentifier
     * @param userIdentifiers
     * @throws IdentityStoreException
     */
    public void addUserToGroup(GroupIdentifier groupIdentifier,
                               UserIdentifier userIdentifiers) throws IdentityStoreException;

    /**
     * @param groupIdentifier
     * @param userIdentifiers
     * @throws IdentityStoreException
     */
    public void addUserToGroups(List<GroupIdentifier> groupIdentifier,
                                UserIdentifier userIdentifiers) throws IdentityStoreException;

    /**
     * @param newCredentials
     * @throws IdentityStoreException
     */
    @SuppressWarnings("rawtypes")
    public void resetCredentials(Credential newCredentials)
            throws IdentityStoreException;

    /**
     * @param credential
     * @throws IdentityStoreException
     */
    @SuppressWarnings("rawtypes")
    public void addCredential(Credential credential)
            throws IdentityStoreException;

    /**
     * @param credential
     * @throws IdentityStoreException
     */
    @SuppressWarnings("rawtypes")
    public void removeCredential(Credential credential)
            throws IdentityStoreException;

    /**
     * @param groupIdentifier
     * @return
     * @throws IdentityStoreException
     */
    public List<PrivilegedRWUser> getUsersInGroup(
            GroupIdentifier groupIdentifier) throws IdentityStoreException;

    /**
     * @param groupIdentifier
     * @param searchCriteria
     * @return
     * @throws IdentityStoreException
     */
    public List<PrivilegedRWUser> getUsersInGroup(
            GroupIdentifier groupIdentifier, UserSearchCriteria searchCriteria)
            throws IdentityStoreException;

    /**
     * @param userIdentifier
     * @return
     * @throws IdentityStoreException
     */
    public List<PrivilegedReadWriteGroup> getGroups(
            UserIdentifier userIdentifier) throws IdentityStoreException;

    /**
     * @param userIdentifier
     * @param searchCriteria
     * @return
     * @throws IdentityStoreException
     */
    public List<PrivilegedReadWriteGroup> getGroups(
            UserIdentifier userIdentifier, GroupSearchCriteria searchCriteria)
            throws IdentityStoreException;

    /**
     * @return
     */
    public ReadWriteLinkedAccountStore getLinkedAccountStore();

}
