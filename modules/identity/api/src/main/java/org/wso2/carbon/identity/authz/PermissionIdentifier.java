package org.wso2.carbon.identity.authz;

import org.wso2.carbon.identity.authn.StoreIdentifier;
import org.wso2.carbon.identity.commons.EntityIdentifier;

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
