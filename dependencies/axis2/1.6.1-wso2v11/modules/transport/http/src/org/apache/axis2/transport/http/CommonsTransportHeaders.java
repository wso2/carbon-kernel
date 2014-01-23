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

import org.apache.commons.httpclient.Header;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CommonsTransportHeaders implements Map {
    private Header[] headers;

    HashMap headerMap = null;

    public CommonsTransportHeaders(Header[] headers) {
        this.headers = headers;
    }

    private void init() {
        headerMap = new HashMap();

        for (int i = 0; i < headers.length; i++) {
            headerMap.put(headers[i].getName(), headers[i].getValue());
        }
    }

    public int size() {
        if (headerMap == null) {
            init();
        }
        return headerMap.size();
    }

    public void clear() {
        if (headerMap != null) {
            headerMap.clear();
        }
    }

    public boolean isEmpty() {
        if (headerMap == null) {
            init();
        }
        return headerMap.isEmpty();
    }

    public boolean containsKey(Object key) {
        if (headerMap == null) {
            init();
        }
        return headerMap.containsKey(key);
    }

    public boolean containsValue(Object value) {
        if (headerMap == null) {
            init();
        }
        return headerMap.containsValue(value);
    }

    public Collection values() {
        if (headerMap == null) {
            init();
        }
        return headerMap.values();
    }

    public void putAll(Map t) {
        if (headerMap == null) {
            init();
        }
        headerMap.putAll(t);
    }

    public Set entrySet() {
        if (headerMap == null) {
            init();
        }
        return headerMap.entrySet();
    }

    public Set keySet() {
        if (headerMap == null) {
            init();
        }
        return headerMap.keySet();
    }

    public Object get(Object key) {
        if (headerMap == null) {
            init();
        }
        return headerMap.get(key);
    }

    public Object remove(Object key) {
        if (headerMap == null) {
            init();
        }
        return headerMap.remove(key);
    }

    public Object put(Object key, Object value) {
        if (headerMap == null) {
            init();
        }
        return headerMap.put(key, value);
    }
}
