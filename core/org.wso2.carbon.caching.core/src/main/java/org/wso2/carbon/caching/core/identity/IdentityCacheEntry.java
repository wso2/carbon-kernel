/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.caching.core.identity;

import org.wso2.carbon.caching.core.CacheEntry;

import java.security.Key;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;

/**
 * Identity Cache entry which wraps the identity related cache entry values
 */
public class IdentityCacheEntry extends CacheEntry {

    private String cacheEntry;
    private Set<String> cacheEntrySet;
    private String[] cacheEntryArray;
    private int hashEntry;
    private long cacheInterval;
    private boolean cacheClearing;
    private Key secretKey;
    private Date date;    
    private static final long serialVersionUID = 3746964700806693258L;

    public IdentityCacheEntry(String cacheEntry) {
        this.cacheEntry = cacheEntry;
    }

    public IdentityCacheEntry(int hashEntry) {
        this.hashEntry = hashEntry;
    }

    public IdentityCacheEntry(boolean cacheClearing) {
        this.cacheClearing = cacheClearing;
    }

    public IdentityCacheEntry(String cacheEntry, long cacheInterval) {
        this.cacheEntry = cacheEntry;
        this.cacheInterval = cacheInterval;
    }

    public IdentityCacheEntry(String[] cacheEntryArray) {
        this.cacheEntryArray = Arrays.copyOf(cacheEntryArray, cacheEntryArray.length);
    }

    public IdentityCacheEntry(Set<String> cacheEntrySet) {
        this.cacheEntrySet = cacheEntrySet;
    }

    public IdentityCacheEntry(String cacheEntry, Key secretKey, Date date) {
        this.cacheEntry = cacheEntry;
        this.secretKey = secretKey;
        this.date = date;
    }

    public String getCacheEntry() {
        return cacheEntry;
    }

    public int getHashEntry() {
        return hashEntry;
    }

    public long getCacheInterval() {
        return cacheInterval;
    }

    public Set<String> getCacheEntrySet() {
        return cacheEntrySet;
    }

    public boolean isCacheClearing() {
        return cacheClearing;
    }

    public String[] getCacheEntryArray() {
        return cacheEntryArray;
    }

    public Key getSecretKey() {
        return secretKey;
    }

    public Date getDate() {
        return date;
    }
}
