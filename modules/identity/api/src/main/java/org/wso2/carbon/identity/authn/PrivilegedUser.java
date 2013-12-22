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
import org.wso2.carbon.identity.authz.Permission;
import org.wso2.carbon.identity.authz.PrivilegedRole;
import org.wso2.carbon.identity.authz.RoleIdentifier;
import org.wso2.carbon.identity.authz.spi.AuthorizationStore;
import org.wso2.carbon.identity.authz.spi.RoleSearchCriteria;
import org.wso2.carbon.identity.claim.Claim;
import org.wso2.carbon.identity.claim.ClaimIdentifier;
import org.wso2.carbon.identity.claim.DialectIdentifier;
import org.wso2.carbon.identity.commons.EntryIdentifier;
import org.wso2.carbon.identity.credential.spi.Credential;
import org.wso2.carbon.identity.profile.Profile;
import org.wso2.carbon.identity.profile.ProfileIdentifier;

public class PrivilegedUser extends User {

	private IdentityStore identityStore;
	private AuthorizationStore authzStore;
	private EntryIdentifier entryIdentifier;

	/**
	 * 
	 * @param identityStore
	 * @param authzStore
	 * @param userIdentifier
	 */
	public PrivilegedUser(IdentityStore identityStore, AuthorizationStore authzStore,
			UserIdentifier userIdentifier) {
		super(userIdentifier);
		this.authzStore = authzStore;
		this.identityStore = identityStore;
	}

	/**
	 * 
	 * @return
	 */
	public EntryIdentifier getUserEntryId() {
		if (entryIdentifier == null) {
			entryIdentifier = identityStore.getUserEntryId(getUserIdentifier());
		}
		return entryIdentifier;
	}

	/**
	 * 
	 * @return
	 */
	public List<PrivilegedGroup> getGroups() {
		List<PrivilegedGroup> groups = identityStore.getGroups(getUserIdentifier());
		return Collections.unmodifiableList(groups);
	}

	/**
	 * 
	 * @param searchCriteria
	 * @return
	 */
	public List<PrivilegedGroup> getGroups(GroupSearchCriteria searchCriteria) {
		List<PrivilegedGroup> groups = identityStore.getGroups(getUserIdentifier(), searchCriteria);
		return Collections.unmodifiableList(groups);
	}

	/**
	 * 
	 * @return
	 */
	public List<PrivilegedRole> getRoles() {
		List<PrivilegedRole> roles = authzStore.getRoles(getUserIdentifier());
		return Collections.unmodifiableList(roles);
	}

	/**
	 * 
	 * @param searchCriteria
	 * @return
	 */
	public List<PrivilegedRole> getRoles(RoleSearchCriteria searchCriteria) {
		List<PrivilegedRole> roles = authzStore.getRoles(getUserIdentifier(), searchCriteria);
		return Collections.unmodifiableList(roles);
	}

	/**
	 * 
	 * @param dialectIdentifier
	 * @param profileIdentifier
	 * @return
	 */
	public List<Claim> getAttributes(DialectIdentifier dialectIdentifier,
			ProfileIdentifier profileIdentifier) {
		List<Claim> claims = identityStore.getUserAttributes(getUserIdentifier(),
				dialectIdentifier, profileIdentifier);
		return Collections.unmodifiableList(claims);
	}

	/**
	 * 
	 * @param dialectIdentifier
	 * @return
	 */
	public List<Claim> getAttributes(DialectIdentifier dialectIdentifier) {
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
	 */
	public List<Claim> getAttributes(DialectIdentifier dialectIdentifier,
			List<ClaimIdentifier> claimUris, ProfileIdentifier profileIdentifier) {
		List<Claim> claims = identityStore.getUserAttributes(getUserIdentifier(),
				dialectIdentifier, claimUris, profileIdentifier);
		return Collections.unmodifiableList(claims);
	}

	/**
	 * 
	 * @param dialectIdentifier
	 * @param claims
	 * @param profileIdentifier
	 */
	public void addAttributes(DialectIdentifier dialectIdentifier, List<Claim> claims,
			ProfileIdentifier profileIdentifier) {
		identityStore.addUserAttributes(getUserIdentifier(), dialectIdentifier, claims,
				profileIdentifier);
	}

	/**
	 * 
	 * @param dialectIdentifier
	 * @param claims
	 */
	public void addAttributes(DialectIdentifier dialectIdentifier, List<Claim> claims) {
		identityStore.addUserAttributes(getUserIdentifier(), dialectIdentifier, claims, null);
	}

	/**
	 * 
	 * @param dialectIdentifier
	 * @param claimUris
	 * @return
	 */
	public List<Profile> getProfiles(DialectIdentifier dialectIdentifier,
			List<ClaimIdentifier> claimIdentifier) {
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
	 * @param groupIdentifiers
	 */
	public void addToGroup(List<GroupIdentifier> groupIdentifiers) {
		identityStore.addUserToGroups(groupIdentifiers, getUserIdentifier());
	}

	/**
	 * 
	 * @param roleIdentifiers
	 */
	public void assignToRoles(List<RoleIdentifier> roleIdentifiers) {
		authzStore.assignRolesToUser(getUserIdentifier(), roleIdentifiers);
	}

	/**
	 * 
	 * @param roleIdentifier
	 */
	public void assignToRole(RoleIdentifier roleIdentifier) {
		authzStore.assignRoleToUser(getUserIdentifier(), roleIdentifier);
	}

	/**
	 * 
	 * @param roleIdentifie
	 * @return
	 */
	public boolean hasRole(RoleIdentifier roleIdentifie) {
		return authzStore.isUserHasRole(getUserIdentifier(), roleIdentifie);
	}

	/**
	 * 
	 * @param permission
	 * @return
	 */
	public boolean hasPermission(Permission permission) {
		return authzStore.isUserHasPermission(getUserIdentifier(), permission);
	}

	/**
	 * 
	 * @param groupIdentifier
	 * @return
	 */
	public boolean inGroup(GroupIdentifier groupIdentifier) {
		return identityStore.isUserInGroup(getUserIdentifier(), groupIdentifier);
	}

	/**
	 * 
	 * @param linkedEntryIdentifier
	 */
	public void linkAccount(EntryIdentifier linkedEntryIdentifier) {
		identityStore.getLinkedAccountStore().link(entryIdentifier, linkedEntryIdentifier);
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
	 * 
	 * @param linkedEntryIdentifier
	 */
	public void unlinkAccount(EntryIdentifier linkedEntryIdentifier) {
		identityStore.getLinkedAccountStore().unlink(entryIdentifier, linkedEntryIdentifier);
	}

	/**
	 * 
	 * @param newCredentials
	 */
	@SuppressWarnings("rawtypes")
	public void resetCredentials(Credential newCredentials) {
		identityStore.resetCredentials(newCredentials);
	}

	/**
	 * 
	 * @param credential
	 */
	@SuppressWarnings("rawtypes")
	public void addCredential(Credential credential) {
		identityStore.addCredential(credential);
	}

	/**
	 * 
	 * @param credential
	 */
	@SuppressWarnings("rawtypes")
	public void removeCredential(Credential credential) {
		identityStore.removeCredential(credential);
	}

	/**
	 * 
	 */
	public void drop() {
		identityStore.dropUser(getUserIdentifier());
	}

}