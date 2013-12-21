package org.wso2.carbon.identity.account;

import java.util.List;
import java.util.Properties;

import org.wso2.carbon.identity.authn.UserIdentifier;
import org.wso2.carbon.identity.commons.EntryIdentifier;

public interface LinkedAccountStore {

	/**
	 * 
	 * @param properties
	 */
	public void init(Properties properties);

	/**
	 * 
	 * @param entryIdentifier
	 * @param linkedEntryIdentifier
	 */
	public void link(EntryIdentifier entryIdentifier, EntryIdentifier linkedEntryIdentifier);

	/**
	 * 
	 * @param entryIdentifier
	 * @param linkedEntryIdentifier
	 */
	public void unlink(EntryIdentifier entryIdentifier, EntryIdentifier linkedEntryIdentifier);

	/**
	 * 
	 * @param entryIdentifier
	 * @return
	 */
	public List<UserIdentifier> getLinkedAccounts(EntryIdentifier entryIdentifier);
}
