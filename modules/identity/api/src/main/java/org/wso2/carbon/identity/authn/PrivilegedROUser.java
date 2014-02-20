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
import org.wso2.carbon.identity.authn.spi.ReadOnlyIdentityStore;
import org.wso2.carbon.identity.authz.AuthorizationStoreException;
import org.wso2.carbon.identity.authz.PrivilegedReadOnlyRole;
import org.wso2.carbon.identity.authz.spi.ReadOnlyAuthorizationStore;
import org.wso2.carbon.identity.authz.spi.RoleSearchCriteria;
import org.wso2.carbon.identity.commons.IdentityException;

public class PrivilegedROUser extends PrivilegedUser<PrivilegedReadOnlyGroup, PrivilegedReadOnlyRole> {

	private ReadOnlyIdentityStore<PrivilegedROUser, PrivilegedReadOnlyGroup, PrivilegedReadOnlyRole> identityStore;
	private ReadOnlyAuthorizationStore<PrivilegedROUser, PrivilegedReadOnlyGroup, PrivilegedReadOnlyRole> authzStore;

	/**
	 * 
	 * @param identityStore
	 * @param authzStore
	 * @param userIdentifier
	 */
	public PrivilegedROUser(UserIdentifier userIdentifier,
			ReadOnlyIdentityStore<PrivilegedROUser, PrivilegedReadOnlyGroup, PrivilegedReadOnlyRole> identityStore,
			ReadOnlyAuthorizationStore<PrivilegedROUser, PrivilegedReadOnlyGroup, PrivilegedReadOnlyRole> authzStore) 
			throws IdentityException{
		super(userIdentifier, identityStore, authzStore);
		this.authzStore = authzStore;
		this.identityStore = identityStore;
	}

	/**
	 * 
	 * @return
	 * @throws IdentityStoreException
	 */
	public List<PrivilegedReadOnlyGroup> getGroups() throws IdentityStoreException {
		List<PrivilegedReadOnlyGroup> groups = identityStore.getGroups(getUserIdentifier());
		return Collections.unmodifiableList(groups);
	}

	/**
	 * 
	 * @param searchCriteria
	 * @return
	 * @throws IdentityStoreException 
	 */
	public List<PrivilegedReadOnlyGroup> getGroups(GroupSearchCriteria searchCriteria) throws IdentityStoreException {
		List<PrivilegedReadOnlyGroup> groups = identityStore.getGroups(getUserIdentifier(), searchCriteria);
		return Collections.unmodifiableList(groups);
	}

	/**
	 * 
	 * @return
	 * @throws AuthorizationStoreException
	 */
	public List<PrivilegedReadOnlyRole> getRoles() throws AuthorizationStoreException {
		List<PrivilegedReadOnlyRole> roles = authzStore.getRoles(getUserIdentifier());
		return Collections.unmodifiableList(roles);
	}

	/**
	 * 
	 * @param searchCriteria
	 * @return
	 * @throws AuthorizationStoreException
	 */
	public List<PrivilegedReadOnlyRole> getRoles(RoleSearchCriteria searchCriteria) throws AuthorizationStoreException {
		List<PrivilegedReadOnlyRole> roles = authzStore.getRoles(getUserIdentifier(), searchCriteria);
		return Collections.unmodifiableList(roles);
	}

}