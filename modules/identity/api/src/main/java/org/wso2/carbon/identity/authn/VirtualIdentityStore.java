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

import org.wso2.carbon.identity.authn.spi.GroupSearchCriteria;
import org.wso2.carbon.identity.authn.spi.UserSearchCriteria;
import org.wso2.carbon.identity.credential.spi.Credential;

public interface VirtualIdentityStore {

	/**
	 * 
	 * @param credential
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public PrivilegedUser authenticate(Credential credential);

	/**
	 * Creates a user in the underlying user store.
	 * 
	 * @param user
	 * @return
	 */
	public PrivilegedUser createUser(User user);

	/**
	 * 
	 * @param userIdentifier
	 * @return
	 */
	public PrivilegedUser getUser(UserIdentifier userIdentifier);

	/**
	 * 
	 * @param searchCriteria
	 * @return
	 */
	public List<PrivilegedUser> getUsers(UserSearchCriteria searchCriteria);

	/**
	 * Creates a group in the underlying user store.
	 * 
	 * @param group
	 * @return
	 */
	public PrivilegedGroup createGroup(Group group);

	/**
	 * 
	 * @param userIdentifier
	 * @return
	 */
	public PrivilegedGroup getGroup(GroupIdentifier userIdentifier);

	/**
	 * 
	 * @param searchCriteria
	 * @return
	 */
	public List<PrivilegedGroup> getGroups(GroupSearchCriteria searchCriteria);

}
