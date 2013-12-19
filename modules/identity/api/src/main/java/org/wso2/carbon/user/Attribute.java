package org.wso2.carbon.user;

public class Attribute {

	private AttributeIdentifier attributeIdentifier;
	@SuppressWarnings("rawtypes")
	private AttributeValue attributeValue;

	/**
	 * 
	 * @param attributeIdentifier
	 * @param attributeValue
	 */
	@SuppressWarnings("rawtypes")
	public Attribute(AttributeIdentifier attributeIdentifier, AttributeValue attributeValue) {
		this.attributeValue = attributeValue;
		this.attributeIdentifier = attributeIdentifier;
	}

	/**
	 * 
	 * @return
	 */
	public AttributeIdentifier getAttributeIdentifier() {
		return attributeIdentifier;
	}

	/**
	 * 
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public AttributeValue getAttributeValue() {
		return attributeValue;
	}

}