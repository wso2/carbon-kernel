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

public class Permission {

	private String resource;
	private String action;
	private PermissionIdentifier permissionIdentifier;
	private String description;

	/**
	 * 
	 * @param resource
	 * @param action
	 * @param storeIdentifier
	 */
	public Permission(String resource, String action, PermissionIdentifier permissionIdentifier) {
		this.resource = resource;
		this.action = action;
		this.permissionIdentifier = permissionIdentifier;
	}

	/**
	 * 
	 * @param resource
	 * @param action
	 * @param storeIdentifier
	 * @param description
	 */
	public Permission(String resource, String action, PermissionIdentifier permissionIdentifier,
			String description) {
		this.resource = resource;
		this.action = action;
		this.permissionIdentifier = permissionIdentifier;
		this.description = description;
	}

	/**
	 * 
	 * @return
	 */
	public String getResource() {
		return resource;
	}

	/**
	 * 
	 * @return
	 */
	public String getAction() {
		return action;
	}

	/**
	 * 
	 * @return
	 */
	public PermissionIdentifier getPermissionIdentifier() {
		return permissionIdentifier;
	}

	/**
	 * 
	 * @return
	 */
	public String getDescription() {
		return description;
	}

}
