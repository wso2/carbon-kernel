package org.wso2.carbon.caching.core;

import java.io.Serializable;

public class StringCacheKey extends CacheKey implements Serializable{
	
	private static final long serialVersionUID = 6617405572569314103L;
	
	private String username;
	
	public StringCacheKey(String username) {
		super();
		this.username = username;
	}

    public String getKeyValue(){
        return this.username;
    }

	@Override
	public boolean equals(Object ob) {
		if (!(ob instanceof StringCacheKey)) {
			return false;
		}
		if (username.equals(((StringCacheKey)ob).getKeyValue())) {
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return username.hashCode();
	}

}
