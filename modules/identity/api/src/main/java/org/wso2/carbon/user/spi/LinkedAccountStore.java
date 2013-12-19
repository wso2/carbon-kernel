package org.wso2.carbon.user.spi;

import java.util.List;
import java.util.Properties;

import org.wso2.carbon.user.EntryIdentifier;
import org.wso2.carbon.user.UserIdentifier;

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
