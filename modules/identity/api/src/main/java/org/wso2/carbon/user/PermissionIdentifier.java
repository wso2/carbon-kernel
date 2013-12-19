package org.wso2.carbon.user;

public class PermissionIdentifier extends EntityIdentifier {

	private StoreIdentifier storeIdentifier;

	/**
	 * 
	 * @param value
	 * @param provider
	 */
	public PermissionIdentifier(String value, StoreIdentifier storeIdentifier) {
		super(value);
		this.storeIdentifier = storeIdentifier;
	}

	/**
	 * 
	 * @param storeIdentifier
	 */
	public PermissionIdentifier(StoreIdentifier storeIdentifier) {
		super(null);
		this.storeIdentifier = storeIdentifier;
	}

	public StoreIdentifier getStoreIdentifier() {
		return storeIdentifier;
	}

}
