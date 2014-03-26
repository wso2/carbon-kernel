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

package org.wso2.carbon.context.login.identifiers;

public class UserIdentifier extends EntityIdentifier {

	private DialectIdentifier dialectIdentifier;
	private ClaimIdentifier claimIdentifier;
	private StoreIdentifier storeIdentifier;

	/**
	 * 
	 * @param dialectUri
	 * @param claimUri
	 * @param provider
	 * @param value
	 */
	public UserIdentifier(DialectIdentifier dialectIdentifier, ClaimIdentifier claimIdentifier,
			StoreIdentifier storeIdentifier, String value) {
		super(value);
		this.dialectIdentifier = dialectIdentifier;
		this.claimIdentifier = claimIdentifier;
		this.storeIdentifier = storeIdentifier;
	}

	/**
	 * 
	 * @return
	 */
	public DialectIdentifier getDialectUri() {
		return dialectIdentifier;
	}

	/**
	 * 
	 * @return
	 */
	public ClaimIdentifier getClaimUri() {
		return claimIdentifier;
	}

	/**
	 * 
	 * @return
	 */
	public StoreIdentifier getStoreIdentifier() {
		return storeIdentifier;
	}

}