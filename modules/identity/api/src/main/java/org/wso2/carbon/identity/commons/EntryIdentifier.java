package org.wso2.carbon.identity.commons;

import org.wso2.carbon.identity.authn.StoreIdentifier;

public class EntryIdentifier extends EntityIdentifier {

	private StoreIdentifier storeIdentifier;

	/**
	 * 
	 * @param value
	 * @param storeIdentifier
	 */
	public EntryIdentifier(String value, StoreIdentifier storeIdentifier) {
		super(value);
		this.storeIdentifier = storeIdentifier;
	}

	/**
	 * 
	 * @return
	 */
	public StoreIdentifier getStoreIdentifier() {
		return storeIdentifier;
	}

}
