package org.wso2.carbon.user.spi;

import java.util.Properties;

import org.wso2.carbon.user.Credential;
import org.wso2.carbon.user.EntryIdentifier;
import org.wso2.carbon.user.InvalidCredentialException;

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
	public void validateCredentials(EntryIdentifier entryId, Credential credential)
			throws InvalidCredentialException;

	/**
	 * 
	 * @param entryId
	 * @param newCredentials
	 */
	@SuppressWarnings("rawtypes")
	public void resetCredentials(EntryIdentifier entryId, Credential newCredentials);

	/**
	 * 
	 * @param entryId
	 * @param credential
	 */
	@SuppressWarnings("rawtypes")
	public void addCredential(EntryIdentifier entryId, Credential credential);

	/**
	 * 
	 * @param entryId
	 * @param credential
	 */
	@SuppressWarnings("rawtypes")
	public void removeCredential(EntryIdentifier entryId, Credential credential);

}
