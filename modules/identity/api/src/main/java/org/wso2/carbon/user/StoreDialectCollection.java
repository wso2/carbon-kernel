package org.wso2.carbon.user;

import java.util.Collections;
import java.util.List;

public class StoreDialectCollection {

	private ClaimManager claimManager;
	private StoreIdentifier storeIdentifier;

	/**
	 * 
	 * @param storeIdentifier
	 * @param claimManager
	 */
	public StoreDialectCollection(StoreIdentifier storeIdentifier, ClaimManager claimManager) {
		this.claimManager = claimManager;
		this.storeIdentifier = storeIdentifier;
	}

	/**
	 * 
	 * @param dialectIdentifier
	 * @param claimIdentifier
	 * @return
	 */
	public AttributeIdentifier getAttributeIdentifier(DialectIdentifier dialectIdentifier,
			ClaimIdentifier claimIdentifier) {
		return claimManager.getAttributeIdentifier(dialectIdentifier, claimIdentifier,
				storeIdentifier);
	}

	/**
	 * 
	 * @param dialectIdentifier
	 * @return
	 */
	public List<AttributeIdentifier> getAllAttributeIdentifiers(DialectIdentifier dialectIdentifier) {
		List<AttributeIdentifier> attrIdentifiers = claimManager.getAllAttributeIdentifiers(
				dialectIdentifier, storeIdentifier);
		return Collections.unmodifiableList(attrIdentifiers);
	}

}
