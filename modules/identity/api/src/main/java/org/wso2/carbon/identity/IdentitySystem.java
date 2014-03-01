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

package org.wso2.carbon.identity;

import org.wso2.carbon.identity.account.spi.ReadWriteLinkedAccountStore;
import org.wso2.carbon.identity.authn.IdentityStoreManager;
import org.wso2.carbon.identity.authz.AuthorizationStoreManager;
import org.wso2.carbon.identity.claim.ClaimManager;
import org.wso2.carbon.identity.commons.IdentityException;
import org.wso2.carbon.identity.config.IdentityServiceConfig;

/**
 * Entry point in Identity library for administrative operations.
 */
public interface IdentitySystem {
	
	/**
	 * 
	 * @param identityServiceConfig
	 * @throws IdentityException
	 */
	public void init(IdentityServiceConfig identityServiceConfig) throws IdentityException;
	
	/**
	 * 
	 * @return
	 */
	public IdentityStoreManager getIdentityStoreManager();
//	public ReadWriteIdentityStoreManager getIdentityStoreManager();

	/**
	 * 
	 * @return
	 */
	public AuthorizationStoreManager getAuthorizationStoreManager();
//	public ReadWriteAuthorizationStoreManager getAuthorizationStoreManager();

	/**
	 * 
	 * @return
	 */
	public ClaimManager getClaimManager();

	/**
	 * 
	 * @return
	 */
	public ReadWriteLinkedAccountStore getLinkedAccountStore();
}
