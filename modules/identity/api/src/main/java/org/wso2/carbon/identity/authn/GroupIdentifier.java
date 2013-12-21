package org.wso2.carbon.identity.authn;

import org.wso2.carbon.identity.commons.EntityIdentifier;
import org.wso2.carbon.identity.commons.Visibility;

public class GroupIdentifier extends EntityIdentifier {

	private Visibility visibility;
	private StoreIdentifier storeIdentifier;

	public GroupIdentifier(StoreIdentifier storeIdentifier, String value, Visibility visibility) {
		super(value);
		this.storeIdentifier = storeIdentifier;
		this.visibility = visibility;
	}

	public Visibility getVisibility() {
		return visibility;
	}

	public StoreIdentifier getStoreIdentifier() {
		return storeIdentifier;
	}

}
