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

package org.wso2.carbon.identity.credential.spi;

import java.util.Properties;

import org.wso2.carbon.identity.commons.EntryIdentifier;
import org.wso2.carbon.identity.credential.InvalideCredentialException;
import org.wso2.carbon.identity.credential.UnsupportedCredentialException;

public interface CredentialStore {

	/**
	 * 
	 * @param properties
	 */
	public void init(Properties properties);

	/**
	 * 
	 * @param credential
	 * @throws InvalidCredentialException
	 */
	@SuppressWarnings("rawtypes")
	public void validateCredentials(EntryIdentifier entryId, Credential credential) throws InvalideCredentialException;

	/**
	 * 
	 * @param entryId
	 * @param newCredentials
	 * @throws UnsupportedCredentialException
	 */
	@SuppressWarnings("rawtypes")
	public void resetCredentials(EntryIdentifier entryId, Credential newCredentials) throws UnsupportedCredentialException;

	/**
	 * 
	 * @param entryId
	 * @param credential
	 * @throws UnsupportedCredentialException
	 */
	@SuppressWarnings("rawtypes")
	public void addCredential(EntryIdentifier entryId, Credential credential) throws UnsupportedCredentialException;

	/**
	 * 
	 * @param entryId
	 * @param credential
	 */
	@SuppressWarnings("rawtypes")
	public void removeCredential(EntryIdentifier entryId, Credential credential);

}
