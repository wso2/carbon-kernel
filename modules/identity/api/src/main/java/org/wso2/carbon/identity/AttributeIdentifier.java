package org.wso2.carbon.user;

public class AttributeIdentifier extends EntityIdentifier {

	private StoreIdentifier storeIdentifier;

	/**
	 * 
	 * @param storeIdentifier
	 * @param value
	 */
	public AttributeIdentifier(StoreIdentifier storeIdentifier, String value) {
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
