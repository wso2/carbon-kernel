/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.transport.http;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Pass-Thru / delayed get and put of the values from HttpServletRequest.
 */
public class TransportHeaders implements Map<String,String> {
    
	HttpServletRequest request;
    
	Map<String, String> localHeaderMap = new HashMap<String,String>();
	
	private boolean localDataPopulated;
	
    public TransportHeaders(HttpServletRequest request) {
        this.request = request;
    }
    
    private void populateLocalData() {
    	@SuppressWarnings("unchecked")
    	Enumeration<String> headers = (Enumeration<String>) this.getRequest().getHeaderNames();
    	/* the headers may not be given by the container as well, where it would be null */
    	if (headers != null) {
    		this.localHeaderMap = new HashMap<String, String>();
    		String name;
    	    while (headers.hasMoreElements()) {
    		    name = headers.nextElement();
    		    /* headers are not case sensitive, so we treat everything in lower-case */
    		    this.localHeaderMap.put(name.toLowerCase(), this.getRequest().getHeader(name));
    	    }
    	    this.localDataPopulated = true;
    	} else {
    		/* this inability of the container must be notified to the outside */
    		throw new RuntimeException("The HTTP request header names cannot be enumerated");
    	}
    }
    
    public HttpServletRequest getRequest() {
    	return request;
    }
    
    public boolean isLocalDataPopulated() {
    	return localDataPopulated;
    }
    
    public Map<String, String> getLocalData() {
    	return localHeaderMap;
    }
    
    public void checkAndPopulatedLocalData() {
    	if (!this.isLocalDataPopulated()) {
    		this.populateLocalData();
    	}
    }

	public int size() {
		this.checkAndPopulatedLocalData();
		return this.getLocalData().size();
	}

	public boolean isEmpty() {
		return this.size() == 0;
	}

	public boolean containsKey(Object key) {
		this.checkAndPopulatedLocalData();
		return this.getLocalData().containsKey(key.toString().toLowerCase());
	}

	public boolean containsValue(Object value) {
		this.checkAndPopulatedLocalData();
		return this.getLocalData().containsValue(value);
	}

	public String get(Object key) {
		if (this.isLocalDataPopulated()) {
			return this.getLocalData().get(key.toString().toLowerCase());
		} else {
			return this.getRequest().getHeader(key.toString());
		}
	}

	public String put(String key, String value) {
		this.checkAndPopulatedLocalData();
		return this.getLocalData().put(key.toLowerCase(), value);
	}

	public String remove(Object key) {
		this.checkAndPopulatedLocalData();
		return this.getLocalData().remove(key.toString().toLowerCase());
	}

	public void putAll(Map<? extends String, ? extends String> map) {
		this.checkAndPopulatedLocalData();
		for (Entry<? extends String, ? extends String> entry : map.entrySet()) {
			this.put(entry.getKey(), entry.getValue());
		}
	}

	public void clear() {
		this.checkAndPopulatedLocalData();
		this.getLocalData().clear();
	}

	public Set<String> keySet() {
		this.checkAndPopulatedLocalData();
		return this.getLocalData().keySet();
	}

	public Collection<String> values() {
		this.checkAndPopulatedLocalData();
		return this.getLocalData().values();
	}

	public Set<java.util.Map.Entry<String, String>> entrySet() {
		this.checkAndPopulatedLocalData();
		return this.getLocalData().entrySet();
	}

}
