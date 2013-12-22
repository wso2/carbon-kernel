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

import org.wso2.carbon.identity.account.spi.LinkedAccountStore;
import org.wso2.carbon.identity.claim.ClaimManager;
import org.wso2.carbon.identity.config.spi.IdentityStoreConfig;
import org.wso2.carbon.identity.credential.spi.CredentialStore;

public interface IdentityStoreManager extends ReadOnlyIdentityStoreManager {

	public void init(IdentityStoreConfig primaryStoreConfig, ClaimManager claimManager,
			LinkedAccountStore linkedAccountStore, CredentialStore credentialStore);

	/**
	 * 
	 * @param storeConfig
	 */
	public void addIdentityStore(IdentityStoreConfig storeConfig);

	/**
	 * 
	 * @param storeIdentifier
	 */
	public void dropIdentityStore(StoreIdentifier storeIdentifier);

	/**
	 * 
	 * @param storeConfig
	 */
	public void updateIdentityStore(IdentityStoreConfig storeConfig);

}
