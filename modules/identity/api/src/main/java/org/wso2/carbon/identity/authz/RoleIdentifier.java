package org.wso2.carbon.identity.authz;

import org.wso2.carbon.identity.authn.StoreIdentifier;
import org.wso2.carbon.identity.commons.EntityIdentifier;

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
