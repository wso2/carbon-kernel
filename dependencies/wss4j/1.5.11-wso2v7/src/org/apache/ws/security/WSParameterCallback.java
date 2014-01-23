package org.apache.ws.security;

import javax.security.auth.callback.Callback;

public class WSParameterCallback implements Callback {

	public static final int KDC_DES_AES_FACTOR = 0;
	public static final int SERVICE_PRINCIPLE_PASSWORD = 1;
	private int property;
	private String stringValue;
	private int intValue;

	public WSParameterCallback(int property) {
		this.property = property;
	}

	public int getProperty() {
		return property;
	}

	public void setProperty(int property) {
		this.property = property;
	}

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	public int getIntValue() {
		return intValue;
	}

	public void setIntValue(int intValue) {
		this.intValue = intValue;
	}
}
