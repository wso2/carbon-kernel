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

package org.wso2.carbon.identity.authn.spi;

import org.wso2.carbon.identity.account.spi.ReadOnlyLinkedAccountStore;
import org.wso2.carbon.identity.authn.GroupIdentifier;
import org.wso2.carbon.identity.authn.IdentityStoreException;
import org.wso2.carbon.identity.authn.PrivilegedGroup;
import org.wso2.carbon.identity.authn.PrivilegedUser;
import org.wso2.carbon.identity.authn.StoreDialectCollection;
import org.wso2.carbon.identity.authn.StoreIdentifier;
import org.wso2.carbon.identity.authn.UserIdentifier;
import org.wso2.carbon.identity.authn.VirtualReadOnlyIdentityStore;
import org.wso2.carbon.identity.authz.PrivilegedRole;
import org.wso2.carbon.identity.claim.Claim;
import org.wso2.carbon.identity.claim.ClaimIdentifier;
import org.wso2.carbon.identity.claim.DialectIdentifier;
import org.wso2.carbon.identity.commons.EntityTree;
import org.wso2.carbon.identity.commons.EntryIdentifier;
import org.wso2.carbon.identity.credential.spi.CredentialStore;
import org.wso2.carbon.identity.profile.Profile;
import org.wso2.carbon.identity.profile.ProfileIdentifier;

import java.util.List;
import java.util.Properties;

public interface ReadOnlyIdentityStore<U extends PrivilegedUser<G, R>,
        G extends PrivilegedGroup<U, R>,
        R extends PrivilegedRole<U, G>>
        extends VirtualReadOnlyIdentityStore<U, G, R> {

    /**
     * @param storeDialectCollection
     * @param accountManager
     * @param credentialStore
     * @param properties
     */
    public void init(StoreDialectCollection storeDialectCollection,
                     ReadOnlyLinkedAccountStore linkedAccontStore,
                     CredentialStore credentialStore, Properties properties);

    /**
     * @param userIdentifier
     * @param dialectIdentifier
     * @param claimIdentifiers
     * @param profileIdentifier
     * @return
     * @throws IdentityStoreException
     */
    public List<Claim> getUserAttributes(UserIdentifier userIdentifier,
                                         DialectIdentifier dialectIdentifier,
                                         List<ClaimIdentifier> claimIdentifiers,
                                         ProfileIdentifier profileIdentifier) throws IdentityStoreException;

    /**
     * @param userIdentifier
     * @param dialectIdentifier
     * @param profileIdentifier
     * @return
     * @throws IdentityStoreException
     */
    public List<Claim> getUserAttributes(UserIdentifier userIdentifier,
                                         DialectIdentifier dialectIdentifier,
                                         ProfileIdentifier profileIdentifier) throws IdentityStoreException;

    /**
     * @param userIdentifier
     * @param dialectIdentifier
     * @param profileIdentifier
     * @return
     * @throws IdentityStoreException
     */
    public Profile getUserProfile(UserIdentifier userIdentifier,
                                  DialectIdentifier dialectIdentifier,
                                  ProfileIdentifier profileIdentifier) throws IdentityStoreException;

    /**
     * @param userIdentifier
     * @param dialectIdentifier
     * @param claimUris
     * @return
     * @throws IdentityStoreException
     */
    public List<Profile> getUserProfiles(UserIdentifier userIdentifier,
                                         DialectIdentifier dialectIdentifier, List<ClaimIdentifier> claimUris)
            throws IdentityStoreException;

    /**
     * @param userIdentifier
     * @return
     * @throws IdentityStoreException
     */
    public EntryIdentifier getUserEntryId(UserIdentifier userIdentifier)
            throws IdentityStoreException;

    /**
     * @param groupIdentifier
     * @return
     * @throws IdentityStoreException
     */
    public EntryIdentifier getGroupEntryId(GroupIdentifier groupIdentifier)
            throws IdentityStoreException;

    /**
     * @param userIdentifier
     * @param groupIdentifier
     * @return
     * @throws IdentityStoreException
     */
    public boolean isUserInGroup(UserIdentifier userIdentifier,
                                 GroupIdentifier groupIdentifier) throws IdentityStoreException;

    /**
     * @param groupIdentifier
     * @return
     * @throws IdentityStoreException
     */
    public List<U> getUsersInGroup(GroupIdentifier groupIdentifier)
            throws IdentityStoreException;

    /**
     * @param groupIdentifier
     * @param searchCriteria
     * @return
     * @throws IdentityStoreException
     */
    public List<U> getUsersInGroup(GroupIdentifier groupIdentifier,
                                   UserSearchCriteria searchCriteria) throws IdentityStoreException;

    /**
     * @param groupIdentifier
     * @return
     * @throws IdentityStoreException
     */
    public List<EntityTree> getGroupChildren(GroupIdentifier groupIdentifier)
            throws IdentityStoreException;

    /**
     * @param parentGroupIdentifier
     * @param childGroupIdentifier
     * @return
     * @throws IdentityStoreException
     */
    public boolean hasChildGroup(GroupIdentifier parentGroupIdentifier,
                                 GroupIdentifier childGroupIdentifier) throws IdentityStoreException;

    /**
     * @param childGroupIdentifier
     * @param parentGroupIdentifier
     * @return
     * @throws IdentityStoreException
     */
    public boolean hasParentGroup(GroupIdentifier childGroupIdentifier,
                                  GroupIdentifier parentGroupIdentifier)
            throws IdentityStoreException;

    /**
     * @param userIdentifier
     * @return
     * @throws IdentityStoreException
     */
    public List<G> getGroups(UserIdentifier userIdentifier)
            throws IdentityStoreException;

    /**
     * @param userIdentifier
     * @param searchCriteria
     * @return
     * @throws IdentityStoreException
     */
    public List<G> getGroups(UserIdentifier userIdentifier,
                             GroupSearchCriteria searchCriteria) throws IdentityStoreException;

    /**
     * @return
     */
    public StoreIdentifier getStoreIdentifier();

    /**
     * @return
     */
    public boolean isReadOnly();

    /**
     * @return
     */
    public ReadOnlyLinkedAccountStore getLinkedAccountStore();

}
