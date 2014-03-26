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

package org.wso2.carbon.identity.authn;

import java.util.Collections;
import java.util.List;

import org.wso2.carbon.identity.claim.ClaimIdentifier;
import org.wso2.carbon.identity.claim.ClaimManager;
import org.wso2.carbon.identity.claim.ClaimNotMappedException;
import org.wso2.carbon.identity.claim.DialectIdentifier;
import org.wso2.carbon.identity.commons.AttributeIdentifier;

public class StoreDialectCollection {

	private ClaimManager claimManager;
	private StoreIdentifier storeIdentifier;

	/**
	 * 
	 * @param storeIdentifier
	 * @param claimManager
	 */
	public StoreDialectCollection(StoreIdentifier storeIdentifier,
			ClaimManager claimManager) {
		this.claimManager = claimManager;
		this.storeIdentifier = storeIdentifier;
	}

	/**
	 * 
	 * @param dialectIdentifier
	 * @param claimIdentifier
	 * @return
	 * @throws ClaimNotMappedException
	 */
	public AttributeIdentifier getAttributeIdentifier(
			DialectIdentifier dialectIdentifier, ClaimIdentifier claimIdentifier)
			throws ClaimNotMappedException {
		return claimManager.getAttributeIdentifier(dialectIdentifier,
				claimIdentifier, storeIdentifier);
	}

	/**
	 * 
	 * @param dialectIdentifier
	 * @return
	 * @throws ClaimNotMappedException
	 */
	public List<AttributeIdentifier> getAllAttributeIdentifiers(
			DialectIdentifier dialectIdentifier) throws ClaimNotMappedException {
		List<AttributeIdentifier> attrIdentifiers = claimManager
				.getAllAttributeIdentifiers(dialectIdentifier, storeIdentifier);
		return Collections.unmodifiableList(attrIdentifiers);
	}

}
