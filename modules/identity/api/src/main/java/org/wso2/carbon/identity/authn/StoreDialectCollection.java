package org.wso2.carbon.identity.authn;

import java.util.Collections;
import java.util.List;

import org.wso2.carbon.identity.claim.ClaimIdentifier;
import org.wso2.carbon.identity.claim.ClaimManager;
import org.wso2.carbon.identity.claim.DialectIdentifier;
import org.wso2.carbon.identity.commons.AttributeIdentifier;

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
