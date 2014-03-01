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

import org.wso2.carbon.identity.account.AccountException;
import org.wso2.carbon.identity.authn.spi.GroupSearchCriteria;
import org.wso2.carbon.identity.authn.spi.ReadWriteIdentityStore;
import org.wso2.carbon.identity.authz.AuthorizationStoreException;
import org.wso2.carbon.identity.authz.PrivilegedReadWriteRole;
import org.wso2.carbon.identity.authz.RoleIdentifier;
import org.wso2.carbon.identity.authz.spi.ReadWriteAuthorizationStore;
import org.wso2.carbon.identity.authz.spi.RoleSearchCriteria;
import org.wso2.carbon.identity.claim.Claim;
import org.wso2.carbon.identity.claim.DialectIdentifier;
import org.wso2.carbon.identity.commons.EntryIdentifier;
import org.wso2.carbon.identity.commons.IdentityException;
import org.wso2.carbon.identity.credential.spi.Credential;
import org.wso2.carbon.identity.profile.ProfileIdentifier;

public class PrivilegedRWUser extends PrivilegedUser<PrivilegedReadWriteGroup, PrivilegedReadWriteRole> {

	private ReadWriteIdentityStore identityStore;
	private ReadWriteAuthorizationStore authzStore;

	/**
	 * 
	 * @param identityStore
	 * @param authzStore
	 * @param userIdentifier
	 * @throws IdentityException 
	 */
	public PrivilegedRWUser(UserIdentifier userIdentifier, ReadWriteIdentityStore identityStore, ReadWriteAuthorizationStore authzStore) throws IdentityException {
		super(userIdentifier, identityStore, authzStore);
		this.identityStore = identityStore;
		this.authzStore = authzStore;
	}

	/**
	 * 
	 * @return
	 * @throws IdentityStoreException
	 */
	public List<PrivilegedReadWriteGroup> getGroups() throws IdentityStoreException {
		List<PrivilegedReadWriteGroup> groups = identityStore.getGroups(getUserIdentifier());
		return Collections.unmodifiableList(groups);
	}

	/**
	 * 
	 * @param searchCriteria
	 * @return
	 * @throws IdentityStoreException 
	 */
	public List<PrivilegedReadWriteGroup> getGroups(GroupSearchCriteria searchCriteria) throws IdentityStoreException {
		List<PrivilegedReadWriteGroup> groups = identityStore.getGroups(getUserIdentifier(), searchCriteria);
		return Collections.unmodifiableList(groups);
	}

	/**
	 * 
	 * @return
	 * @throws AuthorizationStoreException
	 */
	public List<PrivilegedReadWriteRole> getRoles() throws AuthorizationStoreException {
		List<PrivilegedReadWriteRole> roles = authzStore.getRoles(getUserIdentifier());
		return Collections.unmodifiableList(roles);
	}

	/**
	 * 
	 * @param searchCriteria
	 * @return
	 * @throws AuthorizationStoreException
	 */
	public List<PrivilegedReadWriteRole> getRoles(RoleSearchCriteria searchCriteria) throws AuthorizationStoreException {
		List<PrivilegedReadWriteRole> roles = authzStore.getRoles(getUserIdentifier(), searchCriteria);
		return Collections.unmodifiableList(roles);
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
	 * @throws IdentityStoreException 
	 */
	public void linkAccount(EntryIdentifier linkedEntryIdentifier) throws AccountException, IdentityStoreException {
		identityStore.getLinkedAccountStore().link(getUserEntryId(), linkedEntryIdentifier);
	}

	/**
	 * 
	 * @param linkedEntryIdentifier
	 * @throws AccountException
	 * @throws IdentityStoreException 
	 */
	public void unlinkAccount(EntryIdentifier linkedEntryIdentifier) throws AccountException, IdentityStoreException {
		identityStore.getLinkedAccountStore().unlink(getUserEntryId(), linkedEntryIdentifier);
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