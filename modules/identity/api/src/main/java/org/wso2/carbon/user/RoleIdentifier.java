package org.wso2.carbon.user;

public class RoleIdentifier extends EntityIdentifier {

	private StoreIdentifier storeIdentifier;

	/**
	 * 
	 * @param value
	 * @param provider
	 */
	public RoleIdentifier(String value, StoreIdentifier storeIdentifier) {
		super(value);
		this.storeIdentifier = storeIdentifier;
	}

	public StoreIdentifier getStoreIdentifier() {
		return storeIdentifier;
	}
}
