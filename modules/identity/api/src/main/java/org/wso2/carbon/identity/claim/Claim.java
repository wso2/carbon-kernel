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

package org.wso2.carbon.identity.claim;

import org.wso2.carbon.identity.authn.AttributeValue;

/**
 * 
 * A statement that one subject makes about itself or another subject. For
 * example, the statement can be about a name, identity, key, group, privilege,
 * or capability. Claims are issued by a provider, and they are given one or
 * more values and then packaged in security tokens that are issued by a
 * security token service (STS). They are also defined by a claim value type
 * and, possibly, associated meta-data.
 * 
 */
public class Claim {

	private ClaimIdentifier claimUri;
	@SuppressWarnings("rawtypes")
	private AttributeValue attributeValue;

	/**
	 * 
	 * @param claimUri
	 * @param attributeValue
	 */
	@SuppressWarnings("rawtypes")
	public Claim(ClaimIdentifier claimUri, AttributeValue attributeValue) {
		this.claimUri = claimUri;
		this.attributeValue = attributeValue;
	}

	/**
	 * 
	 * @return
	 */
	public ClaimIdentifier getClaimUri() {
		return claimUri;
	}

	/**
	 * 
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public AttributeValue getAttribute() {
		return attributeValue;
	}

}