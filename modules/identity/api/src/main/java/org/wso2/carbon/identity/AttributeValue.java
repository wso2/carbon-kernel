package org.wso2.carbon.user;

import java.util.Collections;
import java.util.List;

public class AttributeValue<T> {

	private T value;
	private List<T> values;

	/**
	 * 
	 * @param value
	 */
	public AttributeValue(T value) {
		this.value = value;
	}

	/**
	 * 
	 * @return
	 */
	public T getValue() {
		return value;
	}

	/**
	 * 
	 * @return
	 */
	public List<T> getValues() {
		return Collections.unmodifiableList(values);
	}

}
