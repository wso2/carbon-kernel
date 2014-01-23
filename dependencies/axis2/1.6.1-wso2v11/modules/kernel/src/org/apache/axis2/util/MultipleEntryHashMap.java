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

package org.apache.axis2.util;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

/**
 * This will make a hash map which can contain multiple entries for the same hash value.
 */
public class MultipleEntryHashMap {

    private Map table;

    public MultipleEntryHashMap() {
        this.table = new Hashtable(1);
    }

    /**
     * If you call get once in this, it will remove that item from the map
     *
     * @param key
     * @return
     */
    public Object get(Object key) {
        ArrayList list = (ArrayList) table.get(key);
        if (list != null && list.size() > 0) {
            Object o = list.get(0);
            list.remove(0);
//            if (list.size() == 0) {
//                table.remove(key);
//            }
            return o;
        }

        return null;

    }

    public Object put(Object key, Object value) {
        ArrayList list = (ArrayList) table.get(key);
        if (list == null) {
            ArrayList listToBeAdded = new ArrayList();
            table.put(key, listToBeAdded);
            listToBeAdded.add(value);
        } else {
            list.add(value);
        }

        return value;
    }

    public Set keySet() {

        return table.keySet();
    }
}
