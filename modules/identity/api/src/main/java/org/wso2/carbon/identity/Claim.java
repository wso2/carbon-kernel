package org.wso2.carbon.user;

/**
 * 
 * A statement that one subject makes about itself or another subject. For
 * example, the statement can be about a name, identity, key, group, privilege,
 * or capability. Claims are issued by a provider, and they are given one or
 * more values and then packaged in security tokens that are issued by a
 * security token service (STS). They are also defined by a claim value type
 * and, possibly, associated meta-data.
 * 
 */
public class Claim {

	private ClaimIdentifier claimUri;
	@SuppressWarnings("rawtypes")
	private AttributeValue attributeValue;

	/**
	 * 
	 * @param claimUri
	 * @param attributeValue
	 */
	@SuppressWarnings("rawtypes")
	public Claim(ClaimIdentifier claimUri, AttributeValue attributeValue) {
		this.claimUri = claimUri;
		this.attributeValue = attributeValue;
	}

	/**
	 * 
	 * @return
	 */
	public ClaimIdentifier getClaimUri() {
		return claimUri;
	}

	/**
	 * 
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public AttributeValue getAttribute() {
		return attributeValue;
	}

}