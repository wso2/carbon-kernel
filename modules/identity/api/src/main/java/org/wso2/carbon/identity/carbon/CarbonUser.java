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

package org.wso2.carbon.identity.carbon;

import java.util.List;

import org.wso2.carbon.identity.carbon.commons.IdentityException;
import org.wso2.carbon.identity.carbon.identifiers.EntryIdentifier;
import org.wso2.carbon.identity.carbon.identifiers.GroupIdentifier;
import org.wso2.carbon.identity.carbon.identifiers.RoleIdentifier;
import org.wso2.carbon.identity.carbon.identifiers.StoreIdentifier;
import org.wso2.carbon.identity.carbon.identifiers.UserIdentifier;

public interface CarbonUser<G extends CarbonGroup, R extends CarbonRole> extends User {

	/**
	 * 
	 * @return
	 */
	public EntryIdentifier getUserEntryId();

	/**
	 * 
	 * @return
	 * @throws IdentityException
	 */
	public List<G> getGroups() throws IdentityException;

	/**
	 * 
	 * @return
	 * @throws IdentityException
	 */
	public List<R> getRoles() throws IdentityException;


	/**
	 * 
	 * @return
	 */
	public StoreIdentifier getStoreIdentifier();

	/**
	 * 
	 * @param roleIdentifie
	 * @return
	 * @throws IdentityException
	 */
	public boolean hasRole(RoleIdentifier roleIdentifie) throws IdentityException;

	/**
	 * 
	 * @param permission
	 * @return
	 * @throws IdentityException
	 */
	public boolean hasPermission(CarbonPermission permission) throws IdentityException;
	
	/**
	 * 
	 * @param groupIdentifier
	 * @return
	 * @throws IdentityException
	 */
	public boolean inGroup(GroupIdentifier groupIdentifier) throws IdentityException;

	/**
	 * 
	 * @return
	 * @throws IdentityException 
	 */
	public List<UserIdentifier> getLinkedAccounts() throws IdentityException;

}