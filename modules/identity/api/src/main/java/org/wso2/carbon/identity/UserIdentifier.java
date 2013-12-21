package org.wso2.carbon.user;

public class UserIdentifier extends EntityIdentifier {

	private DialectIdentifier dialectIdentifier;
	private ClaimIdentifier claimIdentifier;
	private StoreIdentifier storeIdentifier;

	/**
	 * 
	 * @param dialectUri
	 * @param claimUri
	 * @param provider
	 * @param value
	 */
	public UserIdentifier(DialectIdentifier dialectIdentifier, ClaimIdentifier claimIdentifier,
			StoreIdentifier storeIdentifier, String value) {
		super(value);
		this.dialectIdentifier = dialectIdentifier;
		this.claimIdentifier = claimIdentifier;
		this.storeIdentifier = storeIdentifier;
	}

	/**
	 * 
	 * @return
	 */
	public DialectIdentifier getDialectUri() {
		return dialectIdentifier;
	}

	/**
	 * 
	 * @return
	 */
	public ClaimIdentifier getClaimUri() {
		return claimIdentifier;
	}

	/**
	 * 
	 * @return
	 */
	public StoreIdentifier getStoreIdentifier() {
		return storeIdentifier;
	}

}