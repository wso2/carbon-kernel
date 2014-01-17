/*
 *  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.authn;

import java.util.Collections;
import java.util.List;

import org.wso2.carbon.identity.authn.spi.GroupSearchCriteria;
import org.wso2.carbon.identity.authn.spi.IdentityStore;
import org.wso2.carbon.identity.authz.AuthorizationStoreException;
import org.wso2.carbon.identity.authz.Permission;
import org.wso2.carbon.identity.authz.PrivilegedRole;
import org.wso2.carbon.identity.authz.RoleIdentifier;
import org.wso2.carbon.identity.authz.spi.AuthorizationStore;
import org.wso2.carbon.identity.authz.spi.RoleSearchCriteria;
import org.wso2.carbon.identity.claim.Claim;
import org.wso2.carbon.identity.claim.ClaimIdentifier;
import org.wso2.carbon.identity.claim.DialectIdentifier;
import org.wso2.carbon.identity.commons.EntryIdentifier;
import org.wso2.carbon.identity.profile.Profile;
import org.wso2.carbon.identity.profile.ProfileIdentifier;

public class PrivilegedROUser extends User {

	private IdentityStore identityStore;
	private AuthorizationStore authzStore;
	private EntryIdentifier entryIdentifier;

	/**
	 * 
	 * @param identityStore
	 * @param authzStore
	 * @param userIdentifier
	 */
	public PrivilegedROUser(IdentityStore identityStore, AuthorizationStore authzStore,
			UserIdentifier userIdentifier) {
		super(userIdentifier);
		this.authzStore = authzStore;
		this.identityStore = identityStore;
	}

	/**
	 * 
	 * @return
	 * @throws IdentityStoreException 
	 */
	public EntryIdentifier getUserEntryId() throws IdentityStoreException {
		if (entryIdentifier == null) {
			entryIdentifier = identityStore.getUserEntryId(getUserIdentifier());
		}
		return entryIdentifier;
	}

	/**
	 * 
	 * @return
	 * @throws IdentityStoreException
	 */
	public List<PrivilegedGroup> getGroups() throws IdentityStoreException {
		List<PrivilegedGroup> groups = identityStore.getGroups(getUserIdentifier());
		return Collections.unmodifiableList(groups);
	}

	/**
	 * 
	 * @param searchCriteria
	 * @return
	 * @throws IdentityStoreException 
	 */
	public List<PrivilegedGroup> getGroups(GroupSearchCriteria searchCriteria) throws IdentityStoreException {
		List<PrivilegedGroup> groups = identityStore.getGroups(getUserIdentifier(), searchCriteria);
		return Collections.unmodifiableList(groups);
	}

	/**
	 * 
	 * @return
	 * @throws AuthorizationStoreException
	 */
	public List<PrivilegedRole> getRoles() throws AuthorizationStoreException {
		List<PrivilegedRole> roles = authzStore.getRoles(getUserIdentifier());
		return Collections.unmodifiableList(roles);
	}

	/**
	 * 
	 * @param searchCriteria
	 * @return
	 * @throws AuthorizationStoreException
	 */
	public List<PrivilegedRole> getRoles(RoleSearchCriteria searchCriteria) throws AuthorizationStoreException {
		List<PrivilegedRole> roles = authzStore.getRoles(getUserIdentifier(), searchCriteria);
		return Collections.unmodifiableList(roles);
	}

	/**
	 * 
	 * @param dialectIdentifier
	 * @param profileIdentifier
	 * @return
	 * @throws IdentityStoreException
	 */
	public List<Claim> getAttributes(DialectIdentifier dialectIdentifier,
			ProfileIdentifier profileIdentifier) throws IdentityStoreException {
		List<Claim> claims = identityStore.getUserAttributes(getUserIdentifier(),
				dialectIdentifier, profileIdentifier);
		return Collections.unmodifiableList(claims);
	}

	/**
	 * 
	 * @param dialectIdentifier
	 * @return
	 * @throws IdentityStoreException
	 */
	public List<Claim> getAttributes(DialectIdentifier dialectIdentifier) throws IdentityStoreException {
		List<Claim> claims = identityStore.getUserAttributes(getUserIdentifier(),
				dialectIdentifier, null);
		return Collections.unmodifiableList(claims);
	}

	/**
	 * 
	 * @param dialectIdentifier
	 * @param claimUris
	 * @param profileIdentifier
	 * @return
	 * @throws IdentityStoreException
	 */
	public List<Claim> getAttributes(DialectIdentifier dialectIdentifier,
			List<ClaimIdentifier> claimUris, ProfileIdentifier profileIdentifier) throws IdentityStoreException {
		List<Claim> claims = identityStore.getUserAttributes(getUserIdentifier(),
				dialectIdentifier, claimUris, profileIdentifier);
		return Collections.unmodifiableList(claims);
	}

	/**
	 * 
	 * @param dialectIdentifier
	 * @param claimUris
	 * @return
	 * @throws IdentityStoreException
	 */
	public List<Profile> getProfiles(DialectIdentifier dialectIdentifier,
			List<ClaimIdentifier> claimIdentifier) throws IdentityStoreException {
		List<Profile> profiles = identityStore.getUserProfiles(getUserIdentifier(),
				dialectIdentifier, claimIdentifier);
		return Collections.unmodifiableList(profiles);
	}

	/**
	 * 
	 * @return
	 */
	public StoreIdentifier getStoreIdentifier() {
		return identityStore.getStoreIdentifier();
	}

	/**
	 * 
	 * @param roleIdentifie
	 * @return
	 * @throws AuthorizationStoreException
	 */
	public boolean hasRole(RoleIdentifier roleIdentifie) throws AuthorizationStoreException {
		return authzStore.isUserHasRole(getUserIdentifier(), roleIdentifie);
	}

	/**
	 * 
	 * @param permission
	 * @return
	 * @throws AuthorizationStoreException
	 */
	public boolean hasPermission(Permission permission) throws AuthorizationStoreException {
		return authzStore.isUserHasPermission(getUserIdentifier(), permission);
	}

	/**
	 * 
	 * @param groupIdentifier
	 * @return
	 * @throws IdentityStoreException
	 */
	public boolean inGroup(GroupIdentifier groupIdentifier) throws IdentityStoreException {
		return identityStore.isUserInGroup(getUserIdentifier(), groupIdentifier);
	}

	/**
	 * 
	 * @return
	 */
	public List<UserIdentifier> getLinkedAccounts() {
		List<UserIdentifier> accounts = identityStore.getLinkedAccountStore().getLinkedAccounts(
				entryIdentifier);
		return Collections.unmodifiableList(accounts);
	}

	/**
	 * @throws IdentityStoreException
	 * 
	 */
	public void drop() throws IdentityStoreException {
		identityStore.dropUser(getUserIdentifier());
	}

}