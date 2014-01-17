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

import java.util.List;

import org.wso2.carbon.identity.account.AccountException;
import org.wso2.carbon.identity.authn.spi.IdentityStore;
import org.wso2.carbon.identity.authz.AuthorizationStoreException;
import org.wso2.carbon.identity.authz.RoleIdentifier;
import org.wso2.carbon.identity.authz.spi.AuthorizationStore;
import org.wso2.carbon.identity.claim.Claim;
import org.wso2.carbon.identity.claim.DialectIdentifier;
import org.wso2.carbon.identity.commons.EntryIdentifier;
import org.wso2.carbon.identity.credential.spi.Credential;
import org.wso2.carbon.identity.profile.ProfileIdentifier;

public class PrivilegedRWUser extends PrivilegedROUser {

	private IdentityStore identityStore;
	private AuthorizationStore authzStore;
	private EntryIdentifier entryIdentifier;

	/**
	 * 
	 * @param identityStore
	 * @param authzStore
	 * @param userIdentifier
	 */
	public PrivilegedRWUser(IdentityStore identityStore, AuthorizationStore authzStore,
			UserIdentifier userIdentifier) {
		super(identityStore, authzStore, userIdentifier);
		this.authzStore = authzStore;
		this.identityStore = identityStore;
	}

	/**
	 * 
	 * @param dialectIdentifier
	 * @param claims
	 * @param profileIdentifier
	 * @throws IdentityStoreException
	 */
	public void addAttributes(DialectIdentifier dialectIdentifier, List<Claim> claims,
			ProfileIdentifier profileIdentifier) throws IdentityStoreException {
		identityStore.addUserAttributes(getUserIdentifier(), dialectIdentifier, claims,
				profileIdentifier);
	}

	/**
	 * 
	 * @param dialectIdentifier
	 * @param claims
	 * @throws IdentityStoreException
	 */
	public void addAttributes(DialectIdentifier dialectIdentifier, List<Claim> claims) throws IdentityStoreException {
		identityStore.addUserAttributes(getUserIdentifier(), dialectIdentifier, claims, null);
	}

	/**
	 * 
	 * @param groupIdentifiers
	 * @throws IdentityStoreException
	 */
	public void addToGroup(List<GroupIdentifier> groupIdentifiers) throws IdentityStoreException {
		identityStore.addUserToGroups(groupIdentifiers, getUserIdentifier());
	}

	/**
	 * 
	 * @param roleIdentifiers
	 * @throws AuthorizationStoreException
	 */
	public void assignToRoles(List<RoleIdentifier> roleIdentifiers) throws AuthorizationStoreException {
		authzStore.assignRolesToUser(getUserIdentifier(), roleIdentifiers);
	}

	/**
	 * 
	 * @param roleIdentifier
	 * @throws AuthorizationStoreException
	 */
	public void assignToRole(RoleIdentifier roleIdentifier) throws AuthorizationStoreException {
		authzStore.assignRoleToUser(getUserIdentifier(), roleIdentifier);
	}

	/**
	 * 
	 * @param linkedEntryIdentifier
	 * @throws AccountException
	 */
	public void linkAccount(EntryIdentifier linkedEntryIdentifier) throws AccountException {
		identityStore.getLinkedAccountStore().link(entryIdentifier, linkedEntryIdentifier);
	}

	/**
	 * 
	 * @param linkedEntryIdentifier
	 * @throws AccountException
	 */
	public void unlinkAccount(EntryIdentifier linkedEntryIdentifier) throws AccountException {
		identityStore.getLinkedAccountStore().unlink(entryIdentifier, linkedEntryIdentifier);
	}

	/**
	 * 
	 * @param newCredentials
	 * @throws IdentityStoreException
	 */
	@SuppressWarnings("rawtypes")
	public void resetCredentials(Credential newCredentials) throws IdentityStoreException {
		identityStore.resetCredentials(newCredentials);
	}

	/**
	 * 
	 * @param credential
	 * @throws IdentityStoreException
	 */
	@SuppressWarnings("rawtypes")
	public void addCredential(Credential credential) throws IdentityStoreException {
		identityStore.addCredential(credential);
	}

	/**
	 * 
	 * @param credential
	 * @throws IdentityStoreException
	 */
	@SuppressWarnings("rawtypes")
	public void removeCredential(Credential credential) throws IdentityStoreException {
		identityStore.removeCredential(credential);
	}

	/**
	 * @throws IdentityStoreException
	 * 
	 */
	public void drop() throws IdentityStoreException {
		identityStore.dropUser(getUserIdentifier());
	}

}