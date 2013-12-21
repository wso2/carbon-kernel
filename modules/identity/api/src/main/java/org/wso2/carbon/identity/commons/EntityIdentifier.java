package org.wso2.carbon.identity.commons;

public class EntityIdentifier {

	private String value;

	/**
	 * 
	 * @param value
	 */
	public EntityIdentifier(String value) {
		this.value = value;
	}

	/**
	 * 
	 * @return
	 */
	public String getValue() {
		return value;
	}
}
