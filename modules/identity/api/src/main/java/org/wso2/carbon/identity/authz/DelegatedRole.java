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

package org.wso2.carbon.identity.authz;

import java.util.Date;
import java.util.List;

public class DelegatedRole extends Role {

	private Date effectiveFrom;
	private Date effectiveUpTo;

	/**
	 * 
	 * @param roleIdentifier
	 * @param permission
	 * @param effectiveFrom
	 * @param effectiveUpTo
	 */
	public DelegatedRole(RoleIdentifier roleIdentifier, List<Permission> permission,
			Date effectiveFrom, Date effectiveUpTo) {
		super(roleIdentifier, permission);
		this.effectiveFrom = effectiveFrom;
		this.effectiveUpTo = effectiveUpTo;
	}

	/**
	 * 
	 * @return
	 */
	public Date getEffectiveUpTo() {
		return effectiveUpTo;
	}

	/**
	 * 
	 * @return
	 */
	public Date getEffectiveFrom() {
		return effectiveFrom;
	}

}