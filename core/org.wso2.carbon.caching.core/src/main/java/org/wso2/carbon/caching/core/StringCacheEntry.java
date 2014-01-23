package org.wso2.carbon.caching.core;

import java.io.Serializable;

public class StringCacheEntry extends CacheEntry implements Serializable{

	private static final long serialVersionUID = 4934315902526774331L;

	private String value;
	
	public StringCacheEntry(String value) {
		super();
		this.value = value;
	}

	public String getStringValue() {
		return value;
	}
	
	

}
