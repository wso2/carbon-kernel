/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/**
 * Implement a WeakReference key to be used in a collection.  Being a WeakReference, it will not
 * prevent the key from being Garbage Collected.  The key can only be created with a reference queue
 * so that users of this class provide cleanup logic which uses the items in the reference queue to
 * cleanup entries in the collection.
 * 
 * Note that the ReferenceQueue will contain the WeakKey instance that assocaited with the
 * referent that was GC'd.  So, the elements on the ReferenceQueue can be used to directly access
 * and remove entries in the collection it is a key for.  For example, one could do something
 * like the following in cleanup logic:
 *     Object gcKey = null;
 *     while ((gcKey = q.poll()) != null) {
 *         WeakKey wk = (WeakKey) gcKey;
 *         <collection value type> removedEntry = collection.remove(wk);
 *         ...
 *     }
 */
public class WeakKey extends WeakReference<Object> {
    // The hashcode for this object will be based on the key object it is created with 
    private int keyHashCode = 0;

    // This constructor is private to prevent creating a key without a ReferenceQueue
    private WeakKey(Object key) {
        super(key);
        keyHashCode = calculateHashCode(key);
    }

    public WeakKey(Object key, ReferenceQueue q) {
        super(key, q);
        keyHashCode = calculateHashCode(key);
    }
    
    private static int calculateHashCode(Object r) {
        return r != null ? r.hashCode() : 0;
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (this == o) {
            return true;
        } else if (!(o instanceof WeakKey)) {
            return false;
        } else {
            WeakKey checkIt = (WeakKey) o;
            if (checkIt.get() != null && checkIt.get().equals(this.get())) {
                return true;
            } else {
                return false;
            }
        }
    }

    public int hashCode() {
        return keyHashCode;
    }
    
    /**
     * Return an instance of WeakKey that can be used in comparsion operations.  For example, it
     * can be used to lookup a key in a collection that has a WeakKey as the key.
     * @param checkKey the key value
     * @return an instance of WeakKey for the value
     */
    public static WeakKey comparisonKey(Object checkKey) {
        return new WeakKey(checkKey);
    }
}
