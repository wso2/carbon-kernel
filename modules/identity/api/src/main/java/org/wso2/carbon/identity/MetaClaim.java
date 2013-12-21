package org.wso2.carbon.user;

import java.util.Collections;
import java.util.Map;

public class MetaClaim {

	private ClaimIdentifier claimIdentifier;
	private Map<String, AttributeIdentifier> attributeIdentifierMap;
	private Map<String, String> properties;

	/**
	 * 
	 * @param claimIdentifier
	 * @param attributeIdentifierMap
	 */
	public MetaClaim(ClaimIdentifier claimIdentifier,
			Map<String, AttributeIdentifier> attributeIdentifierMap) {
		this.claimIdentifier = claimIdentifier;
		this.attributeIdentifierMap = attributeIdentifierMap;
	}

	/**
	 * 
	 * @param claimUri
	 * @param attributeIdentifierMap
	 * @param properties
	 */
	public MetaClaim(ClaimIdentifier claimUri,
			Map<String, AttributeIdentifier> attributeIdentifierMap, Map<String, String> properties) {
		this.claimIdentifier = claimUri;
		this.attributeIdentifierMap = attributeIdentifierMap;
		this.properties = properties;
	}

	/**
	 * 
	 * @param storeIdentifier
	 * @return
	 */
	public AttributeIdentifier getAttributeIdentifier(StoreIdentifier storeIdentifier) {
		return attributeIdentifierMap.get(storeIdentifier.getValue());
	}

	/**
	 * 
	 * @return
	 */
	public Map<String, AttributeIdentifier> getAttributeIdentifierMap() {
		return Collections.unmodifiableMap(attributeIdentifierMap);
	}

	/**
	 * 
	 * @return
	 */
	public ClaimIdentifier getClaimIdentifier() {
		return claimIdentifier;
	}

	/**
	 * 
	 * @return
	 */
	public Map<String, String> getProperties() {
		return Collections.unmodifiableMap(properties);
	}
}